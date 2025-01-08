package com.domain.literalura.repository;

import com.domain.literalura.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT COUNT(b) > 0 FROM Book b WHERE b.title LIKE %:title%")
    Boolean verifiedBDExistence(@Param("title") String title);

    @Query(value = "SELECT * FROM books WHERE :language = ANY (books.languages)", nativeQuery = true)
    List<Book> findBooksByLanguage(@Param("language") String language);

    @Query("SELECT b FROM Book b ORDER BY b.downloads DESC LIMIT 10")
    List<Book> findTop10();
}
