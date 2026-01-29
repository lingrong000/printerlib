package com.cumtenn.printer.model;

public enum Quality {
    Draft(3), Normal(4), High(5);

    private final int code;

    Quality(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 通过code获取enum
    public static Quality fromInt(int code) {
        for (Quality quality : values()) {
            if (quality.code == code) {
                return quality;
            }
        }
        throw new IllegalArgumentException("Invalid Quality code: " + code);
    }
}
