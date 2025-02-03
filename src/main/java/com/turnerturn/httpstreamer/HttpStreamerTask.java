package com.example.httpstreamer;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//TODO implement retryable logic for functions, source, destination, cyberark, and env.   (NOTE: Retry logic is not applied when the retryable methods are not in seperate class from this HttpStreamerTask.  (per technical constraints of spring's retryable logic.) )
@Slf4j
@Component
public class HttpStreamerTask {

    protected HttpStreamer streamer;
    protected String cronExpression;

    protected TaskScheduler taskScheduler;
    private ApplicationContext applicationContext;

    //Optional services to be needed for streamer if the headers have configured cyberark or env.
    //HttpStreamerScheduler is responsible in determining when services are needed.
    protected FetchCyberarkVaultSecretValue fetchCyberarkVaultSecretValue;
    protected FetchEnvironmentPropertyValue fetchEnvironmentPropertyValue;
    protected HttpRequestExecutor httpRequestExecutor;


    public HttpStreamerTask() {
    }

    public HttpStreamerTask with(HttpStreamer streamer) {
        this.streamer = streamer;
        return this;
    }
    public HttpStreamerTask with(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }
    public HttpStreamerTask with(FetchCyberarkVaultSecretValue fetchCyberarkVaultSecretValue) {
        this.fetchCyberarkVaultSecretValue = fetchCyberarkVaultSecretValue;
        return this;
    }
    public HttpStreamerTask with(FetchEnvironmentPropertyValue fetchEnvironmentPropertyValue) {
        this.fetchEnvironmentPropertyValue = fetchEnvironmentPropertyValue;
        return this;
    }

