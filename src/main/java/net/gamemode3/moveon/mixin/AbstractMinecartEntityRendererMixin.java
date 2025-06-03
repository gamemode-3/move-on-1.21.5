package net.gamemode3.moveon.mixin;

import net.minecraft.client.render.entity.AbstractMinecartEntityRenderer;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntityRenderer.class)
public class AbstractMinecartEntityRendererMixin<T extends AbstractMinecartEntity, S extends MinecartEntityRenderState> {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;Lnet/minecraft/client/render/entity/state/MinecartEntityRenderState;F)V", at = @At("HEAD"), cancellable = true)
    public void updateRenderState(T abstractMinecartEntity, S minecartEntityRenderState, float f, CallbackInfo ci) {
//        AbstractMinecartEntityRenderer<T, S> self = (AbstractMinecartEntityRenderer<T, S>) (Object) this;
        ((EntityRendererInvoker) (Object) this).invokeUpdateRenderState(abstractMinecartEntity, minecartEntit2yRenderState, f);
        if (abstractMinecartEntity.getController() instanceof ExperimentalMinecartController experimentalMinecartController) {
//            self.updateFromExperimentalController(abstractMinecartEntity, experimentalMinecartController, minecartEntityRenderState, f);
//            minecartEntityRenderState.usesExperimentalController = true;
        } else if (abstractMinecartEntity.getController() instanceof DefaultMinecartController defaultMinecartController) {
//            self.updateFromDefaultController(abstractMinecartEntity, defaultMinecartController, minecartEntityRenderState, f);
//            minecartEntityRenderState.usesExperimentalController = false;
        }

        long l = abstractMinecartEntity.getId() * 493286711L;
        minecartEntityRenderState.hash = l * l * 4392167121L + l * 98761L;
        minecartEntityRenderState.damageWobbleTicks = abstractMinecartEntity.getDamageWobbleTicks() - f;
        minecartEntityRenderState.damageWobbleSide = abstractMinecartEntity.getDamageWobbleSide();
        minecartEntityRenderState.damageWobbleStrength = Math.max(abstractMinecartEntity.getDamageWobbleStrength() - f, 0.0F);
        minecartEntityRenderState.blockOffset = abstractMinecartEntity.getBlockOffset();
        minecartEntityRenderState.containedBlock = abstractMinecartEntity.getContainedBlock();
        ci.cancel();
    }
}
