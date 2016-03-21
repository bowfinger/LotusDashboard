package lotusbidashboard;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 *
 * @author Jamie
 */
public class HoveredThresholdNode extends StackPane {
    HoveredThresholdNode(int value) {
      setPrefSize(15, 15);

      final Label label = createDataThresholdLabel(value);

      setOnMouseEntered((MouseEvent mouseEvent) -> {
          getChildren().setAll(label);
          setCursor(Cursor.HAND);
          toFront();
      });
      setOnMouseExited((MouseEvent mouseEvent) -> {
          getChildren().clear();
          //setCursor(Cursor.CROSSHAIR);
      });
    }

    private Label createDataThresholdLabel(int value) {
      final Label label = new Label(value + "");
      label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
      label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
      label.setTextFill(Color.FIREBRICK);

      label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
      return label;
    }
  }
