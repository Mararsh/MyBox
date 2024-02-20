package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseChildController extends BaseController {

    @FXML
    protected CheckBox onTopCheck, closeAfterCheck;

    @Override
    public void initValues() {
        try {
            super.initValues();

            stageType = StageType.Child;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (onTopCheck != null) {
                if (getMyStage() != null) {
                    onTopCheck.setSelected(getMyStage().isAlwaysOnTop());
                }
                onTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (getMyStage() == null) {
                            return;
                        }
                        myStage.setAlwaysOnTop(onTopCheck.isSelected());
                        popInformation(myStage.isAlwaysOnTop() ? message("AlwayOnTop") : message("DisableAlwayOnTop"));
                    }
                });
            }
            if (closeAfterCheck != null) {
                closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));
                closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

}
