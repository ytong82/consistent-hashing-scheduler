package interview.aliyun.scheduler;

import java.util.Map;
import java.util.TreeMap;

import interview.aliyun.scheduler.entity.TaskType;

public class App {
    public static void main( String[] args ) {
    	Map<String, Integer> counter = new TreeMap<String, Integer>();
        for (TaskType type : TaskType.values()) {
        	counter.put(type.toString(), 0);
        	if (type == TaskType.TypeC) {
        		System.out.println(type);
        		counter.put(type.toString(), 1);
        	}
        }
        
        counter.forEach(
        	(key, value) -> {
        		System.out.printf("key: %s, value: %s \n", key, value);
        	}
        );
    }
}
