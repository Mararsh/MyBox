package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableDataDefinition;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Base extends BaseController {

    protected TableDataDefinition tableDataDefinition;
    protected TableData2DColumn tableDataColumn;
    protected DataDefinition dataDefinition;
    protected DataDefinition.DataType dataType;
    protected List<ColumnDefinition> columns;
    protected ColumnDefinition.ColumnType defaultColumnType;
    protected String dataName, defaultColValue, colPrefix, inputStyle;
    protected boolean defaultColNotNull;

    protected int totalSize, pagesNumber, pageSize, colsNumber, rowsNumber, widthChange;
    protected long startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected int currentPage, currentRow, currentCol;  // 0-based
    protected int maxRandom, warnThreshold;
    protected short scale;
    protected boolean sourceWithNames, totalRead;
    protected List<ColumnDefinition> savedColumns;
    protected String sourceDelimiterName, editDelimiterName, displayDelimiterName;
    protected SimpleBooleanProperty sheetChangedNotify, dataChangedNotify;

    protected TextField[][] sheetInputs;
    protected CheckBox[] colsCheck, rowsCheck;
    protected Label noDataLabel;
    protected String[][] pageData;
    protected ControlFileBackup backupController;

    protected final int defaultWarnThreshold = 2000;

    protected final ButtonType buttonClose = new ButtonType(message("Close"));
    protected final ButtonType buttonSynchronize = new ButtonType(message("SynchronizeAndClose"));
    protected final ButtonType buttonCancel = new ButtonType(message("Cancel"));

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab sheetTab, textsEditTab, htmlTab, textsDisplayTab, optionsTab, defTab;
    @FXML
    protected VBox sheetBox;
    @FXML
    protected Button analyseSheetButton, synchronizeTextsEditButton,
            rowsAddButton, rowsDeleteButton, columnsAddButton, columnsDeleteButton, widthSheetButton,
            calculateSheetButton, sortSheetButton, deleteSheetButton, equalSheetButton;
    @FXML
    protected CheckBox htmlTitleCheck, htmlColumnCheck, htmlRowCheck, textTitleCheck, textColumnCheck, textRowCheck,
            htmlAllCheck, textAllCheck, overPopMenuCheck, rightClickPopMenuCheck;
    @FXML
    protected TextArea textsDisplayArea;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector, pageSizeSelector, pageSelector;
    @FXML
    protected ControlWebView htmlViewController;
    @FXML
    protected ControlTextDelimiter displayDelimiterController;
    @FXML
    protected RadioButton sumRadio, addRadio, subRadio, multiplyRadio, mergeRadio, ascendingRadio, descendingRadio, copyRadio;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected Label pageLabel, totalLabel;
    @FXML
    protected TextField warnThresholdInput;
    @FXML
    protected ControlData2DDefine columnsController;

    public ControlSheet_Base() {
        baseTitle = message("Data");
        dataType = DataDefinition.DataType.DataFile;
        dataName = "sheet";
        colPrefix = "Field";
        defaultColumnType = ColumnDefinition.ColumnType.String;
        defaultColValue = "";
        defaultColNotNull = false;
    }

}
