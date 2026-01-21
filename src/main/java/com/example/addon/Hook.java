package com.example.addon;

import com.example.addon.Api.util.Statistics;
import com.example.addon.Api.util.SystemNotification;
import com.example.addon.Api.util.Wrapper;
import com.example.addon.Main.List;
import com.example.addon.Main.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Hook extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String Version = BuildInfo.COMMIT;
    public static final Category CATEGORY = new Category("PPHud");
    public static final HudGroup HUD_GROUP = new HudGroup("PPhud");
    public static final Statistics STATS = Statistics.get();

    @Override
    public void onInitialize() {
        LOG.info("Loading Privatehook+" + Version);
        List.Mods();
        Wrapper.disableTutorial();
        MeteorClient.EVENT_BUS.subscribe(STATS);
        Systems.addPreLoadTask(() -> Modules.get().get(DiscordRPC.class).toggle());
        Systems.addPreLoadTask(() -> Modules.get().get(ClientPrefix.class).toggle());
        //SystemNotification.send("Privatehook", "Welcome To Privatehook " + mc.player.getName().getString() + "!");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Nikkaguwop", "PrivateHook");
    }
}
