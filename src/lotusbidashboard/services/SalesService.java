package lotusbidashboard.services;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import lotusbidashboard.Sales;

/**
 *
 * @author Jamie Deville
 */
public class SalesService extends ScheduledService<ObservableList<Sales>> {
    private static SalesService instance = null;
    
    //exists to stop public instantiation
    protected SalesService(){
        //initialise with 30s delay between calls
        this.setPeriod(new Duration(30000));
        //task has additional checks so we want to fail instantly if it throws an error
        this.setMaximumFailureCount(1);
    }
    
    //singleton pattern
    //allows all controllers to interface with same service
    public static SalesService getInstance() {
      if(instance == null) {
         instance = new SalesService();
      }
      return instance;
   }

    @Override
    protected Task<ObservableList<Sales>> createTask() {
        return new SalesTask();
    }
    
    public void setNewDelay(double delay){
        setNewDelay((int)delay);
    }
    
    //sets ScheduledService delay from settings
    public void setNewDelay(int delay){
        super.setPeriod(new Duration(delay * 1000));
    }
    
    //gets current delay to populate settings slider
    public int getDelayAsInt(){
        return (int)super.getPeriod().toSeconds();
    }
}
