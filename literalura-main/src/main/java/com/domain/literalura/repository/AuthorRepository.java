package com.domain.literalura.repository;

import com.domain.literalura.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("SELECT a FROM Author a WHERE a.name LIKE %:name%")
    Optional<Author> findByName(@Param("name") String name);

    @Query("SELECT a FROM Author a WHERE :year BETWEEN CAST(a.birth_year AS integer) AND CAST(a.death_year AS integer)")
    List<Author> findAuthorsAlive(@Param("year") int year);
}
