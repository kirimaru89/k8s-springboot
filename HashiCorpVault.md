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


