package net.gamemode3.moveon.minecart;

import net.gamemode3.moveon.block.ModBlocks;
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


    public static long getDerailmentTimeout() {
        return 4;
    }

    public static boolean isDerailed(AbstractMinecartEntity minecart) {
        long lastDerailment = getLastDerailment(minecart);
        if (lastDerailment < 0) {
            // If lastDerailment is negative, it means it has never derailed
            return false;
        }
        long now = minecart.getWorld().getTime();
        return now - lastDerailment <= getDerailmentTimeout();
    }

    public static boolean canDerail() {
        return true; // This can be toggled with a config option in the future
    }

    public static double getMinDerailmentSpeed(AbstractMinecartEntity minecart) {
        double factor = getMinDerailmentSpeedInterpolationFactor();
        if (factor < 0) {
            return getLightlyPoweredRailMaxSpeed(minecart) * (1 + factor);
        }
        return getLightlyPoweredRailMaxSpeed(minecart) * (1 - factor) + getBoosterRailMaxSpeed(minecart) * factor;
    }

    public static double getMinDerailmentSpeedInterpolationFactor() {
        return 0.1;
    }

    public static double getDerailmentSlowdownFactor() {
        return 0.5; // This can be adjusted to change how quickly minecarts slow down when derailing
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

    public static double getWaterSlowdownFactor() {
        return 0.5;
    }

    public static double getSlowdownFromWater(AbstractMinecartEntity minecart) {
        return minecartTouchingWater(minecart) ? getWaterSlowdownFactor() : 1.0;
    }

    public static double getSpeedRetentionOnRail() {
        return 0.997;
    }

    public static double getLightlyPoweredRailAcceleration() {
        return 0.02;
    }

    public static double getBoosterRailAcceleration() {
        return 0.03;
    }

    public static double getPoweredRailAcceleration(BlockState railState) {
        if (railState.isOf(Blocks.POWERED_RAIL)) {
            return getBoosterRailAcceleration();
        } else if (railState.isOf(ModBlocks.LIGHTLY_POWERED_RAIL)) {
            return getLightlyPoweredRailAcceleration();
        } else {
            throw new IllegalArgumentException("Cannot get powered rail acceleration for rail state " + railState);
        }
    }

    public static double getLightlyPoweredRailMaxSpeedRaw() {
        return 0.6;
    }

    public static double getLightlyPoweredRailMaxSpeed(AbstractMinecartEntity minecart) {
        return getLightlyPoweredRailMaxSpeedRaw() * getSlowdownFromWater(minecart);
    }

    public static double getBoosterRailMaxSpeedRaw() {
        // 5.0 should be max when settings are introduced
        return 1.4;
    }

    public static double getBoosterRailMaxSpeed(AbstractMinecartEntity minecart) {
        return getBoosterRailMaxSpeedRaw() * getSlowdownFromWater(minecart);
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

    public static double getActivePoweredRailDeceleration() {
        return 0.0151;
    }

    public static double getActivatorRailDeceleration() {
        return 0.25;
    }

    public static double getSpeedRetentionOnInactivePoweredRail() {
        return 0.5;
    }

    public static double getPassengerAccelerationFactor() {
        return 0.7;
    }
}
