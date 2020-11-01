package interview.aliyun.scheduler.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import interview.aliyun.scheduler.entity.Server;

public class ServerHelper {
	private PropertyHelper propertyHelper;
	private Random random;
	private final int SERVER_TOTAL;
	
	public ServerHelper(PropertyHelper propertyHelper) {
		this.propertyHelper = propertyHelper;
		this.random = new Random();
		this.SERVER_TOTAL = this.propertyHelper.getServerTotal();
	}
	
	private String generateIpAddr() {
		return random.nextInt(256) + "." + random.nextInt(256) + "." 
				+ random.nextInt(256) + "." + random.nextInt(256);
	}
	
	public List<Server> getServers() {
		List<Server> servers = new ArrayList<Server>();
		Set<String> ips = new HashSet<String>();
		
		for (int i=0; i<SERVER_TOTAL; i++) {
			String ip = generateIpAddr();
			while (ips.contains(ip)) {
				ip = generateIpAddr();
			}
			ips.add(ip);		
			servers.add(new Server(ip));
		}
		return servers;
	}
}
