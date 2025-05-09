cd orchestrationservice
mvn clean package
cd ..
cd step1service
mvn clean package
cd ..
cd step2service
mvn clean package
cd ..
cd step3service
mvn clean package
cd ..
docker compose down
docker compose up --build -d