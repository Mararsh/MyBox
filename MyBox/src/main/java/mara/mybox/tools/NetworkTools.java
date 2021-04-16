package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @License Apache License Version 2.0
 */
public class NetworkTools {

    public static int findFreePort(int port) {
        int p;
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            socket.close();
            p = port;
        } catch (Exception e) {
            try {
                try ( ServerSocket serverSocket = new ServerSocket(0)) {
                    p = serverSocket.getLocalPort();
                }
            } catch (Exception ex) {
                p = -1;
            }
        }
        return p;
    }

    public static boolean isPortUsed(int port) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String installCertificates(String keyStoreFile,
            String passwd, Certificate[] certs, String[] names) {
        try {

            if (keyStoreFile == null || certs == null || names == null
                    || certs.length != names.length) {
                return message("InvalidData");
            }
            char[] passphrase = passwd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            File tmpFile = FileTools.getTempFile();
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                for (int i = 0; i < certs.length; i++) {
                    String alias = names[i].replaceAll(" ", "-");
                    keyStore.setCertificateEntry(alias, certs[i]);
                }
                keyStore.store(out, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            FileTools.rename(tmpFile, cacerts);
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificates(Certificate[] certs, String[] names) {
        return installCertificates(SystemTools.keystore(), SystemTools.keystorePassword(), certs, names);
    }

    public static String installCertificate(String keyStoreFile,
            String passwd, Certificate cert, String name) {
        if (cert == null || name == null) {
            return message("InvalidData");
        }
        Certificate[] certs = {cert};
        String[] names = {name};
        return installCertificates(keyStoreFile, passwd, certs, names);
    }

    public static Certificate getCertificateByFile(File certFile) {
        try ( FileInputStream inStream = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(inStream);
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate[] getCertificatesByFile(File certFile) {
        try ( FileInputStream inStream = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> data = cf.generateCertificates(inStream);
            if (data == null || data.isEmpty()) {
                return null;
            }
            Certificate[] certs = new Certificate[data.size()];
            int index = 0;
            for (Certificate cert : data) {
                certs[index++] = cert;
            }
            return certs;
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate getCertificateByHost(String host) {
        try {
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory()
                    .createSocket(host, 443);
            socket.setSoTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
            } catch (Exception e) {
            }
            return socket.getSession().getPeerCertificates()[0];
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate[] getCertificatesByHost(String host) {
        try {
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory()
                    .createSocket(host, 443);
            socket.setSoTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
            } catch (Exception e) {
            }
            return socket.getSession().getPeerCertificates();
        } catch (Exception e) {
            return null;
        }
    }

    public static String installCertificateByFile(String keyStoreFile,
            String passwd, File certFile, String alias, boolean wholeChain) {
        try {
            if (wholeChain) {
                Certificate[] certs = getCertificatesByFile(certFile);
                if (certs == null || certs.length == 0) {
                    return message("InvalidData");
                }
                String[] names = new String[certs.length];
                names[0] = alias;
                for (int i = 1; i < certs.length; i++) {
                    names[i] = alias + "-chain-" + i;
                }
                return installCertificates(keyStoreFile, passwd, certs, names);
            } else {
                Certificate cert = getCertificateByFile(certFile);
                if (cert == null) {
                    return message("InvalidData");
                }
                return installCertificate(keyStoreFile, passwd, cert, alias);
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificateByFile(File certFile, String alias, boolean wholeChain) {
        return installCertificateByFile(SystemTools.keystore(), SystemTools.keystorePassword(), certFile, alias, wholeChain);
    }

    // https://github.com/escline/InstallCert/blob/master/InstallCert.java
    public static String installCertificateByHost(String keyStoreFile,
            String passwd, String host, String alias, boolean wholeChain) {
        try {
            if (wholeChain) {
                Certificate[] certs = getCertificatesByHost(host);
                if (certs == null || certs.length == 0) {
                    return message("InvalidData");
                }
                String[] names = new String[certs.length];
                names[0] = alias;
                for (int i = 1; i < certs.length; i++) {
                    names[i] = alias + "-chain-" + i;
                }
                return installCertificates(keyStoreFile, passwd, certs, names);
            } else {
                Certificate cert = getCertificateByHost(host);
                if (cert == null) {
                    return message("InvalidData");
                }
                return installCertificate(keyStoreFile, passwd, cert, alias);
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificateByHost(String host, String alias, boolean wholeChain) {
        try {
            return installCertificateByHost(SystemTools.keystore(), SystemTools.keystorePassword(), host, alias, wholeChain);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return e.toString();
        }
    }

    public static String installCertificateByHost(String host, boolean wholeChain) {
        try {
            return installCertificateByHost(host, host, wholeChain);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return e.toString();
        }
    }

    public static String installHostsCertificates(List<String> hosts, boolean wholeChain) {
        if (hosts == null || hosts.isEmpty()) {
            return message("InvalidData");
        }
        try {
            List<Certificate> certsList = new ArrayList<>();
            List<String> namesList = new ArrayList<>();
            for (String host : hosts) {

                if (wholeChain) {
                    Certificate[] hostCerts = getCertificatesByHost(host);
                    if (hostCerts == null) {
                        continue;
                    }
                    for (int i = 0; i < hostCerts.length; i++) {
                        certsList.add(hostCerts[i]);
                        namesList.add(host + (i == 0 ? "" : " chain " + i));
                    }
                } else {
                    Certificate hostCert = getCertificateByHost(host);
                    if (hostCert == null) {
                        continue;
                    }
                    certsList.add(hostCert);
                    namesList.add(host);
                }
            }
            if (certsList.isEmpty()) {
                return message("InvalidData");
            }
            String keyStoreFile = SystemTools.keystore();
            String passwd = SystemTools.keystorePassword();
            Certificate[] certs = new Certificate[certsList.size()];
            String[] names = new String[certsList.size()];
            for (int i = 0; i < namesList.size(); i++) {
                certs[i] = certsList.get(i);
                names[i] = namesList.get(i);
            }
            return installCertificates(keyStoreFile, passwd, certs, names);
        } catch (Exception e) {
            return e.toString();
        }

    }

    public static boolean isHostCertificateInstalled(String host) {
        try {
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory()
                    .createSocket(host, 443);
            socket.setSoTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String uninstallCertificate(String keyStoreFile,
            String passwd, List<String> aliases) throws Exception {
        try {
            char[] passphrase = passwd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            File tmpFile = FileTools.getTempFile();
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                for (String alias : aliases) {
                    keyStore.deleteEntry(alias);
                }
                keyStore.store(out, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            FileTools.rename(tmpFile, cacerts);
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

    // https://www.cnblogs.com/starcrm/p/7071227.html
    public static InetAddress localHost() {
        try {
            InetAddress candidateAddress = null;
            MyBoxLog.debug("InetAddress.getLocalHost():" + InetAddress.getLocalHost());
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
                    ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses();
                        inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    MyBoxLog.debug("inetAddr.getHostAddress:" + inetAddr.getHostAddress());
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return InetAddress.getByName("localhost");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            return null;
        }
    }

    public static LinkedHashMap<String, String> queryURL(String urlString,
            boolean needLocal, boolean needIptaobao, boolean needIpaddress) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        try {
            if (urlString == null) {
                return null;
            }
            URL url = new URL(urlString);
            values.put(message("Address"), url.toString());
            values.put(message("ExternalForm"), url.toExternalForm());
            values.put(message("Decode"), HtmlTools.decodeURL(url.toString()));
            values.put(message("Protocal"), url.getProtocol());
            values.put(message("Host"), url.getHost());
            values.put(message("Path"), url.getPath());
            values.put(message("File"), url.getFile());
            values.put(message("Query"), url.getQuery());
            values.put(message("Authority"), url.getAuthority());
            values.put(message("Reference"), url.getRef());
            values.put(message("Port"), (url.getPort() < 0 ? url.getDefaultPort() : url.getPort()) + "");

            LinkedHashMap<String, String> hostValues
                    = queryHost(url.getHost(), needLocal, needIptaobao, needIpaddress);
            if (hostValues != null) {
                values.putAll(hostValues);
            }
        } catch (Exception e) {
            values.put("error", e.toString());
        }
        return values;
    }

    public static LinkedHashMap<String, String> queryHost(String host,
            boolean needLocal, boolean needIptaobao, boolean needIpaddress) {
        if (host == null || isIPv4(host)) {
            return queryIP(host, needLocal, needIptaobao, needIpaddress);
        }
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        if (needLocal) {
            values.put("<font color=blue>Local</font>", "");
            LinkedHashMap<String, String> ipLocal = ipLocal(host);
            if (ipLocal != null) {
                values.putAll(ipLocal);
            }
        }
        String ip = host2ipv4(host);
        LinkedHashMap<String, String> ipvalues = queryIP(ip, false, needIptaobao, needIpaddress);
        if (ipvalues != null) {
            values.putAll(ipvalues);
        }
        return values;
    }

    public static LinkedHashMap<String, String> queryIP(String ip,
            boolean needLocal, boolean needIptaobao, boolean needIpaddress) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        if (needLocal) {
            values.put("<font color=blue>Local</font>", "");
            LinkedHashMap<String, String> ipLocal = ipLocal(ip);
            if (ipLocal != null) {
                values.putAll(ipLocal);
            }
        }
        if (needIpaddress) {
            values.put("<font color=blue>ipaddress.com</font>", "");
            LinkedHashMap<String, String> ipaddress = ipaddress(ip);
            if (ipaddress != null) {
                values.putAll(ipaddress);
            }
        }
        if (needIptaobao) {
            values.put("<font color=blue>ip.taobao.com</font>", "");
            LinkedHashMap<String, String> iptaobao = iptaobao(ip, true);
            if (iptaobao != null) {
                values.putAll(iptaobao);
            }
        }
        return values;
    }

    // https://ip.taobao.com/outGetIpInfo?ip=210.75.225.254&accessKey=
    public static LinkedHashMap<String, String> iptaobao(String ip, boolean noCode) {
        try {
            if (!isIPv4(ip)) {
                return null;
            }
            String address = "https://ip.taobao.com/outGetIpInfo?ip=" + ip;
            String data = HtmlTools.url2text(address);
            List<String> keys = Arrays.asList(
                    "ip", "country", "area", "region", "city", "county", "isp",
                    "country_id", "area_id", "region_id", "city_id", "county_id", "isp_id");
            if (!noCode) {
                keys.add("code");
                keys.add("msg");
            }
            return JsonTools.jsonValues(data, keys);
        } catch (Exception e) {
            return null;
        }
    }

    public static LinkedHashMap<String, String> ipaddress(String ip) {
        try {
            if (!isIPv4(ip)) {
                return null;
            }
            String address = "https://www.ipaddress.com/ipv4/" + ip;
            String data = HtmlTools.url2text(address);
            List<String> keys = Arrays.asList(
                    "<tr><th>Reverse IP (<abbr title=\"Pointer Record\">PTR</abbr>)</th><td>",
                    "<tr><th><abbr title=\"Autonomous System Number\">ASN</abbr></th><td>",
                    "<tr><th><abbr title=\"Internet Service Provider\">ISP</abbr> / Organization</th><td>",
                    "<tr><th>IP Location</th><td>",
                    "<tr><th>IP Continent</th><td>",
                    "<tr><th>IP Country</th><td>",
                    "<tr><th>IP State</th><td>",
                    "<tr><th>IP City</th><td>",
                    "<tr><th>IP Postcode</th><td>",
                    "<tr><th>IP Latitude</th><td>",
                    "<tr><th>IP Local Time</th><td>");
            int start, end;
            LinkedHashMap<String, String> values = new LinkedHashMap<>();
            values.put("IP by ipaddress.com", ip);
            for (String key : keys) {
                start = data.indexOf(key);
                if (start < 0) {
                    continue;
                }
                data = data.substring(start + key.length());
                end = data.indexOf("</td></tr>");
                if (end < 0) {
                    continue;
                }
                String value = data.substring(0, end);
                String name = key.substring(8, key.length() - 9);
                values.put(name, value);
                start = end + 10;
                if (start >= data.length() - 1) {
                    break;
                }
                data = data.substring(start);
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static LinkedHashMap<String, String> ipLocal(String ip) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            values.put("IP by local lookup", inetAddress.getHostAddress());
            values.put("Host", inetAddress.getHostName());
            values.put("Canonical Host", inetAddress.getCanonicalHostName());
            values.put("isAnyLocalAddress", inetAddress.isAnyLocalAddress() + "");
            values.put("isLinkLocalAddress", inetAddress.isLinkLocalAddress() + "");
            values.put("isLoopbackAddress", inetAddress.isLoopbackAddress() + "");
            values.put("isMulticastAddress", inetAddress.isMulticastAddress() + "");
            values.put("isSiteLocalAddress", inetAddress.isSiteLocalAddress() + "");
        } catch (Exception e) {
            values.put("error", e.toString());
        }
        return values;
    }

    public static String host2ipv4(String host) {
        try {
            if (host == null) {
                return null;
            }
            String ip = host2ipv4ByIpaddress(host);
            if (ip != null && !ip.isBlank()) {
                return ip;
            }
            InetAddress a = InetAddress.getByName(host);
            return a.getHostAddress();
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // https://fastly.net.ipaddress.com/github.global.ssl.fastly.net
    public static String host2ipv4ByIpaddress(String host) {
        try {
            if (host == null) {
                return null;
            }
            String[] nodes = host.split("\\.");
            if (nodes.length < 2) {
                return null;
            }
            String domain = nodes[nodes.length - 2] + "." + nodes[nodes.length - 1];
            String address = "https://" + domain + ".ipaddress.com/" + (nodes.length == 2 ? "" : host);
            String data = HtmlTools.url2text(address);
            if (data == null) {
                return null;
            }
            String flag = "<tr><th>IP Address</th><td><ul class=\"comma-separated\"><li>";
            int start = data.indexOf(flag);
            if (start < 0) {
                return null;
            }
            data = data.substring(start + flag.length());
            int end = data.indexOf("</li>");
            if (end < 0) {
                return null;
            }
            return data.substring(0, end);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String ip2host(String ip) {
        try {
            if (ip == null) {
                return null;
            }
            InetAddress a = InetAddress.getByName(ip);
            return a.getHostName();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean isIPv4(String ip) {
        try {
            if (ip == null) {
                return false;
            }
            String[] nodes = ip.split("\\.");
            if (nodes.length != 4) {
                return false;
            }
            for (String node : nodes) {
                int v = Integer.parseInt(node);
                if (v < 0 || v > 255) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
