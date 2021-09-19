package com.bbk.mrnommultiplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.AndroidGame;
/* {Ads} import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;*/

public class MrNomGame extends AndroidGame {
	/* {Ads} InterstitialAd mInterstitialAd;*/
	Handler handlerFromThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* {Ads} mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-7685223109248301/9376429870");

		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				requestNewInterstitial();
			}
		});*/

		requestNewInterstitial();

		handlerFromThread = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message message) {
				if (message.what == 0) {
					/* {Ads} if (mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
					}*/
				}
			}
		};
	}

	private void requestNewInterstitial() {
		/* {Ads} AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
				.build();

		mInterstitialAd.loadAd(adRequest);*/
	}

	public void showAd() {
		Message message = handlerFromThread.obtainMessage(0);
		message.sendToTarget();
	}

	@Override
	public Screen getStartScreen() {
		return new LoadingScreen(this);
	}
}
