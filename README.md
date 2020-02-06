# wqxuetang-pdf-downloader

文泉学堂PDF下载器

## 本程序仅供学习交流使用,切勿用于商业活动
## 本程序仅供学习交流使用,切勿用于商业活动
## 本程序仅供学习交流使用,切勿用于商业活动
## 下载后的图书PDF请在24小时内删除，切勿传播，如果您觉得图书不错，请购买正版支持！！！

# 构建
## 所需环境

* `JDK 1.8+`
* `maven 3.2+`

拉取代码后，进入项目根目录，执行如下命令

```shell 
mvn package
```
打包成功后，输出的文件会在项目根目录下的`target\pdfa.jar`

# 说明

* 本程序最多在`2020-02-16`号前有效，详情见[用知识战“疫”！无需注册登录，清华社开放的免费知识库快来了解一下！](https://mp.weixin.qq.com/s/rALGeUDptg7iCUhSBXLCaw)

* Jwt private key逆向自[js文件](https://lib-nuanxin.wqxuetang.com/static/read/js/read.v5.3.1.722eb.js)

* 项目中使用的工具类来自[Hutool](https://hutool.cn/)

* PDF生成使用 [iText7](https://itextpdf.com/)


# 如何使用

1. 打开文泉学堂免费阅读[主页](https://lib-nuanxin.wqxuetang.com/#/)

2. 登录网站

3. 搜索你想要下载的图书,然后打开书的主页,这里以*https://lib-nuanxin.wqxuetang.com/#/Book/`2175744`* 为例，我们获取到了书的编号:`2175744`

4. Cookie获取

打开浏览器调试工具，进行如下截图步骤

![操作步骤.png](http://ww1.sinaimg.cn/large/005ViNx8gy1gbn4zpkd7uj315u0k4q66.jpg)

把截图中`Cookie:` 后面的内容全部`复制`下来,然后`粘贴`到`config.properties`文件中的`config.cookie`配置项后面

5. 使用工具

```shell 
java -jar pdfd.jar -b <arg> -c <arg>

```
**参数说明**

* `b`  上述获取到的书籍编号
* `c`  该选项不指定时，程序会默认在pdf.jar所在目录读取`config.properties`文件

**配置文件样例**

```properties
#线程数,目前最好配置为1，请求过快时，服务器会限流，将会导致无法请求
config.threadCount=1
#登录文泉学堂后，cookie值
config.cookie=123456
#工作路径，用于存储临时图片、pdf文件
config.workPath=d:\\temp
# 连续请求最大阈值数，程序将停顿后再执行
config.maxRequestThreshold=45
#超过一定请求后，程序停顿多少秒继续执行(单位秒)
config.waitingSeconds=60
#默认休眠时间(单位毫秒）
config.defaultSleepTime=1000
```
*eg:*
```shell script
java -jar pdfd.jar -b 2175744
```

这时候等个几十秒后你就会在`d:\\temp\\pdfTest\\`看到下载好的PDF文件.


