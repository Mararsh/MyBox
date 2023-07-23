package mara.mybox.controller;

import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlController extends JShellController {

    protected JexlEditorController jexlEditor;

    public JexlController() {
        baseTitle = message("JEXL");
        TipsLabelKey = "JEXLTips";
        category = InfoNode.JEXLCode;
        nameMsg = message("Title");
        valueMsg = message("Codes");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            jexlEditor = (JexlEditorController) editorController;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void edit(String script, String context, String parameters) {
        editNode(null);
        jexlEditor.valueInput.setText(script);
        jexlEditor.moreInput.setText(context);
        jexlEditor.parametersInput.setText(parameters);
    }


    /*
        static methods
     */
    public static JexlController open(String script, String context, String parameters) {
        JexlController controller = (JexlController) WindowTools.openStage(Fxmls.JexlFxml);
        controller.edit(script, context, parameters);
        controller.requestMouse();
        return controller;
    }

}
