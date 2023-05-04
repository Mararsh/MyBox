package mara.mybox.controller;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.net.ssl.SSLSocket;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-3-25
 * @License Apache License Version 2.0
 */
public class NetworkQueryAddressController extends BaseController {

    protected String host, ip;
    protected Certificate[] chain;

    @FXML
    protected TextField addressInput;
    @FXML
    protected TextArea certArea;
    @FXML
    protected Tab certTab;
    @FXML
    protected ControlWebView infoController, ipaddressController, headerController;

    public NetworkQueryAddressController() {
        baseTitle = message("QueryNetworkAddress");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Cert);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            addressInput.setText("https://sourceforge.net");
            infoController.setParent(this);
            ipaddressController.setParent(this);
            headerController.setParent(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void showAddressHistories(Event event) {
        PopTools.popStringValues(this, addressInput, event, "NetworkQueryURLHistories");
    }

    @FXML
    protected void popAddressHistories(Event event) {
        if (UserConfig.getBoolean("NetworkQueryURLHistoriesPopWhenMouseHovering", false)) {
            showAddressHistories(event);
        }
    }

    public void queryUrl(String address) {
        addressInput.setText(address);
        handle(address);
    }

    @FXML
    @Override
    public void goAction() {
        handle(addressInput.getText());
    }

    public void handle(String address) {
        if (address == null || address.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        infoController.clear();
        ipaddressController.clear();
        headerController.clear();
        certArea.clear();
        host = null;
        ip = null;
        chain = null;
        TableStringValues.add("NetworkQueryURLHistories", address);
        task = new SingletonTask<Void>(this) {

            private String info, certString, headerTable;

            @Override
            protected boolean handle() {
                try {
                    certString = null;
                    URL url = new URI(UrlTools.checkURL(address, Charset.defaultCharset())).toURL();
                    String urlAddress = url.toString();
                    task.setInfo(message("Query") + ": " + urlAddress);
                    host = url.getHost();
                    StringTable table = new StringTable(null, urlAddress);
                    table.add(Arrays.asList(message("Address"), url.toString()));
                    table.add(Arrays.asList(message("ExternalForm"), url.toExternalForm()));
                    table.add(Arrays.asList(message("Decode"), UrlTools.decodeURL(url.toString(), Charset.defaultCharset())));
                    table.add(Arrays.asList(message("Protocal"), url.getProtocol()));
                    table.add(Arrays.asList(message("Host"), url.getHost()));
                    table.add(Arrays.asList(message("Path"), url.getPath()));
                    table.add(Arrays.asList(message("File"), url.getFile()));
                    table.add(Arrays.asList(message("Query"), url.getQuery()));
                    table.add(Arrays.asList(message("Authority"), url.getAuthority()));
                    table.add(Arrays.asList(message("Reference"), url.getRef()));
                    table.add(Arrays.asList(message("Port"), (url.getPort() < 0 ? url.getDefaultPort() : url.getPort()) + ""));

                    InetAddress inetAddress = InetAddress.getByName(host);
                    ip = inetAddress.getHostAddress();
                    table.add(Arrays.asList("IP by local lookup", ip));
                    table.add(Arrays.asList("Host", inetAddress.getHostName()));
                    table.add(Arrays.asList("Canonical Host", inetAddress.getCanonicalHostName()));
                    table.add(Arrays.asList("isAnyLocalAddress", inetAddress.isAnyLocalAddress() + ""));
                    table.add(Arrays.asList("isLinkLocalAddress", inetAddress.isLinkLocalAddress() + ""));
                    table.add(Arrays.asList("isLoopbackAddress", inetAddress.isLoopbackAddress() + ""));
                    table.add(Arrays.asList("isMulticastAddress", inetAddress.isMulticastAddress() + ""));
                    table.add(Arrays.asList("isSiteLocalAddress", inetAddress.isSiteLocalAddress() + ""));

                    info = table.html();

                    task.setInfo(message("Query") + ": " + message("Certificate"));
                    certString = readCert(url);

                    task.setInfo(message("Query") + ": " + message("Header"));
                    headerTable = HtmlReadTools.requestHeadTable(url);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                infoController.loadContents(info);
                headerController.loadContents(headerTable);
                ipaddressController.loadAddress("https://www.ipaddress.com/site/" + ip);
                certArea.setText(certString);
            }
        };
        start(task);
    }

    protected String readCert(URL url) {
        try {
            host = url.getHost();

            task.setInfo(message("Query") + ": " + host);

            SSLSocket socket = NetworkTools.sslSocket(host, 443);
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
            task.setError(e.toString());
            return e.toString();
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            addressInput.setText(string);
            goAction();
        }
    }

    @FXML
    public void saveCert() {
        if (chain == null || host == null) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(host + ".crt");
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

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
                    recordFileWritten(file);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return file.exists();
            }

            @Override
            protected void whenSucceeded() {
                TextEditorController.open(file);
            }

        };
        start(task);
    }

    public void showSaveCertMenu(Event event) { //
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

    @FXML
    public void pickSaveCert(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            saveCert();
        } else {
            showSaveCertMenu(event);
        }
    }

    @FXML
    public void popSaveCert(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showSaveCertMenu(event);
        }
    }

}
