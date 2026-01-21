package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class BlinkPlus extends Hooked {
    public enum BlinkMode {
        Normal,
        Damage
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<BlinkMode> blinkMode = sgGeneral.add(new EnumSetting.Builder<BlinkMode>()
        .name("mode")
        .description("When to hold packets.")
        .defaultValue(BlinkMode.Normal)
        .build()
    );

    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("packets")
        .description("Disables after sending this many packets. 0 to disable.")
        .defaultValue(20)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> ticks = sgGeneral.add(new IntSetting.Builder()
        .name("ticks")
        .description("Disables after this many ticks. 0 to disable.")
        .defaultValue(0)
        .min(0)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> renderShape = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shapemode")
        .description("")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("linecolor")
        .description("")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("sidecolor")
        .description("")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .visible(render::get)
        .build()
    );
    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("Notify")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final List<Packet<?>> storedPackets = new ArrayList<>();
    private int ticksPassed = 0;
    private Box renderBox = null;

    public BlinkPlus() {
        super(Hook.CATEGORY, "BlinkPlus", "fakelage");
    }

    @Override
    public void onActivate() {
        ticksPassed = 0;
        storedPackets.clear();
        if (mc.player != null) {
            renderBox = mc.player.getBoundingBox();
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.getNetworkHandler() != null) {
            for (Packet<?> packet : storedPackets) {
                mc.getNetworkHandler().sendPacket(packet);
            }
        }
        storedPackets.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ticksPassed++;
        if (ticks.get() > 0 && ticksPassed >= ticks.get()) {
            if (notify.get()) {
                ChatUtils.info("Blink reached tick limit, disabling.");
            }
            toggle();
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || !(event.packet instanceof PlayerMoveC2SPacket)) {
            return;
        }

        if (shouldDelay()) {
            storedPackets.add(event.packet);
            event.cancel();

            if (packets.get() > 0 && storedPackets.size() >= packets.get()) {
                if (notify.get()) {
                    ChatUtils.info("Blink reached packet limit, disabling.");
                }
                toggle();
            }
        }
    }

    private boolean shouldDelay() {
        if (mc.player == null) return false;

        return switch (blinkMode.get()) {
            case Normal -> true;
            case Damage -> mc.player.hurtTime > 0;
        };
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render.get() && renderBox != null) {
            event.renderer.box(renderBox, sideColor.get(), lineColor.get(), renderShape.get(), 0);
        }
    }

    @Override
    public String getInfoString() {
        if (packets.get() > 0) {
            return String.format("%d / %d", storedPackets.size(), packets.get());
        }
        return String.valueOf(storedPackets.size());
    }
}
