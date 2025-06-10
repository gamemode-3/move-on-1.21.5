package net.gamemode3.moveon.mixin;

import net.gamemode3.moveon.block.ModBlocks;
import net.gamemode3.moveon.config.ModConfig;
import net.gamemode3.moveon.minecart.MinecartHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperimentalMinecartController.class)
public class ExperimentalMinecartControllerMixin {
    @Inject(method = "decelerateFromPoweredRail", at = @At("HEAD"), cancellable = true)
    private void decelerateFromRail(Vec3d velocity, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();

        boolean onPoweredRail = MinecartHelper.onPoweredRail(railState);

        if (onPoweredRail && railState.get(PoweredRailBlock.POWERED)) {
            double velocityLength = velocity.length();
            double maxSpeed = MinecartHelper.getPoweredRailMaxSpeed(railState, minecart);

            if (velocityLength > maxSpeed) {
                double deceleration = ModConfig.getActivePoweredRailDeceleration();
                velocityLength -= deceleration;
                if (velocityLength < maxSpeed) velocityLength = maxSpeed;
            }

            cir.setReturnValue(velocity.normalize().multiply(velocityLength));
            return;
        } else if (onPoweredRail) {
            cir.setReturnValue(velocity.length() < 0.03 ? Vec3d.ZERO : velocity.multiply(ModConfig.getInactivePoweredRailSpeedRetention()));
            return;
        } else if (MinecartHelper.onRail(railState)) {
            double speedRetention = ModConfig.getSpeedRetention();

            velocity = new Vec3d(
                    velocity.x * speedRetention,
                    velocity.y,
                    velocity.z * speedRetention
            );

            minecart.setVelocity(velocity);

            if (railState.isOf(Blocks.ACTIVATOR_RAIL)) {
                velocity = decelerateFromActivatorRail(minecart);
            }

            cir.setReturnValue(velocity);
            return;
        }
        cir.setReturnValue(velocity);
    }

    @Unique
    private Vec3d decelerateFromActivatorRail(AbstractMinecartEntity minecart) {
        Vec3d velocity = minecart.getVelocity();
        double velocityLength = velocity.length();
        double nonDerailmentSpeed = MinecartHelper.getMinDerailmentSpeed(minecart) - 0.1;

        if (velocityLength > nonDerailmentSpeed) {
            double deceleration = ModConfig.getActivatorRailDeceleration();
            velocityLength -= deceleration;
            if (velocityLength < nonDerailmentSpeed) velocityLength = nonDerailmentSpeed;
        }

        return velocity.normalize().multiply(velocityLength);
    }

    @Inject(method="moveAlongTrack", at = @At("HEAD"), cancellable = true)
    private void derailMinecartInCurve(BlockPos blockPos, RailShape railShape, double remainingMovement, CallbackInfoReturnable<Double> cir) {
        if (!ModConfig.getCanDerail()) {
            return;
        }

        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();

        if (MinecartHelper.isDerailed(minecart)) {
            keepMinecartDerailed(minecart);
            cir.setReturnValue(0.0);
            return;
        }

        boolean onCurve = MinecartHelper.onCurve(railShape);

        double velocityLength = minecart.getVelocity().length();

        if (onCurve && velocityLength > MinecartHelper.getMinDerailmentSpeed(minecart)) {
            derailMinecart(minecart, railShape);
            cir.setReturnValue(0.0);
        }
    }

    @Unique
    void derailMinecart(AbstractMinecartEntity minecart, RailShape railShape) {
        MinecartHelper.setLastDerailment(minecart, minecart.getWorld().getTime());

        Vec3d velocity = minecart.getVelocity();
        double velocityLength = velocity.length();
        Vec3d movementDirection = velocity.normalize();


        Vec3d derailmentDirection = switch (railShape) {
            case NORTH_EAST, SOUTH_WEST -> new Vec3d(1, 0, 1).multiply(velocity.x + velocity.z);
            case NORTH_WEST, SOUTH_EAST -> new Vec3d(-1, 0, 1).multiply(-velocity.x + velocity.z);
            default -> new Vec3d(0, 0, 0); // No derailment for straight rails
        };
        derailmentDirection = derailmentDirection.normalize();

        derailmentDirection = derailmentDirection.add(movementDirection).normalize();

        velocityLength *= ModConfig.getDerailmentSlowdownFactor();

        minecart.setVelocity(derailmentDirection.multiply(velocityLength));
        minecart.move(MovementType.SELF, minecart.getVelocity());
        minecart.setOnGround(false);
    }

    @Unique
    void keepMinecartDerailed(AbstractMinecartEntity minecart) {
        minecart.move(MovementType.SELF, minecart.getVelocity());
    }

//    @Inject(method="adjustToRail", at = @At("HEAD"), cancellable = true)
//    private void keepMinecartDerailed1(BlockPos pos, BlockState blockState, boolean ignoreWeight, CallbackInfo ci) {
//        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
//        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();
//
//        if (MinecartHelper.isDerailed(minecart)) {
//            minecart.moveOffRail(minecart.getWorld());
//            ci.cancel();
//        }
//    }
//    @ModifyExpressionValue(
//        method = "tick",
//        at = @At(
//                value = "INVOKE",
//                target = "Lnet/minecraft/block/AbstractRailBlock;isRail(Lnet/minecraft/block/BlockState;)Z"
//        )
//    )
//    private boolean modifyRailCheck(boolean isRail) {
//        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
//        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();
//        return isRail && !MinecartHelper.isDerailed(minecart);
//    }

    @Inject(method="accelerateFromPoweredRail", at = @At("HEAD"), cancellable = true)
    private void accelerateFromPoweredRail(Vec3d velocity, BlockPos railPos, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();
        boolean onPoweredRail = railState.isOf(Blocks.POWERED_RAIL) || railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL);

        if (onPoweredRail && railState.get(PoweredRailBlock.POWERED)) {

            double velocityLength = velocity.length();

            if (velocity.length() > 0.01) {

                double acceleration = MinecartHelper.getPoweredRailAcceleration(railState);
                double maxSpeed = MinecartHelper.getPoweredRailMaxSpeed(railState, minecart);

                if (minecart.hasPassengers()) {
                    acceleration *= ModConfig.getPassengerAccelerationFactor();
                }

                if (velocityLength < maxSpeed) {
                    velocityLength += acceleration;
                    if (velocityLength > maxSpeed) velocityLength = maxSpeed;
                }

                cir.setReturnValue(velocity.normalize().multiply(velocityLength));
            } else {
                Vec3d launchDirection = minecart.getLaunchDirection(railPos);
                cir.setReturnValue(launchDirection.lengthSquared() <= 0.0 ? velocity : launchDirection.multiply(velocity.length() + 0.2));
            }
        } else if (railState.isOf(Blocks.RAIL)) {
            cir.setReturnValue(velocity);
        }
    }

    @Inject(method="getMaxSpeed", at = @At("HEAD"), cancellable = true)
    public void getMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        ExperimentalMinecartController self = (ExperimentalMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();
        cir.setReturnValue(MinecartHelper.getSlowdownFromWater(minecart) * MinecartHelper.getBoosterRailMaxSpeed(minecart));
    }
}
