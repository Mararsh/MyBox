package mara.mybox.tools;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;

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
                try (ServerSocket serverSocket = new ServerSocket(0)) {
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

    public static SSLContext sslContext() {
        try {
            SSLContext context = SSLContext.getInstance(AppValues.HttpsProtocal);
            context.init(null, null, null);
            return context;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static SSLSocketFactory sslSocketFactory() {
        try {
            return sslContext().getSocketFactory();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static SSLSocket sslSocket(String host, int port) {
        try {
            return (SSLSocket) sslSocketFactory().createSocket(host, port);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HttpURLConnection httpConnection(URL url) {
        try {
            if ("https".equals(url.getProtocol())) {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setSSLSocketFactory(sslSocketFactory());
                return conn;
            } else {
                return (HttpURLConnection) url.openConnection();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
            return host;
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
            String data = HtmlReadTools.url2html(address);
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
