package pl.smarthouse.model;

import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ColorPredicate {
  Predicate predicate;
  ComponentColor color;
}
