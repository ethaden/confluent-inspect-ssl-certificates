package io.confluent.ethaden.testjavassl.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class TestJavaSSLClient {
    public static void main(String args[]) {
        int port = 1234;
        String hostname = "localhost";
        System.out.println(String.format("Connecting to server %s on port %d", hostname, port));


        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        SSLSocketFactory ssf = TestJavaSSLClient.getSocketFactory();
        try (Socket connection = ssf.createSocket(hostname, port);
            SSLSocket sslConnection = (SSLSocket) connection) {
            PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
            out.println("Hello World!");
        } catch (IOException e) {
            System.out.println("Unable to initiate connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory getSocketFactory() {
        SSLSocketFactory ssf = null;
        try {
            // set up key manager to do server authentication
            // Open keystore
            String ksPass = System.getProperty("javax.net.ssl.keyStorePassword");
            if (ksPass==null) {
                ksPass = "password";
            }
            char[] ksPassphrase = ksPass.toCharArray();
            KeyStore ksKeys = KeyStore.getInstance("JKS");
            String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
            if (keyStoreFilename == null) {
                keyStoreFilename = "./ssl/client.jks";
            }
            ksKeys.load(new FileInputStream(keyStoreFilename), ksPassphrase);
            // Initialize key manager
            // Open trust store
            String tsPass = System.getProperty("javax.net.ssl.trustStorePassword");
            if (tsPass==null) {
                tsPass = "password";
            }
            char[] tsPassPhrase = tsPass.toCharArray();
            KeyStore ksTrust = KeyStore.getInstance("JKS");
            String trustStoreFilename = System.getProperty("javax.net.ssl.trustStore");
            if (trustStoreFilename == null) {
                trustStoreFilename = "./ssl/truststore.jks";
            }
            ksTrust.load(new FileInputStream(trustStoreFilename), tsPassPhrase);

            KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ksKeys, ksPassphrase);
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ksTrust);
            // Build SSL context
            SSLContext ctx;
            ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ssf = ctx.getSocketFactory();
            return ssf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
