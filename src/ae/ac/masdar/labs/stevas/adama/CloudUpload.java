package ae.ac.masdar.labs.stevas.adama;

import java.util.*;

public class CloudUpload {
	private int configNumber;
	private String roadLoggerUID;
	private String ecuBoardNumber;
	private boolean[] statusCode;
	private long statusTimestamp;
	private long gpsTimestamp;
	private long accTimestamp;
	private String gpsNmea;
	private Float accX;
	private Float accY;
	private Float accZ;
	private String vin;
	private static final Byte speedPid = Byte.parseByte("0D",16);
	private static final Byte rpmPid = Byte.parseByte("0C",16);
	private static final Byte mafPid = Byte.parseByte("10",16);
	
	private Map<Byte,OBDMessage> obdMessages;
	private Map<String, Boolean> labels;
	private List<Integer> dtcList;
	
	public String toString() {
		String message = "";
//		message += "GPS" + Utilities.hexify(gpsTimestamp,8) + gpsNmea+"\n";
//		message += "ACC" + Utilities.hexify(accTimestamp,8) + Utilities.scaleAccelerationGravityRangeHex(accX,1.5f,512,4) + Utilities.scaleAccelerationGravityRangeHex(accY,1.5f,512,4) + Utilities.scaleAccelerationGravityRangeHex(accZ,1.5f,512,4)+"\n";
//		if(vin != null || ecuBoardNumber != null) {
//			message += "VIN" + (vin==null?("UNDEF"+ecuBoardNumber):vin) +"\n";
//		}
		if(obdMessages.containsKey(speedPid) || obdMessages.containsKey(rpmPid) || obdMessages.containsKey(mafPid))
		message /*+=
		(obdMessages.containsKey(speedPid)?("|"+Utilities.displaySpeedData(obdMessages.get(speedPid).getData())):"")
		+(obdMessages.containsKey(rpmPid)?("|"+Utilities.displayRpmData(obdMessages.get(rpmPid).getData())):"")
		+(obdMessages.containsKey(mafPid)?("|"+Utilities.displayMafData(obdMessages.get(mafPid).getData())):"")
		+"|\n";
		message +*/= getUploadMessage();
		return message;
	}
	
	public CloudUpload(String roadLoggerUID, String ecuBoardNumber, Integer configNumber, boolean[] statusCode) {
		this.roadLoggerUID = roadLoggerUID; 
		this.ecuBoardNumber = ecuBoardNumber;
		this.configNumber = configNumber;
		this.statusCode = statusCode;
		
//		this.gpsNmea = "0";
//		this.accX = (float)0;
//		this.accY = (float)0;
//		this.accZ = (float)0;
		this.gpsNmea = null;
		this.accX = null;
		this.accY = null;
		this.accZ = null;

		obdMessages = new HashMap<Byte,OBDMessage>();
		dtcList = new ArrayList<Integer>();
	}
	
	public void setStatusTimestamp(long statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
	}
	
	public void setGps(long gpsTimestamp, String gpsNmea) {
		this.gpsTimestamp = gpsTimestamp;
		this.gpsNmea = gpsNmea;
	}
	
	public void setAcc(long accTimestamp, float accX, float accY, float accZ) {
		this.accTimestamp = accTimestamp;
		this.accX = accX;
		this.accY = accY;
		this.accZ = accZ;
	}
	
	public void setVin(String vin) {
		this.vin = vin;
	}
	
	public void setLabels(Map<String, Boolean> labels) {
		this.labels = labels;
	}
	
	public void addOBDMessage(OBDMessage obdMessage) {
		obdMessages.put(obdMessage.getPID(), obdMessage);
	}
	
	public void addDTC(Integer dtc) {
		dtcList.add(dtc);
	}
	
	public String getUploadMessage() {
		String message = "STATUS,"+Utilities.hexify(statusTimestamp,8)+","+Utilities.padNumber(configNumber,4)+","+roadLoggerUID+","+Utilities.hexify(statusCode,2)+"\r\n";
		message += "GPS" + Utilities.hexify(gpsTimestamp,8) + gpsNmea+"\r\n";
		if(accX != null && accY != null && accZ != null)
			message += "ACC" + Utilities.hexify(accTimestamp,8) + Utilities.scaleAccelerationGravityRangeHex(accX,1.5f,512,4) + Utilities.scaleAccelerationGravityRangeHex(accY,1.5f,512,4) + Utilities.scaleAccelerationGravityRangeHex(accZ,1.5f,512,4)+"\r\n";
		if(dtcList.size() > 0) {
			message += "DTC";
			for(Integer dtc : dtcList) {
				message += ","+dtc;
			}
			message += "\r\n";
		}
		for(OBDMessage obdMessage : obdMessages.values()) {
			message += "OBD"+Utilities.hexify(obdMessage.getTimestamp(),8)+Utilities.hexify(obdMessage.getPID(),2)+((obdMessage.getData() == null)?"00000000FF":(Utilities.hexify(obdMessage.getData(),8)+Utilities.hexify(obdMessage.getData().length,2)))+"\r\n";
		}
		if(vin != null || ecuBoardNumber != null) {
			message += "VIN" + (vin==null?("UNDEF"+(ecuBoardNumber.length()>12?(ecuBoardNumber.substring(0,4)+"...ERROR"):vin)):(vin.length()>17?(vin.substring(0,9)+"...ERROR"):vin)) +"\r\n";
		}
		for(String label : labels.keySet()) {
			message += "LABEL" + label + (labels.get(label)?"1":"0") +"\r\n";
		}
		message += "END\r\n";
		return message;
	}
	
	public boolean isReady() {
		return obdMessages.containsKey(rpmPid) && Utilities.isEngineOn(obdMessages.get(rpmPid).getData()) && !(statusTimestamp == 0 || gpsNmea == null);
	}
	
}
