package mara.mybox.tools;

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
import java.util.TimeZone;
import mara.mybox.data.Era;
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

    public static String nowFileString() {
        return datetimeToString(new Date()).replaceAll(":", "-");
    }

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
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.EraDatetimeZh, CommonValues.LocaleEn);
        formatter.setTimeZone(AppVariables.getTimeZone());
        String dateString = formatter.format(new Date());
        return dateString;
    }

    public static boolean isBC(long value) {
        if (value == CommonValues.InvalidLong) {
            return false;
        }
        String s = datetimeToString(new Date(value), CommonValues.EraDatetimeEn, CommonValues.LocaleEn);
        return s.contains("BC");
    }

    public static String textEra(Era era) {
        if (era == null || era.getValue() == CommonValues.InvalidLong) {
            return "";
        }
        return textEra(era.getValue(), era.getFormat(), era.isIgnoreAD());
    }

    public static String textEra(long value) {
        if (value == CommonValues.InvalidLong) {
            return "";
        }
        return textEra(value, Era.Format.Datetime, true);
    }

    public static String textEra(long value, Era.Format format, boolean ignoreAD) {
        if (value == CommonValues.InvalidLong) {
            return "";
        }
        if (format == null) {
            format = Era.Format.Datetime;
        }
        boolean isChinese = AppVariables.isChinese();
        Locale locale = isChinese ? CommonValues.LocaleZhCN : CommonValues.LocaleEn;
        Date d = new Date(value);
        String s = datetimeToString(d, CommonValues.EraDatetimeEn, CommonValues.LocaleEn);
        boolean showEra = s.contains("BC") || !ignoreAD;
        String sformat;
        if (isChinese) {
            switch (format) {
                case Date:
                    sformat = showEra ? CommonValues.EraDateZh : CommonValues.EraDate;
                    break;
                case Year:
                    sformat = showEra ? CommonValues.EraYearZh : CommonValues.EraYear;
                    break;
                case Month:
                    sformat = showEra ? CommonValues.EraMonthZh : CommonValues.EraMonth;
                    break;
                case Time:
                    sformat = CommonValues.EraTime;
                    break;
                case TimeMs:
                    sformat = CommonValues.EraTimeMs;
                    break;
                case DatetimeMs:
                    sformat = showEra ? CommonValues.EraDatetimeMsZh : CommonValues.EraDatetimeMs;
                    break;
                case DatetimeZone:
                    sformat = showEra ? CommonValues.EraDatetimeZh + " Z" : CommonValues.EraDatetime + " Z";
                    break;
                case DatetimeMsZone:
                    sformat = showEra ? CommonValues.EraDatetimeMsZh + " Z" : CommonValues.EraDatetimeMs + " Z";
                    break;
                default:
                    sformat = showEra ? CommonValues.EraDatetimeZh : CommonValues.EraDatetime;
                    break;
            }
        } else {
            switch (format) {
                case Date:
                    sformat = showEra ? CommonValues.EraDateEn : CommonValues.EraDate;
                    break;
                case Year:
                    sformat = showEra ? CommonValues.EraYearEn : CommonValues.EraYear;
                    break;
                case Month:
                    sformat = showEra ? CommonValues.EraMonthEn : CommonValues.EraMonth;
                    break;
                case Time:
                    sformat = CommonValues.EraTime;
                    break;
                case TimeMs:
                    sformat = CommonValues.EraTimeMs;
                    break;
                case DatetimeMs:
                    sformat = showEra ? CommonValues.EraDatetimeMsEn : CommonValues.EraDatetimeMs;
                    break;
                case DatetimeZone:
                    sformat = showEra ? CommonValues.EraDatetimeEn + " Z" : CommonValues.EraDatetime + " Z";
                    break;
                case DatetimeMsZone:
                    sformat = showEra ? CommonValues.EraDatetimeMsEn + " Z" : CommonValues.EraDatetimeMsEn + " Z";
                    break;
                default:
                    if (showEra) {
                        return s;
                    }
                    sformat = CommonValues.EraDatetime;
                    break;
            }
        }
        return DateTools.datetimeToString(d, sformat, locale);
    }

    public static Date encodeEra(String strDate) {
        if (strDate == null) {
            return null;
        }
        String s = strDate.trim().replace("T", " "), format;
        Locale locale;
        if (s.contains(message("zh", "BC")) || s.contains(message("zh", "AD"))) {
            locale = CommonValues.LocaleZhCN;
            if (s.contains(":") && s.contains("-")) {
                if (s.contains(".")) {
                    format = CommonValues.EraDatetimeMsZh;
                } else {
                    format = CommonValues.EraDatetimeZh;
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
                    format = CommonValues.EraYearZh;
                } else {
                    if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                        format = CommonValues.EraMonthZh;
                    } else {
                        format = CommonValues.EraDateZh;
                    }
                }
            } else if (s.contains(":")) {
                if (s.contains(".")) {
                    format = CommonValues.EraTimeMs;
                } else {
                    format = CommonValues.EraTime;
                }
            } else {
                format = CommonValues.EraYearZh;
            }
        } else if (s.contains(message("en", "BC")) || s.contains(message("en", "AD"))) {
            locale = CommonValues.LocaleEn;
            if (s.contains(":") && s.contains("-")) {
                if (s.contains(".")) {
                    format = CommonValues.EraDatetimeMsEn;
                } else {
                    format = CommonValues.EraDatetimeEn;
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
                    format = CommonValues.EraYearEn;
                } else {
                    if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                        format = CommonValues.EraMonthEn;
                    } else {
                        format = CommonValues.EraDateEn;
                    }
                }
            } else if (s.contains(":")) {
                if (s.contains(".")) {
                    format = CommonValues.EraTimeMs;
                } else {
                    format = CommonValues.EraTime;
                }
            } else {
                format = CommonValues.EraYearEn;
            }
        } else {
            locale = AppVariables.isChinese() ? CommonValues.LocaleZhCN : CommonValues.LocaleEn;
            return adToDatetime(s, locale);
        }
        Date d = stringToDatetime(s, format, locale);
