package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2019-2-10 12:47:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class GenericValue<T> {

    private String type, name;
    private T value;
    private T percentage;

    public GenericValue() {

    }

    public GenericValue(T value) {
        this.type = null;
        this.name = null;
        this.value = value;
    }

    public GenericValue(String name, T value) {
        this.type = null;
        this.name = name;
        this.value = value;
    }

    public GenericValue(String type, String name, T value) {
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

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getPercentage() {
        return percentage;
    }

    public void setPercentage(T percentage) {
        this.percentage = percentage;
    }

}
