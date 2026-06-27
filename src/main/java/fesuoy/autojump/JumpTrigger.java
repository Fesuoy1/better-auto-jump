package fesuoy.autojump;

import java.util.concurrent.ThreadLocalRandom;

public class JumpTrigger {

    private static int ticksRemaining = -1;
    private static int cooldownTicks = 0;
    private static int cooldownMax = 3;

    public static void setCooldownMax(int ticks) {
        cooldownMax = Math.max(0, ticks);
    }

    public static void set(int maxVariance) {
        if (cooldownTicks > 0) return;
        if (ticksRemaining >= 0) return;
        ticksRemaining = maxVariance > 0 ? ThreadLocalRandom.current().nextInt(maxVariance + 1) : 0;
    }

    public static boolean consumeJump() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (ticksRemaining < 0) return false;
        if (ticksRemaining > 0) {
            ticksRemaining--;
            return false;
        }
        ticksRemaining = -1;
        cooldownTicks = cooldownMax;
        return true;
    }

    public static boolean isPending() {
        return ticksRemaining >= 0;
    }
}
