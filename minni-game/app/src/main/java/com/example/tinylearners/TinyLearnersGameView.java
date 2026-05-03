package com.example.tinylearners;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;
import java.util.Random;

public class TinyLearnersGameView extends View implements TextToSpeech.OnInitListener {
    private static final String GAME_TITLE = "minni game";

    private static final int INTRO = 0;
    private static final int HOME = 1;
    private static final int NUMBER_PICK = 2;
    private static final int NUMBER_FEED = 3;
    private static final int COLOURS = 4;
    private static final int ALPHABETS = 5;

    private static final int TEXT = Color.rgb(31, 34, 45);
    private static final int MUTED = Color.rgb(92, 94, 105);
    private static final int CREAM = Color.rgb(255, 253, 236);
    private static final int CARD = Color.rgb(255, 255, 250);
    private static final int GREEN = Color.rgb(87, 205, 109);
    private static final int RED = Color.rgb(241, 91, 67);
    private static final int BLUE = Color.rgb(74, 132, 232);
    private static final int YELLOW = Color.rgb(255, 214, 81);
    private static final int PURPLE = Color.rgb(183, 43, 219);
    private static final int ORANGE = Color.rgb(240, 122, 48);
    private static final int WHITE = Color.WHITE;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
    private final ToneGenerator musicTone = new ToneGenerator(AudioManager.STREAM_MUSIC, 12);

    private final RectF homeButton = new RectF();
    private final RectF muteButton = new RectF();
    private final RectF numbersButton = new RectF();
    private final RectF coloursButton = new RectF();
    private final RectF alphabetsButton = new RectF();
    private final RectF feedButton = new RectF();
    private final RectF resetButton = new RectF();
    private final RectF[] animalCards = {new RectF(), new RectF(), new RectF()};
    private final RectF[] answerCards = {new RectF(), new RectF(), new RectF()};

    private final String[] animalNames = {"Monkey", "Elephant", "Chicken"};
    private final String[] animalEmoji = {"🐵", "🐘", "🐔"};
    private final String[] foodNames = {"Banana", "Sugarcane", "Peanuts"};
    private final String[] foodEmoji = {"🍌", "🎋", "🥜"};
    private final int[] foodAccent = {ORANGE, GREEN, Color.rgb(194, 132, 72)};

    private final int[] colourValues = {BLUE, GREEN, WHITE, PURPLE, RED, YELLOW, ORANGE};
    private final String[] colourNames = {"Blue", "Green", "White", "Purple", "Red", "Yellow", "Orange"};
    private final String[] colourObjectNames = {"Ball", "Bird", "Flower", "Car", "Fish", "Kite", "Star"};
    private final int[] answerValues = new int[3];

    private final String[] alphabetLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private final String[] alphabetWords = {"Apple", "Ball", "Cat", "Dog", "Elephant", "Fish", "Grapes", "Hat", "Ice Cream", "Jug"};
    private final String[] alphabetEmoji = {"🍎", "⚽", "🐱", "🐶", "🐘", "🐟", "🍇", "🎩", "🍦", "🏺"};

    private final int[] musicNotes = {
            ToneGenerator.TONE_DTMF_1,
            ToneGenerator.TONE_DTMF_3,
            ToneGenerator.TONE_DTMF_6,
            ToneGenerator.TONE_DTMF_3
    };

    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private boolean muted = false;
    private boolean animationRunning = false;
    private boolean musicRunning = false;
    private boolean celebrating = false;
    private int scene = INTRO;
    private int musicIndex = 0;
    private long introStart;
    private long now;

    private int selectedAnimal = 0;
    private int foodCount = 0;
    private float pop = 0f;

    private int colourRound = 0;
    private int colourTarget = 0;
    private int colourObject = 0;

    private int alphabetRound = 0;
    private int alphabetTarget = 0;

    public TinyLearnersGameView(Context context) {
        super(context);
        setFocusable(true);
        introStart = System.currentTimeMillis();
        now = introStart;
        textToSpeech = new TextToSpeech(context.getApplicationContext(), this);
        startAnimation();
        startMusic();
    }

