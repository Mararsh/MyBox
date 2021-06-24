package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.CommonTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-25
 * @License Apache License Version 2.0
 */
public class NetworkQueryDNSBatchController extends BaseController {

    protected LoadingController loadingController;

    @FXML
    protected TextArea hostsList, dnsList;

    public NetworkQueryDNSBatchController() {
        baseTitle = AppVariables.message("QueryDNSBatch");
        TipsLabelKey = "QueryDNSBatchTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            hostsList.setText(AppVariables.getUserConfigValue(baseName + "Hosts", ""));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void queryAction() {
        dnsList.clear();
        loadingController = null;
        AppVariables.setUserConfigValue(baseName + "Hosts", hostsList.getText());
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private StringBuilder s, f;

                @Override
                protected boolean handle() {
                    try {
                        String[] names = hostsList.getText().split("\n");
                        s = new StringBuilder();
                        f = new StringBuilder();
                        String host, ip;
                        for (String name : names) {
                            if (NetworkTools.isIPv4(name)) {
                                ip = name;
                                host = NetworkTools.ip2host(ip);
                            } else {
                                host = name;
                                ip = NetworkTools.host2ipv4(host);
                            }
                            if (host == null || host.isBlank() || ip == null || ip.isBlank()) {
                                f.append(name).append("\n");
                                if (loadingController != null) {
                                    loadingController.setInfo(message("Failed") + ":  " + name);
                                }
                            } else {
                                s.append(ip).append("\t ").append(host).append("\n");
                                if (loadingController != null) {
                                    loadingController.setInfo(host + "\t\t" + ip);
                                }
                            }
                            Thread.sleep(2000);
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void finalAction() {
                    dnsList.setText(s.toString());
                    dnsList.setScrollTop(0);
                    String failed = f.toString();
                    if (!failed.isBlank()) {
                        alertError(message("Failed") + ":\n" + failed);
                    }
                }

            };
            loadingController = openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = FxmlControl.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            hostsList.setText(string);
        }
    }

    @FXML
    public void github() {
        try {
            hostsList.clear();
            List<String> hosts = Arrays.asList(
                    "github.com", "github.global.ssl.fastly.net", "api.github.com", "gist.github.com",
                    "nodeload.github.com", "status.github.com", "codeload.github.com",
                    "github-production-release-asset-2e65be.s3.amazonaws.com",
                    "github-production-user-asset-6210df.s3.amazonaws.com",
                    "github-production-repository-file-5c1aeb.s3.amazonaws.com",
                    "live.github.com", "github-cloud.s3.amazonaws.com", "github-com.s3.amazonaws.com",
                    "github.community", "githubapp.com", "pages.github.com",
                    "avatars.githubusercontent.com", "avatars0.githubusercontent.com",
                    "avatars1.githubusercontent.com", "avatars2.githubusercontent.com",
                    "avatars3.githubusercontent.com", "avatars4.githubusercontent.com",
                    "avatars5.githubusercontent.com", "avatars6.githubusercontent.com",
                    "avatars7.githubusercontent.com", "avatars8.githubusercontent.com",
                    "favicons.githubusercontent.com", "githubstatus.com", "media.githubusercontent.com",
                    "camo.githubusercontent.com", "cloud.githubusercontent.com", "raw.githubusercontent.com",
                    "user-images.githubusercontent.com", "customer-stories-feed.github.com"
            );
            for (String host : hosts) {
                hostsList.appendText(host + "\n");
            }
            hostsList.setScrollTop(0);

        } catch (Exception e) {

            popError(e.toString());
        }
    }

    @FXML
    public void editAction() {
        String file;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            file = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        } else {
            file = "/etc/hosts";
        }
        browseURI(new File(file).toURI());
    }

    @FXML
    @Override
    public void copyAction() {
        if (FxmlControl.copyToSystemClipboard(dnsList.getText())) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    @FXML
    public void refreshAction() {
        try {
            String cmd;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                cmd = "ipconfig  /flushdns";
            } else if (os.contains("mac")) {
                cmd = "lookupd -flushcache";
            } else {
                cmd = "systemctl restart nscd ";
            }
            cmd = FxmlControl.askValue(baseTitle, message("FlushDNS"), message("Command"), cmd);
            if (cmd == null || cmd.isBlank()) {
                return;
            }
            String run = CommonTools.run(cmd);
            if (run == null) {
                popFailed();
            } else {
                alertInformation(run);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

}
