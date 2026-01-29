package com.cumtenn.printer.model;

public enum Orientation {
    Portrait(3),
    Landscape(4),
    ReverseLandscape(5),
    ReversePortrait(6),
    None(7);

    private final int code;

    Orientation(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 通过code获取enum
    public static Orientation fromInt(int code) {
        for (Orientation orientation : values()) {
            if (orientation.code == code) {
                return orientation;
            }
        }
        throw new IllegalArgumentException("Invalid Orientation code: " + code);
    }
}
