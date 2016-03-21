/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lotusbidashboard;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 *
 * @author Jamie
 */
public class SalesService extends ScheduledService<ObservableList<Sales>> {
    
    public SalesService(){
        this.setPeriod(new Duration(30000));//initialise with 30s delay
    }

    @Override
    protected Task<ObservableList<Sales>> createTask() {
        return new SalesTask();
    }
    
    //sets ScheduledService delay from settings
    public void setNewDelay(int delay){
        super.setDelay(new Duration(delay * 1000));
    }
    
    //gets current delay to populate settings slider
    public int getDelayAsInt(){
        return (int)super.getDelay().toSeconds();
    }
}