    @Override
    public void onInit(int status) {
        ttsReady = status == TextToSpeech.SUCCESS;
        if (ttsReady) {
            textToSpeech.setLanguage(Locale.US);
            chooseFriendlyVoice();
            textToSpeech.setPitch(1.12f);
            textToSpeech.setSpeechRate(0.86f);
            speak("Welcome to " + GAME_TITLE);
        }
    }

    private void chooseFriendlyVoice() {
        Voice bestVoice = null;
        if (textToSpeech.getVoices() == null) {
            return;
        }
        for (Voice voice : textToSpeech.getVoices()) {
            if (voice == null || voice.getLocale() == null || voice.isNetworkConnectionRequired()) {
                continue;
            }
            if (!Locale.ENGLISH.getLanguage().equals(voice.getLocale().getLanguage())) {
                continue;
            }
            if (bestVoice == null || voice.getQuality() > bestVoice.getQuality()) {
                bestVoice = voice;
            }
        }
        if (bestVoice != null) {
            textToSpeech.setVoice(bestVoice);
        }
    }

    private void startAnimation() {
        if (animationRunning) {
            return;
        }
        animationRunning = true;
        handler.post(animationLoop);
    }

    private final Runnable animationLoop = new Runnable() {
        @Override
        public void run() {
            if (!animationRunning) {
                return;
            }
            now = System.currentTimeMillis();
            if (scene == INTRO && now - introStart > 3600) {
                scene = HOME;
                speak("Choose a game");
            }
            if (pop > 0f) {
                pop = Math.max(0f, pop - 0.06f);
            }
            invalidate();
            handler.postDelayed(this, 33);
        }
    };

    private void startMusic() {
        if (musicRunning) {
            return;
        }
        musicRunning = true;
        handler.postDelayed(musicLoop, 900);
    }

    private final Runnable musicLoop = new Runnable() {
        @Override
        public void run() {
            if (!musicRunning) {
                return;
            }
            if (!muted) {
                musicTone.startTone(musicNotes[musicIndex], 150);
                musicIndex = (musicIndex + 1) % musicNotes.length;
            }
            handler.postDelayed(this, 1200);
        }
    };

    private void speak(String text) {
        if (!ttsReady || muted || text == null) {
            return;
        }
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "laddu_voice");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        drawSoftBackground(canvas, width, height);

        if (scene == INTRO) {
            drawIntro(canvas, width, height);
        } else if (scene == HOME) {
            drawHome(canvas, width, height);
        } else if (scene == NUMBER_PICK) {
            drawNumberPick(canvas, width, height);
        } else if (scene == NUMBER_FEED) {
            drawNumberFeed(canvas, width, height);
        } else if (scene == COLOURS) {
            drawColours(canvas, width, height);
        } else if (scene == ALPHABETS) {
            drawAlphabets(canvas, width, height);
        }

