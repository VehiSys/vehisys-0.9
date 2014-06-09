package ae.ac.masdar.labs.stevas.adama;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import eu.lighthouselabs.obd.reader.activity.ConfigActivity;
import eu.lighthouselabs.obd.reader.activity.MainActivity;

public class CloudCycle implements Runnable {

	private static final String SYNC = "X";
	
	private static List<String> messages = new ArrayList<String>();
	private static List<CloudUpload> queuedUploads = new ArrayList<CloudUpload>();
	private static List<String> cachedFiles = new ArrayList<String>();
	private static CloudUpload upload;
	private static long interval = 2000;
	private static String hostname;
	private static int port;
	private static String roadLoggerUID;
	private static String ecuBoardNumber;
	private static boolean[] statusCode;
	private static Integer configNumber;
	private static boolean uploadPermitted = false;
	private static boolean collectionPermitted = false;
	private static Context context;
	public static final int CACHE_SIZE = 30;
	private static boolean uploadOnWiFiOnly = false;
	private static String deviceId;
	private static Integer phoneType;
	private static final String BACKUP_DIR = "adama";
	private static final String BACKUP_EXT = ".bak";
	private static final SimpleDateFormat CACHE_FORMAT = new SimpleDateFormat("yyyyMMddHHMMssSSS");

	public static boolean isUploadPermitted() {
		return uploadPermitted;
	}
	public static void setUploadPermitted(boolean uploadPermitted) {
		CloudCycle.uploadPermitted = uploadPermitted;
	}

	public static boolean isCollectionPermitted() {
		return collectionPermitted;
	}
	public static void setCollectionPermitted(boolean collectionPermitted) {
		CloudCycle.collectionPermitted = collectionPermitted;
	}
	
	public static boolean isUploadOnWiFiOnly() {
		return uploadOnWiFiOnly;
	}

	public static void setHostname(String hostname) {
		CloudCycle.hostname = hostname;
	}
	public static void setPort(int port) {
		CloudCycle.port = port;
	}
	public static void setRoadLoggerUID(String roadLoggerUID) {
		CloudCycle.roadLoggerUID = roadLoggerUID;
	}
	public static void setEcuBoardNumber(String ecuBoardNumber) {
		CloudCycle.ecuBoardNumber = ecuBoardNumber;
	}
	public static void setUploadOnWiFiOnly(boolean uploadOnWiFiOnly) {
		CloudCycle.uploadOnWiFiOnly = uploadOnWiFiOnly;
	}
	public static void setInterval(long interval) {
		CloudCycle.interval = interval;
	}
	public static long getInterval() {
		return interval;
	}
	public CloudCycle(long intervalS, String hostnameS, int portS, String roadLoggerUIDS, String ecuBoardNumberS, boolean uploadOnWiFiOnlyS, Integer configNumberS, boolean[] statusCodeS, String deviceIdS, Integer phoneTypeS, Context contextS) {
		interval = intervalS;
		hostname = hostnameS;
		port = portS;
		roadLoggerUID = roadLoggerUIDS; 
		ecuBoardNumber = ecuBoardNumberS; 
		configNumber = configNumberS; 
		statusCode = statusCodeS;
		deviceId = deviceIdS;
		phoneType = phoneTypeS;
		context = contextS;
		uploadOnWiFiOnly = uploadOnWiFiOnlyS;
		//load saved files
		for(String filename : context.fileList()) {
			cachedFiles.add(filename);
		}
		
	}
	
