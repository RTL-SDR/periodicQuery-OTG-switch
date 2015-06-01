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

import org.bfr.periodicquery.PeriodicQueryApplication.Location;
import org.bfr.periodicquery.PeriodicQueryApplication.LocationFuzzing;
import org.bfr.periodicquery.PeriodicQueryApplication.SpectrumSource;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class StartStopActivity extends Activity
{
	
	// enable switch
    ToggleButton btnEnable;
    Button btnCheck;
    TextView textInfo;

    private Audio audio;
	
	

	private PeriodicQueryService service = null;
	
	private PeriodicQueryApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_stop);
		
		// Start the service, if it is not already running.
		Intent intent = new Intent(this, PeriodicQueryService.class);
		startService(intent);
		
		// Get the application object
		application = (PeriodicQueryApplication)getApplication();
		
		
		// enable switch
	        audio = new Audio();
	        audio.waveform = Audio.SINE;
	        audio.frequency = 15000.0;
	        audio.level = 1.0;
	        audio.mute = true;
		
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		// Bind service
		bindService(new Intent(this, PeriodicQueryService.class), serviceConnection, BIND_AUTO_CREATE);

		// Reflect the status of the app in the ui (enable/disable buttons, select radio buttons, etc.)
		updateUI();
	}
	
	private void updateUI()
	{
		// Enable/disable start and stop buttons
		if (service!=null)
		{
			((Button)findViewById(R.id.button_start)).setEnabled(!service.isActive());
			((Button)findViewById(R.id.button_stop)).setEnabled(service.isActive());
		}
		
		// Select the correct service selection radio button based on the current setting in the application object
		((RadioButton)findViewById(R.id.radio_google)).setChecked(
				application.getSpectrumSource()==PeriodicQueryApplication.SpectrumSource.Google);
		((RadioButton)findViewById(R.id.radio_microsoft)).setChecked(
				application.getSpectrumSource()==PeriodicQueryApplication.SpectrumSource.Microsoft);
		((RadioButton)findViewById(R.id.rtl_sdr)).setChecked(
				application.getSpectrumSource()==PeriodicQueryApplication.SpectrumSource.RtlSdr);
		
		// Select the correct query interval radio button based on the current setting in the application object
		((RadioButton)findViewById(R.id.radio_10s)).setChecked(application.getQueryInterval()==10);
		((RadioButton)findViewById(R.id.radio_30s)).setChecked(application.getQueryInterval()==30);
		((RadioButton)findViewById(R.id.radio_1m)).setChecked(application.getQueryInterval()==60);
		((RadioButton)findViewById(R.id.radio_2m)).setChecked(application.getQueryInterval()==120);
		((RadioButton)findViewById(R.id.radio_5m)).setChecked(application.getQueryInterval()==300);
		
		// Select the correct location radio button
		((RadioButton)findViewById(R.id.radio_ny)).setChecked(application.getLocation()==Location.NewYork);
		((RadioButton)findViewById(R.id.radio_ohio)).setChecked(application.getLocation()==Location.Ohio);
		
		// Select the correct location fuzzing radio button
		((RadioButton)findViewById(R.id.radio_nofuzz)).setChecked(application.getLocationFuzzing()==LocationFuzzing.Off);
		((RadioButton)findViewById(R.id.radio_fuzz1)).setChecked(application.getLocationFuzzing()==LocationFuzzing.UniformSquare);
		
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		unbindService(serviceConnection);
	}

	public void onStart(View view)
	{
		
		service.start();
		
		updateUI();
		RadioButton rb =  (RadioButton)findViewById(R.id.rtl_sdr);
		if ( rb.isChecked() ){
        audio.start();
        audio.mute = false;
		}
		
	}

	public void onStop(View view)
	{
		
		RadioButton rb =  (RadioButton)findViewById(R.id.rtl_sdr);
		if ( rb.isChecked() ){
			service.stop();
			audio.stop();
		}
		

		updateUI();
	}

	/**
	 * Called when the user presses one of the radio buttons under the 'service' section 
	 * @param view RadioButton that fired the event
	 */
	public void onServiceSelect(View view)
	{
		RadioButton radio = (RadioButton)view;
		
		SpectrumSource provider = SpectrumSource.valueOf(radio.getText().toString());
		application.setSpectrumSource(provider);
	}

	/**
	 * Called when the user presses one of the radio buttons under the 'Query Interval' section 
	 * @param view RadioButton that fired the event
	 */
	public void onIntervalSelect(View view)
	{
		RadioButton radio = (RadioButton)view;
		String text = radio.getText().toString();
		
		if ("10s".equals(text)) application.setQueryInterval(10);
		if ("30s".equals(text)) application.setQueryInterval(30);
		if ("1m".equals(text)) application.setQueryInterval(60);
		if ("2m".equals(text)) application.setQueryInterval(120);
		if ("5m".equals(text)) application.setQueryInterval(300);
		
	}

	/**
	 * Called when the user presses one of the radio buttons under the 'Location' section 
	 * @param view RadioButton that fired the event
	 */
	public void onLocationSelect(View view)
	{
		if (view.getId()==R.id.radio_ny)
			application.setLocation(Location.NewYork);

		if (view.getId()==R.id.radio_ohio)
			application.setLocation(Location.Ohio);
	}

	/**
	 * Called when the user presses one of the radio buttons under the 'Location' section 
	 * @param view RadioButton that fired the event
	 */
	public void onLocationFuzzSelect(View view)
	{
		if (view.getId()==R.id.radio_nofuzz)
			application.setLocationFuzzing(LocationFuzzing.Off);

		if (view.getId()==R.id.radio_fuzz1)
			application.setLocationFuzzing(LocationFuzzing.UniformSquare);
	}

	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder)
		{
			service = ((PeriodicQueryServiceBinder)binder).getService();
			
			updateUI();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			service = null;
		}
		
	};

	
	
	
	

    /**
     * Audio class from `sig-gen` project. (GPLv3 license)
     *
     * url: https://github.com/billthefarmer/sig-gen
     */
    protected class Audio implements Runnable
    {
        protected static final int SINE = 0;
        protected static final int SQUARE = 1;
        protected static final int SAWTOOTH = 2;

        protected int waveform;
        protected boolean mute;

        protected double frequency;
        protected double level;

        protected Thread thread;

        private AudioTrack audioTrack;

        protected Audio()
        {
            frequency = 440.0;
            level = 16384;
            mute = true;
        }

        // Start

        protected void start()
        {
            thread = new Thread(this, "Audio");
            thread.start();
        }

        // Stop

        protected void stop()
        {
            Thread t = thread;
            thread = null;

            // Wait for the thread to exit

            while (t != null && t.isAlive())
                Thread.yield();
        }

        public void run()
        {
            processAudio();
        }

        // Process audio

        protected void processAudio()
        {
            short buffer[];

            int rate =
                    AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
            int minSize =
                    AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

            // Find a suitable buffer size

            int sizes[] = {1024, 2048, 4096, 8192, 16384, 32768};
            int size = 0;

            for (int s: sizes)
            {
                if (s > minSize)
                {
                    size = s;
                    break;
                }
            }

            final double K = 2.0 * Math.PI / rate;

            // Create the audio track

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    size, AudioTrack.MODE_STREAM);
            // Check audioTrack

            if (audioTrack == null)
                return;

            // Check state

            int state = audioTrack.getState();

            if (state != AudioTrack.STATE_INITIALIZED)
            {
                audioTrack.release();
                return;
            }

            audioTrack.play();

            // Create the buffer

            buffer = new short[size];

            // Initialise the generator variables

            double f = frequency;
            double l = 0.0;
            double q = 0.0;

            while (thread != null)
            {
                // Fill the current buffer

                for (int i = 0; i < buffer.length; i++)
                {
                    f += (frequency - f) / 4096.0;
                    l += ((mute? 0.0 : level) * 16384.0 - l) / 4096.0;
                    q += (q < Math.PI)? f * K: (f * K) - (2.0 * Math.PI);

                    switch (waveform)
                    {
                        case SINE:
                            buffer[i] = (short) Math.round(Math.sin(q) * l);
                            break;

                        case SQUARE:
                            buffer[i] = (short) ((q > 0.0)? l: -l);
                            break;

                        case SAWTOOTH:
                            buffer[i] = (short) Math.round((q / Math.PI) * l);
                            break;
                    }
                }

                audioTrack.write(buffer, 0, buffer.length);
            }

            audioTrack.stop();
            audioTrack.release();
        }
    }
	
	
	
	
}
