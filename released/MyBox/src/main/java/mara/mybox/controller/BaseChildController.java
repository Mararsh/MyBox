package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseChildController extends BaseController {

    @FXML
    protected CheckBox closeAfterCheck;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (closeAfterCheck != null) {
                closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                    }
                });
                closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));
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
