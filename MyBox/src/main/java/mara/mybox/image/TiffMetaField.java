/*
 * Apache License Version 2.0
 */
package mara.mybox.image;

import java.util.List;

/**
 *
 * @author mara
 */
public class TiffMetaField {

    private String tagNumber, name, type, description;
    private List<String> values;

    public static TiffMetaField create() {
        return new TiffMetaField();
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
