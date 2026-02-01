/*
    Copyright (C) 2026 Nicola L.C. Talbot

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

import java.util.Vector;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for clipping shapes.
 * @author Nicola L C Talbot
 */
public class ClipDialog extends JDialog
 implements ActionListener, JDRApp, ChangeListener
{
   public ClipDialog(FlowframTk application)
   {
      super(application, application.getResources().getMessage("clip.title"), true);
      this.application = application;

      init();
   }

   private void init()
   {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
       {
          public void windowClosing(WindowEvent evt)
          {
             cancel();
          }
       });

      objects = new Vector<OldNewObject>();

      JDRResources resources = application.getResources();

      JComponent mainComp = new JPanel(new BorderLayout()); 
      getContentPane().add(mainComp, "Center");

      samplePanel = new ClipSamplePanel(this);
      mainComp.add(new JScrollPane(samplePanel), "Center");

      JComponent widgetPanel = Box.createVerticalBox();
      mainComp.add(widgetPanel, "South");

      widgetPanel.setBorder(BorderFactory.createTitledBorder(
       BorderFactory.createEtchedBorder(), resources.getMessage("clip.bounds")));

      JComponent row = createRow();
      widgetPanel.add(row);

      row.add(resources.createAppLabel("clip.bounds.pos"));

      boundsPositionPanel = new LinkedLengthsPanel(resources, "clip.bounds.pos");
      row.add(boundsPositionPanel);
      boundsPositionPanel.addChangeListener(this);

      row = createRow();
      widgetPanel.add(row);

      row.add(resources.createAppLabel("clip.bounds.size"));

      boundsSizePanel = new LinkedLengthsPanel(resources, "clip.bounds.size");
      row.add(boundsSizePanel);
      boundsSizePanel.addChangeListener(this);

      doTaskButton = resources.createDialogButton(
        "clip", "dotask", this, null);
      widgetPanel.add(doTaskButton);

      JComponent rightComp = Box.createVerticalBox();
      mainComp.add(rightComp, "East");

      JComponent bottomPanel = new JPanel(new BorderLayout());
      getContentPane().add(bottomPanel, "South");

      JComponent buttonPanel = new JPanel();
      bottomPanel.add(buttonPanel, "Center");

      okayButton = resources.createOkayCancelHelpButtons(this, buttonPanel,
        this, "sec:clipping", false);

      getRootPane().setDefaultButton(doTaskButton);

      zoomComp = new ZoomComponent(this);
      bottomPanel.add(zoomComp, "West");

      resetButton = resources.createDialogButton(
        "clip", "reload", this, null);
      bottomPanel.add(resetButton, "East");

      String okayText = okayButton.getText();

      if (okayText.isEmpty())
      {
         okayText = okayButton.getToolTipText();
      }

      String doTaskText = doTaskButton.getText();

      if (doTaskText.isEmpty())
      {
         doTaskText = doTaskButton.getToolTipText();
      }

      infoText = String.format("%s%n%n",
         resources.getMessage("message.clip.info", doTaskText, okayText));

      infoArea = resources.createAppInfoArea(24);

      infoArea.setText(infoText);
      rightComp.add(new JScrollPane(infoArea));

      pack();
      setLocationRelativeTo(application);
   }

   private JComponent createRow()
   {
      JComponent comp = Box.createHorizontalBox();
      comp.setAlignmentX(0.0f);
      return comp;
   }

   @Override
   public JDRResources getResources()
   {
      return application.getResources();
   }

   @Override
   public void stateChanged(ChangeEvent evt)
   {
      samplePanel.setClippingBounds(getClippingBounds());
   }

   @Override
   public double getCurrentMagnification()
   {
      CanvasGraphics cg = getCanvasGraphics();
   
      return cg == null ? 1.0 : cg.getMagnification();
   }

   @Override
   public void setCurrentMagnification(double factor)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg != null && factor > 0.0)
      {
         cg.setMagnification(factor);
         samplePanel.updateBounds();
      }
   }

   @Override
   public double zoomAction(ZoomValue zoomValue)
   {
      updateCurrentFactor(zoomValue);

      samplePanel.updateBounds();

      return getCurrentMagnification();
   }

   private void updateCurrentFactor(ZoomValue zoomValue)
   {
      String id = zoomValue.getActionCommand();
      BBox bbox = samplePanel.getBpBounds();

      if (bbox == null) return;

      double factor = getCurrentMagnification();

      if (id.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         double width = bbox.getWidth();

         if (width > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getWidth()/width;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         double height = bbox.getHeight();

         if (height > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getHeight()/(double)height;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         double width = bbox.getWidth();
         double height = bbox.getHeight();

         if (height <= 0 || width <= 0)
         {
            factor = 1.0;
         }
         else
         {
            Dimension dim = samplePanel.getSize();

            if (height > width)
            {
               factor = dim.getHeight()/height;
            }
            else
            {
               factor = dim.getWidth()/width;
            }
         }
      }
      else if (zoomValue instanceof PercentageZoomValue)
      {
         factor = ((PercentageZoomValue)zoomValue).getValue();
      }

      zoomComp.setZoom(zoomValue, factor);

      setCurrentMagnification(factor);
   }

   @Override
   public void showZoomChooser()
   {
   }

   public RenderingHints getRenderingHints()
   {
      return canvas == null ? application.getRenderingHints() 
        : canvas.getRenderingHints();
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvas == null ? application.getDefaultCanvasGraphics() :
         canvas.getCanvasGraphics();
   }

   public void display(JDRFrame frame)
   {
      canvas = frame.getCanvas();
      JDRResources resources = canvas.getResources();

      CanvasGraphics cg = canvas.getCanvasGraphics();

      selectedBounds = null;

      zoomComp.setZoom(application.getMagnification());
      setCurrentMagnification(application.getMagnification());
      okayButton.setEnabled(false);
      doTaskButton.setEnabled(true);

      objects.clear();
      infoHeaderText = "";
      infoArea.setText(infoText);

      JDRGroup allObjects = canvas.getAllObjects();

      for (int i = 0; i < allObjects.size(); i++)
      {
         JDRCompleteObject obj = allObjects.get(i);

         if (obj.isSelected())
         {
            if (obj instanceof JDRClippable)
            {
               objects.add(new OldNewObject(obj, i));

               if (selectedBounds == null)
               {
                  selectedBounds = obj.getStorageBBox();
               }
               else
               {
                  selectedBounds.merge(obj.getStorageBBox());
               }
            }
            else
            {
               infoHeaderText += String.format("%s%n",
                 resources.getMessage("message.clip.not_supported",
                   resources.getDefaultDescription(obj)));
            }
         }
      }

      if (selectedBounds == null)
      {
         resources.error(this,
           resources.getMessage("error.no_clippable_objects"));

         return;
      }

      infoArea.append(infoHeaderText);

      cg.setComponent(samplePanel);

      reset();
      samplePanel.updateBounds();

      setVisible(true);
   }

   protected void reset()
   {
      success = false;

      for (OldNewObject obj : objects)
      {
         obj.reset();
      }

      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      boundsPositionPanel.setValue(
          selectedBounds.getMinX(),
          selectedBounds.getMinY(), unit);

      boundsSizePanel.setValue(
          selectedBounds.getWidth(),
          selectedBounds.getHeight(), unit);

      samplePanel.repaint();
   }

   public void cancel()
   {
      JDRResources resources = getResources();

      if (task != null)
      {
         if (resources.confirm(this, resources.getMessage("process.confirm.abort"))
             != JOptionPane.YES_OPTION)
         {
            return;
         }

         if (task != null)// may have finished while user was responding
         {
            task.cancel(true);
         }

         task = null;
      }

      if (success &&
           resources.confirm(this, 
            resources.getMessage("clip.confirm_discard"))
             != JOptionPane.YES_OPTION)
      {
         return;
      }

      setCurrentMagnification(application.getMagnification());

      getCanvasGraphics().setComponent(canvas);

      setVisible(false);
   }

   public void okay()
   {
      setCurrentMagnification(application.getMagnification());

      getCanvasGraphics().setComponent(canvas);

      canvas.replace(objects, getResources().getMessage("undo.clip"));
      setVisible(false);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         cancel();
      }
      else if (action.equals("dotask"))
      {
         doTask();
      }
      else if (action.equals("reload"))
      {
         reset();
         infoArea.setText(infoText + infoHeaderText);
      }
   }

   private void doTask()
   {
      success = false;
      boundsPositionPanel.setEnabled(false);
      boundsSizePanel.setEnabled(false);
      okayButton.setEnabled(false);
      resetButton.setEnabled(false);
      doTaskButton.setEnabled(false);
      task = new ClipTask(this);
      task.execute();
   }

   public void taskFailed(Exception e)
   {
      finishedTask(getResources().getMessage("error.clip_failed"));

      getResources().error(this, e);

      success = false;
      okayButton.setEnabled(false);
   }

   public void finishedTask(String message)
   {
      infoArea.setText(infoHeaderText + message);

      success = true;
      okayButton.setEnabled(true);
      resetButton.setEnabled(true);
      doTaskButton.setEnabled(true);

      samplePanel.repaint();
      boundsPositionPanel.setEnabled(true);
      boundsSizePanel.setEnabled(true);
      task = null;
   }

   public Rectangle2D getClippingBounds()
   {
      JDRUnit unit = canvas.getCanvasGraphics().getStorageUnit();

      return new Rectangle2D.Double(
         boundsPositionPanel.getValue1(unit),
         boundsPositionPanel.getValue2(unit),
         boundsSizePanel.getValue1(unit),
         boundsSizePanel.getValue2(unit)
      );
   }

   public void shiftClippingBounds(double x, double y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      if (boundsPositionPanel.isLinked())
      {
         boundsPositionPanel.setLinked(false);
      }

      boundsPositionPanel.translate(unit, x, y);
   }

   public void extendClippingArea(double x, double y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      if (boundsSizePanel.isLinked())
      {
         boundsSizePanel.setLinked(false);
      }

      boundsSizePanel.translate(unit, x, y);
   }

   public void extendClippingHeight(double y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      if (boundsSizePanel.isLinked())
      {
         boundsSizePanel.setLinked(false);
      }

      boundsSizePanel.translate(unit, 0, y);
   }

   public void extendClippingWidth(double x)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      if (boundsSizePanel.isLinked())
      {
         boundsSizePanel.setLinked(false);
      }

      boundsSizePanel.translate(unit, x, 0);
   }

   public Vector<OldNewObject> getObjects()
   {
      return objects;
   }

   public BBox getSelectedBounds()
   {
      return selectedBounds;
   }

   private FlowframTk application;
   private JDRCanvas canvas;
   private BBox selectedBounds;

   private Vector<OldNewObject> objects;
   private boolean success = false;

   private ClipSamplePanel samplePanel;
   private LinkedLengthsPanel boundsPositionPanel, boundsSizePanel;
   private JButton doTaskButton, okayButton, resetButton;
   private JTextArea infoArea;
   private String infoText, infoHeaderText;
   private ZoomComponent zoomComp;

   private ClipTask task=null;
}

