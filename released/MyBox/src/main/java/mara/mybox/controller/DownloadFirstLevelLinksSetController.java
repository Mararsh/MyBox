package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.Link.FilenameType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-19
 * @License Apache License Version 2.0
 */
public class DownloadFirstLevelLinksSetController extends BaseController {

    protected FilenameType nameType;

    @FXML
    protected TextField pathInput;
    @FXML
    protected ToggleGroup nameGroup;
    @FXML
    protected RadioButton nameRadio, addressRadio, titleRadio;
    @FXML
    protected Label downloadingsLabel, linksLabel;

    public DownloadFirstLevelLinksSetController() {
        baseTitle = Languages.message("DownloadHtmls");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            String type = UserConfig.getString(baseName + "NameType", "name");
            switch (type) {
                case "title":
                    titleRadio.setSelected(true);
                    nameType = FilenameType.ByLinkTitle;
                    break;
                case "address":
                    addressRadio.setSelected(true);
                    nameType = FilenameType.ByLinkAddress;
                    break;
                default:
                    nameRadio.setSelected(true);
                    nameType = FilenameType.ByLinkName;
                    break;
            }
            nameGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
                if (addressRadio.isSelected()) {
                    nameType = FilenameType.ByLinkAddress;
                } else if (titleRadio.isSelected()) {
                    nameType = FilenameType.ByLinkTitle;
                } else {
                    nameType = FilenameType.ByLinkName;
                }
                UserConfig.setString(baseName + "NameType", nameType.name());
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setValues(BaseController parent, String path) {
        parentController = parent;
        pathInput.setText(path == null || path.isBlank() ? DateTools.nowFileString() : path);
    }

    @FXML
    @Override
    public void okAction() {
        if (parentController == null) {
            return;
        }
        if (pathInput.getText() == null || pathInput.getText().isBlank()) {
            return;
        }
        DownloadFirstLevelLinksController controller = (DownloadFirstLevelLinksController) parentController;
        controller.readLinks(pathInput.getText().trim(), nameType);
        this.closeStage();
    }

}
