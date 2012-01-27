package net.mitchtech.adb;

import java.io.IOException;

import net.mitchtech.adb.simpleanalogoutput.R;

import org.microbridge.server.Server;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SimpleAnalogOutputActivity extends Activity implements OnSeekBarChangeListener {

	public static final String TAG = SimpleAnalogOutputActivity.class.getSimpleName();

	final int DELAY = 150;

	SeekBar mRedSeekBar, mGreenSeekBar, mBlueColorBar;
	View mColorIndicator;

	SharedPreferences mPrefs;
	Server mServer = null;

	int mRedState, mGreenState, mBlueState;
	long mLastChange;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mRedSeekBar = (SeekBar) findViewById(R.id.SeekBarRed);
		mGreenSeekBar = (SeekBar) findViewById(R.id.SeekBarGreen);
		mBlueColorBar = (SeekBar) findViewById(R.id.SeekBarBlue);

		mColorIndicator = findViewById(R.id.ColorIndicator);

		mRedSeekBar.setOnSeekBarChangeListener(this);
		mGreenSeekBar.setOnSeekBarChangeListener(this);
		mBlueColorBar.setOnSeekBarChangeListener(this);

		// load last state
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mRedState = mPrefs.getInt("red", 0);
		mGreenState = mPrefs.getInt("green", 0);
		mBlueState = mPrefs.getInt("blue", 0);

		// set seekbars and feedback color according to last state
		mRedSeekBar.setProgress(mRedState);
		mGreenSeekBar.setProgress(mGreenState);
		mBlueColorBar.setProgress(mBlueState);
		mColorIndicator.setBackgroundColor(Color.rgb(mRedState, mGreenState, mBlueState));

		// Create new TCP Server
		try {
			mServer = new Server(4567);
			mServer.start();
		} catch (IOException e) {
			Log.e("microbridge", "Unable to start TCP server", e);
			System.exit(-1);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// save state
		mPrefs.edit().putInt("red", mRedState).putInt("green", mGreenState).putInt("blue", mBlueState).commit();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - mLastChange > DELAY) {
			updateState(seekBar);
			mLastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mLastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {

		switch (seekBar.getId()) {
		case R.id.SeekBarRed:
			mRedState = seekBar.getProgress();
			break;
		case R.id.SeekBarGreen:
			mGreenState = seekBar.getProgress();
			break;
		case R.id.SeekBarBlue:
			mBlueState = seekBar.getProgress();
			break;
		}
		updateAllColors();

		// provide user feedback
		mColorIndicator.setBackgroundColor(Color.rgb(mRedState, mGreenState, mBlueState));
	}

	private void updateAllColors() {
		try {
			mServer.send(new byte[] { (byte) mRedState, (byte) mGreenState, (byte) mBlueState });
		} catch (IOException e) {
			Log.e(TAG, "problem sending TCP message", e);
		}
	}

}
