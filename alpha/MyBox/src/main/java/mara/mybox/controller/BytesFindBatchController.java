package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data.FileEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-5-10
 * @License Apache License Version 2.0
 */
public class BytesFindBatchController extends FindBatchController {

    @FXML
    protected BytesFindBatchOptions bytesFindOptionsController;

    public BytesFindBatchController() {
        baseTitle = message("BytesFindBatch");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.All);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            optionsController = bytesFindOptionsController;
            editType = FileEditInformation.Edit_Type.Bytes;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
