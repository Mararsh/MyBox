package mara.mybox.image;

import java.io.File;
import java.util.List;
import mara.mybox.color.CIEData;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
public class ImageInformationPng extends ImageInformation {

    protected String colorType, compressionMethod, unitSpecifier, filterMethod, interlaceMethod,
            renderingIntent;
    protected int pixelsPerUnitXAxis = -1, pixelsPerUnitYAxis = -1, bKGD_Grayscale = -1, bKGD_Palette = -1,
            sBIT_Grayscale = -1, sBIT_GrayAlpha_gray = -1, sBIT_GrayAlpha_alpha = -1,
            sBIT_RGB_red = -1, sBIT_RGB_green = -1, sBIT_RGB_blue = -1,
            sBIT_RGBAlpha_red = -1, sBIT_RGBAlpha_green = -1, sBIT_RGBAlpha_blue = -1, sBIT_RGBAlpha_alpha = -1,
            sBIT_Palette_red = -1, sBIT_Palette_green = -1, sBIT_Palette_blue = -1, tRNS_Grayscale = -1,
            tRNS_Palette_index = -1, tRNS_Palette_alpha = -1;
    protected ImageColor bKGD_RGB, tRNS_RGB;
    protected List<ImageColor> pngPalette, suggestedPalette;
    protected int pngPaletteSize, suggestedPaletteSize;
    protected CIEData white, red, blue, green;

    public ImageInformationPng(File file) {
        super(file);
    }

    /*
        get/set
     */
    public String getColorType() {
        return colorType;
    }

    public void setColorType(String colorType) {
        this.colorType = colorType;
    }

    public String getCompressionMethod() {
        return compressionMethod;
    }

