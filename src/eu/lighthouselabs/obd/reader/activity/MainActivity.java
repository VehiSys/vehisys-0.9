/*
 * TODO put header
 */
package eu.lighthouselabs.obd.reader.activity;
import ae.ac.masdar.labs.stevas.adama.CloudCycle;
import ae.ac.masdar.labs.stevas.adama.CloudUpload;
import ae.ac.masdar.labs.stevas.adama.Utilities;
import ae.ac.masdar.labs.stevas.adama.OBDMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.location.GpsStatus.NmeaListener;
import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.commands.control.CommandEquivRatioObdCommand;
import eu.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import eu.lighthouselabs.obd.commands.protocol.VinObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineLoadObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import eu.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelEconomyWithMAFObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelTrimObdCommand;
import eu.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import eu.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import eu.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.enums.FuelTrim;
import eu.lighthouselabs.obd.enums.FuelType;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.R;
import eu.lighthouselabs.obd.reader.drawable.CoolantGaugeView;
import eu.lighthouselabs.obd.reader.drawable.FuelGaugeView;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;
import eu.lighthouselabs.obd.reader.io.ObdGatewayService;
import eu.lighthouselabs.obd.reader.io.ObdGatewayServiceConnection;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob.ObdCommandJobState;
import android.widget.LinearLayout;
//==============================================
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;


/**
 * The main activity.
 */
public class MainActivity extends Activity {
	
	private float x1,x2;
	static final int MIN_DISTANCE = 150;
	private int currentChartType = CHART_CONSUMPTION;
	 public boolean onTouchEvent(MotionEvent event)
	 {     
	     switch(event.getAction())
	     {
	       case MotionEvent.ACTION_DOWN:
	           x1 = event.getX();                         
	       break;         
	       case MotionEvent.ACTION_UP:
	           x2 = event.getX();
	           float deltaX = x1-x2;
	           if (deltaX > MIN_DISTANCE) {
		             //Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show ();
		        	 currentChartType = (currentChartType == CHART_CONSUMPTION?CHART_CARBON:CHART_CONSUMPTION); 
		        	 if(hc.getChildCount() != 0) displayHistory(currentChartType, false);
		       } else if (deltaX < -MIN_DISTANCE) {
		             //Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show ();
		        	 currentChartType = (currentChartType == CHART_CONSUMPTION?CHART_CARBON:CHART_CONSUMPTION); 
		        	 if(hc.getChildCount() != 0) displayHistory(currentChartType, true);
		       }                          
	       break;   
	     }           
	     return super.onTouchEvent(event);       
	 }

	private static final String TAG = "MainActivity";

	private CloudUpload upload;
	/*
	 * TODO put description
	 */
	static final int NO_BLUETOOTH_ID = 0;
	static final int BLUETOOTH_DISABLED = 1;
	static final int NO_GPS_ID = 2;
	static final int COLLECT_TOGGLE = 3;
//	static final int STOP_LIVE_DATA = 4;
	static final int SETTINGS = 5;
	static final int CLEAR_DATA = 6;
	static final int TABLE_ROW_MARGIN = 7;
	static final int NO_ORIENTATION_SENSOR = 8;
	static final int NO_ACCELERATION_SENSOR = 9;
	static final int UPLOAD_TOGGLE = 10;
	static final int CHART_CONSUMPTION = 0;
	static final int CHART_CARBON = 1;
	private static final long MAX_TIMESTAMP = 16^8;
	
	private static Map<String,Boolean> labelRegister = new HashMap<String,Boolean>();

	private String deviceId = null;
	private Integer phoneType = null;
	
	private long startTimestamp;
	
	LinearLayout ll = null;
	ScrollView sv = null;
	LinearLayout hc = null;

	private Handler mHandler = new Handler();
	
	//+++
	private Thread t;
	private CloudCycle cc;

	/**
	 * Callback for ObdGatewayService to update UI.
	 */
	private IPostListener mListener = null;
	private Intent mServiceIntent = null;
	private ObdGatewayServiceConnection mServiceConnection = null;
	
	//+++
	private LocationManager locationManager;

	private SensorManager sensorManager = null;
	private Sensor orientSensor = null;
	private Sensor accelerationSensor = null;
//	private Sensor gravitySensor = null;
	private SharedPreferences prefs = null;

	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakeLock = null;

	private boolean preRequisites = true;

	private String vin = null;
	private Integer speed = null;
	private Double rpm = null;
	private Double maf = null;
	private Float ltft = null;
	private Double equivRatio = null;
	private Integer intManPressVal = null;
	private Integer airIntTempVal = null;
	private float oriX;
	private float oriY;
	private float oriZ;
	private long gpsTimestamp = 0;
	private String gpsNmea = null;
	private long accTimestamp = 0;
	private float accX = 0;
	private float accY = 0;
	private float accZ = 0;
//	private float gravX = 0;
//	private float gravY = 0;
//	private float gravZ = 0;
	
