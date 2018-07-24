package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import mara.mybox.image.ImageConverter;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.FxmlTools.ImageManufactureType;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected int saturateStep = 5, brightnessStep = 5, hueStep = 5, percent = 50;
    protected int saturateOffset = 0, brightnessOffset = 0, hueOffset = 0;
    protected File nextFile, lastFile, refFile;
    protected String ImageSortTypeKey, ImageOpenAfterSaveAsKey, ImageReferenceDisplayKey;
    protected ScrollPane refPane;
    protected ImageView refView;
    protected Image refImage;
    protected ImageFileInformation refInfo;
    private boolean noRatio, isScale;
    private float scale = 1.0f;
    private int width, height;
    private SimpleBooleanProperty changed;

    @FXML
    protected ToolBar fileBar, navBar, refBar, hotBar, scaleBar;
    @FXML
    protected Tab fileTab, zoomTab, hueTab, saturateTab, brightnessTab, filtersTab, replaceColorTab, pixelsTab, refTab, browseTab;
    @FXML
    protected Slider zoomSlider, rotateSlider, hueSlider, saturateSlider, brightnessSlider, binarySlider;
    @FXML
    protected Label zoomValue, rotateValue, hueValue, saturateValue, brightnessValue, binaryValue, tipsLabel;
    @FXML
    protected ToggleGroup sortGroup, pixelsGroup;
    @FXML
    protected Button nextButton, lastButton, origImageButton, selectRefButton, pixelsOkButton, saveButton, recoverButton;
    @FXML
    protected CheckBox openCheck, saveCheck, displayRefCheck, refSyncCheck, keepRatioCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected TextField widthInput, heightInput, scaleInput;
    @FXML
    protected ChoiceBox ratioBox;
    @FXML
    private TabPane tabPane;

    public ImageManufactureController() {
        ImageSortTypeKey = "ImageSortType";
        ImageOpenAfterSaveAsKey = "ImageOpenAfterSaveAs";
        ImageReferenceDisplayKey = "ImageReferenceDisplay";

    }

    @Override
    protected void initializeNext2() {
        try {
            attributes = new ImageAttributes();

            fileBar.setDisable(true);
            navBar.setDisable(true);
            zoomTab.setDisable(true);
            hueTab.setDisable(true);
            saturateTab.setDisable(true);
            brightnessTab.setDisable(true);
            filtersTab.setDisable(true);
//            replaceColorTab.setDisable(true);
            pixelsTab.setDisable(true);
            refTab.setDisable(true);
            hotBar.setDisable(true);
            browseTab.setDisable(true);

            zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    zoomStep = newValue.intValue();
                    zoomValue.setText(zoomStep + "%");
                }
            });

            rotateSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateValue.setText(newValue.intValue() + "");
                    rotateAngle = newValue.intValue();
                }
            });

            saturateSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    saturateStep = newValue.intValue();
                    saturateValue.setText(saturateStep + "%");
                }
            });

            hueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    hueStep = newValue.intValue();
                    hueValue.setText(hueStep + "");
                }
            });

            brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    brightnessStep = newValue.intValue();
                    brightnessValue.setText(brightnessStep + "%");
                }
            });

            binarySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    percent = newValue.intValue();
                    binaryValue.setText(percent + "%");
                }
            });

            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkNevigator();
                    RadioButton selected = (RadioButton) sortGroup.getSelectedToggle();
                    AppVaribles.setConfigValue(ImageSortTypeKey, selected.getText());
                }
            });
            FxmlTools.setRadioSelected(sortGroup, AppVaribles.getConfigValue(ImageSortTypeKey, getMessage("FileName")));

            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue(ImageOpenAfterSaveAsKey, openCheck.isSelected());
                }
            });
            openCheck.setSelected(AppVaribles.getConfigBoolean(ImageOpenAfterSaveAsKey));

            pixelsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
                    if (getMessage("Pixels").equals(selected.getText())) {
                        scaleBar.setDisable(false);
                        isScale = false;
                    } else {
                        scaleBar.setDisable(true);
                        isScale = true;
                        widthInput.setStyle(null);
                        heightInput.setStyle(null);
                    }
                }
            });

            ratioBox.getItems().addAll(FXCollections.observableArrayList(getMessage("BaseOnLarger"),
                    getMessage("BaseOnWidth"), getMessage("BaseOnHeight"), getMessage("BaseOnSmaller")));
            ratioBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkRatioAdjustion(newValue);
                    }
                }
            });
            ratioBox.getSelectionModel().select(0);

            scaleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        scale = Float.valueOf(scaleInput.getText());
                        if (scale > 0) {
                            scaleInput.setStyle(null);
                            noRatio = true;
                            final Image currentImage = imageView.getImage();
                            widthInput.setText(Math.round(currentImage.getWidth() * scale) + "");
                            heightInput.setText(Math.round(currentImage.getHeight() * scale) + "");
                            noRatio = false;
                        } else {
                            scaleInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        scaleInput.setStyle(badStyle);
                    }
                }
            });

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        width = Integer.valueOf(widthInput.getText());
                        if (width > 0) {
                            widthInput.setStyle(null);
                            checkRatio();
                        } else {
                            widthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        widthInput.setStyle(badStyle);
                    }
                }
            });

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        height = Integer.valueOf(heightInput.getText());
                        if (height > 0) {
                            heightInput.setStyle(null);
                            checkRatio();
                        } else {
                            heightInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        heightInput.setStyle(badStyle);
                    }
                }
            });

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    if (keepRatioCheck.isSelected()) {
                        checkRatio();
                    }
                }
            });

            pixelsOkButton.disableProperty().bind(
                    widthInput.styleProperty().isEqualTo(badStyle)
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
            );

            displayRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

            Tooltip tips = new Tooltip(AppVaribles.getMessage("ImageManufactureTips"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(tipsLabel, tips);

            changed = new SimpleBooleanProperty(false);
            changed.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (changed.getValue()) {
                        saveButton.setDisable(false);
                        recoverButton.setDisable(false);
                        getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath() + "*");
                    } else {
                        saveButton.setDisable(true);
                        recoverButton.setDisable(true);
                        getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        if (image != null) {
            fileBar.setDisable(false);
            navBar.setDisable(false);
            zoomTab.setDisable(false);
            hueTab.setDisable(false);
            saturateTab.setDisable(false);
            brightnessTab.setDisable(false);
            filtersTab.setDisable(false);
//            replaceColorTab.setDisable(false);
            pixelsTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            browseTab.setDisable(false);

            widthInput.setText(imageInformation.getxPixels() + "");
            heightInput.setText(imageInformation.getyPixels() + "");
            attributes.setSourceWidth(imageInformation.getxPixels());
            attributes.setSourceHeight(imageInformation.getyPixels());

            checkNevigator();
            straighten();
            showLabel();

            changed.set(false);
        }
    }

    @FXML
    public void recovery() {
        imageView.setImage(image);
        changed.set(false);
        showLabel();
    }

    @FXML
    public void increaseSaturate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeSaturate(currentImage, saturateStep / 100.0f);
                saturateOffset += saturateStep;
                if (saturateOffset > 100) {
                    saturateOffset = 100;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void decreaseSaturate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeSaturate(currentImage, 0.0f - saturateStep / 100.0f);
                saturateOffset -= saturateStep;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void increaseHue() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeHue(currentImage, hueStep);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void decreaseHue() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeHue(currentImage, 0 - hueStep);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void increaseBrightness() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeBrightness(currentImage, brightnessStep / 100.0f);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void decreaseBrightness() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.changeBrightness(currentImage, 0.0f - brightnessStep / 100.0f);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void setInvert() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.manufactureImage(currentImage, ImageManufactureType.Invert);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void setGray() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.manufactureImage(currentImage, ImageManufactureType.Gray);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void setBinary() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final WritableImage newImage = FxmlTools.makeBinary(currentImage, percent);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void pixelsCalculator() {
        try {
            attributes.setKeepRatio(keepRatioCheck.isSelected());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculatorFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            PixelsCalculationController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            controller.setSource(attributes, widthInput, heightInput);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.setTitle(AppVaribles.getMessage("PixelsCalculator"));
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            noRatio = true;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    noRatio = false;
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void setOriginalSize() {
        noRatio = true;
        if (imageInformation.getxPixels() > 0) {
            widthInput.setText(imageInformation.getxPixels() + "");
        }
        if (imageInformation.getyPixels() > 0) {
            heightInput.setText(imageInformation.getyPixels() + "");
        }
        noRatio = false;
    }

    @FXML
    protected void pixelsAction() {
        if (isScale) {
            setScale();
        } else {
            setPixels();
        }
    }

    @FXML
    public void save() {
        if (saveCheck.isSelected()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }
        try {
            final Image currentImage = imageView.getImage();
            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = imageInformation.getImageFormat();
                    final BufferedImage changedImage = FxmlTools.readImage(currentImage);
                    ImageIO.write(changedImage, format, sourceFile);
                    imageInformation = ImageFileReaders.readImageMetaData(sourceFile.getAbsolutePath());
                    image = currentImage;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            changed.set(false);
                            showLabel();
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
            AppVaribles.setConfigValue(targetPathKey, file.getParent());

            final Image currentImage = imageView.getImage();
            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlTools.readImage(currentImage);
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (openCheck.isSelected()) {
                                showImageManufacture(file.getAbsolutePath());
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
    public void next() {
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void last() {
        if (lastFile != null) {
            loadImage(lastFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void browseAction() {
        try {
            if (!stageReloading()) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImagesViewerFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImagesViewerController controller = fxmlLoader.getController();
            controller.setMyStage(myStage);
            myStage.setScene(new Scene(pane));
            myStage.setTitle(getMessage("AppTitle"));
            controller.loadImages(sourceFile.getParentFile(), 10);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void pickColor() {
        imageView.setCursor(Cursor.HAND);
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {

                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });
    }

    @FXML
    public void calculateThreshold() {
        int threshold = ImageGrayTools.calculateThreshold(sourceFile);
        percent = threshold * 100 / 256;
        binarySlider.setValue(percent);
    }

    @FXML
    @Override
    public void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 + zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 + zoomStep / 100.0f));
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refView.getFitHeight() * (1 + zoomStep / 100.0f));
            refView.setFitWidth(refView.getFitWidth() * (1 + zoomStep / 100.0f));
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 - zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 - zoomStep / 100.0f));
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refView.getFitHeight() * (1 - zoomStep / 100.0f));
            refView.setFitWidth(refView.getFitWidth() * (1 - zoomStep / 100.0f));
        }
    }

    @FXML
    @Override
    public void imageSize() {
        final Image currentImage = imageView.getImage();
        imageView.setFitWidth(currentImage.getWidth());
        imageView.setFitHeight(currentImage.getHeight());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refInfo.getyPixels());
            refView.setFitWidth(refInfo.getxPixels());
        }
    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refPane.getHeight() - 5);
            refView.setFitWidth(refPane.getWidth() - 1);
        }
    }

    @FXML
    @Override
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, -40, refPane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, 40, refPane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), 40);
        }
    }

    @FXML
    @Override
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), -40);
        }
    }

    @FXML
    @Override
    public void rotateRight() {
        currentAngle = (currentAngle + rotateAngle) % 360;
        imageView.setRotate(currentAngle);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setRotate(currentAngle);
        }
    }

    @FXML
    @Override
    public void rotateLeft() {
        currentAngle = (360 - rotateAngle + currentAngle) % 360;
        imageView.setRotate(currentAngle);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setRotate(currentAngle);
        }
    }

    @FXML
    @Override
    public void turnOver() {
        currentAngle = (180 + currentAngle) % 360;
        imageView.setRotate(currentAngle);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setRotate(currentAngle);
        }
    }

    @FXML
    @Override
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setRotate(currentAngle);
        }
    }

    @FXML
    public void selectReference() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(sourcePathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            refFile = file;
            AppVaribles.setConfigValue("LastPath", sourceFile.getParent());
            AppVaribles.setConfigValue(sourcePathKey, sourceFile.getParent());

            loadReferenceImage();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void originalImage() {
        refFile = sourceFile;
        loadReferenceImage();
    }

    @FXML
    public void showLabel() {
        final Image currentImage = imageView.getImage();
        if (imageInformation == null || currentImage == null) {
            return;
        }
        String str = AppVaribles.getMessage("Format") + ":" + imageInformation.getImageFormat() + "  "
                + AppVaribles.getMessage("Pixels") + ":" + imageInformation.getxPixels() + "x" + imageInformation.getyPixels() + "  "
                + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(imageInformation.getFile().length()) + "  "
                + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(imageInformation.getFile().lastModified()) + "  "
                + AppVaribles.getMessage("CurrentPixels") + ":" + (int) currentImage.getWidth() + "x" + (int) currentImage.getHeight();
        bottomLabel.setText(str);
    }

    @FXML
    public void popRefInformation() {
        showImageInformation(refInfo);
    }

    @FXML
    public void popRefMeta() {
        showImageMetaData(refInfo);
    }

    private void checkNevigator() {
        if (sourceFile == null) {
            lastFile = null;
            lastButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
            return;
        }
        File path = sourceFile.getParentFile();
        List<File> sortedFiles = new ArrayList<>();
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isFile() && FileTools.isSupportedImage(file)) {
                sortedFiles.add(file);
            }
        }
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.FileName);

        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.CreateTime);

        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.ModifyTime);

        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.Size);
        }

        for (int i = 0; i < sortedFiles.size(); i++) {
            if (sortedFiles.get(i).getAbsoluteFile().equals(sourceFile.getAbsoluteFile())) {
                if (i < sortedFiles.size() - 1) {
                    nextFile = sortedFiles.get(i + 1);
                    nextButton.setDisable(false);
                } else {
                    nextFile = null;
                    nextButton.setDisable(true);
                }
                if (i > 0) {
                    lastFile = sortedFiles.get(i - 1);
                    lastButton.setDisable(false);
                } else {
                    lastFile = null;
                    lastButton.setDisable(true);
                }
                return;
            }
        }
        lastFile = null;
        lastButton.setDisable(true);
        nextFile = null;
        nextButton.setDisable(true);
    }

    private void checkReferenceImage() {
        try {
            if (displayRefCheck.isSelected()) {
                if (splitPane.getItems().size() == 1) {
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
                                if (refInfo == null) {
                                    return;
                                }
                                String str = AppVaribles.getMessage("Format") + ":" + refInfo.getImageFormat() + "  "
                                        + AppVaribles.getMessage("Pixels") + ":" + refInfo.getxPixels() + "x" + refInfo.getyPixels() + "  "
                                        + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(refInfo.getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(refInfo.getFile().lastModified());
                                bottomLabel.setText(str);
                            }
                        });
                    }
                    refPane.setContent(refView);
                    splitPane.getItems().add(0, refPane);
                    splitPane.setDividerPositions(0.5);
                    refBar.setDisable(false);

                    if (refFile == null) {
                        refFile = sourceFile;
                    }
                    if (refImage == null) {
                        loadReferenceImage();
                    }

                }
            } else {
                if (splitPane.getItems().size() == 2) {
                    splitPane.getItems().remove(0);
                    refBar.setDisable(true);
                }
            }

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void loadReferenceImage() {
        if (refFile == null || sourceFile == null) {
            return;
        }
        if (refFile.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
            refImage = image;
            refInfo = imageInformation;
            refView.setImage(refImage);
            if (refPane.getHeight() < refInfo.getyPixels()) {
                refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
                refView.setFitWidth(scrollPane.getWidth() - 1);
            } else {
                refView.setFitHeight(refInfo.getyPixels());
                refView.setFitWidth(refInfo.getxPixels());
            }
            return;
        }
        Task refTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                refInfo = ImageFileReaders.readImageMetaData(refFile.getAbsolutePath());
                refImage = SwingFXUtils.toFXImage(ImageIO.read(refFile), null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refView.setImage(refImage);
                        if (refPane.getHeight() < refInfo.getyPixels()) {
                            refView.setFitHeight(refPane.getHeight() - 5);
                            refView.setFitWidth(refPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(refInfo.getyPixels());
                            refView.setFitWidth(refInfo.getxPixels());
                        }
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

    protected void checkRatioAdjustion(String s) {
        try {
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        try {
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
            attributes.setTargetWidth(width);
            attributes.setTargetHeight(height);
            int sourceX = imageInformation.getxPixels();
            int sourceY = imageInformation.getyPixels();
            if (noRatio || !keepRatioCheck.isSelected() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(width * 1000 / sourceX);
            long ratioY = Math.round(height * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            switch (attributes.getRatioAdjustion()) {
                case ImageConverter.KeepRatioType.BaseOnWidth:
                    heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    break;
                case ImageConverter.KeepRatioType.BaseOnHeight:
                    widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    break;
                case ImageConverter.KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    } else {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    }
                    break;
                case ImageConverter.KeepRatioType.BaseOnSmaller:
                    if (ratioX > ratioY) {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    } else {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    }
                    break;
                default:
                    break;
            }
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void setScale() {
        if (scale == 1.0 || scale <= 0) {
            return;
        }
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeScale(currentImage, scale);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                        showLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void setPixels() {
        if (width <= 0 || height <= 0) {
            return;
        }
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changePixels(currentImage, width, height);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        changed.set(true);
                        showLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
