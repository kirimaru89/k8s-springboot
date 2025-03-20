from README.md folder (root)

# create prometheus
kubectl apply -f prometheus/deployment.yaml

# create grafana
kubectl apply -f grafana/deployment.yaml

# create tempo
helm install my-tempo grafana/tempo -f tempo/values.yaml -n monitoring

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

# oracle
helm upgrade --install oracle12c ecommerce-oracle/oracle12c \
  --namespace default --set image.repository=container-registry.oracle.com/database/enterprise \
  --set image.tag=19.3.0

docker build -t spring-app-1:latest ./spring-app-1
kind load docker-image spring-app-1:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-1

docker build -t spring-app-2:latest ./spring-app-2
kind load docker-image spring-app-2:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-2

docker build -t spring-app-3:latest ./spring-app-3
kind load docker-image spring-app-3:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-3

docker build -t spring-app-4:latest ./spring-app-4
kind load docker-image spring-app-4:latest --name spring-boot-cluster
kubectl rollout restart deployment spring-app-4

kubectl apply -f deployment.yaml 

docker start spring-boot-cluster-control-plane
docker stop spring-boot-cluster-control-plane

# force delete
kubectl delete pod my-postgresql-0 -n default --force --grace-period=0