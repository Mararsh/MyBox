package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class DataManufactureController extends BaseData2DController {

    @FXML
    protected Button tableDefinitionButton;

    public DataManufactureController() {
        baseTitle = message("DataManufacture");
    }

    @Override
    public void initData() {
        try {
            setDataType(Data2D.Type.CSV);
            dataController.isManufacture = true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
