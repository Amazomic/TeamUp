package ru.teampicker.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;

/**
 * Frame-synchronized intro: the six approved storyboard key poses are rendered
 * as continuously interpolated vector motion rather than six discrete GIF frames.
 * No networking, video decoding or animation assets are needed at launch.
 */
final class SplashView extends View {
    private static final int CYAN = Color.rgb(42, 223, 242);
    private static final int TEAL = Color.rgb(20, 232, 204);
    private static final int ORANGE = Color.rgb(255, 174, 99);
    private static final int CORAL = Color.rgb(255, 104, 122);
    private static final int[] BODY_START = {
        Color.rgb(67, 169, 255), Color.rgb(29, 235, 220), Color.rgb(25, 204, 223),
        Color.rgb(255, 177, 86), Color.rgb(255, 132, 45), Color.rgb(255, 113, 130)
    };
    private static final int[] BODY_END = {
        Color.rgb(25, 100, 232), Color.rgb(13, 173, 208), Color.rgb(1, 186, 165),
        Color.rgb(246, 120, 39), Color.rgb(251, 101, 40), Color.rgb(241, 81, 112)
    };

    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] bodies = new Paint[SplashMotion.PLAYER_COUNT];
    private final Paint[] heads = new Paint[SplashMotion.PLAYER_COUNT];
    private final Paint title = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint subtitle = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Path shirt = new Path();
    private final RectF ringBounds = new RectF(58f, 217f, 302f, 461f);
    private float progress;
    private ValueAnimator animator;
    private Runnable onFinished;
    private boolean completed;

    SplashView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setContentDescription("TeamUp. Shuffle into teams");
        setOnClickListener(v -> finish());

        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(15f);
        ring.setStrokeCap(Paint.Cap.ROUND);
        ringGlow.setStyle(Paint.Style.STROKE);
        ringGlow.setStrokeWidth(26f);
        ringGlow.setStrokeCap(Paint.Cap.ROUND);

        for (int i = 0; i < SplashMotion.PLAYER_COUNT; i++) {
            bodies[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            bodies[i].setShader(new LinearGradient(-24f, 5f, 25f, 54f,
                    BODY_START[i], BODY_END[i], Shader.TileMode.CLAMP));
            heads[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            heads[i].setShader(new LinearGradient(-16f, -16f, 16f, 17f,
                    i < 3 ? Color.rgb(225, 255, 255) : Color.rgb(255, 244, 220),
                    i < 3 ? Color.rgb(103, 222, 251) : Color.rgb(255, 188, 139),
                    Shader.TileMode.CLAMP));
        }
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setTextSize(39f);
        title.setTextAlign(Paint.Align.CENTER);
        title.setColor(Color.WHITE);

        subtitle.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        subtitle.setTextSize(15f);
        subtitle.setTextAlign(Paint.Align.CENTER);
        subtitle.setColor(Color.rgb(233, 245, 255));
    }

    void play(Runnable callback) {
        onFinished = callback;
        // Android's global "remove animations" setting should bypass the intro.
        if (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) {
            finish();
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(SplashMotion.DURATION_MS);
        animator.setInterpolator(null); // The motion paths provide their own easing.
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate(); // A new interpolated frame at display refresh (~60 fps).
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                finish();
            }
        });
        animator.start();
    }

    void finish() {
        if (completed) return;
        completed = true;
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        if (onFinished != null) onFinished.run();
    }

    @Override protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        background.setShader(new LinearGradient(0f, 0f, width * .9f, height,
                new int[]{Color.rgb(3, 190, 185), Color.rgb(4, 49, 99),
                        Color.rgb(19, 45, 129), Color.rgb(54, 69, 179)},
                new float[]{0f, .42f, .76f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, width, height, background);
        background.setShader(null);

        float scale = Math.min(width / 360f, height / 760f);
        canvas.save();
        canvas.translate((width - 360f * scale) / 2f,
                (height - 760f * scale) / 2f);
        canvas.scale(scale, scale);

        glow.setShader(new RadialGradient(32f, 42f, 286f,
                new int[]{0x6608E9CB, 0x1909BFCB, 0x00009FD7},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, 360f, 470f, glow);
        glow.setShader(null);

        float ringVisibility = SplashMotion.ringVisibility(progress);
        if (ringVisibility > 0f) {
            ringGlow.setAlpha((int) (44f * ringVisibility));
            ringGlow.setShader(new LinearGradient(62f, 255f, 297f, 382f,
                    CYAN, CORAL, Shader.TileMode.CLAMP));
            canvas.drawArc(ringBounds, 204f, 132f * ringVisibility, false, ringGlow);
            canvas.drawArc(ringBounds, 22f, 134f * ringVisibility, false, ringGlow);

            ring.setAlpha((int) (255f * ringVisibility));
            ring.setShader(new LinearGradient(58f, 217f, 302f, 360f,
                    new int[]{CYAN, Color.rgb(157, 251, 237), ORANGE},
                    null, Shader.TileMode.CLAMP));
            canvas.drawArc(ringBounds, 204f, 132f * ringVisibility, false, ring);
            ring.setShader(new LinearGradient(58f, 380f, 302f, 454f,
                    new int[]{CYAN, Color.rgb(115, 202, 255), CORAL},
                    null, Shader.TileMode.CLAMP));
            canvas.drawArc(ringBounds, 22f, 134f * ringVisibility, false, ring);
            ring.setShader(null);
            ringGlow.setShader(null);
        }

        for (int i = 0; i < SplashMotion.PLAYER_COUNT; i++) {
            float[] position = SplashMotion.position(i, progress);
            drawPlayer(canvas, i, position[0], position[1]);
        }

        float visible = SplashMotion.titleVisibility(progress);
        title.setAlpha((int) (255f * visible));
        subtitle.setAlpha((int) (255f * visible));
        canvas.drawText("TeamUp", 180f, 550f, title);
        canvas.drawText("Shuffle into teams", 180f, 581f, subtitle);
        canvas.restore();
    }

    private void drawPlayer(Canvas canvas, int index, float x, float y) {
        canvas.save();
        canvas.translate(x, y);
        // Slight shrink while moving makes the shuffle feel dynamic.
        float move = SplashMotion.movement(progress);
        float scale = 1f - .08f * (float) Math.sin(Math.PI * move);
        canvas.scale(scale, scale);
        shirt.reset();
        shirt.moveTo(-27f, 48f);
        shirt.cubicTo(-32f, 31f, -22f, 12f, 0f, 12f);
        shirt.cubicTo(22f, 12f, 32f, 31f, 27f, 48f);
        shirt.cubicTo(14f, 57f, -14f, 57f, -27f, 48f);
        shirt.close();
        canvas.drawPath(shirt, bodies[index]);
        canvas.drawCircle(0f, 0f, 17f, heads[index]);
        canvas.restore();
    }
}
