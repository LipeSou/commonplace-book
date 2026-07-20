package com.felipe.commonplace.journal;

import com.felipe.commonplace.journal.dto.JournalResponse;
import com.felipe.commonplace.journal.dto.JournalSection;
import com.felipe.commonplace.note.NoteRepository;
import com.felipe.commonplace.note.dto.NoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 温故知新 — o jornal de lembranças: o que foi escrito há uma semana, há um mês
 * e no mesmo dia de cada ano anterior.
 *
 * A regra é o **dia exato**, não uma janela em volta: dia sem nota simplesmente não
 * vira seção. Lembrança aproximada não é lembrança.
 */
@Service
@RequiredArgsConstructor
public class JournalService {

    private final NoteRepository repository;

    @Transactional(readOnly = true)
    public JournalResponse of(LocalDate date, ZoneId zone) {
        List<JournalSection> sections = new ArrayList<>();

        // dias "mesmo dia do mês/ano anterior" que não existem no calendário caem no último
        // dia válido (31/03 → 28/02; 29/02 → 28/02), como manda o java.time.
        addIfAny(sections, "Há uma semana", date.minusWeeks(1), zone);
        addIfAny(sections, "Há um mês", date.minusMonths(1), zone);

        int oldest = oldestYear(date, zone);
        for (int years = 1; date.getYear() - years >= oldest; years++) {
            addIfAny(sections, years == 1 ? "Há um ano" : "Há " + years + " anos",
                    date.minusYears(years), zone);
        }

        return new JournalResponse(date, sections);
    }

    /** Até que ano vale procurar: o ano da nota mais antiga. Sem notas, não há passado. */
    private int oldestYear(LocalDate date, ZoneId zone) {
        return repository.findOldestCreatedAt()
                .map(oldest -> oldest.atZone(zone).getYear())
                .orElse(date.getYear());
    }

    private void addIfAny(List<JournalSection> sections, String label, LocalDate day, ZoneId zone) {
        Instant from = day.atStartOfDay(zone).toInstant();
        Instant to = day.plusDays(1).atStartOfDay(zone).toInstant();

        List<NoteResponse> notes = repository
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(from, to)
                .stream()
                .map(NoteResponse::from)
                .toList();

        if (!notes.isEmpty()) {
            sections.add(new JournalSection(label, day, notes));
        }
    }
}
