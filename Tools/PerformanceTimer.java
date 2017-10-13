package Tools;

import java.util.HashMap;

/**
 * Created by Rafael on 8/1/2017.
 */
public class PerformanceTimer {
    HashMap<String,Long> times;
    public void Start(String label){
        times.put(label,System.currentTimeMillis());
    }
    public long Stop(String label){
        return System.currentTimeMillis()-times.get(label);
    }
    public long StopPr(String label){
        long time=Stop(label);
        System.out.println(label+": "+time+" ms");
        return time;
    }
}
