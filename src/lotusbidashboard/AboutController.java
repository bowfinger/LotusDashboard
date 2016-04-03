package lotusbidashboard;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;

/**
 *
 * @author Jamie Deville
 */
public class AboutController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //nothing to initialise
    }
    
    @FXML
    public void openEmailClient(ActionEvent event) throws IOException, URISyntaxException {
        //construct uri string
        //uses accessible text as actual email address
        String forUri = String.format("mailto:%s?subject=%s", ((Hyperlink)event.getSource()).getAccessibleText(), "Lotus%20BI%20Dashboard");
        Desktop.getDesktop().mail(new URI(forUri));
    }
    
}
