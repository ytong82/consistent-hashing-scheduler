package interview.aliyun.scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import interview.aliyun.scheduler.entity.Task;
import interview.aliyun.scheduler.helper.TaskHelper;

public class TaskRunner implements Runnable {	
	private int taskNum;
	private int sleepDuration;
	private int runTimes;
	private Scheduler scheduler;
	private TaskHelper taskHelper;
	
	public TaskRunner(int taskNum, int sleepDuration, int runTimes, Scheduler scheduler, TaskHelper taskHelper) {
		this.taskNum = taskNum;
		this.sleepDuration = sleepDuration;
		this.runTimes = runTimes;
		this.scheduler = scheduler;
		this.taskHelper = taskHelper;
	}
	
	public void run() {
		try {
			List<Task> tasks = this.taskHelper.getTasks(this.taskNum);
			int count = 0;
	    	long startTime = System.currentTimeMillis();
	    	long endTime = System.currentTimeMillis();
	    	for (int i=0; i<runTimes; i++) {
		    	for (Task task : tasks) {
		    		count++;
		    		if (count % 10000 == 0) {
		    			endTime = System.currentTimeMillis();
		    			System.out.printf("[THREAD] %s [RUN TIME] %s for [COUNT] %s \n", 
		    					Thread.currentThread().getName(), (endTime - startTime) + "ms", count);
		    			startTime = endTime;
		    		}
		    		this.scheduler.scheduleTask(task);
		    	}
		    	if (i < runTimes) {
		    		TimeUnit.SECONDS.sleep(this.sleepDuration);
		    	}
	    	}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	public int getTaskNum() {
		return taskNum;
	}

	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}

	public int getSleepDuration() {
		return sleepDuration;
	}

	public void setSleepDuration(int sleepDuration) {
		this.sleepDuration = sleepDuration;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public TaskHelper getTaskHelper() {
		return taskHelper;
	}

	public void setTaskHelper(TaskHelper taskHelper) {
		this.taskHelper = taskHelper;
	}
	
}
