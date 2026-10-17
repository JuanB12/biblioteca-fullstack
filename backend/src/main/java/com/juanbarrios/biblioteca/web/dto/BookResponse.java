package com.juanbarrios.biblioteca.web.dto;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        BookStatus status,
        String coverUrl,
        String subjects) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getStatus(),
                book.getCoverUrl(),
                book.getSubjects());
    }
}
