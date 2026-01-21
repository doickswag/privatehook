package com.example.addon.Api.util;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.example.addon.Api.util.GANGTIL.eyePos;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockHelper {


    public static boolean isVecComplete(ArrayList<Vec3i> vlist) {
        BlockPos ppos = mc.player.getBlockPos();
        for (Vec3i b: vlist) {
            BlockPos bb = ppos.add(b.getX(), (int) b.getY(), (int) b.getZ());
            if (getBlock(bb) == Blocks.AIR) return false;
        }
        return true;
    }

    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (distanceBetween(centerPos, pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }
        return blocks;
    }


    public static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }


    public static BlockPos getBlockPosFromDirection(Direction direction, BlockPos orginalPos) {
        return switch (direction) {
            case UP -> orginalPos.up();
            case DOWN -> orginalPos.down();
            case EAST -> orginalPos.east();
            case WEST -> orginalPos.west();
            case NORTH -> orginalPos.north();
            case SOUTH -> orginalPos.south();
        };
    }


    public static Block getBlock(BlockPos p) {
        if (p == null) return null;
        return mc.world.getBlockState(p).getBlock();
    }

    public static boolean isOurSurroundBlock(BlockPos bp) {
        BlockPos ppos = mc.player.getBlockPos();
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;
            BlockPos pos = ppos.offset(direction);
            if (pos.equals(bp)) return true;
        }
        return false;
    }

    public static boolean inside(PlayerEntity en, Box bb) {
        return mc.world != null && mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }
    @SuppressWarnings({"DataFlowIssue", "BooleanMethodIsAlwaysInverted"})
    public static boolean strictDir(BlockPos pos, Direction dir) {
        return switch (dir) {
            case DOWN -> mc.player.getEyePos().y <= pos.getY() + 0.5;
            case UP -> mc.player.getEyePos().y >= pos.getY() + 0.5;
            case NORTH -> mc.player.getZ() < pos.getZ();
            case SOUTH -> mc.player.getZ() >= pos.getZ() + 1;
            case WEST -> mc.player.getX() < pos.getX();
            case EAST -> mc.player.getX() >= pos.getX() + 1;
        };
    }
    public static boolean outOfMiningRange(BlockPos pos, Origin origin, double range) {
        if (origin == Origin.VANILLA) {
            double deltaX = MeteorClient.mc.player.getX() - ((double) pos.getX() + 0.5);
            double deltaY = MeteorClient.mc.player.getY() - ((double) pos.getY() + 0.5) + 1.5;
            double deltaZ = MeteorClient.mc.player.getZ() - ((double) pos.getZ() + 0.5);
            return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > range * range;
        } else {
            Vec3d eyesPos = eyePos(MeteorClient.mc.player);
            double dx = eyesPos.x - (double) pos.getX() - 0.5;
            double dy = eyesPos.y - (double) pos.getY() - 0.5;
            double dz = eyesPos.z - (double) pos.getZ() - 0.5;
            return dx * dx + dy * dy + dz * dz > range * range;
        }
    }
}
