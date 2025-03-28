# enable v2
vault secrets enable -path=secret -version=2 kv

# create value in vault
# stores your Spring Boot datasource config under the path: secret/data/bookapp
# run inside pod
vault kv put secret/bookapp \
  spring.datasource.url="jdbc:otel:mysql://mysql:3306/book_db" \
  spring.datasource.driver-class-name="io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver" \
  spring.datasource.username="user" \
  spring.datasource.password="password"

# Create a policy that grants your app read access to this secret
path "secret/data/bookapp" {
  capabilities = ["read"]
}

# Save as bookapp-policy.hcl, then apply it
vault login [roottoken]
vault policy write bookapp-policy bookapp-policy.hcl
=> vault policy write bookapp-policy bookapp-policy.hcl
Success! Uploaded policy: bookapp-policy

# enable kubernetes authentication
vault auth enable kubernetes

# create springapp-role
vault write auth/kubernetes/role/springapp-role \
  bound_service_account_names=default \
  bound_service_account_namespaces=monitoring \
  policies=springapp-policy \
  ttl=1h

# create springapp-policy
vault policy write springapp-policy - <<EOF
path "secret/data/hello" {
  capabilities = ["read"]
}
EOF

# Configures the Kubernetes auth method with:
#	API Server address
#	Vault’s service account token (used to verify other service accounts)
#	The Kubernetes CA cert

vault write auth/kubernetes/config \
  token_reviewer_jwt=@/var/run/secrets/kubernetes.io/serviceaccount/token \
  kubernetes_host="https://${KUBERNETES_PORT_443_TCP_ADDR}" \
  kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt

# enable database
vault secrets enable database