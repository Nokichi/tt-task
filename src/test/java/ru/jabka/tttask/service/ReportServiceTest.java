package ru.jabka.tttask.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.jabka.tttask.client.TeamClient;
import ru.jabka.tttask.exception.BadRequestException;
import ru.jabka.tttask.model.Member;
import ru.jabka.tttask.model.MemberWithTaskCount;
import ru.jabka.tttask.model.Report;
import ru.jabka.tttask.model.ReportRequest;
import ru.jabka.tttask.repository.ReportRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private TeamClient teamClient;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getByTeam_success() {
        ReportRequest request = ReportRequest.builder()
                .teamId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();
        Set<Member> memberSet = Set.of(Member.builder()
                .memberId(2L)
                .modifiedAt(LocalDateTime.now())
                .teamId(request.teamId())
                .build());
        Mockito.when(teamClient.getByTeamId(request.teamId()))
                .thenReturn(memberSet);
        Report report = Report.builder()
                .totalTeamTasks(1L)
                .todoTeamTasks(2L)
                .inProgressTeamTasks(3L)
                .doneTeamTasks(4L)
                .avgHoursTaskToDone(6.0)
                .topMembersWithTasksCount(List.of(MemberWithTaskCount.builder()
                        .assigneeId(2L)
                        .tasksCount(2L)
                        .build()))
                .build();
        Mockito.when(reportRepository.getByTeam(memberSet.stream()
                        .map(Member::memberId)
                        .collect(Collectors.toSet()),
                request.startDate(),
                request.endDate())).thenReturn(report);
        Report result = reportService.getByTeam(request);
        Assertions.assertEquals(report, result);
        Mockito.verify(reportRepository).getByTeam(memberSet.stream()
                        .map(Member::memberId)
                        .collect(Collectors.toSet()),
                request.startDate(),
                request.endDate());
        Mockito.verify(teamClient).getByTeamId(request.teamId());
    }

    @Test
    void getByTeam_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> reportService.getByTeam(null)
        );
        Assertions.assertEquals("Заполните данные для получения отчета", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never())
                .getByTeam(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void getByTeam_error_nullTeamId() {
        ReportRequest request = ReportRequest.builder()
                .teamId(null)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> reportService.getByTeam(request)
        );
        Assertions.assertEquals("Не заполнен id команды", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never())
                .getByTeam(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void getByTeam_error_nullStartDate() {
        ReportRequest request = ReportRequest.builder()
                .teamId(1L)
                .startDate(null)
                .endDate(LocalDate.now())
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> reportService.getByTeam(request)
        );
        Assertions.assertEquals("Не заполнена дата начала выборки для отчета", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never())
                .getByTeam(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void getByTeam_error_nullEndDate() {
        ReportRequest request = ReportRequest.builder()
                .teamId(1L)
                .startDate(LocalDate.now())
                .endDate(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> reportService.getByTeam(request)
        );
        Assertions.assertEquals("Не заполнена дата окончания выборки для отчета", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never())
                .getByTeam(Mockito.any(), Mockito.any(), Mockito.any());
    }
}