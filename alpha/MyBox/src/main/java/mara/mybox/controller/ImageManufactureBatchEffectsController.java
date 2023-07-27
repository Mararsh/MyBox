package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageGray;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends BaseImageManufactureBatchController {

    @FXML
    protected ControlImageEffectOptions optionsController;

    public ImageManufactureBatchEffectsController() {
        baseTitle = Languages.message("ImageManufactureBatchEffects");

    }

    @Override
    public void initOptionsSection() {
        try {
            optionsController.setValues(this);
            optionsController.quantizationController.quanDataCheck.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                        pixelsOperation = PixelsOperationFactory.create(AlphaTools.removeAlpha(source), null, optionsController.effectType);
                        pixelsOperation.setIntPara1(optionsController.intPara1);
                        pixelsOperation.setIntPara2(optionsController.intPara2);
                        pixelsOperation.setIntPara3(optionsController.intPara3);
                        target = pixelsOperation.operate();
                        break;
                    case Quantization:
                        ImageQuantization quantization = ImageQuantizationFactory.create(
                                source, null, optionsController.quantizationController, false);
                        target = quantization.operate();
                        break;
                    case Gray:
                        target = ImageGray.byteGray(source);
                        break;
                    case BlackOrWhite:
                        int threshold = optionsController.binaryController.threshold(source);
                        ImageBinary imageBinary = new ImageBinary(source, threshold);
                        imageBinary.setIsDithering(optionsController.binaryController.dither());
                        target = imageBinary.operate();
                        break;
                    case Sepia:
                        pixelsOperation = PixelsOperationFactory.create(source, null, optionsController.effectType);
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
            MyBoxLog.error(e);
            return null;
        }

    }

}
