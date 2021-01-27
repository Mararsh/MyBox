package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageBlend;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageManufacture.KeepRatioType;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.PixelBlend.ImagesBlendMode;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureClipboardController extends ImageManufactureOperationController {

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected Image clipSource, currentClip, blendedImage, finalClip, bgImage;
    protected DoubleRectangle rectangle;
    protected Image lastSystemClip;
    protected int rotateAngle, keepRatioType;
    protected ObservableList<ImageClipboard> thumbnails;
    protected boolean loaded;

    @FXML
    protected ListView<ImageClipboard> thumbnailsList;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imagesPane, setPane;
    @FXML
    protected ComboBox<String> blendBox, maxBox, opacityBox, angleBox, ratioBox;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected CheckBox keepRatioCheck, enlargeCheck;
    @FXML
    protected Label listLabel;
    @FXML
    protected Button editButton, refreshButton, demoButton;

    @Override
    public void initPane() {
        try {
            rotateAngle = 0;

            thumbnails = FXCollections.observableArrayList();
            thumbnailsList.setItems(thumbnails);

            ratioBox.getItems().addAll(Arrays.asList(message("BaseOnWidth"), message("BaseOnHeight"),
                    message("BaseOnLarger"), message("BaseOnSmaller")));
            ratioBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkRatioAdjustion(newValue);
                    }
                }
            });
            ratioBox.getSelectionModel().select(0);

            blendBox.getItems().addAll(PixelBlend.allBlendModes());
            blendBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    String mode = blendBox.getSelectionModel().getSelectedItem();
                    blendMode = PixelBlend.getBlendModeByName(mode);
                    opacityBox.setDisable(blendMode != PixelBlend.ImagesBlendMode.NORMAL);
                    if (imageController != null) {
                        pasteClip(0);
                    }
                }
            });
            blendBox.getSelectionModel().selectFirst();

            opacityBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            AppVariables.setUserConfigInt("ImageClipOpacity", (int) (f * 100));
                            FxmlControl.setEditorNormal(opacityBox);
                            if (imageController != null) {
                                pasteClip(0);
                            }
                        } else {
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });
            opacityBox.getSelectionModel().select((AppVariables.getUserConfigInt("ImageClipOpacity", 100) / 100f) + "");

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    pasteClip(newValue.intValue());
                }
            });

            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        rotateLeftButton.setDisable(false);
                        rotateRightButton.setDisable(false);
                        FxmlControl.setEditorNormal(angleBox);
                    } catch (Exception e) {
                        rotateLeftButton.setDisable(true);
                        rotateRightButton.setDisable(true);
                        FxmlControl.setEditorBadStyle(angleBox);
                    }
                }
            });
            angleBox.getSelectionModel().select(0);

            thumbnailsList.setCellFactory(new Callback<ListView<ImageClipboard>, ListCell<ImageClipboard>>() {
                @Override
                public ListCell<ImageClipboard> call(ListView<ImageClipboard> param) {
                    ListCell<ImageClipboard> cell = new ListCell<ImageClipboard>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        protected void updateItem(ImageClipboard item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item.getThumbnail() == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            view.setFitWidth(AppVariables.getUserConfigInt("ThumbnailWidth", 100));
                            Image image = item.getThumbnail();
                            view.setImage(image);
                            setGraphic(view);
                            setText(item.size());
                        }
                    };
                    return cell;
                }
            });
            thumbnailsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        selectClip();
                    }
                }
            });

            deleteButton.disableProperty().bind(thumbnailsList.getSelectionModel().selectedItemProperty().isNull());
            editButton.disableProperty().bind(thumbnailsList.getSelectionModel().selectedItemProperty().isNull());
            okButton.setDisable(true);
            cancelButton.disableProperty().bind(okButton.disableProperty());
            demoButton.disableProperty().bind(okButton.disableProperty());
            angleSlider.disableProperty().bind(okButton.disableProperty());

            maxBox.getItems().addAll(
                    Arrays.asList("20", "50", "100", "10", "30", "5", "80")
            );
            maxBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        int v = Integer.valueOf(newVal);
                        if (v > 0) {
                            FxmlControl.setEditorNormal(maxBox);
                            AppVariables.setUserConfigInt("ImageClipboardMax", v);
                        } else {
                            FxmlControl.setEditorBadStyle(maxBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(maxBox);
                    }
                }
            });
            maxBox.getSelectionModel().select(ImageClipboard.max() + "");

            loadClipboard();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.hideScopePane();
        imageController.resetImagePane();
        imageController.showImagePane();
        refreshButton.requestFocus();
    }

    protected void checkRatioAdjustion(String s) {
        try {
            if (message("BaseOnWidth").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnWidth;
            } else if (message("BaseOnHeight").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnHeight;
            } else if (message("BaseOnLarger").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnLarger;
            } else if (message("BaseOnSmaller").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnSmaller;
            } else {
                keepRatioType = KeepRatioType.None;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void loadClipboard() {
        imageController.showRightPane();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            loaded = false;
            thumbnails.clear();
            tabPane.getSelectionModel().select(imagesPane);
            task = new SingletonTask<Void>() {

                private List<ImageClipboard> clipboards;

                @Override
                protected boolean handle() {
                    clipboards = ImageClipboard.thumbnails();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (clipboards != null) {
                        thumbnails.addAll(clipboards);
                    }
                    loaded = true;
                    refreshButton.requestFocus();
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void loadSystemClipboard() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            final Image clip = SystemTools.fetchImageInClipboard(false);
            if (clip == null
                    || (lastSystemClip != null
                    && FxmlImageManufacture.sameImage(lastSystemClip, clip))) {
                imageController.popInformation(message("NoImageInClipboard"));
                return;
            }
            lastSystemClip = clip;
            task = new SingletonTask<Void>() {

                private ImageClipboard clip;

                @Override
                protected boolean handle() {
                    String name = ImageClipboard.add(lastSystemClip, false);
                    clip = ImageClipboard.thumbnail(name);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    thumbnails.add(0, clip);
                    refreshButton.requestFocus();
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void selectSourceFileDo(File file) {
        recordFileOpened(file);
        selectSourceFile(file);
    }

    @Override
    public void selectSourceFile(final File file) {
        try {
            if (file == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private ImageClipboard clip;

                    @Override
                    protected boolean handle() {
                        String name = ImageClipboard.add(file);
                        clip = ImageClipboard.thumbnail(name);
                        return clip != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        thumbnails.add(0, clip);
                        refreshButton.requestFocus();
                    }
                };
                imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void rotateRight() {
        pasteClip(rotateAngle);
    }

    @FXML
    public void rotateLeft() {
        pasteClip(360 - rotateAngle);

    }

    @FXML
    @Override
    public void deleteAction() {
        ImageClipboard clip = thumbnailsList.getSelectionModel().getSelectedItem();
        if (clip == null) {
            return;
        }
        ImageClipboard.delete(clip.getImageFile().getAbsolutePath());
        thumbnails.remove(clip);
//        thumbnailsList.refresh();
    }

    @FXML
    @Override
    public void clearAction() {
        ImageClipboard.clear();
        thumbnails.clear();
//        thumbnailsList.refresh();
    }

    @FXML
    public void editAction(ActionEvent event) {
        try {
            ImageClipboard selected = thumbnailsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Image selectedImage = selected.image();
            if (selectedImage == null) {
                return;
            }
            final ImageManufactureController controller
                    = (ImageManufactureController) FxmlStage.openStage(CommonValues.ImageManufactureFxml);
            controller.loadImage(selectedImage);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                List<ImageClipboard> clips;

                @Override
                protected boolean handle() {
                    List<Image> examples = Arrays.asList(
                            new Image("img/ww1.png"), new Image("img/ww2.png"), new Image("img/ww5.png"),
                            new Image("img/ww3.png"), new Image("img/ww4.png"), new Image("img/ww6.png"),
                            new Image("img/ww7.png"), new Image("img/ww8.png"), new Image("img/ww9.png"),
                            new Image("img/About.png"), new Image("img/MyBox.png"), new Image("img/DataTools.png"),
                            new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                            new Image("img/PdfTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                            new Image("img/Settings.png"), new Image("img/zz1.png"), new Image("img/jade.png")
                    );
                    clips = new ArrayList<>();
                    for (int i = examples.size() - 1; i >= 0; --i) {
                        String name = ImageClipboard.add(examples.get(i), false);
                        ImageClipboard clip = ImageClipboard.clip(name, false, true);
                        if (clip != null) {
                            clips.add(0, clip);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    thumbnails.addAll(0, clips);
                    refreshButton.requestFocus();
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void selectClip() {
        initOperation();
        if (thumbnails.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        ImageClipboard clip = thumbnailsList.getSelectionModel().getSelectedItem();
                        if (clip == null) {
                            clip = thumbnails.get(0);
                        }
                        clip = ImageClipboard.image(clip.getImageFile().getAbsolutePath());
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        clipSource = clip.getImage();
                        currentClip = clipSource;
                        return clipSource != null;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    imageController.scope = new ImageScope();
                    imageController.maskRectangleData = new DoubleRectangle(0, 0,
                            currentClip.getWidth() - 1, currentClip.getHeight() - 1);
                    imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
                    pasteClip(0);
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void pasteClip(int angle) {
        try {
            if (clipSource == null) {
                return;
            }
            imageController.showRightPane();
            imageController.showImagePane();
            imageController.hideScopePane();
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private boolean enlarged;

                    @Override
                    protected boolean handle() {
                        try {
                            if (angle != 0) {
                                currentClip = FxmlImageManufacture.rotateImage(clipSource, angle);
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }
                            finalClip = FxmlImageManufacture.scaleImage(currentClip,
                                    (int) imageController.scope.getRectangle().getWidth(),
                                    (int) imageController.scope.getRectangle().getHeight(),
                                    keepRatioCheck.isSelected(), keepRatioType);
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            bgImage = imageView.getImage();
                            enlarged = false;
                            if (enlargeCheck.isSelected()) {
                                if (finalClip.getWidth() > bgImage.getWidth()) {
                                    bgImage = FxmlImageManufacture.addMarginsFx(bgImage,
                                            Color.TRANSPARENT, (int) (finalClip.getWidth() - bgImage.getWidth()) + 1,
                                            false, false, false, true);
                                    enlarged = true;
                                }
                                if (finalClip.getHeight() > bgImage.getHeight()) {
                                    bgImage = FxmlImageManufacture.addMarginsFx(bgImage,
                                            Color.TRANSPARENT, (int) (finalClip.getHeight() - bgImage.getHeight()) + 1,
                                            false, true, false, false);
                                    enlarged = true;
                                }
                            }
                            blendedImage = FxmlImageManufacture.blendImages(
                                    finalClip, bgImage,
                                    (int) imageController.scope.getRectangle().getSmallX(),
                                    (int) imageController.scope.getRectangle().getSmallY(),
                                    blendMode, opacity);
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return blendedImage != null;
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (enlarged) {
                            imageController.setImage(ImageOperation.Margins, bgImage);
                        }
                        imageController.setMaskRectangleLineVisible(true);
                        imageController.maskRectangleData = new DoubleRectangle(
                                imageController.maskRectangleData.getSmallX(),
                                imageController.maskRectangleData.getSmallY(),
                                imageController.maskRectangleData.getSmallX() + finalClip.getWidth() - 1,
                                imageController.maskRectangleData.getSmallY() + finalClip.getHeight() - 1);
                        imageController.drawMaskRectangleLineAsData();
                        imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
                        maskView.setImage(blendedImage);
                        maskView.setOpacity(1.0);
                        maskView.setVisible(true);
                        imageView.setVisible(false);
                        imageView.toBack();
                        tabPane.getSelectionModel().select(setPane);
                        okButton.setDisable(false);
                        okButton.requestFocus();
                        imageController.operation = ImageOperation.Paste;
                    }
                };
                imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (imageController.scope == null || imageController.maskRectangleData == null) {
            return;
        }
        if (!imageController.scope.getRectangle().same(imageController.maskRectangleData)) {
            imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
            pasteClip(0);
        }
    }

    @FXML
    @Override
    public void okAction() {
        imageController.popSuccessful();
        imageController.updateImage(ImageOperation.Paste, null, null,
                maskView.getImage(), -1);
        initOperation();
    }

    @FXML
    @Override
    public void cancelAction() {
        initOperation();
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        imageController.popInformation(message("WaitAndHandling"));
        demoButton.setVisible(false);
        Task demoTask = new Task<Void>() {
            private List<File> files;

            @Override
            protected Void call() {
                try {
                    files = new ArrayList<>();
                    BufferedImage foreImage = SwingFXUtils.fromFXImage(finalClip, null);
                    BufferedImage backImage = SwingFXUtils.fromFXImage(bgImage, null);
                    int x = (int) imageController.scope.getRectangle().getSmallX();
                    int y = (int) imageController.scope.getRectangle().getSmallY();
                    for (String name : blendBox.getItems()) {
                        ImagesBlendMode mode = PixelBlend.getBlendModeByName(name);
                        if (mode == ImagesBlendMode.NORMAL) {
                            BufferedImage blended = ImageBlend.blendImages(foreImage, backImage, x, y, mode, 1f);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + message("Opacity") + "-1.0f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                            blended = ImageBlend.blendImages(foreImage, backImage, x, y, mode, 0.5f);
                            tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + message("Opacity") + "-0.5f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                        } else {
                            BufferedImage blended = ImageBlend.blendImages(foreImage, backImage, x, y, mode, 0.5f);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + ".png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                demoButton.setVisible(true);
                refreshButton.requestFocus();
                if (files.isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImagesBrowserController controller
                                    = (ImagesBrowserController) FxmlStage.openStage(CommonValues.ImagesBrowserFxml);
                            controller.loadImages(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });
            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void resetOperationPane() {
        clipSource = null;
        currentClip = null;
        okButton.setDisable(true);
        tabPane.getSelectionModel().select(imagesPane);
        refreshButton.requestFocus();
    }

}
