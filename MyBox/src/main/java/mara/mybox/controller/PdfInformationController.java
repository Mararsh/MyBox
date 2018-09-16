package mara.mybox.controller;

import java.io.File;
import mara.mybox.objects.PdfInformation;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.controller.BaseController;
import mara.mybox.tools.DateTools;
import static mara.mybox.tools.FileTools.showFileSize;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 16:32:08
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfInformationController extends BaseController {

    @FXML
    private TextField FilesPath;
    @FXML
    private TextField FileName;
    @FXML
    private TextField FileSize;
    @FXML
    private TextField title;
    @FXML
    private TextField subject;
    @FXML
    private TextField creator;
    @FXML
    private TextField author;
    @FXML
    private TextField createTime;
    @FXML
    private TextField modifyTime;
    @FXML
    private TextField producer;
    @FXML
    private TextField version;
    @FXML
    private TextField numberOfPages;
    @FXML
    private TextField firstPageSize, firstPageSize2;

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
            logger.error(e.toString());
        }
    }

}
