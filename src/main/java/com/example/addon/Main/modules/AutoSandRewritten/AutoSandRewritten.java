package com.example.addon.Main.modules.AutoSandRewritten;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class AutoSandRewritten extends Hooked {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public AutoSandRewritten() {
        super(Hook.CATEGORY, "AutoSandRewritten", "kicks opps outa holes if the server is ass with their burrow check");
    }

}
