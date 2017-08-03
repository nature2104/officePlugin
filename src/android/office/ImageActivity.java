package office;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.FloatMath;

/**
 * Created by Oliver on 2016/8/3.
 */
public class ImageActivity extends Activity implements View.OnTouchListener {


  private static final int NONE = 0;
  private static final int DRAG = 1;
  private static final int ZOOM = 2;
  private final static String EXTRA_FILE_NAME = "FILE";
  float minScaleR = 1.0f;
  private int mode = NONE;
  private float oldDist;
  private Matrix matrix = new Matrix();
  private Matrix savedMatrix = new Matrix();
  private PointF start = new PointF();
  private PointF mid = new PointF();
  private ImageView imageView,ivSend;
 // private String fileName = "";
  private Bitmap bmp = null;
  private DisplayMetrics dm;
  private String path;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutId("layout_image"));
    bindView();
  }

  private void bindView(){
    Intent intent = getIntent();
    String action = intent.getStringExtra("action");
    if ("action_inner".equals(action)) {
      path = getIntent().getExtras().getString(EXTRA_FILE_NAME);
    } else {
      Uri data = intent.getData();
      path = data.getPath();
    }

    if(path.replace(" ","").equals("")){
      Toast.makeText(ImageActivity.this,getResources().getString(getResources().getIdentifier("can_not_open_file", "string", getPackageName())),Toast.LENGTH_LONG).show();
      onBackPressed();
      return;
    }
    imageView = (ImageView) findViewById(getId("iv_image"));
    ivSend = (ImageView) findViewById(getId("iv_send"));
    imageView.setBackgroundColor(Color.parseColor("#9f000000"));
 //   fileName = path.substring(path.lastIndexOf("/"));
    bmp = BitmapFactory.decodeFile(path);
    imageView.setImageBitmap(bmp);
    dm = getResources().getDisplayMetrics();
    imageView.setOnTouchListener(this);
    Animation animation = AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
    animation.setStartOffset(300);
    ivSend.startAnimation(animation);
    ivSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendFile();
      }
    });
    center(true, true);
    imageView.setImageMatrix(matrix);
  }


  @Override
  protected void onStop() {
    super.onStop();

  }

  @Override
  public void onBackPressed() {
    finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    super.onBackPressed();
  }


  private void sendFile(){
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("image/*");
    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
    startActivity(Intent.createChooser(intent, getResources().getString(getResources().getIdentifier("share_photo", "string", getPackageName()))));
  }

  private int getLayoutId(String layoutName) {
    return getResources().getIdentifier(layoutName, "layout", getPackageName());
  }

  private int getId(String idName) {
    return getResources().getIdentifier(idName, "id", getPackageName());
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {

    ImageView view = (ImageView) v;
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        savedMatrix.set(matrix);
        start.set(event.getX(), event.getY());
        mode = DRAG;
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        mode = NONE;

        break; //多点触控
      case MotionEvent.ACTION_POINTER_DOWN:
        oldDist = spacing(event);
        if (oldDist > 10f) {
          savedMatrix.set(matrix);
          midPoint(mid, event);
          mode = ZOOM;
        }
      case MotionEvent.ACTION_MOVE:
        if (mode == DRAG) {
          matrix.set(savedMatrix);
          matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
        } else if (mode == ZOOM) {
          float newDist = spacing(event);
          if (newDist > 10f) {
            matrix.set(savedMatrix);
            float scale = newDist / oldDist;
            matrix.postScale(scale, scale, mid.x, mid.y);
          }
          }
    }
    view.setImageMatrix(matrix);
    checkView();
    return true;
  }


  //两点间距离
  private float spacing(MotionEvent event) {
    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return (float) Math.sqrt(x * x + y * y);
  }

  //中点坐标
  private void midPoint(PointF point, MotionEvent event) {
    float x = event.getX(0) + event.getX(1);
    float y = event.getY(0) + event.getY(1);
    point.set(x / 2, y / 2);
  }

  /**
   * 横向、纵向居中
   */
  protected void center(boolean horizontal, boolean vertical) {
    Matrix m = new Matrix();
    m.set(matrix);
    RectF rect = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
    m.mapRect(rect);

    float height = rect.height();
    float width = rect.width();

    float deltaX = 0, deltaY = 0;

    if (vertical) {
      int screenHeight = dm.heightPixels;
      if (height < screenHeight) {
        deltaY = (screenHeight - height) / 2 - rect.top;
      } else if (rect.top > 0) {
        deltaY = -rect.top;
      } else if (rect.bottom < screenHeight) {
        deltaY = imageView.getHeight() - rect.bottom;
      }
    }

    if (horizontal) {
      int screenWidth = dm.widthPixels;
      if (width < screenWidth) {
        deltaX = (screenWidth - width) / 2 - rect.left;
      } else if (rect.left > 0) {
        deltaX = -rect.left;
      } else if (rect.right < screenWidth) {
        deltaX = screenWidth - rect.right;
      }
    }
    matrix.postTranslate(deltaX, deltaY);
  }


  /**
   * 限制最大最小缩放比例，自动居中
   */
  private void checkView() {
    float p[] = new float[9];
    matrix.getValues(p);
    if (mode == ZOOM) {
      if (p[0] < minScaleR) {
        matrix.setScale(minScaleR, minScaleR);
      }
      if (p[0] > 15f) {
        matrix.set(savedMatrix);
      }
    }
    center(true, true);
  }


}
