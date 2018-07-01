/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.FxmlTools.ImageManufactureType;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    @Override
    protected void initializeNext2() {
        try {

            fileExtensionFilter = new ArrayList() {
                {
                    add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg",
                            "*.tif", "*.tiff", "*.gif"));
                    add(new FileChooser.ExtensionFilter("png", "*.png"));
                    add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
//                    add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
                    add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
                    add(new FileChooser.ExtensionFilter("gif", "*.gif"));
//                    add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
//                    add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
//                    add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
                }
            };

            toolBar.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );
            setTips();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void recovery() {
        imageView.setImage(image);
    }

    @FXML
    public void setSaturate() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Saturate);
    }

    @FXML
    public void setDesaturate() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Desaturate);
    }

    @FXML
    public void setInvert() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Invert);
    }

    @FXML
    public void setGray() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Gray);
    }

    @FXML
    public void setBrighter() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Brighter);
    }

    @FXML
    public void setDarker() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Darker);
    }

    @FXML
    public void save() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(AppVaribles.getMessage("AppTitle"));
//        alert.setHeaderText("Look, a Confirmation Dialog");
        alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        try {
            String format = FileTools.getFileSuffix(sourceFile.getName());
            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), format, sourceFile);
            image = imageView.getImage();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void saveAs() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("imageSourcePath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showSaveDialog(getMyStage());
            AppVaribles.setConfigValue("imageSourcePath", file.getParent());
            String format = FileTools.getFileSuffix(file.getName());
            ImageFileWriters.writeImageFile(SwingFXUtils.fromFXImage(imageView.getImage(), null), format, file.getAbsolutePath());
//            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), format, file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
