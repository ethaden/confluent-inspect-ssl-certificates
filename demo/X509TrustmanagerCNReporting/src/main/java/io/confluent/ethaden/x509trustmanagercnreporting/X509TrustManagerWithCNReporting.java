package io.confluent.ethaden.testjavassl.server;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public final class X509TrustManagerWithCNReporting implements X509TrustManager {

    private X509TrustManager trustManager = null;

    public X509TrustManagerWithCNReporting(KeyStore trustStore) {

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        for (TrustManager tm: tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager)
                this.trustManager = (X509TrustManager) tm;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        this.trustManager.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        this.trustManager.checkServerTrusted(chain, authType);
    }
}
