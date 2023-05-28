package pl.smarthouse.components;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ValueContainer {
  Object moduleDto;
  HashMap<String, PortalComponent> valueContainer = new HashMap<>();

  public ValueContainer(final Object moduleDto) {
    this.moduleDto = moduleDto;
  }

  public void put(final String path, final PortalComponent component) {
    valueContainer.put(path, component);
  }

  public void updateValues() {
    valueContainer.forEach(
        (path, component) -> {
          final String[] subFields = path.split("\\.");
          Object value = moduleDto;
          for (int i = 0; i < subFields.length; i++) {
            try {
              final Field field = value.getClass().getDeclaredField(subFields[i]);
              field.setAccessible(true); // Allow access to private fields if necessary
              value = field.get(value);
            } catch (final NoSuchFieldException | IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }

          component.setValue((Number) value);
        });
  }
}
