package com.example.addon.Main.modules.Targeting;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import com.example.addon.Main.modules.PacketMine.AutoBreak;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.List;

public class AutoMine extends Hooked {

    public enum MineMode {
        TARGETING("Targeting"),
        SELECT("Select");

        private final String title;

        MineMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMining = settings.createGroup("Main");
    private final SettingGroup sgBehavior = settings.createGroup("Extra");

    private final Setting<MineMode> mineMode = sgGeneral.add(new EnumSetting.Builder<MineMode>()
        .name("mine-mode")
        .description("Which mining logic to use.")
        .defaultValue(MineMode.TARGETING)
        .build());

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum distance to target players.")
        .defaultValue(5)
        .min(0)
        .sliderMax(7)
        .build());

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build());

    private final Setting<FazeAdapt> Adapt = sgMining.add(new EnumSetting.Builder<FazeAdapt>()
        .name("logic")
        .description("The logic for selecting which blocks to mine.")
        .defaultValue(FazeAdapt.Basic)
        .build()
    );

    private final Setting<Boolean> mineBurrow = sgMining.add(new BoolSetting.Builder()
        .name("mine-burrow")
        .description("Mines the block the target is standing in.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> mineHead = sgMining.add(new BoolSetting.Builder()
        .name("mine-head")
        .description("Mines the block at the target's head.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> mineSurround = sgMining.add(new BoolSetting.Builder()
        .name("mine-surround")
        .description("Mines the blocks surrounding the target.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> instant = sgBehavior.add(new BoolSetting.Builder()
        .name("Civ")
        .description("Mines without any delay.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> support = sgBehavior.add(new BoolSetting.Builder()
        .name("support")
        .description("Places a support block if needed before mining.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> rotate = sgBehavior.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the block being mined or placed.")
        .defaultValue(true)
        .build());

    private PlayerEntity target;
    private AutoBreak packetMine;
    private int delayLeft = 0;
    private int surroundIndex = 0;
    private final BlockSelector blockSelector = new BlockSelector();

    private static BlockPos selectedBlock = null;

    public AutoMine() {
        super(Hook.CATEGORY, "Targeting", "Automatically mines blocks around a target.");
    }

    public static void setMinePos(BlockPos pos) {
        selectedBlock = pos;
    }

    public static void resetMinePos() {
        selectedBlock = null;
    }

    @Override
    public void onActivate() {
        packetMine = Modules.get().get(AutoBreak.class);
        if (packetMine == null) {
            error("PacketMine module not found!");
            toggle();
            return;
        }
        if (!packetMine.isActive()) {
            error("PacketMine is not enabled!");
        }

        target = null;
        delayLeft = 0;
        surroundIndex = 0;
        resetMinePos();
    }

    @Override
    public void onDeactivate() {
        resetMinePos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (delayLeft > 0) {
            delayLeft--;
            return;
        }

        if (!packetMine.isActive() || packetMine.isMining()) return;

        if (mineMode.get() == MineMode.SELECT) {
            if (selectedBlock != null) {
                tryMineBlock(selectedBlock);
            }
        } else {
            if (TargetUtils.isBadTarget(target, range.get())) {
                target = TargetUtils.getPlayerTarget(range.get(), priority.get());
                if (target != null) {
                    surroundIndex = 0;
                }
            }

            if (target == null || Friends.get().isFriend(target)) {
                target = null;
                return;
            }

            List<BlockPos> blocksToMine = blockSelector.getBlocks(
                target, Adapt.get(), mineBurrow.get(), mineHead.get(), mineSurround.get(), surroundIndex
            );

            for (BlockPos pos : blocksToMine) {
                if (tryMineBlock(pos)) {
                    if (Adapt.get() == FazeAdapt.FAZEADAPT) {
                        BlockPos targetPos = target.getBlockPos();
                        List<BlockPos> clockwise = Arrays.asList(
                            targetPos.north(),
                            targetPos.east(),
                            targetPos.south(),
                            targetPos.west()
                        );
                        int minedIndex = clockwise.indexOf(pos);
                        if (minedIndex != -1) {
                            surroundIndex = (minedIndex + 1) % 4;
                        }
                    }
                    return;
                }
            }
        }
    }

    private boolean tryMineBlock(BlockPos pos) {
        if (!isMineable(pos)) return false;

        if (support.get()) {
            BlockPos supportPos = pos.down();
            if (mc.world.getBlockState(supportPos).isReplaceable()) {
                FindItemResult obbyResult = InvUtils.findInHotbar(Items.OBSIDIAN);
                if (!obbyResult.found()) return false;

                if (BlockUtils.place(supportPos, obbyResult, rotate.get(), 50, true)) {
                    delayLeft = 1;
                    return true;
                }
                return false;
            }
        }

        mc.player.swingHand(Hand.MAIN_HAND);
        MeteorClient.EVENT_BUS.post(StartBreakingBlockEvent.get(pos, Direction.UP));
        if (!instant.get()) {
            delayLeft = 1;
        }
        return true;
    }

    private boolean isMineable(BlockPos pos) {
        if (pos == null || mc.world.isOutOfHeightLimit(pos) || mc.world.getBlockState(pos).isAir()) {
            return false;
        }
        return PlayerUtils.distanceTo(pos) <= packetMine.getRange();
    }

    @Override
    public String getInfoString() {
        if (target != null) {
            return EntityUtils.getName(target);
        }
        return null;
    }
}
