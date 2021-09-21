package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-3-25
 * @License Apache License Version 2.0
 */
public class NetworkQueryAddressController extends HtmlTableController {

    protected String host;
    protected Certificate[] chain;

    @FXML
    protected ControlStringSelector addressController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton hostRadio, urlRadio, ipRadio;
    @FXML
    protected CheckBox ipaddressCheck, iptaobaoCheck, localCheck;
    @FXML
    protected TextArea certArea;
    @FXML
    protected Tab certTab;

    public NetworkQueryAddressController() {
        baseTitle = message("QueryNetworkAddress");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            addressController.init(this, baseName + "URL", "https://sourceforge.net", 20);

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkType();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkType() {
        if (isSettingValues) {
            return;
        }
        if (urlRadio.isSelected()) {
            addressController.refreshList(baseName + "URL", "https://sourceforge.net");
        } else if (hostRadio.isSelected()) {
            addressController.refreshList(baseName + "Host", "github.com");
        } else if (ipRadio.isSelected()) {
            addressController.refreshList(baseName + "IP", "210.75.225.254");
        }
    }

    public void queryUrl(String address) {
        isSettingValues = true;
        urlRadio.fire();
        addressController.setName(baseName + "URL");
        addressController.set(address);
        isSettingValues = false;
        query(address);
    }

    @FXML
    public void queryAction() {
        query(addressController.value());
    }

    public void query(String address) {
        if (address == null || address.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            webView.getEngine().loadContent("");
            certArea.clear();
            host = null;
            chain = null;
            addressController.refreshList();
            task = new SingletonTask<Void>() {

                private String certString;

                @Override
                protected boolean handle() {
                    certString = null;
                    LinkedHashMap<String, String> values = null;
                    if (hostRadio.isSelected()) {
                        values = NetworkTools.queryHost(address,
                                localCheck.isSelected(), iptaobaoCheck.isSelected(), ipaddressCheck.isSelected());
                    } else if (urlRadio.isSelected()) {
                        values = NetworkTools.queryURL(address,
                                localCheck.isSelected(), iptaobaoCheck.isSelected(), ipaddressCheck.isSelected());
                    } else if (ipRadio.isSelected()) {
                        values = NetworkTools.queryIP(address,
                                localCheck.isSelected(), iptaobaoCheck.isSelected(), ipaddressCheck.isSelected());
                    }
                    initTable(address);
                    if (values != null) {
                        for (String name : values.keySet()) {
                            addData(name, values.get(name));
                        }
                    }
                    certString = readCert();
                    return true;
                }

                protected String readCert() {
                    try {
                        URL url = new URL(UrlTools.checkURL(address, Charset.defaultCharset()));
                        host = url.getHost();

                        SSLContext context = SSLContext.getInstance(AppValues.HttpsProtocal);
                        context.init(null, null, null);
                        SSLSocket socket = (SSLSocket) context.getSocketFactory()
                                .createSocket(host, 443);
                        socket.setSoTimeout(UserConfig.getInt("WebConnectTimeout", 10000));
                        try {
                            socket.startHandshake();
                            socket.close();
                        } catch (Exception e) {
                        }
                        chain = socket.getSession().getPeerCertificates();
                        if (chain == null) {
                            return "Could not obtain server certificate chain";
                        }
                        StringBuilder s = new StringBuilder();
                        for (Certificate cert : chain) {
                            s.append(cert).append("\n\n----------------------------------\n\n");
                        }
                        return s.toString();
                    } catch (Exception e) {
                        return e.toString();
                    }
                }

                @Override
                protected void whenSucceeded() {
                    displayHtml();
                    certArea.setText(certString);
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            addressController.set(string);
            queryAction();
        }
    }

    @FXML
    public void saveCert() {
        if (chain == null || host == null) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(VisitHistory.FileType.Cert, host + ".crt");
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        StringBuilder s = new StringBuilder();
                        Base64.Encoder encoder = Base64.getEncoder();
                        for (Certificate cert : chain) {
                            s.append("-----BEGIN CERTIFICATE-----\n");
                            String certString = encoder.encodeToString(cert.getEncoded());
                            while (true) {
                                if (certString.length() <= 64) {
                                    s.append(certString).append("\n");
                                    break;
                                }
                                s.append(certString.substring(0, 64)).append("\n");
                                certString = certString.substring(64);
                            }
                            s.append("-----END CERTIFICATE-----\n");
                        }
                        TextFileTools.writeFile(file, s.toString());
                        recordFileWritten(file, VisitHistory.FileType.Cert);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return file.exists();
                }

                @Override
                protected void whenSucceeded() {
                    ControllerTools.openTextEditer(null, file);
                }

            };
            start(task);
        }
    }

    @FXML
    public void popSaveCert(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                saveCert();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.setFileType(VisitHistory.FileType.Cert).pop();
    }

}
