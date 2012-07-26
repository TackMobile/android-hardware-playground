package ioio.examples.hello;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton mToggleButton;
	private TextView logView;
	private  StringBuilder logString;
	
	private Looper mLooper;
	private ScrollView mScrollView;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		mToggleButton = (ToggleButton) (findViewById(R.id.button));
		findViewById(R.id.clear_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logString = new StringBuilder();
				logView.setText("");
			}
		});
		findViewById(R.id.force_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (mLooper != null)
						mLooper.setup();
				} catch (ConnectionLostException e) {
					e.printStackTrace();
					log("Failed to force setup");
					log(e.getMessage());
				}
			}
		});
		
		logView = (TextView) findViewById(R.id.logView);
		logString = new StringBuilder();
		log("MainActivity onCreate()");
	}
	
	public void log(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logView.setText(logString.append(s).append("\n").toString());
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}
	
	/** An RGB triplet. */
	private static class RGB {
		byte r;
		byte g;
		byte b;

		RGB() {
			clear();
		}

		public void clear() {
			r = g = b = 0;
		}
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput mLED;
		private SpiMaster mSPI;
		private byte[] mBuffer1 = new byte[48];
		private byte[] mBuffer2 = new byte[48];
		private RGB mRGB = new RGB();

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			log("Looper setup()");
			if (ioio_ != null) {
				mLED = ioio_.openDigitalOutput(0, true);
				mSPI = ioio_.openSpiMaster(5, 4, 3, 6, SpiMaster.Rate.RATE_50K);
			}
		}
		
		@Override
		public void disconnected() {
			log("disconnected()");
			super.disconnected();
		}
		
		@Override
		public void incompatible() {
			log("incompatible()");
			super.incompatible();
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			log("loop()");
			
			mLED.write(!mToggleButton.isChecked());
			
//			if (mOn) {
//				for (int i = 0; i < 32; i++) {
//					mRGB.clear();
//					getRandomColor(mRGB);
//					setLed(i, mRGB);
//				}
//			}
			
			try {
				mSPI.writeReadAsync(0, mBuffer1, mBuffer1.length, mBuffer1.length, null, 0);
				mSPI.writeRead(mBuffer2, mBuffer2.length, mBuffer2.length, null, 0);
				
				// 1 second refresh rate
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log("InterruptionException");
				log(e.getMessage());
			}
		}
		
		/** Choose a random pixel from the current preview frame. */
		private void getRandomColor(RGB rgb) {
			rgb.r = 100;
			rgb.g = 100;
			rgb.b = 100;
		}

		/**
		 * Set an LED to a certain color.
		 * If black is applied, the LED will fade out.
		 */
		private void setLed(int num, RGB rgb) {
			// Find the right buffer to write to (first or second half).
			byte[] buffer;
			if (num >= 16) {
				buffer = mBuffer2;
				num -= 16;
			} else {
				buffer = mBuffer1;
			}
			num *= 3;
			buffer[num++] = rgb.r;
			buffer[num++] = rgb.g;
			buffer[num++] = rgb.b;
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		log("createIOIOLooper");
		mLooper = new Looper();
		return mLooper;
	}
}