#include <jni.h>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <android/log.h>


using namespace std;
using namespace cv;

Mat * mOtsu = NULL;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_opixi_urpproject2_Preview_Hough(JNIEnv *env, jobject thiz, jint width,
                                                 jint height, jbyteArray NV21FrameData,
                                                 jintArray outPixels) {
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    jint * poutPixels = env->GetIntArrayElements(outPixels, 0);


    Mat mGray(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
    Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);

    cvtColor(mGray, mResult, CV_GRAY2BGRA);

    vector<Vec3f> circles;
    HoughCircles(mGray,circles,CV_HOUGH_GRADIENT,1,100,200,25,30,50);


    //findContours(OtsuImg,contours,hierarchy,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);



    for(size_t i=0;i<circles.size();i++){
        Point center(cvRound(circles[i][0]),cvRound(circles[i][1]));
        int radius = cvRound(circles[i][2]);
        circle(mResult,center,3,CV_RGB(255,0,0),-1,8,0);
        circle(mResult,center,radius,CV_RGB(255,0,0),3,8,0);
        //drawContours(mResult,contours,i,Scalar(0,0,255),thickness,8,hierarchy);
    }
    /// thresh = 0 is ignored





    env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
    env->ReleaseIntArrayElements(outPixels, poutPixels, 0);

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_opixi_urpproject2_Preview_ImageProcessing(JNIEnv *env, jobject thiz,
                                                           jint width, jint height,
                                                           jbyteArray NV21FrameData,
                                                           jintArray outPixels) {
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

    int centerX=0;
    int radius=0;
    if ( mOtsu == NULL )
    {
        mOtsu = new Mat(height, width, CV_8UC1);
    }

    // Mat image(height,width,CV_8UC4,(unsigned char *)pNV21FrameData);
    Mat mGray(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
    Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);
    Mat OtsuImg = *mOtsu;
    int thresh = 0;

    //adaptiveThreshold(mGray,OtsuImg,255,CV_ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY,55,5);
    adaptiveThreshold(mGray,OtsuImg,255,CV_ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY,75,5);  // 3번째는 최대값  0과 255 사이의 값이됨. 영상 이진화


    //threshold( mGray, OtsuImg, thresh, 255, THRESH_BINARY | THRESH_OTSU );                    // THRESH_BINARY 는 threshold 계산할때 주변 픽셀 사용하는 크기 3,5,7 식으로 홀수로 넣어줌

    cvtColor(OtsuImg, mResult, CV_GRAY2BGRA);  // 동그라미 색상 변환
    vector<Vec3f> circles;
    HoughCircles(mGray,circles,CV_HOUGH_GRADIENT,2,200,500,40,120,130);  // 숫자 2 누적기 해상도, 200 두원간의 최소 거리, 200 캐니 최대 경계값, 25 투표 최소 개수, 60,70 최소와 최대 반지름
    // mGray, circles 원의 중심점.

    for(size_t i=0;i<circles.size();i++){
        Point center(cvRound(circles[i][0]),cvRound(circles[i][1]));  //cvRound 정수형으로 변환할때 반올림
        centerX = cvRound(circles[i][0]);
        radius = cvRound(circles[i][2]);


        circle(mResult,center,3,Scalar(255,0,0,255),-1,8,0);   //원 센터
        //__android_log_print(ANDROID_LOG_DEBUG,"CHK","Center valus is %d",center.x);
        //__android_log_print(ANDROID_LOG_DEBUG,"CHK","radius is %d",radius);
        circle(mResult,center,radius,Scalar(255,0,0,255),3,8,0);  // 원 아웃라인
        // circle(mResult, center, 10, Scalar(255,0,0,255));

    }
    env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
    env->ReleaseIntArrayElements(outPixels, poutPixels, 0);

    return centerX+radius/2;
   // return centerX+radius+radius/2;     //LED중심과 반지름.    // 한 선을 정해서 그 선의 값을 읽는다.
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_opixi_urpproject2_Preview_Blur(JNIEnv *env, jobject thiz, jint width,
                                                jint height, jbyteArray NV21FrameData,
                                                jintArray outPixels) {
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    jint * poutPixels = env->GetIntArrayElements(outPixels, 0);


    Mat mblur(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
    Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);

    blur(mblur,mblur,Size(2,2));
    Ptr<CLAHE> clahe = createCLAHE();
    clahe->setClipLimit(2);
    clahe->apply(mblur,mblur);
    cvtColor(mblur, mResult, CV_GRAY2BGRA);
    env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
    env->ReleaseIntArrayElements(outPixels, poutPixels, 0);
    return true;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_opixi_urpproject2_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
