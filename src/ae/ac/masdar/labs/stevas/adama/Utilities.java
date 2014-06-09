package ae.ac.masdar.labs.stevas.adama;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class Utilities {
	public static String hexify(long datum, int length) {
		String hexed = Long.toHexString(datum).toUpperCase();
		while(hexed.length() < length) {
			hexed = "0"+hexed;
		}
		return hexed.substring(hexed.length()-length,hexed.length());
	}
	
//	public static String hexify(float datum, int length) {
//		String hexed = Integer.toHexString(Float.floatToIntBits(datum)).toUpperCase();
//		while(hexed.length() < length) {
//			hexed = "0"+hexed;
//		}
//		return hexed.substring(0,length);
//	}

	public static String padNumber(int number, int length) {
		String numString = String.valueOf(number);
		while(numString.length() < length) {
			numString = "0"+numString;
		}
		return numString;
	}
	
	public static String hexify(byte[] data, int length) {
		String hexed = "";
		for(byte datum : data) {
			String hex = Integer.toHexString((int)(datum & 0xFF)).toUpperCase();
			hexed = (hex.length()==2?"":"0")+hex+hexed;
		}
		while(hexed.length() < length) {
			hexed = "0"+hexed;
		}
		return hexed.substring(hexed.length()-length,hexed.length());
	}
	
	public static String hexify(boolean[] flags, int length) {
		int code = 0;
		int i = 0;
		for(boolean flag : flags) {
			code += flag?(2 ^ i):0;
			i++;
		}
		String hexed = Integer.toHexString(code).toUpperCase();
		while(hexed.length() < length) {
			hexed = "0"+hexed;
		}
		return hexed.substring(hexed.length()-length,hexed.length());
	}
	
	public static byte extractPid(String pidCommand) {
		return (byte) Integer.parseInt(pidCommand.indexOf(" ")<0?pidCommand:(pidCommand.split("[ ]")[1]),16);
	}
	
	public static String scaleAccelerationGravityRangeHex(float mpss, float gRange, int signedSteps, int length) {
		long conv = Math.round(mpss*512/(1.5*SensorManager.STANDARD_GRAVITY));
		if(conv > 511) conv = 511;
		if(conv < -512) conv = -512;
		String hexed = Long.toHexString(conv+512).toUpperCase();
		while(hexed.length() < length) {
			hexed = "0"+hexed;
		}
		return hexed.substring(hexed.length()-length,hexed.length());
	}

	public static boolean isEngineOn(byte[] rpmData) {
		if(rpmData == null) return false;
        return (((rpmData[0] << 8) | rpmData[1]) & 0xFFFF) > 0;
	}
	
	public static String displayRpmData(byte[] data) {
		String displayed = "";
//		for(byte datum : data) {
//			displayed += "." + datum;
//		}
		if(data != null) {
	        int b1 = data[0] & 0xFF;
	        int b2 = data[1] & 0xFF;
	        displayed = String.valueOf((((b1 << 8) | b2) & 0xFFFF) / 4)+"r/m";
		}
        return displayed;
	}
	public static String displayMafData(byte[] data) {
		String displayed = "";
//		for(byte datum : data) {
//			displayed += "." + datum;
//		}
		if(data != null) {
	        int b1 = data[0] & 0xFF;
	        int b2 = data[1] & 0xFF;
	        displayed = String.valueOf((((b1 << 8) | b2) & 0xFFFF) / 100.0f)+"g/s";
		}
        return displayed;
	}
	public static String displaySpeedData(byte[] data) {
		String displayed = "";
//		for(byte datum : data) {
//			displayed += "." + datum;
//		}
		if(data != null) displayed = String.valueOf(Math.round(data[0] & 0xFF))+"km/h";
        return displayed;
	}
	
    public static byte[] getByteArray(List<Byte> tbuff) {
    	if(tbuff == null) return null;
        StringBuffer hexeds = new StringBuffer("");
        for(byte tbyte : tbuff) {
        	if(tbyte == 13) break;
        	hexeds.append((char)tbyte);
        }
        String[] hexeda = hexeds.toString().trim().split("[ ]");
        byte[] xbuff = new byte[hexeda.length-2];
        int i = 0;
        for(String hexedb : hexeda) {
       		if(i > 1) {
       			xbuff[i-2] = (byte)Integer.parseInt(hexedb, 16);
       		}
       		i++;
        }
        return xbuff;
    }

//    public static byte[] getObdArray(List<Byte> tbuff) {
//        byte[] xbuff = new byte[tbuff.size()];
//        int i = 0;
//        for(byte tbyte : tbuff) {
//       		xbuff[i++] = tbyte;
//        }
//        return xbuff;
//    }

    public static List<Byte> getByteList(List<Byte> tbuff) {
    	if(tbuff == null) return null;
        StringBuffer hexeds = new StringBuffer("");
        for(byte tbyte : tbuff) {
        	if(tbyte == 13) break;
        	hexeds.append((char)tbyte);
        }
        String[] hexeda = hexeds.toString().trim().split("\\s");
        List<Byte> xbuff = new ArrayList<Byte>();
        for(String hexedb : hexeda) {
        		xbuff.add((byte)Integer.parseInt(hexedb, 16));
        }
        return xbuff;
    }
    
    public static String formatTime(long time) {
    	time /= 1000;
    	long hh = time / 3600;
    	long mm = (time % 3600)/60;
    	long ss = time % 60;
    	return (hh<10?"0":"")+hh+":"+(mm<10?"0":"")+mm+":"+(ss<10?"0":"")+ss;
    }

	public static boolean isOnline(Context context) {
	    NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	    return (netInfo != null && netInfo.isConnected());
	}
	
	public static boolean isWifiOnline(Context context) {
	    NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    return (netInfo != null && netInfo.isConnected());
	}
	
	public static String getIdString(String deviceId, Integer phoneType) {
		if(deviceId != null && phoneType != null) {
			 switch(phoneType){
				 case TelephonyManager.PHONE_TYPE_GSM:
					 return "IMEI" + deviceId + "\r\n";
		
				 case TelephonyManager.PHONE_TYPE_CDMA:
					 return "MEID" + deviceId + "\r\n";
		
				 default:
					 return "OPID" + deviceId + "\r\n";
			 }
		 } else {
			 return "";
		 }
	}
	
	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}

}
