package net.gamemode3.moveon.minecart;

import net.gamemode3.moveon.block.ModBlocks;
import net.gamemode3.moveon.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;

public class MinecartHelper {


    public static void setLastDerailment(AbstractMinecartEntity minecart, long tick) {
        ((CustomMinecartData) minecart).move_on_1_21_5$setLastDerailmentTick(tick);
    }

    public static long getLastDerailment(AbstractMinecartEntity minecart) {
        return ((CustomMinecartData) minecart).move_on_1_21_5$getLastDerailmentTick();
    }


    public static boolean isDerailed(AbstractMinecartEntity minecart) {
        long lastDerailment = getLastDerailment(minecart);
        if (lastDerailment < 0) {
            // If lastDerailment is negative, it means it has never derailed
            return false;
        }
        long now = minecart.getWorld().getTime();
        return now - lastDerailment <= ModConfig.getDerailmentTimeout();
    }

    public static double getMinDerailmentSpeed(AbstractMinecartEntity minecart) {
        double factor = ModConfig.getMinDerailmentSpeedInterpolationFactor();
        if (factor < 0) {
            return getLightlyPoweredRailMaxSpeed(minecart) * (1 + factor);
        }
        return getLightlyPoweredRailMaxSpeed(minecart) * (1 - factor) + getBoosterRailMaxSpeed(minecart) * factor;
    }

    public static boolean onPoweredRail(BlockState railState) {
        return railState.isOf(Blocks.POWERED_RAIL) || railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL);
    }

    public static boolean onRail(BlockState railState) {
        return railState.streamTags().anyMatch(tag -> tag == BlockTags.RAILS);
    }

    public static boolean onCurve(RailShape railShape) {
        return switch(railShape) {
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> true;
            default -> false;
        };
    }

    public static boolean minecartTouchingWater(AbstractMinecartEntity minecart) {
        return minecart.isTouchingWater();
    }

    public static double getSlowdownFromWater(AbstractMinecartEntity minecart) {
        return minecartTouchingWater(minecart) ? ModConfig.getWaterSpeedFactor() : 1.0;
    }

    public static double getPoweredRailAcceleration(BlockState railState) {
        if (railState.isOf(Blocks.POWERED_RAIL)) {
            return ModConfig.getBoosterRailAcceleration();
        } else if (railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL)) {
            return ModConfig.getLightlyPoweredRailAcceleration();
        } else {
            throw new IllegalArgumentException("Cannot get powered rail acceleration for rail state " + railState);
        }
    }

    public static double getLightlyPoweredRailMaxSpeed(AbstractMinecartEntity minecart) {
        return ModConfig.getLightlyPoweredRailMaxSpeed() * getSlowdownFromWater(minecart);
    }

    public static double getBoosterRailMaxSpeed(AbstractMinecartEntity minecart) {
        return ModConfig.getBoosterRailMaxSpeed() * getSlowdownFromWater(minecart);
    }

    public static double getPoweredRailMaxSpeed(BlockState railState, AbstractMinecartEntity minecart) {
        if (railState.isOf(Blocks.POWERED_RAIL)) {
            return getBoosterRailMaxSpeed(minecart);
        } else if (railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL)) {
            return getLightlyPoweredRailMaxSpeed(minecart);
        } else {
            throw new IllegalArgumentException("Cannot get max powered rail speed for rail state " + railState);
        }
    }
}
