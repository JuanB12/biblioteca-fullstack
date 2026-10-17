package com.juanbarrios.biblioteca.client.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryAuthorResponse(String name) {
}
