package net.gamemode3.moveon.mixin;

import com.mojang.datafixers.util.Pair;
import net.gamemode3.moveon.block.ModBlocks;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultMinecartController.class)
public abstract class DefaultMinecartControllerMixin {


    @Inject(method = "moveOnRail", at = @At("HEAD"), cancellable = true)
    public void moveOnRail(ServerWorld world, CallbackInfo ci) {
        DefaultMinecartController self = (DefaultMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();

        BlockPos blockPos = minecart.getRailOrMinecartPos();
        BlockState blockState = self.getWorld().getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof AbstractRailBlock)) {
            return;
        }

        minecart.onLanding();
        double d = minecart.getX();
        double e = minecart.getY();
        double f = minecart.getZ();
        Vec3d vec3d = self.snapPositionToRail(d, e, f);
        e = blockPos.getY();
        boolean stronglyPowered = false;
        boolean powered = false;
        boolean notPowered = false;
        if (blockState.getBlock() instanceof PoweredRailBlock) {
            powered = (Boolean) blockState.get(PoweredRailBlock.POWERED);
            notPowered = !powered;
        }
        if (blockState.isOf(Blocks.POWERED_RAIL)) {
            stronglyPowered = true;
        }

        double g = 0.0078125;
        if (minecart.isTouchingWater()) {
            g *= 0.2;
        }

