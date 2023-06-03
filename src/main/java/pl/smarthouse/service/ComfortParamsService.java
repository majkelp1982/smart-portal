package pl.smarthouse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;

@Service
@RequiredArgsConstructor
public class ComfortParamsService {
  private final WebService webService;

  public ComfortModuleParamsDto getParams(final String serviceAddress) {
    return webService
        .get(getServiceBaseAddress(serviceAddress) + "/params", ComfortModuleParamsDto.class)
        .blockFirst();
  }

  public ComfortModuleParamsDto saveParams(
      final String serviceAddress, final ComfortModuleParamsDto comfortModuleParamsDto)
      throws JsonProcessingException {
    return webService
        .post(
            getServiceBaseAddress(serviceAddress) + "/params",
            ComfortModuleParamsDto.class,
            comfortModuleParamsDto)
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
