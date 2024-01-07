package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewInfoTask;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @License Apache License Version 2.0
 */
public class ImagesBrowserController extends ControlImagesTable {

    protected List<VBox> imageBoxList;
    protected List<ScrollPane> imageScrollList;
    protected List<ImageView> imageViewList;
    protected List<Label> imageTitleList;

    protected List<String> selectedIndexes;
    protected int maxShow = 10000;
    protected DisplayMode displayMode;

    protected enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList
    }

    @FXML
    protected VBox imagesBox, gridBox, viewBox;
    @FXML
    protected ToggleGroup displayGroup;
    @FXML
    protected RadioButton listRadio, thumbRadio, gridRadio;
    @FXML
    protected FlowPane opPane, flowPane;
    @FXML
    protected ToggleGroup popGroup;
    @FXML
    protected Button rotateLeftImagesButton, rotateRightImagesButton, turnOverImagesButton;
    @FXML
    protected ControlImageView viewController;

    public ImagesBrowserController() {
        baseTitle = message("ImagesBrowser");
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(selectFileButton, new Tooltip(message("SelectMultipleFilesBrowse")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            imageBoxList = new ArrayList<>();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedIndexes = new ArrayList<>();

            viewController.backgroundLoad = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            thumbRadio.setSelected(true);
            displayMode = DisplayMode.ThumbnailsList;
            imagesBox.getChildren().clear();
            flowPane.getChildren().clear();

            displayGroup.selectedToggleProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object o, Object n) {
                    if (thumbRadio.isSelected()) {
                        displayMode = DisplayMode.ThumbnailsList;
                    } else if (listRadio.isSelected()) {
                        displayMode = DisplayMode.FilesList;
                    } else {
                        displayMode = DisplayMode.ImagesGrid;
                    }
                    refreshAction();
                }
            });

            opPane.disableProperty().bind(Bindings.isEmpty(tableData));
            mainAreaBox.disableProperty().bind(Bindings.isEmpty(tableData));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        selectedIndexes.clear();
        for (int index : tableView.getSelectionModel().getSelectedIndices()) {
            selectedIndexes.add(index + "");
        }
        if (tableData.isEmpty()) {
            setTitle(getBaseTitle());
        } else {
            setTitle(getBaseTitle() + " "
                    + tableData.get(0).getFile().getParentFile().getAbsolutePath());
        }
        checkButtons();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        boolean noSelection = selectedIndexes.isEmpty();
        deleteButton.setDisable(noSelection);
        editButton.setDisable(noSelection);
        infoButton.setDisable(noSelection);
        metaButton.setDisable(noSelection);
        rotateLeftImagesButton.setDisable(noSelection);
        rotateRightImagesButton.setDisable(noSelection);
        turnOverImagesButton.setDisable(noSelection);
    }

    @Override
    public void itemClicked() {
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }
        viewImage(info);
    }

    @Override
    protected void tableChanged() {
        if (isSettingValues) {
            return;
        }
        super.tableChanged();
        refreshAction();
    }

    public void loadSourceFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        imagesBox.getChildren().clear();
        flowPane.getChildren().clear();
        imageBoxList.clear();
        imageViewList.clear();
        imageTitleList.clear();
        imageScrollList.clear();
        selectedIndexes.clear();
        tableData.clear();
        addFiles(0, files);
    }

    public void loadFilenames(List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            List<File> files = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); ++i) {
                File file = new File(fileNames.get(i));
                files.add(file);
            }
            loadSourceFiles(files);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        List<ImageInformation> invalid = new ArrayList<>();
        for (ImageInformation info : tableData) {
            File file = info.getFile();
            if (file == null || !file.exists() || !file.isFile()) {
                invalid.add(info);
            }
        }
        if (!invalid.isEmpty()) {
            tableData.removeAll(invalid);
        }
        if (displayMode == DisplayMode.ThumbnailsList
                || displayMode == DisplayMode.FilesList) {
            makeTableView();
        } else {
            makeImagesGrid();
        }
    }

    private void makeImagesGrid() {
        try {
            if (displayMode != DisplayMode.ImagesGrid) {
                return;
            }
            imagesBox.getChildren().clear();
            imagesBox.getChildren().add(gridBox);
            flowPane.getChildren().clear();
            imageBoxList.clear();
            imageViewList.clear();
            imageTitleList.clear();
            imageScrollList.clear();
            selectedIndexes.clear();
            for (int i = 0; i < tableData.size(); ++i) {
                ImageInformation imageInfo = tableData.get(i);
                if (imageInfo == null) {
                    continue;
                }
                File file = imageInfo.getFile();

                final VBox vbox = new VBox();
                vbox.setPrefWidth(AppVariables.thumbnailWidth + 20);
                vbox.setPrefHeight(AppVariables.thumbnailWidth + 20);
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
                ImageViewInfoTask itask = new ImageViewInfoTask()
                        .setView(iView).setItem(imageInfo);
                Thread thread = new Thread(itask);
                thread.setDaemon(false);
                thread.start();

                Label titleLabel = new Label();
                titleLabel.setWrapText(true);
                VBox.setVgrow(titleLabel, Priority.NEVER);
                HBox.setHgrow(titleLabel, Priority.ALWAYS);
                if (imageInfo.isIsMultipleFrames()) {
                    String title = file.getName() + " " + message("MultipleFrames");
                    titleLabel.setText(title);
                    titleLabel.setStyle("-fx-text-box-border: purple;   -fx-text-fill: purple;");
                } else {
                    titleLabel.setText(file.getName());
                }

                vbox.getChildren().addAll(titleLabel, sPane);
                vbox.setPickOnBounds(false);

                final int index = i;
                vbox.setOnMouseClicked((MouseEvent event) -> {
                    if (index >= tableData.size()) {
                        return;
                    }
                    ImageInformation info = tableData.get(index);
                    if (info == null) {
                        return;
                    }
                    File mfile = info.getFile();
                    String sindex = index + "";
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popContextMenu(index, iView, event);
                        return;
                    } else if (event.getClickCount() > 1) {
                        ImageEditorController.openFile(mfile);
                        return;
                    }
                    viewImage(imageInfo);
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
                    checkButtons();
                });

                imageScrollList.add(sPane);
                imageViewList.add(iView);
                imageTitleList.add(titleLabel);
                imageBoxList.add(vbox);

            }

            imagesBox.applyCss();
            imagesBox.layout();

            Platform.runLater(() -> {
                paneSizeAll();
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void makeTableView() {
        try {
            switch (displayMode) {
                case ThumbnailsList:
                    if (!tableView.getColumns().contains(imageColumn)) {
                        tableView.getColumns().add(2, imageColumn);
                    }
                    break;
                case FilesList:
                    if (tableView.getColumns().contains(imageColumn)) {
                        tableView.getColumns().remove(imageColumn);
                    }
                    break;
                default:
                    return;
            }
            if (imagesBox.getChildren().contains(tableView)) {
                return;
            }

            imagesBox.getChildren().clear();
            flowPane.getChildren().clear();
            imageBoxList.clear();
            imageViewList.clear();
            imageTitleList.clear();
            imageScrollList.clear();
            selectedIndexes.clear();

            imagesBox.getChildren().add(tableView);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void viewImage(ImageInformation info) {
        viewController.loadImageInfo(info);
    }

    /*
        grid
     */
    public void zoomIn(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.zoomIn((ScrollPane) iView.getUserData(), iView, 5, 5);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void zoomOut(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.zoomOut((ScrollPane) iView.getUserData(), iView, 5, 5);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void paneSize(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.paneSize((ScrollPane) iView.getUserData(), iView);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void imageSize(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.imageSize((ScrollPane) iView.getUserData(), iView);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void zoomOutAll() {
        if (displayMode != DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomOut(i);
            }
        } else {
            for (String i : selectedIndexes) {
                zoomOut(Integer.parseInt(i));
            }
        }
    }

    @FXML
    public void zoomInAll() {
        if (displayMode != DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomIn(i);
            }
        } else {
            for (String i : selectedIndexes) {
                zoomIn(Integer.parseInt(i));
            }
        }
    }

    @FXML
    public void loadedSizeAll() {
        if (displayMode != DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                imageSize(i);
            }
        } else {
            for (String i : selectedIndexes) {
                imageSize(Integer.parseInt(i));
            }
        }
    }

    @FXML
    public void paneSizeAll() {
        if (displayMode != DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                paneSize(i);
            }
        } else {
            for (String i : selectedIndexes) {
                paneSize(Integer.parseInt(i));
            }
        }
    }

    /*
        actions
     */
    public void info(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ImageInformationController.open(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void meta(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ImageMetaDataController.open(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void view(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                File file = imageInfo.getImageFileInformation().getFile();
                ImageEditorController.openFile(file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void delete(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation info = tableData.get(index);
            File file = info.getImageFileInformation().getFile();
            if (FileDeleteTools.delete(file)) {
                tableData.remove(index);
            }
            popSuccessful();
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected int selectedIndix() {
        try {
            return Integer.parseInt(selectedIndexes.get(0));
        } catch (Exception e) {
            return -1;
        }
    }

    @FXML
    public void selectAllImages() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().selectAll();
        } else {
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                selectedIndexes.add(i + "");
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
            }
        }
    }

    @FXML
    public void selectNoneImages() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().clearSelection();
        } else {
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle(null);
            }
        }
    }

    @FXML
    public void rotateRightImages() {
        rotateImages(90);
    }

    @FXML
    public void rotateLeftImages() {
        rotateImages(270);
    }

    @FXML
    public void turnOverImages() {
        rotateImages(180);
    }

    public void rotateImages(int rotateAngle) {
        saveRotation(selectedIndexes, rotateAngle);
    }

    public void rotateImages(int index, int rotateAngle) {
        List<String> indexs = new ArrayList<>();
        indexs.add(index + "");
        saveRotation(indexs, rotateAngle);
    }

    public void saveRotation(List<String> indexs, int rotateAngle) {
        if (indexs == null || indexs.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean hasMultipleFrames = false;

            @Override
            protected boolean handle() {
                try {
                    for (int i = 0; i < tableData.size(); ++i) {
                        if (task == null || !isWorking()) {
                            return false;
                        }
                        if (!indexs.contains(i + "")) {
                            continue;
                        }
                        ImageInformation info = tableData.get(i);
                        if (info.isIsMultipleFrames()) {
                            hasMultipleFrames = true;
                            continue;
                        }
                        File file = info.getFile();
                        BufferedImage bufferedImage = ImageInformation.readBufferedImage(this, info);
                        if (bufferedImage == null || task == null || !isWorking()) {
                            continue;
                        }
                        bufferedImage = TransformTools.rotateImage(this, bufferedImage, rotateAngle);
                        if (bufferedImage == null || task == null || !isWorking()) {
                            continue;
                        }
                        if (!ImageFileWriters.writeImageFile(this, bufferedImage, file)
                                || task == null || !isWorking()) {
                            continue;
                        }
                        ImageInformation newInfo = ImageFileReaders.makeInfo(this,
                                file, AppVariables.thumbnailWidth);
                        if (newInfo == null || task == null || !isWorking()) {
                            continue;
                        }
                        int index = i;
                        Platform.runLater(() -> {
                            tableData.set(index, newInfo);
                            if (displayMode == DisplayMode.ImagesGrid) {
                                ImageView iView = imageViewList.get(index);
                                iView.setImage(newInfo.getThumbnail());
                            } else {
                                tableView.getSelectionModel().select(index);
                            }
                            if (viewController.sourceFile != null
                                    && file.equals(viewController.sourceFile)) {
                                viewImage(newInfo);
                            }
                        });

                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (hasMultipleFrames) {
                    popError(message("CanNotHandleMultipleFrames"));
                } else {
                    popSaved();
                }
            }

        };
        start(task);
    }

    @Override
    public ImagesBrowserController refreshInterfaceAndFile() {
        super.refreshInterface();
        refreshAction();
        return this;
    }

    @FXML
    protected void filesListAction(ActionEvent event) {
        displayMode = DisplayMode.FilesList;
        refreshAction();
    }

    @FXML
    protected void thumbsListAction(ActionEvent event) {
        displayMode = DisplayMode.ThumbnailsList;
        refreshAction();
    }

    @FXML
    protected void gridAction(ActionEvent event) {
        displayMode = DisplayMode.ImagesGrid;
        refreshAction();
    }

    protected void popContextMenu(int index, ImageView iView, MouseEvent event) {
        if (iView == null || iView.getImage() == null) {
            return;
        }
        if (index >= tableData.size()) {
            return;
        }

        ImageInformation info = tableData.get(index);

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(StringTools.menuSuffix(info.getFile().getAbsolutePath()));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (displayMode == DisplayMode.ImagesGrid) {
            menu = new MenuItem(message("PaneSize"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                paneSize(index);
            });
            items.add(menu);

            menu = new MenuItem(message("ImageSize"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                imageSize(index);
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomIn"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomIn(index);
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomOut"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomOut(index);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        if (!info.isIsMultipleFrames()) {

            menu = new MenuItem(message("RotateLeft"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 270);
            });
            items.add(menu);

            menu = new MenuItem(message("RotateRight"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 90);
            });
            items.add(menu);

            menu = new MenuItem(message("TurnOver"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 180);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        menu = new MenuItem(message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        popMenu(iView, items, event.getScreenX(), event.getScreenY());

    }

    @Override
    protected void popTableMenu(MouseEvent event) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(message("RotateRight"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 90);
        });
        items.add(menu);

        menu = new MenuItem(message("TurnOver"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 180);
        });
        items.add(menu);

        menu = new MenuItem(message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        popEventMenu(event, items);

    }

    public List<ImageInformation> selected() {
        List<ImageInformation> list = new ArrayList<>();
        if (selectedIndexes.isEmpty()) {
            return tableData;
        }
        for (int i = 0; i < tableData.size(); ++i) {
            if (selectedIndexes.contains(i + "")) {
                list.add(tableData.get(i));
            }
        }
        return list;
    }

    @FXML
    @Override
    public void playAction() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            ImagesPlayController.playImages(list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            ImagesSaveController.saveImages(this, list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editFrames() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            ImagesEditorController.openImages(list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void options() {
        ImageOptionsController.open(viewController);
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            viewController.menuAction();
            return true;
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            viewController.popAction();
            return true;
        }
        return super.popAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static ImagesBrowserController open() {
        try {
            ImagesBrowserController controller = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesBrowserController loadFiles(List<File> files) {
        try {
            ImagesBrowserController controller = open();
            if (controller != null) {
                controller.loadSourceFiles(files);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesBrowserController loadNames(List<String> files) {
        try {
            ImagesBrowserController controller = open();
            if (controller != null) {
                controller.loadFilenames(files);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesBrowserController openPath(File path) {
        try {
            ImagesBrowserController controller = open();
            if (controller != null) {
                controller.addDirectory(path);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
