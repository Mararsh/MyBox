package mara.mybox.objects;

import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-23 6:44:22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public final class FileInformation {

    protected static final Logger logger = LogManager.getLogger();

    private SimpleStringProperty fileName, newName, fileType, createTime, modifyTime, fileSize, handled;

    public FileInformation() {

    }

    public FileInformation(File file) {

        String filename = file.getAbsolutePath();
        this.handled = new SimpleStringProperty("");
        this.fileName = new SimpleStringProperty(file.getAbsolutePath());
        this.newName = new SimpleStringProperty("");
        if (file.isFile()) {
            this.fileType = new SimpleStringProperty(FileTools.getFileSuffix(filename));
        } else {
            this.fileType = new SimpleStringProperty("");
        }
        this.createTime = new SimpleStringProperty(DateTools.datetimeToString(FileTools.getFileCreateTime(filename)));
        this.modifyTime = new SimpleStringProperty(DateTools.datetimeToString(file.lastModified()));
        this.fileSize = new SimpleStringProperty(FileTools.showFileSize(file.length()));
    }

    public String getFileName() {
        return fileName.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getNewName() {
        return newName.get();
    }

    public void setNewName(String newName) {
        this.newName.set(newName);
    }

    public String getFileType() {
        return fileType.get();
    }

    public void setFileType(String fileType) {
        this.fileType.set(fileType);
    }

    public String getCreateTime() {
        return createTime.get();
    }

    public void setCreateTime(String createTime) {
        this.createTime.set(createTime);
    }

    public String getModifyTime() {
        return modifyTime.get();
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime.set(modifyTime);
    }

    public String getFileSize() {
        return fileSize.get();
    }

    public void setFileSize(String fileSize) {
        this.fileSize.set(fileSize);
    }

    public String getHandled() {
        return handled.get();
    }

    public void setHandled(String handled) {
        this.handled.set(handled);
    }

}
