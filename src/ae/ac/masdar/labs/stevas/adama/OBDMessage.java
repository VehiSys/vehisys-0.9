package ae.ac.masdar.labs.stevas.adama;

public class OBDMessage {
	private byte pid;
	private long timestamp;
	private byte[] data;
	
	public OBDMessage(byte pid, byte[] data, long timestamp) {
		this.pid = pid;
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public byte getPID() {
		return pid;
	}
	public void setPID(byte pid) {
		this.pid = pid;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
