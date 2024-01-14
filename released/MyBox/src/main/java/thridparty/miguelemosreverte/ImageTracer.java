// https://github.com/miguelemosreverte/imagetracerjava
package thridparty.miguelemosreverte;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class ImageTracer {

    public static String versionnumber = "1.1.3";
    private static int[] rawdata;

    public ImageTracer() {
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("ERROR: there's no input filename. Basic usage: \r\n\r\njava -jar ImageTracer.jar <filename>"
                        + "\r\n\r\nor\r\n\r\njava -jar ImageTracer.jar help");

                //System.out.println("Starting anyway with default value for testing purposes.");	
                //saveString("output.svg",imageToSVG("input.jpg",new HashMap<String,Float>()));
            } else if (arraycontains(args, "help") > -1) {
                System.out.println("Example usage:\r\n\r\njava -jar ImageTracer.jar <filename> outfilename test.svg "
                        + "ltres 1 qtres 1 pathomit 1 numberofcolors 128 colorquantcycles 15 "
                        + "scale 1 roundcoords 1 lcpr 0 qcpr 0 desc 1 viewbox 0  blurradius 0 blurdelta 20 \r\n"
                        + "\r\nOnly <filename> is mandatory, if some of the other optional parameters are missing, they will be set to these defaults. "
                        + "\r\nWarning: if outfilename is not specified, then <filename>.svg will be overwritten."
                        + "\r\nSee https://github.com/jankovicsandras/imagetracerjava for details. \r\nThis is version " + versionnumber);
            } else {

                // Parameter parsing
                String outfilename = args[0];
                HashMap<String, Float> options = new HashMap<String, Float>();
                String[] parameternames = {"ltres", "qtres", "pathomit", "numberofcolors", "colorquantcycles", "format", "scale", "roundcoords", "lcpr", "qcpr", "desc", "viewbox", "outfilename", "blurammount"};
                int j = -1;
                float f = -1;
                for (String parametername : parameternames) {
                    j = arraycontains(args, parametername);
                    if (j > -1) {
                        if (parametername == "outfilename") {
                            if (j < (args.length - 1)) {
                                outfilename = args[j + 1];
                            }
                        } else {
                            f = parsenext(args, j);
                            if (f > -1) {
                                options.put(parametername, new Float(f));
                            }
                        }
                    }
                }// End of parameternames loop

                options = checkoptions(options);

                // Loading image, tracing, rendering, saving output file
                if (options.get("format") == 0) {
                    saveString(outfilename + ".svg", imageToSVG(args[0], options));
                } else if (options.get("format") == 1) {
                    saveString(outfilename + ".pdf", imageToPDF(args[0], options));
                } else {
                    System.out.println("ERROR: Incorrect output format. Options: 0 - SVG, 1 - PDF");
                }

            }// End of parameter parsing and processing

        } catch (Exception e) {
            e.printStackTrace();
        }
    }// End of main()

    public static int arraycontains(String[] arr, String str) {
        for (int j = 0; j < arr.length; j++) {
            if (arr[j].toLowerCase().equals(str)) {
                return j;
            }
        }
        return -1;
    }

    public static float parsenext(String[] arr, int i) {
        if (i < (arr.length - 1)) {
            try {
                return Float.parseFloat(arr[i + 1]);
            } catch (Exception e) {
            }
        }
        return -1;
    }

    // Container for the color-indexed image before and tracedata after vectorizing
    public static class IndexedImage {

        public int width, height;
        public int[][] array; // array[x][y] of palette colors
        public byte[][] palette;// array[palettelength][4] RGBA color palette
        public ArrayList<ArrayList<ArrayList<Double[]>>> layers;// tracedata

        public IndexedImage(int[][] marray, byte[][] mpalette) {
            array = marray;
            palette = mpalette;
            width = marray[0].length - 2;
            height = marray.length - 2;// Color quantization adds +2 to the original width and height
        }
    }

    // https://developer.mozilla.org/en-US/docs/Web/API/ImageData
    public static class ImageData {

        public int width, height;
        public byte[] data; // raw byte data: R G B A R G B A ...

        public ImageData(int mwidth, int mheight, byte[] mdata) {
            width = mwidth;
            height = mheight;
            data = mdata;
        }
    }

    // Saving a String as a file
    public static void saveString(String filename, String str) throws Exception {
        File file = new File(filename);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(str);
        bw.close();
    }

    // Loading a file to ImageData, ARGB byte order
    public static ImageData loadImageData(String filename, HashMap<String, Float> options)
            throws Exception {

        BufferedImage image = ImageIO.read(new File(filename));
        return loadImageData(image);
    }

    public static ImageData loadImageData(BufferedImage image) throws Exception {

        int width = image.getWidth();
        int height = image.getHeight();
        rawdata = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] data = new byte[rawdata.length * 4];
        for (int i = 0; i < rawdata.length; i++) {
            data[(i * 4) + 3] = bytetrans((byte) (rawdata[i] >>> 24));
            data[i * 4] = bytetrans((byte) (rawdata[i] >>> 16));
            data[(i * 4) + 1] = bytetrans((byte) (rawdata[i] >>> 8));
            data[(i * 4) + 2] = bytetrans((byte) (rawdata[i]));
        }
        return new ImageData(width, height, data);
    }

    // The bitshift method in loadImageData creates signed bytes where -1 -> 255 unsigned ; -128 -> 128 unsigned ;
    // 127 -> 127 unsigned ; 0 -> 0 unsigned ; These will be converted to -128 (representing 0 unsigned) ...
    // 127 (representing 255 unsigned) and tosvgcolorstr will add +128 to create RGB values 0..255
    public static byte bytetrans(byte b) {
        if (b < 0) {
            return (byte) (b + 128);
        } else {
            return (byte) (b - 128);
        }
    }

    public static byte[][] getPalette(BufferedImage image, HashMap<String, Float> options) {
        int numberofcolors = options.get("numberofcolors").intValue();
        int[][] pixels = new int[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        }
        int[] palette = Quantize.quantizeImage(pixels, numberofcolors);
        byte[][] bytepalette = new byte[numberofcolors][4];

        for (int i = 0; i < palette.length; i++) {
            Color c = new Color(palette[i]);
            bytepalette[i][0] = (byte) c.getRed();
            bytepalette[i][1] = (byte) c.getGreen();
            bytepalette[i][2] = (byte) c.getBlue();
            bytepalette[i][3] = 0;
        }
        return bytepalette;
    }

    ////////////////////////////////////////////////////////////
    //
    //  User friendly functions
    //
    ////////////////////////////////////////////////////////////
    // Loading an image from a file, tracing when loaded, then returning the SVG String
    public static String imageToSVG(String filename, HashMap<String, Float> options)
            throws Exception {
        System.out.println(options.toString());
        ImageData imgd = loadImageData(filename, options);
        return imagedataToSVG(imgd, options, getPalette(ImageIO.read(new File(filename)), options));
    }// End of imageToSVG()

    // Tracing ImageData, then returning the SVG String
    public static String imagedataToSVG(ImageData imgd, HashMap<String, Float> options, byte[][] palette) {
        IndexedImage ii = imagedataToTracedata(imgd, options, palette);
        return SVGUtils.getsvgstring(ii, options);
    }// End of imagedataToSVG()

    // Loading an image from a file, tracing when loaded, then returning PDF String
    public static String imageToPDF(String filename, HashMap<String, Float> options)
            throws Exception {
        ImageData imgd = loadImageData(filename, options);
        IndexedImage ii = imagedataToTracedata(imgd, options, getPalette(ImageIO.read(new File(filename)), options));
        return PDFUtils.getPDFString(ii, options);
    }// End of imagedataToSVG()

    // Loading an image from a file, tracing when loaded, then returning IndexedImage with tracedata in layers
    public IndexedImage imageToTracedata(String filename, HashMap<String, Float> options, byte[][] palette)
            throws Exception {
        ImageData imgd = loadImageData(filename, options);
        return imagedataToTracedata(imgd, options, palette);
    }// End of imageToTracedata()

    public IndexedImage imageToTracedata(BufferedImage image, HashMap<String, Float> options, byte[][] palette)
            throws Exception {
        ImageData imgd = loadImageData(image);
        return imagedataToTracedata(imgd, options, palette);
    }// End of imageToTracedata()

    // Tracing ImageData, then returning IndexedImage with tracedata in layers
    public static IndexedImage imagedataToTracedata(ImageData imgd, HashMap<String, Float> options, byte[][] palette) {
        // 1. Color quantization
        IndexedImage ii = VectorizingUtils.colorquantization(imgd, palette, options);
        // 2. Layer separation and edge detection
        int[][][] rawlayers = VectorizingUtils.layering(ii);
        // 3. Batch pathscan
        ArrayList<ArrayList<ArrayList<Integer[]>>> bps = VectorizingUtils.batchpathscan(rawlayers, (int) (Math.floor(options.get("pathomit"))));
        // 4. Batch interpollation
        ArrayList<ArrayList<ArrayList<Double[]>>> bis = VectorizingUtils.batchinternodes(bps);
        // 5. Batch tracing
        ii.layers = VectorizingUtils.batchtracelayers(bis, options.get("ltres"), options.get("qtres"));
        return ii;
    }// End of imagedataToTracedata()

    // creating options object, setting defaults for missing values
    public static HashMap<String, Float> checkoptions(HashMap<String, Float> options) {
        if (options == null) {
            options = new HashMap<String, Float>();
        }
        // Tracing
        if (!options.containsKey("ltres")) {
            options.put("ltres", 10f);
        }
        if (!options.containsKey("qtres")) {
            options.put("qtres", 10f);
        }
        if (!options.containsKey("pathomit")) {
            options.put("pathomit", 1f);
        }
        // Color quantization
        if (!options.containsKey("numberofcolors")) {
            options.put("numberofcolors", 128f);
        }
        if (!options.containsKey("colorquantcycles")) {
            options.put("colorquantcycles", 15f);
        }
        // Output rendering
        if (!options.containsKey("format")) {
            options.put("format", 0f);
        }
        if (!options.containsKey("scale")) {
            options.put("scale", 1f);
        }
        if (!options.containsKey("roundcoords")) {
            options.put("roundcoords", 1f);
        }
        if (!options.containsKey("lcpr")) {
            options.put("lcpr", 0f);
        }
        if (!options.containsKey("qcpr")) {
            options.put("qcpr", 0f);
        }
        if (!options.containsKey("desc")) {
            options.put("desc", 1f);
        }
        if (!options.containsKey("viewbox")) {
            options.put("viewbox", 0f);
        }
        // Blur
        if (!options.containsKey("blurradius")) {
            options.put("blurradius", 5f);
        }
        if (!options.containsKey("blurdelta")) {
            options.put("blurdelta", 50f);
        }

        return options;
    }// End of checkoptions()

}// End of ImageTracer class
