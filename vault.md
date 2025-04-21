# Integrating HashiCorp Vault with Spring Boot using Vault Agent Injection

## Prerequisites

1. Running Kubernetes cluster
2. HashiCorp Vault installed and running
3. Vault Helm chart installed in the cluster
4. kubectl and vault CLI tools installed

## 1. Vault Server Setup

### 1.1 Enable Kubernetes Authentication
```bash
# Enable Kubernetes auth method
vault auth enable kubernetes

# Configure Kubernetes auth method
vault write auth/kubernetes/config \
    kubernetes_host="https://kubernetes.default.svc:443" \
    token_reviewer_jwt="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \
    kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt \
    issuer="https://kubernetes.default.svc.cluster.local"
```

### 1.2 Create Vault Policies
```bash
# Create a policy for your application
vault policy write spring-app-policy -<<EOF
path "secret/data/spring-app-3/config" {
  capabilities = ["read"]
}
path "secret/metadata/spring-app-3/config" {
  capabilities = ["read", "list"]
}
EOF
```

### 1.3 Create Vault Secrets
```bash
# Create a secret for the application
vault kv put secret/spring-app-3/config \
  spring.datasource.url="jdbc:mysql://mysql:3306/book_db" \
  spring.datasource.driver-class-name="com.mysql.cj.jdbc.Driver" \
  spring.datasource.username="user" \
  spring.datasource.password="password"
```

### 1.4 Configure Kubernetes Authentication Role
```bash
vault write auth/kubernetes/role/spring-app-role \
    bound_service_account_names=spring-app-sa \
    bound_service_account_namespaces=default \
    policies=spring-app-policy \
    ttl=24h
```

## 2. Spring Boot Application Configuration

### 2.1 Add Dependencies
Add the following to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```

### 2.2 Configure application.yml
Create/update `src/main/resources/application.yml`:
```yaml
spring:
  cloud:
    vault:
      host: vault.vault.svc.cluster.local
      port: 8200
      scheme: https
      authentication: KUBERNETES
      kubernetes:
        role: spring-app-role
        service-account-token-file: /var/run/secrets/vault/token
      config:
        order: -10
```

## 3. Kubernetes Configuration

### 3.1 Create ServiceAccount
```yaml
# service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spring-app-sa
```

### 3.2 Update Deployment Configuration
Modify your existing deployment.yaml to include Vault Agent annotations:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-app-3
spec:
  template:
    metadata:
      annotations:
        vault.hashicorp.com/agent-inject: 'true'
        vault.hashicorp.com/agent-inject-status: 'update'
        vault.hashicorp.com/role: 'spring-app-role'
        vault.hashicorp.com/agent-inject-secret-config: 'secret/data/spring-app-3/config'
        vault.hashicorp.com/agent-inject-template-config: |
          {{- with secret "secret/data/spring-app-3/config" -}}
          spring.datasource.username={{ .Data.data.spring.datasource.username }}
          spring.datasource.password={{ .Data.data.spring.datasource.password }}
          api.key={{ .Data.data.api.key }}
          {{- end -}}
    spec:
      serviceAccountName: spring-app-sa
      containers:
        - name: spring-app-3
          # ... rest of your container config
```

## 4. Testing and Verification

1. Apply Kubernetes configurations:
```bash
kubectl apply -f service-account.yaml
kubectl apply -f deployment.yaml
```

2. Verify Vault Agent Injection:
```bash
kubectl get pods
kubectl describe pod <pod-name>
```

3. Check mounted secrets:
```bash
kubectl exec -it <pod-name> -- cat /vault/secrets/config
```

4. Verify application logs:
```bash
kubectl logs <pod-name>
```

## 5. Best Practices

1. **Secret Rotation**
   - Implement periodic secret rotation
   - Use Vault's dynamic secrets when possible
   - Configure appropriate TTLs

2. **Security Considerations**
   - Use minimal required policies
   - Enable audit logging in Vault
   - Regular review of access patterns

3. **Monitoring**
   - Monitor Vault agent health
   - Set up alerts for failed authentications
   - Track secret access patterns

## 6. Troubleshooting

Common issues and solutions:

1. **Authentication Failures**
   - Verify service account configuration
   - Check Vault role bindings
   - Validate Kubernetes auth configuration

2. **Secret Access Issues**
   - Verify policy permissions
   - Check secret path and format
   - Validate mounted secrets path

3. **Application Integration**
   - Verify Spring Cloud Vault configuration
   - Check application logs for Vault-related errors
   - Validate secret injection templates

## 7. Maintenance

Regular maintenance tasks:

1. Keep Vault version updated
2. Rotate service account tokens periodically
3. Audit and clean up unused policies and roles
4. Monitor and adjust resource limits as needed

Remember to replace placeholder values with your actual configuration details and adjust paths/names according to your specific setup.
