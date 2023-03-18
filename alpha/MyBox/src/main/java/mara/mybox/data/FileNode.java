package mara.mybox.data;

import java.io.File;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-03-18
 * @License Apache License Version 2.0
 */
public class FileNode extends FileInformation {

    public boolean isRemote;
    public FileNode parentNode;
    public String nodename;

    public FileNode() {
        init();
        parentNode = null;
        isRemote = false;
    }

    public FileNode(File file) {
        setFileAttributes(file);
    }

    public FileNode(FileNode parentFile, String nodename, boolean isRemote) {
        this.parentNode = parentFile;
        this.nodename = nodename;
        this.isRemote = isRemote;
    }

    /*
        customized get/set
     */
    @Override
    public String getFileName() {
        if (isRemote) {
            return nodename;
        } else {
            return super.getFileName();
        }
    }

    public String fullName() {
        return (parentNode != null ? parentNode.fullName() + File.separator : "") + nodename;
    }

    @Override
    public String getSuffix() {
        if (isRemote) {
            return fileType != null ? message(fileType.name()) : null;
        } else {
            return super.getSuffix();
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

}
