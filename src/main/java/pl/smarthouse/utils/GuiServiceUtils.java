package pl.smarthouse.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pl.smarthouse.exceptions.GuiServiceException;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.core.*;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.core.*;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;

@UtilityClass
@Slf4j
public class GuiServiceUtils {

  public Class<? extends ModuleDto> getModuleDtoClass(final String name) {
    if (name.toUpperCase().contains("VENTILATION")) {
      return VentModuleDto.class;
    }

    if (name.toUpperCase().contains("COMFORT")) {
      return ComfortModuleDto.class;
    }

    if (name.toUpperCase().contains("EXTERNAL_LIGHTS")) {
      return ExternalLightsModuleDto.class;
    }

    if (name.toUpperCase().contains("WEATHER")) {
      return WeatherModuleDto.class;
    }

    throw new GuiServiceException(
        String.format("Exception on getModuleDtoClass. Class not found for: %s", name));
  }

  public void updateData(final ModuleDto moduleDto, final ModuleDto updateObject) {
    // Update basic values
    moduleDto.setError(updateObject.isError());
    moduleDto.setErrorPendingAcknowledge(updateObject.isErrorPendingAcknowledge());
    moduleDto.setUpdateTimestamp(LocalDateTime.now());

    // Update module specific
    if (moduleDto instanceof VentModuleDto) {
      updateVentModule((VentModuleDto) moduleDto, (VentModuleDto) updateObject);
    } else if (moduleDto instanceof ComfortModuleDto) {
      updateComfortModule((ComfortModuleDto) moduleDto, (ComfortModuleDto) updateObject);
    } else if (moduleDto instanceof ExternalLightsModuleDto) {
      updateExternalLightsModule(
          (ExternalLightsModuleDto) moduleDto, (ExternalLightsModuleDto) updateObject);
    } else if (moduleDto instanceof WeatherModuleDto) {
      updateWeatherModule((WeatherModuleDto) moduleDto, (WeatherModuleDto) updateObject);
    } else {
      throw new GuiServiceException(
          String.format(
              "Exception on update data. Class not found for: %s", moduleDto.getClass().getName()));
    }
  }

  private void updateVentModule(final VentModuleDto ventDto, final VentModuleDto updateObject) {
    ventDto.getZoneDtoHashMap().keySet().stream()
        .forEach(
            zoneName ->
                updateVentZone(
                    ventDto.getZoneDtoHashMap().get(zoneName),
                    updateObject.getZoneDtoHashMap().get(zoneName)));

    updateVentFan(ventDto.getFans().getInlet(), updateObject.getFans().getInlet());
    updateVentFan(ventDto.getFans().getOutlet(), updateObject.getFans().getOutlet());

    ventDto
        .getIntakeThrottle()
        .setCurrentPosition(updateObject.getIntakeThrottle().getCurrentPosition());
    ventDto.getIntakeThrottle().setGoalPosition(updateObject.getIntakeThrottle().getGoalPosition());

    updateBme280(ventDto.getAirExchanger().getInlet(), updateObject.getAirExchanger().getInlet());
    updateBme280(ventDto.getAirExchanger().getOutlet(), updateObject.getAirExchanger().getOutlet());
    updateBme280(
        ventDto.getAirExchanger().getFreshAir(), updateObject.getAirExchanger().getFreshAir());
    updateBme280(
        ventDto.getAirExchanger().getUserAir(), updateObject.getAirExchanger().getUserAir());

    updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getWatterIn(),
        updateObject.getForcedAirSystemExchanger().getWatterIn());
    updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getWatterOut(),
        updateObject.getForcedAirSystemExchanger().getWatterOut());
    updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getAirIn(),
        updateObject.getForcedAirSystemExchanger().getAirIn());
    updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getAirOut(),
        updateObject.getForcedAirSystemExchanger().getAirOut());

    ventDto.setCircuitPump(updateObject.getCircuitPump());
    ventDto.setAirCondition(updateObject.getAirCondition());
  }

  private void updateVentZone(final ZoneDto zoneDto, final ZoneDto updateZone) {
    zoneDto.setLastUpdate(LocalDateTime.now());
    zoneDto.setFunctionType(updateZone.getFunctionType());
    zoneDto.setOperation(updateZone.getOperation());
    zoneDto.getThrottle().setCurrentPosition(updateZone.getThrottle().getCurrentPosition());
    zoneDto.getThrottle().setGoalPosition(updateZone.getThrottle().getGoalPosition());
    zoneDto.setRequiredPower(updateZone.getRequiredPower());
  }

  private void updateBme280(
      final Bme280ResponseDto bme280ResponseDto, final Bme280ResponseDto updateBme280) {
    bme280ResponseDto.setTemperature(updateBme280.getTemperature());
    bme280ResponseDto.setPressure(updateBme280.getPressure());
    bme280ResponseDto.setHumidity(updateBme280.getHumidity());
    bme280ResponseDto.setError(updateBme280.isError());
    bme280ResponseDto.setResponseUpdate(updateBme280.getResponseUpdate());
  }

  private void updateDs18b20(final Ds18b20ResultDto sensor, final Ds18b20ResultDto updateSensor) {
    sensor.setAddress(updateSensor.getAddress());
    sensor.setTemp(updateSensor.getTemp());
    sensor.setError(updateSensor.isError());
    sensor.setLastUpdate(updateSensor.getLastUpdate());
  }

  private void updateVentFan(final FanDto fanDto, final FanDto updateFan) {
    fanDto.setCurrentSpeed(updateFan.getCurrentSpeed());
    fanDto.setGoalSpeed(updateFan.getGoalSpeed());
    fanDto.setRevolution(updateFan.getRevolution());
  }

  private void updateComfortModule(
      final ComfortModuleDto comfortDto, final ComfortModuleDto updateObject) {

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

  public ComfortModuleDto createBaseComfortDto() {
    return ComfortModuleDto.builder().sensorResponse(Bme280ResponseDto.builder().build()).build();
  }

  public WeatherModuleDto createBaseWeatherDto() {
    return WeatherModuleDto.builder()
        .sds011Response(Sds011ResponseDto.builder().build())
        .bme280Response(Bme280ResponseDto.builder().build())
        .lightIntense(PinResponseDto.builder().build())
        .build();
  }

  public ExternalLightsModuleDto createBaseExternalLightsDto() {
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
        .forEach(
            zoneName ->
                result.put(
                    zoneName, ZoneDto.builder().throttle(ThrottleDto.builder().build()).build()));
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

  private void updateExternalLightsModule(
      final ExternalLightsModuleDto externalLightsModuleDto,
      final ExternalLightsModuleDto updateObject) {
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

  private void updateWeatherModule(
      final WeatherModuleDto weatherModuleDto, final WeatherModuleDto updateObject) {
    // BME280
    updateBme280(weatherModuleDto.getBme280Response(), updateObject.getBme280Response());

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
