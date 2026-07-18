package com.felipe.commonplace.link;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Os links são índice descartável derivado do content — e notas escritas antes desta tabela
 * existir nunca passaram pelo parser. Se o índice está vazio, reconstrói tudo na subida.
 * Custa uma varredura numa base pessoal e deixa o grafo sempre fiel ao que está escrito.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkReindexRunner implements ApplicationRunner {

    private final LinkRepository repository;
    private final LinkService service;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        int notes = service.reindexAll();
        if (notes > 0) {
            log.info("Grafo reconstruído a partir do content de {} notas", notes);
        }
    }
}
