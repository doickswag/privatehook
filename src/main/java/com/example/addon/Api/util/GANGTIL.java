package com.example.addon.Api.util;

import com.example.addon.Api.mixin.IBlockSettings;
import com.example.addon.Main.modules.ClientPrefix;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GANGTIL {
    private final ClientPrefix clientprefix = new ClientPrefix();
    private boolean placing;
    private int placingTimer;
    private final BlockPos.Mutable placingCrystalBlockPos = new BlockPos.Mutable();

    public static final List<Item> CONCRETE_POWDERS = Arrays.asList(
        Items.WHITE_CONCRETE_POWDER,
        Items.ORANGE_CONCRETE_POWDER,
        Items.MAGENTA_CONCRETE_POWDER,
        Items.LIGHT_BLUE_CONCRETE_POWDER,
        Items.YELLOW_CONCRETE_POWDER,
        Items.LIME_CONCRETE_POWDER,
        Items.PINK_CONCRETE_POWDER,
        Items.GRAY_CONCRETE_POWDER,
        Items.LIGHT_GRAY_CONCRETE_POWDER,
        Items.CYAN_CONCRETE_POWDER,
        Items.PURPLE_CONCRETE_POWDER,
        Items.BLUE_CONCRETE_POWDER,
        Items.BROWN_CONCRETE_POWDER,
        Items.GREEN_CONCRETE_POWDER,
        Items.RED_CONCRETE_POWDER,
        Items.BLACK_CONCRETE_POWDER,
        Items.SAND
    );

    public static ArrayList<Vec3i> surroundPositions = new ArrayList<Vec3i>() {{
        add(new Vec3i(1, 0, 0));
        add(new Vec3i(-1, 0, 0));
        add(new Vec3i(0, 0, 1));
        add(new Vec3i(0, 0, -1));
    }};

    @SuppressWarnings("DataFlowIssue")
    public static boolean replaceable(BlockPos block) {
        return ((IBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).replaceable();
    }

    public static boolean isAnvilBlock(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.ANVIL || BlockHelper.getBlock(pos) == Blocks.CHIPPED_ANVIL || BlockHelper.getBlock(pos) == Blocks.DAMAGED_ANVIL;
    }

    public static boolean isWeb(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.COBWEB || BlockHelper.getBlock(pos) == Block.getBlockFromItem(Items.STRING);
    }

    public static boolean isBurrowed(PlayerEntity p, boolean holeCheck) {
        BlockPos pos = p.getBlockPos();
        if (holeCheck && !Wrapper.isInHole(p)) return false;
        return BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST || BlockHelper.getBlock(pos) == Blocks.OBSIDIAN || isAnvilBlock(pos);
    }

    public static boolean isWebbed(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        if (isWeb(pos)) return true;
        return isWeb(pos.up());
    }

    public static boolean isTrapBlock(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.OBSIDIAN || BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST;
    }

    public static boolean isSurroundBlock(BlockPos pos) {
        //some apes use anchors as surround blocks cus it has the same blast resistance as obsidian
        return BlockHelper.getBlock(pos) == Blocks.OBSIDIAN || BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST || BlockHelper.getBlock(pos) == Blocks.RESPAWN_ANCHOR;
    }

    public static boolean canCrystal(PlayerEntity p) {
        BlockPos tpos = p.getBlockPos();
        for (Vec3i sp : surroundPositions) {
            BlockPos sb = tpos.add(sp.getX(), sp.getY(), sp.getZ());
            if (BlockHelper.getBlock(sb) == Blocks.AIR) return true;
        }
        return false;
    }

    public static void mineWeb(PlayerEntity p, int swordSlot) {
        if (p == null || swordSlot == -1) return;
        BlockPos pos = p.getBlockPos();
        BlockPos webPos = null;
        if (isWeb(pos)) webPos = pos;
        if (isWeb(pos.up())) webPos = pos.up();
        if (isWeb(pos.up(2))) webPos = pos.up(2);
        if (webPos == null) return;
        Wrapper.updateSlot(swordSlot);
        doRegularMine(webPos);
    }

    public static void SilentmineWeb(PlayerEntity p, int swordSlot) {
        if (p == null || swordSlot == -1) return;
        BlockPos pos = p.getBlockPos();
        BlockPos webPos = null;
        if (isWeb(pos)) webPos = pos;
        if (isWeb(pos.up())) webPos = pos.up();
        if (isWeb(pos.up(2))) webPos = pos.up(2);
        if (webPos == null) return;
        Wrapper.updateSlot(swordSlot);
        doPacketMine(webPos);
    }

    public static void doPacketMine(BlockPos targetPos) {
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        Wrapper.swingHand(false);
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }

    public static void doRegularMine(BlockPos targetPos) {
        mc.interactionManager.updateBlockBreakingProgress(targetPos, Direction.UP);
        Vec3d hitPos = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), 50, () -> Wrapper.swingHand(false));
    }

    public static Text getPrefix() {
        MutableText prefix = Text.literal("");
        MutableText name1 = Text.literal("PrivateHook");
        name1.setStyle(name1.getStyle().withFormatting(Formatting.LIGHT_PURPLE));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.DARK_PURPLE))
            .append(Text.literal("«"))
            .append(name1)
            .append(Text.literal("» "));
        return prefix;
    }

    public static boolean movement() {
        return mc.player.input.pressingLeft || mc.player.input.pressingRight || mc.player.input.pressingBack || mc.player.input.pressingForward || mc.player.input.sneaking;
    }

    public static boolean airPlace(BlockPos pos, Hand hand, int slot, boolean rotate, int priority, boolean swing, boolean checkVisible, boolean packetPlacement, boolean silent) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        if (!silent) {
            return BlockUtils.place(pos, hand, slot, rotate, priority, swing, checkVisible, packetPlacement);
        }
        if (slot < 0 || slot > 8) {
            return false;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        if (slot == prevSlot) {
            return BlockUtils.place(pos, hand, slot, rotate, priority, swing, checkVisible, packetPlacement);
        }
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        boolean placed = BlockUtils.place(pos, hand, slot, rotate, priority, swing, checkVisible, packetPlacement);
        mc.player.getInventory().selectedSlot = prevSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        return placed;
    }

    public static int getCount(Item item) {
        if (mc.player == null) return 0;
        int count = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    public static Vec3d closestVec3d(BlockPos blockpos) {
        if (blockpos == null) return new Vec3d(0.0, 0.0, 0.0);
        double x = MathHelper.clamp((mc.player.getX() - blockpos.getX()), 0.0, 1.0);
        double y = MathHelper.clamp((mc.player.getY() - blockpos.getY()), 0.0, 0.6);
        double z = MathHelper.clamp((mc.player.getZ() - blockpos.getZ()), 0.0, 1.0);
        return new Vec3d(blockpos.getX() + x, blockpos.getY() + y, blockpos.getZ() + z);
    }

    public static boolean collidable(BlockPos block) {
        return ((AbstractBlockAccessor) mc.world.getBlockState(block).getBlock()).isCollidable();
    }
    public static Vec3d eyePos(PlayerEntity player) {
        return player.getPos().add(0.0, player.getEyeHeight(player.getPose()), 0.0);
    }
    public static boolean hasAquaAffinity(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (hasEnchantment(Enchantments.AQUA_AFFINITY, stack)) return true;
        }
        return false;
    }

    public static boolean hasEnchantment(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.getValue();
        return v.getEnchantments().stream().anyMatch(entry -> entry.matchesId(enchantmentId));
    }
    public static int getLevel(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.getValue();
        return v.getEnchantments().stream().filter(entry -> entry.matchesId(enchantmentId)).map(v::getLevel).max(Comparator.comparingInt(level -> level)).orElse(0);
    }
}
