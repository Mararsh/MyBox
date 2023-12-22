package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.cell.TableImageInfoCell;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Pane extends ImagesBrowserController_Menu {

    protected void makeImagesPane() {
        try {
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            imagesPane.getChildren().clear();
            imageBoxList = new ArrayList<>();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedIndexes = new ArrayList<>();
            flowPane.getChildren().clear();

            if (displayMode == DisplayMode.ThumbnailsList
                    || displayMode == DisplayMode.FilesList) {
                makeListBox();

            } else {

                makeImagesGrid();

            }
            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void makeImagesGrid() {
        if (displayMode != DisplayMode.ImagesGrid
                || imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        makeGridBox();
        tableData.clear();
        backgroundTask = new FxBackgroundTask<Void>(this) {

            private List<Integer> mfList;

            @Override
            protected boolean handle() {
                try {
                    mfList = new ArrayList<>();
                    for (int i = 0; i < imageFileList.size(); ++i) {
                        if (backgroundTask == null || isCancelled()) {
                            break;
                        }
                        ImageView iView = imageViewList.get(i);
                        File file = imageFileList.get(i);
                        ImageInformation imageInfo = loadInfo(this, file);
                        if (imageInfo == null) {
                            continue;
                        }
                        iView.setImage(imageInfo.getThumbnail());
                        if (imageInfo.isIsMultipleFrames()) {
                            mfList.add(i);
                        }
                        tableData.add(imageInfo);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                for (Integer i : mfList) {
                    Label titleLabel = imageTitleList.get(i);
                    String title = imageFileList.get(i).getName() + " " + Languages.message("MultipleFrames");
                    titleLabel.setText(title);
                    titleLabel.setStyle("-fx-text-box-border: purple;   -fx-text-fill: purple;");
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                paneSizeAll();
                imagesPane.applyCss();
                imagesPane.layout();
            }

        };
        start(backgroundTask, false);
    }

    private void makeGridBox() {
        flowPane.getChildren().clear();
        imagesPane.getChildren().add(flowPane);
        for (int i = 0; i < imageFileList.size(); ++i) {
            File file = imageFileList.get(i);

            final VBox vbox = new VBox();
            vbox.setPrefWidth(thumbWidth + 20);
            vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setPadding(new Insets(1, 1, 1, 1));
            flowPane.getChildren().add(vbox);

            ScrollPane sPane = new ScrollPane();
            sPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(sPane, Priority.ALWAYS);
            HBox.setHgrow(sPane, Priority.ALWAYS);
            sPane.setPannable(true);
            sPane.setFitToWidth(true);
            sPane.setFitToHeight(true);

            AnchorPane aPane = new AnchorPane();
            aPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(aPane, Priority.ALWAYS);
            HBox.setHgrow(aPane, Priority.ALWAYS);
            sPane.setContent(aPane);

            ImageView iView = new ImageView();
            iView.setPreserveRatio(true);
            iView.setUserData(sPane);
            aPane.getChildren().add(iView);

            Label titleLabel = new Label();
            titleLabel.setWrapText(true);
            VBox.setVgrow(titleLabel, Priority.NEVER);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleLabel.setText(file.getName());

            vbox.getChildren().addAll(titleLabel, sPane);
            vbox.setPickOnBounds(false);

            final int index = i;
            vbox.setOnMouseClicked((MouseEvent event) -> {
                String sindex = index + "";
                File clickedFile = imageFileList.get(index);
                if (event.getButton() == MouseButton.SECONDARY) {
                    popContextMenu(index, iView, event);
                    return;
                } else if (event.getClickCount() > 1) {
                    ImageEditorController.openFile(clickedFile);
                    return;
                }
                viewImage(clickedFile);
                if (event.isControlDown()) {
                    if (selectedIndexes.contains(sindex)) {
                        selectedIndexes.remove(sindex);
                        vbox.setStyle(null);
                    } else {
                        selectedIndexes.add(sindex);
                        vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                    }
                } else {
                    for (String ix : selectedIndexes) {
                        imageBoxList.get(Integer.parseInt(ix)).setStyle(null);
                    }
                    selectedIndexes.clear();
                    selectedIndexes.add(sindex);
                    vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                }
            });

            imageScrollList.add(sPane);
            imageViewList.add(iView);
            imageTitleList.add(titleLabel);
            imageBoxList.add(vbox);

        }

        // https://stackoverflow.com/questions/26152642/get-the-height-of-a-node-in-javafx-generate-a-layout-pass
        flowPane.applyCss();
        flowPane.layout();
    }

    private void makeListBox() {
        try {
            if (displayMode != DisplayMode.ThumbnailsList
                    && displayMode != DisplayMode.FilesList) {
                return;
            }
            makeSourceTable();
            imagesPane.getChildren().add(tableView);
            tableData.clear();
            tableView.setItems(tableData);
            tableView.refresh();
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            backgroundTask = new FxBackgroundTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        for (int i = 0; i < imageFileList.size(); ++i) {
                            if (backgroundTask == null || isCancelled()) {
                                break;
                            }
                            File file = imageFileList.get(i);
                            ImageInformation imageInfo = loadInfo(this, file);
                            if (imageInfo != null) {
                                tableData.add(imageInfo);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                }

            };
            start(backgroundTask, false);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void makeSourceTable() {
        try {
            tableView = new TableView<>();
            tableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            HBox.setHgrow(tableView, Priority.ALWAYS);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setTableMenuButtonVisible(true);

            dirColumn = new TableColumn<>(Languages.message("Directory"));
            dirColumn.setPrefWidth(260);
            fileColumn = new TableColumn<>(Languages.message("File"));
            fileColumn.setPrefWidth(160);
            formatColumn = new TableColumn<>(Languages.message("Format"));
            formatColumn.setPrefWidth(60);
            csColumn = new TableColumn<>(Languages.message("Color"));
            csColumn.setPrefWidth(120);
            indexColumn = new TableColumn<>(Languages.message("Index"));
            pixelsColumn = new TableColumn<>(Languages.message("Pixels"));
            pixelsColumn.setPrefWidth(140);
            fileSizeColumn = new TableColumn<>(Languages.message("Size"));
            fileSizeColumn.setPrefWidth(140);
            isMutipleFramesColumn = new TableColumn<>(Languages.message("MultipleFrames"));
            modifiedTimeColumn = new TableColumn<>(Languages.message("ModifiedTime"));
            modifiedTimeColumn.setPrefWidth(200);
            createTimeColumn = new TableColumn<>(Languages.message("CreateTime"));
            createTimeColumn.setPrefWidth(200);

            dirColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            formatColumn.setCellValueFactory(new PropertyValueFactory<>("imageFormat"));
            csColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            pixelsColumn.setCellValueFactory(new PropertyValueFactory<>("pixelsString"));
            fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            fileSizeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(FileTools.showFileSize(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            isMutipleFramesColumn.setCellValueFactory(new PropertyValueFactory<>("isMultipleFrames"));
            isMutipleFramesColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                @Override
                public TableCell<ImageInformation, Boolean> call(
                        TableColumn<ImageInformation, Boolean> param) {
                    TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Boolean item,
                                boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(Languages.message(item.toString()));
                            if (item) {
                                text.setFill(Color.RED);
                            } else {
                                text.setFill(Color.BLACK);
                            }
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            modifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifiedTimeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            text.setText(DateTools.datetimeToString(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(DateTools.datetimeToString(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });

            TableColumn selectColumn = new TableColumn<List<String>, Boolean>();
            selectColumn.setCellFactory(TableRowSelectionCell.create(tableView));
            selectColumn.setPrefWidth(60);
            tableView.getColumns().add(selectColumn);

            if (displayMode == DisplayMode.ThumbnailsList) {
                imageColumn = new TableColumn<>(Languages.message("Image"));
                imageColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
                imageColumn.setCellFactory(new TableImageInfoCell());
                imageColumn.setPrefWidth(110);

                loadColumn = new TableColumn<>(Languages.message("LoadedSize"));
                loadColumn.setPrefWidth(140);
                loadColumn.setCellValueFactory(new PropertyValueFactory<>("loadSizeString"));

                isScaledColumn = new TableColumn<>(Languages.message("Scaled"));
                isScaledColumn.setCellValueFactory(new PropertyValueFactory<>("isScaled"));
                isScaledColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                    @Override
                    public TableCell<ImageInformation, Boolean> call(
                            TableColumn<ImageInformation, Boolean> param) {
                        TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                            @Override
                            protected void updateItem(final Boolean item,
                                    boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                setText(Languages.message(item.toString()));
                            }
                        };
                        return cell;
                    }
                });
                isScaledColumn.setPrefWidth(80);

                tableView.getColumns().addAll(imageColumn, fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn, loadColumn,
                        isMutipleFramesColumn, indexColumn, isScaledColumn, modifiedTimeColumn, createTimeColumn);
            } else {
                tableView.getColumns().addAll(fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn,
                        isMutipleFramesColumn, indexColumn, modifiedTimeColumn, createTimeColumn);
            }

            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        selectedIndexes.clear();
                        for (int index : tableView.getSelectionModel().getSelectedIndices()) {
                            selectedIndexes.add(index + "");
                        }
                    }
                }
            });

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (isSettingValues) {
                            return;
                        }
                        int index = tableView.getSelectionModel().getSelectedIndex();
                        if (index < 0) {
                            return;
                        }
                        popTableMenu(event, index);
                    } else {
                        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
                        if (info == null) {
                            return;
                        }
                        File file = info.getImageFileInformation().getFile();
                        if (event.getClickCount() > 1) {
                            ImageEditorController.openFile(file);
                        } else {
                            viewImage(file);
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
