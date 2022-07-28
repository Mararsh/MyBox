package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import org.apache.commons.io.FileUtils;

/**
 * @Author Mara
 * @CreateDate 2020-11-21
 * @License Apache License Version 2.0
 */
public class FilesDeleteJavaTempController extends BaseController {

    protected File path;

    @FXML
    protected Label pathLabel, resultsLabel;

    public FilesDeleteJavaTempController() {
        baseTitle = Languages.message("DeleteJavaIOTemporaryPathFiles");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            path = FileTools.javaIOTmpPath();
            pathLabel.setText(path.getAbsolutePath());
            countSize();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    // https://www.jb51.net/article/42298.htm
    // https://stackoverflow.com/questions/2149785/get-size-of-folder-or-file
    public void countSize() {
        synchronized (this) {
            SingletonTask countTask = new SingletonTask<Void>(this) {

                private String size;

                @Override
                protected boolean handle() {
                    size = FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(path));
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    resultsLabel.setText(size);
                }

            };
            start(countTask, false);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        startButton.requestFocus();
    }

    @FXML
    public void openFolder() {
        browseURI(path.toURI());
    }

    @FXML
    @Override
    public void startAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private long before = 0, after = 0;

                @Override
                protected boolean handle() {
                    try {
                        System.gc();
                        before = FileUtils.sizeOfDirectory(path);
                        FileDeleteTools.clearJavaIOTmpPath();
                        after = FileUtils.sizeOfDirectory(path);
                    } catch (Exception e) {
//                        MyBoxLog.debug(e.toString());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    resultsLabel.setText(MessageFormat.format(Languages.message("DeleteResults"),
                            FileUtils.byteCountToDisplaySize(before - after),
                            FileUtils.byteCountToDisplaySize(before),
                            FileUtils.byteCountToDisplaySize(after)));
                }

            };
            start(task);
        }
    }

}
