package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.message.TransactionInitiatedMessageDto;
import com.example.orchestrator.dto.request.transaction.CreateTransactionRequestDto;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UpdateAccountBalanceSubRoute extends RouteBuilder {
    @Value("${client.account-service.uri}")
    String accountServiceUri;

    @Override
    public void configure() throws Exception {
        onException(HttpOperationFailedException.class)
                .handled(true) // Đánh dấu là đã xử lý lỗi, không để nó "nhảy ra ngoài"
                .process(exchange -> {
                    // Xử lý ngoại lệ ở đây, ví dụ: ghi log hoặc trả về một phản hồi khác
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    String body = exception.getResponseBody();
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                    parsed.setMessage("[AccountService] " + parsed.getMessage());
                    exchange.getIn().setBody(parsed);

                    exchange.setProperty("reason", parsed.getMessage());
                })
                .to("direct:rollbackUpdateTransactionStatus")
        ;

        // sub-route: account service
        from("direct:updateAccountBalance")
                .saga()
                .process(exchange -> {
                    // Tạo request dto
                    CreateTransactionRequestDto originalRequest = exchange.getProperty("originalRequestBody", CreateTransactionRequestDto.class);
                    Long transactionId = exchange.getProperty("transactionId", Long.class);

                    TransactionInitiatedMessageDto requestDto = TransactionInitiatedMessageDto.builder()
                            .transactionId(transactionId)
                            .accountId(originalRequest.getAccountId())
                            .type(originalRequest.getType())
                            .amount(originalRequest.getAmount())
                            .build();

                    exchange.getIn().setBody(requestDto); // request cho sub-route
                })
                .log("Camel: [AccountService] Update account balance with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(accountServiceUri)

                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                    exchange.getMessage().setBody(parsed);
                })
                .log("Camel: [AccountService] Update account balance successfully")
                .end();
    }

}
