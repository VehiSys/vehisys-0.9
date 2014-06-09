package ae.ac.masdar.labs.stevas.adama;
import java.io.*;
import java.util.*;
import java.net.*;

public class CloudTCPConnection {
	private String hostname;
	private int port;
	private String deviceId;
	private Integer phoneType;
	public CloudTCPConnection(String hostname, int port, String deviceId, Integer phoneType) {
		this.hostname = hostname;
		this.port = port;
		this.deviceId = deviceId;
		this.phoneType = phoneType;
	}

//	public String upload(CloudUpload upload)  throws IOException,UnknownHostException {
//		List<CloudUpload> uploads = new ArrayList<CloudUpload>();
//		uploads.add(upload);
//		return upload(uploads);
//	}
	
	public String upload(List<CloudUpload> uploads) throws Exception {
        List<CloudUpload> uploaded = new ArrayList<CloudUpload>();
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            String response = "";
	        socket = new Socket(InetAddress.getByName(hostname), port);
	        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        out.println(Utilities.getIdString(deviceId,phoneType));
//			out.println("ATE0");
//			response = in.readLine();
	        String uploadMessage;
			for(CloudUpload upload:uploads) {
				if(out.checkError()) throw new Exception("Stream error");
				if ((uploadMessage = upload.getUploadMessage()) != null) {
//					uploadMessage =
//							"STATUS,000EBD20,0001,ABCD,0B\r\n" +
//							"ACC000EBBDD0201021502F9\r\n" +
//							"GPS000EBA4F$GPRMC,001539.042,V,,,,,,,150209,,,N*4A\r\n" +
//							"OBD000EA3C5DE00000000FF\r\n" +
//							"OBD000EACA1AD00000000FF\r\n" +
//							"OBD000EB471BE00000000FF\r\n" +
//							"VIN1G1JC5444R7252367\r\n" +
//							"END\r\n";
					out.println(uploadMessage);
					out.flush();
					uploaded.add(upload);
				}
			}
			return response + (uploaded.size()!=1?(uploaded.size()+" uploaded from memory"):(Utilities.getIdString(deviceId,phoneType)+uploaded.get(0).toString()));
        } catch(Exception ex) {
        	throw ex;
        } finally {
        	uploads.removeAll(uploaded);
			if(out != null) out.close();
			if(in != null) in.close();
			if(socket != null) socket.close();
        }
    }

	public String upload(String uploadMessage, String source) throws Exception {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            String response = "!!";
	        socket = new Socket(InetAddress.getByName(hostname), port);
	        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        out.println(Utilities.getIdString(deviceId,phoneType));
//			out.println("ATE0");
//			response = in.readLine();
			out.println(uploadMessage);
			out.flush();
			return response + (uploadMessage.split("[E][N][D]").length-1) + " uploaded from " + source;
        } catch(Exception ex) {
        	throw ex;
        } finally {
			if(out != null) out.close();
			if(in != null) in.close();
			if(socket != null) socket.close();
        }
    }
	
//	public static void main(String[] params) {
//		String uploadMessage =
//		"STATUS,000EBD20,0001,ABCD,0B\n" +
//		"ACC000EBBDD0201021502F9\n" +
//		"GPS000EBA4F$GPRMC,001539.042,V,,,,,,,150209,,,N*4A\n" +
//		"OBD000EA3C5DE00000000FF\n" +
//		"OBD000EACA1AD00000000FF\n" +
//		"OBD000EB471BE00000000FF\n" +
//		"VIN1G1JC5444R7252367\n" +
//		"END";
//		CloudTCPConnection tcpConn =  new CloudTCPConnection("23.23.126.78",5625);
//		try {
//			tcpConn.upload(uploadMessage);
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
//	}

}

