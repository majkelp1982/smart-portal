package pl.smarthouse.components;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

@Getter
public class Tile extends HorizontalLayout {
  VerticalLayout detailsContainer;

  public Tile(final String imageSrc, final NativeLabel titleLabel) {
    this.addClassName("tile");
    final Image image = new Image(imageSrc, imageSrc);
    image.setHeight("50px");
    titleLabel.getLayout().setHeight("50px");
    detailsContainer = new VerticalLayout();
    this.setAlignItems(Alignment.CENTER);
    this.add(titleLabel.getLayout(), image, detailsContainer);
  }
}
