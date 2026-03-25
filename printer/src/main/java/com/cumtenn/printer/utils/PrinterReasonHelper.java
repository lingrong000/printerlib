package com.cumtenn.printer.utils;

import android.content.Context;
import android.text.TextUtils;

import com.cumtenn.printer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrinterReasonHelper {

    private static final Map<String, Integer> MAP = new HashMap<>();

    static {
        // Media
        MAP.put("media-empty", R.string.print_reason_media_empty);
        MAP.put("media-needed", R.string.print_reason_media_needed);
        MAP.put("media-jam", R.string.print_reason_media_jam);
        MAP.put("media-low", R.string.print_reason_media_low);
        MAP.put("input-tray-missing", R.string.print_reason_input_tray_missing);
        MAP.put("output-area-full", R.string.print_reason_output_area_full);

        // Supplies
        MAP.put("toner-empty", R.string.print_reason_toner_empty);
        MAP.put("toner-low", R.string.print_reason_toner_low);
        MAP.put("ink-empty", R.string.print_reason_ink_empty);
        MAP.put("ink-low", R.string.print_reason_ink_low);
        MAP.put("marker-supply-empty", R.string.print_reason_marker_supply_empty);
        MAP.put("marker-supply-low", R.string.print_reason_marker_supply_low);
        MAP.put("marker-waste-full", R.string.print_reason_marker_waste_full);

        // Hardware
        MAP.put("door-open", R.string.print_reason_door_open);
        MAP.put("cover-open", R.string.print_reason_cover_open);
        MAP.put("interlock-open", R.string.print_reason_interlock_open);
        MAP.put("internal-storage-full", R.string.print_reason_internal_storage_full);
        MAP.put("hardware-fault", R.string.print_reason_hardware_fault);

        // System
        MAP.put("offline", R.string.print_reason_offline);
        MAP.put("paused", R.string.print_reason_paused);
        MAP.put("shutdown", R.string.print_reason_shutdown);
        MAP.put("timed-out", R.string.print_reason_timed_out);
        MAP.put("stopping", R.string.print_reason_stopping);
    }

    /**
     * 将获取到的 state-reasons（支持逗号分隔）转换为中文描述
     */
    public static String getDescriptionByReason(Context context, List<String> reasonList) {
        if (reasonList == null || reasonList.isEmpty()) return "";

        List<String> results = new ArrayList<>();

        for (String r : reasonList) {
            String cleanKey = r.trim()
                    .replaceAll("-(report|warning|error)$", "");

            if (MAP.containsKey(cleanKey)) {
                results.add(context.getString(MAP.get(cleanKey)));
            } else {
                results.add(r);
            }
        }

        if (results.isEmpty()) {
            return context.getString(R.string.print_reason_unknown);
        }
        return TextUtils.join(", ", results);
    }
}

