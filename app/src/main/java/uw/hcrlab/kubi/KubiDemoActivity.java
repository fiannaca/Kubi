package uw.hcrlab.kubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import sandra.libs.asr.asrlib.ASR;
import sandra.libs.tts.TTS;
import sandra.libs.vpa.vpalib.Bot;
import uw.hcrlab.kubi.view.RobotFace;
import uw.hcrlab.kubi.view.RobotFace.Action;
import uw.hcrlab.kubidemo.R;
import uw.hcrlab.kubidemo.dialogs.KubiGestureDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.revolverobotics.kubiapi.Kubi;
import com.revolverobotics.kubiapi.KubiManager;

public class KubiDemoActivity extends ASR implements Observer {

	// The ID of the bot to use for the chatbot, can be changed
	// you can also make a new bot by creating an account in pandorabots.com and making a new chatbot robot
	private String PANDORA_BOT_ID = "b9581e5f6e343f72";
	
	private String TAG = KubiDemoActivity.class.getSimpleName();
	
	private MainThread mainThread;
	private boolean isSleep = false;
	
	/* Menu options */
	@SuppressWarnings("unused")
	private MenuItem mItemListenInput;
	
    private RobotFace robotFace;    // The face of the robot as shown on screen
    private TTS tts;       			 // Convert text to speech component of android 
	private KubiManager kubiManager; // Manager that manages the connected Kubi
    
    private Bot bot;
    
    // Map containing key = simple questions and value = how the robot responds
    private Map<String, String> simpleResponses;

	@SuppressWarnings("unused")
	private boolean trackFaceEnabled;
	
	/* When touched activate the listening */
	public boolean onTouchEvent(MotionEvent e) {
		Log.i(TAG, "Screen touched ");
		switch (e.getAction()) {
		//case MotionEvent.ACTION_DOWN:
	    //case MotionEvent.ACTION_MOVE:
	    case MotionEvent.ACTION_UP:
	    	if (isSleep) {
	    		restartMainThread();
	    		isSleep = false;
	    		kubiManager.getKubi().performGesture(Kubi.GESTURE_FACE_UP);
	    	}
	    	this.listen();
	    	break;
		default: 
			break;
		}
		return true;
	}
    
	private void restartMainThread() {
		if (mainThread.isAlive()) {
			mainThread.interrupt();
		}
		mainThread = new MainThread(robotFace, kubiManager, this);
		mainThread.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Log.i(TAG, "Trying to load OpenCV library");
        
        /*if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback)) {
          Log.e(TAG, "Cannot connect to OpenCV Manager");
        }*/
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_kubi_demo);
		
        robotFace = (RobotFace) findViewById(R.id.face);
        robotFace.addObserver(this);
        
        /*
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_preview);
	    mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    mOpenCvCameraView.setCvCameraViewListener(this);
	    */

        // Set up text to speech capability, using the library in TTSLib
        tts = TTS.getInstance(this);
        
		//Initialize the speech recognizer
		createRecognizer(getApplicationContext());	
		
		this.simpleResponses = new HashMap<String, String>();
    	// Parse the simple questions and responses that the robot is able to perform
    	// These responses are defined under res/values/arrays.xml
    	String[] stringArray = getResources().getStringArray(R.array.promptsAndResponses);
		for (String entry : stringArray) {
			String[] splitResult = entry.split("\\|", 2);
			simpleResponses.put(splitResult[0], splitResult[1]);
		}
		
		// Manager that manages the Kubi's actions
		kubiManager = new KubiManager(new KubiCallback(), true);
		
		// A chat bot web service that the user can optionally use to answer responses
		bot = new Bot(this, PANDORA_BOT_ID, this.tts);
		
