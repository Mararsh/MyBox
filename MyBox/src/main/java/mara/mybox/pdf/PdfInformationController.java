package mara.mybox.pdf;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.MyBoxBaseController;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.DateTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 16:32:08

 * @Description
 * @License Apache License Version 2.0
 */
public class PdfInformationController extends MyBoxBaseController {

    private static final Logger logger = LogManager.getLogger();

    @FXML
    private TextField fileName;
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
    private void closeStage() {
        try {
            getStage().close();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setInformation(PdfInformation info) {
        try {
            fileName.setText(info.getFile().getName());
            title.setText(info.getTitle());
            subject.setText(info.getSubject());
            creator.setText(info.getCreator());
            author.setText(info.getAuthor());
            createTime.setText(DateTools.datetimeToString(info.getCreateTime()));
            modifyTime.setText(DateTools.datetimeToString(info.getModifyTime()));
            producer.setText(info.getProducer());
            version.setText(info.getVersion() + "");
            numberOfPages.setText(info.getNumberOfPages() + "");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
