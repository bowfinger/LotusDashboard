package lotusbidashboard.styling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Jamie Deville
 * 
 * Supplies URI location of CSS file
 * within application directory structure
 */
public class StyleSupplier {
    
    private static final Map<String, String> STYLES = new HashMap<>();
    
    //add project directory URL for styles here
    
    //public static final String CUSTOM = "styling/custom.css";
    public static final String HIGH_CONTRAST = "styling/HighContrast.css";
    
    static{
        //populate hashmap for lists
        //all styles are loaded on instance of this class
        STYLES.put("High Contrast", HIGH_CONTRAST);
        //STYLES.put("Custom", CUSTOM);
    }
    
    public static List<String> getStyles(){
        return STYLES.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }
    
    public static String getStyle(String key){
        return STYLES.get(key);
    }
}
