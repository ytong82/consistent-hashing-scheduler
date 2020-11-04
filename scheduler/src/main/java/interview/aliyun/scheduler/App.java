package interview.aliyun.scheduler;

import java.io.IOException;
//import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import interview.aliyun.scheduler.entity.Server;
import interview.aliyun.scheduler.helper.PropertyHelper;
import interview.aliyun.scheduler.helper.ServerHelper;
import interview.aliyun.scheduler.helper.TaskHelper;

public class App {
	
    public static void main( String[] args ) {
    	// load properties
    	PropertyHelper propertyHelper = new PropertyHelper();
    	try {
    		propertyHelper.loadProperties();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    		System.exit(1);
    	} catch (IllegalArgumentException ex) {
    		ex.printStackTrace();
    		System.exit(1);
    	}
    	
    	// setup helpers
    	ServerHelper serverHelper = new ServerHelper(propertyHelper);
    	TaskHelper taskHelper = new TaskHelper();
    	
    	// setup scheduler
    	List<Server> servers = serverHelper.getServers();
    	Scheduler scheduler = new Scheduler(servers, propertyHelper);
    	
    	// start timer
    	long startTime = System.currentTimeMillis();
    	
    	// schedule
    	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    	
    	// run task runner at the interval of 5s, 100000 tasks in a batch, 2 runners, run for twice
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 5, 2, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	
    	// run task runner at the interval of 10s, 100000 tasks in a batch, 2 runners, run for twice
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 10, 2, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	
    	// run task runner at the interval of 60s, 100000 tasks in a batch, 2 runners, run for once
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 60, 1, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	try {
    		executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  
    	
    	// stop timer
    	long endTime = System.currentTimeMillis();
    	System.out.printf("[THREAD] %s program takes %s to run \n", 
				Thread.currentThread().getName(), (endTime - startTime) + "ms");
    	
    	// print result
    	/*long loadSum = 0;
    	for (Server server : servers) {
    		loadSum += server.getLoad();
    	}
    	
    	DecimalFormat df = new DecimalFormat("0.0000");
    	for (Server server : servers) {
    		System.out.printf("[SERVER] %s takes %s load with %s percentage \n", server.getIp(), server.getLoad(), df.format((float)server.getLoad() / loadSum));
    	}*/
    }
}
