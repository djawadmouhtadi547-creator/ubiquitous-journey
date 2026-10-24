package com.novaclient.mixin;

import com.novaclient.NovaClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
        method = "getFov",
        at = @At("RETURN"),
        cancellable = true,
        require = 1
    )
    private void nova$applyZoom(
            Camera camera,
            float tickDelta,
            boolean changingFov,
            CallbackInfoReturnable<Double> cir
    ) {
        if (!NovaClient.isModuleEnabled("zoom")) {
            return;
        }

        double configured = NovaClient.CONFIG == null
                ? 28.0
                : NovaClient.CONFIG.zoomFov;

        cir.setReturnValue(Math.max(15.0, Math.min(60.0, configured)));
    }
}
