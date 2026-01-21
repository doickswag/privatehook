package com.example.addon.Main.modules;

import com.example.addon.Api.util.Interaction;
import com.example.addon.Api.util.Wrapper;
import com.example.addon.Hook;
import com.example.addon.Main.modules.PacketMine.AutoBreak;
import com.example.addon.Main.modules.Targeting.AutoMine;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static com.example.addon.Api.util.GANGTIL.closestVec3d;
import static meteordevelopment.meteorclient.utils.player.InvUtils.findInHotbar;
import static meteordevelopment.meteorclient.utils.world.BlockUtils.canBreak;

public class AutoCev extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between break attempts.")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Faces the block being mined server side.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("packet")
        .description("Faces the block being mined server side.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders an overlay on the block being broken.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 255))
        .build()
    );
    // --- End of settings ---


    public final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, Integer.MIN_VALUE, 0);
    private int ticks;
    private Direction direction;

    public AutoCev() {
        super(Hook.CATEGORY, "AutoCev", "Instantly breaks a block, breaks a crystal on top, and places a new one.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
        blockPos.set(0, -1, 0);
        AutoMine.resetMinePos();
    }

    @Override
    public void onDeactivate() {
        AutoMine.resetMinePos();
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        direction = event.direction;
        blockPos.set(event.blockPos);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (ticks >= tickDelay.get()) {
            ticks = 0;
            if (mc.world.getBlockState(blockPos).isAir()) {
                breakCrystal();
            }
            if (shouldMine()) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                AutoMine.setMinePos(blockPos);
                FindItemResult crystalToPlace = findInHotbar(Items.END_CRYSTAL);
                if (crystalToPlace.found()) {
                    if (rotate.get()) Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos));
                    doPlace(crystalToPlace, blockPos);
                }
            }
        } else {
            ticks++;
        }
    }

    public boolean shouldMine() {
        if (mc.world.isOutOfHeightLimit(blockPos) || !canBreak(blockPos)) {
            return false;
        }
        return true;
    }

    private void breakCrystal() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal && entity.getBlockPos().equals(blockPos.up())) {
                if (rotate.get()) {
                    Rotations.rotate(Rotations.getYaw(crystal), Rotations.getPitch(crystal));
                }
                mc.interactionManager.attackEntity(mc.player, crystal);
                Wrapper.swingHand(false);
                return;
            }
        }
    }

    private void doPlace(FindItemResult itemResult, BlockPos blockPos) {
        if (blockPos == null || !itemResult.found()) return;
        Hand hand = itemResult.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (itemResult.isOffhand()) {
            mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(closestVec3d(blockPos), Direction.DOWN, blockPos, false));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
            return;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        Interaction.updateSlot(itemResult.slot(), packet.get());
        mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(closestVec3d(blockPos), Direction.DOWN, blockPos, false));
        Wrapper.swingHand(false);
        Interaction.swapBack(prevSlot);
    }
}
