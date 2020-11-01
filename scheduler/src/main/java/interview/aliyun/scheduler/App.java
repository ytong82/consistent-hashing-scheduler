package interview.aliyun.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import interview.aliyun.scheduler.entity.Server;
import interview.aliyun.scheduler.helper.ServerHelper;
import interview.aliyun.scheduler.helper.TaskHelper;

public class App {
	private static void loadProperties() throws IOException {
		InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties");
		Properties prop = new Properties();

        if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            throw new IOException();
        }
        prop.load(input);
	}
	
    public static void main( String[] args ) {
    	// load properties
    	try {
    		loadProperties();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    		System.exit(1);
    	}
    	
    	// setup helpers
    	ServerHelper serverHelper = new ServerHelper();
    	TaskHelper taskHelper = new TaskHelper();
    	
    	// setup scheduler
    	List<Server> servers = serverHelper.getServers();
    	Scheduler scheduler = new Scheduler(servers);
    	
    	// schedule
    	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    	
    	// start task runner at the interval of 5s
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 5, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	
    	// start task runner at the interval of 10s
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 10, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	
    	// start task runner at the interval of 10s
    	for (int i=0; i<2; i++) {
    		TaskRunner taskRunner = new TaskRunner(100000, 60, scheduler, taskHelper);
    		executor.submit(taskRunner);
    	}
    	try {
    		executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
