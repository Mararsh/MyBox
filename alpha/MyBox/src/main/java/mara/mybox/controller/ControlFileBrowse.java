package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.cell.TableTimeCell;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-12
 * @License Apache License Version 2.0
 */
public class ControlFileBrowse extends BaseController {

    protected ObservableList<FileInformation> tableData;
    protected FileSortMode sortMode;

    @FXML
    protected CheckBox listCheck;
    @FXML
    protected VBox listBox;
    @FXML
    protected TableView<FileInformation> tableView;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn, typeColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn, timeColumn;
    @FXML
    protected Label infoLabel;
    @FXML
    protected Button refreshButton;

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (tableView != null) {
                tableData = FXCollections.observableArrayList();
                tableView.setItems(tableData);

                fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("suffix"));
                sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                sizeColumn.setCellFactory(new TableFileSizeCell());
                timeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
                timeColumn.setCellFactory(new TableTimeCell());

                tableView.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getClickCount() > 1) {
                        itemDoubleClicked();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void itemDoubleClicked() {
        FileInformation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        parentController.selectSourceFile(selected.getFile());
    }

    public void setParameter(BaseController parent) {
        try {
            parentController = parent;
            setFileType(parentController.getSourceFileType());
            baseName = parentController.baseName;

            initMore();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMore() {
        sortMode = FileSortMode.NameAsc;
        listCheck.setSelected(UserConfig.getBoolean(baseName + "ListDirectoryValidFiles", true));
        listCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "ListDirectoryValidFiles", listCheck.isSelected());
                checkList();
            }
        });
        checkList();
    }

    public void checkList() {
        if (listCheck.isSelected()) {
            listBox.setDisable(false);
            sourceFile = null;
            setCurrentFile(parentController.sourceFile());
        } else {
            listBox.setDisable(true);
            tableData.clear();
            sourceFile = null;
        }
    }

    public List<File> validFiles(File cfile) {
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

    public void setCurrentFile(File file) {
        if (sourceFile == null || file == null
                || !sourceFile.getParent().equals(file.getParent())) {
            sourceFile = file;
            refreshAction();
        } else {
            sourceFile = file;
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            if (!listCheck.isSelected()) {
                return;
            }
            openSourceButton.setDisable(sourceFile == null);
            refreshButton.setDisable(sourceFile == null);
            tableData.clear();
            tableView.getSortOrder().clear();
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
                for (File file : files) {
                    tableData.add(new FileInformation(file));
                }
            }
            infoLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public File nextFile(File cfile) {
        for (int i = 0; i < tableData.size() - 1; i++) {
            if (tableData.get(i).getFile().equals(cfile)) {
                return tableData.get(i + 1).getFile();
            }
        }
        return null;
    }

    public File previousFile(File cfile) {
        for (int i = 1; i < tableData.size(); i++) {
            if (tableData.get(i).getFile().equals(cfile)) {
                return tableData.get(i - 1).getFile();
            }
        }
        return null;
    }

}
