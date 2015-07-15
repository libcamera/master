package com.example.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class MySurfaceView extends SurfaceView implements Callback, Runnable {

	private Thread th;
	private SurfaceHolder sfh;
	private Paint p;
	private Camera camera; // 定义系统所用的照相机
	private boolean isPreview = false; // 是否在浏览中
	private Camera.AutoFocusCallback mAutoFocusCallBack;
	public String strCaptureFilePath = "/camera_snap.png";
	private static String TAG = "debug";
	private Camera.PreviewCallback previewCallback;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private boolean isshoot = false;
	private boolean isFocus = false;
	public static final String SDCARD_ROOT_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath();// 路径

	public MySurfaceView(Context context, AttributeSet attrs) {

		super(context, attrs);

		p = new Paint();
		p.setAntiAlias(true);
		sfh = this.getHolder();

		th = new Thread(this);
		this.setKeepScreenOn(true);

		setFocusable(true);
		sfh.addCallback(this);

		mAutoFocusCallBack = new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
					isFocus = true;
					camera.setOneShotPreviewCallback(previewCallback);
					Log.d(TAG, "onAutoFocus success");
				}
			}
		};
		previewCallback = new Camera.PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera arg1) {
				if (data != null) {
					Camera.Parameters parameters = camera.getParameters();
					int imageFormat = parameters.getPreviewFormat();
					Log.i("map", "Image Format: " + imageFormat);

					if (isshoot) {
						takePicture();
						isshoot = false;
					}
				}
			}
		};

		mTimer = new Timer();
		mTimerTask = new CameraTimerTask();
		mTimer.schedule(mTimerTask, 0, 5000);
		// 聚焦时间设置

		sfh.addCallback(new Callback() {

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			public void surfaceCreated(SurfaceHolder holder) {
				init_Camera(); // 打开初始化摄像头
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				// 如果camera不为null ,释放摄像头
				if (camera != null) {
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
				System.exit(0);
			}
		});
	}

	public void surfaceCreated(SurfaceHolder holder) {
		th.start();
		init_Camera();
	}

	@SuppressLint("NewApi")
	private void init_Camera() {
		int Photo_Width = 0, Photo_Height = 0;
		int PareView_Width = 640, PareView_Height = 320;

		// 定义两个参数获得最大的预览和拍照像素

		if (!isPreview) {
			camera = Camera.open();
		}

		if (camera != null && !isPreview) {
			try {

				Camera.Parameters parameters = camera.getParameters();
				StringBuffer sb = new StringBuffer();
				List<Camera.Size> sizeList = parameters
						.getSupportedPictureSizes();

				Iterator<Camera.Size> itor1 = sizeList.iterator();

				while (itor1.hasNext()) {

					Camera.Size cur = itor1.next();
					String str = cur.height + "x" + cur.width + ":";
					sb.append(str);

					if (Math.abs(cur.width * cur.height - 2000000) < 500000) {
						Photo_Width = cur.width;
						Photo_Height = cur.height;
						break;
						// 大于500W退出，若是最大像素，可能报错
					}

				}

				StringBuffer sb1 = new StringBuffer();
				List<Camera.Size> sizeList1 = parameters
						.getSupportedPreviewSizes();

				Iterator<Camera.Size> itor2 = sizeList1.iterator();
				while (itor2.hasNext()) {
					Camera.Size cur = itor2.next();
					String str = cur.height + "x" + cur.width + ":";
					sb1.append(str);
					if (cur.width >= 640) {
						PareView_Width = cur.width;
						PareView_Height = cur.height;
					}
				}
				// 得到预览界面和拍照界面的大小
				Log.i("Travel", "sb……" + sb.toString());

				String str = "" + Photo_Width + "*" + Photo_Height;

				//parameters.setPreviewSize(PareView_Width, PareView_Height); // 设置预览照片的大小
				// parameters.setPictureFormat(PixelFormat.RGB_565);
//				parameters.setPictureSize(Photo_Width, Photo_Height); // 设置照片的大小因为旋转了90度

				parameters.setPictureSize(0, 0); // 设置照片的大小因为旋转了90度

				MainActivity.instance.Camera_Size(str);

				camera.setDisplayOrientation(90);
				camera.setPreviewDisplay(sfh); // 通过SurfaceView显示取景画面
				// 自动对焦这个方法是在两者之间，startPreview与stop之间使用
				camera.setParameters(parameters); // android2.3.3以后不需要此行代码
				camera.startPreview(); // 开始预览
				camera.autoFocus(mAutoFocusCallBack);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}

	}

	public void takePoto() {
		if (isFocus) {
			isshoot = true;
			camera.autoFocus(mAutoFocusCallBack);
		} else {
			takePicture();
		}
	}

	public void takePicture() {
		if (camera != null) {

			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}

	public void onFocus() {
		camera.autoFocus(mAutoFocusCallBack);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return true;
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Shutter has closed
		}
	};

	private PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			// TODO Handle RAW image data
		}
	};

	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			// TODO Handle JPEG image data

			/* onPictureTaken肚�J�翰膜@�影鸭僻Y�艾郅���byte */
			Bitmap bm = BitmapFactory.decodeByteArray(_data, 0, _data.length);
			Matrix matrix = new Matrix();

			matrix.setRotate(90);

			Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);

			File myCaptureFile = new File(SDCARD_ROOT_PATH + strCaptureFilePath);
			try {

				MainActivity.instance.Camera_View(resizedBitmap);

				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(myCaptureFile));

				bm.compress(Bitmap.CompressFormat.PNG, 50, bos);

				bos.flush();

				bos.close();

			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			camera.startPreview();
		}
	};

	private void delFile(String strFileName) {
		try {
			File myFile = new File(strFileName);
			if (myFile.exists()) {
				myFile.delete();
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
	}

	private boolean checkSDCard() {
		/* �P�_�O拘�d�O�_�s�b */
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public void surfaceChanged1(SurfaceHolder surfaceholder, int format, int w,
			int h) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Surface Changed");
		init_Camera();
	}

	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			// draw();

			try {
				Thread.sleep(100);
			} catch (Exception ex) {
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	class CameraTimerTask extends TimerTask {
		@Override
		public void run() {
			if (camera != null) {
				camera.autoFocus(mAutoFocusCallBack);
			}
		}
	}
}
