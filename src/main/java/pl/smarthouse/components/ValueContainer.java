package pl.smarthouse.components;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import pl.smarthouse.exceptions.ValueContainerException;
import pl.smarthouse.sharedobjects.enums.ZoneName;

public class ValueContainer {
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
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
          final var value = getFieldValue(path);
          if (Objects.nonNull(value)) {
            if (value instanceof LocalDateTime) {
              final String strValue =
                  ((LocalDateTime) value)
                      .format(TIME_FORMATTER)
                      .toString();
              component.setValue(strValue);
            } else
            if (value instanceof LocalTime) {
              final String strValue =
                      ((LocalTime) value)
                              .format(TIME_FORMATTER)
                              .toString();
              component.setValue(strValue);

            }
            else if (!(value instanceof Enum)
                && (path.contains("pressure") || path.contains("temp"))) {
              final double roundOff =
                  (double) Math.round(Double.parseDouble(value.toString()) * 100) / 100;
              component.setValue(String.valueOf(roundOff));
            } else {
              component.setValue(value);
            }
          }
        });
  }

  private Object getFieldValue(final String path) {
    final String[] subFields = path.split("\\.");
    Object value = moduleDto;
    for (int i = 0; i < subFields.length; i++) {
      try {
        final Field field;
        // If field in super class
        if (subFields[i].charAt(0) == '!') {
          field = value.getClass().getSuperclass().getDeclaredField(subFields[i].substring(1));
        }
        // if field is map
        else if (subFields[i].contains("[")) {
          final String key =
              subFields[i].substring(subFields[i].indexOf("[") + 1, subFields[i].indexOf("]"));
          final String mapName = subFields[i].substring(0, subFields[i].indexOf("["));
          final Field mapField = value.getClass().getDeclaredField(mapName);
          mapField.setAccessible(true);
          final HashMap hashMap = (HashMap) mapField.get(value);
          final ZoneName zoneName = ZoneName.valueOf(key);
          final Object object = hashMap.get(zoneName);
          i++;
          field = object.getClass().getDeclaredField(subFields[i]);
          field.setAccessible(true); // Allow access to private fields if necessary
          value = field.get(object);
          continue;
        } else {
          field = value.getClass().getDeclaredField(subFields[i]);
        }
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
