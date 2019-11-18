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
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.web.WebEngine;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description
 * @License Apache License Version 2.0
 */
public class NetworkTools {

    public static Map<String, String> readCookie(WebEngine webEngine) {
        try {
            String s = (String) webEngine.executeScript("document.cookie;");
            String[] vs = s.split(";");
            Map<String, String> m = new HashMap<>();
            for (String v : vs) {
                String[] vv = v.split("=");
                if (vv.length < 2) {
                    continue;
                }
                m.put(vv[0].trim(), vv[1].trim());
            }
            return m;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean checkWeiboPassport() {

        String passport;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {

            passport = System.getProperty("user.home")
                    + "/AppData/Roaming/mara.mybox.MainApp/webview/localstorage/https_weibo.com_0.localstorage";

        } else if (os.contains("linux")) {
            passport = System.getProperty("user.home")
                    + "/.mara.mybox.MainApp/webview/localstorage/https_weibo.com_0.localstorage";

        } else if (os.contains("mac")) {
            return AppVariables.getUserConfigBoolean("WeiboPassportChecked", false);

        } else {
            return AppVariables.getUserConfigBoolean("WeiboPassportChecked", false);
        }
        return new File(passport).exists();
    }

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

    // https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    public static void trustAll() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {

        }
    }

    public static void defaultSSL() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        } catch (Exception e) {

        }
    }

    // https://stackoverflow.com/questions/20050742/access-denied-to-setfactory-by-executing-httpsurlconnection-setdefaultsslsocketf
    public static void trustAll(URLConnection urlConnection) {
        try {
            if (!(urlConnection instanceof HttpsURLConnection)) {
                return;
            }
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection uc = (HttpsURLConnection) urlConnection;
            uc.setSSLSocketFactory(sc.getSocketFactory());
            uc.setHostnameVerifier(allHostsValid);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void installCertificate(String host) throws Exception {
        int port = 443;
        char[] passphrase = "changeit".toCharArray();

        String path = System.getProperty("java.home") + File.separator + "lib"
                + File.separator + "security";
        File dir = new File(path);
        File cacerts = new File(dir, "cacerts");

//        logger.debug("Loading KeyStore " + cacerts + "...");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts));
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf
                .getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();

        logger.debug("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            logger.debug("Starting SSL handshake...");
            socket.startHandshake();
//            socket.close();
            logger.debug("No errors, certificate is already trusted");
            return;
        } catch (SSLException e) {
            logger.debug(e.toString());
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            logger.debug("Could not obtain server certificate chain");
            return;
        }

        logger.debug("Server sent " + chain.length + " certificate(s):");

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            logger.debug(" " + (i + 1) + " Subject "
                    + cert.getSubjectDN());
            logger.debug("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            logger.debug("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            logger.debug("   md5     " + toHexString(md5.digest()));

            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);
            logger.debug(cert);
            logger.debug("Added certificate to keystore 'cacerts' using alias '"
                    + alias + "'");
        }

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cacerts));
        logger.debug(cacerts);
        ks.store(out, passphrase);
        out.close();

    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    // https://www.cnblogs.com/starcrm/p/7071227.html
    public static InetAddress localHost() {
        try {
            InetAddress candidateAddress = null;
            logger.debug("InetAddress.getLocalHost():" + InetAddress.getLocalHost());
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    logger.debug("inetAddr.getHostAddress:" + inetAddr.getHostAddress());
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

}
