package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import mara.mybox.data.MapPoint;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import static mara.mybox.db.data.GeographyCodeTools.validCoordinate;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeViewController extends MapController {

    protected GeographyCodeController treeController;
    protected GeographyCode geographyCode;

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
        mapPoints = null;
        geographyCode = null;
        webEngine.executeScript("clearMap();");
        titleLabel.setText("");
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private MapPoint mapPoint;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    DataNode savedNode = treeController.nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    GeographyCode tcode;
                    geographyCode = GeographyCodeTools.fromNode(savedNode);
                    if (!validCoordinate(geographyCode)) {
                        error = message("Invalid");
                        return false;
                    }
                    if (isGaoDeMap()) {
                        tcode = GeographyCodeTools.toGCJ02(geographyCode);
                    } else {
                        tcode = GeographyCodeTools.toCGCS2000(geographyCode, false);
                    }
                    mapPoint = new MapPoint(tcode);
                    mapPoint.setLabel(geographyCode.getName())
                            .setInfo(treeController.nodeTable.htmlList(savedNode))
                            .setMarkSize(markerSize)
                            .setMarkerImage(image())
                            .setTextSize(textSize)
                            .setTextColor(textColor)
                            .setIsBold(isBold)
                            .setIsPopInfo(true);
                    mapPoints = new ArrayList<>();
                    mapPoints.add(mapPoint);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawPoint(mapPoint);
                treeController.currentNode = node;
                treeController.opPane.setVisible(true);
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, treeController.rightPane);
    }

}
