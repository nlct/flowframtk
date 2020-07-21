// File          : ArrowStylePanel.java
// Description   : Panel for selecting marker style
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.flowframtk.dialog;

import java.text.DecimalFormat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Panel for selecting marker style.
 * @author Nicola L C Talbot
 */

public class ArrowStylePanel extends JPanel
   implements ActionListener,AdjustmentListener,ListSelectionListener
{
   public ArrowStylePanel(JDRSelector selector, int vertex)
   {
      super();
      selector_ = selector;
      type_ = vertex;

      setLayout(new BorderLayout());

      // composite panel

      JPanel compositePanel = new JPanel();
      compositePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      compositePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(compositePanel, "North");

      compositeButton = getResources().createAppCheckBox("arrow", 
        "composite", false, this);
      compositePanel.add(compositeButton);

      overlayButton = getResources().createAppCheckBox("arrow",
        "overlay", false, this);
      compositePanel.add(overlayButton);

      // marker panels

      markerPanel1 = new MarkerPanel(selector, this);
      markerPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
      markerPanel1.addListSelectionListener(this);
      markerPanel1.addAdjustmentListener(this);

      markerPanel2 = new MarkerPanel(selector, this);
      markerPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
      markerPanel2.addAdjustmentListener(this);

      tabbedPane = new JTabbedPane();

      tabbedPane.add(getResources().getString("arrow.primary")+" ",
                     new JScrollPane(markerPanel1));
      tabbedPane.setMnemonicAt(0,
         getResources().getCodePoint("arrow.primary.mnemonic"));
      tabbedPane.add(getResources().getString("arrow.secondary")+" ",
                     new JScrollPane(markerPanel2));
      tabbedPane.setMnemonicAt(1,
         getResources().getCodePoint("arrow.secondary.mnemonic"));

      add(tabbedPane, "Center");
   }

   public static String getTitle(JDRResources resources, int type)
   {
      String str = "Unspecified Marker";

      switch (type)
      {
         case ArrowStylePanel.START :
            str = resources.getString("linestyle.arrow.start");
         break;
         case ArrowStylePanel.MID :
            str = resources.getString("linestyle.arrow.mid");
         break;
         case ArrowStylePanel.END :
            str = resources.getString("linestyle.arrow.end");
         break;
         case ArrowStylePanel.ALL :
            str = resources.getString("linestyle.arrow.all");
      }

      return str;
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      selector_.repaintSample();
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == compositeButton)
      {
         updatePanel();
      }

      selector_.repaintSample();
   }

   public void valueChanged(ListSelectionEvent evt)
   {
      updatePanel();
   }

   public String getDescription()
   {
      String str = markerPanel1.getDescription();

      if (compositeButton.isSelected())
      {
         str += " "+markerPanel2.getDescription();
      }

      return str;
   }

   public JDRBasicStroke getStroke()
   {
      JDRFrame frame = selector_.application_.getCurrentFrame();

      JDRBasicStroke stroke = null;

      if (frame != null)
      {
         stroke = (JDRBasicStroke)frame.getSelectedStroke().clone();

      }

      if (stroke == null)
      {
         stroke = (JDRBasicStroke)selector_.getStroke();
      }

      JDRMarker marker = getMarker();
      
      switch (type_)
      {
         case START :
            stroke.setStartArrow(marker);
         break;
         case MID :
            stroke.setMidArrow(marker);
         break;
         case END :
            stroke.setEndArrow(marker);
         break;
         case ALL :
            stroke.setStartArrow(marker);
            stroke.setMidArrow(marker);
            stroke.setEndArrow(marker);
         break;
      }

      return stroke;
   }


   public void setStroke(JDRBasicStroke stroke)
   {
      switch (type_)
      {
         case START:
            setMarker(stroke.getStartArrow());
         break;
         case MID:
            setMarker(stroke.getMidArrow());
         break;
         case END :
            setMarker(stroke.getEndArrow());
         break;
         case ALL:
            setMarker(stroke.getStartArrow());
         break;
      }
   }

   public JDRMarker getMarker()
   {
      CanvasGraphics cg = selector_.getCanvasGraphics();

      JDRMarker marker = markerPanel1.getMarker(cg);

      if (compositeButton.isSelected())
      {
         JDRMarker compositeMarker = markerPanel2.getMarker(cg);

         if (compositeMarker.getType() == JDRMarker.ARROW_NONE)
         {
            return marker;
         }

         marker.setCompositeMarker(compositeMarker);
         marker.setOverlay(overlayButton.isSelected());
      }

      return marker;
   }

   public void updatePanel()
   {
      if (markerPanel1 == null || tabbedPane == null) return;

      JDRMarker marker = markerPanel1.getMarker(selector_.getCanvasGraphics());

      boolean noMarker;

      if (marker != null)
      {
         noMarker = (marker.getType()==JDRMarker.ARROW_NONE);
      }
      else
      {
         noMarker = true;
      }

      if (noMarker)
      {
         compositeButton.setSelected(false);
         overlayButton.setEnabled(false);
      }

      compositeButton.setEnabled(!noMarker);
      overlayButton.setEnabled(
          !noMarker && compositeButton.isSelected());

      if (compositeButton.isSelected())
      {
         tabbedPane.setEnabledAt(1, true);
      }
      else
      {
         tabbedPane.setSelectedIndex(0);
         tabbedPane.setEnabledAt(1, false);
      }
   }

   public void setMarker(JDRMarker marker)
   {
      markerPanel1.setMarker(marker);

      JDRMarker composite = marker.getCompositeMarker();
      if (composite==null)
      {
         compositeButton.setSelected(false);
         overlayButton.setEnabled(false);
         tabbedPane.setSelectedIndex(0);
         tabbedPane.requestFocus();
         tabbedPane.setEnabledAt(1, false);
      }
      else
      {
         compositeButton.setSelected(true);
         overlayButton.setEnabled(true);
         overlayButton.setSelected(marker.isOverlaid());
         tabbedPane.setEnabledAt(1, true);
         markerPanel2.setMarker(composite);
      }

      updatePanel();
   }

   public void setDefaults()
   {
      setMarker(new JDRMarker(getResources().getMessageDictionary()));
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;

   private MarkerPanel markerPanel1, markerPanel2;

   // is it a start/mid/end/all arrow?
   protected int type_;

   public static final int START=0, MID=1, END=2, ALL=3;

   // is it a composite arrow?
   private JCheckBox compositeButton;

   // should the composite marker overlap or be offset?
   private JCheckBox overlayButton;

   private JTabbedPane tabbedPane;
}
