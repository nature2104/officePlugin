package hand;

/**
 * Created by cool on 2016/10/9.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 环形进度条
 * Created by cool on 2016/8/17.
 */
public class CircleProgressBar extends View {

  private int mCurrentProgress;//当前的进度
  private int mTotalProgress = 100;//总的进度
  private Paint mCirclePaint;//实心圆画笔
  private Paint mRingPaint;//圆环画笔
  private Paint mTextPaint;//文本画笔
  private int mCircleColor;//实心圆颜色
  private int mRingColor;//圆环颜色
  private int mTextColor;//文本颜色
  private int mCircleRadius;//圆半径
  private int mRingRadius;//圆环半径
  private int mStrokeWidth;//圆环宽
  private int mCenterX;//圆心x坐标
  private int mCenterY;//圆心y坐标
  public CircleProgressBar(Context context) {
    this(context, null);
  }

  public CircleProgressBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mCircleColor = Color.parseColor("#858585");
    mRingColor = Color.parseColor("#63B8FF");
    mTextColor = Color.WHITE;
    mCircleRadius = dp2dx(context,60);
    mStrokeWidth = dp2dx(context, 20);
    init(context);
  }


  private void init(Context context) {
    mRingRadius = mCircleRadius + mStrokeWidth/2;

    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setColor(mCircleColor);
    mCirclePaint.setStyle(Paint.Style.FILL);

    mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mRingPaint.setColor(mRingColor);
    mRingPaint.setStyle(Paint.Style.STROKE);
    mRingPaint.setStrokeWidth(mStrokeWidth);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mTextColor);
    mTextPaint.setTextSize(mCircleRadius / 2);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawCircle(canvas);//画实心圆
    drawRing(canvas);//画圆环
    drawText(canvas);//画文本
  }

  /**
   * 画圆环
   * @param canvas
   */
  private void drawRing(Canvas canvas) {
    if(mCurrentProgress >0) {
      RectF rectF = new RectF();
      rectF.left = mCenterX - mRingRadius;
      rectF.top = mCenterY - mRingRadius;
      rectF.right = mCenterX - mRingRadius + mRingRadius * 2;
      rectF.bottom = mCenterY - mRingRadius + mRingRadius * 2;
      float startAngle = -90;
      float sweepAngle = ((float) mCurrentProgress / mTotalProgress) * 360;
      canvas.drawArc(rectF, startAngle, sweepAngle, false, mRingPaint);
    }
  }

  private void drawText(Canvas canvas) {
    if(mTotalProgress > 0) {

      int progress = mCurrentProgress * 100 / mTotalProgress;
      String text = progress + "%";
      Rect rect = new Rect();
      mTextPaint.getTextBounds(text, 0, text.length(), rect);
      int textWith = rect.right - rect.left;
      int textHight = rect.bottom - rect.top;
      float xPos = mCenterX - textWith / 2;
      float yPos = mCenterY + textHight / 2;
      canvas.drawText(text, xPos, yPos, mTextPaint);
    }
  }

  /**
   * 画实心圆
   * @param canvas
   */
  private void drawCircle(Canvas canvas) {
    canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mCirclePaint);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    mCenterX = w/2;//获取圆心x坐标
    mCenterY = h/2;//获取圆心y坐标
  }

  /**
   * 设置当前进度
   * @param currentProgress
   */
  public void setCurrentProgress(int currentProgress){
    this.mCurrentProgress = currentProgress;
    postInvalidate();
  }

  /**
   * 设置总进度
   * @param totalProgress
   */
  public void setTotalProgress(int totalProgress){
    this.mTotalProgress = totalProgress;
  }


  private int dp2dx(Context context, int dp) {
    float density = context.getResources().getDisplayMetrics().density;
    return (int) (dp*density + 0.5f);
  }

}
