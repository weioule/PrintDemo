# PrinttextDemo
这个一个Android蓝牙打印小票demo，类似美团外卖小票打印


先看一下效果图哈：
&nbsp;&nbsp;![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img01.png)&nbsp;&nbsp;
![image](https://github.com/weioule/PrintDemo/blob/master/app/info/img01.png)

demo里主要是使用汉印打印机进行蓝牙小票打印，它还支持WiFi打印，USB打印和串口打印，SDK对接的话去汉印官网下载相应的zip包，里面有PDF文档和代码案例，文档上功能还是比较多的，比如与蓝牙进行关联以及各种状态获取以及各种属性设置等，项目里面我放了一份PDF的文档，就在info文件夹下。

但像打印的小票排版样式与细节这块相对是比较粗糙，直接在代码里写死的样式，而大多数平台的小票排版都不太一样，这里就涉及到自定义排版，而我这里主要讲的就是小票打印的排版样式了。

我在demo中已经把排版封装到 PrintUtil，主要的信息打印，商品名字、数量和小计都做了自适应兼容，多行等展示亦不会打乱排版，只管放心使用就好。

当然，个别特殊都样式设置就看着改，基本的样式我也封装了些方法，不够再加，如是接的sdk就根据文档给的功能增加，若是按原生的写法通过字节流传输给蓝牙打印机的，也可以将指令写入封装成方法进行添加设置。

demo里面还用到了lombok注解框架，主要是用于注解数据模型的get 和 set方法，这样就不需要写那么多凌乱的get() 和 set()了。



