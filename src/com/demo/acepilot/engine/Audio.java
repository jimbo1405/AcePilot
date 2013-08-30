package com.demo.acepilot.engine;

import java.util.HashMap;
import java.util.Map;

import com.demo.acepilot.R;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Audio implements IModule {

	private static final Map<Context, Audio> audioLookup;
	private final Context context;
	private MediaPlayer currentBackground;
	private int currentBackgroundResID;
	private boolean moduleEnabled = true;
	private float music_volume = 1.0f;
	private float sfx_volume = 1.0f;
	private MusicState musicState = MusicState.IDLE;
	private SFXState sfxState = SFXState.IDLE;
	
	static {
		audioLookup = new HashMap<Context, Audio>();
	}
	
	private static enum MusicState {
		IDLE,
		PAUSE,
		PLAYING;
	}
	
	private static enum SFXState {
		IDLE,
		PAUSE,
		PLAYING;
	}
	
	public static enum Music {
		MAIN_MENU(R.raw.battlelands),
		PLAYING(R.raw.angryrobotiii);
		
		private final int resource_id;
		
		private Music (int resource_id){
			this.resource_id = resource_id;
		}
	}
	
	public static enum SFX {
		CLICK(R.raw.cameraflash),
		EXPLOSION(R.raw.explode3),
		COIN(R.raw.money),
		BAN(R.raw.nono);
		
		private final int resource_id;
		
		private SFX (int resource_id){
			this.resource_id = resource_id;
		}
	}

	/**
	 * Returns an {@code AudioResource} associated with the specified {@link Context}.
	 * @param context: the {@link Context} associated with the returned {@code AudioResource}
	 * @return an {@code AudioResource} associated with the specified {@link Context}.
	 */
	public static Audio getInstance(Context context){
		Audio resource = audioLookup.get(context);
		if(resource == null){
			resource = new Audio(context);
			audioLookup.put(context, resource);
		}
		return resource;
	}
	
	private Audio(Context context){
		this.context = context;
	}
	
	/**
	 * Plays the specified {@link SFX} if there is no other sound effect playing
	 * @param sound the {@link SFX} to play
	 */
	public void play(SFX sound){
		if(!moduleEnabled){
			sfxState = SFXState.IDLE;
			return;
		}
		
		if(sound == null)
			throw new NullPointerException();
		final MediaPlayer sfx = MediaPlayer.create(context, sound.resource_id);
		sfx.setVolume(sfx_volume, sfx_volume);
		sfx.start();
		sfx.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				sfx.release();
				sfxState = SFXState.IDLE;
			}
		});
		sfxState = SFXState.PLAYING;
	}
	
	private final void stopBackgroundMusic(){
		musicState = MusicState.IDLE;
		if(!moduleEnabled) return;
		assert currentBackground != null;
		currentBackground.stop();
		currentBackground.release();
		currentBackgroundResID = 0;
	}
	
	/**
	 * Plays the specified {@link Music}
	 * @param music: the {@link Music} to play
	 */
	public final void play(Music music)
	{
		play(music, false);
	}
	
	/**
	 * Plays the specified {@link Music}
	 * @param music the {@link Music} to play
	 * @param restart if {@link Music} is the same as current background music, replay it from start
	 */
	public final void play(Music music, boolean restart){
		if(music == null)
			throw new NullPointerException();
		if(currentBackground != null){
			if((restart == true)||
			   ((restart == false) && (currentBackgroundResID != music.resource_id)))
				stopBackgroundMusic();
		}
		if(musicState == MusicState.IDLE){
			currentBackground = MediaPlayer.create(context, music.resource_id);
			currentBackground.setVolume(music_volume, music_volume);
			currentBackground.setLooping(true);
			currentBackgroundResID = music.resource_id;
			if(moduleEnabled){
				currentBackground.start();
				musicState = MusicState.PLAYING;
			} else {
				currentBackground.start();
				currentBackground.pause();
				musicState = MusicState.PAUSE;
			}
		}else if(musicState == MusicState.PAUSE) {
			if(moduleEnabled){
				currentBackground.start();
				musicState = MusicState.PLAYING;
			}
		}
	}
	
	/**
	 * Set {@link Music} volume
	 */
	public final void setMusicVolume(float music_volume){
		if(music_volume < 0 || music_volume > 1)
			throw new IllegalArgumentException();
		this.music_volume = music_volume;
		if(currentBackground == null)
			return;
		currentBackground.setVolume(music_volume, music_volume);
	}
	
	/**
	 * Get {@link Music} volume
	 */
	public final float getMusicVolume(){
		return music_volume;
	}
	
	/**
	 * Set sound volume
	 */
	public final void setSFXVolume(float fx_volume){
		if(fx_volume < 0 || fx_volume > 1)
			throw new IllegalArgumentException();
		this.sfx_volume = fx_volume;
	}
	
	/**
	 * Get sound volume
	 */
	public final float getSFXVolume(){
		return sfx_volume;
	}

	/**
	 * Pause {@link Music}
	 */
	public void pauseMusic() {
		if(!moduleEnabled) return;
		if(currentBackground == null) return;
		currentBackground.pause();
		musicState = MusicState.PAUSE;
	}

	/**
	 * Resume {@link Music}
	 */
	public void resumeMusic() {
		if(!moduleEnabled) return;
		if(currentBackground == null /*|| currentBackground.isLooping()*/) return;
		currentBackground.start();
		musicState = MusicState.PLAYING;
	}

	/**
	 * Enable {@link Audio} module
	 */
	@Override
	public void enable() {
		moduleEnabled = true;
		if((musicState == MusicState.IDLE) || (musicState == MusicState.PAUSE)) {
			if(currentBackground == null /*|| currentBackground.isLooping()*/) return;
			currentBackground.start();
			musicState = MusicState.PLAYING;
		}
	}

	/**
	 * Disable {@link Audio} module
	 */
	@Override
	public void disable() {
		moduleEnabled = false;
		if(musicState == MusicState.PLAYING){
			if(currentBackground == null) return;
			currentBackground.pause();
			musicState = MusicState.PAUSE;
		}
	}

	/**
	 * Destroy {@link Audio} module
	 */
	@Override
	public void destroy() {
		if(currentBackground != null)
			stopBackgroundMusic();
		moduleEnabled = false;	
	}

}
