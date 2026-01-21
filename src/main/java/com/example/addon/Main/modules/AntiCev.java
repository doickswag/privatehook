package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AntiCev extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to face the block when placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swings your hand when placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables the module after successfully placing a crystal.")
        .defaultValue(true)
        .build()
    );

    public AntiCev() {
        super(Hook.CATEGORY, "AntiCev", "throwing said i coulnt make a blocker");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof BlockBreakingProgressS2CPacket packet)) return;
        if (mc.world == null || mc.player == null) return;

        BlockPos headPos = mc.player.getBlockPos().up();
        BlockPos minedBlockPos = packet.getPos();

        List<BlockPos> protectedBlocks = new ArrayList<>();
        protectedBlocks.add(headPos);
        protectedBlocks.add(headPos.north());
        protectedBlocks.add(headPos.south());
        protectedBlocks.add(headPos.east());
        protectedBlocks.add(headPos.west());

        if (!protectedBlocks.contains(minedBlockPos)) return;

        Entity entity = mc.world.getEntityById(packet.getEntityId());
        if (!(entity instanceof PlayerEntity) || entity == mc.player) return;

        FindItemResult crystals = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystals.found()) {
            warning("No End Crystals in hotbar, disabling.");
            toggle();
            return;
        }

        Direction[] checkDirections = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        };

        for (Direction direction : checkDirections) {
            BlockPos adjacentBlock = minedBlockPos.offset(direction);
            BlockPos crystalPos = adjacentBlock.up();
            if (!mc.world.getBlockState(adjacentBlock).isAir() && BlockUtils.canPlace(crystalPos)) {
                placeCrystal(crystals, crystalPos);
                return;
            }
        }

        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!obsidian.found()) {
            return;
        }

        for (Direction direction : checkDirections) {
            BlockPos placePos = minedBlockPos.offset(direction);
            BlockPos crystalPos = placePos.up();
            if (BlockUtils.canPlace(placePos) && BlockUtils.canPlace(crystalPos)) {
                BlockUtils.place(placePos, obsidian, rotate.get(), 0, swing.get());
                placeCrystal(crystals, crystalPos);
                return;
            }
        }
    }

    private void placeCrystal(FindItemResult crystal, BlockPos crystalPos) {
        BlockUtils.place(crystalPos, crystal, rotate.get(), 0, swing.get());
        if (autoDisable.get()) {
            info("robot");
            toggle();
        }
    }
}
