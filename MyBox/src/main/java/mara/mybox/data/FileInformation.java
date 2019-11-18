package mara.mybox.data;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    protected String fileName, data, fileSuffix, handled, fileType;
    protected BooleanProperty selectedProperty;

    public enum FileSelectorType {
        All, ExtensionEuqalAny, ExtensionNotEqualAny,
        NameIncludeAny, NameIncludeAll, NameNotIncludeAny, NameNotIncludeAll,
        NameMatchAnyRegularExpression, NameNotMatchAnyRegularExpression,
        FileSizeLargerThan, FileSizeSmallerThan, ModifiedTimeEarlierThan,
        ModifiedTimeLaterThan
    }

    public FileInformation() {
        this.selectedProperty = new SimpleBooleanProperty(false);
    }

    public FileInformation(File file) {
        setFileAttributes(file);
    }

    private void setFileAttributes(File file) {
        this.file = file;
        this.selectedProperty = new SimpleBooleanProperty(false);
        if (file == null) {
            return;
        }
        this.handled = "";
        this.fileName = file.getAbsolutePath();
        this.data = "";
        this.fileSuffix = "";
        if (!file.exists()) {
            this.fileType = message("NotExist");
            this.fileSuffix = this.fileType;
            return;
        }
        if (file.isFile()) {
            this.filesNumber = 1;
            this.fileSize = file.length();
            this.fileType = message("File");
            this.fileSuffix = FileTools.getFileSuffix(fileName);
            if (this.fileSuffix == null || this.fileSuffix.isEmpty()) {
                this.fileSuffix = message("Unknown");
            }
        } else if (file.isDirectory()) {
//            long[] size = FileTools.countDirectorySize(file);
//            this.filesNumber = size[0];
//            this.fileSize = size[1];
            this.fileType = message("Directory");
            this.fileSuffix = this.fileType;
        } else {
            this.fileSuffix = message("Others");
        }
        this.createTime = FileTools.getFileCreateTime(fileName);
        this.modifyTime = file.lastModified();
    }

    public void countDirectorySize() {
        if (file != null && file.isDirectory()) {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public BooleanProperty getSelectedProperty() {
        if (selectedProperty == null) {
            selectedProperty = new SimpleBooleanProperty(false);
        }
        return selectedProperty;
    }

    public void setSelectedProperty(BooleanProperty selectedProperty) {
        if (selectedProperty == null) {
            this.selectedProperty = new SimpleBooleanProperty(false);
        } else {
            this.selectedProperty = selectedProperty;
        }
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setSelected(boolean selected) {
        selectedProperty.set(selected);
    }

}
