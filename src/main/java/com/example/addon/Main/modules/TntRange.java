package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;

public class TntRange extends Hooked {

    public TntRange() {
        super(Hook.CATEGORY, "CrystalRange", "Renders the damage range of ignited tnt");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<ShapeMode> renderType = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("mode")
        .defaultValue(ShapeMode.Lines)
        .build()
    );
    private final Setting<SettingColor> renderColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .defaultValue(new SettingColor(255, 170, 0, 255))
        .build()
    );

    @EventHandler
    public void onRender3dEvent(Render3DEvent event) {
        for (Entity endcrystal : mc.world.getEntities()) {
            if (endcrystal instanceof EndCrystalEntity endCrystalEntity) {
                BlockPos endcrsyatlpos = endCrystalEntity.getBlockPos();
                event.renderer.box(endcrsyatlpos.getX() - 5.2, endcrsyatlpos.getY() - 5.2, endcrsyatlpos.getZ() - 5.2, endcrsyatlpos.getX() + 5.2, endcrsyatlpos.getY() + 5.2, endcrsyatlpos.getZ() + 5.2, new Color(renderColor.get().r, renderColor.get().g, renderColor.get().b, renderColor.get().a), new Color(renderColor.get().r, renderColor.get().g, renderColor.get().b, renderColor.get().a), renderType.get(), 0);
            }
        }
    }
}