	private boolean accSensAvailable = false;
	
	private double tripFuel = 0;
	private double tripDist = 0;

	//+++
	private final NmeaListener nmeaListener = new NmeaListener() {
		public void onNmeaReceived(long timestamp, String nmea) {
			gpsNmea = nmea.trim();
			gpsTimestamp = timestamp*1000;
			//addTableRow("NMEA", nmea);
		}
	};
	
	private final LocationListener locationListener = new  LocationListener() {
	    public void onLocationChanged(Location location) {}
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
	};
	
	private final SensorEventListener orientListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			oriX = event.values[0];
			oriY = event.values[1];
			oriZ = event.values[2];
			
			float x = event.values[0];
			String dir = "";
			if (x >= 337.5 || x < 22.5) {
				dir = "N";
			} else if (x >= 22.5 && x < 67.5) {
				dir = "NE";
			} else if (x >= 67.5 && x < 112.5) {
				dir = "E";
			} else if (x >= 112.5 && x < 157.5) {
				dir = "SE";
			} else if (x >= 157.5 && x < 202.5) {
				dir = "S";
			} else if (x >= 202.5 && x < 247.5) {
				dir = "SW";
			} else if (x >= 247.5 && x < 292.5) {
				dir = "W";
			} else if (x >= 292.5 && x < 337.5) {
				dir = "NW";
			}
			TextView compass = (TextView) findViewById(R.id.compass_text);
			updateTextView(compass, dir);
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
	};
	
//	float[] gravityV = new float[]{0f,0f,0f};

	//+++
	private final SensorEventListener accelerationListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			accX = event.values[0];//-gravX;
			accY = event.values[1];//-gravY;
			accZ = event.values[2];//-gravZ;
//			final float alpha = 0.8f;
//			//gravity is calculated here
//			gravityV[0] = alpha * gravityV[0] + (1f - alpha) * event.values[0];
//			gravityV[1] = alpha * gravityV[1] + (1f - alpha) * event.values[1];
//			gravityV[2] = alpha * gravityV[2] + (1f - alpha) * event.values[2];
//			//acceleration retrieved from the event and the gravity is removed
//			accX = Math.round((event.values[0] - gravityV[0])*1000)/1000;
//			accY = Math.round((event.values[1] - gravityV[1])*1000)/1000;
//			accZ = Math.round((event.values[2] - gravityV[2])*1000)/1000;
			accTimestamp = (event.timestamp/1000000)+(System.currentTimeMillis() - SystemClock.elapsedRealtime());
//			CloudCycle.getUpload().setAcc(accTimestamp,accX, accY, accZ);
//			addTableRow("ACC", accX+"x"+accY+"x"+accZ);
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int acc) {
		}
	};

