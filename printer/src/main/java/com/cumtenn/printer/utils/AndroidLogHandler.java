package com.cumtenn.printer.utils;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class AndroidLogHandler extends Handler {

    @Override
    public void publish(LogRecord record) {
        String tagName = record.getLoggerName();
        if (tagName == null) {
            tagName = "IppClient";
        }
        // 只取最后一段作为tag
        if (tagName.contains(".")) {
            tagName = tagName.substring(tagName.lastIndexOf(".") + 1);
        }

        String message = record.getMessage();
        if (record.getThrown() != null) {
            message += "\n" + Log.getStackTraceString(record.getThrown());
        }

        int level = record.getLevel().intValue();
        if (level >= Level.SEVERE.intValue()) {
            Log.e(tagName, message);
        } else if (level >= Level.WARNING.intValue()) {
            Log.w(tagName, message);
        } else if (level >= Level.INFO.intValue()) {
            Log.i(tagName, message);
        } else if (level >= Level.FINE.intValue()) {
            Log.d(tagName, message);
        } else {
            Log.v(tagName, message);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
