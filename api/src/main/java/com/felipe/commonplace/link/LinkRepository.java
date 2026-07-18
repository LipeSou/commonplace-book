package com.felipe.commonplace.link;

import com.felipe.commonplace.link.dto.GraphEdge;
import com.felipe.commonplace.link.dto.GraphNode;
import com.felipe.commonplace.note.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {

    void deleteBySource(Note source);

    /**
     * As notas que apontam para esta (mais recentes primeiro). Auto-link não conta.
     */
    @Query("""
            select l.source from Link l
            where l.target = :note and l.source <> :note
            order by l.source.updatedAt desc
            """)
    List<Note> findSourcesByTarget(@Param("note") Note note);

    /**
     * Solta os links que apontavam para esta nota — ela sumiu ou foi renomeada.
     */
    @Modifying(flushAutomatically = true)
    @Query("update Link l set l.target = null where l.target = :note")
    void detachTarget(@Param("note") Note note);

    /**
     * Religa os links quebrados que pediam este título — a nota alvo nasceu (ou virou esta).
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            update Link l set l.target = :note
            where l.target is null and lower(l.targetTitle) = lower(:title)
            """)
    void attachTarget(@Param("note") Note note, @Param("title") String title);

    @Query("""
            select new com.felipe.commonplace.link.dto.GraphNode(
                n.id,
                n.title,
                (select count(l) from Link l where l.target = n and l.source <> n))
            from Note n
            order by n.title
            """)
    List<GraphNode> graphNodes();

    @Query("""
            select distinct new com.felipe.commonplace.link.dto.GraphEdge(l.source.id, l.target.id)
            from Link l
            where l.target is not null and l.source <> l.target
            """)
    List<GraphEdge> graphEdges();
}
