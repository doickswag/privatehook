package com.example.addon;

import com.example.addon.Api.util.Statistics;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Hooked extends Module {
    private final String prefix = Formatting.DARK_PURPLE + "«" + Formatting.RESET + Formatting.LIGHT_PURPLE + "PrivateHook" + Formatting.RESET + Formatting.DARK_PURPLE + "»";
    public int priority;
    protected final SettingGroup sgGeneral;
    public boolean nullCheck() {
        return mc.world == null || mc.player == null;
    }

    public Hooked(Category category, String name, String description) {
        super(category, name, description);
        this.sgGeneral = this.settings.getDefaultGroup();

    }

    //  Messages
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.BOLD + (isActive() ? Formatting.GREEN +  name : Formatting.RED + name);
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.BOLD + (isActive() ? Formatting.GREEN +  name : Formatting.RED + name) + Formatting.GRAY + message;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.BOLD + Formatting.RED + name + " " + Formatting.GRAY + text;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendHookInfo(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + text;
            sendMessage(Text.of(msg), Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + Formatting.AQUA + text;
            sendMessage(Text.of(msg), 0);
        }
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }


    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue) {
        return this.setting(name, description, defaultValue, this.sgGeneral);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final IVisible visible) {
        return this.setting(name, description, defaultValue, this.sgGeneral, visible, null, null);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final IVisible visible, final Consumer<T> onChanged, final Consumer<Setting<T>> onModuleActivated) {
        return this.setting(name, description, defaultValue, this.sgGeneral, visible, onChanged, onModuleActivated);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group) {
        return this.setting(name, description, defaultValue, group, null, null);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible) {
        return this.setting(name, description, defaultValue, group, visible, null);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final Consumer<T> onChanged) {
        return this.setting(name, description, defaultValue, group, null, onChanged);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final Consumer<T> onChanged) {
        return this.setting(name, description, defaultValue, group, visible, onChanged, null);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final Consumer<T> onChanged, final Consumer<Setting<T>> onModuleActivated) {
        if (defaultValue instanceof final Enum e8um) {
            if (e8um instanceof MyPotion) {
                return (Setting<T>)group.add((Setting)new PotionSetting(name, description, (MyPotion)defaultValue, (Consumer)onChanged, (Consumer)onModuleActivated, visible));
            }
            return (Setting<T>)group.add((Setting)new EnumSetting(name, description, (Enum)defaultValue, (Consumer)onChanged, (Consumer)onModuleActivated, visible));
        }
        else {
            if (defaultValue instanceof Boolean) {
                return (Setting<T>)group.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name(name)).description(description)).defaultValue((Boolean) defaultValue)).visible(visible)).onChanged((Consumer)onChanged)).onModuleActivated((Consumer)onModuleActivated)).build());
            }
            if (defaultValue instanceof final SettingColor settingColor) {
                return (Setting<T>)group.add((Setting)new ColorSetting(name, description, settingColor, (Consumer)onChanged, (Consumer)onModuleActivated, visible));
            }
            if (defaultValue instanceof final Keybind keybind) {
                return (Setting<T>)group.add((Setting)new KeybindSetting(name, description, keybind, (Consumer)onChanged, (Consumer)onModuleActivated, visible, (Runnable)null));
            }
            if (defaultValue instanceof final String s) {
                return (Setting<T>)group.add((Setting)new StringSetting(name, description, s, (Consumer)onChanged, (Consumer)onModuleActivated, visible, (Class) StarscriptTextBoxRenderer.class, (CharFilter)null, false));
            }
            if (defaultValue instanceof final BlockPos blockPos) {
                return (Setting<T>)group.add((Setting)new BlockPosSetting(name, description, blockPos, (Consumer)onChanged, (Consumer)onModuleActivated, visible));
            }
            if (defaultValue instanceof final Object2IntMap object2IntMap) {
                return (Setting<T>)group.add((Setting)new StatusEffectAmplifierMapSetting(name, description, (Reference2IntMap<StatusEffect>) object2IntMap, (Consumer)onChanged, (Consumer)onModuleActivated, visible));
            }
            if (defaultValue instanceof Integer) {
                return (Setting<T>)group.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name(name)).description(description)).defaultValue((Integer) defaultValue)).visible(visible)).onChanged((Consumer)onChanged)).onModuleActivated((Consumer)onModuleActivated)).build());
            }
            if (defaultValue instanceof Double) {
                return (Setting<T>)group.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(name)).description(description)).defaultValue((Double) defaultValue)).build());
            }
            return null;
        }
    }

    @SafeVarargs
    public final <T> Setting<List<T>> setting(final String name, final String description, final SettingGroup group, final IVisible visible, final T... defaultValue) {
        return this.setting(name, description, group, visible, (Consumer<List<T>>)null, defaultValue);
    }

    @SafeVarargs
    public final <T> Setting<List<T>> setting(final String name, final String description, final SettingGroup group, final IVisible visible, final Consumer<List<T>> onChanged, final T... defaultValue) {
        return this.setting(name, description, group, visible, onChanged, (Consumer<meteordevelopment.meteorclient.settings.Setting<List<T>>>)null, defaultValue);
    }

    public final <T> Setting<List<T>> setting(final String name, final String description, final SettingGroup group, final IVisible visible, final Consumer<List<T>> onChanged, final Consumer<Setting<List<T>>> onModuleActivated, final T... defaultValue) {
        if (defaultValue[0] instanceof String) {
            return (Setting<List<T>>)group.add((Setting)new StringListSetting(name, description, (List) Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, visible, (Class)StarscriptTextBoxRenderer.class, (CharFilter)null));
        }
        if (defaultValue[0] instanceof Enchantment) {
            return (Setting<List<T>>)group.add((Setting)new EnchantmentListSetting(name, description, (Set<RegistryKey<Enchantment>>) Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, visible));
        }
        if (defaultValue[0] instanceof Module) {
            return (Setting<List<T>>)group.add((Setting)new ModuleListSetting(name, description, (List)Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, visible));
        }
        if (defaultValue[0] instanceof Block) {
            return (Setting<List<T>>)group.add((Setting)new BlockListSetting(name, description, (List)Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, Block -> true, visible));
        }
        if (defaultValue[0] instanceof SoundEvent) {
            return (Setting<List<T>>)group.add((Setting)new SoundEventListSetting(name, description, (List)Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, visible));
        }
        return null;
    }

    public Setting<Integer> setting(final String name, final String description, final int defaultValue, final SettingGroup group, final int sliderMax) {
        return (Setting<Integer>)group.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name(name)).description(description)).defaultValue((Integer) defaultValue)).sliderMax(sliderMax).build());
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final double sliderMin, final double sliderMax, final int min, final int max) {
        return this.setting(name, description, defaultValue, group, null, null, null, sliderMin, sliderMax, min, max, 3);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final double sliderMax) {
        return this.setting(name, description, defaultValue, group, visible, null, null, 0.0, sliderMax, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final Consumer<T> onChanged, final double sliderMax) {
        return this.setting(name, description, defaultValue, group, null, onChanged, null, 0.0, sliderMax, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final double sliderMin, final double sliderMax) {
        return this.setting(name, description, defaultValue, this.sgGeneral, null, null, null, sliderMin, sliderMax, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final double sliderMin, final double sliderMax, final int min, final int max) {
        return this.setting(name, description, defaultValue, group, visible, null, null, sliderMin, sliderMax, min, max, 3);
    }

    public Setting<Double> setting(final String name, final String description, final double defaultValue, final SettingGroup group, final double sliderMin, final double sliderMax, final int min, final int max, final int decimalPlaces) {
        return (Setting<Double>)group.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(name)).description(description)).defaultValue(defaultValue).sliderRange(sliderMin, sliderMax).range((double)min, (double)max).decimalPlaces(decimalPlaces).build());
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final double sliderMin, final double sliderMax) {
        return this.setting(name, description, defaultValue, group, null, null, null, sliderMin, sliderMax, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final double sliderMin, final double sliderMax) {
        return this.setting(name, description, defaultValue, group, visible, null, null, sliderMin, sliderMax, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
    }

    public Setting<Double> setting(final String name, final String description, final double defaultValue, final SettingGroup group, final IVisible visible, final double sliderMin, final double sliderMax, final int decimalPlaces) {
        return (Setting<Double>)group.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(name)).description(description)).defaultValue(defaultValue).visible(visible)).sliderRange(sliderMin, sliderMax).decimalPlaces(decimalPlaces).build());
    }

    public Setting<Double> setting(final String name, final String description, final double defaultValue, final SettingGroup group, final double sliderMin, final double sliderMax, final int decimalPlaces) {
        return (Setting<Double>)group.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(name)).description(description)).defaultValue(defaultValue).sliderRange(sliderMin, sliderMax).decimalPlaces(decimalPlaces).build());
    }

    public <T> Setting<T> setting(final String name, final String description, final T defaultValue, final SettingGroup group, final IVisible visible, final Consumer<T> onChanged, final Consumer<Setting<T>> onModuleActivated, final double sliderMin, final double sliderMax, final int min, final int max, final int decimalPlaces) {
        if (defaultValue instanceof Integer) {
            return (Setting<T>)group.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name(name)).description(description)).defaultValue((Integer) defaultValue)).visible(visible)).onChanged((Consumer)onChanged)).onModuleActivated((Consumer)onModuleActivated)).sliderRange((int)sliderMin, (int)sliderMax).range(min, max).build());
        }
        return (Setting<T>)group.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(name)).description(description)).defaultValue((Double) defaultValue)).visible(visible)).onChanged((Consumer)onChanged)).onModuleActivated((Consumer)onModuleActivated)).sliderRange(sliderMin, sliderMax).range((double)min, (double)max).decimalPlaces(decimalPlaces).build());
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final int alpha) {
        return this.setting(name, description, red, green, blue, alpha, false, this.sgGeneral, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final SettingGroup group) {
        return this.setting(name, description, red, green, blue, 255, false, group, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final int alpha, final SettingGroup group) {
        return this.setting(name, description, red, green, blue, alpha, false, group, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final SettingGroup group, final IVisible visible) {
        return this.setting(name, description, red, green, blue, 255, false, group, visible, null, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final int alpha, final SettingGroup group, final IVisible visible) {
        return this.setting(name, description, red, green, blue, alpha, false, group, visible, null, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final int alpha, final boolean rainbow, final SettingGroup group, final IVisible visible) {
        return this.setting(name, description, red, green, blue, alpha, rainbow, group, visible, null, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final SettingGroup group, final IVisible visible, final Consumer<SettingColor> onChanged) {
        return this.setting(name, description, red, green, blue, 255, false, group, visible, onChanged, null);
    }

    public Setting<SettingColor> setting(final String name, final String description, final int red, final int green, final int blue, final int alpha, final boolean rainbow, final SettingGroup group, final IVisible visible, final Consumer<SettingColor> onChanged, final Consumer<Setting<SettingColor>> onModuleActivated) {
        return (Setting<SettingColor>)group.add((Setting)new ColorSetting(name, description, new SettingColor(red, green, blue, alpha, rainbow), (Consumer)onChanged, (Consumer)onModuleActivated, visible));
    }


    public Setting<List<Item>> setting(final String name, final String description, final SettingGroup group, final boolean bypassFilterWhenSavingAndLoading, final Predicate<Item> filter, final IVisible visible, final Consumer<List<Item>> onChanged, final Consumer<Setting<List<Item>>> onModuleActivated, final Item... defaultValue) {
        return (Setting<List<Item>>)group.add((Setting)new ItemListSetting(name, description, (List)Arrays.asList(defaultValue), (Consumer)onChanged, (Consumer)onModuleActivated, visible, (Predicate)filter, bypassFilterWhenSavingAndLoading));
    }

    public SettingGroup group(final String name) {
        return this.settings.createGroup(name);
    }

    public void toggleWithInfo(final String message, final Object... args) {
        this.info(message, args);
        this.toggle();
    }
}
