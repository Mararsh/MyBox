package mara.mybox.data;

import java.util.Date;

/**
 * @Author Mara
 * @CreateDate 2018-10-20 11:31:20
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageHistory {

    private String image, history_location;
    private int update_type;
    private Date operation_time;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHistory_location() {
        return history_location;
    }

    public void setHistory_location(String history_location) {
        this.history_location = history_location;
    }

    public int getUpdate_type() {
        return update_type;
    }

    public void setUpdate_type(int update_type) {
        this.update_type = update_type;
    }

    public Date getOperation_time() {
        return operation_time;
    }

    public void setOperation_time(Date operation_time) {
        this.operation_time = operation_time;
    }

}
