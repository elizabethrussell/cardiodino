/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datadoghealth.heartrace.bluetooth;

import java.util.HashMap;

/*
 * Population of gatt profile
 */
public class GattAttributes {
	private static HashMap<String, String> attributes = new HashMap<String, String>();

	// Zephyr services/characteristics
	public static String GATT_PRIMARY_SERVICE_UUID = "00002800-0000-1000-8000-00805f9b34fb";
	public static String GATT_CHARACTER_UUID = "00002803-0000-1000-8000-00805f9b34fb";
	public static String ACTIVITY_MEAS_UUID = "0000ff11-0000-1000-8000-00805f9b34fb";
	public static String GATT_CLIENT_CHAR_CFG_UUID = "00002902-0000-1000-8000-00805f9b34fb";
	public static String TESTMODE_DATA_UUID = "0000ff12-0000-1000-8000-00805f9b34fb";

    // bluetooth device services/characteristics
    public static String DEVICE_INFO_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_CHAR = "00002a29-0000-1000-8000-00805f9b34fb";

    // heart rate services/characteristics
    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";

    // fitbit services/characteristics
    public static String FITBIT_SERVICE_1 = "adabfb00-6e7d-4601-bda2-bffaa68956ba";
    public static String FITBIT_CHAR_1 = "adabfb04-6e7d-4601-bda2-bffaa68956ba";
    public static String FITBIT_CHAR_2 = "adabfb02-6e7d-4601-bda2-bffaa68956ba";
    public static String FITBIT_CHAR_3 = "adabfb03-6e7d-4601-bda2-bffaa68956ba";
    public static String FITBIT_CHAR_4 = "adabfb01-6e7d-4601-bda2-bffaa68956ba";
    public static String FITBIT_SERVICE_2 = "558dfa00-4fa8-4105-9f02-4eaa93e62980";
    public static String FITBIT_CHAR_5 = "558dfa01-4fa8-4105-9f02-4eaa93e62980";


	static {
		attributes.put(HEART_RATE_SERVICE, "Heart Rate Service");
		attributes.put(DEVICE_INFO_SERVICE, "Device Information Service");
		attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
		attributes.put(MANUFACTURER_NAME_CHAR, "Manufacturer Name String");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}

	public static boolean isHr(String uuid) {
		if (attributes.get(uuid) == "Heart Rate Measurement") {
			return true;
		}
		return false;
	}

	/**
	 * Zephyr's custom UUIDs require first extraction of
	 * 
	 * @param unique_id
	 */
	public void populate(String unique_id) {
		attributes.put("00000000-0000-1000-8000-00805F9B34FB", "Zephyr Custom");
	}

}