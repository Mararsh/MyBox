package mara.mybox.data;

import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2018-7-8 13:14:38
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class FileSynchronizeAttributes {

    private boolean continueWhenError, conditionalCopy, copySubdir, copyEmpty,
            copyNew, copyHidden, onlyCopyReadonly, copyExisted, onlyCopyModified,
            deleteNotExisteds, notCopySome, canReplace,
            copyAttrinutes, copyMTime, setPermissions;
    private long modifyAfter, copiedFilesNumber, copiedDirectoriesNumber, totalFilesNumber,
            totalDirectoriesNumber, totalSize, copiedSize, deletedFiles, deletedDirectories,
            deletedSize, failedDeletedFiles, failedDeletedDirectories, failedDeletedSize;
    private List<String> notCopyNames;
    private int permissions;

    public FileSynchronizeAttributes() {
        continueWhenError = conditionalCopy = copySubdir = copyEmpty = copyNew = copyHidden = true;
        copyExisted = onlyCopyModified = canReplace = copyAttrinutes = copyMTime = true;
        onlyCopyReadonly = deleteNotExisteds = notCopySome = setPermissions = false;
        copiedFilesNumber = copiedDirectoriesNumber = totalFilesNumber = totalDirectoriesNumber = 0;
        totalSize = copiedSize = deletedFiles = deletedDirectories = deletedSize = 0;
        failedDeletedFiles = failedDeletedDirectories = failedDeletedSize = 0;
        permissions = -1;
    }

    public boolean isContinueWhenError() {
        return continueWhenError;
    }

    public void setContinueWhenError(boolean continueWhenError) {
        this.continueWhenError = continueWhenError;
    }

    public boolean isConditionalCopy() {
        return conditionalCopy;
    }

    public void setConditionalCopy(boolean conditionalCopy) {
        this.conditionalCopy = conditionalCopy;
    }

    public boolean isCopySubdir() {
        return copySubdir;
    }

    public void setCopySubdir(boolean copySubdir) {
        this.copySubdir = copySubdir;
    }

    public boolean isCopyEmpty() {
        return copyEmpty;
    }

    public void setCopyEmpty(boolean copyEmpty) {
        this.copyEmpty = copyEmpty;
    }

    public boolean isCopyNew() {
        return copyNew;
    }

    public void setCopyNew(boolean copyNew) {
        this.copyNew = copyNew;
    }

    public boolean isCopyHidden() {
        return copyHidden;
    }

    public void setCopyHidden(boolean copyHidden) {
        this.copyHidden = copyHidden;
    }

    public boolean isOnlyCopyReadonly() {
        return onlyCopyReadonly;
    }

    public void setOnlyCopyReadonly(boolean onlyCopyReadonly) {
        this.onlyCopyReadonly = onlyCopyReadonly;
    }

    public boolean isCopyExisted() {
        return copyExisted;
    }

    public void setCopyExisted(boolean copyExisted) {
        this.copyExisted = copyExisted;
    }

    public boolean isOnlyCopyModified() {
        return onlyCopyModified;
    }

    public void setOnlyCopyModified(boolean onlyCopyModified) {
        this.onlyCopyModified = onlyCopyModified;
    }

    public boolean isDeleteNotExisteds() {
        return deleteNotExisteds;
    }

    public void setDeleteNotExisteds(boolean deleteNotExisteds) {
        this.deleteNotExisteds = deleteNotExisteds;
    }

    public boolean isNotCopySome() {
        return notCopySome;
    }

    public void setNotCopySome(boolean notCopySome) {
        this.notCopySome = notCopySome;
    }

    public boolean isCanReplace() {
        return canReplace;
    }

    public void setCanReplace(boolean canReplace) {
        this.canReplace = canReplace;
    }

    public boolean isCopyAttrinutes() {
        return copyAttrinutes;
    }

    public void setCopyAttrinutes(boolean copyAttrinutes) {
        this.copyAttrinutes = copyAttrinutes;
    }

    public long getModifyAfter() {
        return modifyAfter;
    }

    public void setModifyAfter(long modifyAfter) {
        this.modifyAfter = modifyAfter;
    }

    public List<String> getNotCopyNames() {
        return notCopyNames;
    }

    public void setNotCopyNames(List<String> notCopyNames) {
        this.notCopyNames = notCopyNames;
    }

    public long getCopiedFilesNumber() {
        return copiedFilesNumber;
    }

    public void setCopiedFilesNumber(long copiedFilesNumber) {
        this.copiedFilesNumber = copiedFilesNumber;
    }

    public long getCopiedDirectoriesNumber() {
        return copiedDirectoriesNumber;
    }

    public void setCopiedDirectoriesNumber(long copiedDirectoriesNumber) {
        this.copiedDirectoriesNumber = copiedDirectoriesNumber;
    }

    public long getTotalFilesNumber() {
        return totalFilesNumber;
    }

    public void setTotalFilesNumber(long totalFilesNumber) {
        this.totalFilesNumber = totalFilesNumber;
    }

    public long getTotalDirectoriesNumber() {
        return totalDirectoriesNumber;
    }

    public void setTotalDirectoriesNumber(long totalDirectoriesNumber) {
        this.totalDirectoriesNumber = totalDirectoriesNumber;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCopiedSize() {
        return copiedSize;
    }

    public void setCopiedSize(long copiedSize) {
        this.copiedSize = copiedSize;
    }

    public long getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(long deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    public long getDeletedDirectories() {
        return deletedDirectories;
    }

    public void setDeletedDirectories(long deletedDirectories) {
        this.deletedDirectories = deletedDirectories;
    }

    public long getDeletedSize() {
        return deletedSize;
    }

    public void setDeletedSize(long deletedSize) {
        this.deletedSize = deletedSize;
    }

    public long getFailedDeletedFiles() {
        return failedDeletedFiles;
    }

    public void setFailedDeletedFiles(long failedDeletedFiles) {
        this.failedDeletedFiles = failedDeletedFiles;
    }

    public long getFailedDeletedDirectories() {
        return failedDeletedDirectories;
    }

    public void setFailedDeletedDirectories(long failedDeletedDirectories) {
        this.failedDeletedDirectories = failedDeletedDirectories;
    }

    public long getFailedDeletedSize() {
        return failedDeletedSize;
    }

    public void setFailedDeletedSize(long failedDeletedSize) {
        this.failedDeletedSize = failedDeletedSize;
    }

    public boolean isCopyMTime() {
        return copyMTime;
    }

    public void setCopyMTime(boolean copyMTime) {
        this.copyMTime = copyMTime;
    }

    public boolean isSetPermissions() {
        return setPermissions;
    }

    public void setSetPermissions(boolean setPermissions) {
        this.setPermissions = setPermissions;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

}
