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
      updateVentModule((VentModuleDto) moduleDto, (VentModuleDto) updateObject);

    } else if (moduleDto instanceof ComfortModuleDto) {
      updateComfortModule((ComfortModuleDto) moduleDto, (ComfortModuleDto) updateObject);
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

    ventDto.setUpdateTimestamp(LocalDateTime.now());
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
}
