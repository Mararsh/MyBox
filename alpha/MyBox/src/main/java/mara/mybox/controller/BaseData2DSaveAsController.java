package mara.mybox.controller;

import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.data2d.tools.Data2DConvertTools;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class BaseData2DSaveAsController extends BaseDataConvertController {

    protected BaseData2DLoadController tableController;
    protected Data2DExport export;
    protected Data2D data2D;
    protected TargetType format;
    protected String targetName;
    protected InvalidAs invalidAs = InvalidAs.Blank;

    public BaseData2DSaveAsController() {
        baseTitle = message("SaveAs");
    }

    public void checkParameters() {
        if (targetName == null || targetName.isBlank()) {
            targetName = data2D.dataName();
        }
        if (targetName == null || targetName.isBlank()) {
            targetName = "Data2D";
        } else if (targetName.startsWith(TmpTable.TmpTablePrefix)
                || targetName.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
            targetName = targetName.substring(TmpTable.TmpTablePrefix.length());
        }
        if (targetFile == null) {
            targetFile = Data2DConvertTools.targetFile(targetName, format);
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        if (export != null) {
            export.setTaskController(this);
        }
    }

    @Override
    public void afterSuccess() {
        if (export != null) {
            export.openResults();
        }
    }

    @Override
    public void afterTask() {
        if (data2D != null) {
            data2D.stopTask();
        }
        export = null;
        if (successed) {
            popInformation(message("Done"));
            close();
        } else {
            popError(message("Failed"));
        }
    }

    /*
        static
     */
    public static BaseData2DSaveAsController open() {
        try {
            BaseData2DSaveAsController controller
                    = (BaseData2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
