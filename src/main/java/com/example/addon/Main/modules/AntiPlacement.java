package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import com.google.common.collect.ImmutableSet;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import java.util.List;
import java.util.Set;

public class AntiPlacement extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyInHole = sgGeneral.add(new BoolSetting.Builder()
        .name("InHoleOnly")
        .description("Only functions when you are standing in a hole.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> fallingBlockSafety = sgGeneral.add(new BoolSetting.Builder()
        .name("AntiSand")
        .description("Places a torch if a falling block is predicted to hit you.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> antiweb = sgGeneral.add(new BoolSetting.Builder()
        .name("antiweb")
        .description("Places a torch in holes so no web.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Keybind> antimace = sgGeneral.add(new KeybindSetting.Builder()
        .name("AntiMace (broken)")
        .defaultValue(Keybind.none())
        .build()
    );
    private boolean breaking;

    public AntiPlacement() {
        super(Hook.CATEGORY, "Anti Placement", "Elite Teir shit");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (onlyInHole.get() && !PlayerUtils.isInHole(true))
            return;

        BlockPos head = mc.player.getBlockPos().up();

        if (mc.world.getBlockState(head).getBlock() instanceof BedBlock && !breaking) {
            Rotations.rotate(Rotations.getYaw(head), Rotations.getPitch(head), 50, () -> sendMinePackets(head));
            breaking = true;
        } else if (breaking) {
            Rotations.rotate(Rotations.getYaw(head), Rotations.getPitch(head), 50, () -> sendStopPackets(head));
            breaking = false;
        }

        if (fallingBlockSafety.get()) {
            detectAndHandleFallingBlock();
        }
        if (antiweb.get()) {
            if (!PlayerUtils.isInHole(true))
                return;
                antiweb();
        }
        if (antimace.get().isPressed()) {
            antimace();
        }
    }

    private void detectAndHandleFallingBlock() {
        double rangeHorizontal = 1.0;
        double rangeVertical = 3.0;
        double playerX = mc.player.getX();
        double playerY = mc.player.getY();
        double playerZ = mc.player.getZ();

        Box detectionBox = new Box(
            playerX - rangeHorizontal, playerY, playerZ - rangeHorizontal,
            playerX + rangeHorizontal, playerY + rangeVertical, playerZ + rangeHorizontal
        );

        assert mc.world != null;
        List<FallingBlockEntity> fallingBlocks = mc.world.getEntitiesByClass(FallingBlockEntity.class, detectionBox, entity -> true);

        for (FallingBlockEntity fallingBlock : fallingBlocks) {
            if (fallingBlock.getVelocity().y < 0) {
                BlockPos blockPos = fallingBlock.getBlockPos();
                BlockPos playerPos = mc.player.getBlockPos();

                if (blockPos.getX() == playerPos.getX() && blockPos.getZ() == playerPos.getZ()) {
                    place(playerPos);
                    place2(playerPos);
                    break;
                }
            }
        }
    }
    private void antiweb() {
            BlockPos playerPos = mc.player.getBlockPos();
            place(playerPos);
            place2(playerPos);
    }
    private void antimace() {
        BlockPos playerPos = mc.player.getBlockPos();
        AntiMaceplace(playerPos);
    }
    private void AntiMaceplace(BlockPos blockPos) {
        BlockPos base = mc.player.getBlockPos();
        for (BlockPos offset : new BlockPos[]{
            base.add(1, 0, 0), base.add(1, 1, 0), base.add(1, 2, 0), base.add(1, 0, 1), base.add(1, 0, 2),base.add(1, 0, 3), base.add(1, 1, 1), base.add(1, 1, 2), base.add(1, 1, 3), base.add(1, 2, 1), base.add(1, 2, 2), base.add(1, 2, 3),
        })
        if (mc.world.getBlockState(blockPos).getBlock().asItem() != Items.OBSIDIAN) {
            BlockUtils.place(offset, InvUtils.findInHotbar(Items.OBSIDIAN), 50, false);
        }
    }
    private void place(BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos).getBlock().asItem() != Items.REDSTONE_TORCH) {
            BlockUtils.place(blockPos, InvUtils.findInHotbar(Items.REDSTONE_TORCH), 50, false);
        }
    }
    private void place2(BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos).getBlock().asItem() != Items.REDSTONE) {
            BlockUtils.place(blockPos, InvUtils.findInHotbar(Items.REDSTONE), 50, false);
        }
    }
    private void sendMinePackets(BlockPos blockPos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
    }

    private void sendStopPackets(BlockPos blockPos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private static final Set<Item> itemsneeded = ImmutableSet.of(
        Items.STONE_BUTTON,
        Items.OAK_BUTTON,
        Items.SPRUCE_BUTTON,
        Items.BIRCH_BUTTON,
        Items.JUNGLE_BUTTON,
        Items.ACACIA_BUTTON,
        Items.DARK_OAK_BUTTON,
        Items.MANGROVE_BUTTON,
        Items.CHERRY_BUTTON,
        Items.CRIMSON_BUTTON,
        Items.WARPED_BUTTON,
        Items.BAMBOO_BUTTON,
        Items.POLISHED_BLACKSTONE_BUTTON,
        Items.TORCH,
        Items.REDSTONE_TORCH,
        Items.REDSTONE
    );
}
