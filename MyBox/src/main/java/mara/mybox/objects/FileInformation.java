package mara.mybox.objects;

import java.io.File;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-23 6:44:22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public final class FileInformation {

    private String fileName, fileType, createTime, modifyTime, fileSize;

    public FileInformation(File file) {

        String filename = file.getAbsolutePath();
        this.fileName = file.getAbsolutePath();
        this.fileType = FileTools.getFileSuffix(filename);
        this.createTime = DateTools.datetimeToString(FileTools.getFileCreateTime(filename));
        this.modifyTime = DateTools.datetimeToString(file.lastModified());
        this.fileSize = FileTools.showFileSize(file.length());
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

}
