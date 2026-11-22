package ru.teampicker.app;

/** Full three-mode cycle, normalization and legible text colors. */
public final class ThemePaletteTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
    private static double luminance(int argb) {
        return 0.2126 * linear((argb >>> 16) & 255)
                + 0.7152 * linear((argb >>> 8) & 255)
                + 0.0722 * linear(argb & 255);
    }
    private static double contrast(int a, int b) {
        double first = luminance(a), second = luminance(b);
        return (Math.max(first, second) + 0.05) / (Math.min(first, second) + 0.05);
    }

    public static void main(String[] args) {
        require(ThemePalette.normalize(-10) == ThemePalette.LIGHT, "negative mode");
        require(ThemePalette.normalize(123) == ThemePalette.LIGHT, "invalid mode");
        require(ThemePalette.next(ThemePalette.LIGHT) == ThemePalette.DARK, "light -> dark");
        require(ThemePalette.next(ThemePalette.DARK) == ThemePalette.COLOR, "dark -> color");
        require(ThemePalette.next(ThemePalette.COLOR) == ThemePalette.LIGHT, "color -> light");
        for (int mode = 0; mode < 3; mode++) {
            ThemePalette t = ThemePalette.of(mode);
            require(t.mode == mode, "correct mode");
            require(!t.label.isEmpty(), "visible theme label");
            require(!t.symbol.isEmpty(), "visible theme icon");
            require(contrast(t.ink, t.background) >= 4.5, "main text legible: " + mode);
            require(contrast(t.ink, t.surface) >= 4.5, "card text legible: " + mode);
            require(contrast(t.onAccent, t.accent) >= 4.5, "action text legible: " + mode);
            require(contrast(t.muted, t.surface) >= 4.5, "secondary text legible: " + mode);
            require(contrast(t.hint, t.background) >= 3.0, "placeholder visible: " + mode);
        }
        require(ThemePalette.of(ThemePalette.DARK).dark, "dark status bar flag");
        require(!ThemePalette.of(ThemePalette.COLOR).dark, "color uses light system icons");
        System.out.println("ThemePaletteTest PASS");
    }
}
