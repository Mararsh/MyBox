package mara.mybox.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.color.IccTag.TagType;
import static mara.mybox.color.IccXML.iccHeaderXml;
import mara.mybox.tools.ByteTools;
import static mara.mybox.tools.ByteTools.bytesToHex;
import static mara.mybox.tools.ByteTools.bytesToHexFormat;
import static mara.mybox.tools.ByteTools.bytesToInt;
import static mara.mybox.tools.ByteTools.intToBytes;
import static mara.mybox.tools.ByteTools.subBytes;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.CommonValues.Indent;

/**
 * @Author Mara
 * @CreateDate 2019-5-12
 * @Description
 * @License Apache License Version 2.0
 */
public class IccHeader {

    private byte[] header;
    private LinkedHashMap<String, IccTag> fields;
    private boolean isValid;
    private String error;

    public IccHeader(byte[] data) {
        try {
            this.header = ByteTools.subBytes(data, 0, 128);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public LinkedHashMap<String, IccTag> readFields() {
        if (header == null) {
            return null;
        }
        if (fields == null) {
            fields = iccHeaderFields(header);
        }
        return fields;
    }

    public IccTag field(String name) {
        try {
            return getFields().get(name);
        } catch (Exception e) {
            return null;
        }
    }

    public Object value(String name) {
        try {
            IccTag tag = getFields().get(name);
            return tag.getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public String hex(int offset, int size) {
        try {
            return bytesToHexFormat(subBytes(header, offset, size));
        } catch (Exception e) {
            return null;
        }
    }

    public String xml() {
        try {
            return iccHeaderXml(getFields());
        } catch (Exception e) {
            return null;
        }

    }

    public boolean update(byte[] newHeader) {
        if (newHeader == null || newHeader.length != 128) {
            return false;
        }
        LinkedHashMap<String, IccTag> newFields = iccHeaderFields(newHeader);
        if (newFields == null) {
            return false;
        }
        this.header = newHeader;
        fields = newFields;
        return true;
    }

    /*
        Methods based on bytes
     */
    public static LinkedHashMap<String, IccTag> iccHeaderFields(byte[] header) {
        try {
            LinkedHashMap<String, IccTag> fields = new LinkedHashMap();
            fields.put("ProfileSize",
                    new IccTag("ProfileSize", 0, subBytes(header, 0, 4), TagType.Int,
                            bytesToInt(subBytes(header, 0, 4))));
            fields.put("CMMType",
                    new IccTag("CMMType", 4, subBytes(header, 4, 4), TagType.Text,
                            deviceManufacturerDisplay(subBytes(header, 4, 4)), DeviceManufacturers(), true));
            fields.put("ProfileVersion",
                    new IccTag("ProfileVersion", 8, subBytes(header, 8, 2), TagType.Double,
                            Float.parseFloat((int) (header[8]) + "." + (int) (header[9]))));
            fields.put("ProfileDeviceClass",
                    new IccTag("ProfileDeviceClass", 12, subBytes(header, 12, 4), TagType.Text,
                            profileDeviceClassDisplay(subBytes(header, 12, 4)), ProfileDeviceClasses(), false));
            fields.put("ColorSpaceType",
                    new IccTag("ColorSpaceType", 16, subBytes(header, 16, 4), TagType.Text,
                            colorSpaceType(subBytes(header, 16, 4)), ColorSpaceTypes(), true));
            fields.put("PCSType",
                    new IccTag("PCSType", 20, subBytes(header, 20, 4), TagType.Text,
                            new String(subBytes(header, 20, 4)), PCSTypes(), false));
            fields.put("CreateTime",
                    new IccTag("CreateTime", 24, subBytes(header, 24, 12), TagType.Text,
                            IccTagType.dateTimeString(subBytes(header, 24, 12))));
            fields.put("ProfileFile",
                    new IccTag("ProfileFile", 36, subBytes(header, 36, 4), TagType.Text,
                            new String(subBytes(header, 36, 4))));
            fields.put("PrimaryPlatform",
                    new IccTag("PrimaryPlatform", 40, subBytes(header, 40, 4), TagType.Text,
                            primaryPlatformDisplay(subBytes(header, 40, 4)), PrimaryPlatforms(), false));
            boolean[] profileFlags = IccHeader.profileFlags(header[47]);
            byte[] bytes = subBytes(header, 44, 4);
            fields.put("ProfileFlagEmbedded",
                    new IccTag("ProfileFlagEmbedded", 44, bytes, TagType.Boolean,
                            profileFlags[0]));
            fields.put("ProfileFlagIndependently",
                    new IccTag("ProfileFlagIndependently", 44, bytes, TagType.Boolean,
                            !profileFlags[1]));
            fields.put("ProfileFlagMCSSubset",
                    new IccTag("ProfileFlagMCSSubset", 44, bytes, TagType.Boolean,
                            profileFlags[2]));
            fields.put("DeviceManufacturer",
                    new IccTag("DeviceManufacturer", 48, subBytes(header, 48, 4), TagType.Text,
                            deviceManufacturerDisplay(subBytes(header, 48, 4)), DeviceManufacturers(), true));
            fields.put("DeviceModel",
                    new IccTag("DeviceModel", 52, subBytes(header, 52, 4), TagType.Text,
                            new String(subBytes(header, 52, 4))));
            bytes = subBytes(header, 56, 8);
            boolean[] deviceAttributes = IccHeader.deviceAttributes(header[63]);
            fields.put("DeviceAttributeTransparency",
                    new IccTag("DeviceAttributeTransparency", 56, bytes, TagType.Boolean,
                            deviceAttributes[0]));
            fields.put("DeviceAttributeMatte",
                    new IccTag("DeviceAttributeMatte", 56, bytes, TagType.Boolean,
                            deviceAttributes[1]));
            fields.put("DeviceAttributeNegative",
                    new IccTag("DeviceAttributeNegative", 56, bytes, TagType.Boolean,
                            deviceAttributes[2]));
            fields.put("DeviceAttributeBlackOrWhite",
                    new IccTag("DeviceAttributeBlackOrWhite", 56, bytes, TagType.Boolean,
                            deviceAttributes[3]));
            fields.put("DeviceAttributePaperBased",
                    new IccTag("DeviceAttributePaperBased", 56, bytes, TagType.Boolean,
                            !deviceAttributes[4]));
            fields.put("DeviceAttributeTextured",
                    new IccTag("DeviceAttributeTextured", 56, bytes, TagType.Boolean,
                            deviceAttributes[5]));
            fields.put("DeviceAttributeIsotropic",
                    new IccTag("DeviceAttributeIsotropic", 56, bytes, TagType.Boolean,
                            !deviceAttributes[6]));
            fields.put("DeviceAttributeSelfLuminous",
                    new IccTag("DeviceAttributeSelfLuminous", 56, bytes, TagType.Boolean,
                            deviceAttributes[7]));
            fields.put("RenderingIntent",
                    new IccTag("RenderingIntent", 64, subBytes(header, 64, 4), TagType.Text,
                            renderingIntentDisplay(subBytes(header, 64, 4)), RenderingIntents(), true));
            double[] xyz = IccTagType.XYZNumber(subBytes(header, 68, 12));
            fields.put("PCCIlluminantX",
                    new IccTag("PCCIlluminantX", 68, subBytes(header, 68, 4), TagType.Double,
                            xyz[0]));
            fields.put("PCCIlluminantY",
                    new IccTag("PCCIlluminantY", 72, subBytes(header, 72, 4), TagType.Double,
                            xyz[1]));
            fields.put("PCCIlluminantZ",
                    new IccTag("PCCIlluminantZ", 76, subBytes(header, 76, 4), TagType.Double,
                            xyz[2]));
            fields.put("Creator",
                    new IccTag("Creator", 80, subBytes(header, 80, 4), TagType.Text,
                            deviceManufacturerDisplay(subBytes(header, 80, 4)), DeviceManufacturers(), true));
            fields.put("ProfileID",
                    new IccTag("ProfileID", 84, subBytes(header, 84, 16), TagType.Bytes,
                            bytesToHexFormat(subBytes(header, 84, 16))));
            fields.put("SpectralPCS",
                    new IccTag("SpectralPCS", 100, subBytes(header, 100, 4), TagType.Text,
                            new String(subBytes(header, 100, 4))));
            fields.put("SpectralPCSWaveLengthRange",
                    new IccTag("SpectralPCSWaveLengthRange", 104, subBytes(header, 104, 6), TagType.Bytes,
                            bytesToHexFormat(subBytes(header, 104, 6))));
            fields.put("BispectralPCSWaveLengthRange",
                    new IccTag("BispectralPCSWaveLengthRange", 110, subBytes(header, 110, 6), TagType.Bytes,
                            bytesToHexFormat(subBytes(header, 110, 6))));
            fields.put("MCS",
                    new IccTag("MCS", 116, subBytes(header, 116, 4), TagType.Bytes,
                            bytesToHexFormat(subBytes(header, 116, 4))));
            fields.put("ProfileDeviceSubclass",
                    new IccTag("ProfileDeviceSubclass", 120, subBytes(header, 120, 4), TagType.Text,
                            new String(subBytes(header, 120, 4))));
            fields.put("ProfileSubclassVersion",
                    new IccTag("ProfileSubclassVersion", 104, subBytes(header, 104, 6), TagType.Double,
                            Float.parseFloat((int) (header[10]) + "." + (int) (header[11]))));

            return fields;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    /*
        Decode values of Header Fields
     */
    public static boolean[] profileFlags(byte value) {
        boolean[] values = new boolean[3];
        values[0] = (value & 0x01) == 0x01;
        values[1] = (value & 0x02) == 0x02;
        values[2] = (value & 0x04) == 0x04;
        return values;
    }

    public static boolean[] deviceAttributes(byte value) {
        boolean[] values = new boolean[8];
        values[0] = (value & 0x01) == 0x01;
        values[1] = (value & 0x02) == 0x02;
        values[2] = (value & 0x04) == 0x04;
        values[3] = (value & 0x08) == 0x08;
        values[4] = (value & 0x10) == 0x10;
        values[5] = (value & 0x20) == 0x20;
        values[6] = (value & 0x40) == 0x40;
        values[7] = (value & 0x80) == 0x80;
        return values;
    }

    public static String colorSpaceType(byte[] value) {
        String cs = new String(value);
        if (cs.startsWith("nc")) {
            cs = "nc" + bytesToHex(subBytes(value, 2, 2)).toUpperCase();
        }
        return cs;
    }

    public static String renderingIntent(byte[] value) {
        int intv = bytesToInt(value);
        switch (intv) {
            case 0:
                return "Perceptual";
            case 1:
                return "MediaRelativeColorimetric";
            case 2:
                return "Saturation";
            case 3:
                return "ICCAbsoluteColorimetric";
            default:
                return bytesToHex(value);
        }
    }


    /*
        Display of Header Fields
     */
    public static String deviceManufacturerDisplay(byte[] value) {
        return deviceManufacturerDisplay(new String(value));
    }

    public static String deviceManufacturerDisplay(String value) {
        for (String[] item : DeviceManufacturers) {
            if (item[0].equals(value)) {
                return item[0] + Indent + item[1];
            }
        }
        return value;
    }

    public static String profileDeviceClassDisplay(byte[] value) {
        return profileDeviceClassDisplay(new String(value));
    }

    public static String profileDeviceClassDisplay(String value) {
        for (String[] item : ProfileDeviceClasses) {
            if (item[0].equals(value)) {
                return item[0] + Indent + getMessage(item[1]);
            }
        }
        return value;
    }

    public static String primaryPlatformDisplay(byte[] value) {
        return primaryPlatformDisplay(new String(value));
    }

    public static String primaryPlatformDisplay(String value) {
        for (String[] item : PrimaryPlatforms) {
            if (item[0].equals(value)) {
                return item[0] + Indent + item[1];
            }
        }
        return value;
    }

    public static String renderingIntentDisplay(byte[] value) {
        int intv = bytesToInt(value);
        switch (intv) {
            case 0:
                return getMessage("Perceptual");
            case 1:
                return getMessage("MediaRelativeColorimetric");
            case 2:
                return getMessage("Saturation");
            case 3:
                return getMessage("ICCAbsoluteColorimetric");
            default:
                return bytesToHex(value);
        }
    }

    /*
        Encode values of Header Fields
     */
    public static byte[] profileVersion(String value) {
        byte[] bytes = new byte[4];
        try {
            int pos = value.indexOf(".");
            if (pos >= 0) {
                int major = Integer.parseInt(value.substring(0, pos));
                byte[] mbytes = intToBytes(major);
                bytes[0] = mbytes[3];
                int minor = Integer.parseInt(value.substring(pos + 1));
                mbytes = intToBytes(minor);
                bytes[1] = mbytes[3];
            } else {
                int major = Integer.parseInt(value);
                byte[] mbytes = intToBytes(major);
                bytes[0] = mbytes[3];
            }
        } catch (Exception e) {
        }
        return bytes;
    }

    public static byte[] colorSpaceType(String value) {
        if (!value.startsWith("nc")) {
            return IccProfile.first4ASCII(value);
        }
        byte[] bytes = new byte[4];
        try {
            if (value.length() < 6) {
                return bytes;
            }

            byte[] vBytes = IccProfile.toBytes("nc");
            System.arraycopy(vBytes, 0, bytes, 0, 2);
            vBytes = ByteTools.hexToBytes(value.substring(2, 6));
            System.arraycopy(vBytes, 0, bytes, 2, 2);
        } catch (Exception e) {
        }
        return bytes;
    }

    public static byte[] profileFlags(boolean Embedded,
            boolean Independently, boolean MCSSubset) {
        int v = Embedded ? 0x01 : 0;
        v = v | (Independently ? 0 : 0x02);
        v = v | (MCSSubset ? 0x04 : 0);
        return intToBytes(v);
    }

    public static byte[] deviceAttributes(
            boolean Transparency, boolean Matte, boolean Negative, boolean BlackOrWhite,
            boolean PaperBased, boolean Textured, boolean Isotropic, boolean SelfLuminous) {
        int v = Transparency ? 0x01 : 0;
        v = v | (Matte ? 0x02 : 0);
        v = v | (Negative ? 0x04 : 0);
        v = v | (BlackOrWhite ? 0x08 : 0);
        v = v | (PaperBased ? 0 : 0x10);
        v = v | (Textured ? 0x20 : 0);
        v = v | (Isotropic ? 0 : 0x40);
        v = v | (SelfLuminous ? 0x80 : 0);
        byte[] bytes = intToBytes(v);
        byte[] bytes8 = new byte[8];
        System.arraycopy(bytes, 0, bytes8, 4, 4);
        return bytes8;
    }

    public static byte[] RenderingIntent(String value) {
        byte[] bytes = new byte[4];
        if ("Perceptual".equals(value)
                || getMessage("Perceptual").equals(value)) {
            bytes[3] = 0;
        } else if ("MediaRelativeColorimetric".equals(value)
                || getMessage("MediaRelativeColorimetric").equals(value)) {
            bytes[3] = 1;
        } else if ("Saturation".equals(value)
                || getMessage("Saturation").equals(value)) {
            bytes[3] = 2;
        } else if ("ICCAbsoluteColorimetric".equals(value)
                || getMessage("ICCAbsoluteColorimetric").equals(value)) {
            bytes[3] = 3;
        }
        return bytes;
    }

    /*
        Data
     */
    public static List<String> ProfileDeviceClasses() {
        List<String> types = new ArrayList();
        for (String[] item : ProfileDeviceClasses) {
            types.add(item[0] + Indent + getMessage(item[1]));
        }
        return types;
    }

    public static String[][] ProfileDeviceClasses = {
        {"scnr", "InputDeviceProfile"},
        {"mntr", "DisplayDeviceProfile"},
        {"prtr", "OutputDeviceProfile"},
        {"link", "DeviceLinkProfile"},
        {"abst", "AbstractProfile"},
        {"spac", "ColorSpaceConversionProfile"},
        {"nmcl", "NamedColorProfile"},
        {"cenc", "ColorEncodingSpaceProfile"},
        {"mid ", "MultiplexIdentificationProfile"},
        {"mlnk", "MultiplexLinkProfile"},
        {"mvis", "MultiplexVisualizationProfile"}
    };

    public static List<String> PrimaryPlatforms() {
        List<String> types = new ArrayList();
        for (String[] item : PrimaryPlatforms) {
            types.add(item[0] + Indent + item[1]);
        }
        return types;
    }

    public static String[][] PrimaryPlatforms = {
        {"APPL", "Apple Computer Inc."},
        {"MSFT", "Microsoft Corporation"},
        {"SGI ", "Silicon Graphics Inc."},
        {"SUNW", "Sun Microsystems Inc."},
        {"TGNT", "Taligent Inc."}
    };

    public static List<String> ColorSpaceTypes() {
        return Arrays.asList(ColorSpaceTypes);
    }

    public static String[] ColorSpaceTypes = {
        "XYZ ", "Lab ", "Luv ", "YCbr", "Yxy ", "LMS ", "RGB ", "GRAY", "HSV ", "HLS ", "CMYK", "CMY ",
        "2CLR", "3CLR", "4CLR", "5CLR", "6CLR", "7CLR", "8CLR", "9CLR",
        "ACLR", "BCLR", "CCLR", "DCLR", "ECLR", "FCLR"
    };

    public static List<String> PCSTypes() {
        return Arrays.asList(PCSTypes);
    }

    public static String[] PCSTypes = {
        "XYZ ", "Lab ", "    "
    };

    public static List<String> RenderingIntents() {
        return Arrays.asList(RenderingIntents);
    }

    public static String[] RenderingIntents = {
        "Perceptual", "MediaRelativeColorimetric", "Saturation", "ICCAbsoluteColorimetric"
    };

    public static List<String> DeviceManufacturers() {
        List<String> types = new ArrayList();
        for (String[] item : DeviceManufacturers) {
            types.add(item[0] + Indent + item[1]);
        }
        return types;
    }

    public static String[][] DeviceManufacturers = {
        {"4d2p", "Erdt Systems GmbH & Co KG"},
        {"AAMA", "Aamazing Technologies, Inc."},
        {"ACER", "Acer Peripherals"},
        {"ACLT", "Acolyte Color Research"},
        {"ACTI", "Actix Sytems, Inc."},
        {"ADAR", "Adara Technology, Inc."},
        {"ADBE", "Adobe Systems Inc."},
        {"ADI ", "ADI Systems, Inc."},
        {"AGFA", "Agfa Graphics N.V."},
        {"ALMD", "Alps Electric USA, Inc."},
        {"ALPS", "Alps Electric USA, Inc."},
        {"ALWN", "Alwan Color Expertise"},
        {"AMTI", "Amiable Technologies, Inc."},
        {"AOC ", "AOC International (U.S.A), Ltd."},
        {"APAG", "Apago"},
        {"APPL", "Apple Computer Inc."},
        {"AST ", "AST"},
        {"AT&T", "AT&T Computer Systems"},
        {"BAEL", "BARBIERI electronic"},
        {"BRCO", "Barco NV"},
        {"BRKP", "Breakpoint Pty Limited"},
        {"BROT", "Brother Industries, LTD"},
        {"BULL", "Bull"},
        {"BUS ", "Bus Computer Systems"},
        {"C-IT", "C-Itoh"},
        {"CAMR", "Intel Corporation"},
        {"CANO", "Canon, Inc. (Canon Development Americas, Inc.)"},
        {"CARR", "Carroll Touch"},
        {"CASI", "Casio Computer Co., Ltd."},
        {"CBUS", "Colorbus PL"},
        {"CEL ", "Crossfield"},
        {"CELx", "Crossfield"},
        {"CGS ", "CGS Publishing Technologies International GmbH"},
        {"CHM ", "Rochester Robotics"},
        {"CIGL", "Colour Imaging Group, London"},
        {"CITI", "Citizen"},
        {"CL00", "Candela, Ltd."},
        {"CLIQ", "Color IQ"},
        {"CMCO", "Chromaco, Inc."},
        {"CMiX", "CHROMiX"},
        {"COLO", "Colorgraphic Communications Corporation"},
        {"COMP", "COMPAQ Computer Corporation"},
        {"COMp", "Compeq USA/Focus Technology"},
        {"CONR", "Conrac Display Products"},
        {"CORD", "Cordata Technologies, Inc."},
        {"CPQ ", "Compaq Computer Corporation"},
        {"CPRO", "ColorPro"},
        {"CRN ", "Cornerstone"},
        {"CTX ", "CTX International, Inc."},
        {"CVIS", "ColorVision"},
        {"CWC ", "Fujitsu Laboratories, Ltd."},
        {"DARI", "Darius Technology, Ltd."},
        {"DATA", "Dataproducts"},
        {"DCP ", "Dry Creek Photo"},
        {"DCRC", "Digital Contents Resource Center, Chung-Ang University"},
        {"DELL", "Dell Computer Corporation"},
        {"DIC ", "Dainippon Ink and Chemicals"},
        {"DICO", "Diconix"},
        {"DIGI", "Digital"},
        {"DL&C", "Digital Light & Color"},
        {"DPLG", "Doppelganger, LLC"},
        {"DS ", "Dainippon Screen"},
        {"DSOL", "DOOSOL"},
        {"DUPN", "DuPont"},
        {"EPSO", "Epson"},
        {"ESKO", "Esko-Graphics"},
        {"ETRI", "Electronics and Telecommunications Research Institute"},
        {"EVER", "Everex Systems, Inc."},
        {"EXAC", "ExactCODE GmbH"},
        {"Eizo", "EIZO NANAO CORPORATION"},
        {"FALC", "Falco Data Products, Inc."},
        {"FF ", "Fuji Photo Film Co.,LTD"},
        {"FFEI", "FujiFilm Electronic Imaging, Ltd."},
        {"FNRD", "fnord software"},
        {"FORA", "Fora, Inc."},
        {"FORE", "Forefront Technology Corporation"},
        {"FP ", "Fujitsu"},
        {"FPA ", "WayTech Development, Inc."},
        {"FUJI", "Fujitsu"},
        {"FX ", "Fuji Xerox Co., Ltd."},
        {"GCC ", "GCC Technologies, Inc."},
        {"GGSL", "Global Graphics Software Limited"},
        {"GMB ", "Gretagmacbeth"},
        {"GMG ", "GMG GmbH & Co. KG"},
        {"GOLD", "GoldStar Technology, Inc."},
        {"GOOG", "Google"},
        {"GPRT", "Giantprint Pty Ltd"},
        {"GTMB", "Gretagmacbeth"},
        {"GVC ", "WayTech Development, Inc."},
        {"GW2K", "Sony Corporation"},
        {"HCI ", "HCI"},
        {"HDM ", "Heidelberger Druckmaschinen AG"},
        {"HERM", "Hermes"},
        {"HITA", "Hitachi America, Ltd."},
        {"HP ", "Hewlett-Packard"},
        {"HTC ", "Hitachi, Ltd."},
        {"HiTi", "HiTi Digital, Inc."},
        {"IBM ", "IBM Corporation"},
        {"IDNT", "Scitex Corporation, Ltd."},
        {"IEC ", "Hewlett-Packard"},
        {"IIYA", "Iiyama North America, Inc."},
        {"IKEG", "Ikegami Electronics, Inc."},
        {"IMAG", "Image Systems Corporation"},
        {"IMI ", "Ingram Micro, Inc."},
        {"INTC", "Intel Corporation"},
        {"INTL", "N/A (INTL)"},
        {"INTR", "Intra Electronics USA, Inc."},
        {"IOCO", "Iocomm International Technology Corporation"},
        {"IPS ", "InfoPrint Solutions Company"},
        {"IRIS", "Scitex Corporation, Ltd."},
        {"ISL ", "Ichikawa Soft Laboratory"},
        {"ITNL", "N/A (ITNL)"},
        {"IVM ", "IVM"},
        {"IWAT", "Iwatsu Electric Co., Ltd."},
        {"Idnt", "Scitex Corporation, Ltd."},
        {"Inca", "Inca Digital Printers Ltd."},
        {"Iris", "Scitex Corporation, Ltd."},
        {"JPEG", "Joint Photographic Experts Group"},
        {"JSFT", "Jetsoft Development"},
        {"JVC ", "JVC Information Products Co."},
        {"KART", "Scitex Corporation, Ltd."},
        {"KFC ", "KFC Computek Components Corporation"},
        {"KLH ", "KLH Computers"},
        {"KMHD", "Konica Minolta Holdings, Inc."},
        {"KNCA", "Konica Corporation"},
        {"KODA", "Kodak"},
        {"KYOC", "Kyocera"},
        {"Kart", "Scitex Corporation, Ltd."},
        {"LCAG", "Leica Camera AG"},
        {"LCCD", "Leeds Colour"},
        {"LDAK", "Left Dakota"},
        {"LEAD", "Leading Technology, Inc."},
        {"LEXM", "Lexmark International, Inc."},
        {"LINK", "Link Computer, Inc."},
        {"LINO", "Linotronic"},
        {"LITE", "Lite-On, Inc."},
        {"Leaf", "Leaf"},
        {"Lino", "Linotronic"},
        {"MAGC", "Mag Computronic (USA) Inc."},
        {"MAGI", "MAG Innovision, Inc."},
        {"MANN", "Mannesmann"},
        {"MICN", "Micron Technology, Inc."},
        {"MICR", "Microtek"},
        {"MICV", "Microvitec, Inc."},
        {"MINO", "Minolta"},
        {"MITS", "Mitsubishi Electronics America, Inc."},
        {"MITs", "Mitsuba Corporation"},
        {"MNLT", "Minolta"},
        {"MODG", "Modgraph, Inc."},
        {"MONI", "Monitronix, Inc."},
        {"MONS", "Monaco Systems Inc."},
        {"MORS", "Morse Technology, Inc."},
        {"MOTI", "Motive Systems"},
        {"MSFT", "Microsoft Corporation"},
        {"MUTO", "MUTOH INDUSTRIES LTD."},
        {"Mits", "Mitsubishi Electric Corporation Kyoto Works"},
        {"NANA", "NANAO USA Corporation"},
        {"NEC ", "NEC Corporation"},
        {"NEXP", "NexPress Solutions LLC"},
        {"NISS", "Nissei Sangyo America, Ltd."},
        {"NKON", "Nikon Corporation"},
        {"NONE", "none"},
        {"OCE ", "Oce Technologies B.V."},
        {"OCEC", "OceColor"},
        {"OKI ", "Oki"},
        {"OKID", "Okidata"},
        {"OKIP", "Okidata"},
        {"OLIV", "Olivetti"},
        {"OLYM", "OLYMPUS OPTICAL CO., LTD"},
        {"ONYX", "Onyx Graphics"},
        {"OPTI", "Optiquest"},
        {"PACK", "Packard Bell"},
        {"PANA", "Matsushita Electric Industrial Co., Ltd."},
        {"PANT", "Pantone, Inc."},
        {"PBN ", "Packard Bell"},
        {"PFU ", "PFU Limited"},
        {"PHIL", "Philips Consumer Electronics Co."},
        {"PNTX", "HOYA Corporation PENTAX Imaging Systems Division"},
        {"POne", "Phase One A/S"},
        {"PREM", "Premier Computer Innovations"},
        {"PRIN", "Princeton Graphic Systems"},
        {"PRIP", "Princeton Publishing Labs"},
        {"QLUX", "Hong Kong"},
        {"QMS ", "QMS, Inc."},
        {"QPCD", "QPcard AB"},
        {"QUAD", "QuadLaser"},
        {"QUME", "Qume Corporation"},
        {"RADI", "Radius, Inc."},
        {"RDDx", "Integrated Color Solutions, Inc."},
        {"RDG ", "Roland DG Corporation"},
        {"REDM", "REDMS Group, Inc."},
        {"RELI", "Relisys"},
        {"RGMS", "Rolf Gierling Multitools"},
        {"RICO", "Ricoh Corporation"},
        {"RNLD", "Edmund Ronald"},
        {"ROYA", "Royal"},
        {"RPC ", "Ricoh Printing Systems,Ltd."},
        {"RTL ", "Royal Information Electronics Co., Ltd."},
        {"SAMP", "Sampo Corporation of America"},
        {"SAMS", "Samsung, Inc."},
        {"SANT", "Jaime Santana Pomares"},
        {"SCIT", "Scitex Corporation, Ltd."},
        {"SCRN", "Dainippon Screen"},
        {"SDP ", "Scitex Corporation, Ltd."},
        {"SEC ", "SAMSUNG ELECTRONICS CO.,LTD"},
        {"SEIK", "Seiko Instruments U.S.A., Inc."},
        {"SEIk", "Seikosha"},
        {"SGUY", "ScanGuy.com"},
        {"SHAR", "Sharp Laboratories"},
        {"SICC", "International Color Consortium"},
        {"SONY", "SONY Corporation"},
        {"SPCL", "SpectraCal"},
        {"STAR", "Star"},
        {"STC ", "Sampo Technology Corporation"},
        {"Scit", "Scitex Corporation, Ltd."},
        {"Sdp ", "Scitex Corporation, Ltd."},
        {"Sony", "Sony Corporation"},
        {"TALO", "Talon Technology Corporation"},
        {"TAND", "Tandy"},
        {"TATU", "Tatung Co. of America, Inc."},
        {"TAXA", "TAXAN America, Inc."},
        {"TDS ", "Tokyo Denshi Sekei K.K."},
        {"TECO", "TECO Information Systems, Inc."},
        {"TEGR", "Tegra"},
        {"TEKT", "Tektronix, Inc."},
        {"TI ", "Texas Instruments"},
        {"TMKR", "TypeMaker Ltd."},
        {"TOSB", "TOSHIBA corp."},
        {"TOSH", "Toshiba, Inc."},
        {"TOTK", "TOTOKU ELECTRIC Co., LTD"},
        {"TRIU", "Triumph"},
        {"TSBT", "TOSHIBA TEC CORPORATION"},
        {"TTX ", "TTX Computer Products, Inc."},
        {"TVM ", "TVM Professional Monitor Corporation"},
        {"TW ", "TW Casper Corporation"},
        {"ULSX", "Ulead Systems"},
        {"UNIS", "Unisys"},
        {"UTZF", "Utz Fehlau & Sohn"},
        {"VARI", "Varityper"},
        {"VIEW", "Viewsonic"},
        {"VISL", "Visual communication"},
        {"VIVO", "Vivo Mobile Communication Co., Ltd"},
        {"WANG", "Wang"},
        {"WLBR", "Wilbur Imaging"},
        {"WTG2", "Ware To Go"},
        {"WYSE", "WYSE Technology"},
        {"XERX", "Xerox Corporation"},
        {"XRIT", "X-Rite"},
        {"Z123", "Lavanya's test Company"},
        {"ZRAN", "Zoran Corporation"},
        {"Zebr", "Zebra Technologies Inc"},
        {"appl", "Apple Computer Inc."},
        {"bICC", "basICColor GmbH"},
        {"berg", "bergdesign incorporated"},
        {"ceyd", "Integrated Color Solutions, Inc."},
        {"clsp", "MacDermid ColorSpan, Inc."},
        {"ds ", "Dainippon Screen"},
        {"dupn", "DuPont"},
        {"ffei", "FujiFilm Electronic Imaging, Ltd."},
        {"flux", "FluxData Corporation"},
        {"iris", "Scitex Corporation, Ltd."},
        {"kart", "Scitex Corporation, Ltd."},
        {"lcms", "Little CMS"},
        {"lino", "Linotronic"},
        {"none", "none"},
        {"ob4d", "Erdt Systems GmbH & Co KG"},
        {"obic", "Medigraph GmbH"},
        {"quby", "Qubyx Sarl"},
        {"scit", "Scitex Corporation, Ltd."},
        {"scrn", "Dainippon Screen"},
        {"sdp ", "Scitex Corporation, Ltd."},
        {"siwi", "SIWI GRAFIKA CORPORATION"},
        {"yxym", "YxyMaster GmbH"}
    };


    /*
        Get/Set
     */
    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public LinkedHashMap<String, IccTag> getFields() {
        if (fields == null) {
            readFields();
        }
        return fields;
    }

    public void setFields(LinkedHashMap<String, IccTag> fields) {
        this.fields = fields;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