//	private final SensorEventListener gravityListener = new SensorEventListener() {
//		@Override
//		public void onSensorChanged(SensorEvent event) {
//			gravX = event.values[0];
//			gravY = event.values[1];
//			gravZ = event.values[2];
//		}
//		@Override
//		public void onAccuracyChanged(Sensor sensor, int acc) {
//		}
//	};

	public void updateTextView(final TextView view, final String txt) {
		new Handler().post(new Runnable() {
			public void run() {
				view.setText(txt);
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * TODO clean-up this upload thing
		 * 
		 * ExceptionHandler.register(this,
		 * "http://www.whidbeycleaning.com/droid/server.php");
		 */
		setContentView(R.layout.main);

		mListener = new IPostListener() {
			public void stateUpdate(ObdCommandJob job) {
				try {
				if(job.getState() != ObdCommandJobState.FINISHED) return;
				String cmdCommand = job.getCommand().getCommand().trim();
				String cmdName = job.getCommand().getName();
				String cmdResult = job.getCommand().getFormattedResult();
				List<Byte> cmdData = job.getCommand().getBuff();
				if(cmdData.size() == 0 || cmdResult.length() == 0) return;

//				Log.d(TAG, FuelTrim.LONG_TERM_BANK_1.getBank() + " equals " + cmdName + "?");
				
				if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName)) {
					TextView tvRpm = (TextView) findViewById(R.id.rpm_text);
					tvRpm.setText(cmdResult);
					if(!"NODATA".equals(cmdResult))
						rpm = ((EngineRPMObdCommand) job.getCommand()).getValue();
					else rpm = null;
				} else if (AvailableCommandNames.SPEED.getValue().equals(
						cmdName)) {
					TextView tvSpeed = (TextView) findViewById(R.id.spd_text);
					tvSpeed.setText(cmdResult);
					if(!"NODATA".equals(cmdResult))
						speed = ((SpeedObdCommand) job.getCommand()).getValue();
					else speed = null;
				} else if (AvailableCommandNames.MAF.getValue().equals(cmdName)) {
					if(!"NODATA".equals(cmdResult))
						maf = ((MassAirFlowObdCommand) job.getCommand()).getValue();
					else maf = null;
					//addTableRow(cmdName, cmdResult);
//				} else if (FuelTrim.LONG_TERM_BANK_1.getBank().equals(cmdName)) {
//					try {
//					if(!"NODATA".equals(cmdResult))
//						ltft = ((FuelTrimObdCommand) job.getCommand()).getValue();
//					else ltft = null;
//					} catch(Exception eee) {
//						String x = "";
//						for(byte val : cmdData) {
//							x += (char)((int)val);
//						}
//						addTableRow("ERRX","["+cmdResult+"]");
//					}
				} else if (AvailableCommandNames.FUEL_LEVEL.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						((FuelGaugeView) findViewById(R.id.fuel_gauge)).setFuel(((FuelLevelObdCommand) job.getCommand()).getValue());
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				} else if (AvailableCommandNames.INTAKE_MANIFOLD_PRESSURE.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						intManPressVal = ((IntakeManifoldPressureObdCommand) job.getCommand()).getValue();
					else intManPressVal =  null;
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				} else if (AvailableCommandNames.AIR_INTAKE_TEMP.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						airIntTempVal = ((AirIntakeTemperatureObdCommand) job.getCommand()).getValue();
					else airIntTempVal =  null;
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				} else if (AvailableCommandNames.EQUIV_RATIO.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						equivRatio = ((CommandEquivRatioObdCommand) job.getCommand()).getRatio();
					else equivRatio = null;
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				//+++
				} else if (AvailableCommandNames.AMBIENT_AIR_TEMP.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						((TextView) findViewById(R.id.air_temp_text)).setText(cmdResult);
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				//+++
				} else if (AvailableCommandNames.ENGINE_COOLANT_TEMP.getValue().equals(cmdName)) {
					try {
					if(!"NODATA".equals(cmdResult))
						((CoolantGaugeView) findViewById(R.id.coolant_gauge)).setTemp(((EngineCoolantTemperatureObdCommand) job.getCommand()).getValue());
//					addTableRow(cmdName, cmdResult);
					} catch(Exception eee) {}
				//+++
				} else if (AvailableCommandNames.VIN.getValue().equals(cmdName)) {
					//lock full (17-character) VIN when it appears, otherwise reject
					if(!"NODATA".equals(cmdResult) && ((VinObdCommand) job.getCommand()).getFormattedResult().length() == 17)
						vin = ((VinObdCommand) job.getCommand()).getFormattedResult();
//					addTableRow("ALL",CloudCycle.getUpload().getUploadMessage());
//					addTableRow("ACC:", new Date(accTimestamp) +"/"+Math.round(accX*512/(1.5*SensorManager.STANDARD_GRAVITY)) +"x"+Math.round(accY*512/(1.5*SensorManager.STANDARD_GRAVITY))+"x"+Math.round(accZ*512/(1.5*SensorManager.STANDARD_GRAVITY)));
//					addTableRow("ELAPSED", String.valueOf(SystemClock.elapsedRealtime()/60000));
//					addTableRow("CURRENT", String.valueOf(new Date(System.currentTimeMillis())));
//					CloudUpload newUpload = CloudCycle.getUpload();
//					if(newUpload != upload) {
//						if(upload != null) addTableRow("SENDING", upload.getUploadMessage());
//						upload = newUpload;
//					}
					//addTableRow(cmdName, cmdResult);
				} else {
//					addTableRow(cmdName, cmdResult);
				}
				//+++???????????????????????????????????????????????
				try {
					if(cmdCommand.startsWith("01 ") && cmdCommand.length() > 3 && ("NODATA".equals(cmdResult) || (cmdData != null && Utilities.getByteArray(cmdData).length <= 4 && Utilities.getByteArray(cmdData).length > 0)))
						if(!"NODATA".equals(cmdResult)) //keep blanks out
							CloudCycle.getUpload().addOBDMessage(new OBDMessage(Utilities.extractPid(cmdCommand),("NODATA".equals(cmdResult)?null:Utilities.getByteArray(cmdData)), (new Date()).getTime() - startTimestamp));
				} catch(Exception ex) {
					//addTableRow(String.valueOf("["+cmdCommand+"]"),ex.getMessage());
				}
				} catch(Exception ex) {
					addTableRow("ERR",ex.getLocalizedMessage());
				}
			}
		};
		TelephonyManager phonyMan = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE));
		deviceId = phonyMan.getDeviceId();
		phoneType = phonyMan.getPhoneType();
		
		/*
		 * Validate GPS service.
		 */
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//+++ replace with composite from GPS coords?

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
			/*
			 * TODO for testing purposes we'll not make GPS a pre-requisite.
			 */
			preRequisites = false;
			showDialog(NO_GPS_ID);
		}

		/*
		 * Validate Bluetooth service.
		 */
		// Bluetooth device exists?
		final BluetoothAdapter mBtAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBtAdapter == null) {
			preRequisites = false;
			showDialog(NO_BLUETOOTH_ID);
		} else {
			// Bluetooth device is enabled?
			if (!mBtAdapter.isEnabled()) {
				preRequisites = false;
				showDialog(BLUETOOTH_DISABLED);
			}
		}

		/*
		 * Get Orientation and Acceleration sensors.
		 */
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		List<Sensor> oriSens = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		if (oriSens.size() == 0) {
			showDialog(NO_ORIENTATION_SENSOR);
		} else {
			orientSensor = oriSens.get(0);
		}
		
		List<Sensor> accSens = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
