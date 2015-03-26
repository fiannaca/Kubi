package uw.hcrlab.kubi;

import java.util.Random;

import uw.hcrlab.kubi.view.RobotFace;
import uw.hcrlab.kubi.view.RobotFace.Action;
import uw.hcrlab.kubi.view.RobotFace.Emotion;
import android.util.Log;

import com.revolverobotics.kubiapi.Kubi;
import com.revolverobotics.kubiapi.KubiManager;

/**
 * The Main thread which contains the main loop */
public class MainThread extends Thread {
	// the different between real time and calculated time to perform an action
	private final long EPSILON = 100;

	// sleep after 11 mins
	private final long SLEEP_TIME = 11 * 60 * 1000;
	// blink after 5 seconds
	private final long BLINK_TIME = 5 * 1000;
	// look around every 3 mins
	private long BORING_TIME = 3 * 60 * 1000;

	private long nextSleepTime;
	private long nextBlinkTime;
	private long nextBoringTime;
	private Random random;

	private static final String TAG = MainThread.class.getSimpleName();
	private RobotFace robotFace;
	private KubiManager kubiManager;
	private KubiDemoActivity activity;
	
	// flag to hold state 
	private boolean running;


	public MainThread(RobotFace robotFace, KubiManager kubiManager, KubiDemoActivity activity) {
		super();
		this.robotFace = robotFace;
		this.kubiManager = kubiManager;
		this.activity = activity;
		this.running = true;
		random = new Random();
		nextSleepTime = System.currentTimeMillis() + SLEEP_TIME + random.nextInt(10) * 60 * 1000;
		nextBlinkTime = System.currentTimeMillis() + BLINK_TIME + random.nextInt(10) * 1000;
		nextBoringTime = System.currentTimeMillis() + BORING_TIME + random.nextInt(60) * 1000;
	}
	
	@Override
	public void run() {
		Log.d(TAG, "Starting the main loop");

		robotFace.showAction(Action.WAKE);
		robotFace.setEmotion(Emotion.NORMAL);
		
		while (running) {
			try {
				synchronized (robotFace) {
					if (Math.abs(System.currentTimeMillis() - nextSleepTime) < EPSILON && !activity.isSleep()) {
						Log.i(TAG, "Sleep at " + System.currentTimeMillis());
						robotFace.showAction(Action.SLEEP);
						activity.setSleep(true);
						kubiFaceDown();
						nextSleepTime = 0;
						running = false;
					}
					
					if (Math.abs(System.currentTimeMillis() - nextBlinkTime) < EPSILON && !activity.isSleep())  {
						Log.i(TAG, "Blink at " + System.currentTimeMillis());
						robotFace.showAction(Action.BLINK);
						nextBlinkTime = System.currentTimeMillis() + BLINK_TIME + random.nextInt(10) * 1000;
					}
					if (Math.abs(System.currentTimeMillis() - nextBoringTime) < EPSILON && !activity.isSleep()) {
						Log.i(TAG, "Look around at " + System.currentTimeMillis());
						kubiLookAround();
						nextBoringTime = System.currentTimeMillis() + BORING_TIME + random.nextInt(60) * 1000;
					}
				}

			} catch (Exception e) {}
		}
	}
	
	private void kubiLookAround() {
		try {
			kubiManager.getKubi().performGesture(Kubi.GESTURE_RANDOM);
		} catch (Throwable e) {}
	}

	public void setRunning(boolean running) {
		this.running = running;
		if (!running) {
			activity.setSleep(true);
			robotFace.showAction(Action.SLEEP);
			robotFace.setEmotion(Emotion.SLEEP);
			kubiFaceDown();
		}
	}

	private void kubiFaceDown() {
		try {
			kubiManager.getKubi().performGesture(Kubi.GESTURE_FACE_DOWN);
		} catch (Throwable e) {}
	}
}
