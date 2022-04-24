package mara.mybox.controller;

import mara.mybox.db.data.ShapeDescription;
import mara.mybox.db.table.TableShapeDescription;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @License Apache License Version 2.0
 */
public class ControlShapesLayers extends BaseSysTableController<ShapeDescription> {

    protected TableShapeDescription tableShapeDescription;

    public ControlShapesLayers() {

    }

    @Override
    public void initValues() {
        try {
            super.initValues();

        } catch (Exception e) {
        }
    }

    @Override
    public void setTableDefinition() {
        tableShapeDescription = new TableShapeDescription();
        tableDefinition = tableShapeDescription;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkButtons() {
        super.checkButtons();
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
