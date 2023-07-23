package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import static mara.mybox.tools.FileSortTools.FileSortMode.ModifyTimeDesc;
import static mara.mybox.tools.FileSortTools.FileSortMode.NameAsc;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-12
 * @License Apache License Version 2.0
 */
public class ControlFilesBrowse extends ControlFileBrowse {

    protected ImagesBrowserController browserController;

    @FXML
    protected ToggleGroup sortGroup;
    @FXML
    protected RadioButton NameAscRadio, NameDescRadio, FormatAscRadio, FormatDescRadio,
            ModifyTimeDescRadio, ModifyTimeAscRadio, SizeDescRadio, SizeAscRadio,
            CreateTimeDescRadio, CreateTimeAscRadio;
    @FXML
    protected Button nextFileButton, previousFileButton;

    public void setParameter(ImagesBrowserController parent) {
        browserController = parent;
        super.setParameter(parent);
    }

    @Override
    public void initMore() {
        try {
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
            MyBoxLog.error(e);
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
        refreshAction();
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            openSourceButton.setDisable(sourceFile == null);
            nextFileButton.setDisable(true);
            previousFileButton.setDisable(true);
            if (sourceFile == null) {
                infoLabel.setText("");
                return;
            }
            String info = message("Directory") + ": "
                    + sourceFile.getParent() + "\n";
            List<File> files = validFiles(sourceFile);
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
            MyBoxLog.debug(e);
        }
    }

    protected List<File> nextFiles(File file, int filesNumber) {
        if (file == null || filesNumber <= 0) {
            return null;
        }
        try {
            List<File> pathFiles = validFiles(file);
            int total = pathFiles.size();
            if (total <= 0) {
                return null;
            }
            List<File> files = new ArrayList<>();
            int pos = pathFiles.indexOf(file);
            if (pos >= total - 1) {
                return null;
            }
            if (pos < 0) {
                pos = 0;
            }
            int start = pos + 1;
            int end = Math.min(start + filesNumber, total);
            for (int i = start; i < end; ++i) {
                files.add(pathFiles.get(i));
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected List<File> previousFiles(File file, int filesNumber) {
        if (file == null || filesNumber <= 0) {
            return null;
        }
        try {
            List<File> pathFiles = validFiles(file);
            int total = pathFiles.size();
            if (total <= 0) {
                return null;
            }
            List<File> files = new ArrayList<>();
            int pos = pathFiles.indexOf(file);
            if (pos == 0) {
                return null;
            }
            if (pos < 0) {
                pos = total;
            }
            int end = pos;
            int start = Math.max(end - filesNumber, 0);
            for (int i = start; i < end; ++i) {
                files.add(pathFiles.get(i));
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    public void nextAction() {
        ObservableList<File> imageFileList = browserController.imageFileList;
        if (imageFileList == null || imageFileList.isEmpty()) {
            nextFileButton.setDisable(true);
            previousFileButton.setDisable(true);
            return;
        }
        List<File> files = nextFiles(imageFileList.get(imageFileList.size() - 1), browserController.filesNumber);
        if (files == null || files.isEmpty()) {
            popError(message("NoMore"));
            nextFileButton.setDisable(true);
        } else {
            browserController.loadImages(files, browserController.colsNum);
            previousFileButton.setDisable(false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        ObservableList<File> imageFileList = browserController.imageFileList;
        if (imageFileList == null || imageFileList.isEmpty()) {
            nextFileButton.setDisable(true);
            previousFileButton.setDisable(true);
            return;
        }
        List<File> files = previousFiles(imageFileList.get(0), browserController.filesNumber);
        if (files == null || files.isEmpty()) {
            popError(message("NoMore"));
            previousFileButton.setDisable(true);
        } else {
            browserController.loadImages(files, browserController.colsNum);
            nextFileButton.setDisable(false);
        }
    }

}
