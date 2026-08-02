package com.Ariadne.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        var request = exchange.getRequest();

        log.info("--> {} {}", request.getMethod(), request.getURI().getPath());

        return chain.filter(exchange).doFinally(signal -> {
            long durationMs = java.time.Duration.between(start, Instant.now()).toMillis();
            log.info("<-- {} {} [{}ms] status={}",
                    request.getMethod(),
                    request.getURI().getPath(),
                    durationMs,
                    exchange.getResponse().getStatusCode());
        });
    }

    @Override
    public int getOrder() {
        return -2;
    }
}