class ClipSamplePanel extends JPanel
   implements Scrollable,MouseListener,MouseMotionListener
{
   public ClipSamplePanel(ClipDialog dialog)
   {
      super(null);

      this.dialog = dialog;

      clipStoragePt = new Point2D.Double();

      setBackground(Color.WHITE);
      setPreferredSize(MIN_SIZE);

      addMouseListener(this);
      addMouseMotionListener(this);
   }

   @Override
   public Dimension getMinimumSize()
   {
      return MIN_SIZE;
   }

   @Override
   public Dimension getPreferredScrollableViewportSize()
   {
      return prefViewportSize;
   }

   public boolean getScrollableTracksViewportWidth() {return false;}
   public boolean getScrollableTracksViewportHeight(){return false;}

   public Dimension getUnitIncrement()
   {
      CanvasGraphics cg = dialog.getCanvasGraphics();

      JDRGrid grid = cg.getGrid();
      Point2D minor = grid.getMinorTicDistance();
      Point2D major = grid.getMinorTicDistance();

      double incX = (minor.getX() > 0 ? minor.getX() : major.getX());
      double incY = (minor.getY() > 0 ? minor.getY() : major.getY());

      return new Dimension(
       (int)Math.ceil(cg.bpToComponentX(incX)),
       (int)Math.ceil(cg.bpToComponentY(incY)));
   }

   @Override
   public int getScrollableUnitIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction)
   {
      int currentPos = 0;

      Dimension unitIncrement = getUnitIncrement();

      int inc;

      if (orientation == SwingConstants.HORIZONTAL)
      {
         currentPos = visibleRect.x;
         inc = unitIncrement.width;
      }
      else
      {
         currentPos = visibleRect.y;
         inc = unitIncrement.height;
      }

      if (direction < 0)
      {
         int newPosition = currentPos - (currentPos/inc) * inc;

         return newPosition==0 ? inc : newPosition;
      }
      else
      {
         return ((currentPos/inc)+1)*inc - currentPos;
      }
   }

   @Override
   public int getScrollableBlockIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction)
   {
      Dimension unitIncrement = getUnitIncrement();

      if (orientation == SwingConstants.HORIZONTAL)
         return (unitIncrement.width >= visibleRect.width) ?
           visibleRect.width :
           visibleRect.width - unitIncrement.width;
      else
         return (unitIncrement.height >= visibleRect.height) ?
           visibleRect.height :
           visibleRect.height - unitIncrement.height;
   }

   @Override
   public void mouseClicked(MouseEvent evt)
   {
      anchorPt = null;
   }

   @Override
   public void mouseEntered(MouseEvent evt)
   {
   }

   @Override
   public void mouseExited(MouseEvent evt)
   {
   }

   @Override
   public void mousePressed(MouseEvent evt)
   {
      anchorPt = null;
      clipHotspot = -1;

      if (evt.getClickCount() == 1
          && (evt.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK)
                == InputEvent.BUTTON1_DOWN_MASK)
      {
         anchorPt = evt.getPoint();

         if (clipBBox != null)
         {
            CanvasGraphics cg = dialog.getCanvasGraphics();

            clipStoragePt.setLocation(
               cg.componentXToStorage(evt.getX()-xOffset),
               cg.componentYToStorage(evt.getY()-yOffset)
             );

            clipHotspot = clipBBox.getHotspotFromStoragePoint(clipStoragePt);

            switch (clipHotspot)
            {
               case BBox.HOTSPOT_S:
               case BBox.HOTSPOT_SE:
               case BBox.HOTSPOT_E:
               break;
               default:
                  if (clipBBox.contains(clipStoragePt))
                  {
                     clipHotspot = BBox.HOTSPOT_NONE;
                  }
                  else
                  {
                     clipHotspot = -1;
                  }
            }
         }
      }
   }

   @Override
   public void mouseReleased(MouseEvent evt)
   {
      anchorPt = null;
   }

   @Override
   public void mouseDragged(MouseEvent evt)
   {
      if (anchorPt != null)
      {
         CanvasGraphics cg = dialog.getCanvasGraphics();

         if (clipHotspot > -1)
         {
            double x = evt.getX() - anchorPt.getX();
            double y = evt.getY() - anchorPt.getY();

            double storageX = cg.componentXToStorage(x);
            double storageY = cg.componentYToStorage(y);

            switch (clipHotspot)
            {
               case BBox.HOTSPOT_S:
                  dialog.extendClippingHeight(storageY);
               break;
               case BBox.HOTSPOT_E:
                  dialog.extendClippingWidth(storageX);
               break;
               case BBox.HOTSPOT_SE:
                  dialog.extendClippingArea(storageX, storageY);
               break;
               default:
                  dialog.shiftClippingBounds(storageX, storageY);
            }
         }

         anchorPt.setLocation(evt.getX(), evt.getY());
      }
   }

   @Override
   public void mouseMoved(MouseEvent evt)
   {
   }

   public void updateBounds()
   {
      CanvasGraphics cg = dialog.getCanvasGraphics();

      bounds = dialog.getSelectedBounds();

      if (bounds != null)
      {
         bpBounds = new BBox(cg, 
           cg.storageToBp(bounds.getMinX()),
           cg.storageToBp(bounds.getMinY()),
           cg.storageToBp(bounds.getMaxX()),
           cg.storageToBp(bounds.getMaxY())
         );

         Dimension orgSize = getPreferredSize();

         prefViewportSize = new Dimension(
          (int)Math.ceil(cg.bpToComponentX(bpBounds.getWidth()))+PADDING,
          (int)Math.ceil(cg.bpToComponentY(bpBounds.getHeight()))+PADDING);

         if (prefViewportSize.width < MIN_SIZE.width)
         {
            prefViewportSize.width = MIN_SIZE.width;
         }

         if (prefViewportSize.height < MIN_SIZE.height)
         {
            prefViewportSize.height = MIN_SIZE.height;
         }

         setPreferredSize(prefViewportSize);

         int halfPadding = PADDING/2;

         xOffset = halfPadding - cg.bpToComponentX(bpBounds.getMinX());
         yOffset = halfPadding - cg.bpToComponentY(bpBounds.getMinY());

         revalidate();
      }

      repaint();
   }

   public BBox getBpBounds()
   {
      return bpBounds;
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      if (bounds == null) return;

      CanvasGraphics cg = dialog.getCanvasGraphics();

      Graphics2D oldG2 = cg.getGraphics();

      Graphics2D g2 = (Graphics2D)g;

      cg.setGraphicsDevice(g2);

      AffineTransform oldAf = g2.getTransform();
      Paint oldPaint = g2.getPaint();
      RenderingHints oldHints = g2.getRenderingHints();

      RenderingHints hints = dialog.getRenderingHints();

      if (hints != null)
      {
         g2.setRenderingHints(hints);
      }

      g2.translate(xOffset, yOffset);

      Vector<OldNewObject> objects = dialog.getObjects();

      for (OldNewObject oldNewObj : objects)
      {
         ((JDRClippable)oldNewObj.getOldObject()).drawClipDraft();

         JDRCompleteObject newObj = oldNewObj.getNewObject();

         if (newObj != null && oldNewObj.hasChanged())
         {
            newObj.draw();
         }
      }

      if (clipBBox != null)
      {
         clipBBox.draw(CLIP_HOTSPOTS_FLAG);
      }

      g2.setTransform(oldAf);
      g2.setPaint(oldPaint);
      g2.setRenderingHints(oldHints);

      cg.setGraphicsDevice(oldG2);
   }

   public void setClippingBounds(Rectangle2D rect)
   {
      clippingBounds = rect;

      if (rect == null)
      {
         clipBBox = null;
      }
      else
      {
         clipBBox = new BBox(dialog.getCanvasGraphics(), clippingBounds);
      }

      repaint();
   }

   private ClipDialog dialog;
   private BBox bounds, clipBBox;
   private Rectangle2D clippingBounds;

   private BBox bpBounds=null;
   private Point anchorPt;
   private Point2D clipStoragePt;
   private int clipHotspot=-1;
   private Dimension prefViewportSize = MIN_SIZE;
   private double xOffset=0.0, yOffset = 0.0;

   private static short CLIP_HOTSPOTS_FLAG
     = (short)(BBox.SOUTH | BBox.EAST | BBox.SOUTH_EAST);

   public static final int PADDING=10;
   private static final Dimension MIN_SIZE = new Dimension(300, 300);
}

