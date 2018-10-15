package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageManufactureValues;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.OperationType;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.FxmlImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController extends ImageViewerController {

    protected ScrollPane refPane, scopePane;
    protected ImageView refView, scopeView;
    protected Label refLabel;
    protected VBox refBox, scopeBox;

    protected ImageManufactureValues values;
    protected boolean isSettingValues, scopePaneValid, isSwitchingTab;

    protected String initTab;
    protected TextField scopeText;
    protected int stageWidth, stageHeight;

    protected class ImageManufactureParameters {

    }

    @FXML
    protected ToolBar hotBar;
    @FXML
    protected Tab fileTab, viewTab, colorTab, filtersTab, watermarkTab, cropTab, arcTab, shadowTab, effectsTab;
    @FXML
    protected Tab replaceColorTab, sizeTab, refTab, browseTab, transformTab, cutMarginsTab, addMarginsTab;
    @FXML
    protected Label tipsLabel;
    @FXML
    protected Button selectRefButton, saveButton, recoverButton, undoButton, redoButton;
    @FXML
    protected CheckBox showRefCheck, showScopeCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected HBox hotBox;
    @FXML
    protected VBox imageBox;
    @FXML
    protected ComboBox hisBox;

    public ImageManufactureController() {
        sourcePathKey = "ImageSourcePathKey";

    }

    @Override
    protected void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCommon() {
        try {
            values = new ImageManufactureValues();
            values.setRefSync(true);

            browseTab.setDisable(true);
            viewTab.setDisable(true);
            colorTab.setDisable(true);
            effectsTab.setDisable(true);
            filtersTab.setDisable(true);
            replaceColorTab.setDisable(true);
            sizeTab.setDisable(true);
            refTab.setDisable(true);
            transformTab.setDisable(true);
            watermarkTab.setDisable(true);
            arcTab.setDisable(true);
            shadowTab.setDisable(true);
            cutMarginsTab.setDisable(true);
            addMarginsTab.setDisable(true);
            cropTab.setDisable(true);

            hotBar.setDisable(true);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldTab, Tab newTab) {
                    if (isSettingValues) {
                        return;
                    }
                    hidePopup();
                    switchTab(newTab);
                }
            });

            Tooltip tips = new Tooltip(getMessage("ImageManufactureTips"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(tipsLabel, tips);

            if (showScopeCheck != null) {
                tips = new Tooltip(getMessage("ShowScopeComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(showScopeCheck, tips);
                showScopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                        setScopePane();
                    }
                });
            }

            tips = new Tooltip(getMessage("ImageRefTips"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(showRefCheck, tips);
            showRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

//            hisBox.getItems().addAll(Arrays.asList(AppVaribles.getMessage("CacheSetting")));
//            hisBox.valueProperty().addListener(new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue ov, String oldValue, String newValue) {
//
//                }
//            });
//            hisBox.getSelectionModel().select(0);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkReferenceImage() {
        try {
            values.setShowRef(showRefCheck.isSelected());

            if (values.isShowRef()) {

                if (refPane == null) {
                    refPane = new ScrollPane();
                    refPane.setPannable(true);
                    refPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    VBox.setVgrow(refPane, Priority.ALWAYS);
                    HBox.setHgrow(refPane, Priority.ALWAYS);
                }
                if (refView == null) {
                    refView = new ImageView();
                    refView.setPreserveRatio(true);
                    refView.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (values.getRefInfo() == null) {
                                return;
                            }
                            String str = AppVaribles.getMessage("Format") + ":" + values.getRefInfo().getImageFormat() + "  "
                                    + AppVaribles.getMessage("Pixels") + ":" + values.getRefInfo().getxPixels() + "x" + values.getRefInfo().getyPixels();
                            if (values.getRefInfo().getFile() != null) {
                                str += "  " + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(values.getRefInfo().getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(values.getRefInfo().getFile().lastModified());
                            }
                            bottomLabel.setText(str);
                        }
                    });
                    refPane.setContent(refView);
                }

                if (refBox == null) {
                    refBox = new VBox();
                    VBox.setVgrow(refBox, Priority.ALWAYS);
                    HBox.setHgrow(refBox, Priority.ALWAYS);
                    refLabel = new Label();
                    refLabel.setText(getMessage("Reference"));
                    refLabel.setAlignment(Pos.CENTER);
                    VBox.setVgrow(refLabel, Priority.NEVER);
                    HBox.setHgrow(refLabel, Priority.ALWAYS);
                    refBox.getChildren().add(0, refLabel);
                    refBox.getChildren().add(1, refPane);
                }

                if (values.getRefFile() == null) {
                    values.setRefFile(values.getSourceFile());
                }
                if (values.getRefImage() == null) {
                    loadReferenceImage();
                } else {
                    refView.setImage(values.getRefImage());
                    if (values.getRefInfo() != null) {
//                            logger.debug(scrollPane.getHeight() + " " + refInfo.getyPixels());
                        if (scrollPane.getHeight() < values.getRefInfo().getyPixels()) {
                            refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
//                                refView.setFitWidth(scrollPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(values.getRefInfo().getyPixels());
//                                refView.setFitWidth(refInfo.getxPixels());
                        }
                    }
                }

                if (!splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().add(0, refBox);
                }

            } else {

                if (refBox != null && splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().remove(refBox);
                }

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

        setSplitPane();

    }

    protected void loadReferenceImage() {
        if (values.getRefFile() == null || values.getSourceFile() == null) {
            return;
        }
        if (values.getRefFile().getAbsolutePath().equals(values.getSourceFile().getAbsolutePath())) {
            values.setRefImage(image);
            values.setRefInfo(values.getImageInfo());
            refView.setImage(image);
            if (scrollPane.getHeight() < values.getImageInfo().getyPixels()) {
                refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
                refView.setFitWidth(scrollPane.getWidth() - 1);
            } else {
                refView.setFitHeight(values.getImageInfo().getyPixels());
                refView.setFitWidth(values.getImageInfo().getxPixels());
            }
            return;
        }
        Task refTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                values.setRefInfo(ImageFileReaders.readImageMetaData(values.getRefFile().getAbsolutePath()));
                values.setRefImage(SwingFXUtils.toFXImage(ImageIO.read(values.getRefFile()), null));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refView.setImage(values.getRefImage());
                        if (refPane.getHeight() < values.getRefInfo().getyPixels()) {
                            refView.setFitHeight(refPane.getHeight() - 5);
                            refView.setFitWidth(refPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(values.getRefInfo().getyPixels());
                            refView.setFitWidth(values.getRefInfo().getxPixels());
                        }
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(refTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(refTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected void setImageChanged(boolean imageChanged) {
        values.setImageChanged(imageChanged);
        if (imageChanged) {
            saveButton.setDisable(false);
            recoverButton.setDisable(false);
            undoButton.setDisable(false);
            redoButton.setDisable(true);
            getMyStage().setTitle(getBaseTitle() + "  " + values.getSourceFile().getAbsolutePath() + "*");
        } else {
            saveButton.setDisable(true);
            recoverButton.setDisable(true);
            if (values.getSourceFile() != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + values.getSourceFile().getAbsolutePath());
            }
        }
        setBottomLabel();
    }

    protected void switchTab(Tab newTab) {

        String tabName = null;
        if (fileTab.equals(newTab)) {
            tabName = "file";
        } else if (sizeTab.equals(newTab)) {
            tabName = "size";
        } else if (cropTab.equals(newTab)) {
            tabName = "crop";
        } else if (filtersTab.equals(newTab)) {
            tabName = "filters";
        } else if (effectsTab.equals(newTab)) {
            tabName = "effects";
        } else if (colorTab.equals(newTab)) {
            tabName = "color";
        } else if (replaceColorTab.equals(newTab)) {
            tabName = "replaceColor";
        } else if (watermarkTab.equals(newTab)) {
            tabName = "watermark";
        } else if (arcTab.equals(newTab)) {
            tabName = "arc";
        } else if (shadowTab.equals(newTab)) {
            tabName = "shadow";
        } else if (transformTab.equals(newTab)) {
            tabName = "transform";
        } else if (cutMarginsTab.equals(newTab)) {
            tabName = "cutMargins";
        } else if (addMarginsTab.equals(newTab)) {
            tabName = "addMargins";
        } else if (viewTab.equals(newTab)) {
            tabName = "view";
        } else if (refTab.equals(newTab)) {
            tabName = "ref";
        } else if (browseTab.equals(newTab)) {
            tabName = "browse";
        }
        switchTab(tabName);
    }

    protected void switchTab(String tabName) {
        try {
            isSwitchingTab = true;
            String fxml = null;
            switch (tabName) {
                case "file":
                    fxml = CommonValues.ImageManufactureFileFxml;
                    break;
                case "size":
                    fxml = CommonValues.ImageManufactureSizeFxml;
                    break;
                case "crop":
                    fxml = CommonValues.ImageManufactureCropFxml;
                    break;
                case "color":
                    fxml = CommonValues.ImageManufactureColorFxml;
                    break;
                case "filters":
                    fxml = CommonValues.ImageManufactureFiltersFxml;
                    break;
                case "effects":
                    fxml = CommonValues.ImageManufactureEffectsFxml;
                    break;
                case "replaceColor":
                    fxml = CommonValues.ImageManufactureReplaceColorFxml;
                    break;
                case "watermark":
                    fxml = CommonValues.ImageManufactureWatermarkFxml;
                    break;
                case "arc":
                    fxml = CommonValues.ImageManufactureArcFxml;
                    break;
                case "shadow":
                    fxml = CommonValues.ImageManufactureShadowFxml;
                    break;
                case "transform":
                    fxml = CommonValues.ImageManufactureTransformFxml;
                    break;
                case "cutMargins":
                    fxml = CommonValues.ImageManufactureCutMarginsFxml;
                    break;
                case "addMargins":
                    fxml = CommonValues.ImageManufactureAddMarginsFxml;
                    break;
                case "view":
                    fxml = CommonValues.ImageManufactureViewFxml;
                    break;
                case "ref":
                    fxml = CommonValues.ImageManufactureRefFxml;
                    break;
                case "browse":
                    fxml = CommonValues.ImageManufactureBrowseFxml;
                    break;
            }

            if (fxml != null) {
                values.setStageWidth(stageWidth);
                values.setStageHeight(stageHeight);
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(fxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setValues(values);
                controller.setTab(tabName);
                controller.initInterface();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setTab(String tab) {
        try {
            if (tab == null) {
                return;
            }
            isSettingValues = true;
            switch (tab) {
                case "file":
                    tabPane.getSelectionModel().select(fileTab);
                    break;
                case "size":
                    tabPane.getSelectionModel().select(sizeTab);
                    break;
                case "crop":
                    tabPane.getSelectionModel().select(cropTab);
                    break;
                case "color":
                    tabPane.getSelectionModel().select(colorTab);
                    break;
                case "filters":
                    tabPane.getSelectionModel().select(filtersTab);
                    break;
                case "effects":
                    tabPane.getSelectionModel().select(effectsTab);
                    break;
                case "replaceColor":
                    tabPane.getSelectionModel().select(replaceColorTab);
                    break;
                case "watermark":
                    tabPane.getSelectionModel().select(watermarkTab);
                    break;
                case "arc":
                    tabPane.getSelectionModel().select(arcTab);
                    break;
                case "shadow":
                    tabPane.getSelectionModel().select(shadowTab);
                    break;
                case "transform":
                    tabPane.getSelectionModel().select(transformTab);
                    break;
                case "cutMargins":
                    tabPane.getSelectionModel().select(cutMarginsTab);
                    break;
                case "addMargins":
                    tabPane.getSelectionModel().select(addMarginsTab);
                    break;
                case "view":
                    tabPane.getSelectionModel().select(viewTab);
                    break;
                case "ref":
                    tabPane.getSelectionModel().select(refTab);
                    break;
                case "browse":
                    tabPane.getSelectionModel().select(browseTab);
                    break;
            }
            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            isSettingValues = true;

            sourceFile = values.getSourceFile();
            image = values.getImage();
            imageInformation = values.getImageInfo();

//            if (values.getStageWidth() > 0) {
//                getMyStage().setWidth(values.getStageWidth());
//            }
//            if (values.getStageHeight() > 0) {
//                getMyStage().setHeight(values.getStageHeight());
//            }
            cropTab.setDisable(false);
            colorTab.setDisable(false);
            filtersTab.setDisable(false);
            effectsTab.setDisable(false);
            arcTab.setDisable(false);
            shadowTab.setDisable(false);
            replaceColorTab.setDisable(false);
            sizeTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            transformTab.setDisable(false);
            watermarkTab.setDisable(false);
            cutMarginsTab.setDisable(false);
            addMarginsTab.setDisable(false);
            viewTab.setDisable(false);
            browseTab.setDisable(false);

            undoButton.setDisable(true);
            redoButton.setDisable(true);

            imageView.setImage(values.getCurrentImage());
            imageView.setCursor(Cursor.OPEN_HAND);
            fitSize();
            setBottomLabel();
            setImageChanged(values.isImageChanged());

            showRefCheck.setSelected(values.isShowRef());

            getMyStage().widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue,
                        Number oldWidth, Number newWidth) {
                    stageWidth = newWidth.intValue();
                }
            });
            getMyStage().heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue,
                        Number oldHeight, Number newHeight) {
                    stageHeight = newHeight.intValue();
                }
            });

            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }
            isSettingValues = true;

            values.setSourceFile(sourceFile);
            values.setImage(image);
            values.setImageInfo(imageInformation);
            values.setCurrentImage(image);
            setImageChanged(false);

            if (initTab != null) {
                switchTab(initTab);
            } else {
                initInterface();
            }

            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    //  Hotbar Methods
    @FXML
    public void save() {
        if (saveButton.isDisabled()) {
            return;
        }
        if (values.isIsConfirmBeforeSave()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAs();
                return;
            }

        }

        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String format = values.getImageInfo().getImageFormat();
                final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(values.getCurrentImage());
                ImageFileWriters.writeImageFile(bufferedImage, format, values.getSourceFile().getAbsolutePath());
                imageInformation = ImageFileReaders.readImageMetaData(values.getSourceFile().getAbsolutePath());
                image = values.getCurrentImage();
                values.setImage(image);
                values.setImageInfo(imageInformation);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        setImageChanged(false);
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void saveAs() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(targetPathKey, file.getParent());

            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(values.getCurrentImage());
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Load) {
                                sourceFileChanged(file);

                            } else if (values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Open) {
                                openImageManufactureInNew(file.getAbsolutePath());
                            }
                        }
                    });
                    return null;
                }
            };
            openHandlingStage(saveTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void zoomIn() {
        try {
            super.zoomIn();
            if (values.isRefSync() && refView != null) {
                refView.setFitWidth(imageView.getFitWidth());
                refView.setFitHeight(imageView.getFitWidth());
            }
            if (scopeView != null) {
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        super.zoomOut();
        if (values.isRefSync() && refView != null) {
            refView.setFitWidth(imageView.getFitWidth());
            refView.setFitHeight(imageView.getFitWidth());
        }
        if (scopeView != null) {
            scopeView.setFitWidth(imageView.getFitWidth());
            scopeView.setFitHeight(imageView.getFitHeight());
        }
    }

    @FXML
    @Override
    public void imageSize() {
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);
        if (values.isRefSync() && refView != null) {
            refView.setFitHeight(-1);
            refView.setFitWidth(-1);
        }
        if (scopeView != null) {
            scopeView.setFitHeight(-1);
            scopeView.setFitWidth(-1);
        }
    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitWidth(scrollPane.getWidth() - 5);
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        if (values.isRefSync() && refView != null) {
            refView.setFitWidth(scrollPane.getWidth() - 5);
            refView.setFitHeight(scrollPane.getHeight() - 5);
        }
        if (scopeView != null) {
            scopeView.setFitWidth(scrollPane.getWidth() - 5);
            scopeView.setFitHeight(scrollPane.getHeight() - 5);
        }
    }

    // Common Methods
    @FXML
    public void setBottomLabel() {
        if (values == null || values.getImageInfo() == null || values.getCurrentImage() == null) {
            return;
        }
        String str = AppVaribles.getMessage("Format") + ":" + values.getImageInfo().getImageFormat() + "  "
                + AppVaribles.getMessage("Pixels") + ":" + values.getImageInfo().getxPixels() + "x" + values.getImageInfo().getyPixels() + "  "
                + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(values.getImageInfo().getFile().length()) + "  "
                + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(values.getImageInfo().getFile().lastModified()) + "  "
                + AppVaribles.getMessage("CurrentPixels") + ":" + (int) values.getCurrentImage().getWidth() + "x" + (int) values.getCurrentImage().getHeight();
        bottomLabel.setText(str);
    }

    @FXML
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);
    }

    @FXML
    public void recovery() {
        imageView.setImage(values.getImage());
        values.setUndoImage(values.getCurrentImage());
        values.setCurrentImage(values.getImage());
        setImageChanged(false);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    @FXML
    public void undoAction() {
        if (values.getUndoImage() == null) {
            undoButton.setDisable(true);
        }
        values.setRedoImage(values.getCurrentImage());
        values.setCurrentImage(values.getUndoImage());
        imageView.setImage(values.getUndoImage());
        setImageChanged(true);
        undoButton.setDisable(true);
        redoButton.setDisable(false);
    }

    @FXML
    public void redoAction() {
        if (values.getRedoImage() == null) {
            redoButton.setDisable(true);
        }
        values.setUndoImage(values.getCurrentImage());
        values.setCurrentImage(values.getRedoImage());
        imageView.setImage(values.getRedoImage());
        setImageChanged(true);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            if ("s".equals(key) || "S".equals(key)) {  // ctrl-s
                if (!saveButton.isDisabled()) {
                    save();
                }
            } else if ("r".equals(key) || "R".equals(key)) {  // ctrl-r
                if (!recoverButton.isDisabled()) {
                    recovery();
                }
            }
        }
    }

    public void setScope(ImageScope imageScope) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageScopeFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImageScopeController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();

            imageScope.setImage(values.getCurrentImage());
            String title = AppVaribles.getMessage("ImageManufactureScope");
            switch (imageScope.getOperationType()) {
                case OperationType.Color:
                    title += " - " + AppVaribles.getMessage("Color");
                    break;
                case OperationType.ReplaceColor:
                    title += " - " + AppVaribles.getMessage("ReplaceColor");
                    break;
                case OperationType.Filters:
                    title += " - " + AppVaribles.getMessage("Filters");
                    break;
                case OperationType.Crop:
                    title += " - " + AppVaribles.getMessage("Crop");
                    break;
                default:
                    break;
            }
            controller.loadImage(this, imageScope, title);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setScopePane() {
        try {
            if (showScopeCheck == null) {
                return;
            }

            if (values.getCurrentScope() == null) {
                values.setScopeImage(null);
                values.setScopeInfo(null);

            } else {
                if (values.getCurrentScope().isAll()) {
                    values.setScopeImage(values.getCurrentImage());
                } else {
                    values.setScopeImage(values.getCurrentScope().getImage());
                }

                ImageFileInformation scopeInfo = new ImageFileInformation();
                scopeInfo.setImageFormat(values.getImageInfo().getImageFormat());
                scopeInfo.setxPixels(values.getImageInfo().getxPixels());
                scopeInfo.setyPixels(values.getImageInfo().getyPixels());
                values.setScopeInfo(scopeInfo);
            }

            if (scopePaneValid && showScopeCheck.isSelected()) {

                if (scopePane == null) {
                    scopePane = new ScrollPane();
                    scopePane.setPannable(true);
                    scopePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    VBox.setVgrow(scopePane, Priority.ALWAYS);
                    HBox.setHgrow(scopePane, Priority.ALWAYS);
                }
                if (scopeView == null) {
                    scopeView = new ImageView();
                    scopeView.setPreserveRatio(true);
                    scopeView.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            String str = AppVaribles.getMessage("Format") + ":" + values.getScopeInfo().getImageFormat() + "  "
                                    + AppVaribles.getMessage("Pixels") + ":" + values.getScopeInfo().getxPixels() + "x" + values.getScopeInfo().getyPixels();
                            if (values.getScopeInfo().getFile() != null) {
                                str += "  " + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(values.getScopeInfo().getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(values.getScopeInfo().getFile().lastModified());
                            }
                            bottomLabel.setText(str);
                        }
                    });
                    scopePane.setContent(scopeView);
                }

                if (scopeBox == null) {
                    scopeBox = new VBox();
                    VBox.setVgrow(scopeBox, Priority.ALWAYS);
                    HBox.setHgrow(scopeBox, Priority.ALWAYS);
                    scopeText = new TextField();
                    scopeText.setAlignment(Pos.CENTER_LEFT);
                    scopeText.setEditable(false);
                    scopeText.setStyle("-fx-text-fill: #961c1c;");
                    VBox.setVgrow(scopeText, Priority.NEVER);
                    HBox.setHgrow(scopeText, Priority.ALWAYS);
                    scopeBox.getChildren().add(0, scopeText);
                    scopeBox.getChildren().add(1, scopePane);
                }
                scopeText.setText(getMessage("CurrentScope") + ":"
                        + ImageScopeController.getScopeText(values.getCurrentScope()));

                Tooltip stips = new Tooltip(getMessage("ScopeImageComments"));
                stips.setFont(new Font(16));
                FxmlTools.quickTooltip(scopeBox, stips);

                scopeView.setImage(values.getScopeImage());

                if (!splitPane.getItems().contains(scopeBox)) {
                    splitPane.getItems().add(0, scopeBox);

                }

            } else {
                if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {
                    splitPane.getItems().remove(scopeBox);
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

        setSplitPane();

    }

    protected void setSplitPane() {
        switch (splitPane.getItems().size()) {
            case 3:
                splitPane.getDividers().get(0).setPosition(0.33333);
                splitPane.getDividers().get(1).setPosition(0.66666);
//                splitPane.setDividerPositions(0.33, 0.33, 0.33); // This way not work!
                break;
            case 2:
                splitPane.getDividers().get(0).setPosition(0.5);
//               splitPane.setDividerPositions(0.5, 0.5); // This way not work!
                break;
            default:
                splitPane.setDividerPositions(1);
                break;
        }
        splitPane.layout();
        fitSize();
    }

    public void scopeDetermined(ImageScope imageScope) {
        values.setCurrentScope(imageScope);
        setScopePane();
    }

    @Override
    public boolean stageReloading() {
        if (isSwitchingTab) {
            return true;
        }
        return checkSavingBeforeExit();
    }

    @Override
    public boolean stageClosing() {
        if (!checkSavingBeforeExit()) {
            return false;
        }
        return super.stageClosing();
    }

    public boolean checkSavingBeforeExit() {
        if (values.isImageChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ImageChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                save();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else if (result.get() == buttonSaveAs) {
                saveAs();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String getInitTab() {
        return initTab;
    }

    public void setInitTab(String initTab) {
        this.initTab = initTab;
    }

    public ImageManufactureValues getValues() {
        return values;
    }

    public void setValues(final ImageManufactureValues values) {
        this.values = values;
    }

}
