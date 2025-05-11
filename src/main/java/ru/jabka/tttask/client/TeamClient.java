package ru.jabka.tttask.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.jabka.tttask.model.Member;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TeamClient {

    private final RestTemplate teamServiceRestTemplate;

    public Set<Member> getByTeamId(final Long teamId) {
        return Set.of(teamServiceRestTemplate.getForObject("/api/v1/member/by-team/" + teamId, Member[].class));
    }
}