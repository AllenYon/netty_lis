package cn.link.lis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ConfigUtils {

	public static String readString(Context ctx, String key, String def) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		return mShared.getString(key, def);
	}

	public static String readString(Context ctx, String key) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		return mShared.getString(key, "");
	}

	public static boolean writeString(Context ctx, String key, String value) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = mShared.edit();
		edit.putString(key, value);
		return edit.commit();
	}

	public static boolean readBoolean(Context ctx, String key, boolean def) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		return mShared.getBoolean(key, def);
	}

	public static boolean readBoolean(Context ctx, String key) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		return mShared.getBoolean(key, false);
	}

	public static boolean writeBoolean(Context ctx, String key, boolean value) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = mShared.edit();
		edit.putBoolean(key, value);
		return edit.commit();
	}

	public static boolean writeLong(Context ctx, String key, long l) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = mShared.edit();
		edit.putLong(key, l);
		return edit.commit();
	}

	public static long readLong(Context ctx, String key) {
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		return mShared.getLong(key, -1l);
	}

	public static void clear(Context ctx) {
		// TODO Auto-generated method stub
		SharedPreferences mShared = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = mShared.edit();
		edit.clear();
		edit.commit();

	}
}
