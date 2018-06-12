package mara.mybox.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 9:44:53
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DateTools {

    public static String datetimeToString(long dvalue) {
        return datetimeToString(new Date(dvalue));
    }

    public static String datetimeToString(Date theDate) {
        return datetimeToString(theDate, CommonValues.zoneZhCN);
    }

    public static String datetimeToString(Date theDate, TimeZone theZone) {
        if (theDate == null || theZone == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(CommonValues.DatetimeFormat);
        formatter.setTimeZone(theZone);
        String dateString = formatter.format(theDate);
        return dateString;
    }

}
