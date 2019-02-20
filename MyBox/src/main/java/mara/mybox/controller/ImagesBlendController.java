package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.fxml.image.ImageTools;
import mara.mybox.image.ImageBlend.ImagesBlendMode;
import mara.mybox.image.ImageBlend.ImagesRelativeLocation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.data.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2018-10-31
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesBlendController extends ImageViewerController {

    protected String ImageBlendFileTypeKey;
    private ImagesRelativeLocation location;
    private ImagesBlendMode blendMode;
    private int x, y;
    private boolean isAreaValid;
    private float opacity;

    private File foreFile, backFile, targetFile;
    private Image foreImage, backImage;
    private ImageInformation foreInfo, backInfo;

    @FXML
    private VBox mainPane, targetBox;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane foreScroll, backScroll;
    @FXML
    private ImageView foreView, backView;
    @FXML
    private HBox foreBox, backBox, opacityHBox, toolBox;
    @FXML
    private ToggleGroup locationGroup;
    @FXML
    private Button openTargetButton;
    @FXML
    private ComboBox<String> targetTypeBox, blendModeBox, opacityBox;
    @FXML
    private Label foreTitle, foreLabel, backTitle, backLabel, pointLabel;
    @FXML
    private Button newWindowButton;
    @FXML
    private TextField pointX, pointY;
    @FXML
    private CheckBox intersectOnlyCheck;

    public ImagesBlendController() {
        ImageBlendFileTypeKey = "ImageBlendFileType";
    }

    @Override
    protected void initializeNext2() {
        try {
            initSourcesSection();
            initOptionsSection();
            initTargetSection();

            targetBox.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSourcesSection() {
        try {

            foreBox.setDisable(true);
            backBox.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initOptionsSection() {
        try {
            locationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLocation();
                }
            });
            location = ImagesRelativeLocation.Foreground_In_Background;
            pointLabel.setText(AppVaribles.getMessage("ClickOnBackgournd"));

            blendModeBox.getItems().addAll(Arrays.asList(AppVaribles.getMessage("NormalMode"),
                    AppVaribles.getMessage("DissolveMode"), AppVaribles.getMessage("MultiplyMode"), AppVaribles.getMessage("ScreenMode"),
                    AppVaribles.getMessage("OverlayMode"), AppVaribles.getMessage("HardLightMode"), AppVaribles.getMessage("SoftLightMode"),
                    AppVaribles.getMessage("ColorDodgeMode"), AppVaribles.getMessage("LinearDodgeMode"), AppVaribles.getMessage("DivideMode"),
                    AppVaribles.getMessage("ColorBurnMode"), AppVaribles.getMessage("LinearBurnMode"), AppVaribles.getMessage("VividLightMode"),
                    AppVaribles.getMessage("LinearLightMode"), AppVaribles.getMessage("SubtractMode"), AppVaribles.getMessage("DifferenceMode"),
                    AppVaribles.getMessage("ExclusionMode"), AppVaribles.getMessage("DarkenMode"), AppVaribles.getMessage("LightenMode"),
                    AppVaribles.getMessage("HueMode"), AppVaribles.getMessage("SaturationMode"), AppVaribles.getMessage("ColorMode"),
                    AppVaribles.getMessage("LuminosityMode")
            ));
            blendModeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    checkBlendMode(newValue);
                }
            });
            blendMode = null;

            pointX.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPoint();
                }
            });
            pointY.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPoint();
                }
            });
            isAreaValid = true;

            opacityBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        opacity = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacityBox.getEditor().setStyle(null);
                            blendImages();
                        } else {
                            opacity = 0.5f;
                            opacityBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        opacity = 0.5f;
                        opacityBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            opacityBox.getSelectionModel().select(0);
            opacityHBox.setDisable(true);

            intersectOnlyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    blendImages();
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkLocation() {
        if (foreImage == null || backImage == null) {
            return;
        }
        RadioButton selected = (RadioButton) locationGroup.getSelectedToggle();
        if (AppVaribles.getMessage("FinB").equals(selected.getText())) {
            location = ImagesRelativeLocation.Foreground_In_Background;
            pointLabel.setText(AppVaribles.getMessage("ClickOnBackgournd"));
            bottomLabel.setText(AppVaribles.getMessage("BlendedSize") + ": "
                    + (int) backImage.getWidth() + "*" + (int) backImage.getHeight());

        } else if (AppVaribles.getMessage("BinF").equals(selected.getText())) {
            location = ImagesRelativeLocation.Background_In_Foreground;
            pointLabel.setText(AppVaribles.getMessage("ClickOnForegournd"));
            bottomLabel.setText(AppVaribles.getMessage("BlendedSize") + ": "
                    + (int) foreImage.getWidth() + "*" + (int) foreImage.getHeight());

        } else {
            return;
        }
        pointX.setText("0");
        pointY.setText("0");
    }

    private void checkBlendMode(String mode) {
        opacityHBox.setDisable(true);
        if (AppVaribles.getMessage("NormalMode").equals(mode)) {
            blendMode = ImagesBlendMode.NORMAL;
            opacityHBox.setDisable(false);

        } else if (AppVaribles.getMessage("DissolveMode").equals(mode)) {
            blendMode = ImagesBlendMode.DISSOLVE;

        } else if (AppVaribles.getMessage("MultiplyMode").equals(mode)) {
            blendMode = ImagesBlendMode.MULTIPLY;

        } else if (AppVaribles.getMessage("ScreenMode").equals(mode)) {
            blendMode = ImagesBlendMode.SCREEN;

        } else if (AppVaribles.getMessage("OverlayMode").equals(mode)) {
            blendMode = ImagesBlendMode.OVERLAY;

        } else if (AppVaribles.getMessage("HardLightMode").equals(mode)) {
            blendMode = ImagesBlendMode.HARD_LIGHT;

        } else if (AppVaribles.getMessage("SoftLightMode").equals(mode)) {
            blendMode = ImagesBlendMode.SOFT_LIGHT;

        } else if (AppVaribles.getMessage("ColorDodgeMode").equals(mode)) {
            blendMode = ImagesBlendMode.COLOR_DODGE;

        } else if (AppVaribles.getMessage("LinearDodgeMode").equals(mode)) {
            blendMode = ImagesBlendMode.LINEAR_DODGE;

        } else if (AppVaribles.getMessage("DivideMode").equals(mode)) {
            blendMode = ImagesBlendMode.DIVIDE;

        } else if (AppVaribles.getMessage("ColorBurnMode").equals(mode)) {
            blendMode = ImagesBlendMode.COLOR_BURN;

        } else if (AppVaribles.getMessage("LinearBurnMode").equals(mode)) {
            blendMode = ImagesBlendMode.LINEAR_BURN;

        } else if (AppVaribles.getMessage("VividLightMode").equals(mode)) {
            blendMode = ImagesBlendMode.VIVID_LIGHT;

        } else if (AppVaribles.getMessage("LinearLightMode").equals(mode)) {
            blendMode = ImagesBlendMode.LINEAR_LIGHT;

        } else if (AppVaribles.getMessage("SubtractMode").equals(mode)) {
            blendMode = ImagesBlendMode.SUBTRACT;

        } else if (AppVaribles.getMessage("DifferenceMode").equals(mode)) {
            blendMode = ImagesBlendMode.DIFFERENCE;

        } else if (AppVaribles.getMessage("ExclusionMode").equals(mode)) {
            blendMode = ImagesBlendMode.EXCLUSION;

        } else if (AppVaribles.getMessage("DarkenMode").equals(mode)) {
            blendMode = ImagesBlendMode.DARKEN;

        } else if (AppVaribles.getMessage("LightenMode").equals(mode)) {
            blendMode = ImagesBlendMode.LIGHTEN;

        } else if (AppVaribles.getMessage("HueMode").equals(mode)) {
            blendMode = ImagesBlendMode.HUE;

        } else if (AppVaribles.getMessage("SaturationMode").equals(mode)) {
            blendMode = ImagesBlendMode.SATURATION;

        } else if (AppVaribles.getMessage("ColorMode").equals(mode)) {
            blendMode = ImagesBlendMode.COLOR;

        } else if (AppVaribles.getMessage("LuminosityMode").equals(mode)) {
            blendMode = ImagesBlendMode.LUMINOSITY;

        }
        blendImages();
    }

    private void checkPoint() {
        try {
            isAreaValid = true;
            x = Integer.valueOf(pointX.getText());
            if (x >= 0 && x < backImage.getWidth()) {
                pointX.setStyle(null);
                isAreaValid = true;
            } else {
                pointX.setStyle(badStyle);
                x = 0;
                isAreaValid = false;
            }
        } catch (Exception e) {
            pointX.setStyle(badStyle);
            x = 0;
            isAreaValid = false;
        }
        try {
            y = Integer.valueOf(pointY.getText());
            if (y >= 0 && y < backImage.getHeight()) {
                pointY.setStyle(null);
            } else {
                pointY.setStyle(badStyle);
                y = 0;
                isAreaValid = false;
            }
        } catch (Exception e) {
            pointY.setStyle(badStyle);
            y = 0;
            isAreaValid = false;
        }

        blendImages();
    }

    private void initTargetSection() {
        try {
            toolBox.disableProperty().bind(
                    foreBox.disableProperty()
                            .or(backBox.disableProperty())
                            .or(blendModeBox.selectionModelProperty().getValue().selectedItemProperty().isNull())
                            .or(pointX.styleProperty().isEqualTo(badStyle))
                            .or(pointY.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void selectForegroundImage(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            foreFile = file;
            AppVaribles.setUserConfigValue(LastPathKey, foreFile.getParent());
            AppVaribles.setUserConfigValue(sourcePathKey, foreFile.getParent());

            final String fileName = file.getPath();
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        BufferedImage bufferImage = ImageIO.read(file);
                        if (task.isCancelled()) {
                            return null;
                        }
                        foreImage = SwingFXUtils.toFXImage(bufferImage, null);
                        foreInfo = ImageFileReaders.readImageFileMetaData(fileName).getImageInformation();
                        if (task.isCancelled()) {
                            return null;
                        }
                        foreInfo.setImage(foreImage);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                foreView.setPreserveRatio(true);
                                foreView.setImage(foreImage);
                                if (foreScroll.getHeight() < foreImage.getHeight()) {
                                    foreView.setFitWidth(foreScroll.getWidth() - 1);
                                    foreView.setFitHeight(foreScroll.getHeight() - 5);
                                } else {
                                    foreView.setFitWidth(foreView.getImage().getWidth());
                                    foreView.setFitHeight(foreView.getImage().getHeight());
                                }
                                foreTitle.setText(AppVaribles.getMessage("ForegroundImage") + " "
                                        + (int) foreImage.getWidth() + "*" + (int) foreImage.getHeight());
                                foreLabel.setText(fileName);
                                foreBox.setDisable(false);
                                afterImagesOpened();
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void openForegroundImage(ActionEvent event) {
        if (foreFile != null) {
            openImageViewer(foreFile.getAbsolutePath());
        }
    }

    @FXML
    private void setForegroundPaneSize(ActionEvent event) {
        foreView.setFitWidth(foreScroll.getWidth() - 1);
        foreView.setFitHeight(foreScroll.getHeight() - 5);
    }

    @FXML
    private void setForegroundImageSize(ActionEvent event) {
        foreView.setFitWidth(foreView.getImage().getWidth());
        foreView.setFitHeight(foreView.getImage().getHeight());
    }

    @FXML
    private void selectBackgroundImage(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            backFile = file;
            AppVaribles.setUserConfigValue(LastPathKey, backFile.getParent());
            AppVaribles.setUserConfigValue(sourcePathKey, backFile.getParent());

            final String fileName = file.getPath();
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        BufferedImage bufferImage = ImageIO.read(file);
                        if (task.isCancelled()) {
                            return null;
                        }
                        backImage = SwingFXUtils.toFXImage(bufferImage, null);
                        backInfo = ImageFileReaders.readImageFileMetaData(fileName).getImageInformation();
                        if (task.isCancelled()) {
                            return null;
                        }
                        backInfo.setImage(backImage);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                backView.setPreserveRatio(true);
                                backView.setImage(backImage);
                                if (backScroll.getHeight() < backImage.getHeight()) {
                                    backView.setFitWidth(backScroll.getWidth() - 1);
                                    backView.setFitHeight(backScroll.getHeight() - 5);
                                } else {
                                    backView.setFitWidth(backView.getImage().getWidth());
                                    backView.setFitHeight(backView.getImage().getHeight());
                                }
                                backTitle.setText(AppVaribles.getMessage("BackgroundImage") + " "
                                        + (int) backImage.getWidth() + "*" + (int) backImage.getHeight());
                                backLabel.setText(fileName);
                                backBox.setDisable(false);
                                afterImagesOpened();
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void afterImagesOpened() {
        if (foreImage != null && backImage != null) {
            targetBox.setDisable(false);
            blendImages();
        } else {
            targetBox.setDisable(true);
        }
    }

    @FXML
    private void openBackgroundImage(ActionEvent event) {
        if (backFile != null) {
            openImageViewer(backFile.getAbsolutePath());
        }
    }

    @FXML
    private void setBackgroundPaneSize(ActionEvent event) {
        backView.setFitWidth(backScroll.getWidth() - 1);
        backView.setFitHeight(backScroll.getHeight() - 5);
    }

    @FXML
    private void setBackgroundImageSize(ActionEvent event) {
        backView.setFitWidth(backView.getImage().getWidth());
        backView.setFitHeight(backView.getImage().getHeight());
    }

    @FXML
    @Override
    public void saveAction() {
        if (image == null) {
            return;
        }
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
            targetFile = file;

            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        String filename = targetFile.getAbsolutePath();
                        String format = FileTools.getFileSuffix(filename);
                        final BufferedImage bufferedImage = ImageTools.getBufferedImage(image);
                        if (task.isCancelled()) {
                            return null;
                        }
                        ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void newWindow(ActionEvent event) {
        openImageViewer(image);
    }

    @FXML
    private void openTargetPath(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(targetPath.toURI());
        } catch (Exception e) {

        }
    }

    @FXML
    private void foreClicked(MouseEvent event) {
        if (foreImage == null || location != ImagesRelativeLocation.Background_In_Foreground) {
            return;
        }
        foreView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * foreImage.getWidth() / foreView.getBoundsInLocal().getWidth());
        int yv = (int) Math.round(event.getY() * foreImage.getHeight() / foreView.getBoundsInLocal().getHeight());

        pointX.setText(xv + "");
        pointY.setText(yv + "");
    }

    @FXML
    private void backClicked(MouseEvent event) {
        if (backImage == null || location != ImagesRelativeLocation.Foreground_In_Background) {
            return;
        }
        backView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * backImage.getWidth() / backView.getBoundsInLocal().getWidth());
        int yv = (int) Math.round(event.getY() * backImage.getHeight() / backView.getBoundsInLocal().getHeight());

        pointX.setText(xv + "");
        pointY.setText(yv + "");
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (image == null) {
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * image.getWidth() / imageView.getBoundsInLocal().getWidth());
        int yv = (int) Math.round(event.getY() * image.getHeight() / imageView.getBoundsInLocal().getHeight());

        pointX.setText(xv + "");
        pointY.setText(yv + "");

    }

    private void blendImages() {
        if (!isAreaValid || foreImage == null || backImage == null
                || blendMode == null) {
            image = null;
            imageView.setImage(null);
            bottomLabel.setText("");
            return;
        }

        bottomLabel.setText(AppVaribles.getMessage("Loading..."));

        image = ImageTools.blendImages(foreImage, backImage,
                location, x, y, intersectOnlyCheck.isSelected(), blendMode, opacity);
        if (image == null) {
            bottomLabel.setText("");
            return;
        }
        imageView.setImage(image);
        fitSize();
        bottomLabel.setText(AppVaribles.getMessage("BlendedSize") + ": "
                + (int) image.getWidth() + "*" + (int) image.getHeight());
    }

}
