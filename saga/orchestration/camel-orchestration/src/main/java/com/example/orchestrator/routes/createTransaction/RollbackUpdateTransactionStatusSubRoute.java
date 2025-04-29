package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.request.transaction.UpdateTransactionStatusDto;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RollbackUpdateTransactionStatusSubRoute extends RouteBuilder {
    @Value("${client.transaction-service.rollback-uri}")
    String rollbackTransactionServiceUri;

    @Override
    public void configure() throws Exception {
        // sub-route: transaction service
        from("direct:rollbackUpdateTransactionStatus")
                .saga()
                // Tạo request dto
                .process(exchange -> {
                    Long transactionId = exchange.getProperty("transactionId", Long.class);

                    UpdateTransactionStatusDto requestDto = UpdateTransactionStatusDto.builder()
                            .transactionId(transactionId)
                            .build();

                    exchange.getIn().setBody(requestDto);
                })
                .log("Camel: [TransactionService] rollback transaction with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(rollbackTransactionServiceUri)
                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                    String reason = exchange.getProperty("reason", String.class);
                    // custom response ###
                    parsed.setCode(400);
                    parsed.setMessage(reason);
                    exchange.getMessage().setBody(parsed);
                })
                .log("Camel: [TransactionService] Rollback Transaction successfully")
                .end();
    }
}
