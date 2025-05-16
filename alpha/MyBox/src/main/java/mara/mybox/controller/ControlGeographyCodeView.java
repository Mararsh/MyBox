package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.GeographyCodeTools;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class ControlGeographyCodeView extends BaseMapController {

    protected GeographyCodeController dataController;
    protected DataNode viewNode;

    public void setPatrameters(GeographyCodeController controller) {
        try {
            dataController = controller;

            initMap();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadNode(long nodeid) {
        if (webEngine == null || !mapLoaded || nodeid < 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            GeographyCode geoCode;
            DataNode node;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    node = nodeTable.query(conn, nodeid);
                    if (node == null) {
                        return false;
                    }
                    geoCode = GeographyCodeTools.fromNode(node);
                    if (isGaoDeMap()) {
                        geoCode = GeographyCodeTools.toGCJ02(geoCode);
                    } else {
                        geoCode = GeographyCodeTools.toCGCS2000(geoCode, false);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawCode(geoCode);
                dataController.infoButton.setDisable(false);
                dataController.editButton.setDisable(false);
                viewNode = node;
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, dataController.rightPane);
    }

}
