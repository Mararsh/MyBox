package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.FileNode;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-8
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectorySynchronizeController extends BaseTaskController {

    protected FileSynchronizeAttributes copyAttr;
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strFailedDelete;
    protected String strHandlingDirectory, strHandled;
    protected String strDeleteSuccessfully, strFileDeleteSuccessfully, strDirectoryDeleteSuccessfully;

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected ControlSynchronizeOptions optionsController;

    public DirectorySynchronizeController() {
        baseTitle = message("DirectorySynchronize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setParameters(this);

            initTarget();

            strFailedCopy = message("FailedCopy") + ": ";
            strCreatedSuccessfully = message("CreatedSuccessfully") + ": ";
            strCopySuccessfully = message("CopySuccessfully") + ": ";
            strDeleteSuccessfully = message("DeletedSuccessfully") + ": ";
            strFailedDelete = message("FailedDelete") + ": ";
            strFileDeleteSuccessfully = message("FileDeletedSuccessfully") + ": ";
            strDirectoryDeleteSuccessfully = message("DirectoryDeletedSuccessfully") + ": ";
            strHandlingDirectory = message("HandlingDirectory") + ": ";
            strHandled = message("Handled") + ": ";

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public void initTarget() {
        try {
            targetPathInputController.parent(this);

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        task
     */
    protected boolean checkTarget() {
        targetPath = targetPathInputController.pickFile();
        if (targetPath == null) {
            popError(message("Invalid") + ": " + message("TargetPath"));
            return false;
        }
        if (FileTools.isEqualOrSubPath(targetPath.getAbsolutePath(), sourcePath.getAbsolutePath())) {
            popError(message("TreeTargetComments"));
            return false;
        }
        targetPath.setWritable(true);
        targetPath.setExecutable(true);
        return true;
    }

    protected boolean checkSource() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            if (sourcePath == null || !sourcePath.exists()) {
                popError(message("Invalid") + ": " + message("SourcePath"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        copyAttr = optionsController.pickOptions();
        return copyAttr != null && checkTarget() && checkSource();
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        return synchronize(currentTask, targetPath.getAbsolutePath());
    }

    public boolean synchronize(FxTask currentTask, String targetpath) {
        showLogs(message("SourcePath") + ": " + sourcePath.getAbsolutePath());
        showLogs(message("TargetPath") + ": " + targetpath);
        boolean done;
        FileNode targetNode = targetNode(targetpath);
        if (!targetNode.isExisted()) {
            targetMkdirs(sourcePath, targetNode);
            showLogs(strCreatedSuccessfully + targetpath);
        }
        if (copyAttr.isConditionalCopy()) {
            done = conditionalCopy(currentTask, sourcePath, targetNode);
        } else {
            if (targetNode.isExisted()) {
                showLogs(message("ClearingTarget"));
                if (clearDir(currentTask, targetNode, false)) {
                    showLogs(message("TargetCleared"));
                } else if (!copyAttr.isContinueWhenError()) {
                    showLogs(message("FailClearTarget"));
                    return false;
                }
            }
            done = copyWholeDirectory(currentTask, sourcePath, targetNode);
        }
        if (!done || currentTask == null || !currentTask.isWorking()) {
            return false;
        }
        if (optionsController.deleteSourceCheck.isSelected()) {
            done = FileDeleteTools.deleteDir(currentTask, sourcePath);
            showLogs(message("SourcePathCleared"));
        }
        return done;
    }

    public boolean conditionalCopy(FxTask currentTask, File sourcePath, FileNode targetNode) {
        try {
            if (targetNode == null || sourcePath == null
                    || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(currentTask, sourcePath, targetNode)
                    && !copyAttr.isContinueWhenError()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                srcFileName = srcFile.getAbsolutePath();
                len = srcFile.length();
                if (srcFile.isFile()) {
                    copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                } else if (srcFile.isDirectory()) {
                    copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                }
                copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                if (srcFile.isHidden() && !copyAttr.isCopyHidden()) {
                    continue;
                }
                if (srcFile.canWrite() && copyAttr.isOnlyCopyReadonly()) {
                    continue;
                }
                if (copyAttr.isNotCopySome()) {
                    List<String> blacks = copyAttr.getNotCopyNames();
                    String srcName = srcFile.getName();
                    boolean black = false;
                    for (String b : blacks) {
                        if (currentTask == null || !currentTask.isWorking()) {
                            return false;
                        }
                        if (srcName.contains(b)) {
                            black = true;
                            break;
                        }
                    }
                    if (black) {
                        continue;
                    }
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                FileNode targetChildNode = targetNode(targetNode.getFullName() + File.separator + srcFile.getName());
                String targetChildName = targetChildNode.nodeFullName();
                if (srcFile.isFile()) {
                    if (copyAttr.isOnlyCopyModified()) {
                        if (srcFile.lastModified() <= copyAttr.getModifyAfter()) {
                            continue;
                        }
                    }
                    if (targetChildNode.isExisted()) {
                        if (!copyAttr.isCopyExisted()) {
                            continue;
                        }
                        if (copyAttr.isOnlyCopyModified()) {
                            if (!isModified(srcFile, targetChildNode)) {
                                continue;
                            }
                        }
                    } else if (!copyAttr.isCopyNew()) {
                        continue;
                    }
                    if (copyFile(currentTask, srcFile, targetChildNode)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCopySuccessfully + copyAttr.getCopiedFilesNumber() + " "
                                    + srcFileName + " -> " + targetChildName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetChildName);
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory() && copyAttr.isCopySubdir()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(strHandlingDirectory + srcFileName);
                    }
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (!targetChildNode.isExisted()) {
                        targetMkdirs(srcFile, targetChildNode);
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                    }
                    if (conditionalCopy(currentTask, srcFile, targetChildNode)) {
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strHandled + srcFileName + " -> " + targetChildName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetChildName);
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    public boolean copyWholeDirectory(FxTask currentTask, File sourcePath, FileNode targetNode) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                srcFileName = srcFile.getAbsolutePath();
                len = srcFile.length();
                if (srcFile.isFile()) {
                    copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                } else if (srcFile.isDirectory()) {
                    copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                }
                copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                FileNode targetChildNode = targetNode(targetNode.getFullName() + File.separator + srcFile.getName());
                String targetChildName = targetChildNode.getFullName();
                if (srcFile.isFile()) {
                    if (copyFile(currentTask, srcFile, targetChildNode)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCopySuccessfully + copyAttr.getCopiedFilesNumber() + " "
                                    + srcFileName + " -> " + targetChildName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetChildName);
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(message("HandlingDirectory") + " " + srcFileName);
                    }
                    targetMkdirs(srcFile, targetChildNode);
                    copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strCreatedSuccessfully + targetChildName);
                    }
                    if (copyWholeDirectory(currentTask, srcFile, targetChildNode)) {
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strHandled + srcFileName + " -> " + targetChildName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetChildName);
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    public boolean copyFile(FxTask currentTask, File sourceFile, FileNode targetNode) {
        try {
            if (currentTask == null || !currentTask.isWorking() || targetNode == null
                    || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            String srcname = sourceFile.getAbsolutePath();
            String tarname = targetNode.getFullName();
            if (!targetNode.isExisted()) {
                if (copyAttr.isCopyAttrinutes()) {
                    Files.copy(Paths.get(srcname), Paths.get(tarname),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(srcname), Paths.get(tarname));
                }
            } else if (!copyAttr.isCanReplace()) {
                return false;
            } else if (copyAttr.isCopyAttrinutes()) {
                Files.copy(Paths.get(srcname), Paths.get(tarname),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(srcname), Paths.get(tarname),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean clearDir(FxTask currentTask, FileNode targetNode, boolean record) {
        if (currentTask == null || !currentTask.isWorking() || targetNode == null) {
            return false;
        }
        List<FileNode> children = targetChildren(currentTask, targetNode);
        if (children == null) {
            return true;
        }
        for (FileNode child : children) {
            if (currentTask == null || !currentTask.isWorking()) {
                return false;
            }
            long len = child.getFileSize();
            if (len <= 0) {
                continue;
            }
            if (child.isDirectory()) {
                if (clearDir(currentTask, child, record)) {
                    try {
                        deleteTargetFile(currentTask, child);
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getDeletedDirectories() + "  "
                                        + strDirectoryDeleteSuccessfully + child.nodeFullName());
                            }
                        }
                    } catch (Exception e) {
                        if (record) {
                            copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                            copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                            updateLogs(strFailedDelete + child.nodeFullName());
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    if (record) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + child.nodeFullName());
                        }
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    deleteTargetFile(currentTask, child);
                    if (record) {
                        copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getDeletedFiles() + "  "
                                    + strFileDeleteSuccessfully + child.nodeFullName());
                        }
                    }
                } catch (Exception e) {
                    if (record) {
                        copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        updateLogs(strFailedDelete + child.nodeFullName());
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
        }
        return true; // When return true, it is not necessary that the dir is cleared.
    }

    public boolean deleteNonExisted(FxTask currentTask, File sourcePath, FileNode targetNode) {
        if (currentTask == null || !currentTask.isWorking() || sourcePath == null || targetNode == null) {
            return false;
        }
        if (!copyAttr.isDeleteNotExisteds() || !targetNode.isDirectory()) {
            return true;
        }
        List<FileNode> children = targetChildren(currentTask, targetNode);
        if (currentTask == null || !currentTask.isWorking()) {
            return false;
        }
        if (children == null) {
            return true;
        }
        for (FileNode child : children) {
            if (currentTask == null || !currentTask.isWorking()) {
                return false;
            }
            File srcFile = new File(sourcePath + File.separator + new File(child.getFileName()).getName());
            if (srcFile.exists()) {
                continue;
            }
            long len = child.getFileSize();
            if (child.isDirectory()) {
                if (clearDir(currentTask, child, true)) {
                    try {
                        deleteTargetFile(currentTask, child);
                        copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strDirectoryDeleteSuccessfully + child.nodeFullName());
                        }
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        showLogs(strFailedDelete + child.nodeFullName());
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    showLogs(strFailedDelete + child.nodeFullName());
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    deleteTargetFile(currentTask, child);
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFileDeleteSuccessfully + child.nodeFullName());
                    }
                } catch (Exception e) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    showLogs(strFailedDelete + child.nodeFullName());
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
        }
        return true; // When return true, it is not necessary that all things are good.
    }

    public boolean isModified(File srcFile, FileNode targetNode) {
        return srcFile.lastModified() > targetNode.getModifyTime();
    }

    @Override
    public void afterSuccess() {
        updateLogs(message("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                + message("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                + message("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
        updateLogs(message("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                + message("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                + message("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
        if (copyAttr.isConditionalCopy() && copyAttr.isDeleteNotExisteds()) {
            updateLogs(message("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                    + message("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                    + message("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
        }

        if (miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (openCheck.isSelected()) {
            openTarget();
        }
    }

    /*
        target
     */
    public FileNode targetNode(String targetName) {
        return new FileNode(new File(targetName));
    }

    public List<FileNode> targetChildren(FxTask currentTask, FileNode targetNode) {
        List<FileNode> list = new ArrayList<>();
        try {
            String path = targetNode.getFullName();
            String[] names = new File(path).list();
            for (String name : names) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return list;
                }
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                list.add(targetNode(path + File.separator + name));
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
        return list;
    }

    public void deleteTargetFile(FxTask currentTask, FileNode targetNode) {
        FileDeleteTools.delete(currentTask, targetNode.getFullName());
    }

    public void targetMkdirs(File srcFile, FileNode targetNode) {
        try {
            File tFile = new File(targetNode.getFullName());
            tFile.mkdirs();
            tFile.setExecutable(true);
            tFile.setReadable(true);
            tFile.setWritable(true);
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(strCreatedSuccessfully + tFile.getAbsolutePath());
            }
//            if (copyAttr.isCopyAttrinutes()) {  // this looks not work
////                tFile.setExecutable(srcFile.canExecute());
////                tFile.setReadable(srcFile.canRead());
////                tFile.setWritable(srcFile.canWrite());
//                tFile.setLastModified(srcFile.lastModified());   
//            }
        } catch (Exception e) {
            showLogs(e.toString());
        }

    }

    @FXML
    @Override
    public void openTarget() {
        try {
            browseURI(targetPathInputController.pickFile().toURI());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
