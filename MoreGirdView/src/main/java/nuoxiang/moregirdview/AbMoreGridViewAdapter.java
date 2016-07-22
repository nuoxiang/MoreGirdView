package nuoxiang.moregirdview;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by x on 2016/7/14.
 */
public abstract class AbMoreGridViewAdapter {
    protected Context context;
    private List<ImageInfo> imageInfo;

    public AbMoreGridViewAdapter(Context context, List<ImageInfo> imageInfo) {
        this.context = context;
        this.imageInfo = imageInfo;
    }

    /**
     * 如果要实现图片点击的逻辑，重写此方法即可
     *
     * @param context      上下文
     * @param moreGridView 控件
     * @param index        当前点击图片的的索引
     * @param imageInfo    图片地址的数据集合
     */
    protected abstract void onImageItemClick(Context context, MoreGridView moreGridView, int index, List<ImageInfo> imageInfo);

    /**
     * 生成ImageView容器的方式，默认使用NineGridImageViewWrapper类，即点击图片后，图片会有蒙板效果
     * 如果需要自定义图片展示效果，重写此方法即可
     *
     * @param context 上下文
     * @return 生成的 ImageView
     */
    protected ImageView generateImageView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    public List<ImageInfo> getImageInfo() {
        return imageInfo;
    }

    public void setImageInfoList(List<ImageInfo> imageInfo) {
        this.imageInfo = imageInfo;
    }

    public abstract void onDisplayImage(Context context, ImageView imageView, String url);
}
