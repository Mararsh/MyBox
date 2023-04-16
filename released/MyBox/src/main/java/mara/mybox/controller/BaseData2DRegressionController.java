package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-8-19
 * @License Apache License Version 2.0
 */
public class BaseData2DRegressionController extends BaseData2DChartController {

    protected double alpha, intercept, rSquare;

    @FXML
    protected CheckBox interceptCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlData2DResults regressionDataController;
    @FXML
    protected ControlWebView modelController;
    @FXML
    protected Button dataButton;
    @FXML
    protected ControlData2DResults resultsController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            alpha = UserConfig.getDouble(baseName + "Alpha", 0.05);
            if (alpha >= 1 || alpha <= 0) {
                alpha = 0.05;
            }
            if (alphaSelector != null) {
                alphaSelector.getItems().addAll(Arrays.asList(
                        "0.05", "0.01", "0.02", "0.03", "0.06", "0.1"
                ));
                alphaSelector.getSelectionModel().select(alpha + "");
                alphaSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            double v = Double.parseDouble(newValue);
                            if (v > 0 && v < 1) {
                                alpha = v;
                                alphaSelector.getEditor().setStyle(null);
                                UserConfig.setDouble(baseName + "Alpha", alpha);
                            } else {
                                alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            if (interceptCheck != null) {
                interceptCheck.setSelected(UserConfig.getBoolean(baseName + "Intercept", true));
                interceptCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Intercept", interceptCheck.isSelected());
                });
            }

            if (modelController != null) {
                modelController.setParent(this);
            }
            if (regressionDataController != null) {
                regressionDataController.setNoRowNumber();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editModelAction() {
        modelController.editAction();
    }

    @FXML
    public void popModelMenu(Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showModelMenu(event);
        }
    }

    @FXML
    public void showModelMenu(Event event) {
        modelController.showFunctionsMenu(event);
    }

    @FXML
    @Override
    public void dataAction() {
        resultsController.dataAction();
    }

    @FXML
    @Override
    public void viewAction() {
        resultsController.editAction();
    }

    @FXML
    public void about() {
        openLink(HelpTools.AboutDataAnalysisHtml());
    }

}
