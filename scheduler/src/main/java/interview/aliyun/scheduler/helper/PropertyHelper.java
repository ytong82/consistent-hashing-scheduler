package interview.aliyun.scheduler.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import interview.aliyun.scheduler.entity.TaskType;


public class PropertyHelper {
	private static final String propertyFilePath = "src/main/resources/config.properties";
	private Properties props = new Properties();
	
	private double imbalanceFacotor = 1.2f;
	private double boundLoadThresholdFactor = 1f;
	
	public void loadProperties() throws IOException, IllegalArgumentException {
		InputStream input = new FileInputStream(propertyFilePath);
        props.load(input);
        validateProperties();
	}
	
	private void validateProperties() throws IllegalArgumentException {
		for (String key : this.props.stringPropertyNames()) {
			if ( !key.equals("scheduler.imbalance.factor") && !key.equals("scheduler.bound.load.threshold.factor")) {
				try {
					Integer.parseInt(this.props.getProperty(key));
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException();
				}
			}
		}
			
		if (props.getProperty("scheduler.imbalance.factor") != null
				&& props.getProperty("scheduler.imbalance.factor").trim() != "") {
			try {
				this.imbalanceFacotor = Double.parseDouble(props.getProperty("scheduler.imbalance.factor"));
				if (this.imbalanceFacotor < 1.05f ) { 
					throw new IllegalArgumentException();
				}
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException();
			}
		}
		
		if (props.getProperty("scheduler.bound.load.threshold.factor") != null
				&& props.getProperty("scheduler.bound.load.threshold.factor").trim() != "") {
			try {
				this.boundLoadThresholdFactor = Double.parseDouble(props.getProperty("scheduler.bound.load.threshold.factor"));
				if (this.boundLoadThresholdFactor < 0.1 || boundLoadThresholdFactor >= TaskType.values().length) {
					throw new IllegalArgumentException();
				}
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException();
			}
		}
	}
	
	public double getImbalanceFacotr() {
		return this.imbalanceFacotor;
	}
	
	public double getBoundLoadThresholdFactor() {
		return this.boundLoadThresholdFactor;
	}
	
	public int getServerTotal() {
		return Integer.parseInt(this.props.getProperty("server.total", "10000"));
	}
}
