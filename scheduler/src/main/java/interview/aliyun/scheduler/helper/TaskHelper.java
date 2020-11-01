package interview.aliyun.scheduler.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import interview.aliyun.scheduler.entity.Task;
import interview.aliyun.scheduler.entity.TaskType;

public class TaskHelper {
	private Random random = new Random();
	
	public List<Task> getTasks(int taskNum) {
		List<Task> tasks = new ArrayList<Task>();	
		for (int i=0; i<taskNum; i++) {
			String id  = String.format("%12d", i).replace(" ", "0");
			TaskType type = TaskType.getRandomTaskType();
			int weight = random.nextInt(11);
			tasks.add(new Task(id, type, weight));
		}
		return tasks;
	}
}
