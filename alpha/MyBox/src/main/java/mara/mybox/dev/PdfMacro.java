package mara.mybox.dev;

import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class PdfMacro extends BaseMacro {

    @Override
    public boolean run() {
        try {
            if (parameters == null) {
                return false;
            }
            inputFile = getInputFile();
            MyBoxLog.console(inputFile);
            String op = getOperation();
            MyBoxLog.console(op);
            if (op == null) {
                op = "edit";
            }
            op = op.toLowerCase();
            switch (op) {
                case "edit":
                    ImageEditorController.openFile(inputFile);
                    return true;
                case "sharp":
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
//                convolution.setImage(inImage).setKernel(kernel)
//                        .setExcludeScope(excludeScope())
//                        .setSkipTransparent(skipTransparent())
//                        .setTask(currentTask);
//                opInfo = message("Intensity") + ": " + sharpenController.intensity;
//                return convolution.startFx();
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

        return false;
    }

}
