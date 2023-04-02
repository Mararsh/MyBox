package mara.mybox.value;

import java.util.TimeZone;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class TimeFormats {

    public static final TimeZone zoneUTC = TimeZone.getTimeZone("GMT+0"); // UTC
    public static final TimeZone zoneZhCN = TimeZone.getTimeZone("GMT+8"); // Beijing zone, UTC+0800

    public static final String Datetime = "yyyy-MM-dd HH:mm:ss";
    public static final String Date = "yyyy-MM-dd";
    public static final String Month = "yyyy-MM";
    public static final String Year = "yyyy";
    public static final String Time = "HH:mm:ss";
    public static final String TimeMs = "HH:mm:ss.SSS";
    public static final String DatetimeMs = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DatetimeZone = "yyyy-MM-dd HH:mm:ss Z";
    public static final String DatetimeMsZone = "yyyy-MM-dd HH:mm:ss.SSS Z";

    public static final String Datetime2 = "yyyy-MM-dd_HH-mm-ss-SSS";
    public static final String Datetime3 = "yyyyMMddHHmmssSSS";
    public static final String Datetime4 = "yyyy-MM-dd_HH-mm-ss";
    public static final String DatetimeFormat5 = "yyyy.MM.dd HH:mm:ss";

    public static final String DatetimeE = "MM/dd/yyyy HH:mm:ss";
    public static final String DateE = "MM/dd/yyyy";
    public static final String MonthE = "MM/yyyy";
    public static final String DatetimeMsE = "MM/dd/yyyy HH:mm:ss.SSS";
    public static final String DatetimeZoneE = "MM/dd/yyyy HH:mm:ss Z";

    public static final String DatetimeA = "y-MM-dd HH:mm:ss";
    public static final String DatetimeMsA = "y-MM-dd HH:mm:ss.SSS";
    public static final String DateA = "y-MM-dd";
    public static final String MonthA = "y-MM";
    public static final String YearA = "y";
    public static final String TimeA = "HH:mm:ss";
    public static final String TimeMsA = "HH:mm:ss.SSS";

    public static final String DatetimeB = "MM/dd/y HH:mm:ss";
    public static final String DatetimeMsB = "MM/dd/y HH:mm:ss.SSS";
    public static final String DateB = "MM/dd/y";
    public static final String MonthB = "MM/y";

    public static final String DateC = "yyyy/MM/dd";

}
