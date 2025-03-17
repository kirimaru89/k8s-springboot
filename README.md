# move to spring-app-1 folder
cd spring-app-1

# build docker image
docker build -t spring-app-1:latest .

# load local docker image into kind
kind load docker-image spring-app-1:latest --name spring-boot-cluster

# restart deployment
kubectl rollout restart deployment spring-app-1


# move to spring-app-2 folder
cd spring-app-2

# build docker image
docker build -t spring-app-2:latest .

# load local docker image into kind
kind load docker-image spring-app-2:latest --name spring-boot-cluster

# restart deployment
kubectl rollout restart deployment spring-app-2




# move to spring-app-3 folder
cd spring-app-3

# build docker image
docker build -t spring-app-3:latest .

# load local docker image into kind
kind load docker-image spring-app-3:latest --name spring-boot-cluster

# restart deployment
kubectl rollout restart deployment spring-app-3


# move to spring-app-4 folder
cd spring-app-4

# build docker image
docker build -t spring-app-4:latest .

# load local docker image into kind
kind load docker-image spring-app-4:latest --name spring-boot-cluster

# restart deployment
kubectl rollout restart deployment spring-app-4




kubectl apply -f deployment.yaml 