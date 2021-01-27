package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-05-14
 * @License Apache License Version 2.0
 */
public class DataQueryController extends BaseController {

    protected BaseDataManageController dataController;

    @FXML
    protected ControlDataQuery queryController;

    public DataQueryController() {
        baseTitle = message("DataQuery");
    }

    public void setValue(BaseDataManageController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        if (dataController == null || initCondition == null) {
            return;
        }
        try {
            this.baseName = dataController.baseName;
            this.baseTitle = dataController.baseTitle + " " + baseTitle;
            getMyStage().setTitle(dataController.baseTitle + " " + baseTitle);

            ControlStyle.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);

            this.dataController = dataController;
            queryController.setControls(dataController, initCondition, tableDefinition, prefixEditable, supportTop);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        queryController.savedCondition = queryController.save();
        if (queryController.savedCondition == null) {
            return;
        }
        switch (queryController.dataOperation) {
            case QueryData:
                dataController.loadAsConditions(queryController.savedCondition);
                break;
            case ClearData:
                dataController.clearAsConditions(queryController.savedCondition);
                break;
            default:
                return;
        }
        closeStage();
    }

}
