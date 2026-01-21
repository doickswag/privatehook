package com.example.addon.Main.modules;

import com.example.addon.Api.util.MathUtils;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class MoneyBooster extends Hooked {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public MoneyBooster() {
        super(Hook.CATEGORY, "Money Booster", "Boosts Yo Bands");
    }
    public final Setting<bandwarriormode> racks = sgGeneral.add(new EnumSetting.Builder<bandwarriormode>()
        .name("Rack")
        .description("aaaaa")
        .defaultValue(bandwarriormode.OneBand)
        .build()
    );
    public enum bandwarriormode {
        OneBand,
        MultiBand
    }
    @Override
    public String getInfoString() {
        if (racks.get() == bandwarriormode.OneBand) {
            return String.valueOf(MathUtils.getNumber(0, 1000000000));
        }
        if (racks.get() == bandwarriormode.MultiBand) {
            return String.valueOf(MathUtils.getRandomNumber(0, 1000000000));
        }
        return "bandless";
    }
}

