package ru.teampicker.app;

/** One-shot state for the splash's press-to-hold interaction. */
final class SplashHoldState {
    private boolean holding;
    private boolean completed;

    boolean press() {
        if (completed) return false;
        holding = true;
        return true;
    }

    boolean isHolding() {
        return holding;
    }

    /** Returns true exactly once, when the user releases a held splash. */
    boolean release() {
        if (completed || !holding) return false;
        holding = false;
        completed = true;
        return true;
    }

    /** Returns true only when the animation ends without an active hold. */
    boolean animationFinished() {
        if (completed || holding) return false;
        completed = true;
        return true;
    }
}
