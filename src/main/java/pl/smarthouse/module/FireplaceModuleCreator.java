package pl.smarthouse.module;

import org.springframework.stereotype.Component;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Ds18b20ResultDto;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.sharedobjects.dto.fireplace.core.Throttle;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.utils.CreatorUtils;

@Component
public class FireplaceModuleCreator extends ModuleCreator {

  @Override
  public ModuleDto createBaseModel() {
    return FireplaceModuleDto.builder()
        .mode(Mode.OFF)
        .state(State.OFF)
        .waterIn(Ds18b20ResultDto.builder().build())
        .waterOut(Ds18b20ResultDto.builder().build())
        .chimney(Ds18b20ResultDto.builder().build())
        .pump(State.OFF)
        .throttle(new Throttle())
        .build();
  }

  @Override
  public ModuleCreatorType getModuleCreatorType() {
    return ModuleCreatorType.FIREPLACE;
  }

  @Override
  public String enrichServiceAddress(final String serviceAddress) {
    return serviceAddress + "/fireplace";
  }

  @Override
  public void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObj) {
    final FireplaceModuleDto fireplaceModuleDto = (FireplaceModuleDto) moduleDto;
    final FireplaceModuleDto updateObject = (FireplaceModuleDto) updateObj;

    fireplaceModuleDto.setMode(updateObject.getMode());
    fireplaceModuleDto.setState(updateObject.getState());

    CreatorUtils.updateDs18b20(fireplaceModuleDto.getWaterIn(), updateObject.getWaterIn());
    CreatorUtils.updateDs18b20(fireplaceModuleDto.getWaterOut(), updateObject.getWaterOut());
    CreatorUtils.updateDs18b20(fireplaceModuleDto.getChimney(), updateObject.getChimney());

    fireplaceModuleDto.setPump(updateObject.getPump());
    fireplaceModuleDto.getThrottle().setGoalPosition(updateObject.getThrottle().getGoalPosition());
    fireplaceModuleDto
        .getThrottle()
        .setCurrentPosition(updateObject.getThrottle().getCurrentPosition());
  }
}
