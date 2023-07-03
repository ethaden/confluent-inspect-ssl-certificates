package io.confluent.ethaden.x509trustmanagercnreporting;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public final class X509TrustManagerFactorySpiWithCNReporting extends TrustManagerFactorySpi {

    KeyStore truststore=null;

    @Override
    protected void engineInit(KeyStore truststore) throws KeyStoreException {
        System.out.println("engineInit(KeyStore) called");
        this.truststore = truststore;
    }

    @Override
    protected void engineInit(ManagerFactoryParameters params)
            throws InvalidAlgorithmParameterException {
        System.out.println("engineInit(ManagerFactoryParameters) called");
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        System.out.println("engineGetTrustManagers() called");
        try {
            return new TrustManager[] {new X509TrustManagerWithCNReporting(this.truststore)};
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

