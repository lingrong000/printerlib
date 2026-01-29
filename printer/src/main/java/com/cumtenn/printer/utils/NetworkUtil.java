package com.cumtenn.printer.utils;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class NetworkUtil {
    private static final String IPV4_PATTERN =
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern ipv4Pattern = Pattern.compile(IPV4_PATTERN);

    public static boolean isIpAddressValid(String ipAddress) {
        if (TextUtils.isEmpty(ipAddress)) {
            return false;
        }

        return ipv4Pattern.matcher(ipAddress).matches();
    }

}
