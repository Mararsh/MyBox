package mara.mybox.dev;

import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class ImageMacro extends BaseMacro {

    protected BufferedImage sourceImage;
    protected BufferedImage resultImage;

    @Override
    public void reset() {
        super.reset();
        sourceImage = null;
        resultImage = null;
    }

    @Override
    public boolean valid() {
        return parameters != null;
    }

    @Override
    public boolean run() {
        try {
            ok = false;
            resultImage = null;
            if (sourceImage == null) {
                file = getFile();
                if (file != null && file.exists()) {
                    sourceImage = ImageIO.read(file);
                }
            }
            if (sourceImage == null) {
                file = FxFileTools.getInternalFile("/img/Mybox.png", "image", "Mybox.png");
                sourceImage = ImageIO.read(file);
            }
            String op = getOperation();
            if (op == null) {
                op = "edit";
            }
            op = op.toLowerCase();
            switch (op) {
                case "edit":
                    Platform.runLater(() -> {
                        ImageEditorController.openFile(file);
                    });
                    ok = true;
                    break;
                case "sharp":
                    ok = sharp();
                    break;
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
        return ok;
    }

    public boolean sharp() {
        try {
            short intensity = getShort("intensity");
            if (intensity == AppValues.InvalidShort) {
                intensity = 2;
            }
            String a = get("algorithm");
            ConvolutionKernel kernel;
            if ("eight".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
            } else if ("four".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
            } else {
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
            }
            if ("zero".equalsIgnoreCase(get("edge"))) {
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
            } else {
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
            }
            String color = get("color");
            if ("grey".equalsIgnoreCase(color) || "gray".equalsIgnoreCase(color)) {
                kernel.setColor(ConvolutionKernel.Color.Grey);
            } else if ("bw".equalsIgnoreCase(color) || "blackwhite".equalsIgnoreCase(color)) {
                kernel.setColor(ConvolutionKernel.Color.BlackWhite);
            } else {
                kernel.setColor(ConvolutionKernel.Color.Keep);
            }
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(sourceImage).setKernel(kernel).setTask(task);
            resultImage = convolution.start();
            return resultImage != null;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        try {
            if (resultImage != null) {
                ImageEditorController.openImage(SwingFXUtils.toFXImage(resultImage, null));
            }

        } catch (Exception e) {
            displayError(e.toString());
        }
    }
}
