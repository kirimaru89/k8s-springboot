from README.md folder (root)

# create prometheus
kubectl apply -f prometheus/deployment.yaml

# create grafana
kubectl apply -f grafana/deployment.yaml

# create tempo
helm install my-tempo grafana/tempo -f tempo/values.yaml -n monitoring
helm upgrade my-tempo grafana/tempo -f tempo/values.yaml -n monitoring

# create otel colletor
helm install my-opentelemetry-collector opentelemetry-helm/opentelemetry-collector \
  --namespace monitoring \
  --create-namespace \
  --version 0.118.0 \
  --values otelcollector/values.yaml

# upgrade otel collector
helm upgrade my-opentelemetry-collector opentelemetry-helm/opentelemetry-collector \
  --namespace monitoring \
  --version 0.118.0 \
  --values otelcollector/values.yaml

# create kafka
<!-- helm install my-kafka bitnami/kafka --version 31.5.0 --namespace monitoring -->
helm repo add kafka https://helm-charts.itboon.top/kafka/
helm install my-kafka kafka/kafka --version 18.0.1 -n monitoring

# oracle - setup using docker
docker pull container-registry.oracle.com/database/free:23.6.0.0-lite-arm64

docker run -d --name stanfordoradb \
  -p 1521:1521 -p 5500:5500 \                                                                   
  -e ORACLE_PWD=Stanford \
  -e ORACLE_CHARACTERSET=AL32UTF8 \
  -v $(pwd)/opt/oracle/oradata:/opt/oracle/oradata \
  container-registry.oracle.com/database/free:23.6.0.0-lite-arm64

# redis
helm install my-redis bitnami/redis --version 20.11.3

DOCKER_BUILDKIT=1 docker buildx build \
  --platform linux/arm64 \
  -t spring-app-1:latest \
  ./spring-app-1
kind load docker-image spring-app-1:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-1

DOCKER_BUILDKIT=1 docker buildx build \
  --platform linux/arm64 \
  -t spring-app-2:latest \
  ./spring-app-2
kind load docker-image spring-app-2:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-2

DOCKER_BUILDKIT=1 docker buildx build \
  --platform linux/arm64 \
  -t spring-app-3:latest \
  ./spring-app-3
kind load docker-image spring-app-3:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-3

DOCKER_BUILDKIT=1 docker buildx build \
  --platform linux/arm64 \
  -t spring-app-4:latest \
  ./spring-app-4
kind load docker-image spring-app-4:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-4

kubectl apply -f deployment.yaml 

docker start spring-boot-cluster-control-plane
docker stop spring-boot-cluster-control-plane

# force delete
kubectl delete pod my-postgresql-0 -n default --force --grace-period=0


# deploy configuration watcher
# application
kubectl apply -f configmaps/application-rbac.yaml
kubectl apply -f spring-app-1/deployment.yaml
# configuration watcher
kubectl apply -f configmaps/configuration-watcher-rbac.yaml
kubectl apply -f configmaps/configuration-watcher.yaml
# logbook-config
kubectl apply -f configmaps/logbook-config.yaml
