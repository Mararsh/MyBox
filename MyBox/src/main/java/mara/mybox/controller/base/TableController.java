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
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
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

    public ObservableList<T> sourceData = FXCollections.observableArrayList();

    @FXML
    public Button insertButton, viewButton, clearButton, upButton, downButton;
    @FXML
    public TableView<T> sourceTable;
    @FXML
    public TableColumn<T, Image> imageColumn;
    @FXML
    public TableColumn<T, String> fileColumn, sizeColumn, typeColumn;
    @FXML
    public TableColumn<T, Integer> indexColumn;
    @FXML
    public VBox sourcesBox;
    @FXML
    public Label sourcesLabel;

    public TableController() {

        fileExtensionFilter = CommonValues.ImageExtensionFilter;
    }

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
            sourcesLabel.setText("");

            fileColumn.setCellValueFactory(new PropertyValueFactory<T, String>("filename"));
            fileColumn.setPrefWidth(320);
            typeColumn.setCellValueFactory(new PropertyValueFactory<T, String>("colorSpace"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<T, Integer>("index"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<T, String>("pixelsString"));
            sizeColumn.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
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

            sourceTable.setItems(sourceData);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        checkTableSelected();
                    }
                }
            });
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        viewAction();
                    }
                }
            });
            checkTableSelected();

            sourceData.addListener(new ListChangeListener<T>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends T> change) {
                    if (!isSettingValues) {

                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkTableSelected() {
        int selected = sourceTable.getSelectionModel().getSelectedIndex();
        boolean none = (selected < 0);
        insertButton.setDisable(none);
        upButton.setDisable(none);
        downButton.setDisable(none);
        deleteButton.setDisable(none);
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (viewButton != null) {
            viewButton.setDisable(none);
        }
        if (none) {
            bottomLabel.setText("");
        } else {
            bottomLabel.setText(getMessage("DoubleClickToView"));
        }
    }

    @FXML
    public void addAction(ActionEvent event) {
        addAction(sourceData.size());
    }

    @FXML
    public void insertAction(ActionEvent event) {
        int index = sourceTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addAction(index);
        } else {
            insertButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = sourceTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addAction(index, file);
        } else {
            insertButton.setDisable(true);
        }
    }

    @Override
    public void addFile(File file) {
        addAction(sourceData.size(), file);
    }

    public void addAction(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            addAction(index, files);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void addAction(int index, File file) {
        if (file == null) {
            return;
        }
        List<File> files = new ArrayList();
        files.add(file);
        addAction(index, files);
    }

    public abstract void addAction(final int index, final List<File> files);

    @FXML
    @Override
    public void deleteAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > sourceData.size() - 1) {
                continue;
            }
            sourceData.remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    public void clearAction(ActionEvent event) {
        sourceData.clear();
        sourceTable.refresh();
    }

    @FXML
    public void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            T info = sourceData.get(index);
            sourceData.set(index, sourceData.get(index - 1));
            sourceData.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                sourceTable.getSelectionModel().select(index - 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    public void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == sourceData.size() - 1) {
                continue;
            }
            T info = sourceData.get(index);
            sourceData.set(index, sourceData.get(index + 1));
            sourceData.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < sourceData.size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    public void viewAction() {
        T info = sourceTable.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }

    }

}
