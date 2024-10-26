Spider Sockey
--------------
### About
Spider Sockey is a mod that adds a websocket server which is closely integrated
with your minecraft server. While functionality is limited for now, it lets you 
securely run commands on your server from any websocket client. A personal example
of its use case can be seen in my Blub discord bot project.

### Setup
To use this mod there are a couple of things which you need to set up outside your
standard mod installation process. First, as this mod uses secure connections over the
`wss` protocol, you will need to acquire some kind of SSL Certificate for the Sockey
server. For this there are two options:
1. Use a self-signed certificate.
2. Use a certificate from a trusted Certificate Authority.

#### Self-signed Certificate
Creating a self-signed certificate is the easiest and most practical way for most people
to get started. If you're hosting a larger more public server, you should consider getting
a certificate from a trusted Certificate Authority.

To create a self-signed certificate, you'll want to start by creating a custom config for
generating a Certificate Signing Request (CSR) using OpenSSL. It might look something like this:
```cnf
[req]
default_bits       = 2048
prompt             = no
default_md         = sha256
distinguished_name = dn
req_extensions     = req_ext

[dn]
C  = [Country code i.e. US]
ST = [State or province]
L  = [Locality]
O  = [Organization] # If you don't belong to an organization, you can put your name here or leave it blank
OU = [Unit/Department] # If you don't have a department, you can leave it blank
CN = [Domain name]  # Common Name, replace with your IP if needed

[req_ext]
subjectAltName = @alt_names

[alt_names] # Fill these fields as needed. It is best practice to include your CN as a SAN
DNS.1 = yourdomain.com
DNS.2 = www.yourdomain.com
IP.1  = 127.0.0.1
IP.2  = 0.0.0.0
```
With `[dn]` fields such as `O` and `OU`, you can leave them blank if you don't belong to an organization, but
it's best to provide as much detail as possible. These are the only two fields that can be left blank. It's worth
noting that if you are connecting over a local network, you can use your local IP address as the `CN` field with
the caveat that you will also *need* to include the same IP address in the `IP.1` field under `[alt_names]`.

Once you have your custom config, you can generate a self-signed certificate using the following commands:
```sh
# Generate the private key and CSR
openssl genpkey -algorithm RSA -out server.key -pkeyopt rsa_keygen_bits:2048
openssl req -new -key server.key -out server.csr -config openssl.cnf
# Generate the certificate
openssl x509 -req -in server.csr -signkey server.key -out server.crt -days 365 -extfile openssl.cnf -extensions req_ext
```
With that, you should have a `server.key` and `server.crt` file which you can use for your Sockey server. With it being
self-signed, you'll also want to include a copy of the certificate in your client's trust store to avoid any SSL errors.

Once you have the key and the certificate, you'll need to ensure that they are named `server.key` and `server.crt` 
respectively and are located in the `config/spidersockey/` directory of your server folder. If you don't have a 
`config/spidersockey/` directory and try to start the server, the mod will create one for you and promptly exit because
it can't find the required files.

#### Tokens
One last thing you'll need to set up is the `token.txt` file. This file should contain a single line with a *secret* token
which will be used to verify that the client connecting to the server is authorized to do so. This ensures that only
clients with your authorization can connect to the server. The token is kept in the same `config/spidersockey/` directory
as the `server.key` and `server.crt` files.
