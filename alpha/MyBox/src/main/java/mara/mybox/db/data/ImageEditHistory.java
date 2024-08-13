package mara.mybox.db.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-10-20 11:31:20
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageEditHistory extends BaseData {

    public static final int Default_Max_Histories = 20;

    protected long iehid;
    protected File imageFile, historyFile, thumbnailFile;
    protected String updateType, objectType, opType, scopeType, scopeName;
    protected Date operationTime;
    protected Image thumbnail;

    private void init() {
        iehid = -1;
        operationTime = new Date();
    }

    public ImageEditHistory() {
        init();
    }

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    public String getType() {
        if (opType != null && !opType.isEmpty()) {
            return message(opType);
        } else if (objectType != null && !objectType.isEmpty()) {
            return message(objectType);
        } else if (updateType != null && !updateType.isEmpty()) {
            return message(updateType);
        } else if (scopeType != null && !scopeType.isEmpty()) {
            return message(scopeType);
        } else if (scopeName != null && !scopeName.isEmpty()) {
            return message(scopeName);
        }
        return null;
    }

    public String getDesc() {
        String s;
        if (objectType != null && !objectType.isEmpty()) {
            s = objectType + " ";
        } else if (opType != null && !opType.isEmpty()) {
            s = opType + " ";
        } else if (updateType != null && !updateType.isEmpty()) {
            s = updateType + " ";
        } else {
            s = " ";
        }
        if (scopeType != null && !scopeType.isEmpty()) {
            s += message(scopeType);
        }
        if (scopeName != null && !scopeName.isEmpty()) {
            s += " " + message(scopeName);
        }
        return s;
    }

    public long getSize() {
        return historyFile != null && historyFile.exists() ? historyFile.length() : 0;
    }

    public String getFileName() {
        return historyFile != null && historyFile.exists() ? historyFile.getName() : null;
    }

    public Image historyImage(FxTask task) {
        try {
            if (!historyFile.exists()) {
                return null;
            }
            BufferedImage bufferedImage = ImageFileReaders.readImage(task, historyFile);
            if (bufferedImage != null) {
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageEditHistory create() {
        return new ImageEditHistory();
    }

    public static boolean valid(ImageEditHistory data) {
        return data != null
                && data.getImageFile() != null && data.getHistoryFile() != null
                && data.getImageFile().exists() && data.getHistoryFile().exists()
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
                File imageFile = data.getImageFile();
                return imageFile == null ? null : imageFile.getAbsolutePath();
            case "history_location":
                File hisFile = data.getHistoryFile();
                return hisFile == null ? null : hisFile.getAbsolutePath();
            case "thumbnail_file":
                File thumbFile = data.getThumbnailFile();
                return thumbFile == null ? null : thumbFile.getAbsolutePath();
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
                    data.setImageFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setImageFile(f);
                        }
                    }
                    return true;
                case "history_location":
                    data.setHistoryFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setHistoryFile(f);
                        }
                    }
                    return true;
                case "thumbnail_file":
                    data.setThumbnailFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setThumbnailFile(f);
                        }
                    }
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
            MyBoxLog.debug(e);
        }
        return false;
    }

    /*
        get/set
     */
    public long getIehid() {
        return iehid;
    }

    public ImageEditHistory setIehid(long iehid) {
        this.iehid = iehid;
        return this;
    }

    public File getImageFile() {
        return imageFile;
    }

    public ImageEditHistory setImageFile(File imageFile) {
        this.imageFile = imageFile;
        return this;
    }

    public File getHistoryFile() {
        return historyFile;
    }

    public ImageEditHistory setHistoryFile(File historyFile) {
        this.historyFile = historyFile;
        return this;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public ImageEditHistory setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
        return this;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public ImageEditHistory setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public String getUpdateType() {
        return updateType;
    }

    public ImageEditHistory setUpdateType(String updateType) {
        this.updateType = updateType;
        return this;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public ImageEditHistory setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
        return this;
    }

    public String getObjectType() {
        return objectType;
    }

    public ImageEditHistory setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    public String getOpType() {
        return opType;
    }

    public ImageEditHistory setOpType(String opType) {
        this.opType = opType;
        return this;
    }

    public String getScopeType() {
        return scopeType;
    }

    public ImageEditHistory setScopeType(String scopeType) {
        this.scopeType = scopeType;
        return this;
    }

    public String getScopeName() {
        return scopeName;
    }

    public ImageEditHistory setScopeName(String scopeName) {
        this.scopeName = scopeName;
        return this;
    }

}
