package net.gamemode3.moveon.mixin;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {

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

    @Inject(method="areMinecartImprovementsEnabled", at=@At("HEAD"), cancellable=true)
    private static void areMinecartImprovementsEnabled(CallbackInfoReturnable<Boolean> cir) {
        // Disable minecart improvements to prevent issues with the custom launch direction
        cir.setReturnValue(true);
    }
}

