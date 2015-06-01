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
package org.bfr.periodicquery.sdr;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

/**
 * 
 * This is a Java wrapper around the native rtl_power program. It uses the RootTools library to call 
 * the program inside a root shell.
 *
 */
public class SdrSpectrumSensing
{

	private static Command command;
	private static Shell rootShell;

	public static void sense()
	{
		Log.d("SpectrumQuery", "rtlsdr-query-start");
		Log.d("SpectrumQuery", "rtlsdr-query-execute %.4f %.4f");
		
		try
		{
			rootShell = Shell.startNewRootShell(20000, 3);
			
			// Clear logcat buffer
			command = new Command(1,  "/data/local/tmp/rtl_power -f 118M:120M:8k  /sdcard/airband.txt" )
			{

				@Override
				public void commandOutput(int id, String line)
				{
					Log.d("SpectrumQuery", "rtlsdr-query-output " + line);
				}

				@Override
				public void commandTerminated(int id, String reason)
				{
					Log.d("SpectrumQuery", "rtlsdr-error " + reason);
				}

				@Override
				public void commandCompleted(int id, int exitCode)
				{
					Log.d("SpectrumQuery", "rtlsdr-query-done " + exitCode);
				}
				
			};
			
			rootShell.add(command);
			
		} catch (IOException e)
		{
			Log.d("SpectrumQuery", "rtlsdr-error i/o exception: " + e.getMessage());
			e.printStackTrace();
		} catch (TimeoutException e)
		{
			Log.d("SpectrumQuery", "rtlsdr-error time-out: " + e.getMessage());
			e.printStackTrace();
		} catch (RootDeniedException e)
		{
			Log.d("SpectrumQuery", "rtlsdr-error root denied");
			e.printStackTrace();
		} 
		
		Log.d("SpectrumQuery", "rtlsdr-query-done");
		
	}
	
}
