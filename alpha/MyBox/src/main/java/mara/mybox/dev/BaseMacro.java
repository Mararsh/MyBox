package mara.mybox.dev;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class BaseMacro {

    public static final String ParameterPrefix = "MacroPara_";

    protected Map<String, String> parameters;

    public BaseMacro() {
        init();
    }

    public final void init() {
        parameters = null;
    }

    public void put(String key, String value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key.toLowerCase(), value);
    }

    public String get(String key) {
        if (parameters == null) {
            return null;
        }
        return parameters.get(key);
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(parameters.get(key));
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public short getShort(String key) {
        try {
            return Short.parseShort(parameters.get(key));
        } catch (Exception e) {
            return AppValues.InvalidShort;
        }
    }

    public String getFunction() {
        try {
            return parameters.get(ParameterPrefix + "1");
        } catch (Exception e) {
            return null;
        }
    }

    public String getOperation() {
        try {
            return parameters.get(ParameterPrefix + "2");
        } catch (Exception e) {
            return null;
        }
    }

    public File getFile() {
        try {
            return new File(parameters.get("file"));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean run() {
        info();
        String func = getFunction();
        if (func == null) {
            return false;
        }
        func = func.toLowerCase();
        switch (func) {
            case "image":
                return handlImage();
        }
        return false;
    }

    public boolean handlImage() {
        if (parameters == null) {
            return false;
        }
        File file = getFile();
        if (file == null) {
            return false;
        }
        String op = getOperation();
        if (op == null) {
            op = "edit";
        }
        op = op.toLowerCase();
        switch (op) {
            case "edit":
                ImageEditorController.openFile(file);
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
        return false;
    }

    public void info() {
        if (parameters == null) {
            return;
        }
        MyBoxLog.console(parameters);
    }

    /*
        static
     */
    public static BaseMacro create() {
        return new BaseMacro();
    }

    public static BaseMacro parse(String args) {
        if (args == null) {
            return null;
        }
        return parse(args.split(" "));
    }

    public static BaseMacro parse(String[] args) {
        if (args == null) {
            return null;
        }
        BaseMacro macro = create();
        int index = 0, pos;
        for (String arg : args) {
            pos = arg.indexOf("=");
            if (pos > 0) {
                macro.put(arg.substring(0, pos), arg.substring(pos + 1, arg.length()));
            } else {
                macro.put(ParameterPrefix + ++index, arg);
            }
        }
        return macro;
    }

    /*
        get/set
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public BaseMacro setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

}
