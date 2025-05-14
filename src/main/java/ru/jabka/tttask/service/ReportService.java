package ru.jabka.tttask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.jabka.tttask.client.TeamClient;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Member;
import ru.jabka.tttask.model.Report;
import ru.jabka.tttask.model.ReportRequest;
import ru.jabka.tttask.repository.ReportRepository;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final TeamClient teamClient;

    @Transactional(readOnly = true)
    public Report getByTeam(final ReportRequest request) {
        validateReportRequest(request);
        Set<Member> members = teamClient.getByTeamId(request.teamId());
        return reportRepository.getByTeam(
                members.stream().map(Member::memberId).collect(Collectors.toSet()),
                request.startDate(),
                request.endDate());
    }

    private void validateReportRequest(final ReportRequest request) {
        ofNullable(request).orElseThrow(() -> new BadRequestException("Заполните данные для получения отчета"));
        ofNullable(request.teamId()).orElseThrow(() -> new BadRequestException("Не заполнен id команды"));
        ofNullable(request.startDate()).orElseThrow(() -> new BadRequestException("Не заполнена дата начала выборки для отчета"));
        ofNullable(request.endDate()).orElseThrow(() -> new BadRequestException("Не заполнена дата окончания выборки для отчета"));
    }
}