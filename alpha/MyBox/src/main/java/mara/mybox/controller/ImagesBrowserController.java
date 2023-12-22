package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @License Apache License Version 2.0
 *
 * ImagesBrowserController < ImagesBrowserController_Pane <
 * ImagesBrowserController_Menu < ImagesBrowserController_Action <
 * ImagesBrowserController_Load < BaseImageController
 */
public class ImagesBrowserController extends ImagesBrowserController_Pane {

    public ImagesBrowserController() {
        baseTitle = message("ImagesBrowser");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            displayMode = DisplayMode.ThumbnailsList;
            thumbWidth = UserConfig.getInt(baseName + "ThumbnailWidth", 100);
            if (thumbWidth <= 0) {
                thumbWidth = 100;
            }

            opPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            mainAreaBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            buttonsPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            rightPane.disableProperty().bind(Bindings.isEmpty(imageFileList));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(selectFileButton, new Tooltip(message("SelectMultipleFilesBrowse")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        makeImagesPane();
    }

    @Override
    public ImagesBrowserController refreshInterfaceAndFile() {
        super.refreshInterface();
        makeImagesPane();
        return this;
    }

    /*
        static
     */
    public static ImagesBrowserController open() {
        try {
            ImagesBrowserController controller = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
