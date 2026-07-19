package com.felipe.commonplace.export;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Exportação", description = "As notas saem em `.md`, para abrir em qualquer outro app.")
public class ExportController {

    private final ExportService service;

    @GetMapping(produces = "application/zip")
    @Operation(summary = "Exporta todas as notas",
            description = """
                    Um zip com um arquivo `.md` por nota: frontmatter (título, datas, tags)
                    reconstruído no topo e o content bruto embaixo, byte a byte.
                    """)
    public ResponseEntity<byte[]> exportAll() {
        byte[] zip = service.zipAll();
        String fileName = "commonplace-book-" + LocalDate.now() + ".zip";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(zip);
    }
}