//        logger.debug(s + "  " + locale.getLanguage() + " " + format + "  " + locale + "  "
//                + DateTools.textEra(d.getTime(), Era.Format.Datetime, false));
        return d;
    }

    public static Date adToDatetime(String strDate, Locale locale) {
        if (strDate == null) {
            return null;
        }
        String s = strDate.trim().replace("T", " "), format;
        if (s.contains(":") && s.contains("-")) {
            if (s.contains(".")) {
                format = CommonValues.DatetimeMs;
            } else {
                format = CommonValues.DatetimeFormat;
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
                format = CommonValues.YearFormat;
            } else {
                if (ss.indexOf("-") == ss.lastIndexOf("-")) {
                    format = CommonValues.MonthFormat;
                } else {
                    format = CommonValues.DateFormat;
                }
            }
        } else if (s.contains(":")) {
            if (s.contains(".")) {
                format = CommonValues.TimeMs;
            } else {
                format = CommonValues.TimeFormat;
            }
        } else {
            format = CommonValues.YearFormat;
        }
//        logger.debug(s + "  " + format + "  " + locale + "  "
//                + stringToDatetime(s, format, locale).getTime());
        return stringToDatetime(s, format, locale);
    }

    public static String datetimeToString(long dvalue) {
        if (dvalue == CommonValues.InvalidLong) {
            return null;
        }
        return datetimeToString(new Date(dvalue));
    }

    public static String datetimeToString(Date theDate) {
        return datetimeToString(theDate, AppVariables.getTimeZone());
    }

    public static String datetimeToString(Date theDate, TimeZone theZone) {
        return datetimeToString(theDate, CommonValues.DatetimeFormat, theZone);
    }

    public static String datetimeToString(long theDate, String format) {
        if (theDate == CommonValues.InvalidLong) {
            return null;
        }
        return datetimeToString(new Date(theDate), format);
    }

    public static String datetimeToString(Date theDate, String format) {
        return datetimeToString(theDate, format, Locale.getDefault());
    }

    public static String datetimeToString(Date theDate, String format, TimeZone theZone) {
        if (theDate == null || theZone == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(theZone);
        String dateString = formatter.format(theDate);
        return dateString;
    }

    public static String datetimeToString(Date theDate, String format, Locale locale) {
        if (theDate == null || locale == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
        String dateString = formatter.format(theDate);
        return dateString;
    }

    public static String datetime5ToString(Date theDate) {
        if (theDate == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.EraDatetimeZh, CommonValues.LocaleEn);
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
            SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.EraDatetimeZh, Locale.getDefault());
//            formatter.setTimeZone(theZone);
            ParsePosition pos = new ParsePosition(0);
            return formatter.parse(strDate, pos);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDatetime(String strDate, String format) {
        return stringToDatetime(strDate, format, Locale.getDefault());
    }

    public static Date stringToDatetime(String strDate, String format, Locale locale) {
        if (strDate == null || strDate.isEmpty()
                || format == null || format.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
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

    public static String duration(Date time1, Date time2, Era.Format format) {
        if (time1 == null || time2 == null) {
            return null;
        }
        Era.Format dFormat = format;
        if (format == null) {
            dFormat = Era.Format.Datetime;
        }
        switch (dFormat) {
            case Date:
                return dateDuration(time1, time2);
            case Month:
                return yearsMonthsDuration(time1, time2);
            case Year:
                return yearsDuration(time1, time2);
            case Time:
                return DateTools.timeDuration(time1, time2);
            case TimeMs:
                return msDuration(time1, time2);
            case DatetimeMs:
                return datetimeMsDuration(time1, time2);
            case DatetimeZone:
                return datetimeZoneDuration(time1, time2);
            case DatetimeMsZone:
                return datetimeMsZoneDuration(time1, time2);
            default:
                return datetimeDuration(time1, time2);
        }
    }

}
