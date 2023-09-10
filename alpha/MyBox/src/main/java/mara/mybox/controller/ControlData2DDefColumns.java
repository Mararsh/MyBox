package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2023-9-10
 * @License Apache License Version 2.0
 */
public class ControlData2DDefColumns extends BaseData2DColumnsController {

    public ControlData2DDefColumns() {
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            primaryColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isIsPrimaryKey();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (isChanging || rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isIsPrimaryKey()) {
                                        isChanging = true;
                                        column.setIsPrimaryKey(value);
                                        status(Status.Modified);
                                        isChanging = false;
                                    }
                                } catch (Exception e) {
                                    MyBoxLog.debug(e);
                                }
                            }
                        };
                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            primaryColumn.setEditable(true);
            primaryColumn.getStyleClass().add("editable-column");

            autoColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isAuto();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (isChanging || rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isAuto()) {
                                        isChanging = true;
                                        column.setAuto(value);
                                        status(Status.Modified);
                                        isChanging = false;
                                    }
                                } catch (Exception e) {
                                    MyBoxLog.debug(e);
                                }
                            }
                        };
                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            autoColumn.setEditable(true);
            autoColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void showExportMenu(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem("CSV", StyleTools.getIconImageView("iconCSV.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV();
            });
            items.add(menu);

            menu = new MenuItem("JSON", StyleTools.getIconImageView("iconJSON.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportJSON();
            });
            items.add(menu);

            menu = new MenuItem("XML", StyleTools.getIconImageView("iconXML.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportXML();
            });
            items.add(menu);

            menu = new MenuItem("Excel", StyleTools.getIconImageView("iconExcel.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportExcel();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("Data2DDefinitionExportMenuPopWhenMouseHovering", true));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DDefinitionExportMenuPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(mevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popExportMenu(Event event) {
        if (UserConfig.getBoolean("Data2DDefinitionExportMenuPopWhenMouseHovering", true)) {
            showExportMenu(event);
        }
    }

    public List<String> names() {
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Name"), message("Type"),
                message("Length"), message("Width"), message("DisplayFormat"),
                message("NotNull"), message("Editable"), message("PrimaryKey"), message("AutoGenerated"),
                message("DefaultValue"), message("Color"), message("ToInvalidValue"),
                message("DecimalScale"), message("Century"), message("FixTwoDigitYears"),
                message("Description")));
        return names;
    }

    public void exportCSV() {
        File file = chooseSaveFile(VisitHistory.FileType.CSV);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                File tmpFile = FileTmpTools.getTempFile();
                try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile,
                        Charset.forName("UTF-8")), CsvTools.csvFormat(",", true))) {
                    csvPrinter.printRecord(names());
                    List<String> row = new ArrayList<>();
                    for (Data2DColumn col : tableData) {
                        row.add(col.getColumnName());
                        row.add(col.getType().name());
                        row.add(col.getLength() + "");
                        row.add(col.getWidth() + "");
                        row.add(col.getFormat());
                        row.add(col.isNotNull() ? "1" : "0");
                        row.add(col.isEditable() ? "1" : "0");
                        row.add(col.isIsPrimaryKey() ? "1" : "0");
                        row.add(col.isAuto() ? "1" : "0");
                        row.add(col.getDefaultValue());
                        row.add(col.getColor().toString());
                        row.add(col.getInvalidAs().name());
                        row.add(col.getScale() + "");
                        row.add(col.getCentury() + "");
                        row.add(col.isFixTwoDigitYear() ? "1" : "0");
                        row.add(col.getDescription());
                        csvPrinter.printRecord(row);
                        row.clear();
                    }
                    csvPrinter.flush();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return FileTools.rename(tmpFile, file, true);
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.CSV);
                DataFileCSVController.open(file, Charset.forName("UTF-8"), true, ",");
            }
        };
        start(task);
    }

    public void exportXML() {
        File file = chooseSaveFile(VisitHistory.FileType.XML);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {

                return file.exists();
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.XML);
                DataFileCSVController.open(file, Charset.forName("UTF-8"), true, ",");
            }
        };
        start(task);
    }

    public void exportJSON() {
        File file = chooseSaveFile(VisitHistory.FileType.JSON);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {

                return file.exists();
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.JSON);
                DataFileCSVController.open(file, Charset.forName("UTF-8"), true, ",");
            }
        };
        start(task);
    }

    public void exportExcel() {
        File file = chooseSaveFile(VisitHistory.FileType.Excel);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {

                return file.exists();
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.Excel);
                DataFileExcelController.open(file, true);
            }
        };
        start(task);
    }

}