        Vec3d vec3d2 = self.getVelocity();
        RailShape railShape = blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST:
                self.setVelocity(vec3d2.add(-g, 0.0, 0.0));
                e++;
                break;
            case ASCENDING_WEST:
                self.setVelocity(vec3d2.add(g, 0.0, 0.0));
                e++;
                break;
            case ASCENDING_NORTH:
                self.setVelocity(vec3d2.add(0.0, 0.0, g));
                e++;
                break;
            case ASCENDING_SOUTH:
                self.setVelocity(vec3d2.add(0.0, 0.0, -g));
                e++;
        }

        vec3d2 = self.getVelocity();
        Pair<Vec3i, Vec3i> pair = AbstractMinecartEntity.getAdjacentRailPositionsByShape(railShape);
        Vec3i vec3i = pair.getFirst();
        Vec3i vec3i2 = pair.getSecond();
        double h = vec3i2.getX() - vec3i.getX();
        double i = vec3i2.getZ() - vec3i.getZ();
        double j = Math.sqrt(h * h + i * i);
        double k = vec3d2.x * h + vec3d2.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }

        double l = Math.min(2.0, vec3d2.horizontalLength());
        vec3d2 = new Vec3d(l * h / j, vec3d2.y, l * i / j);
        self.setVelocity(vec3d2);


        Entity entity = minecart.getFirstPassenger();
        Vec3d vec3d3;
        if (minecart.getFirstPassenger() instanceof ServerPlayerEntity serverPlayerEntity) {
            vec3d3 = serverPlayerEntity.getInputVelocityForMinecart();
        } else {
            vec3d3 = Vec3d.ZERO;
        }

        if (entity instanceof PlayerEntity && vec3d3.lengthSquared() > 0.0) {
            Vec3d vec3d4 = vec3d3.normalize();
            double m = self.getVelocity().horizontalLengthSquared();
            if (vec3d4.lengthSquared() > 0.0 && m < 0.01) {
                self.setVelocity(self.getVelocity().add(vec3d3.x * 0.001, 0.0, vec3d3.z * 0.001));
                notPowered = false;
            }
        }

        if (notPowered) {
            double n = self.getVelocity().horizontalLength();
            if (n < 0.03) {
                self.setVelocity(Vec3d.ZERO);
            } else {
                self.setVelocity(self.getVelocity().multiply(0.5, 0.0, 0.5));
            }
        }

        double n = blockPos.getX() + 0.5 + vec3i.getX() * 0.5;
        double o = blockPos.getZ() + 0.5 + vec3i.getZ() * 0.5;
        double p = blockPos.getX() + 0.5 + vec3i2.getX() * 0.5;
        double q = blockPos.getZ() + 0.5 + vec3i2.getZ() * 0.5;
        h = p - n;
        i = q - o;
        double r;
        if (h == 0.0) {
            r = f - blockPos.getZ();
        } else if (i == 0.0) {
            r = d - blockPos.getX();
        } else {
            double s = d - n;
            double t = f - o;
            r = (s * h + t * i) * 2.0;
        }

        d = n + h * r;
        f = o + i * r;
        self.setPos(d, e, f);
        double s = minecart.hasPassengers() ? 0.75 : 1.0;
        double maxSpeed = ((AbstractMinecartEntityInvoker) minecart).invokeGetMaxSpeed(world);
        vec3d2 = self.getVelocity();
        minecart.move(MovementType.SELF, new Vec3d(MathHelper.clamp(s * vec3d2.x, -maxSpeed, maxSpeed), 0.0, MathHelper.clamp(s * vec3d2.z, -maxSpeed, maxSpeed)));
        if (vec3i.getY() != 0
                && MathHelper.floor(minecart.getX()) - blockPos.getX() == vec3i.getX()
                && MathHelper.floor(minecart.getZ()) - blockPos.getZ() == vec3i.getZ()) {
            self.setPos(minecart.getX(), minecart.getY() + vec3i.getY(), minecart.getZ());
        } else if (vec3i2.getY() != 0
                && MathHelper.floor(minecart.getX()) - blockPos.getX() == vec3i2.getX()
                && MathHelper.floor(minecart.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            self.setPos(minecart.getX(), minecart.getY() + vec3i2.getY(), minecart.getZ());
        }

        self.setVelocity(((AbstractMinecartEntityInvoker) minecart).invokeApplySlowdown(self.getVelocity()));
        Vec3d vec3d5 = self.snapPositionToRail(minecart.getX(), minecart.getY(), minecart.getZ());
        if (vec3d5 != null && vec3d != null) {
            double u = (vec3d.y - vec3d5.y) * 0.05;
            Vec3d vec3d6 = self.getVelocity();
            double v = vec3d6.horizontalLength();
            if (v > 0.0) {
                self.setVelocity(vec3d6.multiply((v + u) / v, 1.0, (v + u) / v));
            }

            self.setPos(minecart.getX(), vec3d5.y, minecart.getZ());
        }

        int w = MathHelper.floor(minecart.getX());
        int x = MathHelper.floor(minecart.getZ());
        if (w != blockPos.getX() || x != blockPos.getZ()) {
            Vec3d vec3d6 = self.getVelocity();
            double v = vec3d6.horizontalLength();
            self.setVelocity(v * (w - blockPos.getX()), vec3d6.y, v * (x - blockPos.getZ()));
        }


        maxSpeed = 0.4;


        double AccelerationModifier = 0.06;

        if (blockState.isOf(Blocks.POWERED_RAIL)) {
            AccelerationModifier = 0.22;
            maxSpeed *= 2.5;
        }
        else if (blockState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL)) {
            AccelerationModifier = 0.09;
            maxSpeed *= 1.5;
        }

        final double startModifier = 1 / 3.0;

        if (powered) {
            Vec3d vec3d6 = self.getVelocity();
            double v = vec3d6.horizontalLength();
            if (v > 0.01) {

                Vec3d newVelocity = vec3d6.add(
                        vec3d6.x / v * AccelerationModifier,
                        0.0,
                        vec3d6.z / v * AccelerationModifier
                );

                if (newVelocity.horizontalLength() > maxSpeed) {
                    newVelocity = newVelocity.multiply(0.8, 1.0, 0.8);
                    if (newVelocity.horizontalLength() < maxSpeed) {
                        newVelocity = newVelocity.normalize().multiply(maxSpeed);
                    }
                }

                self.setVelocity(newVelocity);
            } else {
                AccelerationModifier *= startModifier;

                Vec3d vec3d7 = self.getVelocity();
                double x1 = vec3d7.x;
                double z1 = vec3d7.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (minecart.willHitBlockAt(blockPos.west())) {
                        x1 = AccelerationModifier;
                    } else if (minecart.willHitBlockAt(blockPos.east())) {
                        x1 = -AccelerationModifier;
                    }
                } else {
                    if (railShape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (minecart.willHitBlockAt(blockPos.north())) {
                        z1 = AccelerationModifier;
                    } else if (minecart.willHitBlockAt(blockPos.south())) {
                        z1 = -AccelerationModifier;
                    }
                }

                self.setVelocity(x1, vec3d7.y, z1);
            }
        }
        ci.cancel();
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    public void getMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        DefaultMinecartController self = (DefaultMinecartController) (Object) this;
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) self).getMinecart();

        cir.setReturnValue(
                minecart.isTouchingWater() ? 0.2 : 0.8
        );
    }

}
