package nuoxiang.moregirdview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by x on 2016/7/14.
 */
public class MoreGridView extends ViewGroup {
    public static final int MODE_FILL = 0;          //填充模式，类似于微信
    public static final int MODE_GRID = 1;          //网格模式，类似于QQ，4张图会 2X2布局

    private int singleImageSize = 250;              // 单张图片时的最大大小,单位dp
    private float singleImageRatio = 1.0f;          // 单张图片的宽高比(宽/高)
    private int maxImageSize = 9;                   // 最大显示的图片数
    private int gridSpacing = 3;                    // 宫格间距，单位dp
    private int mode = MODE_FILL;                   // 默认使用fill模式

    private int columnCount = DEFAULT_COLUMN;    // 列数
    private int rowCount;       // 行数
    private int gridWidth;      // 宫格宽度
    private int gridHeight;     // 宫格高度

    private List<ImageView> imageViews;
    private List<ImageInfo> mImageInfo;
    private AbMoreGridViewAdapter mAdapter;

    private static final int DEFAULT_COLUMN = 3;

    public MoreGridView(Context context) {
        super(context);
    }

    public MoreGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public MoreGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MoreGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        gridSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gridSpacing, dm);
        singleImageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, singleImageSize, dm);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoreGridView);
        gridSpacing = (int) a.getDimension(R.styleable.MoreGridView_ngv_gridSpacing, gridSpacing);
        singleImageSize = a.getDimensionPixelSize(R.styleable.MoreGridView_ngv_singleImageSize, singleImageSize);
        singleImageRatio = a.getFloat(R.styleable.MoreGridView_ngv_singleImageRatio, singleImageRatio);
        maxImageSize = a.getInt(R.styleable.MoreGridView_ngv_maxSize, maxImageSize);
        mode = a.getInt(R.styleable.MoreGridView_ngv_mode, mode);
        columnCount = a.getInt(R.styleable.MoreGridView_ngv_column, columnCount);

        a.recycle();

        imageViews = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        if (mImageInfo != null && mImageInfo.size() > 0) {
            if (mImageInfo.size() == 1) {
                gridWidth = singleImageSize > totalWidth ? totalWidth : singleImageSize;
                gridHeight = (int) (gridWidth / singleImageRatio);
                //矫正图片显示区域大小，不允许超过最大显示范围
                if (gridHeight > singleImageSize) {
                    float ratio = singleImageSize * 1.0f / gridHeight;
                    gridWidth = (int) (gridWidth * ratio);
                    gridHeight = singleImageSize;
                }
            } else {
                gridWidth = (totalWidth - gridSpacing * (columnCount - 1)) / columnCount;
                gridHeight = (int) (gridWidth / singleImageRatio);
                //这里无论是几张图片，宽高都按总宽度的 1/3
//                gridWidth = gridHeight = (totalWidth - gridSpacing * 2) / 3;
            }
            width = gridWidth * columnCount + gridSpacing * (columnCount - 1) + getPaddingLeft() + getPaddingRight();
            height = gridHeight * rowCount + gridSpacing * (rowCount - 1) + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mImageInfo == null) return;
        int childrenCount = mImageInfo.size();
        for (int i = 0; i < childrenCount; i++) {
            ImageView childrenView = (ImageView) getChildAt(i);
            if (childrenView != null) {
                if (mAdapter != null) {
                    mAdapter.onDisplayImage(getContext(), childrenView, mImageInfo.get(i).getThumbnailUrl());
                }
                int rowNum = i / columnCount;
                int columnNum = i % columnCount;
                int left = (gridWidth + gridSpacing) * columnNum + getPaddingLeft();
                int top = (gridHeight + gridSpacing) * rowNum + getPaddingTop();
                int right = left + gridWidth;
                int bottom = top + gridHeight;
                childrenView.layout(left, top, right, bottom);
            }
        }
    }

    /**
     * 设置适配器
     */
    public void setAdapter(@NonNull AbMoreGridViewAdapter adapter) {
        mAdapter = adapter;
        List<ImageInfo> imageInfo = adapter.getImageInfo();

        if (imageInfo == null || imageInfo.isEmpty()) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }

        int imageCount = imageInfo.size();
        if (maxImageSize > 0 && imageCount > maxImageSize) {
            imageInfo = imageInfo.subList(0, maxImageSize);
            imageCount = imageInfo.size();   //再次获取图片数量
        }

        //默认是3列显示，行数根据图片的数量决定
        rowCount = imageCount / columnCount + (imageCount % columnCount == 0 ? 0 : 1);
//        columnCount = 3;
        //grid模式下，显示4张使用2X2模式
        if (mode == MODE_GRID) {
            if (imageCount == 4) {
                rowCount = 2;
                columnCount = 2;
            } else { //3X?模式
                columnCount = 3;
                rowCount = imageCount / columnCount + (imageCount % columnCount == 0 ? 0 : 1);
            }
        }

        //保证View的复用，避免重复创建
        if (mImageInfo == null) {
            for (int i = 0; i < imageCount; i++) {
                ImageView iv = getImageView(i);
                if (iv == null) return;
                addView(iv, generateDefaultLayoutParams());
            }
        } else {
            int oldViewCount = mImageInfo.size();
            int newViewCount = imageCount;
            if (oldViewCount > newViewCount) {
                removeViews(newViewCount, oldViewCount - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = oldViewCount; i < newViewCount; i++) {
                    ImageView iv = getImageView(i);
                    if (iv == null) return;
                    addView(iv, generateDefaultLayoutParams());
                }
            }
        }
        //修改最后一个条目，决定是否显示更多
        if (adapter.getImageInfo().size() > maxImageSize) {
            View child = getChildAt(maxImageSize - 1);
//            if (child instanceof MoreGridViewWrapper) {
//                NineGridViewWrapper imageView = (NineGridViewWrapper) child;
//                imageView.setMoreNum(adapter.getImageInfo().size() - maxImageSize);
//            }
        }
        mImageInfo = imageInfo;
        requestLayout();
    }

    /**
     * 获得 ImageView 保证了 ImageView 的重用
     */
    private ImageView getImageView(final int position) {
        ImageView imageView;
        if (position < imageViews.size()) {
            imageView = imageViews.get(position);
        } else {
            imageView = mAdapter.generateImageView(getContext());
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.onImageItemClick(getContext(), MoreGridView.this, position, mAdapter.getImageInfo());
                }
            });
            imageViews.add(imageView);
        }
        return imageView;
    }

    /**
     * 设置宫格间距
     */
    public void setGridSpacing(int spacing) {
        gridSpacing = spacing;
    }

    /**
     * 设置只有一张图片时的宽
     */
    public void setSingleImageSize(int maxImageSize) {
        singleImageSize = maxImageSize;
    }

    /**
     * 设置只有一张图片时的宽高比
     */
    public void setSingleImageRatio(float ratio) {
        singleImageRatio = ratio;
    }

    /**
     * 设置最大图片数
     */
    public void setMaxSize(int maxSize) {
        maxImageSize = maxSize;
    }

    public int getMaxSize() {
        return maxImageSize;
    }
}
