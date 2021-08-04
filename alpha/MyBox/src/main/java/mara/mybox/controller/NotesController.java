package mara.mybox.controller;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.NoteTag;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.HtmlReadTools;

import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NotesController extends BaseDataTableController<Note> {

    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TableTag tableTag;
    protected TableNoteTag tableNoteTag;
    protected Note currentNote;
    protected Notebook bookOfCurrentNote;
    protected MutableDataSet htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected boolean noteChanged;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected ListView<Tag> tagsList, noteTagsList;
    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected TableColumn<Note, Long> ntidColumn;
    @FXML
    protected TableColumn<Note, String> titleColumn;
    @FXML
    protected TableColumn<Note, Date> timeColumn;
    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Button refreshNotesButton, clearNotesButton, deleteNotesButton, moveDataNotesButton, copyNotesButton, addBookNoteButton,
            addNoteButton, queryTagsButton, deleteTagsButton, renameTagButton, okNoteTagsButton,
            refreshTimesButton, queryTimesButton, refreshTagsButton, synchronizeButton;
    @FXML
    protected VBox notesConditionBox, timesBox;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected FlowPane namesPane, tagsPane, noteTagsPane;
    @FXML
    protected Label conditionLabel, notebookLabel;
    @FXML
    protected RadioButton titleRadio, contentsRadio;
    @FXML
    protected ControlStringSelector searchController;
    @FXML
    protected TabPane notePane, noteEditPane;
    @FXML
    protected Tab noteViewTab, noteCodesTab, noteEditorTab, noteMarkdownTab, noteStyleTab, noteTagsTab;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected BaseWebViewController webviewController;
    @FXML
    protected ControlHtmlCodes codesController;
    @FXML
    protected TextArea styleInput, markdownArea;

    public NotesController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableNotebook = new TableNotebook();
        tableNote = new TableNote();
        tableTag = new TableTag();
        tableNoteTag = new TableNoteTag();
        tableDefinition = tableNote;
        notebooksController.selectedNode = null;
        currentNote = null;
    }

    @Override
    protected void initColumns() {
        try {
            ntidColumn.setCellValueFactory(new PropertyValueFactory<>("ntid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            notebooksController.setParent(this);

            timeController.setParent(this, false);
            searchController.init(this, baseName + "Saved", Languages.message("Note"), 20);

            super.initControls();

            notebooksController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadBook(notebooksController.selectedNode);
                }
            });

            subCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (notebooksController.selectedNode != null) {
                        loadTableData();
                    }
                }
            });

            synchronizeButton.setDisable(true);
            noteEditPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldTab, Tab newTab) {
                    synchronizeButton.setDisable(newTab != noteCodesTab && newTab != noteEditorTab && newTab != noteMarkdownTab);
                    if (oldTab == noteStyleTab) {
                        String style = UserConfig.getUserConfigString(baseName + "Style", null);
                        if (style != null && !style.isBlank()) {
                            style = "<style type=\"text/css\">\n" + style + "\n</style>";
                        } else {
                            style = "";
                        }
                        String html = style + HtmlReadTools.body(codesController.codes(), false);
                        webviewController.loadContents(html);
                        htmlEditor.setHtmlText(html);
                    }
                }
            });

            titleInput.setText(Languages.message("Note"));
            styleInput.setText(UserConfig.getUserConfigString(baseName + "Style", HtmlStyles.DefaultStyle));

            timeController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timeController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });

            tagsList.setCellFactory(p -> new ListCell<Tag>() {
                @Override
                public void updateItem(Tag item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText(item.getTag());
                }
            });
            tagsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tagsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tag>() {
                @Override
                public void changed(ObservableValue ov, Tag oldValue, Tag newValue) {
                    queryTagsButton.setDisable(newValue == null);
                    deleteTagsButton.setDisable(newValue == null);
                    renameTagButton.setDisable(newValue == null);
                }
            });
            tagsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    Tag selected = tagsList.getSelectionModel().getSelectedItem();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popTagMenu(event, selected);
                    } else if (event.getClickCount() > 1) {
                        queryTags();
                    }
                }
            });

            webviewController.setParameters(this);
            initCodesTab();
            initHtmlEdtior();
            initMarkdownTab();

            noteTagsList.setCellFactory(p -> new ListCell<Tag>() {
                @Override
                public void updateItem(Tag item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText(item.getTag());
                }
            });
            noteTagsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean keyF1() {
        synchronize();
        return true;
    }

    protected void initCodesTab() {
        try {
            codesController.setParameters(this);
            codesController.codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        codesChanged(true);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initHtmlEdtior() {
        try {
            htmlEditor.setOnDragExited(new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    if (!isSettingValues) {
                        richTextChanged(true);
                    }
                }
            });
            htmlEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (!isSettingValues) {
                        richTextChanged(true);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initMarkdownTab() {
        try {
            markdownArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        markdownChanged(true);
                    }
                }
            });

            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf("PEGDOWN"));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    AbbreviationExtension.create(),
                    DefinitionExtension.create(),
                    FootnoteExtension.create(),
                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            htmlOptions.set(HtmlRenderer.INDENT_SIZE, 4)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, false)
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, true);
            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            if (tableNotebook.size() < 2
                    && PopTools.askSure(getBaseTitle(), Languages.message("ImportExamples"))) {
                notebooksController.importExamples();
            } else {
                loadBooks();
            }
            refreshTags();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeTools.setTooltip(clearNotesButton, new Tooltip(Languages.message("ClearNotes")));
            NodeTools.setTooltip(deleteNotesButton, new Tooltip(Languages.message("DeleteNotes")));
            NodeTools.setTooltip(moveDataNotesButton, new Tooltip(Languages.message("MoveNotes")));
            NodeTools.setTooltip(copyNotesButton, new Tooltip(Languages.message("CopyNotes")));
            NodeTools.setTooltip(synchronizeButton, Languages.message("SynchronizeChangesToOtherPanes") + "\nF1");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void clearQuery() {
        notebooksController.selectedNode = null;
        notebooksController.changedNode = null;
        queryConditions = null;
        queryConditionsString = null;
        tableData.clear();
        notesConditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        currentPageStart = 1;
    }

    /*
        book
     */
    protected void loadBooks() {
        notebooksController.loadTree();
        refreshTimes();
    }

    protected void loadBook(Notebook book) {
        clearQuery();
        notebooksController.selectedNode = book;
        if (book != null) {
            queryConditions = " notebook=" + book.getNbid();
            loadTableData();
        }
    }

    protected void bookChanged(Notebook book) {
        if (book == null) {
            return;
        }
        if (bookOfCurrentNote != null && book.getNbid() == bookOfCurrentNote.getNbid()) {
            bookOfCurrentNote = book;
            updateBookOfCurrentNote();
        }
        if (notebooksController.selectedNode != null && notebooksController.selectedNode.getNbid() == book.getNbid()) {
            notebooksController.selectedNode = book;
            makeConditionPane();
        }
    }

    /*
        Tags
     */
    protected void popTagMenu(MouseEvent event, Tag tag) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (tag != null) {
            menu = new MenuItem(tag.getTag());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(Languages.message("Add"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addTag();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameTag();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Query"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            queryTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(Languages.message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshTags();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(Languages.message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    @FXML
    public void refreshTags() {
        synchronized (this) {
            tagsList.getItems().clear();
            tagsPane.setDisable(true);
            SingletonTask tagsTask = new SingletonTask<Void>() {
                private List<Tag> tags;

                @Override
                protected boolean handle() {
                    tags = tableTag.readAll();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().setAll(tags);
                }

                @Override
                protected void finalAction() {
                    tagsPane.setDisable(false);
                }

            };
            tagsTask.setSelf(tagsTask);
            Thread thread = new Thread(tagsTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void addTag() {
        addTag(false);
    }

    @FXML
    public void selectAllTags() {
        tagsList.getSelectionModel().selectAll();
    }

    @FXML
    public void selectNoneTags() {
        tagsList.getSelectionModel().clearSelection();
    }

    @FXML
    protected void renameTag() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            int index = tagsList.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                return;
            }
            Tag tag = tagsList.getItems().get(index);
            String name = PopTools.askValue(getBaseTitle(), tag.getTag(), Languages.message("Rename"), tag.getTag() + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Tag updated;

                @Override
                protected boolean handle() {
                    updated = new Tag(name).setTgid(tag.getTgid());
                    updated = tableTag.updateData(updated);
                    return updated != null;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().set(index, updated);
                    refreshNoteTags();
                    popSuccessful();
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void queryTags() {
        List<Tag> selected = tagsList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(Languages.message("TagsQueryComments"));
            return;
        }
        clearQuery();
        queryConditions = TableNote.tagsCondition(selected);
        queryConditionsString = Languages.message("Tag") + ":";
        for (Tag tag : selected) {
            queryConditionsString += " " + tag.getTag();
        }
        loadTableData();
    }

    @FXML
    public void clearTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(getBaseTitle(), Languages.message("Tags"), Languages.message("SureDeleteAll"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return tableTag.clearData() >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().clear();
                    noteTagsList.getItems().clear();
                    popSuccessful();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void deleteTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Tag> selected = tagsList.getSelectionModel().getSelectedItems();
            if (selected == null) {
                return;
            }
            if (!PopTools.askSure(getBaseTitle(), Languages.message("Tags"), Languages.message("SureDelete"))) {
                return;
            }
            List<Tag> tags = new ArrayList<>();
            tags.addAll(selected);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return tableTag.deleteData(tags) >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().removeAll(tags);
                    refreshNoteTags();
                    popSuccessful();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        Times
     */
    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            timesBox.setDisable(true);
            SingletonTask timesTask = new SingletonTask<Void>() {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    times = TableNote.times();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    timeController.loadTree("update_time", times, false);
                }

                @Override
                protected void finalAction() {
                    timesBox.setDisable(false);
                }

            };
            timesTask.setSelf(timesTask);
            Thread thread = new Thread(timesTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(Languages.message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        Search
     */
    @FXML
    protected void search() {
        String s = searchController.value();
        if (s == null || s.isBlank()) {
            popError(Languages.message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(Languages.message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = Languages.message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }

        } else {
            queryConditions = null;
            queryConditionsString = Languages.message("Contents") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( html like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
    }

    /*
        Notes list
     */
    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        makeConditionPane();
    }

    public void makeConditionPane() {
        notesConditionBox.getChildren().clear();
        if (notebooksController.selectedNode == null) {
            if (queryConditionsString != null) {
                conditionLabel.setText(queryConditionsString);
                notesConditionBox.getChildren().add(conditionLabel);
            }
            notesConditionBox.applyCss();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>() {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableNotebook.ancestor(notebooksController.selectedNode.getNbid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    List<Node> nodes = new ArrayList<>();
                    if (ancestor != null) {
                        for (Notebook book : ancestor) {
                            Hyperlink link = new Hyperlink(book.getName());
                            link.setWrapText(true);
                            link.setMinHeight(Region.USE_PREF_SIZE);
                            link.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    loadBook(book);
                                }
                            });
                            nodes.add(link);
                            nodes.add(new Label(">"));
                        }
                    }
                    Label label = new Label(notebooksController.selectedNode.getName());
                    label.setWrapText(true);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    nodes.add(label);
                    namesPane.getChildren().setAll(nodes);
                    notesConditionBox.getChildren().setAll(namesPane, subCheck);
                    notesConditionBox.applyCss();
                }
            };
            bookTask.setSelf(bookTask);
            Thread thread = new Thread(bookTask);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @Override
    public int readDataSize() {
        if (notebooksController.selectedNode != null && subCheck.isSelected()) {
            return TableNote.withSubSize(tableNotebook, notebooksController.selectedNode.getNbid());

        } else if (queryConditions != null) {
            return tableNote.conditionSize(queryConditions);

        } else {
            return 0;
        }
    }

    @Override
    public List<Note> readPageData() {
        if (notebooksController.selectedNode != null && subCheck.isSelected()) {
            return tableNote.withSub(tableNotebook, notebooksController.selectedNode.getNbid(), currentPageStart - 1, currentPageSize);

        } else if (queryConditions != null) {
            return tableNote.queryConditions(queryConditions, currentPageStart - 1, currentPageSize);

        } else {
            return null;
        }
    }

    @Override
    protected int clearData() {
        if (queryConditions != null) {
            return tableNote.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(Languages.message("DeleteNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("MoveNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("AddNote"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addBookNote();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("ClearNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearNotes();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(Languages.message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(Languages.message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(Languages.message("Refresh"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                refreshAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemClicked() {
        editAction(null);
    }

    @Override
    protected int checkSelected() {
        if (isSettingValues) {
            return -1;
        }
        int selection = super.checkSelected();
        deleteNotesButton.setDisable(selection == 0);
        copyNotesButton.setDisable(selection == 0);
        moveDataNotesButton.setDisable(selection == 0);
        selectedLabel.setText(Languages.message("Selected") + ": " + selection);
        return selection;
    }

    @FXML
    protected void refreshNotes() {
        refreshAction();
    }

    @FXML
    protected void addBookNote() {
        bookOfCurrentNote = notebooksController.selectedNode;
        editNote(null);
    }

    @FXML
    protected void copyNotes() {
        NotesCopyNotesController.oneOpen(this);
    }

    @FXML
    protected void moveNotes() {
        NotesMoveNotesController.oneOpen(this);
    }

    @FXML
    protected void clearNotes() {
        clearAction();
        refreshTimes();
    }

    @FXML
    protected void deleteNotes() {
        deleteAction();
        refreshTimes();
    }

    @Override
    protected int deleteData(List<Note> data) {
        int ret = super.deleteData(data);
        if (ret <= 0) {
            return ret;
        }
        if (currentNote == null || data == null || data.isEmpty()) {
            return 0;
        }
        for (Note note : data) {
            if (note.getNtid() == currentNote.getNtid()) {
                Platform.runLater(() -> {
                    editNote(null);
                });
                break;
            }
        }
        return ret;
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        Note note = tableView.getSelectionModel().getSelectedItem();
        if (note != null) {
            editNote(note);
        }
    }

    /*
        edit note
     */
    @FXML
    protected void addNote() {
        editNote(null);
    }

    protected void editNote(Note note) {
        if (!checkBeforeNextAction()) {
            return;
        }
        currentNote = note;
        loadNote();
    }

    protected void loadNote() {
        synchronized (this) {
            if (currentNote != null) {
                idInput.setText(currentNote.getNtid() + "");
                titleInput.setText(currentNote.getTitle());
                timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
            } else {
                idInput.setText("");
                timeInput.setText("");
                titleInput.setText(Languages.message("Note"));
            }
            SingletonTask loadTask = new SingletonTask<Void>() {

                private String html, md;

                @Override
                protected boolean handle() {
                    try {
                        if (currentNote != null) {
                            html = currentNote.getHtml();
                            md = htmlConverter.convert(html);
                        } else {
                            html = "";
                            md = "";
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.console(e);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    setContents(html, md);
                    isSettingValues = false;
                    updateBookOfCurrentNote();
                    refreshNoteTags();
                    showRightPane();
                    noteChanged(false);
                }

            };
            loadTask.setSelf(loadTask);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void setContents(String html, String md) {
        String style = UserConfig.getUserConfigString(baseName + "Style", HtmlStyles.DefaultStyle);
        if (style != null && !style.isBlank()) {
            style = "<!DOCTYPE html><html><head><style type=\"text/css\">\n" + style + "\n</style></head></html>";
        } else {
            style = "";
        }
        codesController.load(html);
        webviewController.loadContents(style + html);
        htmlEditor.setHtmlText(style + html);
        markdownArea.setText(md);
    }

    protected void updateBookOfCurrentNote() {
        synchronized (this) {
            SingletonTask noteTask = new SingletonTask<Void>() {
                private String chainName;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (currentNote != null) {
                            if (bookOfCurrentNote == null || bookOfCurrentNote.getNbid() != currentNote.getNotebookid()) {
                                bookOfCurrentNote = tableNotebook.find(conn, currentNote.getNotebookid());
                            }
                        }
                        if (bookOfCurrentNote == null) {
                            bookOfCurrentNote = notebooksController.root(conn);
                        }
                        if (bookOfCurrentNote == null) {
                            return false;
                        }
                        chainName = notebooksController.chainName(conn, bookOfCurrentNote);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    notebookLabel.setText(chainName);
                }
            };
            noteTask.setSelf(noteTask);
            Thread thread = new Thread(noteTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void copyNote() {
        if (!checkBeforeNextAction()) {
            return;
        }
        idInput.setText("");
        timeInput.setText("");
        titleInput.setText(titleInput.getText() + " - " + Languages.message("Copy"));
        currentNote = null;
        noteChanged(true);
    }

    @FXML
    protected void recoverNote() {
        loadNote();
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            String title = titleInput.getText();
            if (title == null || title.isBlank()) {
                popError(Languages.message("TitleNonEmpty"));
                return;
            }
            rightPane.setDisable(true);
            SingletonTask saveTask = new SingletonTask<Void>() {
                private Note note;
                private String html, md;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        note = new Note();
                        note.setTitle(title);
                        Tab tab = noteEditPane.getSelectionModel().getSelectedItem();
                        if (tab == noteEditorTab) {
                            html = HtmlReadTools.body(htmlEditor.getHtmlText(), false);
                            md = htmlConverter.convert(html);
                        } else if (tab == noteMarkdownTab) {
                            md = markdownArea.getText();
                            html = md2html(md);
                        } else {
                            html = HtmlReadTools.body(codesController.codes(), false);
                            md = htmlConverter.convert(html);
                        }
                        note.setHtml(html);
                        note.setUpdateTime(new Date());
                        if (currentNote != null) {
                            note.setNtid(currentNote.getNtid());
                            note.setNotebook(currentNote.getNotebookid());
                            currentNote = tableNote.updateData(conn, note);
                        } else {
                            if (bookOfCurrentNote == null) {
                                bookOfCurrentNote = notebooksController.root(conn);
                            }
                            note.setNotebook(bookOfCurrentNote.getNbid());
                            currentNote = tableNote.insertData(conn, note);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return currentNote != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    idInput.setText(currentNote.getNtid() + "");
                    timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
                    isSettingValues = true;
                    setContents(html, md);
                    isSettingValues = false;
                    updateBookOfCurrentNote();
                    if (notebooksController.selectedNode != null
                            && notebooksController.selectedNode.getNbid() == currentNote.getNotebookid()) {
                        refreshNotes();
                    }
                    refreshTimes();
                    noteChanged(false);
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            saveTask.setSelf(saveTask);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void synchronize() {
        synchronized (this) {
            SingletonTask syTask = new SingletonTask<Void>() {

                private String html, md;

                @Override
                protected boolean handle() {
                    try {
                        Tab tab = noteEditPane.getSelectionModel().getSelectedItem();
                        if (tab == noteEditorTab) {
                            html = HtmlReadTools.body(htmlEditor.getHtmlText(), false);
                            md = htmlConverter.convert(html);
                        } else if (tab == noteMarkdownTab) {
                            md = markdownArea.getText();
                            html = md2html(md);
                        } else if (tab == noteCodesTab) {
                            html = HtmlReadTools.body(codesController.codes(), false);
                            md = htmlConverter.convert(html);
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    setContents(html, md);
                    codesChanged(true);
                    richTextChanged(true);
                    markdownChanged(true);
                }

            };
            syTask.setSelf(syTask);
            Thread thread = new Thread(syTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void editMarkdown() {
        MarkdownEditerController controller
                = (MarkdownEditerController) openStage(Fxmls.MarkdownEditorFxml);
        controller.loadMarkdown(markdownArea.getText());
    }

    @FXML
    protected void clearNoteMarkdown() {
        markdownArea.clear();
    }

    @FXML
    public void popMarkdownValues(MouseEvent mouseEvent) {
        MenuMarkdownEditController.open(myController, markdownArea, mouseEvent);
    }

    protected String md2html(String md) {
        try {
            if (md == null || md.isBlank()) {
                return "";
            }
            com.vladsch.flexmark.util.ast.Node document = htmlParser.parse(md);
            String html = HtmlWriteTools.html(null, htmlRender.render(document));
            return HtmlReadTools.body(html, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return "";
        }
    }

    protected void codesChanged(boolean changed) {
        noteCodesTab.setText(Languages.message("HtmlCodes") + (changed ? " *" : ""));
        if (changed) {
            noteChanged = true;
        }
    }

    protected void richTextChanged(boolean changed) {
        noteEditorTab.setText(Languages.message("RichText") + (changed ? " *" : ""));
        if (changed) {
            noteChanged = true;
        }
    }

    protected void markdownChanged(boolean changed) {
        noteMarkdownTab.setText("Markdown" + (changed ? " *" : ""));
        if (changed) {
            noteChanged = true;
        }
    }

    protected void noteChanged(boolean changed) {
        if (!changed) {
            codesChanged(false);
            richTextChanged(false);
            markdownChanged(false);
        }
        noteChanged = changed;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!noteChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("NoteChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    /*
        Note Style
     */
    @FXML
    public void popDefaultStyle(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                menu = new MenuItem(Languages.message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        styleInput.setText(HtmlStyles.styleValue(style));
                        UserConfig.setUserConfigString(baseName + "Style", styleInput.getText());
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            NodeTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void setStyle() {
        UserConfig.setUserConfigString(baseName + "Style", styleInput.getText());
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    /*
        Note tags
     */
    @FXML
    public void refreshNoteTags() {
        synchronized (this) {
            noteTagsList.getItems().clear();
            noteTagsPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>() {
                private List<Tag> noteTags = null;
                private int count = 0;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<Tag> tags = tableTag.readAll(conn);
                        if (tags != null && !tags.isEmpty() && currentNote != null) {
                            List<Long> noteTagIDs = tableNoteTag.readTags(conn, currentNote.getNtid());
                            if (noteTagIDs != null && !noteTagIDs.isEmpty()) {
                                noteTags = new ArrayList<>();
                                for (Tag tag : tags) {
                                    if (noteTagIDs.contains(tag.getTgid())) {
                                        noteTags.add(count++, tag);
                                    } else {
                                        noteTags.add(tag);
                                    }
                                }
                            } else {
                                noteTags = tags;
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (noteTags != null) {
                        noteTagsList.getItems().setAll(noteTags);
                        noteTagsList.getSelectionModel().selectRange(0, count);
                    }
                }

                @Override
                protected void finalAction() {
                    noteTagsPane.setDisable(false);
                }

            };
            noteTagsTask.setSelf(noteTagsTask);
            Thread thread = new Thread(noteTagsTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void selectAllNoteTags() {
        noteTagsList.getSelectionModel().selectAll();
    }

    @FXML
    public void selectNoneNoteTags() {
        noteTagsList.getSelectionModel().clearSelection();
    }

    @FXML
    public void addNoteTag() {
        addTag(true);
    }

    public void addTag(boolean forCurrentNote) {
        synchronized (this) {
            String name = PopTools.askValue(getBaseTitle(), Languages.message("Add"), Languages.message("Tag"), Languages.message("Tag") + new Date().getTime());
            if (name == null || name.isBlank()) {
                return;
            }
            for (Tag tag : tagsList.getItems()) {
                if (name.equals(tag.getTag())) {
                    popError(Languages.message("AlreadyExisted"));
                    return;
                }
            }
            rightPane.setDisable(true);
            SingletonTask tagTask = new SingletonTask<Void>() {
                private Tag tag = null;
                private boolean updateCurrent;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        tag = tableTag.insertData(conn, new Tag(name));
                        updateCurrent = forCurrentNote && tag != null && currentNote != null;
                        if (updateCurrent) {
                            tableNoteTag.insertData(conn, new NoteTag(currentNote.getNtid(), tag.getTgid()));
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tag != null;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().add(0, tag);
                    noteTagsList.getItems().add(0, tag);
                    if (updateCurrent) {
                        noteTagsList.getSelectionModel().selectFirst();
                    }
                    popSuccessful();
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            tagTask.setSelf(tagTask);
            Thread thread = new Thread(tagTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void okNoteTags() {
        if (currentNote == null) {
            return;
        }
        synchronized (this) {
            rightPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    List<Tag> selected = noteTagsList.getSelectionModel().getSelectedItems();
                    List<Long> tags = new ArrayList();
                    if (selected != null) {
                        for (Tag tag : selected) {
                            tags.add(tag.getTgid());
                        }
                    }
                    long noteid = currentNote.getNtid();
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement statement = conn.prepareStatement(TableNoteTag.DeleteNoteTags)) {
                        statement.setLong(1, noteid);
                        statement.executeUpdate();
                        conn.setAutoCommit(false);
                        for (Long tagid : tags) {
                            tableNoteTag.insertData(conn, new NoteTag(noteid, tagid));
                        }
                        conn.commit();
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            noteTagsTask.setSelf(noteTagsTask);
            Thread thread = new Thread(noteTagsTask);
            thread.setDaemon(false);
            thread.start();
        }
    }


    /*
        static methods
     */
    public static NotesController oneOpen() {
        NotesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesController) {
                try {
                    controller = (NotesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesController) WindowTools.openStage(Fxmls.NotesFxml);
        }
        return controller;
    }

}
