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
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class Data2DSetStylesController extends Data2DHandleController {

    protected String style;

    @FXML
    protected ToggleGroup colorGroup, bgGroup;
    @FXML
    protected ColorSet fontColorController, bgColorController;
    @FXML
    protected ComboBox<String> fontSizeSelector;
    @FXML
    protected CheckBox colorCheck, bgCheck, sizeCheck, boldCheck;
    @FXML
    protected TextField moreInput;
    @FXML
    protected Label effectLabel;
    @FXML
    protected RadioButton clearRadio, colorDefaultRadio, bgDefaultRadio;
    @FXML
    protected VBox styleBox;

    @Override
    public void initControls() {
        try {
            super.initControls();

            styleBox.disableProperty().bind(clearRadio.selectedProperty());

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle();
                }
            });
            fontColorController.thisPane.disableProperty().bind(colorDefaultRadio.selectedProperty());
            fontColorController.init(this, baseName + "Color", Color.BLACK);
            fontColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle();
                }
            });

            bgGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle();
                }
            });
            bgColorController.thisPane.disableProperty().bind(bgDefaultRadio.selectedProperty());
            bgColorController.init(this, baseName + "BgColor", Color.TRANSPARENT);
            bgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle();
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
                    UserConfig.setString(baseName + "FontSize", newValue);
                    checkStyle();
                }
            });

            boldCheck.setSelected(UserConfig.getBoolean(baseName + "Bold", false));
            boldCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Bold", newValue);
                    checkStyle();
                }
            });

            moreInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkStyle();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkStyle() {
        if (clearRadio.isSelected()) {
            style = null;
            return;
        }
        style = "";
        String size = fontSizeSelector.getValue();
        if (size != null && !message("Default").equals(size)) {
            style = "-fx-font-size: " + size + "; ";
        }
        if (!colorDefaultRadio.isSelected()) {
            style += "-fx-text-fill: " + fontColorController.rgb() + "; ";
        }
        if (!bgDefaultRadio.isSelected()) {
            style += "-fx-background-color: " + bgColorController.rgb() + "; ";
        }
        if (boldCheck.isSelected()) {
            style += "-fx-font-weight: bolder; ";
        }
        String more = moreInput.getText();
        if (more != null && !more.isBlank()) {
            style += more;
        }
        if (style.isBlank()) {
            effectLabel.setStyle(null);
        } else {
            effectLabel.setStyle(style);
        }
    }

    @Override
    public boolean checkOptions() {
        checkStyle();
        return super.checkOptions();
    }

    @Override
    public void handleRowsTask() {
        try {
            tableController.isSettingValues = true;
            for (int row : sourceController.checkedRowsIndices) {
                for (int col : sourceController.checkedColsIndices) {
                    data2D.setStyle(row, col, style);
                }
                tableController.tableData.set(row, tableController.tableData.get(row));
                sourceController.tableData.set(row, sourceController.tableData.get(row));
            }
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }


    /*
        static
     */
    public static Data2DSetStylesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetStylesController controller = (Data2DSetStylesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetStylesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
