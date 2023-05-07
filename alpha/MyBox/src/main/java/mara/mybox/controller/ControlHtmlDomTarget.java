package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.HtmlNode;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class ControlHtmlDomTarget extends BaseHtmlTreeController {
    
    @FXML
    protected RadioButton beforeRadio, afterRadio, inRadio;
    
    @Override
    public void treeClicked(MouseEvent event, TreeItem<HtmlNode> item) {
        if (item == null) {
            return;
        }
        if (item.getParent() == null) {
            inRadio.setSelected(true);
        }
    }
}
