SSL_DIR=./ssl
if [ \! -d ${SSL_DIR} ]; then
    mkdir ${SSL_DIR}
fi

# Create CA
if [ \! -e ${SSL_DIR}/ca.key ]; then
#keytool -genkeypair -noprompt -alias root -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=example.invalid" -validity 9365 -keypass password -keystore ${SSL_DIR}/ca.jks -storepass password -storetype pkcs12 -ext BasicConstraints:critical=ca:true -ext KeyUsage:critical=keyCertSign,cRLSign
    openssl req -x509 -newkey rsa:4096 -keyout ${SSL_DIR}/ca.key -out ${SSL_DIR}/ca.pem \
    -sha256 -days 3650 -nodes -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=CA"
#     \
#    -addext "BasicConstraints:critical=ca:true" \
#    -addext "keyUsage = digitalSignature, keyEncipherment, dataEncipherment, cRLSign, keyCertSign" \
#    -addext "extendedKeyUsage = serverAuth, clientAuth"
fi

# Server
if [ \! -e ${SSL_DIR}/server.key ]; then
    openssl genrsa -out ${SSL_DIR}/server.key 2048
    # Sign
    openssl req -new -key ${SSL_DIR}/server.key -subj req -new -out ${SSL_DIR}/server.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=Server" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = serverAuth"
    openssl x509 -req -sha256 -days 3650 -in ${SSL_DIR}/server.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/server.pem -copy_extensions "copy"
fi

# Client
if [ \! -e ${SSL_DIR}/client.key ]; then
    openssl genrsa -out ${SSL_DIR}/client.key 2048
    # Sign
    openssl req -new -key ${SSL_DIR}/client.key -subj req -new -out ${SSL_DIR}/client.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=Client" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = clientAuth"
    openssl x509 -req -sha256 -days 3650 -in ${SSL_DIR}/client.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/client.pem -copy_extensions "copy"
fi

# Client expired
if [ \! -e ${SSL_DIR}/client-expired.key ]; then
    openssl genrsa -out ${SSL_DIR}/client-expired.key 2048
    # Sign
    faketime 'yesterday 12 pm' /bin/bash -c 'openssl req -new -key ${SSL_DIR}/client-expired.key -subj req -new -out ${SSL_DIR}/client-expired.csr -subj "/C=DE/L=World/O=Confluent/OU=CSTA/CN=Client Expired" \
        -addext "keyUsage = digitalSignature,keyAgreement" \
        -addext "extendedKeyUsage = clientAuth"'
    faketime 'yesterday 6 am' /bin/bash -c 'openssl x509 -req -sha256 -days 1 -in ${SSL_DIR}/client-expired.csr -CA ${SSL_DIR}/ca.pem -CAkey ${SSL_DIR}/ca.key -out ${SSL_DIR}/client-expired.pem -copy_extensions "copy"'
fi

# Verify
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/server.pem
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/client.pem
# openssl verify -verbose -CAfile ${SSL_DIR}/ca.pem ${SSL_DIR}/client-expired.pem