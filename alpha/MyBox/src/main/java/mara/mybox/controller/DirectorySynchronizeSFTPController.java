package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class DirectorySynchronizeSFTPController extends DirectorySynchronizeController {

    @FXML
    protected TextField addressInput, userInput, passwordInput, pathInput;

    public DirectorySynchronizeSFTPController() {
        baseTitle = message("DirectorySynchronizeSFTP");
    }

    @Override
    public void initTarget() {
        try {

            operationBarController.openTargetButton.setVisible(false);
            operationBarController.openCheck.setVisible(false);

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(addressInput.textProperty().isEmpty())
                            .or(pathInput.textProperty().isEmpty())
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected boolean checkTarget() {
//        targetPath = targetPathInputController.file();
//        if (targetPath == null) {
//            popError(message("Invlid") + ": " + message("TargetPath"));
//            return false;
//        }
//        if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
//            popError(message("TargetPathShouldNotSourceSub"));
//            return false;
//        }
        return true;
    }

    @FXML
    @Override
    public void saveAction() {

    }

    @FXML
    public void showHistories(Event event) {

    }

    @FXML
    public void popHistories(Event event) {

    }

}
