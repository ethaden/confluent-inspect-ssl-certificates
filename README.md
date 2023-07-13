# This is a demo to explore how to deal with expired SSL certificates

This proof-of-concept solves the problem, that no useful information is generated if a client with an expired but otherwise valid SSL certificate connects to an SSL-protected server. In complex IT environments it might be pretty hard to identify the misconfigured client.
In this example code, the thrown exception is slightly updated to include the common name of the certificate, but only if the certificate is just expired and otherwise valid. This should reduce the potential of misuse to a minimum.

CAUTION: Everything contained in this repository is not supported by Confluent.

DISCLAIMER AND WARNING: You use this code at your own risk! Please do not use it for production systems. The author may not be held responsible for any harm caused by this code!

## Pre-conditions
Please install and use Java 11.x for compiling the code (newer versions won't be compatible with the Kafka binaries in the docker images).
You also need openssl tools. And you might need to install gradle, too.

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

However, as this code depends on log4j, you would need to ensure manually to have all the external libs on the classpath.
Therefore, this project provides some custom gradle tasks to make your life easier.

## Running the server
Using the server with just the standard SSL certificate validation of Java (no reporting of common name of expired certificates) can be done by running the following in subfolder `demo`:

```bash
./gradlew :server:run
```

Alternatively, a custom trust manager factory can be used by specifying command line option `-e`:

```bash
./gradlew :server:run --args="-e"
```

Here, obviously the code of the application needs to be updated.

As a last alternative, there is a way to run the original server code but still enable reporting of the common name of expired certificates.
For this to work, a custom Java security provider has been implemented (in `ExpiredCertReporterProvider.java`). This provider enables a custom TrustManagerFactory algorithm called `ExpiredCertReporter` (name has been chosen arbitrarily while implementing the code). Both, the name of the class of the security provider and the custom algorithm used for the trust store have to be specified in a file called `java.security`, which has been provided in the `demo` folder for convenience. That file will just extend the system-wide java.security settings (Java will append the settings to the end automatically).

Now run the following in folder `demo`:

```bash
./gradlew :server:runWithLogging
```


In all cases, check the exception thrown whenever a client with an expired SSL certificate connects to the server, see below.

## Running the client
In `demo` run (using the valid certificate):

```bash
java -jar client/build/libs/client-0.1.0.jar
```


Run the client with a valid client certificate explicitely:

```bash
java -Djavax.net.ssl.keyStore="./ssl/client.jks" \
  -Djavax.net.ssl.keyStorePassword="password" \
  -Djavax.net.ssl.trustStore="./ssl/truststore.jks" \
  -Djavax.net.ssl.trustStorePassword="password" \
  -jar client/build/libs/client-0.1.0.jar
```

For convenience, this gradle task will do exactly the same:

```bash
./gradlew :client:run
```

Run the client with an expired client certificate:

```bash
java -Djavax.net.ssl.keyStore="./ssl/client-expired.jks" \
  -Djavax.net.ssl.keyStorePassword="password" \
  -Djavax.net.ssl.trustStore="./ssl/truststore.jks" \
  -Djavax.net.ssl.trustStorePassword="password" \
  -jar client/build/libs/client-0.1.0.jar
```

Again, for convenience, this gradle task will do exactly the same:

```bash
./gradlew :client:runWithExpiredCert
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


## Using the proof-of-concept with Confluent Platform
Warning: This is experimental.

Create the certificates as shown above. Then start the docker containers:

```bash
docker-compose up -d
```

Check that everything is running:

```bash
docker-compose ps
```

### Producing data
First, add an entry to your /etc/hosts in order to make sure name resolution works:

```
127.0.0.1  server
```

Then produce data to topic `test` (within folder `demo`):

```bash
kafka-console-producer --bootstrap-server localhost:9092 --producer.config kafka/producer.properties -topic test
```

You may also try to produce to the topic with an expired certificate:

```bash
kafka-console-producer --bootstrap-server localhost:9092 --producer.config kafka/producer-expired-ssl.properties -topic test
```

Read the data again (in folder `demo`):

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --consumer.config kafka/consumer.properties --from-beginning --topic test
```

Try now to consume data, but use an expired SSL certificate:

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --consumer.config kafka/consumer-expired-ssl.properties --from-beginning --topic test
```

In parallel in a different shell, check the broker logs by running:

```bash
docker-compose logs -f server
```

