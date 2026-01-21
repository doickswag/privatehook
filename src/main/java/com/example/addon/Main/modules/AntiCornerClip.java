package com.example.addon.Main.modules;

import com.example.addon.Api.Elite.PlayerDeathEvent;
import com.example.addon.Api.util.RenderBlock;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class AntiCornerClip extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Renders");
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to target players.")
        .defaultValue(5)
        .min(0)
        .sliderMax(7)
        .build());

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to face the block being placed.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> antiWaste = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-waste")
        .description("Stops targeting a player after a certain amount of torches are used.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> wasteAmount = sgGeneral.add(new IntSetting.Builder()
        .name("waste-amount")
        .description("The maximum number of torches to place on a single target before finding a new one.")
        .defaultValue(4)
        .min(1)
        .sliderMax(64)
        .visible(antiWaste::get)
        .build());

    private final Setting<Boolean> render = this.setting("render", "Renders the block where it is placing a crystal.", Boolean.valueOf(false), this.sgRender);
    private final Setting<Integer> renderTime = this.setting("render-time", "Ticks to render the block for.", Integer.valueOf(8), this.sgRender, this.render::get);
    private final Setting<Boolean> fade = this.setting("fade", "Will reduce the opacity of the rendered block over time.", Boolean.valueOf(true), this.sgRender, this.render::get);
    private final Setting<ShapeMode> shapeMode = this.setting("shape-mode", "How the shapes are rendered.", ShapeMode.Both, this.sgRender, this.render::get);
    private final Setting<SettingColor> sideColor = this.setting("side-color", "The side color.", 0, 0, 255, 10, this.sgRender, this.render::get);
    private final Setting<SettingColor> lineColor = this.setting("line-color", "The line color.", 0, 0, 255, 200, this.sgRender, this.render::get);
    private float lastFrameDurationMs = 16.66F;
    private long lastFrameTimeNanos = -1L;
    private final List<RenderBlock> renderBlocks = Collections.synchronizedList(new ArrayList<>());
    private PlayerEntity target;
    private final Map<UUID, Integer> wastedTorches = new HashMap<>();

    public AntiCornerClip() {
        super(Hook.CATEGORY, "AntiCornerclip", "nono niggas");
    }

    @Override
    public void onActivate() {
        target = null;
        wastedTorches.clear();
        this.renderBlocks.clear();
        synchronized (this.renderBlocks) {
            this.renderBlocks.clear();
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
        wastedTorches.clear();
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        wastedTorches.remove(event.getPlayer().getUuid());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        findTarget();
        if (target == null) return;
        synchronized (this.renderBlocks) {
            RenderBlock.tick(this.renderBlocks);
        }
        FindItemResult torch = InvUtils.findInHotbar(Items.REDSTONE_TORCH);
        if (!torch.found()) {
            return;
        }
        BlockPos targetPos = target.getBlockPos();
        if (!mc.world.getBlockState(targetPos).isReplaceable()) {
            return;
        }
        if (this.render.get()) {
            synchronized (this.renderBlocks) {
                this.renderBlocks.add(new RenderBlock(targetPos, this.renderTime.get()));
            }
        }
        boolean placed = BlockUtils.place(targetPos, torch, rotate.get(), 50);
        if (placed && antiWaste.get()) {
            int count = wastedTorches.getOrDefault(target.getUuid(), 0) + 1;
            wastedTorches.put(target.getUuid(), count);
            if (count >= wasteAmount.get()) {
                info("antiwaste ", EntityUtils.getName(target));
                target = null;
            }
        }
    }

    private void findTarget() {
        if (target != null && (!target.isAlive() || target.distanceTo(mc.player) > range.get() || Friends.get().isFriend(target))) {
            target = null;
        }
        if (target == null) {
            PlayerEntity bestTarget = null;
            double bestDistance = Double.MAX_VALUE;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player.equals(mc.player) || !player.isAlive() || Friends.get().isFriend(player)) {
                    continue;
                }
                double distance = player.distanceTo(mc.player);
                if (distance > range.get() || distance >= bestDistance) {
                    continue;
                }
                if (antiWaste.get() && wastedTorches.getOrDefault(player.getUuid(), 0) >= wasteAmount.get()) {
                    continue;
                }

                bestTarget = player;
                bestDistance = distance;
            }
            target = bestTarget;
        }
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        long now = System.nanoTime();
        if (this.lastFrameTimeNanos != -1L) {
            this.lastFrameDurationMs = (float) (now - this.lastFrameTimeNanos) / 1_000_000.0F;
        }

        this.lastFrameTimeNanos = now;

        if (this.render.get() && !this.renderBlocks.isEmpty()) {
            synchronized (this.renderBlocks) {
                for (RenderBlock block : this.renderBlocks) {
                    block.render(event, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), this.fade.get());
                }
            }
        }
    }
}
