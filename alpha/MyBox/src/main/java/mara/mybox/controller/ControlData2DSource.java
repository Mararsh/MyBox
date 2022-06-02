package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends ControlData2DLoad {

    protected ControlData2DEditTable tableController;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ControlJShell filterController;

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            if (tableController == null) {
                return;
            }
            selectController.setParameters(parent, tableController);
            this.parentController = parent;
            this.tableController = tableController;

            filterController.setParamters(selectController.data2D);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

//    public boolean initFilter() {
//        return jexlController.pickInputs();
//    }
//
//    public boolean satisfyFilter(List<Data2DColumn> columns, List<String> values, long rowIndex) {
//        try {
//            if (columns == null || columns.isEmpty() || values == null || columns.size() != values.size()) {
//                return false;
//            }
//            String jexlContext = "jexlContext.set(\"" + message("RowNumber2") + "\", " + rowIndex + ");\n";
//            for (int i = 0; i < columns.size(); i++) {
//                Data2DColumn column = columns.get(i);
//                String value = values.get(i);
//                if (column.valueQuoted()) {
//                    value = "'" + value + "'";
//                }
//                jexlContext += "jexlContext.set(\"" + column.getColumnName() + "\", " + value + ");\n";
//            }
//            JShellTools.runScript(jexlController.jShell, jexlContext);
//            String result = jexlController.runScript();
//            return result != null && result.equalsIgnoreCase("true");
//        } catch (Exception e) {
//            MyBoxLog.error(e);
//            return false;
//        }
//    }
}
