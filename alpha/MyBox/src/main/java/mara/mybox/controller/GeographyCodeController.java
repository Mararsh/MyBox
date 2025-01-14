package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeGeographyCode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends DataTreeController {

    @FXML
    protected GeographyCodeViewController mapController;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTree(new TableNodeGeographyCode(), null);

            mapController.setPatrameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void loadCurrent(DataNode node) {
        nullCurrent();
        if (node == null) {
            return;
        }
        mapController.loadNode(node);
    }

    @Override
    protected void nullCurrent() {
        currentNode = null;
        infoButton.setDisable(true);
        editButton.setDisable(true);
        mapController.clearMap();
    }

    @FXML
    @Override
    public boolean infoAction() {
        if (currentNode == null) {
            return false;
        }
        popNode(currentNode);
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

    @Override
    public void cleanPane() {
        try {
            mapController.cleanPane();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
