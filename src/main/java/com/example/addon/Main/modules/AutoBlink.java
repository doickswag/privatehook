package com.example.addon.Main.modules;

import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBlink extends Hooked {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> leaveHoleBlink = sgGeneral.add(new BoolSetting.Builder()
        .name("Leave Hole Blink")
        .description("Enables blink when leaving a hole.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enterHoleBlink = sgGeneral.add(new BoolSetting.Builder()
        .name("Enter Hole Blink")
        .description("Disables blink when entering a hole.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> safeHoleBlink = sgGeneral.add(new BoolSetting.Builder()
        .name("Safe Hole Blink")
        .description("Disables blink if old hole is not valid.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("Notify")
        .description("")
        .defaultValue(false)
        .build()
    );

    private BlockPos currentPos;
    private BlockPos blinkHolePos;
    private BlinkPlus blinkModule;

    public AutoBlink() {
        super(Hook.CATEGORY, "AutoBlink", "blink in hole");
    }

    @Override
    public void onActivate() {
        blinkModule = Modules.get().get(BlinkPlus.class);
        if (mc.player != null) {
            currentPos = mc.player.getBlockPos();
        }
        blinkHolePos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        BlockPos prevPos = this.currentPos;
        this.currentPos = mc.player.getBlockPos();

        if (this.safeHoleBlink.get() && this.blinkHolePos != null && !isHole(this.blinkHolePos)) {
            if (blinkModule.isActive()) {
                blinkModule.toggle();
                if (notify.get()) {
                    info("Disabling Blink, hole is no longer safe.");
                }
            }
            this.blinkHolePos = null;
        }

        if (prevPos != null && !this.currentPos.equals(prevPos)) {
            boolean inCurrentHole = isHole(this.currentPos);
            boolean wasInPrevHole = isHole(prevPos);

            if (this.enterHoleBlink.get() && inCurrentHole && !wasInPrevHole) {
                if (blinkModule.isActive()) {
                    blinkModule.toggle();
                    if (notify.get()) {
                        info("Disabling Blink, entered a hole.");
                    }
                }
                this.blinkHolePos = null;
            }

            if (this.leaveHoleBlink.get() && !inCurrentHole && wasInPrevHole) {
                if (!blinkModule.isActive()) {
                    blinkModule.toggle();
                    if (notify.get()) {
                        info("Enabling Blink, left a hole.");
                    }
                }
                this.blinkHolePos = prevPos;
            }
        }
    }

    private boolean isHole(BlockPos blockPos) {
        if (blockPos == null) return false;
        int air = 0;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockState state = mc.world.getBlockState(blockPos.offset(direction));

            if (state.getBlock().getBlastResistance() < 600) {
                if (direction == Direction.DOWN) return false;
                air++;
                for (Direction dir : Direction.values()) {
                    if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                    BlockState blockState1 = mc.world.getBlockState(blockPos.offset(direction).offset(dir));

                    if (blockState1.getBlock().getBlastResistance() < 600) return false;
                }
            }
        }
        return air < 2;
    }
}
