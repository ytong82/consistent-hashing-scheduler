package interview.aliyun.scheduler.entity;

import java.util.Random;

public enum TaskType {
	TypeA, 
	TypeB,
	TypeC,
	TypeD,
	TypeE;
	
	public static TaskType getRandomTaskType() {
		Random random = new Random();
		return values()[random.nextInt(values().length)];
	}
}
