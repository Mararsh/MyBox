package mara.mybox.controller;

import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTableTreeData;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeController extends BaseController {

    protected ControlDataTreeView treeController;
    protected BaseDataTreeNodeController nodeController;

    protected BaseTableTreeData dataTable;
    protected TableDataNode treeTable;
    protected TableTag tagTable;
    protected TableDataTag treeTagTable;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataTable == null) {
                return;
            }
            treeTable = new TableDataNode(dataTable);
            tagTable = new TableTag();
            treeTagTable = new TableDataTag(dataTable);

            nodeController.setParameters(this);

            treeController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void popNode(DataNode item) {
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