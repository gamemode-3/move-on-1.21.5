package net.gamemode3.moveon.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker<T extends Entity, S extends EntityRenderState> {
    @Invoker("updateRenderState")
    void invokeUpdateRenderState(T entity, S entityRenderState, float tickDelta);
}
