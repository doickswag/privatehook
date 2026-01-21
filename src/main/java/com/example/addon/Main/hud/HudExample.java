package com.example.addon.Main.hud;

import com.example.addon.Hook;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.MathHelper;

public class HudExample extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgWave = settings.createGroup("Wave");
    public static final HudElementInfo<HudExample> INFO = new HudElementInfo<>(Hook.HUD_GROUP, "example", "HUD element example.", HudExample::new);

    public final Setting<String> clientName = sgGeneral.add(new StringSetting.Builder()
        .name("Custom Name")
        .description("Name to display.")
        .defaultValue("PrivateHook-Beta")
        .build()
    );
    private final Setting<Boolean> Version = sgGeneral.add(new BoolSetting.Builder()
        .name("Version")
        .description("version")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders placed sand and obsidian blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The base color for the marker.")
        .defaultValue(Color.MAGENTA)
        .build()
    );
    private final Setting<Boolean> backround = sgGeneral.add(new BoolSetting.Builder()
        .name("Backround")
        .description("Render a background behind the text.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> backroundcolor = sgGeneral.add(new ColorSetting.Builder()
        .name("backroundcolor")
        .description("The color of the background.")
        .defaultValue(Color.MAGENTA)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Modify the size of the text.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> wave = sgWave.add(new BoolSetting.Builder()
        .name("Wave")
        .description("Enables wave color animation.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> waveColor = sgWave.add(new ColorSetting.Builder()
        .name("Wave Color")
        .description("The target color for the wave effect.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> speed = sgWave.add(new DoubleSetting.Builder()
        .name("Wave Speed")
        .description("The speed of the color waves.")
        .defaultValue(1)
        .min(0)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> length = sgWave.add(new DoubleSetting.Builder()
        .name("Wave Length")
        .description("Determines how stretched the wave effect is over time.")
        .defaultValue(5)
        .min(0)
        .sliderRange(1, 10)
        .build()
    );

    public HudExample() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        //String text = builddate.get() ? clientName.get() + " " + BuildInfo.BUILD_TIME : clientName.get();
        String text = Version.get() ? clientName.get() + "+" + Hook.Version : clientName.get();
        if (backround.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), backroundcolor.get());
        }
        double f = 0;
        if (wave.get()) {
            f = Math.sin(System.currentTimeMillis() / 1000d * speed.get()) + 1;
        }
        double textWidth = renderer.textWidth(text, shadow.get(), scale.get());
        double textHeight = renderer.textHeight(shadow.get(), scale.get());
        setSize(textWidth, textHeight);

        Color finalColor = getColor(color.get(), waveColor.get(), f);
        renderer.text(text, x, y, finalColor, shadow.get(), scale.get());
    }

    private Color getColor(SettingColor base, SettingColor target, double f) {
        if (!this.wave.get()) {
            return base;
        }
        return new Color(
            colorVal(base.r, target.r, f),
            colorVal(base.g, target.g, f),
            colorVal(base.b, target.b, f),
            base.a
        );
    }
    private int colorVal(int original, int wave, double f) {
        return MathHelper.clamp((int) Math.floor(wave + (original - wave) * f), 0, 255);
    }
}
