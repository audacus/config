package ch.audacus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Config {

	private static final int JSON_INDENT_FACTOR = 4;

	private static Map<String, Object> config = null;
	private static File file = null;

	public static void setConfigFile(final String path) {
		Config.file = new File(path);
	}

	public static void readConfig() {
		Config.config = new HashMap<String, Object>();
		// create string from file
		final StringBuilder string = new StringBuilder();
		Scanner scanner;
		try {
			scanner = new Scanner(Config.file);
			while (scanner.hasNext()) {
				string.append(scanner.nextLine());
			}
			scanner.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}

		Config.config = Config.jsonToMap(new JSONObject(string));
	}

	public static Map<String, Object> jsonToMap(final JSONObject object) throws JSONException {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Iterator<String> iterator = object.keys();

		while (iterator.hasNext()) {
			final String key = iterator.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = Config.jsonToList((JSONArray) value);
			} else if (value instanceof JSONObject) {
				value = Config.jsonToMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> jsonToList(final JSONArray array) throws JSONException {
		final List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = Config.jsonToList((JSONArray) value);
			} else if (value instanceof JSONObject) {
				value = Config.jsonToMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static Object get(final String propertyPath) {
		if (Config.config == null) {
			Config.readConfig();
		}
		return Config.findProperty(propertyPath, Config.config, false, null);
	}

	public static Object set(final String propertyPath, final Object value) {
		if (Config.config == null) {
			Config.readConfig();
		}
		return Config.findProperty(propertyPath, Config.config, true, value);
	}

	private static Object findProperty(final String propertyPath, final Map<String, Object> map, final boolean setValue, final Object value) {
		String[] splits;
		Object property = null;
		String propertyName;
		int nextPropertyNameStart;

		splits = propertyPath.split("\\.");
		if (splits.length > 0) {
			propertyName = splits[0];
			nextPropertyNameStart = propertyName.length() + 1;
			property = map.get(propertyName);
			if (property instanceof Map && propertyPath.length() > nextPropertyNameStart) {
				property = Config.findProperty(propertyPath.substring(nextPropertyNameStart), (Map<String, Object>) property, setValue, value);
			} else if (setValue) {
				map.put(propertyName, value == null ? "" : value);
				Config.writeConfig();
				return value;
			}
		}
		return property;
	}

	private static void writeConfig() {
		final JSONObject json = new JSONObject(Config.config);
		try {
			final FileWriter writer = new FileWriter(Config.file);
			writer.write(json.toString(Config.JSON_INDENT_FACTOR));
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
