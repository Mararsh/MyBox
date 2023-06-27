package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.SVG;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgTree extends ControlXmlTree {

    protected SvgEditorController editorController;
    protected SVG svg;

    @FXML
    protected ControlSvgNodeEdit svgNodeController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            nodeController = svgNodeController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setRoot(TreeItem<XmlTreeNode> root) {
        super.setRoot(root);
        svg = new SVG(doc);
    }

    @FXML
    @Override
    public void clearTree() {
        super.clearTree();
        svg = null;
    }

}
