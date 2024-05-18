package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class WebBrowserController extends BaseController {

    protected Map<Tab, WebAddressController> tabControllers;
    protected Tab hisTab, favoriteTab;

    @FXML
    protected Tab initTab;
    @FXML
    protected Button functionsButton;

    public WebBrowserController() {
        baseTitle = message("WebBrowser");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            tabControllers = new HashMap();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    WebAddressController controller = tabControllers.get(newValue);
                    if (controller != null) {
                        String title = controller.webViewController.title();
                        if (title == null) {
                            setTitle(baseTitle);
                        } else {
                            setTitle(baseTitle + " - " + title);
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            StyleTools.setIconTooltips(functionsButton, "iconFunction.png", message("Functions"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        popFunctionsMenu(event, initTab, null);
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        showFunctionsMenu(event, initTab, null);
    }

    public void popFunctionsMenu(Event event, Tab tab, String title) {
        if (UserConfig.getBoolean("WebBrowserFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event, tab, title);
        }
    }

    public void showFunctionsMenu(Event fevent, Tab tab, String title) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;
            if (title != null && !title.isBlank()) {
                menu = new MenuItem(StringTools.menuPrefix(title));
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            int index = tabPane.getTabs().indexOf(tab);

            menu = new MenuItem(message("AddAtRight"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                newTab(index + 1, true);
            });
            items.add(menu);

            if (tab != initTab) {
                menu = new MenuItem(message("AddAtLeft"), StyleTools.getIconImageView("iconAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    newTab(index, true);
                });
                items.add(menu);

                menu = new MenuItem(message("AddAtEnd"), StyleTools.getIconImageView("iconAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    newTab(-1, true);
                });
                items.add(menu);

                menu = new MenuItem(message("AddAtHead"), StyleTools.getIconImageView("iconAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    newTab(1, true);
                });
                items.add(menu);

                menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    tabPane.getSelectionModel().select(tab);
                });
                items.add(menu);

                menu = new MenuItem(message("Close"), StyleTools.getIconImageView("iconClose.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    tabPane.getTabs().remove(tab);
                });
                items.add(menu);

                menu = new MenuItem(message("CloseOthers"), StyleTools.getIconImageView("iconClose.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    List<Tab> tabs = new ArrayList<>();
                    tabs.addAll(tabPane.getTabs());
                    for (Tab t : tabs) {
                        if (t != initTab && t != tab) {
                            tabPane.getTabs().remove(t);
                        }
                    }
                });
                items.add(menu);

                if (index > 1) {
                    menu = new MenuItem(message("CloseAllInLeft"), StyleTools.getIconImageView("iconClose.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        for (int i = index - 1; i > 0; i--) {
                            tabPane.getTabs().remove(i);
                        }
                    });
                    items.add(menu);
                }

                if (index < tabPane.getTabs().size() - 1) {
                    menu = new MenuItem(message("CloseAllInRight"), StyleTools.getIconImageView("iconClose.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        for (int i = tabPane.getTabs().size() - 1; i > index; i--) {
                            tabPane.getTabs().remove(i);
                        }
                    });
                    items.add(menu);
                }

            }

            menu = new MenuItem(message("CloseAll"), StyleTools.getIconImageView("iconClose.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                List<Tab> tabs = new ArrayList<>();
                tabs.addAll(tabPane.getTabs());
                for (Tab t : tabs) {
                    if (t != initTab) {
                        tabPane.getTabs().remove(t);
                    }
                }
            });
            menu.setDisable(tabPane.getTabs().size() < 2);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("WebFavorites"), StyleTools.getIconImageView("iconStarFilled.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("WebHistories"), StyleTools.getIconImageView("iconHistory.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("WebBrowserFunctionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WebBrowserFunctionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setHead(Tab tab, ImageView view, String texts) {
        try {
            Button button = new Button();
            button.setGraphic(view);
            tab.setGraphic(button);
            button.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popFunctionsMenu(event, tab, texts);
                }
            });
            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    showFunctionsMenu(event, tab, texts);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected WebAddressController newTab(int index, boolean focus) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(WindowTools.class.getResource(
                    Fxmls.WebAddressFxml), AppVariables.CurrentBundle);
            Pane pane = fxmlLoader.load();
            Tab tab = new Tab();
            tab.setContent(pane);
            if (index < 0) {
                tabPane.getTabs().add(tab);
            } else {
                tabPane.getTabs().add(index, tab);
            }
            if (focus) {
                getMyStage().setIconified(false);
                tabPane.getSelectionModel().select(tab);
            }
            setHead(tab, StyleTools.getIconImageView("iconMyBox.png"), null);
            refreshStyle(pane);

            WebAddressController controller = (WebAddressController) fxmlLoader.getController();
            controller.initTab(this, tab);
            if (tabControllers == null) {
                tabControllers = new HashMap();
            }
            tabControllers.put(tab, controller);
            tab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    tabControllers.remove(tab);
                }
            });
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public WebAddressController loadAddress(String address, boolean focus) {
        WebAddressController controller = newTab(-1, focus);
        if (address != null) {
            controller.loadAddress(address);
        }
        return controller;
    }

    public WebAddressController loadContents(String contents, boolean focus) {
        WebAddressController controller = newTab(-1, focus);
        if (contents != null) {
            controller.loadContents(contents);
        }
        return controller;
    }

    public WebAddressController loadContents(String contents, String style, boolean focus) {
        WebAddressController controller = newTab(-1, focus);
        if (contents != null) {
            controller.initStyle(style);
            controller.loadContents(contents);
        }
        return controller;
    }

    public WebAddressController loadFile(File file) {
        WebAddressController controller = newTab(-1, true);
        controller.loadFile(file);
        return controller;
    }

    public void download(String address, String name) {
        if (address == null) {
            return;
        }
        String dname;
        if (name != null && !name.isBlank()) {
            dname = name;
            String nameSuffix = FileNameTools.ext(name);
            String addrSuffix = FileNameTools.ext(address);
            if (addrSuffix != null && !addrSuffix.isBlank()) {
                if (nameSuffix == null || nameSuffix.isBlank()
                        || !addrSuffix.equalsIgnoreCase(nameSuffix)) {
                    dname = name + "." + addrSuffix;
                }
            }
        } else {
            dname = address;
        }
        int pos = dname.lastIndexOf("/");
        if (pos >= 0) {
            dname = (pos < dname.length() - 1) ? dname.substring(pos + 1) : "";
        }
        File dnFile = chooseSaveFile(VisitHistory.FileType.All, dname);
        if (dnFile == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                File tmpFile = HtmlReadTools.download(this, address);
                return FileTools.override(tmpFile, dnFile);
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                browseURI(dnFile.toURI());
            }

        };
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            tabControllers.clear();
            tabControllers = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static WebBrowserController open() {
        WebBrowserController controller = (WebBrowserController) WindowTools.openStage(Fxmls.WebBrowserFxml);
        if (controller != null) {
            controller.requestMouse();
        }
        return controller;
    }

    public static WebBrowserController oneOpen() {
        WebBrowserController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebBrowserController) {
                try {
                    controller = (WebBrowserController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebBrowserController) WindowTools.openStage(Fxmls.WebBrowserFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static WebBrowserController openFile(File file) {
        WebBrowserController controller = oneOpen();
        if (controller != null && file != null) {
            controller.loadFile(file);
        }
        return controller;
    }

    public static WebBrowserController openAddress(String address, boolean focus) {
        WebBrowserController controller = oneOpen();
        if (controller != null && address != null) {
            controller.loadAddress(address, focus);
        }
        return controller;
    }

    public static WebAddressController openHtml(String contents, boolean focus) {
        WebBrowserController controller = oneOpen();
        if (controller != null && contents != null) {
            return controller.loadContents(contents, focus);
        }
        return null;
    }

    public static WebAddressController openHtml(String contents, String style, boolean focus) {
        WebBrowserController controller = oneOpen();
        if (controller != null && contents != null) {
            return controller.loadContents(contents, style, focus);
        }
        return null;
    }

}
