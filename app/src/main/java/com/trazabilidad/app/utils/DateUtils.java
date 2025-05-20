package com.trazabilidad.app.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    public static String getRelativeDateTimeString(Context context, Date date) {
        if (date == null) {
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        calendar.setTime(date);

        // Verificar si es hoy
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Hoy " + android.text.format.DateFormat.format("HH:mm", date);
        }

        // Verificar si es ayer
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Ayer " + android.text.format.DateFormat.format("HH:mm", date);
        }

        // Si no es ni hoy ni ayer, usar formato estándar
        return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", date).toString();
    }
}
