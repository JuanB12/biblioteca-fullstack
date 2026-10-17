package com.juanbarrios.biblioteca.client.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryEditionResponse(
        String title,
        List<AuthorRef> authors,
        @JsonProperty("publish_date") String publishDate,
        List<String> subjects) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuthorRef(String key) {
    }
}
