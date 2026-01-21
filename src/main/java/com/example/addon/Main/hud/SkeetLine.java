package com.example.addon.Main.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SkeetLine extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static final HudElementInfo<SkeetLine> INFO = new HudElementInfo<>(
        com.example.addon.Hook.HUD_GROUP,
        "SkeetLine",
        "skkkkkeeetttttt",
        SkeetLine::new
    );

    private final Setting<SettingColor> color1 = sgGeneral.add(new ColorSetting.Builder()
        .name("color-1")
        .description("First gradient color.")
        .defaultValue(new SettingColor(87, 9, 241))
        .build());

    private final Setting<SettingColor> color2 = sgGeneral.add(new ColorSetting.Builder()
        .name("color-2")
        .description("Second gradient color.")
        .defaultValue(new SettingColor(10, 30, 245))
        .build());

    private final Setting<SettingColor> color3 = sgGeneral.add(new ColorSetting.Builder()
        .name("color-3")
        .description("Third gradient color.")
        .defaultValue(new SettingColor(0, 255, 216))
        .build());

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Height of the line.")
        .defaultValue(1.3)
        .min(0.5)
        .max(3.0)
        .build());

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Speed of the animation.")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .build());

    private final Setting<Integer> colorsNumber = sgGeneral.add(new IntSetting.Builder()
        .name("colors-number")
        .description("Number of colors in the gradient.")
        .defaultValue(3)
        .min(1)
        .max(3)
        .build());

    public enum Direction {
        LeftToRight,
        RightToLeft
    }

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("Direction of animation.")
        .defaultValue(Direction.LeftToRight)
        .build());

    private float time = 0;

    public SkeetLine() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double screenWidth = mc.getWindow().getScaledWidth();
        double barHeight  = height.get();
        int    segments   = mc.getWindow().getScaledWidth();
        double segWidth   = screenWidth / segments;
        float  offset     = (float)(time % screenWidth);
        if (direction.get() == Direction.RightToLeft) offset = -offset;

        double xBase = -screenWidth;
        Color c1 = color1.get(), c2 = color2.get(), c3 = color3.get();

        for (int i = 0; i < segments; i++) {
            double x1 = xBase + i * segWidth + offset;
            double x2 = x1 + segWidth;
            if (x2 < 0 || x1 > screenWidth) continue;

            float t = (float)i / segments;
            Color col;
            switch (colorsNumber.get()) {
                case 1: col = c1; break;
                case 2: col = lerpColor(c1, c2, t); break;
                default:
                    if (t < 0.5f) col = lerpColor(c1, c2, t * 2f);
                    else           col = lerpColor(c2, c3, (t - 0.5f) * 2f);
            }

            renderer.quad(x1, y, x2, y + barHeight, col);
        }

        time += speed.get().floatValue();
        setSize(screenWidth, barHeight);
    }

    private static Color lerpColor(Color a, Color b, float t) {
        float r = a.r + (b.r - a.r) * t;
        float g = a.g + (b.g - a.g) * t;
        float b2 = a.b + (b.b - a.b) * t;
        float a2 = a.a + (b.a - a.a) * t;
        return new Color(r, g, b2, a2);
    }

}
