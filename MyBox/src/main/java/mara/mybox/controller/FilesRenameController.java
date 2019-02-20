package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileInformation;

import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortType;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameController extends FilesBatchController {

    protected List<String> targetNames;
    protected int currentAccum, digit;

    protected List<File> includeFiles;
    protected int dirFiles, dirRenamed;
    protected File currentDir;
    protected Map<String, String> currentNewNames;
    protected Map<String, Map<String, String>> newNames;

    @FXML
    protected TableColumn<FileInformation, String> newColumn;
    @FXML
    protected Button recoveryAllButton, recoverySelectedButton;
    @FXML
    protected CheckBox originalCheck, stringCheck, accumCheck, suffixCheck, descentCheck;
    @FXML
    protected TextField stringInput, suffixInput;
    @FXML
    protected ToggleGroup sortGroup;

    public FilesRenameController() {
        targetPathKey = "FileTargetPath";
        sourcePathKey = "FileSourcePath";

        fileExtensionFilter = new ArrayList();
        fileExtensionFilter.add(new FileChooser.ExtensionFilter("*", "*.*"));
    }

    @Override
    protected void initSourceSection() {
        try {
            super.initSourceSection();

            newColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("newName"));

            sourceFilesInformation.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    checkDataChanged();
                }
            });
            checkDataChanged();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDataChanged() {
        digitInput.setText((sourceFilesInformation.size() + "").length() + "");
        if (sourceFilesInformation.isEmpty()) {
            recoverySelectedButton.setDisable(true);
            recoveryAllButton.setDisable(true);
            insertFilesButton.setDisable(true);
            insertDirectoryButton.setDisable(true);
            upButton.setDisable(true);
            downButton.setDisable(true);
            deleteButton.setDisable(true);
            clearButton.setDisable(true);
        } else {
            clearButton.setDisable(false);
        }
    }

    @Override
    protected void checkTableSelected() {
        super.checkTableSelected();
        ObservableList<Integer> selected = sourceTableView.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());
        if (none) {
            recoverySelectedButton.setDisable(true);
            recoveryAllButton.setDisable(true);
        }
    }

    @Override
    protected void initTargetSection() {

        suffixCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                suffixInput.setDisable(!suffixCheck.isSelected());
            }
        });
        suffixInput.setDisable(!suffixCheck.isSelected());

        operationBarController.startButton.disableProperty().bind(
                Bindings.isEmpty(sourceFilesInformation)
                        .or(addFilesButton.disableProperty())
        );

    }

    @FXML
    @Override
    protected void clearAction(ActionEvent event) {
        sourceFilesInformation.clear();
        sourceTableView.refresh();
        addFilesButton.setDisable(false);
        addDirectoryButton.setDisable(false);
    }

    @Override
    protected void makeActualParameters() {
        if (actualParameters != null && paused) {
            actualParameters.startIndex = actualParameters.currentIndex;
            return;
        }

        actualParameters = new ProcessParameters();
        actualParameters.currentIndex = 0;
        sourcesHandling = new ArrayList();
        if (sourceFilesInformation != null && !sourceFilesInformation.isEmpty()) {
            if (isPreview) {
                int index = 0;
                ObservableList<Integer> selected = sourceTableView.getSelectionModel().getSelectedIndices();
                if (selected != null && !selected.isEmpty()) {
                    index = selected.get(0);
                }
                sourcesHandling.add(index);
                digit = 1;
                digitInput.setText(digit + "");
            } else {
                sortFileInformations(sourceFilesInformation);
                for (int i = 0; i < sourceFilesInformation.size(); i++) {
                    sourcesHandling.add(i);
                }
                int bdigit = (sourcesHandling.size() + "").length();
                try {
                    digit = Integer.valueOf(digitInput.getText());
                    if (digit < bdigit) {
                        digit = bdigit;
                    }
                } catch (Exception e) {
                    digit = bdigit;
                }
                digitInput.setText(digit + "");
            }
        }
        newNames = new HashMap();

    }

    protected void sortFileInformations(List<FileInformation> files) {
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFileInformations(files, FileSortType.FileName);
        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFileInformations(files, FileSortType.CreateTime);
        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFileInformations(files, FileSortType.ModifyTime);
        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFileInformations(files, FileSortType.Size);
        }
        if (descentCheck.isSelected()) {
            Collections.reverse(files);
        }
    }

    protected void sortFiles(List<File> files) {
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.FileName);
        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.CreateTime);
        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.ModifyTime);
        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.Size);
        }
        if (descentCheck.isSelected()) {
            Collections.reverse(files);
        }
    }

    @Override
    protected String handleCurrentFile(FileInformation d) {
        File file = d.getFile();
        currentParameters.sourceFile = file;

        String newName = renameFile(file);
        if (newName != null) {
            d.setNewName(new File(newName).getAbsolutePath());
            return AppVaribles.getMessage("Successful");
        } else {
            d.setNewName("");
            return AppVaribles.getMessage("Failed");
        }
    }

    protected String renameFile(File file) {
        String newName = makeFilename(file);
        try {
            if (newName != null) {
                if (file.renameTo(new File(newName))) {
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
    protected String handleCurrentDirectory(FileInformation d) {
        try {
            currentDir = d.getFile();
            dirFiles = dirRenamed = 0;
            currentNewNames = new HashMap();
            String[] filters = null;
            if (filesNameCheck.isSelected()) {
                filters = filesNameInput.getText().trim().split("\\s+");
                if (filters.length == 0) {
                    filters = null;
                }
            }
            renameDirectory(currentDir, filters, subDirCheck.isSelected());
            newNames.put(currentDir.getAbsolutePath(), currentNewNames);
            return MessageFormat.format(AppVaribles.getMessage("DirRenameSummary"), dirFiles, dirRenamed);
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }

    protected void renameDirectory(File dir, String[] filters, boolean checkSub) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        try {
            try {
                currentAccum = Integer.valueOf(acumFromInput.getText());
            } catch (Exception e) {
                currentAccum = 0;
            }
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(dir.listFiles()));
            sortFiles(files);

            int bdigit = (files.size() + "").length();
            try {
                digit = Integer.valueOf(digitInput.getText());
                if (digit < bdigit) {
                    digit = bdigit;
                }
            } catch (Exception e) {
                digit = bdigit;
            }

            for (File file : files) {
                if (file.isFile()) {
                    dirFiles++;
                    String originalName = file.getAbsolutePath();
                    if (filters != null) {
                        boolean isValid = false;
                        for (String name : filters) {
                            if (FileTools.getFileName(originalName).contains(name)) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    }
                    String newName = renameFile(file);
                    if (newName != null) {
                        dirRenamed++;
                        currentNewNames.put(newName, originalName);
                    }
                } else if (checkSub) {
                    renameDirectory(file, filters, checkSub);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void donePost() {
        addFilesButton.setDisable(true);
        addDirectoryButton.setDisable(true);
        insertFilesButton.setDisable(true);
        insertDirectoryButton.setDisable(true);
        deleteButton.setDisable(true);
        recoveryAllButton.setDisable(false);
        recoverySelectedButton.setDisable(false);
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
                if (fillZero.isSelected()) {
                    pageNumber = ValueTools.fillLeftZero(currentAccum, digit);
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

    @FXML
    protected void recoveryAllAction(ActionEvent event) {
        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
            return;
        }
        for (FileInformation f : sourceFilesInformation) {
            String originalName = f.getFileName();
            if (f.getIsFile()) {
                String newName = f.getNewName();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (newFile.renameTo(new File(originalName))) {
                        f.setHandled(AppVaribles.getMessage("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVaribles.getMessage("FailRecovered"));
                    }
                }
                f.setNewName("");
            } else {
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
                f.setHandled(MessageFormat.format(AppVaribles.getMessage("DirRecoverSummary"),
                        currentNewNames.size(), recovered));
                newNames.remove(originalName);
            }
        }
        sourceTableView.refresh();
        addFilesButton.setDisable(false);
        addDirectoryButton.setDisable(false);
        insertFilesButton.setDisable(false);
        insertDirectoryButton.setDisable(false);
        deleteButton.setDisable(false);
        upButton.setDisable(false);
        downButton.setDisable(false);
        recoverySelectedButton.setDisable(true);
        recoveryAllButton.setDisable(true);
    }

    @FXML
    protected void recoverySelectedAction(ActionEvent event) {
        ObservableList<FileInformation> selected = sourceTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation f : selected) {
            String originalName = f.getFileName();
            if (f.getIsFile()) {
                String newName = f.getNewName();
                if (newName == null || newName.trim().isEmpty()) {
                    continue;
                }
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (newFile.renameTo(new File(originalName))) {
                        f.setHandled(AppVaribles.getMessage("Recovered"));
                        f.setFileName(originalName);
                    } else {
                        f.setHandled(AppVaribles.getMessage("FailRecovered"));
                    }
                }
                f.setNewName("");
            } else {
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
                f.setHandled(MessageFormat.format(AppVaribles.getMessage("DirRecoverSummary"),
                        currentNewNames.size(), recovered));
                newNames.remove(originalName);
            }
        }
        sourceTableView.refresh();
        addFilesButton.setDisable(true);
        addDirectoryButton.setDisable(true);
        insertFilesButton.setDisable(true);
        insertDirectoryButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @Override
    protected void openTarget(ActionEvent event) {
        try {
            if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
                return;
            }
            File f = new File(sourceFilesInformation.get(0).getFileName());
            if (f.isDirectory()) {
                Desktop.getDesktop().browse(new File(f.getPath()).toURI());
            } else {
                Desktop.getDesktop().browse(new File(f.getParent()).toURI());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
