package com.example.addon.Main.hud;

import com.example.addon.Api.Elite.PacketEvent;
import com.example.addon.Api.Elite.StopWatch;
import com.example.addon.Hook;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Watermark extends HudElement {
    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final Setting<String> clientName = sgGeneral.add(new StringSetting.Builder()
        .name("Custom Name")
        .description("Name to display.")
        .defaultValue("PrivateHook.CC")
        .build()
    );
    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("packets")
        .description("Should the list care about the info text length when sorting?")
        .defaultValue(true)
        .build()
    );
    private static int ping() {
        if (mc.getNetworkHandler() == null || mc.player == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? playerListEntry.getLatency() : 0;
    }
    public static final HudElementInfo<Watermark> INFO = new HudElementInfo<>(Hook.HUD_GROUP, "Watermark", "Watermark", Watermark::new);

    public Watermark() {
        super(INFO);
    }
    private final StopWatch packetTimer = new StopWatch.Single();

    int packets;
    int packetsServer;
    private final Setting<SettingColor> lineColor = sgGeneral.add((new ColorSetting.Builder())
        .name("line color")
        .description("color wow")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add((new ColorSetting.Builder())
        .name("text color")
        .description("color wow")
        .defaultValue(new SettingColor())
        .build()
    );
    @EventHandler(priority = 1)
    public void onPacketSend(PacketEvent.Send event) {
        packets++;
    }

    @EventHandler(priority = 2)
    public void onPacketSend(PacketEvent.Receive event) {
        packetsServer++;
    }
    @Override
    public void render(HudRenderer renderer) {
        if (packetTimer.hasBeen(1000)) {
            packets = 0;
            packetsServer = 0;
            packetTimer.reset();
        }
        int sent = packets;
        int received = packetsServer;
        int fps = mc.getCurrentFps();
        int tps = (int) TickRate.INSTANCE.getTickRate();
        int ping = ping();
        String text = packet.get() ? String.format("%s | Packets: (Sent: %d) (Received: %d) | %d Fps", clientName.get(), sent, received, fps) : String.format("%s | %s | %d FPS | %d TPS | %d ms", clientName.get(), Hook.Version, fps, tps, ping);
        int textWidth = (int) renderer.textWidth(text, true);
        int textHeight = (int) renderer.textHeight(true);

        int paddingX = 6;
        int paddingY = 5;
        int underlineHeight = 2;
        Color lcolor = lineColor.get();
        Color tcolor = textColor.get();

        int outerWidth = textWidth + paddingX * 2;
        int outerHeight = textHeight + paddingY * 2;

        setSize(outerWidth, outerHeight);

//        renderer.quad(x - backgroundOffset, y - backgroundOffset, outerWidth + backgroundOffset * 2, outerHeight + backgroundOffset * 2, new Color(50, 50, 50, 255));
        renderer.quad(x, y, outerWidth, outerHeight, new Color(0, 0, 0, 255));
        renderer.quad(x, y - underlineHeight, outerWidth, underlineHeight, lcolor);
        renderer.text(text, x + paddingX, y + paddingY, tcolor, true);
        renderer.text(clientName.get(), x + paddingX, y + paddingY, tcolor, true);
    }

}
