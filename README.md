# PrintDemo
这个一个Android蓝牙打印小票demo，类似美团外卖小票打印 和 新手指引蒙层提示demo


一 小票打印
<br>
先看一下效果图：

![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img03.JPG)&nbsp;&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img04.JPG)&nbsp;&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img05.JPG)

demo里主要是使用汉印打印机进行蓝牙小票打印，它还支持WiFi打印，USB打印和串口打印，SDK对接的话去汉印官网下载相应的zip包，里面有PDF文档和代码案例，文档上功能还是比较多的，比如与蓝牙进行关联以及各种状态获取以及各种属性设置等，项目里面我放了一份PDF的文档，就在info文件夹下。

但像打印的小票排版样式与细节这块相对是比较粗糙，直接在代码里写死的样式，而大多数平台的小票排版都不太一样，这里就涉及到自定义排版，而我这里主要讲的就是小票打印的排版样式了。

我在demo中已经把排版封装到 PrintUtil，主要的信息打印，商品名字、数量和小计都做了自适应兼容，多行等展示亦不会打乱排版，只管放心使用就好。

当然，个别特殊都样式设置就看着改，基本的样式我也封装了些方法，不够再加，如是接的sdk就根据文档给的功能增加，若是按原生的写法通过字节流传输给蓝牙打印机的，也可以将指令写入封装成方法进行添加设置。

demo里面还用到了lombok注解框架，主要是用于注解数据模型的get 和 set方法，这样就不需要写那么多凌乱的get() 和 set()了。

29/4.<br>
1 新增选择打印机功能<br>
2 新增爱印打印机支持<br>
3 优化打印排版格式 （这个排版格式是可以公用的，打印机基本都是可以打印byte数组）<br>

08/5.<br>
1 优化汉印与爱印打印机连接状态回显<br>
2 优化打印机连接成功后弹出提示<br>
3 新增复坤打印机支持<br>

2021/12/15.<br>
1 各打印机新增图片打印<br>
2 新增汉印打印机二维码打印<br>

<br> 
二  新手指引蒙层提示
<br>
效果图如下：

![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img001.png)&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img002.png)&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img003.png)&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img004.png)&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img005.png)&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img006.png)&nbsp;

demo里的蒙层提示，主要是将目标控件抠出来，提示指向的既是页面中真实展示的控件，这样蒙层消失后用户的焦点感觉就会如德芙般纵享丝滑。

接下来说一说用法，功能都在HintView这个类里，主要方法有：
<br>
1、setTargetView(); 将所要指向的目标view传进去，里面会自动对目标view大小与坐标进行测量，然后在蒙层上复制抠出一个完全透明的view，即可完全展示出底层的目标view 。
<br> 
2、setCustomGuideView(); 就是将所要提示的带有提示语和箭头的布局view传进，里面会根据所setDirction()设置的方向和setOffset() X轴、Y轴的偏移量将改布局进行排版展示。
<br> 
3、setMoreTransparentView(); 是传其他需要抠图的控件，需要展示出来的控件里面一并给抠出来
<br> 
4、setShape(); 设置抠图的形状，有圆形，矩形，椭圆形
<br> 
5、setOutsideShape(); 设置绘制目标控件的外围形状，一样可以绘制圆形，矩形，椭圆形
<br> 
6、setOutsideSpace(); 设置外围与目标控件的间隔
<br> 
7、setRadius(); 设置抠出目标控件的圆角，应与目标控件圆角一致
<br> 
8、setDotted(); 设置围围形状图的虚线实线
<br> 
9、setCancelable(); 设置是否点击屏幕消失
<br> 

项目里还使用了带阴影背景的LCardView 和 自定义圆角图片控件RoundedImageView

在使用时需要注意：当目标控件完全展示出来后再调用展示提示的方法，因为里面需要使用目标控件的信息，目标控件要展示后才能获取到宽高、坐标等信息。

蒙层这里我没有做屏幕适配，具体的屏幕适配就需要自个去实现了，因为每个产品和设计的想法都不同。


