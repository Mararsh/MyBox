package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
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
            scopeController.setEditor(this);
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
            return ImageScopeTools.toDataNode(scope);
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
                || scope == null || scope.getScopeType() == null) {
            return;
        }
        if (nodeEditor.isNewNode()) {
            nodeEditor.isSettingValues = true;
            nodeEditor.titleInput.setText(
                    scope.getScopeType() + "_" + DateTools.datetimeToString(new Date()));
            nodeEditor.isSettingValues = false;
        }
        setScopeControls(scope);
    }

    @FXML
    public void clearValue() {
        scopeController.resetScope();
    }

//    @Override
//    public void pasteNode(InfoNode node) {
//        if (node == null) {
//            return;
//        }
//        if (scopeController.scope == null) {
//            return;
//        }
////        ImageScope srcScope = ImageScopeTools.fromXML(null, myController, node.getInfo());
////        if (srcScope != null) {
////            scopeController.loadScope();
////            nodeChanged(true);
////        }
////        tabPane.getSelectionModel().select(valueTab);
//    }
//
//    @Override
//    public void newNodeCreated() {
//        super.newNodeCreated();
//        scopeController.indicateScope();
//    }
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return scopeController.keyEventsFilter(event); // pass event to editor
    }

}
