package hand;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.lzy.okgo.request.BaseRequest;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Locale;

import office.BaseDocumentActivity;
import office.FileCallback;
import office.ImageActivity;
import office.PDFActivity;
import office.PresentationActivity;
import office.SpreadsheetActivity;
import office.WordActivity;
import okhttp3.Call;
import okhttp3.Response;

public class OfficePlugin extends CordovaPlugin {

  private CallbackContext mCallbackContext;
  public static final String EXTRA_FILE_NAME = "FILE";
  private String path;
  private boolean isNeedSelect = false;

  private AlertDialog alertDialog;
  private ColorfulProgressBar colorfulProgressBar;
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        int progress = msg.what;
      colorfulProgressBar.setProgress(progress);
      if(msg.arg1 == 1){
        alertDialog.show();
      }else if(msg.arg1 ==2){
        alertDialog.dismiss();
      }
    }
  };
  private SharedPreferences sp;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.e("399", "initialize");
    AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
    View view = View.inflate(cordova.getActivity(), getLayout("item_number_progress"), null);
    colorfulProgressBar = (ColorfulProgressBar) view.findViewById(getId("cpb_progress"));
    colorfulProgressBar.setMax(100);
    colorfulProgressBar.setProgress(0);
    alertDialog = builder.create();
    alertDialog.setCancelable(false);
    alertDialog.setView(view, 0, 0, 0, 0);

    sp = cordova.getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
    File externalStorageDirectory = Environment.getExternalStorageDirectory();
    Log.e("399",externalStorageDirectory.getAbsolutePath());
    initOkGo();
  }

  private void initOkGo() {
    //必须调用初始化
    OkGo.init(cordova.getActivity().getApplication());
    //好处是全局参数统一,特定请求可以特别定制参数
    try {
      //以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
      OkGo.getInstance()

        //打开该调试开关,控制台会使用 红色error 级别打印log,并不是错误,是为了显眼,不需要就不要加入该行
        .debug("OkGo")

        //如果使用默认的 60秒,以下三行也不需要传
        .setConnectTimeout(OkGo.DEFAULT_MILLISECONDS)  //全局的连接超时时间
        .setReadTimeOut(OkGo.DEFAULT_MILLISECONDS)     //全局的读取超时时间
        .setWriteTimeOut(OkGo.DEFAULT_MILLISECONDS)    //全局的写入超时时间

        //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy0216/
        .setCacheMode(CacheMode.NO_CACHE)

        //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
        .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)

        //如果不想让框架管理cookie,以下不需要
//                .setCookieStore(new MemoryCookieStore())                //cookie使用内存缓存（app退出后，cookie消失）
        .setCookieStore(new PersistentCookieStore());       //cookie持久化存储，如果cookie不过期，则一直有效

      //可以设置https的证书,以下几种方案根据需要自己设置
//                    .setCertificates()                                  //方法一：信任所有证书（选一种即可）
//                    .setCertificates(getAssets().open("srca.cer"))      //方法二：也可以自己设置https证书（选一种即可）
//                    .setCertificates(getAssets().open("aaaa.bks"), "123456", getAssets().open("srca.cer"))//方法三：传入bks证书,密码,和cer证书,支持双向加密

      //可以添加全局拦截器,不会用的千万不要传,错误写法直接导致任何回调不执行
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        return chain.proceed(chain.request());
//                    }
//                })
      //设置全局公共参数
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.mCallbackContext = callbackContext;
    path = args.getString(0);
    if ("openFileByFileUrl".equals(action)) {
      isNeedSelect = args.getBoolean(1);
      //检查权限
      if (!PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        //进入到这里代表没有权限.
        PermissionHelper.requestPermission(this,1,Manifest.permission.WRITE_EXTERNAL_STORAGE);

      } else {
        String filePath = sp.getString(path, "");
        if(!TextUtils.isEmpty(filePath)){
          File file = new File(filePath);
          if(file.exists()){
            openFileBySelect(filePath);
            return true;
          }
        }
        try {
          doDownLoad(path);
        } catch (Exception e) {
          alertDialog.dismiss();
          callbackContext.error("网络请求错误");
        }
      }
      return true;
    } else if ("openFileByFilePath".equals(action)) {
      //检查权限
      if (!PermissionHelper.hasPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
        //进入到这里代表没有权限.
        PermissionHelper.requestPermission(this,2,Manifest.permission.READ_EXTERNAL_STORAGE);
      } else {
        open(path);
      }
      return true;
    }
    callbackContext.error("error");
    return false;
  }

  /**
   * 下载附件
   *
   * @param path
   */
  private void doDownLoad(final String path) {

    OkGo.get(path)
      .tag(this)
      .execute(new FileCallback() {
        @Override
        public void onSuccess(File file, Call call, Response response) {
          String absolutePath = file.getAbsolutePath();
          sp.edit().putString(path,absolutePath).commit();
          Log.e("399", "path: " + absolutePath);
          openFileBySelect(absolutePath);

        }

        @Override
        public void onBefore(BaseRequest request) {
//          alertDialog.show();
          Log.e("399", "onBefore");
          Message message = new Message();
          message.arg1 = 1;
          mHandler.sendMessage(message);
        }

        @Override
        public void onAfter(File file,Exception e) {
          Log.e("399", "onAfter");
//          alertDialog.dismiss();
          Message message = new Message();
          message.what = 0;
          message.arg1 = 2;
          mHandler.sendMessage(message);
        }

        @Override
        public void downloadProgress(long currentSize, long totalSize, final float progress, long networkSpeed) {
          Log.e("399", "currentSize: " + currentSize + " totalSize: " + totalSize + " progress" + progress);
//          colorfulProgressBar.setMax((int) totalSize);
//          colorfulProgressBar.setProgress((int) currentSize);
          Message message = new Message();
          message.what = (int) (progress * 100);
          mHandler.sendMessage(message);
        }

        @Override
        public void onError(Call call, Response response, Exception e) {
          super.onError(call, response, e);
          Log.e("399", e.getMessage());
          mCallbackContext.error(e.getMessage());
        }
      });
  }

  private void openFileBySelect(String absolutePath) {
    if (isNeedSelect) {
      Intent fileIntent = getIntentByFileType(absolutePath);
      if (fileIntent != null) {
        cordova.getActivity().startActivity(fileIntent);
      } else {
        open(absolutePath);
      }
    } else {
      open(absolutePath);
    }
  }

  /**
   * 根据文件的类型获取相应的Intent
   *
   * @param absolutePath
   */
  private Intent getIntentByFileType(String absolutePath) {
    String ext = getTpye(absolutePath);
    if(TextUtils.isEmpty(ext)){
      mCallbackContext.error("格式错误");
      return null;
    }
    Intent intent;
    if (ext.endsWith(".doc") || ext.endsWith(".docx")) {
      intent = getWordFileIntent(absolutePath);
    } else if (ext.endsWith(".txt")) {
      intent = getTxtFileIntent(absolutePath);
    } else if (ext.endsWith(".xls") || ext.endsWith(".xlsx")) {
      intent = getExcelFileIntent(absolutePath);
    } else if (ext.endsWith(".ppt") || ext.endsWith(".pptx")) {
      intent = getPptFileIntent(absolutePath);
    } else if (ext.endsWith(".pdf")) {
      intent = getPdfFileIntent(absolutePath);
    } else if (ext.endsWith(".jpg") || ext.endsWith(".jpeg") || ext.endsWith(".png") || ext.endsWith(".bmp") || ext.endsWith(".gif")) {
      intent = getImageFileIntent(absolutePath);
    } else {
      mCallbackContext.error("格式错误");
      return null;
    }
    return intent;
  }

  private String getTpye(String absolutePath) {
    int indexOf = absolutePath.lastIndexOf('.');
    if (indexOf != -1) {
      String ext = absolutePath.substring(indexOf).toLowerCase(Locale.US);
      return ext;
    }
    return null;
  }

  /**
   * 打开文件
   *
   * @param filePath
   */
  private void open(String filePath) {
    Log.e("399", filePath);
    String ext = getTpye(filePath);
    if (TextUtils.isEmpty(ext)) {
      mCallbackContext.error("格式错误");
      return;
    }
    Class<? extends BaseDocumentActivity> targetActivity = null;

    if (ext.endsWith(".doc") || ext.endsWith(".docx") || ext.endsWith(".txt")) {
      targetActivity = WordActivity.class;
    } else if (ext.endsWith(".xls") || ext.endsWith(".xlsx")) {
      targetActivity = SpreadsheetActivity.class;
    } else if (ext.endsWith(".ppt") || ext.endsWith(".pptx")) {
      targetActivity = PresentationActivity.class;
    } else if (ext.endsWith(".pdf")) {
      targetActivity = PDFActivity.class;
    } else if (ext.endsWith(".jpg") || ext.endsWith(".jpeg") || ext.endsWith(".png") || ext.endsWith(".bmp") || ext.endsWith(".gif")) {
      Intent intent = new Intent(cordova.getActivity(), ImageActivity.class);
      intent.putExtra(EXTRA_FILE_NAME, filePath);
      cordova.getActivity().startActivity(intent);
      return;
    } else {
      Log.e("399", "格式错误");
      mCallbackContext.error("格式错误");
      return;
    }
    Intent intent = new Intent(cordova.getActivity(), targetActivity);
    intent.putExtra(EXTRA_FILE_NAME, filePath);
    intent.putExtra("action", "action_inner");

    cordova.getActivity().startActivity(intent);
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    super.onRequestPermissionResult(requestCode, permissions, grantResults);
    if (requestCode == 1) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //用户同意授权
        try {
          doDownLoad(path);
        } catch (Exception e) {
          alertDialog.dismiss();
          mCallbackContext.error("网络请求错误");
        }
      } else {
        //用户拒绝授权
        Log.e("399", "用户拒绝授权");
      }
    } else if (requestCode == 2) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //用户同意授权
        open(path);
      } else {
        //用户拒绝授权
        Log.e("399", "用户拒绝授权");
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    OkGo.getInstance().cancelTag(this);
  }

  /**
   * 获取打开Word的intent
   *
   * @param param
   * @return
   */
  public static Intent getWordFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "application/msword");
    return intent;
  }


  /**
   * 获取打开pdf的Intent
   *
   * @param param
   * @return
   */
  public static Intent getPdfFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "application/pdf");
    return intent;
  }

  /**
   * 获取打开ppt的Intent
   *
   * @param param
   * @return
   */
  public static Intent getPptFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
    return intent;
  }

  /**
   * 获取打开Excel的intent
   *
   * @param param
   * @return
   */
  public static Intent getExcelFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "application/vnd.ms-excel");
    return intent;
  }

  /**
   * 获取打开Txt的intent
   *
   * @param param
   * @return
   */
  public static Intent getTxtFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "text/plain");
    return intent;
  }

  /**
   * 获取打开Txt的intent
   *
   * @param param
   * @return
   */
  public static Intent getImageFileIntent(String param) {
    Intent intent = new Intent("android.intent.action.VIEW");
    intent.addCategory("android.intent.category.DEFAULT");
    Uri uri = Uri.fromFile(new File(param));
    intent.setDataAndType(uri, "image/jpeg");
    return intent;
  }

  private int getId(String idName) {
    Resources resources = cordova.getActivity().getResources();
    int resId = resources.getIdentifier(idName, "id", cordova.getActivity().getPackageName());
    return resId;
  }

  private int getLayout(String LayoutName) {
    Resources resources = cordova.getActivity().getResources();
    int resId = resources.getIdentifier(LayoutName, "layout", cordova.getActivity().getPackageName());
    return resId;
  }
}
