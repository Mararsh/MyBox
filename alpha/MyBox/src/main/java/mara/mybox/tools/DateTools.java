package mara.mybox.tools;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import mara.mybox.data.Era;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 9:44:53
 * @License Apache License Version 2.0
 */
public class DateTools {

    // invalid values are always in the end
    public static int compare(String s1, String s2, boolean desc) {
        double d1, d2;
        SimpleDateFormat df = new SimpleDateFormat();
        try {
            d1 = df.parse(s1).getTime();
        } catch (Exception e) {
            d1 = Double.NaN;
        }
        try {
            d2 = df.parse(s2).getTime();
        } catch (Exception e) {
            d2 = Double.NaN;
        }
        return DoubleTools.compare(d1, d2, desc);
    }

    public static void printFormats() {
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.CHINESE).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.ENGLISH).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CHINESE).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ENGLISH).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.CHINESE).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.ENGLISH).format(new Date()));
        MyBoxLog.console(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.CHINESE).format(new Date()));

    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public static String nowFileString() {
        SimpleDateFormat formatter = new SimpleDateFormat(TimeFormats.Datetime2);
        formatter.setTimeZone(getTimeZone());
        String dateString = formatter.format(new Date());
        return dateString;
    }

    public static String nowString() {
        return datetimeToString(new Date());
    }

    public static String nowString3() {
        return datetimeToString(new Date(), TimeFormats.Datetime3);
    }

    public static boolean isBC(long value) {
        if (value == AppValues.InvalidLong) {
            return false;
        }
        String s = datetimeToString(new Date(value), TimeFormats.EraDatetimeEnd, Languages.LocaleEn, null);
        return s.contains("BC");
    }

    public static String textEra(Era era) {
        if (era == null || era.getValue() == AppValues.InvalidLong) {
            return "";
        }
        return textEra(era.getValue(), era.getFormat());
    }

    public static String textEra(String value) {
        return textEra(encodeEra(value));
    }

    public static String textEra(Date value) {
        return value == null ? null : textEra(value.getTime());
    }

    public static String textEra(long value) {
        if (value == AppValues.InvalidLong) {
            return "";
        }
        return textEra(value, isBC(value) ? TimeFormats.EraDatetimeEnd : TimeFormats.Datetime);
    }

    public static String textEra(long value, String format) {
        if (value == AppValues.InvalidLong) {
            return "";
        }
        return datetimeToString(new Date(value), format, Languages.locale(), null);
    }

    public static String eraFormatStart(String s) {
        String format;
        if (s.contains(":") && s.contains("-")) {
            if (s.contains(".")) {
                format = TimeFormats.EraDatetimeMsStart;
            } else {
                format = TimeFormats.EraDatetimeStart;
            }
            if (s.contains("+")) {
                format += " Z";
            }
        } else if (s.contains("-")) {
            String ss = s;
            if (ss.startsWith("-")) {
                ss = ss.substring(1);
            }
            if (!ss.contains("-")) {
                format = TimeFormats.EraYearStart;
            } else {
                if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                    format = TimeFormats.EraMonthStart;
                } else {
                    format = TimeFormats.EraDateStart;
                }
            }
        } else if (s.contains(":")) {
            if (s.contains(".")) {
                format = TimeFormats.TimeMsA;
            } else {
                format = TimeFormats.TimeA;
            }
        } else {
            format = TimeFormats.EraYearStart;
        }
        return format;
    }

    public static String eraFormatEnd(String s) {
        String format;
        if (s.contains(":") && s.contains("-")) {
            if (s.contains(".")) {
                format = TimeFormats.EraDatetimeMsEnd;
            } else {
                format = TimeFormats.EraDatetimeEnd;
            }
            if (s.contains("+")) {
                format += " Z";
            }
        } else if (s.contains("-")) {
            String ss = s;
            if (ss.startsWith("-")) {
                ss = ss.substring(1);
            }
            if (!ss.contains("-")) {
                format = TimeFormats.EraYearEnd;
            } else {
                if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                    format = TimeFormats.EraMonthEnd;
                } else {
                    format = TimeFormats.EraDateEnd;
                }
            }
        } else if (s.contains(":")) {
            if (s.contains(".")) {
                format = TimeFormats.TimeMsA;
            } else {
                format = TimeFormats.TimeA;
            }
        } else {
            format = TimeFormats.EraYearEnd;
        }
        return format;
    }

    public static Date encodeEra(String strDate) {
        if (strDate == null) {
            return null;
        }
        String s = strDate.trim().replace("T", " "), format;
        Locale locale;
        if (s.startsWith(message("zh", "BC")) || s.startsWith(message("zh", "AD"))) {
            locale = Languages.LocaleZhCN;
            format = eraFormatStart(s);

        } else if (s.startsWith(message("en", "BC")) || s.startsWith(message("en", "AD"))) {
            locale = Languages.LocaleEn;
            format = eraFormatStart(s);

        } else if (s.endsWith(message("zh", "BC")) || s.endsWith(message("zh", "AD"))) {
            locale = Languages.LocaleZhCN;
            format = eraFormatEnd(s);

        } else if (s.endsWith(message("en", "BC")) || s.endsWith(message("en", "AD"))) {
            locale = Languages.LocaleEn;
            format = eraFormatEnd(s);

        } else {
            return encodeDate(s);
        }
        Date d = stringToDatetime(s, format, locale);
//        MyBoxLog.debug(s + "  " + locale.getLanguage() + " " + format + "  " + locale + "  "
//                + DateTools.textEra(d.getTime(), Era.Format.Datetime, false));
        return d;
    }

    public static Date encodeDate(String strDate) {
        return encodeDate(strDate, Languages.locale());
    }

    public static Date encodeDate(String strDate, Locale locale) {
        try {
            if (strDate == null || strDate.isBlank()) {
                return null;
            }
            String s = strDate.trim().replace("T", " "), format;
            String ss = s;
            if (ss.startsWith("-")) {
                ss = s.substring(1);
            }
            if (s.contains("/")) {
                if (s.contains(":")) {
                    if (s.contains(".")) {
                        format = TimeFormats.DatetimeMsE;
                    } else {
                        format = TimeFormats.DatetimeE;
                    }
                    if (s.contains("+")) {
                        format += " Z";
                    }
                } else if (s.indexOf("/") == s.lastIndexOf("/")) {
                    format = TimeFormats.MonthE;
                } else {
                    format = TimeFormats.DateE;
                }
            } else if (ss.contains("-")) {
                if (ss.contains(":")) {
                    if (ss.contains(".")) {
                        format = TimeFormats.DatetimeMsA;
                    } else {
                        format = TimeFormats.DatetimeA;
                    }
                    if (ss.contains("+")) {
                        format += " Z";
                    }
                } else if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                    format = TimeFormats.MonthA;
                } else {
                    format = TimeFormats.DateA;
                }
            } else if (s.contains(":")) {
                if (s.contains(".")) {
                    format = TimeFormats.TimeMs;
                } else {
                    format = TimeFormats.Time;
                }
            } else {
                format = TimeFormats.YearA;
            }
//            MyBoxLog.debug(s + "  " + format);
//        MyBoxLog.debug(s + "  " + format + "  " + locale + "  "
//                + stringToDatetime(s, format, locale).getTime());
            return stringToDatetime(s, format, locale);
        } catch (Exception e) {
            return null;
        }
    }

    public static String datetimeToString(long dvalue) {
        if (dvalue == AppValues.InvalidLong) {
            return null;
        }
        return datetimeToString(new Date(dvalue));
    }

    public static String datetimeToString(Date theDate) {
        if (theDate == null) {
            return null;
        }
        String format = isBC(theDate.getTime()) ? TimeFormats.EraDatetimeEnd : TimeFormats.Datetime;
        return datetimeToString(theDate, format, null, null);
    }

    public static String datetimeToString(long theDate, String format) {
        if (theDate == AppValues.InvalidLong) {
            return null;
        }
        return datetimeToString(new Date(theDate), format);
    }

    public static String datetimeToString(Date theDate, String format) {
        return datetimeToString(theDate, format, null, null);
    }

    public static String datetimeToString(Date theDate, String inFormat, Locale inLocale, TimeZone inZone) {
        if (theDate == null) {
            return null;
        }
        String format = inFormat;
        if (format == null || format.isBlank()) {
            format = isBC(theDate.getTime()) ? TimeFormats.EraDatetimeEnd : TimeFormats.Datetime;
        }
        Locale locale = inLocale;
        if (locale == null) {
            locale = Languages.locale();
        }
        TimeZone zone = inZone;
        if (zone == null) {
            zone = getTimeZone();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
        formatter.setTimeZone(zone);
        String dateString = formatter.format(theDate);
        return dateString;
    }

    public static Date localDateToDate(LocalDate localDate) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
            Date date = Date.from(zdt.toInstant());
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate dateToLocalDate(Date date) {
        try {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDate localDate = instant.atZone(zoneId).toLocalDate();
            return localDate;
        } catch (Exception e) {
            return null;
        }
    }

    public static String localDateToString(LocalDate localDate) {
        return dateToString(localDateToDate(localDate));
    }

    public static LocalDate stringToLocalDate(String strDate) {
        return dateToLocalDate(encodeEra(strDate));
    }

    public static Date stringToDatetime(String strDate, String format) {
        return stringToDatetime(strDate, format, Locale.getDefault());
    }

    public static Date stringToDatetime(String strDate, String format, Locale locale) {
        if (strDate == null || strDate.isEmpty()
                || format == null || format.isEmpty()) {
            return null;
        }
//        MyBoxLog.debug(strDate + "  " + format + "  " + locale);
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
            return formatter.parse(strDate, new ParsePosition(0));
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseMonth(String month) {
        return DateTools.encodeEra(month);
    }

    public static Date thisMonth() {
        return DateTools.stringToDatetime(nowString().substring(0, 7), "yyyy-MM");
    }

    public static String dateToString(Date theDate) {
        return datetimeToString(theDate, TimeFormats.Date);
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

    public static String datetimeMsDuration(long milliseconds) {
        String f = milliseconds < 0 ? "-" : "";
        long ms = Math.abs(milliseconds);
        String date = dateDuration(ms);
        String timeMs = timeMsDuration(ms);
        return f + (date.isBlank() ? timeMs : date + " " + timeMs);
    }

    public static String dateDuration(long milliseconds) {
        String f = milliseconds < 0 ? "-" : "";
        long ms = Math.abs(milliseconds);
        int days = (int) (ms / (24 * 3600 * 1000));
        int years = days / 365;
        days = days % 365;
        int month = days / 12;
        days = days % 12;
        return f + dateDuration(years, month, days);
    }

    public static String dateDuration(Date time1, Date time2) {
        Period period = period(time1, time2);
        if (period == null) {
            return null;
        }
        String date = dateDuration(period.getYears(), period.getMonths(), period.getDays());
        return (period.isNegative() ? "-" : "") + date;
    }

    public static String dateDuration(int years, int months, int days) {
        String date;
        if (years > 0) {
            if (months > 0) {
                if (days > 0) {
                    date = MessageFormat.format(message("DurationYearsMonthsDays"),
                            years, months, days);
                } else {
                    date = MessageFormat.format(message("DurationYearsMonths"),
                            years, months);
                }
            } else if (days > 0) {
                date = MessageFormat.format(message("DurationYearsDays"),
                        years, days);
            } else {
                date = MessageFormat.format(message("DurationYears"),
                        years);
            }
        } else {
            if (months > 0) {
                if (days > 0) {
                    date = MessageFormat.format(message("DurationMonthsDays"),
                            months, days);
                } else {
                    date = MessageFormat.format(message("DurationMonths"),
                            months);
                }
            } else if (days > 0) {
                date = MessageFormat.format(message("DurationDays"),
                        days);
            } else {
                date = "";
            }
        }
        return date;
    }

    public static String yearsMonthsDuration(Date time1, Date time2) {
        Period period = period(time1, time2);
        String date;
        if (period.getYears() > 0) {
            date = MessageFormat.format(message("DurationYearsMonths"),
                    period.getYears(), period.getMonths());
        } else if (period.getMonths() > 0) {
            date = MessageFormat.format(message("DurationMonths"),
                    period.getYears(), period.getMonths());
        } else {
            date = "";
        }
        return (period.isNegative() ? "-" : "") + date;
    }

    public static String yearsDuration(Date time1, Date time2) {
        Period period = period(time1, time2);
        String date;
        if (period.getYears() > 0) {
            date = MessageFormat.format(message("DurationYears"),
                    period.getYears());
        } else {
            date = "";
        }
        return (period.isNegative() ? "-" : "") + date;
    }

    public static String timeDuration(Date time1, Date time2) {
        Duration duration = duration(time1, time2);
        return (duration.isNegative() ? "-" : "")
                + timeDuration(duration.getSeconds() * 1000);
    }

    public static String msDuration(Date time1, Date time2) {
        return timeMsDuration(time1.getTime() - time2.getTime());
    }

    public static String datetimeDuration(Date time1, Date time2) {
        return dateDuration(time1, time2) + " "
                + timeDuration(Math.abs(time1.getTime() - time2.getTime()));
    }

    public static String datetimeZoneDuration(Date time1, Date time2) {
        return dateDuration(time1, time2) + " "
                + timeDuration(Math.abs(time1.getTime() - time2.getTime())) + " "
                + TimeZone.getDefault().getDisplayName();
    }

    public static String datetimeMsDuration(Date time1, Date time2) {
        return dateDuration(time1, time2) + " "
                + timeMsDuration(Math.abs(time1.getTime() - time2.getTime()));
    }

    public static String datetimeMsZoneDuration(Date time1, Date time2) {
        return dateDuration(time1, time2) + " "
                + timeMsDuration(Math.abs(time1.getTime() - time2.getTime())) + " "
                + TimeZone.getDefault().getDisplayName();
    }

    public static String timeDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        String f = seconds < 0 ? "-" : "";
        long s = Math.abs(seconds);
        if (s < 60) {
            return f + String.format("00:%02d", s);
        }
        long minutes = s / 60;
        s = s % 60;
        if (minutes < 60) {
            return f + String.format("%02d:%02d", minutes, s);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return f + String.format("%02d:%02d:%02d", hours, minutes, s);

    }

    public static String timeMsDuration(long milliseconds) {
        String f = milliseconds < 0 ? "-" : "";
        long ms = Math.abs(milliseconds);
        if (ms < 1000) {
            return f + String.format("00:00:0.%03d", ms);
        }
        long seconds = ms / 1000;
        ms = ms % 1000;
        if (seconds < 60) {
            return f + String.format("00:%02d.%03d", seconds, ms);
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return f + String.format("%02d:%02d.%03d", minutes, seconds, ms);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return f + String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }

    public static Duration duration(Date time1, Date time2) {
        if (time1 == null || time2 == null) {
            return null;
        }
        Instant instant1 = Instant.ofEpochMilli​(time1.getTime());
        Instant instant2 = Instant.ofEpochMilli​(time2.getTime());
        return Duration.between(instant1, instant2);
    }

    public static Period period(Date time1, Date time2) {
        if (time1 == null || time2 == null) {
            return null;
        }
        LocalDate localDate1 = LocalDate.ofEpochDay(time1.getTime() / (24 * 3600000));
        LocalDate localDate2 = LocalDate.ofEpochDay(time2.getTime() / (24 * 3600000));
        return Period.between(localDate1, localDate2);
    }

    public static String duration(Date time1, Date time2, String format) {
        if (time1 == null || time2 == null) {
            return null;
        }
        String dFormat = format;
        if (dFormat == null) {
            dFormat = TimeFormats.Datetime;
        }
        switch (dFormat) {
            case TimeFormats.Date:
                return dateDuration(time1, time2);
            case TimeFormats.Month:
                return yearsMonthsDuration(time1, time2);
            case TimeFormats.Year:
                return yearsDuration(time1, time2);
            case TimeFormats.Time:
                return DateTools.timeDuration(time1, time2);
            case TimeFormats.TimeMs:
                return msDuration(time1, time2);
            case TimeFormats.DatetimeMs:
                return datetimeMsDuration(time1, time2);
            case TimeFormats.DatetimeZone:
                return datetimeZoneDuration(time1, time2);
            case TimeFormats.DatetimeMsZone:
                return datetimeMsZoneDuration(time1, time2);
            default:
                return datetimeDuration(time1, time2);
        }
    }

    public static long randomTime(Random r) {
        if (r == null) {
            r = new Random();
        }
        return r.nextLong(new Date().getTime() * 100);
    }

    public static String randomTimeString(Random r) {
        if (r == null) {
            r = new Random();
        }
        return datetimeToString(randomTime(r));
    }

    public static String randomDateString(Random r) {
        if (r == null) {
            r = new Random();
        }
        return dateToString(new Date(randomTime(r)));
    }

}
