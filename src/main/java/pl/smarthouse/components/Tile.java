package pl.smarthouse.components;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Tile extends HorizontalLayout {
  VerticalLayout detailsContainer;

  public Tile(final String imageSrc, final String title) {
    final Image image = new Image(imageSrc, imageSrc);
    image.setHeight("50px");
    final Label titleLabel = new Label(title);
    titleLabel.setHeight("50px");
    detailsContainer = new VerticalLayout();
    this.setAlignItems(Alignment.CENTER);
    this.add(titleLabel, image, detailsContainer);
  }

  public VerticalLayout getDetailsContainer() {
    return detailsContainer;
  }
}
