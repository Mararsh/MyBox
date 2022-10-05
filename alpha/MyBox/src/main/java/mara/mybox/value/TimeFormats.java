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

    public static final String EraDatetimeEnd = "y-M-d H:m:s G";
    public static final String EraDatetimeMsEnd = "y-M-d H:m:s.S G";
    public static final String EraDateEnd = "y-M-d G";
    public static final String EraMonthEnd = "y-M G";
    public static final String EraYearEnd = "y G";

    public static final String EraDatetimeStart = "Gy-M-d H:m:s";
    public static final String EraDatetimeMsStart = "Gy-M-d H:m:s.S";
    public static final String EraDateStart = "Gy-M-d";
    public static final String EraMonthStart = "Gy-M";
    public static final String EraYearStart = "Gy";

    public static final String EraDatetimeEndE = "M/d/y H:m:s G";
    public static final String EraDatetimeMsEndE = "M/d/y H:m:s.S G";
    public static final String EraDateEndE = "M/d/y G";
    public static final String EraMonthEndE = "M/d G";

    public static final String EraDatetimeStartE = "GM/d/y H:m:s";
    public static final String EraDatetimeMsStartE = "GM/d/y H:m:s.S";
    public static final String EraDateStartE = "GM/d/y";
    public static final String EraMonthStartE = "GM/y";

    public static final String DatetimeA = "y-M-d H:m:s";
    public static final String DatetimeMsA = "y-M-d H:m:s.S";
    public static final String DateA = "y-M-d";
    public static final String MonthA = "y-M";
    public static final String YearA = "y";
    public static final String TimeA = "H:m:s";
    public static final String TimeMsA = "H:m:s.S";

}
