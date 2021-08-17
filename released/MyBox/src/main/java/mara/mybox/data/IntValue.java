package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2019-2-10 12:47:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntValue {

    private String type, name;
    private int value;
    private float percentage;

    public IntValue() {

    }

    public IntValue(int value) {
        this.type = null;
        this.name = null;
        this.value = value;
    }

    public IntValue(String name, int value) {
        this.type = null;
        this.name = name;
        this.value = value;
    }

    public IntValue(String type, String name, int value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

}
