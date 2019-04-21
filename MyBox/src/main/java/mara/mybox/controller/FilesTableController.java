package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mara.mybox.controller.base.BaseController;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FileSizeCell;
import mara.mybox.fxml.FileTimeCell;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-7-6
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesTableController extends BaseController {

    public ObservableList<FileInformation> sourceFilesInformation = FXCollections.observableArrayList();

    @FXML
    public Pane filesTablePane;
    @FXML
    public Button addFilesButton, clearButton, upButton, downButton, insertFilesButton, openButton,
            addDirectoryButton, insertDirectoryButton;
    @FXML
    public TableView<FileInformation> filesTableView;
    @FXML
    public TableColumn<FileInformation, String> handledColumn, fileColumn;
    @FXML
    public TableColumn<FileInformation, Long> sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    public TableColumn<FileInformation, Boolean> typeColumn;

    @Override
    public void initializeNext() {
        try {

            initSourceSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initSourceSection() {
        try {

            if (filesTableView == null) {
                return;
            }

            sourceFilesInformation = FXCollections.observableArrayList();

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Long>("modifyTime"));
            modifyTimeColumn.setCellFactory(new Callback<TableColumn<FileInformation, Long>, TableCell<FileInformation, Long>>() {
                @Override
                public TableCell<FileInformation, Long> call(TableColumn<FileInformation, Long> param) {
                    return new FileTimeCell();
                }
            });
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Long>("createTime"));
            createTimeColumn.setCellFactory(new Callback<TableColumn<FileInformation, Long>, TableCell<FileInformation, Long>>() {
                @Override
                public TableCell<FileInformation, Long> call(TableColumn<FileInformation, Long> param) {
                    return new FileTimeCell();
                }
            });
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Boolean>("isFile"));
                typeColumn.setCellFactory(new Callback<TableColumn<FileInformation, Boolean>, TableCell<FileInformation, Boolean>>() {
                    @Override
                    public TableCell<FileInformation, Boolean> call(TableColumn<FileInformation, Boolean> param) {
                        TableCell<FileInformation, Boolean> cell = new TableCell<FileInformation, Boolean>() {
                            final Text text = new Text();

                            @Override
                            public void updateItem(final Boolean item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    if (item) {
                                        text.setText(AppVaribles.getMessage("File"));
                                    } else {
                                        text.setText(AppVaribles.getMessage("Directory"));
                                    }
                                }
                                setGraphic(text);
                            }
                        };
                        return cell;
                    }
                });
            }
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Long>("fileSize"));
            sizeColumn.setCellFactory(new Callback<TableColumn<FileInformation, Long>, TableCell<FileInformation, Long>>() {
                @Override
                public TableCell<FileInformation, Long> call(TableColumn<FileInformation, Long> param) {
                    return new FileSizeCell();
                }
            });

            filesTableView.setItems(sourceFilesInformation);
            filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            filesTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });
            filesTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });

            sourceFilesInformation.addListener(new ListChangeListener<FileInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends FileInformation> change) {
                    long size = 0;
                    for (FileInformation source : sourceFilesInformation) {
                        size += source.getFile().length();
                    }
                    String s = AppVaribles.getMessage("FilesNumber") + ": " + sourceFilesInformation.size() + " "
                            + AppVaribles.getMessage("TotalSize") + ": " + FileTools.showFileSize(size);
                    if (bottomLabel != null) {
                        bottomLabel.setText(s);
                    }
                    if (parentController != null && parentController.bottomLabel != null) {
                        parentController.bottomLabel.setText(s);
                    }
                }
            });

            checkTableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkTableSelected() {
        ObservableList<Integer> selected = filesTableView.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());
        if (insertFilesButton != null) {
            insertFilesButton.setDisable(none);
        }

        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (openButton != null) {
            openButton.setDisable(none);
        }
        upButton.setDisable(none);
        downButton.setDisable(none);
        deleteButton.setDisable(none);
    }

    @FXML
    public void addFilesAction(ActionEvent event) {
        addFiles(sourceFilesInformation.size());
    }

    public void addFiles(int index) {
        File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
        addFilesUnderPath(index, defaultPath);
    }

    public void addFilesUnderPath(int index, File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            addFiles(index, files);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void addFile(File file) {
        addFiles(sourceFilesInformation.size(), file);
    }

    public void addFiles(int index, File file) {
        if (file == null) {
            return;
        }
        List<File> files = new ArrayList();
        files.add(file);
        addFiles(index, files);
    }

    public void addFiles(int index, List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileAdded(files.get(0));

            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            if (infos.isEmpty()) {
                return;
            }
            if (index < 0 || index >= sourceFilesInformation.size()) {
                sourceFilesInformation.addAll(infos);
            } else {
                sourceFilesInformation.addAll(index, infos);
            }
            filesTableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void insertFilesAction(ActionEvent event) {
        int index = filesTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFiles(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = filesTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFiles(index, file);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void addDirectoryAction(ActionEvent event) {
        addDirectoryAction(sourceFilesInformation.size());
    }

    @FXML
    public void insertDirectoryAction(ActionEvent event) {
        if (insertDirectoryButton == null) {
            return;
        }
        int index = filesTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectoryAction(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    public void addDirectoryAction(int index) {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath != null) {
                dirChooser.setInitialDirectory(defaultPath);
            }
            File directory = dirChooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            addDirectory(index, directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void addDirectory(int index, File directory) {
        try {
            recordFileOpened(directory);

            FileInformation d = new FileInformation(directory);
            if (index < 0 || index >= sourceFilesInformation.size()) {
                sourceFilesInformation.add(d);
            } else {
                sourceFilesInformation.add(index, d);
            }
            filesTableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void addDirectory(File directory) {
        addDirectory(sourceFilesInformation.size(), directory);
    }

    @Override
    public void insertDirectory(File directory) {
        int index = filesTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index, directory);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(filesTableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            if (info.getNewName() != null && !info.getNewName().isEmpty()) {
                FxmlStage.openTarget(getClass(), myStage, info.getNewName());
            } else {
                FxmlStage.openTarget(getClass(), myStage, info.getFile().getAbsolutePath());
            }
        }
    }

    @FXML
    public void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(filesTableView.getSelectionModel().getSelectedIndices());
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
//            filesTableView.getSelectionModel().select(index - 1);
        }
        for (Integer index : selected) {
            if (index > 0) {
                filesTableView.getSelectionModel().select(index - 1);
            }
        }
        filesTableView.refresh();
    }

    @FXML
    public void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(filesTableView.getSelectionModel().getSelectedIndices());
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
                filesTableView.getSelectionModel().select(index + 1);
            }
        }
        filesTableView.refresh();
    }

    @FXML
    public void clearAction() {
        sourceFilesInformation.clear();
        addFilesButton.setDisable(false);
        deleteButton.setDisable(true);
        upButton.setDisable(true);
        downButton.setDisable(true);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(filesTableView.getSelectionModel().getSelectedIndices());
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
        filesTableView.refresh();
    }

    public FileInformation findData(String filename) {
        for (FileInformation d : sourceFilesInformation) {
            if (d.getFileName().equals(filename)) {
                return d;
            }
        }
        return null;
    }

}
