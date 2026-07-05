package com.felipe.commonplace.tag;

import com.felipe.commonplace.tag.dto.TagResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    /**
     * Todas as tags em uso (com ao menos uma nota), com a contagem, em ordem alfabética.
     */
    @Query("""
            select new com.felipe.commonplace.tag.dto.TagResponse(t.name, count(n))
            from Tag t join t.notes n
            group by t.name
            order by t.name
            """)
    List<TagResponse> listWithCounts();
}
