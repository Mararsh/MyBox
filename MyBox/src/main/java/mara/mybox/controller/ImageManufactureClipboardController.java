package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageManufacture.KeepRatioType;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.PixelBlend.ImagesBlendMode;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
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
    protected Image clipSource, currentClip, blendedImage;
    protected DoubleRectangle rectangle;
    protected Image lastSystemClip;
    protected int rotateAngle, keepRatioType;
    protected ObservableList<ImageClipboard> thumbnails;

    @FXML
    protected ListView<ImageClipboard> thumbnailsList;
    @FXML
    protected VBox clipboardBox;
    @FXML
    protected ComboBox<String> blendBox, maxBox, opacityBox, angleBox, ratioBox;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected CheckBox keepRatioCheck;
    @FXML
    protected FlowPane setBox;
    @FXML
    protected Label listLabel;
    @FXML
    protected Button editButton;

    public ImageManufactureClipboardController() {
        baseTitle = AppVariables.message("ImageManufactureClipboard");
        operation = ImageOperation.Clipboard;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = clipboardPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);

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
                    if (parent != null) {
                        displayClip(0);
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
                            if (parent != null) {
                                displayClip(0);
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
                    displayClip(newValue.intValue());
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
                    if (!parent.editable.get()) {
                        return;
                    }
                    if (event.getClickCount() > 1) {
                        pasteAction();
                    }
                }
            });
            loadClipboard();

            deleteButton.disableProperty().bind(thumbnailsList.getSelectionModel().selectedItemProperty().isNull());
            editButton.disableProperty().bind(thumbnailsList.getSelectionModel().selectedItemProperty().isNull());
            okButton.setDisable(true);
            cancelButton.disableProperty().bind(okButton.disableProperty());
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

            pasteButton.disableProperty().bind(parent.editable.not());
            setBox.disableProperty().bind(parent.editable.not());
            listLabel.disableProperty().bind(parent.editable.not());

        } catch (Exception e) {
            logger.error(e.toString());
        }
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
            logger.error(e.toString());
        }
    }

    @FXML
    public void loadSystemClipboard() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            final Image clip = SystemTools.fetchImageInClipboard(false);
            if (clip == null
                    || (lastSystemClip != null
                    && FxmlImageManufacture.sameImage(lastSystemClip, clip))) {
                parent.popInformation(message("NoImageInClipboard"));
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
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void loadClipboard() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            thumbnails.clear();
            clipboardBox.setDisable(true);
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
                }

                @Override
                protected void taskQuit() {
                    clipboardBox.setDisable(false);
                    task = null;
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
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
                if (task != null) {
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
                    }
                };
                parent.openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void rotateRight() {
        displayClip(rotateAngle);
    }

    @FXML
    public void rotateLeft() {
        displayClip(360 - rotateAngle);

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
    public void editAction() {
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
            logger.error(e.toString());
        }
    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null) {
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
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    @Override
    public void pasteAction() {
        if (!parent.editable.get() || thumbnails.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected Void call() {
                    try {
                        ImageClipboard clip = thumbnailsList.getSelectionModel().getSelectedItem();
                        if (clip == null) {
                            clip = thumbnails.get(0);
                        }
                        clip = ImageClipboard.image(clip.getImageFile().getAbsolutePath());
                        if (task == null || isCancelled()) {
                            return null;
                        }
                        clipSource = clip.getImage();
                        currentClip = clipSource;
                        ok = clipSource != null;
                    } catch (Exception e) {

                    }
                    return null;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.maskRectangleData = new DoubleRectangle(0, 0,
                            currentClip.getWidth() - 1, currentClip.getHeight() - 1);
                    imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
                    imageController.setMaskRectangleLineVisible(true);
                    imageController.drawMaskRectangleLineAsData();
                    imageController.imageLabel.setText(message("PasteComments2"));

                    okButton.setDisable(false);
                    displayClip(0);
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void displayClip(int angle) {
        try {
            if (!parent.editable.get() || clipSource == null) {
                return;
            }
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private Image clip, blended;

                    @Override
                    protected boolean handle() {
                        try {
                            if (angle != 0) {
                                currentClip = FxmlImageManufacture.rotateImage(clipSource, angle);
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }
                            clip = FxmlImageManufacture.scaleImage(currentClip,
                                    (int) imageController.scope.getRectangle().getWidth(),
                                    (int) imageController.scope.getRectangle().getHeight(),
                                    keepRatioCheck.isSelected(), keepRatioType);
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            blended = FxmlImageManufacture.blendImages(
                                    clip, imageController.imageView.getImage(),
                                    (int) imageController.scope.getRectangle().getSmallX(),
                                    (int) imageController.scope.getRectangle().getSmallY(),
                                    blendMode, opacity);
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return blended != null;
                        } catch (Exception e) {
                            logger.error(e.toString());
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        imageController.maskRectangleData = new DoubleRectangle(
                                imageController.maskRectangleData.getSmallX(),
                                imageController.maskRectangleData.getSmallY(),
                                imageController.maskRectangleData.getSmallX() + clip.getWidth() - 1,
                                imageController.maskRectangleData.getSmallY() + clip.getHeight() - 1);
                        imageController.drawMaskRectangleLineAsData();
                        imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
                        imageController.maskView.setImage(blended);
                        imageController.maskView.setOpacity(1);
                        imageController.maskView.setLayoutX(imageController.imageView.getLayoutX());
                        imageController.maskView.setLayoutY(imageController.imageView.getLayoutY());
                        imageController.maskView.setFitWidth(imageController.imageView.getFitWidth());
                        imageController.maskView.setFitHeight(imageController.imageView.getFitHeight());
                    }

                };
                parent.openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void paneClicked(MouseEvent event) {
        if (imageController.scope == null || imageController.maskRectangleData == null) {
            return;
        }
        if (!imageController.scope.getRectangle().same(imageController.maskRectangleData)) {
            imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
            displayClip(0);
        }
    }

    @FXML
    @Override
    public void copyAction() {
        // This responses to shortcut. Must empty to avoid handle twice
    }

    @FXML
    public void copyAction(ActionEvent event) {
        parent.copy(false);  // This response to the button
    }

    @FXML
    @Override
    public void okAction() {
        if (!parent.editable.get()) {
            return;
        }
        parent.updateImage(ImageOperation.Paste, null, null,
                imageController.maskView.getImage(), -1);
        clipSource = null;
        currentClip = null;
        imageController.clearValues();
        okButton.setDisable(true);
    }

    @FXML
    @Override
    public void cancelAction() {
        if (!parent.editable.get()) {
            return;
        }
        clipSource = null;
        currentClip = null;
        imageController.clearValues();
        okButton.setDisable(true);
    }

}
