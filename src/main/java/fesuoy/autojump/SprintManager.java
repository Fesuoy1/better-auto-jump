package fesuoy.autojump;

public class SprintManager {

    private static boolean wasSprintingAtEdge = false;

    public static void record(boolean isSprinting) {
        wasSprintingAtEdge = isSprinting;
    }

    public static boolean wasSprintingBeforeJump() {
        return wasSprintingAtEdge;
    }

    public static void reset() {
        wasSprintingAtEdge = false;
    }
}
