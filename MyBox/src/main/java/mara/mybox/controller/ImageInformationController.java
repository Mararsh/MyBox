/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformationController extends BaseController {

    @FXML
    private TextField FilesPath;
    @FXML
    private TextField FileName;
    @FXML
    private TextField FileSize;
    @FXML
    private TextField CreateTime;
    @FXML
    private TextField ModifyTime;
    @FXML
    private TextField ImageFormat;
    @FXML
    private TextField xPixels;
    @FXML
    private TextField yPixels;
    @FXML
    private TextField xDensity;
    @FXML
    private TextField yDensity;
    @FXML
    private TextField xSize;
    @FXML
    private TextField ySize;
    @FXML
    private TextField ColorSpace;
    @FXML
    private TextField ColorChannels;
    @FXML
    private TextField BitDepth;
    @FXML
    private TextField AlphaChannel;
    @FXML
    private TextField compressionType;
    @FXML
    private TextField lossless;
    @FXML
    private TextField ImageOrientation, NumberOfImages, IndexOfImage;

    @FXML
    private void closeStage(MouseEvent event) {
        try {
            closeStage();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadInformation(ImageInformation info) {
        try {
            ImageFileInformation finfo = info.getImageFileInformation();
            File file = finfo.getFile();
            FilesPath.setText(file.getParent());
            FileName.setText(file.getName());
            CreateTime.setText(finfo.getCreateTime());
            ModifyTime.setText(finfo.getModifyTime());
            FileSize.setText(finfo.getFileSize());
            xPixels.setText(info.getWidth() + "");
            yPixels.setText(info.getHeight() + "");
            if (info.getwDensity() > 0) {
                xDensity.setText(info.getwDensity() + " dpi");
                float xinch = info.getWidth() / info.getwDensity();
                xSize.setText(xinch + " " + AppVaribles.getMessage("inches")
                        + " = " + (xinch * 2.54) + " " + AppVaribles.getMessage("centimetres"));
            }
            if (info.gethDensity() > 0) {
                yDensity.setText(info.gethDensity() + " dpi");
                float yinch = info.getHeight() / info.gethDensity();
                ySize.setText(yinch + " " + AppVaribles.getMessage("inches")
                        + " = " + (yinch * 2.54) + " " + AppVaribles.getMessage("centimetres"));
            }
            ColorSpace.setText(info.getColorSpace());
            ColorChannels.setText(info.getColorChannels() + "");
            BitDepth.setText(info.getBitDepth() + "");
            AlphaChannel.setText(AppVaribles.getMessage(info.isHasAlpha() + ""));
            compressionType.setText(info.getCompressionType());
            lossless.setText(AppVaribles.getMessage(info.isIsLossless() + ""));
            ImageOrientation.setText(info.getImageRotation());
            ImageFormat.setText(info.getImageFormat() + " " + info.getExtraFormat());
            NumberOfImages.setText(info.getImageFileInformation().getNumberOfImages() + "");
            IndexOfImage.setText(info.getIndex() + "");

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
