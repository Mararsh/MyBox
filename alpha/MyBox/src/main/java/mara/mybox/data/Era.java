package mara.mybox.data;

import java.util.Date;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.TimeFormats;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
/*
   "1970-01-01 08:00:00 AD" = 0
   "0 AD" = "1 BC" = "0" = "-0" = "0000" = "-0000" = "0001-01-01 00:00:00 BC" = -62167420800000
   "1 AD" = "1" = "0001" = "0001-01-01 00:00:00" = "0001-01-01 00:00:00 AD" = -62135798400000
   "202 BC" = "-203" = "-0203" = "-0203-01-01 00:00:00" = "0202-01-01 00:00:00 BC" = -68510476800000
   "202 AD" = "202" = "0202" = "0202-01-01 00:00:00" = "0202-01-01 00:00:00 AD" = -55792742400000
 */
public class Era {

    protected long value = AppValues.InvalidLong;
    protected String format = TimeFormats.Datetime;
    protected boolean ignoreAD = true;

    public Era(long value) {
        this.value = value;
    }

    public Era(long value, String format, boolean ignoreAD) {
        this.value = value;
        this.format = format;
        this.ignoreAD = ignoreAD;
    }

    public Era(String s) {
        Date d = DateTools.encodeDate(s);
        if (d != null) {
            value = d.getTime();
        }
    }

    public String text() {
        if (value == AppValues.InvalidLong) {
            return null;
        }
        return DateTools.textEra(value, format);
    }

    /*
        get/set
     */
    public long getValue() {
        return value;
    }

    public Era setValue(long value) {
        this.value = value;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public Era setFormat(String format) {
        this.format = format;
        return this;
    }

    public boolean isIgnoreAD() {
        return ignoreAD;
    }

    public Era setIgnoreAD(boolean ignoreAD) {
        this.ignoreAD = ignoreAD;
        return this;
    }

}
