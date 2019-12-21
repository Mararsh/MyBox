package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifViewerController extends ImageViewerController {

    protected Image[] images;
    protected int currentIndex, interval, fromIndex, toIndex, totalNumber;

    @FXML
    protected ComboBox<String> intervalCBox, frameBox;
    @FXML
    protected Button pauseButton, extractButton;
    @FXML
    protected HBox operation3Box;
    @FXML
    protected Label promptLabel, commentsLabel;
    @FXML
    protected TextField fromInput, toInput;

    public ImageGifViewerController() {
        baseTitle = AppVariables.message("ImageGifViewer");

        SourceFileType = VisitHistory.FileType.Gif;
        SourcePathType = VisitHistory.FileType.Gif;
        TargetFileType = VisitHistory.FileType.Gif;
        TargetPathType = VisitHistory.FileType.Gif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        TipsLabelKey = "GifViewTips";
        needNotRulers = true;
        needNotCoordinates = true;

        sourceExtensionFilter = CommonFxValues.GifExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext2() {
        try {
            operation3Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(fromInput.getText());
                        if (v >= 0 && v <= totalNumber - 1 && v <= toIndex) {
                            fromIndex = v;
                            fromInput.setStyle(null);
                        } else {
                            fromInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        fromInput.setStyle(badStyle);
                    }
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(toInput.getText());
                        if (v >= 0 && v <= totalNumber - 1 && fromIndex <= v) {
                            toIndex = v;
                            toInput.setStyle(null);
                        } else {
                            toInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        toInput.setStyle(badStyle);
                    }
                }
            });

            extractButton.disableProperty().bind(
                    Bindings.isEmpty(fromInput.textProperty())
                            .or(fromInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(toInput.textProperty()))
                            .or(toInput.styleProperty().isEqualTo(badStyle))
            );

            interval = 500;
            List<String> values = Arrays.asList("500", "300", "1000", "2000", "3000", "5000", "10000");
            intervalCBox.getItems().addAll(values);
            intervalCBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            interval = v;
                            FxmlControl.setEditorNormal(intervalCBox);
                            showGifImage(currentIndex);
                        } else {
                            FxmlControl.setEditorBadStyle(intervalCBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intervalCBox);
                    }
                }
            });
            intervalCBox.getSelectionModel().select(0);

            frameBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            showGifFrame(v);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            setPauseButton(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void loadImage(final File file, final boolean onlyInformation,
            final int inLoadWidth, final int inFrameIndex, boolean inCareFrames) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            sourceFile = file;
            final String fileName = file.getPath();
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(fileName);
                        if (imageFileInformation == null || imageFileInformation.getImageInformation() == null) {
                            return true;
                        }
                        imageInformation = imageFileInformation.getImageInformation();
                        if (onlyInformation) {
                            return true;
                        }
                        List<BufferedImage> bimages = ImageFileReaders.readFrames("gif", fileName);
                        if (bimages == null) {
                            return false;
                        }
                        totalNumber = bimages.size();
                        images = new Image[totalNumber];
                        for (int i = 0; i < totalNumber; i++) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            Image m = SwingFXUtils.toFXImage(bimages.get(i), null);
                            images[i] = m;
                        }
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        if (totalNumber > 0) {
                            image = images[0];
                        }
                        return image != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        afterImageLoaded();
                        isSettingValues = true;
                        fromInput.setText("0");
                        toInput.setText((totalNumber - 1) + "");
                        isSettingValues = false;
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (images == null || images.length == 0) {
                return;
            }
            showGifImage(0);
            List<String> frames = new ArrayList<>();
            for (int i = 0; i < images.length; i++) {
                frames.add(i + "");
            }
            frameBox.getItems().clear();
            frameBox.getItems().addAll(frames);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setPauseButton(boolean setAsPaused) {
        if (setAsPaused) {
            ControlStyle.setIcon(pauseButton, ControlStyle.getIcon("iconPlay.png"));
            FxmlControl.setTooltip(pauseButton, new Tooltip(message("Continue")));
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("Paused");
        } else {
            ControlStyle.setIcon(pauseButton, ControlStyle.getIcon("iconPause.png"));
            FxmlControl.setTooltip(pauseButton, new Tooltip(message("Pause")));
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pauseButton.setUserData("Playing");
        }
        pauseButton.applyCss();
    }

    @FXML
    public void pauseAction() {
        try {

            if (pauseButton.getUserData().equals("Playing")) {
                showGifFrame(currentIndex);

            } else if (pauseButton.getUserData().equals("Paused")) {
                showGifImage(currentIndex);

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        try {
            showGifFrame(currentIndex - 2);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        try {
            showGifFrame(currentIndex);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        extractAction();
    }

    @FXML
    public void extractAction() {
        try {
            if (sourceFile == null || images.length == 0
                    || totalNumber <= 0 || fromIndex > toIndex) {
                return;
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    FileTools.getFilePrefix(sourceFile.getName()),
                    CommonFxValues.ImageExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private List<String> filenames;

                    @Override
                    protected boolean handle() {
                        filenames = ImageGifFile.extractGifImages(sourceFile, file, fromIndex, toIndex);
                        return filenames != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        multipleFilesGenerated(filenames);
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void editAction() {
        try {
            final ImageGifEditerController controller
                    = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml);
            controller.selectSourceFile(sourceFile);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showGifImage(int start) {
        try {
            if (images == null || images.length == 0) {
                return;
            }
            setPauseButton(false);
            currentIndex = start;
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentFrame();
                        }
                    });
                }
            }, 0, interval);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showGifFrame(int frame) {
        try {
            if (images == null || images.length == 0) {
                return;
            }
            if (timer != null) {
                timer.cancel();
            }
            currentIndex = frame;
            setCurrentFrame();
            setPauseButton(true);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setCurrentFrame() {
        if (currentIndex > images.length - 1) {
            currentIndex -= images.length;
        } else if (currentIndex < 0) {
            currentIndex += images.length;
        }
        imageView.setImage(images[currentIndex]);
        refinePane();
        promptLabel.setText(AppVariables.message("TotalFrames") + ": " + images.length + "  "
                + AppVariables.message("CurrentFrame") + ": " + currentIndex + "  "
                + AppVariables.message("Size") + ": " + (int) images[currentIndex].getWidth()
                + "*" + (int) images[currentIndex].getHeight());
        isSettingValues = true;
        frameBox.getSelectionModel().select(currentIndex + "");
        isSettingValues = false;

        currentIndex++;
    }

    @Override
    public ImageGifViewerController refresh() {
        File oldfile = sourceFile;

        ImageGifViewerController c = (ImageGifViewerController) refreshBase();
        if (c == null) {
            return null;
        }
        c.loadImage(sourceFile);
        return c;
    }

}
