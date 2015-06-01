/**
	Copyright 2014 [BFR]
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
**/
package org.bfr.periodicquery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver
{
	
	// Wake-up action
	private static String WakeUpAction = "WAKEUP";

	// Runnable to call when the alarm expires 
	private static Runnable callBack;
	
	public AlarmReceiver()
	{
	}
	
	public static void setCallBack(Runnable callBack)
	{
		AlarmReceiver.callBack = callBack;
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d("SpectrumQuery", "alarm fired");
		runCallBack(context);		
	}
	
	private static void runCallBack(Context context)
	{
		// Cancel intent
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(WakeUpAction);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.cancel(sender);
		
		// Run call-back
		if (callBack!=null)
			callBack.run();
	}
	
	public static void scheduleWakeupFromNow(Context context, long time)
	{
		Log.d("SpectrumQuery", "scheduling for: " + time + "ms into the future");
		scheduleWakeup(context, System.currentTimeMillis() + time);
	}

	public static void scheduleWakeup(Context context, long time)
	{
		// Get the AlarmManager service
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		// Create a wake-up event for the target time
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(WakeUpAction);

		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, sender);
	}

}