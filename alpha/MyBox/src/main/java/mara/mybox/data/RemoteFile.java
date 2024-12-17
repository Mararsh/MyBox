package mara.mybox.data;

import com.jcraft.jsch.SftpATTRS;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-03-18
 * @License Apache License Version 2.0
 */
public class RemoteFile extends FileNode {

    public RemoteFile() {
        separator = "/";
    }

    public RemoteFile(SftpATTRS attrs) {
        try {
            if (attrs == null) {
                isExisted = false;
                return;
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
    }

    @Override
    public String getFullName() {
        return nodeFullName();
    }

    @Override
    public String getSuffix() {
        return fileType != null ? message(fileType.name()) : null;
    }

}
