package com.cumtenn.printer.utils;

import java.util.HashMap;
import java.util.Map;

import de.gmuth.ipp.attributes.MediaSize;

public class PrintParamHelper {
    public static final Map<String, MediaSize> SIZE_MAP = new HashMap<>();

    static {
        SIZE_MAP.put("iso_a3_297x420mm", new MediaSize(29700, 42000));
        SIZE_MAP.put("iso_a4_210x297mm", new MediaSize(21000, 29700));
        SIZE_MAP.put("iso_a5_148x210mm", new MediaSize(14800, 21000));
        SIZE_MAP.put("iso_a6_105x148mm", new MediaSize(10500, 14800));
        SIZE_MAP.put("na_letter_8.5x11in", new MediaSize(21590, 27940));
        SIZE_MAP.put("na_executive_7.25x10.5in", new MediaSize(18415, 26670));
        SIZE_MAP.put("jis_b5_182x257mm", new MediaSize(18200, 25700));
        SIZE_MAP.put("jis_b6_128x182mm", new MediaSize(12800, 18200));
    }
}
