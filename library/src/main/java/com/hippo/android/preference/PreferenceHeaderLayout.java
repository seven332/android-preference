/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.android.preference;

/*
 * Created by Hippo on 5/8/2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

// TODO Use RecyclerView for complex layout

/**
 * A view to show preference header list.
 */
public class PreferenceHeaderLayout extends ListView {

  /**
   * Default value for {@link Header#id Header.id} indicating that no
   * identifier value is set.  All other values (including those below -1)
   * are valid.
   */
  public static final long HEADER_ID_UNDEFINED = -1;

  private ArrayList<Header> headers = new ArrayList<>();
  private int itemLayoutResId;
  private boolean removeIconIfEmpty;
  private HeaderAdapter adapter;

  public PreferenceHeaderLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public PreferenceHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, 0);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray ta =
        context.obtainStyledAttributes(attrs, R.styleable.PreferenceHeaderLayout, defStyleAttr, 0);

    // Read headers
    TypedValue tv = ta.peekValue(R.styleable.PreferenceHeaderLayout_headers);
    if (tv != null) {
      headers.addAll(loadHeadersFromResource(context, tv.resourceId));
    }

    // Read header item layout
    tv = ta.peekValue(R.styleable.PreferenceHeaderLayout_itemLayout);
    if (tv != null) {
      itemLayoutResId = tv.resourceId;
    } else {
      itemLayoutResId = R.layout.ap_preference_header_item;
    }

    ta.recycle();

    // Create adapter
    adapter = new HeaderAdapter(context);
    setAdapter(adapter);
  }

  /**
   * Sets the layout resource id of header item.
   * The layout must contain three elements:
   * <table>
   *   <tr>
   *     <th>Type</th><th>Id</th>
   *   </tr>
   *   <tr>
   *     <td>{@link ImageView}</td><td>{@link android.R.id#icon}</td>
   *   </tr>
   *   <tr>
   *     <td>{@link TextView}</td><td>{@link android.R.id#title}</td>
   *   </tr>
   *   <tr>
   *     <td>{@link TextView}</td><td>{@link android.R.id#summary}</td>
   *   </tr>
   * </table>
   */
  public void setItemLayoutResId(@LayoutRes int itemLayoutResId) {
    if (this.itemLayoutResId != itemLayoutResId) {
      this.itemLayoutResId = itemLayoutResId;
      adapter.notifyDataSetChanged();
    }
  }

  /**
   * Whether remove the icon view when the icon is {@code null}.
   */
  public void setRemoveIconIfEmpty(boolean removeIconIfEmpty) {
    if (this.removeIconIfEmpty != removeIconIfEmpty) {
      this.removeIconIfEmpty = removeIconIfEmpty;
      adapter.notifyDataSetChanged();
    }
  }

  /**
   * Parse the given XML file as a header description, adding each
   * parsed Header into the target list.
   */
  public static List<Header> loadHeadersFromResource(@NonNull Context context, @XmlRes int resId) {
    List<Header> headers = new ArrayList<>();
    XmlResourceParser parser = null;
    try {
      parser = context.getResources().getXml(resId);
      AttributeSet attrs = Xml.asAttributeSet(parser);

      int type;
      //noinspection StatementWithEmptyBody
      while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
          && type != XmlPullParser.START_TAG) {
        // Parse next until start tag is found
      }

      String nodeName = parser.getName();
      if (!"preference-headers".equals(nodeName)) {
        throw new RuntimeException(
            "XML document must start with <preference-headers> tag; found"
                + nodeName + " at " + parser.getPositionDescription());
      }

      final int outerDepth = parser.getDepth();
      while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
          && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
          continue;
        }

        nodeName = parser.getName();
        if ("header".equals(nodeName)) {
          Header header = new Header();

          TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PreferenceHeader);
          header.id = ta.getResourceId(R.styleable.PreferenceHeader_android_id,
              (int) HEADER_ID_UNDEFINED);
          header.title = ta.getString(R.styleable.PreferenceHeader_android_title);
          header.summary = ta.getString(R.styleable.PreferenceHeader_android_summary);
          header.iconRes = ta.getResourceId(R.styleable.PreferenceHeader_android_icon, 0);
          ta.recycle();

          headers.add(header);
        } else {
          skipCurrentTag(parser);
        }
      }

    } catch (XmlPullParserException e) {
      throw new RuntimeException("Error parsing headers", e);
    } catch (IOException e) {
      throw new RuntimeException("Error parsing headers", e);
    } finally {
      if (parser != null) parser.close();
    }
    return headers;
  }

  private static void skipCurrentTag(XmlPullParser parser)
      throws XmlPullParserException, IOException {
    int outerDepth = parser.getDepth();
    int type;
    //noinspection StatementWithEmptyBody
    while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
        && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
    }
  }

  /**
   * Description of a single Header item that the user can select.
   */
  public static final class Header {

    /**
     * Identifier for this header, to correlate with a new list when
     * it is updated. The default value is
     * {@link #HEADER_ID_UNDEFINED}, meaning no id.
     */
    public long id = HEADER_ID_UNDEFINED;

    /**
     * Title of the header that is shown to the user.
     */
    public CharSequence title;

    /**
     * Optional summary describing what this header controls.
     */
    public CharSequence summary;

    /**
     * Optional icon resource to show for this header.
     */
    public int iconRes;
  }

  private static class HeaderViewHolder {
    ImageView icon;
    TextView title;
    TextView summary;
  }

  private class HeaderAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    public HeaderAdapter(Context context) {
      inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
      return headers.size();
    }

    @Override
    public Object getItem(int position) {
      return headers.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      HeaderViewHolder holder;
      View view;

      if (convertView == null) {
        view = inflater.inflate(itemLayoutResId, parent, false);
        holder = new HeaderViewHolder();
        holder.icon = (ImageView) view.findViewById(android.R.id.icon);
        holder.title = (TextView) view.findViewById(android.R.id.title);
        holder.summary = (TextView) view.findViewById(android.R.id.summary);
        view.setTag(holder);
      } else {
        view = convertView;
        holder = (HeaderViewHolder) view.getTag();
      }

      // All view fields must be updated every time, because the view may be recycled
      Header header = headers.get(position);
      if (removeIconIfEmpty) {
        if (header.iconRes == 0) {
          holder.icon.setVisibility(View.GONE);
        } else {
          holder.icon.setVisibility(View.VISIBLE);
          holder.icon.setImageResource(header.iconRes);
        }
      } else {
        holder.icon.setImageResource(header.iconRes);
      }
      holder.title.setText(header.title);
      CharSequence summary = header.summary;
      if (!TextUtils.isEmpty(summary)) {
        holder.summary.setVisibility(View.VISIBLE);
        holder.summary.setText(summary);
      } else {
        holder.summary.setVisibility(View.GONE);
      }

      return view;
    }
  }
}
