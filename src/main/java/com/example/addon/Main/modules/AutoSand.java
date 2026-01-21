package com.example.addon.Main.modules;

import com.example.addon.Api.util.CalculationUtil;
import com.example.addon.Api.util.GANGTIL;
import com.example.addon.Api.util.BlockHelper;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import com.google.common.collect.ImmutableSet;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.example.addon.Api.util.GANGTIL.replaceable;
import static meteordevelopment.meteorclient.utils.entity.TargetUtils.getPlayerTarget;

public class AutoSand extends Hooked {
    private final SettingGroup sgSettings = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.getDefaultGroup();

    private final Setting<Double> range = sgSettings.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The maximum distance to target players.")
        .defaultValue(4)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );

    private final Setting<SortPriority> priority = sgSettings.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );

    private final Setting<Boolean> placeSand = sgSettings.add(new BoolSetting.Builder()
        .name("putting-sand")
        .description("Puts sand on the target's head.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> placeObsidian = sgSettings.add(new BoolSetting.Builder()
        .name("putting-obsidian")
        .description("Places obsidian around the target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgSettings.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards blocks when placing.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> airplace = sgSettings.add(new BoolSetting.Builder()
        .name("airplace")
        .description("airplaces sand")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> disableOnUse = sgSettings.add(new BoolSetting.Builder()
        .name("disable-on-use")
        .description("Disables the module after placing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> placeOnce = sgSettings.add(new BoolSetting.Builder()
        .name("place-once")
        .description("Only places sand once per toggle.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyInHole = sgSettings.add(new BoolSetting.Builder()
        .name("only-if-in-hole")
        .description("Only places sand if the enemy is in a hole.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> placeWeb = sgSettings.add(new BoolSetting.Builder()
        .name("place-web")
        .description("Places a cobweb at the target's head if sand is placed.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> placeWebMio = sgSettings.add(new BoolSetting.Builder()
        .name("place-web-Mio")
        .description("Enables mios autoweb")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> mine = sgSettings.add(new BoolSetting.Builder()
        .name("MINE")
        .description("Enables mios autoweb")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> webDelay = sgSettings.add(new IntSetting.Builder()
        .name("web-delay")
        .description("Delay (in ticks) before placing cobweb after sand placement.")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> AntiAntiAutoSand = sgSettings.add(new BoolSetting.Builder()
        .name("AntiAntiAutoSand")
        .description("Mines interfering blocks (e.g. cobwebs, buttons, torches) from inside the target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the marker.")
        .defaultValue(Color.MAGENTA)
        .build()
    );

    private final Setting<Boolean> renderEnabled = sgRender.add(new BoolSetting.Builder()
        .name("render-placed-blocks")
        .description("Renders placed sand and obsidian blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> scale = sgRender.add(new DoubleSetting.Builder()
        .name("render-scale")
        .description("The scale of the rendered box when 'slimmasks elite render' is on.")
        .defaultValue(1.0)
        .range(0.1, 2.0)
        .sliderRange(0.1, 2.0)
        .build()
    );
    private final Setting<Boolean> slimmaskrender = sgRender.add(new BoolSetting.Builder()
        .name("slimmasks elite render")
        .description("slims elit erender")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Time (in ticks) for placed block renders to fade out.")
        .defaultValue(20)
        .range(1, 100)
        .sliderMax(60)
        .build()
    );

    private static final Set<Block> INTERFERING_BLOCKS = ImmutableSet.of(
        Blocks.COBWEB,
        Blocks.STONE_BUTTON,
        Blocks.ACACIA_BUTTON,
        Blocks.BAMBOO_BUTTON,
        Blocks.BIRCH_BUTTON,
        Blocks.CHERRY_BUTTON,
        Blocks.CRIMSON_BUTTON,
        Blocks.DARK_OAK_BUTTON,
        Blocks.MANGROVE_BUTTON,
        Blocks.JUNGLE_BUTTON,
        Blocks.SPRUCE_BUTTON,
        Blocks.POLISHED_BLACKSTONE_BUTTON,
        Blocks.WARPED_BUTTON,
        Blocks.OAK_BUTTON,
        Blocks.TORCH,
        Blocks.REDSTONE_TORCH
    );

    private final Map<BlockPos, Integer> placedSand = new HashMap<>();
    private final Map<BlockPos, Integer> placedObsidian = new HashMap<>();
    private final Map<BlockPos, Integer> placedWebs = new HashMap<>();
    private final CalculationUtil calcUtil = new CalculationUtil();
    private PlayerEntity target = null;
    private boolean hasPlacedSand = false;
    private int webDelayTimer = 0;
    private boolean webPlacementPending = false;
    private BlockPos mineBlock;
    private float progress;
    private FindItemResult tool;
    private WebAura webAura;

    public AutoSand() {
        super(Hook.CATEGORY, "AutoSand", "Prevent opp holecamp type shyt");
    }

    @Override
    public void onActivate() {
        resetState();
        if (placeWebMio.get()) {
            if (webAura != null && !webAura.isActive()) {
                webAura.toggle();
            } else if (webAura == null) {
            }
        }
    }

    private void antiAntiAutoSand() {
        if (target == null) return;
        BlockPos feet = target.getBlockPos();
        Block interferingBlock = BlockHelper.getBlock(feet);
        if (isInterfering(interferingBlock)) {
            if (interferingBlock == Blocks.COBWEB) {
                int swordSlot = InvUtils.findInHotbar(Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.WOODEN_SWORD).slot();
                if (swordSlot != -1) {
                    GANGTIL.mineWeb(target, swordSlot);
                } else {
                    GANGTIL.doPacketMine(feet);
                }
            } else {
                GANGTIL.doPacketMine(feet);
            }
        }
    }

    private boolean isInterfering(Block block) {
        return INTERFERING_BLOCKS.contains(block);
    }

    private boolean isHoleBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL;
    }

    private boolean isTargetInHole() {
        if (target == null) return false;
        BlockPos pos = target.getBlockPos();
        return isHoleBlock(pos.add(1, 0, 0)) &&
            isHoleBlock(pos.add(-1, 0, 0)) &&
            isHoleBlock(pos.add(0, 0, 1)) &&
            isHoleBlock(pos.add(0, 0, -1)) &&
            isHoleBlock(pos.add(0, -1, 0));
    }

    private boolean placeSand() {
        if (target == null || mc.player == null || mc.world == null) return false;
        if (onlyInHole.get() && !isTargetInHole()) return false;
        if (placeOnce.get() && hasPlacedSand) return false;
        BlockPos sandPos = target.getBlockPos().up(2);
        if (!replaceable(sandPos)) {
            return false;
        }

        FindItemResult sandItem = InvUtils.findInHotbar(itemStack ->
            GANGTIL.CONCRETE_POWDERS.contains(itemStack.getItem())
        );
        if (!sandItem.found()) return false;
        boolean success;
        if (airplace.get()) {
            success = GANGTIL.airPlace(sandPos, Hand.MAIN_HAND, sandItem.slot(), rotate.get(), 0, false, true, false, true);
        } else {
            if (!BlockUtils.canPlace(sandPos, true)) {
                return false;
            }

            success = BlockUtils.place(
                sandPos,
                sandItem,
                rotate.get(),
                0,
                false
            );
        }
        if (success) {
            placedSand.put(sandPos, fadeTime.get());
            if (placeOnce.get()) hasPlacedSand = true;
            calcUtil.reset();
            if (placeWeb.get()) {
                webPlacementPending = true;
                webDelayTimer = webDelay.get();
            }
        }
        return success;
    }

    private boolean placeObsidian() {
        if (target == null) return false;
        BlockPos base = target.getBlockPos();
        boolean placedAny = false;
        for (BlockPos offset : new BlockPos[]{
            base.add(1, 0, 0), base.add(1, 1, 0), base.add(1, 2, 0)
        }) {
            FindItemResult obsidianItem = InvUtils.findInHotbar(Items.OBSIDIAN);
            if (!obsidianItem.found()) return placedAny;

            if (replaceable(offset)) {
                boolean didPlace = BlockUtils.place(offset, obsidianItem, rotate.get(), 0, false);
                if (didPlace) {
                    placedObsidian.put(offset, fadeTime.get());
                    placedAny = true;
                }
            }
        }
        if (placedAny) calcUtil.reset();
        return placedAny;
    }

    private boolean placeWeb() {
        if (target == null || !placeWeb.get() || mc.player == null || mc.world == null) return false;
        BlockPos headPos = target.getBlockPos().up();
        if (!replaceable(headPos)) return false;
        FindItemResult webItem = InvUtils.findInHotbar(Items.COBWEB);
        if (!webItem.found()) return false;
        boolean success = BlockUtils.place(
            headPos,
            webItem,
            rotate.get(),
            0,
            false
        );
        if (success) {
            placedWebs.put(headPos, fadeTime.get());
            calcUtil.reset();
        }
        return success;
    }

    private void handleMining() {
        if (mine.get()) {
            if (mineBlock == null) {
                for (int i = 0; i <= 2; i++) {
                    BlockPos pos = target.getBlockPos().up(i);
                    var state = mc.world.getBlockState(pos);
                    boolean isSandLike = GANGTIL.CONCRETE_POWDERS.contains(state.getBlock().asItem());
                    if (!replaceable(pos) && !isSandLike) {
                        mineBlock = pos;
                        tool = InvUtils.findFastestTool(state);
                        progress = 0f;
                        break;
                    }
                }
            }
            if (mineBlock != null) {
                if (tool == null || !tool.found()) {
                    tool = InvUtils.findFastestTool(mc.world.getBlockState(mineBlock));
                }
                if (mc.world.getBlockState(mineBlock).isAir() || tool == null || !tool.found()) {
                    mineBlock = null;
                    progress = 0f;
                    return;
                }
                mine(false, mineBlock, tool);
                progress += BlockUtils.getBreakDelta(tool.slot(), mc.world.getBlockState(mineBlock));
                if (progress >= 1.0f) {
                    mine(true, mineBlock, tool);
                    mineBlock = null;
                    progress = 0f;
                }
            }
        }
    }

    private void runPlacementLogic() {
        boolean placedThisTick = false;
        if (AntiAntiAutoSand.get()) antiAntiAutoSand();
        handleMining();
        if (mineBlock == null || !mine.get()) {
            if (placeObsidian.get() && this.placeObsidian()) placedThisTick = true;
            if (placeSand.get()) {
                if (this.placeSand()) placedThisTick = true;
            }
        }
        if (webPlacementPending) {
            if (webDelayTimer-- <= 0) {
                if (this.placeWeb()) placedThisTick = true;
                webPlacementPending = false;
            }
        }
        if (placeWebMio.get()) ChatUtils.sendPlayerMsg(";toggle WebAura");
        if (disableOnUse.get() && placedThisTick) toggle();
    }

    private void updateTarget() {
        PlayerEntity oldTarget = target;
        if (meteordevelopment.meteorclient.utils.entity.TargetUtils.isBadTarget(target, range.get())) {
            target = getPlayerTarget(range.get(), priority.get());
        }
        if (target != null && Friends.get().isFriend(target)) {
            target = null;
        }

        if (target != oldTarget) {
            calcUtil.reset();
            if (target != null) {
                calcUtil.calculate();
            }
        }
    }

    private void tickRenderMaps() {
        placedSand.replaceAll((pos, ticks) -> ticks - 1);
        placedSand.entrySet().removeIf(entry -> entry.getValue() <= 0);
        placedObsidian.replaceAll((pos, ticks) -> ticks - 1);
        placedObsidian.entrySet().removeIf(entry -> entry.getValue() <= 0);
        placedWebs.replaceAll((pos, ticks) -> ticks - 1);
        placedWebs.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        tickRenderMaps();
        updateTarget();
        if (target == null) return;
        runPlacementLogic();
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        if (!renderEnabled.get()) return;
        renderPlacedBlocks(event, placedSand);
        renderPlacedBlocks(event, placedObsidian);
        renderPlacedBlocks(event, placedWebs);
    }

    private void renderPlacedBlocks(Render3DEvent event, Map<BlockPos, Integer> map) {
        for (var entry : map.entrySet()) {
            float alpha = Math.max(0, entry.getValue() / (float) fadeTime.get());
            Color fadedColor = new Color(color.get().r, color.get().g, color.get().b, (int) (alpha * color.get().a));
            BlockPos pos = entry.getKey();
            if (slimmaskrender.get()) {
                Box marker = new Box(pos.down());
                marker = marker.stretch(0, 1, 0);
                double scaleValue = scale.get();
                if (scaleValue != 1.0) {
                    double expandX = (scaleValue - 1) * marker.getLengthX() / 2.0;
                    double expandY = (scaleValue - 1) * marker.getLengthY() / 3.0;
                    double expandZ = (scaleValue - 1) * marker.getLengthZ() / 2.0;
                    marker = marker.expand(expandX, expandY, expandZ);
                }
                event.renderer.box(marker, fadedColor, fadedColor, ShapeMode.Both, 0);
            } else {
                event.renderer.box(new Box(pos), fadedColor, fadedColor, ShapeMode.Both, 0);
            }
        }
    }

    private void mine(final boolean done, final BlockPos pos, final FindItemResult pick) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!pick.found()) return;
        if (this.rotate.get()) {
            Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), () -> sendMinePackets(done, pos, pick));
        } else {
            //sendMinePackets(done, pos, pick);
            MeteorClient.EVENT_BUS.post(StartBreakingBlockEvent.get(pos, Direction.UP));
        }
    }

    private void sendMinePackets(final boolean done, final BlockPos pos, final FindItemResult pick) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!pick.found()) return;

        final Direction direction = BlockUtils.getDirection(pos);
        if (direction == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        InvUtils.swap(pick.slot(), false);
        if (!done) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
        }
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
        if (done) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        InvUtils.swap(prevSlot, false);
    }

    @Override
    public String getInfoString() {
        if (target != null) {
            return EntityUtils.getName(target) + " " + calcUtil.getCalculationTime();
        }
        return null;
    }

    @Override
    public void onDeactivate() {
        resetState();
        if (placeWeb.get()) {
            if (webAura != null && webAura.isActive()) {
                webAura.toggle();
            }
        }
    }

    private void resetState() {
        target = null;
        hasPlacedSand = false;
        webPlacementPending = false;
        webDelayTimer = 0;
        calcUtil.reset();
        placedSand.clear();
        placedObsidian.clear();
        placedWebs.clear();
        mineBlock = null;
        progress = 0f;
        tool = null;
    }
}
