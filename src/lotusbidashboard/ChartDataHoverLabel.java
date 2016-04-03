package lotusbidashboard;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 *
 * @author Jamie Deville - 511202
 * 
 * Creates a hover label for chart nodes using an altered version of code
 * found at: https://gist.github.com/jewelsea/4681797
 */
public class ChartDataHoverLabel extends StackPane {
    public ChartDataHoverLabel(int value) {
      super.setPrefSize(15, 15);

      final Label label = createHoverLabel(value);

      setOnMouseEntered((MouseEvent mouseEvent) -> {
          getChildren().setAll(label);
          setCursor(Cursor.HAND);
          toFront();
      });
      setOnMouseExited((MouseEvent mouseEvent) -> {
          getChildren().clear();
      });
    }

    private Label createHoverLabel(int value) {
      final Label label = new Label(value + "");
      label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
      label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
      label.setTextFill(Color.FIREBRICK);

      label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
      return label;
    }
  }
