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

import android.app.Application;

/**
 * 
 * State shared between activities and the main service. 
 * 
 */
public class PeriodicQueryApplication extends Application
{
	
	// Spectrum sources
	public enum SpectrumSource
	{
		Google,
		Microsoft,
		SpectrumBridge,
		RtlSdr
	}

	// Pre-defined locations
	public enum Location
	{
		NewYork(40.707472, -74.011222),
		Ohio(41.102884, -82.957361);
		
		public double latitude, longitude;
		
		private Location(double latitude, double longitude)
		{
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
	
	// Location fuzzing options
	public enum LocationFuzzing
	{
		Off,
		UniformSquare
	}
	
	// Spectrum Database Service
	private SpectrumSource spectrumSource = SpectrumSource.Microsoft;
	
	// Querying interval in seconds
	private int queryInterval = 30;
	
	// Pre-defined location
	private Location location = Location.NewYork;
	
	private LocationFuzzing locationFuzzing = LocationFuzzing.Off;

	public SpectrumSource getSpectrumSource()
	{
		return spectrumSource;
	}
	
	public void setSpectrumSource(SpectrumSource spectrumSource)
	{
		this.spectrumSource = spectrumSource;
	}
	
	public int getQueryInterval()
	{
		return queryInterval;
	}
	
	public void setQueryInterval(int queryInterval)
	{
		this.queryInterval = queryInterval;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public void setLocation(Location location)
	{
		this.location = location;
	}
	
	public LocationFuzzing getLocationFuzzing()
	{
		return locationFuzzing;
	}
	
	public void setLocationFuzzing(LocationFuzzing locationFuzzing)
	{
		this.locationFuzzing = locationFuzzing;
	}

}
