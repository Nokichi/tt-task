package ru.jabka.tttask.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.jabka.tttask.model.Report;
import ru.jabka.tttask.model.ReportRequest;
import ru.jabka.tttask.service.ReportService;

@RestController
@Tag(name = "Отчеты")
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Выгрузить отчет по задачам для команды за определенный период")
    public Report getByTeamId(@RequestBody final ReportRequest request) {
        return reportService.getByTeam(request);
    }
}