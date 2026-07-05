package com.felipe.commonplace.tag;

import com.felipe.commonplace.tag.dto.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repository;
    private final TagExtractor extractor;

    @Transactional(readOnly = true)
    public java.util.List<TagResponse> listAll() {
        return repository.listWithCounts();
    }

    /**
     * Deriva as tags do content e devolve as entidades Tag correspondentes
     * (reaproveitando as que já existem, criando as novas). Não altera o content.
     */
    @Transactional
    public Set<Tag> resolveFrom(String content) {
        Set<Tag> tags = new LinkedHashSet<>();
        for (String name : extractor.extract(content)) {
            tags.add(repository.findByName(name).orElseGet(() -> repository.save(new Tag(name))));
        }
        return tags;
    }
}
