package ru.teampicker.app;

/**
 * Continuous, overlapping transition from the animated splash to the app.
 * Package-private and free of Android dependencies for fast regression tests.
 */
final class SplashTransition {
    static final long DURATION_MS = 540L;

    private SplashTransition() {}

    private static float smooth(float progress) {
        float p = Math.max(0f, Math.min(1f, progress));
        return p * p * (3f - 2f * p);
    }

    /** The outgoing illustration lingers while the actual UI begins to appear. */
    static float introAlpha(float progress) {
        return 1f - smooth((progress - .18f) / .82f);
    }

    /** The app becomes legible before the splash is completely transparent. */
    static float contentAlpha(float progress) {
        return smooth(progress / .80f);
    }

    /** Fraction of the initial vertical lift still remaining. */
    static float contentOffset(float progress) {
        return 1f - smooth(progress);
    }

    /** Small zoom-out of the illustration makes the dissolve feel intentional. */
    static float introZoom(float progress) {
        return 1f + .025f * smooth(progress);
    }
}
