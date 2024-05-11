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

    /*
        parse to date
     */
    // strDate does not contain ear code(bc/ad)
    public static String parseFormat(String strDate) {
        try {
            if (strDate == null || strDate.isBlank()) {
                return null;
            }
            String fullString = strDate.trim().replace("T", " ");
            String parsed = fullString, format;
            if (parsed.startsWith("-")) {
                parsed = fullString.substring(1);
            }
            int len = parsed.length();
            if (len <= 4) {
                format = TimeFormats.YearA;
            } else if (parsed.contains("/")) {
                if (parsed.charAt(4) == '/') {
                    if (parsed.contains(":")) {
                        if (parsed.contains(".")) {
                            format = TimeFormats.DatetimeMsC;
                        } else {
                            format = TimeFormats.DatetimeC;
                        }
                    } else if (parsed.indexOf("/") == parsed.lastIndexOf("/")) {
                        format = TimeFormats.MonthC;
                    } else {
                        format = TimeFormats.DateC;
                    }
                } else {
                    if (parsed.contains(":")) {
                        if (parsed.contains(".")) {
                            format = TimeFormats.DatetimeMsB;
                        } else {
                            format = TimeFormats.DatetimeB;
                        }
                    } else if (parsed.indexOf("/") == parsed.lastIndexOf("/")) {
                        format = TimeFormats.MonthB;
                    } else {
                        format = TimeFormats.DateB;
                    }
                }

            } else if (parsed.contains("-")) {
                if (parsed.contains(":")) {
                    if (parsed.contains(".")) {
                        format = TimeFormats.DatetimeMsA;
                    } else {
                        format = TimeFormats.DatetimeA;
                    }
                } else if (parsed.indexOf("-") == parsed.lastIndexOf("-")) {
                    format = TimeFormats.MonthA;
                } else {
                    format = TimeFormats.DateA;
                }
            } else if (parsed.contains(":")) {
                if (parsed.contains(".")) {
                    format = TimeFormats.TimeMs;
                } else {
                    format = TimeFormats.Time;
                }
            } else {
                format = TimeFormats.YearA;
            }
            if (parsed.contains("+")) {
                format += " Z";
            }
            return format;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date encodeDate(String strDate, int century) {
        if (strDate == null) {
            return null;
        }
        try {
            long lv = Long.parseLong(strDate);
            if (lv >= 10000 || lv <= -10000) {
                return new Date(lv);
            }
        } catch (Exception e) {
        }
        String s = strDate.trim().replace("T", " "), format;
        Locale locale;
        int len = s.length();
        if (s.startsWith(message("zh", "BC") + " ")) {
            locale = Languages.LocaleZhCN;
            format = "G " + parseFormat(s.substring((message("zh", "BC") + " ").length(), len));

        } else if (s.startsWith(message("zh", "BC"))) {
            locale = Languages.LocaleZhCN;
            format = "G" + parseFormat(s.substring((message("zh", "BC")).length(), len));

        } else if (s.startsWith(message("zh", "AD") + " ")) {
            locale = Languages.LocaleZhCN;
            format = "G " + parseFormat(s.substring((message("zh", "AD") + " ").length(), len));

        } else if (s.startsWith(message("zh", "AD"))) {
            locale = Languages.LocaleZhCN;
            format = "G" + parseFormat(s.substring((message("zh", "AD")).length(), len));

        } else if (s.startsWith(message("en", "BC") + " ")) {
            locale = Languages.LocaleEn;
            format = "G " + parseFormat(s.substring((message("en", "BC") + " ").length(), len));

        } else if (s.startsWith(message("en", "BC"))) {
            locale = Languages.LocaleEn;
            format = "G" + parseFormat(s.substring((message("en", "BC")).length(), len));

        } else if (s.startsWith(message("en", "AD") + " ")) {
            locale = Languages.LocaleEn;
            format = "G " + parseFormat(s.substring((message("en", "AD") + " ").length(), len));

        } else if (s.startsWith(message("en", "AD"))) {
            locale = Languages.LocaleEn;
            format = "G" + parseFormat(s.substring((message("en", "AD")).length(), len));

        } else if (s.endsWith(" " + message("zh", "BC"))) {
            locale = Languages.LocaleZhCN;
            format = parseFormat(s.substring(0, len - (" " + message("zh", "BC")).length())) + " G";

        } else if (s.endsWith(message("zh", "BC"))) {
            locale = Languages.LocaleZhCN;
            format = parseFormat(s.substring(0, len - message("zh", "BC").length())) + "G";

        } else if (s.endsWith(" " + message("zh", "AD"))) {
            locale = Languages.LocaleZhCN;
            format = parseFormat(s.substring(0, len - (" " + message("zh", "AD")).length())) + " G";

        } else if (s.endsWith(message("zh", "AD"))) {
            locale = Languages.LocaleZhCN;
            format = parseFormat(s.substring(0, len - message("zh", "AD").length())) + "G";

        } else if (s.endsWith(" " + message("en", "BC"))) {
            locale = Languages.LocaleEn;
            format = parseFormat(s.substring(0, len - (" " + message("en", "BC")).length())) + " G";

        } else if (s.endsWith(message("en", "BC"))) {
            locale = Languages.LocaleEn;
            format = parseFormat(s.substring(0, len - message("en", "BC").length())) + "G";

        } else if (s.endsWith(" " + message("en", "AD"))) {
            locale = Languages.LocaleEn;
            format = parseFormat(s.substring(0, len - (" " + message("en", "AD")).length())) + " G";

        } else if (s.endsWith(message("en", "AD"))) {
            locale = Languages.LocaleEn;
            format = parseFormat(s.substring(0, len - message("en", "AD").length())) + "G";

        } else {
            locale = Languages.locale();
            format = parseFormat(s);
        }
        Date d = encodeDate(s, format, locale, century);
        return d;
    }

    public static Date encodeDate(String strDate) {
        return encodeDate(strDate, 0);
    }

    public static Date encodeDate(String strDate, Locale locale, int century) {
        return encodeDate(strDate, parseFormat(strDate), locale, century);
    }

    public static Date encodeDate(String strDate, String format) {
        return encodeDate(strDate, format, Locale.getDefault(), 0);
    }

    public static Date encodeDate(String strDate, String format, int century) {
        return encodeDate(strDate, format, Locale.getDefault(), century);
    }

    // century.  0: not fix      -1:fix as default   others:fix as value
    public static Date encodeDate(String strDate, String format, Locale locale, int century) {
        if (strDate == null || strDate.isEmpty()
                || format == null || format.isEmpty()) {
            return null;
        }
//        MyBoxLog.debug(strDate + "  " + format + "  " + locale + "  " + century);
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
            if (century >= 0) {
                formatter.set2DigitYearStart(new SimpleDateFormat("yyyy")
                        .parse(century > 0 ? century + "" : "0000"));
            }
            return formatter.parse(strDate, new ParsePosition(0));
        } catch (Exception e) {
            try {
                return new Date(Long.parseLong(strDate));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static Date thisMonth() {
        return DateTools.encodeDate(nowString().substring(0, 7), "yyyy-MM");
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

    public static LocalDate stringToLocalDate(String strDate) {
        return dateToLocalDate(encodeDate(strDate));
    }


    /*
        return string
     */
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

    public static String nowDate() {
        return datetimeToString(new Date(), TimeFormats.Date);
    }

    public static String textEra(Era era) {
        if (era == null || era.getValue() == AppValues.InvalidLong) {
            return "";
        }
        return textEra(era.getValue(), era.getFormat());
    }

    public static String textEra(String value) {
        return textEra(encodeDate(value));
    }

    public static String textEra(Date value) {
        return value == null ? null : textEra(value.getTime());
    }

    public static String textEra(long value) {
        if (value == AppValues.InvalidLong) {
            return "";
        }
        return textEra(value, null);
    }

    public static String textEra(long value, String format) {
        if (value == AppValues.InvalidLong) {
            return "";
        }
        return datetimeToString(new Date(value), format, Languages.locale(), null);
    }

    public static String localDateToString(LocalDate localDate) {
        return dateToString(localDateToDate(localDate));
    }

    public static String datetimeToString(long dvalue) {
        if (dvalue == AppValues.InvalidLong) {
            return null;
        }
        return datetimeToString(new Date(dvalue));
    }

    public static String datetimeToString(Date theDate) {
        return datetimeToString(theDate, null, null, null);
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
            format = isBC(theDate.getTime()) ? bcFormat() : TimeFormats.Datetime;
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

    public static String dateToString(Date theDate) {
        return datetimeToString(theDate, TimeFormats.Date);
    }

    public static String dateToMonthString(Date theDate) {
        return datetimeToString(theDate).substring(0, 7);
    }

    public static String dateToYearString(Date theDate) {
        return datetimeToString(theDate).substring(0, 4);
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

    public static String randomDateString(Random r, String format) {
        if (r == null) {
            r = new Random();
        }
        return datetimeToString(randomTime(r), format);
    }

    /*
        others
     */
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

    public static long zeroYear() {
        Calendar ca = Calendar.getInstance();
        ca.set(0, 0, 1, 0, 0, 0);
        return ca.getTime().getTime();
    }

    public static long randomTime(Random r) {
        if (r == null) {
            r = new Random();
        }
        return r.nextLong(new Date().getTime() * 100);
    }

    public static String bcFormat() {
        return TimeFormats.Datetime + " G";
    }

    public static boolean isBC(long value) {
        if (value == AppValues.InvalidLong) {
            return false;
        }
        String s = datetimeToString(new Date(value), bcFormat(), Languages.LocaleEn, null);
        return s.contains("BC");
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

}
