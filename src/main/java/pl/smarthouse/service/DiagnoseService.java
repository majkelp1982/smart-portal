package pl.smarthouse.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;
import pl.smarthouse.model.diagnostic.ModuleDetails;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiagnoseService {
  public static final String WAITING_FOR_MODULE_RESPONSE = "WAITING FOR MODULE RESPONSE";
  private final ModuleService moduleService;
  private final WebService webService;
  private final ErrorHandlingService errorHandlingService;
  private final ConcurrentLinkedQueue<ErrorPredictionDiagnostic> errors =
      new ConcurrentLinkedQueue<>();

  public Flux<ModuleDetails> getModulesDetails() {
    return Flux.fromIterable(moduleService.getModuleDtos()).map(this::toModuleDetails);
  }

  public int getModuleCount() {
    return moduleService.getModuleDtos().size();
  }

  private ModuleDetails toModuleDetails(final ModuleDto moduleDto) {
    final ModuleDetails moduleDetails = new ModuleDetails();
    moduleDetails.setType(moduleDto.getType());
    moduleDetails.setMacAddress(moduleDto.getModuleMacAddress());
    moduleDetails.setServiceAddress(moduleDto.getServiceAddress());
    moduleDetails.setVersion(moduleDto.getServiceVersion());
    moduleDetails.setServiceUpdateTimestamp(moduleDto.getServiceUpdateTimestamp());
    moduleDetails.setModuleIpAddress(moduleDto.getModuleIpAddress());
    moduleDetails.setFirmware(moduleDto.getModuleFirmwareVersion());
    moduleDetails.setModuleUpdateTimestamp(moduleDto.getModuleUpdateTimestamp());
    moduleDetails.setUptimeInMinutes(moduleDto.getUptimeInMinutes());
    return moduleDetails;
  }

  public void restartModule(final String moduleIpAddress) {
    webService.get(moduleIpAddress + "/restart", String.class).subscribe();
  }

  public Flux<List<ErrorPredictionDiagnostic>> updateModulesErrors() {
    return Flux.fromStream(moduleService.getModuleDtos().stream())
        .parallel()
        .flatMap(
            moduleDto ->
                webService
                    .get(
                        getBaseServiceAddress(moduleDto.getServiceAddress())
                            + "/errorhandler/pending",
                        HashMap.class)
                    .flatMap(
                        pendingErrors ->
                            webService
                                .get(
                                    getBaseServiceAddress(moduleDto.getServiceAddress())
                                        + "/errorhandler/active",
                                    HashMap.class)
                                .flatMap(
                                    activeErrors -> {
                                      pendingErrors.putAll(activeErrors);
                                      return Mono.just(pendingErrors);
                                    }))
                    .onErrorResume(
                        throwable -> {
                          log.error(
                              "Error occurred when getting error list from {}, error: {}",
                              moduleDto.getType(),
                              throwable.getMessage());
                          errorHandlingService.createConnectionIssueError(
                              moduleDto.getType(), throwable);
                          return Mono.empty();
                        })
                    .map(errors -> toErrorList(moduleDto.getType(), errors))
                    .doOnNext(
                        list ->
                            log.info(
                                "Updating errors. Module: {}, list size: {}, total: {}",
                                moduleDto.getType(),
                                list.size(),
                                errors.size()))
                    .map(
                        errorPredictionsDiagnostic ->
                            updateErrors(moduleDto.getType(), errorPredictionsDiagnostic)))
        .sequential();
  }

  public synchronized List<ErrorPredictionDiagnostic> updateErrors(
      final String moduleName, final List<ErrorPredictionDiagnostic> updateErrors) {
    errorHandlingService.finishConnectionIssueError(moduleName);
    errors.removeIf(errors -> moduleName.equals(errors.getType()));
    errors.addAll(updateErrors);
    return updateErrors;
  }

  public void acknowledgeAllPending() {
    Flux.fromStream(moduleService.getModuleDtos().stream())
        .doOnNext(
            moduleDto ->
                log.info(
                    "Sending acknowledge all pending errors to module: {}", moduleDto.getType()))
        .flatMap(
            moduleDto ->
                webService.patch(
                    getBaseServiceAddress(moduleDto.getServiceAddress())
                        + "/errorhandler/pending/clear",
                    String.class,
                    ""))
        .subscribe();
  }

  private String getBaseServiceAddress(final String serviceAddress) {
    final int indexOfPortSeparator = serviceAddress.indexOf(":");
    if (indexOfPortSeparator != -1) {
      final int endOfPort = serviceAddress.indexOf("/", indexOfPortSeparator);
      if (endOfPort != -1) {
        return serviceAddress.substring(0, endOfPort);
      }
    }
    return serviceAddress;
  }

  private List<ErrorPredictionDiagnostic> toErrorList(
      final String moduleName, final Object objectHashMap) {
    final HashMap<String, ErrorPredictionDiagnostic> errorPredictions =
        (HashMap<String, ErrorPredictionDiagnostic>) objectHashMap;
    final List<ErrorPredictionDiagnostic> mappedErrors = new ArrayList<>();
    errorPredictions
        .keySet()
        .forEach(
            key ->
                mappedErrors.add(
                    toErrorPredictionDiagnostic(moduleName, key, errorPredictions.get(key))));
    return mappedErrors;
  }

  private ErrorPredictionDiagnostic toErrorPredictionDiagnostic(
      final String moduleName, final String hashcode, final Object errorPredictionObject) {
    final ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    final ErrorPredictionDiagnostic errorPredictionDiagnostic =
        objectMapper.convertValue(errorPredictionObject, ErrorPredictionDiagnostic.class);
    errorPredictionDiagnostic.setType(moduleName);
    errorPredictionDiagnostic.setHashCode(hashcode);
    return errorPredictionDiagnostic;
  }

  public ConcurrentLinkedQueue<ErrorPredictionDiagnostic> getErrors(final boolean isGroupEnabled) {
    if (isGroupEnabled) {
      final Map<String, List<ErrorPredictionDiagnostic>> errorsByType =
          errors.stream().collect(Collectors.groupingBy(ErrorPredictionDiagnostic::getType));
      final ConcurrentLinkedQueue<ErrorPredictionDiagnostic> resultList =
          new ConcurrentLinkedQueue<>();
      errorsByType.forEach(
          (type, moduleErrors) -> resultList.addAll(groupErrorsByMessage(moduleErrors)));
      return resultList;
    }
    return errors;
  }

  private ConcurrentLinkedQueue<ErrorPredictionDiagnostic> groupErrorsByMessage(
      final List<ErrorPredictionDiagnostic> moduleErrors) {
    final Map<String, List<ErrorPredictionDiagnostic>> errorsByMessage =
        moduleErrors.stream().collect(Collectors.groupingBy(ErrorPredictionDiagnostic::getMessage));

    final ConcurrentLinkedQueue<ErrorPredictionDiagnostic> resultList =
        new ConcurrentLinkedQueue<>();
    errorsByMessage.forEach((message, errors) -> resultList.add(aggregateErrorMessages(errors)));
    return resultList;
  }

  private ErrorPredictionDiagnostic aggregateErrorMessages(
      final List<ErrorPredictionDiagnostic> errorMessage) {
    final List<ErrorPredictionDiagnostic> sortedList =
        errorMessage.stream()
            .sorted(Comparator.comparing(ErrorPredictionDiagnostic::getBeginTimestamp))
            .toList();
    final ErrorPredictionDiagnostic firstErrorPredictionDiagnostic = sortedList.get(0);
    final AtomicLong totalTime = new AtomicLong();
    final AtomicInteger errorCount = new AtomicInteger();
    sortedList.forEach(
        errorPredictionDiagnostic -> {
          errorCount.getAndIncrement();
          totalTime.addAndGet(errorPredictionDiagnostic.getDuration());
        });

    final ErrorPredictionDiagnostic resultAggregatedError = new ErrorPredictionDiagnostic();
    resultAggregatedError.setType(firstErrorPredictionDiagnostic.getType());
    resultAggregatedError.setMessage(firstErrorPredictionDiagnostic.getMessage());
    resultAggregatedError.setPriority(firstErrorPredictionDiagnostic.getPriority());
    resultAggregatedError.setBeginTimestamp(firstErrorPredictionDiagnostic.getBeginTimestamp());
    LocalDateTime lastEndTimestamp = sortedList.get(sortedList.size() - 1).getEndTimestamp();
    if (lastEndTimestamp == null) {
      lastEndTimestamp = LocalDateTime.now();
    }
    resultAggregatedError.setEndTimestamp(lastEndTimestamp);
    resultAggregatedError.setErrorCount(errorCount.get());
    resultAggregatedError.setTotalTimeInMinutes(totalTime.get() / 60);
    return resultAggregatedError;
  }

  public void initModuleErrors() {
    moduleService.getModuleDtos().stream()
        .map(moduleDto -> createInitModuleError(moduleDto.getType()))
        .forEach(
            errorPredictionDiagnostic ->
                updateErrors(
                    errorPredictionDiagnostic.getType(), List.of(errorPredictionDiagnostic)));
  }

  private ErrorPredictionDiagnostic createInitModuleError(final String moduleName) {
    final ErrorPredictionDiagnostic errorPredictionDiagnostic = new ErrorPredictionDiagnostic();
    errorPredictionDiagnostic.setType(moduleName);
    errorPredictionDiagnostic.setMessage(WAITING_FOR_MODULE_RESPONSE);
    errorPredictionDiagnostic.setBeginTimestamp(LocalDateTime.now());
    return errorPredictionDiagnostic;
  }
}