	public static CloudUpload getUpload() {
		synchronized(SYNC) {
			if(upload == null) {
				upload = new CloudUpload(roadLoggerUID, ecuBoardNumber, configNumber, statusCode);
			}
			return upload;
		}
	}
	public static int getQueueSize() {
		synchronized(SYNC) {
			return queuedUploads.size() + cachedFiles.size()*CACHE_SIZE;
		}
	}
	public static int clearQueue() {
		synchronized(SYNC) {
			int count = queuedUploads.size();
			queuedUploads.clear();
			List<String> cleared = new ArrayList<String>();
			for(String cachedFile : cachedFiles) {
				if(context.deleteFile(cachedFile)) {
					count += CACHE_SIZE;
					cleared.add(cachedFile);
				}
			}
			cachedFiles.removeAll(cleared);
			return count;
		}
	}
	public static List<String> getMessages() {
		List<String> sending = new ArrayList<String>();
		synchronized(SYNC) {
			sending.addAll(messages);
			messages.clear();
		}
		return sending;
	}
	public void run() {
		CloudTCPConnection tcpConn = new CloudTCPConnection(hostname,port, deviceId, phoneType);
		getUpload();
		while(true) {
			try {
				Thread.sleep(interval);
			} catch(InterruptedException iex) {
				iex.printStackTrace();
			}
			synchronized(SYNC) {
				if(uploadPermitted && Utilities.isOnline(context) && (!uploadOnWiFiOnly || Utilities.isWifiOnline(context)) && (queuedUploads.size() > 0 || cachedFiles.size() > 0)) {
					//handle those in memory
					if(queuedUploads.size() > 0) {
						try {
							messages.add(tcpConn.upload(queuedUploads));
						} catch(Exception ex) {
							messages.add(ex.getClass().getSimpleName());
						}
					}
					//handle those in file cache
					if(cachedFiles.size() > 0) {
						List<String> sent = new ArrayList<String>();
						try {
								String filename = cachedFiles.get(0);
								FileInputStream fis = context.openFileInput(String.valueOf(filename));
								BufferedReader br = new BufferedReader(new InputStreamReader(fis));
								String line = null;
								StringBuffer contents = new StringBuffer();
								while((line = br.readLine()) != null) {
									contents.append(line);
									contents.append("\r\n");
								}
								messages.add(tcpConn.upload(contents.toString(),filename));
								sent.add(filename);
								fis.close();
								context.deleteFile(filename);
						} catch(Exception ex) {
							messages.add(ex.getClass().getSimpleName());
						}
						cachedFiles.removeAll(sent);
					}
				}
				if(collectionPermitted && upload != null && upload.isReady()) {
					queuedUploads.add(upload);
					OutputStream os = null;
					try {
						//running backup with new file every 5 minutes
						String filename = CACHE_FORMAT.format(new Date(((new Date()).getTime()/300000)*300000))+BACKUP_EXT;
					    //Internal storage option (not accessible from outside app)
//						os = context.openFileOutput(String.valueOf(filename), Context.MODE_APPEND);
//						os.write(upload.getUploadMessage().getBytes());
//						os.close();
						//External storage option
						if(Utilities.isExternalStorageWritable()) {
							//try to create separate dir for app 
							File dir = new File(Environment.getExternalStorageDirectory(),BACKUP_DIR);
							if(!dir.exists()) {
								if(!dir.mkdirs()) {
									dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
								} else {
									dir.setWritable(true, false);
									dir.setReadable(true, false);
								}
							}
							File file = new File(dir, filename);
							if(file.createNewFile()) {
								file.setWritable(true, false);
								file.setReadable(true, false);
							}
							if(file.exists() && file.canWrite()) {
								os = new BufferedOutputStream(new FileOutputStream(file,true));
								os.write(upload.getUploadMessage().getBytes());
							}
						}
					} catch(Exception ex) {
						messages.add(ex.getClass().getSimpleName());
					} finally {
						if(os != null) {
							try {
								os.close();
							} catch(Exception ex) {
								messages.add(ex.getClass().getSimpleName());
							}
						}
					}
					if(uploadPermitted && Utilities.isOnline(context) && (!uploadOnWiFiOnly || Utilities.isWifiOnline(context))) {
						try {
							messages.add(tcpConn.upload(queuedUploads));
						} catch(Exception ex) {
							messages.add(ex.getClass().getSimpleName());
						}
					}
					upload = null;
				}
				if(queuedUploads.size() == CACHE_SIZE) {
					String filename = CACHE_FORMAT.format(new Date((new Date()).getTime()));
					try {
						FileOutputStream fos = context.openFileOutput(String.valueOf(filename), Context.MODE_PRIVATE);
						for(CloudUpload queuedUpload : queuedUploads) {
							fos.write(queuedUpload.getUploadMessage().getBytes());
						}
						fos.close();
						cachedFiles.add(filename);
						queuedUploads.clear();
						messages.add(CACHE_SIZE+" cached to "+filename);
					} catch(Exception ex) {
						messages.add(ex.getClass().getSimpleName());
					}
				}
			}
			if(Thread.interrupted()) {
				break;
			}
		}
	}
	


}
