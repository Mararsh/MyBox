package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;
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
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description
 * @License Apache License Version 2.0
 */
public class ConfigTools {

    private static final Logger logger = LogManager.getLogger();

    public static String readConfigValue(String key) {
        try {
            String value = null;
            try (InputStream in = new BufferedInputStream(new FileInputStream(CommonValues.UserConfigFile))) {
                Properties conf = new Properties();
                conf.load(in);
                value = conf.getProperty(key);
            }
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean writeConfigValue(String key, String value) {
        try {
            Properties conf = new Properties();
            try (InputStream in = new FileInputStream(CommonValues.UserConfigFile)) {
                conf.load(in);
            }
            try (OutputStream out = new FileOutputStream(CommonValues.UserConfigFile)) {
                if (value == null) {
                    conf.remove(key);
                } else {
                    conf.setProperty(key, value);
                }
                conf.store(out, "Update " + key);
            }
            return true;
        } catch (Exception e) {
//            logger.error(e.toStsring());
            return false;
        }
    }

    public static boolean checkWeiboPassport() {
        String passport;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            passport = System.getProperty("user.home")
                    + "/AppData/Roaming/mara.mybox.MainApp/webview/localstorage/https_passport.weibo.com_0.localstorage";

        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            passport = System.getProperty("user.home")
                    + "/.mara.mybox.MainApp/webview/localstorage/https_passport.weibo.com_0.localstorage";

        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return AppVaribles.getConfigBoolean("WeiboPassportChecked", false);

        } else {
            return AppVaribles.getConfigBoolean("WeiboPassportChecked", false);
        }
        return new File(passport).exists();
    }

    public static boolean isOtherPlatforms() {
        String p = System.getProperty("os.name").toLowerCase();
        logger.debug(p);
        return !p.contains("windows") && !p.contains("linux");
    }

    public static void initSSL() {

        try {

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub

                }
            }};
            // Install the all-trusting trust manager
            SSLContext sc;
            try {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.debug(e.toString());
            }

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // TODO Auto-generated method stub
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static void InstallCert(String caPassword, String address, String alias) {
        try {
            //
//            Security.setProperty("javax.net.debug", "all");
//////            Security.setProperty("javax.net.ssl.trustStore", System.getProperty("user.home") +  File.separator +"cacerts");
//            Security.setProperty("javax.net.ssl.trustStore", "D:/Programs/Java/jdk1.8.0_172/jre/lib/security/cacerts");
//            Security.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//            Security.setProperty("javax.net.ssl.keyStore", "D:/Programs/Java/jdk1.8.0_172/jre/lib/security/cacerts");
//            Security.setProperty("javax.net.ssl.keyStorePassword", "changeit");
//            Security.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
//            Security.setProperty("allowLegacyHelloMessages", "true");
//            Security.setProperty("ocsp.enable", "false");
//            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
//            System.setProperty("com.sun.security.enableCRLDP", "false");
//            ConfigTools.InstallCert("changeit", "www.weibo.com", "weibo");
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub

                }
            }};
            // Install the all-trusting trust manager
            SSLContext sc;
            try {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyManagementException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // TODO Auto-generated method stub
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            int port;
            char[] passphrase;
            port = 443;
            String p = (caPassword == null) ? "changeit" : caPassword;
            passphrase = p.toCharArray();
            File dir = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "security");
            File file = new File(dir, "cacerts");
            System.out.println("Loading KeyStore " + file + "...");
            KeyStore ks;
            try (InputStream in = new FileInputStream(file)) {
                ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(in, passphrase);
            }
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory factory = context.getSocketFactory();
            System.out.println("Opening connection to " + address + ":" + port + "...");
            SSLSocket socket = (SSLSocket) factory.createSocket(address, port);
            socket.setSoTimeout(10000);
            try {
                System.out.println("Starting SSL handshake...");
                socket.startHandshake();
                socket.close();
                System.out.println();
                System.out.println("No errors, certificate is already trusted");
            } catch (SSLException e) {
                System.out.println();
                e.printStackTrace(System.out);
            }
            X509Certificate[] chain = tm.chain;
            if (chain == null) {
                System.out.println("Could not obtain server certificate chain");
                return;
            }
            System.out.println();
            System.out.println("Server sent " + chain.length + " certificate(s):");
            System.out.println();
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            try (OutputStream out = new FileOutputStream(file)) {
                for (int i = 0; i < chain.length; i++) {
                    X509Certificate cert = chain[i];
                    System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
                    System.out.println("  Issuer " + cert.getIssuerDN());
                    sha1.update(cert.getEncoded());
                    System.out.println("  sha1  " + toHexString(sha1.digest()));
                    md5.update(cert.getEncoded());
                    System.out.println("  md5   " + toHexString(md5.digest()));
                    System.out.println();

                    String a = alias + "-" + (i + 1);
                    ks.setCertificateEntry(alias, cert);
                    ks.store(out, passphrase);
                    System.out.println();
                    System.out.println(cert);
                    System.out.println();
                    System.out.println("Added certificate to keystore 'jssecacerts' using alias '" + a + "'");
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());

        }
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
//            throw new UnsupportedOperationException();
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}
