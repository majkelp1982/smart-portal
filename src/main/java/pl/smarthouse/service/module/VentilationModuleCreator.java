package pl.smarthouse.service.module;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.Ds18b20ResultDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.core.*;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.utils.CreatorUtils;

@Component
@RequiredArgsConstructor
public class VentilationModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
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

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.VENTILATION;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/vent";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final VentModuleDto ventDto = (VentModuleDto) moduleDto;
    final VentModuleDto updateObject = (VentModuleDto) updateObj;

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

    CreatorUtils.updateBme280(
        ventDto.getAirExchanger().getInlet(), updateObject.getAirExchanger().getInlet());
    CreatorUtils.updateBme280(
        ventDto.getAirExchanger().getOutlet(), updateObject.getAirExchanger().getOutlet());
    CreatorUtils.updateBme280(
        ventDto.getAirExchanger().getFreshAir(), updateObject.getAirExchanger().getFreshAir());
    CreatorUtils.updateBme280(
        ventDto.getAirExchanger().getUserAir(), updateObject.getAirExchanger().getUserAir());

    CreatorUtils.updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getWatterIn(),
        updateObject.getForcedAirSystemExchanger().getWatterIn());
    CreatorUtils.updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getWatterOut(),
        updateObject.getForcedAirSystemExchanger().getWatterOut());
    CreatorUtils.updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getAirIn(),
        updateObject.getForcedAirSystemExchanger().getAirIn());
    CreatorUtils.updateDs18b20(
        ventDto.getForcedAirSystemExchanger().getAirOut(),
        updateObject.getForcedAirSystemExchanger().getAirOut());

    ventDto.setCircuitPump(updateObject.getCircuitPump());
    ventDto.setAirCondition(updateObject.getAirCondition());
    ventDto.setFireplaceAirOverpressureActive(updateObject.getFireplaceAirOverpressureActive());
  }

  private void updateVentZone(final ZoneDto zoneDto, final ZoneDto updateZone) {
    zoneDto.setLastUpdate(LocalDateTime.now());
    zoneDto.setFunctionType(updateZone.getFunctionType());
    zoneDto.setOperation(updateZone.getOperation());
    zoneDto.getThrottle().setCurrentPosition(updateZone.getThrottle().getCurrentPosition());
    zoneDto.getThrottle().setGoalPosition(updateZone.getThrottle().getGoalPosition());
    zoneDto.setRequiredPower(updateZone.getRequiredPower());
  }

  private void updateVentFan(final FanDto fanDto, final FanDto updateFan) {
    fanDto.setCurrentSpeed(updateFan.getCurrentSpeed());
    fanDto.setGoalSpeed(updateFan.getGoalSpeed());
    fanDto.setRevolution(updateFan.getRevolution());
  }
}
