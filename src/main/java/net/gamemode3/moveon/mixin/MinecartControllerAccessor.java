package net.gamemode3.moveon.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartController;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartController.class)
public interface MinecartControllerAccessor {
    @Accessor("minecart")
    AbstractMinecartEntity getMinecart();
}
