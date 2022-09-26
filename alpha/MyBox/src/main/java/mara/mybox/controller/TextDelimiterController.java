package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class TextDelimiterController extends BaseController {

    protected String delimiterName;
    protected SimpleBooleanProperty okNotify;

    @FXML
    protected ControlTextDelimiter delimiterController;

    public TextDelimiterController() {
        okNotify = new SimpleBooleanProperty();
    }

    public void setParameters(BaseController parent, String initName, boolean hasBlanks) {
        try {
            parentController = parent;
            baseName = parent.baseName;
            delimiterName = initName;

            delimiterController.setControls(baseName, hasBlanks);
            delimiterController.setDelimiterName(initName);
            delimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    delimiterName = delimiterController.delimiterName;
                }
            });

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

    @Override
    public void cleanPane() {
        try {
            okNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static TextDelimiterController open(BaseController parent, String initName, boolean hasBlanks) {
        try {
            TextDelimiterController controller = (TextDelimiterController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.TextDelimiterFxml, false);
            controller.setParameters(parent, initName, hasBlanks);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
