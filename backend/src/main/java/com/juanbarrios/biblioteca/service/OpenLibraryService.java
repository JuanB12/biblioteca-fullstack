package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.client.openlibrary.ExternalBookData;
import com.juanbarrios.biblioteca.client.openlibrary.OpenLibraryClient;
import com.juanbarrios.biblioteca.exception.ExternalBookLookupException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private final OpenLibraryClient openLibraryClient;

    public ExternalBookData preview(String isbn) {
        return openLibraryClient.lookupByIsbn(isbn)
                .orElseThrow(() -> new ExternalBookLookupException(
                        "No se pudo obtener informacion para el ISBN " + isbn + " desde Open Library", null));
    }
}
