package ru.jabka.tttask.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.jabka.tttask.model.UserResponse;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate userService;

    public Set<UserResponse> getAllByIds(final Set<Long> ids) {
        String queryParams = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("&ids="));
        return Set.of(userService.getForObject("/api/v1/user?ids=" + queryParams, UserResponse[].class));
    }

    public UserResponse getById(final Long id) {
        return userService.getForObject("/api/v1/user/" + id, UserResponse.class);
    }
}