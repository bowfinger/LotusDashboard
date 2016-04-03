package lotusbidashboard.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Jamie Deville
 */
public class LogInService extends Service{
    
    private String username = "";
    private String password = "";

    @Override
    protected Task createTask() {
        return new LogInTask(username, password);
    }
    
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    
}
