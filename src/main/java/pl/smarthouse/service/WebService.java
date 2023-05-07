package pl.smarthouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebService {
  private final WebClient webClient;

  public <T> Flux<T> get(String url, final Class<T> tClass) {
    if (!url.contains("http")) {
      url = "http://" + url;
    }
    return webClient.get().uri(url).exchangeToFlux(response -> processResponse(response, tClass));
  }

  private <T> Flux<T> processResponse(final ClientResponse clientResponse, final Class<T> clazz) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToFlux(clazz);
    } else {
      return clientResponse
          .createError()
          .flatMapMany(
              error -> {
                log.error("Error while processing response: {}", error);
                return Mono.error((Throwable) error);
              });
    }
  }
}
