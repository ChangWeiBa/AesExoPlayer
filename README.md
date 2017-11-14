## ExoPlayer利用自定义DataSource实现直接播放AES加密音频

开局一张图

![dataSource.png](/blob/master/dataSource.png "dataSource.png")

[ExoPlayer源码浅析](http://www.jianshu.com/p/4dede867739d "ExoPlayer源码浅析")

[ExoPlayer官方文档](http://www.jianshu.com/p/4dede867739d "ExoPlayer 官方文档")

[ExoPlayer GitHub](https://github.com/google/ExoPlayer "ExoPlayer Github")


## 需求与适用范围
首先本文的适用范围是使用ExoPlayer框架时，直接解密播放已经经过AES加密过（或者类似需求）的音频或者视频，是利用官方demo内DefaultDataSourceFactory与DefaultDataSource改造而来。有需求就可以继续往下看了

##### 0.故事的开始
故事的开始还得从新需求开始说起。公司新开了一个旅游项目，其中主要功能就是播放在线或者本地音频，这个非常非常平常的需求，让我开始EXO之旅。

为什么不使用最平常简单的MediaPlayer,因为程序员喜新厌旧哇 嘻嘻。

其中我们最主要的需求就是，下载下来的本地音频需要加密存放在本地，在播放的时候进行解密播放，以保证数据的安全性，在这里选用了常见的AES加解密。

相信这些需求应该的比较常见的类型了，在选用Exoplayer之后，在线播放什么的肯定不用花费过多时间了，但在进行本地播放的时候碰到了问题。

当初在查询ExoPlayer文档的时候是知道和看中了它的自定义资源播放功能的，官方文档和各种源码解析都说它有强大的自定义功能，但是等到我真正要实现我功能的时候，我感觉ExoPlayer是一个孤儿...

为什么在GitHub上8000+星星的ExoPlayer在百度上的资料这么少（想抄都没地方抄）。。 百度Google了一圈下来，折腾完ExtractorsFactory，折腾Mp3Extractor，完全摸错了门。后来在[stackOverflow](https://stackoverflow.com/questions/37658411/ecb-encryption-with-exoplayer "stackoverflow") 看到终于一篇关于自定义dataSource的提问，至此找到问题解决入口。


## 思路与使用方式

关于ExoPlayer的各种源码、使用解析在这就不赘叙了。正常的情况下，使用ExoPlayer默认的DefaultDataSource是完成不了直接播放AES加密音频的。

最开始，要实现播放，只能先把AES音频解密成正常的MP3音频，这样，就完全打破了最初我们要把本地下载下来的音频加密存放与播放的预想，解密过后的音频总会在文件夹中显示出来，并以正常的MP3存在，这样音频就毫无安全性可言了，也就是说没有完成需求。

为解决能直接播放AES加密音频的问题，我们采用的是自定义改写DataSourceFactory类，改写DataSource类，复用官方demo中Aes128DataSource类来完成的。

只需要改写这一系列DataSource类文件，就可以完成我们直接播放AES音频文件的需求，下面逐个进行解析。

通过前期查看和研究ExoPlayer源码我们观察到，这一系列的资源提供、分拆与解析过程在ExtractorMediaSource下的ExtractorMediaPeriod类有很清晰的体现。

![DefaultExtractorInput.png](/blob/master/DefaultExtractorInput.png "DefaultExtractorInput.png")

而我们需要实现的需求中，本质上只是把MP3文件加密了一次而已，它在播放的时候，最终只需要在拆解提供资源的时候把加密的文件流解密成正常的MP3流，给ExtractorMediaSource提供正常的MP3流即可，所以并不需要再过多的进行其他复杂操作就可以完成此需求。

那么我实现的就是重写DefaultDataSourceFactory资源提供工厂类，改造DefaultDataSource，根据文件类型判断，加密的音频使用Aes128DataSource类拆解进行解密，未加密或者在线的URL继续使用getFileDataSource或者getContentDataSource原本默认的实现进行拆解。最后只需要用我们改写的factory替换默认的DataSourceFactory就可以完成我们的需求了。

![TestPlayerActivity.png](/blob/master/TestPlayerActivity.png "TestPlayerActivity.png")

###### 1.AitripDataSourceFactory
AitripDataSourceFactory是重写的DataSourceFactory工厂类，直接copyDefaultDataSource而来。主要是重写了createDataSource()方法，用于进入资源选择类AitripDataSource

```java
    @Override
    public AitripDataSource createDataSource() {
        return new AitripDataSource(context, listener, baseDataSourceFactory.createDataSource());
    }
```

###### 2.AitripDataSource
改写DataSource内open方法，用于扩展数据源选择方案，以让其正确选择到我们的解密Aes128DataSource类，注释基本可以解释一切。因为我在本地加密的文件是在文件名后又添加了.aitrip字段，所以只需根据文件名判断是否包含.aitrip，选择正确的DataSource解析即可。

```java
    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Assertions.checkState(dataSource == null);
        // Choose the correct source for the scheme.
        //选择正确的数据源方案
        String scheme = dataSpec.uri.getScheme();
        //如果URI是一个本地文件路径或本地文件的引用。
        Timber.e("解密：000000," + scheme + ",path:" + dataSpec.uri.getPath());
        if (Util.isLocalFileUri(dataSpec.uri)) {
            //如果路径尾包含aitrip的文件名，使用解密类
            if (dataSpec.uri.getPath().endsWith(".aitrip")) {
                Aes128DataSource aes128DataSource =
                        new Aes128DataSource(getFileDataSource(), Aikey.getBytes(), Aikey.getBytes());
                dataSource = aes128DataSource;
            } else {//否则，正常解析mp3
                if (dataSpec.uri.getPath().startsWith("/android_asset/")) {
                    dataSource = getAssetDataSource();
                } else {
                    dataSource = getFileDataSource();
                }
            }
        } else if (SCHEME_ASSET.equals(scheme)) {
            dataSource = getAssetDataSource();
        } else if (SCHEME_CONTENT.equals(scheme)) {
            dataSource = getContentDataSource();
        } else if (SCHEME_RTMP.equals(scheme)) {
            dataSource = getRtmpDataSource();
        } else {
            dataSource = baseDataSource;
        }
        // Open the source and return.
        return dataSource.open(dataSpec);
    }
 ```
###### 3.Aes128DataSource
直接copy自官方demo的AesDataSource，其本用于HLS文件播放列表解密的，但是很巧的是采用了解密方式，对我也同样适用，所以并不需要改动任何一行代码。。
它主要是实现open后，利用Aes解密方式解密文件流提供给上层使用。

```java
  @Override
  public long open(DataSpec dataSpec) throws IOException {
    Cipher cipher;
    try {
      cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }

    Key cipherKey = new SecretKeySpec(encryptionKey, "AES");
    AlgorithmParameterSpec cipherIV = new IvParameterSpec(encryptionIv);

    try {
      cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherIV);
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }

    cipherInputStream = new CipherInputStream(
        new DataSourceInputStream(upstream, dataSpec), cipher);

    return C.LENGTH_UNSET;
  }

```

就这样。。 没了。   完成之后再看回来，这么简单。。。 全是copy。。 说个毛啊。。。。  然而，对于刚接触ExoPlayer，想要实现类似功能，而网上没啥参考的小伙伴来说，应该是有点用的，特此记录。。。。



## 结束

有帮助就点星星哟

下载源码慢慢看喔

有什么疑问或者建议欢迎issues留言噢


[ExoPlayerTest小demo的Github地址](https://github.com/ChangWeiBa/AesExoPlayer "ExoPlayerTest小demo的Github地址")












