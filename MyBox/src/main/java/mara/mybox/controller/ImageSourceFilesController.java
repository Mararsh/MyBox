package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSourceFilesController extends ImageBaseController {

    @FXML
    protected TableView<FileInformation> sourceTable;
    @FXML
    protected TableColumn<FileInformation, String> handledColumn, fileColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    private Button addButton, upButton, downButton, deleteButton, clearButton;

    public ImageSourceFilesController() {
        sourceFilesInformation = FXCollections.observableArrayList();
    }

    @Override
    protected void initializeNext() {

        try {

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("createTime"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileSize"));

            sourceTable.setItems(sourceFilesInformation);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    protected void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setConfigValue(LastPathKey, path);
            AppVaribles.setConfigValue(sourcePathKey, path);
            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            sourceFilesInformation.addAll(infos);
            sourceTable.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    protected void deleteAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            sourceFilesInformation.remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    protected void clearAction(ActionEvent event) {
        sourceFilesInformation.clear();
        sourceTable.refresh();
    }

    @FXML
    protected void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            showImageView(info.getFile().getAbsolutePath());
        }
    }

    @FXML
    protected void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index - 1));
            sourceFilesInformation.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                sourceTable.getSelectionModel().select(index - 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    protected void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index + 1));
            sourceFilesInformation.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < sourceFilesInformation.size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    public TableView<FileInformation> getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(TableView<FileInformation> sourceTable) {
        this.sourceTable = sourceTable;
    }

    public TableColumn<FileInformation, String> getHandledColumn() {
        return handledColumn;
    }

    public void setHandledColumn(TableColumn<FileInformation, String> handledColumn) {
        this.handledColumn = handledColumn;
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

    public TableColumn<FileInformation, String> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<FileInformation, String> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public TableColumn<FileInformation, String> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<FileInformation, String> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public Button getAddButton() {
        return addButton;
    }

    public void setAddButton(Button addButton) {
        this.addButton = addButton;
    }

    public Button getUpButton() {
        return upButton;
    }

    public void setUpButton(Button upButton) {
        this.upButton = upButton;
    }

    public Button getDownButton() {
        return downButton;
    }

    public void setDownButton(Button downButton) {
        this.downButton = downButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public void setClearButton(Button clearButton) {
        this.clearButton = clearButton;
    }

}
