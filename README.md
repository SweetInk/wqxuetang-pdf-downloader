# wqxuetang-pdf-downloader

文泉学堂PDF下载器

本程序仅供学习交流使用,切勿用于商业活动

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

2. 搜索你想要下载的图书,然后打开书的主页,这里以https://lib-nuanxin.wqxuetang.com/#/Book/`2175744`为例，这里我们获取到了书的编号,`2175744`

3. 使用工具
```shell 
java -jar pdfd.jar <bookId> <workDir>
```
*参数说明*

* `bookId`  上述获取到的书籍编号
* `workDir` PDF保存的路径

eg:
```shell script
java -jar pdfd.jar 2175744 d:\\temp
```
这时候等个几十秒后你就会在`d:\\temp\\pdfTest\\`看到下载好的PDF文件.


