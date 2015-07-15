package com.example.widget;

import com.example.camera.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class HistgramSurfaceView extends SurfaceView implements Callback,
		Runnable {

	private SurfaceHolder sfh;
	private Paint paint;
	private Thread th;
	private boolean flag;
	private Canvas canvas;
	public static int ScreenX, ScreenY;

	public int[] display;
	public int Max=0;
	
	public HistgramSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		setFocusable(true);
	}

	/**
	 * 设置直方图
	 * 
	 * @param display
	 *            直方图数组
	 */
	public void SetHistgram(int[] display) {
		this.display = display;
		Max=0;
		for(int i=0;i<display.length;i++)
		{
			Max=Math.max(Max, display[i]);
		}
		
	}

	public void myDraw() {

		try {
			canvas = sfh.lockCanvas();

			canvas.drawColor(getResources().getColor(R.color.vifrification));

			canvas.save();

			canvas.restore();

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (flag) {
			long start = System.currentTimeMillis();
			myDraw();
			long end = System.currentTimeMillis();

			try {
				if (end - start < 50) {
					Thread.sleep(50 - (end - start));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		ScreenX = this.getWidth();
		ScreenY = this.getHeight();
		flag = true;

		// 实例线程
		th = new Thread(this);
		// 启动线程
		th.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

}
