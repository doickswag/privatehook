package com.example.addon.Main.modules;

import com.example.addon.Api.util.TextUtils;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class AutoEz extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMessages = settings.createGroup("EZ Messages");
    private final SettingGroup sgKillstreak = settings.createGroup("Killstreak");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("How close a player needs to be for the message to send.")
        .defaultValue(30)
        .min(0)
        .sliderMax(100)
        .build());

    private final Setting<Boolean> dms = sgGeneral.add(new BoolSetting.Builder()
        .name("direct-messages")
        .description("Whether to send the EZ message as a DM to the victim.")
        .defaultValue(false)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between EZ messages.")
        .defaultValue(100)
        .min(0)
        .sliderMax(200)
        .build());

    private final Setting<List<String>> messages = sgMessages.add(new StringListSetting.Builder()
        .name("ez-messages")
        .description("EZ messages to randomly pick from. Use {player} for their name.")
        .defaultValue(List.of(
            "{player} died to the power of PrivateHook!",
            "You can tell {player} isn't part of PrivateHook!",
            "EZ {player}. PrivateHook owns me and all!",
            "GG {player}! PrivateHook Over Everything!"
        ))
        .build());

    private final Setting<Boolean> addKsSuffix = sgKillstreak.add(new BoolSetting.Builder()
        .name("always-add-killstreak")
        .description("Append your killstreak to messages.")
        .defaultValue(false)
        .build());

    private final Setting<String> ksSuffixMsg = sgKillstreak.add(new StringSetting.Builder()
        .name("killstreak-suffix-message")
        .description("Use {ks} for number and {ksSuffix} for suffix.")
        .defaultValue(" | {ks}{ksSuffix} kill in a row!")
        .visible(addKsSuffix::get)
        .build());

    private final Setting<Boolean> sayKillStreak = sgKillstreak.add(new BoolSetting.Builder()
        .name("announce-killstreak")
        .description("Send a special message each X kills.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> ksCount = sgKillstreak.add(new IntSetting.Builder()
        .name("each-x-messages")
        .description("Kills before sending special killstreak message.")
        .defaultValue(5)
        .min(1)
        .sliderMax(10)
        .visible(sayKillStreak::get)
        .build());

    private final Setting<String> ksMsg = sgKillstreak.add(new StringSetting.Builder()
        .name("killstreak-message")
        .description("Use {ks} and {ksSuffix} here.")
        .defaultValue("EZ. {ks}{ksSuffix} kill in a row. Privatehook on top.")
        .visible(sayKillStreak::get)
        .build());

    private int delayLeft;
    private final List<PlayerEntity> deadPlayers = new ArrayList<>();

    public AutoEz() {
        super(Hook.CATEGORY, "AutoEZ", "Automatically sends a message in chat when you kill someone.");
    }

    @Override
    public void onActivate() {
        delayLeft = 0;
        deadPlayers.clear();
    }

    @Override
    public void onDeactivate() {
        deadPlayers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (delayLeft > 0) {
            delayLeft--;
            return;
        }
        deadPlayers.removeIf(player -> player.isRemoved() || player.getHealth() > 0);
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (isTarget(player)) {
                deadPlayers.add(player);
                triggerEzMessage(player);
                break;
            }
        }
    }

    private boolean isTarget(PlayerEntity player) {
        if (player == null) return false;
        return !player.equals(mc.player) &&
            !Friends.get().isFriend(player) &&
            mc.player.distanceTo(player) <= range.get() &&
            player.getHealth() <= 0 &&
            !deadPlayers.contains(player);
    }

    private void triggerEzMessage(PlayerEntity victim) {
        if (messages.get().isEmpty()) {
            warning("empty");
            return;
        }
        delayLeft = delay.get();
        String victimName = victim.getGameProfile().getName();
        int ks = Hook.STATS.getKillStreak();
        String ksSuffix = getSuffix(ks);

        String msg;
        if (sayKillStreak.get() && ks > 0 && ks % ksCount.get() == 0) {
            msg = ksMsg.get();
        } else {
            msg = TextUtils.getNewMessage(messages.get());
            if (addKsSuffix.get()) msg += ksSuffixMsg.get();
        }

        msg = msg.replace("{player}", victimName)
            .replace("{pops}", String.valueOf(Hook.STATS.getTotemPops(victim)))
            .replace("{totem}", Hook.STATS.getTotemPops(victim) == 1 ? "totem" : "totems")
            .replace("{ks}", String.valueOf(ks))
            .replace("{ksSuffix}", ksSuffix);

        if (dms.get()) {
            TextUtils.sendNewMessage("/msg " + victimName + " " + msg);
        } else {
            TextUtils.sendNewMessage(msg);
        }
    }

    private String getSuffix(int n) {
        if (n >= 11 && n <= 13) return "th";
        return switch (n % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
