package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.jsoup.Jsoup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2021-7-6
 * @License Apache License Version 2.0
 */
public class BaseWebViewController extends BaseWebViewController_Assist {

    public BaseWebViewController() {
    }

    public void setParameters(BaseController parent) {
        if (parent == null) {
            return;
        }
        this.parentController = parent;
        this.baseName = parent.baseName;
        myController = this;
    }

    public void setParameters(BaseController parent, WebView webView) {
        setParameters(parent);
        this.webView = webView;
        webView.setUserData(this);
        this.setFileType();
        initWebView();
    }

    @FXML
    public void zoomIn() {
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    public void zoomOut() {
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    public void backAction() {
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    public void forwardAction() {
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    public void refreshAction() {
        goAction();
    }

    public String currentHtml() {
        return WebViewTools.getHtml(webEngine);
    }

    @FXML
    @Override
    public void saveAsAction() {
        String name;
        if (sourceFile != null) {
            name = FileNameTools.appendName(sourceFile.getName(), "m");
        } else {
            name = new Date().getTime() + ".htm";
        }
        final File file = chooseSaveFile(UserConfig.getUserConfigPath(baseName + "TargetPath"),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String html = currentHtml();
            if (html == null) {
                popError(message("NoData"));
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        Charset charset = HtmlReadTools.htmlCharset(html);
                        File tmpFile = TmpFileTools.getTempFile();
                        try ( BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                            out.write(html);
                            out.flush();
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                        return FileTools.rename(tmpFile, file);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    recordFileWritten(file);
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
    @Override
    public void cancelAction() {
        webEngine.getLoadWorker().cancel();
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            String html = WebViewTools.getHtml(webEngine);
            doc = webEngine.getDocument();
            boolean isFrameset = framesDoc != null && framesDoc.size() > 0;

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(address);
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            if (backwardButton == null) {
                int hisSize = (int) webEngine.executeScript("window.history.length;");

                menu = new MenuItem(message("ZoomIn"));
                menu.setOnAction((ActionEvent event) -> {
                    zoomIn();
                });
                items.add(menu);

                menu = new MenuItem(message("ZoomOut"));
                menu.setOnAction((ActionEvent event) -> {
                    zoomOut();
                });
                items.add(menu);

                menu = new MenuItem(message("Refresh"));
                menu.setOnAction((ActionEvent event) -> {
                    refreshAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Cancel"));
                menu.setOnAction((ActionEvent event) -> {
                    cancelAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Backward"));
                menu.setOnAction((ActionEvent event) -> {
                    backAction();
                });
                menu.setDisable(hisSize < 2);
                items.add(menu);

                menu = new MenuItem(message("Forward"));
                menu.setOnAction((ActionEvent event) -> {
                    forwardAction();
                });
                menu.setDisable(hisSize < 2);
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddAsFavorite"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoriteAddController controller = (WebFavoriteAddController) WindowTools.openStage(Fxmls.WebFavoriteAddFxml);
                controller.setValues(webEngine.getTitle(), address);

            });
            menu.setDisable(address == null || address.isBlank());
            items.add(menu);

            menu = new MenuItem(message("WebFavorites"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("WebHistories"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            items.add(menu);

            if (address != null && address.isBlank()) {
                menu = new MenuItem(message("QueryNetworkAddress"));
                menu.setOnAction((ActionEvent event) -> {
                    NetworkQueryAddressController controller
                            = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
                    controller.queryUrl(address);
                });
                items.add(menu);
            }

            if (!(parentController instanceof HtmlSnapController)) {
                menu = new MenuItem(message("HtmlSnap"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlSnapController controller = (HtmlSnapController) WindowTools.openStage(Fxmls.HtmlSnapFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else if (html != null && !html.isBlank()) {
                        controller.loadContents(html);
                    }
                });
                menu.setDisable(html == null || html.isBlank());
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            List<MenuItem> editItems = new ArrayList<>();

            if (!(parentController instanceof HtmlEditorController)) {
                menu = new MenuItem(message("HtmlEditor"));
                menu.setOnAction((ActionEvent event) -> {
                    edit(html);
                });
                menu.setDisable(html == null || html.isBlank());
                editItems.add(menu);
            }

            if (isFrameset) {
                NodeList frameList = webEngine.getDocument().getElementsByTagName("frame");
                if (frameList != null) {
                    List<MenuItem> frameItems = new ArrayList<>();
                    for (int i = 0; i < frameList.getLength(); i++) {
                        org.w3c.dom.Node node = frameList.item(i);
                        if (node == null) {
                            continue;
                        }
                        int index = i;
                        Element element = (Element) node;
                        String src = element.getAttribute("src");
                        String name = element.getAttribute("name");
                        String frame = message("Frame") + index;
                        if (name != null && !name.isBlank()) {
                            frame += " :   " + name;
                        } else if (src != null && !src.isBlank()) {
                            frame += " :   " + src;
                        }
                        menu = new MenuItem(frame);
                        menu.setOnAction((ActionEvent event) -> {
                            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
                            if (src != null && !src.isBlank()) {
                                controller.loadAddress(UrlTools.fullAddress(address, src));
                            } else {
                                controller.loadContents(WebViewTools.getFrame(webEngine, index));
                            }

                        });
                        menu.setDisable(html == null || html.isBlank());
                        frameItems.add(menu);
                    }
                    if (!frameItems.isEmpty()) {
                        Menu frameMenu = new Menu(message("Frame"));
                        frameMenu.getItems().addAll(frameItems);
                        editItems.add(frameMenu);
                    }
                }
            }

            if (!editItems.isEmpty()) {
                editItems.add(new SeparatorMenuItem());
                items.addAll(editItems);
            }

            menu = new MenuItem(message("WebFind"));
            menu.setOnAction((ActionEvent event) -> {
                find(html);
            });
            menu.setDisable(html == null || html.isBlank());
            items.add(menu);

            menu = new MenuItem(message("WebElements"));
            menu.setOnAction((ActionEvent event) -> {
                WebElementsController controller = (WebElementsController) WindowTools.openStage(Fxmls.WebElementsFxml);
                if (address != null && !address.isBlank()) {
                    controller.loadAddress(address);
                } else if (html != null && !html.isBlank()) {
                    controller.loadContents(html);
                }
                controller.toFront();
            });
            menu.setDisable(html == null || html.isBlank());
            items.add(menu);

            Menu elementsMenu = new Menu(message("Extract"));
            List<MenuItem> elementsItems = new ArrayList<>();

            menu = new MenuItem(message("Texts"));
            menu.setOnAction((ActionEvent event) -> {
                texts(html);
            });
            menu.setDisable(isFrameset || html == null || html.isBlank());
            elementsItems.add(menu);

            menu = new MenuItem(message("Links"));
            menu.setOnAction((ActionEvent event) -> {
                links();
            });
            menu.setDisable(isFrameset || doc == null);
            elementsItems.add(menu);

            menu = new MenuItem(message("Images"));
            menu.setOnAction((ActionEvent event) -> {
                images();
            });
            menu.setDisable(isFrameset || doc == null);
            elementsItems.add(menu);

            menu = new MenuItem(message("Headings"));
            menu.setOnAction((ActionEvent event) -> {
                toc(html);
            });
            menu.setDisable(isFrameset || html == null || html.isBlank());
            elementsItems.add(menu);

            elementsMenu.getItems().setAll(elementsItems);
            items.add(elementsMenu);

            items.add(new SeparatorMenuItem());

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("OpenLinkBySystem"));
                menu.setOnAction((ActionEvent event) -> {
                    browse(address);
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("OpenLinkInNewTab"));
                menu.setOnAction((ActionEvent event) -> {
                    WebBrowserController c = WebBrowserController.oneOpen();
                    c.loadAddress(address, false);
                });
                items.add(menu);

                menu = new MenuItem(message("OpenLinkInNewTabSwitch"));
                menu.setOnAction((ActionEvent event) -> {
                    WebBrowserController c = WebBrowserController.oneOpen();
                    c.loadAddress(address, true);
                });
                items.add(menu);
            }

            menu = new MenuItem(message("PopupClose"));
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
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void find(String html) {
        WebFindController controller = (WebFindController) WindowTools.openStage(Fxmls.WebFindFxml);
        controller.loadContents(html);
        controller.setAddress(address);
        controller.toFront();
    }

    @FXML
    @Override
    public void findAction() {
        find(WebViewTools.getHtml(webEngine));
    }

    public HtmlEditorController edit(String html) {
        HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
        if (address != null && !address.isBlank()) {
            controller.loadAddress(address);
        } else if (html != null && !html.isBlank()) {
            controller.loadContents(html);
        }
        return controller;
    }

    @FXML
    public void editAction() {
        edit(WebViewTools.getHtml(webEngine));
    }

    protected void links() {
        doc = webEngine.getDocument();
        if (doc == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("a");
            if (aList == null || aList.getLength() < 1) {
                if (parentController != null) {
                    parentController.popInformation(message("NoData"));
                }
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getTextContent();
                String title = element.getAttribute("title");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? title : name) + "</a>",
                        name == null ? "" : name,
                        title == null ? "" : title,
                        URLDecoder.decode(href, charset),
                        URLDecoder.decode(linkAddress, charset)
                ));
                table.add(row);
                index++;
            }
            table.editHtml();
        } catch (Exception e) {
            if (parentController != null) {
                parentController.popError(e.toString());
            }
        }
    }

    protected void images() {
        doc = webEngine.getDocument();
        if (doc == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("img");
            if (aList == null || aList.getLength() < 1) {
                if (parentController != null) {
                    parentController.popInformation(message("NoData"));
                }
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("src");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getAttribute("alt");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? message("Link") : name) + "</a>",
                        "<img src=\"" + linkAddress + "\" " + (name == null ? "" : "alt=\"" + name + "\"") + " width=100/>",
                        name == null ? "" : name,
                        URLDecoder.decode(href, charset),
                        URLDecoder.decode(linkAddress, charset)
                ));
                table.add(row);
                index++;
            }
            table.editHtml();
        } catch (Exception e) {
            if (parentController != null) {
                parentController.popError(e.toString());
            }
        }
    }

    protected void toc(String html) {
        if (html == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        String toc = HtmlReadTools.toc(html, 8);
        if (toc == null || toc.isBlank()) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        c.loadContents(toc);
        c.toFront();
    }

    protected void texts(String html) {
        if (html == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        String texts = Jsoup.parse(html).wholeText();
        if (texts == null || texts.isBlank()) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        c.loadContents(texts);
        c.toFront();
    }

    @FXML
    @Override
    public void popAction() {
        HtmlEditorController controller = edit(WebViewTools.getHtml(webEngine));
        controller.setAsPopup(baseName + "Pop");
    }

    @Override
    public boolean controlAltO() {
        selectNoneAction();
        return true;
    }

    @FXML
    @Override
    public void selectNoneAction() {
        WebViewTools.selectNone(webView.getEngine());
    }

    @Override
    public boolean controlAltU() {
        selectAction();
        return true;
    }

    @FXML
    @Override
    public void selectAction() {
        WebViewTools.selectElement(webView, element);
    }

    @Override
    public boolean controlAltT() {
        copyTextToSystemClipboard();
        return true;
    }

    @FXML
    public void copyTextToSystemClipboard() {
        if (webView == null) {
            return;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, text);
    }

    @Override
    public boolean controlAltH() {
        copyHtmlToSystemClipboard();
        return true;
    }

    @FXML
    public void copyHtmlToSystemClipboard() {
        String html = WebViewTools.selectedHtml(webView.getEngine());
        if (html == null || html.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, html);
    }

}
