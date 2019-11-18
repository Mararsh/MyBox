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
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
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

    protected FilesRenameTableController filesController;
    protected List<String> targetNames;
    protected int currentAccum, digit, startNumber;
    protected RenameType renameType;

    protected List<File> includeFiles;
    protected Map<String, String> currentNewNames;
    protected Map<String, Map<String, String>> newNames;

    @FXML
    protected VBox renameOptionsBox, numberBox, replaceBox;
    @FXML
    protected FlowPane suffixPane, prefixPane, extensionPane;
    @FXML
    protected CheckBox fillZeroCheck, originalCheck, stringCheck, accumCheck,
            suffixCheck, descentCheck, recountCheck;
    @FXML
    protected TextField oldStringInput, newStringInput, newExtInput,
            prefixInput, suffixInput, stringInput;
    @FXML
    protected ToggleGroup sortGroup, renameGroup;

    public static enum RenameType {
        ReplaceSubString, AppendSuffix, AppendPrefix, AddSequenceNumber,
        ChangeExtension
    }

    public FilesRenameController() {
        baseTitle = AppVariables.message("FilesRename");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            filesController = (FilesRenameTableController) tableController;

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            renameGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkRenameType();
                }
            });
            checkRenameType();

            fillZeroCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue("FileRenameFillZero", fillZeroCheck.isSelected());
                }
            });
            fillZeroCheck.setSelected(AppVariables.getUserConfigBoolean("FileRenameFillZero", true));

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(tableController.getAddFilesButton().disableProperty())
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void checkRenameType() {
        renameOptionsBox.getChildren().clear();

        RadioButton selected = (RadioButton) renameGroup.getSelectedToggle();
        if (message("ReplaceSubString").equals(selected.getText())) {
            renameType = RenameType.ReplaceSubString;
            renameOptionsBox.getChildren().addAll(replaceBox);

        } else if (message("AppendSuffix").equals(selected.getText())) {
            renameType = RenameType.AppendSuffix;
            renameOptionsBox.getChildren().addAll(suffixPane);

        } else if (message("AppendPrefix").equals(selected.getText())) {
            renameType = RenameType.AppendPrefix;
            renameOptionsBox.getChildren().addAll(prefixPane);

        } else if (message("AddSequenceNumber").equals(selected.getText())) {
            renameType = RenameType.AddSequenceNumber;
            renameOptionsBox.getChildren().addAll(numberBox);

        } else if (message("ChangeExtension").equals(selected.getText())) {
            renameType = RenameType.ChangeExtension;
            renameOptionsBox.getChildren().addAll(extensionPane);
        }
        FxmlControl.refreshStyle(renameOptionsBox);

    }

    @Override
    public boolean makeBatchParameters() {
        switch (renameType) {
            case ReplaceSubString:
                if (oldStringInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AppendPrefix:
                if (prefixInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AppendSuffix:
                if (suffixInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AddSequenceNumber:
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
                    startNumber = Integer.valueOf(acumFromInput.getText());
                } catch (Exception e) {
                    startNumber = 0;
                }
                currentAccum = startNumber;
                break;
            case ChangeExtension:

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
        showHandling(srcFile);
        FileInformation d = tableController.data(currentParameters.currentIndex);
        String newName = renameFile(srcFile);
        if (newName != null) {
            d.setData(new File(newName).getAbsolutePath());
            return AppVariables.message("Successful");
        } else {
            d.setData("");
            return AppVariables.message("Failed");
        }
    }

    protected String renameFile(File file) {
        String newName = makeNewFilename(file);
        try {
            if (newName != null) {
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (targetExistType != TargetExistType.Replace) {
                        return null;
                    }
                    newFile.delete();
                }
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
    protected boolean handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return false;
        }
        try {
            if (recountCheck.isSelected()) {
                currentAccum = startNumber;
            }
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(sourcePath.listFiles()));
            if (renameType == RenameType.AddSequenceNumber) {
                sortFiles(files);
                int bdigit = (files.size() + "").length();
                if (digit < bdigit) {
                    digit = bdigit;
                }
            }
            for (File file : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (file.isFile()) {
                    dirFilesNumber++;
                    if (!match(file)) {
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
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
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

    protected String makeNewFilename(File file) {
        try {
            if (file == null || !file.isFile()) {
                return null;
            }
            String filePath = file.getParent() + File.separator;
            String newName;
            switch (renameType) {
                case ReplaceSubString:
                    newName = file.getName().replaceAll(
                            oldStringInput.getText(), newStringInput.getText());
                    break;
                case AppendPrefix:
                    newName = prefixInput.getText() + file.getName();
                    break;
                case AppendSuffix:
                    newName = FileTools.appendName(file.getName(), suffixInput.getText());
                    break;
                case AddSequenceNumber:
                    newName = "";
                    if (originalCheck.isSelected()) {
                        newName += FileTools.getFilePrefix(file.getName());
                    }
                    if (stringCheck.isSelected()) {
                        newName += stringInput.getText();
                    }
                    String pageNumber = currentAccum + "";
                    if (fillZeroCheck.isSelected()) {
                        pageNumber = StringTools.fillLeftZero(currentAccum, digit);
                    }
                    newName += pageNumber;
                    currentAccum++;
                    newName += "." + FileTools.getFileSuffix(file.getName());
                    break;
                case ChangeExtension:
                    newName = FileTools.replaceFileSuffix(file.getName(), newExtInput.getText());
                    break;
                default:
                    return null;
            }

            return filePath + newName;
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
                String newName = f.getData();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    File oldFile = new File(originalName);
                    if (oldFile.exists()) {
                        if (targetExistType != TargetExistType.Replace) {
                            f.setHandled(AppVariables.message("FailRecovered"));
                            continue;
                        } else {
                            oldFile.delete();
                        }
                    }
                    if (newFile.renameTo(oldFile)) {
                        f.setHandled(AppVariables.message("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVariables.message("FailRecovered"));
                    }
                }
                f.setData("");
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
                    File oldFile = new File(originalFileName);

                    if (file.renameTo(oldFile)) {
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
                String newName = f.getData();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    File oldFile = new File(originalName);
                    if (oldFile.exists()) {
                        if (targetExistType != TargetExistType.Replace) {
                            f.setHandled(AppVariables.message("FailRecovered"));
                            continue;
                        } else {
                            oldFile.delete();
                        }
                    }
                    if (newFile.renameTo(oldFile)) {
                        f.setHandled(AppVariables.message("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVariables.message("FailRecovered"));
                    }
                }
                f.setData("");
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
                    File oldFile = new File(originalFileName);
                    if (oldFile.exists()) {
                        if (targetExistType != TargetExistType.Replace) {
                            f.setHandled(AppVariables.message("FailRecovered"));
                            continue;
                        } else {
                            oldFile.delete();
                        }
                    }
                    if (file.renameTo(oldFile)) {
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
    public void okAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        if (newNames != null) {
            newNames.clear();
        }
        for (FileInformation f : tableData) {
            f.setHandled("");
            String newName = f.getData();
            if (newName != null && !newName.isEmpty()) {
                f.setFileName(newName);
                f.setFile(new File(newName));
                f.setData("");
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
