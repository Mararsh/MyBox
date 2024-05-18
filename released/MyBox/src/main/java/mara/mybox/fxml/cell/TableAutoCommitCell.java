package mara.mybox.fxml.cell;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.Era;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.converter.ColorStringConverter;
import mara.mybox.fxml.converter.DoubleStringFromatConverter;
import mara.mybox.fxml.converter.EraStringConverter;
import mara.mybox.fxml.converter.FloatStringFromatConverter;
import mara.mybox.fxml.converter.IntegerStringFromatConverter;
import mara.mybox.fxml.converter.LongStringFromatConverter;
import mara.mybox.fxml.converter.ShortStringFromatConverter;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;
import mara.mybox.value.UserConfig;

/**
 * Reference:
 * https://stackoverflow.com/questions/24694616/how-to-enable-commit-on-focuslost-for-tableview-treetableview
 * By Ogmios
 *
 * @Author Mara
 * @CreateDate 2020-12-03
 * @License Apache License Version 2.0
 *
 * This is an old issue since 2011
 * https://bugs.openjdk.java.net/browse/JDK-8089514
 * https://bugs.openjdk.java.net/browse/JDK-8089311
 */
public class TableAutoCommitCell<S, T> extends TextFieldTableCell<S, T> {

    protected String editingText;
    protected int editingRow = -1;
    protected ChangeListener<Boolean> focusListener;
    protected ChangeListener<String> editListener;
    protected EventHandler<KeyEvent> keyReleasedHandler;

    public TableAutoCommitCell() {
        this(null);
    }

