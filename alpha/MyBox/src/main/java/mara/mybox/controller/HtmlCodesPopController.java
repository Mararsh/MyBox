package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public class HtmlCodesPopController extends TextPopController {

    protected WebView sourceWebView;
    protected String separateLine;
    protected Clipboard clipboard;

    public HtmlCodesPopController() {
        baseTitle = message("HtmlCodes");
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            textArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuHtmlCodesController.open(myController, textArea, event);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setWebView(String baseName, WebView sourceWebView) {
        try {
            this.baseName = baseName;
            this.sourceWebView = sourceWebView;
            refreshAction();

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void checkSychronize() {
        if (sourceWebView != null) {
            if (listener == null) {
                listener = new ChangeListener<State>() {
                    @Override
                    public void changed(ObservableValue ov, State oldv, State newv) {
                        if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                            refreshAction();
                        }
                    }
                };
            }
            if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                sourceWebView.getEngine().getLoadWorker().stateProperty().addListener(listener);
            } else {
                sourceWebView.getEngine().getLoadWorker().stateProperty().removeListener(listener);
            }
        } else {
            super.checkSychronize();
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (sourceInput != null) {
            textArea.setText(sourceInput.getText());

        } else if (sourceWebView != null) {
            textArea.setText(WebViewTools.getHtml(sourceWebView));

        } else {
            sychronizedCheck.setVisible(false);
            refreshButton.setVisible(false);
        }
    }

    @FXML
    @Override
    public void editAction() {
        HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(textArea.getText());
        controller.toFront();
    }

    @FXML
    @Override
    public void saveAsAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            File file = chooseSaveFile();
            if (file == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        File tmpFile = HtmlWriteTools.writeHtml(textArea.getText());
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
                    HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
                    controller.sourceFileChanged(file);
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static HtmlCodesPopController openInput(BaseController parent, TextInputControl textInput) {
        try {
            if (textInput == null) {
                return null;
            }
            HtmlCodesPopController controller = (HtmlCodesPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlCodesPopFxml, false);
            controller.setSourceInput(parent.baseName, textInput);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlCodesPopController openWebView(BaseController parent, WebView srcWebView) {
        try {
            if (srcWebView == null) {
                return null;
            }
            HtmlCodesPopController controller = (HtmlCodesPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlCodesPopFxml, false);
            controller.setWebView(parent.baseName, srcWebView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
