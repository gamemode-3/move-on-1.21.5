package net.gamemode3.moveon.config;

import com.mojang.datafixers.util.Pair;
import net.gamemode3.moveon.MoveOn;

public class ModConfig {
    private static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    private static double WATER_SPEED_FACTOR;
    private static double PASSENGER_ACCELERATION_FACTOR;

    private static double SPEED_RETENTION;
    private static double INACTIV_POWERED_RAIL_SPEED_RETENTION;

    private static double LIGHTLY_POWERED_RAIL_ACCELERATION;
    private static double BOOSTER_RAIL_ACCELERATION;
    private static double LIGHTLY_POWERED_RAIL_MAX_SPEED;
    private static double BOOSTER_RAIL_MAX_SPEED;

    private static double ACTIVE_POWERED_RAIL_DECELERATION;
    private static double ACTIVATOR_RAIL_DECELERATION;

    private static boolean CAN_DERAIL;
    private static double MIN_DERAILMENT_SPEED_INTERPOLATION_FACTOR;
    private static double DERAILMENT_SLOWDOWN_FACTOR;
    private static int DERAILMENT_TIMEOUT;


    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(MoveOn.MOD_ID + "-config").provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.water-speed-factor", 0.5),
                "limits maximum speed"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.passenger-acceleration-factor", 0.7),
                "acceleration multiplier when minecart has passengers"
        );

        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.speed-retention", 0.99),
                "on regular rails"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.inactive-powered-rail-speed-retention", 0.5),
                "on inactive powered rails"
        );

        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.lightly-powered-rail-acceleration", 0.03),
                "on active copper rails"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.booster-rail-acceleration", 0.05),
                "on active gold rails"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.lightly-powered-rail-max-speed", 0.6),
                "on active copper rails"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.booster-rail-max-speed", 1.4),
                "on active gold rails"
        );

        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.active-powered-rail-deceleration", 0.0151),
                "when minecart is going faster than the rail's max speed"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.activator-rail-deceleration", 0.25),
                "additional deceleration to slow minecart below derailment speed"
        );

        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.can-derail", true),
                "if false, minecarts will never derail"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.min-derailment-speed-interpolation-factor", 0.1),
                """
                        interpolates between copper rail and gold rail speed
                        0 => if minecart is going any faster than copper rail max speed, it will derail
                        1 => gold rail speed is the limit, it will basically never derail
                        -0.5 => if the minecart is going faster than half the copper rail's max speed, it will derail
                        """
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.derailment-slowdown-factor", 0.5),
                "how much minecart slows down when it derails"
        );
        configs.addKeyValuePair(
                new Pair<>("move-on.minecarts.derailment-timeout", 4),
                "for how many ticks minecart will not be able to get back onto a rail"
        );
    }

    private static void assignConfigs() {
        WATER_SPEED_FACTOR = CONFIG.getOrDefault("move-on.minecarts.water-speed-factor", 0.5);
        PASSENGER_ACCELERATION_FACTOR = CONFIG.getOrDefault("move-on.minecarts.passenger-acceleration-factor", 0.7);

        SPEED_RETENTION = CONFIG.getOrDefault("move-on.minecarts.speed-retention", 0.99);
        INACTIV_POWERED_RAIL_SPEED_RETENTION = CONFIG.getOrDefault("move-on.minecarts.inactive-powered-rail-speed-retention", 0.5);

        LIGHTLY_POWERED_RAIL_ACCELERATION = CONFIG.getOrDefault("move-on.minecarts.lightly-powered-rail-acceleration", 0.03);
        BOOSTER_RAIL_ACCELERATION = CONFIG.getOrDefault("move-on.minecarts.booster-rail-acceleration", 0.06);
        LIGHTLY_POWERED_RAIL_MAX_SPEED = CONFIG.getOrDefault("move-on.minecarts.lightly-powered-rail-max-speed", 0.6);
        BOOSTER_RAIL_MAX_SPEED = CONFIG.getOrDefault("move-on.minecarts.booster-rail-max-speed", 1.4);

        ACTIVE_POWERED_RAIL_DECELERATION = CONFIG.getOrDefault("move-on.minecarts.active-powered-rail-deceleration", 0.0151);
        ACTIVATOR_RAIL_DECELERATION = CONFIG.getOrDefault("move-on.minecarts.activator-rail-deceleration", 0.25);

        CAN_DERAIL = CONFIG.getOrDefault("move-on.minecarts.can-derail", true);
        MIN_DERAILMENT_SPEED_INTERPOLATION_FACTOR = CONFIG.getOrDefault("move-on.minecarts.min-derailment-speed-interpolation-factor", 0.1);
        DERAILMENT_SLOWDOWN_FACTOR = CONFIG.getOrDefault("move-on.minecarts.derailment-slowdown-factor", 0.5);
        DERAILMENT_TIMEOUT = CONFIG.getOrDefault("move-on.minecarts.derailment-timeout", 4);

        MoveOn.LOGGER.info("all {} configs for {} have been set properly", configs.size(), MoveOn.MOD_ID);
    }

    public static double getWaterSpeedFactor() {
        return WATER_SPEED_FACTOR;
    }
    public static double getPassengerAccelerationFactor() {
        return PASSENGER_ACCELERATION_FACTOR;
    }

    public static double getSpeedRetention() {
        return SPEED_RETENTION;
    }
    public static double getInactivePoweredRailSpeedRetention() {
        return INACTIV_POWERED_RAIL_SPEED_RETENTION;
    }

    public static double getLightlyPoweredRailAcceleration() {
        return LIGHTLY_POWERED_RAIL_ACCELERATION;
    }
    public static double getBoosterRailAcceleration() {
        return BOOSTER_RAIL_ACCELERATION;
    }
    public static double getLightlyPoweredRailMaxSpeed() {
        return LIGHTLY_POWERED_RAIL_MAX_SPEED;
    }
    public static double getBoosterRailMaxSpeed() {
        return BOOSTER_RAIL_MAX_SPEED;
    }

    public static double getActivePoweredRailDeceleration() {
        return ACTIVE_POWERED_RAIL_DECELERATION;
    }
    public static double getActivatorRailDeceleration() {
        return ACTIVATOR_RAIL_DECELERATION;
    }

    public static boolean getCanDerail() {
        return CAN_DERAIL;
    }
    public static double getMinDerailmentSpeedInterpolationFactor() {
        return MIN_DERAILMENT_SPEED_INTERPOLATION_FACTOR;
    }
    public static double getDerailmentSlowdownFactor() {
        return DERAILMENT_SLOWDOWN_FACTOR;
    }
    public static int getDerailmentTimeout() {
        return DERAILMENT_TIMEOUT;
    }
}
