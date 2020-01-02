package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesBrowserController extends ImageViewerController {

    private final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    private final ObservableList<ImageInformation> tableData = FXCollections.observableArrayList();
    private int rowsNum, colsNum, filesNumber, popSize = 500;
    private List<VBox> imageBoxList;
    private List<ScrollPane> imageScrollList;
    private List<ImageView> imageViewList;
    private List<Label> imageTitleList;
    private Popup imagePop;
    private ImageView popView;
    private Text popText;

    private TableView<ImageInformation> tableView;
    private TableColumn<ImageInformation, Image> imageColumn;
    private TableColumn<ImageInformation, String> fileColumn, formatColumn, pixelsColumn, csColumn, loadColumn;
    private TableColumn<ImageInformation, Integer> indexColumn;
    private TableColumn<ImageInformation, Long> fileSizeColumn, modifiedTimeColumn, createTimeColumn;
    private TableColumn<ImageInformation, Boolean> isSampledColumn, isScaledColumn, isMutipleFramesColumn;

    protected List<File> nextFiles, previousFiles;
    protected List<Integer> selectedIndexes;
    protected List<ImageInformation> selectedImages;
    protected String ImageSortTypeKey = "ImageSortType";
    protected int maxShow = 100;
    private File path;
    private DisplayMode displayMode;

    private enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList, None
    }

    @FXML
    protected VBox imagesPane, mainBox;
    @FXML
    protected HBox fileOpBox, zoomBox;
    @FXML
    protected FlowPane rotatePane;
    @FXML
    protected ComboBox<String> colsnumBox, filesBox, popSizeSelector;
    @FXML
    private CheckBox saveRotationCheck, popCheck;
    @FXML
    private Label totalLabel;
    @FXML
    private Button filesListButton, thumbsListButton;
    @FXML
    protected ToggleGroup popGroup;

    public ImagesBrowserController() {
        baseTitle = AppVariables.message("ImagesBrowser");

        TipsLabelKey = "ImagesBrowserTips";
        defaultLoadWidth = 512;
    }

    @Override
    public void initializeNext2() {
        try {
            colsNum = -1;
            currentAngle = 0;
            displayMode = DisplayMode.ImagesGrid;

            List<String> cvalues = Arrays.asList("3", "4", "6",
                    AppVariables.message("ThumbnailsList"),
                    AppVariables.message("FilesList"),
                    "2", "5", "7", "8", "9", "10",
                    "16", "25", "20", "12", "15");
            colsnumBox.getItems().addAll(cvalues);
            colsnumBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        if (AppVariables.message("ThumbnailsList").equals(newValue)) {
                            displayMode = DisplayMode.ThumbnailsList;
                        } else if (AppVariables.message("FilesList").equals(newValue)) {
                            displayMode = DisplayMode.FilesList;
                        } else {
                            if (displayMode != DisplayMode.ImagesGrid) {
                                tableData.clear();
                            }
                            displayMode = DisplayMode.ImagesGrid;
                            colsNum = Integer.valueOf(newValue);
                            if (colsNum >= 0) {
                                FxmlControl.setEditorNormal(colsnumBox);
                            } else {
                                FxmlControl.setEditorBadStyle(colsnumBox);
                                return;
                            }
                        }
                        makeImagesPane();
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(colsnumBox);
                    }
                }
            });

            filesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        filesNumber = Integer.valueOf(newValue);
                        if (filesNumber > 0) {
                            FxmlControl.setEditorNormal(filesBox);
                            makeImagesNevigator(true);

                        } else {
                            FxmlControl.setEditorBadStyle(filesBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(filesBox);
                    }
                }
            });

            List<String> sizeValues = Arrays.asList("500", "400", "600", "300", "200", "700");
            popSizeSelector.getItems().addAll(sizeValues);
            popSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            FxmlControl.setEditorNormal(popSizeSelector);
                            popSize = v;
                            AppVariables.setUserConfigInt("ImageBrowserPopSize", popSize);
                        } else {
                            FxmlControl.setEditorBadStyle(popSizeSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(popSizeSelector);
                    }
                }
            });
            popSizeSelector.getSelectionModel().select(AppVariables.getUserConfigInt("ImageBrowserPopSize", 500) + "");

            popCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImagesBrowserPop", newValue);
                }
            });
            popCheck.setSelected(AppVariables.getUserConfigBoolean("ImagesBrowserPop", true));

            fileBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            viewPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            browsePane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            tipsPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            mainBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            leftPaneControl.visibleProperty().bind(Bindings.isEmpty(imageFileList).not());

            FxmlControl.setTooltip(filesListButton, new Tooltip(message("FilesList")));
            FxmlControl.setTooltip(thumbsListButton, new Tooltip(message("ThumbnailsList")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void setLoadWidth() {
        makeImagesPane();
    }

    public void paneSize(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null
                || sPane == null) {
            return;
        }
        FxmlControl.paneSize(sPane, iView);
    }

    public void imageSize(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.imageSize(sPane, iView);
    }

    public void zoomIn(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.zoomIn(sPane, iView, xZoomStep, yZoomStep);
    }

    public void zoomOut(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.zoomOut(sPane, iView, xZoomStep, yZoomStep);
    }

    public void moveRight(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, -40, sPane.getVvalue());
    }

    public void moveLeft(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, 40, sPane.getVvalue());
    }

    public void moveUp(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, sPane.getHvalue(), 40);
    }

    public void moveDown(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, sPane.getHvalue(), -40);
    }

    @Override
    public void rotate(final int rotateAngle) {
        currentAngle = rotateAngle;
        if (saveRotationCheck.isSelected()) {
            saveRotation(rotateAngle);
            return;
        }
        switch (displayMode) {
            case FilesList:
                break;
            case ThumbnailsList:
            case ImagesGrid:
                if (selectedIndexes == null || selectedIndexes.isEmpty()) {
                    for (int i = 0; i < imageViewList.size(); i++) {
                        ImageView iView = imageViewList.get(i);
                        iView.setRotate(iView.getRotate() + currentAngle);
                    }
                } else {
                    for (int i = 0; i < selectedIndexes.size(); i++) {
                        ImageView iView = imageViewList.get(selectedIndexes.get(i));
                        iView.setRotate(iView.getRotate() + currentAngle);
                    }
                }
                break;
        }

    }

    public void saveRotation(final int rotateAngle) {
        if (!saveRotationCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<Integer> selected;

                @Override
                protected boolean handle() {
                    selected = new ArrayList<>();
                    if (selectedIndexes == null || selectedIndexes.isEmpty()) {
                        for (int i = 0; i < tableData.size(); i++) {
                            ImageInformation info = tableData.get(i);
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            tableData.set(i, newInfo);
                        }
                    } else {
                        selected.addAll(selectedIndexes);
                        for (int i = 0; i < selectedIndexes.size(); i++) {
                            int index = selectedIndexes.get(i);
                            ImageInformation info = tableData.get(index);
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            tableData.set(index, newInfo);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (displayMode == DisplayMode.ImagesGrid) {
                        for (int i = 0; i < imageViewList.size(); i++) {
                            ImageView iView = imageViewList.get(i);
                            iView.setImage(tableData.get(i).getImage());
                        }
                    } else {
                        tableView.refresh();
                        for (int i = 0; i < selected.size(); i++) {
                            tableView.getSelectionModel().select(selected.get(i));
                        }
                    }

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private ImageInformation saveRotation(ImageInformation info, int rotateAngle) {
        if (info == null || info.getImageFileInformation() == null || info.isIsMultipleFrames()) {
            return null;
        }
        try {
            File file = info.getImageFileInformation().getFile();
            BufferedImage bufferedImage = ImageFileReaders.readImage(file);
            bufferedImage = ImageManufacture.rotateImage(bufferedImage, rotateAngle);
            ImageFileWriters.writeImageFile(bufferedImage, file);
            ImageInformation newInfo = loadImageInfo(file);
            return newInfo;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void paneSize() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                paneSize(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                paneSize(i);
            }
        }
    }

    @FXML
    @Override
    public void loadedSize() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                imageSize(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                imageSize(i);
            }
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                zoomIn(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                zoomIn(i);
            }
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                zoomOut(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                zoomOut(i);
            }
        }
    }

    @FXML
    @Override
    public void moveRight() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveRight(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                moveRight(i);
            }
        }

    }

    @FXML
    @Override
    public void moveLeft() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveLeft(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                moveLeft(i);
            }
        }
    }

    @FXML
    @Override
    public void moveUp() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveUp(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                moveUp(i);
            }
        }
    }

    @FXML
    @Override
    public void moveDown() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveDown(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); i++) {
                moveDown(i);
            }
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

    @FXML
    public void viewAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            ImageInformation info = selectedImages.get(0);
            if (info != null) {
                File file = info.getImageFileInformation().getFile();
                FxmlStage.openImageViewer(null, file);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void renameAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            ImageInformation info = selectedImages.get(0);
            if (info != null) {
                File file = info.getImageFileInformation().getFile();
                String oname = file.getAbsolutePath();
                File newFile = renameFile(file);
                if (newFile != null) {
                    String nname = newFile.getAbsolutePath();
                    int index = selectedIndexes.get(0);
                    imageFileList.set(index, newFile);
                    makeImagesNevigator(true);
                    popInformation(MessageFormat.format(AppVariables.message("FileRenamed"), oname, nname));
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void deleteFilesAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(AppVariables.message("SureDelete"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                }
            }
            int count = 0;
            for (ImageInformation info : selectedImages) {
                File file = info.getImageFileInformation().getFile();
                if (file.delete()) {
                    imageFileList.remove(file);
                    count++;
                }
            }
            popInformation(AppVariables.message("TotalDeletedFiles") + ": " + count);
            makeImagesNevigator(true);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            ImageInformation info = selectedImages.get(0);
            if (info != null) {
                showImageInformation(info);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeImagesPane() {
        try {
            imagesPane.getChildren().clear();
            imageBoxList = new ArrayList<>();
//        imageInfoList.clear();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedImages = new ArrayList<>();
            selectedIndexes = new ArrayList<>();
            fileOpBox.setDisable(true);
            rowsNum = 0;

            if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
                zoomBox.setDisable(true);
                rotatePane.setDisable(true);
                loadWidthBox.setDisable(true);
                makeListBox();

            } else if (colsNum > 0) {
                zoomBox.setDisable(false);
                rotatePane.setDisable(false);
                loadWidthBox.setDisable(false);
                makeImagesGrid();
            }
            FxmlControl.refreshStyle(thisPane);

        } catch (Exception e) {
            logger.debug(e.toString());
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
            if (task != null) {
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
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void makeGridBox() {
        int num = tableData.size();
        HBox line = new HBox();
        for (int i = 0; i < num; i++) {
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

            final ImageView iView = new ImageView();
            iView.setPreserveRatio(true);
            iBox.getChildren().add(iView);
            Label titleLabel = new Label();
            titleLabel.setWrapText(true);
            VBox.setVgrow(titleLabel, Priority.NEVER);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            vbox.getChildren().add(titleLabel);
            vbox.getChildren().add(sPane);

            final ImageInformation imageInfo = tableData.get(i);
            final File file = imageInfo.getImageFileInformation().getFile();
            final Image iImage = imageInfo.getImage();
            iView.setImage(iImage);

            String title = file.getName();
            if (imageInfo.isIsMultipleFrames()) {
                title += " " + AppVariables.message("MultipleFrames");
                titleLabel.setStyle("-fx-text-box-border: red;   -fx-text-fill: red;");
            }
            if (imageInfo.isIsSampled()) {
                title += " " + AppVariables.message("Sampled");
                titleLabel.setStyle("-fx-text-box-border: red;   -fx-text-fill: red;");
            }
//            if (imageInfo.isIsScaled()) {
//                title += " " + AppVariables.message("Scaled");
//            }
            titleLabel.setText(title);

            final int index = i;
            vbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (selectedImages.contains(imageInfo)) {
                        selectedImages.remove(imageInfo);
                        vbox.setStyle(null);
                    } else {
                        selectedImages.add(imageInfo);
                        vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                    }
                    Integer o = Integer.valueOf(index);
                    if (selectedIndexes.contains(o)) {
                        selectedIndexes.remove(o);
                    } else {
                        selectedIndexes.add(o);
                    }
                    fileOpBox.setDisable(selectedImages.isEmpty());
                    viewButton.setDisable(selectedImages.size() > 1);
                    infoButton.setDisable(selectedImages.size() > 1);
                    renameButton.setDisable(selectedImages.size() > 1);

                    if (event.getClickCount() > 1) {
                        FxmlStage.openImageViewer(null, file);
                    }
                }
            });

            vbox.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    String str1 = imageInfo.getFileName() + " "
                            + AppVariables.message("Format") + ":" + imageInfo.getImageFormat() + "  "
                            + AppVariables.message("ModifyTime") + ":" + DateTools.datetimeToString(file.lastModified()) + " "
                            + AppVariables.message("Size") + ":" + FileTools.showFileSize(file.length()) + "  "
                            + AppVariables.message("Pixels") + ":" + imageInfo.getWidth() + "x" + imageInfo.getHeight() + "  "
                            + AppVariables.message("LoadedSize") + ":"
                            + (int) iView.getImage().getWidth() + "x" + (int) iView.getImage().getHeight() + "  "
                            + AppVariables.message("DisplayedSize") + ":"
                            + (int) iView.getFitWidth() + "x" + (int) iView.getFitHeight();
                    bottomLabel.setText(str1);
                    if (imagePop != null) {
                        imagePop.hide();
                    }
                    if (popCheck.isSelected()) {
                        makeImagePopup(vbox, imageInfo, iView);
                    }
                }
            });

            tableData.add(imageInfo);
            imageScrollList.add(sPane);
            imageViewList.add(iView);
            imageTitleList.add(titleLabel);
            imageBoxList.add(vbox);

        }

        for (int i = 0; i < num; i++) {
            double w = imagesPane.getWidth() / colsNum - 5;
            double h = imagesPane.getHeight() / rowsNum - 5;
            VBox vbox = imageBoxList.get(i);
            vbox.setPrefWidth(w);
            vbox.setPrefHeight(h);
        }
        // https://stackoverflow.com/questions/26152642/get-the-height-of-a-node-in-javafx-generate-a-layout-pass
        imagesPane.applyCss();
        imagesPane.layout();
        paneSize();

    }

    private void makeImagePopup(VBox imageBox, ImageInformation imageInfo, ImageView iView) {
        try {
            File file = imageInfo.getImageFileInformation().getFile();
            final Image iImage = imageInfo.getImage();
            imagePop = new Popup();
            imagePop.setWidth(popSize + 40);
            imagePop.setHeight(popSize + 40);
            imagePop.setAutoHide(true);

            VBox vbox = new VBox();
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setMaxHeight(Double.MAX_VALUE);
            vbox.setStyle("-fx-background-color: white;");
            imagePop.getContent().add(vbox);

            popView = new ImageView();
            popView.setImage(iImage);
            if (iImage.getWidth() > iImage.getHeight()) {
                popView.setFitWidth(popSize);
            } else {
                popView.setFitHeight(popSize);
            }
            popView.setPreserveRatio(true);
            vbox.getChildren().add(popView);

            popText = new Text();
            popText.setStyle("-fx-font-size: 1.0em;");

            vbox.getChildren().add(popText);
            vbox.setPadding(new Insets(15, 15, 15, 15));

            String info = imageInfo.getFileName() + "\n"
                    + AppVariables.message("Format") + ":" + imageInfo.getImageFormat() + "  "
                    + AppVariables.message("ModifyTime") + ":" + DateTools.datetimeToString(file.lastModified()) + "\n"
                    + AppVariables.message("Size") + ":" + FileTools.showFileSize(file.length()) + "  "
                    + AppVariables.message("Pixels") + ":" + imageInfo.getWidth() + "x" + imageInfo.getHeight() + "  "
                    + AppVariables.message("LoadedSize") + ":"
                    + (int) iView.getImage().getWidth() + "x" + (int) iView.getImage().getHeight() + "  "
                    + AppVariables.message("DisplayedSize") + ":"
                    + (int) iView.getFitWidth() + "x" + (int) iView.getFitHeight();
            popText.setText(info);
            popText.setWrappingWidth(popSize);
            Bounds bounds = imageBox.localToScreen(imageBox.getBoundsInLocal());
            imagePop.show(imageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY());

        } catch (Exception e) {
            logger.debug(e.toString());
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

            fileColumn = new TableColumn<>(AppVariables.message("File"));
            fileColumn.setPrefWidth(220);
            formatColumn = new TableColumn<>(AppVariables.message("Format"));
            formatColumn.setPrefWidth(60);
            csColumn = new TableColumn<>(AppVariables.message("Color"));
            csColumn.setPrefWidth(120);
            indexColumn = new TableColumn<>(AppVariables.message("Index"));
            pixelsColumn = new TableColumn<>(AppVariables.message("Pixels"));
            pixelsColumn.setPrefWidth(140);
            fileSizeColumn = new TableColumn<>(AppVariables.message("Size"));
            fileSizeColumn.setPrefWidth(140);
            isMutipleFramesColumn = new TableColumn<>(AppVariables.message("MultipleFrames"));
            modifiedTimeColumn = new TableColumn<>(AppVariables.message("ModifiedTime"));
            modifiedTimeColumn.setPrefWidth(200);
            createTimeColumn = new TableColumn<>(AppVariables.message("CreateTime"));
            createTimeColumn.setPrefWidth(200);

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            formatColumn.setCellValueFactory(new PropertyValueFactory<>("imageFormat"));
            csColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            pixelsColumn.setCellValueFactory(new PropertyValueFactory<>("pixelsString"));
            fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            fileSizeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(TableColumn<ImageInformation, Long> param) {
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
                public TableCell<ImageInformation, Boolean> call(TableColumn<ImageInformation, Boolean> param) {
                    TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(AppVariables.message(item.toString()));
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
                public TableCell<ImageInformation, Long> call(TableColumn<ImageInformation, Long> param) {
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
                public TableCell<ImageInformation, Long> call(TableColumn<ImageInformation, Long> param) {
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
                imageColumn = new TableColumn<>(AppVariables.message("Image"));
                loadColumn = new TableColumn<>(AppVariables.message("LoadedSize"));
                loadColumn.setPrefWidth(140);
                isSampledColumn = new TableColumn<>(AppVariables.message("Sampled"));
                isScaledColumn = new TableColumn<>(AppVariables.message("Scaled"));

                imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
                imageColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Image>, TableCell<ImageInformation, Image>>() {
                    @Override
                    public TableCell<ImageInformation, Image> call(TableColumn<ImageInformation, Image> param) {
                        final ImageView iview = new ImageView();
                        iview.setPreserveRatio(true);
                        iview.setFitWidth(100);
                        iview.setFitHeight(100);

                        TableCell<ImageInformation, Image> cell = new TableCell<ImageInformation, Image>() {
                            @Override
                            protected void updateItem(final Image item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null || getTableRow() == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                iview.setImage(item);
                                setGraphic(iview);
                                int row = getTableRow().getIndex();
                                for (int i = imageViewList.size(); i <= row; i++) {
                                    imageViewList.add(iview);
                                }
                            }
                        };
                        return cell;
                    }
                });

                isSampledColumn.setCellValueFactory(new PropertyValueFactory<>("isSampled"));
                isSampledColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                    @Override
                    public TableCell<ImageInformation, Boolean> call(TableColumn<ImageInformation, Boolean> param) {
                        TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(final Boolean item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                text.setText(AppVariables.message(item.toString()));
                                if (item) {
                                    text.setFill(Color.RED);
                                }
                                setGraphic(text);
                            }
                        };
                        return cell;
                    }
                });
                isScaledColumn.setCellValueFactory(new PropertyValueFactory<>("isScaled"));
                isScaledColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                    @Override
                    public TableCell<ImageInformation, Boolean> call(TableColumn<ImageInformation, Boolean> param) {
                        TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                            @Override
                            protected void updateItem(final Boolean item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                setText(AppVariables.message(item.toString()));
                            }
                        };
                        return cell;
                    }
                });
                loadColumn.setCellValueFactory(new PropertyValueFactory<>("loadSizeString"));

                tableView.getColumns().addAll(imageColumn, fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn, loadColumn,
                        isMutipleFramesColumn, indexColumn, isSampledColumn, isScaledColumn, modifiedTimeColumn, createTimeColumn);
            } else {
                tableView.getColumns().addAll(fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn,
                        isMutipleFramesColumn, indexColumn, modifiedTimeColumn, createTimeColumn);
            }

            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        selectedImages = tableView.getSelectionModel().getSelectedItems();
                        selectedIndexes = tableView.getSelectionModel().getSelectedIndices();
                        fileOpBox.setDisable(selectedImages.isEmpty());
                        viewButton.setDisable(selectedImages.size() > 1);
                        infoButton.setDisable(selectedImages.size() > 1);
                        renameButton.setDisable(selectedImages.size() > 1);
                    }
                }
            });
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    ImageInformation info = tableView.getSelectionModel().getSelectedItem();
                    if (info == null) {
                        return;
                    }
                    File file = info.getImageFileInformation().getFile();
                    if (event.getClickCount() > 1) {
                        FxmlStage.openImageViewer(null, file);
                    }
                }
            });
        } catch (Exception e) {
            logger.debug(e.toString());
        }
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
                if (task != null) {
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
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void loadImageInfos() {
        try {
            Map<String, ImageInformation> oldList = new HashMap<>();
            for (ImageInformation info : tableData) {
                oldList.put(info.getFileName(), info);
            }
            tableData.clear();
            ImageInformation imageInfo;
            int width;
            if (displayMode == DisplayMode.ImagesGrid) {
                width = loadWidth;
            } else {
                width = 100;
            }
            for (int i = 0; i < imageFileList.size(); i++) {
                File file = imageFileList.get(i);
                imageInfo = oldList.get(file.getAbsolutePath());
                if (imageInfo == null || imageInfo.getImageFileInformation() == null) {
                    if (displayMode == DisplayMode.FilesList) {
                        ImageFileInformation finfo = ImageInformation.loadImageFileInformation(file);
                        if (finfo == null) {
                            continue;
                        }
                        imageInfo = finfo.getImageInformation();
                    } else {
                        imageInfo = ImageInformation.loadImage(file, width, 0);
                    }
                } else {
                    if (displayMode != DisplayMode.FilesList && imageInfo.getImage() == null) {
                        imageInfo = ImageInformation.loadImage(file, width, 0);
                    }
                }

                if (imageInfo == null) {
                    continue;
                }
                tableData.add(imageInfo);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private ImageInformation loadImageInfo(File file) {
        ImageInformation imageInfo;
        int width;
        if (displayMode == DisplayMode.ImagesGrid) {
            width = loadWidth;
        } else {
            width = 100;
        }
        if (displayMode == DisplayMode.FilesList) {
            ImageFileInformation finfo = ImageInformation.loadImageFileInformation(file);
            if (finfo == null) {
                return null;
            }
            imageInfo = finfo.getImageInformation();
        } else {
            imageInfo = ImageInformation.loadImage(file, width, 0);
        }
        return imageInfo;
    }

    @Override
    public void makeImageNevigator() {
        makeImagesNevigator(true);
    }

    private void makeImagesNevigator(boolean makeCurrentList) {
        if (isSettingValues) {
            return;
        }
        previousFiles = new ArrayList<>();
        nextFiles = new ArrayList<>();
        try {
            if (imageFileList != null && !imageFileList.isEmpty() && filesNumber > 0) {
                loadingController = openHandlingStage(Modality.WINDOW_MODAL);

                File firstFile = imageFileList.get(0);
                path = firstFile.getParentFile();
                List<File> pathFiles = new ArrayList<>();
                for (File file : path.listFiles()) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        pathFiles.add(file);
                    }
                }
                FileTools.sortFiles(pathFiles, sortMode);
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
                    for (int i = start; i <= end; i++) {
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

                    for (int i = max - 1; i >= 0; i--) {
                        String fname = pathFnames.get(i);
                        if (!iFnames.contains(fname)) {
                            previousFiles.add(0, new File(fname));
                            if (previousFiles.size() == filesNumber) {
                                break;
                            }
                        }
                    }

                    for (int i = min + 1; i < pathFnames.size(); i++) {
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
            logger.debug(e.toString());
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
    protected void selectImages(ActionEvent event) {
        File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
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
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileOpened(files.get(0));

            loadImages(files);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void loadFiles(List<String> fileNames) {
        try {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); i++) {
                File file = new File(fileNames.get(i));
                files.add(file);
            }
            loadImages(files);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImages(List<File> files) {
        try {
            imageFileList.clear();
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); i++) {
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
            logger.error(e.toString());
        }
    }

    public void loadImages(List<File> files, int cols) {
        try {
            imageFileList.clear();
            colsNum = cols;
            if (files != null && cols > 0) {
                for (int i = 0; i < files.size(); i++) {
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
            logger.error(e.toString());
        }
    }

    public void loadImages(File path, int number) {
        try {
            imageFileList.clear();
            if (path != null && path.isDirectory() && path.exists() && number > 0) {
                for (File file : path.listFiles()) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() == number || imageFileList.size() >= maxShow) {
                            break;
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
            logger.error(e.toString());
        }
    }

    private void loadImages() {
        try {
            path = null;
            filesNumber = 0;
            currentAngle = 0;
            totalLabel.setText("");
            getMyStage().setTitle(getBaseTitle());
            if (imageFileList == null || imageFileList.isEmpty() || colsNum <= 0) {
                return;
            }
            isSettingValues = true;
            path = imageFileList.get(0).getParentFile();
            filesBox.getItems().clear();
            int total = 0;
            for (File file : path.listFiles()) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    total++;
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
            logger.error(e.toString());
        }
    }

    @Override
    public ImagesBrowserController refresh() {
        List<File> oldNames = new ArrayList<>();
        for (File f : imageFileList) {
            oldNames.add(f);
        }
        int oldCols = colsNum;

        ImagesBrowserController c = (ImagesBrowserController) refreshBase();
        if (c == null) {
            return null;
        }
        if (!oldNames.isEmpty() && oldCols > 0) {
            c.loadImages(oldNames, oldCols);
        }

        return c;
    }

    @FXML
    protected void filesListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(AppVariables.message("FilesList"));
    }

    @FXML
    protected void thumbsListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(AppVariables.message("ThumbnailsList"));
    }

}
