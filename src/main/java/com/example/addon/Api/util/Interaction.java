package com.example.addon.Api.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;
import javax.swing.*;

import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Interaction {
    private static int prevSlot = -1;
    public static int sloty;
    private static int[] slots;

    public static void updateSlot(FindItemResult result, boolean packet) {
        updateSlot(result.slot(), packet);
    }

    public static boolean updateSlot(int slot, boolean packet) {
        if (slot < 0 || slot > 8) return false;
        if (prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;

        // updates slot on client and server side
        mc.player.getInventory().selectedSlot = slot;
        if (packet) mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        return true;
    }

    public static boolean swapBack(int slot) {
        return updateSlot(slot, true);
    }
        public static boolean swapBack() {
        if (prevSlot == -1) return false;

        boolean return_ = updateSlot(prevSlot, true);
        prevSlot = -1;
        return return_;
    }

    public static boolean invSwitch(int slot) {
        if (slot >= 0) {
            ScreenHandler handler = mc.player.currentScreenHandler;
            Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
            stack.put(slot, handler.getSlot(slot).getStack());

            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId,
                handler.getRevision(), PlayerInventory.MAIN_SIZE + sloty,
                slot, SlotActionType.SWAP, handler.getSlot(slot).getStack(), stack)
            );
            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
            slots = new int[]{slot, sloty};
            return true;
        }
        return false;
    }
    public static void doSwing(SwingHand swingHand, boolean packetSwing, @Nullable Hand autoHand) {
        switch (swingHand) {
            case MainHand -> doSwing(Hand.MAIN_HAND, packetSwing);
            case OffHand -> doSwing(Hand.OFF_HAND, packetSwing);
            case Auto -> doSwing(autoHand, packetSwing);
        }
    }

    public static void doSwing(Hand hand, boolean packetSwing) {
        if (packetSwing) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        else mc.player.swingHand(hand);
    }

    public static Hand toHand(SwingHand swingHand) {
        return switch (swingHand) {
            case MainHand, Auto -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
        };
    }

    public static void placeBlock(Hand hand, BlockHitResult result, boolean packetPlace) {
         mc.interactionManager.interactBlock(mc.player, hand, result);
    }

    public static boolean hasEntity(Box box) {
        return hasEntity(box, entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity);
    }

    public static boolean hasEntity(Box box, Predicate<Entity> predicate) {
        return EntityUtils.intersectsWithEntity(box, predicate);
    }

    public enum SwingHand {
        MainHand, OffHand, Auto
    }
}
