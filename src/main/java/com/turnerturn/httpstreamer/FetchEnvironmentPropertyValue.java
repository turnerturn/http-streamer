package com.example.demo.httpstreamer;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FetchEnvironmentPropertyValue {

    private final Environment environment;

    //TODO implement retryable
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String execute(String key){
        // TODO  FetchEnvironmentPropertyValue Implement logic to fetch value from environment properties
        return environment.getProperty(key);
    }
}
