package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-5
 * @License Apache License Version 2.0
 */
public class TestCase {

    protected int id;
    protected String object, version;
    protected Operation operation;
    protected Type type;
    protected Stage stage;
    protected Status Status;
    protected BaseController controller;

    public static enum Type {
        UserInterface, Function, Bundary, Data, API, IO, Exception, Performance, Robustness, Usability, Compatibility, Security, Document
    }

    public static enum Operation {
        OpenInterface, ClickButton, OpenFile, Edit
    }

    public static enum Stage {
        Unit, Integration, Verification, Alpha, Beta
    }

    public static enum Status {
        Testing, Success, Fail, NotTested
    }

    public TestCase() {
        init();
    }

    public TestCase(int id, Type type, Operation operation, String object) {
        init();
        this.id = id;
        this.type = type;
        this.operation = operation;
        this.object = object;
    }

    private void init() {
        type = Type.UserInterface;
        version = AppValues.AppVersion;
        stage = Stage.Alpha;
        Status = Status.NotTested;
    }


    /*
        static
     */
    public static List<TestCase> testCases() {
        List<TestCase> cases = new ArrayList<>();
        try {
            int index = 1;
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("Notes")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfView")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfConvertImagesBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfImagesConvertBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfCompressImagesBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfConvertHtmlsBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfExtractImagesBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfExtractTextsBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfOCRBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PdfSplitBatch")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("MergePdf")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PDFAttributes")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("PDFAttributesBatch")));

            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("MarkdownEditer")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("MarkdownToHtml")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("MarkdownToText")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("MarkdownToPdf")));

            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlEditor")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("WebFind")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("WebElements")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlSnap")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlExtractTables")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlToMarkdown")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlToText")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlToPdf")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlSetCharset")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlSetStyle")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlMergeAsHtml")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlMergeAsMarkdown")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlMergeAsPDF")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlMergeAsText")));
            cases.add(new TestCase(index++, Type.UserInterface, Operation.OpenInterface, message("HtmlFrameset")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return cases;
    }

    /*
        get/set
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getOperationName() {
        return message(operation.name());
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Type getType() {
        return type;
    }

    public String getTypeName() {
        return message(type.name());
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Status getStatus() {
        return Status;
    }

    public String getStatusName() {
        return message(Status.name());
    }

    public void setStatus(Status Status) {
        this.Status = Status;
    }

    public BaseController getController() {
        return controller;
    }

    public void setController(BaseController controller) {
        this.controller = controller;
    }

}
