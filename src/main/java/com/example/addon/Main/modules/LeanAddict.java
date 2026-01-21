package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.util.Formatting;

public class LeanAddict extends Hooked {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public LeanAddict() {
        super(Hook.CATEGORY, "Lean Addict", "i feel like lucki");
    }

    @Override
    public String getInfoString() {
        if (mc.player != null && mc.player.getActiveItem() != ItemStack.EMPTY) {
            ItemStack activeItem = mc.player.getActiveItem();
            if (activeItem.getItem() instanceof PotionItem) {
                return Formatting.DARK_PURPLE + "Lean";
            }
        }
        return Formatting.RED + "Lean";
    }
}
