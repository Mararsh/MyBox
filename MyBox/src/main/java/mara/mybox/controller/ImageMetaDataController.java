/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    private TextField fileInput;
    @FXML
    private TextArea metaDataInput;

    public ImageMetaDataController() {
        baseTitle = AppVariables.message("ImageMetaData");

    }

    public void loadImageFileMeta(ImageInformation info) {
        try {
            if (info == null || info.getFileName() == null) {
                return;
            }
            fileInput.setText(info.getFileName());
            ImageFileInformation finfo = info.getImageFileInformation();
            metaDataInput.setText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            metaDataInput.appendText("<ImageMetadata file=\"" + finfo.getFileName() + "\"  numberOfImages=\""
                    + finfo.getNumberOfImages() + "\">\n");
            int index = 1;
            for (ImageInformation imageInfo : finfo.getImagesInformation()) {
                metaDataInput.appendText("    <Image index=\"" + index + "\">\n");
                metaDataInput.appendText(imageInfo.getMetaDataXml());
                metaDataInput.appendText("    </Image>\n");
                index++;
            }
            metaDataInput.appendText("</ImageMetadata>\n");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            myStage.toFront();
                            metaDataInput.home();
                            metaDataInput.requestFocus();
                        }
                    });
                }
            }, 1000);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
