package com.juanbarrios.biblioteca.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.exception.DuplicateIsbnException;
import com.juanbarrios.biblioteca.security.JwtAuthenticationFilter;
import com.juanbarrios.biblioteca.service.BookService;
import com.juanbarrios.biblioteca.service.OpenLibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private OpenLibraryService openLibraryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void search_returns200_withBookList() throws Exception {
        Book book = Book.builder().id(1L).title("Cien anios de soledad").author("Garcia Marquez")
                .isbn("123").status(BookStatus.DISPONIBLE).build();
        when(bookService.search(any(), any(), any(), any())).thenReturn(new PageImpl<>(java.util.List.of(book)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Cien anios de soledad"))
                .andExpect(jsonPath("$.content[0].status").value("DISPONIBLE"));
    }

    @Test
    void create_returns400_whenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Autor","isbn":"111"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/books"));
    }

    @Test
    void create_returns201_whenValid() throws Exception {
        Book saved = Book.builder().id(5L).title("Rayuela").author("Cortazar").isbn("222")
                .status(BookStatus.DISPONIBLE).build();
        when(bookService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Rayuela","author":"Cortazar","isbn":"222"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.isbn").value("222"));
    }

    @Test
    void create_returns409_whenIsbnIsDuplicate() throws Exception {
        when(bookService.create(any())).thenThrow(new DuplicateIsbnException("222"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Rayuela","author":"Cortazar","isbn":"222"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un libro registrado con el ISBN: 222"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", 5L))
                .andExpect(status().isNoContent());
    }
}
