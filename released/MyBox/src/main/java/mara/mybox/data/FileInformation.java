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
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);
    protected long sizeWithSubdir = -1, sizeWithoutSubdir = -1, filesWithSubdir = -1, filesWithoutSubdir = -1;
    protected long duration;  // milliseconds

    public enum FileType {
        File, Directory, Link, Socket, Block, Character, FIFO, Root, Digest, NotExist
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
        selected.set(false);
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

    public void countDirectorySize(Task task, boolean countSubdir, boolean reset) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        if (countSubdir) {
            if (reset || sizeWithSubdir < 0 || filesWithSubdir < 0) {
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
            if (reset || sizeWithoutSubdir < 0 || filesWithoutSubdir < 0) {
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

    public String getHierarchyNumber() {
        return hierarchyNumber(file);
    }

    public static String hierarchyNumber(File f) {
        if (f == null) {
            return "";
        }
        File parent = f.getParentFile();
        if (parent == null) {
            return "";
        }
        String p = hierarchyNumber(parent);
        String[] children = parent.list();
        p = p == null || p.isBlank() ? "" : p + ".";
        String name = f.getName();
        for (int i = 0; i < children.length; i++) {
            String c = children[i];
            if (name.equals(c)) {
                return p + (i + 1);
            }
        }
        return null;
    }

    public static FileInformation clone(FileInformation sourceInfo, FileInformation targetInfo) {
        if (sourceInfo == null || targetInfo == null) {
            return null;
        }
        targetInfo.file = sourceInfo.file;
        targetInfo.tableIndex = sourceInfo.tableIndex;
        targetInfo.fileSize = sourceInfo.fileSize;
        targetInfo.createTime = sourceInfo.createTime;
        targetInfo.modifyTime = sourceInfo.modifyTime;
        targetInfo.filesNumber = sourceInfo.filesNumber;
        targetInfo.data = sourceInfo.data;
        targetInfo.handled = sourceInfo.handled;
        targetInfo.fileType = sourceInfo.fileType;
        targetInfo.selected.set(sourceInfo.selected.get());
        targetInfo.sizeWithSubdir = sourceInfo.sizeWithSubdir;
        targetInfo.sizeWithoutSubdir = sourceInfo.sizeWithoutSubdir;
        targetInfo.filesWithSubdir = sourceInfo.filesWithSubdir;
        targetInfo.filesWithoutSubdir = sourceInfo.filesWithoutSubdir;
        targetInfo.duration = sourceInfo.duration;
        return targetInfo;
    }

    /*
        custmized get/set
     */
    public void setFile(File file) {
        setFileAttributes(file);
    }

    public String getSuffix() {
        if (file != null) {
            if (file.isDirectory()) {
                return null;
            } else {
                return FileNameTools.ext(file.getName());
            }
        } else if (data != null) {
            return FileNameTools.ext(data);
        } else {
            return null;
        }
    }

    public String getFullName() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return data;
        }
    }

    public String getPath() {
        if (file != null) {
            if (file.isDirectory()) {
                return file.getAbsolutePath() + File.separator;
            } else {
                return file.getParent() + File.separator;
            }
        } else {
            return null;
        }
    }

    public String getTfileName() {
        if (file != null) {
            if (file.isDirectory()) {
                return null;
            } else {
                return file.getName();
            }
        } else {
            return null;
        }
    }

    public String getFileName() {
        if (file != null) {
            return file.getName();
        } else {
            return null;
        }
    }

    public boolean isSelected() {
        return selected.get();
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

    public BooleanProperty getSelected() {
        return selected;
    }

    public void setSelected(boolean select) {
        selected.set(select);
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
