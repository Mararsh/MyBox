package mara.mybox.controller;

import java.io.File;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.XmlTools;
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
    @FXML
    protected ControlXmlOptions optionsController;

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
    public void initControls() {
        try {
            super.initControls();

            typesettingCheck.selectedProperty().bindBidirectional(optionsController.indentCheck.selectedProperty());

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

    @Override
    public void domMenuAction() {
        domController.popFunctionsMenu(null);
    }

    @FXML
    @Override
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.xmlHelps(true));
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
