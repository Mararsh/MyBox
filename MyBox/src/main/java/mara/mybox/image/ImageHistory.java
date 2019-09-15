package mara.mybox.image;

import java.util.Date;
import javafx.scene.image.Image;

/**
 * @Author Mara
 * @CreateDate 2018-10-20 11:31:20
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageHistory {

    private String image, historyLocation, updateType, objectType, opType, scopeType, scopeName;
    private Date operationTime;
    private Image thumbnail;

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
