package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.image.ImageScope;
import mara.mybox.data.ConvolutionKernel;

/**
 * @Author Mara
 * @CreateDate 2019-2-15 16:54:15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConvolution extends mara.mybox.image.ImageConvolution {

    public ImageConvolution() {
        this.operationType = OperationType.Convolution;
    }

    public ImageConvolution(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
    }

    public ImageConvolution(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = scope;
    }

    public ImageConvolution(Image image, ConvolutionKernel kernel) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = null;
        init(kernel);
    }

    public ImageConvolution(Image image, ImageScope scope, ConvolutionKernel kernel) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = scope;
        init(kernel);
    }

    private void init(ConvolutionKernel kernel) {
        setKernel(kernel);
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

}
