package com.example.addon.Main.modules;

import com.example.addon.Api.util.*;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import com.google.common.collect.ImmutableSet;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class WebAura extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");
    private final SettingGroup sgRender = settings.createGroup("Renders");

    public enum WebMode { HEAD, FEET, BOTH, BELOW }

    private final Setting<WebMode> webMode = sgGeneral.add(new EnumSetting.Builder<WebMode>()
        .name("placementmode")
        .description("Where to place webs on the target.")
        .defaultValue(WebMode.BOTH)
        .build()
    );

    private final Setting<Boolean> antiHole = sgGeneral.add(new BoolSetting.Builder()
        .name("antihole")
        .description("Cancel placing webs if target is inside a hole.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> prediction = sgAdvanced.add(new BoolSetting.Builder()
        .name("predictholeleave")
        .description("If in hole, predict exit location and web there.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> predictRange = sgAdvanced.add(new IntSetting.Builder()
        .name("predictionrange")
        .description("Range to search for exit positions when predicting.")
        .defaultValue(3)
        .range(1, 10)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> elytraJump = sgGeneral.add(new BoolSetting.Builder()
        .name("elytrajumpmode")
        .description("When target is webbed and elytra jumping, place webs ahead.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> advanceDistance = sgGeneral.add(new IntSetting.Builder()
        .name("advancedistance")
        .description("Number of webs to place in front when elytra jumping.")
        .defaultValue(3)
        .range(1, 10)
        .sliderMax(10)
        .build()
    );

    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("placedelay")
        .description("Delay between web placements (ms).")
        .defaultValue(50)
        .range(0, 500)
        .sliderMax(200)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blockspertick")
        .description("Max webs to place per tick.")
        .defaultValue(1)
        .range(1, 5)
        .sliderMax(5)
        .build()
    );

    private final Setting<Boolean> bottom = sgGeneral.add(new BoolSetting.Builder()
        .name("webbottom")
        .description("Place a web below the target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> bottomOnlyInHole = sgGeneral.add(new BoolSetting.Builder()
        .name("bottom-only-holes")
        .description("Only place bottom web if target is in a hole.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> antiAntiWeb = sgAdvanced.add(new BoolSetting.Builder()
        .name("antiantiweb")
        .description("")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> antiflygetaway = sgAdvanced.add(new BoolSetting.Builder()
        .name("antifly")
        .description("when the oppa is trapped in autotrapped it will place the web on their head of ontop of the trap")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> movepredict = sgAdvanced.add(new BoolSetting.Builder()
        .name("movepredict")
        .description("predicts movement of the player using exploration")
        .defaultValue(false)
        .build()
    );
    //--------------------Extrapolation--------------------//
    private final Setting<Integer> selfExt = sgAdvanced.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description("How many ticks of movement should be predicted for self damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extrapolation = sgAdvanced.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgAdvanced.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The maximum distance to target players.")
        .defaultValue(4)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the webs when placing.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> render = this.setting("render", "Renders the block where it is placing a crystal.", Boolean.valueOf(false), this.sgRender);
    private final Setting<Integer> renderTime = this.setting("render-time", "Ticks to render the block for.", Integer.valueOf(8), this.sgRender, this.render::get);
    private final Setting<Boolean> fade = this.setting("fade", "Will reduce the opacity of the rendered block over time.", Boolean.valueOf(true), this.sgRender, this.render::get);
    private final Setting<ShapeMode> shapeMode = this.setting("shape-mode", "How the shapes are rendered.", ShapeMode.Both, this.sgRender, this.render::get);
    private final Setting<SettingColor> sideColor = this.setting("side-color", "The side color.", 0, 0, 255, 10, this.sgRender, this.render::get);
    private final Setting<SettingColor> lineColor = this.setting("line-color", "The line color.", 0, 0, 255, 200, this.sgRender, this.render::get);
    private float lastFrameDurationMs = 16.66F;
    private long lastFrameTimeNanos = -1L;
    private final List<RenderBlock> renderBlocks = Collections.synchronizedList(new ArrayList<>());

    private static final Set<Item> INTERFERING_ITEMS = ImmutableSet.of(
        Items.COBWEB, Items.STONE_BUTTON, Items.ACACIA_BUTTON,
        Items.BAMBOO_BUTTON, Items.BIRCH_BUTTON, Items.CHERRY_BUTTON,
        Items.CRIMSON_BUTTON, Items.DARK_OAK_BUTTON, Items.MANGROVE_BUTTON,
        Items.JUNGLE_BUTTON, Items.SPRUCE_BUTTON, Items.POLISHED_BLACKSTONE_BUTTON,
        Items.WARPED_BUTTON, Items.OAK_BUTTON, Items.TORCH,
        Items.REDSTONE_TORCH, Items.ENDER_CHEST
    );

    private PlayerEntity target = null;
    private long lastPlaceTime = 0;
    private int placedThisTick = 0;
    private final CalculationUtil calcUtil = new CalculationUtil();
    private final Map<AbstractClientPlayerEntity, Box> extMap = new HashMap<>();

    public WebAura() {
        super(Hook.CATEGORY, "WebAura", "Automatically places webs on other players.");
    }

    @Override
    public void onActivate() {
        calcUtil.reset();
        this.renderBlocks.clear();
        synchronized (this.renderBlocks) {
            this.renderBlocks.clear();
        }
    }

    private void updateTarget() {
        if (TargetUtils.isBadTarget(target, range.get())) {
            target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        }
        if (target != null && Friends.get().isFriend(target)) {
            target = null;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        placedThisTick = 0;
        synchronized (this.renderBlocks) {
            RenderBlock.tick(this.renderBlocks);
        }
        if (mc.world == null || mc.player == null) return;
        updateTarget();
        if (target == null) return;
        BlockPos targetPos;
        if (movepredict.get()) {
            extMap.clear();
            extMap.put((AbstractClientPlayerEntity) target, target.getBoundingBox());
            ExtrapolationUtils.extrapolateMap(extMap, player -> player == mc.player ? selfExt.get() : extrapolation.get(), player -> extSmoothness.get());
            targetPos = BlockPos.ofFloored(extMap.get(target).getCenter());
        } else {
            targetPos = target.getBlockPos();
        }
        RunLogic(targetPos);
    }

    private void RunLogic(BlockPos targetPos) {
        if (antiHole.get() && Wrapper.isInHole(target)) return;
        if (antiAntiWeb.get() && handleAntiAntiWeb(targetPos)) return;
        if (prediction.get() && Wrapper.isInHole(target) && handleHolePrediction(targetPos)) return;
        if (antiflygetaway.get() && handleElytraTakeoff(targetPos)) return;
        if (elytraJump.get() && isWebbed(target) && wearingElytra(target) && handleElytraJump(targetPos)) return;
        if (bottom.get() && handleBottomPlacement(targetPos)) return;
        handleDefaultPlacement(targetPos);
    }

    private boolean placeWeb(BlockPos pos) {
        if (placedThisTick >= blocksPerTick.get()) return false;
        long now = System.currentTimeMillis();
        if (now - lastPlaceTime < placeDelay.get()) return false;
        if (this.render.get()) {
            synchronized (this.renderBlocks) {
                this.renderBlocks.add(new RenderBlock(pos, this.renderTime.get()));
            }
        }
        if (BlockUtils.place(pos, InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 0, false)) {
            lastPlaceTime = now;
            placedThisTick++;
            calcUtil.reset();
            return true;
        }

        return false;
    }

    private BlockPos predictExit(BlockPos start, int range) {
        Direction facing = Direction.fromRotation(target.getYaw());
        for (int i = 1; i <= range; i++) {
            BlockPos checkPos = start.offset(facing, i);
            if (mc.world.getBlockState(checkPos).isAir() && mc.world.getBlockState(checkPos.up()).isAir()) {
                if (BlockUtils.canPlace(checkPos)) return checkPos;
            }
        }
        return null;
    }

    private boolean isWebbed(PlayerEntity p) {
        return GANGTIL.isWebbed(p);
    }

    private boolean wearingElytra(PlayerEntity p) {
        return p.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    private boolean isInterfering(BlockPos pos) {
        return INTERFERING_ITEMS.contains(mc.world.getBlockState(pos).getBlock().asItem());
    }

    private boolean handleAntiAntiWeb(BlockPos targetPos) {
        if (isInterfering(targetPos)) GANGTIL.doPacketMine(targetPos);
        if (isInterfering(targetPos.up())) GANGTIL.doPacketMine(targetPos.up());
        return false;
    }

    private boolean handleHolePrediction(BlockPos targetPos) {
        BlockPos exit = predictExit(targetPos, predictRange.get());
        return exit != null && placeWeb(exit);
    }

    private boolean handleElytraTakeoff(BlockPos targetPos) {
        if (wearingElytra(target)) {
            return placeWeb(targetPos.up(2));
        }
        return false;
    }

    private boolean handleElytraJump(BlockPos targetPos) {
        if (target.getVelocity().y < 0.1) return false;
        Vec3d dir = target.getRotationVec(1.0f);
        boolean placed = false;
        for (int i = 1; i <= advanceDistance.get(); i++) {
            BlockPos p = targetPos.add((int) Math.floor(dir.x * i), 0, (int) Math.floor(dir.z * i));
            if (placeWeb(p)) placed = true;
            if (placedThisTick >= blocksPerTick.get()) break;
        }
        return placed;
    }

    private boolean handleBottomPlacement(BlockPos targetPos) {
        if (bottomOnlyInHole.get() && !Wrapper.isInHole(target)) return false;
        BlockPos downPos = targetPos.down();
        if (mc.world.getBlockState(downPos).isAir()) {
            placeWeb(downPos);
            return placeWeb(targetPos);
        }
        return false;
    }

    private void handleDefaultPlacement(BlockPos targetPos) {
        switch (webMode.get()) {
            case HEAD -> placeWeb(targetPos.up());
            case FEET -> placeWeb(targetPos);
            case BOTH -> {
                placeWeb(targetPos);
                placeWeb(targetPos.up());
            }
            case BELOW -> placeWeb(targetPos.down());
        }
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        long now = System.nanoTime();
        if (this.lastFrameTimeNanos != -1L) {
            this.lastFrameDurationMs = (float) (now - this.lastFrameTimeNanos) / 1_000_000.0F;
        }

        this.lastFrameTimeNanos = now;

        if (this.render.get() && !this.renderBlocks.isEmpty()) {
            synchronized (this.renderBlocks) {
                for (RenderBlock block : this.renderBlocks) {
                    block.render(event, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), this.fade.get());
                }
            }
        }
    }
    @Override
    public String getInfoString() {
        if (target != null) return EntityUtils.getName(target) + " " + calcUtil.getCalculationTime();
        return null;
    }
}
