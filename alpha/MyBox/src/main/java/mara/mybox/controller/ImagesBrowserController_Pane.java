package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableImageInfoCell;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Pane extends ImagesBrowserController_Menu {

    @Override
    public void makeImageNevigator() {
    }

    @Override
    protected void makeImagesNevigator(boolean makeCurrentList) {
        if (isSettingValues) {
            return;
        }
        previousFiles = new ArrayList<>();
        nextFiles = new ArrayList<>();
        try {
            if (imageFileList != null && !imageFileList.isEmpty() && filesNumber > 0) {
                loadingController = handling();

                File firstFile = imageFileList.get(0);
                path = firstFile.getParentFile();
                List<File> pathFiles = new ArrayList<>();
                File[] pfiles = path.listFiles();
                if (pfiles != null) {
                    for (File file : pfiles) {
                        if (file.isFile() && FileTools.isSupportedImage(file)) {
                            pathFiles.add(file);
                        }
                    }
                    FileSortTools.sortFiles(pathFiles, sortMode);
                }
                totalLabel.setText("/" + pathFiles.size());

                if (makeCurrentList) {
                    imageFileList.clear();
                    int pos = pathFiles.indexOf(firstFile);
                    if (pos < 0) {
                        pos = 0;
                    }
                    int start;
                    int end;
                    if (pathFiles.size() <= filesNumber) {
                        start = 0;
                        end = pathFiles.size() - 1;
                    } else if (pos + filesNumber < pathFiles.size()) {
                        start = pos;
                        end = pos + filesNumber - 1;
                    } else {
                        start = pathFiles.size() - filesNumber;
                        end = pathFiles.size() - 1;
                    }
                    for (int i = start; i <= end; ++i) {
                        imageFileList.add(pathFiles.get(i));
                    }
                }

                if (pathFiles.size() > filesNumber) {

                    List<String> pathFnames = new ArrayList<>();
                    for (File f : pathFiles) {
                        pathFnames.add(f.getAbsolutePath());
                    }
                    List<String> iFnames = new ArrayList<>();
                    for (File f : imageFileList) {
                        iFnames.add(f.getAbsolutePath());
                    }
                    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                    for (String f : iFnames) {
                        int index = pathFnames.indexOf(f);
                        if (index < 0) {
                            continue;
                        }
                        if (index <= min) {
                            min = index;
                        }
                        if (index >= max) {
                            max = index;
                        }
                    }
                    if (max < min) {
                        min = -1;
                        max = pathFiles.size();
                    }

                    for (int i = max - 1; i >= 0; --i) {
                        String fname = pathFnames.get(i);
                        if (!iFnames.contains(fname)) {
                            previousFiles.add(0, new File(fname));
                            if (previousFiles.size() == filesNumber) {
                                break;
                            }
                        }
                    }

                    for (int i = min + 1; i < pathFnames.size(); ++i) {
                        String fname = pathFnames.get(i);
                        if (!iFnames.contains(fname)) {
                            nextFiles.add(new File(fname));
                            if (nextFiles.size() == filesNumber) {
                                break;
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        if (nextFiles.isEmpty()) {
            nextFiles = null;
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
        if (previousFiles.isEmpty()) {
            previousFiles = null;
            previousButton.setDisable(true);
        } else {
            previousButton.setDisable(false);
        }

        if (loadingController != null) {
            loadingController.closeStage();
        }
        makeImagesPane();
    }

    protected void makeImagesPane() {
        try {
            imagesPane.getChildren().clear();
            imageBoxList = new ArrayList<>();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedIndexes = new ArrayList<>();
            rowsNum = 0;

            if (displayMode == ImagesBrowserController_Load.DisplayMode.ThumbnailsList
                    || displayMode == ImagesBrowserController_Load.DisplayMode.FilesList) {
                if (viewBox.getChildren().contains(gridOptionsBox)) {
                    viewBox.getChildren().remove(gridOptionsBox);
                }
                makeListBox();

            } else if (colsNum > 0) {
                if (!viewBox.getChildren().contains(gridOptionsBox)) {
                    viewBox.getChildren().add(gridOptionsBox);
                }
                makeImagesGrid();

            }
            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void makeImagesGrid() {
        if (colsNum <= 0 || displayMode != ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            return;
        }

        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    loadImageInfos();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    makeGridBox();
                }

            };
            start(task);
        }
    }

    protected void makeGridBox() {
        int num = tableData.size();
        HBox line = new HBox();
        for (int i = 0; i < num; ++i) {
            if (i % colsNum == 0) {
                line = new HBox();
                line.setAlignment(Pos.TOP_CENTER);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                imagesPane.getChildren().add(line);
                VBox.setVgrow(line, Priority.ALWAYS);
                HBox.setHgrow(line, Priority.ALWAYS);
                rowsNum++;
            }

            final VBox vbox = new VBox();
            vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            line.setAlignment(Pos.TOP_CENTER);
            vbox.setPadding(new Insets(5, 5, 5, 5));
            line.getChildren().add(vbox);

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
            vbox.getChildren().addAll(titleLabel, sPane);
            vbox.setPickOnBounds(false);

            ImageInformation imageInfo = tableData.get(i);
            File file = imageInfo.getImageFileInformation().getFile();
            iView.setImage(imageInfo.loadThumbnail(thumbWidth));

            String title = file.getName();
            if (imageInfo.isIsMultipleFrames()) {
                title += " " + Languages.message("MultipleFrames");
                titleLabel.setStyle("-fx-text-box-border: purple;   -fx-text-fill: purple;");
            }
            titleLabel.setText(title);

            final int index = i;
            vbox.setOnMouseClicked((MouseEvent event) -> {
                File clickedFile = tableData.get(index).getFile();
                if (event.getButton() == MouseButton.SECONDARY) {
                    if (contextMenuCheck.isSelected()) {
                        popImageMenu(index, iView, event);
                    }
                    return;
                } else if (event.getClickCount() > 1) {
                    ControllerTools.openImageViewer(null, clickedFile);
                    return;
                }
                currentIndex = index;
                filenameLabel.setText(clickedFile.getAbsolutePath());
                loadImageFile(clickedFile, loadWidth, 0);
                Integer o = Integer.valueOf(index);
                if (event.isControlDown()) {
                    if (selectedIndexes.contains(o)) {
                        selectedIndexes.remove(o);
                        vbox.setStyle(null);
                    } else {
                        selectedIndexes.add(o);
                        vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                    }
                } else {
                    for (int ix : selectedIndexes) {
                        imageBoxList.get(ix).setStyle(null);
                    }
                    selectedIndexes.clear();
                    selectedIndexes.add(o);
                    vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                }
            });

            imageScrollList.add(sPane);
            imageViewList.add(iView);
            imageTitleList.add(titleLabel);
            imageBoxList.add(vbox);

        }

        for (int i = 0; i < num; ++i) {
            double w = imagesPane.getWidth() / colsNum - 5;
            double h = imagesPane.getHeight() / rowsNum - 5;
            VBox vbox = imageBoxList.get(i);
            vbox.setPrefWidth(w);
            vbox.setPrefHeight(h);
        }

        // https://stackoverflow.com/questions/26152642/get-the-height-of-a-node-in-javafx-generate-a-layout-pass
        imagesPane.applyCss();
        imagesPane.layout();

        paneSizeAll();
    }

    protected void makeListBox() {
        try {
            if (displayMode != ImagesBrowserController_Load.DisplayMode.ThumbnailsList && displayMode != ImagesBrowserController_Load.DisplayMode.FilesList) {
                return;
            }
            makeSourceTable();
            imagesPane.getChildren().add(tableView);
            tableView.setItems(null);
            tableView.refresh();
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        loadImageInfos();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        tableView.setItems(tableData);
                        tableView.refresh();
                    }

                };
                start(task);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadImageInfos() {
        try {
            tableData.clear();
            for (int i = 0; i < imageFileList.size(); ++i) {
                File file = imageFileList.get(i);
                ImageInformation imageInfo = loadImageInfo(file);
                if (imageInfo != null) {
                    tableData.add(imageInfo);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void makeSourceTable() {
        try {
            tableView = new TableView<>();
            tableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            HBox.setHgrow(tableView, Priority.ALWAYS);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setTableMenuButtonVisible(true);

            fileColumn = new TableColumn<>(Languages.message("File"));
            fileColumn.setPrefWidth(220);
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

            if (displayMode == ImagesBrowserController_Load.DisplayMode.ThumbnailsList) {
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
                        selectedIndexes = tableView.getSelectionModel().getSelectedIndices();
                    }
                }
            });

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (isSettingValues || !contextMenuCheck.isSelected()) {
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
                            ControllerTools.openImageViewer(null, file);
                        } else {
                            loadImageFile(file, loadWidth, 0);
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
