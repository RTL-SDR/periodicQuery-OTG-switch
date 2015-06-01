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

import org.bfr.periodicquery.PeriodicQueryApplication.LocationFuzzing;
import org.bfr.periodicquery.PeriodicQueryApplication.SpectrumSource;
import org.bfr.periodicquery.sdr.SdrSpectrumSensing;
import org.bfr.querytools.google.GoogleSpectrumQuery;
import org.bfr.querytools.logging.Logger;
import org.bfr.querytools.msr.MsrSpectrumQuery;
import org.bfr.querytools.spectrumbridge.SpectrumBridgeQuery;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PeriodicQueryService extends Service
{
	
	// Maximum time to keep a wake lock in ms 
	private static long wakeLockTimeOut = 10*1000;
	
	// Binder so Activities can bind this service.
	private final PeriodicQueryServiceBinder binder = new PeriodicQueryServiceBinder(this);

	// Active
	boolean active = false;
	
	// First query of a sequence? Needed for spectrum bridge
	boolean firstQuery = false;
	
	public PeriodicQueryService()
	{
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
		
		Logger.setLogger(new Logger() {

			@Override
			public void logMessage(String message)
			{
				Log.d("SpectrumQuery", message);
			}
			
		});
	}

	public void start()
	{
		if (active)
			return;
		
		// Start polling
		AlarmReceiver.scheduleWakeupFromNow(this, 0);

		// Signal that we're now active
		active = true;
		
		// First query after new start
		firstQuery = true;
	}

	public void stop()
	{
		if (!active)
			return;

		// Signal that we're now deactivated
		active = false;
	}

	public boolean isActive()
	{
		return active;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		AlarmReceiver.setCallBack(pollingCallback);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		
		// The intent to launch when the user clicks the expanded notification
		Intent launchIntent = new Intent(this, StartStopActivity.class);
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

		// This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.ic_launcher, "Periodic Query", System.currentTimeMillis());

		// This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(this, "Periodic Query", "Periodic Query", pendIntent);
		notice.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(1234, notice);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	Runnable pollingCallback = new Runnable()
	{
		
		public void spectrumQuery(PeriodicQueryApplication app)
		{
			// Determine location
			double latitude = app.getLocation().latitude;
			double longitude = app.getLocation().longitude;
			
			// Optionally apply fuzzing
			if (app.getLocationFuzzing()==LocationFuzzing.UniformSquare)
			{
				latitude += Math.random() - 0.5;
				longitude += Math.random() - 0.5;
			}
			
			// Execute the query
			switch(app.getSpectrumSource())
			{
			case Google:
				GoogleSpectrumQuery.query(latitude, longitude);
				break;
			case Microsoft:
				MsrSpectrumQuery.query(latitude, longitude);
				break;
			case SpectrumBridge:
				SpectrumBridgeQuery.query(latitude, longitude, firstQuery);
				break;
			default:
				// none
			}
			
			firstQuery = false;
		}
		
		public void spectrumSense()
		{
			SdrSpectrumSensing.sense();
		}
		
		@Override
		public void run()
		{
			final PeriodicQueryApplication app = (PeriodicQueryApplication)getApplication();
			
			// Schedule next wakeup
			Log.d("SpectrumQuery", "active: " + active);
			
			if (active)
				AlarmReceiver.scheduleWakeupFromNow(PeriodicQueryService.this, app.getQueryInterval() * 1000);
			
			// Get the power manager
			PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
			
			// Create and acquire a wake lock to keep the processor wake
			final WakeLock lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Spectrum Query");
			lock.acquire(wakeLockTimeOut);
			
			new Thread() 
			{
				
				public void run()
				{
					try
					{
						
						if (app.getSpectrumSource()==SpectrumSource.RtlSdr)
							spectrumSense();
						else
							spectrumQuery(app);
				
					} catch (Exception ex)
					{
						ex.printStackTrace();						
					} finally
					{
						if (lock.isHeld())
							lock.release();
					}
				};
				
			}.start();
			
		}

	};
	
}

