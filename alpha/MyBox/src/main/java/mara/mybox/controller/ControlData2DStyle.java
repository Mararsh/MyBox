package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class ControlData2DStyle extends BaseController {

    protected Data2DStyle currentStyle;
    protected Label showLabel;

    @FXML
    protected ToggleGroup colorGroup, bgGroup;
    @FXML
    protected ColorSet fontColorController, bgColorController;
    @FXML
    protected ComboBox<String> fontSizeSelector;
    @FXML
    protected CheckBox boldCheck;
    @FXML
    protected TextArea moreInput;
    @FXML
    protected RadioButton colorDefaultRadio, colorSetRadio, bgDefaultRadio, bgSetRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle(currentStyle);
                }
            });
            fontColorController.thisPane.disableProperty().bind(colorDefaultRadio.selectedProperty());
            fontColorController.init(this, baseName + "Color", Color.BLACK);
            fontColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle(currentStyle);
                }
            });

            bgGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle(currentStyle);
                }
            });
            bgColorController.thisPane.disableProperty().bind(bgDefaultRadio.selectedProperty());
            bgColorController.init(this, baseName + "BgColor", Color.TRANSPARENT);
            bgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle(currentStyle);
                }
            });

            List<String> sizes = Arrays.asList(
                    message("Default"), "0.8em", "1.2em",
                    "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            fontSizeSelector.getItems().addAll(sizes);
            fontSizeSelector.getSelectionModel().select(UserConfig.getString(baseName + "FontSize", message("Default")));
            fontSizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> o, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString(baseName + "FontSize", newValue);
                    checkStyle(currentStyle);
                }
            });

            boldCheck.setSelected(UserConfig.getBoolean(baseName + "Bold", false));
            boldCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Bold", newValue);
                    checkStyle(currentStyle);
                }
            });

            moreInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkStyle(currentStyle);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkStyle(Data2DStyle style) {
        if (isSettingValues || style == null) {
            return;
        }
        currentStyle = style;
        if (colorDefaultRadio.isSelected()) {
            currentStyle.setFontColor(null);
        } else {
            currentStyle.setFontColor(fontColorController.rgb());
        }
        if (bgDefaultRadio.isSelected()) {
            currentStyle.setBgColor(null);
        } else {
            currentStyle.setBgColor(bgColorController.rgb());
        }
        currentStyle.setFontSize(fontSizeSelector.getValue());
        currentStyle.setBold(boldCheck.isSelected());
        currentStyle.setMoreStyle(moreInput.getText());
        if (showLabel != null) {
            showLabel.setText(currentStyle.getD2sid() < 0
                    ? message("NewData") : (message("ID") + ": " + currentStyle.getD2sid()));
            showLabel.setStyle(currentStyle.finalStyle());
        }
    }

    public void loadNull(Data2DStyle style) {
        currentStyle = style;
        isSettingValues = true;
        colorDefaultRadio.fire();
        bgDefaultRadio.fire();
        fontSizeSelector.setValue(message("Default"));
        boldCheck.setSelected(false);
        moreInput.clear();
        isSettingValues = false;
    }

    // For display, indices are 1-based and included
    // For internal, indices are 0-based and excluded
    public void editStyle(Data2DStyle style) {
        if (style == null) {
            loadNull(style);
            return;
        }
        currentStyle = style;
        isSettingValues = true;
        if (currentStyle.getFontColor() != null && !currentStyle.getFontColor().isBlank()) {
            fontColorController.setColor(Color.web(currentStyle.getFontColor()));
            colorSetRadio.fire();
        }
        if (currentStyle.getBgColor() != null && !currentStyle.getBgColor().isBlank()) {
            bgColorController.setColor(Color.web(currentStyle.getBgColor()));
            bgSetRadio.fire();
        }
        fontSizeSelector.setValue(currentStyle.getFontSize());
        boldCheck.setSelected(currentStyle.isBold());
        moreInput.setText(currentStyle.getMoreStyle());
        isSettingValues = false;
    }

    @FXML
    public void clearMoreSyles() {
        moreInput.clear();
    }

    @FXML
    public void cssGuide() {
        WebBrowserController.oneOpen("https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html", true);
    }

}
