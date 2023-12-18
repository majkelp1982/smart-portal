package pl.smarthouse.module;

import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.PinResponseDto;
import pl.smarthouse.sharedobjects.dto.core.Sds011ResponseDto;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import pl.smarthouse.utils.CreatorUtils;

@Component
public class WeatherModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    return WeatherModuleDto.builder()
        .sds011Response(Sds011ResponseDto.builder().build())
        .bme280Response(Bme280ResponseDto.builder().build())
        .lightIntense(PinResponseDto.builder().build())
        .build();
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.WEATHER;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/weather";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final WeatherModuleDto weatherModuleDto = (WeatherModuleDto) moduleDto;
    final WeatherModuleDto updateObject = (WeatherModuleDto) updateObj;

    // BME280
    CreatorUtils.updateBme280(
        weatherModuleDto.getBme280Response(), updateObject.getBme280Response());

    // SDS011
    final Sds011ResponseDto sds011ResponseDto = weatherModuleDto.getSds011Response();
    final Sds011ResponseDto sds011Update = updateObject.getSds011Response();

    sds011ResponseDto.setMode(sds011Update.getMode());
    sds011ResponseDto.setError(sds011Update.isError());
    sds011ResponseDto.setResponseUpdate(sds011Update.getResponseUpdate());
    sds011ResponseDto.setPm10(sds011Update.getPm10());
    sds011ResponseDto.setPm025(sds011Update.getPm025());

    // LightIntense
    weatherModuleDto.setLightIntense(updateObject.getLightIntense());
  }
}
