/**
 * Copyright (C) 2014 Paul Cech
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eazegraph.lib.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import org.eazegraph.lib.R;
import org.eazegraph.lib.communication.IOnBarClickedListener;
import org.eazegraph.lib.models.BaseModel;
import org.eazegraph.lib.utils.Utils;

import java.util.List;

/**
 * The abstract class for every type of bar chart, which handles the general calculation for the bars.
 */
public abstract class BaseBarChart extends BaseChart {

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public BaseBarChart(Context context) {
        super(context);

        mBarWidth = Utils.dpToPx(DEF_BAR_WIDTH);
        mBarMargin = Utils.dpToPx(DEF_BAR_MARGIN);
        mFixedBarWidth = DEF_FIXED_BAR_WIDTH;
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public BaseBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.BaseBarChart, 0, 0);

        try {

            mBarWidth = a.getDimension(R.styleable.BaseBarChart_egBarWidth,
                    Utils.dpToPx(DEF_BAR_WIDTH));
            mBarMargin = a.getDimension(R.styleable.BaseBarChart_egBarMargin,
                    Utils.dpToPx(DEF_BAR_MARGIN));
            mFixedBarWidth =
                    a.getBoolean(R.styleable.BaseBarChart_egFixedBarWidth, DEF_FIXED_BAR_WIDTH);

        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

    }

    /**
     * Returns the onBarClickedListener.
     * @return
     */
    public IOnBarClickedListener getOnBarClickedListener() {
        return mListener;
    }

    /**
     * Sets the onBarClickedListener
     * @param _listener The listener which will be set.
     */
    public void setOnBarClickedListener(IOnBarClickedListener _listener) {
        mListener = _listener;
    }

    /**
     * Returns the width of a bar.
     * @return
     */
    public float getBarWidth() {
        return mBarWidth;
    }

    /**
     * Sets the width of bars.
     * @param _barWidth Width of bars
     */
    public void setBarWidth(float _barWidth) {
        mBarWidth = _barWidth;
        onDataChanged();
    }

    /**
     * Checks if the bars have a fixed width or is dynamically calculated.
     * @return
     */
    public boolean isFixedBarWidth() {
        return mFixedBarWidth;
    }

    /**
     * Sets if the bar width should be fixed or dynamically caluclated
     * @param _fixedBarWidth True if it should be a fixed width.
     */
    public void setFixedBarWidth(boolean _fixedBarWidth) {
        mFixedBarWidth = _fixedBarWidth;
        onDataChanged();
    }

    /**
     * Returns the bar margin, which is set by user if the bar widths are calculated dynamically.
     * @return
     */
    public float getBarMargin() {
        return mBarMargin;
    }

    /**
     * Sets the bar margin.
     * @param _barMargin Bar margin
     */
    public void setBarMargin(float _barMargin) {
        mBarMargin = _barMargin;
        onDataChanged();
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mGraph.layout(0, 0, w, (int) (h - mLegendHeight));
        mLegend.layout(0, (int) (h - mLegendHeight), w, h);

        if (getData().size() > 0) {
            onDataChanged();
        }
    }

    /**
     * Invalidates graph and legend and forces them to be redrawn.
     */
    protected void invalidateGraphs() {
        mGraph.invalidate();
        mLegend.invalidate();
    }

