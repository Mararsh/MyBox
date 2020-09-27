package mara.mybox.color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static mara.mybox.color.IccTagType.XYZ;
import static mara.mybox.color.IccTagType.XYZDoubles;
import static mara.mybox.color.IccTagType.XYZNumber;
import static mara.mybox.color.IccTagType.XYZNumberDoubles;
import static mara.mybox.color.IccTagType.curve;
import static mara.mybox.color.IccTagType.curveDoubles;
import static mara.mybox.color.IccTagType.dateTime;
import static mara.mybox.color.IccTagType.geometryType;
import static mara.mybox.color.IccTagType.illuminantType;
import static mara.mybox.color.IccTagType.lut;
import static mara.mybox.color.IccTagType.measurement;
import static mara.mybox.color.IccTagType.multiLocalizedUnicode;
import static mara.mybox.color.IccTagType.observerType;
import static mara.mybox.color.IccTagType.s15Fixed16Array;
import static mara.mybox.color.IccTagType.s15Fixed16ArrayDoubles;
import static mara.mybox.color.IccTagType.signature;
import static mara.mybox.color.IccTagType.text;
import static mara.mybox.color.IccTagType.u16Fixed16Number;
import static mara.mybox.color.IccTagType.viewingConditions;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.CommonValues.Indent;

