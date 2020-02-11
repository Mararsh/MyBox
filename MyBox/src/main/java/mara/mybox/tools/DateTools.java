package mara.mybox.tools;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 9:44:53
 * @Description
 * @License Apache License Version 2.0
 */
public class DateTools {

    public static String nowString() {
        return datetimeToString(new Date());
    }

    public static String nowString3() {
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat3);
        formatter.setTimeZone(AppVariables.getTimeZone());
        String dateString = formatter.format(new Date());
        return dateString;
    }

    public static String nowString4() {
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat4);
        formatter.setTimeZone(AppVariables.getTimeZone());
        String dateString = formatter.format(new Date());
        return dateString;
    }

    public static String nowString5() {
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat5, CommonValues.LocaleEnUS);
        formatter.setTimeZone(AppVariables.getTimeZone());
        String dateString = formatter.format(new Date());
        return dateString;
    }

    public static String datetimeToString(long dvalue) {
        return datetimeToString(new Date(dvalue));
    }

    public static String datetimeToString(Date theDate) {
        return datetimeToString(theDate, AppVariables.getTimeZone());
    }

    public static String datetimeToString(Date theDate, TimeZone theZone) {
        return datetimeToString(theDate, CommonValues.DatetimeFormat, theZone);
    }

    public static String datetimeToString(Date theDate, String format) {
        return datetimeToString(theDate, format, AppVariables.getTimeZone());
    }

    public static String datetimeToString(Date theDate, String format,
            TimeZone theZone) {
        if (theDate == null || theZone == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(theZone);
        String dateString = formatter.format(theDate);
        return dateString;
    }

    public static String datetime5ToString(Date theDate) {
        if (theDate == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat5, CommonValues.LocaleEnUS);
        formatter.setTimeZone(AppVariables.getTimeZone());
        String dateString = formatter.format(theDate);
        return dateString;
    }

    public static String stringToString(String theDate) {
        return datetimeToString(stringToDatetime(theDate));
    }

    public static Date localDate2Date(LocalDate localDate) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
            Date date = Date.from(zdt.toInstant());
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate date2LocalDate(Date date) {
        try {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDate localDate = instant.atZone(zoneId).toLocalDate();
            return localDate;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDatetime(String strDate) {
        return DateTools.stringToDatetime(strDate, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date stringToDatetime5(String strDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat5, CommonValues.LocaleEnUS);
//            formatter.setTimeZone(theZone);
            ParsePosition pos = new ParsePosition(0);
            return formatter.parse(strDate, pos);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDatetime(String strDate, String format) {
        if (strDate == null || strDate.isEmpty()
                || format == null || format.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
//            formatter.setTimeZone(theZone);
            ParsePosition pos = new ParsePosition(0);
            return formatter.parse(strDate, pos);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseMonth(String month) {
        return stringToDatetime(month, "yyyy-MM");
    }

    public static Date thisMonth() {
        return DateTools.stringToDatetime(nowString().substring(0, 7), "yyyy-MM");
    }

    public static String dateToMonthString(Date theDate) {
        return datetimeToString(theDate).substring(0, 7);
    }

    public static String dateToYearString(Date theDate) {
        return datetimeToString(theDate).substring(0, 4);
    }

    public static boolean isWeekend(long time) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(time));
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    public static String showTime(long milliseconds) {
        long ms = milliseconds;
        if (milliseconds < 1000) {
            return MessageFormat.format(message("MillisecondsNumber"), ms);
        }
        long seconds = ms / 1000;
        ms = ms % 1000;
        if (seconds < 60) {
            return MessageFormat.format(message("SecondsNumber"), seconds, ms);
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return MessageFormat.format(message("MinutesNumber"), minutes, seconds, ms);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours < 24) {
            return MessageFormat.format(message("HoursNumber"), hours, minutes, seconds, ms);
        }
        long days = hours / 24;
        hours = hours % 24;
        if (days < 365) {
            return MessageFormat.format(message("DaysNumber"), days, hours, minutes, seconds, ms);
        }
        long years = days / 365;
        days = days % 365;
        return MessageFormat.format(message("YearsNumber"), years, days, hours, minutes, seconds, ms);
    }

    public static String showDuration(long milliseconds) {
        long ms = milliseconds;
        if (milliseconds < 1000) {
            return String.format("00:00:0.%03d", ms);
        }
        long seconds = ms / 1000;
        ms = ms % 1000;
        if (seconds < 60) {
            return String.format("00:%02d.%03d", seconds, ms);
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%02d:%02d.%03d", minutes, seconds, ms);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }

    public static String showSeconds(long seconds) {
        if (seconds < 60) {
            return String.format("00:%02d", seconds);
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    }

}
