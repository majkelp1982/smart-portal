package pl.smarthouse.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import lombok.experimental.UtilityClass;
import pl.smarthouse.exceptions.GuiServiceException;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.Ds18b20ResultDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.core.*;
import pl.smarthouse.sharedobjects.enums.ZoneName;

@UtilityClass
public class GuiServiceUtils {

  public Class<? extends ModuleDto> getModuleDtoClass(final String name) {
    if (name.toUpperCase().contains("VENTILATION")) {
      return VentModuleDto.class;
    }

    if (name.toUpperCase().contains("COMFORT")) {
      return ComfortModuleDto.class;
    }

    throw new GuiServiceException(
        String.format("Exception on getModuleDtoClass. Class not found for: %s", name));
  }

  public void updateData(final ModuleDto moduleDto, final ModuleDto updateObject) {
    if (moduleDto instanceof VentModuleDto) {
      // TODO

    } else if (moduleDto instanceof ComfortModuleDto) {
      updateComfortModule((ComfortModuleDto) moduleDto, (ComfortModuleDto) updateObject);
    } else {
      throw new GuiServiceException(
          String.format(
              "Exception on update data. Class not found for: %s", moduleDto.getClass().getName()));
    }
  }

  private void updateComfortModule(
      final ComfortModuleDto comfortDto, final ComfortModuleDto updateObject) {

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

    comfortDto.setUpdateTimestamp(LocalDateTime.now());
  }

  public ComfortModuleDto createBaseComfortDto() {
    return ComfortModuleDto.builder().sensorResponse(Bme280ResponseDto.builder().build()).build();
  }

  public VentModuleDto createBaseVentDto() {
    return VentModuleDto.builder()
        .zoneDtoHashMap(zoneDtoHashMap())
        .fans(fans())
        .intakeThrottle(IntakeThrottleDto.builder().build())
        .airExchanger(airExchangerDto())
        .forcedAirSystemExchanger(forcedAirSystemExchanger())
        .build();
  }

  private HashMap<ZoneName, ZoneDto> zoneDtoHashMap() {
    final HashMap<ZoneName, ZoneDto> result = new HashMap<>();
    Arrays.stream(ZoneName.values())
        .forEach(zoneName -> result.put(zoneName, ZoneDto.builder().build()));
    return result;
  }

  private FansDto fans() {
    return FansDto.builder()
        .inlet(FanDto.builder().build())
        .outlet(FanDto.builder().build())
        .build();
  }

  private AirExchangerDto airExchangerDto() {
    return AirExchangerDto.builder()
        .inlet(Bme280ResponseDto.builder().build())
        .outlet(Bme280ResponseDto.builder().build())
        .freshAir(Bme280ResponseDto.builder().build())
        .userAir(Bme280ResponseDto.builder().build())
        .build();
  }

  private ForcedAirSystemExchangerDto forcedAirSystemExchanger() {
    return ForcedAirSystemExchangerDto.builder()
        .watterIn(Ds18b20ResultDto.builder().build())
        .watterOut(Ds18b20ResultDto.builder().build())
        .airIn(Ds18b20ResultDto.builder().build())
        .airOut(Ds18b20ResultDto.builder().build())
        .build();
  }
}
