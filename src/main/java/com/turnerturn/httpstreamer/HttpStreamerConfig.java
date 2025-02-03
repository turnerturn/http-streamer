package com.example.demo.httpstreamer;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class HttpStreamerConfig {

    private List<HttpStreamer> streamers;

    public List<HttpStreamer> getStreamers() {
        return streamers;
    }

    public void setStreamers(List<HttpStreamer> streamers) {
        this.streamers = streamers;
    }

}