    /**
     * This is the main entry point after the graph has been inflated. Used to initialize the graph
     * and its corresponding members.
     */
    @Override
    protected void initializeGraph() {
        mGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphPaint.setStyle(Paint.Style.FILL);

        mLegendPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mLegendPaint.setColor(DEF_LEGEND_COLOR);
        mLegendPaint.setTextSize(mLegendTextSize);
        mLegendPaint.setStrokeWidth(2);
        mLegendPaint.setStyle(Paint.Style.FILL);

        mMaxFontHeight = Utils.calculateMaxTextHeight(mLegendPaint);

        mGraph = new Graph(getContext());
        addView(mGraph);

        mLegend = new Legend(getContext());
        addView(mLegend);

        mRevealAnimator = ValueAnimator.ofFloat(0, 1);
        mRevealAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRevealValue = (animation.getAnimatedFraction());
                mGraph.invalidate();
            }
        });
        mRevealAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mStartedAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    /**
     * Calculates the bar width and bar margin based on the _DataSize and settings and starts the boundary
     * calculation in child classes.
     * @param _DataSize Amount of data sets
     */
    protected void calculateBarPositions(int _DataSize) {
        if (_DataSize == 0) {
            // no need to calculate anything if there is no data
            // -> return avoids "java.lang.ArithmeticException: divide by zero"
            return;
        }

        float barWidth = mBarWidth;
        float margin = mBarMargin;

        if (!mFixedBarWidth) {
            // calculate the bar width if the bars should be dynamically displayed
            barWidth = (mGraphWidth / _DataSize) - margin;
        } else {
            // calculate margin between bars if the bars have a fixed width
            float cumulatedBarWidths = barWidth * _DataSize;
            float remainingWidth = mGraphWidth - cumulatedBarWidths;
            margin = remainingWidth / _DataSize;
        }

        calculateBounds(barWidth, margin);
        mLegend.invalidate();
        mGraph.invalidate();
    }

    /**
     * Calculates the bar boundaries based on the bar width and bar margin.
     * @param _Width    Calculated bar width
     * @param _Margin   Calculated bar margin
     */
    protected abstract void calculateBounds(float _Width, float _Margin);

    /**
     * Callback method for drawing the bars in the child classes.
     * @param _Canvas The canvas object of the graph view.
     */
    protected abstract void drawBars(Canvas _Canvas);

    /**
     * Returns the list of data sets which hold the information about the legend boundaries and text.
     * @return List of BaseModel data sets.
     */
    protected abstract List<? extends BaseModel> getLegendData();

    protected abstract List<RectF> getBarBounds();

    //##############################################################################################
    // Graph
    //##############################################################################################
    protected class Graph extends View {
        /**
         * Simple constructor to use when creating a view from code.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         */
        protected Graph(Context context) {
            super(context);
        }

        /**
         * Implement this to do your drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawBars(canvas);
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mGraphHeight = h - mTopPadding;
            mGraphWidth = w - mLeftPadding - mRightPadding;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean result = false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                performClick();
                result = true;

                if (mListener == null) {
                    // we're not interested in clicks on individual bars here
                    BaseBarChart.this.onTouchEvent(event);
                } else {
                    float newX = event.getX();
                    float newY = event.getY();
                    int counter = 0;

                    for (RectF rectF : getBarBounds()) {
                        if (Utils.intersectsPointWithRectF(rectF, newX, newY)) {
                            mListener.onBarClicked(counter);
                            break; // no need to check other bars
                        }
                        counter++;
                    }
                }
            }

            return result;
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

    }

    //##############################################################################################
    // Legend
    //##############################################################################################
    protected class Legend extends View {
        /**
         * Simple constructor to use when creating a view from code.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         */
        private Legend(Context context) {
            super(context);
        }

        /**
         * Implement this to do your drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for (BaseModel model : getLegendData()) {
                if (model.canShowLabel()) {
                    RectF bounds = model.getLegendBounds();
                    canvas.drawText(model.getLegendLabel(), model.getLegendLabelPosition(),
                            bounds.bottom - mMaxFontHeight, mLegendPaint);
                    canvas.drawLine(bounds.centerX(),
                            bounds.bottom - mMaxFontHeight * 2 - mLegendTopPadding,
                            bounds.centerX(), mLegendTopPadding, mLegendPaint);
                }
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

    }

    //##############################################################################################
    // Variables
    //##############################################################################################

    private static final String LOG_TAG = BaseBarChart.class.getSimpleName();

    // All float values are dp values and will be converted into px values in the constructor
    public static final float DEF_BAR_WIDTH = 32.f;
    public static final boolean DEF_FIXED_BAR_WIDTH = false;
    public static final float DEF_BAR_MARGIN = 12.f;

    protected IOnBarClickedListener mListener = null;

    protected Graph mGraph;
    protected Legend mLegend;

    protected Paint mGraphPaint;
    protected Paint mLegendPaint;

    protected float mBarWidth;
    protected boolean mFixedBarWidth;
    protected float mBarMargin;

}
