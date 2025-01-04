package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.data.ImageFileInformation;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-21
 * @License Apache License Version 2.0
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    protected TextField fileInput;
    @FXML
    protected TextArea metaDataInput;

    public ImageMetaDataController() {
        baseTitle = Languages.message("ImageMetaData");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void loadImageFileMeta(ImageInformation info) {
        fileInput.setText("");
        metaDataInput.setText("");
        if (info == null || info.getFile() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        fileInput.setText(info.getFile().getAbsolutePath());
        task = new FxSingletonTask<Void>(this) {

            StringBuilder s;

            @Override
            protected boolean handle() {
                ImageFileInformation finfo = info.getImageFileInformation();
                s = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                s.append("<ImageMetadata file=\"").
                        append(finfo.getFile().getAbsolutePath()).
                        append("\"  numberOfImages=\"").
                        append(finfo.getNumberOfImages()).
                        append("\">\n");
                int index = 1;
                for (ImageInformation imageInfo : finfo.getImagesInformation()) {
                    s.append("    <Image index=\"").append(index).append("\">\n");
                    s.append(imageInfo.getMetaDataXml());
                    s.append("    </Image>\n");
                    index++;
                }
                s.append("</ImageMetadata>\n");
                return true;
            }

            @Override
            protected void whenSucceeded() {
                metaDataInput.setText(s.toString());
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            myStage.requestFocus();
                            metaDataInput.home();
                            metaDataInput.requestFocus();
                        });
                    }
                }, 1000);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        final File file = saveAsFile();
        if (file == null) {
            return;
        }
        save(file, false);
    }

    public void save(File file, boolean isEdit) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                ok = TextFileTools.writeFile(file, metaDataInput.getText()) != null;
                recordFileWritten(file);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (isEdit) {
                    TextEditorController.open(file);
                } else {
                    popSuccessful();
                }
            }

        };
        start(task);
    }

    @FXML
    public void editAction() {
        File file = FileTmpTools.getTempFile(".txt");
        save(file, true);
    }

    /*
        static
     */
    public static ImageMetaDataController open() {
        try {
            ImageMetaDataController controller = (ImageMetaDataController) WindowTools.openStage(Fxmls.ImageMetaDataFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageMetaDataController open(ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            ImageMetaDataController controller = open();
            if (controller != null) {
                controller.loadImageFileMeta(info);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
