package ru.teampicker.app;

// Dependency-free regression test. Run with javac/java from the project root.
public final class SplashMotionTest {
    private static void near(float actual, float expected, float tolerance, String what) {
        if (Math.abs(actual - expected) > tolerance) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        check(SplashMotion.DURATION_MS >= 1800 && SplashMotion.DURATION_MS <= 3000,
                "Splash should not block the app for too long");

        for (int player = 0; player < SplashMotion.PLAYER_COUNT; player++) {
            float[] start = SplashMotion.position(player, -1f);
            float[] initial = SplashMotion.position(player, 0f);
            float[] end = SplashMotion.position(player, 1f);
            float[] beyond = SplashMotion.position(player, 2f);
            near(start[0], initial[0], .001f, "start x clamps");
            near(start[1], initial[1], .001f, "start y clamps");
            near(end[0], beyond[0], .001f, "end x clamps");
            near(end[1], beyond[1], .001f, "end y clamps");
            check(Math.abs(end[0] - initial[0]) > 8f,
                    "each player must visibly move to a team");
            for (int frame = 0; frame <= 144; frame++) {
                float progress = frame / 144f;
                float[] pos = SplashMotion.position(player, progress);
                check(Float.isFinite(pos[0]) && Float.isFinite(pos[1]),
                        "interpolated frame must be valid");
                check(pos[0] >= 0f && pos[0] <= 360f &&
                      pos[1] >= 100f && pos[1] <= 600f,
                      "player stays on screen");
            }
        }

        near(SplashMotion.ringVisibility(0f), 0f, .001f, "ring initially hidden");
        near(SplashMotion.ringVisibility(1f), 1f, .001f, "ring complete");
        near(SplashMotion.titleVisibility(.50f), 0f, .001f, "logo not too early");
        near(SplashMotion.titleVisibility(1f), 1f, .001f, "logo fully visible");

        float before = 0f;
        for (int frame = 0; frame <= 144; frame++) {
            float progress = frame / 144f;
            float motion = SplashMotion.movement(progress);
            check(motion + .0001f >= before, "no backward movement");
            near(SplashMotion.movement(progress), motion, .0001f, "deterministic frame");
            before = motion;
        }

        check(SplashMotion.movement(.25f) > 0f && SplashMotion.movement(.25f) < 1f,
                "intermediate motion frames must exist");
        check(SplashMotion.movement(.50f) > SplashMotion.movement(.25f),
                "motion advances continuously between keyframes");
        check(SplashMotion.movement(.75f) > SplashMotion.movement(.50f),
                "movement is completed gradually");
        System.out.println("SplashMotionTest PASS (144 interpolated frames, 6 players)");
    }
}
