package de.jaylawl.dripleafcontrol.util;

public class ConfigurableData {

    public static final int DEFAULT_TICKS_UNTIL_UNSTABLE_TILT = 0;
    public static final int DEFAULT_TICKS_UNTIL_PARTIAL_TILT = 10;
    public static final int DEFAULT_TICKS_UNTIL_FULL_TILT = 20;
    public static final int DEFAULT_TICKS_UNTIL_NONE_TILT = 30;

    public int ticksUntilUnstableTilt = DEFAULT_TICKS_UNTIL_UNSTABLE_TILT;
    public int ticksUntilPartialTilt = DEFAULT_TICKS_UNTIL_PARTIAL_TILT;
    public int ticksUntilFullTilt = DEFAULT_TICKS_UNTIL_FULL_TILT;
    public int ticksUntilNoneTilt = DEFAULT_TICKS_UNTIL_NONE_TILT;

    public ConfigurableData() {
    }

    //

}
