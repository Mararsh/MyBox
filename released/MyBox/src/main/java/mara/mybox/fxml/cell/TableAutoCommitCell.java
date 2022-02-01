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
import javafx.scene.control.TableRow;
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
import mara.mybox.fxml.ColorStringConverter;
import mara.mybox.fxml.DoubleStringFromatConverter;
import mara.mybox.fxml.EraStringConverter;
import mara.mybox.fxml.FloatStringFromatConverter;
import mara.mybox.fxml.IntegerStringFromatConverter;
import mara.mybox.fxml.LongStringFromatConverter;
import mara.mybox.fxml.ShortStringFromatConverter;
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

    protected TextField textFieldRef;
    protected boolean isEdit;
    protected ChangeListener<Boolean> focusListener;
    protected EventHandler<KeyEvent> keyPressedHandler;
    protected ChangeListener<String> editListener;

    public TableAutoCommitCell() {
        this(null);
    }

    public TableAutoCommitCell(final StringConverter<T> conv) {
        super(conv);
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TableView<S> table = getTableView();
                if (table != null) {
                    table.edit(rowIndex(), getTableColumn());
                }
            }
        });

    }

    public boolean valid() {
        try {
            return valid(getConverter().fromString(textFieldRef.getText()));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean valid(final T value) {
        return true;
    }

    public int rowIndex() {
        TableRow row = getTableRow();
        return row == null ? -1 : row.getIndex();
    }

    @Override
    public void startEdit() {
        super.startEdit();

        isEdit = true;
        if (focusListener == null) {
            initListeners();
        }
        if (textFieldRef != null) {
            textFieldRef.focusedProperty().removeListener(focusListener);
            textFieldRef.setOnKeyPressed(null);
            textFieldRef.textProperty().removeListener(editListener);
            textFieldRef.setStyle(null);
        }
        Node g = getGraphic();
        textFieldRef = (g != null && g instanceof TextField) ? (TextField) g : null;
        if (textFieldRef != null) {
            textFieldRef.focusedProperty().addListener(focusListener);
            textFieldRef.setOnKeyPressed(keyPressedHandler);
            textFieldRef.textProperty().addListener(editListener);
        }
    }

    public void initListeners() {
        focusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                try {
                    if (isEdit && !newValue) {
                        commitEdit(getConverter().fromString(textFieldRef.getText()));
                    }
                } catch (Exception e) {
                }
            }
        };

        editListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (textFieldRef == null) {
                        return;
                    }
                    if (valid()) {
                        textFieldRef.setStyle(null);
                    } else {
                        textFieldRef.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                }
            }
        };

        keyPressedHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:
                        isEdit = false;
                        cancelEdit(); // see CellUtils#createTextField(...)
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

    @Override
    public void commitEdit(final T value) {
        try {
            boolean valid = valid(value);
            if (isEditing()) {
                if (valid) {
                    super.commitEdit(value);
                } else {
                    cancelEdit();
                }
            } else {
                TableView<S> table = getTableView();
                TableRow<S> row = getTableRow();
                TableColumn<S, T> column = getTableColumn();
                if (valid && table != null && row != null && column != null) {
                    final TablePosition<S, T> pos = new TablePosition<>(table, row.getIndex(), column); // instead of tbl.getEditingCell()
                    final CellEditEvent<S, T> ev = new CellEditEvent<>(table, pos, TableColumn.editCommitEvent(), value);
                    Event.fireEvent(column, ev);
                    updateItem(value, false);
                } else {
                    cancelEdit();
                }
                if (table != null) {
                    table.edit(-1, null);
                }
            }
            if (textFieldRef != null) {
                textFieldRef.focusedProperty().removeListener(focusListener);
                textFieldRef.setOnKeyPressed(null);
                textFieldRef.textProperty().removeListener(editListener);
                textFieldRef.setStyle(null);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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
                return new TableAutoCommitCell<>(new DateTimeStringConverter(new SimpleDateFormat(TimeFormats.DatetimeFormat)));
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
