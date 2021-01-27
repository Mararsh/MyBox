package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageGray;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageMosaic;
import mara.mybox.image.ImageQuantization;
import mara.mybox.image.PixelsOperation;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

    @FXML
    protected ImageManufactureEffectsOptionsController optionsController;

    public ImageManufactureBatchEffectsController() {
        baseTitle = AppVariables.message("ImageManufactureBatchEffects");

    }

    @Override
    public void initOptionsSection() {
        try {
            optionsController.setValues(this);
            optionsController.quanDataCheck.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            PixelsOperation pixelsOperation;
            ImageConvolution imageConvolution;
            if (null != optionsController.effectType) {
                switch (optionsController.effectType) {
                    case EdgeDetect:
                        if (optionsController.eightLaplaceRadio.isSelected()) {
                            optionsController.kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                        } else if (optionsController.eightLaplaceExcludedRadio.isSelected()) {
                            optionsController.kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert();
                        } else if (optionsController.fourLaplaceRadio.isSelected()) {
                            optionsController.kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace();
                        } else if (optionsController.fourLaplaceExcludedRadio.isSelected()) {
                            optionsController.kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert();
                        } else {
                            return null;
                        }
                        optionsController.kernel.setGray(optionsController.valueCheck.isSelected());
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(optionsController.kernel);
                        target = imageConvolution.operate();
                        break;
                    case Emboss:
                        optionsController.kernel = ConvolutionKernel.makeEmbossKernel(
                                optionsController.intPara1, optionsController.intPara2, optionsController.valueCheck.isSelected());
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(optionsController.kernel);
                        target = imageConvolution.operate();
                        break;
                    case Thresholding:
                        pixelsOperation = PixelsOperation.create(ImageManufacture.removeAlpha(source), null, optionsController.effectType);
                        pixelsOperation.setIntPara1(optionsController.intPara1);
                        pixelsOperation.setIntPara2(optionsController.intPara2);
                        pixelsOperation.setIntPara3(optionsController.intPara3);
                        target = pixelsOperation.operate();
                        break;
                    case Quantization:
                        ImageQuantization quantization = ImageQuantization.create(
                                ImageManufacture.removeAlpha(source),
                                null, optionsController.quantizationAlgorithm,
                                optionsController.quanColors, optionsController.regionSize,
                                optionsController.weight1, optionsController.weight2, optionsController.weight3,
                                optionsController.quanDataCheck.isSelected(), optionsController.quanDitherCheck.isSelected(),
                                 optionsController.ceilCheck.isSelected());
                        target = quantization.operate();
                        break;
                    case Gray:
                        target = ImageGray.byteGray(source);
                        break;
                    case BlackOrWhite:
                        ImageBinary imageBinary;
                        switch (optionsController.intPara1) {
                            case 2:
                                imageBinary = new ImageBinary(source, -1);
                                break;
                            case 3:
                                imageBinary = new ImageBinary(source, optionsController.intPara2);
                                break;
                            default:
                                int t = ImageBinary.calculateThreshold(source);
                                imageBinary = new ImageBinary(source, t);
                                break;
                        }
                        imageBinary.setIsDithering(optionsController.valueCheck.isSelected());
                        target = imageBinary.operate();
                        break;
                    case Sepia:
                        pixelsOperation = PixelsOperation.create(source, null, optionsController.effectType);
                        pixelsOperation.setIntPara1(optionsController.intPara1);
                        target = pixelsOperation.operate();
                        break;
                    case Mosaic: {
                        ImageMosaic mosaic = ImageMosaic.create(source, null,
                                ImageMosaic.MosaicType.Mosaic, optionsController.intPara1);
                        target = mosaic.operate();
                    }
                    break;
                    case FrostedGlass: {
                        ImageMosaic mosaic
                                = ImageMosaic.create(source, null,
                                        ImageMosaic.MosaicType.FrostedGlass, optionsController.intPara1);
                        target = mosaic.operate();
                    }
                    break;
                    default:
                        break;
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
