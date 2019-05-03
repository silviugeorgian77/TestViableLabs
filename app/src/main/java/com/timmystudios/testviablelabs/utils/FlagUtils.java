package com.timmystudios.testviablelabs.utils;

/**
 * See <a href="https://www.countryflags.io">the official site for Country Flags </a>
 */
public class FlagUtils {

    public enum Style {
        FLAT("flat"),
        SHYNY("shiny");

        String name;

        Style(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum Width {
        PX_16(16),
        PX_24(24),
        PX_32(32),
        PX_48(48),
        PX_64(64);

        int width;

        Width(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }
    }

    public static final String URL = "https://www.countryflags.io/%s/%s/%s.png";

    public static String getFlagUrl(String countryCode, Width width, Style style) {
        return String.format(URL, countryCode, style.getName(), width.getWidth());
    }

}