		mainThread = new MainThread(robotFace, kubiManager, this);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
		restartMainThread();
	}
	
	@Override
	protected void onStop() {
		destroyMainThread();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kubi_demo, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        int itemId = item.getItemId();
		if (itemId == R.id.action_gestures) {
			DialogFragment dialog = new KubiGestureDialog();
			dialog.show(getFragmentManager(), "KubiGestureDialogFragment");
		} else if (itemId == R.id.connect_kubi) {
			// Callback function is in charge of connecting to kubi
        	kubiManager.findAllKubis();
		} else if (itemId == R.id.action_face_track) {
			this.trackFaceEnabled = true;
		} else if (itemId == R.id.action_listen) {
			this.listen();
		}
        return true;
    }
    
    // Returns the kubi manage of this activity
    public KubiManager getKubiManager(){
		return this.kubiManager;
	}
    
    // Shows a simple notification that the kubi is not connected
    private void showNotConnectedMsg() {
		Context context = getApplicationContext();
		CharSequence text = "No Kubi connected";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
    }
    
	/* Handler for situations where you listen to user input */
	private void listen() {
		Log.i(TAG, "listening");
		try {
			super.listen(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH, 1);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),"ASR could not be started: invalid params", Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage());
		} 
	}

	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
		boolean demo = false;
		String bestResult = nBestList.get(0);
		
		Log.d(TAG, "Speech input: " + bestResult);
		
		String toSpeak = simpleResponses.get(bestResult);
		// Depending on the known inputs, maybe do some actions as well

		if (bestResult.equalsIgnoreCase("goodbye")) {
			isSleep = true;
			toSpeak = "until next time";
		} else if (bestResult.equalsIgnoreCase("Hello")) {
			if (!tryConnectingToKubi()) {
				toSpeak = "Hello there. I'm not connected to a kubi.";
				showNotConnectedMsg();
			} else {
				toSpeak = "Hello there.";
				kubiManager.getKubi().performGesture(Kubi.GESTURE_NOD);
			}
		} else if(bestResult.equalsIgnoreCase("Connect")) {
			kubiManager.findAllKubis();
			toSpeak = "connecting to a kubi";
		} else if(bestResult.equalsIgnoreCase("uhm")) {
			robotFace.showAction(Action.GLARE);
			toSpeak = "tell me about it";
		} else if (bestResult.equalsIgnoreCase("what can you do")) {
			if (kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				toSpeak = "let me show you";
				demo = true;
			} else {
				toSpeak = "I can show you if I'm connected to a Kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("you are funny")) {
			toSpeak = "thank you";
			robotFace.showAction(Action.GIGGLE);
		} else if (bestResult.equalsIgnoreCase("I have to be honest with you")) {
			toSpeak = "what have you done";
			robotFace.showAction(Action.GLARE);
		} else if (bestResult.equalsIgnoreCase("on your left")) {
			if(kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				kubiManager.getKubi().performGesture(Kubi.GESTURE_LEFT);
				toSpeak = "okay. I think I see you";
			} else {
				toSpeak = "I'm not connected to a kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("on your right")) {
			if(kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				kubiManager.getKubi().performGesture(Kubi.GESTURE_RIGHT);
				toSpeak = "okay. I think I see you";
			} else {
				toSpeak = "I'm not connected to a kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("shake")) {
			if(kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				kubiManager.getKubi().performGesture(Kubi.GESTURE_SHAKE);
			} else {
				toSpeak = "I'm not connected to a kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("bow")) {
			if(kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				kubiManager.getKubi().performGesture(Kubi.GESTURE_BOW);
			} else {
				toSpeak = "I'm not connected to a kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("scan")) {
			if(kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				kubiManager.getKubi().performGesture(Kubi.GESTURE_SCAN);
			} else {
				toSpeak = "I'm not connected to a kubi.";
				showNotConnectedMsg();
			}
		} else if (bestResult.equalsIgnoreCase("go to sleep")) {
			isSleep = true;
			toSpeak = "until next time";
		// Showing a face, assumes that the 3rd index is a valid emotion
		} else if (bestResult.matches("show me (a|an|your) (.*)")) {
			String emotion = bestResult.split(" ")[3];
			Log.d(TAG, "Emotion : " + emotion);
			if(emotion.equalsIgnoreCase("happy")) {
				robotFace.setEmotion(RobotFace.Emotion.HAPPY);
				toSpeak = "Happy face";
			} else if(emotion.equalsIgnoreCase(":-(")) {
				robotFace.setEmotion(RobotFace.Emotion.SAD);
				toSpeak = "Sad face";
			} else if(emotion.equalsIgnoreCase("surprised") || emotion.equalsIgnoreCase("surprise")) {
				robotFace.setEmotion(RobotFace.Emotion.SURPRISED);
				toSpeak = "Surprise!";
			} else if(emotion.equalsIgnoreCase("wink")) {
				robotFace.showAction(Action.WINK);
				toSpeak = "Wink";
			} else if(emotion.equalsIgnoreCase("worried")) {
				robotFace.setEmotion(RobotFace.Emotion.WORRIED);
				toSpeak = "I'm worried";
			} else if(emotion.equalsIgnoreCase("giggle")) {
				Thread t1 = new Thread(new Runnable() {
				     public void run()
				     {robotFace.showAction(Action.GIGGLE);}
				});  
				t1.start();
				toSpeak = "haha";
			} else if(emotion.equalsIgnoreCase("smile")) {
				Thread t1 = new Thread(new Runnable() {
				     public void run()
				     {robotFace.showAction(Action.SMILE);}
				});  
				t1.start();
				toSpeak = "Smiling";
			// Default behavior: Can't show the thing you want
			} else {
				robotFace.setEmotion(RobotFace.Emotion.NORMAL);
				toSpeak = "I can't show you that";
			}
		}

		try {
			if(toSpeak != null){
				Log.d(TAG, "Saying : " + toSpeak);
				tts.speak(toSpeak, "EN");
			}  else {
				// Default response
				bot.initiateQuery(bestResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(demo && kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
			demo = false;
			kubiManager.getKubi().performGesture(Kubi.GESTURE_LEFT);
			kubiManager.getKubi().performGesture(Kubi.GESTURE_RIGHT);
			kubiManager.getKubi().performGesture(Kubi.GESTURE_BOW);
			kubiManager.getKubi().performGesture(Kubi.GESTURE_NOD);
			kubiManager.getKubi().performGesture(Kubi.GESTURE_SCAN);
			kubiManager.getKubi().performGesture(Kubi.GESTURE_SHAKE);
			
		}
		
		if (isSleep) {
			mainThread.setRunning(false);
		}		
	}

	private boolean tryConnectingToKubi() {
		if (kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
			return true;
		} 
		final int trials = 5;
		for (int i = 0; i < trials; i++) {
			kubiManager.findAllKubis();
			if (kubiManager.getStatus() == KubiManager.STATUS_CONNECTED) {
				return true;
			} 
		}
		return false;
	}

	@Override
	public void processAsrReadyForSpeech() {
		Toast.makeText(this, "I'm listening", Toast.LENGTH_LONG).show();
	}

	@Override
	public void processAsrError(int errorCode) {

		String errorMessage;
		switch (errorCode) 
        {
	        case SpeechRecognizer.ERROR_AUDIO: 
	        	errorMessage = "Audio recording error"; 
	        	break;
	        case SpeechRecognizer.ERROR_CLIENT: 
	        	errorMessage = "Client side error";
	        	break;
	        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: 
	        	errorMessage = "Insufficient permissions" ; 
	        	break;
	        case SpeechRecognizer.ERROR_NETWORK: 
	        	errorMessage = "Network related error" ;
	        	break;
	        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:                
	            errorMessage = "Network operation timeout"; 
	            break;
	        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
	        	errorMessage = "RecognitionServiceBusy" ; 
	        	break;
	        case SpeechRecognizer.ERROR_SERVER: 
	        	errorMessage = "Server sends error status"; 
	        	break;
	        case SpeechRecognizer.ERROR_NO_MATCH: 
	        	errorMessage = "pardon me";
	        	//errorMessage = "No matching message" ;
	        	break;
	        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: 
	        	errorMessage = null;
	        	//errorMessage = "Input not audible";
	        	break;
	        default:
	        	errorMessage = "ASR error";
	        	break;
        }

		try {
			if (errorMessage != null) {
				tts.speak(errorMessage,"EN");
			}
		} catch (Exception e) {
			Log.e(TAG, "English not available for TTS, default language used instead");
		}
		
		// If there is an error, shows feedback to the user and writes it in the log
	    Log.e(TAG, "Error: "+ errorMessage);
	    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

	}

	// Shut down TTS engine when finished
	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying...");
		destroyMainThread();
		tts.shutdown();
		super.onDestroy();
	}

	private void destroyMainThread() {
		boolean retry = true;
		while (retry) {
			try {
				mainThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}

	}

	@Override
	public void update(Observable observable, Object data) {
		this.onTouchEvent((MotionEvent) data);
	}

	public void setSleep(boolean b) {
		isSleep = true;
	}

	public boolean isSleep() {
		return isSleep;
	}

	public void startListening() {
		Log.i(TAG, "start listening");
		this.listen();
	}
}