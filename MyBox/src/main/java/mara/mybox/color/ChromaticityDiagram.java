package mara.mybox.color;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-17 9:57:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ChromaticityDiagram {

    private int width = 1300, height = 1300;
    private int margins = 60, ruler = 10, each = 5, grid = each * ruler;
    private BufferedImage image;
    private Color gridColor = new Color(0.9f, 0.9f, 0.9f), rulerColor = Color.LIGHT_GRAY,
            textColor, bgColor;
    private Graphics2D g;
    private int stepH = (height - margins * 2) / grid;
    private int stepW = (width - margins * 2) / grid;
    private int startH = margins + stepH, startW = margins + stepW;
    private int endH = stepH * grid + margins;
    private int endW = stepW * grid + margins;
    private int fontSize = 20;
    private Font dataFont, commentsFont;
    private int dotSize = 6;
    private boolean isLine, show2Degree, show10Degree, showDataSource;
    private File dataSourceFile;
    private String dataSourceTexts;
    private String title;
    private LinkedHashMap<DataType, Boolean> show;
    private double calculateX = -1, calculateY = -1;
    private Color calculateColor;

    public static enum DataType {
        Grid, CIE2Degree, CIE10Degree, CIEDataSource, Calculate, Wave, CIELines, ECILines, sRGBLines, AdobeLines, AppleLines,
        PALLines, NTSCLines, ColorMatchLines, ProPhotoLines, SMPTECLines, WhitePoints
    }

    public ChromaticityDiagram() {
        show = new LinkedHashMap();
    }

    /*
        Diagram
     */
    public BufferedImage drawData(LinkedHashMap<DataType, Boolean> show) {
        this.show = show;
        return drawData();
    }

    public BufferedImage drawData() {
        try {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g = image.createGraphics();
            if (bgColor != null) {
                g.setColor(bgColor);
                g.fillRect(0, 0, width, height);

            }
            if (fontSize <= 0) {
                fontSize = 20;
            }
            dataFont = new Font(null, Font.PLAIN, fontSize);
            commentsFont = new Font(null, Font.BOLD, fontSize + 8);
            if (Color.BLACK.equals(bgColor)) {
                textColor = Color.WHITE;
            } else {
                textColor = Color.BLACK;
            }

            // Title / Bottom
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            if (title == null) {
                title = message("ChromaticityDiagram");
            }
            g.setFont(commentsFont);
            g.setColor(textColor);
            g.drawString(title, margins + 400, 50);
            g.setFont(dataFont);
            g.drawString(message("ChromaticityDiagramComments"), 20, endH + 55);

            backGround();
            outlines();
            whitePoints();
            primariesLines();
            calculate();

            g.dispose();
            return image;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    private void backGround() {
        try {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            BasicStroke stroke;
            stroke = new BasicStroke(2);
            g.setStroke(stroke);
            g.setFont(dataFont);

            if (show.get(DataType.Grid)) {
                g.setColor(gridColor);
                for (int i = 0; i < grid; ++i) {
                    int h = startH + i * stepH;
                    g.drawLine(margins, h, endW, h);
                    int w = startW + i * stepW;
                    g.drawLine(w, margins, w, endH);
                }
                g.setColor(rulerColor);
                for (int i = 0; i < ruler; ++i) {
                    int h = margins + i * stepH * each;
                    g.drawLine(margins, h, endW, h);
                    int w = margins + i * stepW * each;
                    g.drawLine(w, margins, w, endH);
                }
            }

            g.setColor(textColor);
            g.drawLine(margins, margins, margins, endH);
            g.drawLine(margins, endH, endW, endH);
            for (int i = 0; i < ruler; ++i) {
                int h = margins + (i + 1) * stepH * each;
                g.drawString("0." + (9 - i), 10, h + 10);
                int w = margins + i * stepW * each;
                g.drawString("0." + i, w - 10, endH + 30);
            }
            g.drawString("1.0", endW - 15, endH + 30);
            g.drawString("1.0", 10, margins);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void outlines() {
        try {
            List<CIEData> data;
            CIEData cieData = new CIEData();
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            if (show.get(DataType.CIE2Degree)) {
                data = cieData.cie1931Observer2Degree1nmData(cs);
                outline(data, message("CIE1931Observer2Degree"), 535, textColor);
            }
            if (show.get(DataType.CIE10Degree)) {
                data = cieData.cie1964Observer10Degree1nmData(cs);
                if (show.get(DataType.CIE2Degree)) {
                    outline(data, message("CIE1964Observer10Degree"), 525, Color.BLUE);
                } else {
                    outline(data, message("CIE1964Observer10Degree"), 525, textColor);
                }
            }
            if (show.get(DataType.CIEDataSource)) {
                data = null;
                if (dataSourceTexts != null && !dataSourceTexts.isEmpty()) {
                    data = CIEData.read(dataSourceTexts);
                } else if (dataSourceFile != null) {
                    data = CIEData.read(dataSourceFile, cs);
                }
                if (data != null) {
                    if (show.get(DataType.CIE2Degree) || show.get(DataType.CIE10Degree)) {
                        outline(data, message("InputtedData"), 520, Color.RED);
                    } else {
                        outline(data, message("InputtedData"), 520, textColor);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void outline(List<CIEData> data, String name, int markWave, Color waveColor) {
        try {
            if (data == null || data.isEmpty()) {
                return;
            }
            Color pColor;
            int dotSizeHalf = dotSize / 2;
            BasicStroke strokeSized = new BasicStroke(dotSize);
            BasicStroke stroke1 = new BasicStroke(1);
            int dataW = width - margins * 2, dataH = height - margins * 2;
            int x, y, wave, lastx = -1, lasty = -1;
            double[] srgb;
            for (CIEData d : data) {
                wave = d.getWaveLength();
                x = (int) Math.round(margins + dataW * d.getNormalizedX());
                y = (int) Math.round(endH - dataH * d.getNormalizedY());
                srgb = d.getChannels();
                if (srgb == null) {
                    srgb = CIEData.sRGB65(d);
                }
                pColor = new Color((float) srgb[0], (float) srgb[1], (float) srgb[2]);
                g.setColor(pColor);
                g.setStroke(stroke1);
                if (isLine) {
                    if (lastx >= 0 && lasty >= 0) {
                        g.setStroke(strokeSized);
                        g.drawLine(lastx, lasty, x, y);
                    } else {
                        g.fillRect(x - dotSizeHalf, y - dotSizeHalf, dotSize, dotSize);
                    }
                    lastx = x;
                    lasty = y;
                } else {
                    g.fillRect(x - dotSizeHalf, y - dotSizeHalf, dotSize, dotSize);
                }
                if (wave == markWave) {
                    g.setColor(waveColor);
                    g.drawLine(x, y, x + 300, y + markWave - 560);
                    g.drawString(name, x + 310, y + markWave - 560);
                }
                if (show.get(DataType.Wave)) {
                    if (wave == 360 || wave == 830 || wave == 460) {
                        g.setColor(waveColor);
                        g.drawString(wave + "nm", x + 10, y);
                    } else if (wave > 470 && wave < 620) {
                        if (wave % 5 == 0) {
                            g.setColor(waveColor);
                            g.drawString(wave + "nm", x + 10, y);
                        }
                    } else if (wave >= 620 && wave <= 640) {
                        if (wave % 10 == 0) {
                            g.setColor(waveColor);
                            g.drawString(wave + "nm", x + 10, y);
                        }
                    }
                } else {
                }
            }

            // Bottom line
            CIEData d1 = data.get(0);
            CIEData d2 = data.get(data.size() - 1);
            double x1 = d1.getNormalizedX(), x2 = d2.getNormalizedX();
            double y1 = d1.getNormalizedY(), y2 = d2.getNormalizedY();
            colorLine(x1, y1, x2, y2);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void whitePoints() {
        if (!show.get(DataType.WhitePoints)) {
            return;
        }
        int dataW = width - margins * 2, dataH = height - margins * 2;
        int x, y;
        g.setFont(dataFont);
        g.setColor(textColor);
        BasicStroke stroke1 = new BasicStroke(1);
        g.setStroke(stroke1);

        double[] xy = Illuminant.Illuminant1931A;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("A", x + 10, y + 5);

        xy = Illuminant.Illuminant1931D50;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("D50", x + 10, y + 5);

        xy = Illuminant.Illuminant1931C;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("C", x + 10, y + 15);

        xy = Illuminant.Illuminant1931E;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("E", x + 10, y + 10);

        xy = Illuminant.Illuminant1931D65;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("D65", x - 40, y);

        xy = Illuminant.Illuminant1931D55;
        x = (int) Math.round(margins + dataW * xy[0]);
        y = (int) Math.round(endH - dataH * xy[1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.drawString("D55", x - 30, y - 10);

    }

    private void primariesLines() {
        if (show == null) {
            return;
        }
        for (DataType name : show.keySet()) {
            if (!show.get(name)) {
                continue;
            }

            switch (name) {
                case CIELines:
                    primariesLines(RGBColorSpace.CIERGB, RGBColorSpace.CIEPrimariesD50, 150, 15);
                    break;
                case ECILines:
                    primariesLines(RGBColorSpace.ECIRGB, RGBColorSpace.ECIPrimariesD50, 150, -40);
                    break;
                case sRGBLines:
                    primariesLines(RGBColorSpace.sRGB, RGBColorSpace.sRGBPrimariesD50, 260, 0);
                    break;
                case AdobeLines:
                    primariesLines(RGBColorSpace.AdobeRGB, RGBColorSpace.AdobePrimariesD50, 180, 30);
                    break;
                case AppleLines:
                    primariesLines(RGBColorSpace.AppleRGB, RGBColorSpace.ApplePrimariesD50, 220, -70);
                    break;
                case PALLines:
                    primariesLines(RGBColorSpace.PALRGB, RGBColorSpace.PALPrimariesD50, 220, 20);
                    break;
                case NTSCLines:
                    primariesLines(RGBColorSpace.NTSCRGB, RGBColorSpace.NTSCPrimariesD50, 130, -30);
                    break;
                case ColorMatchLines:
                    primariesLines(RGBColorSpace.ColorMatchRGB, RGBColorSpace.ColorMatchPrimariesD50, 200, -30);
                    break;
                case ProPhotoLines:
                    primariesLines(RGBColorSpace.ProPhotoRGB, RGBColorSpace.ProPhotoPrimariesD50, 150, 0);
                    break;
                case SMPTECLines:
                    primariesLines(RGBColorSpace.SMPTECRGB, RGBColorSpace.SMPTECPrimariesD50, 150, 0);
                    break;
            }
        }
    }

    private void primariesLines(String name, double[][] xy, int xOffset, int yOffset) {
        if (image == null || g == null) {
            return;
        }
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
        g.setComposite(ac);
        BasicStroke stroke;
        stroke = new BasicStroke(3);
        g.setStroke(stroke);
        g.setFont(dataFont);
        int x, y;
        int dataW = width - margins * 2, dataH = height - margins * 2;

        // vertexes
        g.setColor(Color.RED);
        x = (int) Math.round(margins + dataW * xy[0][0]);
        y = (int) Math.round(endH - dataH * xy[0][1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.setColor(textColor);
//        g.drawString(getMessage("Red"), x + 10, y);
        g.drawLine(x, y, x + xOffset, y + yOffset);
        g.drawString(name, x + xOffset + 10, y + yOffset + 10);

        g.setColor(Color.GREEN);
        x = (int) Math.round(margins + dataW * xy[1][0]);
        y = (int) Math.round(endH - dataH * xy[1][1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.setColor(textColor);
//        g.drawString(getMessage("Green"), x + 10, y);
        g.drawLine(x, y, x + xOffset, y + yOffset);
        g.drawString(name, x + xOffset + 10, y + yOffset + 10);

        g.setColor(Color.BLUE);
        x = (int) Math.round(margins + dataW * xy[2][0]);
        y = (int) Math.round(endH - dataH * xy[2][1]);
        g.drawOval(x - 6, y - 6, 12, 12);
        g.setColor(textColor);
//        g.drawString(getMessage("Blue"), x + 10, y);

        // lines
        colorLine(xy[1][0], xy[1][1], xy[0][0], xy[0][1]);
        colorLine(xy[2][0], xy[2][1], xy[0][0], xy[0][1]);
        colorLine(xy[2][0], xy[2][1], xy[1][0], xy[1][1]);
    }

    private void colorLine(double ix1, double iy1, double ix2, double iy2) {
        if (image == null || g == null || ix1 == ix2) {
            return;
        }
        double x1, y1, x2, y2;
        if (ix1 < ix2) {
            x1 = ix1;
            y1 = iy1;
            x2 = ix2;
            y2 = iy2;
        } else {
            x1 = ix2;
            y1 = iy2;
            x2 = ix1;
            y2 = iy1;
        }
        Color pColor;
        BasicStroke strokeSized = new BasicStroke(dotSize);
        BasicStroke stroke1 = new BasicStroke(1);
        int dataW = width - margins * 2, dataH = height - margins * 2;
        int x, y, halfDot = dotSize / 2, lastx = -1, lasty = -1;
        double ratio = (y2 - y1) / (x2 - x1);
        double step = (x2 - x1) / 100;
        double[] srgb;
        for (double bx = x1 + step; bx < x2; bx += step) {
            double by = (bx - x1) * ratio + y1;
            double bz = 1 - bx - by;
            double[] relativeXYZ = CIEData.relative(bx, by, bz);
            srgb = CIEColorSpace.XYZd50toSRGBd65(relativeXYZ);
            pColor = new Color((float) srgb[0], (float) srgb[1], (float) srgb[2]);
            g.setColor(pColor);
            x = (int) Math.round(margins + dataW * bx);
            y = (int) Math.round(endH - dataH * by);
            if (isLine) {
                if (lastx >= 0 && lasty >= 0) {
                    g.setStroke(strokeSized);
                    g.drawLine(lastx, lasty, x, y);
                } else {
                    g.setStroke(stroke1);
                    g.fillRect(x - halfDot, y - halfDot, dotSize, dotSize);
                }
                lastx = x;
                lasty = y;
            } else {
                g.setStroke(stroke1);
                g.fillRect(x - halfDot, y - halfDot, dotSize, dotSize);
            }
        }
    }

    private void calculate() {
        if (!show.get(DataType.Calculate)
                || calculateX < 0 || calculateX > 1
                || calculateY <= 0 || calculateY > 1) {
            return;
        }
        int dataW = width - margins * 2, dataH = height - margins * 2;
        BasicStroke stroke1 = new BasicStroke(1);
        g.setStroke(stroke1);

        double z = 1 - calculateX - calculateY;
        if (z < 0 || z > 1) {
            return;
        }
        Color pColor = calculateColor;
        if (pColor == null) {
            double[] relativeXYZ = CIEData.relative(calculateX, calculateY, z);
            double[] srgb = CIEColorSpace.XYZd50toSRGBd65(relativeXYZ);
            pColor = new Color((float) srgb[0], (float) srgb[1], (float) srgb[2]);
        }
        int x = (int) Math.round(margins + dataW * calculateX);
        int y = (int) Math.round(endH - dataH * calculateY);
        g.setColor(pColor);
        g.fillOval(x - 10, y - 10, 20, 20);

        g.setFont(dataFont);
        g.setColor(textColor);
        g.drawString(message("CalculatedValues"), x + 15, y + 5);

    }

    /*
        get/set
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMargins() {
        return margins;
    }

    public void setMargins(int margins) {
        this.margins = margins;
    }

    public int getRuler() {
        return ruler;
    }

    public void setRuler(int ruler) {
        this.ruler = ruler;
    }

    public int getEach() {
        return each;
    }

    public void setEach(int each) {
        this.each = each;
    }

    public int getGrid() {
        return grid;
    }

    public void setGrid(int grid) {
        this.grid = grid;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public Color getRulerColor() {
        return rulerColor;
    }

    public void setRulerColor(Color rulerColor) {
        this.rulerColor = rulerColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public int getStepH() {
        return stepH;
    }

    public void setStepH(int stepH) {
        this.stepH = stepH;
    }

    public int getStepW() {
        return stepW;
    }

    public void setStepW(int stepW) {
        this.stepW = stepW;
    }

    public int getStartH() {
        return startH;
    }

    public void setStartH(int startH) {
        this.startH = startH;
    }

    public int getStartW() {
        return startW;
    }

    public void setStartW(int startW) {
        this.startW = startW;
    }

    public int getEndH() {
        return endH;
    }

    public void setEndH(int endH) {
        this.endH = endH;
    }

    public int getEndW() {
        return endW;
    }

    public void setEndW(int endW) {
        this.endW = endW;
    }

    public Font getDataFont() {
        return dataFont;
    }

    public void setDataFont(Font dataFont) {
        this.dataFont = dataFont;
    }

    public Graphics2D getG() {
        return g;
    }

    public void setG(Graphics2D g) {
        this.g = g;
    }

    public Font getCommnetsFont() {
        return commentsFont;
    }

    public void setCommnetsFont(Font commnetsFont) {
        this.commentsFont = commnetsFont;
    }

    public int getDotSize() {
        return dotSize;
    }

    public void setDotSize(int dotSize) {
        this.dotSize = dotSize;
    }

    public boolean isIsLine() {
        return isLine;
    }

    public void setIsLine(boolean isLine) {
        this.isLine = isLine;
    }

    public boolean isShow2Degree() {
        return show2Degree;
    }

    public void setShow2Degree(boolean show2Degree) {
        this.show2Degree = show2Degree;
    }

    public boolean isShow10Degree() {
        return show10Degree;
    }

    public void setShow10Degree(boolean show10Degree) {
        this.show10Degree = show10Degree;
    }

    public File getDataSourceFile() {
        return dataSourceFile;
    }

    public void setDataSourceFile(File dataSourceFile) {
        this.dataSourceFile = dataSourceFile;
    }

    public boolean isShowDataSource() {
        return showDataSource;
    }

    public void setShowDataSource(boolean showDataSource) {
        this.showDataSource = showDataSource;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LinkedHashMap<DataType, Boolean> getShow() {
        return show;
    }

    public void setShow(LinkedHashMap<DataType, Boolean> show) {
        this.show = show;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public double getCalculateX() {
        return calculateX;
    }

    public void setCalculateX(double calculateX) {
        this.calculateX = calculateX;
    }

    public double getCalculateY() {
        return calculateY;
    }

    public void setCalculateY(double calculateY) {
        this.calculateY = calculateY;
    }

    public Color getCalculateColor() {
        return calculateColor;
    }

    public void setCalculateColor(Color calculateColor) {
        this.calculateColor = calculateColor;
    }

    public String getDataSourceTexts() {
        return dataSourceTexts;
    }

    public void setDataSourceTexts(String dataSourceTexts) {
        this.dataSourceTexts = dataSourceTexts;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Font getCommentsFont() {
        return commentsFont;
    }

    public void setCommentsFont(Font commentsFont) {
        this.commentsFont = commentsFont;
    }

}
