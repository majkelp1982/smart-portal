package pl.smarthouse.service.module;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ModuleService {
  private final WebService webService;
  private final ModuleManagerProperties moduleManagerProperties;
  private final List<ModuleDto> moduleList;
  private final Set<ModuleCreator> moduleCreators;

  @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 30)
  private void updateModels() {
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
        .doOnNext(this::updateModuleList)
        .subscribe();
  }

  private void updateModuleList(final SettingsDto settingsDto) {
    final ModuleCreator moduleCreator = getModuleCreator(settingsDto.getType());
    final Optional<ModuleDto> moduleDtoOptional =
        moduleList.stream()
            .filter(module -> module.getType().equals(settingsDto.getType()))
            .findFirst();
    ModuleDto moduleDto = null;
    if (moduleDtoOptional.isPresent()) {
      moduleDto = moduleDtoOptional.get();
    } else {
      moduleDto = moduleCreator.createBaseModel();
      moduleDto.setType(settingsDto.getType());
      moduleList.add(moduleDto);
    }
    moduleDto.setServiceAddress(
        moduleCreator.enrichServiceAddress(settingsDto.getServiceAddress()));
    moduleDto.setModuleMacAddress(settingsDto.getModuleMacAddress());
    moduleDto.setServiceVersion(settingsDto.getServiceVersion());
    moduleDto.setModuleFirmwareVersion(settingsDto.getModuleFirmwareVersion());
    moduleDto.setModuleIpAddress(settingsDto.getModuleIpAddress());
    moduleDto.setModuleUpdateTimestamp(
        LocalDateTime.ofInstant(settingsDto.getModuleUpdateTimestamp(), ZoneId.systemDefault()));
    moduleDto.setServiceUpdateTimestamp(
        LocalDateTime.ofInstant(settingsDto.getServiceUpdateTimestamp(), ZoneId.systemDefault()));
    moduleDto.setConnectionEstablish(settingsDto.isConnectionEstablish());
    moduleDto.setUptimeInMinutes(settingsDto.getUptimeInMinutes());
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
                            getModuleCreator(moduleDto.getType())
                                .updateData(moduleDto, updateData)))
        .subscribe();
  }

  public List<ModuleDto> getModuleDtos() {
    return moduleList;
  }
}
