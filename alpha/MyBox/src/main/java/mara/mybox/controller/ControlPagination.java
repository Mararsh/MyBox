package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.Pagination;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-2-24
 * @License Apache License Version 2.0
 */
public class ControlPagination extends BaseController {

    protected Pagination pagination;

    @FXML
    protected Button pagesButton;
    @FXML
    protected Label label;
    @FXML
    protected FlowPane flowPane;

    public void setParameters(BaseController parent, Pagination pagi) {
        try {
            parentController = parent;
            baseName = parentController.baseName + "_Pages";

            int pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            if (pageSize < 1) {
                pageSize = 50;
            }
            pagination = pagi != null ? pagi : new Pagination();
            pagination.initSize(pageSize);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateLabels() {
        try {
            long start = pagination.startRowOfCurrentPage + 1;
            long end = pagination.endRowOfCurrentPage + 1;
            String s = message("Rows") + ": " + "[" + start + "-" + end + "]"
                    + (end - start + 1);
            if (pagination.totalSize > 0) {
                s += "/" + pagination.totalSize;
            }
            if (pagination.pageSize > 0) {
                s += "   " + message("Page") + ":" + (pagination.currentPage + 1)
                        + "/" + pagination.pagesNumber;
            }
            if (pagination.selectedRows > 0) {
                s += "   " + message("Selected") + ":" + pagination.selectedRows;
            }
            label.setText(s);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setRightOrientation() {
        flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    public void setVisible(boolean show) {
        thisPane.setVisible(show);
    }

    public void hide() {
        thisPane.setVisible(false);
    }

    public void show() {
        thisPane.setVisible(true);
        updateLabels();
    }

    public boolean isVisible() {
        return thisPane.isVisible();
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

        MenuItem menu;

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

            menu = new MenuItem(message("PageTo") + "...");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                String value = PopTools.askValue(parentController.getTitle(),
                        null, message("PageTo"), pagination.currentPage + "");
                if (value == null || value.isBlank()) {
                    return;
                }
                try {
                    int v = Integer.parseInt(value) - 1;
                    if (v == pagination.currentPage) {
                        return;
                    }
                    parentController.goPage(v);
                } catch (Exception e) {
                    popError(e.toString());
                }
            });
            items.add(menu);
        }

        menu = new MenuItem(message("PageSize") + "...");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            String value = PopTools.askValue(parentController.getTitle(),
                    null, message("PageSize"), pagination.pageSize + "");
            if (value == null || value.isBlank()) {
                return;
            }
            try {
                int v = Integer.parseInt(value);
                if (v == pagination.pageSize || v <= 0) {
                    return;
                }
                pagination.pageSize = v;
                UserConfig.setInt(baseName + "PageSize", pagination.pageSize);
                parentController.pageSize(v);
            } catch (Exception e) {
                popError(e.toString());
            }
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            parentController.refreshAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

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
