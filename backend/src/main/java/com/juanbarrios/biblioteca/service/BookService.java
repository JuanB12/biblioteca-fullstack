package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.exception.BookNotAvailableException;
import com.juanbarrios.biblioteca.exception.DuplicateIsbnException;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.web.dto.BookCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public Book create(BookCreateRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException(request.isbn());
        }

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publicationYear(request.publicationYear())
                .coverUrl(request.coverUrl())
                .subjects(request.subjects())
                .status(BookStatus.DISPONIBLE)
                .build();

        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getByIdOrThrow(id);
        if (book.getStatus() != BookStatus.DISPONIBLE) {
            throw new BookNotAvailableException(
                    "Solo se pueden eliminar libros en estado DISPONIBLE (actual: " + book.getStatus() + ")");
        }
        bookRepository.delete(book);
    }

    public Page<Book> search(String title, String author, BookStatus status, Pageable pageable) {
        return bookRepository.search(blankToNull(title), blankToNull(author), status, pageable);
    }

    public Book getByIdOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un libro con id: " + id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
