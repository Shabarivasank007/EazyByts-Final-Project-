package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class WebClientFilter {
    private static final Logger logger = LoggerFactory.getLogger(WebClientFilter.class);

    private WebClientFilter() {
        // Private constructor to prevent instantiation
    }

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n");
                sb.append("URI: ").append(clientRequest.url()).append("\n");
                sb.append("Method: ").append(clientRequest.method()).append("\n");
                
                clientRequest.headers().forEach((name, values) -> {
                    // Mask API keys in headers
                    if (name.equalsIgnoreCase("X-API-Key") || name.equalsIgnoreCase("X-Api-Key") || name.equalsIgnoreCase("Authorization")) {
                        values = values.stream().map(v -> "***MASKED***").toList();
                    }
                    sb.append(name).append(": ").append(String.join(", ", values)).append("\n");
                });
                
                logger.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (logger.isDebugEnabled()) {
                HttpStatus status = (HttpStatus) clientResponse.statusCode();
                StringBuilder sb = new StringBuilder("Response: \n");
                sb.append("Status: ").append(status.value()).append(" ").append(status.getReasonPhrase()).append("\n");
                
                clientResponse.headers().asHttpHeaders().forEach((name, values) -> 
                    sb.append(name).append(": ").append(String.join(", ", values)).append("\n")
                );
                
                logger.debug(sb.toString());
            }
            return Mono.just(clientResponse);
        });
    }
}
