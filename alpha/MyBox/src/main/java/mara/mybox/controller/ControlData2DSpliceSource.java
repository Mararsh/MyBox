package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DDefinition;

/**
 * @Author Mara
 * @CreateDate 2022-5-22
 * @License Apache License Version 2.0
 */
public class ControlData2DSpliceSource extends ControlData2DSource {

    @FXML
    @Override
    public void editAction() {
        Data2DDefinition.open(data2D);
    }

}
