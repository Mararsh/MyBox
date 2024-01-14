package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
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
public class ControlImageRound extends BaseController {

    protected int w, h, wPer, hPer;

    @FXML
    protected ComboBox<String> wSelector, hSelector, wPerSelector, hPerSelector;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected RadioButton wPerRadio, hPerRadio;
    @FXML
    protected ToggleGroup wGroup, hGroup;

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorController.init(this, baseName + "Color", Color.TRANSPARENT);

            w = UserConfig.getInt(baseName + "Width", 20);
            if (w <= 0) {
                w = 20;
            }
            wSelector.getItems().addAll(Arrays.asList("20", "15", "30", "50", "150", "300", "10", "3"));
            wSelector.setValue(w + "");
            wSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkWidth();
                }
            });

            wPer = UserConfig.getInt(baseName + "WidthPercent", 10);
            if (wPer <= 0) {
                wPer = 10;
            }
            wPerSelector.getItems().addAll(Arrays.asList("10", "15", "5", "8", "20", "30", "25"));
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
            if (h <= 0) {
                h = 20;
            }
            hSelector.getItems().addAll(Arrays.asList("20", "15", "30", "50", "150", "300", "10", "3"));
            hSelector.setValue(h + "");
            hSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkHeight();
                }
            });

            hPer = UserConfig.getInt(baseName + "HeightPercent", 10);
            if (hPer <= 0) {
                hPer = 10;
            }
            hPerSelector.getItems().addAll(Arrays.asList("10", "15", "5", "8", "20", "30", "25"));
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
        int v;
        try {
            v = Integer.parseInt(wPerSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0 && v <= 100) {
            wPer = v;
            UserConfig.setInt(baseName + "WidthPercent", wPer);
            ValidationTools.setEditorNormal(wPerSelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("ImageWidthPercentage"));
            ValidationTools.setEditorBadStyle(wPerSelector);
            return false;
        }
    }

    private boolean checkWidth() {
        int v;
        try {
            v = Integer.parseInt(wSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            w = v;
            UserConfig.setInt(baseName + "Width", w);
            ValidationTools.setEditorNormal(wSelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Width"));
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
        int v;
        try {
            v = Integer.parseInt(hPerSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0 && v <= 100) {
            hPer = v;
            UserConfig.setInt(baseName + "HeightPercent", hPer);
            ValidationTools.setEditorNormal(hPerSelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("ImageHeightPercentage"));
            ValidationTools.setEditorBadStyle(hPerSelector);
            return false;
        }
    }

    private boolean checkHeight() {
        int v;
        try {
            v = Integer.parseInt(hSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            h = v;
            UserConfig.setInt(baseName + "Height", h);
            ValidationTools.setEditorNormal(hSelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Height"));
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

    public boolean pickValues() {
        return checkWidthType() && checkHeightType();
    }

}
