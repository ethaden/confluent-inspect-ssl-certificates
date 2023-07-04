# This is a demo to explore how to deal with expired SSL certificates

## Generating SSL certificates

Use the script found in the `demo` folder for generating SSL certificates:

```bash
cd demo
./create-ssl-certificates.sh
```

You'll find the certificates in `demo/ssl`.

## Running the code in general

By default, both the server and the client use the `demo/ssl/truststore.jks` and `demo/ssl/server.jks` or `demo/ssl/client.jks` respectively. All keystores use the password `password`.

You can overwrite the truststore or keystore just the common way you do this in Java, for example for the server:

```bash
java -Djavax.net.ssl.keyStore="./ssl/server.jks" \
  -Djavax.net.ssl.keyStorePassword="password" \
  -Djavax.net.ssl.trustStore="./ssl/truststore.jks" \
  -Djavax.net.ssl.trustStorePassword="password" \
  -jar server/build/libs/server-0.1.0.jar
```

## Running the server
Using the server with just the standard SSL certificate validation of Java (no reporting of common name of expired certificates) can be done by running the following in subfolder `demo`:

```bash
java -jar server/build/libs/server-0.1.0.jar
```

Alternatively, a custom trust manager factory can be used by specifying command line option `-e`:

```bash
java -classpath server/build/libs/server-0.1.0.jar:X509TrustmanagerCNReporting/build/libs/X509TrustmanagerCNReporting-0.1.0.jar \
    io.confluent.ethaden.testjavassl.server.TestJavaSSLServer -e
```

Here, obviously the code of the application needs to be updated.

As a last alternative, there is a way to run the original server code but still enable reporting of the common name of expired certificates.
For this to work, a custom Java security provider has been implemented (in `ExpiredCertReporterProvider.java`). This provider enables a custom TrustManagerFactory algorithm called `ExpiredCertReporter` (name has been chosen arbitrarily while implementing the code). Both, the name of the class of the security provider and the custom algorithm used for the trust store have to be specified in a file called `java.security`, which has been provided in the `demo` folder for convenience. That file will just extend the system-wide java.security settings (Java will append the settings to the end automatically).

Now run the following in folder `demo`:

```bash
java -classpath server/build/libs/server-0.1.0.jar:X509TrustmanagerCNReporting/build/libs/X509TrustmanagerCNReporting-0.1.0.jar \
    -Djava.security.properties=java.security \
    io.confluent.ethaden.testjavassl.server.TestJavaSSLServer
```



WARNING: You use this code at your own risk! Please do not use it for production systems. The author may not be held responsible for any harm caused by this code!


## Running the client
In `demo` run:

```bash
java -jar client/build/libs/client-0.1.0.jar
```
Run the client with a valid client certificate:

```bash
java -Djavax.net.ssl.keyStore="./ssl/client.jks" \
  -Djavax.net.ssl.keyStorePassword="password" \
  -Djavax.net.ssl.trustStore="./ssl/truststore.jks" \
  -Djavax.net.ssl.trustStorePassword="password" \
  -jar client/build/libs/client-0.1.0.jar
```

Run the client with an expired client certificate:

```bash
java -Djavax.net.ssl.keyStore="./ssl/client-expired.jks" \
  -Djavax.net.ssl.keyStorePassword="password" \
  -Djavax.net.ssl.trustStore="./ssl/truststore.jks" \
  -Djavax.net.ssl.trustStorePassword="password" \
  -jar client/build/libs/client-0.1.0.jar
```


## Testing the setup
Start the server, then try to connect with a valid client certificate by running the following in demo`:

```bash
openssl s_client -connect localhost:1234 -verifyCAfile ssl/ca.pem  \
  -cert ssl/client.pem -key ssl/client.key
```

Now try to do the same with a valid but expired client certificate:

```bash
openssl s_client -connect localhost:1234 -verifyCAfile ssl/ca.pem  \
  -cert ssl/client-expired.pem -key ssl/client-expired.key
```
