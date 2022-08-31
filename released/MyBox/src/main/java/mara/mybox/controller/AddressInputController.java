package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-24
 * @License Apache License Version 2.0
 */
public class AddressInputController extends BaseController {

    protected String name, address;
    protected SimpleBooleanProperty notify;

    @FXML
    protected TextField nameInput, addressInput;

    public AddressInputController() {
        baseTitle = message("Address");
    }

    public void setParameters(BaseController parent) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName;
            }
            getMyStage().centerOnScreen();

            notify = new SimpleBooleanProperty();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    @FXML
    @Override
    public void okAction() {
        name = nameInput.getText();
        address = addressInput.getText();
        if (address == null || address.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("Address"));
            return;
        }
        notify.set(!notify.get());
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public void cleanPane() {
        try {
            notify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
