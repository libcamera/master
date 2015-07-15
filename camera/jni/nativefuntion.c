#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <math.h>
#include <malloc.h>
#include <string.h>

#define  LOG_TAG    "libibmphotophun"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct {
	uint8_t red;
	uint8_t green;
	uint8_t blue;
	uint8_t alpha;
} argb;
//ARGB转换结构体

int display[256 * 3];
//直方图统计后的存储位置

static void Complare_8BitHist(AndroidBitmapInfo* info, uint8_t* pixels);
//直方图统计函数
static void Complare_24BitHist(AndroidBitmapInfo* info, argb* pixels);
//24bit直方图统计
static void HistgramAverrage24Bit(AndroidBitmapInfo* info, argb* inpixels,
		argb* outpixels);
//24Bit直方图均值化
static void HistgramAverrage8Bit(AndroidBitmapInfo* info, uint8_t* inpixels,
		uint8_t* outpixels);
//8Bit直方图均值化

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_HoughCircles(
		JNIEnv* env, jobject obj) {

}

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_renderPlasma(
		JNIEnv* env, jobject obj, jobject bitmapcolor, jobject bitmapgray) {
	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	void* pixelsobel;
	int ret;
	int y;
	int x;

	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 !");
		return -1;
	}

	if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	// modify pixels with image processing algorithm

	for (y = 0; y < infocolor.height; y++) {
		argb * line = (argb *) pixelscolor;
		uint8_t * grayline = (uint8_t *) pixelsgray;
		for (x = 0; x < infocolor.width; x++) {
			grayline[x] = 255
					- ((30 * line[x].red + 59 * line[x].green
							+ 11 * line[x].blue) / 100);
		}

		pixelscolor = (char *) pixelscolor + infocolor.stride;
		pixelsgray = (char *) pixelsgray + infogray.stride;

	}

	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);

	return 1;
}

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_SobelEdges(
		JNIEnv * env, jobject obj, jobject bitmapgray, jobject bitmapedges) {
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	AndroidBitmapInfo infoedges;
	void* pixelsedge;
	int ret;
	int y;
	int x;
	int sumX, sumY, sum;
	int i, j;
	int Gx[3][3];
	int Gy[3][3];
	uint8_t *graydata;
	uint8_t *edgedata;

	LOGI("findEdges running");

	Gx[0][0] = -1;
	Gx[0][1] = 0;
	Gx[0][2] = 1;
	Gx[1][0] = -2;
	Gx[1][1] = 0;
	Gx[1][2] = 2;
	Gx[2][0] = -1;
	Gx[2][1] = 0;
	Gx[2][2] = 1;

	Gy[0][0] = 1;
	Gy[0][1] = 2;
	Gy[0][2] = 1;
	Gy[1][0] = 0;
	Gy[1][1] = 0;
	Gy[1][2] = 0;
	Gy[2][0] = -1;
	Gy[2][1] = -2;
	Gy[2][2] = -1;

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapedges, &infoedges)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	LOGI(
			"gray image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infogray.width, infogray.height, infogray.stride, infogray.format, infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	LOGI(
			"color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infoedges.width, infoedges.height, infoedges.stride, infoedges.format, infoedges.flags);
	if (infoedges.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapedges, &pixelsedge)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	// modify pixels with image processing algorithm

	LOGI("time to modify pixels....");

	graydata = (uint8_t *) pixelsgray;
	edgedata = (uint8_t *) pixelsedge;

	for (y = 0; y <= infogray.height - 1; y++) {
		for (x = 0; x < infogray.width - 1; x++) {
			sumX = 0;
			sumY = 0;
			// check boundaries
			if (y == 0 || y == infogray.height - 1) {
				sum = 0;
			} else if (x == 0 || x == infogray.width - 1) {
				sum = 0;
			} else {
				// calc X gradient
				for (i = -1; i <= 1; i++) {
					for (j = -1; j <= 1; j++) {
						sumX +=
								(int) ((*(graydata + x + i
										+ (y + j) * infogray.stride))
										* Gx[i + 1][j + 1]);
					}
				}

				// calc Y gradient
				for (i = -1; i <= 1; i++) {
					for (j = -1; j <= 1; j++) {
						sumY +=
								(int) ((*(graydata + x + i
										+ (y + j) * infogray.stride))
										* Gy[i + 1][j + 1]);
					}
				}

				sum = 255 - abs(sumX) - abs(sumY);

			}

			if (sum > 255)
				sum = 255;
			if (sum < 0)
				sum = 0;

			*(edgedata + x + y * infogray.width) = 255 - (uint8_t) sum;
		}
	}

	AndroidBitmap_unlockPixels(env, bitmapgray);
	AndroidBitmap_unlockPixels(env, bitmapedges);

	return 1;
}

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_CannyEdges(
		JNIEnv * env, jobject obj, jobject bitmapgray, jobject bitmapedges) {
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	AndroidBitmapInfo infoedges;
	void* pixelsedge;
	int ret;
	int y;
	int x;
	int sumX, sumY, sum;
	int i, j;
	uint8_t *graydata;
	uint8_t *edgedata;

	LOGI("findEdges running");

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapedges, &infoedges)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	LOGI(
			"gray image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infogray.width, infogray.height, infogray.stride, infogray.format, infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	LOGI(
			"color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infoedges.width, infoedges.height, infoedges.stride, infoedges.format, infoedges.flags);
	if (infoedges.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapedges, &pixelsedge)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -1;
	}

	// modify pixels with image processing algorithm

	LOGI("time to modify pixels....");

	graydata = (uint8_t *) pixelsgray;
	edgedata = (uint8_t *) pixelsedge;

	AndroidBitmap_unlockPixels(env, bitmapgray);
	AndroidBitmap_unlockPixels(env, bitmapedges);

	return 1;
}

