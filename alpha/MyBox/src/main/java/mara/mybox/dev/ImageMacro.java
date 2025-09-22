package mara.mybox.dev;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

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
            command = "function=image";
            inputFile = getInputFile();
            if (inputFile != null && inputFile.exists()) {
                sourceImage = ImageIO.read(inputFile);
            }
            if (sourceImage == null) {
                inputFile = FxFileTools.getInternalFile("/img/Mybox.png", "image", "Mybox.png");
                sourceImage = ImageIO.read(inputFile);
            }
            if (inputFile != null) {
                command += " inputFile=\"" + inputFile.getAbsolutePath() + "\"";
            }
            outputFile = getOutputFile();
            if (outputFile != null) {
                command += " outputFile=\"" + outputFile.getAbsolutePath() + "\"";
            }
            String op = getOperation();
            if (op == null) {
                op = "edit";
            }
            op = op.toLowerCase();
            switch (op) {
                case "edit":
                    outputFile = inputFile;
                    command += " operation=edit";
                    displayInfo(message("Parameters") + ": " + command);
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
            String a = get("algorithm");
            command += " operation=sharp algorithm=\"";
            ConvolutionKernel kernel;
            if ("eight".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                command += "eight\"";
            } else if ("four".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                command += "four\"";
            } else {
                short intensity = getShort("intensity");
                if (intensity == AppValues.InvalidShort) {
                    intensity = 2;
                }
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
                command += "mask\" intensity=" + intensity;
            }
            if ("zero".equalsIgnoreCase(get("edge"))) {
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
                command += " edge=zero";
            } else {
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
                command += " edge=copy";
            }
            String color = get("color");
            if ("grey".equalsIgnoreCase(color) || "gray".equalsIgnoreCase(color)) {
                kernel.setColor(ConvolutionKernel.Color.Grey);
                command += " color=grey";
            } else if ("bw".equalsIgnoreCase(color) || "blackwhite".equalsIgnoreCase(color)) {
                kernel.setColor(ConvolutionKernel.Color.BlackWhite);
                command += " color=blackwhite";
            } else {
                kernel.setColor(ConvolutionKernel.Color.Keep);
                command += " color=keep";
            }
            displayInfo(message("Parameters") + ": " + command);
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(sourceImage).setKernel(kernel).setTask(task);
            resultImage = convolution.start();
            if (outputFile != null) {
                ImageFileWriters.writeImageFile(task, resultImage, outputFile);
            }
            return resultImage != null;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    @Override
    public void displayResult() {
        try {
            displayEnd();;
            if (outputFile != null && outputFile.exists()) {
                ImageEditorController.openFile(outputFile);
            } else if (resultImage != null) {
                displayInfo(message("ImageGenerated"));
                ImageEditorController.openImage(SwingFXUtils.toFXImage(resultImage, null));
            }

        } catch (Exception e) {
            displayError(e.toString());
        }
    }
}
