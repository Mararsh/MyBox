package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlImageShadow extends BaseController {

    protected int w, h, wPer, hPer;

    @FXML
    protected ComboBox<String> wSelector, hSelector, wPerSelector, hPerSelector;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected RadioButton wPerRadio, hPerRadio;
    @FXML
    protected ToggleGroup wGroup, hGroup;
    @FXML
    protected CheckBox blurCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorController.init(this, baseName + "Color", Color.DARKGRAY);

            w = UserConfig.getInt(baseName + "Width", 20);
            wSelector.getItems().addAll(Arrays.asList("10", "-10", "20", "-20", "5", "-5", "8", "-8", "0",
                    "15", "-15", "30", "-30", "3", "-3", "6", "-6", "1", "-1", "50", "-50"));
            wSelector.setValue(w + "");
            wSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkWidth();
                }
            });

            wPer = UserConfig.getInt(baseName + "WidthPercent", 5);
            wPerSelector.getItems().addAll(Arrays.asList("5", "-5", "2", "-2", "1", "-1", "3", "-3", "0",
                    "10", "-10", "4", "-4", "6", "-6", "8", "-8", "7", "-7", "9", "-9", "20", "-20"));
            wPerSelector.setValue(wPer + "");
            wPerSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkWidthPercent();
                }
            });

            wGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkWidthType();
                }
            });
            checkWidthType();

            h = UserConfig.getInt(baseName + "Height", 20);
            hSelector.getItems().addAll(Arrays.asList("10", "-10", "20", "-20", "5", "-5", "8", "-8", "0",
                    "15", "-15", "30", "-30", "3", "-3", "6", "-6", "1", "-1", "50", "-50"));
            hSelector.setValue(h + "");
            hSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkHeight();
                }
            });

            hPer = UserConfig.getInt(baseName + "HeightPercent", 5);
            hPerSelector.getItems().addAll(Arrays.asList("5", "-5", "2", "-2", "1", "-1", "3", "-3", "0",
                    "10", "-10", "4", "-4", "6", "-6", "8", "-8", "7", "-7", "9", "-9", "20", "-20"));
            hPerSelector.setValue(hPer + "");
            hPerSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkHeightPercent();
                }
            });

            hGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkHeightType();
                }
            });
            checkHeightType();

            blurCheck.setSelected(UserConfig.getBoolean(baseName + "Blur", true));
            blurCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.getBoolean(baseName + "Blur", blurCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private boolean checkWidthType() {
        wSelector.setDisable(true);
        wPerSelector.setDisable(true);
        wSelector.getEditor().setStyle(null);
        wPerSelector.getEditor().setStyle(null);

        if (wPerRadio.isSelected()) {
            wPerSelector.setDisable(false);
            return checkWidthPercent();

        } else {
            wSelector.setDisable(false);
            return checkWidth();

        }
    }

    private boolean checkWidthPercent() {
        try {
            wPer = Integer.parseInt(wPerSelector.getValue());
            UserConfig.setInt(baseName + "WidthPercent", wPer);
            ValidationTools.setEditorNormal(wPerSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ImageWidthPercentage"));
            ValidationTools.setEditorBadStyle(wPerSelector);
            return false;
        }
    }

    private boolean checkWidth() {
        try {
            w = Integer.parseInt(wSelector.getValue());
            UserConfig.setInt(baseName + "Width", w);
            ValidationTools.setEditorNormal(wSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("HorizontalOffset"));
            ValidationTools.setEditorBadStyle(wSelector);
            return false;
        }
    }

    private boolean checkHeightType() {
        hSelector.setDisable(true);
        hPerSelector.setDisable(true);
        hSelector.getEditor().setStyle(null);
        hPerSelector.getEditor().setStyle(null);

        if (hPerRadio.isSelected()) {
            hPerSelector.setDisable(false);
            return checkHeightPercent();

        } else {
            hSelector.setDisable(false);
            return checkHeight();

        }
    }

    private boolean checkHeightPercent() {
        try {
            hPer = Integer.parseInt(hPerSelector.getValue());
            UserConfig.setInt(baseName + "HeightPercent", hPer);
            ValidationTools.setEditorNormal(hPerSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ImageHeightPercentage"));
            ValidationTools.setEditorBadStyle(hPerSelector);
            return false;
        }
    }

    private boolean checkHeight() {
        try {
            h = Integer.parseInt(hSelector.getValue());
            UserConfig.setInt(baseName + "Height", h);
            ValidationTools.setEditorNormal(hSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("VerticalOffset"));
            ValidationTools.setEditorBadStyle(hSelector);
            return false;
        }
    }

    public boolean wPercenatge() {
        return wPerRadio.isSelected();
    }

    public boolean hPercenatge() {
        return hPerRadio.isSelected();
    }

    public java.awt.Color awtColor() {
        return colorController.awtColor();
    }

    public Color color() {
        return colorController.color();
    }

    public boolean blur() {
        return blurCheck.isSelected();
    }

    public boolean pickValues() {
        return checkWidthType() && checkHeightType();
    }

}
