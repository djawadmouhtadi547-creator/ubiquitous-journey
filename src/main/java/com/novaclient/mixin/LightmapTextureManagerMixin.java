package com.novaclient.mixin;

import com.novaclient.NovaClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void nova$fullbright(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (NovaClient.isModuleEnabled("fullbright")) cir.setReturnValue(1.0F);
    }
}
