package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class TextDelimiterController extends ControlTextDelimiter {

    protected SimpleBooleanProperty okNotify;

    public TextDelimiterController() {
    }

    public void setParameters(BaseController parent, String initName, boolean hasBlanks) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            okNotify = new SimpleBooleanProperty();

            setControls(baseName, hasBlanks);
            setDelimiter(initName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        okNotify.set(!okNotify.get());
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    /*
        static
     */
    public static TextDelimiterController open(BaseController parent, String initName, boolean hasBlanks) {
        try {
            TextDelimiterController controller = (TextDelimiterController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.TextDelimiterFxml);
            controller.setParameters(parent, initName, hasBlanks);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
