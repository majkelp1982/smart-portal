package pl.smarthouse.service.module;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightsMqttDto;

@Component
@RequiredArgsConstructor
public class LightsMqttModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    LightsMqttDto lightsMqttDto = new LightsMqttDto();
    lightsMqttDto.setLights(new HashMap<>());
    lightsMqttDto.setMotionSensors(new HashMap<>());
    lightsMqttDto.setRequireZoneStates(new HashMap<>());
    return lightsMqttDto;
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.LIGHTS_MQTT;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/lightsmqtt";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final LightsMqttDto lightsMqttDto = (LightsMqttDto) moduleDto;
    final LightsMqttDto updateObject = (LightsMqttDto) updateObj;

    lightsMqttDto.getLights().putAll(updateObject.getLights());
    lightsMqttDto.getRequireZoneStates().putAll(updateObject.getRequireZoneStates());
    lightsMqttDto.getMotionSensors().putAll(updateObject.getMotionSensors());
  }
}
