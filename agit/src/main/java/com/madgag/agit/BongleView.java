package com.madgag.agit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import static com.madgag.agit.PinnedHeaderListView.PinnedHeaderAdapter.PINNED_HEADER_GONE;
import static com.madgag.agit.PinnedHeaderListView.PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
import static com.madgag.agit.PinnedHeaderListView.PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;

public class BongleView extends ExpandableListView implements AbsListView.OnScrollListener {
    private View mHeaderView;
    private int mHeaderViewWidth;
    private int mHeaderViewHeight;
    private boolean mHeaderViewVisible;
    private static final String TAG = "BONGLE";
    private static final int MAX_ALPHA = 255;
    private ExpandableListAdapter expAdapter;

    public BongleView(Context context) {
        super(context);
    }

    public BongleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BongleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        expAdapter = adapter;
        super.setAdapter(adapter);
        setOnScrollListener(this);
        stuffIt();
    }

    private void stuffIt() {
        setPinnedHeaderView(getAdapter().getView(0, null, this));
        //View mHeaderView = inflater.inflate(R.layout.list_section, list, false);
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;

        // Disable vertical fading when the pinned header is present
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }

        requestLayout();
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
            // mHeaderViewVisible = true;
        }
    }

    /**
     * Computes the state of the pinned header.  It can be invisible, fully
     * visible or partially pushed up out of the view.
     */
    public int getPinnedHeaderState(int position) {
//        if (mIndexer == null || mCursor == null || mCursor.getCount() == 0) {
//            return PINNED_HEADER_GONE;
//        }

        int realPosition = position; // getRealPosition(position);
        if (realPosition < 0) {
            return PINNED_HEADER_GONE;
        }

        if (!isGroupExpanded(getPackedPositionGroup(getExpandableListPosition(position)))) {
            return PINNED_HEADER_GONE;
        }

        // The header should get pushed up if the top item shown
        // is the last item in a group.

        if (getPackedPositionType(getExpandableListPosition(position+1)) == PACKED_POSITION_TYPE_GROUP) {
            return PINNED_HEADER_PUSHED_UP;
        }

        return PINNED_HEADER_VISIBLE;
    }


    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }

        int state = getPinnedHeaderState(position);
        switch (state) {
            case PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case PINNED_HEADER_VISIBLE: {
                configurePinnedHeader(mHeaderView, position, MAX_ALPHA);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                int bottom = firstView.getBottom();
                Log.d(TAG, "PINNED_HEADER_PUSHED_UP firstView="+firstView+" firstView.getBottom()="+bottom);
                int itemHeight = firstView.getHeight();
                int headerHeight = mHeaderView.getHeight();
                int y;
                int alpha;
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                Log.d(TAG, "configureHeaderView y="+y);
                configurePinnedHeader(mHeaderView, position, alpha);
                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
                }
                mHeaderViewVisible = true;
                break;
            }
        }
    }

    public void configurePinnedHeader(View header, int position, int alpha) {
//        PinnedHeaderCache cache = (PinnedHeaderCache)header.getTag();
//        if (cache == null) {
//            cache = new PinnedHeaderCache();
//            cache.titleView = (TextView)header.findViewById(R.id.header_text);
//            cache.textColor = cache.titleView.getTextColors();
//            cache.background = header.getBackground();
//            header.setTag(cache);
//        }

        int realPosition = position; // getRealPosition(position);
        int group = getPackedPositionGroup(getExpandableListPosition(realPosition));
        Log.d(TAG, "configurePinnedHeader group="+group);
        mHeaderView = expAdapter.getGroupView(group, false, mHeaderView, this); // TODO Performance

        //int section = getSectionForPosition(realPosition);

        //String title = (String)mIndexer.getSections()[section];
        //cache.titleView.setText(title);
        Drawable background = new ColorDrawable(Color.WHITE); // header.getBackground()
        if (alpha == MAX_ALPHA) {
            // Opaque: use the default background, and the original text color
            header.setBackgroundDrawable(background);
            // cache.titleView.setTextColor(cache.textColor); // cache.background
        } else {

            header.setBackgroundDrawable(background);

            // Faded: use a solid color approximation of the background, and
            // a translucent text color
//            header.setBackgroundColor(Color.rgb(
//                    Color.red(mPinnedHeaderBackgroundColor) * alpha / 255,
//                    Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
//                    Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));

//            int textColor = cache.textColor.getDefaultColor();
//            cache.titleView.setTextColor(Color.argb(alpha,
//                    Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        }
    }
                

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (view instanceof BongleView) {
            ((BongleView)view).configureHeaderView(firstVisibleItem);
        }
    }
}
