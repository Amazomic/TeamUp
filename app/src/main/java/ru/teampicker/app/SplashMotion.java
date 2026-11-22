package ru.teampicker.app;

/** Deterministic, resolution-independent paths for every animation frame. */
final class SplashMotion {
    static final long DURATION_MS = 2300L;
    static final int PLAYER_COUNT = 6;

    // Logical screen coordinates; SplashView scales them to the actual viewport.
    private static final float[][] START = {
        {64f, 180f}, {160f, 290f}, {95f, 450f},
        {280f, 200f}, {290f, 360f}, {200f, 470f}
    };
    private static final float[][] CONTROL = {
        {48f, 250f}, {112f, 210f}, {85f, 330f},
        {308f, 255f}, {266f, 276f}, {292f, 320f}
    };
    private static final float[][] END = {
        {98f, 328f}, {151f, 350f}, {125f, 390f},
        {225f, 328f}, {274f, 350f}, {249f, 390f}
    };

    private SplashMotion() {}

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float smooth(float value) {
        float p = clamp(value);
        return p * p * (3f - 2f * p);
    }

    static float movement(float progress) {
        return smooth((progress - .10f) / .66f);
    }

    static float ringVisibility(float progress) {
        return smooth((progress - .26f) / .54f);
    }

    static float titleVisibility(float progress) {
        return smooth((progress - .74f) / .20f);
    }

    static float[] position(int player, float progress) {
        if (player < 0 || player >= PLAYER_COUNT)
            throw new IllegalArgumentException("Player out of range: " + player);
        float t = movement(progress);
        float inverse = 1f - t;
        float x = inverse * inverse * START[player][0]
                + 2f * inverse * t * CONTROL[player][0]
                + t * t * END[player][0];
        float y = inverse * inverse * START[player][1]
                + 2f * inverse * t * CONTROL[player][1]
                + t * t * END[player][1];
        return new float[]{x, y};
    }
}
