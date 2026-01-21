package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;

public class VehicleOneHit extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("The number of packets to send.")
        .defaultValue(16)
        .range(1, 100)
        .sliderRange(1, 20)
        .build()
    );

    private boolean ignorePackets;

    public VehicleOneHit() {
        super(Hook.CATEGORY, "vehicle-one-hit", "Destroy vehicles with one hit.");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (ignorePackets
            || !(event.packet instanceof PlayerInteractEntityC2SPacket)
            || !(mc.crosshairTarget instanceof EntityHitResult ehr)
            || (!(ehr.getEntity() instanceof AbstractMinecartEntity) && !(ehr.getEntity() instanceof BoatEntity))
        ) return;

        ignorePackets = true;
        for (int i = 0; i < amount.get() - 1; i++) {
            mc.player.networkHandler.sendPacket(event.packet);
        }
        ignorePackets = false;
    }

    @Override
    public String getInfoString() {
        return amount.get().toString();
    }
}
