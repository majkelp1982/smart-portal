package pl.smarthouse.module;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.exceptions.ModuleCreatorException;
import pl.smarthouse.properties.ModuleManagerProperties;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.SettingsDto;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class ModuleService implements ApplicationListener<ApplicationStartedEvent> {
  private final WebService webService;
  private final ModuleManagerProperties moduleManagerProperties;
  private final List<ModuleDto> moduleList;
  private final List<ModuleCreator> moduleCreators;

  @Override
  public void onApplicationEvent(final ApplicationStartedEvent event) {
    initializeModels();
  }

  private void initializeModels() {
    webService
        .get(moduleManagerProperties.getURL() + "/all", SettingsDto.class)
        .filter(
            settingsDto ->
                Arrays.stream(ModuleCreatorType.values())
                    .anyMatch(
                        moduleCreatorType ->
                            settingsDto.getType().contains(moduleCreatorType.toString())))
        .doOnDiscard(
            SettingsDto.class,
            discardedElement ->
                log.error("For type: {}, no module creator is defined", discardedElement.getType()))
        .doOnNext(this::createModel)
        .subscribe();
  }

  private void createModel(final SettingsDto settingsDto) {
    final ModuleCreator moduleCreator = getModuleCreator(settingsDto.getType());
    final ModuleDto moduleDto = moduleCreator.createBaseModel();
    moduleDto.setServiceAddress(
        moduleCreator.enrichServiceAddress(settingsDto.getServiceAddress()));
    moduleDto.setModuleName(settingsDto.getType());
    moduleList.add(moduleDto);
  }

  private ModuleCreator getModuleCreator(final String type) {
    return moduleCreators.stream()
        .filter(moduleCreator -> type.contains(moduleCreator.getModuleCreatorType().toString()))
        .findFirst()
        .orElseThrow(
            () ->
                new ModuleCreatorException(
                    String.format("Module creator for type: %s not found", type)));
  }

  @Scheduled(fixedDelay = 5000)
  public void updateModelsScheduler() {
    Flux.fromIterable(moduleList)
        .flatMap(
            moduleDto ->
                webService
                    .get(moduleDto.getServiceAddress(), moduleDto.getClass())
                    .doOnNext(
                        updateData ->
                            getModuleCreator(moduleDto.getModuleName())
                                .updateData(moduleDto, updateData)))
        .subscribe();
  }

  public List<ModuleDto> getModuleDtos() {
    return moduleList;
  }
}
