package com.example.orchestrator.routes.createTransaction;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.request.transaction.UpdateTransactionStatusDto;
import com.example.orchestrator.dto.response.transaction.TransactionResponseDto;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CompleteUpdateTransactionStatusSubRoute extends RouteBuilder {
    @Value("${client.transaction-service.complete-uri}")
    String completeTransactionServiceUri;

    @Override
    public void configure() throws Exception {
        // sub-route: transaction service
        from("direct:completeUpdateTransactionStatus")
                .saga()
                // Tạo request dto
                .process(exchange -> {
                    Long transactionId = exchange.getProperty("transactionId", Long.class);
                    UpdateTransactionStatusDto requestDto = UpdateTransactionStatusDto.builder()
                            .transactionId(transactionId)
                            .build();

                    exchange.getIn().setBody(requestDto);
                })
                .log("Camel: [TransactionService] complete transaction with ID")
                .marshal().json()   // <-- Serialize to JSON
                .to(completeTransactionServiceUri)
                .process(exchange -> {
                    /* cần unwrap ResponseEntity */
                    Long transactionId = exchange.getProperty("transactionId", Long.class);
                    TransactionResponseDto responseDto = TransactionResponseDto.builder()
                            .id(transactionId)
                            .build();
                    // custom response ###
                    ApiResponseDto<TransactionResponseDto> responseBody = new ApiResponseDto<>();
                    responseBody.setCode(200);
                    responseBody.setMessage("Complete");
                    responseBody.setData(responseDto);
                    exchange.getMessage().setBody(responseBody);
                })
                .log("Camel: [TransactionService] Complete Transaction successfully")
                .end();
    }
}
