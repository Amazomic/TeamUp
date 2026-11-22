package ru.teampicker.app;

/** Centralized UI palette with a persistent, three-state cycle. Pure Java for testing. */
final class ThemePalette {
    static final int LIGHT = 0;
    static final int DARK = 1;
    static final int COLOR = 2;

    final int mode;
    final String label;
    final String symbol;
    final int background;
    final int backgroundEnd;
    final int ink;
    final int muted;
    final int accent;
    final int onAccent;
    final int surface;
    final int control;
    final int hint;
    final int historySurface;
    final boolean dark;

    private ThemePalette(int mode, String label, String symbol, int background,
                         int backgroundEnd, int ink, int muted, int accent,
                         int onAccent, int surface, int control, int hint,
                         int historySurface, boolean dark) {
        this.mode = mode;
        this.label = label;
        this.symbol = symbol;
        this.background = background;
        this.backgroundEnd = backgroundEnd;
        this.ink = ink;
        this.muted = muted;
        this.accent = accent;
        this.onAccent = onAccent;
        this.surface = surface;
        this.control = control;
        this.hint = hint;
        this.historySurface = historySurface;
        this.dark = dark;
    }

    static int normalize(int mode) {
        return mode >= LIGHT && mode <= COLOR ? mode : LIGHT;
    }

    static int next(int mode) {
        return (normalize(mode) + 1) % 3;
    }

    static ThemePalette of(int requestedMode) {
        switch (normalize(requestedMode)) {
            case DARK:
                return new ThemePalette(DARK, "Dark", "☾",
                        0xFF101827, 0xFF18243C, 0xFFF0F4FF, 0xFFB5C2D6,
                        0xFF91B5FF, 0xFF101827, 0xFF1C293D, 0xFF27334B,
                        0xFFA7B6CF, 0xFF24334A, true);
            case COLOR:
                return new ThemePalette(COLOR, "Color", "✦",
                        0xFFE5FAF4, 0xFFF6E8FF, 0xFF183247, 0xFF486577,
                        0xFF087584, 0xFFFFFFFF, 0xFFFCFEFF, 0xFFC8EEE9,
                        0xFF577A85, 0xFFFFF2E7, false);
            default:
                return new ThemePalette(LIGHT, "Light", "☼",
                        0xFFF6F7FB, 0xFFF6F7FB, 0xFF222A3B, 0xFF667188,
                        0xFF4F60E0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFF0F2FB,
                        0xFF7A8295, 0xFFF7F8FC, false);
        }
    }
}
