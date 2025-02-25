# 整理一下开发过程中遇到的问题

## 反射系统隐藏方法为null

```xml
<application>
    android:name="ModuleApplication"
</application>
```

[Yuki -> ModuleApplication](https://fankes.github.io/YukiHookAPI/#/api/document?id=moduleapplication-class)

## 深色模式重启activity闪白屏

```kotlin
finish()
startActivity(Intent(Context, MainActivity::class.java))
overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
```
## Fragment调用MainActivity里的方法

```kotlin
(activity as MainActivity?)?.restart()
```

## 动态取色部分不生效
```kotlin
//将动态取色语句放置在onCreate最上层
DynamicColors.applyToActivityIfAvailable(this)
```

## Key.BACK Fragment后台堆栈问题(更换Navigation后弃用)

Preference#app:fragment 默认入栈

```kotlin
//入栈
FragmentTransaction.addToBackStack(null).commit()
//不入栈
FragmentTransaction.commitNow()
```

## Fragment OnBackPressed(更换Navigation后弃用)

Fragment没有Activity里的OnBackPressed返回事件
```kotlin
//处理Fragment嵌套子Fragment返回问题
//1.定义扩展函数
typealias OnBackPressedTypeAlias = () -> Unit
/**
 * 解决 Fragment 中 OnBackPressed 事件, 默认结束当前Fragment依附的Activity
 * @param type true:结束当前Activity，false：响应callback回调
 */
fun Fragment.setOnHandleBackPressed(type: Boolean = true, callback: OnBackPressedTypeAlias? = null) {
    requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (type) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                } else {
                    callback?.invoke()
                }
            }
        })
}

//2.在Fragment中的onCreate方法中使用
setOnBackPressed()
//若需要在返回时做出相应处理
setOnBackPressed(false) {
    Toast.makeText(context, "点击了返回键", Toast.LENGTH_LONG).show()
    // 在Fragment中点击物理返回按钮，回退到手机桌面
    startActivity(Intent(Intent.ACTION_MAIN).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        addCategory(Intent.CATEGORY_HOME)
    })
}
```

TextView链接点击跳转
```kotlin
MaterialTextView(context).apply {
    val url = "<a href='https://www.baidu.com'>百度</a>"
    text = Html.fromHtml("这是百度 $url", 0)
    movementMethod = LinkMovementMethod.getInstance()
}
```