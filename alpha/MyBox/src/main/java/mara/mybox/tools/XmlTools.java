package mara.mybox.tools;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.dev.MyBoxLog;
import org.w3c.dom.Document;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @Author Mara
 * @CreateDate 2019-5-9 10:31:34
 * @License Apache License Version 2.0
 */
public class XmlTools {

    public static void iccHeaderXml(LinkedHashMap<String, Object> header, File file) {
        if (header == null || file == null) {
            return;
        }
        try {
            SAXTransformerFactory sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = sf.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Result result = new StreamResult(new BufferedOutputStream(new FileOutputStream(file)));
            handler.setResult(result);
            handler.startDocument();
            AttributesImpl attr = new AttributesImpl();
            handler.startElement("", "", "IccProfile", attr);
            handler.startElement("", "", "Header", attr);

            handler.startElement("", "", "PreferredCMMType", attr);
            String stringV = (String) header.get("CMMType");
            handler.characters(stringV.toCharArray(), 0, stringV.length());
            handler.endElement("", "", "PreferredCMMType");

            handler.startElement("", "", "PCSIlluminant", attr);
            attr.clear();
            attr.addAttribute("", "", "X", "", header.get("x") + "");
            attr.addAttribute("", "", "Y", "", header.get("y") + "");
            attr.addAttribute("", "", "Z", "", header.get("z") + "");
            handler.startElement("", "", "XYZNumber", attr);
            handler.endElement("", "", "XYZNumber");
            handler.endElement("", "", "PCSIlluminant");

            handler.endElement("", "", "Header");
            handler.endElement("", "", "IccProfile");

            handler.endDocument();

        } catch (Exception e) {

        }

    }

    public static String toText(Document doc, boolean indent) {
        if (doc == null) {
            return null;
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
//            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(os);
            transformer.transform(new DOMSource(doc), streamResult);
            os.flush();
            os.close();
            return os.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
