package com.juanbarrios.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.exception.BookNotAvailableException;
import com.juanbarrios.biblioteca.exception.DuplicateIsbnException;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.web.dto.BookCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    @Test
    void create_throwsDuplicateIsbn_whenIsbnAlreadyExists() {
        BookCreateRequest request = new BookCreateRequest("Titulo", "Autor", "111", 2020, null, null);
        when(bookRepository.existsByIsbn("111")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(DuplicateIsbnException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void create_savesBook_whenIsbnIsNew() {
        BookCreateRequest request = new BookCreateRequest("Titulo", "Autor", "222", 2020, null, null);
        when(bookRepository.existsByIsbn("222")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book saved = bookService.create(request);

        assertThat(saved.getStatus()).isEqualTo(BookStatus.DISPONIBLE);
        assertThat(saved.getIsbn()).isEqualTo("222");
    }

    @Test
    void delete_throwsBookNotAvailable_whenStatusIsNotDisponible() {
        Book book = Book.builder().id(1L).isbn("333").status(BookStatus.PRESTADO).build();
        when(bookRepository.findById(1L)).thenReturn(java.util.Optional.of(book));

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BookNotAvailableException.class);

        verify(bookRepository, never()).delete(any());
    }

    @Test
    void delete_removesBook_whenDisponible() {
        Book book = Book.builder().id(2L).isbn("444").status(BookStatus.DISPONIBLE).build();
        when(bookRepository.findById(2L)).thenReturn(java.util.Optional.of(book));

        bookService.delete(2L);

        verify(bookRepository).delete(book);
    }

    @Test
    void getByIdOrThrow_throwsResourceNotFound_whenMissing() {
        when(bookRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> bookService.getByIdOrThrow(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
