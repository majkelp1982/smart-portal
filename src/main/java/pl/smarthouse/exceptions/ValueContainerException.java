package pl.smarthouse.exceptions;

public class ValueContainerException extends RuntimeException {
  public ValueContainerException(final String message, final Exception exception) {
    super(message, exception);
  }
}