class ClipTask extends SwingWorker<String,Void>
{
   public ClipTask(ClipDialog dialog)
   {
      super();
      this.dialog = dialog;
   }

   protected String doInBackground() 
      throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      Vector<OldNewObject> objects = dialog.getObjects();

      Rectangle2D bounds = dialog.getClippingBounds();
      BBox bbox = new BBox(dialog.getCanvasGraphics(), bounds);

      JDRResources resources = dialog.getResources();

      StringBuilder builder = new StringBuilder();

      for (OldNewObject oldNewObj : objects)
      {
         JDRCompleteObject orgObj = oldNewObj.getOldObject();

         if (orgObj.isCompletelyInsideStorageBox(bbox))
         {
            builder.append(resources.getMessage(
              "message.clip.unchanged",
              resources.getDefaultDescription(orgObj)
            ));
         }
         else
         {
            try
            {
               JDRCompleteObject newObj = ((JDRClippable)orgObj).clip(bounds);

               oldNewObj.setNewObject(newObj);

               String name = newObj.getClass().getSimpleName();
               String canonical = newObj.getClass().getCanonicalName();

               if (canonical == null)
               {
                  canonical = name;
               }

               builder.append(resources.getMessage(
                 "message.clip.replaced",
                 resources.getDefaultDescription(orgObj),
                 resources.getMessageWithFallback("class."+canonical, name)
               ));
            }
            catch (UnableToClipException e)
            {
               oldNewObj.setNewObject(null);

               builder.append(resources.getMessage(
                 "message.clip.removed",
                 resources.getDefaultDescription(orgObj)
               ));
            }
         }

         builder.append(String.format("%n"));
      }

      return builder.toString();
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         dialog.finishedTask(get());
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
      }
   }

   private ClipDialog dialog;
}
