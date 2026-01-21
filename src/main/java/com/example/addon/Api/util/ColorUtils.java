package com.example.addon.Api.util;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;

import java.awt.*;

public class ColorUtils {
    public static int toARGB(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }
    private static int red;
    private static int green;
    private static int blue;

    public static int getRGBA(){
        return new Color(red, green, blue, 255).getRGB();
    }


    public static int toRGBA(int r, int g, int b) {
        return ColorUtils.toRGBA(r, g, b, 255);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBA(float r, float g, float b, float a) {
        return ColorUtils.toRGBA((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (a * 255.0f));
    }

    public static int injectAlpha(int color, int alpha) {
        return toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), alpha);
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    public static int toRGBA(float[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return ColorUtils.toRGBA(colors[0], colors[1], colors[2], colors[3]);
    }

    public static int toRGBA(double[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return ColorUtils.toRGBA((float) colors[0], (float) colors[1], (float) colors[2], (float) colors[3]);
    }

    public static int toRGBA(Color color) {
        return ColorUtils.toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    public static SettingColor getColorFromPercent(double percent) {
        SettingColor distanceColor = new SettingColor(0, 0, 0);
        if (!(percent < 0.0) && !(percent > 1.0)) {
            int r;
            int g;
            if (percent < 0.5) {
                r = 255;
                g = (int) (255.0 * percent / 0.5);
            } else {
                g = 255;
                r = 255 - (int) (255.0 * (percent - 0.5) / 0.5);
            }

            distanceColor.set(r, g, 0, 255);
            return distanceColor;
        } else {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }
    }

}
