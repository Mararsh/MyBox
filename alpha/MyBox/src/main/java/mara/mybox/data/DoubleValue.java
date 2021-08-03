package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2019-2-10 12:47:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleValue {

    private String type, name;
    private double value;
    private double percentage;

    public DoubleValue() {

    }

    public DoubleValue(double value) {
        this.type = null;
        this.name = null;
        this.value = value;
    }

    public DoubleValue(String name, double value) {
        this.type = null;
        this.name = name;
        this.value = value;
    }

    public DoubleValue(String type, String name, double value) {
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

}
