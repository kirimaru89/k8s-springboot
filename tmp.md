protoc \
  -I=proto \
  --java_out=out/java \
  --plugin=protoc-gen-grpc-java=/Users/chiennguyen/Desktop/spring/k8s-springboot/gRPC/proto/src/main/protoc-gen-grpc-java \
  --grpc-java_out=out/grpc \
  digitaldocuments/library/v1/account/account_service.proto