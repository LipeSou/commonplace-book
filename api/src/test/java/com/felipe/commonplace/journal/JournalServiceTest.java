package com.felipe.commonplace.journal;

import com.felipe.commonplace.journal.dto.JournalResponse;
import com.felipe.commonplace.journal.dto.JournalSection;
import com.felipe.commonplace.note.Note;
import com.felipe.commonplace.note.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JournalServiceTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 7, 20);

    private NoteRepository repository;
    private JournalService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(NoteRepository.class);
        service = new JournalService(repository);
        // por padrão o passado é vazio; cada teste acende os dias que quer
        when(repository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(any(), any()))
                .thenReturn(List.of());
        when(repository.findOldestCreatedAt()).thenReturn(Optional.empty());
    }

    @Test
    void passadoVazioNaoGeraNenhumaSecao() {
        assertThat(service.of(HOJE, SP).sections()).isEmpty();
    }

    @Test
    void secoesSaemDasMaisRecentesParaAsMaisAntigas() {
        notasEm(LocalDate.of(2026, 7, 13), "semana passada");
        notasEm(LocalDate.of(2026, 6, 20), "mês passado");
        notasEm(LocalDate.of(2025, 7, 20), "ano passado");
        notasEm(LocalDate.of(2024, 7, 20), "dois anos atrás");
        notaMaisAntigaEm(LocalDate.of(2024, 1, 1));

        assertThat(service.of(HOJE, SP).sections())
                .extracting(JournalSection::label, JournalSection::date)
                .containsExactly(
                        tuple("Há uma semana", LocalDate.of(2026, 7, 13)),
                        tuple("Há um mês", LocalDate.of(2026, 6, 20)),
                        tuple("Há um ano", LocalDate.of(2025, 7, 20)),
                        tuple("Há 2 anos", LocalDate.of(2024, 7, 20)));
    }

    @Test
    void diaSemNotaNaoViraSecao() {
        notasEm(LocalDate.of(2025, 7, 20), "só o ano passado");
        notaMaisAntigaEm(LocalDate.of(2025, 1, 1));

        JournalResponse journal = service.of(HOJE, SP);

        assertThat(journal.date()).isEqualTo(HOJE);
        assertThat(journal.sections()).singleElement().satisfies(section -> {
            assertThat(section.label()).isEqualTo("Há um ano");
            assertThat(section.notes()).extracting("title").containsExactly("só o ano passado");
        });
    }

    @Test
    void naoProcuraAlemDaNotaMaisAntiga() {
        notaMaisAntigaEm(LocalDate.of(2025, 12, 1));
        LocalDate anosDemaisAtras = LocalDate.of(2020, 7, 20);
        notasEm(anosDemaisAtras, "nota impossível");

        assertThat(service.of(HOJE, SP).sections()).isEmpty();

        // o caderno começou em 2025: perguntar por 2020 seria trabalho jogado fora
        Mockito.verify(repository, Mockito.never())
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                        anosDemaisAtras.atStartOfDay(SP).toInstant(),
                        anosDemaisAtras.plusDays(1).atStartOfDay(SP).toInstant());
    }

    @Test
    void oDiaComecaNoFusoDeQuemPergunta() {
        // 20/07 em São Paulo (UTC-3) vai das 03:00Z às 03:00Z do dia seguinte
        notaMaisAntigaEm(LocalDate.of(2025, 1, 1));
        notasEm(LocalDate.of(2025, 7, 20), "ano passado");

        service.of(HOJE, SP);

        Mockito.verify(repository).findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                Instant.parse("2025-07-20T03:00:00Z"), Instant.parse("2025-07-21T03:00:00Z"));
    }

    @Test
    void diaQueNaoExisteNoMesAnteriorCaiNoUltimoValido() {
        notasEm(LocalDate.of(2026, 2, 28), "fevereiro é curto");

        assertThat(service.of(LocalDate.of(2026, 3, 31), SP).sections())
                .extracting(JournalSection::label, JournalSection::date)
                .containsExactly(tuple("Há um mês", LocalDate.of(2026, 2, 28)));
    }

    private void notaMaisAntigaEm(LocalDate day) {
        when(repository.findOldestCreatedAt()).thenReturn(Optional.of(day.atStartOfDay(SP).toInstant()));
    }

    private void notasEm(LocalDate day, String... titles) {
        Instant start = day.atStartOfDay(SP).toInstant();
        List<Note> notes = Arrays.stream(titles).map(title -> {
            Note note = new Note(title, "conteúdo de " + title);
            note.setCreatedAt(start);
            note.setUpdatedAt(start);
            return note;
        }).toList();

        when(repository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                start, day.plusDays(1).atStartOfDay(SP).toInstant()))
                .thenReturn(notes);
    }
}
