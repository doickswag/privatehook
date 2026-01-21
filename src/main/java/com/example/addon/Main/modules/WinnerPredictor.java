package com.example.addon.Main.modules;

import com.example.addon.Api.util.Wrapper;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

public class WinnerPredictor extends Hooked {
    private PlayerEntity opp = null;

    public WinnerPredictor() {
        super(Hook.CATEGORY, "Winner Predictor", "ez");
    }


    @EventHandler
    private void onTick() {
        if (mc.world == null || mc.player == null) return;

        opp = null;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || Friends.get().isFriend(player)) continue;
            if (mc.player.distanceTo(player) <= 3) {
                opp = player;
                break;
            }
        }
    }

    @Override
    public String getInfoString() {
        if (opp == null) return null;
        String status = PlayerUtils.getTotalHealth() > Wrapper.getTotalHealth(opp) ? Formatting.GREEN + "Win" : Formatting.RED + "Lose";
        if (Wrapper.isLagging()) {
            status += Formatting.GRAY + " | " + Formatting.YELLOW + "Lag";
        }
        return status;
    }
}
