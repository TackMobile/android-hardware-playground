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

					//MainActivity.this.helper_.restart();
					
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
				
				//logView.setText(logString.append(s).append("\n").toString());
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
			
			RGB color = null;
			try {
				color = getSlidersColor();
			} catch (Exception e) {
				log(e.getMessage());
			}
			
			int rand1, rand2;
			int max = 3;
			for (int i = 0; i < 48; i += 3) {
				mBuffer1[i] = color.r;
				mBuffer1[i+1] = color.g;
				mBuffer1[i+2] = color.b;
				mBuffer2[i] = color.r;
				mBuffer2[i+1] = color.g;
				mBuffer2[i+2] = color.b;
			}
			try {
				writeBuffers();
				Thread.sleep(500);
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
			
			return new RGB((byte)r, (byte)g, (byte)b);
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