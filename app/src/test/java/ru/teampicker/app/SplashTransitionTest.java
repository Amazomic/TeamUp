package ru.teampicker.app;

/** Dependency-free regression test for overlapping splash-to-content transition. */
public final class SplashTransitionTest {
    private static void close(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.0001f) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(SplashTransition.DURATION_MS >= 350L
                        && SplashTransition.DURATION_MS <= 750L,
                "Crossfade should feel gentle without delaying entry");
        close(SplashTransition.introAlpha(-1f), 1f, "intro before transition");
        close(SplashTransition.contentAlpha(-1f), 0f, "content before transition");
        close(SplashTransition.contentOffset(-1f), 1f, "content starts slightly below");
        close(SplashTransition.introAlpha(2f), 0f, "intro after transition");
        close(SplashTransition.contentAlpha(2f), 1f, "content fully revealed");
        close(SplashTransition.contentOffset(2f), 0f, "content reaches final position");
        close(SplashTransition.introZoom(0f), 1f, "intro starts unscaled");
        check(SplashTransition.introZoom(1f) > 1f
                        && SplashTransition.introZoom(1f) < 1.06f,
                "intro zoom is subtle");

        float previousIntro = 1f;
        float previousContent = 0f;
        float previousOffset = 1f;
        for (int i = 0; i <= 144; i++) {
            float progress = i / 144f;
            float intro = SplashTransition.introAlpha(progress);
            float content = SplashTransition.contentAlpha(progress);
            float offset = SplashTransition.contentOffset(progress);
            check(intro >= 0f && intro <= 1f, "intro alpha in range");
            check(content >= 0f && content <= 1f, "content alpha in range");
            check(intro <= previousIntro + 0.0001f, "intro fades monotonically");
            check(content + 0.0001f >= previousContent,
                    "content fades in monotonically");
            check(offset <= previousOffset + 0.0001f,
                    "content rises smoothly, never falls back");
            check(intro + content >= 0.90f,
                    "overlapping opacity avoids a blank flicker");
            previousIntro = intro;
            previousContent = content;
            previousOffset = offset;
        }
        check(SplashTransition.introAlpha(0.5f) > 0f
                        && SplashTransition.contentAlpha(0.5f) > 0f,
                "both screens are simultaneously visible during the dissolve");
        System.out.println("SplashTransitionTest PASS (144 overlapping fade steps)");
    }
}
