package com.example.chatgptapireiseapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/travels")
@RequiredArgsConstructor
public class TravelController {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @PostMapping
    List<String> getLuggage(@RequestBody String destination) {
        String jsonText = webClient.post()
                .bodyValue(new ChatGPTRequest("Gib als JSON-String-Array (Beispiel: `[\"Hut\"]`) 10 Gegenstände aus, die ich mitnehmen soll für die Reise nach: " + destination))
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .map(ChatGPTResponse::text)
                .block();
        try {
            return objectMapper.readValue(jsonText, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of("Handgepäck");
        }
    }
}

record ChatGPTMessage(
        String role,
        String content
) {
}

record ChatGPTRequest(
        String model,
        List<ChatGPTMessage> messages
) {
    ChatGPTRequest(String message) {
        this("gpt-3.5-turbo", Collections.singletonList(new ChatGPTMessage("user", message)));
    }
}

record ChatGPTChoice(
        ChatGPTMessage message
) {
}

record ChatGPTResponse(
        List<ChatGPTChoice> choices
) {
    public String text() {
        return choices.get(0).message().content();
    }
}
