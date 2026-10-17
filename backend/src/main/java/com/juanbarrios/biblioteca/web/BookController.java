package com.juanbarrios.biblioteca.web;

import com.juanbarrios.biblioteca.client.openlibrary.ExternalBookData;
import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.service.BookService;
import com.juanbarrios.biblioteca.service.OpenLibraryService;
import com.juanbarrios.biblioteca.web.dto.BookCreateRequest;
import com.juanbarrios.biblioteca.web.dto.BookResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final OpenLibraryService openLibraryService;

    @GetMapping
    public Page<BookResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) BookStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return bookService.search(title, author, status, pageable).map(BookResponse::from);
    }

    @GetMapping("/lookup/{isbn}")
    public ExternalBookData lookup(@PathVariable String isbn) {
        return openLibraryService.preview(isbn);
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest request) {
        Book book = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
