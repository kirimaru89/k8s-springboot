package com.example.inputhttpserver.config;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoggingClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        log.info("Initiating gRPC call: MethodName={}, MethodType={}", 
                 method.getFullMethodName(), method.getType());

        if (callOptions.getDeadline() != null) {
            log.debug("Call Deadline: {} ms", callOptions.getDeadline().timeRemaining(TimeUnit.MILLISECONDS));
        }
        if (callOptions.getAuthority() != null) {
            log.debug("Call Authority: {}", callOptions.getAuthority());
        }

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {
                log.debug("gRPC Request: Method={}, Payload={}", method.getFullMethodName(), message.toString());
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Log request headers
                if (headers != null && headers.keys().size() > 0) {
                    log.debug("gRPC Request Headers: Method={}, Headers={}", method.getFullMethodName(), headers.toString());
                } else {
                    log.debug("gRPC Request Headers: Method={}, Headers=None", method.getFullMethodName());
                }

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        // Log response headers
                        if (headers != null && headers.keys().size() > 0) {
                            log.debug("gRPC Response Headers: Method={}, Headers={}", method.getFullMethodName(), headers.toString());
                        } else {
                            log.debug("gRPC Response Headers: Method={}, Headers=None", method.getFullMethodName());
                        }
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onMessage(RespT message) {
                        log.debug("gRPC Response: Method={}, Payload={}", method.getFullMethodName(), message.toString());
                        super.onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        String statusLog = String.format("gRPC call finished: Method=%s, Status=%s (%s)", 
                                                       method.getFullMethodName(), status.getCode(), status.getDescription());
                        if (trailers != null && trailers.keys().size() > 0) {
                             statusLog += String.format(", Trailers=%s", trailers.toString());
                        }

                        if (status.isOk()) {
                            log.info(statusLog);
                        } else {
                            if (status.getCause() != null) {
                                log.error(statusLog + ", Cause: ", status.getCause());
                            } else {
                                log.error(statusLog);
                            }
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }
} 