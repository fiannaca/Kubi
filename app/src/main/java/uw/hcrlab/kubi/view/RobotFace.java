package uw.hcrlab.kubi.view;

import java.util.ArrayList;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RobotFace extends SurfaceView implements SurfaceHolder.Callback {

	private static ArrayList<Observer> observers = new ArrayList<Observer>();
	
	private static String TAG = RobotFace.class.getSimpleName();
	
	// Default color for the background  
	public static int BACKGROUND_COLOR = Color.BLACK;
	
	public enum Action {
		SMILE, WINK, BLINK, GIGGLE, GLARE, SLEEP, WAKE, SURPRISED, THINK, GUILTY, LISTENL, LISTENR, NBLINK
	}
	
	public enum EyeShape{
		CIRCLE, OVAL, RECTANGLE
	}
	
	public enum TouchMode{
		NORMAL, POSITION_LEFT_EYE, POSITION_RIGHT_EYE
	}
	
	// Emotional states for the face
	public enum Emotion {
		SAD, HAPPY, IDLE, SURPRISED, NORMAL, SLEEP, GIGGLE, ANGRY, WINK, WORRIED, ROLLING
	}
	
	public enum Eye_ID {
		LEFT, RIGHT, BOTH
	}
	
	private static float DEFAULT_EYE_OUTER_RADIUS = 200;
	
	// The width of the view (set when the surface is initially created)
	private int screenWidth;
	
	// The height of the view (set when the surface is initially created)
	private int screenHeight;
	
	// Creating two eyes for the face of the robot
	private RobotEye left_eye;
	private RobotEye right_eye;
	
	// The mouth of the robot
	//private Mouth mouth;

	// Determines what state the face should show
	private Emotion state;
	
	// Depending on the mode, an on touch event can cause different things to occur
	// Default value on construction in NORMAL
	private TouchMode tmode;
	
	// For changing eye_shape
	private EyeShape eye_shape;
	
	public RobotFace(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// Default state is idle
		this.state = Emotion.NORMAL;
		this.tmode = TouchMode.NORMAL;
		this.eye_shape = EyeShape.CIRCLE;
		
		// Tell the SurfaceHolder ( -> getHolder() ) to receive SurfaceHolder callback
		getHolder().addCallback(this);	

	}
	
	/* Setters */
	public void addObserver(Observer ob) {
		observers.add(ob);
	}
	
	public void setTouchMode (TouchMode tmode) {this.tmode = tmode;}
	
	public void setEyeShape (EyeShape eye_shape) {
		this.eye_shape = eye_shape;
		this.left_eye.setEyeShape(eye_shape);
		this.right_eye.setEyeShape(eye_shape);
		this.drawFace();
	}
	
	// Setter for the emotion of the robot, will automatically change the emotion
	public void setEmotion(Emotion e) {
		this.state = e;
		this.left_eye.setEmotion(e);
		this.right_eye.setEmotion(e);
		this.drawFace();
	}
	
	/* Getters */
	public int getScreenWidth( ){return this.screenWidth; }
	public int getScreenHeight() { return this.screenHeight; }
	public RobotEye getLeftEye() { return this.left_eye;}
	public RobotEye getRightEye() { return this.right_eye;}
	//public Mouth getMouth() { return this.mouth; }
	public TouchMode getTouchMode() { return this.tmode; }
	public EyeShape getEyeShape() { return this.eye_shape; }
	public Emotion getEmotion(){ return this.state; }
	
	/* Handles the view's behavior when something is touched on screen */
	@SuppressLint("ClickableViewAccessibility") 
	public boolean onTouchEvent(MotionEvent e) {
		for (Observer ob : observers) {
			ob.update(null, e);
		}
		return true;
	}
	
	/** Moves eyes to view at a point in the screen */
	public void moveEyes(float x, float y, Eye_ID eye_id) {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		try {
			canvas = holder.lockCanvas();
			synchronized (holder) {
				// clear the screen
				canvas.drawColor(BACKGROUND_COLOR);
				if(eye_id == Eye_ID.LEFT)
					left_eye.moveEyeBall(canvas, x, y);
				else if (eye_id ==  Eye_ID.RIGHT)
					right_eye.moveEyeBall(canvas, x, y);
				else {
					left_eye.moveEyeBall(canvas, x, y);
					right_eye.moveEyeBall(canvas, x, y);
				}
			}
		} finally {
			if (canvas != null) {
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	public void moveEyes(float left_x, float left_y, float right_x, float right_y) {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		try {
			canvas = holder.lockCanvas();
			if (canvas != null) {
				/*eyeHandler(canvas);*/
			}
			synchronized (holder) {
				// clear the screen
				canvas.drawColor(BACKGROUND_COLOR);
				left_eye.moveEyeBall(canvas, left_x, left_y);
				right_eye.moveEyeBall(canvas, right_x, right_y);
			}
		} finally {
			holder.unlockCanvasAndPost(canvas);
		}
	}

	/* Obtain the size (width and height) of the view to correctly position the eyes */
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		this.screenHeight = h;
		this.screenWidth = w;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		this.showAction(Action.BLINK);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// Initialize the eyes (this assumes the screen has been measured)
		// DEFAULT VALUES HERE
		left_eye = new RobotEye(screenWidth * 4/16, screenHeight/3, 
								DEFAULT_EYE_OUTER_RADIUS, Eye_ID.LEFT);
		right_eye = new RobotEye(screenWidth * 12/16, screenHeight/3, 
								DEFAULT_EYE_OUTER_RADIUS, Eye_ID.RIGHT);
		
		drawFace();
	}
	
	/* Redraws the whole face on the canvas (required when new settings for the eye size and locations are called */
	public void drawFace() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		try {
			canvas = holder.lockCanvas();
			synchronized (holder) {
				// clear the screen
				canvas.drawColor(BACKGROUND_COLOR);
				this.left_eye.moveEyeBall (canvas, left_eye.getX(), left_eye.getY());
				this.right_eye.moveEyeBall(canvas, right_eye.getX(), right_eye.getY());
			}
		} finally {
			if (canvas != null) {
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void showAction(Action action) {
		switch (action) {
			case SMILE: 	showSmile();		break;
			case WINK:		showWink();			break;
			case BLINK: 	showBlink();		break;
			case GIGGLE: 	showGiggle();		break;
			case GLARE:		showGaze();			break;
			case SLEEP:		showSleep();		break;
			case WAKE:		showWake();			break;
			case SURPRISED: showSurprised();	break;
			case THINK:		showThink();		break;
			case GUILTY:	showGuilty();		break;
			case LISTENL:	showListenL();		break;
			case LISTENR:	showListenR();		break;
			case NBLINK:	showNormalBlink();	break;
			default:							break;
		}
	}

	private void showListenR() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		int limit = (int)(DEFAULT_EYE_OUTER_RADIUS * 2f/3);
		if (this.eye_shape == EyeShape.OVAL) {
			limit = (int) DEFAULT_EYE_OUTER_RADIUS;
		}
		
		// move the pupil to the right
		for (int i = 0; i <= limit; i += 10 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, 0);
					right_eye.movePupil(canvas, i, 0); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// delay
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// goes back to normal
		for (int i = limit; i >= 0; i -= 10) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, 0);
					right_eye.movePupil(canvas, i, 0); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private void showListenL() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		int limit = (int)(DEFAULT_EYE_OUTER_RADIUS * 2f/3);
		if (this.eye_shape == EyeShape.OVAL) {
			limit = (int) DEFAULT_EYE_OUTER_RADIUS;
		}
		
		// move the pupil to the left
		for (int i = 0; i <= limit; i += 10 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, -i, 0);
					right_eye.movePupil(canvas, -i, 0); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// delay
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// goes back to normal
		for (int i = limit; i >= 0; i -= 10) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, -i, 0);
					right_eye.movePupil(canvas, -i, 0); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private void showGuilty() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		int xlimit = (int)(DEFAULT_EYE_OUTER_RADIUS * 2f/3);
		int ylimit = xlimit;
		if (this.eye_shape == EyeShape.OVAL) {
			xlimit = (int) DEFAULT_EYE_OUTER_RADIUS;
		}
		
		// move the pupil down
		for (int j = 0; j > -ylimit; j-= 5){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, 0, -j);
					right_eye.movePupil(canvas, 0, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		// move the pupil from the middle to the left
		for (int i = 0; i > -xlimit/3; i -= 1 ){
			int j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i) * Math.abs(i));
			if (this.eye_shape == EyeShape.OVAL) {
				j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i * 2/3) * Math.abs(i * 2/3));
			}
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, j);
					right_eye.movePupil(canvas, i, j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// delay
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// move the pupil from the left to the middle
		for (int i = - xlimit/3; i < 0; i += 1 ){
			int j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i) * Math.abs(i));
			if (this.eye_shape == EyeShape.OVAL) {
				j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i * 2/3) * Math.abs(i * 2/3));
			}
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, j);
					right_eye.movePupil(canvas, i, j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// move the pupil up
		for (int j = -ylimit; j < 0; j+= 5){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, 0, -j);
					right_eye.movePupil(canvas, 0, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		this.setEmotion(Emotion.NORMAL);
		// delay
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		showNormalBlink();
	}

	private void showThink() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		int xlimit = (int)(DEFAULT_EYE_OUTER_RADIUS * 2f/3);
		int ylimit = xlimit;
		if (this.eye_shape == EyeShape.OVAL) {
			xlimit = (int) DEFAULT_EYE_OUTER_RADIUS;
		}
		
		// move the pupil up
		for (int j = 0; j < ylimit; j+= 5){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, 0, -j);
					right_eye.movePupil(canvas, 0, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		// move the pupil from the middle to the right
		for (int i = 0; i < xlimit/3; i += 1 ){
			int j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i) * Math.abs(i));
			if (this.eye_shape == EyeShape.OVAL) {
				j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i * 2/3) * Math.abs(i * 2/3));
			}
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, -j);
					right_eye.movePupil(canvas, i, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// delay
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// move the pupil from the right back to the middle
		for (int i = xlimit/3; i < 0; i += 1 ){
			int j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i) * Math.abs(i));
			if (this.eye_shape == EyeShape.OVAL) {
				j = (int) Math.sqrt(ylimit * ylimit - Math.abs(i * 2/3) * Math.abs(i * 2/3));
			}
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, i, -j);
					right_eye.movePupil(canvas, i, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		// move the pupil down
		for (int j = ylimit; j > 0; j-= 5){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.movePupil(canvas, 0, -j);
					right_eye.movePupil(canvas, 0, -j); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		this.setEmotion(Emotion.NORMAL);
		
		// delay
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		showBlink();
	}

	private void showSurprised() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// expand the pupil
		for (int i = 0; i < (DEFAULT_EYE_OUTER_RADIUS / 8); i += 10 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.expandEye(canvas, i);
					right_eye.expandEye(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		
		try {
			canvas = holder.lockCanvas();
			synchronized (holder) {
				// clear the screen
				canvas.drawColor(BACKGROUND_COLOR);
				left_eye.expandEye(canvas, DEFAULT_EYE_OUTER_RADIUS / 8);
				right_eye.expandEye(canvas, DEFAULT_EYE_OUTER_RADIUS / 8); 
			}
		} finally {
			if (canvas != null) {
				holder.unlockCanvasAndPost(canvas);
			}
		}
		
		// delay
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// goes back to normal
		for (int i = (int) (DEFAULT_EYE_OUTER_RADIUS / 8); i > 0; i -= 2) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.expandEye(canvas, i);
					right_eye.expandEye(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}	
	}

	// only used while sleeping (Emotion is SLEEP)
	private void showWake() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// goes back to normal
		for (int i = (int) (DEFAULT_EYE_OUTER_RADIUS * 2); i > 0; i -= 20) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveUpperLids(canvas, i);
					right_eye.moveUpperLids(canvas, i); 
				}
			} catch (NullPointerException e) {
				Log.i(TAG, "Canvas is unavailable.");
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		this.setEmotion(Emotion.NORMAL);
		showNormalBlink();
	}

	// the functions below are only used while normal (Emotion is NORMAL)
	
	private void showSleep() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// closing eyes
		for (int i = 0; i < DEFAULT_EYE_OUTER_RADIUS * 2; i += 5 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveUpperLidsAndPupil(canvas, i, 0, i/3);
					right_eye.moveUpperLidsAndPupil(canvas, i, 0, i/3);
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		this.setEmotion(Emotion.SLEEP);
	}

	private void showGaze() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// for the oval, need to move the eye up by alpha first
		if (this.eye_shape == EyeShape.OVAL) {
			for (int i = 0; i <= 20 ; i++) {
				try {
					canvas = holder.lockCanvas();
					synchronized (holder) {
						canvas.drawColor(BACKGROUND_COLOR);
						left_eye.changeAngle(canvas, i);
						right_eye.changeAngle(canvas, -i);
					}
				} finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
			
			// delay
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		// gazing
		for (int i = 0; i < DEFAULT_EYE_OUTER_RADIUS * 3/4; i += 4 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveBothLids(canvas, i);
					right_eye.moveBothLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		// delay
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// goes back to normal
		for (int i = (int) (DEFAULT_EYE_OUTER_RADIUS * 3/4); i > 0; i -= 5) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveBothLids(canvas, i);
					right_eye.moveBothLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		// for the oval eye, need to go back to normal from alpha
		if (this.eye_shape == EyeShape.OVAL) {
			for (int i = 20; i > 0 ; i--) {
				try {
					canvas = holder.lockCanvas();
					synchronized (holder) {
						canvas.drawColor(BACKGROUND_COLOR);
						left_eye.changeAngle(canvas, i);
						right_eye.changeAngle(canvas, -i);
					}
				} finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
		
		this.setEmotion(Emotion.NORMAL);	
	}

	private void showGiggle() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// giggling
		for (int i = 0; i < 3; i++){
			this.setEmotion(Emotion.HAPPY);
			// delay
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {	
					left_eye.MoveEyeVertical(canvas, -50);
					right_eye.MoveEyeVertical(canvas, -50);
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
			
			// delay
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.setEmotion(Emotion.NORMAL);	
	}
	
	private void showNormalBlink() {
		showBlink();
		showBlink();
		// delay
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		showBlink();
	}

	private void showBlink() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// closing eye
		for (int i = 0; i < DEFAULT_EYE_OUTER_RADIUS * 2; i += 150 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveUpperLids(canvas, i);
					right_eye.moveUpperLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		this.setEmotion(Emotion.SLEEP);
		// delay
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// goes back to normal
		for (int i = (int) (DEFAULT_EYE_OUTER_RADIUS * 2); i > 0; i -= 150) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.moveUpperLids(canvas, i);
					right_eye.moveUpperLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}			
		this.setEmotion(Emotion.NORMAL);
	}

	private void showWink() {
		Canvas canvas = null;
		SurfaceHolder holder = getHolder();
		
		// closing the right eye
		for (int i = 0; i < DEFAULT_EYE_OUTER_RADIUS * 2; i += 100 ){
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.drawEye(canvas);
					right_eye.moveUpperLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}				   	
		}
		this.setEmotion(Emotion.WINK);
		// delay
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// goes back to normal
		for (int i = (int) (DEFAULT_EYE_OUTER_RADIUS * 2); i > 0; i -= 100) {
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					// clear the screen
					canvas.drawColor(BACKGROUND_COLOR);
					left_eye.drawEye(canvas);
					right_eye.moveUpperLids(canvas, i); 
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}			
		this.setEmotion(Emotion.NORMAL);
	}

	private void showSmile() {
		this.setEmotion(Emotion.HAPPY);

		// delay
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.setEmotion(Emotion.NORMAL);
	}

}
