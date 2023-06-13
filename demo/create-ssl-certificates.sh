export SSL_DIR=./ssl
if [ \! -d ${SSL_DIR} ]; then
    mkdir ${SSL_DIR}
fi

# Create CA
if [ \! -e ${SSL_DIR}/ca.key ]; then
#keytool -genkeypair -noprompt -alias root -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=example.invalid" -validity 9365 -keypass password -keystore ${SSL_DIR}/ca.jks -storepass password -storetype pkcs12 -ext BasicConstraints:critical=ca:true -ext KeyUsage:critical=keyCertSign,cRLSign
    openssl req -x509 -newkey rsa:2048 -keyout ${SSL_DIR}/ca.key -out ${SSL_DIR}/ca.pem \
    -sha256 -days 3650 -nodes -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=CA"
    keytool -import -noprompt -alias root -file ${SSL_DIR}/ca.pem -trustcacerts -storetype JKS -storepass password -keystore ${SSL_DIR}/truststore.jks
fi

# Server
if [ \! -e ${SSL_DIR}/server.key ]; then
    openssl genrsa -out ${SSL_DIR}/server.key 2048
    # Sign
    openssl req -new -key ${SSL_DIR}/server.key -subj req -new -out ${SSL_DIR}/server.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=localhost" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = serverAuth"
    openssl x509 -nopass -req -sha256 -days 3650 -in ${SSL_DIR}/server.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/server.pem -copy_extensions "copy"
    openssl pkcs12 -export -in ${SSL_DIR}/server.pem -inkey ${SSL_DIR}/server.key \
               -out ${SSL_DIR}/server.p12 -name localhost \
               -CAfile ${SSL_DIR}/ca.crt -caname root -password pass:password
    keytool -importkeystore -noprompt \
        -deststorepass password -destkeypass password -destkeystore ${SSL_DIR}/server.jks \
        -srckeystore ${SSL_DIR}/server.p12 -srcstoretype PKCS12 -srcstorepass password \
        -alias localhost
fi

# Client
if [ \! -e ${SSL_DIR}/client.key ]; then
    openssl genrsa -out ${SSL_DIR}/client.key 2048
    # Sign
    openssl req -new -key ${SSL_DIR}/client.key -subj req -new -out ${SSL_DIR}/client.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=Client" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = clientAuth"
    openssl x509 -req -sha256 -days 3650 -in ${SSL_DIR}/client.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/client.pem -copy_extensions "copy"
    openssl pkcs12 -export -in ${SSL_DIR}/client.pem -inkey ${SSL_DIR}/client.key \
               -out ${SSL_DIR}/client.p12 -name client \
               -CAfile ${SSL_DIR}/ca.crt -caname root -password pass:password
    keytool -importkeystore -noprompt \
        -deststorepass password -destkeypass password -destkeystore ${SSL_DIR}/client.jks \
        -srckeystore ${SSL_DIR}/client.p12 -srcstoretype PKCS12 -srcstorepass password \
        -alias client
fi


# Client expired
if [ \! -e ${SSL_DIR}/client-expired.key ]; then
    openssl genrsa -out ${SSL_DIR}/client-expired.key 2048
    # Sign
    faketime 'yesterday 12 pm' /bin/bash -c 'openssl req -new -key ${SSL_DIR}/client-expired.key -subj req -new -out ${SSL_DIR}/client-expired.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=Client Expired" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = clientAuth"'
    faketime 'yesterday 6 am' /bin/bash -c 'openssl x509 -req -sha256 -days 1 -in ${SSL_DIR}/client-expired.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/client-expired.pem -copy_extensions "copy"'
    openssl pkcs12 -export -in ${SSL_DIR}/client-expired.pem -inkey ${SSL_DIR}/client-expired.key \
               -out ${SSL_DIR}/client-expired.p12 -name client-expired \
               -CAfile ${SSL_DIR}/ca.crt -caname root -password pass:password
    keytool -importkeystore -noprompt \
        -deststorepass password -destkeypass password -destkeystore ${SSL_DIR}/client-expired.jks \
        -srckeystore ${SSL_DIR}/client-expired.p12 -srcstoretype PKCS12 -srcstorepass password \
        -alias client-expired
fi

# Verify
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/server.pem
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/client.pem
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/client-expired.pem
