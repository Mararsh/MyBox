/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.FileInformation;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class FilesTableController extends BaseController {

    protected List<FileChooser.ExtensionFilter> fileExtensionFilter;
    protected ObservableList<FileInformation> tableData = FXCollections.observableArrayList();
    protected String configPathName;

    @FXML
    private Pane filesTablePane;
    @FXML
    private Button addButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<FileInformation> filesTableView;
    @FXML
    private TableColumn<FileInformation, String> fileColumn;
    @FXML
    private TableColumn<FileInformation, String> modifyTimeColumn;
    @FXML
    private TableColumn<FileInformation, String> createTimeColumn;
    @FXML
    private TableColumn<FileInformation, String> typeColumn;
    @FXML
    private TableColumn<FileInformation, Long> sizeColumn;

    @Override
    protected void initializeNext() {
        try {
            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));

            fileColumn.setCellValueFactory(new PropertyValueFactory("fileName"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory("createTime"));
            typeColumn.setCellValueFactory(new PropertyValueFactory("fileType"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory("fileSize"));

            filesTableView.setItems(tableData);
            filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            String defaultPath = AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"));
            if (configPathName != null) {
                defaultPath = AppVaribles.getConfigValue(configPathName, defaultPath);
            }
            fileChooser.setInitialDirectory(new File(defaultPath));
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files.size() > 0) {
                String path = files.get(0).getParent();
                AppVaribles.setConfigValue("LastPath", path);
                if (configPathName != null) {
                    AppVaribles.setConfigValue(configPathName, path);
                }
            }
            for (File file : files) {
                if (findData(file.getAbsolutePath()) != null) {
                    continue;
                }
                FileInformation newFile = new FileInformation(file);
                tableData.add(newFile);
            }

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    void clearAction(ActionEvent event) {
        tableData.clear();
    }

    @FXML
    void deleteAction(ActionEvent event) {
        ObservableList<FileInformation> selected = filesTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation d : selected) {
            tableData.remove(d);
        }
    }

    private FileInformation findData(String filename) {
        for (FileInformation d : tableData) {
            if (d.getFileName().equals(filename)) {
                return d;
            }
        }
        return null;
    }

    public ObservableList<FileInformation> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<FileInformation> tableData) {
        this.tableData = tableData;
    }

    public List<FileChooser.ExtensionFilter> getFileExtensionFilter() {
        return fileExtensionFilter;
    }

    public void setFileExtensionFilter(List<FileChooser.ExtensionFilter> fileExtensionFilter) {
        this.fileExtensionFilter = fileExtensionFilter;
    }

    public Pane getFilesTablePane() {
        return filesTablePane;
    }

    public void setFilesTablePane(Pane filesTablePane) {
        this.filesTablePane = filesTablePane;
    }

    public Button getAddButton() {
        return addButton;
    }

    public void setAddButton(Button addButton) {
        this.addButton = addButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public void setClearButton(Button clearButton) {
        this.clearButton = clearButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public TableView<FileInformation> getFilesTableView() {
        return filesTableView;
    }

    public void setFilesTableView(TableView<FileInformation> filesTableView) {
        this.filesTableView = filesTableView;
    }

    public TableColumn<FileInformation, String> getFileColumn() {
        return fileColumn;
    }

    public void setFileColumn(TableColumn<FileInformation, String> fileColumn) {
        this.fileColumn = fileColumn;
    }

    public TableColumn<FileInformation, String> getModifyTimeColumn() {
        return modifyTimeColumn;
    }

    public void setModifyTimeColumn(TableColumn<FileInformation, String> modifyTimeColumn) {
        this.modifyTimeColumn = modifyTimeColumn;
    }

    public TableColumn<FileInformation, String> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<FileInformation, String> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public TableColumn<FileInformation, String> getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(TableColumn<FileInformation, String> typeColumn) {
        this.typeColumn = typeColumn;
    }

    public TableColumn<FileInformation, Long> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<FileInformation, Long> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public String getConfigPathName() {
        return configPathName;
    }

    public void setConfigPathName(String configPathName) {
        this.configPathName = configPathName;
    }

}
