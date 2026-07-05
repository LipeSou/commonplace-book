package com.felipe.commonplace.tag;

import com.felipe.commonplace.tag.dto.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tags derivadas dos `#hashtags` do content. Só leitura — a fonte é sempre a nota.")
public class TagController {

    private final TagService service;

    @GetMapping
    @Operation(summary = "Lista as tags em uso",
            description = "Todas as tags com ao menos uma nota, com a contagem, em ordem alfabética.")
    public List<TagResponse> findAll() {
        return service.listAll();
    }
}
