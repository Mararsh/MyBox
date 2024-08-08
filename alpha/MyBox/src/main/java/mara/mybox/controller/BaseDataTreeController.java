package mara.mybox.controller;

import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeController extends BaseController {

    protected ControlDataTreeView treeView;
    protected BaseDataTreeNodeController nodeController;

    protected BaseTable dataTable;
    protected TableTree tableTree;
    protected TableTag tableTag;
    protected TableTreeTag tableTreeTag;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void initBaseTable(BaseTable baseTable,
            ControlDataTreeView tree,
            BaseDataTreeNodeController controller) {
        try {
            dataTable = baseTable;
            nodeController = controller;
            treeView = tree;

            tableTree = new TableTree(dataTable);
            tableTag = new TableTag();
            tableTreeTag = new TableTreeTag(tableTree);

            nodeController.setManager(this);

            treeView.setManager(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void popNode(TreeNode item) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = item.html();
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        start(task);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (nodeController == null) {
            return super.keyEventsFilter(event);
        }
        if (nodeController.thisPane.isFocused() || nodeController.thisPane.isFocusWithin()) {
            if (nodeController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return nodeController.keyEventsFilter(event); // pass event to editor
    }

}
