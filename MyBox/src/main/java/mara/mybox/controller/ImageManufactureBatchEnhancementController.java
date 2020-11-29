package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageConvolution;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEnhancementController extends ImageManufactureBatchController {

    @FXML
    protected ImageManufactureEnhancementOptionsController optionsController;

    public ImageManufactureBatchEnhancementController() {
        baseTitle = AppVariables.message("ImageManufactureBatchEnhancement");

    }

    @Override
    public void initOptionsSection() {
        try {
            optionsController.setValues(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }

        switch (optionsController.enhanceType) {
            case Contrast:
                return true;
            case Convolution:
                if (optionsController.kernel == null) {
                    int index = optionsController.stringSelector.getSelectionModel().getSelectedIndex();
                    if (optionsController.kernels == null || optionsController.kernels.isEmpty() || index < 0) {
                        return false;
                    }
                    optionsController.kernel = optionsController.kernels.get(index);
                }
                return true;
            case Smooth:
                switch (optionsController.smoothAlgorithm) {
                    case AverageBlur:
                        optionsController.kernel = ConvolutionKernel.makeAverageBlur(optionsController.intPara1);
                        return true;
                    case GaussianBlur:
                        optionsController.kernel = ConvolutionKernel.makeGaussBlur(optionsController.intPara1);
                        return true;
                    case MotionBlur:
                        optionsController.kernel = ConvolutionKernel.makeMotionBlur(optionsController.intPara1);
                        break;
                    default:
                        return false;
                }
            case Sharpen:
                switch (optionsController.sharpenAlgorithm) {
                    case EightNeighborLaplace:
                        optionsController.kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                        return true;
                    case FourNeighborLaplace:
                        optionsController.kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                        return true;
                    case UnsharpMasking:
                        optionsController.kernel = ConvolutionKernel.makeUnsharpMasking(optionsController.intPara1);
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }

    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            ImageConvolution imageConvolution;
            if (null != optionsController.enhanceType) {
                switch (optionsController.enhanceType) {
                    case Contrast:
                        ImageContrast imageContrast = new ImageContrast(source, optionsController.contrastAlgorithm);
                        imageContrast.setIntPara1(optionsController.intPara1);
                        imageContrast.setIntPara2(optionsController.intPara2);
                        target = imageContrast.operate();
                        break;
                    case Convolution:
                    case Smooth:
                    case Sharpen:
                        if (optionsController.kernel == null) {
                            return null;
                        }
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(optionsController.kernel);
                        target = imageConvolution.operate();
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
