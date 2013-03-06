package com.jakewharton.salvage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SalvageTestRunner.class)
public class RecyclingPagerAdapterTest {
  final Context context = new Activity();
  RecycleBin recycleBin;

  @Before public void setUp() {
    recycleBin = mock(RecycleBin.class);
  }

  @Test public void recycledViewIsNotAttemptedForIgnoredType() {
    LinearLayout container = new LinearLayout(context);
    final TextView child = new TextView(context);

    RecyclingPagerAdapter adapter = new RecyclingPagerAdapter(recycleBin) {
      @Override public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
      }

      @Override public View getView(int position, View convertView, ViewGroup container) {
        return child;
      }

      @Override public int getCount() {
        throw new AssertionError("getCount should not have been called.");
      }
    };
    verify(recycleBin).setViewTypeCount(1);

    adapter.instantiateItem(container, 0);
    verifyNoMoreInteractions(recycleBin);
  }

  @Test public void ignoredViewTypeIsNotRecycled() {
    LinearLayout container = new LinearLayout(context);
    TextView child = new TextView(context);
    container.addView(child);

    RecyclingPagerAdapter adapter = new RecyclingPagerAdapter(recycleBin) {
      @Override public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
      }

      @Override public View getView(int position, View convertView, ViewGroup container) {
        throw new AssertionError("getView should not have been called.");
      }

      @Override public int getCount() {
        throw new AssertionError("getCount should not have been called.");
      }
    };
    verify(recycleBin).setViewTypeCount(1);

    adapter.destroyItem(container, 0, child);
    verifyNoMoreInteractions(recycleBin);
  }
}
