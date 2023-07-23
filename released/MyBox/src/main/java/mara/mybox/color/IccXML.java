package mara.mybox.color;

import java.awt.color.ICC_Profile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.color.IccHeader.renderingIntent;
import static mara.mybox.tools.ByteTools.subBytes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;


/**
 * @Author Mara
 * @CreateDate 2019-5-14 10:21:53
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IccXML {

    public static String iccXML(IccHeader header, IccTags tags) {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            s.append("<IccProfile>\n");

            String headerXml = iccHeaderXml(header.getFields());
            if (headerXml == null) {
                return null;
            }
            s.append(headerXml);

            s.append("\n");

            String tagesXml = iccTagsXml(tags.getTags());
            if (tagesXml == null) {
                return null;
            }
            s.append(tagesXml);

            s.append("</IccProfile>\n");
            return s.toString();

        } catch (Exception e) {
            return null;
        }

    }

    public static String iccHeaderXml(ICC_Profile profile) {
        if (profile == null) {
            return null;
        }
        return iccHeaderXml(subBytes(profile.getData(), 0, 128));
    }

    public static String iccHeaderXml(byte[] header) {
        return iccHeaderXml(IccHeader.iccHeaderFields(header));
    }

    public static String iccHeaderXml(LinkedHashMap<String, IccTag> fields) {
        if (fields == null) {
            return null;
        }
        try {
            StringBuilder s = new StringBuilder();
            s.append(AppValues.Indent).append("<Header>\n");

            for (String key : fields.keySet()) {
                IccTag field = fields.get(key);
                switch (key) {
                    case "ProfileFlagIndependently":
                    case "ProfileFlagMCSSubset":
                    case "DeviceAttributeMatte":
                    case "DeviceAttributeNegative":
                    case "DeviceAttributeBlackOrWhite":
                    case "DeviceAttributePaperBased":
                    case "DeviceAttributeTextured":
                    case "DeviceAttributeIsotropic":
                    case "DeviceAttributeSelfLuminous":
                    case "PCCIlluminantY":
                    case "PCCIlluminantZ":
                        continue;
                    case "CMMType":
                    case "ProfileDeviceClass":
                    case "PrimaryPlatform":
                    case "DeviceManufacturer":
                    case "Creator":
                        String v = (String) field.getValue();
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<").append(key).append(">")
                                .append(v.substring(0, 4)).append("</").append(key).append(">\n");
                        break;
                    case "RenderingIntent":
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<").append(key).append(">")
                                .append(renderingIntent(field.getBytes())).append("</").append(key).append(">\n");
                        break;
                    case "ProfileFlagEmbedded":
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<ProfileFlags>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Embedded>")
                                .append(field.getValue()).append("</Embedded>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Independently>")
                                .append(fields.get("ProfileFlagIndependently").getValue()).append("</Independently>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<MCSSubset>")
                                .append(fields.get("ProfileFlagMCSSubset").getValue()).append("</MCSSubset>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append("</ProfileFlags>\n");
                        break;
                    case "DeviceAttributeTransparency":
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<DeviceAttributes>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Transparency>")
                                .append(field.getValue()).append("</Transparency>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Matte>")
                                .append(fields.get("DeviceAttributeMatte").getValue()).append("</Matte>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Negative>")
                                .append(fields.get("DeviceAttributeNegative").getValue()).append("</Negative>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<BlackOrWhite>")
                                .append(fields.get("DeviceAttributeBlackOrWhite").getValue()).append("</BlackOrWhite>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<PaperBased>")
                                .append(fields.get("DeviceAttributePaperBased").getValue()).append("</PaperBased>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Textured>")
                                .append(fields.get("DeviceAttributeTextured").getValue()).append("</Textured>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Isotropic>")
                                .append(fields.get("DeviceAttributeIsotropic").getValue()).append("</Isotropic>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<SelfLuminous>")
                                .append(fields.get("DeviceAttributeSelfLuminous").getValue()).append("</SelfLuminous>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append("</DeviceAttributes>\n");
                        break;
                    case "PCCIlluminantX":
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<ConnectionSpaceIlluminant>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<X>")
                                .append(field.getValue()).append("</X>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Y>")
                                .append(fields.get("PCCIlluminantY").getValue()).append("</Y>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Z>")
                                .append(fields.get("PCCIlluminantZ").getValue()).append("</Z>\n");
                        s.append(AppValues.Indent).append(AppValues.Indent).append("</ConnectionSpaceIlluminant>\n");
                        break;
                    default:
                        s.append(AppValues.Indent).append(AppValues.Indent).append("<").append(key).append(">")
                                .append(field.getValue()).append("</").append(key).append(">\n");
                        break;
                }
            }

            s.append(AppValues.Indent).append("</Header>\n");
            return s.toString();

        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }

    }

    public static String iccTagsXml(List<IccTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            StringBuilder s = new StringBuilder();
            s.append(AppValues.Indent).append("<Tags>\n");

            for (IccTag tag : tags) {
                s.append(AppValues.Indent).append(AppValues.Indent).append("<").append(tag.getName())
                        .append("  tag=\"").append(tag.getTag()).append("\"  offset=\"")
                        .append(tag.getOffset()).append("\" size=\"").append(tag.getBytes().length);
                if (tag.getType() != null) {
                    s.append("\"  type=\"").append(tag.getType()).append("\">\n");
                    if (tag.getValue() != null) {
                        switch (tag.getType()) {
                            case Text:
                            case Signature: {
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<value>").append(tag.getValue()).append("</value>\n");
                                break;
                            }

                            case MultiLocalizedUnicode: {
                                Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<ASCII length=\"").append(values.get("AsciiLength")).append("\">\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<![CDATA[").append(values.get("Ascii")).append("]]>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</ASCII>\n");

                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<Unicode code=\"").append(values.get("UnicodeCode")).
                                        append("\" length=\"").append(values.get("UnicodeLength")).append("\">\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<![CDATA[").append(values.get("Unicode")).append("]]>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</Unicode>\n");

                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<ScriptCode code=\"").append(values.get("ScriptCodeCode")).
                                        append("\" length=\"").append(values.get("ScriptCodeLength")).append("\">\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<![CDATA[").append(values.get("ScriptCode")).append("]]>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</ScriptCode>\n");

                                break;
                            }

                            case XYZ: {
                                double[][] values = (double[][]) tag.getValue();
                                for (double[] xyz : values) {
                                    s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                            append("<XYZ x=\"").append(xyz[0]).append("\" y=\"").
                                            append(xyz[1]).append("\" z=\"").append(xyz[2]).append("\"/>\n");
                                }
                                break;
                            }

                            case Curve: {
                                double[] values = (double[]) tag.getValue();
                                int count = 1;
                                for (double value : values) {
                                    if (count % 6 == 1) {
                                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent);
                                    }
                                    s.append(value).append("    ");
                                    if (count % 6 == 0 && count < values.length) {
                                        s.append("\n");
                                    }
                                    count++;
                                }
                                s.append("\n");
                                break;
                            }

                            case Measurement: {
                                Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<observer>").append(values.get("observer")).append("</observer>\n");
                                double[] tristimulus = (double[]) values.get("tristimulus");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<tristimulus x=\"").append(tristimulus[0]).append("\" y=\"").
                                        append(tristimulus[1]).append("\" z=\"").append(tristimulus[2]).append("\"/>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<geometry>").append(values.get("geometry")).append("</geometry>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<flare>").append((double) values.get("flare")).append("</flare>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<type>").append(values.get("illuminantType")).append("</type>\n");
                                break;
                            }

                            case ViewingConditions: {
                                Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                double[] illuminant = (double[]) values.get("illuminant");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<illuminant x=\"").append(illuminant[0]).append("\" y=\"").
                                        append(illuminant[1]).append("\" z=\"").append(illuminant[2]).append("\"/>\n");
                                double[] surround = (double[]) values.get("surround");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<surround x=\"").append(surround[0]).append("\" y=\"").
                                        append(surround[1]).append("\" z=\"").append(surround[2]).append("\"/>\n");
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append("<type>").append(values.get("illuminantType")).append("</type>\n");
                                break;
                            }

                            case S15Fixed16Array: {
                                double[] values = (double[]) tag.getValue();
                                for (int i = 0; i < values.length; ++i) {
                                    if (i % 3 == 0) {
                                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent);
                                        if (i > 0 && i < values.length - 1) {
                                            s.append("\n");
                                        }
                                    }
                                    s.append(values[i]).append("   ");
                                }
                                s.append("\n");
                                break;
                            }

                            case DateTime: {
                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                        append(tag.getValue()).append("\n");
                                break;
                            }

                            case LUT: {
                                try {
                                    Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                    s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                            append("<InputChannelsNumber>").append(values.get("InputChannelsNumber")).
                                            append("</InputChannelsNumber>\n");
                                    s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                            append("<OutputChannelsNumber>").append(values.get("OutputChannelsNumber")).
                                            append("</OutputChannelsNumber>\n");
                                    if (values.get("type") != null) {
                                        String type = (String) values.get("type");
                                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                append("<type>").append(type).append("</type>\n");
                                        if (type.equals("lut8") || type.equals("lut16")) {
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<GridPointsNumber>").append(values.get("GridPointsNumber")).
                                                    append("</GridPointsNumber>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<Matrix>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append(values.get("e1")).append(AppValues.Indent).
                                                    append(values.get("e2")).append(AppValues.Indent).
                                                    append(values.get("e3")).append(AppValues.Indent).append("\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append(values.get("e4")).append(AppValues.Indent).
                                                    append(values.get("e5")).append(AppValues.Indent).
                                                    append(values.get("e6")).append(AppValues.Indent).append("\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append(values.get("e7")).append(AppValues.Indent).
                                                    append(values.get("e8")).append(AppValues.Indent).
                                                    append(values.get("e9")).append(AppValues.Indent).append("\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</Matrix>\n");
                                            if (type.equals("lut16")) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                        append("<InputTablesNumber>").append(values.get("InputTablesNumber")).
                                                        append("</InputTablesNumber>\n");
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                        append("<OutputTablesNumber>").append(values.get("OutputTablesNumber")).
                                                        append("</OutputTablesNumber>\n");
                                            }
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<InputTables>\n");
                                            List<List<Double>> InputTables = (List<List<Double>>) values.get("InputTables");
                                            for (List<Double> input : InputTables) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent);
                                                for (Double i : input) {
                                                    s.append(i).append(AppValues.Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("InputTablesTruncated") != null) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</InputTables>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<CLUTTables>\n");
                                            List<List<Double>> CLUTTables = (List<List<Double>>) values.get("CLUTTables");
                                            for (List<Double> GridPoint : CLUTTables) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent);
                                                for (Double p : GridPoint) {
                                                    s.append(p).append(AppValues.Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("CLUTTablesTruncated") != null) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</CLUTTables>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("<OutputTables>\n");
                                            List<List<Double>> OutputTables = (List<List<Double>>) values.get("OutputTables");
                                            for (List<Double> output : OutputTables) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent);
                                                for (Double o : output) {
                                                    s.append(o).append(AppValues.Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("OutputTablesTruncated") != null) {
                                                s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).append("</OutputTables>\n");
                                        } else {
                                            MyBoxLog.debug("OffsetBCurve:" + values.get("OffsetBCurve"));
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<OffsetBCurve>").append(values.get("OffsetBCurve")).append("</OffsetBCurve>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<OffsetMatrix>").append(values.get("OffsetMatrix")).append("</OffsetMatrix>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<OffsetMCurve>").append(values.get("OffsetMCurve")).append("</OffsetMCurve>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<OffsetCLUT>").append(values.get("OffsetCLUT")).append("</OffsetCLUT>\n");
                                            s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                    append("<OffsetACurve>").append(values.get("OffsetACurve")).append("</OffsetACurve>\n");
                                        }
                                    } else {
                                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                                append(" <!-- Not Decoded -->\n");
                                    }
                                } catch (Exception e) {
                                    s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                            append(" <!-- Not Decoded -->\n");
                                }
                                break;
                            }

                        }
                    } else {
                        s.append(AppValues.Indent).append(AppValues.Indent).append(AppValues.Indent).
                                append(" <!-- Not Decoded -->\n");
                    }
                } else {
                    s.append("\"  type=\"Not Decoded\">\n");
                }

                s.append(AppValues.Indent).append(AppValues.Indent).append("</").append(tag.getName()).append(">\n");
            }

            s.append(AppValues.Indent).append("</Tags>\n");

            return s.toString();

        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }

    }

}
