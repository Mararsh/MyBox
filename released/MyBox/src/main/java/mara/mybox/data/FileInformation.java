package mara.mybox.data;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import mara.mybox.tools.FileNameTools;
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
    protected long tableIndex, fileSize = -1, createTime, modifyTime, filesNumber = 0;
    protected String data, handled;
    protected FileType fileType;
    protected final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    protected boolean selected;
    protected long sizeWithSubdir = -1, sizeWithoutSubdir = -1, filesWithSubdir = -1, filesWithoutSubdir = -1;
    protected long duration;  // milliseconds

    public enum FileType {
        File, Directory, Root, Digest, NotExist
    }

    public enum FileSelectorType {
        All, ExtensionEuqalAny, ExtensionNotEqualAny,
        NameIncludeAny, NameIncludeAll, NameNotIncludeAny, NameNotIncludeAll,
        NameMatchRegularExpression, NameNotMatchRegularExpression,
        NameIncludeRegularExpression, NameNotIncludeRegularExpression,
        FileSizeLargerThan, FileSizeSmallerThan, ModifiedTimeEarlierThan,
        ModifiedTimeLaterThan
    }

    public FileInformation() {
        init();
    }

    public final void init() {
        file = null;
        filesNumber = tableIndex = fileSize = createTime = modifyTime = -1;
        fileType = FileType.NotExist;
        handled = null;
        data = null;
        selectedProperty.set(false);
        sizeWithSubdir = sizeWithoutSubdir = filesWithSubdir = filesWithoutSubdir = -1;
        duration = 3000;
    }

    public FileInformation(File file) {
        setFileAttributes(file);
    }

    public final void setFileAttributes(File file) {
        init();
        this.file = file;
        if (duration < 0) {
            duration = 3000;
        }
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            this.fileType = FileType.NotExist;
            return;
        }
        if (file.isFile()) {
            filesNumber = 1;
            fileSize = file.length();
            fileType = FileType.File;
        } else if (file.isDirectory()) {
//            long[] size = FileTools.countDirectorySize(file);
//            this.filesNumber = size[0];
//            this.fileSize = size[1];
            sizeWithSubdir = sizeWithoutSubdir = -1;
            filesWithSubdir = filesWithoutSubdir = -1;
            fileType = FileType.Directory;
        }
        this.createTime = FileTools.createTime(file);
        this.modifyTime = file.lastModified();
    }

    public void setDirectorySize(boolean countSubdir) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        if (countSubdir) {
            fileSize = sizeWithSubdir;
            filesNumber = filesWithSubdir;
        } else {
            fileSize = sizeWithoutSubdir;
            filesNumber = filesWithoutSubdir;
        }
    }

    public void countDirectorySize(Task task, boolean countSubdir) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        if (countSubdir) {
            if (sizeWithSubdir < 0 || filesWithSubdir < 0) {
                long[] size = FileTools.countDirectorySize(file, countSubdir);
                if (task == null || task.isCancelled()) {
                    return;
                }
                filesWithSubdir = size[0];
                sizeWithSubdir = size[1];
            }
            fileSize = sizeWithSubdir;
            filesNumber = filesWithSubdir;
        } else {
            if (sizeWithoutSubdir < 0 || filesWithoutSubdir < 0) {
                long[] size = FileTools.countDirectorySize(file, countSubdir);
                if (task == null || task.isCancelled()) {
                    return;
                }
                filesWithoutSubdir = size[0];
                sizeWithoutSubdir = size[1];
            }
            fileSize = sizeWithoutSubdir;
            filesNumber = filesWithoutSubdir;
        }
    }

    /*
        custmized get/set
     */
    public void setFile(File file) {
        setFileAttributes(file);
    }

    public String getSuffix() {
        if (file != null) {
            return FileNameTools.suffix(file.getName());
        } else if (data != null) {
            return FileNameTools.suffix(data);
        } else {
            return null;
        }
    }

    public String getFileName() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return data;
        }
    }

    /*
        get/set
     */
    public File getFile() {
        return file;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHandled() {
        return handled;
    }

    public void setHandled(String handled) {
        this.handled = handled;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
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

    public long getFilesNumber() {
        return filesNumber;
    }

    public void setFilesNumber(long filesNumber) {
        this.filesNumber = filesNumber;
    }

    public BooleanProperty getSelectedProperty() {
        return selectedProperty;
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setSelected(boolean selected) {
        selectedProperty.set(selected);
    }

    public long getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(long tableIndex) {
        this.tableIndex = tableIndex;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
