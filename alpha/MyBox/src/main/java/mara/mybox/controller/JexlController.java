package mara.mybox.controller;

import mara.mybox.db.data.TreeNode;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlController extends JShellController {

    public JexlController() {
        baseTitle = message("JEXL");
        TipsLabelKey = "JEXLTips";
        category = TreeNode.JEXL;
        nameMsg = message("Title");
        valueMsg = message("Codes");
    }

}
