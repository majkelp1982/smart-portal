package pl.smarthouse.module;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.core.RdbDimmerResponseDto;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneDto;

@Component
@RequiredArgsConstructor
public class ExternalLightsModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    return ExternalLightsModuleDto.builder()
        .entrance(
            LightZoneDto.builder()
                .rdbDimmerResponse(RdbDimmerResponseDto.builder().build())
                .build())
        .driveway(
            LightZoneDto.builder()
                .rdbDimmerResponse(RdbDimmerResponseDto.builder().build())
                .build())
        .carport(
            LightZoneDto.builder()
                .rdbDimmerResponse(RdbDimmerResponseDto.builder().build())
                .build())
        .garden(
            LightZoneDto.builder()
                .rdbDimmerResponse(RdbDimmerResponseDto.builder().build())
                .build())
        .build();
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.EXTERNAL_LIGHTS;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/lights";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final ExternalLightsModuleDto externalLightsModuleDto = (ExternalLightsModuleDto) moduleDto;
    final ExternalLightsModuleDto updateObject = (ExternalLightsModuleDto) updateObj;

    updateRdbDimmerResponseDto(
        externalLightsModuleDto.getEntrance().getRdbDimmerResponse(),
        updateObject.getEntrance().getRdbDimmerResponse());
    updateRdbDimmerResponseDto(
        externalLightsModuleDto.getDriveway().getRdbDimmerResponse(),
        updateObject.getDriveway().getRdbDimmerResponse());
    updateRdbDimmerResponseDto(
        externalLightsModuleDto.getCarport().getRdbDimmerResponse(),
        updateObject.getCarport().getRdbDimmerResponse());
    updateRdbDimmerResponseDto(
        externalLightsModuleDto.getGarden().getRdbDimmerResponse(),
        updateObject.getGarden().getRdbDimmerResponse());
  }

  private void updateRdbDimmerResponseDto(
      final RdbDimmerResponseDto rdbDimmerResponseDto, final RdbDimmerResponseDto update) {
    rdbDimmerResponseDto.setMode(update.getMode());
    rdbDimmerResponseDto.setState(update.isState());
    rdbDimmerResponseDto.setPower(update.getPower());
    rdbDimmerResponseDto.setGoalPower(update.getGoalPower());
    rdbDimmerResponseDto.setIncremental(update.isIncremental());
    rdbDimmerResponseDto.setMsDelay(update.getMsDelay());
  }
}
