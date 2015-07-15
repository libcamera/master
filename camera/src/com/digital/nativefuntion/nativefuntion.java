package com.digital.nativefuntion;

import java.util.List;

import org.opencv.features2d.KeyPoint;

import android.graphics.Bitmap;

public class nativefuntion {
	/**
	 * Sobel��Ե��⣬
	 * 
	 * @param bitmapIn
	 *            ����ͼ��
	 * @param bitmapOut
	 *            ���ͼ��
	 * @return ����1�ɹ�������С��0����ʧ��
	 */
	public native static int SobelEdges(Bitmap bitmapIn, Bitmap bitmapOut);

	/**
	 * Canny��Ե���
	 * 
	 * @param bitmapIn
	 *            ����ͼ��
	 * @param bitmapOut
	 *            ���ͼ��
	 * @return ����1�ɹ�������С��0����ʧ��
	 */

	public native static int CannyEdges(Bitmap bitmapIn, Bitmap bitmapOut);

	/**
	 * Prewitte��Ե���
	 * 
	 * @param bitmapIn
	 *            ����ͼ��
	 * @param bitmapOut
	 *            ���ͼ��
	 * @return ����1�ɹ�������С��0����ʧ��
	 */
	public native static int PrewitteEdges(Bitmap bitmapIn, Bitmap bitmapOut);

	/**
	 * Ostu��ֵ��
	 * 
	 * @param bitmapIn
	 *            ����ͼ��
	 * @param bitmapOut
	 *            ���ͼ��
	 * @return ����1�ɹ�������С��0����ʧ��
	 */
	public native static int OstuTwoValue(Bitmap bitmapIn, Bitmap bitmapOut);

	/**
	 * ��ɫת�Ҷ�ͼ
	 * 
	 * @param bitmapcolor
	 *            �����ɫͼƬ
	 * @param bitmapgray
	 *            ����Ҷ�ͼ��
	 * @return ����1�ɹ�������С��0����ʧ��
	 */
	public native static int renderPlasma(Bitmap bitmapcolor, Bitmap bitmapgray);

	/**
	 * ֱ��ͼ���
	 * 
	 * @param bitmapcolor
	 *            ����ͼ��
	 * @return ֱ��ͼ���
	 */
	public native static int[] nativeHistgram(Bitmap bitmapcolor);
	
	/**
	 * ֱ��ͼ��ֵ��
	 * @param bitmapIn ����ͼ��
	 * @param bitmapOut ���ͼ��
	 * @return -1 ��ͼ����ݣ�-2���Ǳ�׼��ʽ  -3 ���������ʽ���벻ͬ  1�ɹ�
	 */
	public native static int nativeHistgramAverrage(Bitmap bitmapIn,Bitmap bitmapOut);
	
	public native static boolean nativeDector(List<KeyPoint> keypoints);
	
	public native static void HoughCircles();

	static {
		try {
			System.loadLibrary("nativefuntion");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