    public TableAutoCommitCell(StringConverter<T> conv) {
        super(conv);
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseClicked(event);
            }
        });
    }

    public void mouseClicked(MouseEvent event) {
        startEdit();
    }

    public TextField editor() {
        Node g = getGraphic();
        if (g != null && g instanceof TextField) {
            return (TextField) g;
        } else {
            return null;
        }
    }

    public int size() {
        try {
            return getTableView().getItems().size();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return -1;
        }
    }

    public int rowIndex() {
        int rowIndex = -2;
        try {
            int v = getTableRow().getIndex();
            if (v >= 0 && v < size()) {
                rowIndex = v;
            }
        } catch (Exception e) {
        }
        return rowIndex;
    }

    public boolean isEditingRow() {
        int rowIndex = rowIndex();
        return rowIndex >= 0 && editingRow == rowIndex;

    }

    public boolean validText(String text) {
        try {
            return valid(getConverter().fromString(text));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean valid(final T value) {
        return true;
    }

    public boolean changed(final T value) {
        T oValue = getItem();
        if (oValue == null) {
            return value != null;
        } else {
            return !oValue.equals(value);
        }
    }

    protected String name() {
        return message("TableRowNumber") + " " + (rowIndex() + 1) + "\n"
                + getTableColumn().getText();
    }

    public boolean initEditor() {
        editingRow = rowIndex();
        return editingRow >= 0;
    }

    @Override
    public void startEdit() {
        try {
            if (!initEditor()) {
                return;
            }
            editCell();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void editCell() {
        try {
            super.startEdit();

            clearEditor();
            TextField editor = editor();
            if (editor == null) {
                return;
            }
            if (focusListener == null) {
                initListeners();
            }
            editingText = editor.getText();
            editor.focusedProperty().addListener(focusListener);
            editor.textProperty().addListener(editListener);
            editor.setOnKeyReleased(keyReleasedHandler);
            editor.setStyle(null);
            NodeStyleTools.setTooltip(editor, message("EditCellComments"));
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void initListeners() {
        focusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                try {
                    if (!newValue) {
                        if (AppVariables.commitModificationWhenDataCellLoseFocus && isEditingRow()) {
                            commitEdit(getConverter().fromString(editingText));
                        } else {
                            clearEditor();
                            cancelCell();
                        }
                        editingRow = -1;
                    }
                } catch (Exception e) {
                }
            }
        };

        editListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (!isEditingRow()) {
                        return;
                    }
                    TextField editor = editor();
                    if (editor == null) {
                        return;
                    }
                    editingText = editor.getText();
                    if (validText(editingText)) {
                        editor.setStyle(null);
                    } else {
                        editor.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                }
            }
        };

        keyReleasedHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:
                        cancelCell();
                        event.consume();
                        break;
                    case TAB:
                        if (event.isShiftDown()) {
                            getTableView().getSelectionModel().selectPrevious();
                        } else {
                            getTableView().getSelectionModel().selectNext();
                        }
                        event.consume();
                        break;
                    case UP:
                        getTableView().getSelectionModel().selectAboveCell();
                        event.consume();
                        break;
                    case DOWN:
                        getTableView().getSelectionModel().selectBelowCell();
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        };

    }

    public void clearEditor() {
        try {
            TextField editor = editor();
            if (editor == null) {
                return;
            }
            if (focusListener != null) {
                editor.focusedProperty().removeListener(focusListener);
            }
            if (editListener != null) {
                editor.textProperty().removeListener(editListener);
            }
            editor.setOnKeyReleased(null);
            editor.setStyle(null);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void commitEdit(T value) {
        try {
            clearEditor();
            boolean valid = valid(value);
            boolean changed = changed(value);
            if (!isEditingRow() || !valid || !changed) {
                cancelCell();
            } else {
                setCellValue(value);
            }
            editingRow = -1;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean setCellValue(T value) {
        try {
            if (isEditing()) {
                super.commitEdit(value);
            } else {
                return commit(value);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean commit(T value) {
        try {
            TableView<S> table = getTableView();
            if (table != null && isEditingRow()) {
                TableColumn<S, T> column = getTableColumn();
                if (column == null) {
                    cancelCell();
                } else {
                    TablePosition<S, T> pos = new TablePosition<>(table, editingRow, column);
                    CellEditEvent<S, T> ev = new CellEditEvent<>(table, pos, TableColumn.editCommitEvent(), value);
                    Event.fireEvent(column, ev);
                    updateItem(value, false);
                }
            } else {
                cancelCell();
            }
            if (table != null) {
                table.edit(-1, null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public void cancelCell() {
        editingRow = -1;
        cancelEdit();
        updateItem(getItem(), false);
    }

    /*
        static 
     */
    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forStringColumn() {
        return new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> param) {
                return new TableAutoCommitCell<>(new DefaultStringConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Integer>, TableCell<S, Integer>> forIntegerColumn() {
        return new Callback<TableColumn<S, Integer>, TableCell<S, Integer>>() {
            @Override
            public TableCell<S, Integer> call(TableColumn<S, Integer> param) {
                return new TableAutoCommitCell<>(new IntegerStringFromatConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Long>, TableCell<S, Long>> forLongColumn() {
        return new Callback<TableColumn<S, Long>, TableCell<S, Long>>() {
            @Override
            public TableCell<S, Long> call(TableColumn<S, Long> param) {
                return new TableAutoCommitCell<>(new LongStringFromatConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Float>, TableCell<S, Float>> forFloatColumn() {
        return new Callback<TableColumn<S, Float>, TableCell<S, Float>>() {
            @Override
            public TableCell<S, Float> call(TableColumn<S, Float> param) {
                return new TableAutoCommitCell<>(new FloatStringFromatConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Double>, TableCell<S, Double>> forDoubleColumn() {
        return new Callback<TableColumn<S, Double>, TableCell<S, Double>>() {
            @Override
            public TableCell<S, Double> call(TableColumn<S, Double> param) {
                return new TableAutoCommitCell<>(new DoubleStringFromatConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Short>, TableCell<S, Short>> forShortColumn() {
        return new Callback<TableColumn<S, Short>, TableCell<S, Short>>() {
            @Override
            public TableCell<S, Short> call(TableColumn<S, Short> param) {
                return new TableAutoCommitCell<>(new ShortStringFromatConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Date>, TableCell<S, Date>> forDateTimeColumn() {
        return new Callback<TableColumn<S, Date>, TableCell<S, Date>>() {
            @Override
            public TableCell<S, Date> call(TableColumn<S, Date> param) {
                return new TableAutoCommitCell<>(new DateTimeStringConverter(new SimpleDateFormat(TimeFormats.Datetime)));
            }
        };
    }

    public static <S> Callback<TableColumn<S, Era>, TableCell<S, Era>> forEraColumn() {
        return new Callback<TableColumn<S, Era>, TableCell<S, Era>>() {
            @Override
            public TableCell<S, Era> call(TableColumn<S, Era> param) {
                return new TableAutoCommitCell<>(new EraStringConverter());
            }
        };
    }

    public static <S> Callback<TableColumn<S, Color>, TableCell<S, Color>> forColorColumn() {
        return new Callback<TableColumn<S, Color>, TableCell<S, Color>>() {
            @Override
            public TableCell<S, Color> call(TableColumn<S, Color> param) {
                return new TableAutoCommitCell<>(new ColorStringConverter());
            }
        };
    }

}
