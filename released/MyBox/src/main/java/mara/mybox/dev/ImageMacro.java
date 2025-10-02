package mara.mybox.dev;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import mara.mybox.color.ColorMatch;
import mara.mybox.color.ColorMatch.MatchAlgorithm;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperationFactory;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class ImageMacro extends BaseMacro {

    protected BufferedImage sourceImage;
    protected BufferedImage resultImage;
    protected PixelsOperation pixelsOperation;
    protected ImageConvolution convolution;
    protected ConvolutionKernel kernel;

    @Override
    public void reset() {
        super.reset();
        sourceImage = null;
        resultImage = null;
        pixelsOperation = null;
    }

    @Override
    public String defaultOperation() {
        return "edit";
    }

    @Override
    public File defaultInputFile() {
        return FxFileTools.getInternalFile("/img/MyBox.png", "image", "MyBox.png");
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
                displayError(message("Invalid" + ": inputFile") + (inputFile != null ? ("\n" + inputFile.getAbsolutePath()) : ""));
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
            if (!ok) {
                return false;
            }
            sourceImage = ImageIO.read(inputFile);
            return sourceImage != null;
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
            ImageScope scope = new ImageScope();
            ColorMatch colorMatch = scope.getColorMatch();

            pixelsOperation = PixelsOperationFactory.create(sourceImage, scope,
                    PixelsOperation.OperationType.ReplaceColor,
                    PixelsOperation.ColorActionType.Set);

            Color color;
            try {
                color = FxColorTools.cssToAwt(readArgument("color"));
                writeParameter("color", color);

                List<java.awt.Color> colors = new ArrayList();
                colors.add(color);
                scope.setColors(colors);

                pixelsOperation.setColorPara1(color);
            } catch (Exception e) {
                displayError(message("Invalid" + ": color"));
                return false;
            }

            try {
                Color newColor = FxColorTools.cssToAwt(readArgument("newColor"));
                writeParameter("newColor", newColor);

                pixelsOperation.setColorPara2(newColor);
            } catch (Exception e) {
                displayError(message("Invalid" + ": newColor"));
                return false;
            }

            boolean invert = false, trans = false;
            String invertv = readArgument("invert");
            if (invertv != null) {
                invert = StringTools.isTrue(invertv);
            }
            writeParameter("invert", invert);
            scope.setColorExcluded(invert);

            String transv = readArgument("trans");
            if (transv != null) {
                trans = StringTools.isTrue(transv);
            }
            writeParameter("trans", trans);
            pixelsOperation.setSkipTransparent(color.getRGB() != 0 && !trans);

            MatchAlgorithm a = null;
            String aname = readArgument("algorithm");
            if (aname == null) {
                a = ColorMatch.DefaultAlgorithm;
            } else {
                for (MatchAlgorithm ma : MatchAlgorithm.values()) {
                    if (Languages.matchIgnoreCase(ma.name(), aname)) {
                        a = ma;
                        break;
                    }
                }
            }
            if (a == null) {
                displayError(message("Invalid" + ": algorithm"));
                return false;
            }
            writeParameter("algorithm", a);
            colorMatch.setAlgorithm(a);

            double threshold = ColorMatch.suggestedThreshold(a);
            try {
                String v = readArgument("threshold");
                if (v != null) {
                    threshold = Double.parseDouble(v);
                }
            } catch (Exception e) {
                displayError(message("Invalid" + ": threshold"));
                return false;
            }
            writeParameter("threshold", threshold);
            colorMatch.setThreshold(threshold);

            if (ColorMatch.supportWeights(a)) {
                double hueWeight = 1d, brightnessWeight = 1d, saturationWeight = 1d;
                try {
                    String v = readArgument("hueWeight");
                    if (v != null) {
                        hueWeight = Double.parseDouble(v);
                    }
                } catch (Exception e) {
                    displayError(message("Invalid" + ": hueWeight"));
                    return false;
                }
                writeParameter("hueWeight", hueWeight);
                colorMatch.setHueWeight(hueWeight);

                try {
                    String v = readArgument("brightnessWeight");
                    if (v != null) {
                        brightnessWeight = Double.parseDouble(v);
                    }
                } catch (Exception e) {
                    displayError(message("Invalid" + ": brightnessWeight"));
                    return false;
                }
                writeParameter("brightnessWeight", brightnessWeight);
                colorMatch.setBrightnessWeight(brightnessWeight);

                try {
                    String v = readArgument("saturationWeight");
                    if (v != null) {
                        saturationWeight = Double.parseDouble(v);
                    }
                } catch (Exception e) {
                    displayError(message("Invalid" + ": saturationWeight"));
                    return false;
                }
                writeParameter("saturationWeight", saturationWeight);
                colorMatch.setSaturationWeight(saturationWeight);
            }

            boolean hue = true, brightness = false, saturation = false;
            String huev = readArgument("hue");
            if (huev != null) {
                hue = StringTools.isTrue(huev);
            }
            writeParameter("hue", hue);

            String brightnessv = readArgument("brightness");
            if (brightnessv != null) {
                brightness = StringTools.isTrue(brightnessv);
            }
            writeParameter("brightness", brightness);

            String saturationv = readArgument("saturation");
            if (saturationv != null) {
                saturation = StringTools.isTrue(saturationv);
            }
            writeParameter("saturation", saturation);

            pixelsOperation.setBoolPara1(hue)
                    .setBoolPara2(brightness)
                    .setBoolPara3(saturation);

            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean checkSharp() {
        try {

            String a = readArgument("algorithm");
            if (a == null || "mask".equalsIgnoreCase(a)) {
                short intensity = 2;
                String v = readArgument("intensity");
                if (v != null) {
                    try {
                        intensity = Short.parseShort(v);
                    } catch (Exception e) {
                        displayError(message("Invalid" + ": intensity"));
                        return false;
                    }
                }
                a = "mask";
                writeParameter("intensity", intensity);
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
            } else if ("eight".equalsIgnoreCase(a)) {
                a = "eight";
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
            } else if ("four".equalsIgnoreCase(a)) {
                a = "four";
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
            } else {
                displayError(message("Invalid" + ": algorithm"));
                return false;
            }
            writeParameter("algorithm", a);

            String edge = readArgument("edge");
            if (edge == null || "copy".equalsIgnoreCase(edge)) {
                edge = "copy";
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
            } else if ("zero".equalsIgnoreCase(edge)) {
                edge = "zero";
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
            } else {
                displayError(message("Invalid" + ": edge"));
                return false;
            }
            writeParameter("edge", edge);

            String color = readArgument("color");
            if (color == null || "keep".equalsIgnoreCase(color)) {
                color = "keep";
                kernel.setColor(ConvolutionKernel.Color.Keep);
            } else if ("grey".equalsIgnoreCase(color) || "gray".equalsIgnoreCase(color)) {
                color = "grey";
                kernel.setColor(ConvolutionKernel.Color.Grey);
            } else if ("bw".equalsIgnoreCase(color) || "blackwhite".equalsIgnoreCase(color)) {
                color = "blackwhite";
                kernel.setColor(ConvolutionKernel.Color.BlackWhite);
            } else {
                displayError(message("Invalid" + ": color=" + color));
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
            File outputFile = getOutputFile();
            if (resultImage != null && outputFile != null) {
                ImageFileWriters.writeImageFile(task, resultImage, outputFile);
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
        return ok;
    }

    public boolean runReplace() {
        try {
            pixelsOperation.setImage(sourceImage).setTask(task);
            resultImage = pixelsOperation.start();
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean runSharp() {
        try {
            convolution = ImageConvolution.create();
            convolution.setImage(sourceImage).setKernel(kernel).setTask(task);
            resultImage = convolution.start();
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
