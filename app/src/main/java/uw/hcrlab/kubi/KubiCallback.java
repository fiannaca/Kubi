package uw.hcrlab.kubi;

import java.util.ArrayList;

import android.util.Log;

import com.revolverobotics.kubiapi.IKubiManagerDelegate;
import com.revolverobotics.kubiapi.Kubi;
import com.revolverobotics.kubiapi.KubiManager;
import com.revolverobotics.kubiapi.KubiSearchResult;

public class KubiCallback implements IKubiManagerDelegate {
	public static String TAG = "KubiCallback";
	
	@Override
	public void kubiDeviceFound(KubiManager manager, KubiSearchResult result) {
		Log.i(TAG, "A kubi device was found");
		// Attempt to connect to the kubi
		manager.connectToKubi(result);
	}

	@Override
	public void kubiManagerFailed(KubiManager manager, int reason) {
		Log.i(TAG, "Failed. Reason: " + reason);
		if (reason == KubiManager.FAIL_CONNECTION_LOST || reason == KubiManager.FAIL_DISTANCE) {
			manager.findAllKubis();
		}
	}

	@Override
	public void kubiManagerStatusChanged(KubiManager manager, int oldStatus, int newStatus) {
		// When the Kubi has successfully connected, nod as a sign of success
		if (newStatus == KubiManager.STATUS_CONNECTED && oldStatus == KubiManager.STATUS_CONNECTING) {
			Kubi kubi = manager.getKubi();
			kubi.performGesture(Kubi.GESTURE_NOD);
		}
	}

	@Override
	public void kubiScanComplete(KubiManager manager, ArrayList<KubiSearchResult> result) {
		Log.i(TAG, "Kubi scan completed");
		Log.i(TAG, "Size of result is " + result.size());
		if(result.size() > 0) {
			manager.stopFinding();
			// Attempt to connect to the kubi
			manager.connectToKubi(result.get(0));
		}
	}

}
