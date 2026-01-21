package com.example.addon.Main.modules;

import com.example.addon.Hook;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VillagerBreeder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The radius to search for villagers.")
        .defaultValue(10)
        .min(1)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> foodAmount = sgGeneral.add(new IntSetting.Builder()
        .name("food amount")
        .description("Amount of food to give each villager.")
        .defaultValue(12)
        .min(1)
        .sliderMax(64)
        .build()
    );
    private final Setting<Food> foodType = sgGeneral.add(new EnumSetting.Builder<Food>()
        .name("food type")
        .description("The type of food to give to the villagers.")
        .defaultValue(Food.Bread)
        .onChanged(food -> foodAmount.set(food.defaultAmount))
        .build()
    );

    private final Setting<Integer> throwDelay = sgGeneral.add(new IntSetting.Builder()
        .name("throw delay")
        .description("The delay in ticks between throwing each food item.")
        .defaultValue(4)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> breedingTimeout = sgGeneral.add(new IntSetting.Builder()
        .name("breeding timeout")
        .description("The time in ticks to wait for a baby to appear before stopping.")
        .defaultValue(600)
        .min(100)
        .sliderMax(1200)
        .build()
    );

    private final Setting<Boolean> checkBeds = sgGeneral.add(new BoolSetting.Builder()
        .name("check beds")
        .description("Checks if there are enough beds for the villagers to breed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> minBeds = sgGeneral.add(new IntSetting.Builder()
        .name("min beds")
        .description("The minimum number of beds required. Villagers need one free bed to make a baby.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .visible(checkBeds::get)
        .build()
    );

    private final Setting<Integer> bedCheckRadius = sgGeneral.add(new IntSetting.Builder()
        .name("bed check radius")
        .description("The radius to check for beds around the villagers.")
        .defaultValue(16)
        .min(1)
        .sliderMax(32)
        .visible(checkBeds::get)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto disable")
        .description("Disables the module after a baby is born or if it fails.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to face the villagers when throwing food.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders highlights.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> villagerColor = sgRender.add(new ColorSetting.Builder()
        .name("villager color")
        .description("The color to render around the target villagers.")
        .defaultValue(new SettingColor(80, 255, 80, 75))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> activeVillagerColor = sgRender.add(new ColorSetting.Builder()
        .name("active villager color")
        .description("The color to render around the villager currently being fed.")
        .defaultValue(new SettingColor(255, 255, 80, 150))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> bedColor = sgRender.add(new ColorSetting.Builder()
        .name("bed color")
        .description("The color to render around the required beds.")
        .defaultValue(new SettingColor(255, 80, 80, 75))
        .visible(() -> render.get() && checkBeds.get())
        .build()
    );

    public enum Food {
        Bread(Items.BREAD, 3),
        Carrot(Items.CARROT, 12),
        Potato(Items.POTATO, 12),
        Beetroot(Items.BEETROOT, 12);

        public final Item item;
        public final int defaultAmount;

        Food(Item item, int defaultAmount) {
            this.item = item;
            this.defaultAmount = defaultAmount;
        }
    }

    private enum Stage {
        Searching,
        Feeding,
        Waiting,
        Done
    }

    private Stage stage;
    private final List<VillagerEntity> targetVillagers = new ArrayList<>();
    private VillagerEntity currentFeedingTarget;
    private int foodThrownCount;
    private int throwTimer;
    private final List<BlockPos> beds = new ArrayList<>();
    private int timeoutTimer;
    private int initialVillagerCount;

    public VillagerBreeder() {
        super(Hook.CATEGORY, "villager-breeder", "Automatically breeds villagers by throwing food at them.");
    }

    @Override
    public void onActivate() {
        stage = Stage.Searching;
        throwTimer = 0;
        timeoutTimer = 0;
        targetVillagers.clear();
        currentFeedingTarget = null;
        foodThrownCount = 0;
        beds.clear();
        initialVillagerCount = 0;
    }

    @Override
    public void onDeactivate() {
        targetVillagers.clear();
        beds.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        switch (stage) {
            case Searching -> {
                if (!findTargets()) {
                    if (autoDisable.get()) {
                        error("Could not find valid targets. Disabling.");
                        toggle();
                    }
                    return;
                }
                info("Found %d villagers and %d beds. Proceeding to feed.", targetVillagers.size(), beds.size());
                currentFeedingTarget = targetVillagers.get(0);
                foodThrownCount = 0;
                stage = Stage.Feeding;
            }
            case Feeding -> {
                if (throwTimer > 0) {
                    throwTimer--;
                    return;
                }

                if (currentFeedingTarget == null || !currentFeedingTarget.isAlive() || targetVillagers.stream().anyMatch(v -> v == null || !v.isAlive())) {
                    error("A target villager is no longer valid. Restarting search.");
                    stage = Stage.Searching;
                    return;
                }

                if (currentFeedingTarget.isReadyToBreed()) {
                    if (currentFeedingTarget.equals(targetVillagers.get(0))) {
                        info("First villager is ready to breed. Moving to the second.");
                        currentFeedingTarget = targetVillagers.get(1);
                        foodThrownCount = 0;
                        return;
                    } else {
                        info("Both villagers are ready to breed. Waiting...");
                        stage = Stage.Waiting;
                        timeoutTimer = breedingTimeout.get();
                        initialVillagerCount = (int) StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                            .filter(e -> e instanceof VillagerEntity && e.distanceTo(mc.player) <= range.get() * 2)
                            .count();
                        return;
                    }
                }

                if (foodThrownCount >= foodAmount.get()) {
                    return;
                }

                FindItemResult food = InvUtils.findInHotbar(foodType.get().item);
                if (!food.found()) {
                    error("No %s found in hotbar. Disabling.", foodType.get().item.getName().getString());
                    if (autoDisable.get()) toggle();
                    stage = Stage.Done;
                    return;
                }

                throwSingleFood(food, currentFeedingTarget);
                foodThrownCount++;
                throwTimer = throwDelay.get();
            }
            case Waiting -> {
                if (timeoutTimer-- <= 0) {
                    error("Breeding timed out. Disabling.");
                    if (autoDisable.get()) toggle();
                    stage = Stage.Done;
                    return;
                }

                if (targetVillagers.stream().anyMatch(v -> !v.isReadyToBreed())) {
                    error("A villager is no longer ready to breed. Restarting process.");
                    stage = Stage.Searching;
                    return;
                }

                long currentVillagerCount = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                    .filter(e -> e instanceof VillagerEntity && e.distanceTo(mc.player) <= range.get() * 2)
                    .count();

                if (currentVillagerCount > initialVillagerCount) {
                    info("Successfully bred a new villager!");
                    if (autoDisable.get()) toggle();
                    stage = Stage.Done;
                }
            }
            case Done -> {
                // Do nothing
            }
        }
    }

    private boolean findTargets() {
        targetVillagers.clear();
        beds.clear();

        List<VillagerEntity> potentialVillagers = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
            .filter(e -> e instanceof VillagerEntity && e.isAlive() && mc.player.distanceTo(e) <= range.get())
            .map(e -> (VillagerEntity) e)
            .filter(v -> !v.isBaby() && v.canGather(new ItemStack(foodType.get().item)))
            .sorted(Comparator.comparingDouble(v -> v.distanceTo(mc.player)))
            .collect(Collectors.toList());

        if (potentialVillagers.size() < 2) {
            error("Not enough villagers in range (%d found).", potentialVillagers.size());
            return false;
        }

        targetVillagers.add(potentialVillagers.get(0));
        targetVillagers.add(potentialVillagers.get(1));

        if (checkBeds.get()) {
            Vec3d centerPoint = targetVillagers.get(0).getPos().add(targetVillagers.get(1).getPos()).multiply(0.5);
            BlockPos centerBlockPos = BlockPos.ofFloored(centerPoint);

            for (BlockPos pos : BlockPos.iterate(centerBlockPos.add(-bedCheckRadius.get(), -bedCheckRadius.get(), -bedCheckRadius.get()),
                centerBlockPos.add(bedCheckRadius.get(), bedCheckRadius.get(), bedCheckRadius.get()))) {
                if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                    beds.add(pos.toImmutable());
                }
            }

            if (beds.size() < minBeds.get()) {
                error("Not enough beds found. Required: %d, Found: %d.", minBeds.get(), beds.size());
                targetVillagers.clear();
                return false;
            }
        }
        return true;
    }

    private void throwSingleFood(FindItemResult food, VillagerEntity villager) {
        Runnable throwAction = () -> {
            InvUtils.swap(food.slot(), true);
            mc.player.dropSelectedItem(false);
            InvUtils.swapBack();
        };

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(villager), Rotations.getPitch(villager), throwAction);
        } else {
            throwAction.run();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;

        for (VillagerEntity villager : targetVillagers) {
            boolean isActive = stage == Stage.Feeding && villager.equals(currentFeedingTarget);
            SettingColor color = isActive ? activeVillagerColor.get() : villagerColor.get();
            event.renderer.box(villager.getBoundingBox(), color, color, shapeMode.get(), 0);
        }

        if (checkBeds.get()) {
            for (BlockPos bedPos : beds) {
                event.renderer.box(bedPos, bedColor.get(), bedColor.get(), shapeMode.get(), 0);
            }
        }
    }

    @Override
    public String getInfoString() {
        if (stage == null) return "Disabled";
        if (stage == Stage.Feeding && currentFeedingTarget != null) {
            return "Feeding " + (targetVillagers.indexOf(currentFeedingTarget) + 1);
        }
        return stage.name();
    }
}
