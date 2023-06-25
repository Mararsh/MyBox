package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-29
 * @License Apache License Version 2.0
 */
public class ColorQueryController extends BaseController {

    protected ColorData color;

    @FXML
    protected TextField colorInput;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected Button queryButton, refreshButton, paletteButton;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected HtmlTableController htmlController;

    public ColorQueryController() {
        baseTitle = message("ColorQuery");
    }

    @Override
    public void initControls() {
        try {
            colorInput.setText("#552288");

            colorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                if (isSettingValues || newVal == null) {
                    return;
                }
                colorInput.setText(FxColorTools.color2rgba(newVal));
                queryAction();
            });

            separatorInput.setText(UserConfig.getString(baseName + "Separator", ", "));

            queryButton.disableProperty().bind(colorInput.textProperty().isEmpty());
            refreshButton.disableProperty().bind(colorInput.textProperty().isEmpty()
                    .or(separatorInput.textProperty().isEmpty())
            );
            paletteButton.disableProperty().bind(queryButton.disableProperty());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(queryButton, message("Query") + "\nF1 / ENTER");
            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void queryAction() {
        try {
            String value = colorInput.getText();
            if (value == null || value.isBlank()) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return;
            }
            String separator = separatorInput.getText();
            if (separator == null || separator.isEmpty()) {
                separator = ", ";
            }
            UserConfig.setString(baseName + "Separator", separator);
            ColorData c = new ColorData(value).setvSeparator(separator).convert();
            if (c.getSrgb() == null) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return;
            }
            TableStringValues.add("ColorQueryColorHistories", value);
            color = c;
            htmlController.displayHtml(color.html());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void showExamples(Event event) {
        PopTools.popColorExamples(this, colorInput, event);
    }

    @FXML
    public void popExamples(Event event) {
        if (UserConfig.getBoolean("ColorExamplesPopWhenMouseHovering", false)) {
            showExamples(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, colorInput, event, "ColorQueryColorHistories", false, true);
    }

    @FXML
    protected void popColorHistories(Event event) {
        if (UserConfig.getBoolean("ColorQueryColorHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        queryAction();
    }

    @FXML
    public void addColor() {
        if (color == null) {
            return;
        }
        ColorsManageController.addOneColor(color.getColor());
    }

    @Override
    public boolean keyEnter() {
        return keyF1();
    }

    @Override
    public boolean keyF1() {
        if (queryButton.isDisable()) {
            return false;
        }
        queryAction();
        return true;
    }

    /*
        static
     */
    public static ColorQueryController open() {
        try {
            ColorQueryController controller = (ColorQueryController) WindowTools.openStage(Fxmls.ColorQueryFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
