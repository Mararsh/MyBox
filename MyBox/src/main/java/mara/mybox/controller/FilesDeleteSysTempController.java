package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import org.apache.commons.io.FileUtils;

/**
 * @Author Mara
 * @CreateDate 2020-11-21
 * @License Apache License Version 2.0
 */
public class FilesDeleteSysTempController extends BaseController {

    protected File path;

    @FXML
    protected Label pathLabel, resultsLabel;

    public FilesDeleteSysTempController() {
        baseTitle = AppVariables.message("DeleteSysTemporaryPathFiles");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            path = new File(System.getProperty("java.io.tmpdir"));
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
            SingletonTask countTask = new SingletonTask<Void>() {

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
            countTask.setSelf(countTask);
            Thread thread = new Thread(countTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        startButton.requestFocus();
    }

    @FXML
    public void openFolder() {
        FxmlStage.browseURI(getMyStage(), path.toURI());
    }

    @FXML
    @Override
    public void startAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private long before = 0, after = 0;

                @Override
                protected boolean handle() {
                    try {
                        System.gc();
                        before = FileUtils.sizeOfDirectory(path);
                        File[] files = path.listFiles();
                        if (files == null) {
                            return true;
                        }
                        for (File file : files) {
                            try {
                                if (file.isDirectory()) {
                                    try {
                                        FileUtils.cleanDirectory(file);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    FileUtils.deleteQuietly(file);
                                }
                            } catch (Exception e) {
                            }
                        }
                        after = FileUtils.sizeOfDirectory(path);
                    } catch (Exception e) {
//                        MyBoxLog.debug(e.toString());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    resultsLabel.setText(MessageFormat.format(AppVariables.message("DeleteResults"),
                            FileUtils.byteCountToDisplaySize(before - after),
                            FileUtils.byteCountToDisplaySize(before),
                            FileUtils.byteCountToDisplaySize(after)));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
