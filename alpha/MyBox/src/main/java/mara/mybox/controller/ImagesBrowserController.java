package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableImageInfoCell;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @License Apache License Version 2.0
 */
public class ImagesBrowserController extends ImageViewerController {

    private final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    private final ObservableList<ImageInformation> tableData = FXCollections.observableArrayList();
    private int rowsNum, colsNum, filesNumber, thumbWidth, currentIndex;
    private List<VBox> imageBoxList;
    private List<ScrollPane> imageScrollList;
    private List<ImageView> imageViewList;
    private List<Label> imageTitleList;

    private TableView<ImageInformation> tableView;
    private TableColumn<ImageInformation, ImageInformation> imageColumn;
    private TableColumn<ImageInformation, String> fileColumn, formatColumn, pixelsColumn, csColumn, loadColumn;
    private TableColumn<ImageInformation, Integer> indexColumn;
    private TableColumn<ImageInformation, Long> fileSizeColumn, modifiedTimeColumn, createTimeColumn;
    private TableColumn<ImageInformation, Boolean> isScaledColumn, isMutipleFramesColumn;

    protected List<File> nextFiles, previousFiles;
    protected List<Integer> selectedIndexes;
    protected int maxShow = 100;
    private File path;
    private DisplayMode displayMode;

    private enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList, None
    }

    @FXML
    protected VBox imagesPane, mainBox, viewBox, gridOptionsBox;
    @FXML
    protected ComboBox<String> colsnumBox, filesBox, thumbWidthSelector;
    @FXML
    protected CheckBox saveRotationCheck;
    @FXML
    protected Label totalLabel, filenameLabel;
    @FXML
    protected ToggleGroup popGroup;

    public ImagesBrowserController() {
        baseTitle = Languages.message("ImagesBrowser");
        TipsLabelKey = "ImagesBrowserTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            colsNum = -1;
            displayMode = DisplayMode.ImagesGrid;
            currentIndex = -1;
            thumbWidth = UserConfig.getUserConfigInt("ThumbnailWidth", 100);
            thumbWidth = thumbWidth > 0 ? thumbWidth : 100;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> cvalues = Arrays.asList("3", "4", "6",
                    Languages.message("ThumbnailsList"), Languages.message("FilesList"),
                    "2", "5", "7", "8", "9", "10", "16", "25", "20", "12", "15");
            colsnumBox.getItems().addAll(cvalues);
            colsnumBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        if (Languages.message("ThumbnailsList").equals(newValue)) {
                            displayMode = DisplayMode.ThumbnailsList;
                        } else if (Languages.message("FilesList").equals(newValue)) {
                            displayMode = DisplayMode.FilesList;
                        } else {
                            tableData.clear();
                            displayMode = DisplayMode.ImagesGrid;
                            colsNum = Integer.valueOf(newValue);
                            if (colsNum >= 0) {
                                ValidationTools.setEditorNormal(colsnumBox);
                            } else {
                                ValidationTools.setEditorBadStyle(colsnumBox);
                                return;
                            }
                        }
                        makeImagesPane();
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(colsnumBox);
                    }
                }
            });

            filesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
                    try {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        filesNumber = Integer.valueOf(newValue);
                        if (filesNumber > 0) {
                            ValidationTools.setEditorNormal(filesBox);
                            makeImagesNevigator(true);

                        } else {
                            ValidationTools.setEditorBadStyle(filesBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(filesBox);
                    }
                }
            });

            thumbWidthSelector.getItems().addAll(Arrays.asList("100", "150", "50", "200", "300"));
            thumbWidthSelector.setValue(thumbWidth + "");
            thumbWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            ValidationTools.setEditorNormal(thumbWidthSelector);
                            thumbWidth = v;
                            UserConfig.setUserConfigInt("ThumbnailWidth", thumbWidth);
                            loadImages();
                        } else {
                            ValidationTools.setEditorBadStyle(thumbWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(thumbWidthSelector);
                    }
                }
            });

            saveRotationCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "SaveRotation", true));
            saveRotationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "SaveRotation", saveRotationCheck.isSelected());
                }
            });

            fileBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            viewPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            browsePane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            mainBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            leftPaneControl.visibleProperty().bind(Bindings.isEmpty(imageFileList).not());
            rightPane.disableProperty().bind(Bindings.isEmpty(imageFileList));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(selectFileButton, new Tooltip(Languages.message("SelectMultipleFilesBrowse")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        if (saveRotationCheck.isSelected()) {
            saveRotation(selectedIndexes, rotateAngle);
        } else {
            rotateImages(selectedIndexes, rotateAngle);
        }
    }

    public void rotateImages(int index, int rotateAngle) {
        List<Integer> indexs = new ArrayList<>();
        indexs.add(index);
        if (saveRotationCheck.isSelected()) {
            saveRotation(indexs, rotateAngle);
        } else {
            rotateImages(indexs, rotateAngle);
        }
    }

    public void rotateImages(List<Integer> indexs, int rotateAngle) {
        switch (displayMode) {
            case FilesList:
                break;
            case ThumbnailsList:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < tableData.size(); ++i) {
                        ImageInformation info = tableData.get(i);
                        if (info.isIsMultipleFrames()) {
                            continue;
                        }
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(i, info);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageInformation info = tableData.get(index);
                        if (info.isIsMultipleFrames()) {
                            continue;
                        }
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(index, info);
                    }
                }
                tableView.refresh();
                if (indexs != null) {
                    for (int i = 0; i < indexs.size(); ++i) {
                        tableView.getSelectionModel().select(indexs.get(i));
                    }
                }
                break;
            case ImagesGrid:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < imageViewList.size(); ++i) {
                        ImageView iView = imageViewList.get(i);
                        iView.setRotate(iView.getRotate() + rotateAngle);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageView iView = imageViewList.get(index);
                        iView.setRotate(iView.getRotate() + rotateAngle);
                    }
                }
                break;
        }
    }

    public void saveRotation(List<Integer> indexs, final int rotateAngle) {
        if (!saveRotationCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private int handled = 0;
                private boolean hasMultipleFrames = false;

                @Override
                protected boolean handle() {
                    if (indexs == null || indexs.isEmpty()) {
                        for (int i = 0; i < tableData.size(); ++i) {
                            ImageInformation info = tableData.get(i);
                            if (info.isIsMultipleFrames()) {
                                hasMultipleFrames = true;
                                continue;
                            }
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (newInfo == null) {
                                continue;
                            }
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadThumbnail(thumbWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(i, newInfo);
                            handled++;
                        }
                    } else {
                        for (int i = 0; i < indexs.size(); ++i) {
                            int index = indexs.get(i);
                            ImageInformation info = tableData.get(index);
                            if (info.isIsMultipleFrames()) {
                                hasMultipleFrames = true;
                                continue;
                            }
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (newInfo == null) {
                                continue;
                            }
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadThumbnail(thumbWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(index, newInfo);
                            handled++;
                        }
                    }
                    return true;
                }

                private ImageInformation saveRotation(ImageInformation info, double rotateAngle) {
                    if (info == null || info.getImageFileInformation() == null || info.isIsMultipleFrames()) {
                        return null;
                    }
                    try {
                        File file = info.getImageFileInformation().getFile();
                        BufferedImage bufferedImage = ImageInformation.readBufferedImage(info);
                        bufferedImage = TransformTools.rotateImage(bufferedImage, (int) rotateAngle);
                        ImageFileWriters.writeImageFile(bufferedImage, file);
                        ImageInformation newInfo = loadImageInfo(file);
                        return newInfo;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return null;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (hasMultipleFrames) {
                        popError(Languages.message("CanNotHandleMultipleFrames"));
                    }
                    if (handled == 0) {
                        return;
                    }
                    if (displayMode == DisplayMode.ImagesGrid) {
                        if (indexs == null || indexs.isEmpty()) {
                            for (int i = 0; i < tableData.size(); ++i) {
                                ImageView iView = imageViewList.get(i);
                                iView.setImage(tableData.get(i).getThumbnail());
                            }
                        } else {
                            for (int i = 0; i < indexs.size(); ++i) {
                                int index = indexs.get(i);
                                ImageView iView = imageViewList.get(index);
                                iView.setImage(tableData.get(index).getThumbnail());
                            }
                        }
                    } else {
                        tableView.refresh();
                        if (indexs != null) {
                            for (int i = 0; i < indexs.size(); ++i) {
                                tableView.getSelectionModel().select(indexs.get(i));
                            }
                        }
                    }
                    popSaved();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFiles != null) {
            previousFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(nextFiles);
            makeImagesNevigator(false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFiles != null) {
            nextFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(previousFiles);
            makeImagesNevigator(false);
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
                ControllerTools.openImageViewer(null, file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void fileRenamed(File newFile) {
        fileRenamed(currentIndex, newFile);
    }

    public void rename(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            FileRenameController controller = (FileRenameController) WindowTools.openStage(Fxmls.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(index, newFile);
                });
            });
            controller.set(file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void fileRenamed(int index, File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            changeFile(info, newFile);
            tableData.set(index, info);
            imageFileList.set(index, newFile);
            if (displayMode == DisplayMode.ImagesGrid) {
                imageTitleList.get(index).setText(newFile.getName());
            } else if (displayMode == DisplayMode.FilesList || displayMode == DisplayMode.ThumbnailsList) {
                tableView.refresh();
            }
            if (index == currentIndex) {
                super.fileRenamed(newFile);
                filenameLabel.setText(newFile.getAbsolutePath());
            } else {
                recordFileOpened(newFile);
                popInformation(MessageFormat.format(Languages.message("FileRenamed"), file.getAbsolutePath(), newFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void deleteFilesAction() {
        try {
            if (selectedIndexes == null || selectedIndexes.isEmpty()) {
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
                if (!PopTools.askSure(getMyStage().getTitle(), Languages.message("SureDelete"))) {
                    return;
                }
            }
            int count = 0;
            for (int index : selectedIndexes) {
                ImageInformation info = tableData.get(index);
                File file = info.getImageFileInformation().getFile();
                if (FileDeleteTools.delete(file)) {
                    imageFileList.remove(file);
                    count++;
                }
            }
            popInformation(Languages.message("TotalDeletedFiles") + ": " + count);
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void delete(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
                if (!PopTools.askSure(getMyStage().getTitle(), Languages.message("SureDelete"))) {
                    return;
                }
            }
            ImageInformation info = tableData.get(index);
            File file = info.getImageFileInformation().getFile();
            if (FileDeleteTools.delete(file)) {
                imageFileList.remove(file);
            }
            popSuccessful();
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void info(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ControllerTools.showImageInformation(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void meta(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ControllerTools.showImageMetaData(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void makeImagesPane() {
        try {
            imagesPane.getChildren().clear();
            imageBoxList = new ArrayList<>();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedIndexes = new ArrayList<>();
            rowsNum = 0;

            if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
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

    private void makeImagesGrid() {
        if (colsNum <= 0 || displayMode != DisplayMode.ImagesGrid) {
            return;
        }

        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    private void makeGridBox() {
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
            vbox.setPadding(new Insets(5, 5, 5, 5));
            line.getChildren().add(vbox);

            ScrollPane sPane = new ScrollPane();
            sPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(sPane, Priority.ALWAYS);
            HBox.setHgrow(sPane, Priority.ALWAYS);
            sPane.setPannable(true);
            sPane.setFitToWidth(true);
            sPane.setFitToHeight(true);

            HBox iBox = new HBox();
            iBox.setAlignment(Pos.TOP_CENTER);
            iBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(iBox, Priority.ALWAYS);
            HBox.setHgrow(iBox, Priority.ALWAYS);
            sPane.setContent(iBox);

            ImageView iView = new ImageView();
            iView.setPreserveRatio(true);
            iBox.getChildren().add(iView);

            Label titleLabel = new Label();
            titleLabel.setWrapText(true);
            VBox.setVgrow(titleLabel, Priority.NEVER);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            vbox.getChildren().add(titleLabel);
            vbox.getChildren().add(sPane);
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

    }

    protected void popImageMenu(int index, ImageView iView, MouseEvent event) {
        if (iView == null || iView.getImage() == null) {
            return;
        }
        if (index >= tableData.size()) {
            return;
        }

        ImageInformation info = tableData.get(index);

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(info.getFile().getAbsolutePath());
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        if (!info.isIsMultipleFrames()) {

            menu = new MenuItem(Languages.message("RotateLeft"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 270);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("RotateRight"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 90);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("RotateLeft"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 270);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("TurnOver"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 180);
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(Languages.message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(iView, event.getScreenX(), event.getScreenY());

    }

    private void makeSourceTable() {
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

    protected void popTableMenu(MouseEvent event, int index) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(Languages.message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("RotateRight"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 90);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("TurnOver"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 180);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(Languages.message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    private void makeListBox() {
        try {
            if (displayMode != DisplayMode.ThumbnailsList && displayMode != DisplayMode.FilesList) {
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
                task = new SingletonTask<Void>() {

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
                handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void loadImageInfos() {
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

    private ImageInformation loadImageInfo(File file) {
        ImageInformation imageInfo;
        ImageFileInformation finfo = ImageInformation.readImageFileInformation(file);
        if (finfo == null) {
            return null;
        }
        imageInfo = finfo.getImageInformation();
        if (displayMode != DisplayMode.FilesList) {
            imageInfo.loadThumbnail(thumbWidth);
        }
        return imageInfo;
    }

    @Override
    public void makeImageNevigator() {
    }

    private void makeImagesNevigator(boolean makeCurrentList) {
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

    @FXML
    @Override
    public void selectSourcePath() {
        File defaultPath = UserConfig.getUserConfigPath(baseName + "SourcePath");
        selectSourcePath(defaultPath);
    }

    @Override
    public void selectSourcePath(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            fileChooser.setTitle("Select multiple files");
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileOpened(files.get(0));

            loadImages(files);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void loadFiles(List<String> fileNames) {
        try {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); ++i) {
                File file = new File(fileNames.get(i));
                files.add(file);
            }
            loadImages(files);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files) {
        try {
            imageFileList.clear();
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.exists() && file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
                filesNumber = imageFileList.size();
                colsNum = (int) Math.sqrt(filesNumber);
                colsNum = Math.max(colsNum, filesNumber / colsNum);
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files, int cols) {
        try {
            imageFileList.clear();
            colsNum = cols;
            if (files != null && cols > 0) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(File path, int number) {
        try {
            imageFileList.clear();
            if (path != null && path.isDirectory() && path.exists() && number > 0) {
                File[] pfiles = path.listFiles();
                if (pfiles != null) {
                    for (File file : pfiles) {
                        if (file.isFile() && FileTools.isSupportedImage(file)) {
                            imageFileList.add(file);
                            if (imageFileList.size() == number || imageFileList.size() >= maxShow) {
                                break;
                            }
                        }
                    }
                }
                if (!imageFileList.isEmpty()) {
                    filesNumber = imageFileList.size();
                    colsNum = (int) Math.sqrt(filesNumber);
                    colsNum = Math.max(colsNum, filesNumber / colsNum);
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void loadImages() {
        try {
            path = null;
            filesNumber = 0;
            totalLabel.setText("");
            getMyStage().setTitle(getBaseTitle());
            if (imageFileList == null || imageFileList.isEmpty() || colsNum <= 0) {
                return;
            }
            isSettingValues = true;
            path = imageFileList.get(0).getParentFile();
            filesBox.getItems().clear();
            int total = 0;
            File[] pfiles = path.listFiles();
            if (pfiles != null) {
                for (File file : pfiles) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        total++;
                    }
                }
            }
            List<Integer> fvalues = Arrays.asList(9, 16, 12, 15, 25, 4, 3, 8, 6, 10,
                    36, 30, 24, 48);
            for (int fn : fvalues) {
                if (fn <= total) {
                    filesBox.getItems().add(fn + "");
                }
            }
            if (!filesBox.getItems().contains(total + "")) {
                if (filesBox.getItems().size() > 6) {
                    filesBox.getItems().add(6, total + "");
                } else {
                    filesBox.getItems().add(total + "");
                }
            }
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            if (!colsnumBox.getItems().contains(colsNum + "")) {
                colsnumBox.getItems().add(0, colsNum + "");
            }
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;

            getMyStage().setTitle(getBaseTitle() + " " + path.getAbsolutePath());
            totalLabel.setText("/" + total);
            displayMode = DisplayMode.ImagesGrid;

            makeImagesNevigator(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public ImagesBrowserController refreshInterfaceAndFile() {
        super.refreshInterface();
        makeImagesNevigator(true);
        return this;
    }

    @FXML
    protected void filesListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(Languages.message("FilesList"));
    }

    @FXML
    protected void thumbsListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(Languages.message("ThumbnailsList"));
    }

    @FXML
    protected void gridAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select("" + (colsNum > 0 ? colsNum : 3));
    }

    @FXML
    public void selectAllImages() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().selectAll();
        } else {
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                selectedIndexes.add(i);
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

}
