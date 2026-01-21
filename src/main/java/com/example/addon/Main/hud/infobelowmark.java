package com.example.addon.Main.hud;

import com.example.addon.Hook;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class infobelowmark extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static final HudElementInfo<infobelowmark> INFO = new HudElementInfo<>(Hook.HUD_GROUP, "InfoBelowMark", "HUD element example.", infobelowmark::new);

    private final Setting<Boolean> watermark = sgGeneral.add(new BoolSetting.Builder()
        .name("Watermark")
        .description("Displays a watermark above the modules.")
        .defaultValue(false)
        .build()
    );
    private final Setting<String> clientName = sgGeneral.add(new StringSetting.Builder()
        .name("Custom Name")
        .description("Name to display.")
        .defaultValue("PrivateHook.cc")
        .build()
    );
    private final Setting<Double> playerrange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Player Range")
        .description("Max distance to detect players.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 12)
        .build()
    );
    private final Setting<Double> placerange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description("Max distance to detect place target or hole.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 12)
        .build()
    );

    public infobelowmark() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        float lineHeight = (float) (renderer.textHeight(true) + 2);
        if (mc.world == null || mc.player == null) return;

        if (watermark.get()) {
            renderer.text(clientName.get(), x, y, new Color(255, 255, 255, 255), true, 1);
            y += lineHeight;
        }

        boolean htr = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .anyMatch(p -> mc.player.squaredDistanceTo(p) <= Math.pow(playerrange.get(), 2));
        renderer.text("HTR", x, y, htr ? Color.GREEN : Color.RED, true, 1);
        y += lineHeight;

        boolean plr = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .anyMatch(p -> mc.player.squaredDistanceTo(p) <= Math.pow(placerange.get(), 2));
        renderer.text("PLR", x, y, plr ? Color.GREEN : Color.RED, true, 1);
        y += lineHeight;


        int totems = 0;
        for (int i = 0; i <= 45; ++i) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totems++;
            }
        }
        if (mc.player.getInventory().getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            totems++;
        }
        renderer.text(Integer.toString(totems), x, y, totems != 0 ? Color.GREEN : Color.RED, true, 1);
        y += lineHeight;
        int ping = EntityUtils.getPing((PlayerEntity) mc.player);

        renderer.text("PING " + ping, x, y, ping <= 100 ? Color.GREEN : Color.RED, true, 1);
        boolean lby = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .anyMatch(p -> mc.player.squaredDistanceTo(p) <= Math.pow(playerrange.get(), 2)
                && isInObbyHole(p));
        y += lineHeight;
        renderer.text("LBY", x, y, lby ? Color.GREEN : Color.RED, true, 1);
        y += lineHeight;

        int textWidth = (int) renderer.textWidth(String.valueOf(y), true);
        int textHeight = (int) renderer.textHeight(true);
        int outerWidth = textWidth;
        int outerHeight = textHeight;
        setSize(outerWidth, outerHeight);

    }

    private boolean isInObbyHole(Entity entity) {
        BlockPos pos = entity.getBlockPos();
        if (mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN) return false;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock() != Blocks.OBSIDIAN) return false;
        }
        return true;
    }
}
