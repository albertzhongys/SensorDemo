package com.albert.soft.reaction;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AminActivity extends Activity implements OnClickListener {
	private int mInterval1 = 1500; // 2000ms
	private int mInterval2 = 1500; // 2000ms
	private int mInterval3 = 5; //

	private int mDetal = 100; //
	private int mDetal3 = 1; //

	private int mSound1 = 1;
	private int mSound2 = 2;

	private HashMap<Integer, Integer> mId2BgMap = null;
	private HashMap<Integer, Integer> mMapId2Interval = null;
	private HashMap<Integer, Integer> mMapId2Sound = null;
	private boolean mbStart = false;
	Random mRandom = null;
	int mCurRandomId = -1;
	int mDiffId = -1;

	private LinearLayout mBgLayout = null;

	private Button mBtnUp1 = null;
	private Button mBtnDown1 = null;
	private TextView mTvLabel1 = null;

	private Button mBtnUp2 = null;
	private Button mBtnDown2 = null;
	private TextView mTvLabel2 = null;

	private Button mBtnUp3 = null;
	private Button mBtnDown3 = null;
	private TextView mTvLabel3 = null;

	private ToggleButton tBtn1 = null;
	private ToggleButton tBtn2 = null;
	
	private ImageView mImageView = null;

	WakeLock m_wklk;
	SharedPreferences mSharedPreferences;

	// sound
	SoundPool sp;
	boolean mbPlaySound1 = false;

	// SoundPool bbqsp;
	// int bbqid;
	// boolean mbPlaySound2 = false;

	private final int MSGID_BG_SHOW = 1;
	private final int MSGID_BG_HIDE = 2;
	private final int MSGID_SOUND_PLAY = 3;

	private Handler mHandle = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSGID_BG_SHOW:
				if (mbStart) {
					PlayFrame();
					int delay = mMapId2Interval.get(mCurRandomId) * 1 / 3;
					mHandle.sendEmptyMessageDelayed(MSGID_BG_HIDE, delay);
				} else {
					mHandle.removeMessages(MSGID_BG_SHOW);
				}
				break;
			case MSGID_BG_HIDE:
				if (mbStart) {
					HideFrame();
					int delay = mMapId2Interval.get(mCurRandomId) * 2 / 3;
					mHandle.sendEmptyMessageDelayed(MSGID_BG_SHOW, delay);
				} else {
					mHandle.removeMessages(MSGID_BG_SHOW);
				}
				break;
			case MSGID_SOUND_PLAY:
				if (mbStart) {
					// playPPQ();
				}
				break;
			default:
				break;
			}

			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSharedPreferences = getSharedPreferences("TestSharedPreferences", MODE_PRIVATE);
		mInterval1 = mSharedPreferences.getInt("mInterval1", 1000);
		mInterval2 = mSharedPreferences.getInt("mInterval2", 1000);
		mInterval3 = mSharedPreferences.getInt("mInterval3", 5);
		mbPlaySound1 = mSharedPreferences.getBoolean("mbPlaySound1", false);

		mBgLayout = (LinearLayout) findViewById(R.id.bglayout);

		mBtnUp1 = (Button) findViewById(R.id.ly1_up);
		mBtnDown1 = (Button) findViewById(R.id.ly1_down);
		mTvLabel1 = (TextView) findViewById(R.id.ly1_label);
		mBtnUp1.setOnClickListener(this);
		mBtnDown1.setOnClickListener(this);

		mBtnUp2 = (Button) findViewById(R.id.ly2_up);
		mBtnDown2 = (Button) findViewById(R.id.ly2_down);
		mTvLabel2 = (TextView) findViewById(R.id.ly2_label);
		mBtnUp2.setOnClickListener(this);
		mBtnDown2.setOnClickListener(this);

		mBtnUp3 = (Button) findViewById(R.id.ly3_up);
		mBtnDown3 = (Button) findViewById(R.id.ly3_down);
		mTvLabel3 = (TextView) findViewById(R.id.ly3_label);
		mBtnUp3.setOnClickListener(this);
		mBtnDown3.setOnClickListener(this);

		tBtn1 = (ToggleButton) findViewById(R.id.tbtn1);
		tBtn1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				tBtn1.setChecked(isChecked);
				if (isChecked) {
					start();
				} else {
					stop();
				}
			}
		});

		tBtn2 = (ToggleButton) findViewById(R.id.tbtn2);
		tBtn2.setChecked(mbPlaySound1);
		tBtn2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				tBtn2.setChecked(isChecked);
				mbPlaySound1 = isChecked;
				
				mSharedPreferences.edit().putBoolean("mbPlaySound1", mbPlaySound1).commit();
			}
		});
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView.setImageResource(R.drawable.ic_launcher);

		mTvLabel1.setText("" + mInterval1);
		mTvLabel2.setText("" + mInterval2);
		mTvLabel3.setText("" + mInterval3);

		initData();
		InitSound();

