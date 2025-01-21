package pl.smarthouse.service.module;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.SpaDevice;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.enums.SpaDeviceState;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.utils.CreatorUtils;

@Component
@RequiredArgsConstructor
public class ChillZoneModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    return ChillZoneModuleDto.builder()
        .chillRoom(createInitSpaDevice())
        .sauna(createInitSpaDevice())
        .build();
  }

  private SpaDevice createInitSpaDevice() {
    SpaDevice spaDevice = new SpaDevice();
    spaDevice.setState(State.OFF);
    spaDevice.setRelayState(SpaDeviceState.OFF);
    spaDevice.setStateOnTriggerTimeStamp(LocalDateTime.now());
    spaDevice.setBme280ResponseDto(new Bme280ResponseDto());
    return spaDevice;
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.CHILL_ZONE;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/chillzone";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final ChillZoneModuleDto chillZoneModuleDto = (ChillZoneModuleDto) moduleDto;
    final ChillZoneModuleDto updateObject = (ChillZoneModuleDto) updateObj;
    updateSpaDevice(chillZoneModuleDto.getSauna(), updateObject.getSauna());
    updateSpaDevice(chillZoneModuleDto.getChillRoom(), updateObject.getChillRoom());
  }

  private void updateSpaDevice(SpaDevice device, SpaDevice updateObject) {
    device.setState(updateObject.getState());
    device.setRelayState(updateObject.getRelayState());
    device.setLeftHoldTimeInMinutes(updateObject.getLeftHoldTimeInMinutes());
    CreatorUtils.updateBme280(device.getBme280ResponseDto(), updateObject.getBme280ResponseDto());
  }
}
