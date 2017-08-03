/*
 * Copyright (C) Unpublished by Olivephone Co.Ltd. All rights reserved.
 * Olivephone Co.Ltd, Confidential and Proprietary.
 * Unless otherwise explicitly stated in writing, this software may not be used by or disclosed to any third party.
 * This software is subject to copyright protection under the laws of the People's Republic of China and other countries.
 * Unless otherwise explicitly stated, this software is only provided by Olivephone Co.Ltd "AS IS".
 */
package office;

import java.io.File;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.olivephone.sdk.InternalCopyListener;
import com.olivephone.sdk.SelectionListener;
import com.olivephone.sdk.WordViewController;
import com.olivephone.sdk.WordViewController.RevisingStyle;

/**
 * @author SiJyun
 */
public class WordActivity extends BaseDocumentActivity {
	private final Handler handler;

	public WordActivity() {
		this.handler = new Handler();
	}

	@Override
	protected void onCreate(File file) {
		super.onCreate(file);
	}

	@Override
	protected void initDocument(File file) {
		super.initDocument(file);
		final WordViewController word = (WordViewController) this.docViewController;
		word.setRevisingStyle(RevisingStyle.FinalStat);
		word.setInternalCopyListener(new InternalCopyListener() {
			@Override
			public void onCopy(String copyText) {
				Toast.makeText(WordActivity.this, copyText, Toast.LENGTH_SHORT).show();
			}
		});
		word.setSelectionListener(new SelectionListener() {
			@Override
			public void onSelectionChanged() {
				WordActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int start = word.getSelectionStart();
						int end = word.getSelectionEnd();
						if (start == end) {
//							Toast.makeText(WordActivity.this, "No selection", Toast.LENGTH_SHORT).show();
						} else {
//							Toast.makeText(WordActivity.this, "Selected", Toast.LENGTH_SHORT).show();
						}
					}
				});

			}
		});
	}

	@Override
	protected String copyTextInternal() {
		WordViewController word = ((WordViewController) this.docViewController);
		int start = word.getSelectionStart();
		int end = word.getSelectionEnd();
		if (start > end) {
			return "(No Selection)";
		} else {
			return word.getPlainText(start, end);
		}
	}

	private static final String MENU_SAVE = "save";
	private static final String MENU_ITALIC = "italic";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_SAVE);
		menu.add(MENU_ITALIC);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		WordViewController word = ((WordViewController) this.docViewController);
		if (item.getTitle().equals(MENU_SAVE)) {
			word.save(new WordViewController.SaveListener() {
				@Override
				public void onSaveProgressChanged(final int newProgress) {
					WordActivity.this.handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(WordActivity.this, "Save Progress : " + newProgress, Toast.LENGTH_SHORT).show();

						}
					});
				}

				@Override
				public void onSaveFinished() {
					WordActivity.this.handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(WordActivity.this, "Saved", Toast.LENGTH_SHORT).show();
						}
					});

				}

				@Override
				public void onSaveError(String message, final Throwable throwable) {
					WordActivity.this.handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(WordActivity.this, "Save Fail", Toast.LENGTH_SHORT).show();
							throwable.printStackTrace();
						}
					});
				}

				@Override
				public void onSaveCancelled() {
					WordActivity.this.handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(WordActivity.this, "Save Cancel", Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			return true;
		} else if (item.getTitle().equals(MENU_ITALIC)) {
			int start = word.getSelectionStart();
			int end = word.getSelectionEnd();
			if (start < end) {
				WordViewController.SpanStyle span = word.getSpanStyle(start, end);
				// 如果span.italic==null表明没有预设值
				if (span.italic == null || !span.italic) {
					span.italic = true;
				} else {
					span.italic = false;
				}
				word.setSpanStyle(start, end, span);
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}
}
