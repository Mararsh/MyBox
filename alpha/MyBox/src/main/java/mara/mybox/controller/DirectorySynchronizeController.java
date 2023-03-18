package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.dev.MyBoxLog;
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
    @FXML
    protected CheckBox miaoCheck, openCheck;

    public DirectorySynchronizeController() {
        baseTitle = message("DirectorySynchronize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTarget();

            miaoCheck.setSelected(UserConfig.getBoolean(baseName + "Miao", true));
            miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Miao", miaoCheck.isSelected());
                }
            });

            openCheck.setSelected(UserConfig.getBoolean(baseName + "OpenTarget", true));
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "OpenTarget", openCheck.isSelected());
                }
            });

            strFailedCopy = message("FailedCopy") + ": ";
            strCreatedSuccessfully = message("CreatedSuccessfully") + ": ";
            strCopySuccessfully = message("CopySuccessfully") + ": ";
            strDeleteSuccessfully = message("DeletedSuccessfully") + ": ";
            strFailedDelete = message("FailedDelete") + ": ";
            strFileDeleteSuccessfully = message("FileDeletedSuccessfully") + ": ";
            strDirectoryDeleteSuccessfully = message("DirectoryDeletedSuccessfully") + ": ";
            strHandlingDirectory = message("HandlingDirectory");
            strHandled = message("Handled");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void initTarget() {
        try {
            targetPathInputController.baseName(baseName).init();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(targetPathInputController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        task
     */
    protected boolean checkTarget() {
        targetPath = targetPathInputController.file();
        if (targetPath == null) {
            popError(message("Invlid") + ": " + message("TargetPath"));
            return false;
        }
        if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
            popError(message("TargetPathShouldNotSourceSub"));
            return false;
        }
        updateLogs(message("TargetPath") + ": " + targetPath.getAbsolutePath() + "\n", true, true);
        if (!targetPath.exists()) {
            targetPath.mkdirs();
            updateLogs(strCreatedSuccessfully + targetPath.getAbsolutePath(), true);
        }
        targetPath.setWritable(true);
        targetPath.setExecutable(true);
        return true;
    }

    protected boolean checkSource() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            updateLogs(message("SourcePath") + ": " + sourcePathInput.getText() + "\n", true, true);

            return true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        copyAttr = optionsController.pickOptions();
        return copyAttr != null && checkTarget() && checkSource();
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        initLogs();
    }

    @Override
    public boolean doTask() {
        return synchronize(targetPath.getAbsolutePath());
    }

    public boolean synchronize(String targetDirectory) {
        boolean done;
        if (copyAttr.isConditionalCopy()) {
            done = conditionalCopy(sourcePath, targetDirectory);
        } else {
            if (targetExist(targetDirectory)) {
                updateLogs(message("ClearingTarget"), true);
                if (clearDir(targetDirectory, false)) {
                    updateLogs(message("TargetCleared"), true);
                } else if (!copyAttr.isContinueWhenError()) {
                    updateLogs(message("FailClearTarget"), true);
                    return false;
                }
            }
            done = copyWholeDirectory(sourcePath, targetDirectory);
        }
        if (!done || task == null || task.isCancelled()) {
            return false;
        }
        if (optionsController.deleteSourceCheck.isSelected()) {
            done = FileDeleteTools.deleteDir(sourcePath);
            updateLogs(message("SourcePathCleared"), true);
        }
        return done;
    }

    public boolean conditionalCopy(File sourcePath, String targetDirectory) {
        try {
            if (targetDirectory == null || sourcePath == null
                    || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(sourcePath, targetDirectory)
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
                if (task == null || task.isCancelled()) {
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
                        if (srcName.contains(b)) {
                            black = true;
                            break;
                        }
                    }
                    if (black) {
                        continue;
                    }
                }
                String targetName = targetName(targetDirectory + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyAttr.isOnlyCopyModified()) {
                        if (srcFile.lastModified() <= copyAttr.getModifyAfter()) {
                            continue;
                        }
                    }
                    if (targetExist(targetName)) {
                        if (!copyAttr.isCopyExisted()) {
                            continue;
                        }
                        if (copyAttr.isOnlyCopyModified()) {
                            if (srcFile.lastModified() <= targetFileModifyTime(targetName)) {
                                continue;
                            }
                        }
                    } else if (!copyAttr.isCopyNew()) {
                        continue;
                    }
                    if (copyFile(srcFile, targetName)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCopySuccessfully + ": " + copyAttr.getCopiedFilesNumber() + " "
                                    + srcFileName + " -> " + targetName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetName);
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory() && copyAttr.isCopySubdir()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(strHandlingDirectory + ": " + srcFileName);
                    }
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (!targetExist(targetName)) {
                        targetMkdirs(targetName);
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + targetName);
                        }
                    }
                    if (conditionalCopy(srcFile, targetName)) {
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strHandled + ": " + srcFileName + " -> " + targetName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetName);
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            updateLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    public boolean copyWholeDirectory(File sourcePath, String targetDirectory) {
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
                if (task == null || task.isCancelled()) {
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
                String targetName = targetName(targetDirectory + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyFile(srcFile, targetName)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCopySuccessfully + ": " + copyAttr.getCopiedFilesNumber() + " "
                                    + srcFileName + " -> " + targetName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetName);
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(message("HandlingDirectory") + " " + srcFileName);
                    }
                    targetMkdirs(targetName);
                    copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strCreatedSuccessfully + targetName);
                    }
                    if (copyWholeDirectory(srcFile, targetName)) {
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strHandled + ": " + srcFileName + " -> " + targetName);
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetName);
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            showLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    public boolean clearDir(String dir, boolean record) {
        if (task == null || task.isCancelled()) {
            return false;
        }
        List<String> children = targetChildren(dir);
        if (children == null) {
            return true;
        }
        for (String child : children) {
            if (task == null || task.isCancelled()) {
                return false;
            }
            long len = targetFileLength(child);
            if (len <= 0) {
                continue;
            }
            if (isTargetDirectory(child)) {
                if (clearDir(child, record)) {
                    try {
                        deleteTargetFile(child);
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getDeletedDirectories() + "  " + strDirectoryDeleteSuccessfully + child);
                            }
                        }
                    } catch (Exception e) {
                        if (record) {
                            copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                            copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                            updateLogs(strFailedDelete + child);
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
                            updateLogs(strFailedDelete + child);
                        }
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            try {
                deleteTargetFile(child);
                if (record) {
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(copyAttr.getDeletedFiles() + "  " + strFileDeleteSuccessfully + child);
                    }
                }
            } catch (Exception e) {
                if (record) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + child);
                }
                if (!copyAttr.isContinueWhenError()) {
                    return false;
                }
            }
        }
        return true; // When return true, it is not necessary that the dir is cleared.
    }

    public boolean copyFile(File sourceFile, String targetFile) {
        try {
            if (task == null || task.isCancelled()
                    || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (!targetExist(targetFile)) {
                if (copyAttr.isCopyAttrinutes()) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile));
                }
            } else if (!copyAttr.isCanReplace() || isTargetDirectory(targetFile)) {
                return false;
            } else if (copyAttr.isCopyAttrinutes()) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean deleteNonExisted(File sourcePath, String targetName) {
        if (task == null || task.isCancelled()) {
            return false;
        }
        if (!copyAttr.isDeleteNotExisteds() || !isTargetDirectory(targetName)) {
            return true;
        }
        List<String> children = targetChildren(targetName);
        if (children == null) {
            return true;
        }
        for (String child : children) {
            if (task == null || task.isCancelled()) {
                return false;
            }
            File srcFile = new File(sourcePath + File.separator + new File(child).getName());
            if (srcFile.exists()) {
                continue;
            }
            long len = targetFileLength(child);
            if (isTargetDirectory(child)) {
                if (clearDir(child, true)) {
                    try {
                        deleteTargetFile(child);
                        copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strDirectoryDeleteSuccessfully + child);
                        }
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            showLogs(strFailedDelete + child);
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFailedDelete + child);
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    deleteTargetFile(child);
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFileDeleteSuccessfully + child);
                    }
                } catch (Exception e) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    showLogs(strFailedDelete + child);
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
        }
        return true; // When return true, it is not necessary that all things are good.
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
            openTarget(null);
        }
    }

    /*
        target
     */
    public String targetName(String targetName) {
        return targetName;
    }

    public boolean targetExist(String targetName) {
        return new File(targetName).exists();
    }

    public List<String> targetChildren(String targetName) {
        List<String> list = new ArrayList<>();
        try {
            String[] names = new File(targetName).list();
            for (String name : names) {
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                list.add(targetName(targetName + File.separator + name));
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
        return list;
    }

    public boolean isTargetDirectory(String targetName) {
        return new File(targetName).isDirectory();
    }

    public long targetFileLength(String targetName) {
        return new File(targetName).length();
    }

    public long targetFileModifyTime(String targetName) {
        return new File(targetName).lastModified();
    }

    public void deleteTargetFile(String targetName) {
        FileDeleteTools.delete(targetName);
    }

    public void targetMkdirs(String targetDirectory) {
        new File(targetDirectory).mkdirs();
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            browseURI(targetPathInputController.file().toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
