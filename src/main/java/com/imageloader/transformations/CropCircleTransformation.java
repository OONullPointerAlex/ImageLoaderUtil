package com.imageloader.transformations;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

public class CropCircleTransformation implements Transformation<Bitmap> {

  private BitmapPool mBitmapPool;
  private Paint mBorderPaint;
  private float mBorderWidth;

  public CropCircleTransformation(Context context) {
    this(Glide.get(context).getBitmapPool());
  }


  public CropCircleTransformation(Context context, int borderWidth, int borderColor) {
    this(context);
    mBorderWidth = Resources.getSystem().getDisplayMetrics().density * borderWidth;

    mBorderPaint = new Paint();
    mBorderPaint.setDither(true);
    mBorderPaint.setAntiAlias(true);
    mBorderPaint.setColor(borderColor);
    mBorderPaint.setStyle(Paint.Style.STROKE);
    mBorderPaint.setStrokeWidth(mBorderWidth);
  }

  public CropCircleTransformation(BitmapPool pool) {
    this.mBitmapPool = pool;
  }

  @Override
  public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
    Bitmap source = resource.get();
    int size = Math.min(source.getWidth(), source.getHeight());

    int width = (source.getWidth() - size) / 2;
    int height = (source.getHeight() - size) / 2;

    Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
    if (bitmap == null) {
      bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    BitmapShader shader =
            new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    if (width != 0 || height != 0) {
      // source isn't square, move viewport to center
      Matrix matrix = new Matrix();
      matrix.setTranslate(-width, -height);
      shader.setLocalMatrix(matrix);
    }
    paint.setShader(shader);
    paint.setAntiAlias(true);

    float r = size / 2f;
    canvas.drawCircle(r, r, r, paint);

    if (mBorderPaint != null) {
      float borderRadius = r - mBorderWidth / 2;
      canvas.drawCircle(r, r, borderRadius, mBorderPaint);
    }

    return BitmapResource.obtain(bitmap, mBitmapPool);
  }

  @Override
  public String getId() {
    return getClass().getName();
  }

}