/**
 * @Author Mara
 * @CreateDate 2019-5-7 16:11:08
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IccTag {

    private String tag, name, description;
    private int offset, size;
    private Object value;
    private byte[] bytes;
    private TagType type;
    private List<String> valueSelection, arrayNames;
    private boolean isHeaderField, editable, selectionEditable, changed, first4ascii, normalizeLut;

    public static enum TagType {
        Text, MultiLocalizedUnicode, Int, Double, XYZ, Curve, ViewingConditions,
        Measurement, Signature, S15Fixed16Array, DateTime, LUT, Boolean, Bytes, BooleanArray
    }

    public IccTag(String tag, int offset, byte[] bytes, boolean normalizeLut) {
        this.tag = tag;
        this.offset = offset;
        this.bytes = bytes;
        this.normalizeLut = normalizeLut;
        name = tagName(tag);
        description = tagDescription(name);
        type = tagType(tag);
        value = tagValue(this.type, bytes, normalizeLut);
        valueSelection = tagSelection(tag);
        changed = false;
        isHeaderField = false;
        selectionEditable = false;
        editable = false;
    }

    public IccTag(String tag, int offset, byte[] bytes,
            TagType type, Object value) {
        this.tag = tag;
        this.offset = offset;
        this.bytes = bytes;
        this.name = tag;
        this.description = tag;
        this.type = type;
        this.value = value;
        this.valueSelection = null;
        this.selectionEditable = false;
        this.isHeaderField = true;
        this.editable = true;
        changed = false;
    }

    public IccTag(String tag, int offset, byte[] bytes,
            TagType type, Object value,
            List<String> valueSelection, boolean selectionEditable) {
        this.tag = tag;
        this.offset = offset;
        this.bytes = bytes;
        this.name = tag;
        this.description = tag;
        this.type = type;
        this.value = value;
        this.valueSelection = valueSelection;
        this.selectionEditable = selectionEditable;
        this.isHeaderField = true;
        this.editable = true;
        changed = false;
    }

    public String display() {
        try {
            if (valueSelection != null) {
                String v = (String) value;
                for (String s : valueSelection) {
                    if (s.startsWith(v)) {
                        return s;
                    }
                }
            }
            return tagDisplay(type, value);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean update(String newValue) {
        return update(this, newValue);
    }

    public boolean update(String key, String newValue) {
        if (key == null) {
            return update(newValue);
        }
        switch (type) {
            case ViewingConditions:
                switch (key.toLowerCase()) {
                    case "illuminant":
                        return updateViewingConditionsIlluminant(this, newValue);
                    case "surround":
                        return updateViewingConditionsSurround(this, newValue);
                    case "illuminanttype":
                        return updateViewingConditionsType(this, newValue);
                }
            case Measurement:
                switch (key.toLowerCase()) {
                    case "observer":
                        return updateMeasurementObserver(this, newValue);
                    case "tristimulus":
                        return updateMeasurementTristimulus(this, newValue);
                    case "geometry":
                        return updateMeasurementGeometry(this, newValue);
                    case "flare":
                        return updateMeasurementFlare(this, newValue);
                    case "illuminanttype":
                        return updateMeasurementType(this, newValue);
                }
        }
        return update(this, newValue);
    }


    /*
        Decode tag
     */
    public static String tagName(String tag) {
        for (String[] item : tagNames) {
            if (item[0].equals(tag)) {
                return item[1];
            }
        }
        return tag;
    }

    public static String tagDescription(String tagName) {
        for (String[] item : tagDescriptions) {
            if (item[0].equals(tagName)) {
                return item[1];
            }
        }
        return "";
    }

    public static TagType tagType(String tag) {
        if (tag == null) {
            return null;
        }
        switch (tag) {
            case "desc":
            case "dmnd":
            case "dmdd":
            case "vued":
                return TagType.MultiLocalizedUnicode;
            case "cprt":
                return TagType.Text;
            case "wtpt":
            case "bkpt":
            case "rXYZ":
            case "gXYZ":
            case "bXYZ":
            case "lumi":
                return TagType.XYZ;
            case "rTRC":
            case "gTRC":
            case "bTRC":
            case "kTRC":
                return TagType.Curve;
            case "view":
                return TagType.ViewingConditions;
            case "meas":
                return TagType.Measurement;
            case "tech":
                return TagType.Signature;
            case "chad":
                return TagType.S15Fixed16Array;
            case "calt":
                return TagType.DateTime;
            case "A2B0":
            case "A2B1":
            case "A2B2":
            case "B2A0":
            case "B2A1":
            case "B2A2":
            case "gamt":
                return TagType.LUT;
        }

        return null;
    }

    public static Object tagValue(TagType type, byte[] bytes, boolean normalizeLut) {
        if (type == null || bytes == null) {
            return null;
        }
        switch (type) {
            case MultiLocalizedUnicode:
                return multiLocalizedUnicode(bytes);
            case Text:
                return text(bytes);
            case XYZ:
                return XYZ(bytes);
            case Curve:
                return curve(bytes);
            case ViewingConditions:
                return viewingConditions(bytes);
            case Measurement:
                return measurement(bytes);
            case Signature:
                return signature(bytes);
            case S15Fixed16Array:
                return s15Fixed16Array(bytes);
            case DateTime:
                return dateTime(bytes);
            case LUT:
                return lut(bytes, normalizeLut);
        }

        return null;
    }

    public static List<String> tagSelection(String tag) {
        switch (tag) {
            case "tech":
                return technologyTypes();
        }
        return null;
    }

    public static String tagDisplay(TagType type, Object value) {
        if (type == null || value == null) {
            return "";
        }
        String display;
        try {
            switch (type) {
                case Text:
                case Signature:
                case DateTime:
                    display = value + "";
                    break;

                case MultiLocalizedUnicode:
                    display = IccTagType.textDescriptionDisplay((Map<String, Object>) value);
                    break;

                case XYZ:
                    display = IccTagType.XYZNumberDisplay((double[][]) value);
                    break;

                case Curve:
                    display = IccTagType.curveDisplay((double[]) value);
                    break;

                case ViewingConditions:
                    display = IccTagType.viewingConditionsDisplay((Map<String, Object>) value);
                    break;

                case Measurement:
                    display = IccTagType.measurementDisplay((Map<String, Object>) value);
                    break;

                case S15Fixed16Array:
                    display = IccTagType.s15Fixed16ArrayDisplay((double[]) value);
                    break;

                case LUT:
                    display = IccTagType.lutDisplay((Map<String, Object>) value);
                    break;

                default:
                    display = value + "";
            }

        } catch (Exception e) {
            display = value + "";
        }
        return display;
    }

    /*
        Encode tag
     */
    public static boolean update(IccTag tag, String newValue) {
        try {
            byte[] bytes = null;
            Object value = null;
            switch (tag.getType()) {
                case Text: {
                    value = newValue;
                    bytes = text(newValue);
                    break;
                }

                case MultiLocalizedUnicode:
                    bytes = multiLocalizedUnicode(tag, newValue);
                    value = multiLocalizedUnicode(bytes);
                    break;

                case Signature:
                    bytes = signature(newValue);
                    value = signature(bytes);
                    break;

                case DateTime:
                    bytes = dateTime(newValue);
                    value = dateTime(bytes);
                    break;

                case XYZ: {
                    double[][] doubles = XYZDoubles(newValue);
                    bytes = XYZ(doubles);
                    value = doubles;
                    break;
                }

                case Curve: {
                    double[] doubles = curveDoubles(newValue);
                    bytes = curve(doubles);
                    value = doubles;
                    break;
                }

                case S15Fixed16Array: {
                    double[] doubles = s15Fixed16ArrayDoubles(newValue);
                    bytes = s15Fixed16Array(doubles);
                    value = doubles;
                    break;
                }

            }
            if (value == null || bytes == null) {
                return false;
            }
            tag.setValue(value);
            tag.setBytes(bytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateViewingConditionsIlluminant(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.ViewingConditions) {
                return false;
            }
            double[] doubles = XYZNumberDoubles(newValue);
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("illuminant", doubles);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] illuminantBytes = XYZNumber(doubles);
            System.arraycopy(illuminantBytes, 0, tagBytes, 8, 12);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateViewingConditionsSurround(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.ViewingConditions) {
                return false;
            }
            double[] doubles = XYZNumberDoubles(newValue);
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("surround", doubles);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] surroundBytes = XYZNumber(doubles);
            System.arraycopy(surroundBytes, 0, tagBytes, 20, 12);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateViewingConditionsType(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.ViewingConditions) {
                return false;
            }
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("illuminantType", newValue);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] typeBytes = illuminantType(newValue);
            System.arraycopy(typeBytes, 0, tagBytes, 32, 4);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMeasurementObserver(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.Measurement) {
                return false;
            }
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("observer", newValue);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] observerBytes = observerType(newValue);
            System.arraycopy(observerBytes, 0, tagBytes, 8, 4);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMeasurementTristimulus(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.Measurement) {
                return false;
            }
            double[] doubles = XYZNumberDoubles(newValue);
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("tristimulus", doubles);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] tristimulusBytes = XYZNumber(doubles);
            System.arraycopy(tristimulusBytes, 0, tagBytes, 12, 12);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMeasurementGeometry(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.Measurement) {
                return false;
            }
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("geometry", newValue);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] geometryBytes = geometryType(newValue);
            System.arraycopy(geometryBytes, 0, tagBytes, 24, 4);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMeasurementFlare(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.Measurement) {
                return false;
            }
            double flare = Double.parseDouble(newValue);
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("flare", flare);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] flareBytes = u16Fixed16Number(flare);
            System.arraycopy(flareBytes, 0, tagBytes, 28, 4);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMeasurementType(IccTag tag, String newValue) {
        try {
            if (tag.getType() != TagType.Measurement) {
                return false;
            }
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            values.put("illuminantType", newValue);
            tag.setValue(values);

            byte[] tagBytes = tag.getBytes();
            byte[] typeBytes = illuminantType(newValue);
            System.arraycopy(typeBytes, 0, tagBytes, 32, 4);
            tag.setBytes(tagBytes);

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    /*
        Values
     */
    // https://sno.phy.queensu.ca/~phil/exiftool/TagNames/ICC_Profile.html
    public static String[][] tagNames = {
        {"A2B0", "AToB0"},
        {"A2B1", "AToB1"},
        {"A2B2", "AToB2"},
        {"B2A0", "BToA0"},
        {"B2A1", "BToA1"},
        {"B2A2", "BToA2"},
        {"B2D0", "BToD0"},
        {"B2D1", "BToD1"},
        {"B2D2", "BToD2"},
        {"B2D3", "BToD3"},
        {"D2B0", "DToB0"},
        {"D2B1", "DToB1"},
        {"D2B2", "DToB2"},
        {"D2B3", "DToB3"},
        {"Header", "ProfileHeader"},
        {"MS00", "WCSProfiles"},
        {"bTRC", "BlueTRC"},
        {"bXYZ", "BlueMatrixColumn"},
        {"bfd", "UCRBG"},
        {"bkpt", "MediaBlackPoint"},
        {"calt", "CalibrationDateTime"},
        {"chad", "ChromaticAdaptation"},
        {"chrm", "Chromaticity"},
        {"ciis", "ColorimetricIntentImageState"},
        {"clot", "ColorantTableOut"},
        {"clro", "ColorantOrder"},
        {"clrt", "ColorantTable"},
        {"cprt", "ProfileCopyright"},
        {"crdi", "CRDInfo"},
        {"desc", "ProfileDescription"},
        {"devs", "DeviceSettings"},
        {"dmdd", "DeviceModelDesc"},
        {"dmnd", "DeviceMfgDesc"},
        {"dscm", "ProfileDescriptionML"},
        {"fpce", "FocalPlaneColorimetryEstimates"},
        {"gTRC", "GreenTRC"},
        {"gXYZ", "GreenMatrixColumn"},
        {"gamt", "Gamut"},
        {"kTRC", "GrayTRC"},
        {"lumi", "Luminance"},
        {"meas", "Measurement"},
        {"meta", "Metadata"},
        {"mmod", "MakeAndModel"},
        {"ncl2", "NamedColor2"},
        {"ncol", "NamedColor"},
        {"ndin", "NativeDisplayInfo"},
        {"pre0", "Preview0"},
        {"pre1", "Preview1"},
        {"pre2", "Preview2"},
        {"ps2i", "PS2RenderingIntent"},
        {"ps2s", "PostScript2CSA"},
        {"psd0", "PostScript2CRD0"},
        {"psd1", "PostScript2CRD1"},
        {"psd2", "PostScript2CRD2"},
        {"psd3", "PostScript2CRD3"},
        {"pseq", "ProfileSequenceDesc"},
        {"psid", "ProfileSequenceIdentifier"},
        {"psvm", "PS2CRDVMSize"},
        {"rTRC", "RedTRC"},
        {"rXYZ", "RedMatrixColumn"},
        {"resp", "OutputResponse"},
        {"rhoc", "ReflectionHardcopyOrigColorimetry"},
        {"rig0", "PerceptualRenderingIntentGamut"},
        {"rig2", "SaturationRenderingIntentGamut"},
        {"rpoc", "ReflectionPrintOutputColorimetry"},
        {"sape", "SceneAppearanceEstimates"},
        {"scoe", "SceneColorimetryEstimates"},
        {"scrd", "ScreeningDesc"},
        {"scrn", "Screening"},
        {"targ", "CharTarget"},
        {"tech", "Technology"},
        {"vcgt", "VideoCardGamma"},
        {"view", "ViewingConditions"},
        {"vued", "ViewingCondDesc"},
        {"wtpt", "MediaWhitePoint"}
    };

    public static String[][] tagDescriptions = {
        {"AToB0", "Multi-dimensional transformation structure"},
        {"AToB1", "Multi-dimensional transformation structure"},
        {"AToB2", "Multi-dimensional transformation structure"},
        {"BlueMatrixColumn", "The third column in the matrix used in matrix/TRC transforms. (This column is combined with the linear blue channel during the matrix multiplication)."},
        {"BlueTRC", "Blue channel tone reproduction curve"},
        {"BToA0", "Multi-dimensional transformation structure"},
        {"BToA1", "Multi-dimensional transformation structure"},
        {"BToA2", "Multi-dimensional transformation structure"},
        {"BToD0", "Multi-dimensional transformation structure"},
        {"BToD1", "Multi-dimensional transformation structure"},
        {"BToD2", "Multi-dimensional transformation structure"},
        {"BToD3", "Multi-dimensional transformation structure"},
        {"CalibrationDateTime", "Profile calibration date and time"},
        {"CharTarget", "Characterization target such as IT8/7.2"},
        {"ChromaticAdaptation", "Converts an nCIEXYZ colour relative to the actual adopted white to the nCIEXYZ colour relative to the PCS adopted white. Required only if the chromaticity of the actual adopted white is different from that of the PCS adopted white."},
        {"Chromaticity", "Set of phosphor/colorant chromaticity"},
        {"ColorantOrder", "Identifies the laydown order of colorants"},
        {"ColorantTable", "Identifies the colorants used in the profile. Required for N-component based Output profiles and DeviceLink profiles only if the data colour space field is xCLR (e.g. 3CLR)"},
        {"ColorantTableOut", "Identifies the output colorants used in the profile, required only if the PCS Field is xCLR (e.g. 3CLR)"},
        {"ColorimetricIntentImageState", "Indicates the image state of PCS colorimetry produced using the colorimetric intent transforms"},
        {"ProfileCopyright", "Profile copyright information"},
        {"DeviceMfgDesc", "Displayable description of device manufacturer"},
        {"DeviceModelDesc", "Displayable description of device model"},
        {"DToB0", "Multi-dimensional transformation structure"},
        {"DToB1", "Multi-dimensional transformation structure"},
        {"DToB2", "Multi-dimensional transformation structure"},
        {"DToB3", "Multi-dimensional transformation structure"},
        {"Gamut", "Out of gamut: 8-bit or 16-bit data"},
        {"GrayTRC", "Grey tone reproduction curve"},
        {"GreenMatrixColumn", "The second column in the matrix used in matrix/TRC transforms (This column is combined with the linear green channel during the matrix multiplication)."},
        {"GreenTRC", "Green channel tone reproduction curve"},
        {"Luminance", "Absolute luminance for emissive device"},
        {"Measurement", "Alternative measurement specification information"},
        {"MediaBlackPoint", "nCIEXYZ of Media black point"},
        {"MediaWhitePoint", "nCIEXYZ of media white point"},
        {"NamedColor2", "PCS and optional device representation for named colours"},
        {"OutputResponse", "Description of the desired device response"},
        {"PerceptualRenderingIntentGamut", "When present, the specified gamut is defined to be the reference medium gamut for the PCS side of both the A2B0 and B2A0 tags"},
        {"Preview0", "Preview transformation: 8-bit or 16-bit data"},
        {"Preview1", "Preview transformation: 8-bit or 16-bit data"},
        {"Preview2", "Preview transformation: 8-bit or 16-bit data"},
        {"ProfileDescription", "Structure containing invariant and localizable versions of the profile name for displays"},
        {"ProfileSequenceDesc", "An array of descriptions of the profile sequence"},
        {"RedMatrixColumn", "The first column in the matrix used in matrix/TRC transforms. (This column is combined with the linear red channel during the matrix multiplication)."},
        {"RedTRC", "Red channel tone reproduction curve"},
        {"SaturationRenderingIntentGamut", "When present, the specified gamut is defined to be the reference medium gamut for the PCS side of both the A2B2 and B2A2 tags"},
        {"Technology", "Device technology information such as LCD, CRT, Dye Sublimation, etc."},
        {"ViewingCondDesc", "Viewing condition description"},
        {"ViewingConditions", "Viewing condition parameters"}
    };

    public static String[][] Technology = {
        {"fscn", "Film scanner"},
        {"dcam", "Digital camera"},
        {"rscn", "Reflective scanner"},
        {"ijet", "Ink jet printer"},
        {"twax", "Thermal wax printer"},
        {"epho", "Electrophotographic printer"},
        {"esta", "Electrostatic printer"},
        {"dsub", "Dye sublimation printer"},
        {"rpho", "Photographic paper printer"},
        {"fprn", "Film writer"},
        {"vidm", "Video monitor"},
        {"vidc", "Video camera"},
        {"pjtv", "Projection television"},
        {"CRT ", "Cathode ray tube display"},
        {"PMD ", "Passive matrix display"},
        {"AMD ", "Active matrix display"},
        {"KPCD", "Photo CD"},
        {"imgs", "Photographic image setter"},
        {"grav", "Gravure"},
        {"offs", "Offset lithography"},
        {"silk", "Silkscreen"},
        {"flex", "Flexography"},
        {"mpfs", "Motion picture film scanner"},
        {"mpfr", "Motion picture film recorder"},
        {"dmpc", "Digital motion picture camera"},
        {"dcpj", "Digital cinema projector"}
    };

    public static String technology(String value) {
        for (String[] item : Technology) {
            if (item[0].equals(value)) {
                return item[0] + Indent + item[1];
            }
        }
        return value;
    }

    public static List<String> technologyTypes() {
        List<String> types = new ArrayList<>();
        for (String[] item : Technology) {
            types.add(item[0] + Indent + item[1]);
        }
        return types;
    }

    /*
        Set/Get
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @SuppressWarnings("unchecked")
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    public List<String> getValueSelection() {
        return valueSelection;
    }

    public void setValueSelection(List<String> valueSelection) {
        this.valueSelection = valueSelection;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isIsHeaderField() {
        return isHeaderField;
    }

    public void setIsHeaderField(boolean isHeaderField) {
        this.isHeaderField = isHeaderField;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isSelectionEditable() {
        return selectionEditable;
    }

    public void setSelectionEditable(boolean selectionEditable) {
        this.selectionEditable = selectionEditable;
    }

    public List<String> getArrayNames() {
        return arrayNames;
    }

    public void setArrayNames(List<String> arrayNames) {
        this.arrayNames = arrayNames;
    }

    public int getSize() {
        size = bytes.length;
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
