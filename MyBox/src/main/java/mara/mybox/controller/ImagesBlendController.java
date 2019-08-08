package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
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
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageBlend.ImagesRelativeLocation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.PixelBlend.ImagesBlendMode;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;

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

    private File foreFile, backFile;
    private Image foreImage, backImage;
    private ImageInformation foreInfo, backInfo;

    @FXML
    private VBox targetBox;
    @FXML
    private ScrollPane foreScroll, backScroll;
    @FXML
    private ImageView foreView, backView;
    @FXML
    private HBox foreBox, backBox, opacityHBox, toolBox;
    @FXML
    private ToggleGroup locationGroup;
    @FXML
    private ComboBox<String> blendModeBox, opacityBox;
    @FXML
    private Label foreTitle, foreLabel, backTitle, backLabel, pointLabel;
    @FXML
    private TextField pointX, pointY;
    @FXML
    private CheckBox intersectOnlyCheck;

    public ImagesBlendController() {
        baseTitle = AppVaribles.message("ImagesBlend");

        ImageBlendFileTypeKey = "ImageBlendFileType";
        needNotRulers = true;
        needNotCoordinates = true;
    }

    @Override
    public void initializeNext2() {
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
            pointLabel.setText(AppVaribles.message("ClickOnBackgournd"));

            blendModeBox.getItems().addAll(PixelBlend.allBlendModes());
            blendModeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    checkBlendMode();
                }
            });

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
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        opacity = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            FxmlControl.setEditorNormal(opacityBox);
                            blendImages();
                        } else {
                            opacity = 0.5f;
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        opacity = 0.5f;
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });

            intersectOnlyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    blendImages();
                }
            });

            isSettingValues = true;
            blendModeBox.getSelectionModel().select(0);
            opacityBox.getSelectionModel().select(0);
            isSettingValues = false;
            checkBlendMode();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkLocation() {
        if (foreImage == null || backImage == null) {
            return;
        }
        isSettingValues = true;
        RadioButton selected = (RadioButton) locationGroup.getSelectedToggle();
        if (AppVaribles.message("FinB").equals(selected.getText())) {
            location = ImagesRelativeLocation.Foreground_In_Background;
            pointLabel.setText(AppVaribles.message("ClickOnBackgournd"));
            bottomLabel.setText(AppVaribles.message("BlendedSize") + ": "
                    + (int) backImage.getWidth() + "*" + (int) backImage.getHeight());

        } else if (AppVaribles.message("BinF").equals(selected.getText())) {
            location = ImagesRelativeLocation.Background_In_Foreground;
            pointLabel.setText(AppVaribles.message("ClickOnForegournd"));
            bottomLabel.setText(AppVaribles.message("BlendedSize") + ": "
                    + (int) foreImage.getWidth() + "*" + (int) foreImage.getHeight());

        } else {
            return;
        }
        pointX.setText("0");
        pointY.setText("0");
        isSettingValues = false;
        checkPoint();
    }

    private void checkBlendMode() {
        String mode = blendModeBox.getSelectionModel().getSelectedItem();
        blendMode = PixelBlend.getBlendModeByName(mode);
        opacityHBox.setDisable(blendMode != ImagesBlendMode.NORMAL);

        if (!isSettingValues) {
            blendImages();
        }

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

        if (!isSettingValues) {
            blendImages();
        }
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
    public void selectForegroundImage() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            selectForegroundImage(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectForegroundImage(final File file) {
        try {
            if (file == null) {
                return;
            }
            foreFile = file;
            recordFileOpened(file);

            final String fileName = file.getPath();
            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {

                    BufferedImage bufferImage = ImageFileReaders.readImage(file);
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    foreImage = SwingFXUtils.toFXImage(bufferImage, null);
                    foreInfo = ImageFileReaders.readImageFileMetaData(fileName).getImageInformation();
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    foreInfo.setImage(foreImage);

                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
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
                                foreTitle.setText(AppVaribles.message("ForegroundImage") + " "
                                        + (int) foreImage.getWidth() + "*" + (int) foreImage.getHeight());
                                foreLabel.setText(fileName);
                                foreBox.setDisable(false);
                                afterImagesOpened();
                            }
                        });
                    }
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
    protected void popForeground(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectForegroundImage();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectForegroundImage(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    private void openForegroundImage(ActionEvent event) {
        if (foreFile != null) {
            openImageViewer(foreFile);
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
    public void selectBackgroundImage() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            selectBackgroundImage(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectBackgroundImage(final File file) {
        try {
            if (file == null) {
                return;
            }
            backFile = file;
            recordFileOpened(backFile);

            final String fileName = file.getPath();
            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {

                    BufferedImage bufferImage = ImageFileReaders.readImage(file);
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    backImage = SwingFXUtils.toFXImage(bufferImage, null);
                    backInfo = ImageFileReaders.readImageFileMetaData(fileName).getImageInformation();
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    backInfo.setImage(backImage);

                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
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
                                backTitle.setText(AppVaribles.message("BackgroundImage") + " "
                                        + (int) backImage.getWidth() + "*" + (int) backImage.getHeight());
                                backLabel.setText(fileName);
                                backBox.setDisable(false);
                                afterImagesOpened();
                            }
                        });
                    }
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
    protected void popBackground(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectBackgroundImage();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectBackgroundImage(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
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
            openImageViewer(backFile);
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
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (image == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVaribles.getUserConfigPath(targetPathKey),
                    null, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
            targetFile = file;

            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    try {
                        String filename = targetFile.getAbsolutePath();
                        String format = FileTools.getFileSuffix(filename);
                        final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(image);
                        if (task == null || task.isCancelled()) {
                            return null;
                        }
                        ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                popInformation(AppVaribles.message("Saved"));
                                openImageViewer(file);

                            } else {
                                popInformation(AppVaribles.message("Failed"));
                            }
                        }
                    });
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
            browseURI(targetPath.toURI());
            recordFileOpened(targetPath);
        } catch (Exception e) {

        }
    }

    @FXML
    private void foreClicked(MouseEvent event) {
        if (foreImage == null || location != ImagesRelativeLocation.Background_In_Foreground) {
            return;
        }
        foreView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * foreImage.getWidth() / foreView.getBoundsInParent().getWidth());
        int yv = (int) Math.round(event.getY() * foreImage.getHeight() / foreView.getBoundsInParent().getHeight());

        pointX.setText(xv + "");
        pointY.setText(yv + "");
    }

    @FXML
    private void backClicked(MouseEvent event) {
        if (backImage == null || location != ImagesRelativeLocation.Foreground_In_Background) {
            return;
        }
        backView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * backImage.getWidth() / backView.getBoundsInParent().getWidth());
        int yv = (int) Math.round(event.getY() * backImage.getHeight() / backView.getBoundsInParent().getHeight());

        pointX.setText(xv + "");
        pointY.setText(yv + "");
    }

    @FXML
    @Override
    public void imageClicked(MouseEvent event) {
        if (image == null) {
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int xv = (int) Math.round(event.getX() * image.getWidth() / imageView.getBoundsInParent().getWidth());
        int yv = (int) Math.round(event.getY() * image.getHeight() / imageView.getBoundsInParent().getHeight());

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

        bottomLabel.setText(AppVaribles.message("Loading..."));

        image = FxmlImageManufacture.blendImages(foreImage, backImage,
                location, x, y, intersectOnlyCheck.isSelected(), blendMode, opacity);
        if (image == null) {
            bottomLabel.setText("");
            return;
        }
        imageView.setImage(image);
        fitSize();
        bottomLabel.setText(AppVaribles.message("BlendedSize") + ": "
                + (int) image.getWidth() + "*" + (int) image.getHeight());
    }

}
