package mara.mybox.data;

import java.util.Date;

/**
 * @Author Mara
 * @CreateDate 2020-10-4
 * @License Apache License Version 2.0
 */
public class StringValues {

    protected String key, value;
    protected Date time;

    public StringValues(String key, String value, Date time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
