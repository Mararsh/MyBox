package mara.mybox.controller;

import javafx.fxml.FXML;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgNodeEdit extends ControlXmlNodeEdit {

    @FXML
    public void drawAction() {
        SvgElementEditController.open(((ControlSvgTree) treeController).editorController, treeItem);
    }

}
