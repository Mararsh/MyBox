package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
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

    protected ImageScopeController scopeController;
    protected ImageScope scope;

    @FXML
    protected ControlSelectPixels valuesController;

    public ImageScopeEditor() {
        defaultExt = "png";
    }

    protected void setParameters(ImageScopeController scopeController) {
        try {
            this.scopeController = scopeController;
            valuesController.showNotify.addListener(new ChangeListener<Boolean>() {
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
    protected void editNode(InfoNode node) {
        if (node != null) {
            scope = ImageScopeTools.fromXML(null, myController, node.getInfo());
        } else {
            scope = null;
        }
        if (scope == null) {
            scope = new ImageScope();
        }
        valuesController.loadScope(scope);
        nodeChanged(false);
        updateEditorTitle(node);
    }

    @Override
    protected String nodeInfo() {
        if (scope == null) {
            scope = new ImageScope();
        }
        scope.setName(nodeTitle());
        String info = ImageScopeTools.toXML(scope, "");
        return info;
    }

    @Override
    public void valueChanged(boolean changed) {
        super.valueChanged(changed);
        if (isSettingValues || scope == null) {
            return;
        }
        String name = attributesController.nameInput.getText();
        if (name == null || name.isBlank()) {
            attributesController.nameInput.setText(
                    scope.getScopeType() + "_" + DateTools.datetimeToString(new Date()));
        }
    }

    @FXML
    @Override
    public void clearValue() {
        valuesController.clearControls();
    }

    @Override
    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        if (scope == null) {
            scope = new ImageScope();
        }
        ImageScope srcScope = ImageScopeTools.fromXML(null, myController, node.getInfo());
        if (srcScope == null) {
            valuesController.loadScope(scope);
            nodeChanged(true);
        }
        tabPane.getSelectionModel().select(valueTab);
    }

}
