package com.example.camera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.*;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import com.digital.nativefuntion.nativefuntion;
import org.opencv.core.CvType;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private MySurfaceView msurfaceView;
	private Button mButton;
	private Button mSobelButton;
	private TextView mText;
	private TextView mState;
	private ImageView mimage;
	private Bitmap mBitmap, mBitmap1;
	public static MainActivity instance;

	private static String SrcPath = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/camera_snap.png";
	private static String SavePath = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/new.jpg";

	private static String SavePath2 = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/new";

	public static final int MODE_STATE_RGBA = 1;
	public static final int MODE_STATE_GRAY = 2;
	public static final int MODE_STATE_SOBEL = 3;
	public static final int MODE_STATE_CANNY = 4;
	public static final int MODE_STATE_PREWITT = 5;
	public static final int MODE_STATE_HOUNGH = 6;
	public static final int MODE_STATE_GRAYOPENCV = 7;
	public static final int MODE_STATE_GAUSSIAN = 8;
	public static final int MODE_STATE_HOUNGHLINE = 9;
	public static final int MODE_STATE_HOUNGHLINEP = 10;
	public static final int MODE_STATE_HOUNGHLINES = 11;
	public static final int MODE_STATE_OSTU = 15;
	public static final int MODE_STATE_DECTOR = 16;

	public static final int MODE_STATE_NATIVE_OSTU = 12;
	public static final int MODE_STATE_NATIVE_HISTGRAM = 13;
	public static final int MODE_STATE_NATIVE_HISTGRAMEVERAGE = 14;
	private static final int MODE_STATE_SUBMAT = 17;

	private int ModeState;

	private static final int CANNY_LOW = 90;
	private static final int CANNY_LOW2 = 180;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.layout_camera);

		msurfaceView = (MySurfaceView) findViewById(R.id.mySurfaceView1);
		mButton = (Button) findViewById(R.id.button1);
		mimage = (ImageView) findViewById(R.id.imageView1);
		mText = (TextView) findViewById(R.id.textView1);
		mSobelButton = (Button) findViewById(R.id.button2);

		mState = (TextView) findViewById(R.id.textView2);
		mState.setText(getResources().getText(R.string.str_state_rgba));

		ModeState = 0;

		mButton.setOnClickListener(newListener);
		mSobelButton.setOnClickListener(newListener);

	}

	Button.OnClickListener newListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.button1:
				ModeState = MODE_STATE_RGBA;
				msurfaceView.takePoto();
				break;
			case R.id.button2:
				ModeState = MODE_STATE_GRAY;
				msurfaceView.takePoto();
				mState.setText(getResources().getText(R.string.str_state_gray)
						.toString());
				Deal_Bitmap();
				break;
			default:
				break;
			}
		}

	};

	private void Deal_Bitmap() {
		if (mBitmap != null) {
			switch (ModeState) {
			case MODE_STATE_RGBA:
				mState.setText(getResources().getText(R.string.str_state_rgba)
						.toString());
				mimage.setImageBitmap(mBitmap);
				break;
			case MODE_STATE_GRAY:
				mState.setText(getResources().getText(R.string.str_state_gray)
						.toString());
				mimage.setImageBitmap(mBitmap1);
				break;
			case MODE_STATE_SOBEL:
				mState.setText(getResources().getText(R.string.str_state_sobel)
						.toString());
				Sobel_Bitmap();
				break;
			case MODE_STATE_NATIVE_OSTU:
				mState.setText(getResources().getText(R.string.str_state_sobel)
						.toString());
				TwoValue_Bitmap();
				break;

			case MODE_STATE_CANNY:
				mState.setText(getResources().getText(R.string.str_state_canny)
						.toString());
				Canny_Bitmap();
				break;
			case MODE_STATE_PREWITT:
				mState.setText(getResources().getText(R.string.str_state_canny)
						.toString());
				Prewitt_Bitmap();
				break;
			case MODE_STATE_GRAYOPENCV:
				grayMat();
				break;
			case MODE_STATE_HOUNGH:
				Houngh_Bitmap();
				break;
			case MODE_STATE_GAUSSIAN:
				GaussianBlur();
				break;
			case MODE_STATE_HOUNGHLINE:
				Houngh_Line();
				break;
			case MODE_STATE_HOUNGHLINEP:
				Houngh_LineP();
				break;
			case MODE_STATE_NATIVE_HISTGRAM:
				Native_Histgram();
				break;
			case MODE_STATE_NATIVE_HISTGRAMEVERAGE:
				HistgramAverage_Bitmap();
				break;
			case MODE_STATE_OSTU:
				OpenCV_Ostu();
				break;
			case MODE_STATE_DECTOR:
				fastFeatureDetector();
				break;
			case MODE_STATE_SUBMAT:
				SubMat();
				break;
			default:
				mMakeTextToast("No Photo..", true);
				break;
			}
		}
	}

	private void Native_Histgram() {
		if (mBitmap != null) {

			int[] hist = nativefuntion.nativeHistgram(mBitmap);

			for (int i = 0; i < hist.length; i++) {
				System.out.println(hist[i]);
			}
			mimage.setImageBitmap(mBitmap);
		} else {
			mMakeTextToast("No Photo..", true);
		}

	}

	private void grayMat() {
		Mat srcimg = Highgui.imread(SrcPath, 0);
		Mat grayimg = new Mat();

		if (!srcimg.empty()) {
			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_GRAY2RGBA, 4);
			// �Ҷȱ仯
			saveMat(SavePath, grayimg);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void Prewitt_Bitmap() {
		if (mBitmap != null) {
			Bitmap mBitmap2 = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ALPHA_8);
			nativefuntion.PrewitteEdges(mBitmap1, mBitmap2);
			mimage.setImageBitmap(mBitmap2);
		} else {
			mMakeTextToast("No Photo..", true);
		}

	}

	private void TwoValue_Bitmap() {

		// TODO Auto-generated method stub
		if (mBitmap != null) {
			Bitmap mBitmap2 = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ALPHA_8);

			mState.setText("" + nativefuntion.OstuTwoValue(mBitmap1, mBitmap2));
			mimage.setImageBitmap(mBitmap2);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void Sobel_Bitmap() {
		// TODO Auto-generated method stub
		if (mBitmap != null) {
			Bitmap mBitmap2 = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ALPHA_8);
			nativefuntion.SobelEdges(mBitmap1, mBitmap2);
			mimage.setImageBitmap(mBitmap2);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void HistgramAverage_Bitmap() {
		// TODO Auto-generated method stub
		if (mBitmap != null) {
			Bitmap mBitmap2 = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), mBitmap1.getConfig());
			nativefuntion.nativeHistgramAverrage(mBitmap1, mBitmap2);
			mimage.setImageBitmap(mBitmap2);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void Sobel_Mat() {

	}

	private void Canny_Bitmap() {
		if (mBitmap != null) {

			Mat img = Highgui.imread(SrcPath, 0);
			Mat img3 = new Mat();

			Size size = new Size(9, 9);

			Imgproc.GaussianBlur(img, img3, size, 2, 2);
			
	//	Imgproc.bilateralFilter(src, dst, d, sigmaColor, sigmaSpace)
			
			//Imgproc.Sobel(img3, img3,5, 1, 0);
			Imgproc.Canny(img3, img3, CANNY_LOW, CANNY_LOW2);

			saveMat(SavePath2+"canny.jpg", img3);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	@SuppressLint("SdCardPath")
	private void Houngh_LineP() {
		if (mBitmap != null) {
			// HoughCircles();
			Mat srcimg = Highgui.imread(SrcPath, 1);
			// �xȡһ���DƬ�Ԅ��D�Q��ҶȵĈD��
			Mat date = new Mat();
			// �惦����Ĕ���
			Mat grayimg = new Mat();
			// �ҶȈD�惦���g

			long time = System.currentTimeMillis();

			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_RGB2GRAY, 0);
			// �Ҷȱ仯
			Size size = new Size(9, 9);

			//Imgproc.GaussianBlur(grayimg, grayimg, size, 2, 2);

			// Imgproc.threshold(grayimg, grayimg, 100, 200, 1);
			
			Imgproc.Sobel(grayimg, grayimg, 3, 1, 0);

			//Imgproc.Canny(grayimg, grayimg, CANNY_LOW, CANNY_LOW2);

			Imgproc.HoughLinesP(grayimg, date, 1, Math.PI / 180, 100);

			for (int x = 0; x < date.cols(); x++) {

				double[] rho = date.get(0, x);
				Point pt1 = new Point();
				Point pt2 = new Point();

				pt1.x = rho[0];
				pt1.y = rho[1];
				pt2.x = rho[2];
				pt2.y = rho[3];

				Core.line(srcimg, pt1, pt2, new Scalar(0, 0, 255), 2);

			}
			time = Math.abs(time - System.currentTimeMillis());
			Toast.makeText(getApplicationContext(), "" + time,
					Toast.LENGTH_LONG).show();

			saveMat(SavePath, srcimg);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	@SuppressLint("SdCardPath")
	private void Houngh_Line() {
		if (mBitmap != null) {
			// HoughCircles();
			Mat srcimg = Highgui.imread(SrcPath, 1);
			// �xȡһ���DƬ�Ԅ��D�Q��ҶȵĈD��
			Mat date = new Mat();
			// �惦����Ĕ���
			Mat grayimg = new Mat();
			// �ҶȈD�惦���g

			Size size = new Size(3, 3);
			// ��˹�V������С
			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_RGB2GRAY, 0);
			// �Ҷȱ仯
			Imgproc.GaussianBlur(grayimg, grayimg, size, 2, 2);
			// �M�и�˹�V��
			Imgproc.threshold(grayimg, grayimg, 100, 200, 1);

			Imgproc.Canny(grayimg, grayimg, CANNY_LOW, CANNY_LOW2);
			// ��Ե���

			Imgproc.HoughLines(grayimg, date, 1, Math.PI / 180, 100);
			// �A��׃��

			for (int x = 0; x < date.cols(); x++) {

				Point pt1 = new Point();
				Point pt2 = new Point();

				double[] data = date.get(0, x);
				double rho = data[0];
				double theta = data[1];
				double a = Math.cos(theta);
				double b = Math.sin(theta);
				double x0 = a * rho;
				double y0 = b * rho;
				pt1.x = Math.round(x0 + 1000 * (-b));
				pt1.y = Math.round(y0 + 1000 * a);
				pt2.x = Math.round(x0 - 1000 * (-b));
				pt2.y = Math.round(y0 - 1000 * a);
				Core.line(srcimg, pt1, pt2, new Scalar(0, 0, 255), 3);
			}

			saveMat(SavePath, srcimg);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void Houngh_Bitmap() {
		if (mBitmap != null) {
			// HoughCircles();
			long time = System.currentTimeMillis();

			Mat srcimg = Highgui.imread(SrcPath, 1);
			// �xȡһ���DƬ�Ԅ��D�Q��ҶȵĈD��
			Mat date = new Mat();
			// �惦����Ĕ���
			Mat grayimg = new Mat();
			// �ҶȈD�惦���g

			Size size = new Size(3, 3);
			// ��˹�V������С
			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_RGB2GRAY, 0);
			// �Ҷȱ仯
			Imgproc.GaussianBlur(grayimg, grayimg, size, 2, 2);
			// �M�и�˹�V��
			Imgproc.HoughCircles(grayimg, date, Imgproc.CV_HOUGH_GRADIENT, 2,
					300, 180, 200, 30, 300);

			for (int x = 0; x < date.cols(); x++) {
				double Circle[] = date.get(0, x);
				Point center = new Point(Math.round(Circle[0]),
						Math.round(Circle[1]));
				int radius = (int) Math.round(Circle[2]);

				Core.circle(srcimg, center, 2, new Scalar(0, 0, 255), 4);
				Core.circle(srcimg, center, radius, new Scalar(0, 0, 255), 4);
			}

			time = Math.abs(time - System.currentTimeMillis());

			Toast.makeText(getApplicationContext(), "" + time,
					Toast.LENGTH_LONG).show();

			saveMat(SavePath2+"hough.jpg", srcimg);
		} else {
			mMakeTextToast("No Photo..", true);
		}

	}

	private void SubMat() {
		if (mBitmap != null) {
			// HoughCircles();
			long time = System.currentTimeMillis();

			Mat srcimg = Highgui.imread(SrcPath, 1);
			// �xȡһ���DƬ�Ԅ��D�Q��ҶȵĈD��
			Mat date = new Mat();
			// �惦����Ĕ���
			Mat grayimg = new Mat();
			// �ҶȈD�惦���g

			Size size = new Size(3, 3);
			// ��˹�V������С
			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_RGB2GRAY, 0);
			// �Ҷȱ仯
			Imgproc.GaussianBlur(grayimg, grayimg, size, 2, 2);
			// �M�и�˹�V��
			Imgproc.HoughCircles(grayimg, date, Imgproc.CV_HOUGH_GRADIENT, 2,
					300, 180, 200, 30, 300);

			for (int x = 0; x < date.cols(); x++) {
				double Circle[] = date.get(0, x);
				Point center = new Point(Math.round(Circle[0]),
						Math.round(Circle[1]));
				int radius = (int) Math.round(Circle[2]);

				Mat submat = srcimg.submat((int) Circle[1] - radius,
						(int) Circle[1] + radius, (int) Circle[0] - radius,
						(int) (Circle[0] + radius));

				FeatureDetector fd = FeatureDetector
						.create(FeatureDetector.PYRAMID_FAST);

				List<KeyPoint> keypoints = new ArrayList<KeyPoint>();

				fd.detect(submat, keypoints);

				for (int i = 0; i < keypoints.size(); i++) {
					if (keypoints.get(i).response > 70) {

						int detectx = (int) (keypoints.get(i).pt.x + Circle[0] - radius);

						int detecty = (int) (keypoints.get(i).pt.y + Circle[1] - radius);

						Core.circle(srcimg, new Point(detectx, detecty), 10,
								new Scalar(0, 0, 255, 255));
					}

				}

				Core.circle(srcimg, center, 2, new Scalar(0, 0, 255), 4);
				Core.circle(srcimg, center, radius, new Scalar(0, 0, 255), 4);
			}

			time = Math.abs(time - System.currentTimeMillis());

			Toast.makeText(getApplicationContext(), "" + time,
					Toast.LENGTH_LONG).show();

			saveMat(SavePath, srcimg);
		} else {
			mMakeTextToast("No Photo..", true);
		}
	}

	private void GaussianBlur() {
		if (mBitmap != null) {

			Mat srcimg = Highgui.imread(SrcPath, 0);
			Mat grayimg = new Mat();

			// Mat grayimg=Highgui.imread(SrcPath, 0);
			Size size = new Size(9, 9);

			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_GRAY2RGBA, 4);
			// �Ҷȱ仯
			Imgproc.GaussianBlur(grayimg, grayimg, size, 2, 2);
			// ��˹�˲���

			saveMat(SavePath, grayimg);
		} else {
			mMakeTextToast("gaosi..", true);
		}

	}

	private void fastFeatureDetector() {
		if (mBitmap != null) {

			Mat srcimg = Highgui.imread(SrcPath, 1);
			Mat grayimg = new Mat();

			// Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_RGB2GRAY, 0);

			long time = System.currentTimeMillis();

			FeatureDetector fd = FeatureDetector
					.create(FeatureDetector.PYRAMID_FAST);

			List<KeyPoint> keypoints = new ArrayList<KeyPoint>();

			fd.detect(srcimg, keypoints);
			String str = "";

			for (int i = 0; i < keypoints.size(); i++) {
				if (keypoints.get(i).response > 70) {
					Core.circle(srcimg, new Point(keypoints.get(i).pt.x,
							keypoints.get(i).pt.y), 10, new Scalar(0, 0, 255,
							255));
					str += keypoints.get(i).response + ";";
				}

			}

			time = Math.abs(time - System.currentTimeMillis());

			Toast.makeText(getApplicationContext(), "" + time,
					Toast.LENGTH_LONG).show();

			Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG)
					.show();
			saveMat(SavePath, srcimg);
		} else {
			mMakeTextToast("gaosi..", true);
		}

	}

	public Mat OpenMat(String path) {

		Mat srcimg = Highgui.imread(SrcPath, 0);

		return srcimg;
	}

	public boolean saveMat(String path, Mat mat) {
		boolean flag = Highgui.imwrite(path, mat);
		if (flag) {
			File f = new File(path);
			if (f.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(path);

				Matrix matrix = new Matrix();

				matrix.setRotate(90);
				if (bm != null) {
					Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0,
							bm.getWidth(), bm.getHeight(), matrix, true);

					mimage.setImageBitmap(resizedBitmap);
				}
			}
		}// end if
		else {
			Toast.makeText(MainActivity.this,
					"===========ͼƬд��ʧ�ܣ�============����", 3).show();
			return false;
		}
		return true;
	}

	private void OpenCV_Ostu() {
		if (mBitmap != null) {

			Mat srcimg = Highgui.imread(SrcPath, 0);
			Mat grayimg = new Mat();

			Imgproc.cvtColor(srcimg, grayimg, Imgproc.COLOR_GRAY2RGBA, 4);
			// �Ҷȱ仯

			Bitmap bmp = Bitmap.createBitmap(grayimg.cols(), grayimg.rows(),
					Bitmap.Config.ALPHA_8);

			Bitmap bitmapOut = Bitmap.createBitmap(grayimg.cols(),
					grayimg.rows(), Bitmap.Config.ALPHA_8);

			if (Utils.matToBitmap(grayimg, bmp)) {

				int m = nativefuntion.OstuTwoValue(bmp, bitmapOut);

				Toast.makeText(this, "" + m, Toast.LENGTH_LONG).show();
			}

			Imgproc.threshold(grayimg, grayimg, 155, 255,
					Imgproc.THRESH_BINARY_INV);

			saveMat(SavePath, grayimg);
		} else {
			Toast.makeText(getApplicationContext(), "No photo",
					Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("ShowToast")
	public void mMakeTextToast(String str, boolean isLong) {
		if (isLong == true) {
			Toast.makeText(this, str, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
		}
	}

	public void Camera_Size(String str) {
		mText.setText(str);
	}

	public void Camera_View(Bitmap bmp) {

		// mimage.setImageBitmap(bmp);
		mBitmap = bmp;
		if (mBitmap != null) {
			mBitmap1 = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ALPHA_8);

			nativefuntion.renderPlasma(mBitmap, mBitmap1);
			Deal_Bitmap();
		}

	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Log.i(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Log.i(TAG, "Menu Item selected " + item);

		switch (item.getItemId()) {
		case R.id.itemcany:
			ModeState = MODE_STATE_CANNY;
			break;
		case R.id.itemgray:
			ModeState = MODE_STATE_GRAYOPENCV;
			break;
		case R.id.itemhoungh:
			ModeState = MODE_STATE_HOUNGH;
			break;
		case R.id.itemnativegray:
			ModeState = MODE_STATE_GRAY;
			break;
		case R.id.itemnativesobel:
			ModeState = MODE_STATE_SOBEL;
			break;
		case R.id.itemreast:
			ModeState = MODE_STATE_RGBA;
			break;
		case R.id.itemfinish:
			finish();
			break;
		case R.id.itemnativeprewitt:
			ModeState = MODE_STATE_PREWITT;
			break;
		case R.id.itemgaosi:
			ModeState = MODE_STATE_GAUSSIAN;
			break;
		case R.id.itemhounghline:
			ModeState = MODE_STATE_HOUNGHLINE;
			break;
		case R.id.itemhounghlinep:
			ModeState = MODE_STATE_HOUNGHLINEP;
			break;
		case R.id.itemnativeostu:
			ModeState = MODE_STATE_NATIVE_OSTU;
			break;
		case R.id.itemnativehistgram:
			ModeState = MODE_STATE_NATIVE_HISTGRAM;
			break;
		case R.id.itemnativehistgramaverage:
			ModeState = MODE_STATE_NATIVE_HISTGRAMEVERAGE;
			break;
		case R.id.itemostu:
			ModeState = MODE_STATE_OSTU;
			break;
		case R.id.itemdector:
			ModeState = MODE_STATE_DECTOR;
			break;
		case R.id.itemsubmat:
			ModeState = MODE_STATE_SUBMAT;
			break;
		default:
			break;
		}

		Deal_Bitmap();

		return true;
	}

}
