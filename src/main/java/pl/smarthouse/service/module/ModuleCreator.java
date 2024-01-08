package pl.smarthouse.service.module;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.sharedobjects.dto.ModuleDto;

@RequiredArgsConstructor
public abstract class ModuleCreator {
  public abstract ModuleDto createBaseModel();

  protected abstract void updateDataSpec(final ModuleDto moduleDto, final ModuleDto updateObject);

  public abstract ModuleCreatorType getModuleCreatorType();

  public abstract String enrichServiceAddress(String serviceAddress);

  public void updateData(final ModuleDto moduleDto, final ModuleDto updateObject) {
    checkModels(moduleDto, updateObject);
    // Update basic values
    moduleDto.setError(updateObject.isError());
    moduleDto.setErrorPendingAcknowledge(updateObject.isErrorPendingAcknowledge());
    moduleDto.setUpdateTimestamp(LocalDateTime.now());
    updateDataSpec(moduleDto, updateObject);
  }

  private void checkModels(final ModuleDto moduleDto, final ModuleDto updateObject) {
    final String moduleDtoClassName = moduleDto.getClass().getName();
    final String updateObjectClassName = updateObject.getClass().getName();
    if (!moduleDtoClassName.equals(updateObjectClassName)) {
      throw new RuntimeException(
          String.format(
              "update object: %s, doesn't match module object: %s",
              updateObjectClassName, moduleDtoClassName));
    }

    if (!moduleDtoClassName
        .toUpperCase()
        .contains(formatModuleCreatorTypeName(getModuleCreatorType()))) {
      throw new RuntimeException(
          String.format(
              "update object: %s, doesn't match creator type: %s",
              updateObjectClassName, getModuleCreatorType()));
    }
  }

  private String formatModuleCreatorTypeName(final ModuleCreatorType moduleCreatorType) {
    return moduleCreatorType.toString().replace("_", "").toUpperCase();
  }
}
