package com.albert.soft.reaction;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener {
	private int mInterval1 = 1500; // 2000ms
	private int mInterval2 = 1500; // 2000ms
	private int mInterval3 = 5; // 左右的出现概率的比率
	private int mPauseInterval = 100; // 中间停留时间
	private int mRepeatTimesLeft = 0; // 左右重复次数
	private int mRepeatTimesRight = 0; // 左右重复次数
	
	
	private int mDetal = 100; //
	private int mDetal3 = 1; // 
	private int mDetalPauseInterval = 100; //

	private int mSound1 = 1;
	private int mSound2 = 2;
	
	private int mSoundLeft = 1;
	private int mSoundRight = 2;

	
	private HashMap<Integer, Integer> mId2BgMapPoolForRandom = null;
	private HashMap<Integer, Integer> mMapId2Interval = null;
	private HashMap<Integer, Integer> mMapId2Sound = null;
//	private HashMap<Integer, Integer> mId2BgMapPoolForRepaet = null;
	private Vector<Integer> mId2BgMapPoolForRepaet = null;
	private boolean mbStart = false;
	private Random mRandom = null;
	private int mPreDirection = -1;
	private int mDiffDirectionID = -1;
	private int mRepeatIndex = 0;

	private RelativeLayout mBgLayout = null;

	private Button mBtnUp1 = null;
	private Button mBtnDown1 = null;
	private TextView mTvLabel1 = null;

	private Button mBtnUp2 = null;
	private Button mBtnDown2 = null;
	private TextView mTvLabel2 = null;

	private Button mBtnUp3 = null;
	private Button mBtnDown3 = null;
	private TextView mTvLabel3 = null;

	private Button mBtnUp4 = null;
	private Button mBtnDown4 = null;
	private TextView mTvLabel4 = null;
	
	// repeat times left
	private Button mBtnRepeatTimesUpLeft = null;
	private Button mBtnRepeatTimesDownLeft = null;
	private TextView mTvRepeatTimesLeft = null;
	
	// repeat times right
	private Button mBtnRepeatTimesUpRight = null;
	private Button mBtnRepeatTimesDownRight = null;
	private TextView mTvRepeatTimesRight = null;	
	
	// stop times
	private Button mBtnStopTimesUp = null;
	private Button mBtnStopTimesDown = null;
	private TextView mTvStopTimes = null;	
	private int mStopTimes = 0; // 
	
	// stop interval
	private Button mBtnStopIntervalUp = null;
	private Button mBtnStopIntervalDown = null;
	private TextView mTvStopInterval = null;		
	private int mStopInterval = 0; // 

	private ToggleButton mBtnStop = null;
	private ToggleButton mBtnSound = null;
	
	private ImageView mImageView = null;

	private WakeLock m_wklk;
	private SharedPreferences mSharedPreferences;

	// sound
	private SoundPool sp;
	boolean mbPlaySound1 = false;
	
	private int mCurrentFrame = 0;
	private TextView mTextView = null;

	SoundPool TipsSoundPool;
	// int bbqid;
	// boolean mbPlaySound2 = false;

	private final int MSGID_BG_SHOW = 1;
	private final int MSGID_BG_STOP = 2;
	private final int MSGID_SOUND_PLAY = 3;

	private Handler mHandle = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSGID_BG_SHOW:
				if (mbStart) {
					PlayFrame();
					
					if(mStopTimes > 0 && mCurrentFrame % mStopTimes  == 0)
					{
						if(mStopInterval == 0)
						{
							int delay = mMapId2Interval.get(mPreDirection);
							mHandle.sendEmptyMessageDelayed(MSGID_BG_STOP, delay + mPauseInterval);
						}
						else
						{
							mHandle.sendEmptyMessageDelayed(MSGID_BG_SHOW, mStopInterval);
						}
					}
					else
					{
						int delay = mMapId2Interval.get(mPreDirection);
						mHandle.sendEmptyMessageDelayed(MSGID_BG_SHOW, delay + mPauseInterval);
					}

				} else {
					mHandle.removeMessages(MSGID_BG_SHOW);
				}
				break;
			case MSGID_BG_STOP:
				mBtnStop.setChecked(false);
//				if (mbStart) {
//					HideFrame();
//					int delay = mMapId2Interval.get(mPreDirection) * 1 / 2;
//					mHandle.sendEmptyMessageDelayed(MSGID_BG_SHOW, delay);
//				} else {
//					mHandle.removeMessages(MSGID_BG_SHOW);
//				}
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
		mPauseInterval = mSharedPreferences.getInt("mPauseInterval", 500);
		mbPlaySound1 = mSharedPreferences.getBoolean("mbPlaySound1", false);
		mRepeatTimesLeft = mSharedPreferences.getInt("mRepeatTimesLeft", 0);
		mRepeatTimesRight = mSharedPreferences.getInt("mRepeatTimesRight", 0);
		mStopTimes = mSharedPreferences.getInt("mStopTimes", 0);
		mStopInterval = mSharedPreferences.getInt("mStopInterval", 5000);

		mBgLayout = (RelativeLayout) findViewById(R.id.bglayout);

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
		
		mBtnUp4 = (Button) findViewById(R.id.ly4_up);
		mBtnDown4 = (Button) findViewById(R.id.ly4_down);
		mTvLabel4 = (TextView) findViewById(R.id.ly4_label);
		mBtnUp4.setOnClickListener(this);
		mBtnDown4.setOnClickListener(this);
		
		mBtnRepeatTimesUpLeft = (Button) findViewById(R.id.bt_repeattimes_up_left);
		mBtnRepeatTimesDownLeft = (Button) findViewById(R.id.bt_repeattimes_down_left);
		mTvRepeatTimesLeft = (TextView) findViewById(R.id.tv_repeattimes_left);
		mBtnRepeatTimesUpLeft.setOnClickListener(this);
		mBtnRepeatTimesDownLeft.setOnClickListener(this);
		
		mBtnRepeatTimesUpRight = (Button) findViewById(R.id.bt_repeattimes_up_right);
		mBtnRepeatTimesDownRight = (Button) findViewById(R.id.bt_repeattimes_down_right);
		mTvRepeatTimesRight = (TextView) findViewById(R.id.tv_repeattimes_right);
		mBtnRepeatTimesUpRight.setOnClickListener(this);
		mBtnRepeatTimesDownRight.setOnClickListener(this);
		
		mBtnStopTimesUp= (Button) findViewById(R.id.bt_stoptimes_up);
		mBtnStopTimesDown = (Button) findViewById(R.id.bt_stoptimes_down);
		mTvStopTimes = (TextView) findViewById(R.id.tv_stoptimes);
		mBtnStopTimesUp.setOnClickListener(this);
		mBtnStopTimesDown.setOnClickListener(this);
		
		mBtnStopIntervalUp= (Button) findViewById(R.id.bt_stopinterval_up);
		mBtnStopIntervalDown = (Button) findViewById(R.id.bt_stopinterval_down);
		mTvStopInterval = (TextView) findViewById(R.id.tv_stopinterval);
		mBtnStopIntervalUp.setOnClickListener(this);
		mBtnStopIntervalDown.setOnClickListener(this);

		mBtnStop = (ToggleButton) findViewById(R.id.tbtn1);
		mBtnStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtnStop.setChecked(isChecked);
				if (isChecked) {
					start();
				} else {
					stop();
				}
			}
		});

		mBtnSound = (ToggleButton) findViewById(R.id.tbtn2);
		mBtnSound.setChecked(mbPlaySound1);
		mBtnSound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtnSound.setChecked(isChecked);
				mbPlaySound1 = isChecked;
				
				mSharedPreferences.edit().putBoolean("mbPlaySound1", mbPlaySound1).commit();
			}
		});
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView.setImageResource(R.drawable.ic_ball);
		mImageView.setVisibility(View.INVISIBLE);

		mTextView = (TextView) findViewById(R.id.textView);
		mTextView.setText("" + mCurrentFrame);
		
		mTvLabel1.setText("" + mInterval1);
		mTvLabel2.setText("" + mInterval2);
		mTvLabel3.setText("" + mInterval3);
		mTvLabel4.setText("" + mPauseInterval);
		
		mTvRepeatTimesLeft.setText("" + mRepeatTimesLeft);
		mTvRepeatTimesRight.setText("" + mRepeatTimesRight);
		
		mTvStopTimes.setText("" + mStopTimes);
		mTvStopInterval.setText("" + mStopInterval);

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
			mBtnStop.setChecked(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop();
		mCurrentFrame = 0;
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
		if(mInterval3 < 4)
			mDiffDirectionID = R.drawable.left;
		else if(mInterval3 > 6)
			mDiffDirectionID = R.drawable.right;
		else
			mDiffDirectionID = -1;
		// id
		if (mId2BgMapPoolForRandom == null) {
			mId2BgMapPoolForRandom = new HashMap<Integer, Integer>();
		}

		mId2BgMapPoolForRandom.clear();
		for (int i = 0; i < 100; i++) {
			if (i < mInterval3*10) {
				mId2BgMapPoolForRandom.put(i, R.drawable.left);
			} else {
				mId2BgMapPoolForRandom.put(i, R.drawable.right);
			}
		}

		// interval
		if (mMapId2Interval == null) {
			mMapId2Interval = new HashMap<Integer, Integer>();
		}
		mMapId2Interval.clear();
		mMapId2Interval.put(R.drawable.left, mInterval1);
		mMapId2Interval.put(R.drawable.right, mInterval2);

//		for (int i = 0; i < 10; i++) {
//			if (i < mInterval3) {
//			} else {
//			}
//		}
		
		if (mId2BgMapPoolForRepaet == null) {
			mId2BgMapPoolForRepaet = new Vector<Integer>();
		}

		mId2BgMapPoolForRepaet.clear();
		mRepeatIndex = 0;
		for (int i = 0; i < mRepeatTimesLeft; i++) {
			mId2BgMapPoolForRepaet.add(R.drawable.left);
		}

		for (int i = 0; i < mRepeatTimesRight; i++) {
			mId2BgMapPoolForRepaet.add(R.drawable.right);
		}

		// sound
//		if (mMapId2Sound == null) {
//			mMapId2Sound = new HashMap<Integer, Integer>();
//		}
//
//		mMapId2Sound.clear();
//		for (int i = 0; i < 100; i++) {
//			if (i < mInterval3 * 10) {
//				mMapId2Sound.put(i, mSound1);
//			} else {
//				mMapId2Sound.put(i, mSound2);
//			}
//		}
	}

	public void InitSound() {
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mSound1 = sp.load(this, R.raw.sound1, 1);
		mSound2 = sp.load(this, R.raw.sound2, 1);
		

		TipsSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mSoundLeft = TipsSoundPool.load(this, R.raw.left, 1);
		mSoundRight = TipsSoundPool.load(this, R.raw.right, 1);
	}

	private void PlayFrame() {
		int directionID = 0;
		
		if(mRepeatTimesLeft <= 0 || mRepeatTimesRight <= 0)
		{
			do {
				int r = mRandom.nextInt(mId2BgMapPoolForRandom.size());
				//Log.v("PPTrain", "r:" + r);
				directionID = mId2BgMapPoolForRandom.get( r );
			} while (mDiffDirectionID == directionID && mPreDirection == directionID);
		}
		else
		{
			directionID = mId2BgMapPoolForRepaet.get(mRepeatIndex%mId2BgMapPoolForRepaet.size());
			mRepeatIndex ++ ;
		}

		mPreDirection = directionID;
		//Log.v("PPTrain", "randomId:" + mPreDirection);
//		mBgLayout.setBackgroundResource(mId2BgMap.get(mCurRandomId));
		
		PlayAmination(mPreDirection, mMapId2Interval.get(mPreDirection));
		
		mCurrentFrame ++;
		mTextView.setText("" + mCurrentFrame);

		// 提示声音
		// playSound();

		// 球的声音
		// mHandle.sendEmptyMessageDelayed(2, mbPlaySound1 ? 400 : 0);
	}

	private void HideFrame() {
//		mBgLayout.setBackgroundColor(0xFFFFFFFF);
	}

	public void playSound(int soundId, SoundPool ssp) {
		if (mbPlaySound1) {
			AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			float volumnRatio = volumnCurrent / audioMaxVolumn;
			ssp.play(soundId/*mMapId2Sound.get(mCurRandomId)*/, volumnRatio, volumnRatio, 1, 0, 1f);
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

	private void refreshAndSave(TextView textView, int value, boolean initProb) {
		textView.setText("" + value);
		if (initProb) {
			initProbability();
		}

		// save
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("mInterval1", mInterval1);
		edit.putInt("mInterval2", mInterval2);
		edit.putInt("mInterval3", mInterval3);
		edit.putInt("mPauseInterval", mPauseInterval);
		edit.putInt("mRepeatTimesLeft", mRepeatTimesLeft);
		edit.putInt("mRepeatTimesRight", mRepeatTimesRight);
		edit.putInt("mStopTimes", mStopTimes);
		edit.putInt("mStopInterval", mStopInterval);
		edit.commit();
	}

	private void start() {
		mHandle.sendEmptyMessageDelayed(MSGID_BG_SHOW, 500);
		mImageView.setVisibility(View.VISIBLE);
		mbStart = true;
		mRepeatIndex = 0;
//		mBtnStop.setChecked(true);
	}

	private void stop() {
		mHandle.removeMessages(MSGID_BG_SHOW);
		mImageView.setVisibility(View.INVISIBLE);
//		mBtnStop.setChecked(false);
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
				refreshAndSave(mTvLabel1, mInterval1, true);
			}
		}
			break;
		case R.id.ly1_down: {
			int t = mInterval1 - mDetal;
			if (t > 0) {
				mInterval1 = t;
				refreshAndSave(mTvLabel1, mInterval1, true);
			}
		}
			break;

		case R.id.ly2_up: {
			int t = mInterval2 + mDetal;
			if (t > 0) {
				mInterval2 = t;
				refreshAndSave(mTvLabel2, mInterval2, true);
			}
		}
			break;
		case R.id.ly2_down: {
			int t = mInterval2 - mDetal;
			if (t > 0) {
				mInterval2 = t;
				refreshAndSave(mTvLabel2, mInterval2, true);
			}
		}
			break;

		case R.id.ly3_up: {
			int t = mInterval3 + mDetal3;
			if (t > 0) {
				mInterval3 = t;
				refreshAndSave(mTvLabel3, mInterval3, true);
			}
		}
			break;
		case R.id.ly3_down: {
			int t = mInterval3 - mDetal3;
			if (t > 0) {
				mInterval3 = t;
				refreshAndSave(mTvLabel3, mInterval3, true);
			}
		}
			break;
		case R.id.ly4_up: {
			int t = mPauseInterval + mDetalPauseInterval;
			if (t >= 0) {
				mPauseInterval = t;
				refreshAndSave(mTvLabel4, mPauseInterval, false);
			}
		}
			break;
		case R.id.ly4_down: {
			int t = mPauseInterval - mDetalPauseInterval;
			if (t >= 0) {
				mPauseInterval = t;
				refreshAndSave(mTvLabel4, mPauseInterval, false);
			}
		}
			break;
		case R.id.bt_repeattimes_up_left: {
			int t = mRepeatTimesLeft + 1;
			if (t <= 10) {
				mRepeatTimesLeft = t;
				refreshAndSave(mTvRepeatTimesLeft, mRepeatTimesLeft, true);
			}
		}
			break;
		case R.id.bt_repeattimes_down_left: {
			int t = mRepeatTimesLeft - 1;
			if (t >= 0) {
				mRepeatTimesLeft = t;
				refreshAndSave(mTvRepeatTimesLeft, mRepeatTimesLeft, true);
			}
		}
			break;
		case R.id.bt_repeattimes_up_right: {
			int t = mRepeatTimesRight + 1;
			if (t <= 10) {
				mRepeatTimesRight = t;
				refreshAndSave(mTvRepeatTimesRight, mRepeatTimesRight, true);
			}
		}
			break;
		case R.id.bt_repeattimes_down_right: {
			int t = mRepeatTimesRight- 1;
			if (t >= 0) {
				mRepeatTimesRight = t;
				refreshAndSave(mTvRepeatTimesRight, mRepeatTimesRight, true);
			}
		}
			break;			
		case R.id.bt_stoptimes_up: {
			int t = mStopTimes + 1;
			if (t <= 10) {
				mStopTimes = t;
				refreshAndSave(mTvStopTimes, mStopTimes, true);
			}
		}
			break;
		case R.id.bt_stoptimes_down: {
			int t = mStopTimes- 1;
			if (t >= 0) {
				mStopTimes = t;
				refreshAndSave(mTvStopTimes, mStopTimes, true);
			}
		}
			break;		
		case R.id.bt_stopinterval_up: {
			int t = mStopInterval + 1000;
			if (t <= 20000) {
				mStopInterval = t;
				refreshAndSave(mTvStopInterval, mStopInterval, true);
			}
		}
			break;
		case R.id.bt_stopinterval_down: {
			int t = mStopInterval- 1000;
			if (t >= 0) {
				mStopInterval = t;
				refreshAndSave(mTvStopInterval, mStopInterval, true);
			}
		}
			break;				
		}

	}
	
	
	// 播放动画
	private void PlayAmination(int direction, int duration)
	{
		final int direct = direction;
		playSound(direct == R.drawable.left ? mSoundLeft:mSoundRight, TipsSoundPool);
		playSound(mSound1, sp);
		mHandle.postDelayed(new Runnable(){   
		    public void run() {   
		    	playSound(mSound2, sp);
		    }   
		 }, duration / 4 * 3);
		
		// 去球
		AnimationSet animationSet1 = new AnimationSet(true);
		TranslateAnimation transAnimation1 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, (direction == R.drawable.left? 0f:1f), 
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, 1f);
		transAnimation1.setDuration(duration / 4 * 3);
		animationSet1.addAnimation(transAnimation1);
		
		// 回来
		final AnimationSet animationSet2 = new AnimationSet(true);
		TranslateAnimation transAnimation2 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  (direction == R.drawable.left? 0f:1f), 
				Animation.RELATIVE_TO_PARENT, 0f, 
				Animation.RELATIVE_TO_PARENT, 1f, 
				Animation.RELATIVE_TO_PARENT, 0f);
		transAnimation2.setDuration(duration / 4 * 1);
		animationSet2.addAnimation(transAnimation2);
		
		animationSet1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation paramAnimation) {
				if(mbStart){
					// 方向提示
//					playSound(mSound1, sp);
				}
			}
			
			@Override
			public void onAnimationRepeat(Animation paramAnimation) {
			}
			
			@Override
			public void onAnimationEnd(Animation paramAnimation) {
				if(mbStart){
					mImageView.startAnimation(animationSet2);
				}
			}
		});
		
		animationSet2.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation paramAnimation) {
				if(mbStart){
//					playSound(mSound2, sp);
				}
			}
			
			@Override
			public void onAnimationRepeat(Animation paramAnimation) {
			}
			
			@Override
			public void onAnimationEnd(Animation paramAnimation) {
			}
		});
		
		mImageView.startAnimation(animationSet1);
	}

}
