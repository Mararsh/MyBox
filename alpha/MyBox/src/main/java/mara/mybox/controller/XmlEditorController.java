package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class XmlEditorController extends BaseDomEditorController {
    
    @FXML
    protected ControlXmlTree domController;
    
    public XmlEditorController() {
        baseTitle = message("XmlEditor");
        TipsLabelKey = "XmlEditorTips";
        typeName = "XML";
    }
    
    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
    }
    
    @Override
    public void initValues() {
        try {
            super.initValues();
            
            domController.xmlEditor = this;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    @Override
    public String makeBlank() {
        String name = PopTools.askValue(getBaseTitle(), message("Create"), message("Root"), "data");
        if (name == null || name.isBlank()) {
            return null;
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<" + name + "></" + name + ">";
    }
    
    @Override
    public String currentEncoding() {
        String encoding = domController.doc.getXmlEncoding();
        if (encoding == null) {
            encoding = "utf-8";
        }
        return encoding;
    }
    
    @Override
    public void openSavedFile(File file) {
        XmlEditorController.open(file);
    }
    
    @Override
    public void loadDom(String xml, boolean updated) {
        domController.makeTree(xml);
        domChanged(updated);
    }
    
    @Override
    public String textsByDom() {
        return XmlTools.transform(domController.doc);
    }
    
    @Override
    public void clearDom() {
        domController.clearTree();
        domChanged(true);
    }
    
    @FXML
    protected void options(Event event) {
        XmlOptionsController.open();
    }
    
    @Override
    public void domMenuAction() {
        domController.showFunctionsMenu(null);
    }
    
    @Override
    protected List<MenuItem> helpMenus(Event event) {
        return HelpTools.xmlHelps();
    }
    
    @FXML
    protected void example() {
        File example = HelpTools.xmlExample(AppVariables.CurrentLangName);
        if (example != null && example.exists()) {
            loadTexts(TextFileTools.readTexts(null, example, Charset.forName("utf-8")));
        }
    }

    /*
        static
     */
    public static XmlEditorController load(String xml) {
        try {
            XmlEditorController controller = (XmlEditorController) WindowTools.openStage(Fxmls.XmlEditorFxml);
            controller.writePanes(xml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    public static XmlEditorController open(File file) {
        try {
            XmlEditorController controller = (XmlEditorController) WindowTools.openStage(Fxmls.XmlEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
}
