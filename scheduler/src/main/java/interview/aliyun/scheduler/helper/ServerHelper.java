package interview.aliyun.scheduler.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import interview.aliyun.scheduler.entity.Server;

public class ServerHelper {
	private static final int SERVER_TOTAL = 10000;
	private Random random = new Random();
	
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
