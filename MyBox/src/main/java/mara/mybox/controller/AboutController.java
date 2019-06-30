package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlControl;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class AboutController extends BaseController {

    @FXML
    private Label version, date;
    @FXML
    private Hyperlink userGuideLink;

    public AboutController() {
        baseTitle = AppVaribles.getMessage("About");
    }

    @Override
    public void initializeNext() {
        version.setText(CommonValues.AppVersion);
        date.setText(CommonValues.AppVersionDate);
        userGuideLink.setText("https://github.com/Mararsh/MyBox/releases/download/v"
                + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                + "-Overview-" + AppVaribles.getLanguage() + ".pdf");

        FxmlControl.miao8();

    }

}
