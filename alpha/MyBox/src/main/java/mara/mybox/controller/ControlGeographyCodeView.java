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

    protected GeographyCodeController treeController;

    public void setPatrameters(GeographyCodeController controller) {
        try {
            treeController = controller;

            initMap();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadNode(DataNode node) {
        if (webEngine == null || !mapLoaded || node == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            GeographyCode geoCode;
            DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = treeController.nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    geoCode = GeographyCodeTools.fromNode(savedNode);
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
                treeController.viewNode = savedNode;
                treeController.infoButton.setDisable(false);
                treeController.editButton.setDisable(false);
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, treeController.rightPane);
    }

}
