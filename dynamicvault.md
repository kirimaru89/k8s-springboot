spring-app-5 => k8s cluster
postgres => k8s cluster => forward to 5433 on host
hashicorp vault => image: hashicorp/vault:1.19; port 55000 on host

1. Make sure that your user in database has admin privileges
psql -U postgres -h localhost -d k8spostgres -p 5433
=> input password
=> inside postgres: 
\du
=> should print something like this;
k8spostgres=# \du
                                   List of roles
 Role name |                         Attributes                         | Member of 
-----------+------------------------------------------------------------+-----------
 postgres  | Superuser, Create role, Create DB, Replication, Bypass RLS | {}

this user should have admin privileges because it will use credential to create temporary user
in order to generate dynamic username/password with time to live (TTL)

host:
docker logs my-dynamic-vault
=> Unseal Key: 0es4IwajD0cgJsIrKpkFT9DvRt4cfWzIIxPd6MKEL88=
Root Token: ###

inside vault
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN= ###

inside vault
vault write database/config/my-postgres \
  plugin_name=postgresql-database-plugin \
  allowed_roles="payments-app" \
  connection_url="postgresql://postgres:Q9mn6pUr0i@host.docker.internal:5433/k8spostgres?sslmode=disable" \
  username="postgres" \
  password="Q9mn6pUr0i"

inside vault (my-postgres => from vault write database/config/my-postgres)
# testing with 10 min
vault write database/roles/payments-app \
  db_name=my-postgres \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO \"{{name}}\";" \
  revocation_statements="DROP ROLE IF EXISTS \"{{name}}\";" \
  default_ttl="10m" \
  max_ttl="10m"

# prod - 10 days
vault write database/roles/payments-app \
  db_name=my-postgres \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO \"{{name}}\";" \
  revocation_statements="DROP ROLE IF EXISTS \"{{name}}\";" \
  default_ttl="240h" \
  max_ttl="240h"

host: you will see new user: v-root-payments-8m09sIhA366CqfzdTF4N-1749003805
psql -U postgres -h localhost -p 5433 -d k8spostgres
=> enter password

\du
=>                     Role name                    |                         Attributes                         | Member of 
-------------------------------------------------+------------------------------------------------------------+-----------
 postgres                                        | Superuser, Create role, Create DB, Replication, Bypass RLS | {}
 v-root-payments-8m09sIhA366CqfzdTF4N-1749003805 | Password valid until 2025-06-04 02:24:30+00                | {}

k8spostgres=# 