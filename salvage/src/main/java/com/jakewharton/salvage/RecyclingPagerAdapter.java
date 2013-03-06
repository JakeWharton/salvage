package com.jakewharton.salvage;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * A {@link PagerAdapter} which behaves like an {@link android.widget.Adapter} with view types and
 * view recycling.
 */
public abstract class RecyclingPagerAdapter extends PagerAdapter {
  static final int IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE;

  private final RecycleBin recycleBin;

  public RecyclingPagerAdapter() {
    this(new RecycleBin());
  }

  RecyclingPagerAdapter(RecycleBin recycleBin) {
    this.recycleBin = recycleBin;
    recycleBin.setViewTypeCount(getViewTypeCount());
  }

  @Override public void notifyDataSetChanged() {
    recycleBin.scrapActiveViews();
    super.notifyDataSetChanged();
  }

  @Override public final Object instantiateItem(ViewGroup container, int position) {
    int viewType = getItemViewType(position);
    View view = null;
    if (viewType != IGNORE_ITEM_VIEW_TYPE) {
      view = recycleBin.getScrapView(position, viewType);
    }
    view = getView(position, view, container);
    container.addView(view);
    return view;
  }

  @Override public final void destroyItem(ViewGroup container, int position, Object object) {
    View view = (View) object;
    container.removeView(view);
    int viewType = getItemViewType(position);
    if (viewType != IGNORE_ITEM_VIEW_TYPE) {
      recycleBin.addScrapView(view, position, viewType);
    }
  }

  @Override public final boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  public int getViewTypeCount() {
    return 1;
  }

  @SuppressWarnings("UnusedParameters") // Argument potentially used by subclasses.
  public int getItemViewType(int position) {
    return 0;
  }

  public abstract View getView(int position, View convertView, ViewGroup container);
}
