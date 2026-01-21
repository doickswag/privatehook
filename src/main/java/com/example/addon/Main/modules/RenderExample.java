package com.example.addon.Main.modules;

import com.example.addon.Api.util.GANGTIL;
import com.example.addon.Api.util.RenderBlock;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RenderExample extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Renders");
    private final Setting<Boolean> render = this.setting("render", "Renders the block where it is placing a crystal.", Boolean.valueOf(false), this.sgRender);
    private final Setting<Integer> renderTime = this.setting("render-time", "Ticks to render the block for.", Integer.valueOf(8), this.sgRender, this.render::get);
    private final Setting<Boolean> fade = this.setting("fade", "Will reduce the opacity of the rendered block over time.", Boolean.valueOf(true), this.sgRender, this.render::get);
    private final Setting<ShapeMode> shapeMode = this.setting("shape-mode", "How the shapes are rendered.", ShapeMode.Both, this.sgRender, this.render::get);
    private final Setting<SettingColor> sideColor = this.setting("side-color", "The side color.", 0, 0, 255, 10, this.sgRender, this.render::get);
    private final Setting<SettingColor> lineColor = this.setting("line-color", "The line color.", 0, 0, 255, 200, this.sgRender, this.render::get);
    private float lastFrameDurationMs = 16.66F;
    private long lastFrameTimeNanos = -1L;
    private final List<RenderBlock> renderBlocks = Collections.synchronizedList(new ArrayList<>());

    public RenderExample() {
        super(Hook.CATEGORY, "RenderExample", "");
    }

    public void onActivate() {
        this.renderBlocks.clear();
        synchronized (this.renderBlocks) {
            this.renderBlocks.clear();
        }
    }

    @EventHandler
    public void onTick() {
        synchronized (this.renderBlocks) {
            RenderBlock.tick(this.renderBlocks);
        }
    }
    private boolean ExamplePlace(BlockPos pos) {
        if (this.render.get()) {
            synchronized (this.renderBlocks) {
                this.renderBlocks.add(new RenderBlock(pos, this.renderTime.get()));
            }
        }
        return false;
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
