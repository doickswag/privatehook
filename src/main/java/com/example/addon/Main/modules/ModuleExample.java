package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ModuleExample extends Hooked {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    /**
     * Example setting.
     * The {@code name} parameter should be in kebab-case.
     * If you want to access the setting from another class, simply make the setting {@code public}, and use
     * {@link meteordevelopment.meteorclient.systems.modules.Modules#get(Class)} to access the {@link Module} object.
     */
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The size of the marker.")
        .defaultValue(2.0d)
        .range(0.5d, 10.0d)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the marker.")
        .defaultValue(Color.MAGENTA)
        .build()
    );
    private final Setting<Integer> interger = sgRender.add(new IntSetting.Builder()
        .name("wter")
        .description("")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Double> range = sgRender.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The maximum distance to target players.")
        .defaultValue(4)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );

    private final Setting<SortPriority> priority = sgRender.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );
    private final Setting<Boolean> placeSand = sgRender.add(new BoolSetting.Builder()
        .name("putting-sand")
        .description("Puts sand on the target's head.")
        .defaultValue(false)
        .build()
    );
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public ModuleExample() {
        super(Hook.CATEGORY, "world-origin", "An example module that highlights the center of the world.");
    }

    /**
     * Example event handling method.
     * Requires {@link Hook#getPackage()} to be setup correctly, will fail silently otherwise.
     */
    @EventHandler
    private void onRender3d(Render3DEvent event) {
        // Create & stretch the marker object
        Box marker = new Box(BlockPos.ORIGIN);
        marker.stretch(
            scale.get() * marker.getLengthX(),
            scale.get() * marker.getLengthY(),
            scale.get() * marker.getLengthZ()
        );

        // Render the marker based on the color setting
        event.renderer.box(marker, color.get(), color.get(), ShapeMode.Both, 0);
    }
}
