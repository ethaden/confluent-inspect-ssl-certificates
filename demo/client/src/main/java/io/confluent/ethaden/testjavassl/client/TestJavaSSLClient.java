package io.confluent.ethaden.testjavassl.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class TestJavaSSLClient {
    public static void main(String args[]) {
        System.out.println("USAGE: java -jar <server-jar-file>");

        int port = 1234;
        String hostname = "localhost";

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        SSLSocketFactory ssf = TestJavaSSLClient.getSocketFactory();
        try (Socket connection = ssf.createSocket(hostname, port);
            SSLSocket sslConnection = (SSLSocket) connection) {
            // sslListener
            //         .setEnabledCipherSuites(new String[] {"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256"});
            //sslListener.setEnabledProtocols(new String[] {"TLSv1.2"});
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
            char[] ksPassphrase = "password".toCharArray();
            // KeyStore kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ksKeys = KeyStore.getInstance("JKS");
            ksKeys.load(new FileInputStream("./ssl/client.jks"), ksPassphrase);
            // Initialize key manager
            // Open trust store
            char[] tsPassPhrase = "password".toCharArray();
            KeyStore ksTrust = KeyStore.getInstance("JKS");
            ksTrust.load(new FileInputStream("./ssl/truststore.jks"), tsPassPhrase);

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
