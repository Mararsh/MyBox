package mara.mybox.controller;

import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
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
    protected DataTable dataTable;
    protected boolean created;

    public BaseData2DSaveAsController() {
        baseTitle = message("SaveAs");
    }

    public void checkTargets() {
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
            targetFile = Data2D.targetFile(targetName, format);
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        created = false;
        if (export != null) {
            export.setTaskController(this);
            export.setCss(HtmlStyles.TableStyle);
        }
    }

    @Override
    public void afterSuccess() {
        if (format == TargetType.DatabaseTable) {
            if (dataTable != null) {
                Data2DManufactureController.openDef(dataTable);
                created = true;
            }
        } else if (export != null) {
            export.openResults(this);
            created = export.isCreated();
        }
    }

    @Override
    public void afterTask() {
        if (data2D != null) {
            data2D.stopTask();
        }
        export = null;
        if (created) {
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
