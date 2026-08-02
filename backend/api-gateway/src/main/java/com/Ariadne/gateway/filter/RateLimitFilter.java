package com.Ariadne.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final ReactiveStringRedisTemplate redisTemplate;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        String ip = (remoteAddress != null && remoteAddress.getAddress() != null)
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
        String key = "rate-limit:" + ip;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    Mono<Boolean> setExpiry = (count == 1)
                            ? redisTemplate.expire(key, WINDOW)
                            : Mono.just(true);

                    return setExpiry.then(Mono.defer(() -> {
                        if (count > MAX_REQUESTS) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                            byte[] body = """
                                {"success":false,"error":{"code":"RATE_LIMITED","message":"Too many requests, please try again later"}}"""
                                    .getBytes(StandardCharsets.UTF_8);
                            var buffer = exchange.getResponse().bufferFactory().wrap(body);
                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        }
                        return chain.filter(exchange);
                    }));
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}