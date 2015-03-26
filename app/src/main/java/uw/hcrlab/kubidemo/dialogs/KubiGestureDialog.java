package uw.hcrlab.kubidemo.dialogs;

import uw.hcrlab.kubidemo.R;
import uw.hcrlab.kubi.KubiDemoActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.revolverobotics.kubiapi.Kubi;
import com.revolverobotics.kubiapi.KubiManager;

public class KubiGestureDialog extends DialogFragment {
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        builder.setTitle(R.string.gesture_title)
        .setItems(R.array.gesture_list, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	KubiManager kubi_manager = ((KubiDemoActivity) getActivity()).getKubiManager();
            	Kubi kubi = kubi_manager.getKubi();
            	
            	// Refer to the change_emotion_list in res/values/arrays.xml
            	// to figure out which index maps to which button
            	if(kubi_manager.getStatus() == kubi_manager.STATUS_CONNECTED) {
	            	if(which == 0) {
	            		// NOD
	            		kubi.performGesture(Kubi.GESTURE_NOD);
	            	} else if (which == 1) {
	            		// SHAKE
	            		kubi.performGesture(Kubi.GESTURE_SHAKE);
	            	} else if (which == 2){
	            		// BOW
	            		kubi.performGesture(Kubi.GESTURE_BOW);
	            	} else if (which == 3) {
	            		// SCAN
	            		kubi.performGesture(Kubi.GESTURE_SCAN);
	            	}
            	} else {
        			Context context = getActivity().getApplicationContext();
        			CharSequence text = "No Kubi connected";
        			int duration = Toast.LENGTH_SHORT;

        			Toast toast = Toast.makeText(context, text, duration);
        			toast.show();
            	}
            		
            // The 'which' argument contains the index position
            // of the selected item
            }
        });
        return builder.create();
    }
}
