package com.felipe.commonplace.journal;

import com.felipe.commonplace.journal.dto.JournalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
@Tag(name = "Jornal", description = "温故知新 — o que foi escrito nesta data no passado.")
public class JournalController {

    private final JournalService service;

    /** O fuso de casa: usado quando o cliente não diz de onde está perguntando. */
    @Value("${commonplace.zone}")
    private String defaultZone;

    @GetMapping
    @Operation(summary = "As lembranças de um dia",
            description = """
                    Notas escritas há uma semana, há um mês e no mesmo dia de cada ano anterior.
                    O dia é **exato**: sem nota naquele dia, a seção não aparece.
                    """)
    @ApiResponse(responseCode = "200", description = "O jornal do dia (pode vir sem nenhuma seção)")
    @ApiResponse(responseCode = "400", description = "Data ou fuso horário inválido")
    public JournalResponse journal(
            @Parameter(description = "O dia de referência (padrão: hoje, no fuso informado)", example = "2026-07-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Fuso de quem pergunta — é ele que define onde o dia começa",
                    example = "America/Sao_Paulo")
            @RequestParam(required = false) String zone) {

        ZoneId zoneId = ZoneId.of(zone == null || zone.isBlank() ? defaultZone : zone);
        return service.of(date == null ? LocalDate.now(zoneId) : date, zoneId);
    }
}
