package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.request.fraud.CheckFraudTransactionRequestDto;
import com.example.orchestrator.dto.request.transaction.CreateTransactionRequestDto;
import com.example.orchestrator.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.orchestrator.exception.FraudDetectedException;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckFraudDetectionSubRoute extends RouteBuilder {
    @Value("${client.fraud-service.uri}")
    String fraudServiceUri;

    @Override
    public void configure() throws Exception {
        onException(HttpOperationFailedException.class, FraudDetectedException.class)
                .handled(true) // Đánh dấu là đã xử lý lỗi, không để nó "nhảy ra ngoài"
                .process(exchange -> {
                    // Xử lý ngoại lệ ở đây, ví dụ: ghi log hoặc trả về một phản hồi khác
                    // Lấy exception từ exchange
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

                    // Kiểm tra loại exception và xử lý riêng
                    if (exception instanceof FraudDetectedException fraudException) {
                        ApiResponseDto<Void> parsed = ApiResponseDto.<Void>builder()
                                .code(400)
                                .message(fraudException.getMessage())
                                .build();
                        parsed.setMessage("[FraudDetectionService] " + parsed.getMessage());
                        exchange.getIn().setBody(parsed);

                        exchange.setProperty("reason", parsed.getMessage());
                    } else if (exception instanceof HttpOperationFailedException httpException) {
                        String body = httpException.getResponseBody();
                        ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                        parsed.setMessage("[FraudDetectionService] " + parsed.getMessage());
                        exchange.getIn().setBody(parsed);

                        exchange.setProperty("reason", parsed.getMessage());
                    }
                })
                .to("direct:rollbackUpdateAccountBalance")
                .end()
        ;


        // sub-route: transaction service
        from("direct:checkFraudDetection")
                .saga()
                .process(exchange -> {
                    // Tạo request dto
                    CreateTransactionRequestDto originalRequest = exchange.getProperty("originalRequestBody", CreateTransactionRequestDto.class);
                    Long transactionId = exchange.getProperty("transactionId", Long.class);

                    CheckFraudTransactionRequestDto requestDto = CheckFraudTransactionRequestDto.builder()
                            .transactionId(transactionId)
                            .accountId(originalRequest.getAccountId())
                            .type(originalRequest.getType())
                            .amount(originalRequest.getAmount())
                            .build();

                    exchange.getIn().setBody(requestDto); // request cho sub-route
                })
                .log("Camel: [FraudDetectionService] check fraud detection for transaction with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(fraudServiceUri)
                .choice()
                .when(body())
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<CheckFraudTransactionResponseDto> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<CheckFraudTransactionResponseDto>>() {});

                    Boolean isFraudulent = parsed.getData().getIsFraudulent();
                    // Kiểm tra isFraudulent
                    if (!isFraudulent) {
                        exchange.getMessage().setBody(parsed);
                    } else {
                        throw new FraudDetectedException(parsed.getData().getReason());
                    }
                })
                .log("Camel: [FraudDetectionService] Check fraud transaction successfully")
                .to("direct:completeUpdateAccountBalance")
                .to("direct:completeUpdateTransactionStatus")
                .end();
    }

}
