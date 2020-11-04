package interview.aliyun.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import interview.aliyun.scheduler.entity.Server;
import interview.aliyun.scheduler.entity.Task;
import interview.aliyun.scheduler.entity.TaskType;
import interview.aliyun.scheduler.helper.HashUtilHelper;
import interview.aliyun.scheduler.helper.PropertyHelper;

public class Scheduler {
	private static final int VIRTUAL_NODE_NUM = 10;
	private static final String GENERAL_RING_NAME= "General";
	
	private PropertyHelper propertyHelper;
	private final double IMBALANCE_FACTOR;
	private final double BOUND_LOAD_THRESHOLD_FACTOR;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	
	private int loadSum;
	private int serverSum;
	private double maxAssignedLoad;
	private int bound_load_threshold;
	private Map<String, Map<String, Server>> servers;
	private Map<String, SortedMap<Integer, String>> hashRings = new HashMap<String, SortedMap<Integer,String>>();

	private String getVirtualNodeName(String ip, int num) {
		return ip + "&&VN" + String.valueOf(num);
	}
	
	private String getServerName(String virtualNode) {
		return virtualNode.split("&&")[0];
	}
	
	synchronized private void updateMaxAssignedLoad(int weight) {
		this.loadSum += weight;
		this.maxAssignedLoad = (this.loadSum / this.serverSum) * IMBALANCE_FACTOR;
		this.bound_load_threshold = (int) (this.serverSum / TaskType.values().length * BOUND_LOAD_THRESHOLD_FACTOR);
	}
	
	synchronized private void updateHashRings() {
		// clean up all hash rings
		this.hashRings.clear();
		
		// update general and task specific hash ring
		for (String keyName: this.servers.keySet()) {
			SortedMap<Integer, String> hashRing = new TreeMap<Integer, String>();
			for (Server server : this.servers.get(keyName).values()) {
				for (int i=0; i<VIRTUAL_NODE_NUM; i++) {
					String virtualNodeName = getVirtualNodeName(server.getIp(), i);
					int hashVal = HashUtilHelper.getHashVal(virtualNodeName);
					//System.out.println("[" + virtualNodeName + "] launched @ " + hashVal);
					hashRing.put(hashVal, virtualNodeName);
				}
			}
			this.hashRings.put(keyName, hashRing);
		}
	}
	
	public Scheduler(List<Server> servers, PropertyHelper propertyHelper) {
		// set properties
		this.propertyHelper = propertyHelper;
		this.IMBALANCE_FACTOR = this.propertyHelper.getImbalanceFacotr();
		this.BOUND_LOAD_THRESHOLD_FACTOR = this.propertyHelper.getBoundLoadThresholdFactor();
		
		// define servers and hash rings
		this.servers = new HashMap<String, Map<String, Server>>();
		this.hashRings = new HashMap<String, SortedMap<Integer, String>>();
				
		// initialize general servers and hash ring
		this.loadSum = 0;
		this.serverSum = 0;
		Map<String, Server> generalServers = new HashMap<String, Server>();
		SortedMap<Integer, String> generalRing = new TreeMap<Integer, String>();
		for (Server server: servers) {
			this.serverSum++;
			generalServers.put(server.getIp(), server);
			for (int i=0; i<VIRTUAL_NODE_NUM; i++) {
				String virtualNodeName = getVirtualNodeName(server.getIp(), i);
				int hashVal = HashUtilHelper.getHashVal(virtualNodeName);
				//System.out.println("[" + virtualNodeName + "] launched @ " + hashVal);
				generalRing.put(hashVal, virtualNodeName);
			}
		}
		this.servers.put(GENERAL_RING_NAME, generalServers);
		this.hashRings.put(GENERAL_RING_NAME, generalRing);
		
		// initialize task specific servers and hash rings
		for (TaskType type : TaskType.values()) {
			this.servers.put(type.toString(), new HashMap<String, Server>());
			this.hashRings.put(type.toString(), new TreeMap<Integer, String>());
        }
	}
	
