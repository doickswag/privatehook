package com.example.addon.Main.modules.Targeting;

import com.example.addon.Api.util.GANGTIL;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockSelector {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    public List<BlockPos> getBlocks(PlayerEntity target, FazeAdapt adapt, boolean mineBurrow, boolean mineHead, boolean mineSurround, int surroundIndex) {
        if (target == null || mc.world == null) return new ArrayList<>();

        List<BlockPos> blocks = new ArrayList<>();
        BlockPos targetPos = target.getBlockPos();

        if (adapt == FazeAdapt.TestSmart) {
            if (mc.world.getBlockState(targetPos.down()).isReplaceable() && !mc.world.getBlockState(targetPos).isAir()) {
                BlockPos undermineBase = targetPos.down();
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos supportBlock = undermineBase.offset(dir);
                    if (!mc.world.getBlockState(supportBlock).isAir()) {
                        blocks.add(supportBlock);
                    }
                }
                blocks.add(targetPos);
                return blocks;
            }
        }

        if (mineBurrow) {
            if (GANGTIL.isBurrowed(target, false)) {
                blocks.add(targetPos);
            }
        }

        if (mineHead) blocks.add(targetPos.up());

        if (mineSurround) {
            switch (adapt) {
                case Basic:
                    blocks.add(targetPos.north());
                    blocks.add(targetPos.south());
                    blocks.add(targetPos.east());
                    blocks.add(targetPos.west());
                    break;

                case FAZEADAPT:
                    List<BlockPos> clockwise = Arrays.asList(
                        targetPos.north(),
                        targetPos.east(),
                        targetPos.south(),
                        targetPos.west()
                    );
                    for (int i = 0; i < 4; i++) {
                        blocks.add(clockwise.get((surroundIndex + i) % 4));
                    }
                    break;

                case Normal:
                case TestSmart:
                    List<BlockPos> unblocked = new ArrayList<>();
                    List<BlockPos> blocked = new ArrayList<>();
                    List<BlockPos> surroundPositions = Arrays.asList(
                        targetPos.north(),
                        targetPos.south(),
                        targetPos.east(),
                        targetPos.west()
                    );

                    for (BlockPos surroundPos : surroundPositions) {
                        if (mc.world.getBlockState(surroundPos.up()).isReplaceable()) {
                            unblocked.add(surroundPos);
                        } else {
                            blocked.add(surroundPos);
                        }
                    }
                    blocks.addAll(unblocked);
                    blocks.addAll(blocked);
                    break;

                case Raper:
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos surroundPos = targetPos.offset(dir);
                        if (mc.world.getBlockState(surroundPos).isAir() || !isBreakable(surroundPos)) {
                            continue;
                        }
                        boolean hasAddedBlocking = false;
                        BlockPos frontPos = surroundPos.offset(dir);
                        if (!mc.world.getBlockState(frontPos).isAir()) {
                            if (isBreakable(frontPos)) {
                                blocks.add(frontPos);
                                hasAddedBlocking = true;
                            } else {
                                Direction side1 = dir.rotateYClockwise();
                                Direction side2 = dir.rotateYCounterclockwise();
                                BlockPos sidePos1 = surroundPos.offset(side1);
                                BlockPos sidePos2 = surroundPos.offset(side2);

                                if (!mc.world.getBlockState(sidePos1).isAir() && isBreakable(sidePos1)) {
                                    blocks.add(sidePos1);
                                    hasAddedBlocking = true;
                                }
                                if (!mc.world.getBlockState(sidePos2).isAir() && isBreakable(sidePos2)) {
                                    blocks.add(sidePos2);
                                    hasAddedBlocking = true;
                                }
                            }
                        }
                        if (!hasAddedBlocking) {
                            BlockPos abovePos = surroundPos.up();
                            if (!mc.world.getBlockState(abovePos).isAir() && isBreakable(abovePos)) {
                                blocks.add(abovePos);
                            }
                        }
                        blocks.add(surroundPos);
                    }
                    break;
            }
        }
        return blocks;
    }

    private boolean isBreakable(BlockPos pos) {
        if (pos == null || mc.world == null) return false;
        return mc.world.getBlockState(pos).getHardness(mc.world, pos) != -1.0f;
    }
}
