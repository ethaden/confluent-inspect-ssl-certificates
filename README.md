# This is a demo to explore how to deal with expired SSL certificates

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

## Testing the setup
Start the server, then run in `demo`:

```bash
openssl s_client -connect localhost:1234 -showcerts
```

