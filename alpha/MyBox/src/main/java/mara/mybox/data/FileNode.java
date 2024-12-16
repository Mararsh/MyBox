package mara.mybox.data;

import com.jcraft.jsch.SftpATTRS;
import java.io.File;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-03-18
 * @License Apache License Version 2.0
 */
public class FileNode extends FileInformation {

    public boolean isRemote, isExisted;
    public FileNode parentNode;
    public String nodename, permission, separator;
    public long accessTime;
    public int uid, gid;

    public FileNode() {
        init();
        parentNode = null;
        isRemote = false;
        separator = File.separator;
    }

    public FileNode(File file) {
        setFileAttributes(file);
        isExisted = file != null && file.exists();
        nodename = super.getFullName();
    }

    public FileNode attrs(SftpATTRS attrs) {
        try {
            if (attrs == null) {
                isExisted = false;
                return this;
            }
            isExisted = true;
            if (attrs.isDir()) {
                setFileType(FileInformation.FileType.Directory);
            } else if (attrs.isBlk()) {
                setFileType(FileInformation.FileType.Block);
            } else if (attrs.isChr()) {
                setFileType(FileInformation.FileType.Character);
            } else if (attrs.isFifo()) {
                setFileType(FileInformation.FileType.FIFO);
            } else if (attrs.isLink()) {
                setFileType(FileInformation.FileType.Link);
            } else if (attrs.isSock()) {
                setFileType(FileInformation.FileType.Socket);
            } else {
                setFileType(FileInformation.FileType.File);
            }
            setFileSize(attrs.getSize());
            setModifyTime(attrs.getMTime() * 1000l);
            setAccessTime(attrs.getATime() * 1000l);
            setUid(attrs.getUId());
            setGid(attrs.getGId());
            setPermission(attrs.getPermissionsString());
        } catch (Exception e) {
        }
        return this;
    }

    public String parentName() {
        return parentNode != null ? parentNode.nodeFullName() : "";
    }

    public String path(boolean endSeparator) {
        String pathname;
        if (parentNode == null) {
            pathname = nodename;
        } else if (isDirectory()) {
            pathname = nodeFullName();
        } else {
            pathname = parentNode.nodeFullName();
        }
        return endSeparator ? pathname + separator : pathname;
    }

    public String nodeFullName() {
        return (parentNode != null ? parentNode.nodeFullName() + separator : "") + nodename;
    }

    public boolean isExisted() {
        return isExisted;
    }

    public boolean isDirectory() {
        return fileType == FileInformation.FileType.Directory;
    }


    /*
        customized get/set
     */
    @Override
    public String getFullName() {
        if (isRemote) {
            return nodeFullName();
        } else {
            return super.getFullName();
        }
    }

    @Override
    public String getFileName() {
        if (file != null) {
            return file.getName();
        } else if (nodename != null) {
            return FileNameTools.name(nodename, separator);
        } else if (data != null) {
            return FileNameTools.name(data, separator);
        } else {
            return null;
        }
    }

    @Override
    public String getSuffix() {
        if (isRemote) {
            return fileType != null ? message(fileType.name()) : null;
        }
        if (fileType != null && fileType != FileType.File) {
            return message(fileType.name());
        }
        if (file != null) {
            if (file.isDirectory()) {
                return null;
            } else {
                return FileNameTools.ext(file.getName(), separator);
            }
        } else if (nodename != null) {
            return FileNameTools.ext(nodename, separator);
        } else if (data != null) {
            return FileNameTools.ext(data, separator);
        } else {
            return null;
        }
    }

    @Override
    public void setData(String data) {
        this.data = data;
        if (nodename == null) {
            nodename = data;
        }
    }

    /*
        get/set
     */
    public FileNode getParentNode() {
        return parentNode;
    }

    public FileNode setParentFile(FileNode parentFile) {
        this.parentNode = parentFile;
        return this;
    }

    public String getNodename() {
        return nodename;
    }

    public FileNode setNodename(String nodename) {
        this.nodename = nodename;
        return this;
    }

    public boolean isIsRemote() {
        return isRemote;
    }

    public FileNode setIsRemote(boolean isRemote) {
        this.isRemote = isRemote;
        return this;
    }

    public String getSeparator() {
        return separator;
    }

    public FileNode setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public FileNode setAccessTime(long accessTime) {
        this.accessTime = accessTime;
        return this;
    }

    public String getPermission() {
        return permission;
    }

    public FileNode setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public int getUid() {
        return uid;
    }

    public FileNode setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public int getGid() {
        return gid;
    }

    public FileNode setGid(int gid) {
        this.gid = gid;
        return this;
    }

}
