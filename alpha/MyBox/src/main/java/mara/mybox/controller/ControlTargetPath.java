package mara.mybox.controller;

import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.value.AppPaths;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlTargetPath extends ControlTargetFile {

    public ControlTargetPath() {
        isSource = false;
        isDirectory = true;
        checkQuit = false;
        permitNull = false;
        mustExist = true;
        notify = new SimpleBooleanProperty(false);
        defaultFile = new File(AppPaths.getGeneratedPath());
    }

}
