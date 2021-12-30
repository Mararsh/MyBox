package mara.mybox.controller;

import javafx.fxml.FXML;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseChildController extends BaseController {

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
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

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

}
