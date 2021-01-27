package mara.mybox.controller;

import java.io.File;
import mara.mybox.data.PdfInformation;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.tools.DateTools;
import static mara.mybox.tools.FileTools.showFileSize;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 16:32:08
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfInformationController extends BaseController {

    @FXML
    protected TextField FilesPath;
    @FXML
    protected TextField FileName;
    @FXML
    protected TextField FileSize;
    @FXML
    protected TextField title;
    @FXML
    protected TextField subject;
    @FXML
    protected TextField creator;
    @FXML
    protected TextField author;
    @FXML
    protected TextField createTime;
    @FXML
    protected TextField modifyTime;
    @FXML
    protected TextField producer;
    @FXML
    protected TextField version;
    @FXML
    protected TextField numberOfPages;
    @FXML
    protected TextField firstPageSize, firstPageSize2;

    public PdfInformationController() {
        baseTitle = AppVariables.message("PdfInformation");

    }

    public void setInformation(PdfInformation info) {
        try {
            File file = info.getFile();
            FilesPath.setText(file.getParent());
            FileName.setText(file.getName());
            FileSize.setText(showFileSize(file.length()));
            title.setText(info.getTitle());
            subject.setText(info.getSubject());
            creator.setText(info.getCreator());
            author.setText(info.getAuthor());
            createTime.setText(DateTools.datetimeToString(info.getCreateTime()));
            modifyTime.setText(DateTools.datetimeToString(info.getModifyTime()));
            producer.setText(info.getProducer());
            version.setText(info.getVersion() + "");
            numberOfPages.setText(info.getNumberOfPages() + "");
            firstPageSize.setText(info.getFirstPageSize());
            firstPageSize2.setText(info.getFirstPageSize2());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