JNIEXPORT jintArray JNICALL Java_com_digital_nativefuntion_nativefuntion_nativeHistgram(
		JNIEnv * env, jobject obj, jobject bitmapsrc) {
	AndroidBitmapInfo infosrc;
	void* pixelssrc;
	int ret;
	int i, j;
	uint8_t *graydata;
	argb* colordata;

	jintArray retArray;
	jint temp[256 * 3];

	LOGI("findEdges running");

	if ((ret = AndroidBitmap_getInfo(env, bitmapsrc, &infosrc)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return NULL;
	}

	if (infosrc.format != ANDROID_BITMAP_FORMAT_A_8) {
		if (infosrc.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
			LOGE("Bitmap format is not A_8 !");
			LOGE(" "+ANDROID_BITMAP_FORMAT_RGBA_8888);
			return NULL;
		}
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapsrc, &pixelssrc)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if (infosrc.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
		colordata = (argb*) pixelssrc;
		Complare_24BitHist(&infosrc, colordata);
		retArray = (*env)->NewIntArray(env, 256 * 3);

		for (i = 0; i < 256 * 3; i++) {
			temp[i] = display[i];
		}

		(*env)->SetIntArrayRegion(env, retArray, 0, 256 * 3, temp);

	} else {
		graydata = (uint8_t *) pixelssrc;
		Complare_8BitHist(&infosrc, graydata);
		retArray = (*env)->NewIntArray(env, 256);
		for (i = 0; i < 256; i++) {
			temp[i] = display[i];
		}
		(*env)->SetIntArrayRegion(env, retArray, 0, 256, temp);
	}

	AndroidBitmap_unlockPixels(env, bitmapsrc);

	LOGE("Bitmap format is complete!");

	return retArray;
}

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_nativeHistgramAverrage(
		JNIEnv * env, jobject obj, jobject bitmapsrc, jobject bitmapout) {
	AndroidBitmapInfo infosrc;
	AndroidBitmapInfo infoout;
	void* pixelssrc;
	void* pixelsout;
	int ret;
	int i, j;
	uint8_t *graydata;
	argb* colordata;
	uint8_t *graydataout;
	argb* colordataout;

	LOGI("findEdges running");

	if ((ret = AndroidBitmap_getInfo(env, bitmapsrc, &infosrc)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if (infosrc.format != ANDROID_BITMAP_FORMAT_A_8) {
		if (infosrc.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
			LOGE("Bitmap format is not A_8 or RGBA_8888!");
			return -2;
		}
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapsrc, &pixelssrc)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -2;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapout, &infoout)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if (infosrc.format != ANDROID_BITMAP_FORMAT_A_8) {
		if (infosrc.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
			LOGE("Bitmap format is not A_8 or RGBA_8888!");
			return -1;
		}
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapout, &pixelsout)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -3;
	}

	if (!(infosrc.format == infoout.format && infosrc.width == infoout.width
			&& infosrc.height == infoout.height)) {
		LOGE("input bitmap diffence with Output Bitmap");
		return -1;
	}
	if (infosrc.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
		colordata = (argb*) pixelssrc;
		colordataout = (argb*) pixelsout;
		Complare_24BitHist(&infosrc, colordata);
		HistgramAverrage24Bit(&infosrc, colordata, colordataout);
	} else {
		graydata = (uint8_t *) pixelssrc;
		graydataout = (uint8_t *) pixelsout;
		Complare_8BitHist(&infosrc, graydata);
		HistgramAverrage8Bit(&infosrc, graydata, graydataout);
	}

	AndroidBitmap_unlockPixels(env, bitmapsrc);
	AndroidBitmap_unlockPixels(env, bitmapout);

	LOGI("Bitmap format is complete!");

	return 1;
}

JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_PrewitteEdges(
		JNIEnv * env, jobject obj, jobject bitmapgray, jobject bitmapedges) {
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	AndroidBitmapInfo infoedges;
	void* pixelsedge;
	int ret;
	int y;
	int x;
	int sumX, sumY, sum;
	int i, j;
	int Gx[3][3];
	int Gy[3][3];
	uint8_t *graydata;
	uint8_t *edgedata;

	LOGI("findEdges running");

	Gx[0][0] = -1;
	Gx[0][1] = 0;
	Gx[0][2] = 1;
	Gx[1][0] = -1;
	Gx[1][1] = 0;
	Gx[1][2] = 1;
	Gx[2][0] = -1;
	Gx[2][1] = 0;
	Gx[2][2] = 1;

	Gy[0][0] = 1;
	Gy[0][1] = 1;
	Gy[0][2] = 1;
	Gy[1][0] = 0;
	Gy[1][1] = 0;
	Gy[1][2] = 0;
	Gy[2][0] = -1;
	Gy[2][1] = -1;
	Gy[2][2] = -1;

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapedges, &infoedges)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	LOGI(
			"gray image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infogray.width, infogray.height, infogray.stride, infogray.format, infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	LOGI(
			"color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infoedges.width, infoedges.height, infoedges.stride, infoedges.format, infoedges.flags);
	if (infoedges.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapedges, &pixelsedge)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	// modify pixels with image processing algorithm

	LOGI("time to modify pixels....");

	graydata = (uint8_t *) pixelsgray;
	edgedata = (uint8_t *) pixelsedge;

	for (y = 0; y <= infogray.height - 1; y++) {
		for (x = 0; x < infogray.width - 1; x++) {
			sumX = 0;
			sumY = 0;
			// check boundaries
			if (y == 0 || y == infogray.height - 1) {
				sum = 0;
			} else if (x == 0 || x == infogray.width - 1) {
				sum = 0;
			} else {
				// calc X gradient
				for (i = -1; i <= 1; i++) {
					for (j = -1; j <= 1; j++) {
						sumX +=
								(int) ((*(graydata + x + i
										+ (y + j) * infogray.stride))
										* Gx[i + 1][j + 1]);
					}
				}

				// calc Y gradient
				for (i = -1; i <= 1; i++) {
					for (j = -1; j <= 1; j++) {
						sumY +=
								(int) ((*(graydata + x + i
										+ (y + j) * infogray.stride))
										* Gy[i + 1][j + 1]);
					}
				}

				sum = 255 - abs(sumX) - abs(sumY);

			}

			if (sum > 255)
				sum = 255;
			if (sum < 0)
				sum = 0;

			*(edgedata + x + y * infogray.width) = 255 - (uint8_t) sum;
		}
	}

	AndroidBitmap_unlockPixels(env, bitmapgray);
	AndroidBitmap_unlockPixels(env, bitmapedges);

	return 1;
}

/**
 * 大律法二值化
 */
JNIEXPORT jint JNICALL Java_com_digital_nativefuntion_nativefuntion_OstuTwoValue(
		JNIEnv * env, jobject obj, jobject bitmapgray, jobject bitmapnew) {
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	AndroidBitmapInfo infonew;
	void* pixelsnew;
	int ret;
	int y;
	int x;
	int i, j;
	float Average[256];
	float x1 = 0, y1 = 0, P1[256], P2[256];
	int m;
	uint8_t *graydata;
	uint8_t *newdata;

	LOGI("findEdges running");

	memset(P1, 0, sizeof(float) * 256);
	memset(P2, 0, sizeof(float) * 256);

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapnew, &infonew)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -1;
	}

	LOGI(
			"gray image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infogray.width, infogray.height, infogray.stride, infogray.format, infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	LOGI(
			"color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d", infonew.width, infonew.height, infonew.stride, infonew.format, infonew.flags);
	if (infonew.format != ANDROID_BITMAP_FORMAT_A_8) {
		LOGE("Bitmap format is not A_8 !");
		return -1;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapnew, &pixelsnew)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