//		List<Sensor> gravSens = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
		if (accSens.size() == 0/* || gravSens.size() == 0*/) {
//			preRequisites = false;
//			showDialog(NO_ACCELERATION_SENSOR);
		} else {
			accSensAvailable = true;
			accelerationSensor = accSens.get(0);
//			gravitySensor = gravSens.get(0);
		}

		// validate app pre-requisites
		if (preRequisites) {
			/*
			 * Prepare service and its connection
			 */

			mServiceIntent = new Intent(this, ObdGatewayService.class);
			mServiceConnection = new ObdGatewayServiceConnection();
			mServiceConnection.setServiceListener(mListener);

			// bind service
			Log.d(TAG, "Binding service..");
			bindService(mServiceIntent, mServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
		startTimestamp = (new Date()).getTime();
		mHandler.post(mTimerCycle);
		((TextView) findViewById(R.id.fuel_econ_text)).setText("----");
		((TextView) findViewById(R.id.inst_fuel_econ_text)).setText("---l/100km");
		((TextView) findViewById(R.id.rpm_text)).setText("-----");
		((TextView) findViewById(R.id.spd_text)).setText("---");
		((TextView) findViewById(R.id.air_temp_text)).setText("---");
		((CoolantGaugeView) findViewById(R.id.coolant_gauge)).setTemp(0);
		((FuelGaugeView) findViewById(R.id.fuel_gauge)).setFuel(0);

	    sv = (ScrollView) findViewById(R.id.labels);
	    ll = new LinearLayout(this);
	    ll.setOrientation(LinearLayout.VERTICAL);
	    sv.addView(ll);

        hc = (LinearLayout) findViewById(R.id.history_chart);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseWakeLockIfHeld();
		mServiceIntent = null;
		mServiceConnection = null;
		mListener = null;
		mHandler = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Pausing..");
		releaseWakeLockIfHeld();
//		locationManager.removeNmeaListener(nmeaListener);
//		sensorManager.unregisterListener(accelerationListener);
//		sensorManager.unregisterListener(gravityListener);
//		sensorManager.unregisterListener(orientListener);
	}

	/**
	 * If lock is held, release. Lock will be held when the service is running.
	 */
	private void releaseWakeLockIfHeld() {
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	private void acquireWakeLockIfNotHeld() {
		if (!wakeLock.isHeld()) {
			wakeLock.acquire();
		}
	}

	protected void onResume() {
		super.onResume();

		Log.d(TAG, "Resuming..");

		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"ObdReader");
		if(CloudCycle.isCollectionPermitted()) acquireWakeLockIfNotHeld();
		refreshLabelInterface();
//        if(hc.getChildCount() == 0) displayHistory(CHART_CONSUMPTION,false);
	}

	private void updateConfig() {
		Intent configIntent = new Intent(this, ConfigActivity.class);
		startActivity(configIntent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, COLLECT_TOGGLE, 0, "Collect Data");
		menu.add(0, UPLOAD_TOGGLE, 0, "Allow Upload");
		menu.add(0, CLEAR_DATA, 0, "Clear Data");
		menu.add(0, SETTINGS, 0, "Settings");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case COLLECT_TOGGLE:
			toggleLiveData();
			return true;
		case UPLOAD_TOGGLE:
			toggleUpload();
			return true;
		case SETTINGS:
			updateConfig();
			return true;
		case CLEAR_DATA:
			clearData();
			return true;
		}
		return false;
	}

	// private void staticCommand() {
	// Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
	// startActivity(commandIntent);
	// }
	
	private void allowUpload() {
		CloudCycle.setUploadPermitted(true);
		startCycle();
		addTableRow("UPLOAD", "Allowed");
	}
	
	private void stopUpload() {
		CloudCycle.setUploadPermitted(false);
		addTableRow("UPLOAD", "Cut");
	}
	
	private void toggleUpload() {
		if(CloudCycle.isUploadPermitted()) {
			stopUpload();
		} else {
			allowUpload();
		}
	}
	
	private void clearData() {
		((TableLayout) findViewById(R.id.data_table)).removeAllViews();
		addTableRow("CLEARED", String.valueOf(CloudCycle.clearQueue()));
	}
	
	private void setPreferences() {
		CloudCycle.setUploadOnWiFiOnly(ConfigActivity.isUploadOnWiFiOnly(prefs));
		CloudCycle.setEcuBoardNumber(ConfigActivity.getECUBoardNumber(prefs));
		CloudCycle.setRoadLoggerUID(ConfigActivity.getRoadLoggerUID(prefs));
		CloudCycle.setHostname(ConfigActivity.getServerAddress(prefs));
		CloudCycle.setPort(ConfigActivity.getServerPort(prefs));
		CloudCycle.setInterval(ConfigActivity.getUpdatePeriod(prefs));
	}

	private void startCycle() {
		if(t==null) {
			cc = new CloudCycle(
					ConfigActivity.getUpdatePeriod(prefs)
					, ConfigActivity.getServerAddress(prefs)
					, ConfigActivity.getServerPort(prefs)
					, ConfigActivity.getRoadLoggerUID(prefs)
					, ConfigActivity.getECUBoardNumber(prefs)
					, ConfigActivity.isUploadOnWiFiOnly(prefs)
					, 1
					, new boolean[]{true,true,true,false,true}
					, deviceId
					, phoneType
					, getApplicationContext());
			if(CloudCycle.getQueueSize() > 0) {
				addTableRow("FOUND",String.valueOf(CloudCycle.getQueueSize()));
			}
			mHandler.post(mMessageCycle);
			(t = new Thread(cc, "CLOUDTHINK_COMM_CYCLE")).start();
		} else {
			setPreferences();
		}
	}

	private void startLiveData() {
		Log.d(TAG, "Starting live data..");
		//+++
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.addNmeaListener(nmeaListener);
		if(accSensAvailable) {
			sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
//			sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		sensorManager.registerListener(orientListener, orientSensor, SensorManager.SENSOR_DELAY_NORMAL);
		CloudCycle.setCollectionPermitted(true);
		startCycle();
		if (!mServiceConnection.isRunning()) {
			Log.d(TAG, "Service is not running. Going to start it..");
			startService(mServiceIntent);
		}
		addTableRow("COLLECT","Active");
		// start command execution
		mHandler.post(mQueueCommands);
		// screen won't turn off until wakeLock.release()
		acquireWakeLockIfNotHeld();
	}

	private void stopLiveData() {
		Log.d(TAG, "Stopping live data..");

		//+++
		locationManager.removeNmeaListener(nmeaListener);
		sensorManager.unregisterListener(accelerationListener);
//		sensorManager.unregisterListener(gravityListener);
		sensorManager.unregisterListener(orientListener);
		CloudCycle.setCollectionPermitted(false);
		addTableRow("COLLECT","Inactive");

//		if (mServiceConnection.isRunning())
//			stopService(mServiceIntent);

		// remove runnable
		mHandler.removeCallbacks(mQueueCommands);
		maf = null;intManPressVal = null;airIntTempVal=null;
		((TextView) findViewById(R.id.fuel_econ_text)).setText("----");
		((TextView) findViewById(R.id.rpm_text)).setText("-----");
		((TextView) findViewById(R.id.spd_text)).setText("---");
		((TextView) findViewById(R.id.air_temp_text)).setText("---");
		((CoolantGaugeView) findViewById(R.id.coolant_gauge)).setTemp(0);
		((FuelGaugeView) findViewById(R.id.fuel_gauge)).setFuel(0);

		releaseWakeLockIfHeld();
	}

	private void toggleLiveData() {
		if(CloudCycle.isCollectionPermitted()) {
			stopLiveData();
		} else {
			startLiveData();
		}
	}

	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		switch (id) {
		case NO_BLUETOOTH_ID:
			build.setMessage("Sorry, your device doesn't support Bluetooth.");
			return build.create();
		case BLUETOOTH_DISABLED:
			build.setMessage("You have Bluetooth disabled. Please enable it!");
			return build.create();
		case NO_GPS_ID:
			build.setMessage("Sorry, your device doesn't support GPS.");
			return build.create();
		case NO_ORIENTATION_SENSOR:
			build.setMessage("Orientation sensor missing?");
			return build.create();
		case NO_ACCELERATION_SENSOR:
			build.setMessage("Acceleration sensor missing?");
			return build.create();
		}
		return null;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem collectItem = menu.findItem(COLLECT_TOGGLE);
		MenuItem settingsItem = menu.findItem(SETTINGS);
		MenuItem clearItem = menu.findItem(CLEAR_DATA);
		MenuItem uploadItem = menu.findItem(UPLOAD_TOGGLE);

		// validate if preRequisites are satisfied.
		if (preRequisites) {
			collectItem.setEnabled(true);
			collectItem.setTitle(CloudCycle.isCollectionPermitted()?"Stop Collecting":"Collect Data");
			settingsItem.setEnabled(!CloudCycle.isCollectionPermitted() && !CloudCycle.isUploadPermitted());
		} else {
			collectItem.setEnabled(false);
			collectItem.setTitle("Collect Data");
			settingsItem.setEnabled(false);
		}
		clearItem.setTitle(CloudCycle.getQueueSize() > 0?"Clear Cache":"Cache Empty");
		clearItem.setEnabled(CloudCycle.getQueueSize() > 0);
		
		uploadItem.setTitle(CloudCycle.isUploadPermitted()?"Cut Upload":"Allow Upload");
//		addTableRow(">>>",preRequisites+"/"+mServiceConnection.isRunning()+"/"+CloudCycle.isUploadPermitted()+"/"+CloudCycle.isCollectionPermitted());

		return true;
	}

	//+++added to hack the table for messages
	public void debugMessage(String key, String val) {
		addTableRow(key, val);
	}
	public void onBackPressed(){
//		  stopUpload();
//		  stopLiveData();
//		  super.onBackPressed();
	}
	private void addTableRow(String key, String val) {
		TableLayout tl = (TableLayout) findViewById(R.id.data_table);
		TableRow tr = new TableRow(this);
		MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
				TABLE_ROW_MARGIN);
		tr.setLayoutParams(params);
		tr.setBackgroundColor(Color.BLACK);
		TextView name = new TextView(this);
		name.setGravity(Gravity.RIGHT);
		name.setText(key + ": ");
		TextView value = new TextView(this);
		value.setGravity(Gravity.LEFT);
		value.setText(val);
		tr.addView(name);
		tr.addView(value);
		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		/*
		 * TODO remove this hack
		 * 
		 * let's define a limit number of rows
		 */
		if (tl.getChildCount() > 4)
			tl.removeViewAt(0);
	}

	private Runnable mTimerCycle = new Runnable(){
		public void run() {
			long timeSinceStart = ((new Date()).getTime()-startTimestamp);
			((TextView) findViewById(R.id.run_time_text)).setText(Utilities.formatTime(timeSinceStart));
			mHandler.postDelayed(mTimerCycle, 1000);
		}
	};
	
	private void refreshLabelInterface() {
		ll.removeAllViews();
		labelRegister.clear();
		String labels = ConfigActivity.getLabels(prefs).trim();
		String[] labelList;
		if(labels.length() == 0) {
			labelList = new String[0];
		} else {
			labelList = labels.split("[,]");
		}
		if(labelList.length == 0) {
			sv.setMinimumHeight(0);
		} else {
			sv.setMinimumHeight(180);
		}
		for(String label : labelList) {
            CheckBox cb = new CheckBox(getApplicationContext());
            label = label.trim();
            cb.setText(label);
            cb.setChecked(false);
            labelRegister.put(label,false);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						labelRegister.put(buttonView.getText().toString(),true);
					} else {
						labelRegister.put(buttonView.getText().toString(),false);
					}
				}
			});
            ll.addView(cb);
		}
	}
	
	private Animation getSlide(boolean toRight) {
		Animation translateAnimation = new TranslateAnimation(0, (toRight?720:-720), 0, 0);
		translateAnimation.setRepeatMode(0);
		translateAnimation.setDuration(500);
		translateAnimation.setFillAfter(true);
		return translateAnimation;
	}
	
	private double[] getHistory(int chartType) {
		double[] data = chartType == CHART_CONSUMPTION? new double[]{10,5,4,6,7,6,5}:new double[]{4,3,2,2,6,4,3};
		return data;
	}
	
	private void displayHistory(int chartType, boolean toRight) {
		double[] dailyData = getHistory(chartType);
		if(hc.getChildCount() > 0) hc.getChildAt(0).startAnimation(getSlide(toRight));
		hc.removeAllViews();
		GraphViewSeries dataSeries = new GraphViewSeries(new GraphViewData[]{});
		double max = 0;
		dataSeries = new GraphViewSeries(
				new GraphViewData[] {
                new GraphViewData(1, dailyData[0])  
                , new GraphViewData(2, dailyData[1])  
                , new GraphViewData(3, dailyData[2])
                , new GraphViewData(4, dailyData[3])
                , new GraphViewData(5, dailyData[4])
                , new GraphViewData(6, dailyData[5])
                , new GraphViewData(7, dailyData[6])
				}
		);
		for(double i : dailyData) {
			if(i > max) max = i;
		}
		String chartTitle = "Stats";
		if(chartType == CHART_CONSUMPTION) {
			chartTitle = "Fuel Consumption (L)";
			dataSeries.getStyle().setValueDependentColor(new ValueDependentColor() {
				public int get(GraphViewDataInterface data) {
					return Color.rgb(204, 102, 0);
				}
			});
		} else if (chartType == CHART_CARBON) {
			chartTitle = "Fuel Efficiency (L/100KM)";
			dataSeries.getStyle().setValueDependentColor(new ValueDependentColor() {
				public int get(GraphViewDataInterface data) {
					return Color.rgb(102, 204, 0);
				}
			});
		}
        GraphView graphView = new BarGraphView(  
        		this // context  
        		, chartTitle // heading  
        );
        graphView.addSeries(dataSeries); // data
        graphView.setHorizontalLabels(new String[]{"U","M","T","W","R","F","S"});
        graphView.getGraphViewStyle().setNumVerticalLabels(5);
        graphView.getGraphViewStyle().setVerticalLabelsWidth(70);
        graphView.setManualYAxisBounds(Math.ceil(max), 0);
        hc.addView(graphView);
	}

