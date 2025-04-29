package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.request.transaction.CreateTransactionRequestDto;
import com.example.orchestrator.dto.response.transaction.TransactionResponseDto;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CreateTransactionSubRoute extends RouteBuilder {
    @Value("${client.transaction-service.uri}")
    String transactionServiceUri;

    @Override
    public void configure() throws Exception {
        onException(HttpOperationFailedException.class)
                .handled(true) // Đánh dấu là đã xử lý lỗi, không để nó "nhảy ra ngoài"
                .process(exchange -> {
                    // Xử lý ngoại lệ ở đây, ví dụ: ghi log hoặc trả về một phản hồi khác
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    String body = exception.getResponseBody();
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                    parsed.setMessage("[TransactionService] " + parsed.getMessage());
                    exchange.getIn().setBody(parsed);
                });

        // sub-route: transaction service
        from("direct:createTransaction")
                .saga()
                // 1. Lưu dữ liệu gốc vào property
                .process(exchange -> {
                    CreateTransactionRequestDto originalRequest = exchange.getIn().getBody(CreateTransactionRequestDto.class);
                    exchange.setProperty("originalRequestBody", originalRequest);
                })
                .log("Camel: [TransactionService] Creating transaction with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(transactionServiceUri)
                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    // Lấy response body
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<TransactionResponseDto> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<TransactionResponseDto>>() {});
                    exchange.getIn().setBody(parsed);

                    /* cần bổ sung transactionId */
                    exchange.setProperty("transactionId", parsed.getData().getId());
                })
                .log("Camel: [TransactionService] Transaction created successfully")
                .end();
    }

}
