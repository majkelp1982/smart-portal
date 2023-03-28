package pl.smarthouse.gui;

import static pl.smarthouse.properties.GuiComfortServiceProperties.COMFORT_TYPE_PREFIX;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smarthouse.model.dto.SettingsDto;
import pl.smarthouse.properties.GuiComfortServiceProperties;
import pl.smarthouse.properties.ModuleManagerProperties;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.comfort.ComfortView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class GuiComfortService {
  private final WebClient webClient;
  private final ModuleManagerProperties moduleManagerProperties;
  private final GuiComfortServiceProperties guiComfortServiceProperties;

  private final ComfortView comfortView;

  @Scheduled(fixedDelay = 10000)
  public void generateComfortGui() {
    if (guiComfortServiceProperties.isSynced()) {
      return;
    }

    webClient
        .get()
        .uri(moduleManagerProperties.getURL() + "/all")
        .exchangeToFlux(this::processResponse)
        .filter(
            settingsDto ->
                Objects.nonNull(settingsDto.getModuleType())
                    && settingsDto.getModuleType().contains(COMFORT_TYPE_PREFIX))
        .flatMap(this::generateGui)
        .map(
            settingsDto -> {
              guiComfortServiceProperties.setSynced(true);
              return settingsDto;
            })
        .doOnError(
            throwable -> {
              log.error(
                  "Error while generate comfort GUI. Message: {}",
                  throwable.getMessage(),
                  throwable);
              guiComfortServiceProperties.setSynced(false);
            })
        .subscribe();
  }

  private Mono<SettingsDto> generateGui(final SettingsDto settingsDto) {
    return Mono.just(settingsDto.getModuleType())
        .map(moduleType -> moduleType.substring(moduleType.indexOf("_") + 1))
        .flatMap(zoneName -> Mono.just(comfortView.createZone(ZoneName.valueOf(zoneName))))
        .thenReturn(settingsDto)
        .doOnSubscribe(
            subscription -> log.info("Create COMFORT gui for: {}", settingsDto.getModuleType()));
  }

  private Flux<SettingsDto> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToFlux(SettingsDto.class);
    } else {
      return clientResponse
          .createError()
          .flatMapMany(
              error -> {
                log.error("Error while process response: {}", error);
                return Mono.error((Throwable) error);
              });
    }
  }
}
