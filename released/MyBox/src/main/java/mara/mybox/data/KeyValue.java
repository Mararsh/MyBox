package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-7-30
 * @License Apache License Version 2.0
 */
public class KeyValue {

    protected String key;
    protected String value;

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /*
        get/set
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