    public void setCompressionMethod(String compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public String getUnitSpecifier() {
        return unitSpecifier;
    }

    public void setUnitSpecifier(String unitSpecifier) {
        this.unitSpecifier = unitSpecifier;
    }

    public int getPixelsPerUnitXAxis() {
        return pixelsPerUnitXAxis;
    }

    public void setPixelsPerUnitXAxis(int pixelsPerUnitXAxis) {
        this.pixelsPerUnitXAxis = pixelsPerUnitXAxis;
    }

    public int getPixelsPerUnitYAxis() {
        return pixelsPerUnitYAxis;
    }

    public void setPixelsPerUnitYAxis(int pixelsPerUnitYAxis) {
        this.pixelsPerUnitYAxis = pixelsPerUnitYAxis;
    }

    public String getFilterMethod() {
        return filterMethod;
    }

    public void setFilterMethod(String filterMethod) {
        this.filterMethod = filterMethod;
    }

    public String getInterlaceMethod() {
        return interlaceMethod;
    }

    public void setInterlaceMethod(String interlaceMethod) {
        this.interlaceMethod = interlaceMethod;
    }

    public List<ImageColor> getPngPalette() {
        return pngPalette;
    }

    public void setPngPalette(List<ImageColor> pngPalette) {
        this.pngPalette = pngPalette;
    }

    public int getbKGD_Grayscale() {
        return bKGD_Grayscale;
    }

    public void setbKGD_Grayscale(int bKGD_Grayscale) {
        this.bKGD_Grayscale = bKGD_Grayscale;
    }

    public int getbKGD_Palette() {
        return bKGD_Palette;
    }

    public void setbKGD_Palette(int bKGD_Palette) {
        this.bKGD_Palette = bKGD_Palette;
    }

    public ImageColor getbKGD_RGB() {
        return bKGD_RGB;
    }

    public void setbKGD_RGB(ImageColor bKGD_RGB) {
        this.bKGD_RGB = bKGD_RGB;
    }

    public CIEData getWhite() {
        return white;
    }

    public void setWhite(CIEData white) {
        this.white = white;
    }

    public CIEData getRed() {
        return red;
    }

    public void setRed(CIEData red) {
        this.red = red;
    }

    public CIEData getBlue() {
        return blue;
    }

    public void setBlue(CIEData blue) {
        this.blue = blue;
    }

    public CIEData getGreen() {
        return green;
    }

    public void setGreen(CIEData green) {
        this.green = green;
    }

    public String getRenderingIntent() {
        return renderingIntent;
    }

    public void setRenderingIntent(String renderingIntent) {
        this.renderingIntent = renderingIntent;
    }

    public int getsBIT_Grayscale() {
        return sBIT_Grayscale;
    }

    public void setsBIT_Grayscale(int sBIT_Grayscale) {
        this.sBIT_Grayscale = sBIT_Grayscale;
    }

    public int getsBIT_GrayAlpha_gray() {
        return sBIT_GrayAlpha_gray;
    }

    public void setsBIT_GrayAlpha_gray(int sBIT_GrayAlpha_gray) {
        this.sBIT_GrayAlpha_gray = sBIT_GrayAlpha_gray;
    }

    public int getsBIT_GrayAlpha_alpha() {
        return sBIT_GrayAlpha_alpha;
    }

    public void setsBIT_GrayAlpha_alpha(int sBIT_GrayAlpha_alpha) {
        this.sBIT_GrayAlpha_alpha = sBIT_GrayAlpha_alpha;
    }

    public int getsBIT_RGB_red() {
        return sBIT_RGB_red;
    }

    public void setsBIT_RGB_red(int sBIT_RGB_red) {
        this.sBIT_RGB_red = sBIT_RGB_red;
    }

    public int getsBIT_RGB_green() {
        return sBIT_RGB_green;
    }

    public void setsBIT_RGB_green(int sBIT_RGB_green) {
        this.sBIT_RGB_green = sBIT_RGB_green;
    }

    public int getsBIT_RGB_blue() {
        return sBIT_RGB_blue;
    }

    public void setsBIT_RGB_blue(int sBIT_RGB_blue) {
        this.sBIT_RGB_blue = sBIT_RGB_blue;
    }

    public int getsBIT_RGBAlpha_red() {
        return sBIT_RGBAlpha_red;
    }

    public void setsBIT_RGBAlpha_red(int sBIT_RGBAlpha_red) {
        this.sBIT_RGBAlpha_red = sBIT_RGBAlpha_red;
    }

    public int getsBIT_RGBAlpha_green() {
        return sBIT_RGBAlpha_green;
    }

    public void setsBIT_RGBAlpha_green(int sBIT_RGBAlpha_green) {
        this.sBIT_RGBAlpha_green = sBIT_RGBAlpha_green;
    }

    public int getsBIT_RGBAlpha_blue() {
        return sBIT_RGBAlpha_blue;
    }

    public void setsBIT_RGBAlpha_blue(int sBIT_RGBAlpha_blue) {
        this.sBIT_RGBAlpha_blue = sBIT_RGBAlpha_blue;
    }

    public int getsBIT_RGBAlpha_alpha() {
        return sBIT_RGBAlpha_alpha;
    }

    public void setsBIT_RGBAlpha_alpha(int sBIT_RGBAlpha_alpha) {
        this.sBIT_RGBAlpha_alpha = sBIT_RGBAlpha_alpha;
    }

    public int getsBIT_Palette_red() {
        return sBIT_Palette_red;
    }

    public void setsBIT_Palette_red(int sBIT_Palette_red) {
        this.sBIT_Palette_red = sBIT_Palette_red;
    }

    public int getsBIT_Palette_green() {
        return sBIT_Palette_green;
    }

    public void setsBIT_Palette_green(int sBIT_Palette_green) {
        this.sBIT_Palette_green = sBIT_Palette_green;
    }

    public int getsBIT_Palette_blue() {
        return sBIT_Palette_blue;
    }

    public void setsBIT_Palette_blue(int sBIT_Palette_blue) {
        this.sBIT_Palette_blue = sBIT_Palette_blue;
    }

    public List<ImageColor> getSuggestedPalette() {
        return suggestedPalette;
    }

    public void setSuggestedPalette(List<ImageColor> suggestedPalette) {
        this.suggestedPalette = suggestedPalette;
    }

    public int gettRNS_Grayscale() {
        return tRNS_Grayscale;
    }

    public void settRNS_Grayscale(int tRNS_Grayscale) {
        this.tRNS_Grayscale = tRNS_Grayscale;
    }

    public int gettRNS_Palette_index() {
        return tRNS_Palette_index;
    }

    public void settRNS_Palette_index(int tRNS_Palette_index) {
        this.tRNS_Palette_index = tRNS_Palette_index;
    }

    public int gettRNS_Palette_alpha() {
        return tRNS_Palette_alpha;
    }

    public void settRNS_Palette_alpha(int tRNS_Palette_alpha) {
        this.tRNS_Palette_alpha = tRNS_Palette_alpha;
    }

    public ImageColor gettRNS_RGB() {
        return tRNS_RGB;
    }

    public void settRNS_RGB(ImageColor tRNS_RGB) {
        this.tRNS_RGB = tRNS_RGB;
    }

    public int getPngPaletteSize() {
        return pngPaletteSize;
    }

    public void setPngPaletteSize(int pngPaletteSize) {
        this.pngPaletteSize = pngPaletteSize;
    }

    public int getSuggestedPaletteSize() {
        return suggestedPaletteSize;
    }

    public void setSuggestedPaletteSize(int suggestedPaletteSize) {
        this.suggestedPaletteSize = suggestedPaletteSize;
    }

}
