package com.example.widget;

import com.example.camera.R;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.*;
import android.content.Context;

public class HistgramDialog extends Dialog {

	private Window window = null;
	private TextView mMessageText;
	private ImageView mIcon;
	private HistgramSurfaceView hgsfv;
	
	public HistgramDialog(Context context) {

		// HistgramValue=HistgramValue;
		super(context);
	}

	public void SetHistgramDate(int[] Histgram) {

		hgsfv.SetHistgram(Histgram);
	}

	/**
	 * ������ַ�
	 * 
	 * @param message
	 *            ��ʾ���ַ�
	 */
	public void SetDialogMessage(String message) {
		mMessageText.setText(message);
	}

	/**
	 * ����ͼƬ��Դ
	 * 
	 * @param drawable
	 *            ��Ӧͼ��
	 */
	public void SetDialogIcon(Drawable drawable) {
		mIcon.setImageDrawable(drawable);
	}

	/**
	 * ����layout����ID������ͼ�꣬��Ϣ
	 * 
	 * @param layoutResID
	 *            dialoglayoutid
	 * @param messageId
	 *            ��ʾ��ID
	 * @param IconId
	 *            ��ԴID
	 */

	public void SetDialogLayout(int layoutResID, int messageId, int IconId) {
		setContentView(layoutResID);
		mMessageText = (TextView) findViewById(messageId);
		mIcon = (ImageView) findViewById(IconId);
	}

	public void SetDialogSurfaceView(int surfaceViewID)
	{
		hgsfv=(HistgramSurfaceView)findViewById(surfaceViewID);
	}
	/***
	 * ��ʾ��Ӧ��dialog��
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 * @param title
	 *            dialog�����
	 */
	public void showDialog(int x, int y, String title) {

		windowDeploy(x, y);
		this.setTitle(title);
		// ���ô����Ի�������ĵط�ȡ���Ի���
		setCanceledOnTouchOutside(true);
		show();
	}

	/**
	 * ���ô��ڷ��
	 * 
	 * @param x
	 * @param y
	 */
	// ���ô�����ʾ
	public void windowDeploy(int x, int y) {
		window = getWindow(); // �õ��Ի���
		window.setWindowAnimations(R.style.dialogWindowAnim); // ���ô��ڵ�������
		window.setBackgroundDrawableResource(R.color.vifrification); // ���öԻ��򱳾�Ϊ͸��
		WindowManager.LayoutParams wl = window.getAttributes();
		// ����x��y�������ô�����Ҫ��ʾ��λ��
		wl.x = x; // xС��0���ƣ�����0����
		wl.y = y; // yС��0���ƣ�����0����

		window.setAttributes(wl);
	}

}
