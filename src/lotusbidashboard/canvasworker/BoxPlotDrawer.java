package lotusbidashboard.canvasworker;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lotusbidashboard.Stats;

/**
 *
 * @author Jamie Deville - 511202
 * @version 0.4b
 * 
 * Custom implementation of a Box Plot chart using the ResizableCanvas class
 * and the Stats object.
 * 
 * Dynamically builds a Box and Whisker Plot graph based on 
 * Min, Max, Lower Quartile, Median and Upper Quartile.
 * 
 * Also creates scale of major and minor tick marks,
 * set using mod10 of Min and Max values and sets a boundary
 * 10 multiplied by the current horizontal scaling ratio.
 */
public class BoxPlotDrawer {

    private static final double MAJOR_TICK_HEIGHT = 30;
    private static final double MINOR_TICK_HEIGHT = 10;
    private static final double OFFSET = 50;
    private static final double BOX_WHISKER_HEIGHT = 30;
    private static final double WHISKER_LINE_WIDTH = 3;
    private static final String FAILED_DATA_TEXT = "Not enough data points to generate Box Plot";
    private static final String TITLE = "Sales Summary Box Plot";
    
    

    public void drawBoxPlot(ResizableCanvas c, Stats s) {
        GraphicsContext gc = c.getGraphicsContext2D();
        Font defaultFont = gc.getFont();
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        double horizontalCentre = c.getWidth() / 2;
        double verticalCentre = c.getHeight() / 2;
        double hRatio = ((c.getWidth() / 2) / (s.getMax() - s.getMin()));
        double vRatio = c.getHeight() / 150;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        //FontMetrics object to calculate width of strings for positioning
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
        float computedStringWidth;
        
        //title
        gc.setFont(new Font(defaultFont.getName(), 10*hRatio));
        FontMetrics headerMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
        computedStringWidth = headerMetrics.computeStringWidth(TITLE);
        gc.fillText(TITLE, horizontalCentre - (computedStringWidth/2), OFFSET);
        gc.setFont(defaultFont);

        //enough data to draw graph
        if (s.getDataPoints() >= 4) {
            

            double graphCentre = (s.getMax() - s.getMin()) / 2;
            double offset = ((s.getQ2() - s.getMin()) - (graphCentre - s.getMin())) * hRatio;
            if (offset > 0) {
                offset = 0;
            }

            gc.setLineWidth(WHISKER_LINE_WIDTH);

            //ranges calculated from q2
            double startWhiskerRange = (s.getQ2() - s.getMin()) * hRatio;
            double lowerQuartileRange = (s.getQ2() - s.getQ1()) * hRatio;
            double endWhiskerRange = (s.getMax() - s.getQ2()) * hRatio;
            double upperQuartileRange = (s.getQ3() - s.getQ2()) * hRatio;

            //first whisker
            gc.strokeLine(
                    horizontalCentre + offset - startWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    horizontalCentre + offset - startWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre + (BOX_WHISKER_HEIGHT * vRatio)
            );
            //whisker join
            gc.strokeLine(
                    horizontalCentre + offset - startWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre,
                    horizontalCentre + offset - lowerQuartileRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre
            );

            //q1 to q2 box
            gc.setFill(Color.GREEN);
            gc.fillRect(
                    horizontalCentre + offset - lowerQuartileRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    lowerQuartileRange,
                    (BOX_WHISKER_HEIGHT * 2) * vRatio
            );
            gc.strokeRect(
                    horizontalCentre + offset - lowerQuartileRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    lowerQuartileRange,
                    (BOX_WHISKER_HEIGHT * 2) * vRatio
            );

            //q2 to q3 box
            gc.setFill(Color.YELLOW);
            gc.fillRect(
                    horizontalCentre + offset,
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    upperQuartileRange - (WHISKER_LINE_WIDTH / 2),//here
                    (BOX_WHISKER_HEIGHT * 2) * vRatio
            );
            gc.strokeRect(
                    horizontalCentre + offset,
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    upperQuartileRange - (WHISKER_LINE_WIDTH / 2), //here
                    (BOX_WHISKER_HEIGHT * 2) * vRatio
            );

            //whisker join
            gc.strokeLine(
                    horizontalCentre + offset + upperQuartileRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre,
                    horizontalCentre + offset + endWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre
            );
            //second whisker
            gc.strokeLine(
                    horizontalCentre + offset + endWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre - (BOX_WHISKER_HEIGHT * vRatio),
                    horizontalCentre + offset + endWhiskerRange - (WHISKER_LINE_WIDTH / 2),
                    verticalCentre + (BOX_WHISKER_HEIGHT * vRatio)
            );
            
            //reset fill for text
            gc.setFill(Color.BLACK);

            //build x-axis
            //first determine lower and upper bounds of chart
            int origin = 0;
            if (s.getMin() - 10 > 0) {
                if (s.getMin() % 10 == 0) {
                    origin = (int) s.getMin() - 10;
                } else {
                    origin = (int) (s.getMin() - (s.getMin() % 10));
                }
            }
            int endpoint = (int) (Math.rint((double) s.getMax() / 10) * 10);
            if (endpoint < s.getMax()) {
                endpoint += 10;
            }

            //get positions of tick marks
            double originX = horizontalCentre + offset - startWhiskerRange - (WHISKER_LINE_WIDTH / 2) - ((s.getMin() - origin) * hRatio);
            double endpointX = horizontalCentre + offset + endWhiskerRange - (WHISKER_LINE_WIDTH / 2) + ((endpoint - s.getMax()) * hRatio);

            //reset stroke width
            gc.setLineWidth(1);

            //fill major ticks
            for (int i = origin; i <= endpoint; i += 5) {
                if (i % 10 == 0) {
                    //major tick
                    gc.strokeLine(
                            originX + ((i - origin) * hRatio),
                            c.getHeight() - OFFSET - MAJOR_TICK_HEIGHT,
                            originX + ((i - origin) * hRatio),
                            c.getHeight() - OFFSET
                    );
                    //add number below
                    computedStringWidth = metrics.computeStringWidth(Integer.toString(i));
                    gc.fillText(Integer.toString(i), originX + ((i - origin) * hRatio) - (computedStringWidth / 2), c.getHeight() - (OFFSET / 2));
                } else {
                    //minor tick
                    gc.strokeLine(
                            originX + ((i - origin) * hRatio),
                            c.getHeight() - OFFSET - MINOR_TICK_HEIGHT,
                            originX + ((i - origin) * hRatio),
                            c.getHeight() - OFFSET
                    );
                }
            }

            //draw joining line
            gc.strokeLine(
                    originX,
                    c.getHeight() - OFFSET,
                    endpointX,
                    c.getHeight() - OFFSET
            );
        } else {
            //write "not enough data points" message
            computedStringWidth = metrics.computeStringWidth(FAILED_DATA_TEXT);
            gc.fillText(
                    FAILED_DATA_TEXT,
                    horizontalCentre - (computedStringWidth / 2),
                    verticalCentre
            );
        }
    }

}
