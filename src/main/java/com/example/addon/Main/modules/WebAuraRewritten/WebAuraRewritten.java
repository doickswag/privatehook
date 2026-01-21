package com.example.addon.Main.modules.WebAuraRewritten;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class WebAuraRewritten extends Hooked {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public WebAuraRewritten() {
        super(Hook.CATEGORY, "WebAuraRewritten", "Webs the opps in battles");
    }

}
