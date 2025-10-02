package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import mara.mybox.data.Pagination;
import mara.mybox.data.Pagination.ObjectType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-2-24
 * @License Apache License Version 2.0
 */
public class ControlPagination extends BaseController {

    @FXML
    protected FlowPane menuPane;
    @FXML
    protected HBox navigatorBox;
    @FXML
    protected ComboBox<String> pageSelector, pageSizeSelector;
    @FXML
    protected Button pagesButton;
    @FXML
    protected Label menuPagesLabel, menuRowsLabel, menuSelectionLabel,
            perPageLabel, pagesLabel, rowsLabel, selectionLabel;

    public void setParameters(BaseController parent, Pagination pagi, ObjectType type) {
        try {
            parentController = parent;
            baseName = parentController.baseName + "_Pages";

            pagination = pagi != null ? pagi : new Pagination();
            pagination.init(type);
            pagination.pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            if (pagination.pageSize < 1) {
                pagination.pageSize = pagination.defaultPageSize;
            }

            List<String> sizeValues = new ArrayList();
            switch (pagination.objectType) {
                case Table:
                    perPageLabel.setText(message("RowsPerPage"));
                    sizeValues.addAll(Arrays.asList("50", "20", "100", "10", "300", "500", "600", "800", "1000", "2000"));
                    break;
                case Bytes:
                    perPageLabel.setText(message("BytesPerPage"));
                    sizeValues.addAll(Arrays.asList("100,000", "500,000", "50,000", "10,000", "20,000",
                            "200,000", "1,000,000", "2,000,000", "20,000,000", "200,000,000"));
                    break;
                case Text:
                    perPageLabel.setText(message("RowsPerPage"));
                    sizeValues.addAll(Arrays.asList("200", "500", "100", "300", "600", "50", "20", "800", "1000", "2000"));
                    break;

            }

            pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    goPage(pageSelector.getValue());
                }
            });

            pageSizeSelector.getItems().addAll(sizeValues);
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    setPageSize(pageSizeSelector.getValue());
                }
            });

            updateStatus(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void reset() {
        pagination.reset();
        if (thisPane.isVisible()) {
            updateStatus(true);
        }
    }

    public void setSelection(String info) {
        pagination.selection = info;
        menuSelectionLabel.setText(info);
        selectionLabel.setText(info);
    }

    public void setBar() {
        boolean show = false;
        String v = UserConfig.getString(baseName + "ShowBar", "show");
        if ("show".equalsIgnoreCase(v)) {
            show = true;
        } else if ("auto".equalsIgnoreCase(v)) {
            double width = thisPane.getWidth();
            if (getMyStage() != null) {
                width = Math.min(width, getMyStage().getWidth());
            }
            if (parentController != null && parentController.getMyStage() != null) {
                width = Math.min(width, parentController.getMyStage().getWidth());
            }
            if (width > 1000) {
                show = true;
            }
        }
        if (show) {
            if (!thisPane.getChildren().contains(navigatorBox)) {
                thisPane.getChildren().add(0, navigatorBox);
            }
        } else {
            if (thisPane.getChildren().contains(navigatorBox)) {
                thisPane.getChildren().remove(navigatorBox);
            }
        }
    }

    public void updateStatus(boolean show) {
        try {
            thisPane.getChildren().clear();
            thisPane.setVisible(show);
            if (!show) {
                return;
            }
            isSettingValues = true;
            thisPane.getChildren().add(menuPane);

            setBar();

            String rows = message("Rows") + ": ";
            if (pagination.rowsNumber > 0) {
                long start = pagination.startRowOfCurrentPage + 1;
                long end = pagination.endRowOfCurrentPage;
                rows += "[" + start + "-" + end + "]"
                        + (end - start + 1);
                rows += "/" + pagination.rowsNumber;
            } else {
                rows += "0";
            }
            menuRowsLabel.setText(rows);
            rowsLabel.setText(rows);

            if (pagination.pageSize > 0) {
                menuPagesLabel.setText(message("Page") + ":"
                        + (pagination.currentPage + 1) + "/" + pagination.pagesNumber);
                pagesLabel.setText("/" + pagination.pagesNumber);
            } else {
                menuPagesLabel.setText(null);
                pagesLabel.setText(null);
            }

            menuSelectionLabel.setText(pagination.selection);
            selectionLabel.setText(pagination.selection);

            List<String> pages = new ArrayList<>();
            for (long i = Math.max(1, pagination.currentPage - 3);
                    i <= Math.min(pagination.pagesNumber, pagination.currentPage + 3); i++) {
                pages.add(i + "");
            }
            for (long i = 1; i <= Math.min(pagination.pagesNumber, 3); i++) {
                String v = i + "";
                if (!pages.contains(v)) {
                    pages.add(v);
                }
            }
            for (long i = pagination.pagesNumber; i >= Math.max(1, pagination.pagesNumber - 3); i--) {
                String v = i + "";
                if (!pages.contains(v)) {
                    pages.add(v);
                }
            }
            pageSelector.getItems().setAll(pages);
            pageSelector.setValue((pagination.currentPage + 1) + "");
            pageSizeSelector.setValue(pagination.pageSize + "");

            pageNextButton.setDisable(!hasNextPage());
            pagePreviousButton.setDisable(!hasPreviousPage());
            pageFirstButton.setDisable(!hasPreviousPage());
            pageLastButton.setDisable(!hasNextPage());
            pageSelector.setDisable(!multiplePages());
            pageSizeSelector.setDisable(!multipleRows());

            setLeftOrientation(UserConfig.getBoolean(baseName + "Left", true));

            isSettingValues = false;

            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void setLeftOrientation(boolean isLeft) {
        if (isSettingValues) {
            return;
        }
        if (isLeft) {
            menuPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            navigatorBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        } else {
            menuPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            navigatorBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    public void show(boolean show) {
        thisPane.setVisible(show);
    }

    public boolean isVisible() {
        return thisPane.isVisible();
    }

    public boolean multipleRows() {
        return pagination.multipleRows();
    }

    public boolean multiplePages() {
        return pagination.multiplePages();
    }

    public boolean hasNextPage() {
        return pagination.hasNextPage();
    }

    public boolean hasPreviousPage() {
        return pagination.hasPreviousPage();
    }

    protected void goPage(String value) {
        try {
            if (isSettingValues || parentController == null
                    || value == null || value.isBlank()) {
                return;
            }
            int v = Integer.parseInt(value) - 1;
            if (!pagination.isValidPage(v)) {
                popError(message("InvalidParameter") + ": " + message("Page"));
                return;
            }
            parentController.loadPage(v);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    protected void setPageSize(String value) {
        try {
            if (isSettingValues || parentController == null
                    || value == null || value.isBlank()) {
                return;
            }
            int v = Integer.parseInt(value.replaceAll(",", ""));
            if (v == pagination.pageSize || v <= 0) {
                return;
            }
            pagination.updatePageSize(v);
            UserConfig.setInt(baseName + "PageSize", pagination.pageSize);
            parentController.loadPage(pagination.currentPage);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @FXML
    public void popPagesMemu(Event event) {
        if (UserConfig.getBoolean(baseName + "MenuPopWhenMouseHovering", true)) {
            showPagesMemu(event);
        }
    }

    @FXML
    public void showPagesMemu(Event event) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Page") + " "
                + (pagination.currentPage + 1) + "/" + pagination.pagesNumber);
        menu.setStyle(attributeTextStyle());
        items.add(menu);
        items.add(new SeparatorMenuItem());

        if (pagination.pagesNumber > 1) {
            if (pagination.currentPage < pagination.pagesNumber - 1) {
                menu = new MenuItem(message("NextPage") + "  ALT+PAGE_DOWN",
                        StyleTools.getIconImageView("iconNext.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageNextAction();
                });
                items.add(menu);
            }

            if (pagination.currentPage > 0) {
                menu = new MenuItem(message("PreviousPage") + "  ALT+PAGE_UP",
                        StyleTools.getIconImageView("iconPrevious.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pagePreviousAction();
                });
                items.add(menu);
            }

            if (pagination.currentPage > 0) {
                menu = new MenuItem(message("FirstPage") + "  ALT+HOME",
                        StyleTools.getIconImageView("iconFirst.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageFirstAction();
                });
                items.add(menu);
            }

            if (pagination.currentPage < pagination.pagesNumber - 1) {
                menu = new MenuItem(message("LastPage") + "  ALT+END",
                        StyleTools.getIconImageView("iconLast.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageLastAction();
                });
                items.add(menu);
            }

            if (pagination.pagesNumber > 1) {
                menu = new MenuItem(message("PageTo") + "...");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    String value = PopTools.askValue(parentController.getTitle(),
                            null, message("PageTo"), (pagination.pagesNumber / 2 + 1) + "");
                    goPage(value);
                });
                items.add(menu);

                Menu pageMenu = new Menu(message("Page"));
                for (String p : pageSelector.getItems()) {
                    menu = new MenuItem(message("Page") + " " + p);
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        goPage(p);
                    });
                    pageMenu.getItems().add(menu);
                }
                items.add(pageMenu);
            }
        }

        menu = new MenuItem(perPageLabel.getText() + "...");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String value = PopTools.askValue(parentController.getTitle(),
                        null, perPageLabel.getText(), pagination.pageSize + "");
                setPageSize(value);
            }
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                parentController.refreshAction();
            }
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        ToggleGroup navGroup = new ToggleGroup();
        RadioMenuItem showmenu = new RadioMenuItem(message("ShowNavigationBar"));
        showmenu.setSelected("show".equalsIgnoreCase(UserConfig.getString(baseName + "ShowBar", "show")));
        showmenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "ShowBar", "show");
                setBar();
            }
        });
        showmenu.setToggleGroup(navGroup);
        items.add(showmenu);

        RadioMenuItem hidemenu = new RadioMenuItem(message("HideNavigationBar"));
        hidemenu.setSelected("hide".equalsIgnoreCase(UserConfig.getString(baseName + "ShowBar", "show")));
        hidemenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "ShowBar", "hide");
                setBar();
            }
        });
        hidemenu.setToggleGroup(navGroup);
        items.add(hidemenu);

        RadioMenuItem automenu = new RadioMenuItem(message("Adaptive"));
        automenu.setSelected("auto".equalsIgnoreCase(UserConfig.getString(baseName + "ShowBar", "show")));
        automenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "ShowBar", "auto");
                setBar();
            }
        });
        automenu.setToggleGroup(navGroup);
        items.add(automenu);

        CheckMenuItem leftItem = new CheckMenuItem(message("Left"),
                StyleTools.getIconImageView("iconLeft.png"));
        leftItem.setSelected(UserConfig.getBoolean(baseName + "Left", true));
        leftItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "Left", leftItem.isSelected());
                setLeftOrientation(leftItem.isSelected());
            }
        });
        items.add(leftItem);

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "MenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "MenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popNodeMenu(pagesButton, items);
    }

    @FXML
    public void goPage(Event event) {
        goPage(pageSelector.getValue());
    }

    @FXML
    @Override
    public void pageNextAction() {
        if (parentController != null) {
            parentController.pageNextAction();
        }
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        if (parentController != null) {
            parentController.pagePreviousAction();
        }
    }

    @FXML
    @Override
    public void pageFirstAction() {
        if (parentController != null) {
            parentController.pageFirstAction();
        }
    }

    @FXML
    @Override
    public void pageLastAction() {
        if (parentController != null) {
            parentController.pageLastAction();
        }
    }

    @Override
    public boolean altPageUp() {
        if (parentController != null) {
            parentController.pagePreviousAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean altPageDown() {
        if (parentController != null) {
            parentController.pageNextAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean altHome() {
        if (parentController != null) {
            parentController.pageFirstAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean altEnd() {
        if (parentController != null) {
            parentController.pageLastAction();
            return true;
        }
        return false;
    }

}
