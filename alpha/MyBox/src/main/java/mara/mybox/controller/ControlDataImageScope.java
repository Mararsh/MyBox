package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.tools.ImageScopeTools;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataImageScope extends BaseDataValuesController {

    protected Image srcImage;

    @FXML
    protected ControlImageScope scopeController;

    @Override
    public void initEditor() {
        try {
            scopeController.setDataEditor(this);
            scopeController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            ImageScope scope = null;
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                scope = ImageScopeTools.fromDataNode(null, myController, nodeEditor.currentNode);
            }
            if (scope == null) {
                scope = new ImageScope();
            }
            isSettingValues = false;
            valueChanged(false);
            setScopeControls(scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            ImageScope scope = scopeController.pickScopeValues();
            return ImageScopeTools.toDataNode(node, scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void setScopeControls(ImageScope scope) {
        if (scope == null) {
            return;
        }
        scopeController.scope = scope.cloneValues();
        File file = null;
        if (scope.getFile() != null) {
            file = new File(scope.getFile());
        }
        if (file == null || !file.exists()) {
            file = ImageItem.exampleImageFile();
        }
        scopeController.sourceFileChanged(file);
    }

    protected void loadScope(ImageScope scope) {
        if (isSettingValues
                || scope == null || scope.getShapeType() == null) {
            return;
        }
        nodeEditor.editNull();
        nodeEditor.isSettingValues = true;
        nodeEditor.titleInput.setText(
                scope.getShapeType() + "_" + DateTools.datetimeToString(new Date()));
        nodeEditor.isSettingValues = false;
        setScopeControls(scope);
    }

    @FXML
    public void clearValue() {
        scopeController.resetScope();
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image fileImage;

            @Override
            protected boolean handle() {
                try {
                    fileImage = FxImageTools.readImage(this, file);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return fileImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = file;
                srcImage = fileImage;
                scopeController.image = fileImage;
                scopeController.applyScope(scopeController.scope);
            }

        };
        start(task);
    }

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent, ImageScope scope) {
        try {
            DataTreeNodeEditorController controller = DataTreeNodeEditorController.open(parent);
            controller.setTable(new TableNodeImageScope());
            ((ControlDataImageScope) controller.dataController).loadScope(scope);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
