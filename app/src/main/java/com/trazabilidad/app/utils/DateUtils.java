package com.trazabilidad.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_TIME_FORMAT.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return TIME_FORMAT.format(date);
    }

    public static String getTimeStamp() {
        return TIMESTAMP_FORMAT.format(new Date());
    }

    public static Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseDateTime(String dateTimeStr) {
        try {
            return DATE_TIME_FORMAT.parse(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
