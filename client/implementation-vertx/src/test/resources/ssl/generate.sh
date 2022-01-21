#!/bin/sh

# This script can be used to re-generate the keystores and truststores for SSL tests

# server.pkcs12.keystore :: server's keystore
keytool -genkeypair -validity 3650 -storepass serverkeystorepassword -dname "cn=example.com,ou=server" -storetype PKCS12 -keystore server.pkcs12.keystore
# server.cert :: server's public key certificate
keytool -export -keystore server.pkcs12.keystore -file server.cert -storepass serverkeystorepassword
# client.pkcs12.truststore :: client's truststore that trusts the server's certificate
keytool -import -trustcacerts -file server.cert -keystore client.pkcs12.truststore -storepass clienttruststorepassword -storetype PKCS12
# server.pkcs12.wrong.keystore :: a server keystore that is NOT trusted by the client
keytool -genkeypair -validity 3650 -storepass serverwrongkeystorepassword -dname "cn=example.com,ou=serverwrong" -storetype PKCS12 -keystore server.pkcs12.wrong.keystore

# client.pkcs12.keystore :: client's keystore
keytool -genkeypair -validity 3650 -storepass clientkeystorepassword -dname "cn=example.com,ou=client" -storetype PKCS12 -keystore client.pkcs12.keystore
# client.cert :: client's public key certificate
keytool -export -keystore client.pkcs12.keystore -file client.cert -storepass clientkeystorepassword
# server.pkcs12.truststore :: server's truststore that trusts the client's certificate
keytool -import -trustcacerts -file client.cert -keystore server.pkcs12.truststore -storepass servertruststorepassword -storetype PKCS12
# client.pkcs12.wrong.keystore :: a client keystore that is NOT trusted by the server
keytool -genkeypair -validity 3650 -storepass clientwrongkeystorepassword -dname "cn=example.com,ou=clientwrong" -storetype PKCS12 -keystore client.pkcs12.wrong.keystore