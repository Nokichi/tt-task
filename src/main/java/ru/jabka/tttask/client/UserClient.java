package ru.jabka.tttask.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.jabka.tttask.model.UserResponse;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate userServiceRestTemplate;

    public Set<UserResponse> getAllByIds(final Set<Long> ids) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/v1/user");
        ids.forEach(id -> builder.queryParam("ids", id));
        return Set.of(userServiceRestTemplate.getForObject(builder.toUriString(), UserResponse[].class));
    }
}