//	private static int msg = 0;
	private Runnable mMessageCycle = new Runnable(){
		final double mmAir = 28.97;
		final double rVal = 8.314;
		final double gasGramsPerLitre = 728.3068;//10594.0826431;
		final double stoichometricMixture = 14.7;
		final static float prec = 100;
		
		public void run() {
			double volEff = ConfigActivity.getVolumetricEfficieny(prefs);
			double engDispLitres = ConfigActivity.getEngineDisplacement(prefs);
			
				CloudCycle.getUpload().setVin(vin);
				CloudCycle.getUpload().setStatusTimestamp((new Date()).getTime() - startTimestamp);
				if(accSensAvailable) CloudCycle.getUpload().setAcc(accTimestamp - startTimestamp,accX, accY, accZ);
				CloudCycle.getUpload().setGps(gpsTimestamp - startTimestamp,gpsNmea);
				CloudCycle.getUpload().setLabels(labelRegister);
				addTableRow("------GPS", (new SimpleDateFormat("yyMMddHHmm",Locale.US)).format(new Date(gpsTimestamp)) + "/" + gpsNmea);
				if(accSensAvailable) addTableRow("ACC", (new SimpleDateFormat("yyMMddHHmm",Locale.US)).format(new Date(accTimestamp)) + "/" + (float)Math.round(accX*prec)/prec +"x"+(float)Math.round(accY*prec)/prec+"x"+(float)Math.round(accZ*prec)/prec);
				addTableRow("VIN", vin);
				List<String> cloudMessages = CloudCycle.getMessages();
				if(!(CloudCycle.isUploadPermitted() && Utilities.isOnline(getApplicationContext())) && CloudCycle.getQueueSize() > 0) {
					addTableRow("STORED",String.valueOf(CloudCycle.getQueueSize()));
				}
				if(cloudMessages.size() > 0) {
					for(String cloudMessage: cloudMessages) {
						addTableRow("XFER",cloudMessage);
					}
				}
				TextView tvMpg = (TextView) findViewById(R.id.fuel_econ_text);
				if(rpm != null && speed != null && speed > 0/* && equivRatio != null*/ && ((intManPressVal != null && airIntTempVal != null) || maf != null)) {
					double airGrams = 0;
					if(maf != null) {
						airGrams = maf;
					} else {
						double imap = (rpm * ((double)intManPressVal)) / ((((double)airIntTempVal)+273.15) * 2);
						airGrams = imap/60 * volEff * engDispLitres * mmAir/rVal;
	//					tvMpg.setText(String.valueOf((Math.round(litresFuel*100)/100)+"|"+(Math.round(distanceKm/100)*100)));
					}
					double litresFuel = airGrams/((equivRatio==null?1.0:equivRatio)*stoichometricMixture*gasGramsPerLitre);
					double distanceKm = ((double)speed)/3600;
					double lp100km = ((double)Math.round(litresFuel*1000/distanceKm))/10;
					if(lp100km <= ConfigActivity.getMaxFuelEconomy(prefs)) {
						if(lp100km>=100)
							tvMpg.setText(String.valueOf((int)Math.round(lp100km)));
						else
							tvMpg.setText(String.valueOf(lp100km));
						tripFuel += litresFuel*CloudCycle.getInterval()/2000;
						tripDist += distanceKm*CloudCycle.getInterval()/2000;
					} else {
						tvMpg.setText("----");
					}
					if(tripDist > 0) {
						((TextView) findViewById(R.id.inst_fuel_econ_text)).setText(String.valueOf(((double)Math.round(tripFuel*1000/tripDist))/10)+"l/100km");
					}
				} else {
					tvMpg.setText("----");
				}
//				addTableRow("VARS", rpm+"|"+speed+"|"/*+volEff+"|"+engDispLitres+"|"*/+intManPressVal+"|"+equivRatio+"|"+airIntTempVal+"|"+maf);
				
				mHandler.postDelayed(mMessageCycle, CloudCycle.getInterval()/2);
		}
	};
	/**
	 * 
	 */
	private Runnable mQueueCommands = new Runnable() {
		public void run() {
			/*
			 * If values are not default, then we have values to calculate MPG
			 */
//			Log.d(TAG, "SPD:" + speed + ", MAF:" + maf + ", LTFT:" + ltft);
//			if (speed != null && speed > 1
//					&& maf != null && maf > 1
//					&& ltft != null && ltft != 0) {
//				FuelEconomyWithMAFObdCommand fuelEconCmd = new FuelEconomyWithMAFObdCommand(
//						FuelType.DIESEL, speed, maf, ltft, false /* TODO */);
//				TextView tvMpg = (TextView) findViewById(R.id.fuel_econ_text);
//				String liters100km = String.format("%.2f", fuelEconCmd.getLitersPer100Km());
//				tvMpg.setText("" + liters100km);
//				Log.d(TAG, "FUELECON:" + liters100km);
//			}
			
			
			if (mServiceConnection.isRunning())
				queueCommands();

			// run again
			mHandler.postDelayed(mQueueCommands, CloudCycle.getInterval()/2);
		}
	};

	/**
	 * 
	 */
	private void queueCommands() {
		final ObdCommandJob airTemp = new ObdCommandJob(
				new AmbientAirTemperatureObdCommand());
		final ObdCommandJob vin = new ObdCommandJob(new VinObdCommand()); //+++
		final ObdCommandJob speed = new ObdCommandJob(new SpeedObdCommand());
		final ObdCommandJob fuelEcon = new ObdCommandJob(
				new FuelEconomyObdCommand());
		final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());
		final ObdCommandJob maf = new ObdCommandJob(new MassAirFlowObdCommand());
		final ObdCommandJob fuelLevel = new ObdCommandJob(new FuelLevelObdCommand());
		final ObdCommandJob intManPress = new ObdCommandJob(new IntakeManifoldPressureObdCommand());
		final ObdCommandJob airIntTemp = new ObdCommandJob(new AirIntakeTemperatureObdCommand());
		final ObdCommandJob ambiAirTemp = new ObdCommandJob(new AmbientAirTemperatureObdCommand());
		final ObdCommandJob coolantTemp = new ObdCommandJob(new EngineCoolantTemperatureObdCommand());
		final ObdCommandJob engineLoad = new ObdCommandJob(new EngineLoadObdCommand());
