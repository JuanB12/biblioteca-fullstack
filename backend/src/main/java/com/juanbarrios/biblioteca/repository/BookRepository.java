package com.juanbarrios.biblioteca.repository;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
              AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%')))
              AND (:status IS NULL OR b.status = :status)
            """)
    Page<Book> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("status") BookStatus status,
            Pageable pageable);

    long countByStatus(BookStatus status);
}
