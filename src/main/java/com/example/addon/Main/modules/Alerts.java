package com.example.addon.Main.modules;

import com.example.addon.Api.util.SystemNotification;
import com.example.addon.Hook;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class Alerts extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to alert you about.")
        .defaultValue(new HashSet<>())
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to detect entities.")
        .defaultValue(10.0)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Set<Entity> alertedEntities = new HashSet<>();

    public Alerts() {
        super(Hook.CATEGORY, "alerts", "Notifies you of various events.");
    }

    @Override
    public void onDeactivate() {
        alertedEntities.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null || entities.get().isEmpty()) return;
        alertedEntities.removeIf(entity -> !entity.isAlive() || entity.distanceTo(mc.player) > range.get() + 2);
        for (Entity entity : mc.world.getEntities()) {
            if (entities.get().contains(entity.getType()) && entity.distanceTo(mc.player) <= range.get() && !alertedEntities.contains(entity)) {
                sendNotification(entity);
            }
        }
    }

    private void sendNotification(Entity entity) {
        String entityName = Names.get(entity.getType());
        SystemNotification.send("Entity Alert", "A " + entityName + " is nearby!");
        alertedEntities.add(entity);
    }
}
