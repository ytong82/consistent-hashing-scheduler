package interview.aliyun.scheduler;

import java.text.DecimalFormat;
import java.util.List;

import interview.aliyun.scheduler.entity.Server;
import interview.aliyun.scheduler.entity.Task;
import interview.aliyun.scheduler.helper.ServerHelper;
import interview.aliyun.scheduler.helper.TaskHelper;

public class App {
    public static void main( String[] args ) {
    	ServerHelper serverHelper = new ServerHelper();
    	TaskHelper taskHelper = new TaskHelper();
    	
    	// setup scheduler
    	List<Server> servers = serverHelper.getServers();
    	Scheduler scheduler = new Scheduler(servers);
    	
    	// start to schedule
    	List<Task> tasks = taskHelper.getTasks();
    	int count = 0;
    	long startTime = System.currentTimeMillis();
    	for (Task task : tasks) {
    		count++;
    		if (count % 10000 == 0) {
    			long endTime = System.currentTimeMillis();
    			System.out.printf("[RUN TIME] %s for [COUNT] %s \n", (endTime - startTime) + "ms", count);
    			startTime = endTime;
    		}
    		scheduler.scheduleTask(task);
    	}
    	
    	// print result
    	long loadSum = 0;
    	for (Server server : servers) {
    		loadSum += server.getLoad();
    	}
    	
    	DecimalFormat df = new DecimalFormat("0.0000");
    	for (Server server : servers) {
    		System.out.printf("[SERVER] %s takes %s load with %s percentage \n", server.getIp(), server.getLoad(), df.format((float)server.getLoad() / loadSum));
    	}
    }
}
