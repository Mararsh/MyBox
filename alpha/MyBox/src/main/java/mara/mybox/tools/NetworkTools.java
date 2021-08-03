package mara.mybox.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.dev.MyBoxLog;

import mara.mybox.value.Languages;

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
            values.put(Languages.message("Address"), url.toString());
            values.put(Languages.message("ExternalForm"), url.toExternalForm());
            values.put(Languages.message("Decode"), UrlTools.decodeURL(url.toString(), Charset.defaultCharset()));
            values.put(Languages.message("Protocal"), url.getProtocol());
            values.put(Languages.message("Host"), url.getHost());
            values.put(Languages.message("Path"), url.getPath());
            values.put(Languages.message("File"), url.getFile());
            values.put(Languages.message("Query"), url.getQuery());
            values.put(Languages.message("Authority"), url.getAuthority());
            values.put(Languages.message("Reference"), url.getRef());
            values.put(Languages.message("Port"), (url.getPort() < 0 ? url.getDefaultPort() : url.getPort()) + "");

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
            String data = HtmlReadTools.url2text(address);
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
            String data = HtmlReadTools.url2text(address);
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
            String data = HtmlReadTools.url2text(address);
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
