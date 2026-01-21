package com.example.addon.Main.modules;

import com.example.addon.Hook;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;

public class ConstAntiQueue extends Module {

    public ConstAntiQueue() {
        super(Hook.CATEGORY, "ConstAntiQueue", "fuck the queue");
    }

    @EventHandler
    private void onMessageRecieve(ReceiveMessageEvent event) {
        if (event.getMessage()
                .getString().startsWith("🌌 You were sent to the queue.")) {
            mc.world.disconnect();
            mc.disconnect(new DisconnectedScreen(
                    new MultiplayerScreen(new TitleScreen()),
                    Text.of("Kicked"),
                    Text.literal("Constantiam Anti Queue")));
        }
    }
}
