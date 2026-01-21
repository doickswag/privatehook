package com.example.addon.Main.hud;

import com.example.addon.Hook;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;

import static com.example.addon.Api.util.Wrapper.getTotalHealth;

public class Health extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public static final HudElementInfo<Health> INFO = new HudElementInfo<>(Hook.HUD_GROUP, "Health", "Shows your health", Health::new);

    public enum Mode {
        Text,
        Bar
    }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Selects whether to render the health as text or as a bar.")
        .defaultValue(Mode.Text)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Render with a shadow.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Color of the health text in text mode.")
        .defaultValue(Color.MAGENTA)
        .build()
    );

    private final Setting<Boolean> backround = sgGeneral.add(new BoolSetting.Builder()
        .name("backround")
        .description("Draw a background for the text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("backround-color")
        .description("Background color when rendering text.")
        .defaultValue(Color.MAGENTA)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale factor for text rendering.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    public Health() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        float totalHealth = getTotalHealth(player);
        Color dynamicColor;
        if (totalHealth >= 21) {
            dynamicColor = Color.YELLOW;
        } else if (totalHealth >= 20) {
            dynamicColor = Color.GREEN;
        } else {
            dynamicColor = Color.RED;
        }

        if (mode.get() == Mode.Text) {
            String text = String.valueOf(Math.round(totalHealth));
            if (backround.get()) {
                renderer.quad(x, y, getWidth(), getHeight(), backroundColor.get());
            }
            setSize(renderer.textWidth(text, shadow.get(), scale.get()), renderer.textHeight(shadow.get(), scale.get()));
            renderer.text(text, x, y, dynamicColor, shadow.get(), scale.get());
        } else if (mode.get() == Mode.Bar) {
            double barX = x;
            double barY = y;
            double barWidth = 165;
            double barHeight = 11;
            double scaleVal = scale.get();
            barX /= scaleVal;
            barY /= scaleVal;
            Renderer2D.COLOR.boxLines(barX, barY, barWidth, barHeight, backroundColor.get());
            Renderer2D.COLOR.render(null);
            double innerX = barX + 2;
            double innerY = barY + 2;
            float maxHealth = player.getMaxHealth();
            float health = player.getHealth();
            float absorb = player.getAbsorptionAmount();
            int maxAbsorb = 16;
            int maxTotal = (int) (maxHealth + maxAbsorb);
            int totalHealthWidth = (int) (161 * maxHealth / maxTotal);
            int totalAbsorbWidth = 161 * maxAbsorb / maxTotal;
            double healthPercent = health / maxHealth;
            double absorbPercent = maxAbsorb == 0 ? 0 : absorb / maxAbsorb;
            int healthWidth = (int) (totalHealthWidth * healthPercent);
            int absorbWidth = (int) (totalAbsorbWidth * absorbPercent);

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(innerX, innerY, healthWidth, 7, dynamicColor, dynamicColor, dynamicColor, dynamicColor);
            Renderer2D.COLOR.quad(innerX + healthWidth, innerY, absorbWidth, 7, dynamicColor, dynamicColor, dynamicColor, dynamicColor);
            Renderer2D.COLOR.render(null);
            setSize(barWidth, barHeight);
        }
    }
}
