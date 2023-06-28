package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgTree extends ControlXmlTree {

    protected SvgEditorController editorController;

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

}