// modify pixels with image processing algorithm

	LOGI("time to modify pixels....");

	graydata = (uint8_t *) pixelsgray;
	newdata = (uint8_t *) pixelsnew;

	Complare_8BitHist(&infogray, pixelsgray);

	memset(P1, 0, sizeof(float) * 256);
	memset(P2, 0, sizeof(float) * 256);

	for (i = 0; i < 256; i++) {
		Average[i] = (float) display[i] / (infogray.height * infogray.width);
	}

	for (i = 0; i < 256; i++) {
		for (j = 0; j < i; j++) {
			P1[i] += Average[j];
			P2[i] += (Average[j] * j);
		}
		x1 += Average[i] * i;
	}

	for (i = 0; i < 255; i++) {
		float temp;
		float temp1;

		if (P1[i] * (1 - P1[i]) != 0)
			temp = (x1 * P1[i] - P2[i]) * (x1 * P1[i] - P2[i])
					/ (P1[i] * (1 - P1[i]));

		if (temp > y1) {
			y1 = temp;
			m = i;
		}
	}

	for (y = 1; y < infogray.height - 1; y++) {
		for (x = 1; x < infogray.width - 1; x++) {
			if ((int) (*(graydata + x + y * infogray.stride)) > m) {
				(*(newdata + x + y * infogray.stride)) = 255;
			} else {
				(*(newdata + x + y * infogray.stride)) = 0;
			}
		}
	}

	AndroidBitmap_unlockPixels(env, bitmapgray);
	AndroidBitmap_unlockPixels(env, bitmapnew);

	return m;
}
/**
 * 直方图统计
 */
