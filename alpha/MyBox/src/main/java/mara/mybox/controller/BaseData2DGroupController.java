package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class BaseData2DGroupController extends Data2DChartXYController {

    @FXML
    protected Tab groupTab;
    @FXML
    protected ControlSelection groupController;
    @FXML
    protected ToggleGroup groupGroup;
    @FXML
    protected RadioButton groupValuesRadio, groupIntervalRadio, groupNumberRadio, groupConditionsRadio;
    @FXML
    protected VBox groupBox, groupValuesBox, groupConditionsBox;
    @FXML
    protected HBox groupColumnlBox, groupIntervalBox, groupNumberBox;

    public BaseData2DGroupController() {
        baseTitle = message("GroupStatistic");
        TipsLabelKey = "GroupEqualTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            groupController.setParameters(this, message("Column"), message("GroupBy"));
            groupGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkGroupType();
                }
            });
            checkGroupType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterRefreshControls() {
    }

    public void checkGroupType() {
        try {
            groupBox.getChildren().clear();
            if (groupValuesRadio.isSelected()) {
                groupBox.getChildren().add(groupValuesBox);

            } else if (groupIntervalRadio.isSelected()) {
                groupBox.getChildren().addAll(groupColumnlBox, groupIntervalBox);

            } else if (groupNumberRadio.isSelected()) {
                groupBox.getChildren().addAll(groupColumnlBox, groupNumberBox);

            } else if (groupConditionsRadio.isSelected()) {
                groupBox.getChildren().add(groupConditionsBox);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void addCondition() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void deleteConditions() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
