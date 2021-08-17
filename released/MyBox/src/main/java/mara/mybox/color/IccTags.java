package mara.mybox.color;

import java.awt.color.ICC_Profile;
import java.util.ArrayList;
import java.util.List;
import static mara.mybox.color.IccProfile.toBytes;
import static mara.mybox.color.IccXML.iccTagsXml;
import static mara.mybox.tools.ByteTools.bytesToInt;
import static mara.mybox.tools.ByteTools.intToBytes;
import static mara.mybox.tools.ByteTools.subBytes;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-5-6
 * @Description
 * @License Apache License Version 2.0
 */
public class IccTags {

    private byte[] data;  // all bytes in the profile, including 128 bytes of header
    private List<IccTag> tags;
    private String xml;
    private boolean isValid, normalizeLut;
    private String error;

    public IccTags(byte[] data, boolean normalizeLut) {
        try {
            this.data = data;
            this.normalizeLut = normalizeLut;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<IccTag> readTags() {
        if (data == null) {
            return null;
        }
        if (tags == null) {
            tags = iccTagsValues(data, normalizeLut);
        }
        return tags;
    }

    public IccTag getTag(String tag) {
        if (tags == null) {
            tags = iccTagsValues(data, normalizeLut);
        }
        return getTag(tags, tag);
    }

    public byte[] update() {
        data = encodeTags(tags);
        return data;
    }

    /*
        Static methods
     */
    public static List<IccTag> iccTagsValues(ICC_Profile profile, boolean normalizedLut) {
        if (profile == null) {
            return null;
        }
        byte[] data = profile.getData();
        return IccTags.iccTagsValues(data, normalizedLut);
    }

    public static List<IccTag> iccTagsValues(byte[] data, boolean normalizedLut) {
        List<IccTag> tags = new ArrayList<>();
        try {
            if (data == null || data.length < 132) {
                return tags;
            }
            int number = bytesToInt(subBytes(data, 128, 4));
            for (int i = 0; i < number; ++i) {
                int offset = bytesToInt(subBytes(data, 136 + i * 12, 4));
                int size = bytesToInt(subBytes(data, 140 + i * 12, 4));
                IccTag tag = new IccTag(new String(subBytes(data, 132 + i * 12, 4)),
                        offset, subBytes(data, offset, size), normalizedLut);
                tags.add(tag);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return tags;
    }

    public static IccTag getTag(List<IccTag> tags, String tag) {
        if (tags == null || tag == null || tag.trim().isEmpty()) {
            return null;
        }
        for (IccTag iccTag : tags) {
            if (iccTag.getTag().equals(tag)) {
                return iccTag;
            }
        }
        return null;
    }

    /*
        Encode data
     */
    public static byte[] encodeTags(List<IccTag> tags) {
        try {
            if (tags == null) {
                return null;
            }
            int number = tags.size();
            int size = 132 + 12 * number;
            for (IccTag tag : tags) {
                size += tag.getBytes().length;
                int m = size % 4;
                if (m > 0) {
                    size += 4 - m;
                }
            }
            byte[] data = new byte[size];
            System.arraycopy(intToBytes(size), 0, data, 0, 4);

            System.arraycopy(intToBytes(number), 0, data, 128, 4);
            int offset = 132 + 12 * number;
            for (int i = 0; i < number; ++i) {
                IccTag tag = tags.get(i);
                size = tag.getBytes().length;
                // Item in Tags Table
                byte[] tagBytes = toBytes(tag.getTag());
                System.arraycopy(tagBytes, 0, data, 132 + i * 12, 4);
                byte[] offsetBytes = intToBytes(offset);
                System.arraycopy(offsetBytes, 0, data, 136 + i * 12, 4);
                byte[] sizeBytes = intToBytes(size);
                System.arraycopy(sizeBytes, 0, data, 140 + i * 12, 4);
                // Tag data
                System.arraycopy(tag.getBytes(), 0, data, offset, size);
                offset += size;
                int m = offset % 4;
                if (m > 0) {
                    offset += 4 - m;
                }
            }

            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }


    /*
        Get/Set
     */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getXml() {
        if (xml == null) {
            xml = iccTagsXml(getTags());
        }
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public List<IccTag> getTags() {
        if (tags == null) {
            readTags();
        }
        return tags;
    }

    public void setTags(List<IccTag> tags) {
        this.tags = tags;
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

    public boolean isNormalizeLut() {
        return normalizeLut;
    }

    public void setNormalizeLut(boolean normalizeLut) {
        this.normalizeLut = normalizeLut;
    }

}
