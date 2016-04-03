package lotusbidashboard.canvasworker;

import javafx.scene.canvas.Canvas;

/**
 *
 * @author Jamie Deville - 511202
 * 
 * extending code found at: 
 * https://dlemmermann.wordpress.com/2014/04/10/javafx-tip-1-resizable-canvas/
 * 
 * Now implements a callback (CanvasRedraw) to enable event 
 * firing method to be accessed in any class that instantiates this.
 */
public class ResizableCanvas extends Canvas {

    //callback interface
    private CanvasRedraw redraw;

    public ResizableCanvas() {
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> redraw.fireEvent());
        heightProperty().addListener(evt -> redraw.fireEvent());
    }

    public void setRedraw(CanvasRedraw redraw) {
        this.redraw = redraw;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
