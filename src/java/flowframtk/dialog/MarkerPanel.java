// File          : MarkerPanel.java
// Description   : Panel for selecting marker type
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting marker type.
 * @see ArrowStylePanel
 * @author Nicola L C Talbot
 */

public class MarkerPanel extends JPanel
   implements ActionListener,ListSelectionListener,
              ChangeListener
{
   public MarkerPanel(JDRSelector selector, ArrowStylePanel panel)
   {
      super();
      selector_ = selector;
      arrowStylePanel = panel;

      initMarkers();

      JPanel markerPanel = new JPanel();
      markerPanel.setLayout(new BoxLayout(markerPanel,
         BoxLayout.Y_AXIS));
      markerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      ButtonGroup markerGroup = new ButtonGroup();

      // no marker

      noMarkerButton = getResources().createAppRadioButton("arrow",
         "nomarker", markerGroup, true, this);

      noMarkerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      markerPanel.add(noMarkerButton);

      // use a marker

      useMarkerButton = getResources().createAppRadioButton("arrow",
         "usemarker", markerGroup, false, this);

      useMarkerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      markerPanel.add(useMarkerButton);

      // marker tab pane

      markerTabPane = new JTabbedPane();
      markerTabPane.setAlignmentX(Component.LEFT_ALIGNMENT);
      markerTabPane.addChangeListener(this);

      markerTabPane.setToolTipText(
         getResources().getString("tooltip.arrow.type"));

      // arrow style markers

      arrowStyleBox = new JList<MarkerItem>(arrowStyles);
      arrowStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      arrowStyleBox.addListSelectionListener(this);
      arrowStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane arrowSp = new JScrollPane(arrowStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.arrows")+" ", null, arrowSp,
         getResources().getToolTipText("arrow.type.arrows"));

      // partial arrows

      partialStyleBox = new JList<MarkerItem>(partialStyles);
      partialStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      partialStyleBox.addListSelectionListener(this);
      partialStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane partialSp = new JScrollPane(partialStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.partial")+" ", null, partialSp,
         getResources().getToolTipText("arrow.type.partial"));

      // data point styles

      dataStyleBox = new JList<MarkerItem>(dataStyles);
      dataStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      dataStyleBox.addListSelectionListener(this);
      dataStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane dataSp = new JScrollPane(dataStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.data")+" ", null, dataSp,
         getResources().getToolTipText("arrow.type.data"));

      // bracket style markers

      bracketStyleBox = new JList<MarkerItem>(bracketStyles);
      bracketStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      bracketStyleBox.addListSelectionListener(this);
      bracketStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane bracketSp = new JScrollPane(bracketStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.bracket")+" ", null, bracketSp,
         getResources().getToolTipText("arrow.type.bracket"));

      // circle/diamond style markers

      shapesStyleBox = new JList<MarkerItem>(shapesStyles);
      shapesStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      shapesStyleBox.addListSelectionListener(this);
      shapesStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane shapesSp = new JScrollPane(shapesStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.decorative")+" ", null, shapesSp,
         getResources().getToolTipText("arrow.type.decorative"));

      // cap style markers

      capStyleBox = new JList<MarkerItem>(capStyles);
      capStyleBox.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      capStyleBox.addListSelectionListener(this);
      capStyleBox.setCellRenderer(new MarkerItem());

      JScrollPane capSp = new JScrollPane(capStyleBox);

      markerTabPane.addTab(
         getResources().getString("arrow.tab.caps")+" ", null, capSp,
         getResources().getToolTipText("arrow.type.caps"));

      markerPanel.add(markerTabPane);

      add(markerPanel, "Left");

      JPanel settingsPanel = new JPanel();
      settingsPanel.setLayout(new BoxLayout(settingsPanel,
         BoxLayout.Y_AXIS));

      JPanel repeatPanel = new JPanel(
         new FlowLayout(FlowLayout.LEADING));
      settingsPanel.add(repeatPanel);

      arrowSize = getResources().createNonNegativeLengthPanel("arrow.size",
       selector_.getSamplePathPanel());

      arrowSize.setEnabled(false);
      arrowSize.setValue(5.0, JDRUnit.bp);
      repeatPanel.add(arrowSize);

      ButtonGroup repeatGroup = new ButtonGroup();

      // single arrow
      arrowSingle = getResources().createAppRadioButton("arrow", "single",
        repeatGroup, true, this);

      arrowSingle.setEnabled(false);
      repeatPanel.add(arrowSingle);

      // double arrow
      arrowDouble = getResources().createAppRadioButton("arrow", "double",
         repeatGroup, false, this);

      arrowDouble.setEnabled(false);
      repeatPanel.add(arrowDouble);

      // triple arrow
      arrowTriple = getResources().createAppRadioButton("arrow", "triple",
         repeatGroup, false, this);

      arrowTriple.setEnabled(false);
      repeatPanel.add(arrowTriple);

      // reversed arrow
      arrowReverse = getResources().createAppCheckBox("arrow", "reversed",
         false, this);

      arrowReverse.setEnabled(false);
      repeatPanel.add(arrowReverse);

      // orientation

      JPanel orientPanel = new JPanel();
      orientPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      settingsPanel.add(orientPanel);

      autoOrient = getResources().createAppCheckBox("arrow", "autoorient",
        true, this);

      orientPanel.add(autoOrient);

      angleField = getResources().createAnglePanel(0.0, JDRAngle.DEGREE);

      orientPanel.add(angleField);

      angleField.getDocument().addDocumentListener(
         new TextFieldSampleListener(selector_.getSamplePathPanel()));

      angleField.setToolTipText(
         getResources().getToolTipText("arrow.angle"));

      autoOrient.setEnabled(false);
      angleField.setEnabled(false);

      // user offset

      JPanel autoOffsetPanel = new JPanel();
      autoOffsetPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      settingsPanel.add(autoOffsetPanel);

      autoOffset = getResources().createAppCheckBox("arrow", "autooffset",
         true, this);

      autoOffsetPanel.add(autoOffset);

      offsetField = getResources().createLengthPanel(
         selector_.getSamplePathPanel());
      autoOffsetPanel.add(offsetField);

      offsetField.setToolTipText(getResources().getToolTipText("arrow.offset"));

      autoOffset.setEnabled(false);
      offsetField.setEnabled(false);

      // repeat gap

      JPanel gapPanel = new JPanel();
      gapPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      settingsPanel.add(gapPanel);

      autoGap = getResources().createAppCheckBox("arrow", "autogap",
         true, this);
      gapPanel.add(autoGap);
      autoGap.addActionListener(this);

      gapField = getResources().createLengthPanel(selector_.getSamplePathPanel());
      gapPanel.add(gapField);

      gapField.setToolTipText(
         getResources().getToolTipText("tooltip.arrow.gap"));

      autoGap.setEnabled(false);
      gapField.setEnabled(false);

      // Colour options

      JPanel colourOptionsPanel =
         new JPanel(new FlowLayout(FlowLayout.LEADING));
      settingsPanel.add(colourOptionsPanel);

      ButtonGroup colourGroup = new ButtonGroup();

      asLineButton = getResources().createAppRadioButton("arrow",
         "colour.dependent", colourGroup, true, this);

      //asLineButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      colourOptionsPanel.add(asLineButton);
      colourGroup.add(asLineButton);

      notAsLineButton = getResources().createAppRadioButton("arrow",
         "colour.independent", colourGroup, false, this);

      //notAsLineButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      colourOptionsPanel.add(notAsLineButton);
      colourGroup.add(notAsLineButton);

      colourPanel = new ColorPanel(getResources());
      colourPanel.setMnemonics(
         getResources().getChar("colour.rgb.mnemonic"),
         getResources().getChar("colour.cmyk.mnemonic"));

      //colourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(colourPanel);

      add(settingsPanel, "Center");

      setEnabled(true);
   }

   public void stateChanged(ChangeEvent e)
   {
      enableMarkerSettings();
      selector_.repaintSample();
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      colourPanel.addAdjustmentListener(al);
   }

   public void addListSelectionListener(ListSelectionListener lis)
   {
      arrowStyleBox.addListSelectionListener(lis);
      partialStyleBox.addListSelectionListener(lis);
      dataStyleBox.addListSelectionListener(lis);
      bracketStyleBox.addListSelectionListener(lis);
      shapesStyleBox.addListSelectionListener(lis);
      capStyleBox.addListSelectionListener(lis);
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      selector_.repaintSample();
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == autoOrient)
      {
         enableAngleBoxes();

         if (!autoOrient.isSelected())
         {
            angleField.requestValueFocus();
         }
      }
      else if (source == autoOffset)
      {
         enableOffsetBoxes();

         if (!autoOffset.isSelected())
         {
            offsetField.requestFocusInWindow();
         }
      }
      else if (source == autoGap)
      {
         enableGapBoxes();

         if (!autoGap.isSelected())
         {
            gapField.requestFocusInWindow();
         }
      }
      else if (source == arrowSingle)
      {
         autoGap.setEnabled(false);
         enableGapBoxes();
      }
      else if (source == arrowDouble || source == arrowTriple)
      {
         int type = getArrowStyle();

         boolean noMarker = (type == JDRMarker.ARROW_NONE);

         autoGap.setEnabled(!noMarker);
         enableGapBoxes();
      }
      else if (source == asLineButton)
      {
         colourPanel.setEnabled(false);
      }
      else if (source == notAsLineButton)
      {
         colourPanel.setEnabled(true);
      }
      else if (source == noMarkerButton)
      {
         enableMarkerBoxes(false);
         enableMarkerSettings();
      }
      else if (source == useMarkerButton)
      {
         enableMarkerBoxes(true);
         enableMarkerSettings();

         getSelectedList().requestFocusInWindow();
      }

      selector_.repaintSample();
   }

   public void valueChanged(ListSelectionEvent evt)
   {
      enableMarkerSettings();
      selector_.repaintSample();
   }

   public void enableMarkerSettings()
   {
      MarkerItem item = getSelectedMarker();

      boolean noMarker = (item.getType() == JDRMarker.ARROW_NONE);
      boolean isResizable = item.isResizable();

      arrowStylePanel.updatePanel();
      enableMarkerBoxes();

      if (arrowSize == null) return;

      arrowSize.setEnabled(isResizable);

      arrowSingle.setEnabled(!noMarker);

      arrowDouble.setEnabled(!noMarker);

      arrowTriple.setEnabled(!noMarker);

      arrowReverse.setEnabled(!noMarker);

      autoOrient.setEnabled(!noMarker);
      enableAngleBoxes();

      autoOffset.setEnabled(!noMarker);
      enableOffsetBoxes();

      autoGap.setEnabled(!noMarker && !arrowSingle.isSelected());
      enableGapBoxes();

      asLineButton.setEnabled(!noMarker);
      notAsLineButton.setEnabled(!noMarker);

      colourPanel.setEnabled(
         !noMarker && notAsLineButton.isSelected());

   }

   private void colourButtonsSetEnabled(boolean flag)
   {
      asLineButton.setEnabled(flag);
      notAsLineButton.setEnabled(flag);
      colourPanel.setEnabled(flag && notAsLineButton.isSelected());
   }

   public void setEnabled(boolean flag)
   {
      MarkerItem marker = getSelectedMarker();

      int type =
         (marker == null ? JDRMarker.ARROW_NONE : marker.getType());

      boolean noMarker = (type == JDRMarker.ARROW_NONE);

      boolean isResizable =
         (marker == null ? false : marker.isResizable());

      useMarkerButton.setEnabled(flag);
      noMarkerButton.setEnabled(flag);
      enableMarkerBoxes(flag);

      arrowSize.setEnabled(flag && isResizable);
      arrowSingle.setEnabled(flag && !noMarker);
      arrowDouble.setEnabled(flag && !noMarker);
      arrowTriple.setEnabled(flag && !noMarker);
      arrowReverse.setEnabled(flag && !noMarker);
      autoOrient.setEnabled(flag && !noMarker);
      enableAngleBoxes();

      asLineButton.setEnabled(!noMarker);
      notAsLineButton.setEnabled(!noMarker);
      colourPanel.setEnabled(
         !noMarker && notAsLineButton.isSelected());
   }

   private void enableMarkerBoxes()
   {
      enableMarkerBoxes(true);
   }

   private void enableMarkerBoxes(boolean flag)
   {
      JScrollPane sp 
         = (JScrollPane)markerTabPane.getSelectedComponent();

      if (!flag)
      {
         sp.getViewport().getView().setEnabled(flag);
         sp.setEnabled(flag);
         markerTabPane.setEnabled(flag);
      }

      boolean selected = useMarkerButton.isSelected();

      JList<MarkerItem> list = getSelectedList(sp);
      list.setEnabled(selected);
      sp.setEnabled(selected);
      markerTabPane.setEnabled(selected);

      if (selected && list.getSelectedIndex()==-1)
      {
         list.setSelectedIndex(0);
      }
   }

   private void enableAngleBoxes()
   {
      if (!autoOrient.isEnabled())
      {
         angleField.setEnabled(false);
         return;
      }

      boolean orient = autoOrient.isSelected();

      angleField.setEnabled(!orient);
   }

   private void enableOffsetBoxes()
   {
      if (!autoOffset.isEnabled())
      {
         offsetField.setEnabled(false);
         return;
      }

      boolean selected = autoOffset.isSelected();

      offsetField.setEnabled(!selected);
   }

   private void enableGapBoxes()
   {
      if (!autoGap.isEnabled())
      {
         gapField.setEnabled(false);
         return;
      }

      boolean selected = autoGap.isSelected();

      gapField.setEnabled(!selected);
   }

   public void setArrowColour(JDRPaint paint)
   {
      if (paint == null)
      {
         asLineButton.setSelected(true);
         colourPanel.setPaint(selector_.getLinePaint());
         colourPanel.setEnabled(false);
      }
      else
      {
         notAsLineButton.setSelected(true);
         colourPanel.setPaint(paint);
         colourPanel.setEnabled(true);
      }
   }

   public JDRPaint getArrowColour(CanvasGraphics cg)
   {
      if (asLineButton.isSelected())
      {
         return null;
      }
      else
      {
         return colourPanel.getPaint(cg);
      }
   }

   @SuppressWarnings("unchecked")
   public JList<MarkerItem> getSelectedList(JScrollPane sp)
   {
      return (JList<MarkerItem>)sp.getViewport().getView();
   }


   public JList<MarkerItem> getSelectedList()
   {
      return getSelectedList((JScrollPane)markerTabPane.getSelectedComponent());
   }

   public MarkerItem getSelectedMarker()
   {
      if (noMarkerButton.isSelected())
      {
         return noMarkerItem;
      }

      JList<MarkerItem> list = getSelectedList();

      MarkerItem item = (MarkerItem)list.getSelectedValue();

      if (item == null)
      {
         list.setSelectedIndex(0);
         item = list.getSelectedValue();
      }

      return item;
   }

   public int getArrowStyle()
   {
      MarkerItem markerItem = getSelectedMarker();

      if (markerItem == null) return JDRMarker.ARROW_NONE;

      return markerItem.getType();
   }

   public void setArrowStyle(int style)
   {
      if (style == JDRMarker.ARROW_NONE)
      {
         noMarkerButton.setSelected(true);
         enableMarkerSettings();
         return;
      }

      useMarkerButton.setSelected(true);
      enableMarkerSettings();

      for (int i = 0; i < arrowStyles.length; i++)
      {
         MarkerItem item = arrowStyles[i];

         if (item.getType() == style)
         {
            arrowStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(ARROW_TAB);

            return;
         }
      }

      for (int i = 0; i < partialStyles.length; i++)
      {
         MarkerItem item = partialStyles[i];

         if (item.getType() == style)
         {
            partialStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(PARTIAL_TAB);

            return;
         }
      }

      for (int i = 0; i < dataStyles.length; i++)
      {
         MarkerItem item = dataStyles[i];

         if (item.getType() == style)
         {
            dataStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(DATA_TAB);

            return;
         }
      }

      for (int i = 0; i < bracketStyles.length; i++)
      {
         MarkerItem item = bracketStyles[i];

         if (item.getType() == style)
         {
            bracketStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(BRACKET_TAB);

            return;
         }
      }

      for (int i = 0; i < shapesStyles.length; i++)
      {
         MarkerItem item = shapesStyles[i];

         if (item.getType() == style)
         {
            shapesStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(SHAPES_TAB);

            return;
         }
      }

      for (int i = 0; i < capStyles.length; i++)
      {
         MarkerItem item = capStyles[i];

         if (item.getType() == style)
         {
            capStyleBox.setSelectedIndex(i);
            markerTabPane.setSelectedIndex(CAP_TAB);

            return;
         }
      }
   }

   public JDRLength getArrowSize()
   {
      return arrowSize.getLength();
   }

   public int getArrowRepeated()
   {
      if (arrowDouble.isSelected()) return 2;
      if (arrowTriple.isSelected()) return 3;

      return 1;
   }

   public void setArrowRepeated(int repeated)
   {
      if (repeated==3)
      {
         arrowTriple.setSelected(true);
      }
      else if (repeated==2)
      {
         arrowDouble.setSelected(true);
      }
      else
      {
         arrowSingle.setSelected(true);
      }
   }

   public boolean getArrowReverse()
   {
      return arrowReverse.isSelected();
   }

   public void setArrowReverse(boolean isReverseArrow)
   {
      arrowReverse.setSelected(isReverseArrow);
   }

   public void setArrowSize(JDRLength width)
   {
      arrowSize.setLength(width);
   }

   public void setArrowSize(double value, JDRUnit unit)
   {
      arrowSize.setValue(value, unit);
   }

   public JDRMarker getMarker(CanvasGraphics cg)
   {
      JDRMarker marker = JDRMarker.getPredefinedMarker(cg,
         getArrowStyle(),
         new JDRLength(getResources().getMessageDictionary(), 1.0, JDRUnit.bp),
         getArrowRepeated(),
         getArrowReverse(),
         getArrowSize());

      marker.setFillPaint(getArrowColour(cg));

      if (!autoOrient.isSelected())
      {
         marker.setOrient(false, angleField.getValue());
      }

      if (!autoOffset.isSelected())
      {
         marker.enableUserOffset(true);
         marker.setOffset(offsetField.getLength());
      }

      if (!autoGap.isSelected())
      {
         marker.enableUserRepeatOffset(true);
         marker.setRepeatOffset(gapField.getLength());
      }

      return marker;
   }

   public void setMarker(JDRMarker marker)
   {
      boolean noMarker = (marker.getType() == JDRMarker.ARROW_NONE);

      CanvasGraphics cg = marker.getCanvasGraphics();

      setArrowStyle(marker.getType());
      setArrowRepeated(marker.getRepeated());
      setArrowReverse(marker.isReversed());

      if (marker.isResizable())
      {
         setArrowSize(marker.getSize());
      }

      setArrowColour(marker.fillPaint);

      arrowSize.setEnabled(marker.isResizable());
      arrowSingle.setEnabled(!noMarker);
      arrowDouble.setEnabled(!noMarker);
      arrowTriple.setEnabled(!noMarker);
      arrowReverse.setEnabled(!noMarker);

      boolean orient = marker.getAutoOrient();

      autoOrient.setSelected(orient);
      autoOrient.setEnabled(!noMarker);
      enableAngleBoxes();

      if (!orient)
      {
         angleField.setValue(marker.getAngle());
      }

      boolean offsetEnabled = marker.isUserOffsetEnabled();

      autoOffset.setSelected(!offsetEnabled);
      autoOffset.setEnabled(!noMarker);
      enableOffsetBoxes();

      if (offsetEnabled)
      {
         offsetField.setLength(marker.getOffset());
      }

      boolean repeatEnabled = marker.isUserRepeatOffsetEnabled();

      autoGap.setSelected(!repeatEnabled);
      autoGap.setEnabled(!noMarker && !arrowSingle.isSelected());
      enableGapBoxes();
 
      if (repeatEnabled)
      {
         gapField.setLength(marker.getRepeatOffset());
      }
   }

   public String getDescription()
   {
      MarkerItem item = getSelectedMarker();

      String str = item.getLabel();

      if (item.getType() == JDRMarker.ARROW_NONE) return str;

      if (item.isResizable())
      {
         str += " "+getArrowSize();
      }

      if (getArrowRepeated()==2)
      {
         str += " "+getResources().getString("arrow.double");
      }
      else if (getArrowRepeated()==3)
      {
         str += " "+getResources().getString("arrow.triple");
      }


      if (getArrowReverse())
      {
         str += " "+getResources().getString("arrow.reversed");
      }

      return str;
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private void initMarkers()
   {
      if (noMarkerItem != null) return;

      noMarkerItem = new MarkerItem(getResources(), JDRMarker.ARROW_NONE,
         getResources().getString("arrow.none"));

      arrowStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_POINTED,
            getResources().getString("arrow.pointed")),
         new MarkerItem(getResources(), JDRMarker.ARROW_POINTED60,
            getResources().getString("arrow.pointed60")),
         new MarkerItem(getResources(), JDRMarker.ARROW_POINTED45,
            getResources().getString("arrow.pointed45")),
         new MarkerItem(getResources(), JDRMarker.ARROW_CUSP,
            getResources().getString("arrow.cusp")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SINGLE,
            getResources().getString("arrow.latex")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_SINGLE,
            getResources().getString("arrow.altlatex")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_SINGLE_OPEN,
            getResources().getString("arrow.altlatex.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE,
            getResources().getString("arrow.triangle")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_OPEN,
            getResources().getString("arrow.triangle.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_EQUILATERAL_FILLED,
            getResources().getString("arrow.equilateral.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_EQUILATERAL_OPEN,
            getResources().getString("arrow.equilateral.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HOOKS,
            getResources().getString("arrow.hooks"))
      };

      partialStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_HOOK_UP,
            getResources().getString("arrow.hook.up")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HOOK_DOWN,
            getResources().getString("arrow.hook.down")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED_UP,
            getResources().getString("arrow.halfpointed.up")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED_DOWN,
            getResources().getString("arrow.halfpointed.down")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED60_UP,
            getResources().getString("arrow.halfpointed60.up")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED60_DOWN,
            getResources().getString("arrow.halfpointed60.down")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED45_UP,
            getResources().getString("arrow.halfpointed45.up")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_POINTED45_DOWN,
            getResources().getString("arrow.halfpointed45.down")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_CUSP_UP,
            getResources().getString("arrow.halfcusp.up")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HALF_CUSP_DOWN,
            getResources().getString("arrow.halfcusp.down"))
      };

      bracketStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_SQUARE,
            getResources().getString("arrow.square")),
         new MarkerItem(getResources(), JDRMarker.ARROW_BAR,
            getResources().getString("arrow.bar")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ROUND,
            getResources().getString("arrow.round")),
         new MarkerItem(getResources(), JDRMarker.ARROW_BRACE,
            getResources().getString("arrow.brace")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_SQUARE,
            getResources().getString("arrow.altsquare")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_BAR,
            getResources().getString("arrow.altbar")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_ROUND,
            getResources().getString("arrow.altround")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ALT_BRACE,
            getResources().getString("arrow.altbrace"))
      };

      shapesStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_CIRCLE,
            getResources().getString("arrow.circle")),
         new MarkerItem(getResources(), JDRMarker.ARROW_DIAMOND,
            getResources().getString("arrow.diamond")),
         new MarkerItem(getResources(), JDRMarker.ARROW_CIRCLE_OPEN,
            getResources().getString("arrow.circle.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_DIAMOND_OPEN,
            getResources().getString("arrow.diamond.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SCISSORS_UP_FILLED,
            getResources().getString("arrow.scissors.up.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SCISSORS_DOWN_FILLED,
            getResources().getString("arrow.scissors.down.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SCISSORS_UP_OPEN,
            getResources().getString("arrow.scissors.up.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SCISSORS_DOWN_OPEN,
            getResources().getString("arrow.scissors.down.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEART_RIGHT_FILLED,
            getResources().getString("arrow.heart.right.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEART_RIGHT_OPEN,
            getResources().getString("arrow.heart.right.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEART_FILLED,
            getResources().getString("arrow.heart.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEART_OPEN,
            getResources().getString("arrow.heart.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SNOWFLAKE,
            getResources().getString("arrow.snowflake")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR_CHEVRON_OPEN,
            getResources().getString("arrow.starchevron.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR_CHEVRON_FILLED,
            getResources().getString("arrow.starchevron.filled"))
      };

      dataStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_DOTFILLED,
            getResources().getString("arrow.dotfilled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_DOTOPEN,
            getResources().getString("arrow.dotopen")),
         new MarkerItem(getResources(), JDRMarker.ARROW_BOXFILLED,
            getResources().getString("arrow.boxfilled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_BOXOPEN,
            getResources().getString("arrow.boxopen")),
         new MarkerItem(getResources(), JDRMarker.ARROW_CROSS,
            getResources().getString("arrow.cross")),
         new MarkerItem(getResources(), JDRMarker.ARROW_PLUS,
            getResources().getString("arrow.plus")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR,
            getResources().getString("arrow.star")),
         new MarkerItem(getResources(), JDRMarker.ARROW_ASTERISK,
            getResources().getString("arrow.asterisk")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR5_FILLED,
            getResources().getString("arrow.star5.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR5_OPEN,
            getResources().getString("arrow.star5.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR6_FILLED,
            getResources().getString("arrow.star6.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_STAR6_OPEN,
            getResources().getString("arrow.star6.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_UP_FILLED,
            getResources().getString("arrow.triangle.up.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_UP_OPEN,
            getResources().getString("arrow.triangle.up.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_DOWN_FILLED,
            getResources().getString("arrow.triangle.down.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_DOWN_OPEN,
            getResources().getString("arrow.triangle.down.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_RHOMBUS_FILLED,
            getResources().getString("arrow.rhombus.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_RHOMBUS_OPEN,
            getResources().getString("arrow.rhombus.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_PENTAGON_FILLED,
            getResources().getString("arrow.pentagon.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_PENTAGON_OPEN,
            getResources().getString("arrow.pentagon.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEXAGON_FILLED,
            getResources().getString("arrow.hexagon.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_HEXAGON_OPEN,
            getResources().getString("arrow.hexagon.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_OCTAGON_FILLED,
            getResources().getString("arrow.octagon.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_OCTAGON_OPEN,
            getResources().getString("arrow.octagon.open")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SEMICIRCLE_FILLED,
            getResources().getString("arrow.semicircle.filled")),
         new MarkerItem(getResources(), JDRMarker.ARROW_SEMICIRCLE_OPEN,
            getResources().getString("arrow.semicircle.open"))
      };

      capStyles = new MarkerItem[]
      {
         new MarkerItem(getResources(), JDRMarker.ARROW_RECTANGLE_CAP,
            getResources().getString("arrow.rectanglecap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_ROUND_CAP,
            getResources().getString("arrow.roundcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_TRIANGLE_CAP,
            getResources().getString("arrow.trianglecap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_INVERT_TRIANGLE_CAP,
            getResources().getString("arrow.inverttrianglecap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_CHEVRON_CAP,
            getResources().getString("arrow.chevroncap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_INVERT_CHEVRON_CAP,
            getResources().getString("arrow.invertchevroncap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_FAST_CAP,
            getResources().getString("arrow.fastcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_INVERT_FAST_CAP,
            getResources().getString("arrow.invertfastcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_BALL_CAP,
            getResources().getString("arrow.ballcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF_CAP,
            getResources().getString("arrow.leafcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF2_CAP,
            getResources().getString("arrow.doubleleafcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF3_CAP,
            getResources().getString("arrow.tripleleafcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_CLUB_CAP,
            getResources().getString("arrow.clubcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF3FOR_CAP,
            getResources().getString("arrow.tripleleafforwardcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF3BACK_CAP,
            getResources().getString("arrow.tripleleafbackcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF2FOR_CAP,
            getResources().getString("arrow.doubleleafforwardcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_LEAF2BACK_CAP,
            getResources().getString("arrow.doubleleafbackcap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_BULGE_CAP,
            getResources().getString("arrow.bulgecap"), 10, 50),
         new MarkerItem(getResources(), JDRMarker.ARROW_CUTOUTBULGE_CAP,
            getResources().getString("arrow.cutoutbulgecap"), 10, 50),
      };
   }

   private JDRSelector selector_;

   // styles
   private JList<MarkerItem> arrowStyleBox, partialStyleBox, dataStyleBox,
      bracketStyleBox, shapesStyleBox, capStyleBox;

   private static final int ARROW_TAB=0, PARTIAL_TAB=1, DATA_TAB=2,
      BRACKET_TAB=3, SHAPES_TAB=4, CAP_TAB=5;

   private JTabbedPane markerTabPane;

   private JRadioButton noMarkerButton, useMarkerButton;

   // arrow size
   private NonNegativeLengthPanel arrowSize;

   // arrow single/double/triple?
   private JRadioButton arrowSingle, arrowDouble, arrowTriple;

   // arrow reversed?
   private JCheckBox arrowReverse;

   // auto orientation ?
   private JCheckBox autoOrient;
   private AnglePanel angleField;

   // auto offset?
   private JCheckBox autoOffset;
   private LengthPanel offsetField;

   // auto repeat gap?
   private JCheckBox autoGap;
   private LengthPanel gapField;

   // does the arrow have a colour independent to the line colour?

   private JRadioButton asLineButton, notAsLineButton;

   private ColorPanel colourPanel;

   public static MarkerItem noMarkerItem = null;

   public static MarkerItem[] arrowStyles;

   public static MarkerItem[] partialStyles;

   public static MarkerItem[] bracketStyles;

   public static MarkerItem[] shapesStyles;

   public static MarkerItem[] dataStyles;

   public static MarkerItem[] capStyles;

   private ArrowStylePanel arrowStylePanel;
}

class MarkerItem implements ListCellRenderer<MarkerItem>
{
   public MarkerItem(JDRResources resources, int markerType, String markerLabel,
                     int penWidth)
   {
      this(resources, markerType, markerLabel, 
           new JDRLength(resources.getMessageSystem(), penWidth, JDRUnit.bp));
   }

   public MarkerItem(JDRResources resources, int markerType, String markerLabel,
                     JDRLength penWidth)
   {
      this(resources, markerType, markerLabel, penWidth, 24);
   }

   public MarkerItem(JDRResources resources, int markerType, String markerLabel,
                     int penWidth, int maxWidth)
   {
      this(resources, markerType, markerLabel, 
           new JDRLength(resources.getMessageSystem(), penWidth, JDRUnit.bp),
           maxWidth);
   }

   public MarkerItem(JDRResources resources, int markerType, String markerLabel,
                     JDRLength penWidth, int maxWidth)
   {
      type = markerType;
      label = markerLabel;

      CanvasGraphics cg = new CanvasGraphics(resources.getMessageSystem());

      JDRMarker marker = JDRMarker.getPredefinedMarker(cg, markerType,
         penWidth, 1, false, 
         new JDRLength(resources.getMessageDictionary(), 5.0, JDRUnit.bp));

      resizable = marker.isResizable();

      if (type != JDRMarker.ARROW_NONE)
      {
         GeneralPath path = marker.getGeneralPath();

         Rectangle bounds = path.getBounds();

         BufferedImage image 
            = new BufferedImage(maxWidth, (int)bounds.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

         RenderingHints renderHints =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);

         renderHints.add(new RenderingHints(
                           RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY));

         double halfHeight = 0.5*bounds.getHeight();

         Graphics2D g = image.createGraphics();

         try
         {
            g.setRenderingHints(renderHints);
            g.translate(-bounds.getX(), halfHeight);

            g.setPaint(Color.black);
            g.fill(path);
         }
         finally
         {
            g.dispose();
         }

         ic = new ImageIcon(image);

         image 
            = new BufferedImage(maxWidth,(int)bounds.getHeight(),
                 BufferedImage.TYPE_INT_ARGB);

         path = marker.getGeneralPath();

         g = image.createGraphics();

         g.translate(-bounds.getX(), halfHeight);

         g.setPaint(disabledForeground);
         g.fill(path);

         g.dispose();

         disabledIc = new ImageIcon(image);
      }
   }

   public MarkerItem(JDRResources resources, int markerType, String markerLabel)
   {
      this(resources, markerType, markerLabel, 
         new JDRLength(resources.getMessageDictionary(), 1.0, JDRUnit.bp));
   }

   public MarkerItem()
   {
      type = JDRMarker.ARROW_NONE;
      label = "";
      panel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            if (cellHasFocus)
            {
               Rectangle bounds = getBounds();
               int w = (int)bounds.getWidth();
               int h = (int)bounds.getHeight();

               bounds.setBounds(1,1,w-2,h-2);

               Graphics2D g2 = (Graphics2D)g;

               Paint oldPaint = g2.getPaint();

               g2.setPaint(Color.darkGray);

               g2.draw(bounds);

               g2.setPaint(oldPaint);
            }
         }
      };

      panel.setLayout(new FlowLayout(FlowLayout.LEADING));
      iconComp = new JLabel();
      textComp = new JLabel();
      textComp.setOpaque(false);
      iconComp.setOpaque(false);
      panel.add(iconComp);
      panel.add(textComp);
   }

   public Component getListCellRendererComponent(JList<? extends MarkerItem> list,
      MarkerItem value, int index, boolean isSelected,
      boolean hasCellFocus)
   {
      MarkerItem item = (MarkerItem)value;

      textComp.setText(item.getLabel()+" ");

      if (!list.isEnabled())
      {
         panel.setBackground(disabledBackground);
         panel.setForeground(disabledForeground);
         iconComp.setIcon(item.disabledIc);
      }
      else if (isSelected)
      {
         panel.setBackground(list.getSelectionBackground());
         panel.setForeground(list.getSelectionForeground());
         iconComp.setIcon(item.ic);
      }
      else
      {
         panel.setBackground(list.getBackground());
         panel.setForeground(list.getForeground());
         iconComp.setIcon(item.ic);
      }

      cellHasFocus = hasCellFocus;
      panel.setEnabled(list.isEnabled());
      panel.setFont(list.getFont());
      panel.setOpaque(true);

      return panel;
   }

   public int getType()
   {
      return type;
   }

   public String getLabel()
   {
      return label;
   }

   public boolean isResizable()
   {
      return resizable;
   }

   public String toString()
   {
      return "MarkerItem [type="+type+", label="+label+"]";
   }

   private int type;
   private String label;
   private Icon ic=null, disabledIc=null;
   private JLabel iconComp=null;
   private JLabel textComp=null;
   private JPanel panel=null;
   private boolean resizable = false;

   private boolean cellHasFocus=false;

   private static Color disabledBackground = Color.lightGray;
   private static Color disabledForeground = Color.gray;
}

