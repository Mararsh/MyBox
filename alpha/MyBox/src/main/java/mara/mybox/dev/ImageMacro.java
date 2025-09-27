package mara.mybox.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
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
    public String defaultOperation() {
        return "edit";
    }

    @Override
    public File defaultInputFile() {
        return FxFileTools.getInternalFile("/img/Mybox.png", "image", "Mybox.png");
    }

    @Override
    public File defaultOutputFile() {
        return FileTmpTools.tmpFile("macro", "jpg");
    }

    @Override
    public boolean run() {
        try {
            ok = false;
            resultImage = null;
            if (inputFile != null && inputFile.exists()) {
                sourceImage = ImageIO.read(inputFile);
            }
            if (sourceImage == null) {
                inputFile = defaultInputFile();
                sourceImage = ImageIO.read(inputFile);
            }
            switch (operation.toLowerCase()) {
                case "edit":
                    outputFile = inputFile;
                    command += commandIO();
                    displayInfo(message("Parameters") + ": " + command);
                    ok = true;
                    break;
                case "replace":
                    ok = sharp();
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
            String a = read("algorithm");
            command += " algorithm=\"";
            ConvolutionKernel kernel;
            if ("eight".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                command += "eight\"";
            } else if ("four".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                command += "four\"";
            } else {
                short intensity = readShort("intensity");
                if (intensity == AppValues.InvalidShort) {
                    intensity = 2;
                }
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
                command += "mask\" intensity=" + intensity;
            }
            if ("zero".equalsIgnoreCase(read("edge"))) {
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
                command += " edge=zero";
            } else {
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
                command += " edge=copy";
            }
            String color = read("color");
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
            command += commandIO();
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
    public void openResult() {
        try {
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
