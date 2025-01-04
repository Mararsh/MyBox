package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.data.GeographyCode;
import mara.mybox.tools.GeographyCodeTools;
import static mara.mybox.tools.GeographyCodeTools.validCoordinate;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeViewController extends BaseMapController {

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

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    DataNode savedNode = treeController.nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    geoCode = GeographyCodeTools.fromNode(savedNode);
                    if (!validCoordinate(geoCode)) {
                        error = message("Invalid");
                        return false;
                    }
                    if (isGaoDeMap()) {
                        geoCode = GeographyCodeTools.toGCJ02(geoCode);
                    } else {
                        geoCode = GeographyCodeTools.toCGCS2000(geoCode, false);
                    }
                    return geoCode != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawCode(geoCode);
                treeController.currentNode = node;
                treeController.setButtonsDisable(false);
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, treeController.rightPane);
    }

}