//		final ObdCommandJob ltft1 = new ObdCommandJob(new FuelTrimObdCommand(
//				FuelTrim.LONG_TERM_BANK_1));
//		final ObdCommandJob ltft2 = new ObdCommandJob(new FuelTrimObdCommand(
//				FuelTrim.LONG_TERM_BANK_2));
//		final ObdCommandJob stft1 = new ObdCommandJob(new FuelTrimObdCommand(
//				FuelTrim.SHORT_TERM_BANK_1));
//		final ObdCommandJob stft2 = new ObdCommandJob(new FuelTrimObdCommand(
//				FuelTrim.SHORT_TERM_BANK_2));
		final ObdCommandJob equiv = new ObdCommandJob(new CommandEquivRatioObdCommand());

		// mServiceConnection.addJobToQueue(airTemp);
		mServiceConnection.addJobToQueue(vin); //+++
		mServiceConnection.addJobToQueue(speed);
		// mServiceConnection.addJobToQueue(fuelEcon);
		mServiceConnection.addJobToQueue(rpm);
		mServiceConnection.addJobToQueue(maf);
		mServiceConnection.addJobToQueue(fuelLevel);
		mServiceConnection.addJobToQueue(intManPress);
		mServiceConnection.addJobToQueue(airIntTemp);
		mServiceConnection.addJobToQueue(equiv);
		mServiceConnection.addJobToQueue(ambiAirTemp);
		mServiceConnection.addJobToQueue(coolantTemp);
		mServiceConnection.addJobToQueue(engineLoad);
//		mServiceConnection.addJobToQueue(ltft1);
		// mServiceConnection.addJobToQueue(ltft2);
		// mServiceConnection.addJobToQueue(stft1);
		// mServiceConnection.addJobToQueue(stft2);
	}
}