from README.md folder (root)

# create prometheus
kubectl apply -f prometheus/deployment.yaml

# create grafana
kubectl apply -f grafana/deployment.yaml

# create tempo
kubectl apply -f tempo/deployment.yaml

# create otel colletor
kubectl apply -f otelcollector/deployment.yaml

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