//		start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		acquirWakeLock();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseWakeLock();
		if(!mbPlaySound1){
			stop();
			tBtn1.setChecked(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void initData() {
		initProbability();
		mRandom = new Random(System.currentTimeMillis());
	}

	private void initProbability() {
		if(mInterval3 < 3)
			mDiffId = R.drawable.left;
		else if(mInterval3 > 7)
			mDiffId = R.drawable.right;
		else
			mDiffId = -1;
		// id
		if (mId2BgMap == null) {
			mId2BgMap = new HashMap<Integer, Integer>();
		}

		mId2BgMap.clear();
		for (int i = 0; i < 10; i++) {
			if (i < mInterval3) {
				mId2BgMap.put(i, R.drawable.left);
			} else {
				mId2BgMap.put(i, R.drawable.right);
			}
		}

		// interval
		if (mMapId2Interval == null) {
			mMapId2Interval = new HashMap<Integer, Integer>();
		}

		mMapId2Interval.clear();
		for (int i = 0; i < 10; i++) {
			if (i < mInterval3) {
				mMapId2Interval.put(i, mInterval1);
			} else {
				mMapId2Interval.put(i, mInterval2);
			}
		}

		// sound
		if (mMapId2Sound == null) {
			mMapId2Sound = new HashMap<Integer, Integer>();
		}

		mMapId2Sound.clear();
		for (int i = 0; i < 10; i++) {
			if (i < mInterval3) {
				mMapId2Sound.put(i, mSound1);
			} else {
				mMapId2Sound.put(i, mSound2);
			}
		}
	}

	public void InitSound() {
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mSound1 = sp.load(this, R.raw.left, 1);
		mSound2 = sp.load(this, R.raw.right, 1);

		// bbqsp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	}

	private void PlayFrame() {
		int randomId = 0;
		do {
			randomId = mRandom.nextInt(mId2BgMap.size());
		} while (mDiffId == randomId && mCurRandomId == randomId);

		mCurRandomId = randomId;
		Log.v("PPTrain", "randomId:" + mCurRandomId);
//		mBgLayout.setBackgroundResource(mId2BgMap.get(mCurRandomId));
		
		PlayAmination(mId2BgMap.get(mCurRandomId), mMapId2Interval.get(mCurRandomId));

		// 提示声音
		playSound();

		// 球的声音
		// mHandle.sendEmptyMessageDelayed(2, mbPlaySound1 ? 400 : 0);
	}

	private void HideFrame() {
//		mBgLayout.setBackgroundColor(0xFFFFFFFF);
	}

	public void playSound() {
		if (mbPlaySound1) {
			AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			float volumnRatio = volumnCurrent / audioMaxVolumn;

			sp.play(mMapId2Sound.get(mCurRandomId), volumnRatio, volumnRatio, 1, 0, 1f);
		}
	}

//	private void playPPQ() {
//		if (mbPlaySound2) {
//			AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//			float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//			float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//			float volumnRatio = volumnCurrent / audioMaxVolumn;
//
//			bbqsp.play(bbqid, volumnRatio, volumnRatio, 1, 0, 1f);
//
//		}
//	}

	private void refreshTime(TextView textView, int value) {
		textView.setText("" + value);

		initProbability();

		// save
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("mInterval1", mInterval1);
		edit.putInt("mInterval2", mInterval2);
		edit.putInt("mInterval3", mInterval3);
		edit.commit();
	}

	private void start() {
		mHandle.sendEmptyMessage(MSGID_BG_SHOW);
		mbStart = true;
	}

	private void stop() {
		mHandle.removeMessages(MSGID_BG_SHOW);
		mbStart = false;
	}

	public void acquirWakeLock() {
		if (m_wklk != null) {
			m_wklk.acquire();
		} else {
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "cn");
			m_wklk.acquire();
		}
	}

	public void releaseWakeLock() {
		if (m_wklk != null) {
			m_wklk.release();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ly1_up: {
			int t = mInterval1 + mDetal;
			if (t > 0) {
				mInterval1 = t;
				refreshTime(mTvLabel1, mInterval1);
			}
		}
			break;
		case R.id.ly1_down: {
			int t = mInterval1 - mDetal;
			if (t > 0) {
				mInterval1 = t;
				refreshTime(mTvLabel1, mInterval1);
			}
		}
			break;

		case R.id.ly2_up: {
			int t = mInterval2 + mDetal;
			if (t > 0) {
				mInterval2 = t;
				refreshTime(mTvLabel2, mInterval2);
			}
		}
			break;
		case R.id.ly2_down: {
			int t = mInterval2 - mDetal;
			if (t > 0) {
				mInterval2 = t;
				refreshTime(mTvLabel2, mInterval2);
			}
		}
			break;

		case R.id.ly3_up: {
			int t = mInterval3 + mDetal3;
			if (t > 0) {
				mInterval3 = t;
				refreshTime(mTvLabel3, mInterval3);
			}
		}
			break;
		case R.id.ly3_down: {
			int t = mInterval3 - mDetal3;
			if (t > 0) {
				mInterval3 = t;
				refreshTime(mTvLabel3, mInterval3);
			}
		}
			break;
		}

	}
	
	private void PlayAmination(int direction, int duration)
	{
		AnimationSet animationSet1 = new AnimationSet(true);
		TranslateAnimation transAnimation1 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, (direction == R.drawable.left? 0f:1f), 
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, 1f);
		transAnimation1.setDuration(duration / 3 * 2);
		animationSet1.addAnimation(transAnimation1);
		
		final AnimationSet animationSet2 = new AnimationSet(true);
		TranslateAnimation transAnimation2 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  (direction == R.drawable.left? 0f:1f), 
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, 1f, 
				Animation.RELATIVE_TO_PARENT, 0f);
		transAnimation2.setDuration(duration / 3 * 1);
		animationSet2.addAnimation(transAnimation2);
		
		mImageView.startAnimation(animationSet1);
		animationSet1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation paramAnimation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation paramAnimation) {
			}
			
			@Override
			public void onAnimationEnd(Animation paramAnimation) {
				mImageView.startAnimation(animationSet2);
			}
		});
	}

}
