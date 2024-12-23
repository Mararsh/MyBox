package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxSingletonTask;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 */
public class ControlImageScope extends BaseImageScope {

    protected ControlDataImageScope editor;

    public void setEditor(ControlDataImageScope parent) {
        try {
            parentController = parent;
            editor = parent;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public Image srcImage() {
        image = editor.srcImage;
        return image;
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image fileImage;

            @Override
            protected boolean handle() {
                try {
                    fileImage = FxImageTools.readImage(this, file);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return fileImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = file;
                image = fileImage;
                editor.srcImage = image;
                editor.sourceFile = sourceFile;
                applyScope(scope);
            }

        };
        start(task);
    }

}
