package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    @FXML
    private Pane titleBar;
    @FXML
    private TitleBarController titleBarController;
    @FXML
    private Pane commonBar;
    @FXML
    private CommonBarController commonBarController;

    @Override
    protected void initStage2() {
        commonBarController.setParentFxml(thisFxml);
        titleBarController.setParentFxml(thisFxml);
    }

}
