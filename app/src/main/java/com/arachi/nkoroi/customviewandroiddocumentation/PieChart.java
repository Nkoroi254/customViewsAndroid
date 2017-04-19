package com.arachi.nkoroi.customviewandroiddocumentation;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

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

public class PieChart extends View {
  private boolean mShowText;
  private final int mTextPos;

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
      mShowText = a.getBoolean(R.styleable.PieChart_showText, false);
      mTextPos  = a.getInteger(R.styleable.PieChart_labelPositionNkoroi, 0);
    }finally {
      a.recycle();
    }
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
   */
  public void setShowText(boolean showText){
    mShowText = showText;
    invalidate();
    requestLayout();
  }
}
