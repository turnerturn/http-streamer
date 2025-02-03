package com.example.demo.httpstreamer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HttpStreamer {
    private Boolean enabled;
    private String cron;
    private String description;
    private Source source;
    private Destination destination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Source {
        private String url;
        private String method;
        private List<Header> headers;
        private List<Param> params;
        private String body;

        
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Destination {
        private String url;
        private String method;
        private List<Header> headers;
        private List<Param> params;
        private String body;

        
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Header {
        private String key;
        private String value;
        private String env;
        private CyberArk cyberark;

        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        public static class CyberArk {
            private String appid;
            private String object;
            private String safe;
            
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Param {
        private String key;
        private String value;
        private String function;
    }
}