package hand;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 漂亮的ProgressBar
 * Created by cool on 2016/9/14.
 */
public class ColorfulProgressBar extends View {

  private int mReachedColor;//进度条到达了的颜色
  private int mUnReachedColor;//进度条未到达的颜色
  private float mReachedHight;//进度条的到达的高度
  private float mUnReachedHight;//进度条未到达的高度
  private float mProgressTextOffset;
  private float mProgressTextSize;

  private Paint mReachPaint;//到达画笔
  private Paint mUnReachPaint;//未到达画笔
  private Paint mTextPaint;//文本画笔
  private int width;
  private int hight;
  private int mCurrentProgress;
  private int mTotalProgress = 100;
  private int textWith;

  public ColorfulProgressBar(Context context) {
    this(context, null);
  }

  public ColorfulProgressBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ColorfulProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mReachedColor = Color.parseColor("#2196F3");
    mUnReachedColor = Color.rgb(204, 204, 204);
    mReachedHight = dp2px(context, 4.5f);
    mUnReachedHight = dp2px(context, 3.5f);
    mProgressTextOffset = dp2px(context, 3.0f);
    mProgressTextSize = sp2px(context, 14.0f);

    init();
  }

  private void init() {
    mReachPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mReachPaint.setColor(mReachedColor);
    mReachPaint.setStrokeWidth(mReachedHight);

    mUnReachPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mUnReachPaint.setColor(mUnReachedColor);
    mUnReachPaint.setStrokeWidth(mUnReachedHight);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mReachedColor);
    mTextPaint.setTextSize(mProgressTextSize);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawUnReachLine(canvas);
    drawText(canvas);
    drawReachLine(canvas);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    if (heightMode != MeasureSpec.EXACTLY) {
      float textHeight = mTextPaint.descent() - mTextPaint.ascent();
      int exceptHeight = (int) (getPaddingTop() + getPaddingBottom() + Math
        .max(Math.max(mReachedHight,
          mUnReachedHight), Math.abs(textHeight)));
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight, MeasureSpec.EXACTLY);

    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  /**
   * 画文本
   *
   * @param canvas
   */
  private void drawText(Canvas canvas) {
    String text = mCurrentProgress * 100 / mTotalProgress + "%";
    Rect rect = new Rect();
    mTextPaint.getTextBounds(text, 0, text.length(), rect);
    textWith = rect.right - rect.left;
    int x = (int) (((float) mCurrentProgress / mTotalProgress) * width + mProgressTextOffset);
    int y = hight / 2 + (rect.bottom - rect.top) / 2;

    if (x >= width - textWith) {
      x = width - textWith;
    }

    canvas.drawText(text, x, y, mTextPaint);
  }


  /**
   * 画未到达的直线
   *
   * @param canvas
   */
  private void drawUnReachLine(Canvas canvas) {
    String text = mCurrentProgress * 100 / mTotalProgress + "%";
    Rect rect = new Rect();
    mTextPaint.getTextBounds(text, 0, text.length(), rect);
    textWith = rect.right - rect.left;
    float startX = (int) (((float) mCurrentProgress / mTotalProgress) * width + mProgressTextOffset + textWith + mProgressTextOffset);
    float startY = hight / 2;
    float endX = width;
    float endY = hight / 2;
    canvas.drawLine(startX, startY, endX, endY, mUnReachPaint);
  }

  /**
   * 画到达了的直线
   *
   * @param canvas
   */
  private void drawReachLine(Canvas canvas) {
    float startX = 0;
    float startY = hight / 2;
    float endX = ((float) mCurrentProgress / mTotalProgress) * width;
    float endY = hight / 2;

    if (endX >= width - textWith) {
      endX = width - textWith;
    }

    canvas.drawLine(startX, startY, endX, endY, mReachPaint);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    width = w;
    hight = h;
  }

  public void setProgress(int currentProgress) {
    mCurrentProgress = currentProgress;
    postInvalidate();
  }

  public void setMax(int totalProgress) {
    mTotalProgress = totalProgress;
  }

  /**
   * dp转px
   *
   * @param context
   * @param dp
   * @return
   */
  public int dp2px(Context context, float dp) {
    float density = context.getResources().getDisplayMetrics().density;
    int px = (int) (dp * density + 0.5f);
    return px;
  }

  /**
   * px转dp
   *
   * @param context
   * @param px
   * @return
   */
  public int px2dp(Context context, float px) {
    float density = context.getResources().getDisplayMetrics().density;
    int dp = (int) (px / density + 0.5f);
    return dp;
  }

  /**
   * sp转px
   *
   * @param context
   * @param sp
   * @return
   */
  public float sp2px(Context context, float sp) {
    final float scale = context.getResources().getDisplayMetrics().scaledDensity;
    return sp * scale;
  }
}
