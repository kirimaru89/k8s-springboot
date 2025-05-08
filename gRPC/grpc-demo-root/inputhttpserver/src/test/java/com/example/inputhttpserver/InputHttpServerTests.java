package com.example.inputhttpserver;

import com.example.inputhttpserver.client.GrpcClient;
import com.example.inputhttpserver.controller.HelloController;
import com.example.inputhttpserver.service.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@Import(GreetingService.class) // Import GreetingService to be available in the context
public class InputHttpServerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrpcClient grpcClient; // Mock GrpcClient as GreetingService depends on it

    @Test
    void helloEndpointShouldReturnGreeting() throws Exception {
        String name = "TestUser";
        String expectedResponse = "Hello TestUser from gRPC";

        when(grpcClient.sayHello(name)).thenReturn(expectedResponse);

        mockMvc.perform(get("/hello/{name}", name))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
} 