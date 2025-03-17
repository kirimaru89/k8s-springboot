# build docker image
docker build -t k8s-demo:latest .

# load local docker image into kind
kind load docker-image k8s-demo:latest --name spring-boot-cluster

# restart deployment
kubectl rollout restart deployment k8s-demo