    public HttpStreamerTask with(HttpRequestExecutor httpRequestExecutor) {
        this.httpRequestExecutor = httpRequestExecutor;
        return this;
    }
    public HttpStreamerTask with(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public HttpStreamerTask with(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        return this;
    }

    public void schedule(){
        log.info("Scheduling task for streamer: {}", streamer);
        taskScheduler.schedule(() -> execute(), new CronTrigger(cronExpression));
    }
    protected void execute() {
        log.info("Executing streamer: {}", streamer);
        try {
            // derive param values from streamer's source
            String sourceUrl = deriveSourceUrl(streamer);
            HttpHeaders sourceHeaders = deriveSourceHeaders(streamer);
            HttpEntity<String> sourceRequest = new HttpEntity<>(streamer.getSource().getBody(), sourceHeaders);

            // execute http request (with retryable logic)
            ResponseEntity<String> sourceResponse = httpRequestExecutor.execute(sourceUrl, streamer.getSource().getMethod(), sourceRequest);
            log.info("Source response: {}", sourceResponse);

            // derive param values from streamer's destination
            String destinationUrl = deriveDestinationUrl(streamer);
            HttpHeaders destinationHeaders = deriveDestinationHeaders(streamer);
            HttpEntity<String> destinationRequest = new HttpEntity<>(sourceResponse.getBody(), destinationHeaders);

            // execute http request (with retryable logic)
            ResponseEntity<String> destinationResponse = httpRequestExecutor.execute(destinationUrl, streamer.getDestination().getMethod(), destinationRequest);
            log.info("Destination response: {}", destinationResponse);

            log.info("Streamer execution completed successfully for: {}", streamer);
        } catch (Exception e) {
            log.error("Error executing streamer: {}", streamer, e);
            throw e;
        }
    }

    protected String deriveSourceUrl(HttpStreamer streamer) {
        log.trace("deriveSourceUrl({})", Optional.ofNullable(streamer).map(HttpStreamer::getSource).orElse(null));
        Objects.requireNonNull(streamer, "streamer must not be null");
        Objects.requireNonNull(streamer.getSource(), "streamer.source must not be null");
        Objects.requireNonNull(streamer.getSource().getUrl(), "streamer.source.url must not be null");
        Objects.requireNonNull(streamer.getSource().getParams(), "streamer.source.params must not be null");

        // Implement URL derivation logic, ensuring final result is URL encoded
        // url and decorate with params. Implement function logic to get value if function configured.
        // https://www.baeldung.com/java-url-query-manipulation
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(streamer.getSource().getUrl());
        streamer.getSource().getParams().forEach(param -> {
            Object paramValue = null;
            if(param.getFunction() != null && !param.getFunction().isEmpty()){
                try {
                    if (applicationContext != null) {
                        Object bean = applicationContext.getBean("paramFunctions");
                        //TODO implement configurable args for functions.
                        Method method = bean.getClass().getMethod(param.getFunction());
                        paramValue = method.invoke(bean);
                    } else {
                        log.error("applicationContext is null, cannot process function: {}", param.getFunction());
                    }
                } catch (NoSuchMethodException e) {
                    log.warn("No matching method found for function: {}", param.getFunction());
                } catch (Exception e) {
                    log.error("Error invoking function: {}", param.getFunction(), e);
                }
            }
            if (paramValue == null && param.getValue() != null) {
                paramValue = param.getValue();
            }
            Objects.requireNonNull(paramValue, "paramValue must not be null. Value is fetched from 1 of 2 configured sources: function or param.value. See readme.md for more details on priority of each source when deriving value.");
            builder.queryParam(param.getKey(), paramValue);
        });
        return builder.toUriString();
    }

    protected String deriveDestinationUrl(HttpStreamer streamer) {
        log.trace("deriveDestinationUrl({})", Optional.ofNullable(streamer).map(HttpStreamer::getDestination).orElse(null));
        Objects.requireNonNull(streamer, "streamer must not be null");
        Objects.requireNonNull(streamer.getDestination(), "streamer.destination must not be null");
        Objects.requireNonNull(streamer.getDestination().getUrl(), "streamer.destination.url must not be null");
        Objects.requireNonNull(streamer.getDestination().getParams(), "streamer.destination.params must not be null");

        // Implement URL derivation logic, ensuring final result is URL encoded
        // url and decorate with params. Implement function logic to get value if function configured.
        // https://www.baeldung.com/java-url-query-manipulation
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(streamer.getDestination().getUrl());
        streamer.getDestination().getParams().forEach(param -> {
            Object paramValue = null;
            if(param.getFunction() != null && !param.getFunction().isEmpty()){
                try {
                    if (applicationContext != null) {
                        Object bean = applicationContext.getBean("paramFunctions");
                        //TODO implement configurable args for functions.
                        Method method = bean.getClass().getMethod(param.getFunction());
                        paramValue = method.invoke(bean);
                    } else {
                        log.error("applicationContext is null, cannot process function: {}", param.getFunction());
                    }
                } catch (NoSuchMethodException e) {
                    log.warn("No matching method found for function: {}", param.getFunction());
                } catch (Exception e) {
                    log.error("Error invoking function: {}", param.getFunction(), e);
                }
            }
            if(( paramValue == null ) && param.getValue() != null) {
                paramValue = param.getValue();
            }
            Objects.requireNonNull(paramValue, "paramValue must not be null. Value is fetched from 1 of 2 configured sources: function or param.value. See readme.md for more details on priority of each source when deriving value.");
            builder.queryParam(param.getKey(), paramValue);
        });
        return builder.toUriString();
    }

    protected HttpHeaders deriveSourceHeaders(HttpStreamer streamer) {
        log.trace("deriveSourceHeaders({})", Optional.ofNullable(streamer).map(HttpStreamer::getSource).orElse(null));
        Objects.requireNonNull(streamer, "streamer must not be null");
        Objects.requireNonNull(streamer.getSource(), "streamer.source must not be null");
        Objects.requireNonNull(streamer.getSource().getHeaders(), "streamer.source.headers must not be null");

        HttpHeaders headers = new HttpHeaders();
        // Populate headers per implementation specs (cyberark, env, then header.value)
        streamer.getSource().getHeaders().forEach(header -> {
            String headerValue = null;
            if(header.getCyberark() != null) {
                //TODO  fetch secret from cyberark and set to headerValue
            }
            if(( headerValue == null || headerValue.isEmpty() ) && header.getEnv() != null) {
                //TODO deriveSourceHeaders headers.set(header.getKey(), System.getenv(header.getEnv()));
            }
            if(( headerValue == null || headerValue.isEmpty() ) && header.getValue() != null) {
                headerValue = header.getValue();
            }

            Objects.requireNonNull(headerValue, "headerValue must not be null.  Value is fetched from 1 of 3 configured sources: cyberark, env, or header.value.  See readme.md for more details on priority of each source when deriving value.");
            headers.set(header.getKey(), headerValue);
        });
        return headers;
    }

    protected HttpHeaders deriveDestinationHeaders(HttpStreamer streamer) {
        log.trace("deriveDestinationUrl({})", Optional.ofNullable(streamer).map(HttpStreamer::getDestination).orElse(null));
        Objects.requireNonNull(streamer, "streamer must not be null");
        Objects.requireNonNull(streamer.getDestination(), "streamer.destination must not be null");
        Objects.requireNonNull(streamer.getDestination().getHeaders(), "streamer.destination.headers must not be null");

        final HttpHeaders headers = new HttpHeaders();
        // Populate headers per implementation specs (cyberark, env, then header.value)
        streamer.getDestination().getHeaders().forEach(header -> {
            String headerValue = null;
            if(header.getCyberark() != null) {
                //TODO  fetch secret from cyberark and set to headerValue
            }
            if(( headerValue == null || headerValue.isEmpty() ) && header.getEnv() != null) {
                //TODO deriveSourceHeaders headers.set(header.getKey(), System.getenv(header.getEnv()));
            }
            if(( headerValue == null || headerValue.isEmpty() ) && header.getValue() != null) {
                headerValue = header.getValue();
            }
            Objects.requireNonNull(headerValue, "headerValue must not be null.  Value is fetched from 1 of 3 configured sources: cyberark, env, or header.value.  See readme.md for more details on priority of each source when deriving value.");
            headers.set(header.getKey(), headerValue);
        });
        return headers;
    }


}

/**
 * These are public methods to be accessible from the configured param functions in our streamer's configuration.  Response of each method can be string, long, or double.
 */
@Slf4j
@Component
@NoArgsConstructor
class ParamFunctions {

    public static String getCurrentMillisecondsTimeLessFiveMinutes() {
        // Subtract 10 minutes from current time
        long currentTimeMillis = System.currentTimeMillis();
        long fiveMinutesInMillis = 5 * 60 * 1000;
        long adjustMillisTime = currentTimeMillis - fiveMinutesInMillis;
        return String.valueOf(adjustMillisTime);
    }

    public static String getCurrentMillisecondsTimeLessTenMinutes() {
        // Subtract 10 minutes from current time
        long currentTimeMillis = System.currentTimeMillis();
        long tenMinutesInMillis = 10 * 60 * 1000;
        long adjustMillisTime = currentTimeMillis - tenMinutesInMillis;
        return String.valueOf(adjustMillisTime);
    }
    public static String getUuid() {
        return java.util.UUID.randomUUID().toString();
    }
}
