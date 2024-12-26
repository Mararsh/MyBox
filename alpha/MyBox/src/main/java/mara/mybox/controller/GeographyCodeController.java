package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends DataTreeController {

    @FXML
    protected GeographyCodeViewController mapController;
    @FXML
    protected FlowPane opPane;

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
        opPane.setVisible(false);
    }

    @FXML
    @Override
    public boolean infoAction() {
        if (currentNode == null) {
            return false;
        }
        FxTask loadTask = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = nodeTable.query(conn, currentNode.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    html = nodeTable.htmlTable(savedNode);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlPopController.showHtml(myController, html);
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(loadTask, false);
        return true;
    }

    @FXML
    public boolean htmlAction() {
        mapController.htmlAction();
        return true;
    }

    @FXML
    public boolean snapAction() {
        mapController.popAction();
        return true;
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
