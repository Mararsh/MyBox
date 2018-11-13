/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfCompressImagesBatchController extends PdfCompressImagesController {

    @FXML
    private TableView<FileInformation> sourceTable;
    @FXML
    private TableColumn<FileInformation, String> fileColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    protected Button addButton, clearButton, openButton, deleteButton, upButton, downButton;

    public PdfCompressImagesBatchController() {

    }

    @Override
    protected void initializeNext2() {
        try {
            allowPaused = false;

            initSourceSection();
            initOptionsSection();
            initTargetSection();

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(jpegBox.styleProperty().isEqualTo(badStyle))
                            .or(thresholdInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void initSourceSection() {
        try {
            sourceFilesInformation = FXCollections.observableArrayList();

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
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkTableSelected() {
        ObservableList<FileInformation> selected = sourceTable.getSelectionModel().getSelectedItems();
        if (selected != null && selected.size() > 0) {
            openButton.setDisable(false);
            upButton.setDisable(false);
            downButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            openButton.setDisable(true);
            upButton.setDisable(true);
            downButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @Override
    protected void initTargetSection() {
        targetPathInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isDirectory()) {
                        targetPathInput.setStyle(badStyle);
                        return;
                    }
                    targetPathInput.setStyle(null);
                    AppVaribles.setConfigValue(targetPathKey, file.getPath());
                    targetPath = file;
                    targetPathChanged();
                } catch (Exception e) {
                }
            }
        });
        targetPathInput.setText(AppVaribles.getConfigValue(targetPathKey, CommonValues.UserFilePath));
    }

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(PdfCompressImagesSourcePathKey, CommonValues.UserFilePath));
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
            AppVaribles.setConfigValue(PdfCompressImagesSourcePathKey, path);
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
    private void deleteAction(ActionEvent event) {
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
    private void clearAction(ActionEvent event) {
        sourceFilesInformation.clear();
        sourceTable.refresh();
    }

    @FXML
    private void openAction() {
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
            try {
                Desktop.getDesktop().browse(info.getFile().toURI());
            } catch (Exception e) {

            }

        }
    }

    @FXML
    private void upAction(ActionEvent event) {
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
    private void downAction(ActionEvent event) {
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

    @Override
    protected void makeMoreParameters() {
        actualParameters.isBatch = true;

        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
            actualParameters = null;
            return;
        }
        sourceFiles = new ArrayList();
        for (FileInformation f : sourceFilesInformation) {
            sourceFiles.add(new File(f.getFileName()));
        }

//        actualParameters.sourceFile = new File(sourceFilesInformation.get(0).getFileName());
        actualParameters.fromPage = 0;
        actualParameters.toPage = 100;
        actualParameters.acumFrom = 0;
        actualParameters.currentNameNumber = 0;
        actualParameters.password = "";
        actualParameters.startPage = 0;
        actualParameters.acumStart = 0;
        actualParameters.acumDigit = 0;

    }

    @FXML
    @Override
    protected void openTarget(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(targetPath.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
