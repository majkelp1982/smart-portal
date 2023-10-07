package pl.smarthouse.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiagnoseService {
  private final GuiService guiService;
  private final WebService webService;
  private final List<ErrorPredictionDiagnostic> errors = new ArrayList<>();

  public Flux<List<ErrorPredictionDiagnostic>> updateModulesErrors() {
    return Flux.fromStream(guiService.getModuleDtos().stream())
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
                    .map(errors -> toErrorPredictionDiagnostics(moduleDto.getModuleName(), errors))
                    .map(
                        ErrorPredictionsDiagnostic ->
                            updateErrors(moduleDto.getModuleName(), ErrorPredictionsDiagnostic)));
  }

  private synchronized List<ErrorPredictionDiagnostic> updateErrors(
      final String moduleName, final Object object) {
    final HashMap<String, ErrorPredictionDiagnostic> updateErrors =
        (HashMap<String, ErrorPredictionDiagnostic>) object;
    errors.removeIf(errors -> moduleName.equals(errors.getModuleName()));
    errors.addAll(updateErrors.values());
    return errors;
  }

  public void acknowledgeAllPending() {
    Flux.fromStream(guiService.getModuleDtos().stream())
        .doOnNext(
            moduleDto ->
                log.info(
                    "Sending acknowledge all pending errors to module: {}",
                    moduleDto.getModuleName()))
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

  private HashMap<String, ErrorPredictionDiagnostic> toErrorPredictionDiagnostics(
      final String moduleName, final HashMap<String, Object> errorPredictions) {
    final HashMap<String, ErrorPredictionDiagnostic> errorPredictionDiagnostics = new HashMap<>();

    errorPredictions.forEach(
        (hashcode, errorPrediction) ->
            errorPredictionDiagnostics.put(
                hashcode, toErrorPredictionDiagnostic(moduleName, errorPrediction)));
    return errorPredictionDiagnostics;
  }

  private ErrorPredictionDiagnostic toErrorPredictionDiagnostic(
      final String moduleName, final Object errorPredictionObject) {
    final ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    final ErrorPredictionDiagnostic errorPredictionDiagnostic =
        objectMapper.convertValue(errorPredictionObject, ErrorPredictionDiagnostic.class);
    errorPredictionDiagnostic.setModuleName(moduleName);
    return errorPredictionDiagnostic;
  }

  public List<ErrorPredictionDiagnostic> getErrors() {
    return errors;
  }
}
