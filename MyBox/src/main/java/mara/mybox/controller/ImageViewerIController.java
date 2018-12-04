package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerIController extends ImageViewerController {

    protected boolean isRefer;

    @FXML
    private HBox buttonsBox;

    @Override
    protected void initializeNext2() {
        try {
            setTips();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    void imageClicked(MouseEvent event) {
        if (!isRefer && event.getClickCount() > 1) {
            openImageManufactureInNew(sourceFile.getAbsolutePath());
//            try {
//                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageManufactureFileFxml), AppVaribles.CurrentBundle);
//                Pane pane = fxmlLoader.load();
//                ImageManufactureController controller = fxmlLoader.getController();
//                controller.loadImage(sourceFile.getAbsolutePath());
//
//                controller.setMyStage(getMyStage());
//                myStage.setScene(new Scene(pane));
//            } catch (Exception e) {
//                logger.error(e.toString());
//            }
        }
    }

    @FXML
    void imageEntered(MouseEvent event) {
        String str = AppVaribles.getMessage("Format") + ":" + imageInformation.getImageFormat() + "  "
                + AppVaribles.getMessage("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  "
                + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified());
        parentController.bottomLabel.setText(str);
    }

    public void removeButtons() {
        thisPane.getChildren().remove(buttonsBox);
    }

    public boolean isIsRefer() {
        return isRefer;
    }

    public void setIsRefer(boolean isRefer) {
        this.isRefer = isRefer;
    }

}
