package interview.aliyun.scheduler.entity;

public class Task {	
	private String id;
	private TaskType type;
	private int weight;
	
	public Task(String id, TaskType type, int weight) {
		this.id = id;
		this.type = type;
		this.weight = weight;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public TaskType getType() {
		return type;
	}
	
	public void setType(TaskType type) {
		this.type = type;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
