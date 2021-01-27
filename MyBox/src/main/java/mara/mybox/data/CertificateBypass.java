/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.table.TableBrowserBypassSSL;

/**
 *
 * @author mara
 */
public class CertificateBypass {

    protected String host;
    protected long createTime;

    public static List<String> bypass() {
        List<CertificateBypass> bypass = TableBrowserBypassSSL.read();
        List<String> hosts = new ArrayList();
        if (bypass != null) {
            for (CertificateBypass cert : bypass) {
                String host = cert.getHost();
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                hosts.add(host);
            }
        }
        return hosts;
    }

    /*
        get/set
     */
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
