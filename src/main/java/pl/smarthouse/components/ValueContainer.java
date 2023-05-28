package pl.smarthouse.components;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import pl.smarthouse.exceptions.ValueContainerException;

public class ValueContainer {
  Object moduleDto;
  HashMap<String, PortalComponent> valueContainer = new HashMap<>();

  public ValueContainer(final Object moduleDto) {
    this.moduleDto = moduleDto;
  }

  public void put(final String path, final PortalComponent component) {
    getFieldValue(path);
    valueContainer.put(path, component);
  }

  public void updateValues() {
    valueContainer.forEach(
        (path, component) -> {
          component.setValue((Number) getFieldValue(path));
        });
  }

  private Object getFieldValue(final String path) {
    final String[] subFields = path.split("\\.");
    Object value = moduleDto;
    for (int i = 0; i < subFields.length; i++) {
      try {
        final Field field = value.getClass().getDeclaredField(subFields[i]);
        field.setAccessible(true); // Allow access to private fields if necessary
        value = field.get(value);
      } catch (final NoSuchFieldException | IllegalAccessException e) {
        final List<String> availableFields =
            Arrays.stream(value.getClass().getDeclaredFields())
                .map(field -> field.getName())
                .collect(Collectors.toList());
        throw new ValueContainerException(
            String.format(
                "Path is: %s, available fields: %s, error message: %s",
                path, availableFields, e.getMessage()),
            e);
      }
    }
    return value;
  }
}
