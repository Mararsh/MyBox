package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-24
 * @License Apache License Version 2.0
 */
public class MyBoxDocController extends BaseController {

    protected File htmlFile;

    @FXML
    protected ControlFileSelecter fileController;

    public MyBoxDocController() {
        baseTitle = message("MakeDocument");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            fileController.label(message("Html"))
                    .isDirectory(false).isSource(true).mustExist(true).permitNull(false)
                    .defaultFile(new File("D:\\玛瑞\\Mybox\\文档\\v6.5.9\\overview\\MyBox-6.5.9-Overview-zh_html"))
                    .baseName(baseName).savedName(baseName + "File").init();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        File file = fileController.file;
        if (file == null || !file.isFile() || !file.exists()) {
            popError(message("Invalid"));
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {

                    } catch (Exception e) {
//                        MyBoxLog.debug(e.toString());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {

                }

            };
            start(task);
        }
    }

}
