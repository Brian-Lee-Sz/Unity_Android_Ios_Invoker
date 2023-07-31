# 为什么要以原生的方式来接入android sdk
 1、需求需要接入的sdk种类会有很多，其中难免会有依赖冲突等问题。使用原生的方式便于解决问题\
 2、现在越来越多公司开始中台化，unity作为业务方只需要提供母包，后续的sdk选择以及相应渠道包可以由中台统一操作\
 3、unity的sdk接入版本更新相比原生平台还是偏慢，有时还会涉及unity编辑器版本的限制
# 功能有哪些
 1、UnityInvoker:unity和android之间的相互调用传参\
 2、常用sdk接入：Facebook、googlePay、GoogleSignIn、Push、Ads等\
 3、选择模块build之后会自动将相应aar导入到unity-assets-Plugin-android目录
 # Grade依赖查看
 1、as默认布局右上角Grade选项\
 2、需要将File-》Settings-》Experimental->Do not build grade task list during Grade sync选择去除勾选\
 3、然后就可在右上角Grade视角查看到app-》Help-》Dependences，执行可以在console打印出相关的依赖信息
# 参考资料
 ##https://zhuanlan.zhihu.com/p/119052124
