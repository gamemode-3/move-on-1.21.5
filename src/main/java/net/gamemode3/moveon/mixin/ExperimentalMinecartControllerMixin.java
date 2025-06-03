package net.gamemode3.moveon.mixin;

import net.gamemode3.moveon.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperimentalMinecartController.class)
public class ExperimentalMinecartControllerMixin {
    @Inject(method="getMaxSpeed", at = @At("HEAD"), cancellable = true)
    public void getMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();

        // Bypass the gamerule check (because the gamerule shit is annoying)
        cir.setReturnValue(minecart.isTouchingWater() ? 0.025 : 2);
    }

    @Inject(method = "decelerateFromPoweredRail", at = @At("HEAD"), cancellable = true)
    private void decelerateFromPoweredRail(Vec3d velocity, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        boolean onPoweredRail = railState.isOf(Blocks.POWERED_RAIL) || railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL);

        if (onPoweredRail && !(Boolean)railState.get(PoweredRailBlock.POWERED)) {
            cir.setReturnValue(velocity.length() < 0.03 ? Vec3d.ZERO : velocity.multiply(0.5));
        } else {
            cir.setReturnValue(velocity);
        }
    }

    @Inject(method="accelerateFromPoweredRail", at = @At("HEAD"), cancellable = true)
    private void accelerateFromPoweredRail(Vec3d velocity, BlockPos railPos, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();
        boolean onPoweredRail = railState.isOf(Blocks.POWERED_RAIL) || railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL);

        if (onPoweredRail && railState.get(PoweredRailBlock.POWERED)) {

            double velocityLength = velocity.length();

            if (velocity.length() > 0.01) {

                double acceleration = 0.09; // Lightly powered rail acceleration
                double maxSpeed = 0.6; // Lightly powered rail max speed
                if (railState.isOf(Blocks.POWERED_RAIL)) {
                    acceleration = 0.15; // Booster rail acceleration
                    maxSpeed = 1.0; // Booster rail max speed
                }

                if (velocityLength < maxSpeed) {
                    velocityLength += acceleration;
                    if (velocityLength > maxSpeed) velocityLength = maxSpeed;
                }

                if (velocityLength > maxSpeed) {
                    double deceleration = 0.01;
                    velocityLength -= deceleration;
                    if (velocityLength < maxSpeed) velocityLength = maxSpeed;
                }

                cir.setReturnValue(velocity.normalize().multiply(velocityLength));
            } else {
                Vec3d vec3d = minecart.getLaunchDirection(railPos);
                cir.setReturnValue(vec3d.lengthSquared() <= 0.0 ? velocity : vec3d.multiply(velocity.length() + 0.2));
            }
        } else {
            cir.setReturnValue(velocity);
        }
    }
}
