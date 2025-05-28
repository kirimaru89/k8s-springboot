# https://developer.hashicorp.com/vault/tutorials/app-integration/spring-reload-secrets

# 🛡️ Integrating HashiCorp Vault with Spring Boot using Vault Agent Injector (Kubernetes)

This guide walks you through integrating HashiCorp Vault into a **Spring Boot** application running inside **Kubernetes (Kind)** using **Vault Agent Injector Sidecar**. This uses environment variables and config file injection, not shell `source`.

---

## 📦 Application Info

| Item                     | Value                  |
|--------------------------|------------------------|
| App Name                 | `spring-app-3`         |
| K8s Service Account      | `spring-app-sa`        |
| Vault Role               | `spring-app-3-role`    |
| Vault Secret Path (KVv2)| `secret/data/spring-app-3/db` |

---

## 🔧 Step-by-Step Guide

---

### 🔹 1. Deploy Vault into Kind Cluster

```bash
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

helm install my-vault hashicorp/vault \
  --namespace monitoring \
  --create-namespace \
  --set "injector.enabled=true" \
  --set "server.dev.enabled=true"


helm uninstall my-vault --namespace monitoring

# for docker only
docker pull hashicorp/vault
```

---

### 🔹 2. Enable Kubernetes Auth Method in Vault

Exec into the Vault pod:

```bash
kubectl exec -it vault-0 -- /bin/sh
vault auth enable kubernetes
```

---

### 🔹 3. Configure Vault Kubernetes Auth Backend

Inside the Vault pod:

```bash
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
vault write auth/kubernetes/config \
  token_reviewer_jwt="$TOKEN" \
  kubernetes_host="https://kubernetes.default.svc:443" \
  kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
```

---

### 🔹 4. Create and Store Secrets in Vault

```bash
vault kv put secret/spring-app-3/db \
  spring.datasource.url="jdbc:postgresql://my-postgresql-1:5432/k8spostgres" \
  spring.datasource.driver-class-name="org.postgresql.Driver" \
  spring.datasource.username="postgres" \
  spring.datasource.password="Q9mn6pUr0i"
```

---

### 🔹 5. Create Vault Policy

```bash
vault policy write spring-app-3-policy -<<EOF
path "secret/data/spring-app-3/*" {
  capabilities = ["read"]
}
EOF
```

---

### 🔹 6. Create Kubernetes Service Account and Role Binding

```bash
kubectl create serviceaccount spring-app-sa

kubectl apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spring-app-3-review-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:auth-delegator
subjects:
- kind: ServiceAccount
  name: spring-app-sa
  namespace: default
EOF
```

Create Vault Role:

```bash
vault write auth/kubernetes/role/spring-app-3-role \
  bound_service_account_names="spring-app-sa" \
  bound_service_account_namespaces="default" \
  policies="spring-app-3-policy" \
  ttl="24h"
```

---

### 🔹 7. Annotate Spring Boot Deployment for Vault Agent Injector

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-app-3
spec:
  replicas: 1
  selector:
    matchLabels:
      app: spring-app-3
  template:
    metadata:
      labels:
        app: spring-app-3
      annotations:
        # inject secrets into /vault/secrets/application.properties
        vault.hashicorp.com/agent-inject: "true"
        vault.hashicorp.com/role: "spring-app-3-role"
        vault.hashicorp.com/agent-inject-secret-application.properties: "secret/data/spring-app-3/db"
        vault.hashicorp.com/agent-inject-template-application.properties: |
          {{- with secret "secret/data/spring-app-3/db" -}}
          spring.datasource.url={{ index .Data.data "spring.datasource.url" }}
          spring.datasource.driver-class-name={{ index .Data.data "spring.datasource.driver-class-name" }}
          spring.datasource.username={{ index .Data.data "spring.datasource.username" }}
          spring.datasource.password={{ index .Data.data "spring.datasource.password" }}
          {{- end }}
    spec:
      serviceAccountName: spring-app-sa
      containers:
        - name: spring-app-3
          image: spring-app-3:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          # read secrets imported from /vault/secrets/ folder
          env:
            - name: SPRING_CONFIG_ADDITIONAL_LOCATION
              value: file:/vault/secrets/
```

---
```

✅ Now Spring Boot reads secrets directly from files injected by Vault Agent.
