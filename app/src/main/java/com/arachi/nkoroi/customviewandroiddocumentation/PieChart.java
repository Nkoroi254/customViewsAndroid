package com.arachi.nkoroi.customviewandroiddocumentation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nkoroi on 19/04/17.
 */

/**
 * To allow android studio to interact with your
 * view, at a minimum you have to provide a constructor
 * with a Context and AttributeSet object as params.
 *
 * This constructor allows the editor to
 * create and edit an instance of the view.
 *
 * In creating custom views you can either extend the View
 * object or extend one of androids views eg Button etc
 */

/**
 * To add a built in View to your UI
 * you specify it in an XML element an control
 * its appearance and behavior with element attributes.
 * Well-written custom views can also be added and styled via XML.
 * To enable this behavior in your custom view, you must:
 * ->Define custom attrs for your view in a <declare-styleable> </declare-styleable> resource element
 * ->Specify values for the attr in your XML layout
 * ->Retrieve attr values at runtime
 * ->Apply the retrieved attr values to your view
 */

/**
 * Apply Custom Attributes
 *
 * The Android resource compiler does a lot of work for you
 * to make calling obtainStyledAttributes() easier.
 * For each <declare-styleable> resource in the res directory,
 * the generated R.java defines both an array of attribute ids
 * and a set of constants that define the index for each attribute
 * in the array. You use the predefined constants to read the
 * attributes from the TypedArray. Here's how the PieChart class
 * reads its attributes:
 */

public class PieChart extends ViewGroup {
  private List<Item> mData = new ArrayList<Item>();

  private float mTotal = 0.0f;

  private RectF mPieBounds = new RectF();

  private Paint mPiePaint;
  private Paint mTextPaint;
  private Paint mShadowPaint;

  private boolean mShowText = false;

  private float mTextX = 0.0f;
  private float mTextY = 0.0f;
  private float mTextWidth = 0.0f;
  private float mTextHeight = 0.0f;
  private int mTextPos = TEXTPOS_LEFT;

  private float mHighlightStrength = 1.15f;

  private float mPointerRadius = 2.0f;
  private float mPointerX;
  private float mPointerY;

  private int mPieRotation;

  private OnCurrentItemChangedListener mCurrentItemChangedListener = null;

  private int mTextColor;
  private PieView mPieView;
  private Scroller mScroller;
  private ValueAnimator mScrollAnimator;
  private GestureDetector mDetector;
  private PointerView mPointerView;

  // The angle at which we measure the current item. This is
  // where the pointer points.
  private int mCurrentItemAngle;

  // the index of the current item.
  private int mCurrentItem = 0;
  private boolean mAutoCenterInSlice;
  private ObjectAnimator mAutoCenterAnimator;
  private RectF mShadowBounds = new RectF();

  /**
   * Draw text to the left of the pie chart
   */
  public static final int TEXTPOS_LEFT = 0;

  /**
   * Draw text to the right of the pie chart
   */
  public static final int TEXTPOS_RIGHT = 1;

  /**
   * The initial fling velocity is divided by this amount.
   */
  public static final int FLING_VELOCITY_DOWNSCALE = 4;

  /**
   *
   */
  public static final int AUTOCENTER_ANIM_DURATION = 250;



  /**
   * Interface definition for a callback to be invoked when the current
   * item changes.
   */
  public interface OnCurrentItemChangedListener {
    void OnCurrentItemChanged(PieChart source, int currentItem);
  }

  /**
   * Class constructor taking only a context. Use this constructor to create
   * {@link PieChart} objects from your own code.
   *
   * @param context
   */
  public PieChart(Context context) {
    super(context);
    init();
  }

  public PieChart(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    /**
     * Apply Custom Attributes
     *
     * When View is created from XML layout, all attributes in XML tag are read 4rm resource
     * bundled and passed to View's constructor as an AttributesSet.
     * Its possible to get them directly but that has some disadvantages eg
     *  -> resource ref within attr values are not resolved
     *  -> Styles are not applied
     * Instead pass the AttributeSet to obtainStyledAttributes().
     * This mtd returns back a TypedArray array of values that have
     * already been de-referenced and styled.
     */
    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.PieChart,
        0,0
    );