	public Server scheduleTask(Task task) {
		// update max assigned load	
		updateMaxAssignedLoad(task.getWeight());
				
		// search task specific ring, then search general ring
		int hashVal = HashUtilHelper.getHashVal(task.getId());
		SortedMap<Integer, String> taskRing = this.hashRings.get(task.getType().toString());
		Server server = null;
		String serverIp = "";
		String virtualNode = "";
		int vnHashKey;
		
		boolean skipTaskRing = true;
		
		rwl.readLock().lock();
		try {
			if (!taskRing.isEmpty()) {	
				SortedMap<Integer, String> subRing = taskRing.tailMap(hashVal);
				if (subRing == null || subRing.isEmpty()) {
					vnHashKey = taskRing.firstKey();
				} else {
					vnHashKey = subRing.firstKey();
				}
				virtualNode = taskRing.get(vnHashKey);
				serverIp = getServerName(virtualNode);
				server = this.servers.get(task.getType().toString()).get(serverIp);
				
				// using bounded load consistent hashing algorithm
				if (server.getLoad() > this.maxAssignedLoad) {	
					Map<String, Server> taskServers = this.servers.get(task.getType().toString());
					if (taskServers.size() > bound_load_threshold) {
						for (Server taskServer : taskServers.values()) {
							if (taskServer.getLoad() < this.maxAssignedLoad) {
								skipTaskRing = false;
								break;
							}
						}					
						if (!skipTaskRing) {
							do {
								// find next virtual node in the ring
								subRing = taskRing.tailMap(vnHashKey + 1);
								if (subRing == null || subRing.isEmpty()) {
									vnHashKey = taskRing.firstKey();	
								} else {
									vnHashKey = subRing.firstKey();
								}
								virtualNode = taskRing.get(vnHashKey);
								serverIp = getServerName(virtualNode);
								server = this.servers.get(task.getType().toString()).get(serverIp);
							} while (server.getLoad() > this.maxAssignedLoad);
						}
					}
				} else {
					skipTaskRing = false;
				}
			} 
		} finally {
			rwl.readLock().unlock();
		}
		
		if (taskRing.isEmpty() || skipTaskRing) {
			rwl.readLock().lock();
			try {
				SortedMap<Integer, String> generalRing = this.hashRings.get(GENERAL_RING_NAME);
				SortedMap<Integer, String> subRing = generalRing.tailMap(hashVal);
				if (subRing == null || subRing.isEmpty()) {
					vnHashKey = generalRing.firstKey();
				} else {
					vnHashKey = subRing.firstKey();
				}
				virtualNode = generalRing.get(vnHashKey);
				serverIp = getServerName(virtualNode);
				server = this.servers.get(GENERAL_RING_NAME).get(serverIp);
				
				// using bounded load consistent hashing algorithm
				if (server.getLoad() > this.maxAssignedLoad) {
					do {
						// find next virtual node in the ring
						subRing = generalRing.tailMap(vnHashKey + 1);
						if (subRing == null || subRing.isEmpty()) {
							vnHashKey = generalRing.firstKey();	
						} else {
							vnHashKey = subRing.firstKey();
						}
						virtualNode = generalRing.get(vnHashKey);
						serverIp = getServerName(virtualNode);
						server = this.servers.get(GENERAL_RING_NAME).get(serverIp);
					} while (server.getLoad() > this.maxAssignedLoad);
				}
			} finally {
				rwl.readLock().unlock();
			}
			// update task specific ring by schedule result
			
			rwl.writeLock().lock();
			try {
				this.servers.get(task.getType().toString()).put(server.getIp(), server);
				for (int i=0; i<VIRTUAL_NODE_NUM; i++) {
					String virtualNodeName = getVirtualNodeName(serverIp, i);
					hashVal = HashUtilHelper.getHashVal(virtualNodeName);
					//System.out.println("[" + virtualNodeName + "] launched @ " + hashVal);
					taskRing.put(hashVal, virtualNodeName);	
				}
			} finally {
				rwl.writeLock().unlock();
			}
		} 
		
		// add this task the server
		// System.out.printf("[SERVER] %s schedule [TASK] %s with type %s \n", server.getIp(), task.getId(), task.getType().toString());
		server.addTask(task);
		return server;
	}
	
	public void addServer(Server server) {
		// update general and task specific servers
		this.serverSum++;
		this.servers.get(GENERAL_RING_NAME).put(server.getIp(), server);
		updateHashRings();
	}
	
	public void removeServer(Server server) {
		// update general and task specific servers
		this.serverSum--;
		for (Map<String, Server> serverMap : this.servers.values()) {
			serverMap.remove(server.getIp());
		}
		updateHashRings();
	}
}
