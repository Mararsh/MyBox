package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-22
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_Base extends ControlSheetDisplay {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected DataDefinition dataDefinition;
    protected DataDefinition.DataType dataType;

    protected TextField[][] inputs;
    protected CheckBox[] colsCheck, rowsCheck;
    protected List<String> copiedRow, copiedCol;
    protected SimpleBooleanProperty notify;
    protected int widthChange, pagesNumber;
    protected boolean rowsSelected, colsSelected, dataChanged;
    protected Label noDataLabel;

    @FXML
    protected VBox sheetBox;
    @FXML
    protected Button sizeSheetButton, deleteSheetButton, copySheetButton, equalSheetButton, editSheetButton;
    @FXML
    protected Tab sheetTab;
    @FXML
    protected ControlSheetDisplay sheetDisplayController;

    public BaseSheetController_Base() {
        baseTitle = Languages.message("DataEdit");
        dataType = DataDefinition.DataType.DataFile;
    }

}