    try{
      //Retrieve the values from the TypeArray and store into
      //fields of this class.
      //
      //The R.stylable.PieChart_* constants rep the index for
      //each custom attribute in the R.styleable.PieChart array.
      mShowText = a.getBoolean(R.styleable.PieChart_showText, false);
      mTextY = a.getDimension(R.styleable.PieChart_labelY, 0.0f);
      mTextWidth = a.getDimension(R.styleable.PieChart_labelWidth, 0.0f);
      mTextHeight = a.getDimension(R.styleable.PieChart_labelHeight, 0.0f);
      mTextPos  = a.getInteger(R.styleable.PieChart_labelPositionNkoroi, 0);
      mTextColor = a.getColor(R.styleable.PieChart_labelColor, 0xff000000);
      mHighlightStrength = a.getFloat(R.styleable.PieChart_highlightStrength, 1.0f);
      mPieRotation = a.getInt(R.styleable.PieChart_pieRotation, 0);
      mPointerRadius = a.getDimension(R.styleable.PieChart_pointerRadius, 2.0f);
      mAutoCenterInSlice = a.getBoolean(R.styleable.PieChart_autoCenterPointerInSlice, false);
    }finally {
      a.recycle();
    }
    init();
  }

  @Override protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
    // Do nothing. Do not call the superclass method--that would start a layout pass
    // on this view's children. PieChart lays out its children in onSizeChanged().
  }

  /**
   * Returns true if the text label should be visible.
   * @return True if the text label should be visible, false otherwise
   */
  public boolean getShowText(){
    return mShowText;
  }


  /**
   * Set the current rotation of the pie graphic. Setting this value may change
   * the current item.
   *
   * @param rotation The current pie rotation, in degrees.1
   */
  public void setPieRotation(int rotation) {
    rotation = (rotation % 360 + 360) % 360;
    mPieRotation = rotation;
    mPieView.rotateTo(rotation);

    calcCurrentItem();
  }
  /**
   * Add Properties and Events
   *
   * Attributes are a powerful way to control
   * the View's appearance and behavior but they can
   * only be read when the view is initialized.
   *
   * To provide dynamic behavior expose a property getter and setter
   * pair for each custom attribute.
   * The 2 methods below show how PieChat exposes a property
   * called showText.
   */
  public boolean isShowText(){
    return mShowText;
  }


  /**
   *
   * invalidate() and requestLayout() ensure views behave reliably.
   * you have to invalidate the view after any change to its properties
   * that might change its appearance, so that the system knows that it needs to be redrawn.
   * Likewise, you need to request a new layout if a property changes that might affect the
   * size or shape of the view. Forgetting these method calls can cause hard-to-find bugs.
   *
   * Custom views should also support event listeners to
   * communicate important events. For instance, PieChart
   * exposes a custom event called OnCurrentItemChanged to
   * notify listeners that the user has rotated the pie chart to
   * focus on a new pie slice.
   * It's easy to forget to expose properties and events,
   * especially when you're the only user of the custom view.
   * Taking some time to carefully define your view's interface
   * reduces future maintenance costs. A good rule to follow is
   * to always expose any property that affects the visible appearance
   * or behavior of your custom view.
   *
   * Controls whether the text label is visible or not. Setting this property to
   * false allows the pie chart graphic to take up the entire visible area of
   * the control.
   *
   * @param showText true if the text label should be visible, false otherwise
   */
  public void setShowText(boolean showText){
    mShowText = showText;
    invalidate();
    //requestLayout();
  }

  /**
   * Returns the Y position of the label text, in pixels
   * @return The Y position of the label text, in pixels.
   */
  public float getTextY(){
    return mTextY;
  }

  /**
   * Set the Y position of the label text, in pixels.
   * @param textY the Y position of the label text, in the pixels
   */
  public void setTextY(float textY){
    mTextY = textY;
    invalidate();
  }

  /**
   * Returns the width reserved for label text, in pixels.
   * @return The width reserved for lable text, in pixels
   */
  public float getTextWidth(){
    return mTextWidth;
  }

  /**
   * Set the width of the area reserved for lable text. This width is constant; it does not
   * change based on the actual width of the label text changes.
   *
   * @Param textWidth The width reserved for label text, in pixels
   */
  public void setTextWidth(float textWidth){
    mTextWidth = textWidth;
    invalidate();
  }

  /**
   * Returns the height of the label font, in pixels.
   *
   * @return The height of the label font, in pixels.
   */
  public float getTextHeight(){
    return mTextHeight;
  }

  /**
   * Set the height of the label font, in pixels.
   *
   * @param  textHeight The height of the label font, in pixels.
   */
  public  void setTextHeight(float textHeight){
    mTextHeight = textHeight;
    invalidate();
  }

  /**
   * Returns a value that specifies whether the label text is to the right
   * or the left of the pie chart graphic.
   *
   * @return One of TEXTPOS_LEFT or TEXTPOS_RIGHT.
   */
  public int getTextPos(){
    return mTextPos;
  }

  /**
   * Set a value that specifies whether the label text is to the right
   * or the left of the pie chart graphic.
   *
   * @param textPos TEXTPOS_LEFT to draw the text to the left of the graphic,
   *                    or TEXTPOS_RIGHT to draw the text to the right of the graphic.
   */
  public void setTextPos(int textPos){
    if (textPos != TEXTPOS_LEFT && textPos != TEXTPOS_RIGHT){
      throw new IllegalArgumentException(
          "TextPos must be one of TEXTPOS_LEFT or TEXTPOS_RIGHT"
      );
    }
    mTextPos = textPos;
    invalidate();
  }

  /**
   * Returns the strength of the highlighting applied to each pie segment.
   *
   * @return The highlight strength.
   */
  public float getHighlightStrength() {
    return mHighlightStrength;
  }

  /**
   * Set the strength of the highlighting that is applied to each pie segment.
   * This number is a floating point number that is multiplied by the base color of
   * each segment to get the highlight color. A value of exactly one produces no
   * highlight at all. Values greater than one produce highlights that are lighter
   * than the base color, while values less than one produce highlights that are darker
   * than the base color.
   *
   * @param highlightStrength The highlight strength.
   */
  public void setHighlightStrength(float highlightStrength) {
    if (highlightStrength < 0.0f) {
      throw new IllegalArgumentException(
          "highlight strength cannot be negative");
    }
    mHighlightStrength = highlightStrength;
    invalidate();
  }

  /**
   * Returns the radius of the filled circle that is drawn at the tip of the current-item
   * pointer.
   *
   * @return The radius of the pointer tip, in pixels.
   */
  public float getPointerRadius() {
    return mPointerRadius;
  }

  /**
   * Set the radius of the filled circle that is drawn at the tip of the current-item
   * pointer.
   *
   * @param pointerRadius The radius of the pointer tip, in pixels.
   */
  public void setPointerRadius(float pointerRadius) {
    mPointerRadius = pointerRadius;
    invalidate();
  }

  /**
   * Returns the current rotation of the pie graphic.
   *
   * @return The current pie rotation, in degrees.
   */
  public int getPieRotation() {
    return mPieRotation;
  }



  /**
   * Returns the index of the currently selected data item.
   *
   * @return The zero-based index of the currently selected data item.
   */
  public int getCurrentItem() {
    return mCurrentItem;
  }

  /**
   * Set the currently selected item. Calling this function will set the current selection
   * and rotate the pie to bring it into view.
   *
   * @param currentItem The zero-based index of the item to select.
   */
  public void setCurrentItem(int currentItem) {
    setCurrentItem(currentItem, true);
  }

  /**
   * Set the current item by index. Optionally, scroll the current item into view. This version
   * is for internal use--the scrollIntoView option is always true for external callers.
   *
   * @param currentItem    The index of the current item.
   * @param scrollIntoView True if the pie should rotate until the current item is centered.
   *                       False otherwise. If this parameter is false, the pie rotation
   *                       will not change.
   */
  private void setCurrentItem(int currentItem, boolean scrollIntoView) {
    mCurrentItem = currentItem;
    if (mCurrentItemChangedListener != null) {
      mCurrentItemChangedListener.OnCurrentItemChanged(this, currentItem);
    }
    if (scrollIntoView) {
      centerOnCurrentItem();
    }
    invalidate();
  }

  /**
   * Add a new data item to this view. Adding an item adds a slice to the pie whose
   * size is proportional to the item's value. As new items are added, the size of each
   * existing slice is recalculated so that the proportions remain correct.
   *
   * @param label The label text to be shown when this item is selected.
   * @param value The value of this item.
   * @param color The ARGB color of the pie slice associated with this item.
   * @return The index of the newly added item.1
   */
  public int addItem(String label, float value, int color) {
    Item it = new Item();
    it.mLabel = label;
    it.mColor = color;
    it.mValue = value;

    // Calculate the highlight color. Saturate at 0xff to make sure that high values
    // don't result in aliasing.
    it.mHighlight = Color.argb(
        0xff,
        Math.min((int) (mHighlightStrength * (float) Color.red(color)), 0xff),
        Math.min((int) (mHighlightStrength * (float) Color.green(color)), 0xff),
        Math.min((int) (mHighlightStrength * (float) Color.blue(color)), 0xff)
    );
    mTotal += value;

    mData.add(it);

    onDataChanged();

    return mData.size() - 1;
  }

  /**
   * Creating Drawing objects
   *
   * android.graphics divides drawing into 2:
   * ->What to draw, handled by Canvas
   * ->How to draw, handled by Paint
   * ie Canvas provides mtds to draw a line and Paint provides mtds
   * to define that lines color.
   * Canvas has mtd to draw a rectangle , whle Paint defines whether to
   * fill that retangle with a color or leave it empty.
   * Simply put {@link Canvas} defines shapes that can be drawn on Screen
   * while {@link Paint} defines the color, style, font and so forth for each shape you draw.
   *
   * So before drawing anything you need to create one or more Paint objects.
   * Piechart does this in init() which is called in the constructor.
   *
   * Note: creating objects ahead of time is an important optimization.
   * Views are redrawn frequently, and many drawing objects require
   * expensive initialization.
   * Creating drawing objects in your onDraw() mtd significantly reduces performance
   * and can cause UI to appear sluggish.
   */
  private void init(){

    //set up the paint for the label text
    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mTextColor);
    if (mTextHeight == 0){
      mTextHeight = mTextPaint.getTextSize();
    } else{
      mTextPaint.setTextSize(mTextHeight);
    }

    //set up paint for the pie slices
    mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPiePaint.setStyle(Paint.Style.FILL);
    mPiePaint.setTextSize(mTextHeight);

    //set up the paint for the shadow
    mShadowPaint = new Paint(0);
    mShadowPaint.setColor(0xff101010);
    mShadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));


    //Add a child view to draw the pie. Putting this in a child view
    //makes it possible to draw it on a separate hardware layer that rotates
    //independently
    mPieView = new PieView(getContext());
    addView(mPieView);
    mPieView.rotateTo(mPieRotation);

    //the pointer doesn't need hardware acceleration , but in order to show up
    //in front of the pie it also needs to be on a separate view.
    mPointerView = new PointerView(getContext());
    addView(mPointerView);

    /**
     * Make Your Transitions Smoooth
     *
     * Set up an animator to animate the PieRotation property. This is used to
     * correct the pie's orientation after the user lets go of it.
     *
     * Users expect UI transitions to be smooth and not stop abruptly. Android
     * introduced property animation framework intoduced in Android 3.0, makes
     * smooth transitions easy.
     *
     * To use the animation system, whenever a property changes that will affect your
     * view's appearance, do not change the property directly. Instead use {@link ValueAnimator}
     * to make the change.
     * In the following example, modify the currently selected pie slice in PieChart that causes
     * the entire chart to rotate so that the selection pointer is centered in the selected slice.
     * {@link ValueAnimator} changes the rotation over a period of several hundred miliseconds, rather
     * than immediately setting the new rotation value.
     */
    if (Build.VERSION.SDK_INT >= 11){
      mAutoCenterAnimator = ObjectAnimator.ofInt(PieChart.this, "PieRotation", 0);

      //Add a listener to hook the onAnimationEnd event so that we can do
      //some cleanup when the pie stops moving
      mAutoCenterAnimator.addListener(new Animator.AnimatorListener() {
        @Override public void onAnimationStart(Animator animator) {

        }

        @Override public void onAnimationEnd(Animator animator) {
          mPieView.decelerate();
        }

        @Override public void onAnimationCancel(Animator animator) {

        }

        @Override public void onAnimationRepeat(Animator animator) {

        }
      });
    }

    //create a gesture detector to handle onTouch messages
    mDetector = new GestureDetector(PieChart.this.getContext(), new GestureListener());

    /**
     * create a {@link Scroller} to handle the fling gesture. 
     */
    if (Build.VERSION.SDK_INT < 11){
      mScroller = new Scroller(getContext());
    }else{
      mScroller = new Scroller(getContext(), null, true);
    }
    /**
     * The {@link Scroller} doesn't support any built in animations functions
     * it just supplies values when we ask it to. So we have to have a way to call
     * it every frame until the fling ends. This code absolutely uses a {@link ValueAnimator}
     * object to generate a callback on every animation frame. We don't use the animated value at all.
     */
    if (Build.VERSION.SDK_INT >= 11){
      mScrollAnimator = ValueAnimator.ofFloat(0, 1);
      mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
          tickScrollAnimation();
        }
      });
    }

    // Turn off long press--this control doesn't use it, and if long press is enabled,
    // you can't scroll for a bit, pause, then scroll some more (the pause is interpreted
    // as a long press, apparently)
    mDetector.setIsLongpressEnabled(false);


    // In edit mode it's nice to have some demo data, so add that here.
    if (this.isInEditMode()) {
      Resources res = getResources();
      addItem("Annabelle", 3, res.getColor(R.color.bluegrass));
      addItem("Brunhilde", 4, res.getColor(R.color.chartreuse));
      addItem("Carolina", 2, res.getColor(R.color.emerald));
      addItem("Dahlia", 3, res.getColor(R.color.seafoam));
      addItem("Ekaterina", 1, res.getColor(R.color.slate));
    }

  }

  private void tickScrollAnimation() {
    if(!mScroller.isFinished()){
      mScroller.computeScrollOffset();
      setPieRotation(mScroller.getCurrY());
    }else{
      mScrollAnimator.cancel();
      onScrollFinished();
    }
  }

  private void setLayerToSW(View v) {
    if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  private void setLayerToHW(View v) {
    if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
      setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
  }


  /**
   * Called when the user finishes a scroll action
   */
  private void onScrollFinished() {
    if (mAutoCenterInSlice){
      centerOnCurrentItem();
    }else {
      mPieView.decelerate();
    }
  }

  /**
   * Kicks off an animation that will result in the pointer being
   * centered in the pie slice of the currently selected item.
   */
  private void centerOnCurrentItem() {
    Item current = mData.get(getCurrentItem());
    int targetAngle = current.mStartAngle + (current.mEndAngle - current.mStartAngle)/2;
    targetAngle -= mCurrentItemAngle;
    if (targetAngle < 90 && mPieRotation > 100) targetAngle += 360;

    if (Build.VERSION.SDK_INT >= 11){
      //Fancy animated version
      mAutoCenterAnimator.setIntValues(targetAngle);
      mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
    }else{
      //Dull non-animated version
      //mPieView.rotateTo(targetAngle);
    }
  }

  /**
   * Handling Layout Events
   * In order to properly draw custom view you have to know what size it is.
   * Some complex view require to perform multiple layout calculations depending on
   * size and shape of their area on screen.
   * You should never make assumptions about size of your view on screen. Even if
   * the view belongs only to your app the app still has to contend with the dynamics
   * of screen sizes , diffrent screen densities and various aspect ratios in both
   * portrait and landscape mode.
   *
   * View has many mtds for handling measurement most do not need to be overridden.
   * If the view does not need special control over its size the only mtd you
   * need to override is onSizechanged().
   *
   * onSizeChanged is called when yor view is first assigned a size, and again if the
   * size of your view changes for any reason.
   * Calculate the positions of dimensions and any other valuee related to your view
   * in onSizeChanged() , instead of recalculating every time the you draw.
   *
   * PieChart's onSizechaged() is where PieChart calculates the bounding rectangle of
   * the pie chart and the relative position of the text label and other visual elements.
   *
   * Note: When the view is assigned a size, the layout manager assumes tha the size
   * includes all the view's padding.
   * You have to handle padding values when you calculate the view's size.
   */
  @Override
  public void onSizeChanged(int w, int h, int oldw, int oldh){
    super.onSizeChanged(w,h,oldw,oldh);

    //Accounting for padding
    float xpad = (float)(getPaddingLeft() + getPaddingRight());
    float ypad = (float)(getPaddingBottom() + getPaddingTop());

    //Account for the label
    if (mShowText) xpad += mTextWidth;

    float ww = (float)w - xpad;
    float hh = (float)h - ypad;

    //Figure out how big we can make the pie.
    float diameter = Math.min(ww, hh);

    mPieBounds = new RectF(
        0.0f,
        0.0f,
        diameter,
        diameter);
    mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());

    mPointerY = mTextY - (mTextHeight / 2.0f);
    float pointerOffset = mPieBounds.centerY() - mPointerY;

    // Make adjustments based on text position
    if (mTextPos == TEXTPOS_LEFT) {
      mTextPaint.setTextAlign(Paint.Align.RIGHT);
      if (mShowText) mPieBounds.offset(mTextWidth, 0.0f);
      mTextX = mPieBounds.left;

      if (pointerOffset < 0) {
        pointerOffset = -pointerOffset;
        mCurrentItemAngle = 225;
      } else {
        mCurrentItemAngle = 135;
      }
      mPointerX = mPieBounds.centerX() - pointerOffset;
    } else {
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      mTextX = mPieBounds.right;

      if (pointerOffset < 0) {
        pointerOffset = -pointerOffset;
        mCurrentItemAngle = 315;
      } else {
        mCurrentItemAngle = 45;
      }
      mPointerX = mPieBounds.centerX() + pointerOffset;
    }

    mShadowBounds = new RectF(
        mPieBounds.left + 10,
        mPieBounds.bottom + 10,
        mPieBounds.right - 10,
        mPieBounds.bottom + 20);

    // Lay out the child view that actually draws the pie.
    mPieView.layout((int) mPieBounds.left,
        (int) mPieBounds.top,
        (int) mPieBounds.right,
        (int) mPieBounds.bottom);
    mPieView.setPivot(mPieBounds.width() / 2, mPieBounds.height() / 2);

    mPointerView.layout(0, 0, w, h);
    onDataChanged();
  }

  /**
   * Calculate which pie slice is under the pointer, and set the current item
   * field accordingly.
   */
  private void calcCurrentItem() {
    int pointerAngle = (mCurrentItemAngle + 360 + mPieRotation) % 360;
    for (int i = 0; i < mData.size(); ++i) {
      Item it = mData.get(i);
      if (it.mStartAngle <= pointerAngle && pointerAngle <= it.mEndAngle) {
        if (i != mCurrentItem) {
          setCurrentItem(i, false);
        }
        break;
      }
    }
  }

  /**
   * Do all of the recalculations needed when the data array changes.
   */
  private void onDataChanged() {
    // When the data changes, we have to recalculate
    // all of the angles.
    int currentAngle = 0;
    for (Item it : mData) {
      it.mStartAngle = currentAngle;
      it.mEndAngle = (int) ((float) currentAngle + it.mValue * 360.0f / mTotal);
      currentAngle = it.mEndAngle;


      // Recalculate the gradient shaders. There are
      // three values in this gradient, even though only
      // two are necessary, in order to work around
      // a bug in certain versions of the graphics engine
      // that expects at least three values if the
      // positions array is non-null.
      //
      it.mShader = new SweepGradient(
          mPieBounds.width() / 2.0f,
          mPieBounds.height() / 2.0f,
          new int[]{
              it.mHighlight,
              it.mHighlight,
              it.mColor,
              it.mColor,
          },
          new float[]{
              0,
              (float) (360 - it.mEndAngle) / 360.0f,
              (float) (360 - it.mStartAngle) / 360.0f,
              1.0f
          }
      );
    }
    calcCurrentItem();
    onScrollFinished();
  }

  /**
   * For finer control over view's layout param implement onMeasure().
   * onMeasure() params are View.MeasureSpec values that tell you how
   * big your view's parent wants your view to be, and whether that size is a
   * hard maximum or just a suggestion.
   * As an optimization, these values are stored in as packed integers, and you use static methods
   * of View.MeasureSpec to unpack the information stored in eah integer.
   *
   * PieChart's onMeasure() tries to make it's area big enough to make the pie as big  as its label.
   *
   * There are three important things to note in the code below:
   * ->The calculations take into account the view's padding.As mentioned earlier, this is the view's responsibility.
   * ->The helper method resolveSizeAndState() is used to create the final width and height values.
   * This helper returns an appropriate View.MeasureSpec value by comparing the view's desired
   * size to the spec passed into onMeasure().
   * ->onMeasure() has no return value. Instead, the method communicates its results
   * by calling setMeasuredDimension(). Calling this method is mandatory. If you
   * omit this call, the View class throws a runtime exception.
   *
   * @param heightMeasureSpec
   * @param widthMeasureSpec
   */
  public void onMeasure(int heightMeasureSpec, int widthMeasureSpec){
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //Try for a width based on our minimum
    int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
    int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

    //Whatever the width ends up being, ask for a height that would let the pie
    //get as big as it can
    int minH = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
    int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)mTextWidth, heightMeasureSpec, 0);
    setMeasuredDimension(w, h);
  }

  // Measurement functions. This example uses a simple heuristic: it assumes that
  // the pie chart should be at least as wide as its label.
  //
  @Override
  protected int getSuggestedMinimumWidth() {
    return (int) mTextWidth * 2;
  }

  @Override
  protected int getSuggestedMinimumHeight() {
    return (int) mTextWidth;
  }


  /**
   * Draw!
   * Once you have object creation and measuring code defined, you can implement onDraw().
   * Every view implements onDraw() differently, but there are some common operations that
   * most views share:
   *
   * -> Draw text using drawText(). Specify the typeFace by calling setTypeFace(), and the text color by
   * calling setColor().
   *
   * ->Draw primitive shapes using drawRect(), drawOval(), and drawArc. hange whether the shape is
   * filled , outlined or both by calling setStyle().
   *
   * -> Draw more complex shapes using the Path class. Define  a shape by adding lines and curves to a Path object
   * , then draw the shape using drawPath(). Just as with primitive shapes , paths can be filled, or outlined, or both
   * depending on the setStyle()
   *
   * ->Draw gradient fills by creating LinearGradient objects. Call setShader() to use your LinearGradient on filled
   * shapes.
   *
   * ->Draw bitmaps using drawBitmap().
   *
   * PieChart uses a mix  of text,  lines and shapes
   * @param canvas
   */
  public void onDraw(Canvas canvas){
    super.onDraw(canvas);

    //Draw the shadow
    canvas.drawOval(
        mShadowBounds,
        mShadowPaint
    );

    //Draw the label text
    if (getShowText()) {
      canvas.drawText(mData.get(mCurrentItem).mLabel, mTextX, mTextY, mTextPaint);
    }

    ////Draw the pie slices
    //for (int i = 0; i < mData.size(); ++i){
    //  Item it = mData.get(i);
    //  mPiePaint.setShader(it.mShader);
    //  canvas.drawArc(mBounds,
    //      360 - it.EndAngle,
    //      it.mEndAngle - it.mStartAngle,
    //      true, mPiePaint);
    //}
    //
    ////Draw the pointer
    //canvas.drawLine(mTextX, mPointerY, mPointerX, mPointerY, mTextPaint);
    //canvas.drawCircle(mPointerX, mPointerY, mPointerSize, mTextPaint);

    //If the API level is less than 11, we can't rely on the view animation system to
    //do the scrolling animation. Need to tick it here and call postInvalidate() until the scrolling is done.
    if (Build.VERSION.SDK_INT < 11){
      tickScrollAnimation();
      if(!mScroller.isFinished()){
        postInvalidate();
      }
    }
  }


  /**
   * Handling input gestures
   *
   * Like many UI frameworks, Android supports an input event model.
   * Users Action are turned into events that trigger callbacks,
   * you can override the callbacks to customize how your app responds to the user.
   *
   * The most common input event in the Android system is touch, whih triggers
   * onTouchEvent(android.view.MotionEvent). Override the mtd to handle the event.
   * Touch events by themselves are not particularly useful. Modern touch UIs define
   * interactions in terms of gestures such as tapping, pulling, pushing, flinging, and zooming.
   * To convert raw touch event into gestures, Android provides {@link GestureDetector}
   */

  @Override
  public boolean onTouchEvent(MotionEvent event){
    boolean result = mDetector.onTouchEvent(event);
    if (!result){
      if (event.getAction() == MotionEvent.ACTION_UP){
        stopScrolling();
        result = true;
      }
    }
    return result;
  }



  /**
   * Force a stop to all pie motion. Called when the user taps during a fling.
   */
  private void stopScrolling() {
    mScroller.forceFinished(true);
    if (Build.VERSION.SDK_INT >= 11) {
      mAutoCenterAnimator.cancel();
    }

    onScrollFinished();
  }

  /**
   * View that draws the pie chart
   */
  private class PieView extends View{
    //used for SDK < 11
    private float mRotation = 0;
    private Matrix mTranformation = new Matrix();
    private PointF mPivot = new PointF();

    /**
     * Construct a PieView
     *
     * @param context
     */
    public PieView(Context context) {
      super(context);
    }

    /**
     * Enable hardware acceleration (consumes memory)
     */
    public void accelerate(){
      setLayerToHW(this);
    }

    /**
     * Disable hardware acceleration (releases memory)
     */
    public void decelerate(){
      setLayerToSW(this);
    }


    @Override
    public void onDraw(Canvas canvas){
      super.onDraw(canvas);

      if (Build.VERSION.SDK_INT < 11){
        mTranformation.set(canvas.getMatrix());
        mTranformation.preRotate(mRotation, mPivot.x, mPivot.y);
        canvas.setMatrix(mTranformation);
      }

      for (Item it : mData){
        mPiePaint.setShader(it.mShader);
        canvas.drawArc(mBounds,
            360 - it.mEndAngle,
            it.mEndAngle - it.mStartAngle,
            true, mPiePaint);
      }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
      mBounds = new RectF(0,0,w,h);
    }

    RectF mBounds;

    public void rotateTo(float pieRotation){
      mRotation = pieRotation;
      if (Build.VERSION.SDK_INT >= 11){
        setRotation(pieRotation);
      }else{
        invalidate();
      }
    }

    public void setPivot(float x, float y){
      mPivot.x = x;
      mPivot.y = y;
      if(Build.VERSION.SDK_INT >= 11){
        setPivotX(x);
        setPivotY(y);
      }else{
        invalidate();
      }
    }
  }

  /**
   * View that draws the pointeer on top of the pie chart
   */
  private class PointerView extends  View {
    /**
     * Constructor a PinterView object
     *
     * @param context
     */
    public PointerView(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas canvas){
      canvas.drawLine(mTextX, mPointerY, mPointerX, mPointerY, mTextPaint);
      canvas.drawCircle(mPointerX, mPointerY, mPointerRadius, mTextPaint);
    }
  }

  /**
   * Maintains the state for a data item
   */
  private class Item{
    public String mLabel;
    public float mValue;
    public int mColor;

    //computed values
    public int mStartAngle;
    public int mEndAngle;

    public int mHighlight;
    public Shader mShader;

  }


  /**
   * Construct {@link GestureDetector} by passing an instance of a class that implements
   * {@link GestureDetector.OnGestureListener}.
   *
   *If all you want is to process afew gestures, you extend {@link GestureDetector.SimpleOnGestureListener}
   *  instead of implementing the {@link GestureDetector.OnGestureListener} interface.
   *  {@link GestureListener} implements {@link GestureDetector.SimpleOnGestureListener} and overrides
   *  onDown({@link MotionEvent}).
   *  Note: You must always implement an onDown() mtd that returns true ie whether or not you implement
   *  {@link GestureDetector.SimpleOnGestureListener} why ? because all gestures begin with onDown() message.
   *  returning false from {@link GestureDetector.SimpleOnGestureListener} onDown()the system assumes that
   *  you want to ignore the rest of the gesture and the other mtds of {@link GestureDetector.OnGestureListener}
   *  never get called.
   *  ie the only time you should return false from onDown is when you intend to ignore an entire
   *  {@link GestureDetector}.
   *
   *  Once you've implemented {@link GestureDetector.OnGestureListener} and created an instance of
   *  {@link GestureDetector}, you can use your {@link GestureDetector} to interpret the touch events you receive
   *  in onTouchEvent().
   */
  class GestureListener extends GestureDetector.SimpleOnGestureListener{

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
      //set the pie rotation directly
      float scrollTheta = vectorToScalarScroll(
          distanceX,
          distanceY,
          e2.getX() - mPieBounds.centerX(),
          e2.getY() - mPieBounds.centerY()
      );
      setPieRotation(getPieRotation() - (int)scrollTheta/FLING_VELOCITY_DOWNSCALE);
      return true;
    }

    @Override
    public boolean onDown(MotionEvent e){
      //The user is interacting with the pie , so we want to turn on acceleration
      //so that the interaction is smooth
      mPieView.accelerate();
      if (isAnimationRunning()){
        stopScrolling();
      }
      return true;
    }

    /**
     * Creating Physically Plausible Motion
     *
     * Gestures are a powerful tool for controlling touch, bt they can be counterintuitive and
     * difficult to remember unless they produce physically plausible results.
     * A good example of this is the fling gesture, where user quickly moves the finger across th screen
     * and then lifts it.
     * This gesture makes sense if the UI responds in the direction of the fling, theslowing down, as if the
     * user had pushed on a flywheel and set it spinning.
     *
     * Simulating the feel of a flywheel isn't trivial. A lot of physics and math are required to get a flywheel
     * model working correctly.
     * Fortunately Android provides helper classes to simulate this and other behaviors.
     * The {@link Scroller} is the basis for handling flywheel-style fling gesture.
     *
     * call fling() with the starting velocity and the minimum and maximum x and y values for the fling.
     * For velocity you can use the value computed by the {@link GestureDetector}
     *
     * Note: Although the velocity calculated by GestureDetector is physically accurate,
     * many developers feel that using this value makes the fling animation too fast. It's
     * common to divide the x and y velocity by a factor of 4 to 8.
     *
     * The fling() sets up the physics model for the fling gesture. Afterwards you need to
     * update the {@link Scroller} by calling {@link Scroller}.computeScrollOffset() at regular intervals.
     * computScrollOffset() updates objects's internal state by reading the current time and using the physics
     * model to calculate the x and y position at that time.
     * Call getCurrX() and getCurrY() to retrieve these values.
     *
     * Most views pass the {@link Scroller} object's x and y position directly to scrollTo().
     * PieChat is a little different in that it uses the current scroll y position to set
     * the rotation angle of the chart.
     *
     * {@link Scroller} computes the scroll positions for you but it does not automatically apply those
     * positions to your view.
     * It's your responsibility to make sure you get and apply new coordinates often enough to make the
     * scrolling animation look smooth.
     * There are two ways to do this:
     * ->postInvalidate() after calling fling(), in order to force redraw.The technique requires to
     * compute scroll offsets in onDraw() and call postInvalidate() every time the scroll offset changes.
     *
     * ->Set up a {@link ValueAnimator} to animate for the duration of the fling, and add a listener
     * to process animation updates animation updates by calling addUpdateListener().
     *
     * PieChat uses the second approach but it's slightly complex to set up, but works more closely
     * with the animation system and doesn't require potentially unnecessary view invalidation.
     * But the drawback being that {@link ValueAnimator} is not available prior to API level 11
     * so this technique cannot be used on devices running Android versions lower than 3.0
     *
     * Note: You can use ValueAnimator in applications that target lower API levels. You just need
     * to make sure to check the current API level at runtime, and omit the calls to the view
     * animation system if the current level is less than 11.
     * {@link PieChart} .init()
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
      //Set up the Scroller for a fling
      float scrollTheta = vectorToScalarScroll(
          velocityX,
          velocityY,
          e2.getX() - mPieBounds.centerX(),
          e2.getY() - mPieBounds.centerY()
      );

      mScroller.fling(0,
          (int) getPieRotation(),
          0,
          (int) scrollTheta/FLING_VELOCITY_DOWNSCALE,
          0,
          0,
          Integer.MIN_VALUE,
          Integer.MAX_VALUE);

      //start the animator and tell it to animate for the expected duration of the fling
      if (Build.VERSION.SDK_INT >= 11){
        mScrollAnimator.setDuration(mScroller.getDuration());
        mScrollAnimator.start();
      }
      return true;
    }

  }

  private boolean isAnimationRunning() {
    return !mScroller.isFinished() || (Build.VERSION.SDK_INT > 11 && mAutoCenterAnimator.isRunning());
  }

  /**
   * Helper method for translating (x,y) scroll vectors into scalar rotation of the pie.
   *
   * @Param dx The x component of the current scroll vector
   * @Param dy The y component of the current scroll vector
   * @Param x  The x position of the current touch, relative to the pie center.
   * @Param y  The y position of the current touch, relative to the pie center.
   * @return The scalar representing the change in angular position for this scroll.
   */
  private static float vectorToScalarScroll(float dx, float dy, float x , float y){
     //get the length of the vector
    float l = (float)Math.sqrt(dx * dx + dy *dy);

    //decide if the scalar should be negative or positive by finding
    //the dot product of the vector perpendicular to (x,y).
    float crossX = -y;
    float crossY = x;

    float dot = (crossX * dx + crossY * dy);
    float sign = Math.signum(dot);

    return l * sign;
  }
}
