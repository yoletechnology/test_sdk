#############################################
#
# 基础混淆配置
#
#############################################


####################基本混淆指令的设置####################

# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5

# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

# 指定不忽略非公共库的类
-dontskipnonpubliclibraryclasses

# 指定不忽略非公共库的类成员
-dontskipnonpubliclibraryclassmembers

# 记录日志，使我们的项目混淆后产生映射文件（类名->混淆后类名）
-verbose

# 忽略警告，避免打包时某些警告出现，没有这个的话，构建报错
-ignorewarnings

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify

# 不混淆Annotation(保留注解)
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