static void Complare_8BitHist(AndroidBitmapInfo* info, uint8_t* pixels) {
	int width = info->width;
	int height = info->height;
	int i, j;

	memset(display, 0, sizeof(int) * 256 * 3);

	for (i = 1; i < height - 1; i++) {
		for (j = 1; j < width - 1; j++) {
			display[255 - (int) (*(pixels + i + j * info->stride))]++;
		}
	}
}

static void Complare_24BitHist(AndroidBitmapInfo* info, argb* pixels) {
	int width = info->width;
	int height = info->height;
	int i, j;

	memset(display, 0, sizeof(int) * 256 * 3);

	for (i = 1; i < height - 1; i++) {
		argb * line = (argb *) pixels;

		for (j = 1; j < width - 1; j++) {
			display[line[j].red]++;
			display[256 + line[j].green]++;
			display[512 - line[j].blue]++;

			//	LOGI(
			//		"Bitmap R=%d,G=%d,B=%d,A=%d", line[j].red, line[j].green, line[j].blue, line->alpha);

		}
		pixels = (uint8_t *) pixels + info->stride;
	}
}

static void HistgramAverrage8Bit(AndroidBitmapInfo* info, uint8_t* inpixels,
		uint8_t* outpixels) {
	int i, j;
	float Average[256];

	int BmpWidth = info->width;
	int BmpHeight = info->height;

	for (i = 0; i < 256; i++) {
		Average[i] = (float) display[i] / (BmpWidth * BmpHeight);
	}

	for (i = 0; i < BmpWidth * BmpHeight; i++) {
		for (j = 0; j < inpixels[i]; j++) {
			outpixels[i] = (int) (255 * Average[j] + 0.5) + outpixels[i];
		}
	}
}

static void HistgramAverrage24Bit(AndroidBitmapInfo* info, argb* inpixels,
		argb* outpixels) {
	int i, j;
	float Average[256 * 3];

	int BmpWidth = info->width;
	int BmpHeight = info->height;

	for (i = 0; i < 256 * 3; i++) {
		Average[i] = (float) display[i] / (BmpWidth * BmpHeight);
	}

	for (i = 0; i < BmpWidth * BmpHeight; i++) {
		for (j = 0; j < inpixels[i].red; j++) {
			outpixels[i].red = (int) (255 * Average[j] + 0.5)
					+ outpixels[i].red;

		}
		for (j = 0; j < inpixels[i].green; j++) {
			outpixels[i].green = (int) (255 * Average[256 + j] + 0.5)
					+ outpixels[i].green;

		}
		for (j = 0; j < inpixels[i].blue; j++) {
			outpixels[i].blue = (int) (255 * Average[512 + j] + 0.5)
					+ outpixels[i].blue;

		}
	}
}
