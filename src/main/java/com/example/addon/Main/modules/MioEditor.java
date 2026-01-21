package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import net.minecraft.util.Formatting;
import meteordevelopment.meteorclient.settings.*;

public class MioEditor extends Hooked {
    public enum Mode {
        Future,
        Default
    }

    public enum WatermarkMode {
        FutureBeta,
        Custom
    }

    public enum SurroundMode {
        Default,
        AutoObsidian,
        AntiDie,
        AutoFeetPlace
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgArraylist = settings.createGroup("Arraylist");

    public final Setting<Boolean> customWatermarkEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("customwatermark")
        .description("")
        .defaultValue(false)
        .build()
    );
    public final Setting<WatermarkMode> watermarkMode = sgGeneral.add(new EnumSetting.Builder<WatermarkMode>()
        .name("Watermakrmode")
        .description("")
        .defaultValue(WatermarkMode.FutureBeta)
        .visible(customWatermarkEnabled::get)
        .build()
    );
    public final Setting<String> custom = sgGeneral.add(new StringSetting.Builder()
        .name("text")
        .description("")
        .defaultValue("PrivateHook.CC")
        .visible(() -> customWatermarkEnabled.get() && watermarkMode.get() == WatermarkMode.Custom)
        .build()
    );
    public final Setting<Boolean> notificationEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("notifications")
        .description("")
        .defaultValue(false)
        .build()
    );
    public final Setting<Mode> notificationMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("")
        .defaultValue(Mode.Future)
        .visible(notificationEnabled::get)
        .build()
    );

    public final Setting<SurroundMode> surroundMode = sgArraylist.add(new EnumSetting.Builder<SurroundMode>()
        .name("surroundmode")
        .description("")
        .defaultValue(SurroundMode.Default)
        .build()
    );

    public MioEditor() {
        super(Hook.CATEGORY, "Editor", "edits mio");
    }

    public String text(String text) {


        // (?i)mio replaces everything with mio
        String replacement = custom.get();
        String moduleName = text.replaceAll("(?i)[Mio] [+]", "").trim();
        String surround = String.valueOf(surroundMode.get());
        String on = this.custom.get() + " " + moduleName + " toggled " + Formatting.GREEN + "on.";
        text = text.replaceAll("(?i)mio", replacement);
        text = text.replaceAll("FeetPlace", surround);
        text = text.replaceAll("(?i)[Mio] [+]", on);
    /*

        if (this.notificationEnabled.get() &&
            this.notificationMode.get() == MioEditor.Mode.Future) {

            if (text.startsWith("([Mio]")) {
                String moduleName = text.replaceAll("[Mio] [+]", "").trim();
                return this.custom.get() + " " + moduleName + " toggled " + Formatting.GREEN + "on.";
            } else if (text.startsWith("[Mio] [-]")) {
                String moduleName = text.replaceAll("[Mio] [-]", "").trim();
                return this.custom.get() + " " + moduleName + " toggled " + Formatting.RED + "off.";
            }
        }
        if (this.customWatermarkEnabled.get()) {
            if (text.contains("Mio")) {
                String watermark = this.watermarkMode.get() == MioEditor.WatermarkMode.FutureBeta
                    ? "Future v2.13.5+27.68af4a4971"
                    : this.custom.get();
                text = text.replace("Mio", watermark);
                }
            }
     */
        return text;
    }
}
