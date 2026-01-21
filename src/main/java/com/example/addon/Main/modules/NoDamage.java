package com.example.addon.Main.modules;

import com.example.addon.Api.util.CalculationUtil;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class NoDamage extends Hooked {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final CalculationUtil calc = new CalculationUtil();

    private final Setting<Double> health = sgGeneral.add(new DoubleSetting.Builder()
        .name("Health")
        .description("throw le pot at health")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 36)
        .build()
    );

    private final Setting<Keybind> forceThrowBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("ForcePot")
        .description("says it in tha name")
        .defaultValue(Keybind.none())
        .build()
    );
    private final Setting<Boolean> lookDown = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("nigga")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> hotbarSlot = sgGeneral.add(new IntSetting.Builder()
        .name("PotionSlot")
        .description("move le potion to tha slot")
        .defaultValue(5)
        .range(1, 9)
        .sliderRange(1, 9)
        .build()
    );
    private final Setting<Integer> throwDelay = sgGeneral.add(new IntSetting.Builder()
        .name("ThrowDelay")
        .description("deklaaty")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 40)
        .build()
    );
    private final Setting<Boolean> holeCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("HoleCheck")
        .description("yo nigger")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> enemyRangeCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("EnemyCheck")
        .description("checks if opps near")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> enemyRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("EnemyRange")
        .description("le opps no get la nodamage")
        .defaultValue(6.0)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting<Boolean> friendCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("Friends")
        .description("bloodgang")
        .defaultValue(true)
        .build()
    );

    private int delay;

    public NoDamage() {
        super(Hook.CATEGORY, "No Damage", "Prevent opp attacks using cum potions");
    }


    @Override
    public void onActivate() {
        delay = 0;
        calc.start();
    }

    @Override
    public void onDeactivate() {
        calc.reset();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (delay > 0) {
            delay--;
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;
        if (holeCheck.get() && !PlayerUtils.isInHole(false)) {
            return;
        }
        if (enemyRangeCheck.get()) {
            for (PlayerEntity other : mc.world.getPlayers()) {
                if (other == player) continue;
                if (player.squaredDistanceTo(other) <= enemyRange.get() * enemyRange.get()) {
                    if (friendCheck.get() && Friends.get().isFriend(other)) {
                        continue;
                    }
                    return;
                }
            }
        }
        if (player.getHealth() > health.get() && !forceThrowBind.get().isPressed()) return;
        FindItemResult potion = InvUtils.find(Items.SPLASH_POTION);
        if (!potion.found()) return;

        FindItemResult hotbarPotion = InvUtils.findInHotbar(Items.SPLASH_POTION);
        if (!hotbarPotion.found()) {
            InvUtils.move().from(potion.slot()).toHotbar(hotbarSlot.get() - 1);
            hotbarPotion = InvUtils.findInHotbar(Items.SPLASH_POTION);
            if (!hotbarPotion.found()) return;
        }
        if (lookDown.get()) {
            FindItemResult finalHotbarPotion = hotbarPotion;
            calc.calculate();
            Rotations.rotate(player.getYaw(), 90, () -> throwPotion(finalHotbarPotion));
        } else {
            calc.calculate();
            throwPotion(hotbarPotion);
        }
        delay = throwDelay.get();
    }

    private void throwPotion(FindItemResult hotbarPotion) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int prevSlot = mc.player.getInventory().selectedSlot;
        if (hotbarPotion.isOffhand()) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            calc.reset();
        } else {
            InvUtils.swap(hotbarPotion.slot(), false);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            InvUtils.swap(prevSlot, false);
            calc.reset();
        }
    }
    @Override
    public String getInfoString() {
        return String.valueOf(calc.getCalculationTime());
    }
}
