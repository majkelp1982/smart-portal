package pl.smarthouse.service.module;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;

@Component
@RequiredArgsConstructor
public class ComfortModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    return ComfortModuleDto.builder().sensorResponse(Bme280ResponseDto.builder().build()).build();
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.COMFORT;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/comfort";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final ComfortModuleDto comfortDto = (ComfortModuleDto) moduleDto;
    final ComfortModuleDto updateObject = (ComfortModuleDto) updateObj;

    comfortDto.setFunctionType(updateObject.getFunctionType());
    comfortDto
        .getSensorResponse()
        .setTemperature(updateObject.getSensorResponse().getTemperature());
    comfortDto.getSensorResponse().setPressure(updateObject.getSensorResponse().getPressure());
    comfortDto.getSensorResponse().setHumidity(updateObject.getSensorResponse().getHumidity());
    comfortDto.getSensorResponse().setError(updateObject.getSensorResponse().isError());
    comfortDto
        .getSensorResponse()
        .setResponseUpdate(updateObject.getSensorResponse().getResponseUpdate());

    comfortDto.setCurrentOperation(updateObject.getCurrentOperation());
    comfortDto.setRequiredPower(updateObject.getRequiredPower());
    comfortDto.setLeftHoldTimeInMinutes(updateObject.getLeftHoldTimeInMinutes());
  }
}
