package ioio.examples.hello;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
	private SeekBar mSeekBar1;
	private SeekBar mSeekBar2;
	private SeekBar mSeekBar3;
	private ScrollView mScrollView;
	private TextView logView;
	
	private StringBuilder logString;
	
	private Looper mLooper;
	

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

					MainActivity.this.helper_.restart();
					
//					if (mLooper == null)
//						mLooper = new Looper();
					
					if (mLooper != null)
						mLooper.setup();
				} catch (ConnectionLostException e) {
					e.printStackTrace();
					log("Failed to force setup");
					log(e.getMessage());
				}
			}
		});
		
		mSeekBar1 = (SeekBar) findViewById(R.id.seekBar1);
		mSeekBar2 = (SeekBar) findViewById(R.id.seekBar2);
		mSeekBar3 = (SeekBar) findViewById(R.id.seekBar3);
				
		logView = (TextView) findViewById(R.id.logView);
		logString = new StringBuilder();
		log("MainActivity onCreate()");
	}
	
	public void log(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (logView.getLineCount() > 500) {
					logString = new StringBuilder();
				}
				
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

		RGB(byte r, byte g, byte b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public RGB() {
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
		
		private static final int STRIP_LENGTH = 32;
		
		/** The on-board LED. */
		private DigitalOutput mLED;
		private SpiMaster mSPI;
		private byte[] mBuffer1 = new byte[48];
		private byte[] mBuffer2 = new byte[48];
		
		private long strip_colors[];
		
		//private RGB mRGB = new RGB();

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
			
			if (ioio_ == null) {
				log("creating ioio factory");
				ioio_ = IOIOFactory.create();
			}
			
			if (ioio_ != null) {
				log("ioio connected");
				mLED = ioio_.openDigitalOutput(0, true);
				mSPI = ioio_.openSpiMaster(5, 4, 3, 6, SpiMaster.Rate.RATE_50K);
				
				//Pre-fill the color array with known values
				strip_colors = new long[STRIP_LENGTH];
				strip_colors[0] = 0xFF0000; //Bright Red
				strip_colors[1] = 0x00FF00; //Bright Green
				strip_colors[2] = 0x0000FF; //Bright Blue
				strip_colors[3] = 0x000000; //Bright Black
//				strip_colors[3] = 0x010000; //Faint red
//				strip_colors[4] = 0x800000; //1/2 red (0x80 = 128 out of 256)

//				strip_colors[3] = 0xFF0000; //Bright Red
//				strip_colors[4] = 0x00FF00; //Bright Green
//				strip_colors[5] = 0x0000FF; //Bright Blue
//				strip_colors[6] = 0xFF0000; //Bright Red
//				strip_colors[7] = 0x00FF00; //Bright Green
//				strip_colors[8] = 0x0000FF; //Bright Blue
//				strip_colors[9] = 0xFF0000; //Bright Red
//				strip_colors[10] = 0x00FF00; //Bright Green
//				strip_colors[11] = 0x0000FF; //Bright Blue
//				strip_colors[12] = 0xFF0000; //Bright Red
//				strip_colors[13] = 0x00FF00; //Bright Green
//				strip_colors[14] = 0x0000FF; //Bright Blue
//				strip_colors[15] = 0xFF0000; //Bright Red
//				strip_colors[16] = 0x00FF00; //Bright Green
//				strip_colors[17] = 0x0000FF; //Bright Blue
//				strip_colors[18] = 0xFF0000; //Bright Red
//				strip_colors[19] = 0x00FF00; //Bright Green
//				strip_colors[20] = 0x0000FF; //Bright Blue
//				strip_colors[21] = 0xFF0000; //Bright Red
//				strip_colors[22] = 0x00FF00; //Bright Green
//				strip_colors[23] = 0x0000FF; //Bright Blue
//				strip_colors[24] = 0xFF0000; //Bright Red
//				strip_colors[25] = 0x00FF00; //Bright Green
//				strip_colors[26] = 0x0000FF; //Bright Blue
//				strip_colors[27] = 0xFF0000; //Bright Red
//				strip_colors[28] = 0x00FF00; //Bright Green
//				strip_colors[29] = 0x0000FF; //Bright Blue
//				strip_colors[30] = 0xFF0000; //Bright Red
//				strip_colors[31] = 0x00FF00; //Bright Green
			} else {
				log("ioio is null. Setup Failed.");
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
			
			// We have 32 LEDs. Each one of them gets lit with probability
			// frequency_. If lit, we pick a random pixel from the current preview
			// frame and use its color. Otherwise, we set the LED to black and
			// setLed() will take care of gradual fading.
			RGB color = null;
			try {
				color = getSlidersColor();
			} catch (Exception e) {
				log(e.getMessage());
			}
			log("Updating LED Strip : "+color.r+" "+color.g+" "+color.b);
			
			int rand1, rand2;
			int max = 3;
			for (int i = 0; i < 48; i++) {
//				setLed(i, color);

				rand1 = (int) (Math.random() * max);
				rand2 = (int) (Math.random() * max);
				mBuffer1[i] = (byte) strip_colors[rand1];
				mBuffer2[i] = (byte) strip_colors[rand2];
			}
			
			try {
				writeBuffers();
				
				// refresh rate
				Thread.sleep(600);
			} catch (InterruptedException e) {
				log("InterruptionException");
				log(e.getMessage());
			}
		}
		
		private void writeBuffers() throws ConnectionLostException, InterruptedException {
			log("writeBuffers()");
			mSPI.writeReadAsync(0, mBuffer1, mBuffer1.length, mBuffer1.length, null, 0);
			mSPI.writeRead(mBuffer2, mBuffer2.length, mBuffer2.length, null, 0);
		}
		
		/** Choose a random pixel from the current preview frame. */
		private RGB getSlidersColor() throws NumberFormatException {
			int r = mSeekBar1.getProgress();
			int g = mSeekBar2.getProgress();
			int b = mSeekBar3.getProgress();
			
			log("Found color values : "+r+" "+g+" "+b);
			
			return new RGB(Byte.valueOf("0"), Byte.valueOf("20"), Byte.valueOf("100"));
//			return new RGB(Byte.valueOf(String.valueOf(r), 10), Byte.valueOf("20"), Byte.valueOf("100"));
//			return new RGB((byte)r, (byte)g, (byte)b);
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
//				num -= 16;
			} else {
				buffer = mBuffer1;
			}
			
			num *= 3;
			buffer[num++] = rgb.r;
			buffer[num++] = rgb.g;
			buffer[num++] = rgb.b;

			// Poor-man's white balanace :)
//			buffer[num++] = fixColor(rgb.r, 0.9);
//			buffer[num++] = rgb.g;
//			buffer[num++] = fixColor(rgb.b, 0.5);
			
			if (num >= 16) {
				mBuffer2 = buffer;
			} else {
				mBuffer1 = buffer;
			}
		}

		/** Attenuates a brightness level. */
		private byte fixColor(byte color, double attenuation) {
			double d = (double) ((int) color & 0xFF) / 256;
			d *= attenuation;
			return (byte) (d * 256);
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
	

	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		log("createIOIOLooper connectionType="+connectionType+" extra="+extra);
		mLooper = new Looper();
		return mLooper;
	}
}