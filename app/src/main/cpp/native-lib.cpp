#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_getRect(JNIEnv *env, jobject instance, jintArray intA_,
                                                    jint x, jint y) {
    jint *intA = env->GetIntArrayElements(intA_, NULL);

    Mat src(Size(360,360),CV_8UC4,(u_char *)intA);
    circle(src,Point(x,y),100,Scalar(0,255,0),5);
    rectangle(src,Point(x,y),Point(x+100,y+100),Scalar(255,255,0),3);

    env->ReleaseIntArrayElements(intA_, intA, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_nativeRgba(JNIEnv *env, jobject instance, jlong addr,
                                                       jintArray x_) {
    jint *x = env->GetIntArrayElements(x_, NULL);

    Mat &img = *(Mat*)addr;
    Mat dst;

    cvtColor(img, dst, COLOR_BGR2HSV);//转为HSV
    inRange(dst, Scalar(114, 43, 46), Scalar(130, 255, 255), dst);

    Mat element = getStructuringElement(MORPH_RECT, Size(3, 3));//

    morphologyEx(dst, dst, MORPH_OPEN, element);//开操作 (去除一些噪点)
    morphologyEx(dst, dst, MORPH_CLOSE, element);//闭操作 (连接一些连通域)

    vector<vector<Point> > contours;
    findContours(dst, contours, noArray(), RETR_LIST, 1);
    if(contours.size())
    {
        vector<RotatedRect> box(contours.size());
        int maxIndex=0;
        double max=0,averA=0;
        for ( int i = 0 ; i < contours.size(); i++)//计算每个轮廓最小外接矩形并求出平均area
        {
            box[i] = minAreaRect(Mat(contours[i]));  //计算每个轮廓最小外接矩形
            averA+= box[i].size.width*box[i].size.height;
            if(box[i].size.width*box[i].size.height>max)
            {
                max=box[i].size.width*box[i].size.height;
                maxIndex=i;
            }
        }
        averA/=box.size();
        for ( int i = 0 ; i < contours.size(); i++)//比较面积并画出外接矩
        {
            if(box[i].size.width*box[i].size.height>=averA)
            {
                Point2f vertices[4];
                box[i].points(vertices);
                for (int x = 0; x < 4; x++)
                    line(img, vertices[x], vertices[(x + 1) % 4], Scalar(0, 255, 0), 2);
            }

        }

        circle(img,(Point)box[maxIndex].center,sqrt(max)/3,Scalar(0,0,200),3);
        x[0]=(int)box[maxIndex].center.x;
        x[1]=(int)box[maxIndex].center.y;

    } else
    {
        x[0]=img.cols/2;
        x[1]=img.rows/2;
        circle(img,Point(432,240),15,Scalar(0,0,200),3);
    }



    drawContours(img, contours, -1, Scalar::all(255));
    line(img, Point(432,0), Point(432,480), Scalar(0, 255, 0), 1);
    line(img, Point(0,240), Point(864,240), Scalar(0, 255, 0), 1);




    env->ReleaseIntArrayElements(x_, x, 0);
}