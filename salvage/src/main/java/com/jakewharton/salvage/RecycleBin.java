package com.jakewharton.salvage;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

/**
 * The recycle bin facilitates reuse of views across layouts. There are two levels of storage:
 * active views and scrap views. Active views are those views which are currently being used on
 * screen. Scrap views are old views that could potentially be used to avoid allocating views
 * unnecessarily.
 * <p>
 * This class was taken from Android's implementation of {@link android.widget.AbsListView} which
 * is copyrighted 2006 The Android Open Source Project.
 */
public class RecycleBin {
  /** Views that are currently in use by the consumer on screen. */
  private SparseArray<View> activeViews = new SparseArray<View>();
  private SparseIntArray activeViewTypes = new SparseIntArray();

  /** Unsorted views that can be used as a convert view. */
  private SparseArray<View>[] scrapViews;
  private SparseArray<View> currentScrapViews;

  private int viewTypeCount;

  public void setViewTypeCount(int viewTypeCount) {
    if (viewTypeCount < 1) {
      throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
    }
    //noinspection unchecked
    SparseArray<View>[] scrapViews = new SparseArray[viewTypeCount];
    for (int i = 0; i < viewTypeCount; i++) {
      scrapViews[i] = new SparseArray<View>();
    }
    this.viewTypeCount = viewTypeCount;
    currentScrapViews = scrapViews[0];
    this.scrapViews = scrapViews;
  }

  protected boolean shouldRecycleViewType(int viewType) {
    return viewType >= 0;
  }

  /** @return A view from the ScrapViews collection. These are unordered. */
  public View getScrapView(int position, int viewType) {
    if (viewTypeCount == 1) {
      return retrieveFromScrap(currentScrapViews, position);
    } else if (viewType >= 0 && viewType < scrapViews.length) {
      return retrieveFromScrap(scrapViews[viewType], position);
    }
    return null;
  }

  /**
   * Put a view into the scrap view list. These views are unordered.
   *
   * @param scrap The view to add
   * @param position The position for which this view was used.
   */
  public void addScrapView(View scrap, int position) {
    int viewType = activeViewTypes.get(position);

    if (viewTypeCount == 1) {
      currentScrapViews.put(position, scrap);
    } else {
      scrapViews[viewType].put(position, scrap);
    }

    scrap.setAccessibilityDelegate(null);
  }

  /**
   * Put a view into the active view list.
   *
   * @param active The view to add.
   * @param position The position at which this view is used.
   * @param viewType The view type for this position.
   */
  public void addActiveView(View active, int position, int viewType) {
    activeViews.put(position, active);
    activeViewTypes.put(position, viewType);
  }

  /** Move all views remaining in active views to scrap views. */
  public void scrapActiveViews() {
    final SparseArray<View> activeViews = this.activeViews;
    final SparseIntArray activeViewTypes = this.activeViewTypes;
    final boolean multipleScraps = viewTypeCount > 1;

    SparseArray<View> scrapViews = currentScrapViews;
    final int count = activeViews.size();
    for (int i = count - 1; i >= 0; i--) {
      final View victim = activeViews.get(i);
      if (victim != null) {
        int whichScrap = activeViewTypes.get(i);

        activeViews.delete(i);
        activeViewTypes.delete(i);

        if (!shouldRecycleViewType(whichScrap)) {
          continue;
        }

        if (multipleScraps) {
          scrapViews = this.scrapViews[whichScrap];
        }
        scrapViews.put(i, victim);

        victim.setAccessibilityDelegate(null);
      }
    }

    pruneScrapViews();
  }

  /**
   * Makes sure that the size of scrapViews does not exceed the size of activeViews.
   * (This can happen if an adapter does not recycle its views).
   */
  private void pruneScrapViews() {
    final int maxViews = activeViews.size();
    final int viewTypeCount = this.viewTypeCount;
    final SparseArray<View>[] scrapViews = this.scrapViews;
    for (int i = 0; i < viewTypeCount; ++i) {
      final SparseArray<View> scrapPile = scrapViews[i];
      int size = scrapPile.size();
      final int extras = size - maxViews;
      size--;
      for (int j = 0; j < extras; j++) {
        scrapPile.removeAt(size--);
      }
    }
  }

  static View retrieveFromScrap(SparseArray<View> scrapViews, int position) {
    int size = scrapViews.size();
    if (size > 0) {
      // See if we still have a view for this position.
      for (int i = 0; i < size; i++) {
        View view = scrapViews.get(i);
        int fromPosition = scrapViews.keyAt(i);
        if (fromPosition == position) {
          scrapViews.remove(i);
          return view;
        }
      }
      int index = size - 1;
      View r = scrapViews.valueAt(index);
      scrapViews.removeAt(index);
      return r;
    } else {
      return null;
    }
  }
}