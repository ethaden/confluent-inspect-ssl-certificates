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
In `demo` run:

```bash
java -jar server/build/libs/server-0.1.0.jar
```

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
