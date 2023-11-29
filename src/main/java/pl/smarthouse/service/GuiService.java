package pl.smarthouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.exceptions.GuiServiceException;
import pl.smarthouse.properties.ModuleManagerProperties;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.SettingsDto;
import pl.smarthouse.utils.GuiServiceUtils;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class GuiService implements ApplicationListener<ApplicationStartedEvent> {
  private final WebService webService;
  private final ModuleManagerProperties moduleManagerProperties;
  private final List<ModuleDto> moduleList = new ArrayList<>();

  @Override
  public void onApplicationEvent(final ApplicationStartedEvent event) {
    initializeModels();
  }

  private void initializeModels() {
    webService
        .get(moduleManagerProperties.getURL() + "/all", SettingsDto.class)
        .filter(settingsDto -> !settingsDto.getType().isEmpty())
        .doOnNext(this::createModel)
        .subscribe();
  }

  private void createModel(final SettingsDto settingsDto) {

    ModuleDto moduleDto = null;
    if (settingsDto.getType().contains("VENTILATION")) {
      moduleDto = GuiServiceUtils.createBaseVentDto();
      moduleDto.setServiceAddress(settingsDto.getServiceAddress() + "/vent");
    }

    if (settingsDto.getType().contains("COMFORT")) {
      moduleDto = GuiServiceUtils.createBaseComfortDto();
      moduleDto.setServiceAddress(settingsDto.getServiceAddress() + "/comfort");
    }

    if (settingsDto.getType().contains("WEATHER")) {
      moduleDto = GuiServiceUtils.createBaseWeatherDto();
      moduleDto.setServiceAddress(settingsDto.getServiceAddress() + "/weather");
    }

    if (settingsDto.getType().contains("EXTERNAL_LIGHTS")) {
      moduleDto = GuiServiceUtils.createBaseExternalLightsDto();
      moduleDto.setServiceAddress(settingsDto.getServiceAddress() + "/lights");
    }

    if (settingsDto.getType().contains("FIREPLACE")) {
      moduleDto = GuiServiceUtils.createBaseFireplaceDto();
      moduleDto.setServiceAddress(settingsDto.getServiceAddress() + "/fireplace");
    }

    if (Objects.isNull(moduleDto)) {
      throw new GuiServiceException(
          String.format(
              "Create Model failed. Creator not found for type: %s", settingsDto.getType()));
    }

    moduleDto.setModuleName(settingsDto.getType());
    moduleList.add(moduleDto);
  }

  @Scheduled(fixedDelay = 10000)
  public void refreshScheduler() {
    moduleList.forEach(moduleDto -> updateModel(moduleDto.getModuleName()));
  }

  private void updateModel(final String name) {
    moduleList.stream()
        .filter(moduleDto -> moduleDto.getModuleName().contains(name))
        .forEach(
            moduleDto ->
                webService
                    .get(
                        moduleDto.getServiceAddress(),
                        GuiServiceUtils.getModuleDtoClass(moduleDto.getModuleName()))
                    .doOnNext(updateData -> GuiServiceUtils.updateData(moduleDto, updateData))
                    .subscribe());
  }

  public List<ModuleDto> getModuleDtos() {
    return moduleList;
  }
}
