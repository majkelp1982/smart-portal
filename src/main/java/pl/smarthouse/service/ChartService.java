package pl.smarthouse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.repository.ModuleRepository;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class ChartService {

  private static final List<String> EXCLUSION_LIST = List.of("_id", "Timestamp", "Update", "class");
  private static final long CET_TIME_OFFSET_IN_SECONDS =
      ZonedDateTime.now().getOffset().getTotalSeconds();
  private static final int FIX_DATETIME_OFFSET_IN_HOURS = 2;
  final Map<String, Set<String>> selectedItemsMap = new ConcurrentHashMap<>();
  private final GuiService guiService;
  private final ModuleRepository moduleRepository;
  private final Map<String, MultiSelectListBox> multiSelectListsMap = new ConcurrentHashMap<>();

  public Flux<Coordinate> getCoordinates(
      final String tableName,
      final String fieldPath,
      final LocalDateTime fromTimestamp,
      final LocalDateTime toTimestamp) {
    return moduleRepository
        .getValues(tableName, fieldPath, fromTimestamp, toTimestamp)
        .map(this::getJsonObjectFromString)
        .map(jsonNode -> getCoordinate(jsonNode, fieldPath));
  }

  public Map<String, MultiSelectListBox> getMultiSelectListsMap() {
    if (multiSelectListsMap.isEmpty()) {
      throw new RuntimeException("The list is empty. First need do be prepared by ChartService");
    }
    final Map<String, Set<String>> selectedItemsMapCopy = new HashMap<>();
    getSelectedItems(false)
        .forEach((moduleName, itemSet) -> selectedItemsMapCopy.put(moduleName, itemSet));

    selectedItemsMapCopy.forEach(
        (moduleName, itemSet) -> {
          log.info(
              "Module name: {}, selected items found: {}",
              moduleName,
              selectedItemsMapCopy.get(moduleName));
          multiSelectListsMap.get(moduleName).select(itemSet);
        });
    return multiSelectListsMap;
  }

  public void deselectAllItems() {
    multiSelectListsMap.values().stream()
        .forEach(multiSelectListBox -> multiSelectListBox.deselectAll());
  }

  public Map<String, Set<String>> getSelectedItems(final boolean forceRefresh) {
    if (forceRefresh) {
      selectedItemsMap.clear();
      multiSelectListsMap.keySet().stream()
          .filter(moduleName -> !multiSelectListsMap.get(moduleName).getSelectedItems().isEmpty())
          .forEach(
              moduleName ->
                  selectedItemsMap.put(
                      moduleName, multiSelectListsMap.get(moduleName).getSelectedItems()));
    }
    return selectedItemsMap;
  }

  private Coordinate getCoordinate(final JsonNode jsonNode, final String fieldPath) {

    final LocalDateTime time =
        LocalDateTime.from(
                OffsetDateTime.parse(jsonNode.get("saveTimestamp").get("$date").asText()))
            .plusSeconds(CET_TIME_OFFSET_IN_SECONDS * 2);
    final String timeString = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    return new Coordinate(timeString, getValue(fieldPath, jsonNode));
  }

  private Object getValue(final String fieldPath, final JsonNode jsonNode) {
    JsonNode nestedNode = jsonNode;
    for (final String splitPath : fieldPath.split("\\.")) {
      nestedNode = nestedNode.get(splitPath);
    }
    return nestedNode;
  }

  public List<Coordinate> handleNotANumberValues(final List<Coordinate> coordinates) {
    if (coordinates.size() == 0) {
      return new ArrayList<>();
    }
    if (!(coordinates.get(0).getY()[0] instanceof IntNode)
        && !(coordinates.get(0).getY()[0] instanceof DoubleNode)) {
      final List<Coordinate> results = new ArrayList<>();
      final Coordinate lastCoordinate = coordinates.get(0);
      results.add(new Coordinate(lastCoordinate.getX(), recalculate((lastCoordinate.getY()[0]))));
      coordinates.forEach(
          coordinate -> {
            if (coordinate.getY()[0] != lastCoordinate.getY()[0]) {
              results.add(
                  new Coordinate(
                      LocalDateTime.parse(coordinate.getX().toString())
                          .minusNanos(1)
                          .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                      recalculate((lastCoordinate.getY()[0]))));
            }
            lastCoordinate.setX(coordinate.getX());
            lastCoordinate.setY(coordinate.getY());
            results.add(new Coordinate(coordinate.getX(), recalculate(coordinate.getY()[0])));
          });
      return results;
    }

    // if not NaN y-values not change and return input
    return coordinates;
  }

  private IntNode recalculate(final Object node) {
    if (node instanceof BooleanNode) {
      return recalculateBoolean((BooleanNode) node);
    }

    if (node instanceof TextNode) {
      return recalculateText((TextNode) node);
    }

    throw new IllegalArgumentException(
        String.format("recalculate of node: %s, not implemented", node.getClass()));
  }

  private IntNode recalculateText(final TextNode textNode) {
    final String value = textNode.asText();
    value.replace("\"", "");
    switch (value) {
      case "OFF" -> {
        return IntNode.valueOf(0);
      }

      case "ON", "STANDBY" -> {
        return IntNode.valueOf(1);
      }
      case "HUMIDITY_ALERT" -> {
        return IntNode.valueOf(2);
      }
      case "AIR_EXCHANGE" -> {
        return IntNode.valueOf(3);
      }
      case "AIR_HEATING" -> {
        return IntNode.valueOf(4);
      }
      case "AIR_COOLING" -> {
        return IntNode.valueOf(5);
      }
      case "AIR_CONDITION" -> {
        return IntNode.valueOf(6);
      }
      case "FLOOR_HEATING" -> {
        return IntNode.valueOf(7);
      }
    }

    throw new IllegalArgumentException(
        String.format("Recalculation text: %s, not implemented", value));
  }

  private IntNode recalculateBoolean(final BooleanNode booleanNode) {
    // true -> 10
    if (booleanNode.asBoolean()) {
      return IntNode.valueOf(1);
    } else {
      // false -> 0
      return IntNode.valueOf(0);
    }
  }

  public Map<String, List<String>> getFieldsMapFromModules() {
    final Map<String, List<String>> map = new HashMap<>();
    log.info("Get fields maps started...");
    guiService.getModuleDtos().stream()
        .map(moduleDto -> moduleDto.getModuleName().toLowerCase())
        .parallel()
        .forEach(
            moduleName -> {
              log.info("Query module name: {}", moduleName);
              moduleRepository
                  .getLastModuleData(moduleName, String.class)
                  .map(this::getJsonObjectFromString)
                  .flatMapMany(jsonNode -> Flux.fromIterable(getJsonFields(jsonNode, "")))
                  .filter(
                      field ->
                          EXCLUSION_LIST.stream()
                              .noneMatch(excludedField -> field.contains(excludedField)))
                  .collectList()
                  .doOnNext(list -> map.put(moduleName, list))
                  .block();
              log.info("Finished collecting list for module name: {}", moduleName);
            });
    log.info("Get fields maps completed...");
    return map;
  }

  private List<String> getJsonFields(final JsonNode jsonNode, final String prefix) {
    final List<String> fields = new ArrayList<>();
    jsonNode
        .fields()
        .forEachRemaining(
            entry -> {
              final String key = entry.getKey();
              final JsonNode value = entry.getValue();
              if (value.isObject()) {
                fields.addAll(getJsonFields(value, prefix + key + "."));
              } else {
                fields.add(prefix + key);
              }
            });
    return fields;
  }

  private JsonNode getJsonObjectFromString(final String jsonString) {
    final ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(jsonString);
    } catch (final JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void prepareMultiSelectListBox(
      final Map<String, List<String>> fieldsMap,
      final Consumer<Set<String>> selectionChangeListener) {
    fieldsMap.forEach(
        (moduleName, values) ->
            multiSelectListsMap.put(
                moduleName, getMultiSelectListBox(values, selectionChangeListener)));
  }

  private MultiSelectListBox<String> getMultiSelectListBox(
      final List<String> values, final Consumer<Set<String>> selectionChangeListener) {
    final MultiSelectListBox<String> multiSelectListBox = new MultiSelectListBox<>();
    multiSelectListBox.setItems(values);
    multiSelectListBox.addSelectionListener(
        event -> selectionChangeListener.accept(event.getAllSelectedItems()));
    return multiSelectListBox;
  }
}
