package ru.teampicker.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Build;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private int BG;
    private int INK;
    private int MUTED;
    private int ACCENT;
    private static final int SPLASH_STATUS_BAR = Color.rgb(5, 111, 138);
    private static final int SPLASH_NAV_BAR = Color.rgb(22, 46, 125);
    private static final int[] TEAM_COLORS = {
        Color.rgb(87, 105, 232), Color.rgb(25, 157, 124),
        Color.rgb(224, 145, 51), Color.rgb(164, 98, 207),
        Color.rgb(219, 88, 119), Color.rgb(40, 154, 181),
        Color.rgb(127, 147, 65), Color.rgb(98, 111, 211)
    };

    private SharedPreferences preferences;
    private ThemePalette theme;
    private LinearLayout page, playerList;
    private TextView playerValue, teamValue;
    private final List<EditText> nameFields = new ArrayList<>();
    private final List<Integer> assignments = new ArrayList<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private int playerCount = 6;
    private int teamCount = 2;
    private boolean changingFields;
    private boolean isShuffling;
    private ValueAnimator shuffleAnimator;
    private SplashView splashView;
    private View mainContent;
    private ValueAnimator splashExitAnimator;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("teamup", MODE_PRIVATE);
        applyTheme(ThemePalette.of(preferences.getInt("ui_theme", ThemePalette.LIGHT)));
        playerCount = Math.max(2, Math.min(30, preferences.getInt("players", 6)));
        teamCount = Math.max(2, Math.min(Math.min(8, playerCount), preferences.getInt("teams", 2)));
        loadAssignments();
        loadHistory();

        configureSystemBars();
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        buildScreen();
        if (savedInstanceState == null) showSplash();
    }

    @Override protected void onPause() {
        // Never keep an unfinished transition running in the background.
        if (splashView != null) completeSplashExit();
        dismissKeyboard();
        super.onPause();
    }

    @Override protected void onStop() {
        // Finish an interrupted draw so fields never remain hidden.
        if (shuffleAnimator != null) shuffleAnimator.end();
        super.onStop();
    }

    private void applyTheme(ThemePalette next) {
        theme = next;
        BG = next.background;
        INK = next.ink;
        MUTED = next.muted;
        ACCENT = next.accent;
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(theme.dark ? 0 :
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void cycleTheme() {
        if (shuffleAnimator != null) shuffleAnimator.end();
        dismissKeyboard();
        saveNames();
        ThemePalette next = ThemePalette.of(ThemePalette.next(theme.mode));
        applyTheme(next);
        preferences.edit().putInt("ui_theme", next.mode).apply();
        configureSystemBars();
        buildScreen();
        // Delicate feedback without flashing to a blank screen.
        FrameLayout content = findViewById(android.R.id.content);
        View screen = content.getChildAt(0);
        if (screen != null) {
            screen.setAlpha(.94f);
            screen.animate().alpha(1f).setDuration(160L).start();
        }
    }

    private int dialogTheme() {
        return theme.dark ? android.R.style.Theme_Material_Dialog_Alert
                          : android.R.style.Theme_Material_Light_Dialog_Alert;
    }

    private void showSplash() {
        Window window = getWindow();
        window.setStatusBarColor(SPLASH_STATUS_BAR);
        window.setNavigationBarColor(SPLASH_NAV_BAR);
        window.getDecorView().setSystemUiVisibility(0);

        FrameLayout content = findViewById(android.R.id.content);
        // Prepare the actual UI under the splash; the two views will crossfade.
        mainContent = content.getChildAt(0);
        mainContent.setAlpha(0f);
        mainContent.setTranslationY(dp(16));

        SplashView intro = new SplashView(this);
        splashView = intro;
        content.addView(intro, new FrameLayout.LayoutParams(-1, -1));
        intro.play(() -> startSplashExit(intro));
    }

    private void startSplashExit(SplashView intro) {
        if (splashView != intro || splashExitAnimator != null) return;
        // Respect system-wide disabled animations on Android 8+.
        if (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) {
            completeSplashExit();
            return;
        }

        Window window = getWindow();
        ArgbEvaluator colors = new ArgbEvaluator();
        ValueAnimator transition = ValueAnimator.ofFloat(0f, 1f);
        splashExitAnimator = transition;
        transition.setDuration(SplashTransition.DURATION_MS);
        transition.setInterpolator(null);
        transition.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            float lift = SplashTransition.contentOffset(progress);

            intro.setAlpha(SplashTransition.introAlpha(progress));
            float zoom = SplashTransition.introZoom(progress);
            intro.setScaleX(zoom);
            intro.setScaleY(zoom);
            mainContent.setAlpha(SplashTransition.contentAlpha(progress));
            mainContent.setTranslationY(dp(16) * lift);

            float colorProgress = 1f - lift;
            window.setStatusBarColor((Integer) colors.evaluate(
                colorProgress, SPLASH_STATUS_BAR, BG));
            window.setNavigationBarColor((Integer) colors.evaluate(
                colorProgress, SPLASH_NAV_BAR, BG));
        });
        transition.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                completeSplashExit();
            }
        });
        transition.start();
    }

    private void completeSplashExit() {
        if (splashExitAnimator != null) {
            ValueAnimator transition = splashExitAnimator;
            splashExitAnimator = null;
            transition.removeAllListeners();
            transition.cancel();
        }

        SplashView intro = splashView;
        splashView = null;
        if (intro != null && intro.getParent() instanceof FrameLayout) {
            ((FrameLayout) intro.getParent()).removeView(intro);
        }
        if (mainContent != null) {
            mainContent.setAlpha(1f);
            mainContent.setTranslationY(0f);
            mainContent = null;
        }
        configureSystemBars();
        if (page != null) page.requestFocus();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setClipChildren(false);
        page.setPadding(dp(18), dp(12), dp(18), dp(24));
        page.setFocusableInTouchMode(true);
        page.setOnClickListener(v -> dismissKeyboard());
        scroll.addView(page);
        setContentView(scroll);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(header, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView title = text("TeamUp", 25, INK, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));
        TextView themeButton = text(theme.symbol, 23, ACCENT, false);
        themeButton.setGravity(Gravity.CENTER);
        themeButton.setBackground(ripple(theme.control, 12, withAlpha(ACCENT, 40)));
        themeButton.setContentDescription("Theme " + theme.label + ". Tap for " +
            ThemePalette.of(ThemePalette.next(theme.mode)).label + " theme");
        header.addView(themeButton, new LinearLayout.LayoutParams(dp(40), dp(36)));
        themeButton.setOnClickListener(v -> {
            pressFeedback(v);
            cycleTheme();
        });
        View headerGap = new View(this);
        header.addView(headerGap, new LinearLayout.LayoutParams(dp(8), 1));

        TextView historyButton = text("History", 13, ACCENT, true);
        historyButton.setGravity(Gravity.CENTER);
        historyButton.setPadding(dp(13), 0, dp(13), 0);
        historyButton.setBackground(ripple(theme.surface, 12, withAlpha(ACCENT, 35)));
        header.addView(historyButton, new LinearLayout.LayoutParams(-2, dp(36)));
        historyButton.setOnClickListener(v -> {
            dismissKeyboard();
            pressFeedback(v);
            showHistory();
        });

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams controlsLp = matchWrap();
        controlsLp.topMargin = dp(8);
        page.addView(controls, controlsLp);
        controls.addView(stepper("Players", true), new LinearLayout.LayoutParams(0, -2, 1f));
        View gap = new View(this);
        controls.addView(gap, new LinearLayout.LayoutParams(dp(10), 1));
        controls.addView(stepper("Teams", false), new LinearLayout.LayoutParams(0, -2, 1f));

        playerList = new LinearLayout(this);
        playerList.setOrientation(LinearLayout.VERTICAL);
        playerList.setClipChildren(false);
        playerList.setClipToPadding(false);
        LinearLayout.LayoutParams listLp = matchWrap();
        listLp.topMargin = dp(14);
        page.addView(playerList, listLp);
        rebuildPlayerRows();

        TextView generate = text("Generate teams", 16, theme.onAccent, true);
        generate.setGravity(Gravity.CENTER);
        generate.setBackground(ripple(ACCENT, 16, withAlpha(theme.onAccent, 55)));
        LinearLayout.LayoutParams buttonLp = matchWrap();
        buttonLp.height = dp(54);
        buttonLp.topMargin = dp(14);
        page.addView(generate, buttonLp);
        generate.setOnClickListener(v -> {
            dismissKeyboard();
            pressFeedback(v);
            generateTeams();
        });
        // Initial focus stays on the screen, not the first name field.
        page.requestFocus();
    }

    private View stepper(String title, boolean players) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(round(theme.surface, 16));

        TextView heading = text(title, 13, MUTED, true);
        box.addView(heading);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = matchWrap();
        rowLp.topMargin = dp(6);
        box.addView(row, rowLp);

        TextView minus = stepButton("−");
        TextView value = text(String.valueOf(players ? playerCount : teamCount), 20, INK, true);
        value.setGravity(Gravity.CENTER);
        TextView plus = stepButton("+");
        row.addView(minus, new LinearLayout.LayoutParams(dp(34), dp(34)));
        row.addView(value, new LinearLayout.LayoutParams(0, dp(34), 1f));
        row.addView(plus, new LinearLayout.LayoutParams(dp(34), dp(34)));

        if (players) playerValue = value; else teamValue = value;
        minus.setOnClickListener(v -> {
            if (isShuffling) return;
            dismissKeyboard();
            if (players && playerCount > 2) {
                playerCount--;
                if (teamCount > playerCount) teamCount = playerCount;
                resetBalancedAssignments();
                persistSettings();
                updateCounters();
                rebuildPlayerRows();
            } else if (!players && teamCount > 2) {
                teamCount--;
                resetBalancedAssignments();
                persistSettings();
                updateCounters();
                updateRowColors();
            }
        });
        plus.setOnClickListener(v -> {
            if (isShuffling) return;
            dismissKeyboard();
            if (players && playerCount < 30) {
                playerCount++;
                resetBalancedAssignments();
                persistSettings();
                updateCounters();
                rebuildPlayerRows();
            } else if (!players && teamCount < Math.min(8, playerCount)) {
                teamCount++;
                resetBalancedAssignments();
                persistSettings();
                updateCounters();
                updateRowColors();
            }
        });
        return box;
    }

    private TextView stepButton(String symbol) {
        TextView button = text(symbol, 22, ACCENT, false);
        button.setGravity(Gravity.CENTER);
        button.setBackground(ripple(theme.control, 11, withAlpha(ACCENT, 45)));
        return button;
    }

    private void rebuildPlayerRows() {
        if (playerList == null) return;
        List<String> oldNames = new ArrayList<>();
        for (EditText field : nameFields) oldNames.add(field.getText().toString());
        changingFields = true;
        playerList.removeAllViews();
        nameFields.clear();
        for (int i = 0; i < playerCount; i++) {
            final int index = i;
            LinearLayout card = new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(12), dp(5), dp(12), dp(5));
            card.setBackground(rowBackground(colorForTeam(assignmentAt(i))));
            LinearLayout.LayoutParams cardLp = matchWrap();
            cardLp.bottomMargin = dp(7);
            playerList.addView(card, cardLp);

            EditText name = new EditText(this);
            name.setSingleLine(true);
            name.setTextSize(15);
            name.setTextColor(INK);
            name.setHintTextColor(theme.hint);
            name.setHint("Player " + (i + 1));
            name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            name.setImeOptions(i == playerCount - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            name.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dismissKeyboard();
                    return true;
                }
                return false;
            });
            name.setBackgroundColor(Color.TRANSPARENT);
            name.setPadding(dp(3), 0, dp(6), 0);
            if (i < oldNames.size()) name.setText(oldNames.get(i));
            else name.setText(preferences.getString("name_" + i, ""));
            card.addView(name, new LinearLayout.LayoutParams(-1, dp(48)));
            nameFields.add(name);
            name.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!changingFields) preferences.edit().putString("name_" + index, s.toString()).apply();
                }
                @Override public void afterTextChanged(Editable e) {}
            });

        }
        changingFields = false;
        saveNames();
    }

    private void generateTeams() {
        if (isShuffling) return;
        // Team colors stay in fixed, contiguous blocks; shuffle player names instead.
        List<String> names = new ArrayList<>();
        List<Integer> previousTeams = new ArrayList<>();
        List<Integer> targetTeams = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String enteredName = nameFields.get(i).getText().toString();
            // Give unnamed players stable identities so their movement is visible, too.
            names.add(enteredName.trim().isEmpty() ? "Player " + (i + 1) : enteredName);
            previousTeams.add(assignmentAt(i));
        }
        for (int team = 0; team < teamCount; team++) {
            int size = playerCount / teamCount + (team < playerCount % teamCount ? 1 : 0);
            for (int i = 0; i < size; i++) targetTeams.add(team);
        }

        List<Integer> best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < 512; attempt++) {
            List<Integer> candidate = new ArrayList<>();
            for (int i = 0; i < playerCount; i++) candidate.add(i);
            Collections.shuffle(candidate);

            int score = 0;
            for (int i = 0; i < playerCount; i++) {
                int oldPlayer = candidate.get(i);
                if (oldPlayer == i) score += playerCount + 1;
                for (int j = i + 1; j < playerCount; j++) {
                    if (previousTeams.get(oldPlayer).equals(previousTeams.get(candidate.get(j)))
                        && targetTeams.get(i).equals(targetTeams.get(j))) {
                        score += playerCount + 1;
                    }
                }
            }
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        animateShuffle(names, best, previousTeams, targetTeams);
    }

    private void animateShuffle(List<String> names, List<Integer> order,
                                List<Integer> previousTeams, List<Integer> targetTeams) {
        // Each real card travels from its current row to the destination row.
        // At completion the layouts stay put, but their names are permuted.
        int count = playerCount;
        final int[] destinations = new int[count];
        for (int destination = 0; destination < count; destination++) {
            destinations[order.get(destination)] = destination;
        }

        final List<LinearLayout> cards = new ArrayList<>();
        final List<GradientDrawable> backgrounds = new ArrayList<>();
        final float[] verticalDistances = new float[count];

        for (int source = 0; source < count; source++) {
            LinearLayout card = (LinearLayout) playerList.getChildAt(source);
            LinearLayout target = (LinearLayout) playerList.getChildAt(destinations[source]);
            verticalDistances[source] = target.getTop() - card.getTop();
            cards.add(card);

            GradientDrawable background = rowBackground(colorForTeam(previousTeams.get(source)));
            card.setBackground(background);
            backgrounds.add(background);
            card.setElevation(dp(3));
        }

        isShuffling = true;
        shuffleAnimator = ValueAnimator.ofFloat(0f, 1f);
        shuffleAnimator.setDuration(1190L);
        shuffleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        final ArgbEvaluator colorEvaluator = new ArgbEvaluator();
        shuffleAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            for (int source = 0; source < count; source++) {
                int destination = destinations[source];
                LinearLayout card = cards.get(source);
                // Shrink on departure, keep compact while crossing, and grow on arrival.
                float shrinkPhase = Math.min(1f, Math.min(progress / .16f,
                    (1f - progress) / .16f));
                float scale = 1f - .14f * Math.max(0f, shrinkPhase);
                card.setScaleX(scale);
                card.setScaleY(scale);
                card.setTranslationY(verticalDistances[source] * progress);

                float curve = (float) Math.sin(Math.PI * progress);
                float side = dp(11 + (source % 3) * 3) * (source % 2 == 0 ? 1f : -1f);
                float offset = source == destination ? 0f : side * curve;
                // Keep the entire animated card within the list's visible width.
                float inset = (1f - scale) * card.getWidth() / 2f;
                float minOffset = -card.getLeft() - inset;
                float maxOffset = playerList.getWidth() - card.getRight() + inset;
                if (minOffset > maxOffset) offset = 0f;
                else offset = Math.max(minOffset, Math.min(maxOffset, offset));
                card.setTranslationX(offset);

                int from = colorForTeam(previousTeams.get(source));
                int to = colorForTeam(targetTeams.get(destination));
                int blended = (Integer) colorEvaluator.evaluate(progress, from, to);
                GradientDrawable background = backgrounds.get(source);
                background.setColor(withAlpha(blended, 38));
                background.setStroke(dp(1), withAlpha(blended, 105));
            }
        });
        shuffleAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean finished;
            private void finish() {
                if (finished) return;
                finished = true;
                // All animated cards have arrived; adopt the final order and colors.
                changingFields = true;
                for (int destination = 0; destination < count; destination++) {
                    nameFields.get(destination).setText(names.get(order.get(destination)));
                }
                changingFields = false;
                saveNames();
                assignments.clear();
                assignments.addAll(targetTeams);
                for (LinearLayout card : cards) {
                    card.setTranslationX(0f);
                    card.setTranslationY(0f);
                    card.setScaleX(1f);
                    card.setScaleY(1f);
                    card.setElevation(0f);
                }
                updateRowColors();
                addHistoryEntry();
                isShuffling = false;
                shuffleAnimator = null;
            }
            @Override public void onAnimationEnd(Animator animation) { finish(); }
            @Override public void onAnimationCancel(Animator animation) { finish(); }
        });
        shuffleAnimator.start();
    }

    private void dismissKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) focused.clearFocus();
        if (page != null) page.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void pressFeedback(View view) {
        view.animate().cancel();
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.animate().scaleX(.95f).scaleY(.95f).setDuration(75L).withEndAction(() ->
            view.animate().scaleX(1f).scaleY(1f).setDuration(125L).start()
        ).start();
    }

    private void addHistoryEntry() {
        HistoryEntry entry = new HistoryEntry();
        entry.createdAt = System.currentTimeMillis();
        for (int i = 0; i < playerCount; i++) {
            String name = nameFields.get(i).getText().toString().trim();
            entry.names.add(name.isEmpty() ? "Player " + (i + 1) : name);
            entry.teams.add(assignmentAt(i));
        }
        history.add(0, entry);
        saveHistory();
    }

    private void showHistory() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(list);

        int availableHeight = (int) (getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().density * .60f);
        scroll.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, dp(Math.min(480, Math.max(220, availableHeight)))));
        scroll.setPadding(dp(20), dp(4), dp(20), dp(4));
        scroll.setClipToPadding(false);

        AlertDialog dialog = new AlertDialog.Builder(this, dialogTheme())
            .setTitle("History")
            .setView(scroll)
            .setNeutralButton("Clear all", null)
            .setNegativeButton("Close", null)
            .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (history.isEmpty()) return;
                new AlertDialog.Builder(this, dialogTheme())
                    .setTitle("Clear history?")
                    .setMessage("All saved team draws will be deleted.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Clear all", (confirm, which) -> {
                        history.clear();
                        saveHistory();
                        renderHistory(list);
                    }).show();
            });
            renderHistory(list);
        });
        dialog.show();
    }

    private void renderHistory(LinearLayout list) {
        list.removeAllViews();
        if (history.isEmpty()) {
            TextView empty = text("No history yet", 14, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(54), 0, dp(54));
            list.addView(empty, matchWrap());
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
        for (int entryIndex = 0; entryIndex < history.size(); entryIndex++) {
            final int index = entryIndex;
            HistoryEntry entry = history.get(entryIndex);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(10), dp(10), dp(10));
            card.setBackground(round(theme.historySurface, 14));
            LinearLayout.LayoutParams cardLp = matchWrap();
            cardLp.bottomMargin = dp(8);
            list.addView(card, cardLp);

            LinearLayout header = new LinearLayout(this);
            header.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(header, matchWrap());
            TextView date = text(format.format(new Date(entry.createdAt)), 13, INK, true);
            header.addView(date, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView delete = text("×", 22, MUTED, false);
            delete.setGravity(Gravity.CENTER);
            delete.setBackground(ripple(Color.TRANSPARENT, 50, withAlpha(INK, 22)));
            header.addView(delete, new LinearLayout.LayoutParams(dp(32), dp(30)));
            delete.setOnClickListener(v -> {
                history.remove(index);
                saveHistory();
                renderHistory(list);
            });

            for (int i = 0; i < entry.names.size(); i++) {
                LinearLayout person = new LinearLayout(this);
                person.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams personLp = matchWrap();
                personLp.topMargin = dp(5);
                card.addView(person, personLp);
                int team = i < entry.teams.size() ? Math.max(0, Math.min(TEAM_COLORS.length - 1, entry.teams.get(i))) : 0;
                person.setPadding(dp(10), dp(8), dp(10), dp(8));
                person.setBackground(rowBackground(colorForTeam(team)));
                TextView name = text(entry.names.get(i), 13, INK, false);
                person.addView(name, new LinearLayout.LayoutParams(-1, -2));
            }
        }
    }

    private void loadHistory() {
        history.clear();
        String saved = preferences.getString("history", "[]");
        try {
            JSONArray entries = new JSONArray(saved);
            for (int i = 0; i < entries.length(); i++) {
                JSONObject object = entries.getJSONObject(i);
                HistoryEntry entry = new HistoryEntry();
                entry.createdAt = object.optLong("createdAt", System.currentTimeMillis());
                JSONArray names = object.optJSONArray("names");
                JSONArray teams = object.optJSONArray("teams");
                if (names != null) for (int n = 0; n < names.length(); n++) entry.names.add(names.optString(n, "Player " + (n + 1)));
                if (teams != null) for (int t = 0; t < teams.length(); t++) entry.teams.add(teams.optInt(t, 0));
                history.add(entry);
            }
        } catch (JSONException ignored) {
            history.clear();
        }
    }

    private void saveHistory() {
        JSONArray entries = new JSONArray();
        for (HistoryEntry entry : history) {
            JSONObject object = new JSONObject();
            JSONArray names = new JSONArray();
            JSONArray teams = new JSONArray();
            try {
                object.put("createdAt", entry.createdAt);
                for (String name : entry.names) names.put(name);
                for (Integer team : entry.teams) teams.put(team);
                object.put("names", names);
                object.put("teams", teams);
                entries.put(object);
            } catch (JSONException ignored) {
                // Skip malformed entry data and keep the rest of the saved history.
            }
        }
        preferences.edit().putString("history", entries.toString()).apply();
    }

    private static class HistoryEntry {
        long createdAt;
        final List<String> names = new ArrayList<>();
        final List<Integer> teams = new ArrayList<>();
    }

    private void updateRowColors() {
        for (int i = 0; i < playerList.getChildCount(); i++) {
            int team = assignmentAt(i);
            LinearLayout card = (LinearLayout) playerList.getChildAt(i);
            card.setBackground(rowBackground(colorForTeam(team)));
        }
        saveAssignments();
    }

    private void resetBalancedAssignments() {
        assignments.clear();
        for (int i = 0; i < playerCount; i++) {
            int team = 0;
            int remaining = i;
            for (int candidate = 0; candidate < teamCount; candidate++) {
                int size = playerCount / teamCount + (candidate < playerCount % teamCount ? 1 : 0);
                if (remaining < size) { team = candidate; break; }
                remaining -= size;
            }
            assignments.add(team);
        }
        saveAssignments();
    }

    private void updateCounters() {
        playerValue.setText(String.valueOf(playerCount));
        teamValue.setText(String.valueOf(teamCount));
    }

    private void persistSettings() {
        preferences.edit().putInt("players", playerCount).putInt("teams", teamCount).apply();
    }

    private void saveNames() {
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < nameFields.size(); i++) editor.putString("name_" + i, nameFields.get(i).getText().toString());
        editor.apply();
    }

    private void saveAssignments() {
        SharedPreferences.Editor editor = preferences.edit().putInt("players", playerCount).putInt("teams", teamCount);
        for (int i = 0; i < assignments.size(); i++) editor.putInt("team_" + i, assignments.get(i));
        editor.apply();
    }

    private void loadAssignments() {
        for (int i = 0; i < playerCount; i++) {
            int fallback = teamCount == 0 ? 0 : Math.min(teamCount - 1, i * teamCount / playerCount);
            int team = preferences.getInt("team_" + i, fallback);
            assignments.add(Math.max(0, Math.min(teamCount - 1, team)));
        }
    }

    private int assignmentAt(int index) {
        if (index >= assignments.size()) return Math.min(teamCount - 1, index * teamCount / playerCount);
        return Math.max(0, Math.min(teamCount - 1, assignments.get(index)));
    }

    private int colorForTeam(int team) { return TEAM_COLORS[team % TEAM_COLORS.length]; }

    private GradientDrawable rowBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(withAlpha(color, 38));
        background.setCornerRadius(dp(15));
        background.setStroke(dp(1), withAlpha(color, 105));
        return background;
    }

    private int withAlpha(int color, int alpha) { return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)); }
    private RippleDrawable ripple(int backgroundColor, int radius, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), round(backgroundColor, radius), null);
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }
}
