package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
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

    protected boolean isConditional;
    protected FileSynchronizeAttributes copyAttr;
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strFailedDelete;
    protected String strDeleteSuccessfully, strFileDeleteSuccessfully, strDirectoryDeleteSuccessfully;

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected VBox dirsBox, conditionsBox, logsBox;
    @FXML
    protected TextField notCopyInput;
    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck, copyReadonlyCheck,
            copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck, notCopyCheck, copyAttrCheck, continueCheck,
            deleteSourceCheck, miaoCheck, openCheck;
    @FXML
    protected DatePicker modifyAfterInput;

    public DirectorySynchronizeController() {
        baseTitle = message("DirectorySynchronize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSource();
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
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void initSource() {
        try {
            deleteNonExistedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (deleteNonExistedCheck.isSelected()) {
                        deleteNonExistedCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteNonExistedCheck.setStyle(null);
                    }
                }
            });

            deleteSourceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (deleteSourceCheck.isSelected()) {
                        deleteSourceCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteSourceCheck.setStyle(null);
                    }
                }
            });

            checkIsConditional();
            copyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkIsConditional();
                }
            });

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

            copyAttr = new FileSynchronizeAttributes();
            copyAttr.setContinueWhenError(continueCheck.isSelected());
            copyAttr.setCopyAttrinutes(copyAttrCheck != null ? copyAttrCheck.isSelected() : true);
            copyAttr.setCopyEmpty(copyEmptyCheck.isSelected());
            copyAttr.setConditionalCopy(isConditional);
            copyAttr.setCopyExisted(copyExistedCheck.isSelected());
            copyAttr.setCopyHidden(copyHiddenCheck.isSelected());
            copyAttr.setCopyNew(copyNewCheck.isSelected());
            copyAttr.setCopySubdir(copySubdirCheck.isSelected());
            copyAttr.setNotCopySome(notCopyCheck.isSelected());
            copyAttr.setOnlyCopyReadonly(copyReadonlyCheck.isSelected());
            List<String> notCopy = new ArrayList<>();
            if (copyAttr.isNotCopySome() && notCopyInput.getText() != null && !notCopyInput.getText().trim().isEmpty()) {
                String[] s = notCopyInput.getText().split(",");
                notCopy.addAll(Arrays.asList(s));
            }
            copyAttr.setNotCopyNames(notCopy);
            copyAttr.setOnlyCopyModified(copyModifiedCheck.isSelected());
            copyAttr.setModifyAfter(0);
            if (copyAttr.isOnlyCopyModified() && modifyAfterInput.getValue() != null) {
                copyAttr.setModifyAfter(DateTools.localDateToDate(modifyAfterInput.getValue()).getTime());
            }
            copyAttr.setDeleteNotExisteds(deleteNonExistedCheck.isSelected());

            if (!copyAttr.isCopyNew() && !copyAttr.isCopyExisted() && !copyAttr.isCopySubdir()) {
                alertInformation(message("NothingCopy"));
                return false;
            }
            // In case that the source path itself is in blacklist
            if (copyAttr.isNotCopySome()) {
                List<String> keys = copyAttr.getNotCopyNames();
                String srcName = sourcePath.getName();
                for (String key : keys) {
                    if (srcName.contains(key)) {
                        alertInformation(message("NothingCopy"));
                        return false;
                    }
                }
            }

            updateLogs(message("SourcePath") + ": " + sourcePathInput.getText() + "\n", true, true);

            strFailedCopy = message("FailedCopy") + ": ";
            strCreatedSuccessfully = message("CreatedSuccessfully") + ": ";
            strCopySuccessfully = message("CopySuccessfully") + ": ";
            strDeleteSuccessfully = message("DeletedSuccessfully") + ": ";
            strFailedDelete = message("FailedDelete") + ": ";
            strFileDeleteSuccessfully = message("FileDeletedSuccessfully") + ": ";
            strDirectoryDeleteSuccessfully = message("DirectoryDeletedSuccessfully") + ": ";

            return true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        initLogs();
        return checkTarget() && checkSource();
    }

    @Override
    public void beforeTask() {
        super.beforeTask();

    }

    @Override
    public boolean doTask() {
        boolean done;
        if (copyAttr.isConditionalCopy()) {
            done = conditionalCopy(sourcePath, targetPath);
        } else {
            if (targetPath.exists()) {
                updateLogs(message("ClearingTarget"), true);
                if (clearDir(targetPath, false)) {
                    updateLogs(message("TargetCleared"), true);
                } else if (!copyAttr.isContinueWhenError()) {
                    updateLogs(message("FailClearTarget"), true);
                    return false;
                }
            }
            done = copyWholeDirectory(sourcePath, targetPath);
        }
        if (!done || task == null || task.isCancelled()) {
            return false;
        }
        if (deleteSourceCheck.isSelected()) {
            done = FileDeleteTools.deleteDir(sourcePath);
            updateLogs(message("SourcePathCleared"), true);
        }
        return done;
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

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            browseURI(targetPathInputController.file().toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkIsConditional() {
        RadioButton sort = (RadioButton) copyGroup.getSelectedToggle();
        if (!message("CopyConditionally").equals(sort.getText())) {
            conditionsBox.setDisable(true);
            isConditional = false;
        } else {
            conditionsBox.setDisable(false);
            isConditional = true;
        }
    }

    protected boolean conditionalCopy(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                showLogs(message("TargetPathShouldNotSourceSub") + ": " + targetPath);
                return false;
            }
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(sourcePath, targetPath) && !copyAttr.isContinueWhenError()) {
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
                File tFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyAttr.isOnlyCopyModified()) {
                        if (srcFile.lastModified() <= copyAttr.getModifyAfter()) {
                            continue;
                        }
                    }
                    if (tFile.exists()) {
                        if (!copyAttr.isCopyExisted()) {
                            continue;
                        }
                        if (copyAttr.isOnlyCopyModified()) {
                            if (srcFile.lastModified() <= tFile.lastModified()) {
                                continue;
                            }
                        }
                    } else if (!copyAttr.isCopyNew()) {
                        continue;
                    }
                    if (copyFile(srcFile, tFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory() && copyAttr.isCopySubdir()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(message("HandlingDirectory") + " " + srcFileName);
                    }
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (!tFile.exists()) {
                        tFile.mkdirs();
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + tFile.getAbsolutePath());
                        }
                    }
                    if (conditionalCopy(srcFile, tFile)) {
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + tFile.getAbsolutePath());
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

    protected boolean copyWholeDirectory(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                showLogs(message("TargetPathShouldNotSourceSub") + ": " + targetPath);
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
                File tFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyFile(srcFile, tFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        showLogs(message("HandlingDirectory") + " " + srcFileName);
                    }
                    tFile.mkdirs();
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strCreatedSuccessfully + tFile.getAbsolutePath());
                    }
                    if (copyWholeDirectory(srcFile, tFile)) {
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            showLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    // clearDir can not be paused to avoid logic messed.
    protected boolean clearDir(File dir, boolean record) {
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        for (File file : files) {
            long len = file.length();
            String filename = file.getAbsolutePath();
            if (file.isDirectory()) {
                if (clearDir(file, record)) {
                    try {
                        FileDeleteTools.delete(file);
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getDeletedDirectories() + "  " + strDirectoryDeleteSuccessfully + filename);
                            }
                        }
                    } catch (Exception e) {
                        if (record) {
                            copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                            copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                            updateLogs(strFailedDelete + filename);
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
                            updateLogs(strFailedDelete + filename);
                        }
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            try {
                FileDeleteTools.delete(file);
                if (record) {
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(copyAttr.getDeletedFiles() + "  " + strFileDeleteSuccessfully + filename);
                    }
                }
            } catch (Exception e) {
                if (record) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + filename);
                }
                if (!copyAttr.isContinueWhenError()) {
                    return false;
                }
            }
        }
        return true; // When return true, it is not necessary that the dir is cleared.
    }

    protected boolean copyFile(File sourceFile, File targetFile) {
        try {
            if (task == null || task.isCancelled()
                    || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (!targetFile.exists()) {
                if (copyAttr.isCopyAttrinutes()) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()));
                }
            } else if (!copyAttr.isCanReplace() || targetFile.isDirectory()) {
                return false;
            } else if (copyAttr.isCopyAttrinutes()) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected boolean deleteNonExisted(File sourcePath, File targetPath) {
        if (!copyAttr.isDeleteNotExisteds() || !targetPath.isDirectory()) {
            return true;
        }
        File[] files = targetPath.listFiles();
        if (files == null) {
            return true;
        }
        for (File tFile : files) {
            if (task == null || task.isCancelled()) {
                return false;
            }
            File srcFile = new File(sourcePath + File.separator + tFile.getName());
            if (srcFile.exists()) {
                continue;
            }
            long len = tFile.length();
            String filename = tFile.getAbsolutePath();
            if (tFile.isDirectory()) {
                if (clearDir(tFile, true)) {
                    try {
                        FileDeleteTools.delete(tFile);
                        copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strDirectoryDeleteSuccessfully + filename);
                        }
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + filename);
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFailedDelete + filename);
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    FileDeleteTools.delete(tFile);
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFileDeleteSuccessfully + filename);
                    }
                } catch (Exception e) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + filename);
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
        }
        return true; // When return true, it is not necessary that all things are good.
    }

}
