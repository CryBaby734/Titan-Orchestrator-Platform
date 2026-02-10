package org.example.titanworker.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanworker.domain.TaskDefinition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpTaskExecutor implements TaskExecutor {

    private final RestClient restClient;

    @Override
    public boolean canExecute(String taskType) {
        return "HTTP_REQUEST".equals(taskType);
    }

    @Override
    public String execute(TaskDefinition taskDefinition) {
        Map<String, Object> payload = taskDefinition.getPayload();

        String url = (String) payload.get("url");
        String method = (String) payload.getOrDefault("method", "POST");

        log.info("Executing HTTP Request: {} {}", method, url);

        String response = null; // Переменная для результата

        if("GET".equalsIgnoreCase(method)) {
            response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            log.info("Response received: {}", response != null ? response.substring(0, Math.min(response.length(), 100)) + "..." : "null");

        } else if("POST".equalsIgnoreCase(method)) {
            Object requestBody = payload.get("body");

            var requestSpec = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON);

            if(requestBody != null) {
                requestSpec.body(requestBody);
            } else {
                log.warn("POST request payload is missing 'body' key, sending empty body");
                // Важно: для некоторых API пустой body может вызвать ошибку, можно передать "{}"
                requestSpec.body("{}");
            }

            response = requestSpec.retrieve()
                    .body(String.class);

            log.info("Response received: {}", response);
        } else {
            throw new UnsupportedOperationException("Method " + method + " not supported");
        }

        return response; // <-- ТЕПЕРЬ ВОЗВРАЩАЕМ РЕЗУЛЬТАТ!
    }
}