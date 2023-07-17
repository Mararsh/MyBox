package mara.mybox.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static mara.mybox.color.IccXML.iccXML;
import mara.mybox.tools.StringTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ByteFileTools;
import mara.mybox.tools.MessageDigestTools;

/**
 * @Author Mara
 * @CreateDate 2019-5-6
 * @Description
 * @License Apache License Version 2.0
 */
public class IccProfile {

    private ICC_Profile profile;
    private File file;
    private ColorSpace colorSpace;
    private int predefinedColorSpaceType;
    private byte[] data;
    private IccHeader header;
    private IccTags tags;
    private boolean isValid, normalizeLut;
    private String error;

    public IccProfile(int colorSpaceType) {
        try {
            profile = ICC_Profile.getInstance(colorSpaceType);
            init();
            isValid = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            isValid = false;
        }

    }

    public IccProfile(String colorSpaceType) {
        try {
            predefinedColorSpaceType = ColorBase.colorSpaceType(colorSpaceType);
            if (predefinedColorSpaceType < 0) {
                return;
            }
            profile = ICC_Profile.getInstance(predefinedColorSpaceType);
            init();
            isValid = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            isValid = false;
        }
    }

    public IccProfile(File profileFile) {
        try {
            if (profileFile == null || !profileFile.exists()) {
                return;
            }
            this.file = profileFile;
            predefinedColorSpaceType = -1;
            profile = ICC_Profile.getInstance(profileFile.getAbsolutePath());
            init();
            isValid = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            isValid = false;
        }
    }

    public IccProfile(byte[] data) {
        try {
            if (data == null) {
                return;
            }
            this.data = data;
            predefinedColorSpaceType = -1;
            isValid = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            isValid = false;
        }
    }

    private void init() {
        try {
            if (profile == null) {
                return;
            }
            if (predefinedColorSpaceType < 0) {
                colorSpace = new ICC_ColorSpace(profile);
            } else {
                colorSpace = ColorSpace.getInstance(predefinedColorSpaceType);
            }
            isValid = colorSpace != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            isValid = false;
        }
    }

    public byte[] readData() {
        if (data == null) {
            if (file != null) {
                data = ByteFileTools.readBytes(file);
            } else if (profile != null) {
                data = profile.getData();
            }
        }
        return data;
    }

    public String readXML() {
        return iccXML(getHeader(), getTags());
    }

    public boolean update(byte[] newHeader) {
        if (newHeader == null || newHeader.length != 128) {
            return false;
        }
        header.update(newHeader);
        data = tags.update();
        System.arraycopy(newHeader, 4, data, 4, 124);

        return setProfileID();

    }

    public boolean setProfileID() {
        try {
            byte[] bytes44 = new byte[4];
            System.arraycopy(data, 44, bytes44, 0, 4);
            byte[] bytes64 = new byte[4];
            System.arraycopy(data, 64, bytes64, 0, 4);
            byte[] bytes84 = new byte[16];
            System.arraycopy(data, 84, bytes84, 0, 16);

            byte[] blank = new byte[4];
            System.arraycopy(blank, 0, data, 44, 4);
            System.arraycopy(blank, 0, data, 64, 4);
            blank = new byte[16];
            System.arraycopy(blank, 0, data, 84, 16);

            byte[] digest = MessageDigestTools.MD5(data);

            System.arraycopy(bytes44, 0, data, 44, 4);
            System.arraycopy(bytes64, 0, data, 64, 4);
            System.arraycopy(digest, 0, data, 84, 16);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

//    public boolean write(File file) {
//        return FileTools.writeFile(file, data);
//    }
    public boolean write(File file, byte[] newHeader) {
        if (!update(newHeader)) {
            return false;
        }
        return ByteFileTools.writeFile(file, data) != null;
    }

    public float[] calculateXYZ(float[] color) {
        if (profile == null) {
            return null;
        }
        if (colorSpace == null) {
            colorSpace = new ICC_ColorSpace(profile);
        }
        if (colorSpace == null) {
            return null;
        }
        return colorSpace.toCIEXYZ(color);
    }

    public float[] calculateCoordinate(float[] xyz) {
        if (xyz == null) {
            return null;
        }
        float x = xyz[0];
        float y = xyz[1];
        float z = xyz[2];
        float sum = x + y + z;
        float[] xy = new float[2];
        xy[0] = x / sum;
        xy[1] = y / sum;
        return xy;
    }

    /*
        Encode
     */
    public static byte[] toBytes(String value) {
        try {
            return value.getBytes(StandardCharsets.US_ASCII);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] first4ASCII(String value) {
        byte[] bytes = new byte[4];
        try {
            String vString = value;
            if (value.length() > 4) {
                vString = value.substring(0, 4);
            } else if (value.length() < 4) {
                vString = StringTools.fillRightBlank(value, 4);
            }
            byte[] vBytes = toBytes(vString);
            System.arraycopy(vBytes, 0, bytes, 0, 4);
        } catch (Exception e) {
        }
        return bytes;
    }


    /*
        Get/Set
     */
    public ICC_Profile getProfile() {
        return profile;
    }

    public void setProfile(ICC_Profile profile) {
        this.profile = profile;
    }

    public byte[] getData() {
        if (data == null) {
            readData();
        }
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ColorSpace getColorSpace() {
        if (profile == null) {
            return null;
        }
        if (colorSpace == null) {
            if (predefinedColorSpaceType < 0) {
                colorSpace = new ICC_ColorSpace(profile);
            } else {
                colorSpace = ColorSpace.getInstance(predefinedColorSpaceType);
            }
        }
        return colorSpace;
    }

    public void setColorSpace(ColorSpace iccColorSpace) {
        this.colorSpace = iccColorSpace;
    }

    public IccHeader getHeader() {
        if (header == null) {
            header = new IccHeader(getData());
        }
        return header;
    }

    public void setHeader(IccHeader header) {
        this.header = header;
    }

    public IccTags getTags() {
        if (tags == null) {
            tags = new IccTags(getData(), normalizeLut);
        }
        return tags;
    }

    public void setTags(IccTags tags) {
        this.tags = tags;
    }

    public boolean isNormalizeLut() {
        return normalizeLut;
    }

    public void setNormalizeLut(boolean normalizeLut) {
        this.normalizeLut = normalizeLut;
    }

    public int getPredefinedColorSpaceType() {
        return predefinedColorSpaceType;
    }

    public void setPredefinedColorSpaceType(int predefinedColorSpaceType) {
        this.predefinedColorSpaceType = predefinedColorSpaceType;
    }

}
