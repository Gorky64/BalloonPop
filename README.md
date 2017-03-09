# BalloonPop
![alt text](http://i.imgur.com/WWmPvZT.jpg "Balloon Popper Game")

#About the application
* A simple game where the goal is to pop (touch) as many balloons as you can.
* If you miss a balloon, your score gets deducted.
* If a balloon de-spawns ( animation runs out ) you lose a life.
* When all lives are depleted game ends.

#The code
Game logic is contained in PopOnView class.

```java
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
        balloonHandler = new Handler();
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

    //Canceling the animations
    public void cancelAnimations() {
        for (Animator animator : animators)
            animator.cancel();
        for (ImageView view : balloons)
            relativeLayout.removeView(view);

        balloonHandler.removeCallbacks(addBalloonRunnable);
        animators.clear();
        balloons.clear();
    }

    public void resume(Context context) {
        gamePaused = false;
        initializeSoundEffects(context);
        if (!dialogDisplayed)
            resetGame();
    }

    //Resetting the game
    public void resetGame() {
        balloons.clear();
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
            balloonHandler.postDelayed(addBalloonRunnable, i * BALLOON_DELAY);
    }

    //Current and all time high score
    private void displayScores() {
        highScoreTextView.setText(resources.getString(R.string.high_score) + " " + highScore);
        currentScoreTextView.setText(resources.getString(R.string.current_score) + " " + score);
        levelTextView.setText(resources.getString(R.string.level) + " " + level);
    }

    //Sound effects - SoundPool with  Map
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

    //Balloon spawn
    public void addNewBalloon() {
        int x = random.nextInt(viewWidth - BALLOON_DIAMETER);
        int y = random.nextInt(viewHeight - BALLOON_DIAMETER);
        int x2 = random.nextInt(viewWidth - BALLOON_DIAMETER);
        int y2 = random.nextInt(viewHeight - BALLOON_DIAMETER);
        final ImageView balloon = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
        balloons.add(balloon);
        balloon.setLayoutParams(new RelativeLayout.LayoutParams(BALLOON_DIAMETER, BALLOON_DIAMETER));
        balloon.setImageResource(random.nextInt(2) == 0 ? R.drawable.balloon_blue : R.drawable.balloon_green);
        balloon.setX(x);
        balloon.setY(y);
        balloon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                touchedBalloon(balloon);
            }
        });

        //Adding and removing balloons
        relativeLayout.addView(balloon);
        balloon.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y).setDuration(animationTime)
        .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animators.add(animation);
            }
            public void onAnimationEnd(Animator animation) {
                animators.remove(animation);
                if (!gamePaused && balloons.contains(balloon)) {
                    missedBalloon(balloon);
                }
            }

        });
    }

    //Touch event - missed a balloon, score and sound effect
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (soundPool != null)
            soundPool.play(MISS_SOUND_ID, volume, volume, SOUND_PRIORITY, 1, 1);
        score -= 20;
        score = Math.max(score, 0);
        displayScores();
        return true;
    }

    //Touch event - touched a balloon, score and sound effect
    private void touchedBalloon(ImageView balloon) {
        relativeLayout.removeView(balloon);
        balloons.remove(balloon);
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
            addNewBalloon();
    }

    //Balloon de-spawned, Shared Preferences for keeping the high score
    public void missedBalloon(ImageView balloon) {
        balloons.remove(balloon);
        relativeLayout.removeView(balloon);
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

            //Restart game dialog
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
            addNewBalloon();
        }
    }

}
```
#High score reset and difficulty

For reseting the high score and changing difficulty i used the CustomPreferenceFragment.

```java
public class CustomPreferenceFragment extends PreferenceFragment {
    //High score reset
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference resetPref = findPreference("resetKEY");
        resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE, 0);
                editor.apply();
                PopOnView.highScore = 0;
                Toast.makeText(getActivity(), "High score set to 0.",
                        Toast.LENGTH_LONG).show();


                return true;
            }

        });
        //Easy Difficulty
        Preference easyPref = findPreference("easyKEY");
        easyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 4;
                PopOnView.score -= 10;
                PopOnView.MAX_LIVES=7;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to EASY",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
        //Medium Difficulty
        Preference mediumPref = findPreference("mediumKEY");
        mediumPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 6;
                PopOnView.score -= 15;
                PopOnView.MAX_LIVES=6;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to MEDIUM",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
        //Hard Difficulty
        Preference hardPref = findPreference("hardKEY");
        hardPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 8;
                PopOnView.score -= 20 * PopOnView.level;
                PopOnView.MAX_LIVES=5;
                PopOnView.BALLOON_DELAY=350;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to HARD",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
    }
}
```

The difficulty is handled by changing the number of points that are deducted, max lives, 
number of balloons appearing and the delay before the balloons start to shrink.
