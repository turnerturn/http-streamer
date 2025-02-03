package com.example.demo.httpstreamer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor
public class HttpRequestExecutor {

    //TODO implement retryable
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<String> execute(String url, String method, HttpEntity<String> request) {
        log.trace("Executing request: url={}, method={}, request={}", url, method, request);
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(url, HttpMethod.valueOf(method), request, String.class);
    }
}
