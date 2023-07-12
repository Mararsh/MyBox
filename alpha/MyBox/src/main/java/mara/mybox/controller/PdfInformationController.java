package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public class PdfInformationController extends HtmlTableController {

    public PdfInformationController() {
        baseTitle = "PDF";
    }

    public void setInformation(PdfInformation info) {
        try {
            File file = info.getFile();
            table = new StringTable();
            table.add(Arrays.asList(message("FilesPath"), file.getParent()));
            table.add(Arrays.asList(message("FileName"), file.getName()));
            table.add(Arrays.asList(message("FileSize"), FileTools.showFileSize(file.length())));
            table.add(Arrays.asList(message("Title"), info.getTitle()));
            table.add(Arrays.asList(message("Subject"), info.getSubject()));
            table.add(Arrays.asList(message("Author"), info.getAuthor()));
            table.add(Arrays.asList(message("Creator"), info.getCreator()));
            table.add(Arrays.asList(message("CreateTime"), DateTools.datetimeToString(info.getCreateTime())));
            table.add(Arrays.asList(message("ModifyTime"), DateTools.datetimeToString(info.getModifyTime())));
            table.add(Arrays.asList(message("PDFProducer"), info.getProducer()));
            table.add(Arrays.asList(message("Version"), info.getVersion() + ""));
            table.add(Arrays.asList(message("NumberOfPages"), info.getNumberOfPages() + ""));
            table.add(Arrays.asList(message("FirstPageSize"), info.getFirstPageSize()));
            table.add(Arrays.asList("", info.getFirstPageSize2()));

            displayHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
