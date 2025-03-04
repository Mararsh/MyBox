package mara.mybox.color;

import java.awt.Color;
import java.awt.color.ColorSpace;
import static mara.mybox.tools.DoubleTools.scale;

/**
 * @Author Mara
 * @CreateDate 2019-5-20 18:51:37
 * @License Apache License Version 2.0
 */
public class CIEData {

    public int waveLength;  // nm
    public double X, Y, Z; // tristimulus values in 0~1.
    public double normalizedX, normalizedY, normalizedZ; // x + y + z = 1
    public double relativeX, relativeY, relativeZ; // y = 1;
    public double red = -1, green = -1, blue = -1; // sRGB
    public int redi = -1, greeni = -1, bluei = -1; // sRGB integer
    public ColorSpace colorSpace;
    public double[] channels;
    public int scale = 8;

    public CIEData() {

    }

    public CIEData(int waveLength, double X, double Y, double Z) {
        this.waveLength = waveLength;
        setTristimulusValues(X, Y, Z);
    }

    public CIEData(int waveLength, double X, double Y, double Z, ColorSpace cs) {
        this.waveLength = waveLength;
        setTristimulusValues(X, Y, Z);
        convert(cs);
    }

    public CIEData(double x, double y) {
        setxy(x, y);
    }

    public CIEData(Color color) {
        double[] xyz = SRGB.SRGBtoXYZd50(color);
        setTristimulusValues(xyz[0], xyz[1], xyz[2]);
    }

    public CIEData(javafx.scene.paint.Color color) {
        double[] xyz = SRGB.SRGBtoXYZd50(new Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue()));
        setTristimulusValues(xyz[0], xyz[1], xyz[2]);
    }

    public final void setTristimulusValues(double X, double Y, double Z) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;

        double[] xyz = CIEDataTools.normalize(X, Y, Z);
        this.normalizedX = xyz[0];
        this.normalizedY = xyz[1];
        this.normalizedZ = xyz[2];

        xyz = CIEDataTools.relative(X, Y, Z);
        this.relativeX = xyz[0];
        this.relativeY = xyz[1];
        this.relativeZ = xyz[2];
    }

    public final void setxy(double x, double y) {
        setxyY(x, y, 1.0);
    }

    public final void setxyY(double[] xyY) {
        setxyY(xyY[0], xyY[1], xyY[2]);
    }

    public final void setxyY(double x, double y, double Y) {
        this.normalizedX = x;
        this.normalizedY = y;
        this.normalizedZ = 1 - x - y;

        this.X = x * Y / y;
        this.Y = Y;
        this.Z = (1 - x - y) * Y / y;

        this.relativeX = x / y;
        this.relativeY = 1.0;
        this.relativeZ = (1 - x - y) / y;
    }

    public final void setRelativeXYZ(double[] XYZ) {
        setRelativeXYZ(XYZ[0], XYZ[1], XYZ[2]);
    }

    public final void setRelativeXYZ(double X, double Y, double Z) {
        this.relativeX = X;
        this.relativeY = Y;
        this.relativeZ = Z;

        double[] xyz = CIEDataTools.normalize(X, Y, Z);
        this.normalizedX = xyz[0];
        this.normalizedY = xyz[1];
        this.normalizedZ = xyz[2];

        this.X = relativeX * 100;
        this.Y = relativeY * 100;
        this.Z = relativeZ * 100;
    }

    public final void setNormalziedXY(double[] xy) {
        setNormalziedXY(xy[0], xy[1]);
    }

    public final void setNormalziedXY(double x, double y) {
        this.normalizedX = x;
        this.normalizedY = y;
        this.normalizedZ = 1 - x - y;

        this.relativeX = x / y;
        this.relativeY = 1;
        this.relativeZ = (1 - x - y) / y;

        this.X = relativeX * 100;
        this.Y = relativeY * 100;
        this.Z = relativeZ * 100;
    }

    public final double[] convert(ColorSpace cs) {
        if (cs == null) {
            channels = CIEDataTools.CIERGB(this);
        } else {
            colorSpace = cs;
            if (cs.isCS_sRGB()) {
                channels = CIEDataTools.sRGB65(this);
            } else {
                channels = CIEDataTools.convert(cs, this);
            }
        }
        return channels;
    }

    public void scaleValues() {
        scaleValues(this.scale);
    }

    public void scaleValues(int scale) {
        this.scale = scale;
        X = scale(X, scale);
        Y = scale(Y, scale);
        Z = scale(Z, scale);
        normalizedX = scale(normalizedX, scale);
        normalizedY = scale(normalizedY, scale);
        normalizedZ = scale(normalizedZ, scale);
        relativeX = scale(relativeX, scale);
        relativeY = scale(relativeY, scale);
        relativeZ = scale(relativeZ, scale);
        if (channels != null) {
            for (int i = 0; i < channels.length; ++i) {
                channels[i] = scale(channels[i], scale);
            }
        }
    }


    /*
    get/set
     */
    public double getX() {
        return X;
    }

    public void setX(double X) {
        this.X = X;
    }

    public double getY() {
        return Y;
    }

    public void setY(double Y) {
        this.Y = Y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double Z) {
        this.Z = Z;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getWaveLength() {
        return waveLength;
    }

    public void setWaveLength(int waveLength) {
        this.waveLength = waveLength;
    }

    public double getNormalizedX() {
        return normalizedX;
    }

    public void setNormalizedX(double normalizedX) {
        this.normalizedX = normalizedX;
    }

    public double getNormalizedY() {
        return normalizedY;
    }

    public void setNormalizedY(double normalizedY) {
        this.normalizedY = normalizedY;
    }

    public double getNormalizedZ() {
        return normalizedZ;
    }

    public void setNormalizedZ(double normalizedZ) {
        this.normalizedZ = normalizedZ;
    }

    public double getRed() {
        if (red == -1) {
            red = channels[0];
        }
        return red;
    }

    public void setRed(double red) {
        this.red = red;
    }

    public double getGreen() {
        if (green == -1) {
            green = channels[1];
        }
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getBlue() {
        if (blue == -1) {
            blue = channels[2];
        }
        return blue;
    }

    public void setBlue(double blue) {
        this.blue = blue;
    }

    public int getRedi() {
        if (redi == -1) {
            redi = (int) Math.round(255 * channels[0]);
        }
        return redi;
    }

    public void setRedi(int redi) {
        this.redi = redi;
    }

    public int getGreeni() {
        if (greeni == -1) {
            greeni = (int) Math.round(255 * channels[1]);
        }
        return greeni;
    }

    public void setGreeni(int greeni) {
        this.greeni = greeni;
    }

    public int getBluei() {
        if (bluei == -1) {
            bluei = (int) Math.round(255 * channels[2]);
        }
        return bluei;
    }

    public void setBluei(int bluei) {
        this.bluei = bluei;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public void setRelativeX(double relativeX) {
        this.relativeX = relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }

    public void setRelativeY(double relativeY) {
        this.relativeY = relativeY;
    }

    public double getRelativeZ() {
        return relativeZ;
    }

    public void setRelativeZ(double relativeZ) {
        this.relativeZ = relativeZ;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }

    public double[] getChannels() {
        return channels;
    }

    public void setChannels(double[] channels) {
        this.channels = channels;
    }

}
