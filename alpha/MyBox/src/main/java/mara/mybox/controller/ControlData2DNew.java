package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-11
 * @License Apache License Version 2.0
 */
public class ControlData2DNew extends ControlData2DTarget {

    protected Data2DCreateController createController;

    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;
    @FXML
    protected TextArea descInput;

    @Override
    public boolean isInvalid() {
        return true;
    }

    public void setParameters(Data2DCreateController controller) {
        try {
            tableController = null;
            createController = controller;
            baseName = createController.baseName + "_" + baseName;

            initControls(baseName);

            optionsPane.getTabs().clear();
            optionsBox.getChildren().clear();

            initTarget(Data2D_Attributes.TargetType.valueOf(UserConfig.getString(baseName + "DataTarget", "CSV")));
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkTarget();
                }
            });
            matrixOptionsController.typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (matrixRadio.isSelected()) {
                        createController.loadValues();
                    }
                }
            });

            checkTarget();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public TargetType checkTarget() {
        try {
            data2D = null;
            super.checkTarget();
            createController.loadValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return format;
    }

}
