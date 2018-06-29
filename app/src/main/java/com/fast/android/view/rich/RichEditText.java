package com.fast.android.view.rich;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tcl.tcldemo.R;

@SuppressLint("AppCompatCustomView")
public class RichEditText extends EditText {

    private Context mContext;

    private Editable mEditable;

    private static final String TAG = "RichNote";
    private ImageTextWatcher imageTextWatcher;
    private RichClickableSpan richClickableSpan;

    public ClickableSpanListener getClickableSpanListener() {
        return clickableSpanListener;
    }

    public void setClickableSpanListener(ClickableSpanListener clickableSpanListener) {
        this.clickableSpanListener = clickableSpanListener;
    }

    ClickableSpanListener clickableSpanListener;


    private interface ClickableSpanListener {
        /**
         * 添加图片点击回调用
         */
        void onSpanClick(int star, int end);
    }


    private class RichClickableSpan implements ClickableSpanListener {


        @Override
        public void onSpanClick(int star, int end) {

        }
    }

    public RichEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }


    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RichEditText(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        imageTextWatcher = new ImageTextWatcher();
        addTextChangedListener(imageTextWatcher);
//        richClickableSpan = new RichClickableSpan();
//        setClickableSpanListener(richClickableSpan);

    }


    /**
     * 添加一个图片
     *
     * @param bitmap
     * @param
     */
    public void addImage(Bitmap bitmap, String filePath) {
        Log.i(TAG, "filePath===" + filePath);
        String pathTag = "<img src=\"" + filePath + "\"/>";
        SpannableString spanString = new SpannableString(pathTag);
        // 获取屏幕的宽高
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int bmWidth = bitmap.getWidth();//图片高度
        int bmHeight = bitmap.getHeight();//图片宽度
        int zoomWidth = getWidth() - (paddingLeft + paddingRight);
        int zoomHeight = (int) (((float) zoomWidth / (float) bmWidth) * bmHeight);
        Bitmap newBitmap = zoomImage(bitmap, zoomWidth, zoomHeight);
        ImageSpan imgSpan = new ImageSpan(mContext, newBitmap);
        spanString.setSpan(imgSpan, 0, pathTag.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mEditable == null) {
            mEditable = this.getText(); // 获取edittext内容
        }

        int start = this.getSelectionStart(); // 设置欲添加的位置

        mEditable.insert(start, spanString); // 设置spanString要添加的位置
        Log.i(TAG, "content===" + mEditable.toString());
        this.setText(mEditable);
        this.setSelection(start, spanString.length());
        setSpanClickable();
    }

    /**
     * @param bitmap
     * @param filePath
     * @param start
     * @param end
     */
    public void addImage(Bitmap bitmap, String filePath, int start, int end) {
        Log.i("imgpath", filePath);
        String pathTag = "<img src=\"" + filePath + "\"/>";
        SpannableString spanString = new SpannableString(pathTag);
        // 获取屏幕的宽高
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int bmWidth = bitmap.getWidth();//图片高度
        int bmHeight = bitmap.getHeight();//图片宽度
        int zoomWidth = getWidth() - (paddingLeft + paddingRight);
        int zoomHeight = (int) (((float) zoomWidth / (float) bmWidth) * bmHeight);
        Bitmap newBitmap = zoomImage(bitmap, zoomWidth, zoomHeight);
        ImageSpan imgSpan = new ImageSpan(mContext, newBitmap);
        spanString.setSpan(imgSpan, 0, pathTag.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Editable editable = this.getText(); // 获取edittext内容
        editable.delete(start, end);//删除
        editable.insert(start, spanString); // 设置spanString要添加的位置
    }

    /**
     * @param
     * @param filePath
     * @param start
     * @param end
     */
    public void addDefaultImage(String filePath, int start, int end) {
        Log.i("imgpath", filePath);
        String pathTag = "<img src=\"" + filePath + "\"/>";
        SpannableString spanString = new SpannableString(pathTag);
        // 获取屏幕的宽高
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int bmWidth = bitmap.getWidth();//图片高度
        int bmHeight = bitmap.getHeight();//图片宽度
        int zoomWidth = getWidth() - (paddingLeft + paddingRight);
        int zoomHeight = (int) (((float) zoomWidth / (float) bmWidth) * bmHeight);
        Bitmap newBitmap = zoomImage(bitmap, zoomWidth, zoomHeight);
        ImageSpan imgSpan = new ImageSpan(mContext, newBitmap);
        spanString.setSpan(imgSpan, 0, pathTag.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mEditable == null) {
            mEditable = this.getText(); // 获取edittext内容
        }
        mEditable.delete(start, end);//删除
        mEditable.insert(start, spanString); // 设置spanString要添加的位置
    }

    /**
     * 对图片进行缩放
     *
     * @param bgimage
     * @param newWidth
     * @param newHeight
     * @return
     */
    private Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        //如果宽度为0 保持原图
        if (newWidth == 0) {
            newWidth = width;
            newHeight = height;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    public void setRichText(String text) {
        this.setText("");
        this.mEditable = this.getText();
        this.mEditable.append(text);
        //遍历查找
        String str = "<img src=\"([/\\w\\W/\\/.]*)\"\\s*/>";
        Pattern pattern = Pattern.compile(str);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            final String localFilePath = matcher.group(1);
            String matchString = matcher.group();
            final int matchStringStartIndex = text.indexOf(matchString);
            final int matchStringEndIndex = matchStringStartIndex + matchString.length();
            ImageLoader.getInstance().loadImage(localFilePath, getDisplayImageOptions(), new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String uri, View arg1) {
                    // TODO Auto-generated method stub
                    //插入一张默认图片
                    addDefaultImage(localFilePath, matchStringStartIndex, matchStringEndIndex);
                }

                @Override
                public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onLoadingComplete(String uri, View arg1, Bitmap bitmap) {
                    // TODO Auto-generated method stub
                    addImage(bitmap, uri, matchStringStartIndex, matchStringEndIndex);
                }

                @Override
                public void onLoadingCancelled(String arg0, View arg1) {

                }
            });
        }
        this.setText(mEditable);
    }

    private DisplayImageOptions getDisplayImageOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_img)
                .showImageForEmptyUri(R.drawable.default_img)
                .showImageOnFail(R.drawable.default_img)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true).build();
        return options;
    }

    public String getRichText() {
        return this.getText().toString();
    }

    private class ImageTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            mEditable = editable;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        //触摸的是EditText控件，且当前EditText可以滚动，则将事件交给EditText处理；否则将事件交由其父类处理
        if (canVerticalScroll()) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        }
        return super.onTouchEvent(motionEvent);

    }

    private boolean canVerticalScroll() {
        //滚动的距离
        int scrollY = this.getScrollY();
        //控件内容的总高度
        int scrollRange = this.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = this.getHeight() - this.getCompoundPaddingTop() - this.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }


    public void setSpanClickable() {
        Spanned s = getText();
        if (s == null) {
            return;
        }
        //setMovementMethod很重要，不然ClickableSpan无法获取点击事件。
        setMovementMethod(LinkMovementMethod.getInstance());
        ImageSpan[] imageSpans = s.getSpans(0, s.length(), ImageSpan.class);

        for (ImageSpan span : imageSpans) {
            final String image_src = span.getSource();
            final int start = s.getSpanStart(span);
            final int end = s.getSpanEnd(span);

            Log.i(TAG, "setSpanClickable , image_src = " + image_src + " , start = " + start + " , end = " + end);

            ClickableSpan click_span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    setCursorVisible(false);
                    if (clickableSpanListener != null) {
                        clickableSpanListener.onSpanClick(start, end);
                    }

                }
            };
            ClickableSpan[] click_spans = s.getSpans(start, end,
                    ClickableSpan.class);
            if (click_spans.length != 0) {
                // remove all click spans
                for (ClickableSpan c_span : click_spans) {
                    ((Spannable) s).removeSpan(c_span);
                }
            }
            ((Spannable) s).setSpan(click_span, start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}