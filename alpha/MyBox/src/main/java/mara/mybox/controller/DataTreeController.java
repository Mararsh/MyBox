package mara.mybox.controller;

import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableNodeDataColumn;
import mara.mybox.db.table.TableNodeHtml;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.db.table.TableNodeJEXL;
import mara.mybox.db.table.TableNodeJShell;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.db.table.TableNodeMathFunction;
import mara.mybox.db.table.TableNodeRowExpression;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableNodeText;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class DataTreeController extends BaseDataTreeController {

    @Override
    public void initControls() {
        try {
            super.initControls();

            treeController.setParameters(this);
            tableController.setParameters(this);

            if (viewController != null) {
                viewController.setParent(this);
                viewController.initStyle = HtmlStyles.styleValue("Table");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static DataTreeController open(BaseController pController, boolean replaceScene, BaseNodeTable table) {
        try {
            if (table == null) {
                return null;
            }
            DataTreeController controller;
            if ((replaceScene || AppVariables.closeCurrentWhenOpenTool) && pController != null) {
                controller = (DataTreeController) pController.loadScene(Fxmls.DataTreeFxml);
            } else {
                controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
            }
            controller.requestMouse();
            controller.initDataTree(table, null);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController open(BaseNodeTable table, DataNode node) {
        try {
            DataTreeController controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
            controller.initDataTree(table, node);
            controller.setAlwaysOnTop();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController textTree(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeText());
    }

    public static DataTreeController htmlTree(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeHtml());
    }

    public static DataTreeController webFavorite(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeWebFavorite());
    }

    public static DataTreeController sql(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeSQL());
    }

    public static DataTreeController mathFunction(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeMathFunction());
    }

    public static DataTreeController imageScope(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeImageScope());
    }

    public static DataTreeController jShell(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJShell());
    }

    public static DataTreeController jexl(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJEXL());
    }

    public static DataTreeController javascript(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJavaScript());
    }

    public static DataTreeController rowExpression(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeRowExpression());
    }

    public static DataTreeController dataColumn(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeDataColumn());
    }

}
