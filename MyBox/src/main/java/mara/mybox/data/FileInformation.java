package mara.mybox.data;

import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-23 6:44:22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileInformation {

    protected File file;
    protected long fileSize, createTime, modifyTime;
    protected SimpleStringProperty fileName, newName, fileSuffix, handled;
    protected SimpleBooleanProperty isFile;
    protected final int IO_BUF_LENGTH = 4096;

    public FileInformation() {

    }

    public FileInformation(File file) {
        setFileAttributes(file);
    }

    private void setFileAttributes(File file) {
        this.file = file;
        if (file == null) {
            return;
        }
        String filename = file.getAbsolutePath();
        this.handled = new SimpleStringProperty("");
        this.fileName = new SimpleStringProperty(file.getAbsolutePath());
        this.isFile = new SimpleBooleanProperty(file.isFile());
        this.newName = new SimpleStringProperty("");
        if (file.isFile()) {
            this.fileSuffix = new SimpleStringProperty(FileTools.getFileSuffix(filename));
        } else {
            this.fileSuffix = new SimpleStringProperty("");
        }
        this.createTime = FileTools.getFileCreateTime(filename);
        this.modifyTime = file.lastModified();
        if (file.isFile()) {
            this.fileSize = file.length();
        } else {
            this.fileSize = 0;
        }
    }

    public String getFileName() {
        return fileName.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public boolean getIsFile() {
        return isFile.get();
    }

    public void setIsFile(boolean isFile) {
        this.isFile.set(isFile);
    }

    public String getNewName() {
        return newName.get();
    }

    public void setNewName(String newName) {
        this.newName.set(newName);
    }

    public String getFileType() {
        return fileSuffix.get();
    }

    public void setFileType(String fileType) {
        this.fileSuffix.set(fileType);
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getHandled() {
        return handled.get();
    }

    public void setHandled(String handled) {
        this.handled.set(handled);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        setFileAttributes(file);
    }

}
