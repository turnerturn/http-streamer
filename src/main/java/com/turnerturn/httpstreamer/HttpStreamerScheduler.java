package com.turnerturn.httpstreamer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpStreamerScheduler {
    @Autowired
    private HttpStreamerConfig httpStreamerConfig;


    @Autowired
    protected TaskScheduler taskScheduler;
    @Autowired
    private ApplicationContext applicationContext;

    //Optional services to be needed for streamer if the headers have configured cyberark or env.
    //HttpStreamerScheduler is responsible in determining when services are needed.
    @Autowired
    protected FetchCyberarkVaultSecretValue fetchCyberarkVaultSecretValue;
    @Autowired
    protected FetchEnvironmentPropertyValue fetchEnvironmentPropertyValue;
    @Autowired
    protected HttpRequestExecutor httpRequestExecutor;

    @PostConstruct
    public void init() {
        log.info("HttpStreamerScheduler initialized");
        httpStreamerConfig.getStreamers().forEach(streamer -> {
            log.info("Streamer: {}", streamer);
            // Schedule HttpStreamerTask with streamer
            scheduleTask(streamer);
        });
    }

    private void scheduleTask(HttpStreamer streamer) {
        log.info("Scheduling task for streamer: {}", streamer);
        new HttpStreamerTask()
            .with(streamer)
            .with("0 0/1 * * * *")
            .with(taskScheduler)
            .with(fetchCyberarkVaultSecretValue)
            .with(fetchEnvironmentPropertyValue)
            .with(httpRequestExecutor)
            .with(applicationContext)
            .schedule();
    }
}
