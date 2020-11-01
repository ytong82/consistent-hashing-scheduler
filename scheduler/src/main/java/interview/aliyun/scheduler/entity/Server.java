package interview.aliyun.scheduler.entity;

import java.util.ArrayList;
import java.util.List;

public class Server {
	private String ip;
	private List<Task> tasks;
	private int load;
	
	public Server(String ip) {
		this.ip = ip;
		this.tasks = new ArrayList<Task>();
		this.load = 0;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public List<Task> getTasks() {
		return this.tasks;
	}
	
	public void addTask(Task task) {
		this.tasks.add(task);
		this.load += task.getWeight();
	}
	
	public int getLoad() {
		return this.load;
	}
}
