package com.example.weatheragent.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfiguration {

    @Bean
    @Qualifier("qWeatherRestClient")
    RestClient qWeatherRestClient(QWeatherProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.normalizedBaseUrl())
                .defaultHeader("X-QW-Api-Key", properties.apiKey() == null ? "" : properties.apiKey())
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) ->
                        decodeGzipIfNecessary(execution.execute(request, body)))
                .build();
    }

    private ClientHttpResponse decodeGzipIfNecessary(ClientHttpResponse response) throws IOException {
        List<String> encodings = response.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        boolean gzipEncoded = encodings.stream()
                .flatMap(value -> List.of(value.split(",")).stream())
                .map(String::trim)
                .anyMatch("gzip"::equalsIgnoreCase);

        return gzipEncoded ? new GzipClientHttpResponse(response) : response;
    }

    private static final class GzipClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse delegate;
        private final HttpHeaders headers;

        private GzipClientHttpResponse(ClientHttpResponse delegate) {
            this.delegate = delegate;
            this.headers = new HttpHeaders();
            this.headers.putAll(delegate.getHeaders());
            this.headers.remove(HttpHeaders.CONTENT_ENCODING);
            this.headers.remove(HttpHeaders.CONTENT_LENGTH);
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public InputStream getBody() throws IOException {
            return new GZIPInputStream(delegate.getBody());
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
