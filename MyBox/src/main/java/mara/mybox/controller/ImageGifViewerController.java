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
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageGifFile;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifViewerController extends ImageViewerController {

    protected Image[] images;
    protected int currentIndex, interval, fromIndex, toIndex, totalNumber;
    protected boolean isSettingValues;

    @FXML
    protected ComboBox<String> intervalCBox, frameBox;
    @FXML
    protected Button pauseButton, extractButton;
    @FXML
    protected HBox opBox, extractBox;
    @FXML
    protected Label promptLabel, commentsLabel;
    @FXML
    protected ComboBox<String> targetTypeBox;
    @FXML
    protected TextField fromInput, toInput;

    public ImageGifViewerController() {
        fileExtensionFilter = CommonValues.GifExtensionFilter;
    }

    @Override
    protected void initializeNext2() {
        try {
            infoBar.setDisable(true);
            opBox.setDisable(true);
            extractBox.setDisable(true);

            targetTypeBox.getItems().addAll(CommonValues.SupportedImages);
            targetTypeBox.getSelectionModel().select(0);

            targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        final File file = new File(newValue);
                        if (!file.exists() || !file.isDirectory()) {
                            targetPathInput.setStyle(badStyle);
                            targetPath = null;
                            return;
                        }
                        targetPathInput.setStyle(null);
                        if (!isSettingValues) {
                            AppVaribles.setUserConfigValue(targetPathKey, file.getPath());
                        }
                        targetPath = file;
                    } catch (Exception e) {
                        targetPathInput.setStyle(badStyle);
                        targetPath = null;
                    }
                }
            });

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
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(fromInput.textProperty()))
                            .or(fromInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(toInput.textProperty()))
                            .or(toInput.styleProperty().isEqualTo(badStyle))
            );

            interval = 500;
            List<String> values = Arrays.asList("500", "300", "1000", "2000", "3000", "5000", "10000");
            intervalCBox.getItems().addAll(values);
            intervalCBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            interval = v;
                            intervalCBox.getEditor().setStyle(null);
                            showGifImage(currentIndex);
                        } else {
                            intervalCBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intervalCBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intervalCBox.getSelectionModel().select(0);

            frameBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            showGifFrame(v);
                        }
                    } catch (Exception e) {
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void loadImage(final File file, final boolean onlyInformation) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            sourceFile = file;
            final String fileName = file.getPath();
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(fileName);
                    imageInformation = imageFileInformation.getImageInformation();
                    List<BufferedImage> bimages = ImageFileReaders.readFrames("gif", fileName);
                    if (bimages == null) {
                        return null;
                    }
                    totalNumber = bimages.size();
                    images = new Image[totalNumber];
                    for (int i = 0; i < totalNumber; i++) {
                        if (task.isCancelled()) {
                            return null;
                        }
                        Image m = SwingFXUtils.toFXImage(bimages.get(i), null);
                        images[i] = m;
                    }
                    if (task.isCancelled()) {
                        return null;
                    }
                    if (totalNumber > 0) {
                        image = images[0];
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            afterImageLoaded();
                            isSettingValues = true;
                            targetPathInput.setText(sourceFile.getParent());
                            targetPrefixInput.setText(FileTools.getFilePrefix(sourceFile.getName()));
                            fromInput.setText("0");
                            toInput.setText((totalNumber - 1) + "");
                            isSettingValues = false;
                        }
                    });
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

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (images == null || images.length == 0) {
                infoBar.setDisable(true);
                opBox.setDisable(true);
                extractBox.setDisable(true);
                return;
            }
            infoBar.setDisable(false);
            opBox.setDisable(false);
            extractBox.setDisable(false);
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

    @FXML
    public void pauseAction() {
        try {
            if (pauseButton.getText().equals(AppVaribles.getMessage("Pause"))) {
                if (timer != null) {
                    timer.cancel();
                }
                previousButton.setDisable(false);
                nextButton.setDisable(false);
                pauseButton.setText(AppVaribles.getMessage("Continue"));
            } else if (pauseButton.getText().equals(AppVaribles.getMessage("Continue"))) {
                showGifImage(currentIndex);
                previousButton.setDisable(true);
                nextButton.setDisable(true);
                pauseButton.setText(AppVaribles.getMessage("Pause"));
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
    public void extractAction() {
        try {
            if (sourceFile == null || images.length == 0
                    || totalNumber <= 0 || fromIndex > toIndex) {
                return;
            }
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String fileName = targetPath.getAbsolutePath() + "/"
                            + targetPrefixInput.getText()
                            + "." + targetTypeBox.getSelectionModel().getSelectedItem();
                    final List<String> filenames
                            = ImageGifFile.extractGifImages(sourceFile, new File(fileName), fromIndex, toIndex);
                    if (task.isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            multipleFilesGenerated(filenames);
                        }
                    });
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
    public void editAction() {
        try {
            final ImageGifEditerController controller
                    = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml, false, true);
            controller.openFile(sourceFile);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showGifImage(int start) {
        try {
            if (images == null || images.length == 0) {
                return;
            }
            previousButton.setDisable(true);
            nextButton.setDisable(true);
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
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setText(AppVaribles.getMessage("Continue"));
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
        promptLabel.setText(AppVaribles.getMessage("TotalFrames") + ": " + images.length + "  "
                + AppVaribles.getMessage("CurrentFrame") + ": " + currentIndex + "  "
                + AppVaribles.getMessage("Size") + ": " + (int) images[currentIndex].getWidth()
                + "*" + (int) images[currentIndex].getHeight());
        currentIndex++;
    }

}
