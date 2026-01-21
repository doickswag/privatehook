package com.example.addon.Main.modules;

import com.example.addon.Api.util.TextUtils;
import com.example.addon.Hook;
import com.example.addon.Hooked;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.orbit.EventHandler;

import java.util.Arrays;
import java.util.List;

public class SKEETBAIT extends Hooked {
    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("Nigga 2 bait")
        .description("baits this person")
        .defaultValue("Thr0wing")
        .build()
    );
    public SKEETBAIT() {
        super(Hook.CATEGORY, "auto bait", "BAD BAIT LALW.");
    }

    @EventHandler
    private void onMessageRecieve(ReceiveMessageEvent event) {
        String baitez = name.get();
        String get = event.getMessage().getString();
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getName().getString().equals(baitez)) {
            if (get.contains(" nn") || get.equals("nn")) {
                int nnbait = (int) (Math.random() * nn.size());
                TextUtils.sendNewMessage(nn.get(nnbait));
            }
            if (get.contains(" bait") || get.equals("bait") || get.equals("bad bait") || get.contains(" bad bait")) {
                int baitbait = (int) (Math.random() * bait.size());
                TextUtils.sendNewMessage(bait.get(baitbait));
            }
            if (get.contains(" skid") || get.equals("skid")) {
                int skidbait = (int) (Math.random() * skid.size());
                TextUtils.sendNewMessage(skid.get(skidbait));
            }
            if (get.contains(" random") || get.equals("random")) {
                int randombait = (int) (Math.random() * random.size());
                TextUtils.sendNewMessage(random.get(randombait));
            }
            if (get.contains(" packed") || get.equals("packed")) {
                int packedbait = (int) (Math.random() * packed.size());
                TextUtils.sendNewMessage(packed.get(packedbait));
            }
            if (get.contains(" vc") || get.equals("vc") || get.equals("come vc")) {
                int vcbait = (int) (Math.random() * vc.size());
                TextUtils.sendNewMessage(vc.get(vcbait));
            }
        }
    }
    public static final List<String> nn = Arrays.asList(
        "IRONIC ASF",
        "im known"
    );
    public static final List<String> bait = Arrays.asList(
        "NOT BAIT LOL"
    );
    public static final List<String> skid = Arrays.asList(
        "LOL I CRACKED YOUR CHEAT"
    );
    public static final List<String> random = Arrays.asList(
        "LOL SPAZZLIL DONT KNOW U"
    );
    public static final List<String> packed = Arrays.asList(
        "COME VC RN"
    );
    public static final List<String> vc = Arrays.asList(
        "JOIN HIGHLAND VC RN"
    );
}
// i told baby "i eat humans" - izaya tiji
// dont add jqq shi u cornball
