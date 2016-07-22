package nuoxiang.demo;

import android.app.Application;

import com.lzy.okhttputils.OkHttpUtils;

/**
 * Created by x on 2016/7/20.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpUtils.init(this);
    }
}
