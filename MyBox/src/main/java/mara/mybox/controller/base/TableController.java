package mara.mybox.controller.base;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class TableController<T> extends BaseController {

    public ObservableList<T> tableData = FXCollections.observableArrayList();

    @FXML
    public Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            deleteFilesButton, clearButton, moveUpButton, moveDownButton, viewFileButton;
    @FXML
    public TableView<T> tableView;
    @FXML
    public TableColumn<T, String> handledColumn, fileColumn, colorSpaceColumn, pixelsColumn;
    @FXML
    public TableColumn<T, Long> sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    public TableColumn<T, Boolean> typeColumn;
    @FXML
    public TableColumn<T, Image> imageColumn;
    @FXML
    public TableColumn<T, Integer> indexColumn;
    @FXML
    public VBox tableBox;
    @FXML
    public Label tableLabel;
    @FXML
    public CheckBox thumbCheck;

    public abstract T getData(File directory);

    public abstract void addFiles(final int index, final List<File> files);

    @FXML
    public abstract void viewFileAction();

    public TableController() {

        fileExtensionFilter = CommonValues.AllExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            initTable();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initTable() {
        try {

            if (tableView == null) {
                return;
            }
            tableData = FXCollections.observableArrayList();
            if (tableLabel != null) {
                tableLabel.setText("");
            }

            tableData.addListener(new ListChangeListener<T>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends T> change) {
                    dataChanged();
                }
            });

            initColumns();
            tableView.setItems(tableData);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        tableSelected();
                    }
                }
            });
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        viewFileAction();
                    }
                }
            });

            if (thumbCheck != null && imageColumn != null) {
                thumbCheck.selectedProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        if (thumbCheck.isSelected()) {
                            if (!tableView.getColumns().contains(imageColumn)) {
                                tableView.getColumns().add(0, imageColumn);
                            }
                        } else {
                            if (tableView.getColumns().contains(imageColumn)) {
                                tableView.getColumns().remove(imageColumn);
                            }
                        }
                    }
                });
            }

            tableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void dataChanged() {

    }

    public void initColumns() {
        try {

            if (handledColumn != null) {
                handledColumn.setCellValueFactory(new PropertyValueFactory<T, String>("handled"));
            }
            if (fileColumn != null) {
                fileColumn.setCellValueFactory(new PropertyValueFactory<T, String>("fileName"));
                fileColumn.setPrefWidth(320);
            }
            if (sizeColumn != null) {
                sizeColumn.setCellValueFactory(new PropertyValueFactory<T, Long>("fileSize"));
                sizeColumn.setCellFactory(new Callback<TableColumn<T, Long>, TableCell<T, Long>>() {
                    @Override
                    public TableCell<T, Long> call(TableColumn<T, Long> param) {
                        TableCell<T, Long> cell = new TableCell<T, Long>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(final Long item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null && item > 0) {
                                    text.setText(FileTools.showFileSize(item));
                                    setGraphic(text);
                                }
                            }
                        };
                        return cell;
                    }
                });
            }

            if (modifyTimeColumn != null) {
                modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<T, Long>("modifyTime"));
                modifyTimeColumn.setCellFactory(new Callback<TableColumn<T, Long>, TableCell<T, Long>>() {
                    @Override
                    public TableCell<T, Long> call(TableColumn<T, Long> param) {
                        TableCell<T, Long> cell = new TableCell<T, Long>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(final Long item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null && item > 0) {
                                    text.setText(DateTools.datetimeToString(item));
                                    setGraphic(text);
                                }
                            }
                        };
                        return cell;
                    }
                });
            }
            if (createTimeColumn != null) {
                createTimeColumn.setCellValueFactory(new PropertyValueFactory<T, Long>("createTime"));
                createTimeColumn.setCellFactory(new Callback<TableColumn<T, Long>, TableCell<T, Long>>() {
                    @Override
                    public TableCell<T, Long> call(TableColumn<T, Long> param) {
                        TableCell<T, Long> cell = new TableCell<T, Long>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(final Long item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null && item > 0) {
                                    text.setText(DateTools.datetimeToString(item));
                                    setGraphic(text);
                                }
                            }
                        };
                        return cell;
                    }
                });
            }

            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<T, Boolean>("isFile"));
                typeColumn.setCellFactory(new Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>>() {
                    @Override
                    public TableCell<T, Boolean> call(TableColumn<T, Boolean> param) {
                        TableCell<T, Boolean> cell = new TableCell<T, Boolean>() {
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

            if (imageColumn != null) {
                imageColumn.setCellValueFactory(new PropertyValueFactory<T, Image>("image"));
                imageColumn.setCellFactory(new Callback<TableColumn<T, Image>, TableCell<T, Image>>() {
                    @Override
                    public TableCell<T, Image> call(TableColumn<T, Image> param) {
                        final ImageView imageview = new ImageView();
                        imageview.setPreserveRatio(true);
                        imageview.setFitWidth(100);
                        imageview.setFitHeight(100);
                        TableCell<T, Image> cell = new TableCell<T, Image>() {
                            @Override
                            public void updateItem(final Image item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    imageview.setImage(item);
                                    setGraphic(imageview);
                                }
                            }
                        };
                        return cell;
                    }
                });
            }

            if (pixelsColumn != null) {
                pixelsColumn.setCellValueFactory(new PropertyValueFactory<T, String>("pixelsString"));
                pixelsColumn.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
                    @Override
                    public TableCell<T, String> call(TableColumn<T, String> param) {
                        TableCell<T, String> cell = new TableCell<T, String>() {
                            final Text text = new Text();

                            @Override
                            public void updateItem(final String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    text.setText(item);
                                    if (item.contains("*")) {
                                        text.setFill(Color.RED);
                                    }
                                    setGraphic(text);
                                }
                            }
                        };
                        return cell;
                    }
                });
            }

            if (colorSpaceColumn != null) {
                colorSpaceColumn.setCellValueFactory(new PropertyValueFactory<T, String>("colorSpace"));
            }

            if (indexColumn != null) {
                indexColumn.setCellValueFactory(new PropertyValueFactory<T, Integer>("index"));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void tableSelected() {
        ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());

        moveUpButton.setDisable(none);
        moveDownButton.setDisable(none);

        deleteFilesButton.setDisable(none);
        if (insertFilesButton != null) {
            insertFilesButton.setDisable(none);
        }
        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (viewFileButton != null) {
            viewFileButton.setDisable(none);
        }
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (metaButton != null) {
            metaButton.setDisable(none);
        }
        if (viewButton != null) {
            viewButton.setDisable(none);
        }

        if (bottomLabel != null) {
            if (none) {
                bottomLabel.setText("");
            } else {
                bottomLabel.setText(getMessage("DoubleClickToView"));
            }
        }
    }

    @FXML
    @Override
    public void addFilesAction(ActionEvent event) {
        addFiles(tableData.size());
    }

    public void addFiles(int index) {
        File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
        addFilesUnderPath(index, defaultPath);
    }

    public void addFilesUnderPath(int index, File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
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
        addFile(tableData.size(), file);
    }

    public void addFile(int index, File file) {
        if (file == null) {
            return;
        }
        List<File> files = new ArrayList();
        files.add(file);
        addFiles(index, files);
    }

    @FXML
    public void addDirectoryAction(ActionEvent event) {
        addDirectoryAction(tableData.size());
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

    @Override
    public void addDirectory(File directory) {
        addDirectory(tableData.size(), directory);
    }

    public void addDirectory(int index, File directory) {
        try {
            recordFileOpened(directory);

            T d = getData(directory);
            if (index < 0 || index >= tableData.size()) {
                tableData.add(d);
            } else {
                tableData.add(index, d);
            }
            tableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void insertFilesAction(ActionEvent event) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFiles(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFile(index, file);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void insertDirectoryAction(ActionEvent event) {
        if (insertDirectoryButton == null) {
            return;
        }
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectoryAction(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    @Override
    public void insertDirectory(File directory) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index, directory);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void deleteFilesAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            tableData.remove(index);
        }
        tableView.refresh();
    }

    @FXML
    public void clearFilesAction() {
        tableData.clear();
        tableView.refresh();
        addFilesButton.setDisable(false);
        deleteFilesButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
    }

    @FXML
    public void moveUpAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            T info = tableData.get(index);
            tableData.set(index, tableData.get(index - 1));
            tableData.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                tableView.getSelectionModel().select(index - 1);
            }
        }
        tableView.refresh();
    }

    @FXML
    public void moveDownAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == tableData.size() - 1) {
                continue;
            }
            T info = tableData.get(index);
            tableData.set(index, tableData.get(index + 1));
            tableData.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < tableData.size() - 1) {
                tableView.getSelectionModel().select(index + 1);
            }
        }
        tableView.refresh();
    }

}
