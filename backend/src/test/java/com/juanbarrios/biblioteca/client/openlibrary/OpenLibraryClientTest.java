package com.juanbarrios.biblioteca.client.openlibrary;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class OpenLibraryClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private OpenLibraryClient client;

    @BeforeEach
    void setUp() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(1))
                .withReadTimeout(Duration.ofSeconds(1));
        ClientHttpRequestFactory factory = ClientHttpRequestFactories.get(settings);

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.baseUrl())
                .requestFactory(factory)
                .build();

        client = new OpenLibraryClient(restClient, wireMock.baseUrl());
    }

    @Test
    void lookupByIsbn_returnsData_whenApiRespondsSuccessfully() {
        wireMock.stubFor(get(urlEqualTo("/isbn/9780307474728.json"))
                .willReturn(okJson("""
                        {
                          "title": "Cien anios de soledad",
                          "authors": [{"key": "/authors/OL123A"}],
                          "publish_date": "1967",
                          "subjects": ["Fiction", "Magical realism"]
                        }
                        """)));
        wireMock.stubFor(get(urlEqualTo("/authors/OL123A.json"))
                .willReturn(okJson("{\"name\": \"Gabriel Garcia Marquez\"}")));

        Optional<ExternalBookData> result = client.lookupByIsbn("9780307474728");

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Cien anios de soledad");
        assertThat(result.get().author()).isEqualTo("Gabriel Garcia Marquez");
        assertThat(result.get().publicationYear()).isEqualTo(1967);
        assertThat(result.get().coverUrl()).contains("9780307474728-L.jpg");
        assertThat(result.get().subjects()).contains("Fiction", "Magical realism");
    }

    @Test
    void lookupByIsbn_returnsEmpty_whenApiReturns404() {
        wireMock.stubFor(get(urlEqualTo("/isbn/0000000000.json")).willReturn(notFound()));

        Optional<ExternalBookData> result = client.lookupByIsbn("0000000000");

        assertThat(result).isEmpty();
    }

    @Test
    void lookupByIsbn_returnsEmpty_whenApiTimesOut() {
        wireMock.stubFor(get(urlEqualTo("/isbn/1111111111.json"))
                .willReturn(okJson("{}").withFixedDelay(3000)));

        Optional<ExternalBookData> result = client.lookupByIsbn("1111111111");

        assertThat(result).isEmpty();
    }

    @Test
    void lookupByIsbn_stillReturnsBookData_whenAuthorLookupFails() {
        wireMock.stubFor(get(urlEqualTo("/isbn/2222222222.json"))
                .willReturn(okJson("""
                        {
                          "title": "Libro sin autor resoluble",
                          "authors": [{"key": "/authors/OLBROKEN"}]
                        }
                        """)));
        wireMock.stubFor(get(urlEqualTo("/authors/OLBROKEN.json")).willReturn(serverError()));

        Optional<ExternalBookData> result = client.lookupByIsbn("2222222222");

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Libro sin autor resoluble");
        assertThat(result.get().author()).isNull();
    }
}
