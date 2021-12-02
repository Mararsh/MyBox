package mara.mybox.controller;

import java.io.File;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlPathInput extends ControlFileSelecter {

    public ControlPathInput() {
        isSource = false;
        isDirectory = true;
        checkQuit = false;
        permitNull = false;
        mustExist = false;
        defaultFile = new File(AppPaths.getGeneratedPath());
    }

    @Override
    public void initControls() {
        super.initControls();
        label.setText(message("TargetPath"));
    }

    @Override
    public ControlFileSelecter baseName(String baseName) {
        this.baseName = baseName;
        this.savedName = baseName + "TargatPath";
        return this;
    }

}
