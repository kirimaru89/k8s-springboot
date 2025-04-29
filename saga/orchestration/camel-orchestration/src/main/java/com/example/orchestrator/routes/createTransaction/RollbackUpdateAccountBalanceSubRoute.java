package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.message.FraudTransactionDetectedMessageDto;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RollbackUpdateAccountBalanceSubRoute extends RouteBuilder {
    @Value("${client.account-service.rollback-uri}")
    String rollbackAccountServiceUri;

    @Override
    public void configure() throws Exception {
        // sub-route: transaction service
        from("direct:rollbackUpdateAccountBalance")
                .saga()
                // Tạo request dto
                .process(exchange -> {
                    Long transactionId = exchange.getProperty("transactionId", Long.class);
                    ApiResponseDto<Void> resBody = exchange.getIn().getBody(ApiResponseDto.class);

                    FraudTransactionDetectedMessageDto requestDto = FraudTransactionDetectedMessageDto
                            .builder()
                            .transactionId(transactionId)
                            .reason(resBody.getMessage())
                            .build();

                    exchange.getIn().setBody(requestDto);
                })
                .log("Camel: [AccountService] rollback account with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(rollbackAccountServiceUri)
                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});

                    exchange.getMessage().setBody(parsed);
                })
                .log("Camel: [AccountService] Rollback Account successfully")
                .to("direct:rollbackUpdateTransactionStatus")
                .end();
    }

}
