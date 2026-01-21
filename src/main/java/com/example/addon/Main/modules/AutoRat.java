package com.example.addon.Main.modules;

import com.example.addon.Api.util.Timer;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoRat extends Hooked {
    private static final long GREEN_DURATION_MS = 10000;
    private final Map<UUID, Timer> popTimers = new HashMap<>();

    public AutoRat() {
        super(Hook.CATEGORY, "AUTORAT", "Rats le opps");
    }

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Ratting Range")
        .description("Distance to rat")
        .defaultValue(3)
        .range(1, 10)
        .sliderMax(10)
        .build()
    );

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet &&
            packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING &&
            packet.getEntity(mc.world) instanceof PlayerEntity player) {
            popTimers.put(player.getUuid(), new Timer());
            popTimers.get(player.getUuid()).reset();
        }
    }

    @Override
    public String getInfoString() {
        StringBuilder sb = new StringBuilder();
        if (mc.world == null || mc.player == null) return "";
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            double dist = mc.player.distanceTo(player);
            if (dist <= range.get()) {
                UUID uuid = player.getUuid();
                String name = player.getName().getString();
                Timer timer = popTimers.get(uuid);
                String colored;
                if (timer != null && !timer.passedMs(GREEN_DURATION_MS)) {
                    colored = Formatting.GREEN + name + Formatting.RESET;
                } else {
                    colored = Formatting.RED + name + Formatting.RESET;
                }

                if (sb.length() > 0) sb.append(", ");
                sb.append(colored);
            }
        }
        return sb.length() > 0 ? sb.toString() : "";
    }
}
