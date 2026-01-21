package com.example.addon.Api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilsPlus {
    private static final Mutable MUTABLE = new Mutable();
    public static Vec3i[] CITY_WITH_BURROW = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};

    public static boolean isSurrounded(LivingEntity target, boolean doubles, boolean onlyBlastProof) {
        BlockPos blockPos = target.getBlockPos();
        int air = 0;

        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                BlockState state = MeteorClient.mc.world.getBlockState(blockPos.offset(direction));
                if (GANGTIL.replaceable(blockPos) || onlyBlastProof && state.getBlock().getBlastResistance() < 600.0F) {
                    if (!doubles || direction == Direction.DOWN) {
                        return false;
                    }

                    ++air;

                    for (Direction dir : Direction.values()) {
                        if (dir != direction.getOpposite() && dir != Direction.UP) {
                            BlockState state2 = MeteorClient.mc.world.getBlockState(blockPos.offset(direction).offset(dir));
                            if (GANGTIL.replaceable(blockPos) || onlyBlastProof && state2.getBlock().getBlastResistance() < 600.0F) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return air < 2;
    }

    public static boolean isSafe(LivingEntity player) {
        return isSurrounded(player, true, true) || isBurrowed(player);
    }

    public static void mine(BlockPos blockPos, boolean swing, boolean rotate) {
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), () -> mine(blockPos, swing, false));
        } else {
            MeteorClient.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            MeteorClient.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            if (swing) {
                MeteorClient.mc.player.swingHand(Hand.MAIN_HAND);
            } else {
                MeteorClient.mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    public static int sort(Entity e1, Entity e2, SortPriority priority) {
        return switch (priority) {
            case LowestDistance ->
                    Double.compare(e1.distanceTo(MeteorClient.mc.player), e2.distanceTo(MeteorClient.mc.player));
            case HighestDistance ->
                    invertSort(Double.compare(e1.distanceTo(MeteorClient.mc.player), e2.distanceTo(MeteorClient.mc.player)));
            case LowestHealth -> sortHealth(e1, e2);
            case HighestHealth -> invertSort(sortHealth(e1, e2));
            case ClosestAngle -> sortAngle(e1, e2);
            default -> throw new IncompatibleClassChangeError();
        };
    }

    private static int sortHealth(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;
        if (!e1l && !e2l) {
            return 0;
        } else if (e1l && !e2l) {
            return 1;
        } else {
            return !e1l ? -1 : Float.compare(((LivingEntity) e1).getHealth(), ((LivingEntity) e2).getHealth());
        }
    }

    private static int sortAngle(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;
        if (!e1l && !e2l) {
            return 0;
        } else if (e1l && !e2l) {
            return 1;
        } else if (!e1l) {
            return -1;
        } else {
            double e1yaw = Math.abs(Rotations.getYaw(e1) - (double) MeteorClient.mc.player.getYaw());
            double e2yaw = Math.abs(Rotations.getYaw(e2) - (double) MeteorClient.mc.player.getYaw());
            double e1pitch = Math.abs(Rotations.getPitch(e1) - (double) MeteorClient.mc.player.getPitch());
            double e2pitch = Math.abs(Rotations.getPitch(e2) - (double) MeteorClient.mc.player.getPitch());
            return Double.compare(Math.sqrt(e1yaw * e1yaw + e1pitch * e1pitch), Math.sqrt(e2yaw * e2yaw + e2pitch * e2pitch));
        }
    }

    private static int invertSort(int sort) {
        if (sort == 0) {
            return 0;
        } else {
            return sort > 0 ? -1 : 1;
        }
    }



    public static boolean isBurrowed(LivingEntity target) {
        return isCollides(MUTABLE.set(target.getX() + 0.3, target.getY(), target.getZ() + 0.3)) || isCollides(MUTABLE.set(target.getX() + 0.3, target.getY(), target.getZ() - 0.3)) || isCollides(MUTABLE.set(target.getX() - 0.3, target.getY(), target.getZ() + 0.3)) || isCollides(MUTABLE.set(target.getX() - 0.3, target.getY(), target.getZ() - 0.3));
    }

    public static boolean isCollides(BlockPos pos) {
        return !MeteorClient.mc.world.getBlockState(pos).getOutlineShape(MeteorClient.mc.world, pos).isEmpty();
    }

    public static boolean isObbyBurrowed(LivingEntity target) {
        return MeteorClient.mc.world.getBlockState(MUTABLE.set(target.getX() + 0.3, target.getY(), target.getZ() + 0.3)).getBlock().getBlastResistance() > 600.0F || MeteorClient.mc.world.getBlockState(MUTABLE.set(target.getX() + 0.3, target.getY(), target.getZ() - 0.3)).getBlock().getBlastResistance() > 600.0F || MeteorClient.mc.world.getBlockState(MUTABLE.set(target.getX() - 0.3, target.getY(), target.getZ() + 0.3)).getBlock().getBlastResistance() > 600.0F || MeteorClient.mc.world.getBlockState(MUTABLE.set(target.getX() - 0.3, target.getY(), target.getZ() - 0.3)).getBlock().getBlastResistance() > 600.0F;
    }

    public static Direction rayTraceCheck(BlockPos pos, boolean forceReturn) {
        Vec3d eyesPos = MeteorClient.mc.player.getEyePos();

        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, BlockUtils2.sideVec(pos, direction), ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player);
            BlockHitResult result = MeteorClient.mc.world.raycast(raycastContext);
            if (result != null && result.getType() == Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if (!forceReturn) {
            return null;
        } else {
            return (double) pos.getY() > eyesPos.y ? Direction.DOWN : Direction.UP;
        }
    }


    public static boolean isNaked(PlayerEntity player) {
        return (player.getInventory().armor == null || player.getInventory().armor.isEmpty()) && (player.getOffHandStack() == null || player.getOffHandStack().isEmpty());
    }

    public static BlockHitResult getPlaceResult(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY() + (double) MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()), MeteorClient.mc.player.getZ());

        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, BlockUtils2.sideVec(pos, direction), ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player);
            BlockHitResult result = MeteorClient.mc.world.raycast(raycastContext);
            if (result != null && result.getType() == Type.BLOCK && result.getBlockPos().equals(pos)) {
                return result;
            }
        }

        return new BlockHitResult(Vec3d.ofCenter(pos), BlockUtils2.getClosestDirection(pos, false), pos, false);
    }

    public static boolean cantSee(BlockPos pos, boolean strictDirections) {
        Vec3d eyePos = PlayerUtils2.eyePos(MeteorClient.mc.player);
        int eyeY = (int) Math.ceil(eyePos.y);

        for (Direction direction : Direction.values()) {
            if (!strictDirections || AntiCheatHelper.isInteractableStrict(MeteorClient.mc.player.getBlockX(), eyeY, MeteorClient.mc.player.getBlockZ(), pos, direction)) {
                RaycastContext raycastContext = new RaycastContext(eyePos, BlockUtils2.sideVec(pos, direction), ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player);
                BlockHitResult result = MeteorClient.mc.world.raycast(raycastContext);
                if (result == null) {
                    return true;
                } else {
                    return switch (result.getType()) {
                        case BLOCK -> !result.getBlockPos().equals(pos);
                        case ENTITY, MISS -> false;
                        default -> throw new IncompatibleClassChangeError();
                    };
                }
            }
        }

        return true;
    }

    public static boolean canSeeBlock(BlockPos hitBlock, Vec3d origin) {
        RaycastContext raycastContext = new RaycastContext(origin, Vec3d.ofCenter(hitBlock), ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player);
        BlockHitResult result = MeteorClient.mc.world.raycast(raycastContext);
        return result.getBlockPos().equals(hitBlock);
    }

    public static float getTotalHealth(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static boolean isBlastRes(Block block) {
        return block == Blocks.RESPAWN_ANCHOR && PlayerUtils.getDimension() == Dimension.Nether || block.getBlastResistance() >= 600.0F;
    }

    public static boolean isGoodForSurround(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.OBSIDIAN || item == Items.ANCIENT_DEBRIS || item == Items.CRYING_OBSIDIAN || item == Items.ANVIL || item == Items.CHIPPED_ANVIL || item == Items.DAMAGED_ANVIL || item == Items.ENCHANTING_TABLE || item == Items.ENDER_CHEST || item == Items.NETHERITE_BLOCK || PlayerUtils.getDimension() == Dimension.Nether && item == Items.RESPAWN_ANCHOR;
    }

    public static Vec3d smartVelocity(Entity entity) {
        return new Vec3d(entity.getX() - entity.prevX, entity.getY() - entity.prevY, entity.getZ() - entity.prevZ);
    }

    private static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(collisions.size() + 1);
        if (!collisions.isEmpty()) {
            builder.addAll(collisions);
        }

        WorldBorder worldBorder = world.getWorldBorder();
        boolean bl = entity != null && worldBorder.canCollide(entity, entityBoundingBox.stretch(movement));
        if (bl) {
            builder.add(worldBorder.asVoxelShape());
        }

        builder.addAll(world.getBlockCollisions(entity, entityBoundingBox.stretch(movement)));
        return adjustMovementForCollisions(movement, entityBoundingBox, builder.build());
    }

    private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0) {
            e = VoxelShapes.calculateMaxOffset(Axis.Y, entityBoundingBox, collisions, e);
            if (e != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(0.0, e, 0.0);
            }
        }

        boolean bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
            if (f != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, f);
            }
        }

        if (d != 0.0) {
            d = VoxelShapes.calculateMaxOffset(Axis.X, entityBoundingBox, collisions, d);
            if (!bl && d != 0.0) {
                entityBoundingBox = entityBoundingBox.offset(d, 0.0, 0.0);
            }
        }

        if (!bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
        }

        return new Vec3d(d, e, f);
    }

    public static float yawFromDir(Direction direction) {
        return switch (direction) {
            case EAST -> -90.0F;
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    public static Direction dirFromVec3d(Vec3d vec) {
        return Direction.fromRotation(Rotations.getYaw(vec));
    }

    public static boolean isSelfTrapBlock(LivingEntity target, BlockPos pos) {
        for (Vec3i city : CITY_WITH_BURROW) {
            for (int i = 0; i < 3; ++i) {
                if (pos.equals(target.getBlockPos().add(city.up(i)))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Mutable[] playerBlocks(PlayerEntity player) {
        Box box = player.getBoundingBox();
        return new Mutable[]{new Mutable(box.maxX, box.minY, box.maxZ), new Mutable(box.maxX, box.minY, box.minZ), new Mutable(box.minX, box.minY, box.maxZ), new Mutable(box.minX, box.minY, box.minZ), new Mutable(box.maxX, box.maxY, box.maxZ), new Mutable(box.maxX, box.maxY, box.minZ), new Mutable(box.minX, box.maxY, box.maxZ), new Mutable(box.minX, box.maxY, box.minZ)};
    }

    public static double getPlayerSpeed(boolean vertical) {
        double dX = MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX;
        double dY = MeteorClient.mc.player.getY() - MeteorClient.mc.player.prevY;
        double dZ = MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ;
        double distance = Math.sqrt(dX * dX + dZ * dZ + (vertical ? dY * dY : 0.0));
        distance *= Modules.get().get(Timer.class).getMultiplier();
        return distance * 20.0;
    }
    public static boolean obbySurrounded(LivingEntity entity) {
        BlockPos pos = entity.getBlockPos();
        return true;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... checked) {
        return new HashSet<>(Arrays.asList(checked));
    }
}
