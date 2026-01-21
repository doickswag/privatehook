package com.example.addon.Main.modules;

import com.example.addon.Api.util.TextUtils;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class BASELOG extends Hooked {

    public BASELOG() {
        super(Hook.CATEGORY, "coordreply", "reply wi la coorc");
    }

    @EventHandler
    private void onMessageRecieve(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();

        if (message.contains(" whispers to you: coords")) {
            String name = message.substring(0, message.indexOf(" whispers to you:"));
            PlayerEntity nigger = null;
            if (mc.world != null) {
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (player.getGameProfile().getName().equals(name)) {
                        nigger = player;
                        break;
                    }
                }
            }
            if (nigger != null && Friends.get().isFriend(nigger)) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                String coords = Math.round(x) + " " + Math.round(y) + " " + Math.round(z);
                TextUtils.messagePlayer(name, coords);
            }
        }
    }
}
