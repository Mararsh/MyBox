package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.fxml.image.FxmlCoverTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-11-05
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureCoverController extends ImageManufactureController {

    private String pix;
    private int leftX, leftY, rightX, rightY, radius, size;
    private OperationType opType;
    ObservableList<String> pixList = FXCollections.observableArrayList();
    private Image picture;
    private float alpha;
    private List<String> prePixList;
    private Map<String, ImageView> imageViewMap;
    private Map<String, Image> prePixMap;

    @FXML
    private ToggleGroup coverGroup;
    @FXML
    private Label leftLabel;
    @FXML
    private HBox radiusBox, pixHBox, rightBox, sizeHBox;
    @FXML
    private ComboBox<String> pixBox, sizeBox, alphaBox;
    @FXML
    private TextField radiusInput, leftXInput, leftYInput, rightXInput, rightYInput;
    @FXML
    private ImageView picView;
    @FXML
    private CheckBox ratioCheck;

    public ImageManufactureCoverController() {
    }

    public enum OperationType {
        MosaicCircle,
        MosaicRectangle,
        FrostedCircle,
        FrostedRectangle,
        Picture
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initCoverTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initCoverTab() {
        try {
            coverGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });
            checkOperationType();

            leftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShape();
                }
            });
            leftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShape();
                }
            });
            rightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShape();
                }
            });
            rightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShape();
                }
            });
            radiusInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShape();
                }
            });

            List<String> sizeList = Arrays.asList(
                    "30", "15", "8", "6", "3", "10", "20", "40");
            sizeBox.getItems().addAll(sizeList);
            sizeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSize();
                }
            });
            sizeBox.getSelectionModel().select(0);

            prePixList = Arrays.asList(
                    "img/bee1.png", "img/buttefly1.png", "img/flower1.png", "img/flower2.png", "img/flower3.png",
                    "img/insect1.png", "img/insect2.png", "img/p1.png", "img/p2.png", "img/p3.png",
                    "img/mybox.png", "img/About.png", "img/DesktopTools.png", "img/FileTools.png",
                    "img/ImageTools.png", "img/PdfTools.png", "img/language.png", "img/position.png"
            );
            pixBox.getItems().addAll(prePixList);
            pixBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

                @Override
                public ListCell<String> call(ListView<String> param) {
                    final ListCell<String> cell = new ListCell<String>() {

                        @Override
                        public void updateItem(String item,
                                boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                ImageView imageView = findImageView(item);
                                imageView.setPreserveRatio(true);
                                imageView.setFitHeight(50);
                                setGraphic(imageView);
                            }
                        }
                    };
                    return cell;
                }
            });
            picView.setPreserveRatio(true);
            picView.setFitHeight(50);
            pixBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    picture = findImage(newValue);
                    picView.setImage(picture);
                    checkShape();
                }
            });
            pixBox.getSelectionModel().select(0);

            alphaBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            alphaBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkAlpha();
                }
            });
            alphaBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private Image findImage(String name) {
        Image pic = null;
        if (prePixMap == null) {
            prePixMap = new HashMap<>();
        } else {
            pic = prePixMap.get(name);
        }
        if (pic == null) {
            pic = new Image(name);
            prePixMap.put(name, pic);
        }
        return pic;
    }

    private ImageView findImageView(String name) {
        ImageView view = null;
        if (imageViewMap == null) {
            imageViewMap = new HashMap<>();
        } else {
            view = imageViewMap.get(name);
        }
        if (view == null) {
            view = new ImageView(findImage(name));
            imageViewMap.put(name, view);
        }
        return view;
    }

    private void checkOperationType() {
        isSettingValues = true;
        RadioButton selected = (RadioButton) coverGroup.getSelectedToggle();
        if (getMessage("MosaicRectangle").equals(selected.getText())) {
            opType = OperationType.MosaicRectangle;
            rightBox.setDisable(false);
            pixHBox.setDisable(true);
            radiusBox.setDisable(true);
            sizeHBox.setDisable(false);
            leftLabel.setText(getMessage("LeftTop"));
            promptLabel.setText(getMessage("RectangleLabel"));
            radiusInput.setStyle(null);
            alphaBox.getEditor().setStyle(null);
            checkSize();

        } else if (getMessage("MosaicCircle").equals(selected.getText())) {
            opType = OperationType.MosaicCircle;
            rightBox.setDisable(true);
            pixHBox.setDisable(true);
            radiusBox.setDisable(false);
            sizeHBox.setDisable(false);
            leftLabel.setText(getMessage("CircleCenter"));
            promptLabel.setText(getMessage("CircleLabel"));
            rightXInput.setStyle(null);
            rightYInput.setStyle(null);
            alphaBox.getEditor().setStyle(null);
            checkSize();

        } else if (getMessage("FrostedRectangle").equals(selected.getText())) {
            opType = OperationType.FrostedRectangle;
            rightBox.setDisable(false);
            pixHBox.setDisable(true);
            radiusBox.setDisable(true);
            sizeHBox.setDisable(false);
            leftLabel.setText(getMessage("LeftTop"));
            promptLabel.setText(getMessage("RectangleLabel"));
            radiusInput.setStyle(null);
            alphaBox.getEditor().setStyle(null);
            checkSize();

        } else if (getMessage("FrostedCircle").equals(selected.getText())) {
            opType = OperationType.FrostedCircle;
            rightBox.setDisable(true);
            pixHBox.setDisable(true);
            radiusBox.setDisable(false);
            sizeHBox.setDisable(false);
            leftLabel.setText(getMessage("CircleCenter"));
            promptLabel.setText(getMessage("CircleLabel"));
            rightXInput.setStyle(null);
            rightYInput.setStyle(null);
            alphaBox.getEditor().setStyle(null);
            checkSize();

        } else if (getMessage("Picture").equals(selected.getText())) {
            opType = OperationType.Picture;
            rightBox.setDisable(false);
            pixHBox.setDisable(false);
            radiusBox.setDisable(true);
            sizeHBox.setDisable(true);
            leftLabel.setText(getMessage("LeftTop"));
            promptLabel.setText(getMessage("RectangleLabel"));
            radiusInput.setStyle(null);
            sizeBox.getEditor().setStyle(null);
            checkAlpha();

        }
        isSettingValues = false;
        checkShape();
    }

    private void checkShape() {
        if (isSettingValues) {
            return;
        }
        try {
            leftX = Integer.valueOf(leftXInput.getText());
            if (leftX >= 0 && leftX <= values.getCurrentImage().getWidth() - 1) {
                leftXInput.setStyle(null);
            } else {
                leftXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            leftXInput.setStyle(badStyle);
        }

        try {
            leftY = Integer.valueOf(leftYInput.getText());
            if (leftY >= 0 && leftY <= values.getCurrentImage().getHeight() - 1) {
                leftYInput.setStyle(null);
            } else {
                leftYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            leftYInput.setStyle(badStyle);
        }

        if (opType == OperationType.MosaicCircle || opType == OperationType.FrostedCircle) {
            rightXInput.setStyle(null);
            rightYInput.setStyle(null);
            try {
                radius = Integer.valueOf(radiusInput.getText());
                if (radius > 0) {
                    radiusInput.setStyle(null);
                } else {
                    radiusInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                radiusInput.setStyle(badStyle);
            }

        } else {

            try {
                rightX = Integer.valueOf(rightXInput.getText());
                if (rightX >= 0 && rightX <= values.getCurrentImage().getWidth() - 1) {
                    rightXInput.setStyle(null);
                } else {
                    rightXInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                rightXInput.setStyle(badStyle);
            }

            try {
                rightY = Integer.valueOf(rightYInput.getText());
                if (rightY >= 0 && rightY <= values.getCurrentImage().getHeight() - 1) {
                    rightYInput.setStyle(null);
                } else {
                    rightYInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                rightYInput.setStyle(badStyle);
            }

            if (leftX >= rightX) {
                rightXInput.setStyle(badStyle);
            }

            if (leftY >= rightY) {
                rightYInput.setStyle(badStyle);
            }
        }

        coverAction();

    }

    private void checkSize() {
        try {
            size = Integer.valueOf((String) sizeBox.getSelectionModel().getSelectedItem());
            if (size > 0) {
                sizeBox.getEditor().setStyle(null);
            } else {
                size = 0;
                sizeBox.getEditor().setStyle(badStyle);
            }

        } catch (Exception e) {
            size = 0;
            sizeBox.getEditor().setStyle(badStyle);
        }
    }

    private void checkAlpha() {
        try {
            alpha = Float.valueOf((String) alphaBox.getSelectionModel().getSelectedItem());
            if (alpha >= 0.0f && alpha <= 1.0f) {
                alphaBox.getEditor().setStyle(null);
            } else {
                alpha = 0.5f;
                alphaBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            alpha = 0.5f;
            alphaBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());

        if (event.getButton() == MouseButton.PRIMARY) {
            isSettingValues = true;
            leftXInput.setText(x + "");
            leftYInput.setText(y + "");
            isSettingValues = false;

        } else if (event.getButton() == MouseButton.SECONDARY) {

            if (opType == OperationType.MosaicCircle || opType == OperationType.FrostedCircle) {
                if (badStyle.equals(leftXInput.getStyle()) || badStyle.equals(leftYInput.getStyle())) {
                    return;
                }
                long r = Math.round(Math.sqrt((x - leftX) * (x - leftX) + (y - leftY) * (y - leftY)));
                isSettingValues = true;
                radiusInput.setText(r + "");
                isSettingValues = false;

            } else {

                isSettingValues = true;
                rightXInput.setText(x + "");
                rightYInput.setText(y + "");
                isSettingValues = false;

            }

        }

        checkShape();

    }

    @FXML
    private void selectPicture(ActionEvent event) {
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
            File picFile = file;
            AppVaribles.setUserConfigValue(LastPathKey, picFile.getParent());
            AppVaribles.setUserConfigValue(sourcePathKey, picFile.getParent());

            final String fileName = file.getPath();
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        BufferedImage bufferImage = ImageIO.read(file);
                        if (task.isCancelled()) {
                            return null;
                        }
                        picture = SwingFXUtils.toFXImage(bufferImage, null);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                picView.setImage(picture);
                                checkShape();
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

    public void clearAction() {
        isSettingValues = true;
        leftXInput.setText("");
        leftYInput.setText("");
        rightXInput.setText("");
        rightYInput.setText("");
        radiusInput.setText("");
        isSettingValues = false;
    }

    public void coverAction() {
        if (isSettingValues || badStyle.equals(radiusInput.getStyle())
                || badStyle.equals(sizeBox.getEditor().getStyle())
                || badStyle.equals(leftXInput.getStyle()) || badStyle.equals(leftYInput.getStyle())
                || badStyle.equals(rightXInput.getStyle()) || badStyle.equals(rightYInput.getStyle())
                || badStyle.equals(alphaBox.getEditor().getStyle())) {
            return;
        }
        if (opType == OperationType.Picture && picture == null) {
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage;
                    if (null == opType) {
                        return null;
                    } else {
                        switch (opType) {
                            case MosaicRectangle:
                                newImage = FxmlCoverTools.makeMosaic(values.getCurrentImage(),
                                        leftX, leftY, rightX, rightY, size);
                                break;
                            case MosaicCircle:
                                newImage = FxmlCoverTools.makeMosaic(values.getCurrentImage(),
                                        leftX, leftY, radius, size);
                                break;
                            case FrostedRectangle:
                                newImage = FxmlCoverTools.makeFrosted(values.getCurrentImage(),
                                        leftX, leftY, rightX, rightY, size);
                                break;
                            case FrostedCircle:
                                newImage = FxmlCoverTools.makeFrosted(values.getCurrentImage(),
                                        leftX, leftY, radius, size);
                                break;
                            case Picture:
                                newImage = FxmlCoverTools.addPicture(values.getCurrentImage(),
                                        picture, leftX, leftY, rightX - leftX + 1, rightY - leftY + 1,
                                        ratioCheck.isSelected(), alpha);
                                break;
                            default:
                                return null;
                        }
                    }
                    if (task.isCancelled()) {
                        return null;
                    }
                    clearAction();
                    recordImageHistory(ImageOperationType.Cover, newImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(values.getCurrentImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
