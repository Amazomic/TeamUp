package ru.teampicker.app;

/** Hold at any instant of the intro freezes its final frame until release. */
public final class SplashHoldStateTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        SplashHoldState normal = new SplashHoldState();
        require(!normal.isHolding(), "initially not held");
        require(normal.animationFinished(), "normal expiry transitions");
        require(!normal.animationFinished(), "expiry callback runs once");
        require(!normal.press(), "finished intro must ignore new touches");

        SplashHoldState early = new SplashHoldState();
        require(early.press(), "press during loading accepted");
        require(early.isHolding(), "press changes state");
        require(!early.animationFinished(), "time cannot dismiss a held splash");
        require(early.isHolding(), "hold is retained after natural duration");
        require(early.release(), "release continues to main screen");
        require(!early.release(), "duplicate release cannot dismiss twice");
        require(!early.animationFinished(), "time after release is a no-op");

        SplashHoldState late = new SplashHoldState();
        require(late.press(), "press near the end accepted");
        require(!late.animationFinished(), "press prevents timeout");
        require(late.release(), "late release resumes");
        SplashHoldState noPress = new SplashHoldState();
        require(!noPress.release(), "stray ACTION_UP doesn't dismiss splash");
        require(noPress.animationFinished(), "stray release must not block natural expiry");

        System.out.println("SplashHoldStateTest PASS");
    }
}
