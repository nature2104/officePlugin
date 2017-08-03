package office;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.olivephone.sdk.DocumentView;
import com.olivephone.sdk.DocumentViewController;
import com.olivephone.sdk.DocumentViewController.PageScaleListener;
import com.olivephone.sdk.DocumentViewController.PageScrollListener;
import com.olivephone.sdk.DocumentViewFactory;
import com.olivephone.sdk.LicenseData;
import com.olivephone.sdk.LoadListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import junit.framework.Assert;

/**
 * @author SiJyun
 */
public abstract class BaseDocumentActivity extends Activity {
  protected static final String LOG_TAG = "OliveOffice SDK";
  public static final String EXTRA_FILE_NAME = "FILE";
  protected LinearLayout contentContainer;

  protected DocumentView docView;
  protected DocumentViewController docViewController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
    super.onCreate(savedInstanceState);


    try {
      InputStream in = this.getAssets().open("Lanxin.license");
      byte[] licenseData = new byte[1024];
      int licenseDataLength = in.read(licenseData);
      LicenseData.setLicense(licenseData, 0, licenseDataLength);
      in.close();
    } catch (IOException e) {
    }
    Intent intent = getIntent();
    String action = intent.getStringExtra("action");
    File file;
    if ("action_inner".equals(action)) {
      Bundle extras = getIntent().getExtras();
      String fileKey = EXTRA_FILE_NAME;
      file = new File(extras.getString(fileKey));
    } else {
      Uri data = intent.getData();
      String filePath = data.getPath();
      file = new File(filePath);
    }

    this.onCreate(file);
  }

  protected void onCreate(final File file) {
//    this.requestWindowFeature(Window.FEATURE_PROGRESS);
    this.setContentView(getLayoutId("content_activity"));
    this.initDocument(file);
    this.initViews();
    this.bindViewToContainer();


    try {
      // 加载文件
      boolean encrypted = BaseDocumentActivity.this.docViewController.checkEncrypted(file);
//          boolean encrypted = BaseDocumentActivity.this.docViewController.checkEncrypted(new FileInputStream(file), true);
      if (encrypted) {
        BaseDocumentActivity.this.requestPasswordAndLoadFile();
      } else {

        BaseDocumentActivity.this.loadDocument(null);
      }
    } catch (IOException e) {
      Log.e(BaseDocumentActivity.this.getPackageName(), "检查文件加密出错", e);
      BaseDocumentActivity.this.finish();
    }
  }

  @Override
  protected void onStop() {
    finish();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    this.docView.destroy();
    this.docViewController = null;
    super.onDestroy();
  }

  protected void initDocument(File file) {
    String fileName = file.getName().toLowerCase(Locale.US);
    Map<String, String> options = new HashMap<String, String>();
    options.put(DocumentViewController.Options.OPTIONS_DEFAULT_SCALE, "0.5");
    options.put(DocumentViewController.Options.OPTIONS_DEFAULT_ENCODING, "UTF-8");
    if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".txt")) {
      this.docView = DocumentViewFactory.newWordView(this);
    } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
      this.docView = DocumentViewFactory.newSpreadsheetView(this);
    } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
      this.docView = DocumentViewFactory.newPresentationView(this);
    } else if (fileName.endsWith(".pdf")) {
      this.docView = DocumentViewFactory.newPDFView(this);
    } else {
      Assert.fail();
    }
    Assert.assertTrue(this.docView != null);
    this.docViewController = this.docView.getController();
    if (!fileName.endsWith(".pdf")) {// PDF In progress
      this.docViewController.setPageScrollListener(new PageScrollListener() {
        @Override
        public void onPageScolled(boolean isFinished) {
          Log.i(LOG_TAG, "Page scrolled. State : " + isFinished);
        }
      });
      this.docViewController.setPageScaleListener(new PageScaleListener() {
        @Override
        public void onPageScaleChanged() {
          Log.i(LOG_TAG, "Page scale changed.");
        }
      });

    }
  }

  protected void initViews() {
    this.contentContainer = (LinearLayout) this.findViewById(getId("content_container"));
    this.findViewById(getId("control_zoom_in")).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BaseDocumentActivity.this.docViewController.zoomIn();
      }
    });
    this.findViewById(getId("control_zoom_out")).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BaseDocumentActivity.this.docViewController.zoomOut();
      }
    });

    this.findViewById(getId("control_goto_top")).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BaseDocumentActivity.this.docViewController.goToTop();
      }
    });
    this.findViewById(getId("control_goto_bottom")).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BaseDocumentActivity.this.docViewController.goToBottom();
      }
    });
    this.findViewById(getId("control_copy_text")).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String text = BaseDocumentActivity.this.copyTextInternal();
        Toast.makeText(BaseDocumentActivity.this, text, Toast.LENGTH_SHORT).show();
      }
    });

  }

  protected void bindViewToContainer() {
    View content = this.docView.asView();
    this.contentContainer.addView(content);
  }

  private void requestPasswordAndLoadFile() {
    final EditText passwordEdit = new EditText(this);
    passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
    new AlertDialog.Builder(this).setTitle("Please input the password:").setView(passwordEdit).setPositiveButton("Open", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // BaseDocumentActivity.this.loadDocument(passwordEdit.getText().toString());
        BaseDocumentActivity.this.loadDocument("890424ok");
      }
    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        BaseDocumentActivity.this.finish();
      }
    }).show();
  }

  protected void loadDocument(String password) {
    Log.e("asd", "load");
    this.docViewController.loadFile(password, new LoadListener() {
      @Override
      public void onWrongPassword() {
        BaseDocumentActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(BaseDocumentActivity.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
            BaseDocumentActivity.this.finish();
          }
        });
      }

      @Override
      public void onProgressChanged(final int newProgress) {
        BaseDocumentActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            BaseDocumentActivity.this.setProgress(newProgress);
          }
        });
      }

      @Override
      public void onError(String description, final Throwable throwable) {
        Log.e(BaseDocumentActivity.this.getPackageName(), description, throwable);
        BaseDocumentActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(BaseDocumentActivity.this, "Load fail.", Toast.LENGTH_SHORT).show();
            BaseDocumentActivity.this.finish();
          }
        });
      }

      @Override
      public void onDocumentLoaded() {
        BaseDocumentActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            BaseDocumentActivity.this.setProgress(10000);
            BaseDocumentActivity.this.onDocumentLoaded();
          }
        });
      }
    });
  }

  protected void onDocumentLoaded() {
  }

  protected String copyTextInternal() {
    return "(Unsupported Opearation)";
  }

  private int getLayoutId(String layoutName) {
    return getResources().getIdentifier(layoutName, "layout", getPackageName());
  }

  private int getId(String idName) {
    return getResources().getIdentifier(idName, "id", getPackageName());
  }
}
