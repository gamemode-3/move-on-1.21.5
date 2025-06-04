package net.gamemode3.moveon.mixin;

import net.gamemode3.moveon.minecart.CustomMinecartData;
import net.gamemode3.moveon.minecart.MinecartHelper;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements CustomMinecartData {

    @Unique
    private long lastDerailmentTick = Long.MIN_VALUE;

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeLastDerailment(NbtCompound nbt, CallbackInfo ci) {
        nbt.putLong("LastDerailmentTick", lastDerailmentTick);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readLastDerailment(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("LastDerailmentTick")) {
            lastDerailmentTick = nbt.getLong("LastDerailmentTick").orElse(0L);
        }
    }

    // Getters and setters
    @Unique
    public long move_on_1_21_5$getLastDerailmentTick() {
        return lastDerailmentTick;
    }

    @Unique
    public void move_on_1_21_5$setLastDerailmentTick(long tick) {
        this.lastDerailmentTick = tick;
    }

    @Inject(method="getLaunchDirection", at=@At("HEAD"), cancellable=true)
    public void getLaunchDirection(BlockPos railPos, CallbackInfoReturnable<Vec3d> cir) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        BlockState blockState = self.getWorld().getBlockState(railPos);
        if (blockState.getBlock() instanceof PoweredRailBlock && blockState.get(PoweredRailBlock.POWERED)) {
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            if (railShape == RailShape.EAST_WEST) {
                if (self.willHitBlockAt(railPos.west())) {
                    cir.setReturnValue(new Vec3d(1.0, 0.0, 0.0));
                    return;
                }

                if (self.willHitBlockAt(railPos.east())) {
                    cir.setReturnValue(new Vec3d(-1.0, 0.0, 0.0));
                    return;
                }
            } else if (railShape == RailShape.NORTH_SOUTH) {
                if (self.willHitBlockAt(railPos.north())) {
                    cir.setReturnValue(new Vec3d(0.0, 0.0, 1.0));
                    return;
                }

                if (self.willHitBlockAt(railPos.south())) {
                    cir.setReturnValue(new Vec3d(0.0, 0.0, -1.0));
                    return;
                }
            }
        }
        cir.setReturnValue(Vec3d.ZERO);
    }

    @Inject(method="moveOffRail", at=@At("HEAD"), cancellable=true)
    protected void moveOffRail(ServerWorld world, CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;



        double d = getOffRailMaxSpeed();
        Vec3d velocity = self.getVelocity();


        // look ahead whether there are rails in the direction of the velocity.
        // if there are rails, slow down to avoid minecart driving rail-less
        Vec3d movementDirection = velocity.normalize();

        for (int i = 1; i <= 6; i++) {
            Vec3d nextPosPrecise = self.getPos().add(movementDirection.multiply(i));
            BlockPos nextPos = new BlockPos(
                MathHelper.floor(nextPosPrecise.x),
                MathHelper.floor(nextPosPrecise.y),
                MathHelper.floor(nextPosPrecise.z)
            );
            BlockState nextState = world.getBlockState(nextPos);
            if (nextState.getBlock() instanceof AbstractRailBlock) {
                d = Math.min(d, getAntiRaillessCartSpeed(i));
                break;
            }
        }

        self.setVelocity(MathHelper.clamp(velocity.x, -d, d), velocity.y, MathHelper.clamp(velocity.z, -d, d));
        if (self.isOnGround()) {
            self.setVelocity(self.getVelocity().multiply(0.5));
        }

        self.move(MovementType.SELF, self.getVelocity());
        if (!self.isOnGround()) {
            self.setVelocity(self.getVelocity().multiply(0.95));
        }
        ci.cancel();
    }

    @Unique
    private double getAntiRaillessCartSpeed(int railDistance) {
        return switch (railDistance) {
            case 1 -> 0.25;
            case 2 -> 0.45;
            case 3 -> 0.65;
            case 4 -> 0.95;
            case 5 -> 1.2;
            case 6 -> 1.5;
            default -> getOffRailMaxSpeed();
        };
    }

    @Unique
    private double getOffRailMaxSpeed() {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        return MinecartHelper.getBoosterRailMaxSpeed(self);
    }

    @Inject(method="areMinecartImprovementsEnabled", at=@At("HEAD"), cancellable=true)
    private static void areMinecartImprovementsEnabled(CallbackInfoReturnable<Boolean> cir) {
        // Disable minecart improvements to prevent issues with the custom launch direction
        cir.setReturnValue(true);
    }
}

