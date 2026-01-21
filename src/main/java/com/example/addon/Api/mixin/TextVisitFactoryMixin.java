package com.example.addon.Api.mixin;

import com.example.addon.Main.modules.MioEditor;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={TextVisitFactory.class})
public class TextVisitFactoryMixin {

    @ModifyArg(method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"))
    private static String modifyTextOne(String text) {
        if (Modules.get() == null) {
            return text;
        }
        MioEditor mioEditor = Modules.get().get(MioEditor.class);
        if (mioEditor == null || !mioEditor.isActive()) {
            return text;
        }
        return mioEditor.text(text);
    }

}
