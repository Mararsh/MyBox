package mara.mybox.db.data;

import java.io.File;
import java.util.Date;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-10-20 11:31:20
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageEditHistory extends BaseData {

    protected long iehid;
    protected String image, historyLocation, updateType, objectType, opType, scopeType, scopeName;
    protected Date operationTime;
    protected Image thumbnail;

    private void init() {
        iehid = -1;
    }

    public ImageEditHistory() {
        init();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            ImageEditHistory his = (ImageEditHistory) super.clone();
            his.setThumbnail(thumbnail);
            his.setOperationTime(new Date());
            his.setIehid(-1);
            return his;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public String getDesc() {
        String s = message(updateType);
        if (objectType != null && !objectType.isEmpty()) {
            s += " " + message(objectType);
        }
        if (opType != null && !opType.isEmpty()) {
            s += " " + message(opType);
        }
        if (scopeType != null && !scopeType.isEmpty()) {
            s += " " + message(scopeType);
        }
        if (scopeName != null && !scopeName.isEmpty()) {
            s += " " + message(scopeName);
        }
        return s;
    }

    public long getSize() {
        File file = new File(historyLocation);
        return file.exists() ? file.length() : 0;
    }

    public String getFileName() {
        File file = new File(historyLocation);
        return file.exists() ? file.getName() : null;
    }

    /*
        static methods
     */
    public static ImageEditHistory create() {
        return new ImageEditHistory();
    }

    public static boolean valid(ImageEditHistory data) {
        return data != null
                && data.getImage() != null && data.getHistoryLocation() != null
                && data.getOperationTime() != null;
    }

    public static Object getValue(ImageEditHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "iehid":
                return data.getIehid();
            case "image_location":
                return data.getImage();
            case "history_location":
                return data.getHistoryLocation();
            case "operation_time":
                return data.getOperationTime();
            case "update_type":
                return data.getUpdateType();
            case "object_type":
                return data.getObjectType();
            case "op_type":
                return data.getOpType();
            case "scope_type":
                return data.getScopeType();
            case "scope_name":
                return data.getScopeName();
        }
        return null;
    }

    public static boolean setValue(ImageEditHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "iehid":
                    data.setIehid(value == null ? -1 : (long) value);
                    return true;
                case "image_location":
                    data.setImage(value == null ? null : (String) value);
                    return true;
                case "history_location":
                    data.setHistoryLocation(value == null ? null : (String) value);
                    return true;
                case "operation_time":
                    data.setOperationTime(value == null ? null : (Date) value);
                    return true;
                case "update_type":
                    data.setUpdateType(value == null ? null : (String) value);
                    return true;
                case "object_type":
                    data.setObjectType(value == null ? null : (String) value);
                    return true;
                case "op_type":
                    data.setOpType(value == null ? null : (String) value);
                    return true;
                case "scope_type":
                    data.setScopeType(value == null ? null : (String) value);
                    return true;
                case "scope_name":
                    data.setScopeName(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    /*
        get/set
     */
    public long getIehid() {
        return iehid;
    }

    public void setIehid(long iehid) {
        this.iehid = iehid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHistoryLocation() {
        return historyLocation;
    }

    public void setHistoryLocation(String historyLocation) {
        this.historyLocation = historyLocation;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

}
