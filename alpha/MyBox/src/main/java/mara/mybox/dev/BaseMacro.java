package mara.mybox.dev;

import java.io.File;
import java.util.LinkedHashMap;
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

    protected String script;
    protected LinkedHashMap<String, String> parameters;

    public BaseMacro() {
        init();
    }

    public BaseMacro(String inScript) {
        parseString(inScript);
    }

    public final void init() {
        parameters = null;
    }

    public boolean parseArray(String[] args) {
        if (args == null) {
            return false;
        }
        int index = 0, pos;
        for (String arg : args) {
            pos = arg.indexOf("=");
            if (pos > 0) {
                put(arg.substring(0, pos), arg.substring(pos + 1, arg.length()));
            } else {
                put(ParameterPrefix + ++index, arg);
            }
        }
        return true;
    }

    // helped with deepseek
    public final boolean parseString(String inScript) {
        try {
            script = inScript;
            parameters = null;
            if (script == null || script.isBlank()) {
                return false;
            }

            boolean inDoubleQuotes = false;
            boolean inSingleQuotes = false;
            StringBuilder currentToken = new StringBuilder();

            for (int i = 0; i < script.length(); i++) {
                char c = script.charAt(i);

                if (c == '"' && !inSingleQuotes) {
                    inDoubleQuotes = !inDoubleQuotes;
                    currentToken.append(c);
                    continue;
                } else if (c == '\'' && !inDoubleQuotes) {
                    inSingleQuotes = !inSingleQuotes;
                    currentToken.append(c);
                    continue;
                }

                if (c == ' ' && !inDoubleQuotes && !inSingleQuotes) {
                    if (currentToken.length() > 0) {
                        processToken(currentToken.toString());
                        currentToken.setLength(0);
                    }
                    continue;
                }

                currentToken.append(c);
            }

            if (currentToken.length() > 0) {
                processToken(currentToken.toString());
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    private void processToken(String token) {
        if (token.contains("=")) {
            int equalsIndex = token.indexOf('=');
            String key = token.substring(0, equalsIndex);
            String value = token.substring(equalsIndex + 1);

            value = removeQuotes(value);
            put(key, value);
        } else {
            String value = removeQuotes(token);
            put(ParameterPrefix + (parameters != null ? parameters.size() + 1 : 1), value);
        }
    }

    private static String removeQuotes(String str) {
        if ((str.startsWith("\"") && str.endsWith("\""))
                || (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public void put(String key, String value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
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

    /*
        get/set
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public BaseMacro setParameters(LinkedHashMap<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

}
