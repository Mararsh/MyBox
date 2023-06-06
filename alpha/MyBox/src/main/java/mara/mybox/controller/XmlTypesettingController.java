package mara.mybox.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-5-26
 * @License Apache License Version 2.0
 */
public class XmlTypesettingController extends BaseBatchFileController {

    protected DocumentBuilder builder;
    protected String encoding;
    protected Transformer transformer;

    @FXML
    protected ControlXmlOptions optionsController;
    @FXML
    protected ToggleGroup targetEncodingGroup;
    @FXML
    protected RadioButton sameEncodingRadio;
    @FXML
    protected ComboBox<String> targetEncodingBox;

    public XmlTypesettingController() {
        baseTitle = message("XmlTypesetting");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
    }

    @Override
    public void initOptionsSection() {

        List<String> setNames = TextTools.getCharsetNames();
        targetEncodingBox.getItems().addAll(setNames);
        targetEncodingBox.getSelectionModel().select(Charset.defaultCharset().name());

    }

    @Override
    public boolean makeMoreParameters() {
        try {
            builder = XmlTreeNode.builder(this);
            if (builder == null) {
                popError(message("Failed") + ": DocumentBuilder");
                return false;
            }
            if (sameEncodingRadio.isSelected()) {
                encoding = null;
            } else {
                encoding = targetEncodingBox.getSelectionModel().getSelectedItem();
            }
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT,
                    UserConfig.getBoolean("XmlTransformerIndent", false) ? "yes" : "no");
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            Document doc = builder.parse(srcFile);
            if (doc == null) {
                return message("Failed");
            }
            String sourceEncoding = doc.getXmlEncoding();
            if (sourceEncoding == null) {
                sourceEncoding = "utf-8";
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, sourceEncoding);
            String xml;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
                StreamResult streamResult = new StreamResult();
                streamResult.setOutputStream(os);
                transformer.transform(new DOMSource(doc), streamResult);
                os.flush();
                os.close();
                xml = os.toString(sourceEncoding);
            } catch (Exception e) {
                updateLogs(e.toString());
                return message("Failed");
            }
            if (xml == null) {
                return message("Failed");
            }
            String targetEncoding = encoding;
            if (targetEncoding == null) {
                targetEncoding = sourceEncoding;
            }
            xml = "<?xml version=\"" + doc.getXmlVersion()
                    + "\" encoding=\"" + targetEncoding + "\" standalone=\"yes\"?>\n"
                    + xml;
            TextFileTools.writeFile(target, xml, Charset.forName(targetEncoding));
            if (target.exists() && target.length() > 0) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            return message("Failed");
        }
    }

}
