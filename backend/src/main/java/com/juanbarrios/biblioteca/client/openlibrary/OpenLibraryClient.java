package com.juanbarrios.biblioteca.client.openlibrary;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


@Component
public class OpenLibraryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    private final RestClient restClient;
    private final String coversBaseUrl;

    public OpenLibraryClient(
            RestClient openLibraryRestClient,
            @Value("${app.openlibrary.covers-url}") String coversBaseUrl) {
        this.restClient = openLibraryRestClient;
        this.coversBaseUrl = coversBaseUrl;
    }

    @Cacheable(cacheNames = "openLibraryLookup", key = "#isbn")
    public Optional<ExternalBookData> lookupByIsbn(String isbn) {
        try {
            OpenLibraryEditionResponse edition = restClient.get()
                    .uri("/isbn/{isbn}.json", isbn)
                    .retrieve()
                    .body(OpenLibraryEditionResponse.class);

            if (edition == null) {
                return Optional.empty();
            }
            return Optional.of(toExternalBookData(edition, isbn));
        } catch (Exception e) {
            log.warn("Fallo la consulta a Open Library para ISBN {}: {}", isbn, e.toString());
            return Optional.empty();
        }
    }

    private ExternalBookData toExternalBookData(OpenLibraryEditionResponse edition, String isbn) {
        String author = resolveFirstAuthorName(edition.authors());
        Integer year = extractYear(edition.publishDate());
        String subjects = (edition.subjects() == null || edition.subjects().isEmpty())
                ? null
                : String.join(", ", edition.subjects());
        String coverUrl = coversBaseUrl + "/b/isbn/" + isbn + "-L.jpg";

        return new ExternalBookData(edition.title(), author, year, coverUrl, subjects);
    }

    private String resolveFirstAuthorName(List<OpenLibraryEditionResponse.AuthorRef> authors) {
        if (authors == null || authors.isEmpty() || authors.get(0).key() == null) {
            return null;
        }
        String key = authors.get(0).key();
        try {
            OpenLibraryAuthorResponse author = restClient.get()
                    .uri(key + ".json")
                    .retrieve()
                    .body(OpenLibraryAuthorResponse.class);
            return author != null ? author.name() : null;
        } catch (RestClientException e) {
            log.warn("Fallo al resolver el nombre del autor {}: {}", key, e.toString());
            return null;
        }
    }

    private Integer extractYear(String publishDate) {
        if (publishDate == null) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(publishDate);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
