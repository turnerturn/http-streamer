package com.turnerturn.httpstreamer;

import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class FetchCyberarkVaultSecretValue {

    //TODO implement retryable
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String execute(String appId,String safe,String folder,String object){
        // TODO  FetchCyberarkVaultSecretValue Implement logic to fetch secret value from CyberArk vault
        return "cyberark-secret-value";
    }
}
