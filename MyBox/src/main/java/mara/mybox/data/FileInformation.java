package mara.mybox.data;

import java.io.File;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-23 6:44:22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileInformation {

    protected File file;
    protected long fileSize = 0, createTime, modifyTime, filesNumber = 1;
    protected String fileName, newName, fileSuffix, handled, fileType;
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
        this.handled = "";
        this.fileName = file.getAbsolutePath();
        this.newName = "";
        this.fileSuffix = "";
        if (!file.exists()) {
            this.fileType = message("NotExist");
            return;
        }
        if (file.isFile()) {
            this.fileSuffix = FileTools.getFileSuffix(fileName);
            this.fileType = message("File");
        } else if (file.isDirectory()) {
            this.fileType = message("Directory");
        }
        this.createTime = FileTools.getFileCreateTime(fileName);
        this.modifyTime = file.lastModified();
        if (file.isFile()) {
            this.filesNumber = 1;
            this.fileSize = file.length();
        } else if (file.isDirectory()) {
            long[] size = FileTools.countDirectorySize(file);
            this.filesNumber = size[0];
            this.fileSize = size[1];
        }
    }


    /*
        get/set
     */
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getHandled() {
        return handled;
    }

    public void setHandled(String handled) {
        this.handled = handled;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        setFileAttributes(file);
    }

    public long getFilesNumber() {
        return filesNumber;
    }

    public void setFilesNumber(long filesNumber) {
        this.filesNumber = filesNumber;
    }

}
