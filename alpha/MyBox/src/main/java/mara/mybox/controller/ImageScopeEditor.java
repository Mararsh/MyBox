package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ImageScopeEditor extends InfoTreeNodeEditor {

    protected Image srcImage;

    @FXML
    protected ControlSelectPixels scopeController;

    public ImageScopeEditor() {
        defaultExt = "png";
    }

    @Override
    public void setManager(InfoTreeManageController treeController) {
        try {
            super.setManager(treeController);

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

    protected void setScope(ImageScope scope) {
        if (isSettingValues || !isNewNode()
                || scope == null || scope.getScopeType() == null) {
            return;
        }
        attributesController.nameInput.setText(
                scope.getScopeType() + "_" + DateTools.datetimeToString(new Date()));
    }

    @Override
    protected void editInfo(InfoNode node) {
        sourceFile = null;
        srcImage = null;
        ImageScope scope = null;
        if (node != null) {
            scope = ImageScopeTools.fromXML(null, myController, node.getInfo());
        }
        scopeController.loadScope(scope);
    }

    @Override
    protected InfoNode nodeInfo(InfoNode node) {
        if (node == null) {
            return null;
        }
        ImageScope scope = scopeController.pickScopeValues();
        if (scope == null) {
            return null;
        }
        scope.setName(nodeTitle());
        String info = ImageScopeTools.toXML(scope, "");
        if (info == null) {
            return null;
        }
        return node.setInfo(info);
    }

    @FXML
    @Override
    public void clearValue() {
        scopeController.clearControls();
    }

    @Override
    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        if (scopeController.scope == null) {
            return;
        }
//        ImageScope srcScope = ImageScopeTools.fromXML(null, myController, node.getInfo());
//        if (srcScope != null) {
//            scopeController.loadScope();
//            nodeChanged(true);
//        }
//        tabPane.getSelectionModel().select(valueTab);
    }

    @Override
    public void newNodeCreated() {
        super.newNodeCreated();
        scopeController.applyScope();
    }

}
