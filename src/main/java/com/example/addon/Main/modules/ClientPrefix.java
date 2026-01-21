package com.example.addon.Main.modules;

import com.example.addon.Api.util.GANGTIL;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

public class ClientPrefix extends Hooked {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> global = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Global")
            .description("Use the prefix on meteor client. If toggled when module is active, restart module.")
            .defaultValue(true)
            .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the marker.")
        .defaultValue(Color.MAGENTA)
        .build()
    );

    public ClientPrefix() {
        super(Hook.CATEGORY, "Custom", "Modifies the client's prefix.");
    }

    public void onActivate() {
        ChatUtils.registerCustomPrefix("com.example.addon", GANGTIL::getPrefix);
    }

    public void onDeactivate() {
        ChatUtils.unregisterCustomPrefix("com.example.addon");
        ChatUtils.registerCustomPrefix("meteordevelopment.meteorclient", GANGTIL::getPrefix);
    }

    @EventHandler
    public void onTick() {
        if (global.get()) {
            ChatUtils.registerCustomPrefix("meteordevelopment.meteorclient", GANGTIL::getPrefix);
        }
        else {
            ChatUtils.unregisterCustomPrefix("meteordevelopment.meteorclient");
        }
    }
}
