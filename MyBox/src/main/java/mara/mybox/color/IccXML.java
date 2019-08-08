package mara.mybox.color;

import java.awt.color.ICC_Profile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.color.IccHeader.renderingIntent;
import static mara.mybox.tools.ByteTools.subBytes;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.CommonValues.Indent;

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
            s.append(Indent).append("<Header>\n");

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
                        s.append(Indent).append(Indent).append("<").append(key).append(">")
                                .append(v.substring(0, 4)).append("</").append(key).append(">\n");
                        break;
                    case "RenderingIntent":
                        s.append(Indent).append(Indent).append("<").append(key).append(">")
                                .append(renderingIntent(field.getBytes())).append("</").append(key).append(">\n");
                        break;
                    case "ProfileFlagEmbedded":
                        s.append(Indent).append(Indent).append("<ProfileFlags>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Embedded>")
                                .append(field.getValue()).append("</Embedded>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Independently>")
                                .append(fields.get("ProfileFlagIndependently").getValue()).append("</Independently>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<MCSSubset>")
                                .append(fields.get("ProfileFlagMCSSubset").getValue()).append("</MCSSubset>\n");
                        s.append(Indent).append(Indent).append("</ProfileFlags>\n");
                        break;
                    case "DeviceAttributeTransparency":
                        s.append(Indent).append(Indent).append("<DeviceAttributes>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Transparency>")
                                .append(field.getValue()).append("</Transparency>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Matte>")
                                .append(fields.get("DeviceAttributeMatte").getValue()).append("</Matte>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Negative>")
                                .append(fields.get("DeviceAttributeNegative").getValue()).append("</Negative>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<BlackOrWhite>")
                                .append(fields.get("DeviceAttributeBlackOrWhite").getValue()).append("</BlackOrWhite>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<PaperBased>")
                                .append(fields.get("DeviceAttributePaperBased").getValue()).append("</PaperBased>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Textured>")
                                .append(fields.get("DeviceAttributeTextured").getValue()).append("</Textured>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Isotropic>")
                                .append(fields.get("DeviceAttributeIsotropic").getValue()).append("</Isotropic>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<SelfLuminous>")
                                .append(fields.get("DeviceAttributeSelfLuminous").getValue()).append("</SelfLuminous>\n");
                        s.append(Indent).append(Indent).append("</DeviceAttributes>\n");
                        break;
                    case "PCCIlluminantX":
                        s.append(Indent).append(Indent).append("<ConnectionSpaceIlluminant>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<X>")
                                .append(field.getValue()).append("</X>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Y>")
                                .append(fields.get("PCCIlluminantY").getValue()).append("</Y>\n");
                        s.append(Indent).append(Indent).append(Indent).append("<Z>")
                                .append(fields.get("PCCIlluminantZ").getValue()).append("</Z>\n");
                        s.append(Indent).append(Indent).append("</ConnectionSpaceIlluminant>\n");
                        break;
                    default:
                        s.append(Indent).append(Indent).append("<").append(key).append(">")
                                .append(field.getValue()).append("</").append(key).append(">\n");
                        break;
                }
            }

            s.append(Indent).append("</Header>\n");
            return s.toString();

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    public static String iccTagsXml(List<IccTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            StringBuilder s = new StringBuilder();
            s.append(Indent).append("<Tags>\n");

            for (IccTag tag : tags) {
                s.append(Indent).append(Indent).append("<").append(tag.getName())
                        .append("  tag=\"").append(tag.getTag()).append("\"  offset=\"")
                        .append(tag.getOffset()).append("\" size=\"").append(tag.getBytes().length);
                if (tag.getType() != null) {
                    s.append("\"  type=\"").append(tag.getType()).append("\">\n");
                    if (tag.getValue() != null) {
                        switch (tag.getType()) {
                            case Text:
                            case Signature: {
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<value>").append(tag.getValue()).append("</value>\n");
                                break;
                            }

                            case MultiLocalizedUnicode: {
                                Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<ASCII length=\"").append(values.get("AsciiLength")).append("\">\n");
                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                        append("<![CDATA[").append(values.get("Ascii")).append("]]>\n");
                                s.append(Indent).append(Indent).append(Indent).append("</ASCII>\n");

                                s.append(Indent).append(Indent).append(Indent).
                                        append("<Unicode code=\"").append(values.get("UnicodeCode")).
                                        append("\" length=\"").append(values.get("UnicodeLength")).append("\">\n");
                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                        append("<![CDATA[").append(values.get("Unicode")).append("]]>\n");
                                s.append(Indent).append(Indent).append(Indent).append("</Unicode>\n");

                                s.append(Indent).append(Indent).append(Indent).
                                        append("<ScriptCode code=\"").append(values.get("ScriptCodeCode")).
                                        append("\" length=\"").append(values.get("ScriptCodeLength")).append("\">\n");
                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                        append("<![CDATA[").append(values.get("ScriptCode")).append("]]>\n");
                                s.append(Indent).append(Indent).append(Indent).append("</ScriptCode>\n");

                                break;
                            }

                            case XYZ: {
                                double[][] values = (double[][]) tag.getValue();
                                for (double[] xyz : values) {
                                    s.append(Indent).append(Indent).append(Indent).
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
                                        s.append(Indent).append(Indent).append(Indent);
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
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<observer>").append(values.get("observer")).append("</observer>\n");
                                double[] tristimulus = (double[]) values.get("tristimulus");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<tristimulus x=\"").append(tristimulus[0]).append("\" y=\"").
                                        append(tristimulus[1]).append("\" z=\"").append(tristimulus[2]).append("\"/>\n");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<geometry>").append(values.get("geometry")).append("</geometry>\n");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<flare>").append((double) values.get("flare")).append("</flare>\n");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<type>").append(values.get("illuminantType")).append("</type>\n");
                                break;
                            }

                            case ViewingConditions: {
                                Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                double[] illuminant = (double[]) values.get("illuminant");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<illuminant x=\"").append(illuminant[0]).append("\" y=\"").
                                        append(illuminant[1]).append("\" z=\"").append(illuminant[2]).append("\"/>\n");
                                double[] surround = (double[]) values.get("surround");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<surround x=\"").append(surround[0]).append("\" y=\"").
                                        append(surround[1]).append("\" z=\"").append(surround[2]).append("\"/>\n");
                                s.append(Indent).append(Indent).append(Indent).
                                        append("<type>").append(values.get("illuminantType")).append("</type>\n");
                                break;
                            }

                            case S15Fixed16Array: {
                                double[] values = (double[]) tag.getValue();
                                for (int i = 0; i < values.length; i++) {
                                    if (i % 3 == 0) {
                                        s.append(Indent).append(Indent).append(Indent);
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
                                s.append(Indent).append(Indent).append(Indent).
                                        append(tag.getValue()).append("\n");
                                break;
                            }

                            case LUT: {
                                try {
                                    Map<String, Object> values = (Map<String, Object>) tag.getValue();
                                    s.append(Indent).append(Indent).append(Indent).
                                            append("<InputChannelsNumber>").append(values.get("InputChannelsNumber")).
                                            append("</InputChannelsNumber>\n");
                                    s.append(Indent).append(Indent).append(Indent).
                                            append("<OutputChannelsNumber>").append(values.get("OutputChannelsNumber")).
                                            append("</OutputChannelsNumber>\n");
                                    if (values.get("type") != null) {
                                        String type = (String) values.get("type");
                                        s.append(Indent).append(Indent).append(Indent).
                                                append("<type>").append(type).append("</type>\n");
                                        if (type.equals("lut8") || type.equals("lut16")) {
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<GridPointsNumber>").append(values.get("GridPointsNumber")).
                                                    append("</GridPointsNumber>\n");
                                            s.append(Indent).append(Indent).append(Indent).append("<Matrix>\n");
                                            s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                    append(values.get("e1")).append(Indent).
                                                    append(values.get("e2")).append(Indent).
                                                    append(values.get("e3")).append(Indent).append("\n");
                                            s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                    append(values.get("e4")).append(Indent).
                                                    append(values.get("e5")).append(Indent).
                                                    append(values.get("e6")).append(Indent).append("\n");
                                            s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                    append(values.get("e7")).append(Indent).
                                                    append(values.get("e8")).append(Indent).
                                                    append(values.get("e9")).append(Indent).append("\n");
                                            s.append(Indent).append(Indent).append(Indent).append("</Matrix>\n");
                                            if (type.equals("lut16")) {
                                                s.append(Indent).append(Indent).append(Indent).
                                                        append("<InputTablesNumber>").append(values.get("InputTablesNumber")).
                                                        append("</InputTablesNumber>\n");
                                                s.append(Indent).append(Indent).append(Indent).
                                                        append("<OutputTablesNumber>").append(values.get("OutputTablesNumber")).
                                                        append("</OutputTablesNumber>\n");
                                            }
                                            s.append(Indent).append(Indent).append(Indent).append("<InputTables>\n");
                                            List<List<Double>> InputTables = (List<List<Double>>) values.get("InputTables");
                                            for (List<Double> input : InputTables) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent);
                                                for (Double i : input) {
                                                    s.append(i).append(Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("InputTablesTruncated") != null) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(Indent).append(Indent).append(Indent).append("</InputTables>\n");
                                            s.append(Indent).append(Indent).append(Indent).append("<CLUTTables>\n");
                                            List<List<Double>> CLUTTables = (List<List<Double>>) values.get("CLUTTables");
                                            for (List<Double> GridPoint : CLUTTables) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent);
                                                for (Double p : GridPoint) {
                                                    s.append(p).append(Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("CLUTTablesTruncated") != null) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(Indent).append(Indent).append(Indent).append("</CLUTTables>\n");
                                            s.append(Indent).append(Indent).append(Indent).append("<OutputTables>\n");
                                            List<List<Double>> OutputTables = (List<List<Double>>) values.get("OutputTables");
                                            for (List<Double> output : OutputTables) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent);
                                                for (Double o : output) {
                                                    s.append(o).append(Indent);
                                                }
                                                s.append("\n");
                                            }
                                            if (values.get("OutputTablesTruncated") != null) {
                                                s.append(Indent).append(Indent).append(Indent).append(Indent).
                                                        append(" <!-- Truncated -->\n");
                                            }
                                            s.append(Indent).append(Indent).append(Indent).append("</OutputTables>\n");
                                        } else {
                                            logger.debug("OffsetBCurve:" + values.get("OffsetBCurve"));
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<OffsetBCurve>").append(values.get("OffsetBCurve")).append("</OffsetBCurve>\n");
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<OffsetMatrix>").append(values.get("OffsetMatrix")).append("</OffsetMatrix>\n");
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<OffsetMCurve>").append(values.get("OffsetMCurve")).append("</OffsetMCurve>\n");
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<OffsetCLUT>").append(values.get("OffsetCLUT")).append("</OffsetCLUT>\n");
                                            s.append(Indent).append(Indent).append(Indent).
                                                    append("<OffsetACurve>").append(values.get("OffsetACurve")).append("</OffsetACurve>\n");
                                        }
                                    } else {
                                        s.append(Indent).append(Indent).append(Indent).
                                                append(" <!-- Not Decoded -->\n");
                                    }
                                } catch (Exception e) {
                                    s.append(Indent).append(Indent).append(Indent).
                                            append(" <!-- Not Decoded -->\n");
                                }
                                break;
                            }

                        }
                    } else {
                        s.append(Indent).append(Indent).append(Indent).
                                append(" <!-- Not Decoded -->\n");
                    }
                } else {
                    s.append("\"  type=\"Not Decoded\">\n");
                }

                s.append(Indent).append(Indent).append("</").append(tag.getName()).append(">\n");
            }

            s.append(Indent).append("</Tags>\n");

            return s.toString();

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

}
