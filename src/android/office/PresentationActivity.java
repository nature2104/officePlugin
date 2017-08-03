package office;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.olivephone.sdk.PageViewController;
import com.olivephone.sdk.PageViewController.PageChangedListener;

import java.io.File;

/**
 * @author SiJyun
 * 
 */
public class PresentationActivity extends BaseDocumentActivity {
	@Override
	protected void onCreate(File file) {
		super.onCreate(file);
	}

	@Override
	protected void initViews() {
		super.initViews();
		this.findViewById(getId("control_page_panel")).setVisibility(View.VISIBLE);
		final PageViewController page = (PageViewController) PresentationActivity.this.docViewController;
		page.setPageChangedListener(new PageChangedListener() {
			@Override
			public void onPageChanged(int pageNumber) throws Exception {
				PresentationActivity.this.updatePageInfo();
			}
		});
		this.findViewById(getId("control_goto_next")).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				page.nextPage();
			}
		});
		this.findViewById(getId("control_goto_prev")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				page.prevPage();
			}
		});
		this.findViewById(getId("control_page_input_ok")).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pageNumber = Integer.parseInt(((EditText) PresentationActivity.this.findViewById(getId("control_page_input"))).getText().toString());
				boolean result = page.gotoPage(pageNumber);
				Toast.makeText(PresentationActivity.this, String.valueOf(result), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onDocumentLoaded() {
		super.onDocumentLoaded();
		this.updatePageInfo();
	}

	private void updatePageInfo() {
		final PageViewController page = (PageViewController) PresentationActivity.this.docViewController;
		((TextView) PresentationActivity.this.findViewById(getId("control_page_info"))).setText(page.getCurrentPage() + "/" + page.getPageCount());
	}

	private int getId(String idName){
		return getResources().getIdentifier(idName,"id",getPackageName());
	}
}
