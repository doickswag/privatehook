package com.example.addon.Main;

import com.example.addon.Main.commands.CommandExample;
import com.example.addon.Main.commands.ReconnectCommand;
import com.example.addon.Main.hud.*;
import com.example.addon.Main.modules.*;
import com.example.addon.Main.modules.AutoSandRewritten.AutoSandRewritten;
import com.example.addon.Main.modules.PacketMine.AutoBreak;
import com.example.addon.Main.modules.Targeting.AutoMine;
import com.example.addon.Main.modules.WebAuraRewritten.WebAuraRewritten;
import com.example.addon.Main.modules.seedmap.SeedMap;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;

public class List {
    public static void Mods() {
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new BurrowAlert());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoSand());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AntiPlacement());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new ClientPrefix());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new VehicleOneHit());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new NoDamage());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new WinnerPredictor());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new LeanAddict());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoRat());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new TntRange());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoWither());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoEz());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new WebAura());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new HeadProtect());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new TNTAura());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new MoneyBooster());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AntiCornerClip());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new DiscordRPC());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new SeedMap());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new ConstAntiQueue());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoCev());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new SKEETBAIT());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new BASELOG());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new MioEditor());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoBlink());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new BlinkPlus());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new HoleSnap());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoBreak());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoMine());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new VillagerRoller());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new VillagerBreeder());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new ChunkLoader());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new Alerts());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoObsidian());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoSort());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new InstantHoleFill());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new InstantSelfFill());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new WebAuraRewritten());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AutoSandRewritten());
        meteordevelopment.meteorclient.systems.modules.Modules.get().add(new AntiCev());

        // Commands
        Commands.add(new CommandExample());
        Commands.add(new ReconnectCommand());

        // HUD
        Hud.get().register(HudExample.INFO);
        Hud.get().register(Health.INFO);
        Hud.get().register(Lister.INFO);
        Hud.get().register(SkeetLine.INFO);
        Hud.get().register(infobelowmark.INFO);
        Hud.get().register(Watermark.INFO);
    }
}
