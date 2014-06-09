/*
 * TODO put header
 */
package eu.lighthouselabs.obd.reader.activity;

import java.util.ArrayList;
import java.util.Set;

import ae.ac.masdar.labs.stevas.adama.CloudCycle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.reader.R;
import eu.lighthouselabs.obd.reader.config.ObdConfig;
import ae.ac.masdar.labs.stevas.adama.CloudCycle;

/**
 * Configuration activity.
 */
public class ConfigActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
	public static final String COLLECT_DATA_KEY = "collect_data_preference";
	public static final String WIFI_ONLY_KEY = "wifi_only_preference";
	public static final String UPDATE_PERIOD_KEY = "update_period_preference";
	public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
	public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
	public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
	public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
	public static final String ENABLE_GPS_KEY = "enable_gps_preference";
	public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
	public static final String CONFIG_READER_KEY = "reader_config_preference";
	public static final String ADDRESS_PORT_KEY = "cloud_server_address_port";
	public static final String ROADLOGGER_UID_KEY = "roadlogger_uid";
	public static final String LABELS_KEY = "labels";
	public static final String ECU_BOARD_NUM_KEY = "ecu_board_number";
	public static final int ROADLOGGER_UID_LENGTH = 4;
	public static final int ECU_NUMBER_MAX_LENGTH = 12;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Read preferences resources available at res/xml/preferences.xml
		 */
		addPreferencesFromResource(R.xml.preferences);

		ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
		ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
		ListPreference listBtDevices = (ListPreference) getPreferenceScreen()
				.findPreference(BLUETOOTH_LIST_KEY);
		String[] prefKeys = new String[] { ENGINE_DISPLACEMENT_KEY,
				VOLUMETRIC_EFFICIENCY_KEY, UPDATE_PERIOD_KEY, MAX_FUEL_ECON_KEY };
		for (String prefKey : prefKeys) {
			EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
					.findPreference(prefKey);
			txtPref.setOnPreferenceChangeListener(this);
		}

		/*
		 * Available OBD commands
		 * 
		 * TODO This should be read from preferences database
		 */
		ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
		PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
				.findPreference(COMMANDS_SCREEN_KEY);
		for (ObdCommand cmd : cmds) {
			CheckBoxPreference cpref = new CheckBoxPreference(this);
			cpref.setTitle(cmd.getName());
			cpref.setKey(cmd.getName());
			cpref.setChecked(true);
			cmdScr.addPreference(cpref);
		}

		/*
		 * Let's use this device Bluetooth adapter to select which paired OBD-II
		 * compliant device we'll use.
		 */
		final BluetoothAdapter mBtAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBtAdapter == null) {
			listBtDevices.setEntries(pairedDeviceStrings
					.toArray(new CharSequence[0]));
			listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

			// we shouldn't get here, still warn user
			Toast.makeText(this, "This device does not support Bluetooth.",
					Toast.LENGTH_LONG);

			return;
		}

		/*
		 * Listen for preferences click.
		 * 
		 * TODO there are so many repeated validations :-/
		 */
		final Activity thisActivity = this;
		listBtDevices.setEntries(new CharSequence[1]);
		listBtDevices.setEntryValues(new CharSequence[1]);
		listBtDevices
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						// see what I mean in the previous comment?
						if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
							Toast.makeText(
									thisActivity,
									"This device does not support Bluetooth or it is disabled.",
									Toast.LENGTH_LONG);
							return false;
						}
						return true;
					}
				});

		/*
		 * Get paired devices and populate preference list.
		 */
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDeviceStrings.add(device.getName() + "\n"
						+ device.getAddress());
				vals.add(device.getAddress());
			}
		}
		listBtDevices.setEntries(pairedDeviceStrings
				.toArray(new CharSequence[0]));
		listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
	}

	/**
	 * OnPreferenceChangeListener method that will validate a preference new
	 * value when it's changed.
	 * 
	 * @param preference
	 *            the changed preference
	 * @param newValue
	 *            the value to be validated and set if valid
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (UPDATE_PERIOD_KEY.equals(preference.getKey())
				|| VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
				|| ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
				|| UPDATE_PERIOD_KEY.equals(preference.getKey())
				|| MAX_FUEL_ECON_KEY.equals(preference.getKey())) {
			try {
				Double.parseDouble(newValue.toString());
				return true;
			} catch (Exception e) {
				Toast.makeText(
						this,
						"Couldn't parse '" + newValue.toString()
								+ "' as a number.", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		if(ADDRESS_PORT_KEY.equals(preference.getKey()) && !newValue.toString().matches(".+[:]\\d{2,4}")) {
			Toast.makeText(
					this,
					"Couldn't parse '" + newValue.toString()
							+ "' in the 'address:port' format.", Toast.LENGTH_LONG).show();
			return false;
		}
		if(ROADLOGGER_UID_KEY.equals(preference.getKey()) && newValue.toString().length() != ROADLOGGER_UID_LENGTH) {
			Toast.makeText(
					this,
					"The roadlogger UID should be +"+ROADLOGGER_UID_LENGTH+" characters long.", Toast.LENGTH_LONG).show();
			return false;
		}
		if(ECU_BOARD_NUM_KEY.equals(preference.getKey()) && newValue.toString().length() > ECU_NUMBER_MAX_LENGTH) {
			Toast.makeText(
					this,
					"The ECU board number '" + newValue.toString()
							+ "' is too long.", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static int getUpdatePeriod(SharedPreferences prefs) {
		String periodString = prefs.getString(ConfigActivity.UPDATE_PERIOD_KEY,
				"2"); // 2 as in seconds
		int period = 2000; // by default 2000ms

		try {
			period = Integer.parseInt(periodString) * 1000;
		} catch (Exception e) {
		}

		if (period <= 0) {
			period = 250;
		}

		return period;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static double getVolumetricEfficieny(SharedPreferences prefs) {
		String veString = prefs.getString(
				ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, "0.85");
		double ve = 0.85;
		try {
			ve = Double.parseDouble(veString);
		} catch (Exception e) {
		}
		return ve;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static double getEngineDisplacement(SharedPreferences prefs) {
		String edString = prefs.getString(
				ConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
		double ed = 1.6;
		try {
			ed = Double.parseDouble(edString);
		} catch (Exception e) {
		}
		return ed;
	}

	//+++
	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static String getECUBoardNumber(SharedPreferences prefs) {
		String ecuNum = prefs.getString(
				ConfigActivity.ECU_BOARD_NUM_KEY, "");
		return (ecuNum == null || ecuNum.length() == 0)?null:ecuNum;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static String getRoadLoggerUID(SharedPreferences prefs) {
		return prefs.getString(
				ConfigActivity.ROADLOGGER_UID_KEY, "ABCD");
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static String getLabels(SharedPreferences prefs) {
		return prefs.getString(
				ConfigActivity.LABELS_KEY, "");
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static String getServerAddress(SharedPreferences prefs) {
		String addrPortString = prefs.getString(
				ConfigActivity.ADDRESS_PORT_KEY, "23.23.126.78:5625");
		String addr = "23.23.126.78";
		try {
			addr = addrPortString.split("[:]")[0];
		} catch (Exception e) {
		}
		return addr;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static int getServerPort(SharedPreferences prefs) {
		String addrPortString = prefs.getString(
				ConfigActivity.ADDRESS_PORT_KEY, "23.23.126.78:5625");
		int port = 5625;
		try {
			port = Integer.parseInt(addrPortString.split("[:]")[1]);
		} catch (Exception e) {
		}
		return port;
	}
	////+++

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
		ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
		ArrayList<ObdCommand> ucmds = new ArrayList<ObdCommand>();
		for (int i = 0; i < cmds.size(); i++) {
			ObdCommand cmd = cmds.get(i);
			boolean selected = prefs.getBoolean(cmd.getName(), true);
			if (selected) {
				ucmds.add(cmd);
			}
		}
		return ucmds;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static double getMaxFuelEconomy(SharedPreferences prefs) {
		String maxStr = prefs.getString(ConfigActivity.MAX_FUEL_ECON_KEY, "70");
		double max = 70;
		try {
			max = Double.parseDouble(maxStr);
		} catch (Exception e) {
		}
		return max;
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static boolean isUploadOnWiFiOnly(SharedPreferences prefs) {
		return prefs.getBoolean(ConfigActivity.WIFI_ONLY_KEY, false);
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 */
	public static String[] getReaderConfigCommands(SharedPreferences prefs) {
		String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
		String[] cmds = cmdsStr.split("\n");
		return cmds;
	}
	
}