package pl.smarthouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParamsService {
  private final WebService webService;

  public <T> T getParams(final String serviceAddress, final Class<T> paramClass) {
    return webService
        .get(getServiceBaseAddress(serviceAddress) + "/params", paramClass)
        .blockFirst();
  }

  public <T> T saveParams(final String serviceAddress, final Class<T> paramClass, final T data) {
    return webService
        .post(getServiceBaseAddress(serviceAddress) + "/params", paramClass, data)
        .blockFirst();
  }

  private String getServiceBaseAddress(final String serviceAddress) {
    final int endpointDelimiterIndex = serviceAddress.indexOf('/');
    if (endpointDelimiterIndex != -1) {
      return serviceAddress.substring(0, endpointDelimiterIndex);
    } else {
      return serviceAddress;
    }
  }
}
