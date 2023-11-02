package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageMarginsController extends BaseShapeController {

    protected ImageEditorController editor;
    protected int addedWidth, distance;
    private OperationType opType;

    public enum OperationType {
        SetMarginsByDragging,
        CutMarginsByColor,
        CutMarginsByWidth,
        AddMargins,
        BlurMargins
    }

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck,
            closeAfterCheck;
    @FXML
    protected FlowPane colorPane, distancePane, marginsPane, widthPane;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected Label commentsLabel;

    public ImageMarginsController() {
        baseTitle = message("Margins");
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    protected void setParameters(ImageEditorController parent) {
        try {
            if (parent == null) {
                close();
            }
            editor = parent;
            loadImage();

            editor.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    loadImage();
                }
            });

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            colorSetController.init(this, baseName + "Color");

            widthSelector.getItems().clear();
            int width = (int) imageView.getImage().getWidth();
            widthSelector.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "20", "10", "5", "100", "200", "300", "50", "150", "500"));
            widthSelector.getSelectionModel().select(UserConfig.getInt("ImageMarginsWidth", 20) + "");
            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkMarginWidth();
                }
            });

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            distanceInput.setText("20");
            distanceInput.setText(UserConfig.getInt("ImageMarginsColorDistance", 20) + "");

            closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                }
            });
            closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));

            okButton.disableProperty().bind(widthSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(distanceInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

            checkOperationType();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void checkOperationType() {
        try {
            clearMask();
            setBox.getChildren().clear();
            ValidationTools.setEditorNormal(widthSelector);
            distanceInput.setStyle(null);
            commentsLabel.setText("");

            if (opGroup.getSelectedToggle() == null) {
                return;
            }

            if (dragRadio.isSelected()) {
                opType = OperationType.SetMarginsByDragging;
                setBox.getChildren().addAll(colorPane);
                initDragging();

            } else if (addRadio.isSelected()) {
                opType = OperationType.AddMargins;
                setBox.getChildren().addAll(colorPane, widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutWidthRadio.isSelected()) {
                opType = OperationType.CutMarginsByWidth;
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutColorRadio.isSelected()) {
                opType = OperationType.CutMarginsByColor;
                setBox.getChildren().addAll(colorPane, distancePane, marginsPane);
                widthSelector.getEditor().setStyle(null);
                checkColorDistance();

            } else if (blurRadio.isSelected()) {
                opType = OperationType.BlurMargins;
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            }

            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initDragging() {
        try {
            commentsLabel.setText(message("ImageDragMarginsComments"));
            maskRectangleData = DoubleRectangle.image(imageView.getImage());
            showMaskRectangle();
            popAnchorMenu = false;
            showAnchors = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void checkMarginWidth() {
        try {
            int v = Integer.parseInt(widthSelector.getValue());
            if (v > 0) {
                addedWidth = v;
                UserConfig.setInt("ImageMarginsWidth", v);
                ValidationTools.setEditorNormal(widthSelector);
            } else {
                ValidationTools.setEditorBadStyle(widthSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(widthSelector);
        }
    }

    protected void checkColorDistance() {
        try {
            int v = Integer.parseInt(distanceInput.getText());
            if (distance >= 0 && distance <= 255) {
                distance = v;
                distanceInput.setStyle(null);
                UserConfig.setInt("ImageMarginsColorDistance", v);
            } else {
                distanceInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            distanceInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (opType != OperationType.SetMarginsByDragging
                || maskRectangleData == null || image == null) {
            return;
        }
        String info = message("CurrentSize") + ": " + (int) Math.round(image.getWidth())
                + "x" + (int) Math.round(image.getHeight()) + "  "
                + message("AfterChange") + ": " + (int) Math.round(maskRectangleData.getWidth())
                + "x" + (int) Math.round(maskRectangleData.getHeight());
        commentsLabel.setText(info);
    }

    protected void loadImage() {
        if (editor == null || !editor.isShowing()) {
            close();
            return;
        }
        loadImage(editor.imageView.getImage());
        if (opType == OperationType.SetMarginsByDragging) {
            initDragging();
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            String info = message("CurrentSize") + ": " + Math.round(image.getWidth())
                    + "x" + Math.round(image.getHeight());
            commentsLabel.setText(info);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (opType != OperationType.SetMarginsByDragging) {
            if (!marginsTopCheck.isSelected()
                    && !marginsBottomCheck.isSelected()
                    && !marginsLeftCheck.isSelected()
                    && !marginsRightCheck.isSelected()) {
                popError(message("NothingHandled"));
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;
            private String value = null;

            @Override
            protected boolean handle() {
                switch (opType) {
                    case SetMarginsByDragging:
                        newImage = MarginTools.dragMarginsFx(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), maskRectangleData);
                        break;
                    case CutMarginsByWidth:
                        newImage = MarginTools.cutMarginsByWidth(imageView.getImage(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = addedWidth + "";
                        break;
                    case CutMarginsByColor:
                        newImage = MarginTools.cutMarginsByColor(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), distance,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = distance + "";
                        break;
                    case AddMargins:
                        newImage = MarginTools.addMarginsFx(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = addedWidth + "";
                        break;
                    case BlurMargins:
                        newImage = MarginTools.blurMarginsAlpha(imageView.getImage(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = addedWidth + "";
                        break;
                    default:
                        return false;
                }
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage(newImage,
                        opType.name() + " " + value + "  " + message("Cost") + ": "
                        + DateTools.datetimeMsDuration(cost));
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    /*
        static methods
     */
    public static ImageMarginsController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageMarginsController controller = (ImageMarginsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageMarginsFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
