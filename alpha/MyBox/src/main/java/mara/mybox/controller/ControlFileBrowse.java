package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import static mara.mybox.tools.FileSortTools.FileSortMode.ModifyTimeDesc;
import static mara.mybox.tools.FileSortTools.FileSortMode.NameAsc;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-12
 * @License Apache License Version 2.0
 */
public class ControlFileBrowse extends BaseController {

    protected FileSortMode sortMode;

    @FXML
    protected ToggleGroup sortGroup;
    @FXML
    protected RadioButton NameAscRadio, NameDescRadio, FormatAscRadio, FormatDescRadio,
            ModifyTimeDescRadio, ModifyTimeAscRadio, SizeDescRadio, SizeAscRadio,
            CreateTimeDescRadio, CreateTimeAscRadio;
    @FXML
    protected Label infoLabel;
    @FXML
    protected Button nextFileButton, previousFileButton;

    public void setParameter(BaseController parent) {
        try {
            parentController = parent;
            setFileType(parentController.getSourceFileType());
            baseName = parentController.baseName;

            String savedMode = UserConfig.getString(baseName + "SortMode", FileSortMode.NameAsc.name());
            sortMode = FileSortTools.sortMode(savedMode);
            switch (sortMode) {
                case ModifyTimeDesc:
                    ModifyTimeDescRadio.setSelected(true);
                    break;
                case ModifyTimeAsc:
                    ModifyTimeAscRadio.setSelected(true);
                    break;
                case CreateTimeDesc:
                    CreateTimeDescRadio.setSelected(true);
                    break;
                case CreateTimeAsc:
                    CreateTimeAscRadio.setSelected(true);
                    break;
                case SizeDesc:
                    SizeDescRadio.setSelected(true);
                    break;
                case SizeAsc:
                    SizeAscRadio.setSelected(true);
                    break;
                case NameDesc:
                    NameDescRadio.setSelected(true);
                    break;
                case FormatDesc:
                    FormatDescRadio.setSelected(true);
                    break;
                case FormatAsc:
                    FormatAscRadio.setSelected(true);
                    break;
                case NameAsc:
                default:
                    NameAscRadio.setSelected(true);
                    break;
            }
            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (newValue == null || isSettingValues) {
                        return;
                    }
                    checkMode();
                }
            });

            setCurrentFile(parentController.sourceFile());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkMode() {
        if (NameDescRadio.isSelected()) {
            sortMode = FileSortMode.NameDesc;
        } else if (FormatAscRadio.isSelected()) {
            sortMode = FileSortMode.FormatAsc;
        } else if (FormatDescRadio.isSelected()) {
            sortMode = FileSortMode.FormatDesc;
        } else if (ModifyTimeDescRadio.isSelected()) {
            sortMode = FileSortMode.ModifyTimeDesc;
        } else if (ModifyTimeAscRadio.isSelected()) {
            sortMode = FileSortMode.ModifyTimeAsc;
        } else if (SizeDescRadio.isSelected()) {
            sortMode = FileSortMode.SizeDesc;
        } else if (SizeAscRadio.isSelected()) {
            sortMode = FileSortMode.SizeAsc;
        } else if (CreateTimeDescRadio.isSelected()) {
            sortMode = FileSortMode.CreateTimeDesc;
        } else if (CreateTimeAscRadio.isSelected()) {
            sortMode = FileSortMode.CreateTimeAsc;
        } else {
            sortMode = FileSortMode.NameAsc;
        }
        UserConfig.setString(baseName + "SortMode", sortMode.name());
        checkStatus();
    }

    public List<File> pathFiles(File cfile) {
        try {
            if (cfile == null) {
                return null;
            }
            File path = cfile.getParentFile();
            File[] filesList = path.listFiles();
            if (filesList == null || filesList.length == 0) {
                return null;
            }
            List<File> files = new ArrayList<>();
            for (File file : filesList) {
                if (file.isFile() && FileFilters.accept(sourceExtensionFilter, file)) {
                    files.add(file);
                }
            }
            FileSortTools.sortFiles(files, sortMode);
            return files;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public void setCurrentFile(File currentfile) {
        this.sourceFile = currentfile;
        checkStatus();
    }

    public void checkStatus() {
        try {
            nextFileButton.setDisable(true);
            previousFileButton.setDisable(true);
            openSourceButton.setDisable(sourceFile == null);
            if (sourceFile == null) {
                infoLabel.setText("");
                openSourceButton.setDisable(true);
                return;
            }
            String info = message("Directory") + ": "
                    + sourceFile.getParent() + "\n";
            List<File> files = pathFiles(sourceFile);
            if (files == null || files.isEmpty()) {
                info += message("Valid") + ": 0";
            } else {
                info += message("Valid") + ": " + files.size();
                String currentName = sourceFile.getAbsolutePath();
                if (!currentName.equals(files.get(0).getAbsolutePath())) {
                    previousFileButton.setDisable(false);
                }
                if (!currentName.equals(files.get(files.size() - 1).getAbsolutePath())) {
                    nextFileButton.setDisable(false);
                }
            }
            infoLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        File file = nextFile(sourceFile);
        if (file == null) {
            popError(message("NoMore"));
            return;
        }
        parentController.sourceFileChanged(file);
    }

    public File nextFile(File cfile) {
        try {
            List<File> files = pathFiles(cfile);
            if (files != null) {
                String currentName = cfile.getAbsolutePath();
                int end = files.size() - 1;
                if (currentName.equals(files.get(end).getAbsolutePath())) {
                    return null;
                }
                for (int i = 0; i < end; i++) {
                    if (currentName.equals(files.get(i).getAbsolutePath())) {
                        return files.get(i + 1);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    @FXML
    @Override
    public void previousAction() {
        File file = previousFile(sourceFile);
        if (file == null) {
            popError(message("NoMore"));
            return;
        }
        parentController.sourceFileChanged(file);
    }

    public File previousFile(File cfile) {
        try {
            List<File> files = pathFiles(cfile);
            if (files != null) {
                String currentName = cfile.getAbsolutePath();
                if (currentName.equals(files.get(0).getAbsolutePath())) {
                    return null;
                }
                for (int i = 1; i <= files.size() - 1; i++) {
                    if (currentName.equals(files.get(i).getAbsolutePath())) {
                        return files.get(i - 1);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

}
