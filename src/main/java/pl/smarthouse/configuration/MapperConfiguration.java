package pl.smarthouse.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

public class MapperConfiguration {
  @Bean
  ModelMapper modelMapper() {
    return new ModelMapper();
  }
}
