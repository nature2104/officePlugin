package office;

import java.io.File;
import com.olivephone.sdk.PDFController;
import com.olivephone.sdk.PageViewController;
import com.olivephone.sdk.PageViewController.PageChangedListener;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author SiJyun
 *
 */
public class PDFActivity extends BaseDocumentActivity {
	@Override
	protected void onCreate(File file) {
		super.onCreate(file);
	}

	@Override
	protected void initViews() {
		super.initViews();
		PDFController pdfController = (PDFController) this.docViewController;

		this.findViewById(getId("control_page_panel")).setVisibility(View.VISIBLE);
		final PageViewController page = (PageViewController) PDFActivity.this.docViewController;

		page.setPageChangedListener(new PageChangedListener() {
			@Override
			public void onPageChanged(int pageNumber) throws Exception {
				PDFActivity.this.updatePageInfo();
			}
		});
		this.findViewById(getId("control_goto_next")).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						page.nextPage();
					}
				});
		this.findViewById(getId("control_goto_prev")).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						page.prevPage();
					}
				});
		this.findViewById(getId("control_page_input_ok")).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
            EditText editText = (EditText) PDFActivity.this.findViewById(getId("control_page_input"));
            if(editText.getText().toString().trim().equals(""))return;
						int pageNumber = Integer
								.parseInt(((EditText) PDFActivity.this
										.findViewById(getId("control_page_input")))
										.getText().toString());
						boolean result = page.gotoPage(pageNumber);
					}
				});

	}

	@Override
	protected void onDocumentLoaded() {
		super.onDocumentLoaded();
		this.updatePageInfo();
	}

	private void updatePageInfo() {
		final PageViewController page = (PageViewController) PDFActivity.this.docViewController;
		((TextView) PDFActivity.this.findViewById(getId("control_page_info")))
				.setText(page.getCurrentPage() + "/" + page.getPageCount());
	}
	private int getId(String idName){
		return getResources().getIdentifier(idName,"id",getPackageName());
	}

}
