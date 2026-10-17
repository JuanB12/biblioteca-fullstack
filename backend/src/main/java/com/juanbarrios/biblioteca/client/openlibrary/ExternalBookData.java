package com.juanbarrios.biblioteca.client.openlibrary;

public record ExternalBookData(
        String title,
        String author,
        Integer publicationYear,
        String coverUrl,
        String subjects) {
}
