package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends DataTreeController {

    @FXML
    protected ControlGeographyCodeView mapController;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");
    }

    public void initCodes(boolean checkEmpty) {
        try {
            initDataTree(new TableNodeGeographyCode(), null, checkEmpty);

            mapController.setPatrameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void autoTesting() {
        try {
            testing = true;
            myStage.setIconified(true);
            AppVariables.autoTestingController.sceneLoaded();

            nodeTable = new TableNodeGeographyCode();
            nodeTable.clearData();

            initDataTree(nodeTable, null, false);
            mapController.setPatrameters(this);

            importExamples(null);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void nullView() {
        mapController.viewNode = null;
        infoButton.setDisable(true);
        editButton.setDisable(true);
        mapController.clearMap();
    }

    @Override
    public void viewNode(DataNode node) {
        nullView();
        if (node == null) {
            return;
        }
        mapController.loadNode(node.getNodeid());
    }

    @Override
    public void showNode(DataNode node) {
        viewNode(node);
    }

    @Override
    protected void reloadView(DataNode node) {
        if (mapController.viewNode != null
                && mapController.viewNode.equals(node)) {
            viewNode(node);
        }
    }

    @FXML
    @Override
    public boolean infoAction() {
        if (mapController.viewNode == null) {
            return false;
        }
        popNode(mapController.viewNode);
        return true;
    }

    @FXML
    public boolean htmlAction() {
        mapController.htmlAction();
        return true;
    }

    @FXML
    public boolean snapAction() {
        mapController.snapAction();
        return true;
    }

    @FXML
    public void optionsAction() {
        mapController.optionsAction();
    }

    @FXML
    public void editAction() {
        editNode(mapController.viewNode);
    }

    @Override
    public void cleanPane() {
        try {
            mapController.cleanPane();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return mapController.keyEventsFilter(event);
    }

    /*
        static methods
     */
    public static GeographyCodeController open() {
        try {
            GeographyCodeController controller
                    = (GeographyCodeController) WindowTools.openStage(Fxmls.GeographyCodeFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCodeController open(BaseController pController, boolean replaceScene, boolean checkEmpty) {
        try {
            GeographyCodeController controller;
            if ((replaceScene || AppVariables.closeCurrentWhenOpenTool) && pController != null) {
                controller = (GeographyCodeController) pController.loadScene(Fxmls.GeographyCodeFxml);
            } else {
                controller = open();
            }
            controller.initCodes(checkEmpty);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
