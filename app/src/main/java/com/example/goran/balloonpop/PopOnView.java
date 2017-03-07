package com.example.goran.balloonpop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Goran on 26.2.2017..
 */


public class PopOnView extends View {
    public static String HIGH_SCORE = "HIGH_SCORE";
    private static final int INITIAL_ANIMATION_DURATION = 6000;
    private static final Random random = new Random();
    //Initial balloon size
    private static final int BALLOON_DIAMETER = 170;
    //Scales down the balloons
    private static final float SCALE_X = 0.30f;
    private static final float SCALE_Y = 0.30f;
    //Difficulty ( number of balloons spawning)
    public static int INITIAL_BALLOON = 5;
    //Time needed to start shrinking
    public static int BALLOON_DELAY = 500;
    //Starting lives
    private static final int LIVES = 3;
    //Max lives
    public static int MAX_LIVES = 7;
    private static final int NEW_LEVEL = 10;
    public static final int HIT_SOUND_ID = 1;
    public static final int MISS_SOUND_ID = 2;
    public static final int DISAPPEAR_SOUND_ID = 3;
    public static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 400;
    private static final int MAX_STREAMS = 1;
    public static final Queue<ImageView> spots = new ConcurrentLinkedQueue<>();
    public static final Queue<Animator> animators = new ConcurrentLinkedQueue<>();
    public static SharedPreferences preferences;
    private int balloonTouched;
    public static int score;
    public static int level;
    private int viewWidth;
    private int viewHeight;
    private long animationTime;
    private boolean gameOver;
    public static boolean gamePaused;
    private boolean dialogDisplayed;
    public static int highScore;
    private TextView highScoreTextView;
    private TextView currentScoreTextView;
    private TextView levelTextView;
    private LinearLayout livesLinearLayout;
    private RelativeLayout relativeLayout;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private Handler spotHandler;
    public static SoundPool soundPool;
    public static int volume;
    private Runnable addSpotRunnable = new Runnable() {
        public void run() {
            addNewSpot();
        }
    };

    public PopOnView(Context context, SharedPreferences sharedPreferences, RelativeLayout parentLayout) {
        super(context);
        preferences = sharedPreferences;
        highScore = preferences.getInt(HIGH_SCORE, 0);
        resources = context.getResources();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        relativeLayout = parentLayout;
        livesLinearLayout = (LinearLayout) relativeLayout.findViewById(R.id.livesLinearLayout);
        highScoreTextView = (TextView) relativeLayout.findViewById(R.id.highScoreTextView);
        currentScoreTextView = (TextView) relativeLayout.findViewById(R.id.currentScoreTextView);
        levelTextView = (TextView) relativeLayout.findViewById(R.id.levelTextView);
        spotHandler = new Handler();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        viewWidth = width;
        viewHeight = height;
    }

    public void pause() {
        gamePaused = true;
        soundPool.release();
        soundPool = null;
        cancelAnimations();
    }

    public void cancelAnimations() {
        for (Animator animator : animators)
            animator.cancel();
        for (ImageView view : spots)
            relativeLayout.removeView(view);

        spotHandler.removeCallbacks(addSpotRunnable);
        animators.clear();
        spots.clear();
    }

    public void resume(Context context) {
        gamePaused = false;
        initializeSoundEffects(context);
        if (!dialogDisplayed)
            resetGame();
    }

    public void resetGame() {
        spots.clear();
        animators.clear();
        livesLinearLayout.removeAllViews();
        animationTime = INITIAL_ANIMATION_DURATION;
        balloonTouched = 0;
        score = 0;
        level = 1;
        gameOver = false;
        displayScores();

        for (int i = 0; i < LIVES; i++) {
            livesLinearLayout.addView(layoutInflater.inflate(R.layout.life, null));
        }
        for (int i = 1; i <= INITIAL_BALLOON; i++)
            spotHandler.postDelayed(addSpotRunnable, i * BALLOON_DELAY);
    }

    private void displayScores() {
        highScoreTextView.setText(resources.getString(R.string.high_score) + " " + highScore);
        currentScoreTextView.setText(resources.getString(R.string.current_score) + " " + score);
        levelTextView.setText(resources.getString(R.string.level) + " " + level);
    }

    public static void initializeSoundEffects(Context context) {
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOUND_QUALITY);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Map<Integer, Integer> soundMap;
        soundMap = new HashMap<>();
        soundMap.put(HIT_SOUND_ID, soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
        soundMap.put(MISS_SOUND_ID, soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
        soundMap.put(DISAPPEAR_SOUND_ID, soundPool.load(context, R.raw.vanish, SOUND_PRIORITY));

    }

    public void addNewSpot() {
        int x = random.nextInt(viewWidth - BALLOON_DIAMETER);
        int y = random.nextInt(viewHeight - BALLOON_DIAMETER);
        int x2 = random.nextInt(viewWidth - BALLOON_DIAMETER);
        int y2 = random.nextInt(viewHeight - BALLOON_DIAMETER);
        final ImageView spot = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
        spots.add(spot);
        spot.setLayoutParams(new RelativeLayout.LayoutParams(BALLOON_DIAMETER, BALLOON_DIAMETER));
        spot.setImageResource(random.nextInt(2) == 0 ? R.drawable.balloon_blue : R.drawable.balloon_green);
        spot.setX(x);
        spot.setY(y);
        spot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                touchedSpot(spot);
            }
        });

        relativeLayout.addView(spot);
        spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y).setDuration(animationTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animators.add(animation);
            }
            public void onAnimationEnd(Animator animation) {
                animators.remove(animation);
                if (!gamePaused && spots.contains(spot)) {
                    missedSpot(spot);
                }
            }

        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (soundPool != null)
            soundPool.play(MISS_SOUND_ID, volume, volume, SOUND_PRIORITY, 1, 1);
        score -= 20;
        score = Math.max(score, 0);
        displayScores();
        return true;
    }

    private void touchedSpot(ImageView spot) {
        relativeLayout.removeView(spot);
        spots.remove(spot);
        ++balloonTouched;
        score += 10 * level;

        if (soundPool != null)
            soundPool.play(HIT_SOUND_ID, volume, volume, SOUND_PRIORITY, 1, 1);
        if (balloonTouched % NEW_LEVEL == 0) {
            ++level;
            animationTime *= 0.95;

            if (livesLinearLayout.getChildCount() < MAX_LIVES) {
                ImageView life = (ImageView) layoutInflater.inflate(R.layout.life, null);
                livesLinearLayout.addView(life);
            }
        }
        displayScores();
        if (!gameOver)
            addNewSpot();
    }

    public void missedSpot(ImageView spot) {
        spots.remove(spot);
        relativeLayout.removeView(spot);
        if (gameOver)
            return;
        if (soundPool != null)
            soundPool.play(DISAPPEAR_SOUND_ID, volume, volume, SOUND_PRIORITY, 1, 1);
        if (livesLinearLayout.getChildCount() == 0) {
            gameOver = true;
            if (score > highScore) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE, score);
                editor.apply();
                highScore = score;
            }
            cancelAnimations();

            Builder dialogBuilder = new Builder(getContext());
            dialogBuilder.setTitle(R.string.game_over);
            dialogBuilder.setMessage(resources.getString(R.string.current_score) + " " + score);
            dialogBuilder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    displayScores();
                    dialogDisplayed = false;
                    resetGame();
                }
            });
            dialogDisplayed = true;
            dialogBuilder.show();
        } else {
            livesLinearLayout.removeViewAt(livesLinearLayout.getChildCount() - 1);
            addNewSpot();
        }
    }

}
