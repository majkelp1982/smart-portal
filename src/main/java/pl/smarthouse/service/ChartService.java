package pl.smarthouse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.repository.ModuleRepository;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartService {

  private static final List<String> EXCLUSION_LIST = List.of("_id", "Timestamp", "Update", "class");
  private static final long CET_TIME_OFFSET_IN_SECONDS =
      ZonedDateTime.now().getOffset().getTotalSeconds();
  private static final int FIX_DATETIME_OFFSET_IN_HOURS = 2;
  private final GuiService guiService;
  private final ModuleRepository moduleRepository;

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

  private Coordinate getCoordinate(final JsonNode jsonNode, final String fieldPath) {

    final LocalDateTime time =
        LocalDateTime.from(
                OffsetDateTime.parse(jsonNode.get("saveTimestamp").get("$date").asText()))
            .plusHours(FIX_DATETIME_OFFSET_IN_HOURS)
            .plusSeconds(CET_TIME_OFFSET_IN_SECONDS);
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

  public Map<String, List<String>> getFieldsMap() {
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

  public HashMap<String, MultiSelectListBox> prepareMultiSelectListBox(
      final Map<String, List<String>> fieldsMap,
      final Consumer<Set<String>> selectionChangeListener) {
    final HashMap<String, MultiSelectListBox> result = new HashMap<>();
    fieldsMap.forEach(
        (moduleName, values) ->
            result.put(moduleName, getMultiSelectListBox(values, selectionChangeListener)));
    return result;
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
