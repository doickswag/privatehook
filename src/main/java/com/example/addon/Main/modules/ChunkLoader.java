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
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Optional;

public class ChunkLoader extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<EntityTypeSelection> entityType = sgGeneral.add(new EnumSetting.Builder<EntityTypeSelection>()
        .name("entity type")
        .description("The type of entity to use for loading chunks.")
        .defaultValue(EntityTypeSelection.Boat)
        .build()
    );

    private final Setting<Boolean> autoPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("auto place")
        .description("Automatically places the entity if it's not found near the portal.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> searchRange = sgGeneral.add(new IntSetting.Builder()
        .name("search range")
        .description("The radius to search for a nether portal.")
        .defaultValue(8)
        .min(4)
        .sliderMax(16)
        .build()
    );

    private final Setting<Integer> pushDelay = sgGeneral.add(new IntSetting.Builder()
        .name("push delay")
        .description("The delay in ticks between pushing the entity into the portal.")
        .defaultValue(200) // 10 seconds
        .min(100)
        .sliderMax(600)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates when placing or interacting with the entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders highlights around the portal and entity.")
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

    private final Setting<SettingColor> portalColor = sgRender.add(new ColorSetting.Builder()
        .name("portal color")
        .description("The color to render around the nether portal.")
        .defaultValue(new SettingColor(148, 0, 211, 75))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> entityColor = sgRender.add(new ColorSetting.Builder()
        .name("entity color")
        .description("The color to render around the target entity.")
        .defaultValue(new SettingColor(255, 165, 0, 75))
        .visible(render::get)
        .build()
    );

    public enum EntityTypeSelection {
        Boat(EntityType.BOAT, Items.OAK_BOAT),
        Minecart(EntityType.MINECART, Items.MINECART);

        public final EntityType<?> type;
        public final Item item;

        EntityTypeSelection(EntityType<?> type, Item item) {
            this.type = type;
            this.item = item;
        }
    }

    private enum Stage {
        Searching,
        Placing,
        Pushing,
        Waiting,
        Error
    }

    private Stage stage;
    private BlockPos portalPos;
    private Entity targetEntity;
    private int timer;

    public ChunkLoader() {
        super(Hook.CATEGORY, "chunk loader", "Keeps chunks loaded by sending an entity through a portal.");
    }

    @Override
    public void onActivate() {
        stage = Stage.Searching;
        portalPos = null;
        targetEntity = null;
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        switch (stage) {
            case Searching -> {
                findPortal();
                if (portalPos == null) {
                    error("No nether portal found within %d blocks. Disabling.", searchRange.get());
                    stage = Stage.Error;
                    toggle();
                    return;
                }

                findEntity();
                if (targetEntity == null) {
                    if (autoPlace.get()) {
                        info("No entity found, preparing to place one.");
                        stage = Stage.Placing;
                    } else {
                        error("No %s found near the portal. Enable 'auto place' or place one manually. Disabling.", entityType.get().name());
                        stage = Stage.Error;
                        toggle();
                    }
                    return;
                }

                info("Found portal and entity. Starting chunk loading cycle.");
                stage = Stage.Pushing;
                timer = pushDelay.get();
            }

            case Placing -> {
                FindItemResult itemResult = findEntityItem();
                if (!itemResult.found()) {
                    error("No %s in hotbar. Disabling.", entityType.get().item.getName().getString());
                    stage = Stage.Error;
                    toggle();
                    return;
                }

                BlockPos placePos = findPlacePos();
                if (placePos == null) {
                    error("Could not find a valid position to place the entity. Disabling.");
                    stage = Stage.Error;
                    toggle();
                    return;
                }

                BlockUtils.place(placePos, itemResult, rotate.get(), 0, true);
                info("Placed entity. Searching for it...");
                stage = Stage.Searching;
            }

            case Pushing -> {
                if (timer > 0) {
                    timer--;
                    return;
                }

                if (targetEntity == null || !targetEntity.isAlive()) {
                    info("Entity is gone. Restarting search...");
                    stage = Stage.Searching;
                    return;
                }

                if (isEntityInPortal(targetEntity)) {
                    stage = Stage.Waiting;
                    timer = 300;
                    return;
                }

                pushEntity();
                info("Pushed entity into the portal. Waiting for it to travel...");
                stage = Stage.Waiting;
                timer = 300;
            }

            case Waiting -> {
                if (timer > 0) {
                    timer--;
                    return;
                }

                info("Cycle complete. Resetting push timer.");
                stage = Stage.Pushing;
                timer = pushDelay.get();
            }

            case Error -> {
            }
        }
    }

    private void findPortal() {
        Optional<BlockPos> foundPortal = BlockPos.stream(mc.player.getBoundingBox().expand(searchRange.get()))
            .filter(pos -> mc.world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL))
            .min(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(pos.toCenterPos())));

        portalPos = foundPortal.orElse(null);
    }

    private void findEntity() {
        if (portalPos == null) return;
        Box searchBox = new Box(portalPos).expand(4);
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getType() == entityType.get().type && searchBox.intersects(entity.getBoundingBox())) {
                targetEntity = entity;
                return;
            }
        }
        targetEntity = null;
    }

    private FindItemResult findEntityItem() {
        Item itemToFind = entityType.get().item;
        if (itemToFind == Items.CHERRY_BOAT) {
            return InvUtils.findInHotbar(Items.CHERRY_BOAT);
        }
        return InvUtils.findInHotbar(itemToFind);
    }

    private BlockPos findPlacePos() {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos checkPos = portalPos.offset(direction);
            if (BlockUtils.canPlace(checkPos)) {
                return checkPos;
            }
        }
        return null;
    }

    private void pushEntity() {
        if (targetEntity == null || mc.player == null) return;
        Vec3d targetPos = targetEntity.getPos();
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(targetEntity), Rotations.getPitch(targetEntity), () -> {
                mc.player.setVelocity(direction.x * 0.1, mc.player.getVelocity().y, direction.z * 0.1);
            });
        } else {
            mc.player.setVelocity(direction.x * 0.1, mc.player.getVelocity().y, direction.z * 0.1);
        }
    }
    private boolean isEntityInPortal(Entity entity) {
        if (entity == null || mc.world == null) return false;
        BlockPos min = BlockPos.ofFloored(entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ);
        BlockPos max = BlockPos.ofFloored(entity.getBoundingBox().maxX, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            if (mc.world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                return true;
            }
        }
        return false;
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;

        if (portalPos != null) {
            event.renderer.box(portalPos, portalColor.get(), portalColor.get(), shapeMode.get(), 0);
        }

        if (targetEntity != null) {
            event.renderer.box(targetEntity.getBoundingBox(), entityColor.get(), entityColor.get(), shapeMode.get(), 0);
        }
    }

    @Override
    public String getInfoString() {
        return stage != null ? stage.name() : "Disabled";
    }
}
