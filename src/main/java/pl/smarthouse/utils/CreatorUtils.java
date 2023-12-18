package pl.smarthouse.utils;

import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.Ds18b20ResultDto;

@UtilityClass
public class CreatorUtils {
  public void updateBme280(
      final Bme280ResponseDto bme280ResponseDto, final Bme280ResponseDto updateBme280) {
    bme280ResponseDto.setTemperature(updateBme280.getTemperature());
    bme280ResponseDto.setPressure(updateBme280.getPressure());
    bme280ResponseDto.setHumidity(updateBme280.getHumidity());
    bme280ResponseDto.setError(updateBme280.isError());
    bme280ResponseDto.setResponseUpdate(updateBme280.getResponseUpdate());
  }

  public void updateDs18b20(final Ds18b20ResultDto sensor, final Ds18b20ResultDto updateSensor) {
    sensor.setAddress(updateSensor.getAddress());
    sensor.setTemp(updateSensor.getTemp());
    sensor.setError(updateSensor.isError());
    sensor.setLastUpdate(updateSensor.getLastUpdate());
  }
}
