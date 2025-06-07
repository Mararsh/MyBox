package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
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

    protected ColorData colorData;

    @FXML
    protected ControlColorInput colorController;
    @FXML
    protected Button refreshButton, paletteButton;
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
            colorController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshAction();
                }
            });

            separatorInput.setText(UserConfig.getString(baseName + "Separator", ", "));

            refreshButton.disableProperty().bind(colorController.colorInput.textProperty().isEmpty()
                    .or(separatorInput.textProperty().isEmpty())
            );
            paletteButton.disableProperty().bind(colorController.colorInput.textProperty().isEmpty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            colorData = colorController.colorData;
            if (colorData == null || colorData.getRgba() == null) {
                return;
            }
            String separator = separatorInput.getText();
            if (separator == null || separator.isEmpty()) {
                separator = ", ";
            }
            UserConfig.setString(baseName + "Separator", separator);
            colorData = new ColorData(colorData.getRgba())
                    .setvSeparator(separator).convert();
            htmlController.displayHtml(colorData.html());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void addColor() {
        if (colorData == null) {
            return;
        }
        ColorsManageController.addOneColor(colorData.getColor());
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("ColorHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.colorHelps(true));
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (colorController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
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
            MyBoxLog.error(e);
            return null;
        }
    }

}