        drawMuteButton(canvas, width);
        if (celebrating) {
            drawWinPopup(canvas, width, height);
        }
    }

    private void drawSoftBackground(Canvas canvas, int width, int height) {
        paint.setShader(new LinearGradient(0, 0, 0, height,
                Color.rgb(255, 247, 105),
                Color.rgb(77, 197, 234),
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        paint.setColor(Color.argb(55, 255, 255, 255));
        canvas.drawCircle(width * 0.2f, height * 0.16f, 150, paint);
        canvas.drawCircle(width * 0.78f, height * 0.28f, 190, paint);
    }

    private void drawIntro(Canvas canvas, int width, int height) {
        float progress = Math.min(1f, (now - introStart) / 3600f);
        drawBigTitle(canvas, GAME_TITLE, "Animals are coming to play");

        float base = -140 + progress * (width + 300);
        drawEmoji(canvas, "🐘", base - 210, height * 0.54f, 105);
        drawEmoji(canvas, "🐵", base, height * 0.53f, 110);
        drawEmoji(canvas, "🐔", base - 390, height * 0.56f, 90);
        drawEmoji(canvas, "🐦", base - 530, height * 0.30f, 78);
        drawEmoji(canvas, "🦜", base - 690, height * 0.25f, 78);
    }

    private void drawHome(Canvas canvas, int width, int height) {
        RectF hero = new RectF(44, 92, width - 44, 382);
        drawCard(canvas, hero, Color.argb(205, 255, 255, 255), Color.argb(90, 255, 255, 255), 36);
        drawEmoji(canvas, "🐵", width / 2f, 164, 88);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(54);
        canvas.drawText(GAME_TITLE, width / 2f, 250, paint);
        paint.setTextSize(27);
        paint.setFakeBoldText(false);
        canvas.drawText("3 fun games just for you!", width / 2f, 312, paint);
        drawStars(canvas, width / 2f, 350, 3, 32);

        float left = 44;
        float right = width - 44;
        float top = 430;
        numbersButton.set(left, top, right, top + 136);
        coloursButton.set(left, top + 158, right, top + 294);
        alphabetsButton.set(left, top + 316, right, top + 452);

        drawMenuCard(canvas, numbersButton, "Feed Animals", "Count 1 to 10", "🍌", ORANGE);
        drawMenuCard(canvas, coloursButton, "Colours", "Pick the right colour", "🎨", PURPLE);
        drawMenuCard(canvas, alphabetsButton, "Alphabets", "Match A B C", "🍎", BLUE);
    }

    private void drawNumberPick(Canvas canvas, int width, int height) {
        drawTopBar(canvas, "Choose Animal", "🍌", 0);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(34);
        paint.setFakeBoldText(true);
        canvas.drawText("Who should we feed?", width / 2f, 190, paint);
        paint.setFakeBoldText(false);

        float cardW = (width - 72) / 3f;
        float top = 245;
        for (int i = 0; i < 3; i++) {
            float left = 24 + i * (cardW + 12);
            animalCards[i].set(left, top, left + cardW, top + 258);
            drawCard(canvas, animalCards[i], CARD, Color.argb(80, 255, 235, 151), 28);
            drawEmoji(canvas, animalEmoji[i], animalCards[i].centerX(), animalCards[i].top + 105, 92);
            paint.setColor(TEXT);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(22);
            paint.setFakeBoldText(true);
            canvas.drawText(animalNames[i], animalCards[i].centerX(), animalCards[i].bottom - 42, paint);
            paint.setFakeBoldText(false);
        }
    }

    private void drawNumberFeed(Canvas canvas, int width, int height) {
        drawTopBar(canvas, "Feed the", foodEmoji[selectedAnimal], foodCount);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(34);
        paint.setFakeBoldText(true);
        canvas.drawText(animalNames[selectedAnimal], width * 0.35f, 125, paint);
        paint.setFakeBoldText(false);

        drawEmoji(canvas, animalEmoji[selectedAnimal], width / 2f, 255, 132);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(CREAM);
        RectF countCircle = new RectF(width / 2f - 126, 330, width / 2f + 126, 582);
        canvas.drawOval(countCircle, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(foodAccent[selectedAnimal]);
        canvas.drawOval(countCircle, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(foodAccent[selectedAnimal]);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(112);
        canvas.drawText(String.valueOf(foodCount), width / 2f, 497, paint);
        paint.setColor(TEXT);
        paint.setTextSize(42);
        canvas.drawText(numberWord(foodCount), width / 2f, 640, paint);
        paint.setFakeBoldText(false);

        drawDots(canvas, foodCount, 696, foodAccent[selectedAnimal]);

        RectF tray = new RectF(42, 735, width - 42, 842);
        drawCard(canvas, tray, Color.argb(230, 255, 255, 245), Color.argb(60, 255, 255, 255), 28);
        drawFoodRow(canvas, tray);

        feedButton.set(58, 885, width * 0.66f, 978);
        drawActionButton(canvas, feedButton, foodEmoji[selectedAnimal] + " Feed!", ORANGE);
        resetButton.set(width * 0.71f, 885, width - 58, 978);
        drawActionButton(canvas, resetButton, "↻", Color.rgb(145, 164, 174));
    }

    private void drawColours(Canvas canvas, int width, int height) {
        drawTopBar(canvas, "Color Fun!", "🎨", colourRound);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(39);
        canvas.drawText("Find this color! 👇", width / 2f, 205, paint);
        paint.setFakeBoldText(false);

        RectF target = new RectF(width / 2f - 116, 242, width / 2f + 116, 474);
        drawColourTarget(canvas, target);
        drawThreeOptionsSideBySide(canvas, 540, true);
        drawMoreForStar(canvas, 738, 10 - colourRound);
    }

    private void drawAlphabets(Canvas canvas, int width, int height) {
        drawTopBar(canvas, "Alphabet Fun!", alphabetEmoji[alphabetTarget], alphabetRound);
        RectF card = new RectF(48, 162, width - 48, 475);
        drawCard(canvas, card, CARD, Color.argb(80, 255, 255, 255), 30);
        drawEmoji(canvas, alphabetEmoji[alphabetTarget], width / 2f, 260, 110);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(46);
        paint.setFakeBoldText(true);
        canvas.drawText(alphabetWords[alphabetTarget], width / 2f, 382, paint);
        paint.setTextSize(24);
        paint.setFakeBoldText(false);
        canvas.drawText("Tap the first letter", width / 2f, 430, paint);

        drawMoreForStar(canvas, 514, 10 - alphabetRound);
        drawThreeOptionsSideBySide(canvas, 610, false);
    }

    private void drawTopBar(Canvas canvas, String title, String icon, int stars) {
        int width = getWidth();
        RectF bar = new RectF(0, 0, width, 146);
        paint.setShader(new LinearGradient(0, 0, 0, 146, Color.rgb(255, 255, 255), Color.rgb(255, 253, 236), Shader.TileMode.CLAMP));
        canvas.drawRect(bar, paint);
        paint.setShader(null);

        homeButton.set(24, 38, 96, 112);
        drawIconTile(canvas, homeButton, "🏠", 38);
        drawEmoji(canvas, icon, 145, 84, 50);

        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(32);
        paint.setFakeBoldText(true);
        canvas.drawText(title, 185, 86, paint);
        paint.setFakeBoldText(false);

        RectF score = new RectF(width - 128, 45, width - 24, 106);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 253, 225));
        canvas.drawRoundRect(score, 30, 30, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(YELLOW);
        canvas.drawRoundRect(score, 30, 30, paint);
        paint.setStyle(Paint.Style.FILL);
        drawEmoji(canvas, "⭐", score.left + 32, score.centerY() + 2, 33);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(32);
        paint.setFakeBoldText(true);
        canvas.drawText(String.valueOf(stars), score.right - 28, score.centerY() + 11, paint);
        paint.setFakeBoldText(false);
    }

    private void drawThreeOptionsSideBySide(Canvas canvas, float top, boolean colourMode) {
        int width = getWidth();
        float gap = 16;
        float cardW = (width - 64 - gap * 2) / 3f;
        for (int i = 0; i < 3; i++) {
            float left = 32 + i * (cardW + gap);
            answerCards[i].set(left, top, left + cardW, top + 158);
            int tint = colourMode ? colourValues[answerValues[i]] : Color.rgb(245, 250, 255);
            drawCard(canvas, answerCards[i], WHITE, Color.argb(70, 255, 255, 255), 24);
            if (colourMode) {
                RectF swatch = new RectF(left + 16, top + 18, left + cardW - 16, top + 98);
                paint.setColor(tint);
                canvas.drawRoundRect(swatch, 24, 24, paint);
                paint.setColor(Color.argb(45, 0, 0, 0));
                canvas.drawRect(swatch.left, swatch.centerY(), swatch.right, swatch.bottom, paint);
                paint.setColor(TEXT);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(21);
                paint.setFakeBoldText(true);
                canvas.drawText(colourNames[answerValues[i]], answerCards[i].centerX(), answerCards[i].bottom - 27, paint);
                paint.setFakeBoldText(false);
            } else {
                paint.setColor(BLUE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(78);
                paint.setFakeBoldText(true);
                canvas.drawText(alphabetLetters[answerValues[i]], answerCards[i].centerX(), top + 100, paint);
                paint.setColor(MUTED);
                paint.setTextSize(18);
                canvas.drawText("Tap", answerCards[i].centerX(), answerCards[i].bottom - 25, paint);
                paint.setFakeBoldText(false);
            }
        }
    }

    private void drawColourTarget(Canvas canvas, RectF box) {
        drawCard(canvas, box, Color.argb(235, 255, 255, 255), Color.argb(60, 255, 255, 255), 36);
        RectF inner = new RectF(box.left + 28, box.top + 28, box.right - 28, box.bottom - 62);
        int color = colourValues[colourTarget];
        if (colourObject % 4 == 0) {
            drawGlossyCircle(canvas, inner.centerX(), inner.centerY(), inner.width() * 0.38f, color);
        } else if (colourObject % 4 == 1) {
            drawCartoonBird(canvas, inner.centerX(), inner.centerY(), color);
        } else if (colourObject % 4 == 2) {
            drawFlower(canvas, inner.centerX(), inner.centerY(), color);
        } else {
            drawCar(canvas, inner, color);
        }
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(23);
        paint.setFakeBoldText(true);
        canvas.drawText(colourObjectNames[colourObject], box.centerX(), box.bottom - 24, paint);
        paint.setFakeBoldText(false);
    }

    private void drawWinPopup(Canvas canvas, int width, int height) {
        paint.setColor(Color.argb(175, 20, 22, 28));
        canvas.drawRect(0, 0, width, height, paint);
        for (int i = 0; i < 20; i++) {
            float x = (i * 97 + now / 7f) % width;
            float y = 120 + (i * 73) % Math.max(260, height - 360);
            drawFirework(canvas, x, y, 34 + i % 4 * 11, colourValues[i % colourValues.length]);
        }
        drawFloatingConfetti(canvas, width, height);

        RectF modal = new RectF(48, 145, width - 48, height - 118);
        drawCard(canvas, modal, CREAM, Color.argb(80, 255, 255, 255), 40);
        drawEmoji(canvas, "🏆", width / 2f, modal.top + 88, 78);
        drawEmoji(canvas, scene == NUMBER_FEED ? "🥳" : "🎯", width / 2f, modal.top + 170, 116);
        drawStars(canvas, width / 2f, modal.top + 278, 3, 48);

        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(43);
        String title = scene == NUMBER_FEED ? "Ten " + foodNames[selectedAnimal] + "s!" : scene == COLOURS ? "Color Master!" : "Alphabet Star!";
        canvas.drawText(title, width / 2f, modal.top + 365, paint);
        paint.setTextSize(25);
        paint.setFakeBoldText(false);
        String sub = scene == NUMBER_FEED ? "The " + animalNames[selectedAnimal].toLowerCase(Locale.US) + " loves you!" : "You finished 10 rounds!";
        canvas.drawText(sub, width / 2f, modal.top + 422, paint);

        homeButton.set(modal.left + 58, modal.bottom - 128, modal.right - 58, modal.bottom - 50);
        drawActionButton(canvas, homeButton, "🎉 Keep Playing!", GREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();

        if (muteButton.contains(x, y)) {
            muted = !muted;
            if (muted && textToSpeech != null) {
                textToSpeech.stop();
            }
            return true;
        }
        if (celebrating) {
            if (homeButton.contains(x, y)) {
                celebrating = false;
                scene = HOME;
                speak("Home");
            }
            return true;
        }
        if (scene == INTRO) {
            scene = HOME;
            speak("Choose a game");
            return true;
        }
        if (homeButton.contains(x, y) && scene != HOME) {
            scene = HOME;
            speak("Home");
            return true;
        }

        if (scene == HOME) {
            if (numbersButton.contains(x, y)) {
                scene = NUMBER_PICK;
                speak("Choose an animal");
            } else if (coloursButton.contains(x, y)) {
                startColours();
            } else if (alphabetsButton.contains(x, y)) {
                startAlphabets();
            }
        } else if (scene == NUMBER_PICK) {
            for (int i = 0; i < animalCards.length; i++) {
                if (animalCards[i].contains(x, y)) {
                    selectedAnimal = i;
                    foodCount = 0;
                    scene = NUMBER_FEED;
                    speak("Feed the " + animalNames[i]);
                    return true;
                }
            }
        } else if (scene == NUMBER_FEED) {
            if (feedButton.contains(x, y)) {
                feedAnimal();
            } else if (resetButton.contains(x, y)) {
                foodCount = 0;
                speak("Start again");
            }
        } else if (scene == COLOURS) {
            handleColourAnswer(x, y);
        } else if (scene == ALPHABETS) {
            handleAlphabetAnswer(x, y);
        }
        return true;
    }

    private void feedAnimal() {
        if (foodCount >= 10) {
            return;
        }
        foodCount++;
        pop = 1f;
        tone.startTone(ToneGenerator.TONE_PROP_ACK, 90);
        speak(numberWord(foodCount));
        if (foodCount == 10) {
            celebrating = true;
            speak("Ten " + foodNames[selectedAnimal] + "s. You won.");
        }
    }

    private void startColours() {
        scene = COLOURS;
        colourRound = 0;
        prepareColourRound();
        speak("Color fun. Find this color.");
    }

    private void prepareColourRound() {
        colourTarget = random.nextInt(colourValues.length);
        colourObject = random.nextInt(colourObjectNames.length);
        fillChoices(colourTarget, colourValues.length);
    }

    private void handleColourAnswer(float x, float y) {
        for (int i = 0; i < answerCards.length; i++) {
            if (answerCards[i].contains(x, y)) {
                String picked = colourNames[answerValues[i]];
                if (answerValues[i] == colourTarget) {
                    colourRound++;
                    tone.startTone(ToneGenerator.TONE_PROP_ACK, 90);
                    speak(picked + ". Correct.");
                    if (colourRound >= 10) {
                        celebrating = true;
                        speak("Color master. You won.");
                    } else {
                        prepareColourRound();
                    }
                } else {
                    tone.startTone(ToneGenerator.TONE_PROP_NACK, 120);
                    speak(picked + ". No, that is wrong.");
                }
                return;
            }
        }
    }

    private void startAlphabets() {
        scene = ALPHABETS;
        alphabetRound = 0;
        prepareAlphabetRound();
        speak("Alphabet fun. Pick the first letter.");
    }

    private void prepareAlphabetRound() {
        alphabetTarget = alphabetRound % alphabetLetters.length;
        fillChoices(alphabetTarget, alphabetLetters.length);
    }

    private void handleAlphabetAnswer(float x, float y) {
        for (int i = 0; i < answerCards.length; i++) {
            if (answerCards[i].contains(x, y)) {
                String letter = alphabetLetters[answerValues[i]];
                if (answerValues[i] == alphabetTarget) {
                    alphabetRound++;
                    tone.startTone(ToneGenerator.TONE_PROP_ACK, 90);
                    speak(letter + " for " + alphabetWords[alphabetTarget] + ". Correct.");
                    if (alphabetRound >= 10) {
                        celebrating = true;
                        speak("Alphabet star. You won.");
                    } else {
                        prepareAlphabetRound();
                    }
                } else {
                    tone.startTone(ToneGenerator.TONE_PROP_NACK, 120);
                    speak(letter + ". No, try again.");
                }
                return;
            }
        }
    }

    private void fillChoices(int target, int max) {
        int correct = random.nextInt(3);
        for (int i = 0; i < 3; i++) {
            answerValues[i] = target;
        }
        for (int i = 0; i < 3; i++) {
            if (i == correct) {
                answerValues[i] = target;
                continue;
            }
            int value;
            do {
                value = random.nextInt(max);
            } while (value == target || usedBefore(value, i));
            answerValues[i] = value;
        }
    }

    private boolean usedBefore(int value, int limit) {
        for (int i = 0; i < limit; i++) {
            if (answerValues[i] == value) {
                return true;
            }
        }
        return false;
    }

    private void drawMenuCard(Canvas canvas, RectF box, String title, String subtitle, String icon, int accent) {
        drawCard(canvas, box, CREAM, Color.argb(60, 255, 255, 255), 26);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent)));
        canvas.drawRoundRect(box, 26, 26, paint);
        paint.setStyle(Paint.Style.FILL);
        RectF iconCircle = new RectF(box.left + 30, box.top + 27, box.left + 112, box.top + 109);
        paint.setColor(WHITE);
        canvas.drawOval(iconCircle, paint);
        drawEmoji(canvas, icon, iconCircle.centerX(), iconCircle.centerY() + 5, 54);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setFakeBoldText(true);
        paint.setTextSize(33);
        canvas.drawText(title, box.left + 140, box.top + 57, paint);
        paint.setTextSize(23);
        paint.setFakeBoldText(false);
        canvas.drawText(subtitle, box.left + 140, box.top + 96, paint);
        paint.setColor(accent);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(42);
        canvas.drawText("▶", box.right - 36, box.centerY() + 15, paint);
    }

    private void drawCard(Canvas canvas, RectF box, int fill, int shine, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(28, 0, 0, 0));
        canvas.drawRoundRect(new RectF(box.left, box.top + 6, box.right, box.bottom + 6), radius, radius, paint);
        paint.setColor(fill);
        canvas.drawRoundRect(box, radius, radius, paint);
        paint.setColor(shine);
        canvas.drawRoundRect(new RectF(box.left + 12, box.top + 10, box.right - 12, box.top + box.height() * 0.48f), radius, radius, paint);
    }

    private void drawIconTile(Canvas canvas, RectF box, String icon, float size) {
        drawCard(canvas, box, WHITE, Color.argb(50, 255, 255, 255), 20);
        drawEmoji(canvas, icon, box.centerX(), box.centerY() + 4, size);
    }

    private void drawActionButton(Canvas canvas, RectF box, String label, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(box, 26, 26, paint);
        paint.setColor(Color.argb(45, 0, 0, 0));
        canvas.drawRect(box.left, box.centerY(), box.right, box.bottom - 3, paint);
        paint.setColor(WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(label.length() > 8 ? 31 : 44);
        canvas.drawText(label, box.centerX(), box.centerY() + 13, paint);
        paint.setFakeBoldText(false);
    }

    private void drawFoodRow(Canvas canvas, RectF tray) {
        int shown = Math.max(1, foodCount);
        float start = tray.left + 54;
        float gap = Math.min(52, (tray.width() - 108) / Math.max(1, shown));
        for (int i = 0; i < shown; i++) {
            drawEmoji(canvas, foodEmoji[selectedAnimal], start + i * gap, tray.centerY() + 10, 40 + pop * 7);
        }
    }

    private void drawDots(Canvas canvas, int filled, float y, int color) {
        int width = getWidth();
        float start = width / 2f - 166;
        for (int i = 0; i < 10; i++) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(i < filled ? color : CREAM);
            canvas.drawCircle(start + i * 37, y, 13, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setColor(color);
            canvas.drawCircle(start + i * 37, y, 13, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawMoreForStar(Canvas canvas, float y, int remaining) {
        RectF box = new RectF(38, y, getWidth() - 38, y + 68);
        paint.setColor(Color.rgb(255, 255, 235));
        canvas.drawRoundRect(box, 28, 28, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(YELLOW);
        canvas.drawRoundRect(box, 28, 28, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(26);
        canvas.drawText(Math.max(0, remaining) + " more for a star! ⭐", box.centerX(), box.centerY() + 10, paint);
        paint.setFakeBoldText(false);
    }

    private void drawStars(Canvas canvas, float cx, float cy, int count, float size) {
        for (int i = 0; i < count; i++) {
            drawEmoji(canvas, "⭐", cx + (i - (count - 1) / 2f) * size * 1.35f, cy, size);
        }
    }

    private void drawBigTitle(Canvas canvas, String title, String subtitle) {
        paint.setColor(TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(54);
        canvas.drawText(title, getWidth() / 2f, 115, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(24);
        canvas.drawText(subtitle, getWidth() / 2f, 154, paint);
    }

    private void drawMuteButton(Canvas canvas, int width) {
        muteButton.set(width - 86, getHeight() - 82, width - 22, getHeight() - 26);
        drawCard(canvas, muteButton, WHITE, Color.argb(45, 255, 255, 255), 18);
        drawEmoji(canvas, muted ? "🔇" : "🔊", muteButton.centerX(), muteButton.centerY() + 3, 30);
    }

    private void drawEmoji(Canvas canvas, String emoji, float cx, float cy, float size) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size);
        paint.setFakeBoldText(false);
        canvas.drawText(emoji, cx, cy + size * 0.34f, paint);
    }

    private void drawGlossyCircle(Canvas canvas, float cx, float cy, float r, int color) {
        paint.setShader(new RadialGradient(cx - r * 0.3f, cy - r * 0.35f, r * 1.1f,
                Color.WHITE, color, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(WHITE);
        canvas.drawCircle(cx, cy, r + 8, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCartoonBird(Canvas canvas, float cx, float cy, int color) {
        paint.setShader(new RadialGradient(cx - 22, cy - 28, 110, Color.WHITE, color, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(cx - 78, cy - 52, cx + 58, cy + 52), paint);
        paint.setShader(null);
        paint.setColor(YELLOW);
        path.reset();
        path.moveTo(cx + 45, cy - 8);
        path.lineTo(cx + 92, cy + 8);
        path.lineTo(cx + 45, cy + 25);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(TEXT);
        canvas.drawCircle(cx + 18, cy - 20, 8, paint);
    }

    private void drawFlower(Canvas canvas, float cx, float cy, int color) {
        paint.setColor(color);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            canvas.drawCircle(cx + (float) Math.cos(angle) * 48, cy + (float) Math.sin(angle) * 48, 31, paint);
        }
        paint.setColor(YELLOW);
        canvas.drawCircle(cx, cy, 34, paint);
    }

    private void drawCar(Canvas canvas, RectF box, int color) {
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(box.left + 8, box.centerY() - 20, box.right - 8, box.centerY() + 55), 24, 24, paint);
        paint.setColor(Color.argb(130, 255, 255, 255));
        canvas.drawRoundRect(new RectF(box.left + 48, box.top + 38, box.right - 48, box.centerY() + 8), 18, 18, paint);
        paint.setColor(TEXT);
        canvas.drawCircle(box.left + 55, box.centerY() + 60, 18, paint);
        canvas.drawCircle(box.right - 55, box.centerY() + 60, 18, paint);
    }

    private void drawFirework(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(color);
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI * 2 / 10 + now / 330.0;
            canvas.drawLine(cx, cy, cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawFloatingConfetti(Canvas canvas, int width, int height) {
        String[] bits = {"🎈", "🎊", "✨", "🌟", "🎉"};
        for (int i = 0; i < 18; i++) {
            float x = (i * 71 + now / 13f) % width;
            float y = 70 + (i * 97 + now / 18f) % Math.max(360, height - 180);
            drawEmoji(canvas, bits[i % bits.length], x, y, 28 + (i % 3) * 8);
        }
    }

    private String numberWord(int value) {
        String[] words = {"Zero!", "One!", "Two!", "Three!", "Four!", "Five!", "Six!", "Seven!", "Eight!", "Nine!", "Ten!"};
        return words[Math.max(0, Math.min(value, 10))];
    }

    public void pauseGame() {
        animationRunning = false;
        musicRunning = false;
        handler.removeCallbacks(animationLoop);
        handler.removeCallbacks(musicLoop);
        musicTone.stopTone();
        tone.stopTone();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void resumeGame() {
        startAnimation();
        startMusic();
    }

    public void releaseGame() {
        pauseGame();
        musicTone.release();
        tone.release();
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}
