package pl.smarthouse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;

@Service
@RequiredArgsConstructor
public class ErrorHandlingService {
  private static final String CONNECTION_ISSUE_ID = "-1";
  private static final String PORTAL_MODULE = "SMART-PORTAL";
  private final List<ErrorPredictionDiagnostic> errorPredictions = new ArrayList<>();
  private final Predicate<ErrorPredictionDiagnostic> isErrorNotFinished =
      errorPredictionDiagnostic -> errorPredictionDiagnostic.getEndTimestamp() == null;

  public List<ErrorPredictionDiagnostic> addPortalErrors(
      final List<ErrorPredictionDiagnostic> list) {
    list.removeIf(
        errorPredictionDiagnostic ->
            errorPredictionDiagnostic.getModuleName().equals(PORTAL_MODULE));
    list.addAll(errorPredictions);
    return list;
  }

  public void createConnectionIssueError(final String moduleName, final Throwable throwable) {
    if (errorPredictions.stream()
        .anyMatch(
            errorPredictionDiagnostic ->
                errorMessageContainsModuleName(moduleName)
                    .and(isErrorCode(CONNECTION_ISSUE_ID))
                    .and(isErrorNotFinished)
                    .test(errorPredictionDiagnostic))) {
      return;
    }

    final ErrorPredictionDiagnostic moduleConnectionError = new ErrorPredictionDiagnostic();
    moduleConnectionError.setModuleName(PORTAL_MODULE);
    moduleConnectionError.setMessage(
        String.format("Module: %s, connection error: %s", moduleName, throwable.getMessage()));
    moduleConnectionError.setBeginTimestamp(LocalDateTime.now());
    moduleConnectionError.setHashCode(CONNECTION_ISSUE_ID);
    errorPredictions.add(moduleConnectionError);
  }

  public void finishConnectionIssueError(final String moduleName) {
    errorPredictions.stream()
        .filter(
            errorPredictionDiagnostic ->
                errorMessageContainsModuleName(moduleName)
                    .and(isErrorNotFinished)
                    .test(errorPredictionDiagnostic))
        .findFirst()
        .ifPresent(
            errorPredictionDiagnostic ->
                errorPredictionDiagnostic.setEndTimestamp(LocalDateTime.now()));
  }

  private Predicate<ErrorPredictionDiagnostic> errorMessageContainsModuleName(
      final String moduleName) {
    return errorPredictionDiagnostic -> errorPredictionDiagnostic.getMessage().contains(moduleName);
  }

  private Predicate<ErrorPredictionDiagnostic> isErrorCode(final String errorCode) {
    return errorPredictionDiagnostic -> errorPredictionDiagnostic.getHashCode().equals(errorCode);
  }
}
