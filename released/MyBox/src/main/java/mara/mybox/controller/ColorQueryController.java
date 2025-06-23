package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
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
    protected Tab colorTab, resultTab;
    @FXML
    protected ControlColorInput colorController;
    @FXML
    protected Button refreshButton, paletteButton;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected ToggleGroup separatorGroup;
    @FXML
    protected RadioButton commaRadio, hyphenRadio, colonRadio, blankRadio, inputRadio;
    @FXML
    protected HtmlTableController htmlController;

    public ColorQueryController() {
        baseTitle = message("QueryColor");
    }

    @Override
    public void initControls() {
        try {
            separatorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    if (!inputRadio.isSelected()) {
                        goAction();
                    }
                }
            });

            separatorInput.setText(UserConfig.getString(baseName + "Separator", null));

            goButton.disableProperty().bind(colorController.colorInput.textProperty().isEmpty()
                    .or(separatorInput.textProperty().isEmpty())
            );
            htmlController.initStyle(HtmlStyles.TableStyle);

            initMore();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMore() {
        try {
            colorController.setParameter(baseName, Color.GOLD);

            colorController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            goAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String pickSeparator() {
        try {
            String separator = ", ";
            if (commaRadio.isSelected()) {
                separator = ", ";
            } else if (hyphenRadio.isSelected()) {
                separator = "-";
            } else if (colonRadio.isSelected()) {
                separator = ":";
            } else if (blankRadio.isSelected()) {
                separator = " ";
            } else if (inputRadio.isSelected()) {
                separator = separatorInput.getText();
                if (separator == null || separator.isEmpty()) {
                    return null;
                }
                UserConfig.setString(baseName + "Separator", separator);
            }
            return separator;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    public void goAction() {
        try {
            colorData = colorController.colorData;
            if (colorData == null || colorData.getRgba() == null) {
                return;
            }
            String separator = pickSeparator();
            if (separator == null || separator.isEmpty()) {
                popError(message("InvalidParamter") + ": " + message("ValueSeparator"));
                return;
            }
            colorData = new ColorData(colorData.getRgba())
                    .setvSeparator(separator).convert();
            htmlController.displayHtml(colorData.html());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        if (colorTab.isSelected()) {
            if (colorController.keyEventsFilter(event)) {
                return true;
            }
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
