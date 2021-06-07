观摩了Google官方的DataBinding项目[architecture-samples](https://github.com/android/architecture-samples/tree/todo-mvvm-databinding)，实践出真知，用一个例子来尝试DataBinding最简单的用法。

Github地址：[https://github.com/LittleFogCat/DataBindingDemo](https://github.com/LittleFogCat/DataBindingDemo)

功能：输入股票代码，查询当前价格和涨幅。

![demo](https://upload-images.jianshu.io/upload_images/6532223-c14af66a28eefc20.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


MVVM+DataBinding+OkHttp，使用的腾讯股票接口[http://qt.gtimg.cn/q=sh600519](http://qt.gtimg.cn/q=sh600519)


**1. 新建模块simpledatabindingkt**

**2. 在module的build.gradle中启用databinding并添加Java8支持**

如果不添加Java8支持，会出现莫名其妙的错误。

```gradle
android {
    ...

    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

**3. 创建StockViewModel类和StockRepository类**

首先明确业务逻辑：我们需要通过网络获取到某只股票当前的行情，并把它显示到View中。

- `StockRepository`用于调用网络api获取数据。
```kotlin
class StockRepository {
    companion object {
        const val URL_PREFIX = "http://qt.gtimg.cn/q="
    }

    fun getStockInfo(code: String, callback: Callback) {
        val realUrl = URL_PREFIX + code
        HttpUtil.httpGet(realUrl, callback)
    }

}
```

- `StockViewModel`处理业务逻辑。这里定义了两个被观察者Observable：用于观察请求到的股票信息的`stockInfoObservable`和用于观察加载状态的`refreshingObservable`。


```kotlin
class StockViewModel() {
    companion object {
        const val TAG = "StockViewModel"
    }

    private val repository = StockRepository()

    val stockInfoObservable = ObservableField<StockInfo>()
    val refreshingObservable = ObservableBoolean(false)
    
    fun queryStock(code: String) {
        refreshingObservable.set(true) // 设置刷新状态
        // 发起请求
        repository.getStockInfo(realCode, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 前略 val stockInfo = ....
                // 根据api返回结果设置股票信息，并更新刷新状态
                stockInfoObservable.set(stockInfo)
                refreshingObservable.set(false)
            }
        })
    }

}
```
*这是一个典型的观察者模式；在**第5节**中View和ViewModel绑定之后，布局中的内容就会因为Observable的更新而更新了。*

*跟MVP架构相比，MVVM更加激进和简洁：它去掉了许多复杂的接口，ViewModel也不像Presenter一样持有View的实例——而是使用观察者模式进行数据的更新。这样的好处是，ViewModel再也不用感知View的存在，只需要关心自己的业务逻辑，而不用关心View的具体实现或者手动去更新状态。从某种意义上来讲，MVVM可以看成MVP的进一步解耦，因为在MVP中，Presenter需要持有View的引用，并且更新它的状态。*


**4. 修改主布局`activity_main.xml`文件**

将原始xml布局用`<layout>`标签包裹起来，然后在其中加入一个`<data>`标签，这个xml文件就被识别为DataBinding布局文件。然后build一下，就可以自动生成binding类了。
`<data>`中的`<variable>`标签表示可以在xml中使用的变量，这里定义了一个`StockViewModel`类型的变量`viewmodel`，这个xml就可以访问`viewmodel`的成员了。这个`viewmodel`变量是在哪里初始化的呢？这在**5**中会说明。

简化后布局如下：
```xml
<layout xmlns:tool="http://schemas.android.com/tools">

    <data>

        <import type="android.view.View" />

        <variable
            name="viewmodel"
            type="top.littlefogcat.simpledatabindingkt.StockViewModel" />

    </data>

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <EditText
                android:id="@+id/input"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>

            <Button
                android:id="@+id/btnGo"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="查询" />
        </LinearLayout>

        <TextView
            android:id="@+id/result"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text='@{viewmodel.refreshingObservable?"加载中...":(viewmodel.stockInfoObservable.name+"（"+viewmodel.stockInfoObservable.code+"）\n当前价："+viewmodel.stockInfoObservable.price+"\n涨幅："+viewmodel.stockInfoObservable.increase+"%")}' />

    </LinearLayout>
</layout>
```

![xml](https://upload-images.jianshu.io/upload_images/6532223-4bbc0d757a80d85c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

build完之后，在build目录下面应该就可以看到自动生成的binding文件了。
![menu](https://upload-images.jianshu.io/upload_images/6532223-2f5e3dd0326c5033.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 注意到，在xml中的查询结果TextView中：
```xml
        <TextView
            android:id="@+id/result"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text='@{viewmodel.refreshingObservable?"加载中...":(viewmodel.stockInfoObservable.name+"（"+viewmodel.stockInfoObservable.code+"）\n当前价："+viewmodel.stockInfoObservable.price+"\n涨幅："+viewmodel.stockInfoObservable.increase+"%")}' />
```
其中的`android:text='@{viewmodel.refreshingObservable?...}'`这个里面用`@{}`包裹起来的部分便是data-binding表达式，其中可以调用`<data>`标签中定义的`viewmodel`对象的成员。它可以是一个变量，或者是一个三元表达式，与Java语法相同。

可以看到，这个表达式特别冗长，作为优化方案，可以使用`BinderAdapter`来处理。[Android Developer - 提供自定义逻辑](https://developer.android.com/topic/libraries/data-binding/binding-adapters#custom-logic)
另外，如果不能够直接进行参数赋值，而有其他无法在xml中完成的逻辑，也可以使用`BinderAdapter`来处理。


**5. 绑定View和ViewModel**

将MainActivity做如下修改：
```kotlin
    private lateinit var mViewModel: StockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this, R.layout.activity_main
        )
        mViewModel = StockViewModel()
        binding.viewmodel = mViewModel // 正是在这里绑定的

        btnGo.setOnClickListener {
            val code = input.text
            mViewModel.queryStock(code.toString())
            if (result.visibility != VISIBLE) {
                result.visibility = VISIBLE
            }
        }
    }
```
这里和普通的Activity不同，没有调用`setContentView`，而是使用`DataBindingUtil.setContentView`来绑定布局。`DataBindingUtil.setContentView`会返回一个Binding对象，也就是**4**中自动build出来的那个。
而**4**中xml布局的`viewmodel`变量，正是这句：`binding.viewmodel = mViewModel`赋值的。

至此，我们将View和ViewModel进行了绑定。再补充一些细节，这个demo就完成了。

![1](https://upload-images.jianshu.io/upload_images/6532223-0f0cc00ec2fca82a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![2](https://upload-images.jianshu.io/upload_images/6532223-e3fc1937815a0a89.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


**遇到的问题**

[报错：Invalid byte 3 of 3-byte UTF-8 sequence.](https://www.jianshu.com/p/f9d8f0082a90)


**小结**

使用DataBinding的基本流程：
1. 在`build.gradle`中开启data-binding；
2. 修改xml布局文件，将根布局替换为`<layout>`，并在其中添加`<data>`标签；
3. build一下，这时候IDE就自动生成Binding文件了。

**2020年9月7日更新：**
新版本的Android Studio只要布局文件修改成了data-binding格式，就自动生成Binding文件，不需要再build了。
