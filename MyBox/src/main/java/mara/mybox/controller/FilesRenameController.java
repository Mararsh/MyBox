package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortMode;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameController extends FilesBatchController {

    protected String FileNameFillZeroKey;
    protected FilesRenameTableController filesController;
    protected List<String> targetNames;
    protected int currentAccum, digit;

    protected List<File> includeFiles;
    protected Map<String, String> currentNewNames;
    protected Map<String, Map<String, String>> newNames;

    @FXML
    protected CheckBox fillZeroCheck, originalCheck, stringCheck, accumCheck, suffixCheck, descentCheck;
    @FXML
    protected TextField stringInput, suffixInput;
    @FXML
    protected ToggleGroup sortGroup;

    public FilesRenameController() {
        baseTitle = AppVariables.message("FilesRename");

//        targetPathKey = "FileTargetPath";
//        sourcePathKey = "FileSourcePath";
        FileNameFillZeroKey = "FileNameFillZeroKey";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            filesController = (FilesRenameTableController) tableController;

            fillZeroCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(FileNameFillZeroKey, fillZeroCheck.isSelected());
                }
            });
            fillZeroCheck.setSelected(AppVariables.getUserConfigBoolean(FileNameFillZeroKey));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            suffixCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    suffixInput.setDisable(!suffixCheck.isSelected());
                }
            });
            suffixInput.setDisable(!suffixCheck.isSelected());

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(tableController.getAddFilesButton().disableProperty())
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeBatchParameters() {
        if (isPreview) {
            digit = 1;
        } else {
            sortFileInformations(tableData);
            try {
                digit = Integer.valueOf(digitInput.getText());
            } catch (Exception e) {
                if (tableController.getTotalFilesNumber() <= 0) {
                    tableController.countTotal();
                }
                digit = (tableController.getTotalFilesNumber() + "").length();
            }
        }
        try {
            currentAccum = Integer.valueOf(acumFromInput.getText());
        } catch (Exception e) {
            currentAccum = 0;
        }

        newNames = new HashMap<>();
        return super.makeBatchParameters();

    }

    protected FileSortMode checkSortMode() {
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        boolean desc = descentCheck.isSelected();
        FileSortMode sortMode = FileSortMode.ModifyTimeDesc;
        if (message("OriginalFileName").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.NameDesc;
            } else {
                sortMode = FileSortMode.NameAsc;
            }
        } else if (message("CreateTime").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.CreateTimeDesc;
            } else {
                sortMode = FileSortMode.CreateTimeAsc;
            }

        } else if (message("ModifyTime").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.ModifyTimeDesc;
            } else {
                sortMode = FileSortMode.ModifyTimeAsc;
            }

        } else if (message("Size").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.SizeDesc;
            } else {
                sortMode = FileSortMode.SizeAsc;
            }

        } else if (message("AddedSequence").equals(sort.getText())) {
            sortMode = null;

        }
        return sortMode;

    }

    protected void sortFileInformations(List<FileInformation> files) {
        FileSortMode sortMode = checkSortMode();
        if (sortMode != null) {
            FileTools.sortFileInformations(files, sortMode);
        }
    }

    protected void sortFiles(List<File> files) {
        FileSortMode sortMode = checkSortMode();
        if (sortMode != null) {
            FileTools.sortFiles(files, sortMode);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        FileInformation d = tableController.data(currentParameters.currentIndex);
        String newName = renameFile(srcFile);
        if (newName != null) {
            d.setNewName(new File(newName).getAbsolutePath());
            return AppVariables.message("Successful");
        } else {
            d.setNewName("");
            return AppVariables.message("Failed");
        }
    }

    protected String renameFile(File file) {
        String newName = makeFilename(file);
        try {
            if (newName != null) {
                File newFile = new File(newName);
                if (file.renameTo(newFile)) {
                    newName = newFile.getAbsolutePath();
                    currentParameters.finalTargetName = newName;
                    targetFiles.add(newFile);
                    return newName;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public String handleDirectory(File dir) {
        currentNewNames = new HashMap<>();
        String result = super.handleDirectory(dir);
        newNames.put(dir.getAbsolutePath(), currentNewNames);
        return result;
    }

    @Override
    protected void handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return;
        }
        try {
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(sourcePath.listFiles()));
            sortFiles(files);
            int bdigit = (files.size() + "").length();
            if (digit < bdigit) {
                digit = bdigit;
            }

            for (File file : files) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                if (file.isFile()) {
                    dirFilesNumber++;
                    if (!matchFilters(file)) {
                        continue;
                    }
                    String originalName = file.getAbsolutePath();
                    String newName = renameFile(file);
                    if (newName != null) {
                        dirFilesHandled++;
                        currentNewNames.put(newName, originalName);
                    }
                } else if (file.isDirectory() && sourceCheckSubdir) {
                    handleDirectory(file, null);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void donePost() {
        super.donePost();
        filesController.setButtonsAfterRenamed();
        tableView.refresh();
    }

    @Override
    public void viewTarget(File file) {
        openTarget(null);
    }

    protected String makeFilename(File file) {
        try {
            if (file == null || !file.isFile()) {
                return null;
            }
            String filename = file.getParent() + "/";
            if (originalCheck.isSelected()) {
                filename += FileTools.getFilePrefix(file.getName());
            }
            if (stringCheck.isSelected()) {
                filename += stringInput.getText();
            }
            if (accumCheck.isSelected()) {
                String pageNumber = currentAccum + "";
                if (fillZeroCheck.isSelected()) {
                    pageNumber = StringTools.fillLeftZero(currentAccum, digit);
                }
                filename += pageNumber;
                currentAccum++;
            }
            if (suffixCheck.isSelected()) {
                filename += "." + suffixInput.getText();
            } else {
                filename += "." + FileTools.getFileSuffix(file.getName());
            }
            return filename;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    protected void recoveryAllAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }

        for (FileInformation f : tableData) {
            String originalName = f.getFileName();
            if (message("File").equals(f.getFileType())) {
                String newName = f.getNewName();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (newFile.renameTo(new File(originalName))) {
                        f.setHandled(AppVariables.message("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVariables.message("FailRecovered"));
                    }
                }
                f.setNewName("");
            } else if (message("Directory").equals(f.getFileType())) {
                currentNewNames = newNames.get(originalName);
                if (currentNewNames == null) {
                    continue;
                }
                int recovered = 0;
                for (String name : currentNewNames.keySet()) {
                    String originalFileName = currentNewNames.get(name);
                    File file = new File(name);
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.renameTo(new File(originalFileName))) {
                        recovered++;
                    }
                }
                f.setHandled(MessageFormat.format(AppVariables.message("DirRecoverSummary"),
                        currentNewNames.size(), recovered));
                newNames.remove(originalName);
            }
        }

    }

    protected void recoverySelectedAction() {
        ObservableList<FileInformation> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation f : selected) {
            String originalName = f.getFileName();
            if (message("File").equals(f.getFileType())) {
                String newName = f.getNewName();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (newFile.renameTo(new File(originalName))) {
                        f.setHandled(AppVariables.message("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVariables.message("FailRecovered"));
                    }
                }
                f.setNewName("");
            } else if (message("Directory").equals(f.getFileType())) {
                currentNewNames = newNames.get(originalName);
                if (currentNewNames == null) {
                    continue;
                }
                int recovered = 0;
                for (String newName : currentNewNames.keySet()) {
                    String originalFileName = currentNewNames.get(newName);
                    File file = new File(newName);
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.renameTo(new File(originalFileName))) {
                        recovered++;
                    }
                }
                f.setHandled(MessageFormat.format(AppVariables.message("DirRecoverSummary"),
                        currentNewNames.size(), recovered));
                newNames.remove(originalName);
            }
        }

    }

    @Override
    public void openTarget(ActionEvent event) {
        try {
            if (tableData == null || tableData.isEmpty()) {
                return;
            }
            File f = new File(tableData.get(0).getFileName());
            if (f.isDirectory()) {
                browseURI(new File(f.getPath()).toURI());
            } else {
                browseURI(new File(f.getParent()).toURI());
            }
            recordFileOpened(f);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
