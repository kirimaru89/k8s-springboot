package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.message.FraudTransactionVerifiedMessageDto;
import com.example.orchestrator.mapper.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CompleteUpdateAccountBalanceSubRoute  extends RouteBuilder {
    @Value("${client.account-service.complete-uri}")
    String completeAccountServiceUri;


    @Override
    public void configure() throws Exception {
        // sub-route: transaction service
        from("direct:completeUpdateAccountBalance")
                .saga()
                // Tạo request dto
                .process(exchange -> {
                    Long transactionId = exchange.getProperty("transactionId", Long.class);
                    FraudTransactionVerifiedMessageDto requestDto = FraudTransactionVerifiedMessageDto
                            .builder()
                            .transactionId(transactionId)
                            .build();

                    exchange.getIn().setBody(requestDto);
                })
                .log("Camel: [AccountService] complete account with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(completeAccountServiceUri)
                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    String body = exchange.getIn().getBody(String.class);
                    ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                    // custom response ###
                    exchange.getMessage().setBody(parsed);
                })
                .log("Camel: [AccountService] Complete Account successfully")
                .end();
    }


}
