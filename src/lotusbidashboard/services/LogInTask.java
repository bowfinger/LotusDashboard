package lotusbidashboard.services;

import javafx.concurrent.Task;

/**
 *
 * @author Jamie Deville
 */
public class LogInTask extends Task<Boolean> {
    
    private static final String USER = "Lotus";
    private static final String PASS = "Lotus";
    
    private String username;
    private String password;
    
    public LogInTask(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected Boolean call() throws Exception {
        Thread.sleep(3000);
        if(username.equals(USER) && password.equals(PASS)){
            return true;
        }else{
            return false;
        }
    }
    
}
