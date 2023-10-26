package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.cell.TableTimeCell;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-12
 * @License Apache License Version 2.0
 */
public class FileBrowseController extends BaseController {

    protected ObservableList<FileInformation> tableData;
    protected FileSortMode sortMode;

    @FXML
    protected CheckBox newCheck;
    @FXML
    protected TableView<FileInformation> tableView;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn, typeColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn, timeColumn;
    @FXML
    protected Label topLabel;
    @FXML
    protected Button refreshButton;

    public FileBrowseController() {
        baseTitle = message("BrowseFiles");
    }

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
            MyBoxLog.error(e);
        }
    }

    public void setParameter(BaseController parent) {
        try {
            parentController = parent;
            sortMode = FileSortMode.NameAsc;

            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            if (parentController != null) {
                setFileType(parentController.getSourceFileType());
                baseName = parentController.baseName;
                sourceFile = parentController.sourceFile();
            }
            if (sourceFile == null) {
                topLabel.setText("");
                bottomLabel.setText("");
                openSourceButton.setDisable(true);
                refreshButton.setDisable(true);
                return;
            }
            openSourceButton.setDisable(false);
            refreshButton.setDisable(false);
            tableData.clear();
            tableView.getSortOrder().clear();

            topLabel.setText(message("Directory") + ": " + sourceFile.getParent());
            List<File> files = validFiles(sourceFile);
            String info = message("Total") + ": ";
            if (files == null || files.isEmpty()) {
                info += "0";
            } else {
                info += files.size();
                for (File file : files) {
                    tableData.add(new FileInformation(file));
                }
                info += "  " + message("DoubleClickToOpen");
            }
            bottomLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    public void itemDoubleClicked() {
        FileInformation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (parentController == null || newCheck.isSelected()) {
            ControllerTools.openTarget(selected.getFile().getAbsolutePath());
        } else {
            parentController.selectSourceFile(selected.getFile());
        }
    }

    /*
        static
     */
    public static FileBrowseController open(BaseController parent) {
        try {
            if (parent == null) {
                return null;
            }
            FileBrowseController controller
                    = (FileBrowseController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.FileBrowseFxml, false);
            if (controller != null) {
                controller.setParameter(parent);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
