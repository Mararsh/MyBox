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

    /*
        parameters
     */
    @Override
    public boolean checkMoreParameters() {
        try {
            File inputFile = getInputFile();
            if (inputFile == null || !inputFile.exists()) {
                error = message("Invalid" + ": inputFile");
                return false;
            }
            String operation = getOperation();
            switch (operation.toLowerCase()) {
                case "edit":
                    ok = checkEdit();
                    break;
                case "replace":
                    ok = checkReplace();
                    break;
                case "sharp":
                    ok = checkSharp();
                    break;
            }
            sourceImage = ImageIO.read(inputFile);
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean checkEdit() {
        try {
            File inputFile = getInputFile();
            writeParameter("outputFile", inputFile);
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean checkReplace() {
        try {
            File inputFile = getInputFile();
            writeParameter("outputFile", inputFile);
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean checkSharp() {
        try {
            String a = readArgument("algorithm");
            short intensity = 2;
            if (a == null || "mask".equalsIgnoreCase(a)) {
                String v = readArgument("intensity");
                if (v != null) {
                    try {
                        intensity = Short.parseShort(v);
                    } catch (Exception e) {
                        error = message("Invalid" + ": intensity");
                        return false;
                    }
                }
                a = "mask";
                writeParameter("intensity", intensity);
            }
            if ("eight".equalsIgnoreCase(a)) {
                a = "eight";
            } else if ("four".equalsIgnoreCase(a)) {
                a = "four";
            } else {
                error = message("Invalid" + ": algorithm ");
                return false;
            }
            writeParameter("algorithm", a);

            String edge = readArgument("edge");
            if (edge == null || "copy".equalsIgnoreCase(edge)) {
                edge = "copy";
            } else if ("zero".equalsIgnoreCase(edge)) {
                edge = "zero";
            } else {
                error = message("Invalid" + ": edge");
                return false;
            }
            writeParameter("edge", edge);

            String color = readArgument("color");
            if (color == null || "keep".equalsIgnoreCase(edge)) {
                color = "keep";
            } else if ("grey".equalsIgnoreCase(color) || "gray".equalsIgnoreCase(color)) {
                color = "grey";
            } else if ("bw".equalsIgnoreCase(color) || "blackwhite".equalsIgnoreCase(color)) {
                color = "blackwhite";
            } else {
                error = message("Invalid" + ": color");
                return false;
            }
            writeParameter("color", color);

            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    /*
        run
     */
    @Override
    public boolean run() {
        try {
            ok = false;
            resultImage = null;
            String operation = getOperation();
            switch (operation.toLowerCase()) {
                case "edit":
                    ok = true;
                    break;
                case "replace":
                    ok = runReplace();
                    break;
                case "sharp":
                    ok = runSharp();
                    break;
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
        return ok;
    }

    public boolean runReplace() {
        try {
            String a = readArgument("algorithm");

//            List<java.awt.Color> colors = new ArrayList();
//            colors.add(originalColor);
//            ImageScope scope = new ImageScope()
//                    .setShapeType(ImageScope.ShapeType.Whole)
//                    .setColors(colors)
//                    .setColorExcluded(excludeCheck.isSelected());
//
//            PixelsOperation pixelsOperation = PixelsOperationFactory.create(null, scope,
//                    PixelsOperation.OperationType.ReplaceColor,
//                    PixelsOperation.ColorActionType.Set)
//                    .setColorPara1(originalColor)
//                    .setColorPara2(newColorSetController.awtColor())
//                    .setSkipTransparent(originalColor.getRGB() != 0 && !handleTransparentCheck.isSelected())
//                    .setBoolPara1(hueCheck.isSelected())
//                    .setBoolPara2(saturationCheck.isSelected())
//                    .setBoolPara3(brightnessCheck.isSelected());
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean runSharp() {
        try {
            String a = readArgument("algorithm");
            ConvolutionKernel kernel;
            if ("eight".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
            } else if ("four".equalsIgnoreCase(a)) {
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
            } else {
                short intensity = readShortArgument("intensity");
                if (intensity == AppValues.InvalidShort) {
                    intensity = 2;
                }
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
            }
            if ("zero".equalsIgnoreCase(readArgument("edge"))) {
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
            } else {
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
            }
            String color = readArgument("color");
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
            File outputFile = getOutputFile();
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
            File outputFile = getOutputFile();
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
