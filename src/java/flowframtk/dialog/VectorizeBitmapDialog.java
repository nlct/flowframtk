// File          : VectorizeBitmapDialog.java
// Description   : Dialog box for vectorizing a bitmap
// Date          : 25th May 2011
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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

import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.undo.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for scanning a bitmap.
 * @author Nicola L C Talbot
 */
public class VectorizeBitmapDialog extends JFrame
   implements ActionListener
{
   public VectorizeBitmapDialog(FlowframTk application)
   {
      super(application.getResources().getString("vectorize.title"));
      this.application = application;

      createAndShowGUI();
   }

   private void createAndShowGUI()
   {
      JDRResources resources = getResources();

      setIconImage(resources.getSmallAppIcon().getImage());

      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            cancel();
         }
      });

      undoManager = new UndoManager();

      ImageIcon ic = resources.appIcon("pipet.png");

      if (ic != null)
      {
         try
         {
            Image pipetImage = ic.getImage();

            if (pipetImage != null)
            {
               Toolkit toolkit = Toolkit.getDefaultToolkit();

               colourPickerCursor = toolkit.createCustomCursor(pipetImage, 
                  new Point(0, ic.getIconHeight()-1), "colour picker");
            }
         }
         catch (Exception e)
         {
            error(e);
         }
      }

      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      JMenu editM = resources.createAppMenu("edit");
      mBar.add(editM);

      undoItem = resources.createAppMenuItem("edit", "undo", this);
      undoItem.setEnabled(false);
      editM.add(undoItem);

      redoItem = resources.createAppMenuItem("edit", "redo", this);
      redoItem.setEnabled(false);
      editM.add(redoItem);

      mainPanel = new ImagePanel(this);
      imageScrollPane = new JScrollPane(mainPanel);

      resultPanel = new ResultPanel(mainPanel);
      resultScrollPane = new JScrollPane(resultPanel);

      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.setBackground(Color.WHITE);
      getContentPane().add(topPanel, "North");

      coordField = resources.createAppInfoField(12);
      topPanel.add(coordField, "West");

      timeElapsedField = resources.createAppInfoField(9);
      topPanel.add(timeElapsedField, "East");

      JPanel middleComp = new JPanel(new BorderLayout());
      middleComp.setOpaque(false);
      middleComp.setAlignmentY(Component.CENTER_ALIGNMENT);

      topPanel.add(middleComp, "Center");

      mainZoomWidget = new ZoomWidget(this, imageScrollPane);
      middleComp.add(mainZoomWidget, "West");
      mainZoomWidget.add(Box.createHorizontalStrut(10), "East");

      cardLayout = new CardLayout();
      cardComp = new JPanel(cardLayout);
      cardComp.setOpaque(false);
      cardComp.setAlignmentY(Component.CENTER_ALIGNMENT);
      middleComp.add(cardComp, "Center");

      JComponent topInfo = resources.createAppInfoArea(
        "vectorize.message.default_info");
      topInfo.setAlignmentY(Component.CENTER_ALIGNMENT);

      cardComp.add(topInfo, "default_info");

      topInfo = resources.createAppInfoArea(
        "vectorize.message.region_picker");
      topInfo.setAlignmentY(Component.CENTER_ALIGNMENT);

      cardComp.add(topInfo, "region_info");

      scanStatusBar = new ScanStatusBar(this);
      cardComp.add(scanStatusBar, "status");
      cardLayout.first(cardComp);

      JPanel bottomPanel = new JPanel();

      applyButton = resources.createDialogButton("vectorize.apply_pinned",
       "apply_pinned", this, null);
      bottomPanel.add(applyButton);

      okayButton = resources.createOkayButton(this);
      bottomPanel.add(okayButton);

      cancelButton = resources.createCancelButton(this);
      bottomPanel.add(cancelButton);

      bottomPanel.add(resources.createHelpButton("vectorize"));

      getContentPane().add(bottomPanel, "South");

      JTabbedPane resultTabbedPane = new JTabbedPane();

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(resultScrollPane, "Center");

      topPanel = new JPanel(new BorderLayout());
      panel.add(topPanel, "North");

      topPanel.add(resources.createAppInfoArea("vectorize.results.info"), "Center");

      resultZoomWidget = new ZoomWidget(this, resultScrollPane);
      resultZoomWidget.setEnabled(false);
      topPanel.add(resultZoomWidget, "East");

      zoomLinkCheckBox = resources.createDialogToggle("vectorize.results.link",
        "link", this, true);
      resultZoomWidget.add(zoomLinkCheckBox, "East");

      resources.addTab(resultTabbedPane, "vectorize.results", panel);

      summaryPanel = new SummaryPanel(this);
      resources.addTab(resultTabbedPane, "vectorize.summary",
         new JScrollPane(summaryPanel));

      messagePanel = resources.createAppInfoArea();
      messagePanel.setOpaque(true);
      resources.addTab(resultTabbedPane, "vectorize.messages",
         new JScrollPane(messagePanel));

      JSplitPane imagePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
         imageScrollPane, resultTabbedPane);

      imagePane.setResizeWeight(0.5);
      imagePane.setOneTouchExpandable(true);

      controlPanel = new ControlPanel(this);

      historyPanel = Box.createVerticalBox();
      historyGroup = new ButtonGroup();

      JTabbedPane sideTabbedPane = new JTabbedPane();
      resources.addTab(sideTabbedPane, "vectorize.controls",
        controlPanel);

      resources.addTab(sideTabbedPane, "vectorize.history",
         new JScrollPane(historyPanel));

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                       imagePane, sideTabbedPane);

      splitPane.setResizeWeight(0.6);
      splitPane.setOneTouchExpandable(true);

      getContentPane().add(splitPane, "Center");

      pack();
      setLocationRelativeTo(application);
   }

   public Color getNotRegionColor()
   {
      return application.getSettings().getVectorizeNotRegion();
   }

   public Color getUnpinnedLineColor()
   {
      return application.getSettings().getVectorizeLine();
   }

   public Color getConnectorColor()
   {
      return application.getSettings().getVectorizeConnector();
   }

   public Color getDragColor()
   {
      return application.getSettings().getVectorizeDrag();
   }

   public int getControlSize()
   {
      return application.getSettings().getVectorizeControlSize();
   }

   public Color getControlColor()
   {
      return application.getSettings().getVectorizeControlColor();
   }

   public void setCancelEnabled(boolean enable)
   {
      if (cancelButton != null)
      {
         cancelButton.setEnabled(enable);
      }
   }

   public void setOkayEnabled(boolean enable)
   {
      if (okayButton != null)
      {
         okayButton.setEnabled(enable);
      }

      if (applyButton != null)
      {
         applyButton.setEnabled(enable && resultPanel.hasResults());
      }
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null)
      {
         return;
      }

      if (command.equals("undo"))
      {
         try
         {
            undoManager.undo();
            addMessageLn(undoItem.getText());
            updateUndoRedo();

            currentHistoryIndex--;
            selectCurrentEditComponent();
         }
         catch (CannotUndoException e)
         {
            error(e);
            undoItem.setEnabled(false);
            undoItem.setText(getResources().getString("label.undo"));
         }
      }
      else if (command.equals("redo"))
      {
         try
         {
            undoManager.redo();
            addMessageLn(redoItem.getText());
            updateUndoRedo();

            currentHistoryIndex++;
            selectCurrentEditComponent();
         }
         catch (CannotRedoException e)
         {
            error(e);
            redoItem.setEnabled(false);
            redoItem.setText(getResources().getString("label.redo"));
         }
      }
      else if (command.equals("apply_pinned"))
      {
         apply();
         updateWidgets();
         resultPanel.repaint();
      }
      else if (command.equals("okay"))
      {
         okay();
      }
      else if (command.equals("cancel"))
      {
         cancel();
      }
      else if (command.equals("link"))
      {
         if (zoomLinkCheckBox.isSelected())
         {
            resultZoomWidget.setValue(mainZoomWidget.getValue());
            resultZoomWidget.setEnabled(false);
            resultPanel.updatePanel();
         }
         else
         {
            resultZoomWidget.setEnabled(true);
         }
      }
   }

   private void updateUndoRedo()
   {
      updateUndoRedo(undoManager.getUndoPresentationName(),
        undoManager.getRedoPresentationName());

      controlPanel.updateWidgets(false, shapeList != null);
   }

   private void updateUndoRedo(String undoName, String redoName)
   {
      boolean canUndo = undoManager.canUndo();
      undoItem.setEnabled(canUndo);

      if (canUndo)
      {
         undoItem.setText(undoName);
      }
      else
      {
         undoItem.setText(getResources().getString("label.undo"));
      }

      boolean canRedo = undoManager.canRedo();
      redoItem.setEnabled(canRedo);

      if (canRedo)
      {
         redoItem.setText(redoName);
      }
      else
      {
         redoItem.setText(getResources().getString("label.redo"));
      }
   }

   private void addUndoableEdit(Vector<ShapeComponentVector> shapes, String name)
   {
      addUndoableEdit(shapes, name, (Result)null);
   }

   private void addUndoableEdit(Vector<ShapeComponentVector> shapes, String name,
     Result result)
   {
      for (int i = historyPanel.getComponentCount()-1; i > currentHistoryIndex; i--)
      {
         historyPanel.remove(i);
      }

      ShapesUndoableEdit edit = new ShapesUndoableEdit(this, 
         oldShapeList, shapes, name, result);
      UndoableEditComp editComp = new UndoableEditComp(edit, this,
          ++currentHistoryIndex);
      historyGroup.add(editComp);
      historyPanel.add(editComp);
      editComp.setSelected(true);

      undoManager.addEdit(edit);
      updateUndoRedo();
      updateShapes(shapes);
   }

   private void addUndoableEdit(Vector<ShapeComponentVector> shapes, String name,
     Vector<Result> resultList)
   {
      for (int i = historyPanel.getComponentCount()-1; i > currentHistoryIndex; i--)
      {
         historyPanel.remove(i);
      }

      ShapesUndoableEdit edit = new ShapesUndoableEdit(this, 
         oldShapeList, shapes, name, resultList);
      UndoableEditComp editComp = new UndoableEditComp(edit, this,
          ++currentHistoryIndex);
      historyGroup.add(editComp);
      historyPanel.add(editComp);
      editComp.setSelected(true);

      undoManager.addEdit(edit);
      updateUndoRedo();
      updateShapes(shapes);
   }

   public void selectHistoryItem(int idx)
   {
      if (idx == currentHistoryIndex)
      {
         return;
      }

      if (idx < currentHistoryIndex)
      {
         for (int i = currentHistoryIndex; i > idx; i--)
         {
            undoManager.undo();
            addMessageLn(undoItem.getText());
            updateUndoRedo();
         }
      }
      else
      {
         for (int i = currentHistoryIndex+1; i <= idx; i++)
         {
            undoManager.redo();
            addMessageLn(redoItem.getText());
            updateUndoRedo();
         }
      }

      currentHistoryIndex = idx;
   }

   public void selectCurrentEditComponent()
   {
      int n = historyPanel.getComponentCount();

      if (currentHistoryIndex < 0 || currentHistoryIndex >= n)
      {
         for (int i = 0; i < n; i++)
         {
            UndoableEditComp comp = (UndoableEditComp)historyPanel.getComponent(i);

            if (comp.isSelected())
            {
               comp.setSelected(false);
               break;
            }
         }

         currentHistoryIndex = -1;
      }
      else
      {
         UndoableEditComp comp = 
            (UndoableEditComp)historyPanel.getComponent(currentHistoryIndex);
         comp.setSelected(true);
      }
   }

   public void setCoords(int x, int y)
   {
      coordField.setText(String.format("%d, %d", x, y));
   }

   public void updateTimeElapsed()
   {
      setTimeElapsed((new Date()).getTime()-startTime);
   }

   public void setTimeElapsed(long time)
   {
      int hours = (int)time/3600000;
      int minutes = (int)(time%3600000)/60000;
      int seconds = (int)(time%60000)/1000;
      timeElapsedField.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return getResources().getMessageDictionary();
   }

   public boolean hasResults()
   {
      return !resultPanel.getResults().isEmpty();
   }

   private void reset()
   {
      shapeList = null;
      mainPanel.clearAllShapes();
      resultPanel.newImage();
      summaryPanel.updateSummary(null);
      undoManager.discardAllEdits();
      historyPanel.removeAll();
      currentHistoryIndex = 0;
      UndoableEditComp editComp= new UndoableEditComp(this);
      editComp.setSelected(true);
      historyGroup.add(editComp);
      historyPanel.add(editComp);
      messagePanel.setText("");

      controlPanel.reset(false);
      controlPanel.updateWidgets(false, shapeList != null);
   }

   public boolean clearImage()
   {
      if (hasCurrentResults())
      {
         int result = getResources().confirm(this, 
           getResources().getString("vectorize.confirm_pin_current"), 
            JOptionPane.YES_NO_CANCEL_OPTION);

         if (result == JOptionPane.YES_OPTION)
         {
            storeResults();
         }
         else if (result == JOptionPane.NO_OPTION)
         {
            resultPanel.updateCurrentShapeList(null);
         }
         else
         {
            return false;
         }
      }

      reset();

      return true;
   }

   public boolean hasCurrentResults()
   {
      return resultPanel.getCurrentShapeList() != null
       && !resultPanel.getCurrentShapeList().isEmpty();
   }

   public Vector<ShapeComponentVector> getCurrentShapeList()
   {
      return resultPanel.getCurrentShapeList();
   }

   public void clearAllResults()
   {
      int result = getResources().confirm(this, 
        getResources().getString("vectorize.confirm_clear_all"));

      if (result == JOptionPane.YES_OPTION)
      {
         reset();
         updateWidgets();
         revalidate();
      }
   }

   public void storeResults()
   {
      if (shapeList == null || shapeList.isEmpty()) return;

      storeOldShapes();
      Vector<Result> resultList = new Vector<Result>(shapeList.size());

      for (ShapeComponentVector shape: shapeList)
      {
         resultList.add(resultPanel.storeShape(shape));
      }

      shapeList = null;

      addUndoableEdit(shapeList, getResources().getString("vectorize.pin_shape"), 
         resultList);

      resultPanel.updateCurrentShapeList(shapeList);
      updateWidgets();
   }

   public void error(Exception e)
   {
      getResources().error(this, e);
   }

   public void error(String msg)
   {
      getResources().error(this, msg);
   }

   public JDRBitmap getBitmap()
   {
      return bitmap;
   }

   public JDRFrame getBitmapFrame()
   {
      return currentFrame;
   }

   public void redisplay()
   {
      setVisible(true);

      int state = getExtendedState();

      if ((state & Frame.ICONIFIED) == Frame.ICONIFIED)
      {
         setExtendedState(state ^ Frame.ICONIFIED);
      }

      toFront();
   }

   public void display()
   {
      if (isVisible() && bitmap != null)
      {
         redisplay();

         if (bitmap != currentFrame.getSelectedBitmap())
         {
            error(getResources().getString("error.vectorize_in_progress"));
         }

         return;
      }

      currentFrame = application.getCurrentFrame();
      bitmap = currentFrame.getSelectedBitmap();

      reset();

      Image image = bitmap.getImage();

      if (image instanceof BufferedImage)
      {
         setImage((BufferedImage)image);
      }
      else
      {
         BufferedImage buffImage = new BufferedImage(bitmap.getIconWidth(),
           bitmap.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);

         Graphics2D g2 = (Graphics2D)buffImage.createGraphics();

         if (g2 != null)
         {
            g2.drawImage(image, 0, 0, null);

            g2.dispose();

            setImage(buffImage);
         }
      }

      controlPanel.setAll(true);
      setVisible(true);
   }

   public void okay()
   {
      if (hasCurrentResults())
      {
         int result = getResources().confirm(this, 
           getResources().getString("vectorize.confirm_include_current"), 
            JOptionPane.YES_NO_CANCEL_OPTION);

         if (result == JOptionPane.YES_OPTION)
         {
            storeResults();
         }
         else if (result == JOptionPane.NO_OPTION)
         {
            resultPanel.updateCurrentShapeList(null);
         }
         else
         {
            return;
         }
      }

      apply();
      bitmap = null;
      setVisible(false);
   }

   public void apply()
   {
      CanvasGraphics cg = currentFrame.getCanvasGraphics();

      JDRGroup group = new JDRGroup(cg);

      Vector<Result> resultList = resultPanel.getResults();

      for (Result result : resultList)
      {
         try
         {
            group.add(result.getJDRPath(cg));
         }
         catch (Exception e)
         {
            getResources().debugMessage(e);
         }
      }

      int n = group.size();

      if (n == 0)
      {
         return;
      }

      double[] matrix = new double[6];
      bitmap.getTransformation(matrix);

      JDRCanvas canvas = currentFrame.getCanvas();
      JDRCompleteObject obj;

      if (n == 1)
      {
         obj = group.firstElement();
         obj.transform(matrix);
      }
      else
      {
         group.transform(matrix);
         obj = group;
      }

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(canvas,
        getResources().getString("undo.vectorize"));

      canvas.selectObject(ce, bitmap, false);

      canvas.addObject(ce, obj, 
         getResources().getString("undo.vectorize"));

      canvas.selectObject(ce, obj, true);

      currentFrame.postEdit(ce);

      resultList.clear();
   }

   public void cancel()
   {
      cancel(true);
   }

   public void cancel(boolean confirm)
   {
      if (confirm && (!resultPanel.getResults().isEmpty() || hasCurrentResults()))
      {
         if (getResources().confirm(this, 
               getResources().getString("vectorize.confirm_discard_all"))
             != JOptionPane.YES_OPTION)
         {
            return;
         }
      }

      bitmap = null;
      setVisible(false);
   }

   public void setImage(BufferedImage image)
   {
      mainPanel.setImage(image);
      resultPanel.updatePanel();
      scanRegion = null;
      revalidate();

      controlPanel.updateWidgets(false, shapeList != null);
   }

   public void zoomChanged(ZoomWidget widget)
   {
      if (widget == mainZoomWidget)
      {
         mainPanel.updatePanel();

         if (zoomLinkCheckBox.isSelected())
         {
            resultZoomWidget.setMagnification(widget);
            resultPanel.updatePanel();
         }
      }
      else if (widget == resultZoomWidget)
      {
         resultPanel.updatePanel();
      }
   }

   public void updateImagePanel()
   {
      mainPanel.updatePanel();
      resultPanel.updatePanel();
   }

   public void repaintImagePanel()
   {
      mainPanel.repaint();
      resultPanel.repaint();
   }

   public void repaintImagePanel(Rectangle2D bounds)
   {
      repaintImagePanel(bounds, true);
   }

   public void repaintImagePanel(Rectangle2D bounds, boolean repaintResults)
   {
      repaintImagePanel(new Rectangle((int)bounds.getX(),
        (int)bounds.getY(), (int)Math.ceil(bounds.getWidth()),
        (int)Math.ceil(bounds.getHeight())), repaintResults);
   }

   public void repaintImagePanel(Rectangle bounds)
   {
      repaintImagePanel(bounds, true);
   }

   public void repaintImagePanel(Rectangle bounds, boolean repaintResults)
   {
      mainPanel.repaint(bounds);

      if (repaintResults)
      {
         resultPanel.repaint(bounds);
      }
   }

   public void addMessageId(String id, Object... params)
   {
      addMessage(getResources().getMessage(id, params));
   }

   public void addMessageIdLn(String id, Object... params)
   {
      addMessageLn(getResources().getMessage(id, params));
   }

   public void addMessage(String msg)
   {
      messagePanel.setText(messagePanel.getText()+msg);
   }

   public void addMessageLn(String msg)
   {
      messagePanel.setText(String.format("%s%s%n", messagePanel.getText(), msg));
   }

   public void addMessageLn()
   {
      messagePanel.setText(String.format("%s%n", messagePanel.getText()));
   }

   public void setWorkingShape(Shape shape)
   {
      mainPanel.setWorkingShape(shape);
   }

   public void setShapes(Vector<Shape> shapes)
   {
      mainPanel.setShapes(shapes);
   }

   public void updateShapes(Vector<ShapeComponentVector> shapeList)
   {
      updateShapes(shapeList, true);
   }

   public void updateShapes(Vector<ShapeComponentVector> shapeList, 
      boolean updateResults)
   {
      this.shapeList = shapeList;
      mainPanel.setShapeList(shapeList);

      if (!updateResults) return;

      resultPanel.updateCurrentShapeList(shapeList);

      summaryPanel.updateSummary(shapeList);
   }

   private void storeOldShapes()
   {
      if (shapeList == null)
      {
         oldShapeList = null;
      }
      else
      {
         oldShapeList = new Vector<ShapeComponentVector>(shapeList.size());

         for (ShapeComponentVector shape : shapeList)
         {
            ShapeComponentVector clonedShape = (ShapeComponentVector)shape.clone();
            oldShapeList.add(clonedShape);
         }
      }
   }

   public void deleteShape(int i)
   {
      storeOldShapes();
      shapeList.remove(i);
      addUndoableEdit(shapeList, getResources().getString("vectorize.delete_shape"));
      resultPanel.updateCurrentShapeList(shapeList);
      updateWidgets();
   }

   public void storeShape(int i)
   {
      storeOldShapes();
      ShapeComponentVector shape = shapeList.remove(i);
      addUndoableEdit(shapeList, getResources().getString("vectorize.pin_shape"), 
         resultPanel.storeShape(shape));
      resultPanel.updateCurrentShapeList(shapeList);
      updateWidgets();
   }

   public void addResult(Result result)
   {
      resultPanel.addResult(result);
   }

   public void removeResult(Result result)
   {
      resultPanel.removeResult(result);
   }

   public void updateWidgets()
   {
      controlPanel.updateWidgets(
        scanStatusBar.isTaskInProgress(), shapeList != null && !shapeList.isEmpty());
   }

   public void showStatusBar()
   {
      cardLayout.show(cardComp, "status");
   }

   public void hideStatusBar()
   {
      showDefaultInfo();
   }

   public void showDefaultInfo()
   {
      cardLayout.show(cardComp, "default_info");
   }

   public void showRegionInfo()
   {
      cardLayout.show(cardComp, "region_info");
   }

   public void startTask(String info, SwingWorker task)
   {
      storeOldShapes();
      scanStatusBar.startTask(info, task);
      setHistoryPanelEnabled(false);
      addMessageLn(info);
   }

   public void taskFailed(Exception e)
   {
      shapeList = oldShapeList;
      finishedTask();

      if (e != null)
      {
         error(e);
      }
   }

   public void finishedTask()
   {
      setWorkingShape(null);
      scanStatusBar.taskFinished();
      controlPanel.taskFinished(shapeList != null);
      updateTimeElapsed();
      setHistoryPanelEnabled(true);
   }

   public void setHistoryPanelEnabled(boolean enable)
   {
      int n = historyPanel.getComponentCount();

      if (n > 0)
      {
         for (int i = 0; i < n; i++)
         {
            UndoableEditComp comp = (UndoableEditComp)historyPanel.getComponent(i);
            comp.setEnabled(enable);
         }
      }
   }

   public void doSelectedTasks()
   {
      startTime = (new Date()).getTime();
      controlPanel.updateWidgets(true, shapeList != null);

      boolean continueToNextStep = true;

      if (controlPanel.isScanImageOn())
      {
         scanImage(continueToNextStep);
      }
      else if (controlPanel.isOptimizeOn())
      {
         doOptimize(continueToNextStep);
      }
      else if (controlPanel.isSplitSubPathsOn())
      {
         doSplitSubPaths(continueToNextStep);
      }
      else if (controlPanel.isLineDetectionOn())
      {
         doLineDetection(continueToNextStep);
      }
      else if (controlPanel.isMergeNearPathsOn())
      {
         doMergeNearPaths(continueToNextStep);
      }
      else if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         error(getResources().getString("vectorize.no_tasks_selected"));
      }
   }

   public void scanImage(boolean continueToNextStep)
   {
      BufferedImage image = mainPanel.getImage();

      if (image == null)
      {
         error(getResources().getString("vectorize.no_image"));
         finishedTask();
         return;
      }

      if (getImageForeground() == null)
      {
         error(getResources().getString("vectorize.foreground_not_set"));
         finishedTask();
         return;
      }

      if (hasCurrentResults())
      {
         int result = getResources().confirm(this, 
            getResources().getString("vectorize.confirm_pin_current"),
            JOptionPane.YES_NO_CANCEL_OPTION);

         if (result == JOptionPane.YES_OPTION)
         {
            storeResults();
         }
         else if (result == JOptionPane.NO_OPTION)
         {
            resultPanel.updateCurrentShapeList(null);
         }
         else
         {
            return;
         }
      }

      updateShapes(null);

      try
      {
         startTask(getResources().getString("vectorize.scanning"), 
            new ScanImage(this, image, continueToNextStep));
      }
      catch (UnsupportedColourType e)
      {
         scanStatusBar.taskFinished();
         clearImage();
         error(e);
      }
   }

   private void updateScanRegion(Vector<ShapeComponentVector> shapeList)
   {
      scanRegion = null;

      for (ShapeComponentVector shapeVec : shapeList)
      {
         Shape shape = shapeVec.getPath();

         if (scanRegion == null)
         {
            scanRegion = new Area(shape);
         }
         else
         {
            scanRegion.add(new Area(shape));
         }
      }
   }

   public void scanFinished(Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      addUndoableEdit(shapeList, 
         getResources().getString("vectorize.scan_image"));

      if (shapeList == null)
      {
         finishedTask();
         error(getResources().getString("vectorize.scan_no_areas_detected"));
         return;
      }

      updateScanRegion(shapeList);

      controlPanel.deselectScanImage();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isOptimizeOn())
      {
         doOptimize(continueToNextStep);
      }
      else if (controlPanel.isSplitSubPathsOn())
      {
         doSplitSubPaths(continueToNextStep);
      }
      else if (controlPanel.isLineDetectionOn())
      {
         doLineDetection(continueToNextStep);
      }
      else if (controlPanel.isMergeNearPathsOn())
      {
         doMergeNearPaths(continueToNextStep);
      }
      else if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doOptimize(boolean continueToNextStep)
   {
      startTask(getResources().getString("vectorize.optimizing_lines"), 
        new OptimizeLines(this, shapeList, continueToNextStep)); 
   }

   public void finishedOptimizeLines(Vector<ShapeComponentVector> shapes,
      boolean continueToNextStep)
   {
      addUndoableEdit(shapes, 
         getResources().getString("vectorize.optimize_lines"));

      if (shapes == null || shapes.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.no_shapes"));
         return;
      }

      controlPanel.deselectOptimizeLines();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isSplitSubPathsOn())
      {
         doSplitSubPaths(continueToNextStep);
      }
      else if (controlPanel.isLineDetectionOn())
      {
         doLineDetection(continueToNextStep);
      }
      else if (controlPanel.isMergeNearPathsOn())
      {
         doMergeNearPaths(continueToNextStep);
      }
      else if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doSplitSubPaths(boolean continueToNextStep)
   {
      startTask(getResources().getString("vectorize.splitting_subpaths"),
          new SplitSubPaths(this, shapeList, continueToNextStep));
   }

   public void finishedSplitSubPaths(Vector<ShapeComponentVector> shapes,
      boolean continueToNextStep)
   {
      addUndoableEdit(shapes, 
         getResources().getString("vectorize.split_subpaths"));

      if (shapes == null || shapes.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.no_shapes"));
         return;
      }

      controlPanel.deselectSubPaths();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isLineDetectionOn())
      {
         doLineDetection(continueToNextStep);
      }
      else if (controlPanel.isMergeNearPathsOn())
      {
         doMergeNearPaths(continueToNextStep);
      }
      else if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doLineDetection(boolean continueToNextStep)
   {
      startTask(getResources().getString("vectorize.detecting_lines"),
          new LineDetection(this, shapeList, continueToNextStep));
   }

   public void finishedLineDetection(Vector<ShapeComponentVector> shapes,
      boolean continueToNextStep)
   {
      addUndoableEdit(shapes, 
         getResources().getString("vectorize.line_detection"));

      if (shapes == null || shapes.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.no_shapes"));
         return;
      }

      controlPanel.deselectLineDetection();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isMergeNearPathsOn())
      {
         doMergeNearPaths(continueToNextStep);
      }
      else if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doMergeNearPaths(boolean continueToNextStep)
   {
      startTask(getResources().getString("vectorize.merge_nearpaths"),
          new MergeNearPaths(this, shapeList, continueToNextStep));
   }

   public void finishedMergeNearPaths(Vector<ShapeComponentVector> shapes,
      boolean continueToNextStep)
   {
      addUndoableEdit(shapes, 
         getResources().getString("vectorize.merge_nearpaths"));

      if (shapes == null || shapes.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.no_shapes"));
         return;
      }

      controlPanel.deselectMergeNearPaths();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isSmoothingOn())
      {
         doSmoothing(continueToNextStep);
      }
      else if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doSmoothing(boolean continueToNextStep)
   {
      if (shapeList == null || shapeList.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.shapes_lost"));
         return;
      }

      startTask(getResources().getString("vectorize.smoothing_shapes"),
          new Smooth(this, shapeList, continueToNextStep)); 
   }

   public void finishedSmoothing(Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      addUndoableEdit(shapeList, 
         getResources().getString("vectorize.smooth_shapes"));

      if (shapeList == null || shapeList.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.shapes_lost"));
         return;
      }

      controlPanel.deselectSmoothing();

      if (!continueToNextStep)
      {
         finishedTask();
         return;
      }

      if (controlPanel.isRemoveTinyPathsOn())
      {
         doRemoveTinyPaths(continueToNextStep);
      }
      else
      {
         finishedTask();
      }
   }

   public void doRemoveTinyPaths(boolean continueToNextStep)
   {
      if (shapeList == null || shapeList.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.shapes_lost"));
         return;
      }

      startTask(getResources().getString("vectorize.removing_tiny_paths"),
          new RemoveTinyPaths(this, shapeList));
   }

   public void finishedRemoveTinyPaths(Vector<ShapeComponentVector> shapeList)
   {
      addUndoableEdit(shapeList, 
         getResources().getString("vectorize.remove_tiny_paths"));

      if (shapeList == null || shapeList.isEmpty())
      {
         finishedTask();
         error(getResources().getString("vectorize.shapes_lost"));
         return;
      }

      controlPanel.deselectRemoveTinyPaths();

      finishedTask();
   }

   public boolean isCancelled()
   {
      return scanStatusBar.isCancelled();
   }

   public boolean isColourPickerOn()
   {
      return controlPanel.isColourPickerOn();
   }

   public void colourPickerChoice(Color colour)
   {
      controlPanel.colourPickerChoice(colour);
   }

   public void setColourPickerCursor(boolean on)
   {
      if (mainPanel != null && colourPickerCursor != null)
      {
         if (on)
         {
            mainPanel.setCursor(colourPickerCursor);
         }
         else
         {
            mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
      }
   }

   public boolean isRegionPickerOn()
   {
      return controlPanel.isRegionPickerOn();
   }

   public void regionPickerChoice(Shape region, int action)
   {
      controlPanel.regionPickerChoice(region, action);
   }

   public Area getRegion()
   {
      return controlPanel.getRegion();
   }

   public void imagePanelClearRegion()
   {
      if (mainPanel != null)
      {
         mainPanel.clearRegion();
      }
   }

   public void imagePanelRestoreOldRegion()
   {
      if (mainPanel != null)
      {
         mainPanel.restoreOldRegion();
      }
   }

   public Area getLastScannedRegion()
   {
      return scanRegion;
   }

   public Area subtractLastScannedRegion()
   {
      if (mainPanel == null) return null;

      int border = controlPanel.getSubtractLastScanBorder();
      Area region = scanRegion;

      if (border > 0)
      {
         BasicStroke stroke = new BasicStroke(2.0f*border);

         region = new Area(stroke.createStrokedShape(scanRegion));
         region.add(scanRegion);
      }

      mainPanel.subtractRegion(region);

      Area notRegion = mainPanel.getNotRegion();

      if (notRegion == null)
      {
         return null;
      }

      region = new Area(new Rectangle2D.Double(0, 0, 
               getImageWidth(), getImageHeight()));

      region.subtract(notRegion);

      return region;
   }

   public Rectangle2D getRegionBounds()
   {
      return mainPanel.getRegionBounds();
   }

   public void setRegionPickerCursor(boolean on)
   {
      if (mainPanel != null)
      {
         if (on)
         {
            mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            showRegionInfo();
         }
         else
         {
            mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            showDefaultInfo();
         }
      }
   }

   public double getMainMagnification()
   {
      return mainZoomWidget.getCurrentMagnification();
   }

   public double getResultMagnification()
   {
      if (resultZoomWidget == null || !resultZoomWidget.isEnabled())
      {
         return mainZoomWidget.getCurrentMagnification() ;
      }
      else
      {
         return resultZoomWidget.getCurrentMagnification();
      }
   }

   public boolean isZoomLinked()
   {
      return zoomLinkCheckBox == null ? true : zoomLinkCheckBox.isSelected();
   }

   public int getImageWidth()
   {
      BufferedImage image = mainPanel.getImage();

      return image == null ? 0 : image.getWidth();
   }

   public int getImageHeight()
   {
      BufferedImage image = mainPanel.getImage();

      return image == null ? 0 : image.getHeight();
   }

   public Color getImageForeground()
   {
      return controlPanel.getImageForeground();
   }

   public double getFuzz()
   {
      return controlPanel.getFuzz();
   }

   public int getSampleWidth()
   {
      return controlPanel.getSampleWidth();
   }

   public int getSampleHeight()
   {
      return controlPanel.getSampleHeight();
   }

   public double getGradientEpsilon()
   {
      return controlPanel.getGradientEpsilon();
   }

   public double getMergeNearPathThreshold()
   {
      return controlPanel.getMergeNearPathThreshold();
   }

   public boolean isRemoveMinTinySubPathsOn()
   {
      return controlPanel.isRemoveMinTinySubPathsOn();
   }

   public double getMinTinySubPathArea()
   {
      return controlPanel.getMinTinySubPathArea();
   }

   public int getMinTinySubPathSize()
   {
      return controlPanel.getMinTinySubPathSize();
   }

   public int getSplitType()
   {
      return controlPanel.getSplitType();
   }

   public double getDeltaThreshold()
   {
      return controlPanel.getDeltaThreshold();
   }

   public boolean isFixedLineWidth()
   {
      return controlPanel.isFixedLineWidth();
   }

   public double getFixedLineWidth()
   {
      return controlPanel.getFixedLineWidth();
   }

   public boolean isRoundRelativeOn()
   {
      return controlPanel.isRoundRelativeOn();
   }

   public boolean isIntersectionDetectionOn()
   {
      return controlPanel.isIntersectionDetectionOn();
   }

   public double getDeltaVarianceThreshold()
   {
      return controlPanel.getDeltaVarianceThreshold();
   }

   public double getSpikeReturnDistance()
   {
      return controlPanel.getSpikeReturnDistance();
   }

   public double getMinimumStubLength()
   {
      return controlPanel.getMinimumStubLength();
   }

   public double getLineDetectTinyStepThreshold()
   {
      return controlPanel.getLineDetectTinyStepThreshold();
   }

   public double getSmoothingTinyStepThreshold()
   {
      return controlPanel.getSmoothingTinyStepThreshold();
   }

   public double getSmoothingDeviationEpsilon()
   {
      return controlPanel.getSmoothingDeviationEpsilon();
   }

   public double getLengthThreshold()
   {
      return controlPanel.getLengthThreshold();
   }

   public double getThresholdDiff()
   {
      return controlPanel.getThresholdDiff();
   }

   public double getCurveThresholdDiff()
   {
      return controlPanel.getCurveThresholdDiff();
   }

   public double getSmoothingMaxDeviation()
   {
      return controlPanel.getSmoothingMaxDeviation();
   }

   public double getMaxTinyPaths()
   {
      return controlPanel.getMaxTinyPaths();
   }

   public double getCurveGradientThreshold()
   {
      return controlPanel.getCurveGradientThreshold();
   }

   public double getCurveStationaryPtThreshold()
   {
      return controlPanel.getCurveStationaryPtThreshold();
   }

   public int getCurveMinPoints()
   {
      return controlPanel.getCurveMinPoints();
   }

   public boolean isTryBezierOn()
   {
      return controlPanel.isTryBezierOn();
   }

   private ImagePanel mainPanel;
   private JScrollPane imageScrollPane, resultScrollPane;
   private ResultPanel resultPanel;
   private SummaryPanel summaryPanel;
   private JTextArea messagePanel;

   private Area scanRegion;

   private JComponent historyPanel;
   private ButtonGroup historyGroup;
   private int currentHistoryIndex=-1;

   private JFileChooser fileChooser;
   private JMenuItem undoItem, redoItem;

   private ScanStatusBar scanStatusBar;
   private ControlPanel controlPanel;
   private ZoomWidget mainZoomWidget, resultZoomWidget;
   private JCheckBox zoomLinkCheckBox;
   private JTextField coordField, timeElapsedField;
   private CardLayout cardLayout;
   private JComponent cardComp;

   private JDRButton applyButton, okayButton, cancelButton;

   private Cursor colourPickerCursor;

   private Vector<ShapeComponentVector> shapeList, oldShapeList;
   private UndoManager undoManager;
   private long startTime;

   public static final long SLEEP_DURATION=10;

   private FlowframTk application;
   private JDRFrame currentFrame;
   private JDRBitmap bitmap;
}

class UndoableEditComp extends JRadioButton implements ActionListener
{
   public UndoableEditComp(VectorizeBitmapDialog dialog)
   {
      super(dialog.getResources().getString("vectorize.history_base"));
      this.dialog = dialog;
      index = 0;
      addActionListener(this);
      createBaseIcon();
   }

   public UndoableEditComp(ShapesUndoableEdit edit, VectorizeBitmapDialog dialog, int index)
   {
      super(edit.getPresentationName(), edit.getIcon(), false);

      setSelectedIcon(edit.getSelectedIcon());

      this.edit = edit;
      this.dialog = dialog;
      this.index = index;

      addActionListener(this);
   }

   private void createBaseIcon()
   {
      BufferedImage image = new BufferedImage(ShapesUndoableEdit.ICON_SIZE,
          ShapesUndoableEdit.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

      Graphics2D g2 = (Graphics2D)image.getGraphics();

      if (g2 != null)
      {
         g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, ShapesUndoableEdit.ICON_SIZE, ShapesUndoableEdit.ICON_SIZE);
         g2.setColor(Color.DARK_GRAY);

         g2.setStroke(new BasicStroke((float)ShapesUndoableEdit.ICON_BORDER));
         g2.drawRect(0, 0, ShapesUndoableEdit.ICON_SIZE, ShapesUndoableEdit.ICON_SIZE);
         g2.dispose();

         setSelectedIcon(new ImageIcon(image));
      }

      image = new BufferedImage(ShapesUndoableEdit.ICON_SIZE,
          ShapesUndoableEdit.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

      g2 = (Graphics2D)image.getGraphics();

      if (g2 != null)
      {
         g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, ShapesUndoableEdit.ICON_SIZE, ShapesUndoableEdit.ICON_SIZE);
         g2.dispose();

         setIcon(new ImageIcon(image));
      }

   }

   public ShapesUndoableEdit getEdit()
   {
      return edit;
   }

   public void actionPerformed(ActionEvent evt)
   {
      if (evt.getSource() == this)
      {
         if (isSelected())
         {
            dialog.selectHistoryItem(index);
         }
      }
   }

   public String toString()
   {
      return edit.getPresentationName();
   }

   private ShapesUndoableEdit edit;
   private VectorizeBitmapDialog dialog;
   private int index;
}

class ShapesUndoableEdit extends AbstractUndoableEdit
{
   public ShapesUndoableEdit(VectorizeBitmapDialog dialog,
     Vector<ShapeComponentVector> oldShapes, 
     Vector<ShapeComponentVector> newShapes, String name, Result result)
   {
      this(dialog, oldShapes, newShapes, name);

      if (result != null)
      {
         resultList = new Vector<Result>(1);
         resultList.add(result);
      }
   }

   public ShapesUndoableEdit(VectorizeBitmapDialog dialog,
     Vector<ShapeComponentVector> oldShapes, 
     Vector<ShapeComponentVector> newShapes, String name, 
     Vector<Result> resultList)
   {
      this(dialog, oldShapes, newShapes, name);
      this.resultList = resultList;
   }

   public ShapesUndoableEdit(VectorizeBitmapDialog dialog,
     Vector<ShapeComponentVector> oldShapes, 
     Vector<ShapeComponentVector> newShapes, String name)
   {
      this.dialog = dialog;
      this.name = name;
      this.newShapes = newShapes;
      this.oldShapes = oldShapes;

      createIcon();
   }

   private void createIcon()
   {
      BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, 
         BufferedImage.TYPE_INT_ARGB);

      Graphics2D g2 = (Graphics2D)image.getGraphics();

      if (g2 != null)
      {
         g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
         g2.setColor(Color.BLACK);

         g2.setRenderingHints(ResultPanel.RENDER_HINTS);

         if (newShapes != null && newShapes.size() > 0)
         {
            Shape[] shapes = new Shape[newShapes.size()];

            Rectangle2D rect = null;

            for (int i = 0; i < newShapes.size(); i++)
            {
               shapes[i] = newShapes.get(i).getPath();

               if (rect == null)
               {
                  rect = shapes[i].getBounds2D();
               }
               else
               {
                  rect = rect.createUnion(shapes[i].getBounds2D());
               }
            }

            double mag = 1.0;

            if (rect != null)
            {
               if (rect.getWidth() > rect.getHeight())
               {
                  mag = (ICON_SIZE-ICON_BORDER)/rect.getWidth();
               }
               else
               {
                  mag = (ICON_SIZE-ICON_BORDER)/rect.getHeight();
               }

               AffineTransform af = new AffineTransform(
                 mag, 0, 0, mag,
                  0.5*ICON_SIZE - mag*(rect.getX()+0.5*rect.getWidth()),
                  0.5*ICON_SIZE - mag*(rect.getY()+0.5*rect.getHeight()));

               for (int i = 0; i < shapes.length; i++)
               {
                  g2.draw(af.createTransformedShape(shapes[i])); 
               }
            }
         }

         g2.dispose();

         icon = new ImageIcon(image, getPresentationName());

         BufferedImage selectedImage 
            = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

         g2 = (Graphics2D)selectedImage.getGraphics();

         if (g2 != null)
         {
            g2.setRenderingHints(ResultPanel.RENDER_HINTS);

            g2.drawImage(image, 0, 0, null);
            g2.setColor(Color.DARK_GRAY);

            g2.setStroke(new BasicStroke((float)ICON_BORDER));
            g2.drawRect(0, 0, ICON_SIZE, ICON_SIZE);
            g2.dispose();

            selectedIcon = new ImageIcon(selectedImage, getPresentationName());
         }
      }
   }

   public Icon getIcon()
   {
      return icon;
   }

   public Icon getSelectedIcon()
   {
      return selectedIcon;
   }

   public String getPresentationName()
   {
      return name;
   }

   public void redo() throws CannotRedoException
   {
      super.redo();

      if (resultList != null)
      {
         for (Result result : resultList)
         {
            dialog.addResult(result);
         }
      }

      dialog.updateShapes(newShapes);
   }

   public void undo() throws CannotUndoException
   {
      super.undo();

      if (resultList != null)
      {
         for (int i = resultList.size()-1; i >= 0; i--)
         {
            dialog.removeResult(resultList.get(i));
         }
      }

      dialog.updateShapes(oldShapes);
   }

   private String name;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> oldShapes=null, newShapes;
   private Vector<Result> resultList;

   private Icon icon, selectedIcon;

   public static final int ICON_SIZE=50, ICON_BORDER=2;
}

class ZoomWidget extends JPanel implements JDRApp
{
   public ZoomWidget(VectorizeBitmapDialog dialog, Component component)
   {
      super(new BorderLayout());
      this.dialog = dialog;
      this.component = component;
      JDRResources resources = dialog.getResources();

      zoomComp = new ZoomComponent(this);
      add(zoomComp, "Center");

      setOpaque(false);
      setAlignmentY(Component.CENTER_ALIGNMENT);
      zoomComp.setAlignmentY(Component.CENTER_ALIGNMENT);
   }

   public JDRResources getResources()
   {
      return dialog.getResources();
   }

   public ZoomValue getValue()
   {
      return zoomComp.getZoomValue();
   }

   public void setValue(ZoomValue newValue)
   {
      if (!newValue.equals(zoomComp.getZoomValue()))
      {
         updateCurrentFactor(newValue);
      }
   }

   public void setMagnification(ZoomWidget other)
   {
      if (this != other)
      {
         setValue(other.getValue());
      }
   }

   public double getCurrentMagnification()
   {
      return factor;
   }

   public void setCurrentMagnification(double mag)
   {
      factor = mag;
      zoomComp.setZoom(mag);

      dialog.zoomChanged(this);
   }

   public double zoomAction(ZoomValue zoomValue)
   {
      updateCurrentFactor(zoomValue);

      dialog.zoomChanged(this);

      return factor;
   }

   private void updateCurrentFactor(ZoomValue zoomValue)
   {
      String id = zoomValue.getActionCommand();

      if (id.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         int width = dialog.getImageWidth();

         if (width != 0)
         {
            Dimension dim = component.getSize();
            factor = dim.getWidth()/(double)width;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         int height = dialog.getImageHeight();

         if (height != 0)
         {
            Dimension dim = component.getSize();
            factor = dim.getHeight()/(double)height;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         int width = dialog.getImageWidth();
         int height = dialog.getImageHeight();

         if (height == 0 || width == 0)
         {
            factor = 1.0;
         }
         else
         {
            Dimension dim = component.getSize();

            if (height > width)
            {
               factor = dim.getHeight()/(double)height;
            }
            else
            {
               factor = dim.getWidth()/(double)width;
            }
         }
      }
      else if (zoomValue instanceof PercentageZoomValue)
      {
         factor = ((PercentageZoomValue)zoomValue).getValue();
      }

      zoomComp.setZoom(zoomValue, factor);
   }

   public void showZoomChooser()
   {
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);

      zoomComp.setEnabled(enable);
   }

   private VectorizeBitmapDialog dialog;

   private ZoomComponent zoomComp;
   private Component component;
   private double factor=1.0;
}

class ScanImagePanel extends JPanel implements ActionListener,ChangeListener
{
   public ScanImagePanel(ControlPanel controlPanel)
   {
      super();
      this.controlPanel = controlPanel;
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);
      JDRResources resources = controlPanel.getResources();

      doScanImageCheckBox = resources.createAppCheckBox(
         "vectorize.scan_image", true, this);
      doScanImageCheckBox.setEnabled(false);
      add(doScanImageCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      foregroundLabel = resources.createAppLabel("vectorize.foreground");
      subPanel.add(foregroundLabel);

      foregroundPanel = new JPanel();
      foregroundPanel.setOpaque(false);
      foregroundPanel.setPreferredSize(new Dimension(20,20));
      foregroundPanel.setMaximumSize(new Dimension(20,20));
      subPanel.add(foregroundPanel);

      foregroundButton = resources.createDialogButton("vectorize",
          "choose_colour", this, null);
      subPanel.add(foregroundButton);

      foregroundChooser = new JColorChooser(Color.BLACK);

      colourPicker = resources.createToggleButton("vectorize", 
         "pipet_selector", null);
      colourPicker.addChangeListener(this);
      subPanel.add(colourPicker);

      fuzzSpinnerModel = new SpinnerNumberModel(0.2, 0.0, 1.0, 0.1);

      fuzzLabel = resources.createAppLabel("vectorize.fuzz");
      subPanel.add(fuzzLabel);

      fuzzSpinner = controlPanel.createSpinner(fuzzLabel, fuzzSpinnerModel);
      subPanel.add(fuzzSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      sampleWidthLabel = resources.createAppLabel("vectorize.sample_width");
      subPanel.add(sampleWidthLabel);

      sampleWidthSpinnerModel = new SpinnerNumberModel(10, 2, 50, 1);

      sampleWidthSpinner = controlPanel.createSpinner(
        sampleWidthLabel, sampleWidthSpinnerModel);
      subPanel.add(sampleWidthSpinner);

      subPanel.add(Box.createHorizontalStrut(10));

      sampleHeightLabel = resources.createAppLabel("vectorize.sample_height");
      subPanel.add(sampleHeightLabel);

      sampleHeightSpinnerModel = new SpinnerNumberModel(10, 2, 50, 1);

      sampleHeightSpinner = controlPanel.createSpinner(
        sampleHeightLabel, sampleHeightSpinnerModel);
      subPanel.add(sampleHeightSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      ButtonGroup bg = new ButtonGroup();

      scanAllButton = resources.createAppRadioButton("vectorize", "scan_all", 
         bg, true, this);
      subPanel.add(scanAllButton);

      scanRegionButton = resources.createAppRadioButton(
        "vectorize", "scan_region", bg, false, this);
      subPanel.add(scanRegionButton);

      regionPicker = resources.createToggleButton("vectorize", "select_region", this);
      subPanel.add(regionPicker);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      subtractLastScanButton = resources.createAppJButton(
        "vectorize", "subtract_last_scan", this);
      subPanel.add(subtractLastScanButton);

      subtractLastScanBorderCheckBox = resources.createAppCheckBox(
        "vectorize", "subtract_last_scan_border", true, this);
      subPanel.add(subtractLastScanBorderCheckBox);

      subtractLastScanBorderModel = new SpinnerNumberModel(
         (getSampleWidth()+getSampleHeight())/4, 0, 50, 1);

      subtractLastScanBorderField = new JSpinner(
         subtractLastScanBorderModel);
      subPanel.add(subtractLastScanBorderField);

      subPanel.add(Box.createHorizontalGlue());
   }

   public void stateChanged(ChangeEvent evt)
   {
      Object src = evt.getSource();

      if (src == doScanImageCheckBox && doScanImageCheckBox.isEnabled())
      {
         boolean enable = doScanImageCheckBox.isSelected();

         fuzzSpinner.setEnabled(enable);
         fuzzLabel.setEnabled(enable);

         sampleWidthSpinner.setEnabled(enable);
         sampleWidthLabel.setEnabled(enable);
         sampleHeightSpinner.setEnabled(enable);
         sampleHeightLabel.setEnabled(enable);

         foregroundButton.setEnabled(enable);
         foregroundLabel.setEnabled(enable);
         colourPicker.setEnabled(enable);

         scanAllButton.setEnabled(enable);
         scanRegionButton.setEnabled(enable);
         regionPicker.setEnabled(enable && scanRegionButton.isSelected());

         subtractLastScanButton.setEnabled(enable);
         subtractLastScanBorderCheckBox.setEnabled(enable);

         if (enable && subtractLastScanBorderCheckBox.isSelected())
         {
            subtractLastScanBorderField.setEnabled(true);
         }
         else
         {
            subtractLastScanBorderField.setEnabled(false);
         }

         controlPanel.updateTaskButton();
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object src = evt.getSource();
      String command = evt.getActionCommand();

      if (command == null)
      {
         command = "";
      }

      if (command.equals("choose_colour"))
      {
         Color col = foregroundChooser.showDialog(this, 
            controlPanel.getResources().getString("vectorize.select_foreground"), 
            getImageForeground());

         if (col != null)
         {
            setImageForeground(col);
         }
      }
      else if (command.equals("subtract_last_scan"))
      {
         VectorizeBitmapDialog dialog = controlPanel.getDialog();
         region = dialog.subtractLastScannedRegion();

         if (region != null)
         {
            scanRegionButton.setSelected(true);
         }
      }
      else if (src == colourPicker)
      {
         controlPanel.colourPickerChange(colourPicker.isSelected());
      }
      else if (src == regionPicker)
      {
         controlPanel.regionPickerChange(regionPicker.isSelected());
      }
      else if ((src == scanAllButton || src == scanRegionButton)
         && regionPicker != null)
      {
         if (scanAllButton.isSelected())
         {
            regionPicker.setSelected(false);
            regionPicker.setEnabled(false);
            controlPanel.imagePanelClearRegion();
            region = null;
         }
         else if (src == scanRegionButton)
         {
            regionPicker.setEnabled(true);

            controlPanel.getDialog().imagePanelRestoreOldRegion();
         }
      }
      else if (src == subtractLastScanBorderCheckBox)
      {
         if (subtractLastScanBorderCheckBox.isEnabled()
              && subtractLastScanBorderCheckBox.isSelected())
         {
            subtractLastScanBorderField.setEnabled(true);
         }
         else
         {
            subtractLastScanBorderField.setEnabled(false);
         }
      }
   }

   public void colourPickerChoice(Color colour)
   {
      setImageForeground(colour);
      colourPicker.setSelected(false);
   }

   public boolean isColourPickerOn()
   {
      return colourPicker.isSelected();
   }

   public Area getRegion()
   {
      return region;
   }

   public void regionPickerChoice(Shape rect, int action)
   {
      Area area;

      if (rect instanceof Area)
      {
         area = (Area)rect;
      }
      else
      {
         area = new Area(rect);
      }

      switch (action)
      {
         case REGION_PICKER_SET:
            region = area;
         break;
         case REGION_PICKER_ADD:
            if (region == null)
            {
               region = area;
            }
            else
            {
               region.add(area);
            }
         break;
         case REGION_PICKER_SUBTRACT:
            if (region == null)
            {
               VectorizeBitmapDialog dialog = controlPanel.getDialog();

               region = new Area(new Rectangle2D.Double(0, 0,
                dialog.getImageWidth(), dialog.getImageHeight()));
            }

            region.subtract(area);
         break;
         default:
            throw new IllegalArgumentException("Invalid region picker action "
             + action);
      }
   }

   public boolean isRegionPickerOn()
   {
      return regionPicker.isSelected();
   }

   public void setSelected(boolean selected)
   {
      setSelected(selected, false);
   }

   public void setSelected(boolean selected, boolean override)
   {
      if (override || doScanImageCheckBox.isEnabled())
      {
         doScanImageCheckBox.setSelected(selected);
      }

      if (regionPicker.isSelected())
      {
         regionPicker.setSelected(false);
      }
   }

   public void updateWidgets(boolean taskInProgress, boolean isVectorized)
   {
      if (!isVectorized)
      {
         doScanImageCheckBox.setSelected(true);
         doScanImageCheckBox.setEnabled(false);
         doScanImageCheckBox.setEnabled(false);
      }
      else
      {
         doScanImageCheckBox.setEnabled(!taskInProgress);
      }

      boolean enable = !taskInProgress 
        && doScanImageCheckBox.isSelected();

      if ((!enable || !doScanImageCheckBox.isSelected()))
      {
         if (colourPicker.isSelected())
         {
            colourPicker.setSelected(false);
         }

         if (regionPicker.isSelected())
         {
            regionPicker.setSelected(false);
         }
      }

      fuzzSpinner.setEnabled(enable);
      fuzzLabel.setEnabled(enable);

      sampleWidthSpinner.setEnabled(enable);
      sampleHeightSpinner.setEnabled(enable);
      sampleWidthLabel.setEnabled(enable);
      sampleHeightLabel.setEnabled(enable);

      foregroundButton.setEnabled(enable);
      foregroundLabel.setEnabled(enable);
      colourPicker.setEnabled(enable);

      scanAllButton.setEnabled(enable);
      scanRegionButton.setEnabled(enable);
      regionPicker.setEnabled(enable && scanRegionButton.isSelected());

      subtractLastScanButton.setEnabled(enable 
        && controlPanel.getDialog().getLastScannedRegion() != null);
   }

   public boolean isScanImageOn()
   {
      return doScanImageCheckBox.isSelected();
   }

   public double getFuzz()
   {
      return fuzzSpinnerModel.getNumber().doubleValue();
   }

   public int getSampleWidth()
   {
      return sampleWidthSpinnerModel.getNumber().intValue();
   }

   public int getSampleHeight()
   {
      return sampleHeightSpinnerModel.getNumber().intValue();
   }

   public int getSubtractLastScanBorder()
   {
      return subtractLastScanBorderCheckBox.isSelected() ?
         subtractLastScanBorderModel.getNumber().intValue() : 0;
   }

   public Color getImageForeground()
   {
      return imageForeground;
   }

   public void setImageForeground(Color col)
   {
      imageForeground = col;

      if (col == null)
      {
         foregroundPanel.setOpaque(false);
      }
      else
      {
         foregroundPanel.setBackground(col);
         foregroundPanel.setOpaque(true);
      }
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         foregroundPanel.setOpaque(false);
         imageForeground = null;

         if (colourPicker.isSelected())
         {
            colourPicker.setSelected(false);
            controlPanel.colourPickerChange(false);
         }

         foregroundChooser.setColor(Color.BLACK);

         fuzzSpinnerModel.setValue(Double.valueOf(2.0));
         sampleWidthSpinnerModel.setValue(Integer.valueOf(10));
         sampleHeightSpinnerModel.setValue(Integer.valueOf(10));

         if (regionPicker.isSelected())
         {
            regionPicker.setSelected(false);
            controlPanel.regionPickerChange(false);
         }

         if (!scanAllButton.isSelected())
         {
            scanAllButton.setSelected(true);
            regionPicker.setEnabled(false);
            controlPanel.imagePanelClearRegion();
         }

         region = null;

         subtractLastScanBorderModel.setValue(
            Integer.valueOf((getSampleWidth()+getSampleHeight())/4));

         if (subtractLastScanBorderCheckBox.isSelected())
         {
            subtractLastScanBorderCheckBox.setSelected(false);
            subtractLastScanBorderField.setEnabled(false);
         }
      }
      else if (region != null)
      {
         VectorizeBitmapDialog dialog = controlPanel.getDialog();
         int width = dialog.getImageWidth();
         int height = dialog.getImageHeight();

         Rectangle bounds = region.getBounds();

         if (bounds.getX()+bounds.getWidth() > width
          || bounds.getY()+bounds.getHeight() > height)
         {
            region = null;
         }
      }
   }

   private JLabel fuzzLabel, foregroundLabel, sampleWidthLabel, sampleHeightLabel;

   private JCheckBox doScanImageCheckBox;

   private JSpinner fuzzSpinner, sampleWidthSpinner, sampleHeightSpinner;

   private SpinnerNumberModel fuzzSpinnerModel, sampleWidthSpinnerModel,
     sampleHeightSpinnerModel;

   private JPanel foregroundPanel;

   private JDRButton foregroundButton;

   private JColorChooser foregroundChooser;

   private JDRToggleButton colourPicker, regionPicker;

   private Area region;

   private Color imageForeground=null;

   private JRadioButton scanAllButton, scanRegionButton;

   private JButton subtractLastScanButton;

   private JCheckBox subtractLastScanBorderCheckBox;

   private JSpinner subtractLastScanBorderField;

   private SpinnerNumberModel subtractLastScanBorderModel;

   private ControlPanel controlPanel;

   public static final int REGION_PICKER_SET=0, REGION_PICKER_ADD=1,
    REGION_PICKER_SUBTRACT=2;
}

class OptimizeLinesPanel extends JPanel implements ChangeListener
{
   public OptimizeLinesPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doOptimizeCheckBox = resources.createAppCheckBox(
        "vectorize.optimize_lines", true, this);
      add(doOptimizeCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      gradientEpsilonSpinnerModel = new SpinnerNumberModel(0.01, 0.0, 100.0,
         0.1);

      gradientEpsilonLabel = resources.createAppLabel(
        "vectorize.gradient_threshold");
      subPanel.add(gradientEpsilonLabel);

      gradientEpsilonSpinner = controlPanel.createSpinner(
         gradientEpsilonLabel, gradientEpsilonSpinnerModel);
      gradientEpsilonSpinner.setMaximumSize(
         gradientEpsilonSpinner.getPreferredSize());
      subPanel.add(gradientEpsilonSpinner);
   }

   public void stateChanged(ChangeEvent evt)
   {
      if (evt.getSource() == doOptimizeCheckBox)
      {
         boolean enable = doOptimizeCheckBox.isSelected();

         gradientEpsilonSpinner.setEnabled(enable);
         gradientEpsilonLabel.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
   }

   public void setSelected(boolean selected)
   {
      doOptimizeCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || controlPanel.isScanImageOn()));

      doOptimizeCheckBox.setEnabled(enable);

      enable = enable && doOptimizeCheckBox.isSelected();

      gradientEpsilonSpinner.setEnabled(enable);
      gradientEpsilonLabel.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         gradientEpsilonSpinnerModel.setValue(Double.valueOf(0.01));
      }
   }

   public double getGradientEpsilon()
   {
      return gradientEpsilonSpinnerModel.getNumber().doubleValue();
   }

   public boolean isOptimizeOn()
   {
      return doOptimizeCheckBox.isSelected();
   }

   private JLabel gradientEpsilonLabel;

   private SpinnerNumberModel gradientEpsilonSpinnerModel; 

   private JSpinner gradientEpsilonSpinner;

   private JCheckBox doOptimizeCheckBox;

   private ControlPanel controlPanel;
}

class SplitSubPathsPanel extends JPanel implements ChangeListener
{
   public SplitSubPathsPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doSplitSubPathsCheckBox = resources.createAppCheckBox(
       "vectorize.split_subpaths", true, this);
      add(doSplitSubPathsCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      removeTinyPathsCheckBox = resources.createAppCheckBox(
        "vectorize.remove_tiny_subpaths", true, this);
      subPanel.add(removeTinyPathsCheckBox);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      minTinyAreaLabel = resources.createAppLabel(
        "vectorize.tiny_subpath.bounding_area_max");
      subPanel.add(minTinyAreaLabel);

      minTinyAreaSpinnerModel = new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0);

      minTinyAreaSpinner = controlPanel.createSpinner(
         minTinyAreaLabel, minTinyAreaSpinnerModel);
      minTinyAreaSpinner.setMaximumSize(
         minTinyAreaSpinner.getPreferredSize());
      subPanel.add(minTinyAreaSpinner);

      minTinySizeLabel = resources.createAppLabel(
         "vectorize.tiny_subpath.size_max");
      subPanel.add(minTinySizeLabel);

      minTinySizeSpinnerModel = new SpinnerNumberModel(10, 0, 100, 1);

      minTinySizeSpinner = controlPanel.createSpinner(
         minTinySizeLabel, minTinySizeSpinnerModel);
      minTinySizeSpinner.setMaximumSize(
         minTinySizeSpinner.getPreferredSize());
      subPanel.add(minTinySizeSpinner);

      splitTypeLabel = resources.createAppLabel("vectorize.split_type");
      add(splitTypeLabel);

      ButtonGroup bg = new ButtonGroup();

      exteriorSplitOnlyButton = resources.createAppRadioButton(
       "vectorize.split.exterior", bg, false, null);
      add(exteriorSplitOnlyButton);

      evenInteriorSplitButton = resources.createAppRadioButton(
         "vectorize.split.even_interior", bg, true, null);
      add(evenInteriorSplitButton);

      splitAllButton = resources.createAppRadioButton(
         "vectorize.split.all", bg, false, null);
      add(splitAllButton);
   }

   public void stateChanged(ChangeEvent evt)
   {
      Object src = evt.getSource();

      if (src == doSplitSubPathsCheckBox)
      {
         boolean enable = doSplitSubPathsCheckBox.isSelected();

         splitTypeLabel.setEnabled(enable);
         exteriorSplitOnlyButton.setEnabled(enable);
         evenInteriorSplitButton.setEnabled(enable);
         splitAllButton.setEnabled(enable);

         removeTinyPathsCheckBox.setEnabled(enable);

         enable = enable && removeTinyPathsCheckBox.isSelected();

         minTinyAreaSpinner.setEnabled(enable);
         minTinyAreaLabel.setEnabled(enable);
         minTinySizeSpinner.setEnabled(enable);
         minTinySizeLabel.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
      else if (src == removeTinyPathsCheckBox)
      {
         boolean enable = removeTinyPathsCheckBox.isEnabled() 
             && removeTinyPathsCheckBox.isSelected();

         minTinyAreaSpinner.setEnabled(enable);
         minTinyAreaLabel.setEnabled(enable);
         minTinySizeSpinner.setEnabled(enable);
         minTinySizeLabel.setEnabled(enable);
      }
   }

   public void setSelected(boolean selected)
   {
      doSplitSubPathsCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || controlPanel.isScanImageOn()));

      doSplitSubPathsCheckBox.setEnabled(enable);

      enable = enable && doSplitSubPathsCheckBox.isSelected();

      splitTypeLabel.setEnabled(enable);
      exteriorSplitOnlyButton.setEnabled(enable);
      evenInteriorSplitButton.setEnabled(enable);
      splitAllButton.setEnabled(enable);

      removeTinyPathsCheckBox.setEnabled(enable);

      enable = enable && removeTinyPathsCheckBox.isSelected();

      minTinyAreaSpinner.setEnabled(enable);
      minTinyAreaLabel.setEnabled(enable);
      minTinySizeSpinner.setEnabled(enable);
      minTinySizeLabel.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         minTinyAreaSpinnerModel.setValue(Double.valueOf(10.0));
         minTinySizeSpinnerModel.setValue(Integer.valueOf(10));

         if (!removeTinyPathsCheckBox.isSelected())
         {
            removeTinyPathsCheckBox.setSelected(true);

            boolean enable = removeTinyPathsCheckBox.isEnabled();

            minTinyAreaSpinner.setEnabled(enable);
            minTinyAreaLabel.setEnabled(enable);
            minTinySizeSpinner.setEnabled(enable);
            minTinySizeLabel.setEnabled(enable);
         }

         if (!evenInteriorSplitButton.isSelected())
         {
            evenInteriorSplitButton.setSelected(true);
         }
      }
   }

   public boolean isRemoveMinTinySubPathsOn()
   {
      return removeTinyPathsCheckBox.isSelected();
   }

   public double getMinTinySubPathArea()
   {
      return minTinyAreaSpinnerModel.getNumber().doubleValue();
   }

   public int getMinTinySubPathSize()
   {
      return minTinySizeSpinnerModel.getNumber().intValue();
   }

   public boolean isSplitSubPathsOn()
   {
      return doSplitSubPathsCheckBox.isSelected();
   }

   public int getSplitType()
   {
      if (exteriorSplitOnlyButton.isSelected())
      {
         return SplitSubPaths.SPLIT_EXTERIOR_ONLY;
      }
      else if (evenInteriorSplitButton.isSelected())
      {
         return SplitSubPaths.EVEN_INTERIOR_SPLIT;
      }
      else
      {
         return SplitSubPaths.SPLIT_ALL;
      }
   }

   private JLabel splitTypeLabel, minTinyAreaLabel,
     minTinySizeLabel;

   private SpinnerNumberModel   
      minTinyAreaSpinnerModel, minTinySizeSpinnerModel;

   private JSpinner minTinyAreaSpinner, minTinySizeSpinner;

   private JCheckBox doSplitSubPathsCheckBox, removeTinyPathsCheckBox;

   private JRadioButton exteriorSplitOnlyButton, evenInteriorSplitButton, splitAllButton;

   private ControlPanel controlPanel;
}

class LineDetectionPanel extends JPanel implements ChangeListener,ActionListener
{
   public LineDetectionPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doLineDetectionCheckBox = resources.createAppCheckBox(
         "vectorize.line_detection", true, this);
      add(doLineDetectionCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      deltaThresholdSpinnerModel = new SpinnerNumberModel(1.5, 0.0, 100.0, 0.5);

      deltaThresholdLabel = resources.createAppLabel("vectorize.delta_threshold");
      subPanel.add(deltaThresholdLabel);

      deltaThresholdSpinner = controlPanel.createSpinner(
         deltaThresholdLabel, deltaThresholdSpinnerModel);
      deltaThresholdSpinner.setMaximumSize(
         deltaThresholdSpinner.getPreferredSize());
      subPanel.add(deltaThresholdSpinner);

      subPanel.add(Box.createHorizontalGlue());

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      ButtonGroup bg = new ButtonGroup();

      fixedLineWidthButton = resources.createAppRadioButton(
        "vectorize", "fixed_line_width", bg, false, this);
      subPanel.add(fixedLineWidthButton);

      fixedLineWidthSpinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
      fixedLineWidthSpinner = new JSpinner(fixedLineWidthSpinnerModel);
      fixedLineWidthSpinner.setEnabled(false);
      fixedLineWidthSpinner.setMaximumSize(
         fixedLineWidthSpinner.getPreferredSize());
      subPanel.add(fixedLineWidthSpinner);

      subPanel.add(Box.createHorizontalGlue());

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      relativeLineWidthButton = resources.createAppRadioButton(
        "vectorize", "relative_line_width", bg, true, this);
      subPanel.add(relativeLineWidthButton);

      roundRelativeLineWidthCheckBox = resources.createAppCheckBox(
        "vectorize", "round_relative_line_width", true, null);
      subPanel.add(roundRelativeLineWidthCheckBox);

      detectIntersections = resources.createAppCheckBox(
         "vectorize.detect_intersections", true, this);
      add(detectIntersections);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      deltaVarianceThresholdSpinnerModel
          = new SpinnerNumberModel(1.0, 0.0, 100.0, 1.0);

      deltaVarianceThresholdLabel = resources.createAppLabel(
         "vectorize.variance_threshold");
      subPanel.add(deltaVarianceThresholdLabel);

      deltaVarianceThresholdSpinner = controlPanel.createSpinner(
         deltaVarianceThresholdLabel, deltaVarianceThresholdSpinnerModel);
      deltaVarianceThresholdSpinner.setMaximumSize(
         deltaVarianceThresholdSpinner.getPreferredSize());
      subPanel.add(deltaVarianceThresholdSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      spikeReturnDistanceSpinnerModel = new SpinnerNumberModel(4.0, 1.0, 20.0, 0.5);

      spikeReturnDistanceLabel = resources.createAppLabel(
         "vectorize.spike_return_distance");
      subPanel.add(spikeReturnDistanceLabel);

      spikeReturnDistanceSpinner = controlPanel.createSpinner(
         spikeReturnDistanceLabel, spikeReturnDistanceSpinnerModel);
      spikeReturnDistanceSpinner.setMaximumSize(
         spikeReturnDistanceSpinner.getPreferredSize());
      subPanel.add(spikeReturnDistanceSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      minStubLengthSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 20.0, 0.5);

      minStubLengthLabel = resources.createAppLabel(
         "vectorize.min_stub_length");
      subPanel.add(minStubLengthLabel);

      minStubLengthSpinner = controlPanel.createSpinner(
         minStubLengthLabel, minStubLengthSpinnerModel);
      minStubLengthSpinner.setMaximumSize(
         minStubLengthSpinner.getPreferredSize());
      subPanel.add(minStubLengthSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      tinyStepThresholdSpinnerModel = new SpinnerNumberModel(3.5, 1.0, 20.0, 0.5);

      tinyStepThresholdLabel = resources.createAppLabel(
         "vectorize.line_detect.tiny_step");
      subPanel.add(tinyStepThresholdLabel);

      tinyStepThresholdSpinner = controlPanel.createSpinner(
         tinyStepThresholdLabel, tinyStepThresholdSpinnerModel);
      tinyStepThresholdSpinner.setMaximumSize(
         tinyStepThresholdSpinner.getPreferredSize());
      subPanel.add(tinyStepThresholdSpinner);
   }

   public void stateChanged(ChangeEvent evt)
   {
      Object src = evt.getSource();

      if (src == doLineDetectionCheckBox)
      {
         boolean enable = doLineDetectionCheckBox.isSelected();

         deltaThresholdSpinner.setEnabled(enable);
         deltaThresholdLabel.setEnabled(enable);

         fixedLineWidthButton.setEnabled(enable);
         relativeLineWidthButton.setEnabled(enable);

         if (enable)
         {
            if (fixedLineWidthButton.isSelected())
            {
               fixedLineWidthSpinner.setEnabled(true);
               roundRelativeLineWidthCheckBox.setEnabled(false);
            }
            else
            {
               fixedLineWidthSpinner.setEnabled(false);
               roundRelativeLineWidthCheckBox.setEnabled(true);
            }
         }
         else
         {
            fixedLineWidthSpinner.setEnabled(false);
            roundRelativeLineWidthCheckBox.setEnabled(false);
         }

         detectIntersections.setEnabled(enable);

         enable = enable && detectIntersections.isSelected();

         deltaVarianceThresholdSpinner.setEnabled(enable);
         deltaVarianceThresholdLabel.setEnabled(enable);

         spikeReturnDistanceSpinner.setEnabled(enable);
         spikeReturnDistanceLabel.setEnabled(enable);

         minStubLengthSpinner.setEnabled(enable);
         minStubLengthLabel.setEnabled(enable);

         tinyStepThresholdSpinner.setEnabled(enable);
         tinyStepThresholdLabel.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
      else if (src == detectIntersections)
      {
         boolean enable = detectIntersections.isEnabled() 
            && detectIntersections.isSelected();

         deltaVarianceThresholdSpinner.setEnabled(enable);
         deltaVarianceThresholdLabel.setEnabled(enable);

         spikeReturnDistanceSpinner.setEnabled(enable);
         spikeReturnDistanceLabel.setEnabled(enable);

         minStubLengthSpinner.setEnabled(enable);
         minStubLengthLabel.setEnabled(enable);

         tinyStepThresholdSpinner.setEnabled(enable);
         tinyStepThresholdLabel.setEnabled(enable);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if ((action.equals("fixed_line_width") || action.equals("relative_line_width")))
      {
         if (fixedLineWidthButton.isSelected())
         {
            fixedLineWidthSpinner.setEnabled(true);
            roundRelativeLineWidthCheckBox.setEnabled(false);
         }
         else
         {
            fixedLineWidthSpinner.setEnabled(false);
            roundRelativeLineWidthCheckBox.setEnabled(true);
         }
      }
   }

   public void setSelected(boolean selected)
   {
      doLineDetectionCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || controlPanel.isScanImageOn()));

      doLineDetectionCheckBox.setEnabled(enable);

      enable = enable && doLineDetectionCheckBox.isSelected();

      deltaThresholdSpinner.setEnabled(enable);
      deltaThresholdLabel.setEnabled(enable);

      fixedLineWidthButton.setEnabled(enable);
      relativeLineWidthButton.setEnabled(enable);

      if (enable)
      {
         if (fixedLineWidthButton.isSelected())
         {
            fixedLineWidthSpinner.setEnabled(true);
            roundRelativeLineWidthCheckBox.setEnabled(false);
         }
         else
         {
            fixedLineWidthSpinner.setEnabled(false);
            roundRelativeLineWidthCheckBox.setEnabled(true);
         }
      }
      else
      {
         fixedLineWidthSpinner.setEnabled(false);
         roundRelativeLineWidthCheckBox.setEnabled(false);
      }

      detectIntersections.setEnabled(enable);

      enable = enable && detectIntersections.isSelected();

      deltaVarianceThresholdSpinner.setEnabled(enable);
      deltaVarianceThresholdLabel.setEnabled(enable);

      spikeReturnDistanceSpinner.setEnabled(enable);
      spikeReturnDistanceLabel.setEnabled(enable);

      minStubLengthSpinner.setEnabled(enable);
      minStubLengthLabel.setEnabled(enable);

      tinyStepThresholdSpinner.setEnabled(enable);
      tinyStepThresholdLabel.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         deltaThresholdSpinnerModel.setValue(Double.valueOf(1.5));
         fixedLineWidthSpinnerModel.setValue(Integer.valueOf(1));

         if (fixedLineWidthSpinner.isEnabled())
         {
            fixedLineWidthSpinner.setEnabled(false);
         }

         if (!relativeLineWidthButton.isSelected())
         {
            relativeLineWidthButton.setSelected(true);

            if (!roundRelativeLineWidthCheckBox.isEnabled())
            {
               roundRelativeLineWidthCheckBox.setEnabled(true);
            }
         }

         if (!roundRelativeLineWidthCheckBox.isSelected())
         {
            roundRelativeLineWidthCheckBox.setSelected(true);
         }

         if (!detectIntersections.isSelected())
         {
            detectIntersections.setSelected(true);
         }

         deltaVarianceThresholdSpinnerModel.setValue(Double.valueOf(1.0));
         spikeReturnDistanceSpinnerModel.setValue(Double.valueOf(4.0));
         minStubLengthSpinnerModel.setValue(Double.valueOf(2.0));
         tinyStepThresholdSpinnerModel.setValue(Double.valueOf(3.5));
      }
   }

   public boolean isFixedLineWidth()
   {
      return fixedLineWidthButton.isSelected();
   }

   public double getFixedLineWidth()
   {
      return fixedLineWidthSpinnerModel.getNumber().doubleValue();
   }

   public boolean isRoundRelativeOn()
   {
      return roundRelativeLineWidthCheckBox.isSelected();
   }

   public double getDeltaThreshold()
   {
      return deltaThresholdSpinnerModel.getNumber().doubleValue();
   }

   public double getDeltaVarianceThreshold()
   {
      return deltaVarianceThresholdSpinnerModel.getNumber().doubleValue();
   }

   public double getSpikeReturnDistance()
   {
      return spikeReturnDistanceSpinnerModel.getNumber().doubleValue();
   }

   public double getMinimumStubLength()
   {
      return minStubLengthSpinnerModel.getNumber().doubleValue();
   }

   public double getTinyStepThreshold()
   {
      return tinyStepThresholdSpinnerModel.getNumber().doubleValue();
   }

   public boolean isLineDetectionOn()
   {
      return doLineDetectionCheckBox.isSelected();
   }

   public boolean isIntersectionDetectionOn()
   {
      return detectIntersections.isSelected();
   }

   private JLabel deltaThresholdLabel, deltaVarianceThresholdLabel, 
    tinyStepThresholdLabel, spikeReturnDistanceLabel,
    minStubLengthLabel;

   private SpinnerNumberModel deltaThresholdSpinnerModel,
      deltaVarianceThresholdSpinnerModel, tinyStepThresholdSpinnerModel,
      spikeReturnDistanceSpinnerModel,
      minStubLengthSpinnerModel,
      fixedLineWidthSpinnerModel;

   private JSpinner deltaThresholdSpinner, deltaVarianceThresholdSpinner,
    tinyStepThresholdSpinner, spikeReturnDistanceSpinner,
    fixedLineWidthSpinner, minStubLengthSpinner;

   private JCheckBox doLineDetectionCheckBox, detectIntersections,
    roundRelativeLineWidthCheckBox;

   private JRadioButton fixedLineWidthButton, relativeLineWidthButton;

   private ControlPanel controlPanel;
}

class MergeNearPathsPanel extends JPanel implements ChangeListener
{
   public MergeNearPathsPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doMergeCheckBox = resources.createAppCheckBox(
        "vectorize.merge_nearpaths", true, this);
      add(doMergeCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      gapLabel = resources.createAppLabel("vectorize.merge_nearpaths.threshold");
      subPanel.add(gapLabel);

      gapModel = new SpinnerNumberModel(2.0, 1.0, 20.0, 1.0);
      gapSpinner = new JSpinner(gapModel);
      gapSpinner.setMaximumSize(gapSpinner.getPreferredSize());
      subPanel.add(gapSpinner);

      subPanel.add(Box.createHorizontalGlue());
   }

   public void stateChanged(ChangeEvent evt)
   {
      Object src = evt.getSource();

      if (src == doMergeCheckBox)
      {
         boolean enable = doMergeCheckBox.isSelected() 
                           && doMergeCheckBox.isEnabled();

         gapLabel.setEnabled(enable);
         gapSpinner.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
   }

   public boolean isMergeNearPathsOn()
   {
      return doMergeCheckBox.isSelected();
   }

   public double getGapThreshold()
   {
      return gapModel.getNumber().doubleValue();
   }

   public void updateWidgets(boolean taskInProgress, boolean isVectorized)
   {
      boolean enable = !taskInProgress;

      doMergeCheckBox.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         gapModel.setValue(Double.valueOf(2.0));
      }
   }

   public void setSelected(boolean selected)
   {
      doMergeCheckBox.setSelected(selected);
   }

   private JLabel gapLabel;
   private SpinnerNumberModel gapModel;
   private JSpinner gapSpinner;

   private ControlPanel controlPanel;
   private JCheckBox doMergeCheckBox;
}

class SmoothingPanel extends JPanel implements ChangeListener
{
   public SmoothingPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doSmoothingCheckBox = resources.createAppCheckBox(
        "vectorize.smooth_shapes", true, this);
      add(doSmoothingCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      tinyStepThresholdSpinnerModel = new SpinnerNumberModel(20.0, 0.0, 100.0, 1.0);

      tinyStepThresholdLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.tiny_step");
      subPanel.add(tinyStepThresholdLabel);

      tinyStepThresholdSpinner = controlPanel.createSpinner(
         tinyStepThresholdLabel, tinyStepThresholdSpinnerModel);
      subPanel.add(tinyStepThresholdSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      lengthThresholdSpinnerModel 
         = new SpinnerNumberModel(20.0, 0.0, 100.0, 1.0);

      lengthThresholdLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.length_threshold");
      subPanel.add(lengthThresholdLabel);

      lengthThresholdSpinner = controlPanel.createSpinner(
         lengthThresholdLabel, lengthThresholdSpinnerModel);
      subPanel.add(lengthThresholdSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      thresholdDiffSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 1.0);

      thresholdDiffLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.threshold_diff");
      subPanel.add(thresholdDiffLabel);

      thresholdDiffSpinner = controlPanel.createSpinner(
         thresholdDiffLabel, thresholdDiffSpinnerModel);
      subPanel.add(thresholdDiffSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      maxDeviationSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 1.0);

      maxDeviationLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.max_deviation");
      subPanel.add(maxDeviationLabel);

      maxDeviationSpinner = controlPanel.createSpinner(
         maxDeviationLabel, maxDeviationSpinnerModel);
      subPanel.add(maxDeviationSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      deviationEpsilonSpinnerModel = new SpinnerNumberModel(0.01, 0.0, 100.0, 0.01);

      deviationEpsilonLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.deviation_epsilon");
      subPanel.add(deviationEpsilonLabel);

      deviationEpsilonSpinner = controlPanel.createSpinner(
         deviationEpsilonLabel, deviationEpsilonSpinnerModel);
      subPanel.add(deviationEpsilonSpinner);

      tryBezierCheckBox = resources.createAppCheckBox(
         "vectorize.smooth_shapes.try_bezier", true, this);
      add(tryBezierCheckBox);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      curveGradientThresholdLabel = resources.createAppLabel(
        "vectorize.smooth_shapes.curve_gradient_threshold");
      subPanel.add(curveGradientThresholdLabel);

      NumberSpinnerField numField = new NumberSpinnerField(2.0, 0.0, 360, 1.0);
      numField.setMaximumSize(numField.getPreferredSize());

      gradientAngleThresholdPanel = new AnglePanel(
        resources.getMessageDictionary(), numField);
      subPanel.add(gradientAngleThresholdPanel);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      curveStationaryPtThresholdSpinnerModel
        = new SpinnerNumberModel(2.0, 0.0, 100.0, 1);

      curveStatPtLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.stat_pt_threshold");
      subPanel.add(curveStatPtLabel);

      curveStationaryPtThresholdSpinner = 
        controlPanel.createSpinner(curveStationaryPtThresholdSpinnerModel);
      curveStatPtLabel.setLabelFor(curveStationaryPtThresholdSpinner);
      subPanel.add(curveStationaryPtThresholdSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      curveThresholdDiffSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 1.0);

      curveThresholdDiffLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.curve_threshold_diff");
      subPanel.add(curveThresholdDiffLabel);

      curveThresholdDiffSpinner = controlPanel.createSpinner(
         curveThresholdDiffLabel, curveThresholdDiffSpinnerModel);
      subPanel.add(curveThresholdDiffSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      curveMinPointsSpinnerModel 
         = new SpinnerNumberModel(5, 3, 100, 1);

      curveMinPointsLabel = resources.createAppLabel(
         "vectorize.smooth_shapes.min_points");
      subPanel.add(curveMinPointsLabel);

      curveMinPointsSpinner = 
        controlPanel.createSpinner(curveMinPointsSpinnerModel);
      curveMinPointsLabel.setLabelFor(curveMinPointsSpinner);
      subPanel.add(curveMinPointsSpinner);

      Dimension prefSize = tinyStepThresholdLabel.getPreferredSize();
      Dimension maxPrefSize = prefSize;

      prefSize = lengthThresholdLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = thresholdDiffLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = maxDeviationLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = deviationEpsilonLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = curveGradientThresholdLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = curveStatPtLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = curveThresholdDiffLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = curveMinPointsLabel.getPreferredSize();

      if (prefSize.width > maxPrefSize.width)
      {
         maxPrefSize = prefSize;
      }

      prefSize = tinyStepThresholdLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      tinyStepThresholdLabel.setPreferredSize(prefSize);
      tinyStepThresholdLabel.setMaximumSize(prefSize);

      prefSize = lengthThresholdLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      lengthThresholdLabel.setPreferredSize(prefSize);
      lengthThresholdLabel.setMaximumSize(prefSize);

      prefSize = thresholdDiffLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      thresholdDiffLabel.setPreferredSize(prefSize);
      thresholdDiffLabel.setMaximumSize(prefSize);

      prefSize = maxDeviationLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      maxDeviationLabel.setPreferredSize(prefSize);
      maxDeviationLabel.setMaximumSize(prefSize);

      prefSize = deviationEpsilonLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      deviationEpsilonLabel.setPreferredSize(prefSize);
      deviationEpsilonLabel.setMaximumSize(prefSize);

      prefSize = curveGradientThresholdLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      curveGradientThresholdLabel.setPreferredSize(prefSize);
      curveGradientThresholdLabel.setMaximumSize(prefSize);

      prefSize = curveStatPtLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      curveStatPtLabel.setPreferredSize(prefSize);
      curveStatPtLabel.setMaximumSize(prefSize);

      prefSize = curveThresholdDiffLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      curveThresholdDiffLabel.setPreferredSize(prefSize);
      curveThresholdDiffLabel.setMaximumSize(prefSize);

      prefSize = curveMinPointsLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      curveMinPointsLabel.setPreferredSize(prefSize);
      curveMinPointsLabel.setMaximumSize(prefSize);
   }

   public void stateChanged(ChangeEvent evt)
   {
      Object src = evt.getSource();

      if (src == doSmoothingCheckBox)
      {
         updateWidgets();

         controlPanel.updateTaskButton();
      }
      else if (src == tryBezierCheckBox)
      {
         updateBezierWidgets();
      }
   }

   private void updateWidgets()
   {
      if (doSmoothingCheckBox.isEnabled())
      {
         boolean enable = doSmoothingCheckBox.isSelected();

         tinyStepThresholdSpinner.setEnabled(enable);
         lengthThresholdSpinner.setEnabled(enable);
         thresholdDiffSpinner.setEnabled(enable);
         curveThresholdDiffSpinner.setEnabled(enable);
         maxDeviationSpinner.setEnabled(enable);
         deviationEpsilonSpinner.setEnabled(enable);
         gradientAngleThresholdPanel.setEnabled(enable);
         curveStationaryPtThresholdSpinner.setEnabled(enable);
         curveMinPointsSpinner.setEnabled(enable);
         tinyStepThresholdLabel.setEnabled(enable);
         lengthThresholdLabel.setEnabled(enable);
         thresholdDiffLabel.setEnabled(enable);
         curveThresholdDiffLabel.setEnabled(enable);
         maxDeviationLabel.setEnabled(enable);
         deviationEpsilonLabel.setEnabled(enable);
         tryBezierCheckBox.setEnabled(enable);

         updateBezierWidgets();
      }
   }

   private void updateBezierWidgets()
   {
      boolean enable = doSmoothingCheckBox.isSelected() 
        && tryBezierCheckBox.isSelected();

      curveGradientThresholdLabel.setEnabled(enable);
      gradientAngleThresholdPanel.setEnabled(enable);
      curveMinPointsLabel.setEnabled(enable);
      curveMinPointsSpinner.setEnabled(enable);
      curveStatPtLabel.setEnabled(enable);
      curveStationaryPtThresholdSpinner.setEnabled(enable);
      curveThresholdDiffLabel.setEnabled(enable);
      curveThresholdDiffSpinner.setEnabled(enable);
   }

   public void setSelected(boolean selected)
   {
      doSmoothingCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || controlPanel.isScanImageOn()));

      doSmoothingCheckBox.setEnabled(enable);

      enable = enable && doSmoothingCheckBox.isSelected();

      tinyStepThresholdSpinner.setEnabled(enable);
      lengthThresholdSpinner.setEnabled(enable);
      thresholdDiffSpinner.setEnabled(enable);
      curveThresholdDiffSpinner.setEnabled(enable);
      maxDeviationSpinner.setEnabled(enable);
      deviationEpsilonSpinner.setEnabled(enable);
      gradientAngleThresholdPanel.setEnabled(enable);
      curveStationaryPtThresholdSpinner.setEnabled(enable);
      curveMinPointsSpinner.setEnabled(enable);
      tinyStepThresholdLabel.setEnabled(enable);
      lengthThresholdLabel.setEnabled(enable);
      thresholdDiffLabel.setEnabled(enable);
      curveThresholdDiffLabel.setEnabled(enable);
      maxDeviationLabel.setEnabled(enable);
      deviationEpsilonLabel.setEnabled(enable);

      tryBezierCheckBox.setEnabled(enable);

      enable = enable && tryBezierCheckBox.isSelected();

      curveGradientThresholdLabel.setEnabled(enable);
      curveMinPointsLabel.setEnabled(enable);
      curveStatPtLabel.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         tinyStepThresholdSpinnerModel.setValue(Double.valueOf(10.0));
         lengthThresholdSpinnerModel.setValue(Double.valueOf(20.0));
         thresholdDiffSpinnerModel.setValue(Double.valueOf(2.0));
         curveThresholdDiffSpinnerModel.setValue(Double.valueOf(2.0));
         maxDeviationSpinnerModel.setValue(Double.valueOf(2.0));
         deviationEpsilonSpinnerModel.setValue(Double.valueOf(0.01));

         gradientAngleThresholdPanel.setDegrees(2.0);
         curveMinPointsSpinnerModel.setValue(Integer.valueOf(5));
         curveStationaryPtThresholdSpinnerModel.setValue(Double.valueOf(2.0));

         if (!tryBezierCheckBox.isSelected())
         {
            tryBezierCheckBox.setSelected(true);
         }
      }
   }

   public boolean isSmoothingOn()
   {
      return doSmoothingCheckBox.isSelected();
   }

   public boolean isTryBezierOn()
   {
      return tryBezierCheckBox.isSelected();
   }

   public double getTinyStepThreshold()
   {
      return tinyStepThresholdSpinnerModel.getNumber().doubleValue();
   }

   public double getLengthThreshold()
   {
      return lengthThresholdSpinnerModel.getNumber().doubleValue();
   }

   public double getThresholdDiff()
   {
      return thresholdDiffSpinnerModel.getNumber().doubleValue();
   }

   public double getCurveThresholdDiff()
   {
      return curveThresholdDiffSpinnerModel.getNumber().doubleValue();
   }

   public double getMaxDeviation()
   {
      return maxDeviationSpinnerModel.getNumber().doubleValue();
   }

   public double getDeviationEpsilon()
   {
      return deviationEpsilonSpinnerModel.getNumber().doubleValue();
   }

   public double getCurveGradientThreshold()
   {
      return gradientAngleThresholdPanel.getValue().toRadians();
   }

   public double getCurveStationaryPtThreshold()
   {
      return curveStationaryPtThresholdSpinnerModel.getNumber().doubleValue();
   }

   public int getCurveMinPoints()
   {
      return curveMinPointsSpinnerModel.getNumber().intValue();
   }

   private JLabel tinyStepThresholdLabel, lengthThresholdLabel, thresholdDiffLabel,
    curveGradientThresholdLabel, curveMinPointsLabel, maxDeviationLabel,
    deviationEpsilonLabel, curveStatPtLabel, curveThresholdDiffLabel;

   private AnglePanel gradientAngleThresholdPanel;

   private SpinnerNumberModel  
     tinyStepThresholdSpinnerModel, lengthThresholdSpinnerModel,
     thresholdDiffSpinnerModel, maxDeviationSpinnerModel,
     deviationEpsilonSpinnerModel, curveStationaryPtThresholdSpinnerModel,
     curveMinPointsSpinnerModel, curveThresholdDiffSpinnerModel;

   private JSpinner curveMinPointsSpinner,
     thresholdDiffSpinner, lengthThresholdSpinner, tinyStepThresholdSpinner,
     maxDeviationSpinner, deviationEpsilonSpinner,
     curveStationaryPtThresholdSpinner, curveThresholdDiffSpinner;

   private JCheckBox doSmoothingCheckBox, tryBezierCheckBox;

   private ControlPanel controlPanel;
}

class RemoveTinyPathsPanel extends JPanel implements ChangeListener
{
   public RemoveTinyPathsPanel(ControlPanel controlPanel)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setAlignmentX(Component.LEFT_ALIGNMENT);

      this.controlPanel = controlPanel;
      JDRResources resources = controlPanel.getResources();

      doRemoveTinyPathsCheckBox = resources.createAppCheckBox(
        "vectorize.remove_tiny_paths", true, this);
      add(doRemoveTinyPathsCheckBox);

      JComponent subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      maxTinyPathsSpinnerModel = new SpinnerNumberModel(4.0, 0.0, 100.0, 1.0);

      maxTinyPathsLabel = resources.createAppLabel(
         "vectorize.remove_tiny_paths.min_area");
      subPanel.add(maxTinyPathsLabel);

      maxTinyPathsSpinner = controlPanel.createSpinner(
         maxTinyPathsLabel, maxTinyPathsSpinnerModel);
      subPanel.add(maxTinyPathsSpinner);
   }

   public void stateChanged(ChangeEvent evt)
   {
      if (evt.getSource() == doRemoveTinyPathsCheckBox)
      {
         boolean enable = doRemoveTinyPathsCheckBox.isSelected();

         maxTinyPathsSpinner.setEnabled(enable);
         maxTinyPathsLabel.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
   }

   public void setSelected(boolean selected)
   {
      doRemoveTinyPathsCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || controlPanel.isScanImageOn()));

      doRemoveTinyPathsCheckBox.setEnabled(enable);

      enable = enable && doRemoveTinyPathsCheckBox.isSelected();

      maxTinyPathsSpinner.setEnabled(enable);
      maxTinyPathsLabel.setEnabled(enable);
   }

   public void reset(boolean revertAll)
   {
      if (revertAll)
      {
         maxTinyPathsSpinnerModel.setValue(Double.valueOf(4.0));
      }
   }

   public double getMaxTinyPaths()
   {
      return maxTinyPathsSpinnerModel.getNumber().doubleValue();
   }

   public boolean isRemoveTinyPathsOn()
   {
      return doRemoveTinyPathsCheckBox.isSelected();
   }

   private JLabel maxTinyPathsLabel;

   private SpinnerNumberModel maxTinyPathsSpinnerModel;

   private JSpinner maxTinyPathsSpinner;

   private JCheckBox doRemoveTinyPathsCheckBox;

   private ControlPanel controlPanel;
}

class ControlPanel extends JPanel implements ActionListener
{
   public ControlPanel(VectorizeBitmapDialog dialog)
   {
      super(new BorderLayout());
      this.dialog = dialog;

      JDRResources resources = dialog.getResources();

      Box mainPanel = Box.createVerticalBox();
      add(new JScrollPane(mainPanel), "Center");

      mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      scanImagePanel = new ScanImagePanel(this);
      scanImagePanel.add(Box.createHorizontalGlue());
      scanImagePanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(scanImagePanel);

      optimizeLinesPanel = new OptimizeLinesPanel(this);
      optimizeLinesPanel.add(Box.createHorizontalGlue());
      optimizeLinesPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(optimizeLinesPanel);

      splitSubPathsPanel = new SplitSubPathsPanel(this);
      splitSubPathsPanel.add(Box.createHorizontalGlue());
      splitSubPathsPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(splitSubPathsPanel);

      lineDetectionPanel = new LineDetectionPanel(this);
      lineDetectionPanel.add(Box.createHorizontalGlue());
      lineDetectionPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(lineDetectionPanel);

      mergeNearPathsPanel = new MergeNearPathsPanel(this);
      mergeNearPathsPanel.add(Box.createHorizontalGlue());
      mergeNearPathsPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(mergeNearPathsPanel);

      smoothingPanel = new SmoothingPanel(this);
      smoothingPanel.add(Box.createHorizontalGlue());
      smoothingPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(smoothingPanel);

      removeTinyPathsPanel = new RemoveTinyPathsPanel(this);
      removeTinyPathsPanel.add(Box.createHorizontalGlue());
      removeTinyPathsPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.add(removeTinyPathsPanel);

      JComponent buttonPanel = Box.createVerticalBox();
      add(buttonPanel, "South");

      JComponent taskButtonPanel = new JPanel();
      buttonPanel.add(taskButtonPanel);

      doTasksButton = resources.createDialogButton(
         "vectorize.dotasks", "dotask", this, null);
      taskButtonPanel.add(doTasksButton);

      selectAllButton = resources.createDialogButton("vectorize", 
       "selectallitems", this, null);
      taskButtonPanel.add(selectAllButton);

      deselectAllButton = resources.createDialogButton("vectorize",
        "deselectallitems", this, null);
      taskButtonPanel.add(deselectAllButton);

      storeResultsButton = resources.createDialogButton("vectorize",
         "pinshapes", this, null);
      taskButtonPanel.add(storeResultsButton);

      clearAllResultsButton = resources.createDialogButton("vectorize",
          "discard_all", this, null);
      taskButtonPanel.add(clearAllResultsButton);

      updateWidgets(false, false);
   }

   public VectorizeBitmapDialog getDialog()
   {
      return dialog;
   }

   public JDRResources getResources()
   {
      return dialog.getResources();
   }

   public JSpinner createSpinner(SpinnerModel model)
   {
      return createSpinner(null, model);
   }

   public JSpinner createSpinner(JLabel label, SpinnerModel model)
   {
      JSpinner spinner = new JSpinner(model);
      spinner.setAlignmentX(Component.LEFT_ALIGNMENT);

      if (label != null)
      {
         label.setLabelFor(spinner);
      }

      Dimension dim = spinner.getPreferredSize();
      dim.width += 10;
      dim.height += 2;
      spinner.setMaximumSize(dim);

      return spinner;
   }

   public void colourPickerChange(boolean on)
   {
      dialog.setColourPickerCursor(on);
   }

   public boolean isColourPickerOn()
   {
      return scanImagePanel.isColourPickerOn();
   }

   public void colourPickerChoice(Color colour)
   {
      scanImagePanel.colourPickerChoice(colour);
   }

   public void regionPickerChange(boolean on)
   {
      dialog.setRegionPickerCursor(on);
   }

   public boolean isRegionPickerOn()
   {
      return scanImagePanel.isRegionPickerOn();
   }

   public void regionPickerChoice(Shape region, int action)
   {
      scanImagePanel.regionPickerChoice(region, action);
   }

   public void imagePanelClearRegion()
   {
      dialog.imagePanelClearRegion();
   }

   public Area getRegion()
   {
      return scanImagePanel.getRegion();
   }

   public void deselectScanImage()
   {
      scanImagePanel.setSelected(false, true);
   }

   public void deselectOptimizeLines()
   {
      optimizeLinesPanel.setSelected(false);
   }

   public void deselectSubPaths()
   {
      splitSubPathsPanel.setSelected(false);
   }

   public void deselectMergeNearPaths()
   {
      mergeNearPathsPanel.setSelected(false);
   }

   public void deselectLineDetection()
   {
      lineDetectionPanel.setSelected(false);
   }

   public void deselectSmoothing()
   {
      smoothingPanel.setSelected(false);
   }

   public void deselectRemoveTinyPaths()
   {
      removeTinyPathsPanel.setSelected(false);
   }

   public void taskFinished(boolean isVectorized)
   {
      updateWidgets(false, isVectorized);
   }

   public void updateWidgets(boolean taskInProgress, 
      boolean isVectorized)
   {
      scanImagePanel.updateWidgets(taskInProgress, isVectorized);
      optimizeLinesPanel.updateWidgets(taskInProgress, isVectorized);
      splitSubPathsPanel.updateWidgets(taskInProgress, isVectorized);
      mergeNearPathsPanel.updateWidgets(taskInProgress, isVectorized);
      lineDetectionPanel.updateWidgets(taskInProgress, isVectorized);
      smoothingPanel.updateWidgets(taskInProgress, isVectorized);
      removeTinyPathsPanel.updateWidgets(taskInProgress, isVectorized);

      dialog.setCancelEnabled(!taskInProgress);
      dialog.setOkayEnabled(!taskInProgress && (isVectorized || dialog.hasResults()));

      boolean enable = !taskInProgress;

      selectAllButton.setEnabled(enable);
      deselectAllButton.setEnabled(enable);
      clearAllResultsButton.setEnabled(enable);

      if (!enable)
      {
         doTasksButton.setEnabled(false);
         storeResultsButton.setEnabled(false);
      }
      else
      {
         updateTaskButton();
         updateStoreResultsButton();
      }
   }

   public void reset(boolean revertAll)
   {
      scanImagePanel.reset(revertAll);
      optimizeLinesPanel.reset(revertAll);
      splitSubPathsPanel.reset(revertAll);
      mergeNearPathsPanel.reset(revertAll);
      lineDetectionPanel.reset(revertAll);
      smoothingPanel.reset(revertAll);
      removeTinyPathsPanel.reset(revertAll);
   }

   public void updateTaskButton()
   {
      if (doTasksButton != null)
      {
         doTasksButton.setEnabled(isScanImageOn() || isOptimizeOn() 
            || isSplitSubPathsOn() || isMergeNearPathsOn() 
            || isSmoothingOn() || isLineDetectionOn()
            || isRemoveTinyPathsOn());
      }
   }

   public void updateStoreResultsButton()
   {
      if (storeResultsButton != null)
      {
         storeResultsButton.setEnabled(dialog.getCurrentShapeList() != null);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null)
      {
         return;
      }

      if (command.equals("dotask"))
      {
         dialog.doSelectedTasks();
      }
      else if (command.equals("pinshapes"))
      {
         dialog.storeResults();
      }
      else if (command.equals("selectallitems"))
      {
         setAll(true);
      }
      else if (command.equals("deselectallitems"))
      {
         setAll(false);
      }
      else if (command.equals("discard_all"))
      {
         dialog.clearAllResults();
      }
   }

   public void setAll(boolean selected)
   {
      scanImagePanel.setSelected(selected);
      optimizeLinesPanel.setSelected(selected);
      splitSubPathsPanel.setSelected(selected);
      mergeNearPathsPanel.setSelected(selected);
      lineDetectionPanel.setSelected(selected);
      smoothingPanel.setSelected(selected);
      removeTinyPathsPanel.setSelected(selected);
   }

   public Color getImageForeground()
   {
      return scanImagePanel.getImageForeground();
   }

   public double getFuzz()
   {
      return scanImagePanel.getFuzz();
   }

   public int getSampleWidth()
   {
      return scanImagePanel.getSampleWidth();
   }

   public int getSampleHeight()
   {
      return scanImagePanel.getSampleHeight();
   }

   public int getSubtractLastScanBorder()
   {
      return scanImagePanel.getSubtractLastScanBorder();
   }

   public double getGradientEpsilon()
   {
      return optimizeLinesPanel.getGradientEpsilon();
   }

   public double getMergeNearPathThreshold()
   {
      return mergeNearPathsPanel.getGapThreshold();
   }

   public boolean isMergeNearPathsOn()
   {
      return mergeNearPathsPanel == null ? false : mergeNearPathsPanel.isMergeNearPathsOn();
   }

   public boolean isRemoveMinTinySubPathsOn()
   {
      return splitSubPathsPanel.isRemoveMinTinySubPathsOn();
   }

   public double getMinTinySubPathArea()
   {
      return splitSubPathsPanel.getMinTinySubPathArea();
   }

   public int getMinTinySubPathSize()
   {
      return splitSubPathsPanel.getMinTinySubPathSize();
   }

   public double getDeltaThreshold()
   {
      return lineDetectionPanel.getDeltaThreshold();
   }

   public double getDeltaVarianceThreshold()
   {
      return lineDetectionPanel.getDeltaVarianceThreshold();
   }

   public double getSpikeReturnDistance()
   {
      return lineDetectionPanel.getSpikeReturnDistance();
   }

   public double getMinimumStubLength()
   {
      return lineDetectionPanel.getMinimumStubLength();
   }

   public double getLineDetectTinyStepThreshold()
   {
      return lineDetectionPanel.getTinyStepThreshold();
   }

   public double getSmoothingTinyStepThreshold()
   {
      return smoothingPanel.getTinyStepThreshold();
   }

   public double getSmoothingDeviationEpsilon()
   {
      return smoothingPanel.getDeviationEpsilon();
   }

   public double getLengthThreshold()
   {
      return smoothingPanel.getLengthThreshold();
   }

   public double getThresholdDiff()
   {
      return smoothingPanel.getThresholdDiff();
   }

   public double getCurveThresholdDiff()
   {
      return smoothingPanel.getCurveThresholdDiff();
   }

   public double getSmoothingMaxDeviation()
   {
      return smoothingPanel.getMaxDeviation();
   }

   public double getCurveGradientThreshold()
   {
      return smoothingPanel.getCurveGradientThreshold();
   }

   public double getCurveStationaryPtThreshold()
   {
      return smoothingPanel.getCurveStationaryPtThreshold();
   }

   public int getCurveMinPoints()
   {
      return smoothingPanel.getCurveMinPoints();
   }

   public double getMaxTinyPaths()
   {
      return removeTinyPathsPanel.getMaxTinyPaths();
   }

   public boolean isScanImageOn()
   {
      return scanImagePanel == null ? false : scanImagePanel.isScanImageOn();
   }

   public boolean isOptimizeOn()
   {
      return optimizeLinesPanel == null ? false : optimizeLinesPanel.isOptimizeOn();
   }

   public boolean isSplitSubPathsOn()
   {
      return splitSubPathsPanel == null ? false : splitSubPathsPanel.isSplitSubPathsOn();
   }

   public int getSplitType()
   {
      return splitSubPathsPanel.getSplitType();
   }

   public boolean isSmoothingOn()
   {
      return smoothingPanel == null ? false : smoothingPanel.isSmoothingOn();
   }

   public boolean isTryBezierOn()
   {
      return smoothingPanel.isTryBezierOn();
   }

   public boolean isFixedLineWidth()
   {
      return lineDetectionPanel.isFixedLineWidth();
   }

   public double getFixedLineWidth()
   {
      return lineDetectionPanel.getFixedLineWidth();
   }

   public boolean isRoundRelativeOn()
   {
      return lineDetectionPanel.isRoundRelativeOn();
   }

   public boolean isLineDetectionOn()
   {
      return lineDetectionPanel == null ? false : lineDetectionPanel.isLineDetectionOn();
   }

   public boolean isIntersectionDetectionOn()
   {
      return lineDetectionPanel.isIntersectionDetectionOn();
   }

   public boolean isRemoveTinyPathsOn()
   {
      return removeTinyPathsPanel == null ? false : 
          removeTinyPathsPanel.isRemoveTinyPathsOn();
   }

   private VectorizeBitmapDialog dialog;

   private JDRButton doTasksButton, selectAllButton, 
      deselectAllButton, clearAllResultsButton, storeResultsButton;

   private ScanImagePanel scanImagePanel;
   private OptimizeLinesPanel optimizeLinesPanel;
   private SplitSubPathsPanel splitSubPathsPanel;
   private MergeNearPathsPanel mergeNearPathsPanel;
   private LineDetectionPanel lineDetectionPanel;
   private SmoothingPanel smoothingPanel;
   private RemoveTinyPathsPanel removeTinyPathsPanel;
}

class ScanStatusBar extends JPanel implements PropertyChangeListener,ActionListener
{
   public ScanStatusBar(VectorizeBitmapDialog dialog)
   {
      super();
      this.dialog = dialog;
      JDRResources resources = dialog.getResources();

      setOpaque(false);
      setAlignmentY(Component.CENTER_ALIGNMENT);

      textField = resources.createAppInfoField(12);
      textField.setAlignmentY(Component.CENTER_ALIGNMENT);
      add(textField);

      progressBar = new JProgressBar(0, 100);
      progressBar.setValue(0);
      progressBar.setStringPainted(true);
      progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
      add(progressBar);

      cancelButton = resources.createAppButton("label", "abort", this);
      cancelButton.setAlignmentY(Component.CENTER_ALIGNMENT);
      add(cancelButton);

      confirmAbort = resources.getString("process.confirm.abort");
   }

   public void propertyChange(PropertyChangeEvent evt)
   {
      if ("progress".equals(evt.getPropertyName()))
      {
         int progress = (Integer) evt.getNewValue();
         progressBar.setValue(progress);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null)
      {
         return;
      }

      if (command.equals("abort"))
      {
         if (getResources().confirm(dialog, confirmAbort)
              == JOptionPane.YES_OPTION)
         {
            cancelButton.setEnabled(false);
            cancelled=true;

            if (currentTask != null)
            {
               currentTask.cancel(true);
            }
         }
      }
   }

   public void taskFinished()
   {
      currentTask = null;
      dialog.hideStatusBar();
      taskInProgress = false;
   }

   public void startTask(String info, SwingWorker task)
   {
      currentTask = task;
      taskInProgress = true;
      textField.setText(info);
      progressBar.setValue(0);
      cancelButton.setEnabled(true);
      cancelled = false;
      dialog.showStatusBar();
      task.addPropertyChangeListener(this);
      task.execute();
   }

   public boolean isCancelled()
   {
      return cancelled;
   }

   public JDRResources getResources()
   {
      return dialog.getResources();
   }

   public boolean isTaskInProgress()
   {
      return taskInProgress;
   }

   private JTextField textField;
   private JProgressBar progressBar;

   private String confirmAbort;

   private SwingWorker currentTask = null;

   private JButton cancelButton;
   private boolean cancelled=false;

   private boolean taskInProgress=false;

   private VectorizeBitmapDialog dialog;
}

class ShapeComponentVector extends Vector<ShapeComponent>
{
   public ShapeComponentVector()
   {
      super();
   }

   public ShapeComponentVector(int capacity)
   {
      super(capacity);
   }

   public Object clone()
   {
      ShapeComponentVector shape = new ShapeComponentVector(size());
      shape.isFilled = isFilled;
      shape.windingRule = windingRule;

      for (ShapeComponent comp : this)
      {
         ShapeComponent newComp = new ShapeComponent();
         newComp.set(comp);
         shape.addComponent(comp);
      }

      return shape;
   }

   public void setRule(int windingRule)
   {
      this.windingRule = windingRule;
   }

   public int getRule()
   {
      return windingRule;
   }

   public void moveTo(Point2D p)
   {
      moveTo(p.getX(), p.getY());
   }

   public void moveTo(double x, double y)
   {
      Point2D pt = (isEmpty() ? null : lastElement().getEnd());

      add(new ShapeComponent(PathIterator.SEG_MOVETO,
        new double[]{x, y}, pt));
   }

   public void lineTo(Point2D p)
   {
      lineTo(p.getX(), p.getY());
   }

   public void lineTo(double x, double y)
   {
      add(new ShapeComponent(PathIterator.SEG_LINETO,
        new double[]{x, y}, lastElement().getEnd()));
   }

   public boolean lineTo(Point2D p, double gradientEpsilon)
   {
      return lineTo(p.getX(), p.getY(), gradientEpsilon);
   }

   public boolean lineTo(double x, double y, double gradientEpsilon)
   {
      ShapeComponent comp = lastElement();

      if (comp.getType() != PathIterator.SEG_LINETO
        && comp.getType() != PathIterator.SEG_MOVETO)
      {
         lineTo(x, y);
         return true;
      }

      Point2D p = comp.getEnd();
      double dx = x-p.getX();
      double dy = y-p.getY();

      if (dx*dx + dy*dy < EPSILON)
      {
         return false;
      }

      if (comp.getType() == PathIterator.SEG_MOVETO)
      {
         lineTo(x, y);
         return true;
      }

      Point2D dp1 = comp.getEndGradient();
      Point2D dp2 = comp.getGradientToEnd(x, y);

      double theta1 = Math.atan2(dp1.getY(), dp1.getX());
      double theta2 = Math.atan2(dp2.getY(), dp2.getX());

      if (Math.abs(theta1-theta2) < gradientEpsilon)
      {
         comp.setEndPoint(x, y);
         return false;
      }
      else
      {
         lineTo(x, y);
         return true;
      }
   }

   public void closePath()
   {
      add(new ShapeComponent(PathIterator.SEG_CLOSE,
        null, lastElement().getEnd()));
   }

   public void addComponent(ShapeComponent comp)
   {
      if (!isEmpty())
      {
         comp.setStart(lastElement().getEnd());
      }

      add(comp);
   }

   public void removeComponent(int index)
   {
      int n = size()-1;

      ShapeComponent comp = remove(index);

      if (index == n)
      {
         return;
      }

      comp = get(index);

      if (index == 0)
      {
         switch (comp.getType())
         {
            case PathIterator.SEG_LINETO:
              comp.setType(PathIterator.SEG_MOVETO);
            break;
            case PathIterator.SEG_MOVETO:
            break;
            default:
               Point2D p1 = comp.getEnd();
               comp.setType(PathIterator.SEG_MOVETO);
               comp.setEndPoint(p1);
         }

         comp.setStart(null);

         return;
      }

      Point2D p0 = comp.getStart();

      comp.setStart(p0);
   }

   public void appendPath(ShapeComponentVector path)
   {
      appendPath(path, false);
   }

   public void appendPath(ShapeComponentVector path, boolean lineConnect)
   {
      if (path.isEmpty()) return;

      if (!isEmpty())
      {
         ShapeComponent firstElement = path.firstElement();
         firstElement.setStart(lastElement().getEnd());

         if (lineConnect && firstElement.getType() == PathIterator.SEG_MOVETO)
         {
            firstElement.setType(PathIterator.SEG_LINETO);
         }
      }

      addAll(path);
   }

   public void prependPath(ShapeComponentVector path, boolean lineConnect)
   {
      if (path.isEmpty())
      {
         return;
      }

      if (!isEmpty())
      {
         ShapeComponent firstElement = firstElement();
         firstElement.setStart(path.lastElement().getEnd());

         if (lineConnect && firstElement.getType() == PathIterator.SEG_MOVETO)
         {
            firstElement.setType(PathIterator.SEG_LINETO);
         }
      }

      addAll(0, path);
   }

   public void appendSubPath(SubPath subPath)
   {
      appendSubPath(subPath, false);
   }

   public void appendSubPath(SubPath subPath, boolean lineConnect)
   {
      if (subPath.size() == 0) return;

      Point2D pt = null;
      boolean isClosed = false;

      if (!isEmpty())
      {
         int n = size()-1;

         for (int i = n; i >= 0; i--)
         {
            ShapeComponent comp = get(i);

            if (comp.getType() != PathIterator.SEG_CLOSE)
            {
               if (pt == null) pt = comp.getEnd();

               if (!isClosed || !lineConnect) break;

               if (comp.getType() == PathIterator.SEG_MOVETO)
               {
                  Point2D p1 = comp.getEnd();

                  double[] coords = new double[6];
                  coords[0] = p1.getX();
                  coords[1] = p1.getY();
                  set(n, new ShapeComponent(PathIterator.SEG_LINETO,
                   coords, pt));

                  break;
               }
            }
            else if (i == n)
            {
               isClosed = true;
            }
         }
      }
      else
      {
         lineConnect = false;
      }

      int startIdx = subPath.getStartIndex();
      int endIdx = subPath.getEndIndex();

      ShapeComponentVector fullVec = subPath.getCompleteVector();

      ShapeComponent comp = new ShapeComponent();
      comp.set(fullVec.get(startIdx));
      comp.setStart(pt);

      if (lineConnect)
      {
         comp.setType(PathIterator.SEG_LINETO);
      }

      add(comp);

      for (int i = startIdx+1; i <= endIdx; i++)
      {
         ShapeComponent newComp = new ShapeComponent();
         newComp.set(fullVec.get(i));
         add(newComp);
      }
   }

   public double getEstimatedLength()
   {
      return getEstimatedLength(0, size()-1);
   }

   public double getEstimatedLength(int startIdx, int endIdx)
   {
      double length = 0.0;
      Point2D p0 = null;

      int n1 = endIdx < startIdx ? size()-1 : endIdx;

      for (int i = startIdx; i <= n1; i++)
      {
         ShapeComponent comp = get(i);

         switch (comp.getType())
         {
            case PathIterator.SEG_CLOSE:
               if (p0 == null)
               {
                  for (int j = i-1; j >= 0; j--)
                  {
                     ShapeComponent comp2 = get(j);

                     if (comp2.getType() == PathIterator.SEG_MOVETO)
                     {
                        p0 = comp2.getEnd();
                     }
                  }
               }
               length += JDRLine.getLength(get(i-1).getEnd(), p0);
               break;
            case PathIterator.SEG_MOVETO:
               p0 = comp.getEnd();
               break;
            default:
               length += comp.getDiagonalLength();
         }
      }

      if (endIdx < startIdx)
      {
         for (int i = 0; i <= endIdx; i++)
         {
            ShapeComponent comp = get(i);

            switch (comp.getType())
            {
               case PathIterator.SEG_CLOSE:
                  if (p0 == null)
                  {
                     for (int j = i-1; j >= 0; j--)
                     {
                        ShapeComponent comp2 = get(j);

                        if (comp2.getType() == PathIterator.SEG_MOVETO)
                        {
                           p0 = comp2.getEnd();
                        }
                     }
                  }
                  length += JDRLine.getLength(get(i-1).getEnd(), p0);
                  break;
               case PathIterator.SEG_MOVETO:
                  p0 = comp.getEnd();
                  break;
               default:
                  length += comp.getDiagonalLength();
            }
         }
      }

      return length;
   }

   public Path2D getPath()
   {
      Path2D.Double path = new Path2D.Double(windingRule, size());

      for (ShapeComponent component : this)
      {
         component.addToPath(path);
      }

      return path;
   }

   public ShapeComponentVector getSubPath(int endIdx)
   {
      ShapeComponentVector subPath = new ShapeComponentVector(size()-endIdx);
      subPath.isFilled = isFilled;
      subPath.windingRule = windingRule;

      for (int i = 0; i <= endIdx; i++)
      {
         subPath.add(get(i));
      }

      return subPath;
   }

   public Path2D getSubPath2D(int endIdx)
   {
      Path2D.Double path = new Path2D.Double(windingRule, size());

      for (int i = 0; i <= endIdx; i++)
      {
         ShapeComponent component = get(i);
         component.addToPath(path);
      }

      return path;
   }

   public static void printShape(Shape shape)
   {
      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
              System.out.format("M %f %f%n", coords[0], coords[1]);
            break;
            case PathIterator.SEG_LINETO:
              System.out.format("L %f %f%n", coords[0], coords[1]);
            break;
            case PathIterator.SEG_QUADTO:
              System.out.format("Q %f %f %f %f%n", coords[0], coords[1], coords[2], coords[3]);
            break;
            case PathIterator.SEG_CUBICTO:
              System.out.format("C %f %f %f %f %f %f%n", coords[0], coords[1], 
                 coords[2], coords[3], coords[4], coords[5]);
            break;
            case PathIterator.SEG_CLOSE:
              System.out.println("Z");
            break;
         }

         pi.next();
      }
   }

   public static ShapeComponentVector create(Shape shape)
   {
      ShapeComponentVector vec = new ShapeComponentVector();

      PathIterator pi = shape.getPathIterator(null);

      vec.setRule(pi.getWindingRule());

      double[] coords = new double[6];
      Point2D start = null;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         vec.add(new ShapeComponent(type, coords, start));

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
              start = new Point2D.Double(coords[0], coords[1]);
            break;
            case PathIterator.SEG_QUADTO:
              start = new Point2D.Double(coords[2], coords[3]);
            break;
            case PathIterator.SEG_CUBICTO:
              start = new Point2D.Double(coords[4], coords[5]);
            break;
         }

         pi.next();
      }

      return vec;
   }

   public String svg()
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < size(); i++)
      {
         if (i > 0)
         {
            builder.append(" ");
         }

         ShapeComponent comp = get(i);
         builder.append(comp);
      }

      return builder.toString();
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < size(); i++)
      {
         ShapeComponent comp = get(i);
         builder.append(String.format("[%d %s] %s%n", i, comp.getStart(), comp));
      }

      return builder.toString();
   }

   public void printSubPath(int startIdx, int endIdx)
   {
      int i = startIdx;
      ShapeComponent comp = get(i);
      System.out.format("sub path [%d, %d]%n", startIdx, endIdx);
      System.out.println("start: "+comp.getStart());
      System.out.println(comp);

      for ( ; i <= endIdx; i++)
      {
         comp = get(i);
         System.out.format("[%d] %s%n", i, comp);
      }
   }

   // approximate - assumes no curves
   public Rectangle2D getBounds2D()
   {
      int n = size();

      if (n <= 1)
      {
         return null;
      }

      ShapeComponent comp = firstElement();

      Point2D p = comp.getEnd();

      // avoid zero areas for horizontal and vertical lines
      double minX = p.getX()-0.25;
      double minY = p.getY()-0.25;
      double maxX = p.getX();
      double maxY = p.getY();

      for (int i = 1; i < n; i++)
      {
         comp = get(i);

         if (comp.getType() == PathIterator.SEG_CLOSE) continue;

         p = comp.getEnd();

         if (p.getX() < minX) minX = p.getX();
         if (p.getX() > maxX) maxX = p.getX();

         if (p.getY() < minY) minY = p.getY();
         if (p.getY() > maxY) maxY = p.getY();
      }

      return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
   }

   public Rectangle getBounds()
   {
      double minX = Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxX = 0;
      double maxY = 0;

      for (ShapeComponent comp : this)
      {
         if (comp.getType() == PathIterator.SEG_CLOSE) continue;

         Point2D pt = comp.getEnd();

         if (pt.getX() > maxX) maxX = pt.getX();
         if (pt.getX() < minX) minX = pt.getX();

         if (pt.getY() > maxY) maxY = pt.getY();
         if (pt.getY() < minY) minY = pt.getY();
      }

      if (minX == Double.MAX_VALUE || minY == Double.MAX_VALUE)
      {
         return null;
      }

      int x1 = (int)Math.floor(minX);
      int y1 = (int)Math.floor(minY);

      int x2 = (int)Math.ceil(maxX);
      int y2 = (int)Math.ceil(maxY);

      return new Rectangle(x1, y1, x2-x1, y2-y1);
   }

   public boolean isFilled()
   {
      return isFilled;
   }

   public void setFilled(boolean isFilled)
   {
      this.isFilled = isFilled;
   }

   public double getLineWidth()
   {
      return lineWidth;
   }

   public void setLineWidth(double width)
   {
      lineWidth = width;
   }

   // Only checks if final element is a closing segment.
   // Use hasSubPaths to check if there are other closing segments.
   public boolean isClosed()
   {
      if (isEmpty()) return false;

      return lastElement().getType() == PathIterator.SEG_CLOSE;
   }

   public boolean hasSubPaths()
   {
      for (int i = size()-2; i > 0; i--)
      {
         if (get(i).getType() == PathIterator.SEG_CLOSE)
         {
            return true;
         }
      }

      return false;
   }

   // assumes no subpaths
   public void computeDirection()
   {
      if (size() < 2) return;

      Rectangle2D bounds = getBounds2D();

      double midX = bounds.getX() + 0.5*bounds.getWidth();
      double midY = bounds.getY() + 0.5*bounds.getHeight();

      ShapeComponent comp1 = get(0);
      ShapeComponent comp2 = get(1);

      Point2D p1 = comp1.getEnd();
      Point2D p2 = comp2.getEnd();

      double theta1 = Math.PI + Math.atan2(p1.getY()-midY, p1.getX()-midX);
      double theta2 = Math.PI + Math.atan2(p2.getY()-midY, p2.getX()-midX);
      double diff = theta2 - theta1;

      if (diff > Math.PI)
      {
         direction = SubPath.DIRECTION_ANTICLOCKWISE;
      }
      else
      {
         direction = SubPath.DIRECTION_CLOCKWISE;
      }
   }

   public int getDirection()
   {
      if (direction == SubPath.DIRECTION_UNSET)
      {
         computeDirection();
      }

      return direction;
   }

   public boolean isOppositeDirection(ShapeComponentVector other)
   {
      return getDirection() + other.getDirection() == 0;
   }

   private int direction = SubPath.DIRECTION_UNSET;
   private int windingRule = Path2D.WIND_NON_ZERO;
   private boolean isFilled = true;
   private double lineWidth=1.0;

   public static final double EPSILON=1e-6;
}

class ShapeComponent
{
   public ShapeComponent()
   {
      type = PathIterator.SEG_MOVETO;
      coords = new double[6];
      coords[0] = 0.0;
      coords[1] = 0.0;
      start = null;
   }

   public ShapeComponent(int type, double[] coords, Point2D start)
   {
      this.type = type;

      if (coords != null)
      {
         this.coords = new double[coords.length];

         for (int i = 0; i < coords.length; i++)
         {
            this.coords[i] = coords[i];
         }
      }

      this.start = start;
   }

   public ShapeComponent(ShapeComponent otherComp)
   {
      this();
      set(otherComp);
   }

   public void set(ShapeComponent otherComp)
   {
      this.type = otherComp.type;
      this.start = otherComp.start;

      if (coords == null)
      {
         if (otherComp.coords == null)
         {
            return;
         }

         coords = new double[otherComp.coords.length];
      }
      else if (otherComp.coords == null)
      {
         coords = null;
         return;
      }
      else if (coords.length != otherComp.coords.length)
      {
         coords = new double[otherComp.coords.length];
      }

      for (int i = 0; i < coords.length; i++)
      {
         this.coords[i] = otherComp.coords[i];
      }
   }

   public ShapeComponent reverse()
   {
      double[] newCoords = (coords == null ? null : new double[coords.length]);

      Point2D newStart = getEnd();
      double newEndX = (start == null ? 0 : start.getX());
      double newEndY = (start == null ? 0 : start.getY());

      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            newCoords[0] = newEndX;
            newCoords[1] = newEndY;
         break;
         case PathIterator.SEG_QUADTO:
            newCoords[0] = coords[0];
            newCoords[1] = coords[1];

            newCoords[2] = newEndX;
            newCoords[3] = newEndY;
         break;
         case PathIterator.SEG_CUBICTO:
            newCoords[0] = coords[2];
            newCoords[1] = coords[3];
            newCoords[2] = coords[0];
            newCoords[3] = coords[1];
            newCoords[4] = newEndX;
            newCoords[5] = newEndY;
         break;
      }

      return new ShapeComponent(type, newCoords, newStart);
   }

   public int getType()
   {
      return type;
   }

   public boolean isCurve()
   {
      return type == PathIterator.SEG_QUADTO || type == PathIterator.SEG_CUBICTO;
   }

   public double[] getStationaryPositions()
   {
      // currently only implemented for cubic Bezier segments

      if (type == PathIterator.SEG_CUBICTO)
      {
         return JDRBezier.getStationaryPositions(start.getX(), start.getY(),
          coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
      }
      else
      {
         return null;
      }
   }

   public double[] getStationaryDeviations(Point2D p1, Point2D p2, 
      double[] resultT, Point2D[] resultP)
   {
      // currently only implemented for cubic Bezier segments

      if (type != PathIterator.SEG_CUBICTO)
      {
         return null;
      }

      double[] t = getStationaryPositions();

      if (t == null)
      {
         return null;
      }

      double t1 = -1;
      double t2 = -1;

      if (t[0] >= 0.0 && t[0] <= 1.0)
      {
         t1 = t[0];
      }

      if (t.length > 1 && t[1] >= 0.0 && t[1] <= 1.0)
      {
         if (t1 < 0.0)
         {
            t1 = t[1];
         }
         else
         {
            t2 = t[1];
         }
      }

      if (t1 < 0.0 && t2 < 0.0)
      {
         return null;
      }

      Point2D b1 = JDRBezier.getP(t1, start.getX(), start.getY(),
       coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);

      double d1 = JDRLine.getLength(p1, b1);

      if (resultT != null)
      {
         resultT[0] = t1;
         resultT[1] = t2;
      }

      if (resultP != null)
      {
         resultP[0] = b1;
      }

      if (t2 < 0.0 || p2 == null)
      {
         return new double[] { d1 };
      }

      Point2D b2 = JDRBezier.getP(t2, start.getX(), start.getY(),
       coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);

      double d2 = JDRLine.getLength(p2, b2);

      if (resultP != null)
      {
         resultP[1] = b2;
      }

      return new double[] { d1, d2 };
   }

   public double[] getCoords()
   {
      return coords;
   }

   public Point2D getStart()
   {
      return start;
   }

   public void setStart(Point2D newStart)
   {
      start = newStart;
   }

   public void setType(int newType)
   {
      setType(newType, null);
   }

   public void setType(int newType, double[] newCoords)
   {
      this.type = newType;

      if (newCoords != null)
      {
         for (int i = 0; i < newCoords.length; i++)
         {
            coords[i] = newCoords[i];
         }
      }
   }

   public Point2D getP(double t, Point2D startPt)
   {
      Point2D p = getEnd();

      return new Point2D.Double((1.0-t)*startPt.getX()+t*p.getX(),
                                (1.0-t)*startPt.getY()+t*p.getY());
   }

   public Point2D getMid(Point2D startPt)
   {// midpoint of diagonal line from start to end

      return JDRLine.getMidPoint(startPt, getEnd());
   }

   public Point2D getMid()
   {
      if (start == null)
      {
         Point2D p = getEnd();

         return new Point2D.Double(0.5*p.getX(), 0.5*p.getY());
      }

      return getMid(start);
   }

   public Point2D getEnd()
   {
      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            return new Point2D.Double(coords[0], coords[1]);
         case PathIterator.SEG_QUADTO:
            return new Point2D.Double(coords[2], coords[3]);
         case PathIterator.SEG_CUBICTO:
            return new Point2D.Double(coords[4], coords[5]);
      }

      return start;
   }

   public void setEndPoint(Point2D pt)
   {
      setEndPoint(pt.getX(), pt.getY());
   }

   public void setEndPoint(double x, double y)
   {
      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            coords[0] = x;
            coords[1] = y;
         return;
         case PathIterator.SEG_QUADTO:
            coords[2] = x;
            coords[3] = y;
         return;
         case PathIterator.SEG_CUBICTO:
            coords[4] = x;
            coords[5] = y;
         return;
      }
   }

   public Point2D getGradientToEnd(ShapeComponent comp)
   {
      double p1x=0.0, p1y=0.0;

      switch (comp.type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            p1x = comp.coords[0];
            p1y = comp.coords[1];
         break;
         case PathIterator.SEG_QUADTO:
            p1x = comp.coords[2];
            p1y = comp.coords[3];
         break;
         case PathIterator.SEG_CUBICTO:
            p1x = comp.coords[4];
            p1y = comp.coords[5];
         break;
      }

      return getGradientToEnd(p1x, p1y);
   }

   public Point2D getGradientToEnd(Point2D p1)
   {
      return getGradientToEnd(p1.getX(), p1.getY());
   }

   public Point2D getGradientToEnd(double p1x, double p1y)
   {
      double p0x=0.0, p0y=0.0;

      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            p0x = coords[0];
            p0y = coords[1];
         break;
         case PathIterator.SEG_QUADTO:
            p0x = coords[2];
            p0y = coords[3];
         break;
         case PathIterator.SEG_CUBICTO:
            p0x = coords[4];
            p0y = coords[5];
         break;
      }

      return JDRLine.getGradient(p0x, p0y, p1x, p1y);
   }

   public Point2D getStartGradient()
   {
      if (start == null) return null;

      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            return new Point2D.Double(coords[0]-start.getX(), coords[1]-start.getY());
         
         case PathIterator.SEG_QUADTO:
            return new Point2D.Double(2*(coords[0]-start.getX()),
                                      2*(coords[1]-start.getY()));

         case PathIterator.SEG_CUBICTO:
            return new Point2D.Double(3*(coords[0]-start.getX()),
                                      3*(coords[1]-start.getY()));
      } 

      return null;
   }

   public Point2D getEndGradient()
   {
      if (start == null) return null;

      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:
            return new Point2D.Double(coords[0]-start.getX(), coords[1]-start.getY());
         
         case PathIterator.SEG_QUADTO:
            return new Point2D.Double(2*(coords[2]-coords[0]),
                                      2*(coords[3]-coords[1]));

         case PathIterator.SEG_CUBICTO:
            return new Point2D.Double(3*(coords[4]-coords[2]),
                                      3*(coords[5]-coords[3]));
      } 

      return null;
   }

   public double getDiagonalLength()
   {
      return Math.sqrt(getSquareDiagonalLength());
   }

   public double getSquareDiagonalLength()
   {
      double x0 = 0.0;
      double y0 = 0.0;

      if (start != null)
      {
         x0 = start.getX();
         y0 = start.getY();
      }

      double dx, dy;

      switch (type)
      {
         case PathIterator.SEG_MOVETO:
         case PathIterator.SEG_LINETO:

            dx = coords[0]-x0;
            dy = coords[1]-y0;

            return dx*dx + dy*dy;

         case PathIterator.SEG_QUADTO:

            dx = coords[2]-x0;
            dy = coords[3]-y0;

            return dx*dx + dy*dy;

         case PathIterator.SEG_CUBICTO:

            dx = coords[4]-x0;
            dy = coords[5]-y0;

            return dx*dx + dy*dy;
      }

      return 0.0;
   }

   public static double getSquareDistance(double p0x, double p0y, double p1x, double p1y)
   {
      double dx = p1x-p0x;
      double dy = p1y-p0y;

      return dx*dx + dy*dy;
   }

   public static double getSquareDistance(Point2D p0, Point2D p1)
   {
      return getSquareDistance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
   }

   public void addToPath(Path2D path)
   {
      switch (type)
      {
         case PathIterator.SEG_CLOSE:
            path.closePath();
         break;
         case PathIterator.SEG_CUBICTO:
            path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
         break;
         case PathIterator.SEG_LINETO:
            path.lineTo(coords[0], coords[1]);
         break;
         case PathIterator.SEG_MOVETO:
            path.moveTo(coords[0], coords[1]);
         break;
         case PathIterator.SEG_QUADTO:
            path.quadTo(coords[0], coords[1], coords[2], coords[3]);
         break;
      }
   }

   public String toString()
   {
      switch (type)
      {
         case PathIterator.SEG_CLOSE:
            return "Z";
         case PathIterator.SEG_CUBICTO:
            return String.format("C %f %f %f %f %f %f", 
              coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
         case PathIterator.SEG_LINETO:
            return String.format("L %f %f", coords[0], coords[1]);
         case PathIterator.SEG_MOVETO:
            return String.format("M %f %f", coords[0], coords[1]);
         case PathIterator.SEG_QUADTO:
            return String.format("Q %f %f %f %f",
                coords[0], coords[1], coords[2], coords[3]);
      }

      return "";
   }

   public String info(JDRResources resources)
   {
      double x0 = (start == null ? 0.0 : start.getX());
      double y0 = (start == null ? 0.0 : start.getY());

      switch (type)
      {
         case PathIterator.SEG_CLOSE:
            return resources.getString("path_element_info.close");
         case PathIterator.SEG_CUBICTO:
            return resources.getMessage("path_element_info.cubic",
              x0, y0, coords[0], coords[1], coords[2], coords[3], 
              coords[4], coords[5], 
              3*(coords[0]-x0), 3*(coords[1]-y0),
              3*(coords[4]-coords[2]), 3*(coords[5]-coords[3]));
         case PathIterator.SEG_LINETO:
            return resources.getMessage("path_element_info.line",
              x0, y0, coords[0], coords[1], coords[0]-x0, coords[1]-y0);
         case PathIterator.SEG_MOVETO:
            return resources.getMessage("path_element_info.move",
              x0, y0, coords[0], coords[1]);
         case PathIterator.SEG_QUADTO:
            return resources.getMessage("path_element_info.quad",
              x0, y0, coords[0], coords[1], coords[2], coords[3],
              2*(coords[0]-x0), 2*(coords[1]-y0),
              2*(coords[2]-coords[0]), 2*(coords[2]-coords[1]));
      }

      return "";
   }

   public void setBend(double bend)
   {
      if (bend < 0 || bend > Math.PI)
      {
         throw new IllegalArgumentException("Invalid bend "+bend);
      }

      this.bend = bend;
   }

   public double getBend()
   {
      return bend;
   }

   private int type;
   private double[] coords;
   private Point2D start=null;
   private double bend = Math.PI;
}

class UnsupportedColourType extends Exception
{
   public UnsupportedColourType(int type)
   {
      super("Unsupported colour type: "+type);
      this.type = type;
   }

   public UnsupportedColourType(String name, int type)
   {
      super(String.format("Unsupported colour type: %s (%d)", name, type));
      this.type = type;
   }

   public UnsupportedColourType(JDRResources resources, int type)
   {
      super(resources.getMessage(
         "vectorize.unsupported_colour_type", type));
      this.type = type;
   }

   public UnsupportedColourType(JDRResources resources, String name, int type)
   {
      super(resources.getMessage("vectorize.unsupported_colour_type_name", 
        name, type));
      this.type = type;
   }

   private int type;
}

class ScanImage extends SwingWorker<Void,Raster>
{
   public ScanImage(VectorizeBitmapDialog dialog, BufferedImage image, boolean continueToNextStep)
     throws UnsupportedColourType
   {
      this.dialog = dialog;
      this.image = image;
      this.continueToNextStep = continueToNextStep;
      this.scanRegion = dialog.getRegion();

      Color col = dialog.getImageForeground();
      JDRResources resources = dialog.getResources();

      int type = image.getType();

      switch (type)
      {
         case BufferedImage.TYPE_3BYTE_BGR:
            foreground = new int[3];
            foreground[0] = col.getRed();
            foreground[1] = col.getGreen();
            foreground[2] = col.getBlue();
         break;
         case BufferedImage.TYPE_4BYTE_ABGR:
            foreground = new int[4];
            foreground[0] = col.getRed();
            foreground[1] = col.getGreen();
            foreground[2] = col.getBlue();
            foreground[3] = col.getAlpha();
         break;
         case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            throw new UnsupportedColourType(resources, "4byte_ABGR_pre", type);
         case BufferedImage.TYPE_BYTE_BINARY:
            throw new UnsupportedColourType(resources, "byte_binary", type);
         case BufferedImage.TYPE_BYTE_GRAY:
            foreground = new int[1];
            foreground[0] = (col.getRed()+col.getGreen()+col.getBlue())/3;
         break;
         case BufferedImage.TYPE_BYTE_INDEXED:
            throw new UnsupportedColourType(resources, "byte_indexed", type);
         case BufferedImage.TYPE_CUSTOM:
            throw new UnsupportedColourType(resources, "custom", type);
         case BufferedImage.TYPE_INT_ARGB:
            throw new UnsupportedColourType(resources, "int_ARGB", type);
         case BufferedImage.TYPE_INT_ARGB_PRE:
            throw new UnsupportedColourType(resources, "int_ARGB pre", type);
         case BufferedImage.TYPE_INT_BGR:
            throw new UnsupportedColourType(resources, "int_bgr", type);
         case BufferedImage.TYPE_INT_RGB:
            throw new UnsupportedColourType(resources, "int_RGB", type);
         case BufferedImage.TYPE_USHORT_555_RGB:
            throw new UnsupportedColourType(resources, "ushort_555_RGB", type);
         case BufferedImage.TYPE_USHORT_565_RGB:
            throw new UnsupportedColourType(resources, "ushort_565_RGB", type);
         case BufferedImage.TYPE_USHORT_GRAY:
            throw new UnsupportedColourType(resources, "ushort_gray", type);
         default:
            throw new UnsupportedColourType(resources, type);
      }

      fuzz = dialog.getFuzz();
      dx = dialog.getSampleWidth();
      dy = dialog.getSampleHeight();
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      int width = image.getWidth();
      int height = image.getHeight();

      int progress=0;
      int maxProgress = (int)Math.round(((double)width*height)/(dx*dy));

      int startX = 0;
      int startY = 0;
      int endX = width;
      int endY = height;

      if (scanRegion != null)
      {
         Rectangle bounds = scanRegion.getBounds();

         startX = (int)Math.max(0, bounds.x);
         startY = (int)Math.max(0, bounds.y);
         endX = (int)Math.min(width, bounds.x+bounds.width);
         endY = (int)Math.min(height, bounds.y+bounds.height);
      }

      for (int x = startX; x < endX; x += dx)
      {
         for (int y = startY; y < endY; y += dy)
         {
            progress++;

            setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
            dialog.updateTimeElapsed();

            Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

            // check for cancel
            if (dialog.isCancelled())
            {
               dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               throw new UserCancelledException(dialog.getMessageDictionary());
            }

            int rectWidth = dx;
            int rectHeight = dy;

            if (x+rectWidth > width)
            {
               rectWidth = width-x;
            }

            if (y+rectHeight > height)
            {
               rectHeight = height-y;
            }

            Rectangle rect = new Rectangle(x, y, rectWidth, rectHeight);

            if (scanRegion != null && !scanRegion.contains(rect))
            {
               if (scanRegion.intersects(rect))
               {
                  Area ar = new Area(rect);
                  ar.intersect(scanRegion);

                  if (ar.isRectangular())
                  {
                     publish(image.getData(ar.getBounds()));
                  }
                  else
                  {
                     processSample(ar, rect);
                  }
               }
            }
            else
            {
               publish(image.getData(rect));
            }
         }
      }

      return null;
   }

   private void processSample(Area ar, Rectangle rect)
     throws InterruptedException
   {
      Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

      // check for cancel
      if (dialog.isCancelled())
      {
         dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         throw new UserCancelledException(dialog.getMessageDictionary());
      }

      processSample(ar, rect.x, rect.y, rect.width, rect.height);
   }

   private void processSample(Area ar, int x, int y, int rectWidth, int rectHeight)
     throws InterruptedException
   {
      int halfRectWidth = rectWidth/2;
      int halfRectHeight = rectHeight/2;

      if (halfRectWidth == 0 || halfRectHeight == 0)
      {
         return;
      }

      dialog.addMessageIdLn(
        "vectorize.message.sample_intersects_region",
        x, y, rectWidth, rectHeight);

      int x1 = x+halfRectWidth;
      int y1 = y+halfRectHeight;

      Rectangle rect = new Rectangle(x, y, halfRectWidth, halfRectHeight);

      if (ar.contains(rect))
      {
         publish(image.getData(rect));
      }
      else if (ar.intersects(rect))
      {
         processSample(ar, rect);
      }

      rect.x = x1;
      rect.y = y;

      if (scanRegion.contains(rect))
      {
         publish(image.getData(rect));
      }
      else if (ar.intersects(rect))
      {
         processSample(ar, rect);
      }

      rect.x = x;
      rect.y = y1;

      if (scanRegion.contains(rect))
      {
         publish(image.getData(rect));
      }
      else if (ar.intersects(rect))
      {
         processSample(ar, rect);
      }

      rect.x = x1;

      if (scanRegion.contains(rect))
      {
         publish(image.getData(rect));
      }
      else if (ar.intersects(rect))
      {
         processSample(ar, rect);
      }
   }

   protected void process(java.util.List<Raster> rasterList)
   {
      Iterator<Raster> iter = rasterList.iterator();
      int[] sample = new int[foreground.length];

      Area shape = null;

      while (iter.hasNext())
      {
         Raster raster = iter.next();

         int minX = raster.getMinX();
         int minY = raster.getMinY();
         int width = raster.getWidth();
         int height = raster.getHeight();
         int maxX = minX+width;
         int maxY = minY+height;

         for (int x = minX; x < maxX; x++)
         {
            for (int y = minY; y < maxY; y++)
            {
               raster.getPixel(x, y, sample);

               double diff = 0;

               for (int i = 0; i < sample.length; i++)
               {
                  double delta = (sample[i]-foreground[i])/255.0;
                  diff += delta*delta;
               }

               diff = Math.sqrt(diff);

               if (sample.length > 1)
               {
                  diff /= sample.length;
               }

               if (diff < fuzz)
               {
                  Rectangle2D rect = new Rectangle2D.Double(x, y, 1, 1);

                  if (shape == null)
                  {
                     shape = new Area(rect);
                     Vector<Shape> shapes = new Vector<Shape>();
                     shapes.add(shape);
                     dialog.setShapes(shapes);
                  }
                  else
                  {
                     shape.add(new Area(rect));
                  }
               }
            }
         }
      }

      if (shape != null)
      {
         addToArea(shape);
         dialog.repaintImagePanel(shape.getBounds());
      }
   }

   public void done()
   {
      setProgress(100);
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      if (area == null)
      {
         dialog.scanFinished(null, false);
      }
      else
      {
         Rectangle2D bounds = area.getBounds2D();
         dialog.addMessageIdLn("vectorize.message.scan_image_results",
          bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

         dialog.addMessageLn(dialog.getResources().applyMessagePattern(
          "vectorize.message.contains_subpaths",
          area.isSingular() ? 1 : 0));

         Vector<ShapeComponentVector> shapeList
            = new Vector<ShapeComponentVector>(1);

         ShapeComponentVector shapeVec = ShapeComponentVector.create(area);
         shapeList.add(shapeVec);

         dialog.scanFinished(shapeList, continueToNextStep);
      }
   }

   private synchronized void addToArea(Area newArea)
   {
      if (area == null)
      {
         area = newArea;
      }
      else
      {
         area.add(newArea);
      }
   }

   private VectorizeBitmapDialog dialog;
   private BufferedImage image;
   private int[] foreground;
   private double fuzz;
   private Area area = null, scanRegion = null;
   private boolean continueToNextStep;
   private int dx, dy;
}

class OptimizeLines extends SwingWorker<Void,ShapeComponentVector>
{
   public OptimizeLines(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapeList = shapeList;

      gradientEpsilon = dialog.getGradientEpsilon();
      this.continueToNextStep = continueToNextStep;
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (shapeList == null || shapeList.isEmpty())
      {
         return null;
      }

      progress = 0;
      maxProgress = 0;
      int numShapes = shapeList.size();

      for (int i = 0; i < numShapes; i++)
      {
         maxProgress += shapeList.get(i).size();
      }

      Vector<ShapeComponentVector> oldShapeList = new Vector<ShapeComponentVector>();
      oldShapeList.addAll(shapeList);
      shapeList.clear();

      for (int i = 0; i < numShapes; i++)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException(dialog.getMessageDictionary());
         }

         Vector<ShapeComponentVector> result = processShape(oldShapeList.get(i));

         for (ShapeComponentVector vec : result)
         {
            publish(vec);
         }
      }

      return null;
   }

   protected void process(java.util.List<ShapeComponentVector> shapeVecList)
   {
      Iterator<ShapeComponentVector> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         ShapeComponentVector vec = iter.next();

         shapeList.add(vec);

         dialog.repaintImagePanel(vec.getBounds());
      }
   }

   private void incProgress()
   {
      progress++;

      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
   }

   private Vector<ShapeComponentVector> processShape(ShapeComponentVector vec)
   {
      int n = vec.size();
      Vector<ShapeComponentVector> newShapes = new Vector<ShapeComponentVector>();

      Point2D prevGrad = null;
      ShapeComponentVector newVec = new ShapeComponentVector(n);
      newVec.setFilled(vec.isFilled());
      newVec.setRule(vec.getRule());
      double[] coords;

      for (int i = 0; i < n; i++)
      {
         incProgress();

         ShapeComponent current = vec.get(i);

         Point2D dp = current.getStartGradient();

         if (dp != null && prevGrad != null)
         {
            double length = Math.sqrt(dp.getX()*dp.getX()+dp.getY()*dp.getY());

            ShapeComponent prevComp = newVec.lastElement();
            coords = current.getCoords();

            if (current.getType() == PathIterator.SEG_LINETO && length < 1e-6)
            {
               continue;
            }

            if (current.getType() == PathIterator.SEG_LINETO
                  && prevComp.getType() == PathIterator.SEG_LINETO)
            {
               double theta1 = Math.atan2(prevGrad.getY(), prevGrad.getX());
               double theta2 = Math.atan2(dp.getY(), dp.getX());

               if (Math.abs(theta1-theta2) < gradientEpsilon)
               {
                  prevComp.setEndPoint(coords[0], coords[1]);
                  prevGrad = prevComp.getStartGradient();
                  continue;
               }
            }
         }

         newVec.add(current);

         prevGrad = dp;
      }

      if (!newVec.isEmpty())
      {
         newShapes.add(newVec);
      }

      return newShapes;
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedOptimizeLines(shapeList, continueToNextStep);
   }

   private int progress, maxProgress;
   private double gradientEpsilon=0.01;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private boolean continueToNextStep;
}

class SubPath
{
   public SubPath(ShapeComponentVector vec, int startIdx, int endIdx)
   {
      this.vec = vec;
      this.startIdx = startIdx;
      this.endIdx = endIdx;
   }

   // approximate - assumes lines not curves
   public Rectangle2D getBounds2D()
   {
      Point2D p = vec.get(startIdx).getEnd();

      // ensure that horizontal/vertical lines don't have zero area.
      double minX = p.getX()-0.5;
      double minY = p.getY()-0.5;
      double maxX = p.getX();
      double maxY = p.getY();

      for (int i = startIdx+1; i <= endIdx; i++)
      {
         ShapeComponent comp = vec.get(i);

         if (comp.getType() != PathIterator.SEG_CLOSE)
         {
            p = comp.getEnd();

            if (p.getX() < minX)
            {
               minX = p.getX();
            }

            if (p.getY() < minY)
            {
               minY = p.getY();
            }

            if (p.getX() > maxX)
            {
               maxX = p.getX();
            }

            if (p.getY() > maxY)
            {
               maxY = p.getY();
            }
         }
      }

      return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
   }

   public ShapeComponent getComponent(int i)
   {
      return vec.get(startIdx+i);
   }

   public ShapeComponentVector getCompleteVector()
   {
      return vec;
   }

   public ShapeComponentVector toVector()
   {
      ShapeComponentVector vec = new ShapeComponentVector(endIdx-startIdx+1);

      vec.appendSubPath(this, false);

      return vec;
   }

   public int getStartIndex()
   {
      return startIdx;
   }

   public int getEndIndex()
   {
      return endIdx;
   }

   public int size()
   {
      return endIdx-startIdx+1;
   }

   public Shape getShape()
   {
      Path2D shape = new Path2D.Double(vec.getRule(), endIdx-startIdx+1);

      ShapeComponent comp = vec.get(startIdx);
      Point2D pt = comp.getEnd();
      shape.moveTo(pt.getX(), pt.getY());

      for (int i = startIdx+1; i <= endIdx; i++)
      {
         comp = vec.get(i);
         comp.addToPath(shape);
      }

      return shape;
   }

   public void addInteriorIndex(int i)
   {
      if (contains == null)
      {
         contains = new Vector<Integer>();
      }

      contains.add(Integer.valueOf(i));
   }

   public void addExteriorIndex(int i)
   {
      if (container == null)
      {
         container = new Vector<Integer>();
      }

      container.add(Integer.valueOf(i));
   }

   public boolean overlaps(int i)
   {
      Integer idx = Integer.valueOf(i);

      return (container != null && container.contains(idx)) 
          || (contains != null && contains.contains(idx));
   }

   public boolean isContainer()
   {
      return contains != null;
   }

   public boolean isInside()
   {
      return container != null;
   }

   public Vector<Integer> getContained()
   {
      return contains;
   }

   public Vector<Integer> getContainers()
   {
      return container;
   }

   public int getContainerLevel(Vector<SubPath> subPaths)
   {
      if (container == null)
      {
         return 0;
      }

      int maxLevel = 1;

      for (Integer idx : container)
      {
         SubPath sp = subPaths.get(idx.intValue());

         if (sp == this || (sp.startIdx == startIdx && sp.endIdx == endIdx))
         {
            continue; // shouldn't happen!
         }

         int parentContainerLevel = sp.getContainerLevel(subPaths);

         int level = parentContainerLevel + 1;

         if (level > maxLevel)
         {
            maxLevel = level;
         }
      }

      return maxLevel;
   }

   public String getSpecs()
   {
      StringBuilder builder = new StringBuilder();

      for (int i = startIdx; i <= endIdx; i++)
      {
         builder.append(vec.get(i));

         if (i < endIdx)
         {
            builder.append(' ');
         }
      }

      return builder.toString();
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder(
        String.format("SubPath[start=%d,end=%d,path=", startIdx, endIdx));

      for (int i = startIdx; i <= endIdx; i++)
      {
         builder.append(vec.get(i));

         if (i < endIdx)
         {
            builder.append(' ');
         }
      }

      builder.append("]");
      return builder.toString();
   }

   public void computeDirection()
   {
      if (startIdx == endIdx) return;

      Rectangle2D bounds = getBounds2D();

      double midX = bounds.getX() + 0.5*bounds.getWidth();
      double midY = bounds.getY() + 0.5*bounds.getHeight();

      ShapeComponent comp1 = vec.get(startIdx);
      ShapeComponent comp2 = vec.get(startIdx+1);

      Point2D p1 = comp1.getEnd();
      Point2D p2 = comp2.getEnd();

      double theta1 = Math.PI + Math.atan2(p1.getY()-midY, p1.getX()-midX);
      double theta2 = Math.PI + Math.atan2(p2.getY()-midY, p2.getX()-midX);
      double diff = theta2 - theta1;

      if (diff > Math.PI)
      {
         direction = DIRECTION_ANTICLOCKWISE;
      }
      else
      {
         direction = DIRECTION_CLOCKWISE;
      }
   }

   public int getDirection()
   {
      if (direction == DIRECTION_UNSET)
      {
         computeDirection();
      }

      return direction;
   }

   public boolean isOppositeDirection(SubPath other)
   {
      return getDirection() + other.getDirection() == 0;
   }

   private ShapeComponentVector vec;
   private int startIdx, endIdx;
   private Vector<Integer> contains, container;

   public static final int DIRECTION_UNSET=0,
     DIRECTION_CLOCKWISE=1, DIRECTION_ANTICLOCKWISE=-1;

   private int direction = DIRECTION_UNSET;
}

class SplitSubPaths extends SwingWorker<Void,Void>
{
   public SplitSubPaths(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapeList = shapeList;

      minTinyArea = dialog.getMinTinySubPathArea();
      minTinySize = dialog.getMinTinySubPathSize();
      removeTiny = dialog.isRemoveMinTinySubPathsOn();

      splitType = dialog.getSplitType();
      this.continueToNextStep = continueToNextStep;

      comparator = new Comparator<Integer>()
      {
         public int compare(Integer val1, Integer val2)
         {
            return val1.compareTo(val2);
         }
      };
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (shapeList == null || shapeList.isEmpty())
      {
         return null;
      }

      Vector<SubPath> subPaths = new Vector<SubPath>();

      for (int i = 0; i < shapeList.size(); i++)
      {
         ShapeComponentVector vec = shapeList.get(i);
         int startIdx = 0;
         int n = vec.size();

         for (int j = 0; j < n; j++)
         {
            ShapeComponent comp = vec.get(j);

            if (comp.getType() == PathIterator.SEG_CLOSE)
            {
               if (j != n-1)
               {
                  ShapeComponent nextComp = vec.get(j+1);
               }

               SubPath sp = new SubPath(vec, startIdx, j);

               if (removeTiny && j-startIdx+1 < minTinySize)
               {
                  Rectangle2D bounds = sp.getBounds2D();
                  double area = bounds.getWidth()*bounds.getHeight();

                  if (area > minTinyArea)
                  {
                     subPaths.add(sp);
                  }
               }
               else
               {
                  subPaths.add(sp);
               }

               startIdx = j+1;
            }
         }

         if (startIdx != n)
         {
            subPaths.add(new SubPath(vec, startIdx, n-1));
         }
      }

      progress = 0;

      int n = subPaths.size();
      maxProgress = n;

      newShapesVec = new Vector<ShapeComponentVector>(n);
      dialog.updateShapes(newShapesVec, false);

      if (splitType == EVEN_INTERIOR_SPLIT || splitType == SPLIT_EXTERIOR_ONLY)
      {
         maxProgress = n*n+n;
         Shape[] shapes = new Shape[n];

         for (int i = 0; i < n; i++)
         {
            incProgress();
            sleepAndCheckCancel();
            shapes[i] = subPaths.get(i).getShape();
         }

         for (int i = 0; i < n; i++)
         {
            SubPath sp = subPaths.get(i);
            Point2D pt = sp.getCompleteVector().get(sp.getStartIndex()).getEnd();

            for (int j = 0; j < n; j++)
            {
               if (i == j) continue;

               incProgress();
               sleepAndCheckCancel();

               if (shapes[j].contains(pt) && !sp.overlaps(j))
               {
                  SubPath sp2 = subPaths.get(j);

                  if (!sp2.overlaps(i))
                  {
                     sp2.addInteriorIndex(i);
                     sp.addExteriorIndex(j);
                  }
               }
            }
         }

         HashMap<Integer,ShapeComponentVector> results
            = new HashMap<Integer,ShapeComponentVector>();

         if (splitType == EVEN_INTERIOR_SPLIT)
         {
            splitEvenExterior(subPaths, results);
         }
         else
         {
            splitExteriorOnly(subPaths, results);
         }

         Set<Integer> keyset = results.keySet();
         Integer[] array = keyset.toArray(new Integer[keyset.size()]);
         Arrays.parallelSort(array, comparator);
         newShapesVec.clear();

         for (int i = 0; i < array.length; i++)
         {
            newShapesVec.add(results.get(array[i]));
         }
   
      }
      else if (splitType == SPLIT_ALL)
      {
         splitAll(subPaths);
      }

      return null;
   }

   private void incProgress()
   {
      progress++;
      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
   }

   private void sleepAndCheckCancel() throws InterruptedException
   {
      dialog.updateTimeElapsed();
      Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

      // check for cancel
      if (dialog.isCancelled())
      {
         throw new UserCancelledException(dialog.getMessageDictionary());
      }
   }

   private void splitAll(Vector<SubPath> subPaths) throws InterruptedException
   {
      for (int i = 0, n = subPaths.size(); i < n; i++)
      {
         incProgress();
         sleepAndCheckCancel();

         SubPath subPath = subPaths.get(i);
         ShapeComponentVector newVec = processSubPath(subPath);

         if (newVec != null)
         {
            newVec.setFilled(false);
         }
      }
   }

   private void splitExteriorOnly(Vector<SubPath> subPaths,
        HashMap<Integer,ShapeComponentVector> results)
   throws InterruptedException
   {
      int n = subPaths.size();

      for (int i = 0; i < n; i++)
      {
         incProgress();
         sleepAndCheckCancel();

         SubPath subPath = subPaths.get(i);

         if (subPath.isContainer())
         {
            ShapeComponentVector fullVec = subPath.getCompleteVector();

            Vector<Integer> contentIndexes = new Vector<Integer>();
            contentIndexes.add(Integer.valueOf(i));

            Vector<Integer> contains = subPath.getContained();
            int capacity = subPath.getEndIndex()-subPath.getStartIndex()+1;

            for (Integer idx : contains)
            {
               SubPath interiorSp = subPaths.get(idx.intValue());

               if (interiorSp.getCompleteVector() == fullVec)
               {
                  contentIndexes.add(idx);

                  capacity += interiorSp.getEndIndex()-interiorSp.getStartIndex()+1;
               }
            }

            contentIndexes.sort(comparator);

            ShapeComponentVector newVec = new ShapeComponentVector(capacity);
            newVec.setRule(fullVec.getRule());
            Point2D prevPt = null;

            for (Integer idx : contentIndexes)
            {
               SubPath sp = subPaths.get(idx.intValue());
               int startIdx = sp.getStartIndex();
               int endIdx = sp.getEndIndex();

               for (int j = startIdx; j <= endIdx; j++)
               {
                  ShapeComponent comp = fullVec.get(j);
                  comp.setStart(prevPt);
                  newVec.add(comp);
                  prevPt = comp.getEnd();
               }
            }

            if (!newVec.isEmpty())
            {
               newShapesVec.add(newVec);
               results.put(contentIndexes.get(0), newVec);
            }
         }
         else if (!subPath.isInside())
         {
            ShapeComponentVector newVec = processSubPath(subPath);

            if (newVec != null)
            {
               results.put(Integer.valueOf(i), newVec);
            }
         }
      }
   }

   private void splitEvenExterior(Vector<SubPath> subPaths,
        HashMap<Integer,ShapeComponentVector> results)
   throws InterruptedException
   {
      int n = subPaths.size();

      for (int i = 0; i < n; i++)
      {
         incProgress();
         sleepAndCheckCancel();

         SubPath subPath = subPaths.get(i);

         int level = subPath.getContainerLevel(subPaths);

         if (subPath.isContainer())
         {
            if (level%2 == 0)
            {
               ShapeComponentVector fullVec = subPath.getCompleteVector();

               Vector<Integer> contentIndexes = new Vector<Integer>();
               contentIndexes.add(Integer.valueOf(i));

               Vector<Integer> contains = subPath.getContained();
               int capacity = subPath.getEndIndex()-subPath.getStartIndex()+1;

               for (Integer idx : contains)
               {
                  SubPath interiorSp = subPaths.get(idx.intValue());

                  if (interiorSp.getCompleteVector() == fullVec
                      && interiorSp.getContainerLevel(subPaths) == level+1)
                  {
                     contentIndexes.add(idx);

                     capacity += interiorSp.getEndIndex()-interiorSp.getStartIndex()+1;
                  }
               }

               contentIndexes.sort(comparator);

               ShapeComponentVector newVec = new ShapeComponentVector(capacity);
               newVec.setRule(fullVec.getRule());
               Point2D prevPt = null;

               for (Integer idx : contentIndexes)
               {
                  SubPath sp = subPaths.get(idx.intValue());
                  int startIdx = sp.getStartIndex();
                  int endIdx = sp.getEndIndex();

                  for (int j = startIdx; j <= endIdx; j++)
                  {
                     ShapeComponent comp = fullVec.get(j);
                     comp.setStart(prevPt);
                     newVec.add(comp);
                     prevPt = comp.getEnd();
                  }
               }

               if (!newVec.isEmpty())
               {
                  newShapesVec.add(newVec);
                  results.put(contentIndexes.get(0), newVec);
               }
            }
         }
         else if (!subPath.isInside() || (level%2 == 0))
         {
            ShapeComponentVector newVec = processSubPath(subPath);

            if (newVec != null)
            {
               results.put(Integer.valueOf(i), newVec);
            }
         }
      }
   }

   private ShapeComponentVector processSubPath(SubPath subPath)
   {
      int startIndex = subPath.getStartIndex();
      int endIndex = subPath.getEndIndex();
      ShapeComponentVector vec = subPath.getCompleteVector();

      ShapeComponentVector newVec = new ShapeComponentVector(endIndex-startIndex+1);
      Point2D prevPt = null;

      for (int i = startIndex; i <= endIndex; i++)
      {
         ShapeComponent comp = vec.get(i);
         comp.setStart(prevPt);

         if (!(comp.getType() == PathIterator.SEG_CLOSE && i != endIndex))
         {
            newVec.add(comp);
            prevPt = comp.getEnd();
         }
      }

      if (!newVec.isEmpty())
      {
         newShapesVec.add(newVec);
         dialog.repaintImagePanel(newVec.getBounds());
         return newVec;
      }

      return null;
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedSplitSubPaths(newShapesVec, continueToNextStep);
   }

   private int progress, maxProgress;
   private double minTinyArea;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private volatile Vector<ShapeComponentVector> newShapesVec;
   private int splitType, minTinySize;
   private boolean continueToNextStep, removeTiny;
   private Comparator<Integer> comparator;

   public static final int SPLIT_ALL=0, EVEN_INTERIOR_SPLIT=1, SPLIT_EXTERIOR_ONLY=2;
}

class MergeNearPaths extends SwingWorker<Void,Void>
{
   public MergeNearPaths(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapeList = shapeList;
      this.continueToNextStep = continueToNextStep;

      deltaThreshold = dialog.getMergeNearPathThreshold();
   }

   protected Void doInBackground() throws InterruptedException
   {
      if (shapeList == null || shapeList.isEmpty())
      {
         return null;
      }

      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      maxProgress = shapeList.size();
      progress = 0;

      for (int i = 0; i < shapeList.size()-1; i++)
      {
         incProgress();
         sleepAndCheckCancel();

         ShapeComponentVector vec = shapeList.get(i);
         boolean isOpenPath = false;

         if (vec.lastElement().getType() == PathIterator.SEG_CLOSE)
         {
            if (!vec.isFilled())
            {// don't merge unfilled loops
               continue;
            }
         }
         else
         {
            isOpenPath = true;
         }

         for (int j = i+1; j < shapeList.size(); j++)
         {
            ShapeComponentVector vec2 = shapeList.get(j);
            boolean isOpenPath2 = false;

            if (vec2.lastElement().getType() == PathIterator.SEG_CLOSE)
            {
               if (!vec2.isFilled()) continue;
            }
            else
            {
               isOpenPath2 = true;
            }

            boolean isModified = false;

            if (isOpenPath && isOpenPath2)
            {
               if (mergeLines(vec, vec2))
               {
                  isModified = true;
               }
            }
            else if (!isOpenPath && !isOpenPath2)
            {
               if (mergeRegions(vec, vec2))
               {
                  isModified = true;
               }
            }

            if (isModified)
            {
               dialog.addMessageIdLn("vectorize.merged_path", vec.svg());
               shapeList.remove(j);
               j--;
               dialog.repaintImagePanel(vec.getBounds());
               incProgress();
               sleepAndCheckCancel();
            }
         }
      }

      return null;
   }

   private boolean mergeLines(ShapeComponentVector vec1, 
       ShapeComponentVector vec2)
   {
      Point2D startPt1 = vec1.firstElement().getEnd();
      Point2D startPt2 = vec2.firstElement().getEnd();

      Point2D endPt1 = vec1.lastElement().getEnd();
      Point2D endPt2 = vec2.lastElement().getEnd();

      double sqThreshold = deltaThreshold*deltaThreshold;

      double dist1 = ShapeComponent.getSquareDistance(endPt1, startPt2);
      double dist2 = ShapeComponent.getSquareDistance(endPt2, startPt1);
      double dist3 = ShapeComponent.getSquareDistance(endPt2, endPt1);
      double dist4 = ShapeComponent.getSquareDistance(startPt1, startPt2);

      if (dist1 <= sqThreshold && dist1 <= dist2
           && dist1 <= dist3 && dist1 <= dist4)
      {
         vec1.appendPath(vec2, true);
         dialog.addMessageIdLn("vectorize.merging_paths", Math.sqrt(dist1),
          vec1.svg(), vec2.svg());
      }
      else if (dist2 <= sqThreshold && dist2 <= dist1
                && dist2 <= dist3 && dist2 <= dist4)
      {
         vec1.prependPath(vec2, true);
         dialog.addMessageIdLn("vectorize.merging_paths", Math.sqrt(dist2),
          vec1.svg(), vec2.svg());
      }
      else if (dist3 <= sqThreshold && dist3 <= dist1
                && dist3 <= dist2 && dist3 <= dist4)
      {
         vec1.lineTo(endPt1);

         for (int i = vec2.size()-1; i > 0; i--)
         {
            vec1.addComponent(vec2.get(i).reverse());
         }

         dialog.addMessageIdLn("vectorize.merging_paths", Math.sqrt(dist3),
          vec1.svg(), vec2.svg());
      }
      else if (dist4 <= sqThreshold && dist4 <= dist1
                && dist4 <= dist2 && dist4 <= dist3)
      {
         ShapeComponentVector reverse = new ShapeComponentVector(vec2.size());

         reverse.moveTo(endPt2);

         for (int i = vec2.size()-1; i > 0; i--)
         {
            reverse.addComponent(vec2.get(i).reverse());
         }

         vec1.prependPath(reverse, true);

         dialog.addMessageIdLn("vectorize.merging_paths", Math.sqrt(dist4),
          vec1.svg(), vec2.svg());
      }
      else
      {
         return false;
      }

      return true;
   }

   private boolean mergeRegions(ShapeComponentVector vec1, 
       ShapeComponentVector vec2)
   {
      if (vec1.hasSubPaths() || vec2.hasSubPaths()
       || vec1.isOppositeDirection(vec2))
      {
         return false;
      }

      int n1 = vec1.size();
      int n2 = vec2.size();

      if (n1 < 3 || n2 < 3)
      {
         return false;
      }

      Point2D startPt1 = vec1.firstElement().getEnd();
      Point2D startPt2 = vec2.firstElement().getEnd();

      Point2D endPt1 = vec1.get(n1-2).getEnd();
      Point2D endPt2 = vec2.get(n2-2).getEnd();

      double sqThreshold = deltaThreshold*deltaThreshold;
      double sum = 0.0;

      int idx1 = -1;
      int idx2 = -1;

      for (int i = 0; i < n1-1; i++)
      {
         ShapeComponent comp1 = vec1.get(i);

         if (comp1.isCurve())
         {
            continue;
         }

         Point2D p1 = (i == 0 ? startPt1 : comp1.getEnd());
         Point2D prevPt = endPt2;

         for (int j = 0; j < n2; j++)
         {
            ShapeComponent comp2 = vec2.get(j);

            Point2D p2 = (j == 0 ? startPt2 : comp2.getEnd());

            if (comp2.isCurve())
            {
               prevPt = p2;
               continue;
            }

            double dist = ShapeComponent.getSquareDistance(p1, p2);

            if (dist > sqThreshold)
            {
               prevPt = p2;
               continue;
            }

            sum = Math.sqrt(dist);

            Point2D nextPt = (i == n1-2 ? startPt1 : vec1.get(i+1).getEnd());

            dist = ShapeComponent.getSquareDistance(prevPt, nextPt);

            if (dist <= sqThreshold)
            {
               sum += Math.sqrt(dist);
               idx1 = i;
               idx2 = j;

               break;
            }

            prevPt = p2;
         }

         if (idx1 != -1)
         {
            break;
         }
      }

      if (idx1 == -1 || idx2 == -1)
      {
         return false;
      }

      int endIdx1 = idx1+1;
      int endIdx2 = (idx2 == 0 ? n2-2 : idx2-1);
      int N = 2;

      for (int i = endIdx1+1, j = 1; i < n1-1; i++, j++)
      {
         ShapeComponent comp1 = vec1.get(i);

         int k = endIdx2-j;

         if (k < 0)
         {
            k = n2-1 + k;
         }

         ShapeComponent comp2 = vec2.get(k);

         if (comp1.isCurve() || comp2.isCurve())
         {
            break;
         }

         Point2D p1 = comp1.getEnd();
         Point2D p2 = comp2.getEnd();

         double dist = ShapeComponent.getSquareDistance(p1, p2);

         if (dist > sqThreshold)
         {
            break;
         }

         sum += Math.sqrt(dist);
         N++;
         endIdx1 = i;
         endIdx2 = k;
      }

      dialog.addMessageIdLn("vectorize.merging_regions", sum/N,
          vec1.svg(), vec2.svg());

      ShapeComponentVector vec3 = new ShapeComponentVector(N);

      for (int i = idx2; i < n2; i++)
      {
         if (i == endIdx2) break;

         ShapeComponent comp = vec2.get(i);

         if (vec3.isEmpty())
         {
            if (comp.getType() == PathIterator.SEG_CLOSE)
            {
               vec3.moveTo(startPt2);
            }
            else
            {
               vec3.moveTo(comp.getEnd());
            }
         }
         else if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            vec3.lineTo(startPt2);
         }
         else
         {
            vec3.addComponent(comp);
         }
      }

      if (idx2 > endIdx2)
      {
         for (int i = 0; i <= endIdx2; i++)
         {
            ShapeComponent comp = vec2.get(i);

            if (vec3.isEmpty())
            {
               vec3.moveTo(comp.getEnd());
            }
            else if (i == 0)
            {
               vec3.lineTo(startPt2);
            }
            else
            {
               vec3.addComponent(comp);
            }
         }
      }

      for (int i = endIdx1; i < n1; i++)
      {
         ShapeComponent comp = vec1.get(i);

         if (vec3.isEmpty())
         {
            vec3.moveTo(comp.getEnd());
         }
         else if (i == 0)
         {
            vec3.lineTo(startPt1);
         }
         else
         {
            vec3.addComponent(comp);
         }
      }

      vec1.setSize(idx1+1);

      vec1.appendPath(vec3, true);

      return true;
   }

   private void incProgress()
   {
      progress++;
      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
   }

   private void sleepAndCheckCancel() throws InterruptedException
   {
      dialog.updateTimeElapsed();
      Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

      // check for cancel
      if (dialog.isCancelled())
      {
         throw new UserCancelledException(dialog.getMessageDictionary());
      }
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedMergeNearPaths(shapeList, continueToNextStep);
   }


   private int progress, maxProgress;
   private double deltaThreshold;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private boolean continueToNextStep;
}

class LineDetection extends SwingWorker<Void,ShapeComponentVector>
{
   public LineDetection(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapeList = shapeList;

      deltaThreshold = dialog.getDeltaThreshold();
      varianceThreshold = dialog.getDeltaVarianceThreshold();
      returnPtDist = dialog.getSpikeReturnDistance();
      minStubLength = dialog.getMinimumStubLength();
      maxTinyStep = dialog.getLineDetectTinyStepThreshold();
      doLineIntersectionCheck = dialog.isIntersectionDetectionOn();
      this.continueToNextStep = continueToNextStep;
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (shapeList == null || shapeList.isEmpty())
      {
         return null;
      }

      progress = 0;
      int numShapes = shapeList.size();
      maxProgress = numShapes;

      Vector<ShapeComponentVector> oldShapeList = new Vector<ShapeComponentVector>();
      oldShapeList.addAll(shapeList);
      shapeList.clear();

      for (int i = 0; i < numShapes; i++)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException(dialog.getMessageDictionary());
         }

         tryLineify(oldShapeList.get(i));
         incProgress();
      }

      return null;
   }

   private void incProgress()
   {
      progress++;
      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
   }

   private void updateCurrentShape(ShapeComponentVector path)
      throws InterruptedException
   {
      dialog.setWorkingShape(path.getPath());

      dialog.updateTimeElapsed();
      Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

      // check for cancel
      if (dialog.isCancelled())
      {
         throw new UserCancelledException(dialog.getMessageDictionary());
      }
   }

   protected void process(java.util.List<ShapeComponentVector> shapeVecList)
   {
      Iterator<ShapeComponentVector> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         ShapeComponentVector vec = iter.next();

         dialog.repaintImagePanel(vec.getBounds());
      }
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedLineDetection(shapeList, continueToNextStep);
   }

   private void addShape(ShapeComponentVector newShape)
   {
      shapeList.add(newShape);
      publish(newShape);
   }

   private void addShape(ShapeComponentVector newShape, String id,
     Object... params)
   {
      shapeList.add(newShape);

      addPathResultMessage(getNumShapes(), id, params);

      publish(newShape);
   }

   private void addPathResultMessage(int n, String id, Object... params)
   {
      dialog.addMessageIdLn("vectorize.path_n_result",
         n, dialog.getResources().getMessage(id, params));
   }

   private int getNumShapes()
   {
      return shapeList.size();
   }

   private void setLineWidth(ShapeComponentVector shape, double delta)
   {
      double width;

      if (dialog.isFixedLineWidth())
      {
         width = dialog.getFixedLineWidth();
      }
      else
      {
         width = 2.0*delta;

         if (dialog.isRoundRelativeOn())
         {
            width = Math.max(1.0, Math.floor(width));
         }
      }

      shape.setLineWidth(width);
   }

   private void tryLineify(ShapeComponentVector vec)
     throws InterruptedException
   {
      int startIdx = 0;
      int n = vec.size();

      if (n < 4)
      {
         addShape(vec, "vectorize.message.insufficient_to_vectorize", n);
         return;
      }

      Vector<SubPath> subPaths = new Vector<SubPath>();

      for (int j = 0; j < n; j++)
      {
         ShapeComponent comp = vec.get(j);

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            subPaths.add(new SubPath(vec, startIdx, j));
            startIdx = j+1;
         }
      }

      if (startIdx != n)
      {
         subPaths.add(new SubPath(vec, startIdx, n-1));
      }

      if (subPaths.isEmpty())
      {
         addShape(vec, "vectorize.message.no_closed_subpaths");
         return;
      }

      if (subPaths.size() == 1)
      {
         dialog.addMessageIdLn("vectorize.message.single_closed_subpath_found");
         tryLineifyRegion(vec);
         return;
      }

      n = subPaths.size();
      Shape[] shapes = new Shape[n];

      SubPath outer = null;
      int outerIdx = -1;
      Vector<SubPath> inner = new Vector<SubPath>();

      for (int i = 0; i < n; i++)
      {
         SubPath sp1 = subPaths.get(i);
         Shape shape1 = sp1.getShape();
         shapes[i] = shape1;
         dialog.addMessageIdLn("vectorize.message.sub_path", (i+1), sp1.getSpecs());

         for (int j = 0; j < i; j++)
         {
            // is this sub-path inside or outside any of the other
            // sub-paths?

            SubPath sp2 = subPaths.get(j);
            Shape shape2 = shapes[j];
            
            Point2D pt1 = vec.get(sp1.getStartIndex()).getEnd();
            Point2D pt2 = vec.get(sp2.getStartIndex()).getEnd();
   
            if (shape1.contains(pt2))
            {
               if (inner.contains(sp1))
               {
                  addShape(vec, "vectorize.message.inner_contains",
                     (i+1));
                  return;
               }

               if (outer == null)
               {
                  outer = sp1;
                  outerIdx = i;
                  dialog.addMessageIdLn("vectorize.message.outer_sub_path", (i+1));
               }

               if (outer == sp1)
               {
                  inner.add(sp2);
                  dialog.addMessageIdLn("vectorize.message.inner_sub_path", (j+1));
               }
            }
            else if (shape2.contains(pt1))
            {
               if (inner.contains(sp2))
               {
                  addShape(vec, "vectorize.message.inner_contains",
                     (j+1));
                  return;
               }

               if (outer == null)
               {
                  outer = sp2;
                  outerIdx = j;
                  dialog.addMessageIdLn("vectorize.message.outer_sub_path", (j+1));
               }

               if (outer == sp2)
               {
                  inner.add(sp1);
                  dialog.addMessageIdLn("vectorize.message.inner_sub_path", (i+1));
               }
            }
            else
            {
               dialog.addMessageIdLn("vectorize.message.sub_paths_dont_contain",
                 (i+1), pt2.getX(), pt2.getY(), (j+1), pt1.getX(), pt1.getY());
            }
         }
      }

      if (outer == null || inner.isEmpty())
      {
         addShape(vec, "vectorize.message.no_outer_inner");
         return;
      }

      for (int i = 0; i < n; i++)
      {
         SubPath sp = subPaths.get(i);

         if (!(outer == sp || inner.contains(sp)))
         {
            addShape(vec, "vectorize.message.not_inner_outer", (i+1));
            return;
         }
      }

      if (inner.size() == 1)
      {
         dialog.addMessageIdLn("vectorize.message.possible_loop");
         tryLineifyLoop(outer, inner.firstElement());
         return;
      }

      dialog.addMessageIdLn("vectorize.message.multi_inner");

      Vector<ShapeComponentVector> shapeVecs = new Vector<ShapeComponentVector>(n);
      boolean modified = false;
      double gradientEpsilon = dialog.getGradientEpsilon();

      for (int i = 0; i < inner.size(); i++)
      {
         SubPath sp1 = inner.get(i);
         int startIdx1 = sp1.getStartIndex();
         int endIdx1 = sp1.getEndIndex();
         Point2D startPt1 = vec.get(startIdx1).getEnd();
         ShapeComponentVector currentVec = null;

         for (int j = i+1; j < inner.size(); j++)
         {
            SubPath sp2 = inner.get(j);

            int startIdx2 = sp2.getStartIndex();
            int endIdx2 = sp2.getEndIndex();
            Point2D startPt2 = vec.get(startIdx2).getEnd();

            int closestStart1=-1;
            int closestStart2=-1;
            boolean reverse = !sp1.isOppositeDirection(sp2);

            for (int k1 = startIdx1; k1 < endIdx1; k1++)
            {
               ShapeComponent comp1 = vec.get(k1);
               Point2D p1 = comp1.getEnd();

               if (reverse)
               {
                  for (int k2 = endIdx2; k2 > startIdx2; k2--)
                  {
                     ShapeComponent comp2 = vec.get(k2);
                     Point2D p2;

                     if (comp2.getType() == PathIterator.SEG_CLOSE)
                     {
                        p2 = startPt2;
                     }
                     else
                     {
                        p2 = comp2.getEnd();
                     }

                     double dist = getDistance(p1, p2);

                     if (dist <= deltaThreshold)
                     {
                        closestStart1 = k1;
                        closestStart2 = k2;
                        break;
                     }
                  }
               }
               else
               {
                  for (int k2 = startIdx2; k2 < endIdx2; k2++)
                  {
                     ShapeComponent comp2 = vec.get(k2);
                     Point2D p2 = comp2.getEnd();

                     double dist = getDistance(p1, p2);

                     if (dist <= deltaThreshold)
                     {
                        closestStart1 = k1;
                        closestStart2 = k2;
                        break;
                     }
                  }
               }

               if (closestStart1 > -1)
               {
                  break;
               }
            }

            if (closestStart1 > -1)
            {
               int closestEnd1 = -1;
               int closestEnd2 = -1;

               for (int k1 = endIdx1-1; k1 > closestStart1; k1--)
               {
                  ShapeComponent comp1 = vec.get(k1);
                  Point2D p1 = comp1.getEnd();

                  if (reverse)
                  {
                     for (int k2 = startIdx2; k2 < closestStart2; k2++)
                     {
                        ShapeComponent comp2 = vec.get(k2);
                        Point2D p2 = comp2.getEnd();

                        double dist = getDistance(p1, p2);

                        if (dist <= deltaThreshold)
                        {
                           closestEnd1 = k1;
                           closestEnd2 = k2;
                           break;
                        }
                     }
                  }
                  else
                  {
                     for (int k2 = endIdx2-1; k2 > closestStart2; k2--)
                     {
                        ShapeComponent comp2 = vec.get(k2);
                        Point2D p2 = comp2.getEnd();

                        double dist = getDistance(p1, p2);

                        if (dist <= deltaThreshold)
                        {
                           closestEnd1 = k1;
                           closestEnd2 = k2;
                           break;
                        }
                     }
                  }

                  if (closestEnd1 > -1)
                  {
                     break;
                  }
               }

               if (closestEnd1 != -1)
               {
                  ShapeComponent comp = vec.get(closestStart1);
                  Point2D p1, p2, p3, p4;

                  if (comp.getType() == PathIterator.SEG_CLOSE)
                  {
                     p1 = startPt1;
                     closestStart1 = startIdx1;
                  }
                  else
                  {
                     p1 = comp.getEnd();
                  }

                  comp = vec.get(closestStart2);

                  if (comp.getType() == PathIterator.SEG_CLOSE)
                  {
                     p2 = startPt2;
                     closestStart2 = startIdx2;
                  }
                  else
                  {
                     p2 = comp.getEnd();
                  }

                  comp = vec.get(closestEnd1);

                  if (comp.getType() == PathIterator.SEG_CLOSE)
                  {
                     p3 = startPt1;
                     closestEnd1 = startIdx1;
                  }
                  else
                  {
                     p3 = comp.getEnd();
                  }

                  comp = vec.get(closestEnd2);

                  if (comp.getType() == PathIterator.SEG_CLOSE)
                  {
                     p4 = startPt2;
                     closestEnd2 = startIdx2;
                  }
                  else
                  {
                     p4 = comp.getEnd();
                  }

                  dialog.addMessageIdLn("vectorize.possible_border",
                   (i+1), p1.getX(), p1.getY(), p3.getX(), p3.getY(),
                   (j+1), p2.getX(), p2.getY(), p4.getX(), p4.getY());

                  if (tryLineifyBorder(sp1, closestStart1, closestEnd1, 
                                   sp2, closestStart2, closestEnd2, reverse))
                  {
                     dialog.addMessageIdLn("vectorize.splitting_border");

                     currentVec = new ShapeComponentVector();

                     currentVec.moveTo(startPt1);

                     for (int k1 = startIdx1+1; k1 <= closestStart1; k1++)
                     {
                        if (k1 == endIdx1)
                        {
                           currentVec.lineTo(startPt1);
                        }
                        else
                        {
                           currentVec.add(new ShapeComponent(vec.get(k1)));
                        }
                     }

                     ShapeComponent prevComp = currentVec.lastElement();

                     if (prevComp.getType() == PathIterator.SEG_LINETO)
                     {
                        Point2D dp1 = prevComp.getEndGradient();
                        Point2D endPt = vec.get(closestStart2).getEnd();
                        Point2D dp2 = prevComp.getGradientToEnd(endPt);

                        double theta1 = Math.atan2(dp1.getY(), dp1.getX());
                        double theta2 = Math.atan2(dp2.getY(), dp2.getX());

                        if (Math.abs(theta1-theta2) < gradientEpsilon)
                        {
                           prevComp.setEndPoint(endPt);
                        }
                        else
                        {
                           currentVec.lineTo(endPt);
                           prevComp = currentVec.lastElement();
                        }
                     }
                     else
                     {
                        currentVec.lineTo(vec.get(closestStart2).getEnd());
                        prevComp = currentVec.lastElement();
                     }

                     if (reverse)
                     {
                        if (closestStart2+1 < endIdx2)
                        {
                           comp = vec.get(closestStart2+1);

                           if (comp.getType() == PathIterator.SEG_LINETO)
                           {
                              Point2D dp1 = prevComp.getEndGradient();
                              Point2D endPt = comp.getEnd();
                              Point2D dp2 = prevComp.getGradientToEnd(endPt);

                              double theta1 = Math.atan2(dp1.getY(), dp1.getX());
                              double theta2 = Math.atan2(dp2.getY(), dp2.getX());

                              if (Math.abs(theta1-theta2) < gradientEpsilon)
                              {
                                 prevComp.setEndPoint(endPt);
                              }
                              else
                              {
                                 currentVec.add(new ShapeComponent(comp));
                                 prevComp = currentVec.lastElement();
                              }
                           }
                           else
                           {
                              currentVec.add(new ShapeComponent(comp));
                              prevComp = currentVec.lastElement();
                           }
                        }

                        if (closestStart2 < closestEnd2)
                        {
                           for (int k2 = closestStart2+2; k2 < closestEnd2; k2++)
                           {
                              currentVec.add(new ShapeComponent(vec.get(k2)));
                           }
                        }
                        else
                        {
                           for (int k2 = closestStart2+2; k2 < endIdx2; k2++)
                           {
                              currentVec.add(new ShapeComponent(vec.get(k2)));
                           }

                           for (int k2 = endIdx2+1; k2 <= closestEnd2; k2++)
                           {
                              currentVec.add(new ShapeComponent(vec.get(k2)));
                           }
                        }
                     }
                     else
                     {
                        for (int k2 = closestStart2-1; k2 >= startIdx2; k2--)
                        {
                           currentVec.lineTo(vec.get(k2).getEnd());
                        }

                        for (int k2 = endIdx2; k2 >= closestEnd2; k2--)
                        {
                           currentVec.lineTo(vec.get(k2-1).getEnd());
                        }
                     }

                     prevComp = currentVec.lastElement();

                     if (prevComp.getType() == PathIterator.SEG_LINETO)
                     {
                        Point2D dp1 = prevComp.getEndGradient();
                        Point2D endPt = vec.get(closestEnd1).getEnd();
                        Point2D dp2 = prevComp.getGradientToEnd(endPt);

                        double theta1 = Math.atan2(dp1.getY(), dp1.getX());
                        double theta2 = Math.atan2(dp2.getY(), dp2.getX());

                        if (Math.abs(theta1-theta2) < gradientEpsilon)
                        {
                           prevComp.setEndPoint(endPt);
                        }
                        else
                        {
                           currentVec.lineTo(endPt);
                           prevComp = currentVec.lastElement();
                        }
                     }
                     else
                     {
                        currentVec.lineTo(vec.get(closestEnd1).getEnd());
                        prevComp = currentVec.lastElement();
                     }

                     if (closestEnd1+1 <= endIdx1)
                     {
                        comp = vec.get(closestEnd1+1);

                        if (comp.getType() == PathIterator.SEG_LINETO)
                        {
                           Point2D dp1 = prevComp.getEndGradient();
                           Point2D endPt = comp.getEnd();
                           Point2D dp2 = prevComp.getGradientToEnd(endPt);

                           double theta1 = Math.atan2(dp1.getY(), dp1.getX());
                           double theta2 = Math.atan2(dp2.getY(), dp2.getX());

                           if (Math.abs(theta1-theta2) < gradientEpsilon)
                           {
                              prevComp.setEndPoint(endPt);
                           }
                           else
                           {
                              currentVec.add(new ShapeComponent(comp));
                           }
                        }
                        else
                        {
                           currentVec.add(new ShapeComponent(comp));
                        }
                     }

                     for (int k1 = closestEnd1+2; k1 <= endIdx1; k1++)
                     {
                        currentVec.add(new ShapeComponent(vec.get(k1)));
                     }

                     modified = true;
                     inner.remove(j);
                     break;
                  }
               }
            }
         }

         if (currentVec == null)
         {
            shapeVecs.add(sp1.toVector());
         }
         else
         {
            shapeVecs.add(currentVec);
         }
      }

      if (modified)
      {
         ShapeComponentVector newVec = new ShapeComponentVector();

         for (int i = 0; i < shapeVecs.size(); i++)
         {
            if (i == outerIdx)
            {
               newVec.appendSubPath(outer);
            }

            newVec.appendPath(shapeVecs.get(i));
         }

         if (outerIdx >= shapeVecs.size())
         {
            newVec.appendSubPath(outer);
         }

         tryLineify(newVec);
      }
      else
      {
         addShape(vec);
      }
   }

   private boolean tryLineifyBorder(SubPath sp1, int start1, int end1,
     SubPath sp2, int start2, int end2, boolean reverse)
    throws InterruptedException
   {
      int n1, n2;

      if (start1 < end1)
      {
         n1 = end1-start1+1;
      }
      else
      {
         n1 = sp1.getEndIndex() - end1 + start1 - sp1.getStartIndex()+1;
      }

      if (reverse)
      {
         if (start2 > end2)
         {
            n2 = start2-end2+1;
         }
         else
         {
            n2 = sp2.getStartIndex()-start2 + sp2.getEndIndex() - end2 + 1;
         }
      }
      else
      {
         if (start2 < end2)
         {
            n2 = end2-start2+1;
         }
         else
         {
            n2 = sp2.getEndIndex() - end2 + start2 - sp2.getStartIndex()+1;
         }
      }

      Vector<Point2D> pts1 = new Vector<Point2D>(n1);
      Vector<Point2D> pts2 = new Vector<Point2D>(n2);

      ShapeComponentVector vec = sp1.getCompleteVector();
      Point2D startPt = vec.get(sp1.getStartIndex()).getEnd();

      if (start1 < end1)
      {
         for (int i = start1; i <= end1; i++)
         {
            ShapeComponent comp = vec.get(i);

            if (comp.getType() == PathIterator.SEG_CLOSE)
            {
               pts1.add(startPt);
            }
            else
            {
               pts1.add(comp.getEnd());
            }
         }
      }
      else
      {
         for (int i = start1; i < sp1.getEndIndex(); i++)
         {
            ShapeComponent comp = vec.get(i);
            pts1.add(comp.getEnd());
         }

         for (int i = sp1.getStartIndex(); i <= end1; i++)
         {
            ShapeComponent comp = vec.get(i);

            if (comp.getType() == PathIterator.SEG_CLOSE)
            {
               pts1.add(startPt);
            }
            else
            {
               pts1.add(comp.getEnd());
            }
         }
      }

      vec = sp2.getCompleteVector();
      startPt = vec.get(sp2.getStartIndex()).getEnd();

      if (reverse)
      {
         if (start2 > end2)
         {
            for (int i = start2; i >= end2; i--)
            {
               ShapeComponent comp = vec.get(i);

               if (comp.getType() == PathIterator.SEG_CLOSE)
               {
                  pts2.add(startPt);
               }
               else
               {
                  pts2.add(comp.getEnd());
               }
            }
         }
         else
         {
            for (int i = start2; i >= sp2.getStartIndex(); i--)
            {
               ShapeComponent comp = vec.get(i);
               pts2.add(comp.getEnd());
            }

            for (int i = sp2.getEndIndex()-1; i >= end2; i--)
            {
               ShapeComponent comp = vec.get(i);
               pts2.add(comp.getEnd());
            }
         }
      }
      else
      {
         if (start2 < end2)
         {
            for (int i = start2; i <= end2; i++)
            {
               ShapeComponent comp = vec.get(i);

               if (comp.getType() == PathIterator.SEG_CLOSE)
               {
                  pts2.add(startPt);
               }
               else
               {
                  pts2.add(comp.getEnd());
               }
            }
         }
         else
         {
            for (int i = start2; i < sp2.getEndIndex(); i++)
            {
               ShapeComponent comp = vec.get(i);
               pts2.add(comp.getEnd());
            }

            for (int i = sp2.getStartIndex(); i <= end2; i++)
            {
               ShapeComponent comp = vec.get(i);

               if (comp.getType() == PathIterator.SEG_CLOSE)
               {
                  pts2.add(startPt);
               }
               else
               {
                  pts2.add(comp.getEnd());
               }
            }
         }
      }

      return tryLineify(pts1, pts2);
   }

   private String listBulgePoints(Vector<Point2D> pts1, Vector<Point2D> pts2)
   {
      StringBuilder builder = new StringBuilder();
      JDRResources resources = dialog.getResources();

      for (int i = 0, n = pts1.size(); i < n; i++)
      {
         Point2D p1 = pts1.get(i);
         Point2D p2 = pts2.get(i);

         builder.append(String.format("%n"));
         builder.append(resources.getMessage("vectorize.lineify_border_pair",
           i, p1.getX(), p1.getY(), p2.getX(), p2.getY()));
      }

      return builder.toString();
   }

   private boolean tryLineify(Vector<Point2D> pts1, Vector<Point2D> pts2)
    throws InterruptedException
   {
      boolean success = false;

      int n1 = pts1.size();
      int n2 = pts2.size();

      if (n1 != n2)
      {
         dialog.addMessageIdLn("vectorize.border_unequal_points",
           n1, n2);
         addBorderPoints(pts1, pts2);

         n1 = pts1.size();
         n2 = pts2.size();

         if (n1 != n2)
         {
            addPathResultMessage(getNumShapes()+1, 
               "vectorize.cant_balance_border_points", n1, n2);
            return false;
         }
      }

      dialog.addMessageIdLn("vectorize.lineify_between_borders",
       listBulgePoints(pts1, pts2));

      double gradientEpsilon = dialog.getGradientEpsilon();

      ShapeComponentVector newPath1 = new ShapeComponentVector();

      Point2D p1 = pts1.get(0);
      Point2D p2 = pts2.get(0);

      double prevX=-1;
      double prevY=-1;

      double averageDelta1 = 0.0;
      int numPts1 = 0;

      double newPath1LastX = -1;
      double newPath1LastY = -1;

      boolean inc = false;
      boolean dec = false;
      double prevDelta = 0.0;
      double firstDelta = 0.0;

      int startBulge = 0;

      for (int i = 0; i < n1; i++)
      {
         p1 = pts1.get(i);
         p2 = pts2.get(i);

         double dx = 0.5*(p2.getX() - p1.getX());
         double dy = 0.5*(p2.getY() - p1.getY());

         double delta = Math.sqrt(dx*dx + dy*dy);

         if (delta <= deltaThreshold)
         {
            if (!(i == 0 || (i == 1 && prevDelta < ShapeComponentVector.EPSILON)))
            {
               if (firstDelta == 0.0)
               {
                  firstDelta = delta;
               }

               if (delta < prevDelta - ShapeComponentVector.EPSILON)
               {
                  dec = true;
               }
               else if (delta > prevDelta + ShapeComponentVector.EPSILON)
               {
                  inc = true;
               }
            }

            double x = p1.getX() + dx;
            double y = p1.getY() + dy;

            if (newPath1.isEmpty())
            {
               newPath1.moveTo(x, y);
            }
            else
            {
               newPath1.lineTo(x, y, gradientEpsilon);
            }

            numPts1++;
            averageDelta1 += delta;

            newPath1LastX = x;
            newPath1LastY = y;

            prevX = x;
            prevY = y;
         }
         else
         {
            startBulge = i;
            break;
         }

         prevDelta = delta;
      }

      // No wedge detection if path fits entire region.

      if (startBulge == 0 && numPts1 > 1)
      {
         // numPts1 may not be the same as newPath1.size() as
         // lineTo(double,double,double) was used.

         averageDelta1 /= numPts1;

         newPath1.setFilled(false);
         setLineWidth(newPath1, averageDelta1);
         addShape(newPath1, "vectorize.success_no_variance",
          averageDelta1);
         return true;
      }

      if (!doLineIntersectionCheck)
      {
         addPathResultMessage(getNumShapes()+1, "vectorize.too_wide");
         return false;
      }

      ShapeComponentVector newPath2 = null;

      double path1Length = newPath1 == null ? 0.0 : newPath1.getEstimatedLength();

      if (newPath1.size() <= 1 || (inc && !dec))
      {
         if (inc)
         {
            // if the distance kept increasing then may be the start of a wedge
            dialog.addMessageIdLn("vectorize.increasing_start",
             firstDelta, prevDelta, newPath1.size(), path1Length,
               newPath1.svg());
         }

         newPath1 = null;
         startBulge = 0;
         path1Length = 0.0;
      }

      int endBulge = n1-1;

      double averageDelta2 = 0.0;
      int numPts2 = 0;

      inc = false;
      dec = false;
      prevDelta = 0;
      firstDelta = 0.0;

      for (int i = 1, m = n1-startBulge; i <= m; i++)
      {
         p1 = pts1.get(n1-i);
         p2 = pts2.get(n2-i);

         double dx = 0.5*(p2.getX()-p1.getX());
         double dy = 0.5*(p2.getY()-p1.getY());

         double delta = Math.sqrt(dx*dx + dy*dy);

         if (delta > deltaThreshold)
         {
            endBulge = n1-i;

            prevX = -1;
            prevY = -1;

            for (int j = endBulge+1; j < n1; j++)
            {
               p1 = pts1.get(j);
               p2 = pts2.get(j);

               double x = p1.getX() + 0.5*(p2.getX() - p1.getX());
               double y = p1.getY() + 0.5*(p2.getY() - p1.getY());

               if (newPath2 == null)
               {
                  if (newPath1 == null 
                      || getDistance(newPath1LastX, newPath1LastY, x, y)
                            > deltaThreshold)
                  {
                     if (newPath1 != null)
                     {
                        if (path1Length < minStubLength)
                        {
                           dialog.addMessageIdLn("vectorize.discarding_small_stub",
                            path1Length, newPath1.svg());

                           newPath1 = null;
                           startBulge = 0;
                        }
                     }

                     newPath2 = new ShapeComponentVector();
                     newPath2.moveTo(x, y);
                  }
                  else
                  {
                     newPath2 = newPath1;
                     newPath2.lineTo(x, y, gradientEpsilon);
                  }
               }
               else
               {
                  newPath2.lineTo(x, y, gradientEpsilon);
               }

               prevX = x;
               prevY = y;
            }

            break;
         }

         if (!(i == 1 || (i == 2 && prevDelta < ShapeComponentVector.EPSILON)))
         {
            if (firstDelta == 0.0)
            {
               firstDelta = delta;
            }

            if (delta < prevDelta - ShapeComponentVector.EPSILON)
            {
               dec = true;
            }
            else if (delta > prevDelta + ShapeComponentVector.EPSILON)
            {
               inc = true;
            }
         }

         numPts2++;
         averageDelta2 += delta;

         prevDelta = delta;
      }

      if (newPath2 == newPath1)
      {
         numPts1 += numPts2;
         averageDelta1 += averageDelta2;
         newPath2 = null;
         path1Length = newPath1.getEstimatedLength();
      }

      if (newPath1 != null && newPath1.size() > 1)
      {
         if (numPts1 > 0)
         {
            averageDelta1 /= numPts1;
         }

         newPath1.setFilled(false);
         setLineWidth(newPath1, averageDelta1);
         addShape(newPath1, "vectorize.success_with_line_length",
          averageDelta1, path1Length);
         success = true;
      }

      double path2Length = 0.0;

      if (newPath2 != null)
      {
         path2Length = newPath2.getEstimatedLength();

         if (path2Length < minStubLength)
         {
            dialog.addMessageIdLn("vectorize.discarding_small_stub",
             path2Length, newPath2.svg());
            newPath2 = null;
            endBulge = n1-1;
         }
         else if (inc && !dec)
         {
            dialog.addMessageIdLn("vectorize.decreasing_end",
               prevDelta, firstDelta, newPath2.size(), path2Length,
               newPath2.svg());
            newPath2 = null;
            endBulge = n1-1;
         }
         else if (newPath2.size() > 1)
         {
            if (numPts2 > 0)
            {
               averageDelta2 /= numPts2;
            }

            newPath2.setFilled(false);
            setLineWidth(newPath2, averageDelta2);
            addShape(newPath2, "vectorize.success_with_line_length",
             averageDelta2, path2Length);
            success = true;
         }
         else
         {
            newPath2 = null;
            endBulge = n1-1;
         }
      }

      if (!success)
      {
         addPathResultMessage(getNumShapes()+1, "vectorize.too_wide");
         return false;
      }

      ShapeComponentVector newPath = new ShapeComponentVector();

      Point2D startBulgePt1 = pts1.get(startBulge);
      Point2D startBulgePt2 = pts2.get(startBulge);
      Point2D endBulgePt1 = pts1.get(endBulge);
      Point2D endBulgePt2 = pts2.get(endBulge);

      Point2D p0 = startBulgePt1;
      newPath.moveTo(p0);

      dialog.addMessageIdLn("vectorize.bulge_detected", 
        startBulge, 
        startBulgePt1.getX(), startBulgePt1.getY(),
        startBulgePt2.getX(), startBulgePt2.getY(),
        endBulge,
        endBulgePt1.getX(), endBulgePt1.getY(),
        endBulgePt2.getX(), endBulgePt2.getY());

      for (int i = startBulge+1; i <= endBulge; i++)
      {
         p1 = pts1.get(i);

         newPath.lineTo(p1, gradientEpsilon);

         p0 = p1;
      }

      for (int i = endBulge; i >= startBulge; i--)
      {
         p1 = pts2.get(i);

         newPath.lineTo(p1, gradientEpsilon);

         p0 = p1;
      }

      if (newPath.size() <= 2)
      {
         addPathResultMessage(getNumShapes()+1, "vectorize.mid_region_collapsed");
         return false;
      }

      newPath.closePath();

      dialog.addMessageIdLn("vectorize.mid_region", newPath.svg());

      tryLineifyRegion(newPath);

      return success;
   }

   private void addBorderPoints(Vector<Point2D> pts1, Vector<Point2D> pts2)
   {
      int n1 = pts1.size();
      int n2 = pts2.size();

      if (n1 < n2)
      {
         addBorderPoints(pts2, pts1);
         return;
      }

      if (n1 == n2)
      {
         return;
      }

      for (int i = 1; i < pts2.size() && i < n1-1 && pts2.size() < n1; i++)
      {
         Point2D p1 = pts1.get(i);
         Point2D p2 = pts2.get(i);

         Point2D p0 = pts2.get(i-1);

         // get closest point on p0-p2 to p1

         pts2.add(i, JDRLine.getClosestPointAlongLine(p0, p2, p1));

         if (pts2.size() == n1)
         {
            return;
         }

         int j = pts2.size()-1-i;

         p1 = pts1.get(n1-1-i);
         p2 = pts2.get(j);

         p0 = pts2.get(j+1);

         // get closest point on p0-p2 to p1

         pts2.add(j+1, JDRLine.getClosestPointAlongLine(p0, p2, p1));

         if (pts2.size() == n1)
         {
            return;
         }
      }
   }

   private boolean tryLineifyBulge(ShapeComponentVector vec, int idx1, int idx2)
    throws InterruptedException
   {
      int n = vec.size();

      Vector<Point2D> pts1 = new Vector<Point2D>(n);
      Vector<Point2D> pts2 = new Vector<Point2D>(n);

      for (int i = idx1+1; i < idx2; i++)
      {
         pts1.add(vec.get(i).getEnd());
      }

      for (int i = idx1; i >= 0; i--)
      {
         pts2.add(vec.get(i).getEnd());
      }

      for (int i = n-2; i >= idx2; i--)
      {
         pts2.add(vec.get(i).getEnd());
      }

      return tryLineify(pts1, pts2);
   }

   private boolean findBulge(ShapeComponentVector vec)
     throws InterruptedException
   {
      int n = vec.size()-1;

      int idx1 = -1;

      Point2D p0 = vec.get(0).getEnd();
      Point2D p1 = p0;

      Point2D q1=null, q2=null;

      for (int i = 0; i < n-1; i++)
      {
         Point2D p2 = vec.get(i+1).getEnd();

         double delta = 0.5*getDistance(p1, p2);

         if (delta <= deltaThreshold)
         {
            q1 = p1;
            q2 = p2;
            idx1 = i;
            break;
         }

         p1 = p2;
      }

      if (idx1 == -1)
      {
         return false;
      }

      int idx2 = -1;

      p1 = p0;

      for (int i = n; i > idx1; i--)
      {
         Point2D p2 = vec.get(i-1).getEnd();

         double delta = 0.5*getDistance(p1, p2);

         if (delta <= deltaThreshold
              && (getDistance(p1, q1) > varianceThreshold
                  && getDistance(p2, q2) > varianceThreshold))
         {
            idx2 = i;
            break;
         }

         p1 = p2;
      }

      if (idx2 == -1 || (idx1 == 0 && idx2 == n) || (idx2 - idx1 <= 1))
      {
         return false;
      }

      return tryLineifyBulge(vec, idx1, idx2);
   }

   private void tryLineifyRegion(ShapeComponentVector vec)
    throws InterruptedException
   {
      int n = vec.size();

      if (n <= 4)
      {// triangle (4), line (3 or 2), single point (1) or empty
         addShape(vec, "vectorize.message.insufficient_to_vectorize", n);
         return;
      }
      else if (vec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         addShape(vec, "vectorize.message.not_closed");
         return;
      }

      ShapeComponent comp0 = vec.firstElement();
      ShapeComponent comp2 = vec.get(n-2);

      Point2D p0 = comp0.getEnd();
      Point2D p1 = comp2.getEnd();

      if (n == 5)
      {// quadrilateral

         ShapeComponent c1 = vec.get(1);
         ShapeComponent c2 = vec.get(2);
         ShapeComponent c3 = comp2;

         double averageDist1 = 0.25*(c1.getDiagonalLength()
                           + c3.getDiagonalLength());

         double averageDist2 = 0.25*(c2.getDiagonalLength()
                           + JDRLine.getLength(p0, p1));

         if ((averageDist1 > deltaThreshold && averageDist2 > deltaThreshold)
          || averageDist1 == averageDist2)
         {
            addShape(vec, "vectorize.failed_no_intersect_check",
              Math.min(averageDist1, averageDist2));
            return;
         }

         ShapeComponentVector newPath = new ShapeComponentVector(2);
         double delta;

         if (averageDist1 < averageDist2)
         {
            newPath.moveTo(c1.getMid());
            newPath.lineTo(c3.getMid());
            delta = averageDist1;
         }
         else
         {
            newPath.moveTo(c2.getMid());
            newPath.lineTo(JDRLine.getMidPoint(p1, p0));
            delta = averageDist2;
         }

         addShape(newPath, "vectorize.success_no_variance", delta);
         return;
      }

      double twiceDelta = 2.0*deltaThreshold;

      Path2D path = vec.getPath();

      Vector<Spike> indexes = null;

      for (int i = 1; i < n; i++)
      {
         ShapeComponent comp = vec.get(i);

         Point2D currentEndPt, currentStartPt, currentMidPt;

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            if (JDRLine.getLength(p1, p0) > twiceDelta)
            {
               continue;
            }

            currentStartPt = p1;
            currentEndPt = p0;
            currentMidPt = JDRLine.getMidPoint(p0, p1);
         }
         else
         {
            if (comp.getDiagonalLength() > twiceDelta)
            {
               continue;
            }

            currentStartPt = comp.getStart();
            currentEndPt = comp.getEnd();
            currentMidPt = comp.getMid();
         }

         ShapeComponent prevComp;

         if (i == 1)
         {
            prevComp = vec.lastElement();
         }
         else
         {
            prevComp = vec.get(i-1);
         }

         Point2D prevDp;

         if (prevComp.getType() == PathIterator.SEG_CLOSE
             || prevComp.getStart() == null)
         {
            prevDp = JDRLine.getGradient(p1, p0);
         }
         else
         {
            prevDp = prevComp.getEndGradient();
         }

         ShapeComponent nextComp;

         if (i == n-1)
         {
            nextComp = vec.get(1);
         }
         else if (i == n-2)
         {
            nextComp = vec.lastElement();
         }
         else
         {
            nextComp = vec.get(i+1);
         }

         Point2D nextDp;

         if (nextComp.getType() == PathIterator.SEG_CLOSE
             || nextComp.getStart() == null)
         {
            nextDp = JDRLine.getGradient(p1, p0);
         }
         else
         {
            nextDp = nextComp.getStartGradient();
         }

         if (Math.abs(JDRLine.getVectorAngle(prevDp, nextDp)-Math.PI)
             > ShapeComponentVector.EPSILON)
         {
            continue;
         }

         double gradLength = Math.sqrt(nextDp.getX()*nextDp.getX()
                                 + nextDp.getY()*nextDp.getY());

         double dx = nextDp.getX()/gradLength;
         double dy = nextDp.getY()/gradLength;

         if (Double.isNaN(dx) || Double.isNaN(dy))
         {
            continue;
         }

         double px = currentMidPt.getX()+dx;
         double py = currentMidPt.getY()+dy;

         if (!path.contains(px, py))
         {
            continue;
         }

         if (indexes == null)
         {
            indexes = new Vector<Spike>();
         }

         indexes.add(new Spike(i, vec, p0, p1, dialog));
      }

      if (indexes == null)
      {
         addShape(vec, "vectorize.no_spikes");
         return;
      }

      int numIndexes = indexes.size();
      Number idxVal1, idxVal2;

      trimSpikeIndexes(vec, indexes, p0, p1);

      numIndexes = indexes.size();

      dialog.addMessageIdLn("vectorize.selected_n_spikes",
       dialog.getResources().formatMessageChoice(numIndexes, 
       "vectorize.n_spikes"));

      for (Spike spike : indexes)
      {
         dialog.addMessageLn(spike.info(dialog.getResources()));
      }

      if (numIndexes == 1)
      {
         stickBulge(vec, indexes.get(0).getIndex(), p0, p1, twiceDelta);

         return;
      }

      idxVal1 = indexes.get(0).getIndex();
      idxVal2 = indexes.get(1).getIndex();

      Vector<Point2D> pts1 = new Vector<Point2D>();
      Vector<Point2D> pts2 = new Vector<Point2D>();

      int idx1 = idxVal1.intValue();
      int idx2 = idxVal2.intValue();

      for (int i = idx1; i < idx2; i++)
      {
         ShapeComponent comp = vec.get(i);

         if (comp.getType() == PathIterator.SEG_CLOSE
           || comp.getStart() == null)
         {
            pts1.add(p0);
         }
         else
         {
            pts1.add(comp.getEnd());
         }
      }

      if (idxVal2 instanceof Double)
      {
         ShapeComponent comp = vec.get(idx2);

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            pts1.add(p0);
         }
         else
         {
            pts1.add(comp.getEnd());
         }
      }

      if (idxVal1 instanceof Double)
      {
         ShapeComponent comp = vec.get(idx1);

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            pts2.add(p0);
         }
         else
         {
            pts2.add(comp.getEnd());
         }
      }

      for (int i = idx1; i > 0; i--)
      {
         ShapeComponent comp = vec.get(i);

         if (comp.getType() == PathIterator.SEG_CLOSE
           || comp.getStart() == null)
         {
            pts2.add(p1);
         }
         else
         {
            pts2.add(comp.getStart());
         }
      }

      for (int i = n-1; i > idx2; i--)
      {
         ShapeComponent comp = vec.get(i);

         if (comp.getType() == PathIterator.SEG_CLOSE
           || comp.getStart() == null)
         {
            pts2.add(p1);
         }
         else
         {
            pts2.add(comp.getStart());
         }
      }

      if (!tryLineify(pts1, pts2))
      {
         addShape(vec);
      }
   }

   public Spike chooseNearSpike(Spike spike1, Spike spike2, 
     ShapeComponentVector vec, Point2D p0, Point2D p1)
   {
      int n = vec.size();

      int i1 = spike1.intValue();
      int i2 = spike2.intValue();

      double l1, l2;

      if (Math.abs(i1-i2) == 1 || (i1 == n-1 && i2 == 1))
      {
         // choose shortest segment

         l1 = spike1.getLength();
         l2 = spike2.getLength();

         if (Math.abs(l2-l1) < ShapeComponentVector.EPSILON)
         {
            // find lengths of neighbouring components

            int before = (i1 < i2 ? i1 : i2) - 1;

            if (before < 1)
            {
               before = n - 1 + before;
            }

            int after = (i1 < i2 ? i2 : i1) + 1;

            if (after >= n)
            {
               after = after - n + 1;
            }

            if (before == n-1)
            {
               l1 = JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l1 = vec.get(before).getSquareDiagonalLength();
            }

            if (after == n-1)
            {
               l2 = JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l2 = vec.get(after).getSquareDiagonalLength();
            }

            if (l2 > l1 + ShapeComponentVector.EPSILON)
            {
               return spike2;
            }
            else if (l1 > l2 + ShapeComponentVector.EPSILON)
            {
               return spike1;
            }
            else
            {// neighbours approximately the same length
               spike1.setIndex(i1+0.5, vec, p0, p1, dialog);
            }

            return spike1;
         }
         else if (l1 < l2)
         {
            return spike1;
         }
         else
         {// l2 < l1
            return spike2;
         }
      }

      if (i1 < i2)
      {
         int before1 = i1-1;

         if (before1 < 1)
         {
            before1 = n - 1 + before1;
         }

         int after2 = i2+1;

         if (after2 >= n)
         {
            after2 = after2 - n + 1;
         }

         if (before1 == n-1)
         {
            l1 = JDRLine.getSquareLength(p1, p0);
         }
         else
         {
            l1 = vec.get(before1).getSquareDiagonalLength();
         }

         if (after2 == n-1)
         {
            l2 = JDRLine.getSquareLength(p1, p0);
         }
         else
         {
            l2 = vec.get(after2).getSquareDiagonalLength();
         }

         if (Math.abs(l2-l1) < ShapeComponentVector.EPSILON)
         {
            before1 = i1-2;

            if (before1 < 1)
            {
               before1 = n - 1 + before1;
            }

            after2 = i2+2;

            if (after2 >= n)
            {
               after2 = after2 - n + 1;
            }

            if (before1 == n-1)
            {
               l1 += JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l1 += vec.get(before1).getSquareDiagonalLength();
            }

            if (after2 == n-1)
            {
               l2 += JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l2 += vec.get(after2).getSquareDiagonalLength();
            }

            return l1 > l2 ? spike1 : spike2;
         }
         else if (l1 > l2)
         {
            return spike1;
         }
         else
         {// l2 > l1
            return spike2;
         }
      }
      else
      {// i2 < i1

         int after1 = i1+1;

         if (after1 >= n)
         {
            after1 = after1 - n + 1;
         }

         int before2 = i2-1;

         if (before2 < 1)
         {
            before2 = n - 1 + before2;
         }

         if (after1 == n-1)
         {
            l1 = JDRLine.getSquareLength(p1, p0);
         }
         else
         {
            l1 = vec.get(after1).getSquareDiagonalLength();
         }

         if (before2 == n-1)
         {
            l2 = JDRLine.getSquareLength(p1, p0);
         }
         else
         {
            l2 = vec.get(before2).getSquareDiagonalLength();
         }

         if (Math.abs(l2-l1) < ShapeComponentVector.EPSILON)
         {
            after1 = i1+2;

            if (after1 >= n)
            {
               after1 = after1 - n + 1;
            }

            before2 = i2-2;

            if (before2 < 1)
            {
               before2 = n - 1 + before2;
            }

            if (after1 == n-1)
            {
               l1 += JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l1 += vec.get(after1).getSquareDiagonalLength();
            }

            if (before2 == n-1)
            {
               l2 += JDRLine.getSquareLength(p1, p0);
            }
            else
            {
               l2 += vec.get(before2).getSquareDiagonalLength();
            }

            return l1 > l2 ? spike1 : spike2;
         }
         else if (l1 > l2)
         {
            return spike1;
         }
         else
         {// l2 > l1
            return spike2;
         }
      }
   }

   // should be at least 6 segments by the time this method is
   // called
   private void trimSpikeIndexes(ShapeComponentVector vec, 
     Vector<Spike> indexes, Point2D p0, Point2D p1)
   {
      int numIndexes = indexes.size();

      dialog.addMessageIdLn("vectorize.n_spikes_found",
       dialog.getResources().formatMessageChoice(numIndexes, 
       "vectorize.n_spikes"));

      for (Spike spike : indexes)
      {
         dialog.addMessageLn(spike.info(dialog.getResources()));
      }

      if (numIndexes == 1)
      {
         return;
      }

      int n = vec.size();

      Spike spike1 = indexes.lastElement();
      Spike spike2 = indexes.firstElement();

      Point2D r1, r2;

      if (spike2.intValue() == 1 && spike1.intValue() == n-1)
      {
         Spike keep = chooseNearSpike(spike1, spike2, vec, p0, p1);

         if (keep.intValue() == 1)
         {
            indexes.set(0, keep);
            indexes.remove(numIndexes-1);
         }
         else
         {
            indexes.set(numIndexes-1, keep);
            indexes.remove(0);
         }

         numIndexes--;
      }

      for (int i = numIndexes-1; i > 0; i--)
      {
         spike1 = indexes.get(i-1);
         spike2 = indexes.get(i);

         boolean tooclose = false;

         if (spike2.intValue()-spike1.intValue() < 2)
         {
            tooclose = true;
         }
         else
         {
            int j1 = spike1.intValue()+1;
            int j2 = spike2.intValue()-1;

            if (j1 == n)
            {
               j1 = 1;
            }

            if (j2 == 0)
            {
               j2 = n-1;
            }

            double l = vec.getEstimatedLength(j1, j2);

            if (l < returnPtDist)
            {
               tooclose = true;
            }
         }

         if (tooclose)
         {
            Spike keep = chooseNearSpike(spike1, spike2, vec, p0, p1);

            indexes.remove(i);
            indexes.set(i-1, keep);

            numIndexes--;
         }
      }

      if (numIndexes < 3)
      {
         return;
      }

      double totalLength = vec.getEstimatedLength(1, n-2)
                         + JDRLine.getLength(p1, p0);

      Spike2Spike best = null;

      for (int i = 0; i < numIndexes; i++)
      {
         spike1 = indexes.get(i);
         Number num1 = spike1.getIndex();
         int nextIdx = 
           (num1 instanceof Integer ? num1.intValue()+1 : num1.intValue()+2);

         if (nextIdx >= n)
         {
            nextIdx = nextIdx - n + 1;
         }

         double halfLength1 = 0.5*spike1.getLength();

         for (int j = i+1; j < numIndexes; j++)
         {
            spike2 = indexes.get(j);

            Number num2 = spike2.getIndex();
            int prevIdx = num2.intValue()-1;

            if (prevIdx < 1)
            {
               prevIdx = n - 1 + prevIdx;
            }

            double l = vec.getEstimatedLength(nextIdx, prevIdx)
                     + halfLength1 + 0.5*spike2.getLength();

            double dx = Math.abs(spike1.getMid().getX() - spike2.getMid().getX());
            double dy = Math.abs(spike1.getMid().getY() - spike2.getMid().getY());

            Spike2Spike s2s = new Spike2Spike(spike1, spike2,
             Math.abs(0.5-(l/totalLength)), Math.min(dx, dy));

            if (best == null || s2s.compareTo(best) < 0)
            {
               best = s2s;
            }
         }
      }

      spike1 = best.getSpike1();
      spike2 = best.getSpike2();

      indexes.clear();
      indexes.add(spike1);
      indexes.add(spike2);

      dialog.addMessageIdLn("vectorize.best_spike_pair",
       spike1.getIndex(), spike1.getMid().getX(), spike1.getMid().getY(),
       spike2.getIndex(), spike2.getMid().getX(), spike2.getMid().getY(),
       best.getFromMidway(), best.getInclinationDifference(),
       best.getAverageLength(), best.getAverageAngleDifference());
   }

   private Number[] selectTwoOfThreeSpikes(ShapeComponentVector vec,
    Point2D p0, Point2D p1,
    Number idxVal1, Number idxVal2, Number idxVal3)
   {
      Point2D m1, m2, m3;

      ShapeComponent c1 = vec.get(idxVal1.intValue());

      if (idxVal1 instanceof Integer)
      {
         if (c1.getType() == PathIterator.SEG_CLOSE)
         {
            m1 = JDRLine.getMidPoint(p1, p0);
         }
         else
         {
            m1 = c1.getMid();
         }
      }
      else
      {
         if (c1.getType() == PathIterator.SEG_CLOSE)
         {
            m1 = p0;
         }
         else
         {
            m1 = c1.getEnd();
         }
      }

      ShapeComponent c2 = vec.get(idxVal2.intValue());

      if (idxVal2 instanceof Integer)
      {
         if (c2.getType() == PathIterator.SEG_CLOSE)
         {
            m2 = JDRLine.getMidPoint(p1, p0);
         }
         else
         {
            m2 = c2.getMid();
         }
      }
      else
      {
         if (c2.getType() == PathIterator.SEG_CLOSE)
         {
            m2 = p0;
         }
         else
         {
            m2 = c2.getEnd();
         }
      }

      ShapeComponent c3 = vec.get(idxVal3.intValue());

      if (idxVal3 instanceof Integer)
      {
         if (c3.getType() == PathIterator.SEG_CLOSE)
         {
            m3 = JDRLine.getMidPoint(p1, p0);
         }
         else
         {
            m3 = c3.getMid();
         }
      }
      else
      {
         if (c3.getType() == PathIterator.SEG_CLOSE)
         {
            m3 = p0;
         }
         else
         {
            m3 = c3.getEnd();
         }
      }

      double d12 = Math.min(Math.abs(m2.getX() - m1.getX()), 
                            Math.abs(m2.getY() - m1.getY()));
      double d13 = Math.min(Math.abs(m3.getX() - m1.getX()), 
                            Math.abs(m3.getY() - m1.getY()));
      double d23 = Math.min(Math.abs(m3.getX() - m2.getX()), 
                            Math.abs(m3.getY() - m2.getY()));

      double l12 = JDRLine.getLength(m1, m2);
      double l13 = JDRLine.getLength(m1, m3);
      double l23 = JDRLine.getLength(m2, m3);

      if (d12 <= d13 && d12 <= d23 && l12 > returnPtDist)
      {
         return new Number[] {idxVal1, idxVal2};
      }

      if (d13 <= d12 && d13 <= d23 && l13 > returnPtDist)
      {
         return new Number[] {idxVal1, idxVal3};
      }

      if (d23 <= d12 && d23 <= d13 && l23 > returnPtDist)
      {
         return new Number[] {idxVal2, idxVal3};
      }

      if (l12 > returnPtDist && l12 >= l13 && l12 >= l23)
      {
         return new Number[] {idxVal1, idxVal2};
      }

      if (l13 > returnPtDist && l13 >= l12 && l13 >= l23)
      {
         return new Number[] {idxVal1, idxVal3};
      }

      if (l23 > returnPtDist)
      {
         return new Number[] {idxVal2, idxVal3};
      }

      return null;
   }

   private void stickBulge(ShapeComponentVector vec, Number idxNum, 
     Point2D p0, Point2D p1, double twiceDelta)
   {
      int n = vec.size();

      // try to find where path starts to bulge

      ShapeComponentVector newPath = new ShapeComponentVector();
      newPath.setFilled(false);

      double averageDist = 0.0;
      int idx1 = idxNum.intValue();
      int idx2 = idx1;

      ShapeComponent comp = vec.get(idx1);

      if (idxNum instanceof Integer)
      {
         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            newPath.moveTo(JDRLine.getMidPoint(p1, p0));
         }
         else
         {
            newPath.moveTo(comp.getMid());
         }

         averageDist = 0.5*comp.getDiagonalLength();
      }
      else
      {
         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            newPath.moveTo(p0);
         }
         else
         {
            newPath.moveTo(comp.getEnd());
         }

         idx2++;
      }

      int i1 = idx1+1;
      int i2 = idx2-1;

      boolean bulgeFound = false;

      for (int i = 1; i < n; i++)
      {
         i1 = idx1+i;
         i2 = idx2-i;

         if (i1 >= n)
         {
            i1 = (i1%n)+1;
         }

         if (i2 <= 0)
         {
            i2 = i2 + n - 1;
         }

         ShapeComponent c1 = vec.get(i1);
         ShapeComponent c2 = vec.get(i2);

         Point2D r1, r2;

         if (c1.getType() == PathIterator.SEG_CLOSE)
         {
            r1 = p0;
         }
         else
         {
            r1 = c1.getEnd();
         }

         if (c2.getType() == PathIterator.SEG_CLOSE)
         {
            r2 = p1;
         }
         else
         {
            r2 = c2.getStart();
         }

         double l = JDRLine.getLength(r1, r2);

         if (l <= twiceDelta)
         {
            averageDist += 0.5*l;

            newPath.lineTo(JDRLine.getMidPoint(r1, r2));
         }
         else
         {
            bulgeFound = true;

            if (doLineIntersectionCheck)
            {
               break;
            }
         }
      }

      double length = newPath.getEstimatedLength();

      if (length < minStubLength)
      {
         dialog.addMessageIdLn("vectorize.discarding_small_stub",
          length, newPath.svg());
         addShape(vec, "vectorize.too_wide");
         return;
      }

      if (newPath.size() > 1)
      {
         averageDist /= newPath.size();

         setLineWidth(newPath, averageDist);

         if (!doLineIntersectionCheck || !bulgeFound)
         {
            if (averageDist < deltaThreshold)
            {
               addShape(newPath, "vectorize.success_with_line_length",
                averageDist, length);
            }
            else
            {
               addShape(vec, "vectorize.failed_no_intersect_check",
                 averageDist);
            }

            return;
         }

         addShape(newPath, "vectorize.success_with_line_length",
             averageDist, length);

         Point2D r0;

         newPath = new ShapeComponentVector();

         comp = vec.get(i1);

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            r0 = p1;
         }
         else
         {
            r0 = comp.getStart();
         }

         newPath.moveTo(r0);

         int m = (i2 < i1 ? n-1 : i2);

         Point2D r1 = r0;

         for (int i = i1; i <= m; i++)
         {
            comp = vec.get(i);

            if (comp.getType() == PathIterator.SEG_CLOSE)
            {
               r1 = p0;
            }
            else
            {
               r1 = comp.getEnd();
            }

            newPath.lineTo(r1);
         }

         if (i2 < i1)
         {
            for (int i = 1; i <= i2; i++)
            {
               comp = vec.get(i);

               if (comp.getType() == PathIterator.SEG_CLOSE)
               {
                  r1 = p0;
               }
               else
               {
                  r1 = comp.getEnd();
               }

               newPath.lineTo(r1);
            }
         }

         newPath.closePath();

         m = newPath.size()-2;
         Point2D dp1 = newPath.firstElement().getStartGradient();
         Point2D dp2 = newPath.get(m).getStartGradient();
         Point2D dp3 = JDRLine.getGradient(r1, r0);

         if (JDRLine.getVectorAngle(dp2, dp3) < ShapeComponentVector.EPSILON)
         {
            newPath.removeComponent(m);
         }

         if (JDRLine.getVectorAngle(dp3, dp2) < ShapeComponentVector.EPSILON)
         {
            newPath.removeComponent(0);
         }

         addShape(newPath, "vectorize.too_wide");
      }
      else
      {
         addShape(vec, "vectorize.too_wide");
      }
   }

   private void tryLineifyRegion2(ShapeComponentVector vec)
    throws InterruptedException
   {
      int n = vec.size();

      if (n < 3)
      {
         addShape(vec, "vectorize.message.insufficient_to_vectorize", n);
         return;
      }
      else if (vec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         addShape(vec, "vectorize.message.not_closed");
         return;
      }

      n--;

      PathCoord[] pts = new PathCoord[n];

      for (int i = 0; i < n; i++)
      {
         ShapeComponent comp = vec.get(i);

         Point2D grad1 = new Point2D.Double();
         Point2D grad2 = new Point2D.Double();
         double bend = getBendAngle(vec, i, grad1, grad2);
         pts[i] = new PathCoord(comp.getType(), comp.getEnd(), bend, grad1, grad2);
      }

      int n2 = n/2;
      int n1 = n-n2;

      LineifyResults results = lineifyBestFit(pts, n1, n2);

      if (results.minAverageDelta <= deltaThreshold)
      {
         if (!doLineIntersectionCheck)
         {
            addShape(results.bestPath, "vectorize.success_no_variance",
             results.minAverageDelta);
            return;
         }
         else
         {
            if (results.variance <= varianceThreshold)
            {
               addShape(results.bestPath, "vectorize.success_intersect_check",
                 results.minAverageDelta, results.variance);
               return;
            }
         }
      }

      // Find spikes
      Integer[] indexes = new Integer[pts.length];
      Vector<Vector<Integer>> spikes = getRegionSpikes(pts, vec.getPath(),
        indexes);

      if (spikes.isEmpty())
      {
         if (!findBulge(vec))
         {
            addShape(vec, "vectorize.no_spikes");
         }

         return;
      }

      int remainingN = 0;

      for (int i = 0; i < indexes.length; i++)
      {
         if (indexes[i] != null)
         {
            remainingN++;
         }
      }

      if (remainingN < 4)
      {
         addShape(vec, "vectorize.too_many_spikes");
         return;
      }

      ShapeComponentVector reducedShape = new ShapeComponentVector(remainingN);

      Point2D prevPt = null;

      for (int i = 0; i < pts.length; i++)
      {
         if (indexes[i] != null)
         {
            Point2D p = pts[i].getPoint();
            double[] coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               reducedShape.add(new ShapeComponent(PathIterator.SEG_MOVETO, 
                 coords, prevPt));
            }
            else
            {
               reducedShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                 coords, prevPt));
            }

            prevPt = p;
         }
      }

      PathCoord[] q = new PathCoord[remainingN];

      for (int i = 0; i < remainingN; i++)
      {
         ShapeComponent comp = reducedShape.get(i);

         Point2D grad1 = new Point2D.Double();
         Point2D grad2 = new Point2D.Double();
         double bend = getBendAngle(vec, i, grad1, grad2);
         q[i] = new PathCoord(comp.getType(), comp.getEnd(), bend, grad1, grad2);
      }

      int m2 = q.length/2;
      int m1 = q.length-m2;

      LineifyResults reducedPathResults = lineifyBestFit(q, m1, m2);

      if (reducedPathResults.minAverageDelta <= deltaThreshold)
      {
         addShape(reducedPathResults.bestPath, "vectorize.reduced_path_success",
           reducedPathResults.minAverageDelta);
      }
      else
      {
         if (!findBulge(vec))
         {
            addShape(vec, "vectorize.reduced_path_failed", 
               reducedPathResults.minAverageDelta);
         }

         return;
      }

      for (Vector<Integer> spike : spikes)
      {
         ShapeComponentVector newShape = new ShapeComponentVector(spike.size()+1);
         prevPt = null;
         double[] coords = null;

         for (Integer num : spike)
         {
            Point2D p = pts[num].getPoint();
            coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               newShape.add(new ShapeComponent(PathIterator.SEG_MOVETO,
                coords, prevPt));
            }
            else
            {
               newShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                coords, prevPt));
            }

            prevPt = p;
         }

         newShape.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));

         dialog.addMessageIdLn("vectorize.trying_spike", spike);
         tryLineifyRegion(newShape);
      }

   }

   private void tryLineifyLoop(SubPath sp1, SubPath sp2)
    throws InterruptedException
   {
      // last component will always be a close path segment
      int n1 = sp1.size()-1;
      int n2 = sp2.size()-1;

      Point2D[] pts1 = new Point2D.Double[n1];
      Point2D[] pts2 = new Point2D.Double[n2];

      for (int i = 0; i < n1; i++)
      {
         ShapeComponent comp = sp1.getComponent(i);
         pts1[i] = comp.getEnd();
      }

      for (int i = n2-1; i >= 0; i--)
      {
         ShapeComponent comp = sp2.getComponent(i);
         pts2[(n2-i-1)%n2] = comp.getEnd();
      }

      int maxN = (int)Math.max(n1, n2);
      ShapeComponentVector path = new ShapeComponentVector(maxN);
      path.setFilled(false);

      LineFit[] linefit = fitLoop(pts1, pts2, 0, path);

      double averageDelta = calculateMean(linefit);

      if (averageDelta <= deltaThreshold)
      {
         if (!doLineIntersectionCheck)
         {
            setLineWidth(path, averageDelta);
            addShape(path, "vectorize.success_no_variance",
             averageDelta);
            return;
         }
         else
         {
            double variance = calculateVariance(linefit, averageDelta);

            if (variance <= varianceThreshold)
            {
               setLineWidth(path, averageDelta);
               addShape(path, "vectorize.success_intersect_check",
                 averageDelta, variance);
               return;
            }
         }
      }

      int maxNonSpikes = 0;
      LineFit[] maxNonSpikesLineFit = linefit;
      double maxNonSpikesDelta = averageDelta;
      int maxNonSpikesOffset = 0;
      ShapeComponentVector maxNonSpikesPath = path;

      if (doLineIntersectionCheck)
      {
         maxNonSpikes = linefit.length - countSpikes(linefit);
      }

      double bestAverageDelta = averageDelta;
      LineFit[] bestLineFit = linefit;
      int bestOffset = 0;
      ShapeComponentVector bestPath = path;

      for (int offset = 1; offset < n2-1; offset++)
      {
         path = new ShapeComponentVector(maxN);
         path.setFilled(false);
         linefit = fitLoop(pts1, pts2, offset, path);

         averageDelta = calculateMean(linefit);

         if (averageDelta < bestAverageDelta)
         {
            bestAverageDelta = averageDelta;
            bestLineFit = linefit;
            bestOffset = offset;
            bestPath = path;
         }

         if (doLineIntersectionCheck)
         {
            int nonSpikes = linefit.length - countSpikes(linefit);

            if (nonSpikes > maxNonSpikes
                 || (nonSpikes == maxNonSpikes && averageDelta < maxNonSpikesDelta))
            {
               maxNonSpikes = nonSpikes;
               maxNonSpikesLineFit = linefit;
               maxNonSpikesDelta = averageDelta;
               maxNonSpikesOffset = offset;
               maxNonSpikesPath = path;
            }
         }
      }

      if (!doLineIntersectionCheck)
      {
         if (bestAverageDelta <= deltaThreshold)
         {
            setLineWidth(path, bestAverageDelta);
            addShape(path, "vectorize.success_no_variance",
             bestAverageDelta);
         }
         else
         {
            addShape(sp1.getCompleteVector(),
             "vectorize.failed_no_intersect_check",
             bestAverageDelta);
         }

         return;
      }

      double variance = calculateVariance(bestLineFit, bestAverageDelta);

      if (bestAverageDelta <= deltaThreshold && variance <= varianceThreshold)
      {
         setLineWidth(path, bestAverageDelta);
         addShape(path, "vectorize.success_intersect_check",
            bestAverageDelta, variance);
         return;
      }

      // Find spikes

      Vector<Vector<Integer>> spikes = getSpikes(bestLineFit);

      if (spikes.isEmpty())
      {
         if (maxNonSpikes > 1 && bestPath != maxNonSpikesPath)
         {
            spikes = getSpikes(maxNonSpikesLineFit);

            if (spikes.isEmpty())
            {
               addShape(sp1.getCompleteVector(),
                 "vectorize.no_spikes", getNumShapes());
               return;
            }

            bestLineFit = maxNonSpikesLineFit;
            bestAverageDelta = maxNonSpikesDelta;
            bestOffset = maxNonSpikesOffset;
            bestPath = maxNonSpikesPath;
         }
         else
         {
            addShape(sp1.getCompleteVector(), "vectorize.no_spikes");
            return;
         }
      }

      // try fitting a line without the spikes.

      Integer[] indexes1 = new Integer[n1];
      Integer[] indexes2 = new Integer[n2];

      for (int i = 0; i < pts1.length; i++)
      {
         indexes1[i] = Integer.valueOf(i);
      }

      for (int i = 0; i < pts2.length; i++)
      {
         indexes2[i] = Integer.valueOf(i);
      }

      for (Vector<Integer> spike : spikes)
      {
         for (Integer num : spike)
         {
            indexes1[bestLineFit[num].idx1] = null;
            indexes2[bestLineFit[num].idx2] = null;
         }
      }

      int remainingN1 = 0;

      for (int i = 0; i < indexes1.length; i++)
      {
         if (indexes1[i] != null)
         {
            remainingN1++;
         }
      }
      
      int remainingN2 = 0;

      for (int i = 0; i < indexes2.length; i++)
      {
         if (indexes2[i] != null)
         {
            remainingN2++;
         }
      }

      if (remainingN1 < 2 || remainingN2 < 2)
      {
         if (bestPath != maxNonSpikesPath)
         {
            spikes = getSpikes(maxNonSpikesLineFit);

            if (spikes.isEmpty())
            {
               addShape(sp1.getCompleteVector(), "vectorize.no_spikes");
               return;
            }

            bestLineFit = maxNonSpikesLineFit;
            bestAverageDelta = maxNonSpikesDelta;
            bestOffset = maxNonSpikesOffset;
            bestPath = maxNonSpikesPath;

            for (int i = 0; i < pts1.length; i++)
            {
               indexes1[i] = Integer.valueOf(i);
            }

            for (int i = 0; i < pts2.length; i++)
            {
               indexes2[i] = Integer.valueOf(i);
            }

            for (Vector<Integer> spike : spikes)
            {
               for (Integer num : spike)
               {
                  indexes1[bestLineFit[num].idx1] = null;
                  indexes2[bestLineFit[num].idx2] = null;
               }
            }

            remainingN1 = 0;

            for (int i = 0; i < indexes1.length; i++)
            {
               if (indexes1[i] != null)
               {
                  remainingN1++;
               }
            }
      
            remainingN2 = 0;

            for (int i = 0; i < indexes2.length; i++)
            {
               if (indexes2[i] != null)
               {
                  remainingN2++;
               }
            }

            if (remainingN1 < 2 || remainingN2 < 2)
            {
               addShape(sp1.getCompleteVector(),
                 "vectorize.too_many_spikes");
               return;
            }
         }
         else
         {
            addShape(sp1.getCompleteVector(),
               "vectorize.too_many_spikes");
            return;
         }
      }

      Point2D[] q1 = new Point2D.Double[remainingN1];
      Point2D[] q2 = new Point2D.Double[remainingN2];

      for (int i = 0, j = 0; i < pts1.length; i++)
      {
         if (indexes1[i] != null)
         {
            q1[j++] = pts1[i];
         }
      }

      for (int i = 0, j = 0; i < pts2.length; i++)
      {
         if (indexes2[i] != null)
         {
            q2[j++] = pts2[i];
         }
      }

      maxN = (int)Math.max(remainingN1, remainingN2);

      for (int offset = 0; offset < remainingN2-1; offset++)
      {
         path = new ShapeComponentVector(maxN);
         path.setFilled(false);
         linefit = fitLoop(q1, q2, offset, path);

         averageDelta = calculateMean(linefit);

         if (averageDelta < bestAverageDelta)
         {
            bestAverageDelta = averageDelta;
            bestPath = path;
         }
      }

      if (bestAverageDelta <= deltaThreshold)
      {
         setLineWidth(bestPath, bestAverageDelta);
         addShape(bestPath, "vectorize.reduced_path_success", 
            bestAverageDelta);
      }
      else
      {
         addShape(sp1.getCompleteVector(),
            "vectorize.reduced_path_failed", 
            bestAverageDelta);
         return;
      }

      for (Vector<Integer> spike : spikes)
      {
         int startIdx = spike.firstElement().intValue();
         int endIdx = spike.lastElement().intValue();

         int sIdx1 = (pts1.length+bestLineFit[startIdx].idx1-1)%pts1.length;
         int sIdx2 = (pts2.length+bestLineFit[startIdx].idx2-1)%pts2.length;
         int eIdx1 = (bestLineFit[endIdx].idx1+1)%pts1.length;
         int eIdx2 = (bestLineFit[endIdx].idx2+1)%pts2.length;

         int m1, m2;

         if (sIdx1 < eIdx1)
         {
            m1 = eIdx1 - sIdx1 + 1;
         }
         else
         {
            m1 = pts1.length - sIdx1 + eIdx1 + 1;
         }

         if (sIdx2 < eIdx2)
         {
            m2 = eIdx2 - sIdx2 + 1;
         }
         else
         {
            m2 = pts2.length - sIdx2 + eIdx2 + 1;
         }

         path = new ShapeComponentVector(m1+m2);

         Point2D prevPt = null;
         double[] coords = null;

         for (int i = 0; i < m1; i++)
         {
            int j = (sIdx1+i)%pts1.length;
            Point2D p = pts1[j];
            coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               path.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
            }
            else
            {
               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
            }

            prevPt = p;
         }

         Point2D p1 = pts2[sIdx2];
         Point2D p2 = pts2[(sIdx2+m2-1)%pts2.length];

         double dx = prevPt.getX()-p1.getX();
         double dy = prevPt.getY()-p1.getY();
         double dist1 = dx*dx + dy*dy;

         dx = prevPt.getX()-p2.getX();
         dy = prevPt.getY()-p2.getY();
         double dist2 = dx*dx + dy*dy;

         if (dist1 < dist2)
         {
            for (int i = 0; i < m2; i++)
            {
               int j = (sIdx2+i)%pts2.length;
               Point2D p = pts2[j];

               coords = new double[6];
               coords[0] = p.getX();
               coords[1] = p.getY();

               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
               prevPt = p;
            }
         }
         else
         {
            for (int i = m2-1; i >= 0; i--)
            {
               int j = (sIdx2+i)%pts2.length;
               Point2D p = pts2[j];

               coords = new double[6];
               coords[0] = p.getX();
               coords[1] = p.getY();

               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
               prevPt = p;
            }
         }

         path.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));

         dialog.addMessageIdLn("vectorize.trying_spike", spike);
         tryLineifyRegion(path);
      }
   }

   private double getAngle(Point2D v1, Point2D v2)
   {
      return getAngle(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   private double getAngle(double v1x, double v1y, double v2x, double v2y)
   {
      double len1 = Math.sqrt(v1x*v1x + v1y*v1y);
      double len2 = Math.sqrt(v2x*v2x + v2y*v2y);

      double dotproduct = v1x*v2x + v1y*v2y;

      double factor = 1.0/(len1*len2);

      if (Double.isNaN(factor))
      {
         return 0.0;
      }

      return Math.acos(dotproduct*factor);
   }

   public static double getSquareDistance(double p0x, double p0y, double p1x, double p1y)
   {
      return ShapeComponent.getSquareDistance(p0x, p0y, p1x, p1y);
   }

   public static double getSquareDistance(Point2D p0, Point2D p1)
   {
      return getSquareDistance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
   }

   public static double getDistance(double p0x, double p0y, double p1x, double p1y)
   {
      return Math.sqrt(getSquareDistance(p0x, p0y, p1x, p1y));
   }

   public static double getDistance(Point2D p0, Point2D p1)
   {
      return getDistance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
   }

   public static double getDistance(PathCoord p0, PathCoord p1)
   {
      return getDistance(p0.getPoint(), p1.getPoint());
   }

   private Vector<Vector<Integer>> getRegionSpikes(PathCoord[] pts,
     Shape shape, Integer[] indexes)
   {
      Vector<Vector<Integer>> spikes = new Vector<Vector<Integer>>();

      double extensionDist = 1.2*returnPtDist;

      int n = pts.length-1;
      indexes[0] = Integer.valueOf(0);
      indexes[n] = Integer.valueOf(n);

      double runningLength = 0.0;

      for (int i = 1; i < n; i++)
      {
         indexes[i] = Integer.valueOf(i);

         runningLength += getDistance(pts[i].getPoint(), pts[i-1].getPoint());

         if (runningLength < maxTinyStep)
         {
            continue;
         }

         Point2D grad1 = pts[i].getGradient1();
         Point2D grad2 = pts[i].getGradient2();
         double angle = Math.PI-getAngle(grad2, grad1);

         if ((angle >= SPIKE_ANGLE_LOWER1 && angle <= SPIKE_ANGLE_UPPER1)
          || (angle >= SPIKE_ANGLE_LOWER2 && angle <= SPIKE_ANGLE_UPPER2))
         {
            double nextDist = getDistance(pts[i].getPoint(), pts[i+1].getPoint());

            if (nextDist < maxTinyStep)
            {
               Point2D nextGrad1 = pts[i+1].getGradient1();
               Point2D nextGrad2 = pts[i+1].getGradient2();
               double nextAngle = Math.PI-getAngle(nextGrad2, nextGrad1);

               if ((nextAngle < SPIKE_ANGLE_UPPER1 && nextAngle < angle)
                || (nextAngle > SPIKE_ANGLE_LOWER2 && nextAngle > angle))
               {
                  // Next angle is sharper
                  continue;
               }
            }

            Point2D q1 = new Point2D.Double(
              pts[i].getX() + extensionDist * grad2.getX(),
              pts[i].getY() + extensionDist * grad2.getY()
              );

            Point2D q2 = new Point2D.Double(
              pts[i].getX() - extensionDist * grad1.getX(),
              pts[i].getY() - extensionDist * grad1.getY()
              );

            double q1Angle = Math.PI + Math.atan2(grad2.getY(), grad2.getX());
            double q2Angle = Math.PI + Math.atan2(grad1.getY(), grad1.getX());

            double qAngle = q2Angle-q1Angle;

            double theta = q1Angle + 0.1*qAngle;
            Point2D q3 = new Point2D.Double(
               pts[i].getX() + extensionDist*Math.cos(theta),
               pts[i].getY() - extensionDist*Math.sin(theta)
            );

            theta = q1Angle + 0.9*qAngle;
            Point2D q4 = new Point2D.Double(
               pts[i].getX() + extensionDist*Math.cos(theta),
               pts[i].getY() - extensionDist*Math.sin(theta)
            );

            theta = q1Angle + 0.5*qAngle;

            Point2D q5 = new Point2D.Double(
               pts[i].getX() + extensionDist*Math.cos(theta),
               pts[i].getY() - extensionDist*Math.sin(theta)
            );

            double newRunningLength = 0.0;

            if (shape.contains(q1) || shape.contains(q2) || shape.contains(q3)
               || shape.contains(q4) || shape.contains(q5))
            {
               double l = 0.0;
               Vector<Integer> currentSpike = new Vector<Integer>();
               currentSpike.add(Integer.valueOf(i));

               double prevDistX = Double.MAX_VALUE;
               double prevDistY = Double.MAX_VALUE;

               for (int j = i+1; j < pts.length; j++)
               {
                  indexes[j] = Integer.valueOf(j);

                  double currentLength = getDistance(pts[j-1], pts[j]);

                  l += currentLength;

                  currentSpike.add(Integer.valueOf(j));

                  if (l < returnPtDist) continue;

                  double distX = Math.abs(pts[i].getX() - pts[j].getX());
                  double distY = Math.abs(pts[i].getY() - pts[j].getY());

                  if (j == n && distX < returnPtDist && distY < returnPtDist)
                  {
                     for (int r = 1; r < currentSpike.size()-1; r++)
                     {
                        indexes[i+r] = null;
                     }

                     i = j;
                     spikes.add(currentSpike);
                     currentSpike = null;
                     break;
                  }
                  else if (distX + distY > prevDistX + prevDistY)
                  {
                     currentSpike.remove(currentSpike.size()-1);

                     for (int r = 1; r < currentSpike.size()-1; r++)
                     {
                        indexes[i+r] = null;
                     }

                     spikes.add(currentSpike);
                     currentSpike = null;

                     if (currentLength < maxTinyStep)
                     {
                        i = j;
                        newRunningLength = currentLength;
                     }
                     else
                     {
                        i = j-1;
                     }
                     break;
                  }
                  else if (distX <= returnPtDist && distY <= returnPtDist)
                  {
                     prevDistX = distX;
                     prevDistY = distY;
                  }
               }
            }

            runningLength = newRunningLength;
         }
      }

      // Do any spikes cut through?

      for (int i = spikes.size()-1; i > 0; i--)
      {
         Vector<Integer> spike = spikes.get(i);

         int spike1StartIdx = spike.firstElement().intValue();
         int spike1EndIdx = spike.lastElement().intValue();
         double minDist1 = Double.MAX_VALUE;
         double minDist2 = Double.MAX_VALUE;
         int foundIdx = -1;

         for (int j = i-1; j >= 0; j--)
         {
            Vector<Integer> spike2 = spikes.get(j);

            int spike2StartIdx = spike2.firstElement().intValue();
            int spike2EndIdx = spike2.lastElement().intValue();

            double dist1 = getDistance(pts[spike1EndIdx], pts[spike2StartIdx]);
            double dist2 = getDistance(pts[spike1StartIdx], pts[spike2EndIdx]);

            if (dist1 <= minDist1 && dist2 <= minDist2)
            {
               minDist1 = dist1;
               minDist2 = dist2;
               foundIdx = j;
            }
         }

         if (foundIdx != -1 && minDist1 <= returnPtDist
              && minDist2 <= returnPtDist)
         {
            Vector<Integer> spike2 = spikes.get(foundIdx);

            spike2.addAll(spike);

            spikes.remove(i);
         }
      }

      return spikes;
   }

   private Vector<Vector<Integer>> getSpikes(LineFit[] linefit)
   {
      Vector<Vector<Integer>> spikes = new Vector<Vector<Integer>>();
      Vector<Integer> currentSpike = null;

      for (int i = 0; i < linefit.length; i++)
      {
         if (linefit[i] == null)
         {
            continue;
         }

         if (linefit[i].delta > deltaThreshold)
         {
            if (currentSpike == null)
            {
               currentSpike = new Vector<Integer>();
            }

            currentSpike.add(Integer.valueOf(i));
         }
         else if (currentSpike != null)
         {
            spikes.add(currentSpike);
            currentSpike = null;
         }
      }

      if (currentSpike != null)
      {
         // does a spike wrap from end to start?

         if (!spikes.isEmpty())
         {
            Vector<Integer> spike = spikes.firstElement();

            if (spike.firstElement().intValue() == 0)
            {
               spikes.remove(0);

               currentSpike.addAll(spike);
            }
         }

         spikes.add(currentSpike);
      }

      for (int i = spikes.size()-1; i >= 0; i--)
      {
         if (spikes.get(i).size() < 2)
         {
            spikes.remove(i);
         }
      }

      return spikes;
   }

   private int countSpikes(LineFit[] linefit)
   {
      int sum = 0;

      for (int i = 0; i < linefit.length; i++)
      {
         if (linefit[i] != null && linefit[i].delta > deltaThreshold)
         {
            sum++;
         }
      }

      return sum;
   }

   private double calculateMean(LineFit[] linefit)
   {
      double sumDelta = 0.0;
      int n = 0;

      for (int i = 0; i < linefit.length; i++)
      {
         if (linefit[i] != null)
         {
            sumDelta += linefit[i].delta;
            n++;
         }
      }

      return sumDelta / n;
   }

   private double calculateVariance(LineFit[] linefit, double averageDelta)
   {
      double sum = 0.0;
      int n = 0;

      for (int i = 0; i < linefit.length; i++)
      {
         if (linefit[i] != null)
         {
            double diff = linefit[i].delta - averageDelta;
            sum += diff*diff;
            n++;
         }
      }

      return sum / n;
   }

   private LineFit[] fitLoop(Point2D[] pts1, Point2D[] pts2,
      int offset, ShapeComponentVector path)
   {
      int n1 = pts1.length;
      int n2 = pts2.length;

      double[] coords = null;
      int maxN = (int)Math.max(n1, n2);
      int minN = (int)Math.min(n1, n2);

      Point2D prevPt = null;
      LineFit[] linefit = new LineFit[maxN];

      int div = maxN/minN;

      for (int i = 0, k = 0; i < minN; i++)
      {
         for (int j = 0; j < div; j++, k++)
         {
            if (k >= maxN) break;

            int idx1, idx2;

            if (n1 == n2)
            {
               idx1 = i;
               idx2 = (i+offset)%n2;
            }
            else if (n1 < n2)
            {
               idx1 = i;
               idx2 = (k+offset)%n2;
            }
            else
            {
               idx1 = k;
               idx2 = (i+offset)%n2;
            }

            Point2D p1 = pts1[idx1];
            Point2D p2 = pts2[idx2];

            double dx = 0.5*(p2.getX()-p1.getX());
            double dy = 0.5*(p2.getY()-p1.getY());

            Point2D p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

            coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               path.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
            }
            else
            {
               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
            }

            double delta = Math.sqrt(dx*dx + dy*dy);

            linefit[k] = new LineFit(delta, idx1, idx2, p1, p2);

            prevPt = p;
         }
      }

      path.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));
      return linefit;
   }

   private ShapeComponentVector tryLineifyArea(SubPath subPath)
    throws InterruptedException
   {
      int startIdx = subPath.getStartIndex();
      int endIdx = subPath.getEndIndex();

      ShapeComponentVector fullVec = subPath.getCompleteVector();
      ShapeComponentVector vec = new ShapeComponentVector(endIdx-startIdx+1);

      Point2D prevPt = null;

      for (int i = startIdx; i <= endIdx; i++)
      {
         ShapeComponent comp = fullVec.get(i);
         comp.setStart(prevPt);
         vec.add(comp);
         prevPt = comp.getEnd();
      }

      return tryLineifyArea(vec);
   }

   private ShapeComponentVector tryLineifyArea(ShapeComponentVector vec)
    throws InterruptedException
   {
      return tryLineifyArea(vec, null);
   }

   private ShapeComponentVector tryLineifyArea(ShapeComponentVector vec,
      Integer[] indexes)
    throws InterruptedException
   {
      int n = vec.size();

      if (n < 3) return vec;

      if (vec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         return vec;
      }

      PathCoord[] pts = new PathCoord[n-1];

      for (int i = 0; i < pts.length; i++)
      {
         ShapeComponent comp = vec.get(i);

         Point2D grad1 = new Point2D.Double();
         Point2D grad2 = new Point2D.Double();
         double bend = getBendAngle(vec, i, grad1, grad2);
         pts[i] = new PathCoord(comp.getType(), comp.getEnd(), bend, grad1, grad2);
      }

      ShapeComponentVector newVec = getBestPath(pts, indexes);

      return newVec == null ? vec : newVec;
   }

   private ShapeComponentVector getBestPath(PathCoord[] pts)
    throws InterruptedException
   {
      return getBestPath(pts, null);
   }

   private ShapeComponentVector getBestPath(PathCoord[] pts, Integer[] indexes)
    throws InterruptedException
   {
      if (pts.length < 3)
      {
         return null;
      }

      int n2 = pts.length / 2;
      int n1 = pts.length - n2;

      LineifyResults results = lineifyBestFit(pts, n1, n2);

      if (results == null || results.bestOffset == -1
            || results.minDelta > deltaThreshold)
      {
         return null;
      }

      double minAverageDelta = results.minAverageDelta;
      LineFit[] bestLineFit = results.bestLineFit;
      ShapeComponentVector bestPath = results.bestPath;
      int bestOffset = results.bestOffset;
      n1 = results.n1;
      n2 = results.n2;
      double variance = results.variance;

      if (!doLineIntersectionCheck)
      {
         return minAverageDelta <= deltaThreshold ? bestPath : null;
      }
      else if (minAverageDelta <= deltaThreshold && variance <= varianceThreshold)
      {
         if (indexes != null)
         {
            for (int i = 0; i < indexes.length; i++)
            {
               indexes[i] = null;
            }
         }

         return bestPath;
      }

      Vector<Vector<Integer>> spikes = new Vector<Vector<Integer>>();
      Vector<Integer> currentSpike = null;

      for (int i = 0; i < bestLineFit.length; i++)
      {
         if (bestLineFit[i].delta > deltaThreshold)
         {
            if (currentSpike == null)
            {
               currentSpike = new Vector<Integer>();
            }

            currentSpike.add(Integer.valueOf(i));
         }
         else if (currentSpike != null)
         {
            if (currentSpike.size() > 1)
            {
               spikes.add(currentSpike);
            }

            currentSpike = null;
         }
      }

      if (currentSpike != null)
      {
         if (currentSpike.size() > 1)
         {
            spikes.add(currentSpike);
         }
      }

      if (spikes.isEmpty())
      {
         return null;
      }

      Integer[] allIndexes = new Integer[pts.length];

      for (int i = 0; i < pts.length; i++)
      {
         allIndexes[i] = Integer.valueOf(i);
      }

      ShapeComponentVector lastShape = null;

      for (Vector<Integer> spike : spikes)
      {
         spike.sort(null);

         int startIdx = spike.firstElement().intValue();
         int endIdx = spike.lastElement().intValue();
         int m1 = endIdx-startIdx+1;
         int m2 = m1;
         Vector<Integer> indexes2 = null;

         Point2D p1=null, p2=null;

         if ((pts.length+bestLineFit[startIdx].idx2
                 - bestLineFit[endIdx].idx2)%pts.length
             == 1 && spike.size() > 2)
         {
            Point2D r1 = pts[bestLineFit[startIdx].idx2].getPoint();
            Point2D r2 = pts[bestLineFit[endIdx].idx2].getPoint();

            Point2D p = pts[bestLineFit[startIdx].idx1].getPoint();

            // get closest point on r1-r2 to p

            double diff_y = r1.getY() - r2.getY();
            double diff_x = r1.getX() - r2.getX();
            double m = diff_y/diff_x;

            double m_sq = 0.0;
            double orthog_m=0.0;
            double orthog_m_sq = 0.0;
            boolean use_orthog = false;
            double factor, x, y;

            if (Double.isNaN(m))
            {
               use_orthog = true;
               orthog_m = -diff_x/diff_y;
               orthog_m_sq = orthog_m*orthog_m;
               factor = 1.0/(1.0 + orthog_m_sq);
            }
            else
            {
               m_sq = m*m;
               factor = 1.0/(1.0 + m_sq);
            }

            if (use_orthog)
            {
               x = (r1.getX()+orthog_m*(r1.getY()-p.getY())
                      +orthog_m_sq*p.getX())*factor;
               y = orthog_m * (x - p.getX()) + p.getY();
            }
            else
            {
               x = (m_sq*r1.getX()+m*(p.getY()-r1.getY())+p.getX())*factor;
               y = m * (x - r1.getX()) + r1.getY();
            }

            // get halfway position between p and (x,y);
            p1 = new Point2D.Double(p.getX()+0.5*(x-p.getX()), 
                                    p.getY()+0.5*(y-p.getY()));

            p = pts[bestLineFit[endIdx].idx1].getPoint();

            // get closest point on r1-r2 to p

            if (use_orthog)
            {
               x = (r2.getX()+orthog_m*(r2.getY()-p.getY())
                        +orthog_m_sq*p.getX())*factor;
               y = orthog_m * (x - p.getX()) + p.getY();
            }
            else
            {
               x = (m_sq*r2.getX()+m*(p.getY()-r2.getY())+p.getX())*factor;
               y = m * (x - r2.getX()) + r2.getY();
            }

            // get halfway position between p and (x,y);
            p2 = new Point2D.Double(p.getX()+0.5*(x-p.getX()), 
                                    p.getY()+0.5*(y-p.getY()));

            m2 = 0;
         }
         else
         {
            indexes2 = new Vector<Integer>(m2);

            for (int j = startIdx; j <= endIdx; j++)
            {
               int k = bestLineFit[j].idx2;

               Integer num = Integer.valueOf(k);

               if (!indexes2.contains(num))
               {
                  indexes2.add(num);
               }
            }

            m2 = indexes2.size();
         }

         Integer[] subIndexes = new Integer[m1+m2];
         ShapeComponentVector newShape = new ShapeComponentVector(m1+m2+2);

         Point2D prevPt = null;
         double[] coords = null;
         int subIdx = 0;

         for (int j = startIdx; j <= endIdx; j++)
         {
            int k = bestLineFit[j].idx1;

            subIndexes[subIdx++] = Integer.valueOf(k);
            Point2D p = pts[k].getPoint();
            coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               newShape.add(new ShapeComponent(PathIterator.SEG_MOVETO,
                 coords, prevPt));
            }
            else
            {
               newShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                 coords, prevPt));
            }

            prevPt = p;
         }

         if (m2 == 0)
         {
            coords = new double[6];
            coords[0] = p2.getX();
            coords[1] = p2.getY();

            newShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                 coords, prevPt));

            coords = new double[6];
            coords[0] = p1.getX();
            coords[1] = p1.getY();

            newShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                 coords, p2));

            prevPt = p1;
         }
         else
         {
            for (int i = m2-1; i >= 0; i--)
            {
               Integer num = indexes2.get(i);
               subIndexes[subIdx++] = num;
               Point2D p = pts[num.intValue()].getPoint();
               coords = new double[6];
               coords[0] = p.getX();
               coords[1] = p.getY();

               newShape.add(new ShapeComponent(PathIterator.SEG_LINETO,
                 coords, prevPt));

               prevPt = p;
            }
         }

         newShape.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));

         ShapeComponentVector newVec = tryLineifyArea(newShape, subIndexes);

         if (newVec != newShape)
         {
            subIdx = 0;

            for (int j = startIdx; j <= endIdx; j++)
            {
               int k = bestLineFit[j].idx1;

               if (subIndexes[subIdx++] == null)
               {
                  allIndexes[k] = null;
               }
            }

            if (indexes2 != null)
            {
               for (Integer num : indexes2)
               {
                  int k = num.intValue();

                  if (subIndexes[subIdx++] == null)
                  {
                     allIndexes[k] = null;
                  }
               }
            }

            if (lastShape != null)
            {
               addShape(lastShape);
            }

            lastShape = newVec;
         }
      }

      int remaining = 0;

      for (int i = 0; i < allIndexes.length; i++)
      {
         if (allIndexes[i] != null)
         {
            remaining++;
         }
      }

      if (remaining == 0)
      {
         return lastShape;
      }

      if (lastShape != null)
      {
         addShape(lastShape);
      }

      if (remaining < 3 || remaining == allIndexes.length)
      {
         return null;
      }

      PathCoord[] remainingPts = new PathCoord[remaining];

      for (int j = 0, i = 0; j < allIndexes.length; j++)
      {
         if (allIndexes[j] != null)
         {
            remainingPts[i++] = pts[j];
         }
      }

      n2 = remaining / 2;
      n1 = remaining - n2;

      results = lineifyBestFit(remainingPts, n1, n2);

      if (indexes != null)
      {
         for (int i = 0; i < indexes.length; i++)
         {
            indexes[i] = null;
         }
      }

      if (results.minAverageDelta <= deltaThreshold
              && results.variance <= varianceThreshold)
      {
         return results.bestPath;
      }

      ShapeComponentVector newShape = new ShapeComponentVector(remaining+1);
      Point2D prevPt = null;
      double[] coords = null;

      for (int i = 0; i < remaining; i++)
      {
         Point2D p = remainingPts[i].getPoint();
         coords = new double[6];
         coords[0] = p.getX();
         coords[1] = p.getY();

         if (prevPt == null)
         {
            newShape.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
         }
         else
         {
            newShape.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
         }

         prevPt = p;
      }

      newShape.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));

      return newShape;
   }

   private LineifyResults lineifyBestFit(PathCoord[] pts, int n1, int n2)
    throws InterruptedException
   {
      double minAverageDelta = Double.MAX_VALUE;
      LineFit[] bestLineFit = null;
      ShapeComponentVector bestPath = null;
      int bestOffset = -1;
      int bestN1 = n1;
      int bestN2 = n2;
      int maxN = (int)Math.max(n1, n2);
      int minN = (int)Math.min(n1, n2);

      for (int i = 0; i < maxN; i++)
      {
         ShapeComponentVector newPath = new ShapeComponentVector(maxN);
         newPath.setFilled(false);
         LineFit[] linefit = tryLineify(pts, i, n1, n2, newPath);

         double averageDelta = 0.0;

         for (int j = 0; j < linefit.length; j++)
         {
            averageDelta += linefit[j].delta;
         }

         averageDelta /= linefit.length;

         if (averageDelta < minAverageDelta)
         {
            bestPath = newPath;
            minAverageDelta = averageDelta;
            bestOffset = i;
            bestLineFit = linefit;

            setLineWidth(bestPath, averageDelta);
            updateCurrentShape(newPath);
         }
      }

      if (bestOffset == -1)
      {
         return null;
      }

      // Find closest bend in the outline

      double startBend = pts[bestOffset].getBend();
      double endBend = pts[(bestOffset+n1)%pts.length].getBend();

      int maxSteps = (int)Math.ceil(0.2*pts.length);

      int bestStartOffset = bestOffset;
      int bestEndOffset = bestOffset+n1;

      double bestStartBend = startBend;
      double bestEndBend = endBend;

      for (int i = 1; i <= maxSteps; i++)
      {
         // bend on either side of start
         int idx = (bestOffset+i)%pts.length;

         if (pts[idx].getBend() < bestStartBend)
         {
            bestStartBend = pts[idx].getBend();
            bestStartOffset = idx;
         }

         idx = (bestOffset+pts.length-i)%pts.length;

         if (pts[idx].getBend() < bestStartBend)
         {
            bestStartBend = pts[idx].getBend();
            bestStartOffset = idx;
         }

         // bend on either side of end
         idx = (bestOffset+n1+i)%pts.length;

         if (pts[idx].getBend() < bestEndBend)
         {
            bestEndBend = pts[idx].getBend();
            bestEndOffset = idx;
         }

         idx = (bestOffset+n1+pts.length-i)%pts.length;

         if (pts[idx].getBend() < bestEndBend)
         {
            bestEndBend = pts[idx].getBend();
            bestEndOffset = idx;
         }
      }

      if (!(bestStartOffset == bestOffset && bestEndOffset == bestOffset+n1)
         && bestStartOffset != bestEndOffset)
      {
         ShapeComponentVector newPath = new ShapeComponentVector(n2);
         newPath.setFilled(false);

         int m1 = (pts.length+bestEndOffset-bestStartOffset)%pts.length;
         int m2 = pts.length-m1;
         int offset = bestStartOffset;

         if (m2 > m1)
         {
            offset = bestEndOffset;
            int tmp = m1;
            m1 = m2;
            m2 = tmp;
         }

         LineFit[] linefit = tryLineify(pts, offset, m1, m2, newPath);

         double averageDelta = 0.0;

         for (int j = 0; j < linefit.length; j++)
         {
            averageDelta += linefit[j].delta;
         }

         averageDelta /= linefit.length;

         if (averageDelta < minAverageDelta)
         {
            minAverageDelta = averageDelta;
            bestPath = newPath;
            bestLineFit = linefit;
            bestOffset = offset;
            bestN1 = m1;
            bestN2 = m2;

            setLineWidth(bestPath, averageDelta);
            updateCurrentShape(newPath);
         }
      }

      double variance = 0.0;
      double minDelta = minAverageDelta;

      for (int i = 0; i < bestLineFit.length; i++)
      {
         double diff = bestLineFit[i].delta - minAverageDelta;
         variance += diff*diff;

         if (bestLineFit[i].delta < minDelta)
         {
            minDelta = bestLineFit[i].delta;
         }
      }

      variance /= bestLineFit.length;

      return new LineifyResults(bestPath, minAverageDelta, bestOffset, bestLineFit,
        bestN1, bestN2, variance, minDelta);
   }

   private LineFit[] tryLineify(PathCoord[] pts, int offset, int n1, int n2, 
      ShapeComponentVector path)
   {
      int maxN = (int)Math.max(n1, n2);
      int minN = (int)Math.min(n1, n2);

      int div = maxN/minN;

      LineFit[] linefit = tryLineify(maxN, minN, div, 0, pts, offset, n1, n2, path);

      if (n1 == n2)
      {
         return linefit;
      }

      LineFit[] bestLineFit = linefit;
      ShapeComponentVector bestPath = path;

      double bestDelta = calculateMean(linefit);

      int remainder = maxN % minN;

      for (int r = 1; r < remainder; r++)
      {
         ShapeComponentVector newPath = new ShapeComponentVector(path.size());
         linefit = tryLineify(maxN, minN, div, r, pts, offset, n1, n2, newPath);

         double delta = calculateMean(linefit);

         if (delta < bestDelta)
         {
            bestDelta = delta;
            bestLineFit = linefit;
            bestPath = newPath;

            setLineWidth(bestPath, bestDelta);
         }
      }

      div = (int)Math.ceil(maxN/(double)minN);

      remainder = maxN-((minN-2)*div);

      for (int r = 1; r < remainder; r++)
      {
         ShapeComponentVector newPath = new ShapeComponentVector(path.size());
         linefit = tryLineify(maxN, minN, div, r, pts, offset, n1, n2, newPath);

         double delta = calculateMean(linefit);

         if (delta < bestDelta)
         {
            bestDelta = delta;
            bestLineFit = linefit;
            bestPath = newPath;
            setLineWidth(bestPath, bestDelta);
         }
      }

      if (bestPath != path)
      {
         path.clear();
         path.addAll(bestPath);
         setLineWidth(path, bestDelta);
      }

      return linefit;
   }

   private LineFit[] tryLineify(int maxN, int minN, int div, int r, 
     PathCoord[] pts, int offset, int n1, int n2, ShapeComponentVector path)
   {
      Point2D prevPt = null;
      LineFit[] linefit = new LineFit[maxN];

      int k = 0;

      for (int i = 0; i < minN; i++)
      {
         for (int j = 0; j < (r == 0 ? div : i == 0 ? r : div); j++, k++)
         {
            if (k >= maxN) break;

            int idx1, idx2;

            if (n1 == n2)
            {
               idx1 = i;
               idx2 = i;
            }
            else if (n1 < n2)
            {
               idx1 = i;
               idx2 = k;
            }
            else
            {
               idx1 = k;
               idx2 = i;
            }

            idx1 = (offset+idx1)%pts.length;
            idx2 = (offset+pts.length-idx2-1)%pts.length;

            Point2D p1 = pts[idx1].getPoint();
            Point2D p2 = pts[idx2].getPoint();

            double dx = 0.5*(p2.getX()-p1.getX());
            double dy = 0.5*(p2.getY()-p1.getY());

            Point2D p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

            double[] coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (prevPt == null)
            {
               path.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
            }
            else
            {
               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
            }

            double delta = Math.sqrt(dx*dx + dy*dy);

            linefit[k] = new LineFit(delta, idx1, idx2, p1, p2);

            prevPt = p;
         }
      }

      for ( ; k < maxN; k++)
      {
         int i = minN-1;
         int idx1, idx2;

         if (n1 < n2)
         {
            idx1 = i;
            idx2 = k;
         }
         else
         {
            idx1 = k;
            idx2 = i;
         }

         idx1 = (offset+idx1)%pts.length;
         idx2 = (offset+pts.length-idx2-1)%pts.length;

         Point2D p1 = pts[idx1].getPoint();
         Point2D p2 = pts[idx2].getPoint();

         double dx = 0.5*(p2.getX()-p1.getX());
         double dy = 0.5*(p2.getY()-p1.getY());

         Point2D p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

         double[] coords = new double[6];
         coords[0] = p.getX();
         coords[1] = p.getY();

         if (prevPt == null)
         {
            path.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
         }
         else
         {
            path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
         }

         double delta = Math.sqrt(dx*dx + dy*dy);

         linefit[k] = new LineFit(delta, idx1, idx2, p1, p2);

         prevPt = p;
      }

      return linefit;
   }

   private ShapeComponentVector tryLineifyArea2(ShapeComponentVector vec)
   {
      int n = vec.size();

      if (n < 3) return vec;

      int numClosed = 0;

      if (vec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         return vec;
      }

      for (int i = 0; i < n; i++)
      {
         ShapeComponent comp = vec.get(i);

         if (comp.getType() == PathIterator.SEG_CLOSE)
         {
            numClosed++;

            if (i != n-1)
            {
               return vec;
            }
         }

         if (i != n-1)
         {
            Point2D grad1 = new Point2D.Double();
            Point2D grad2 = new Point2D.Double();
            double bend = getBendAngle(vec, i, grad1, grad2);
            comp.setBend(bend);
         }
      }

      if (numClosed != 1) return vec;

      if (n == 3)
      {
         vec.remove(2);
         return vec;
      }

      double minDelta = Double.MAX_VALUE;
      ShapeComponentVector bestPath = null;
      int bestOffset = 0;

      int halfWay = n/2;

      for (int offset=0; offset < halfWay; offset++)
      {
         double delta=0.0;

         ShapeComponentVector path = new ShapeComponentVector(halfWay);
         Point2D startPt = null;

         for (int i = 0; i < halfWay; i++)
         {
            int idx1 = offset+i;
            int idx2 = (offset+n-i-2)%(n-1);

            ShapeComponent comp = vec.get(idx1);
            Point2D p1 = comp.getEnd();

            int type1 = (idx1 == n-2 ? PathIterator.SEG_LINETO : 
              vec.get(idx1+1).getType());

            comp = vec.get(idx2);
            Point2D p2 = comp.getEnd();

            int type2 = (idx2 == 0 ? PathIterator.SEG_LINETO : comp.getType());

            Point2D p = new Point2D.Double(
              p1.getX()+0.5*(p2.getX()-p1.getX()),
              p1.getY()+0.5*(p2.getY()-p1.getY()));

            double dx = p.getX()-p1.getX();
            double dy = p.getY()-p1.getY();

            delta += Math.sqrt(dx*dx + dy*dy);

            dx = p.getX()-p2.getX();
            dy = p.getY()-p2.getY();

            delta += Math.sqrt(dx*dx + dy*dy);

            double[] coords = new double[6];
            coords[0] = p.getX();
            coords[1] = p.getY();

            if (startPt == null || 
                 (type1 == PathIterator.SEG_MOVETO && type1 == type2))
            {
               path.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, startPt));
            }
            else
            {
               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, startPt));
            }

            startPt = p;
         }

         delta = delta / (2*halfWay);

         if (delta < minDelta)
         {
            minDelta = delta;
            bestPath = path;
            bestOffset = offset;
         }
      }

      if (bestPath == null) return vec;

      ShapeComponent startComp = vec.get(bestOffset);
      int endIdx = (bestOffset+halfWay)%(n-1);
      ShapeComponent endComp = vec.get(endIdx);

      double bestStartAngle = startComp.getBend();
      double bestEndAngle = endComp.getBend();

      int maxSteps = Math.min(10, n/3);
      int bestStartIdx = bestOffset;
      int bestEndIdx = endIdx;

      for (int i = 1; i < maxSteps; i++)
      {
         int j = bestOffset+i;
         int k = bestOffset-i;

         if (j >= n-1) j = (j+1)%n;

         if (k < 0) k = k + n - 1;

         double angle_j = vec.get(j).getBend();
         double angle_k = vec.get(k).getBend();

         if (angle_j < bestStartAngle)
         {
            bestStartIdx = j;
            bestStartAngle = angle_j;
         }

         if (angle_k < bestStartAngle)
         {
            bestStartIdx = k;
            bestStartAngle = angle_k;
         }

         j = endIdx+i;
         k = endIdx-i;

         if (j >= n-1) j = (j+1)%n;

         if (k < 0) k = k + n - 1;

         angle_j = vec.get(j).getBend();
         angle_k = vec.get(k).getBend();

         if (angle_j < bestEndAngle)
         {
            bestEndIdx = j;
            bestEndAngle = angle_j;
         }

         if (angle_k < bestEndAngle)
         {
            bestEndIdx = k;
            bestEndAngle = angle_k;
         }
      }

      if (bestStartIdx > bestEndIdx)
      {
         int tmp = bestEndIdx;
         bestEndIdx = bestStartIdx;
         bestStartIdx = tmp;
      }

      if ((bestOffset == bestStartIdx && endIdx == bestEndIdx)
          || bestEndIdx - bestStartIdx < 2
          || (bestStartIdx == 0 && bestEndIdx == n-2))
      {
         return minDelta <= deltaThreshold ? bestPath : vec;
      }

      ShapeComponent bestStartComp = vec.get(bestStartIdx);
      ShapeComponent bestEndComp = vec.get(bestEndIdx);

      int n1 = bestEndIdx - bestStartIdx;
      int n2 = n - 1 - n1;

      ShapeComponent lastComp = vec.get(n-2); 
      Point2D lastPoint = lastComp.getEnd();

      PathCoord[] pts1 = new PathCoord[Math.max(n1, n2)];
      PathCoord[] pts2 = new PathCoord[pts1.length];

      if (n1 == n2)
      {
         for (int i = 0; i < n1; i++)
         {
            int j = bestStartIdx+i;
            if (j >= n-1) j = (j+1)%n;

            ShapeComponent comp = vec.get(j);

            int type = (j == n-2 ? PathIterator.SEG_LINETO : vec.get(j+1).getType());

            pts1[i] = new PathCoord(type, comp.getEnd());

            j = bestEndIdx+i;
            if (j >= n-1) j = (j+1)%n;

            comp = vec.get(j);

            type = (j == 0 ? PathIterator.SEG_LINETO : comp.getType());

            pts2[i] = new PathCoord(type, comp.getEnd());
         }
      }
      else if (n1 < n2)
      {
         int d = n2/(n1-1);
         int extra = n2-n1;

         pts1[0] = new PathCoord(PathIterator.SEG_MOVETO, 
                                 vec.get(bestStartIdx).getEnd());

         for (int i = 1, j = 1; j < n2; i++)
         {
            int k = bestStartIdx+i;
            if (k >= n-1) k = (k+1)%n;

            ShapeComponent comp = vec.get(k);
            Point2D p0 = comp.getStart();

            if (k == 0)
            {
               p0 = lastPoint;
            }

            Point2D p1 = comp.getEnd();

            int type = (k == n-2 ? PathIterator.SEG_LINETO : vec.get(k+1).getType());

            if (extra >= d)
            {
               for (int l = 1; l < d && j < pts1.length-1; l++)
               {
                  Point2D pt = comp.getP(((double)l)/d, p0);
                  pts1[j++] = new PathCoord(type, pt);
                  p0 = pt;
               }

               pts1[j++] = new PathCoord(type, p1);
               extra -= (d-1);
            }
            else if (extra > 0)
            {
               for (int l = 1; l <= extra && j < pts1.length-1; l++)
               {
                  Point2D pt = comp.getP(((double)l)/(extra+1), p0);
                  pts1[j++] = new PathCoord(type, pt);
                  p0 = pt;
               }

               pts1[j++] = new PathCoord(type, p1);
               extra = 0;
            }
            else
            {
               pts1[j++] = new PathCoord(type, p1);
            }
         }

         for (int i = n2-1, j=0; i >= 0; i--)
         {
            int k = bestEndIdx+i;
            if (k >= n-1) k = (k+1)%n;

            ShapeComponent comp = vec.get(k);

            int type = (k == 0 ? PathIterator.SEG_LINETO : comp.getType());

            pts2[j++] = new PathCoord(type, comp.getEnd());
         }
      }
      else
      {
         int d = n1/(n2-1);
         int extra = n1-n2;

         pts2[0] = new PathCoord(PathIterator.SEG_MOVETO, 
                                 vec.get(bestEndIdx).getEnd());

         for (int i = 1, j = 1; j < n1; i++)
         {
            int k = bestEndIdx-i;
            if (k < 0) k = k + n - 1;

            ShapeComponent comp = vec.get(k);
            Point2D p0 = comp.getStart();

            if (k == 0)
            {
               p0 = lastPoint;
            }

            Point2D p1 = comp.getEnd();

            int type = (k == 0 ? PathIterator.SEG_LINETO : comp.getType());

            if (extra >= d)
            {
               for (int l = d-1; l >= 1 && j < pts2.length-1; l--)
               {
                  Point2D pt = comp.getP(((double)l)/d, p0);
                  pts2[j++] = new PathCoord(type, pt);
                  p0 = pt;
               }

               pts2[j++] = new PathCoord(type, p1);
               extra -= (d-1);
            }
            else if (extra > 0)
            {
               for (int l = extra; l >= 1 && j < pts2.length-1; l--)
               {
                  Point2D pt = comp.getP(((double)l)/(extra+1), p0);
                  pts2[j++] = new PathCoord(type, pt);
                  p0 = pt;
               }

               pts2[j++] = new PathCoord(type, p1);
               extra = 0;
            }
            else
            {
               pts2[j++] = new PathCoord(type, p1);
            }
         }

         for (int i = n1-1, j=0; i >= 0; i--)
         {
            int k = bestStartIdx+i;
            if (k >= n-1) k = (k+1)%n;

            ShapeComponent comp = vec.get(k);

            int type = (k == n-2 ? PathIterator.SEG_LINETO : vec.get(k+1).getType());

            pts1[j++] = new PathCoord(type, comp.getEnd());
         }
      }

      ShapeComponentVector newPath = new ShapeComponentVector(pts1.length);
      double delta = 0.0;
      Point2D prevPt = null;

      for (int i = 0; i < pts1.length; i++)
      {
         double[] coords = new double[6];

         coords[0] = pts1[i].getX() + 0.5*(pts2[i].getX() - pts1[i].getX());
         coords[1] = pts1[i].getY() + 0.5*(pts2[i].getY() - pts1[i].getY());

         if (prevPt == null 
             || (pts1[i].getType() == PathIterator.SEG_MOVETO 
                  && pts1[i].getType() == pts2[i].getType()))
         {
            newPath.add(new ShapeComponent(PathIterator.SEG_MOVETO, coords, prevPt));
         }
         else
         {
            newPath.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
         }

         double dx = coords[0]-pts1[i].getX();
         double dy = coords[1]-pts1[i].getY();

         delta += Math.sqrt(dx*dx+dy*dy);

         dx = coords[0]-pts2[i].getX();
         dy = coords[1]-pts2[i].getY();

         delta += Math.sqrt(dx*dx+dy*dy);

         prevPt = new Point2D.Double(coords[0], coords[1]);
      }

      delta = delta/(2*pts1.length);

      if (delta < minDelta && delta <= deltaThreshold)
      {
         newPath.setFilled(false);
         return newPath;
      }
      else if (minDelta <= deltaThreshold)
      {
         bestPath.setFilled(false);
         return bestPath;
      }

      return vec;
   }

   /*
    * The initial scan results in a shape consisting of horizontal
    * and vertical steps. The bend angle is the approximate gradient
    * at the mid-way point on the path component, averaging across 
    * the steps on either side. The gradient is the approximate
    * gradient on either side of the component end point.
    */ 
   private double getBendAngle(ShapeComponentVector vec, int idx,
     Point2D grad1, Point2D grad2)
   {
      int n = vec.size();

      // get the average gradient on either side.

      double maxLength = 3*maxTinyStep;
      //double maxLength = 2.0*deltaThreshold;
      int maxSteps = (n < 10 ? 1 : (n < 20 ? 4 : 20));

      double dx1 = 0.0;
      double dy1 = 0.0;
      double dx2 = 0.0;
      double dy2 = 0.0;
      double dx3 = 0.0;
      double dy3 = 0.0;
      double dx4 = 0.0;
      double dy4 = 0.0;

      double runningLength1=0.0, runningLength2=0.0;
      double runningLength3=0.0, runningLength4=0.0;

      ShapeComponent lastComp = vec.get(n-2); 
      Point2D lastPoint = lastComp.getEnd();
      ShapeComponent comp = vec.get(idx); 

      int steps1 = 0;
      int steps2 = 0;
      int steps3 = 0;
      int steps4 = 0;

      boolean stop1 = false;
      boolean stop2 = false;
      boolean stop3 = false;
      boolean stop4 = false;

      Point2D p0, p1, p2;

      Point2D q0, q1, q2;

      if (idx == 0)
      {
         p0 = comp.getMid(lastPoint);
      }
      else
      {
         p0 = comp.getMid();
      }

      q0 = comp.getEnd();

      for (int offset = 1; offset <= maxSteps; offset++)
      {
         int j = idx+offset;
         int k = idx-offset;

         if (j >= n-1)
         {
            j = (j+1)%n;
         }

         if (k < 0)
         {
            k = k + n - 1;
         }

         if (j == k || j == idx || k == idx) break;

         comp = vec.get(j);

         if (j == 0)
         {
            p1 = comp.getMid(lastPoint);
         }
         else
         {
            p1 = comp.getMid();
         }

         q1 = comp.getEnd();

         comp = vec.get(k);

         if (k == 0)
         {
            p2 = comp.getMid(lastPoint);
         }
         else
         {
            p2 = comp.getMid();
         }

         q2 = comp.getEnd();

         if (!stop1)
         {
            double x = p1.getX() - p0.getX();
            double y = p1.getY() - p0.getY();

            double length = Math.sqrt(x*x + y*y);

            dx1 += x/length;
            dy1 += y/length;
            steps1++;

            runningLength1 += length;

            if (runningLength1 > maxLength || steps1 >= maxSteps)
            {
               stop1 = true;
            }
         }

         if (!stop2)
         {
            double x = p2.getX() - p0.getX();
            double y = p2.getY() - p0.getY();

            double length = Math.sqrt(x*x + y*y);

            dx2 += x/length;
            dy2 += y/length;
            steps2++;

            runningLength2 += length;

            if (runningLength2 > maxLength || steps2 >= maxSteps)
            {
               stop2 = true;
            }
         }

         if (!stop3)
         {
            int j2 = j+1;

            if (j2 >= n-1)
            {
               j2 = (j2+1)%n;
            }

            comp = vec.get(j2);
            Point2D r = comp.getEnd();

            double x = r.getX() - q0.getX();
            double y = r.getY() - q0.getY();

            double length = Math.sqrt(x*x + y*y);

            if (length > maxTinyStep)
            {
               x = q1.getX() - q0.getX();
               y = q1.getY() - q0.getY();
               length = Math.sqrt(x*x + y*y);
               stop3 = true;
            }

            if (!stop3 || offset == 1)
            {
               dx3 += x/length;
               dy3 += y/length;
               steps3++;

               runningLength3 += length;

               if (runningLength3 > maxLength || steps3 >= maxSteps)
               {
                  stop3 = true;
               }
            }
         }

         if (!stop4)
         {
            int k2 = k-1;

            if (k2 < 0)
            {
               k2 = k2 + n - 1;
            }

            comp = vec.get(k2);
            Point2D r = comp.getEnd();

            double x = q0.getX() - r.getX();
            double y = q0.getY() - r.getY();

            double length = Math.sqrt(x*x + y*y);

            if (length > maxTinyStep)
            {
               x = q0.getX() - q2.getX();
               y = q0.getY() - q2.getY();
               length = Math.sqrt(x*x + y*y);
               stop4 = true;
            }

            if (!stop4 || offset == 1)
            {
               dx4 += x/length;
               dy4 += y/length;
               steps4++;

               runningLength4 += length;

               if (runningLength4 > maxLength || steps4 >= maxSteps)
               {
                  stop4 = true;
               }
            }
         }

         if (stop1 && stop2 && stop3 && stop4)
         {
            break;
         }
      }

      if (steps3 != 0)
      {
         grad1.setLocation(dx3/steps3, dy3/steps3);
      }

      if (steps4 != 0)
      {
         grad2.setLocation(dx4/steps4, dy4/steps4);
      }

      return getAngle(dx1, dy1, dx2, dy2);
   }

   private double getBendAngle2(ShapeComponentVector vec, int idx,
     Point2D grad1, Point2D grad2)
   {
      int n = vec.size();

      // get the average gradient on either side.

      double maxLength = 2.0*deltaThreshold;
      int maxSteps = (n < 10 ? 1 : (n < 20 ? 4 : 20));

      double angleSum = 0.0;
      double runningLength1=0.0, runningLength2=0.0;
      double runningLength3=0.0, runningLength4=0.0;

      ShapeComponent lastComp = vec.get(n-2); 
      Point2D lastPoint = lastComp.getEnd();
      ShapeComponent comp = vec.get(idx); 

      int steps = 0;

      Point2D p0, p1, p2;

      Point2D q0, q1, q2;

      if (idx == 0)
      {
         p0 = comp.getMid(lastPoint);
      }
      else
      {
         p0 = comp.getMid();
      }

      q0 = comp.getEnd();

      for (int offset = 1; offset <= maxSteps; offset++)
      {
         int j = idx+offset;
         int k = idx-offset;

         if (j >= n-1)
         {
            j = (j+1)%n;
         }

         if (k < 0)
         {
            k = k + n - 1;
         }

         if (j == k || j == idx || k == idx) break;

         comp = vec.get(j);

         if (j == 0)
         {
            p1 = comp.getMid(lastPoint);
         }
         else
         {
            p1 = comp.getMid();
         }

         q1 = comp.getEnd();

         comp = vec.get(k);

         if (k == 0)
         {
            p2 = comp.getMid(lastPoint);
         }
         else
         {
            p2 = comp.getMid();
         }

         q2 = comp.getEnd();

         double v1_x = p1.getX() - p0.getX();
         double v1_y = p1.getY() - p0.getY();

         double v2_x = p2.getX() - p0.getX();
         double v2_y = p2.getY() - p0.getY();

         double length1 = Math.sqrt(v1_x*v1_x + v1_y * v1_y);
         double length2 = Math.sqrt(v2_x*v2_x + v2_y * v2_y);

         double dotproduct = v1_x * v2_x + v1_y * v2_y;

         // angle in range [0, PI]
         double angle = Math.acos(dotproduct / (length1 * length2));

         if (Double.isNaN(angle))
         {
            angle = Math.PI;
         }

         double u1_x = q1.getX() - q0.getX();
         double u1_y = q1.getY() - q0.getY();

         double u2_x = q0.getX() - q2.getX();
         double u2_y = q0.getY() - q2.getY();

         double length3 = Math.sqrt(u1_x*u1_x + u1_y * u1_y);
         double length4 = Math.sqrt(u2_x*u2_x + u2_y * u2_y);

         runningLength3 += length3;
         runningLength4 += length4;

         if (offset == 1)
         {
            grad1.setLocation(u1_x/length3, u1_y/length3);
            grad2.setLocation(u2_x/length4, u2_y/length4);
         }
         else
         {
            if (runningLength3 < maxLength)
            {
               grad1.setLocation(u1_x/length3, u1_y/length3);
            }

            if (runningLength4 < maxLength)
            {
               grad2.setLocation(u2_x/length4, u2_y/length4);
            }
         }

         if (length1 > maxLength || length2 > maxLength)
         {
            if (steps == 0)
            {
               return angle;
            }

            break;
         }

         angleSum += angle;
         steps++;
      }

      return steps == 0 ? Math.PI : angleSum/steps;
   }

   private int progress, maxProgress;
   private double deltaThreshold; // max average deviation
   private double varianceThreshold; // max variance
   private double maxTinyStep; // to assist gradient approximation across small steps
   private double returnPtDist; // spike detection
   private double minStubLength;
   private VectorizeBitmapDialog dialog;
   private boolean continueToNextStep, doLineIntersectionCheck;
   private Vector<ShapeComponentVector> shapeList;
   private static final double HALF_PI = 0.5*Math.PI,
    SPIKE_ANGLE_LOWER1=0.2*Math.PI, SPIKE_ANGLE_UPPER1=0.8*Math.PI,
    SPIKE_ANGLE_LOWER2=1.2*Math.PI, SPIKE_ANGLE_UPPER2=1.8*Math.PI;
}

class Spike
{
   public Spike(Number index)
   {
      this.index = index;
   }

   public Spike(int idx, ShapeComponentVector vec,
    Point2D pathStart, Point2D pathEnd, VectorizeBitmapDialog dialog)
   {
      this.index = Integer.valueOf(idx);
      update(vec, pathStart, pathEnd, dialog);
   }

   public Number getIndex()
   {
      return index;
   }

   public int intValue()
   {
      return index.intValue();
   }

   public void setIndex(double idx, ShapeComponentVector vec,
     Point2D pathStart, Point2D pathEnd, VectorizeBitmapDialog dialog)
   {
      this.index = Double.valueOf(idx);
      update(vec, pathStart, pathEnd, dialog);
   }

   public double getLength()
   {
      return length;
   }

   public double getDiagonalLength()
   {
      return diagonalLength;
   }

   public Point2D getP1()
   {
      return p1;
   }

   public Point2D getP2()
   {
      return p2;
   }

   public Point2D getMid()
   {
      return mid;
   }

   public double getBeforeAngle()
   {
      return beforeAngle;
   }

   public double getAfterAngle()
   {
      return afterAngle;
   }

   public void update(ShapeComponentVector vec,
    Point2D pathStart, Point2D pathEnd, VectorizeBitmapDialog dialog)
   {
      int n = vec.size();
      int idx = index.intValue();

      if (idx == n-1)
      {
         p1 = pathEnd;
         p2 = pathStart;
      }
      else
      {
         ShapeComponent comp = vec.get(idx);
         p1 = comp.getStart();
         p2 = comp.getEnd();
      }

      int prevIdx = idx-1;

      if (prevIdx < 1)
      {
         prevIdx = n - 1 + prevIdx;
      }

      Point2D p3;

      int nextIdx = idx+1;

      if (nextIdx == n)
      {
         nextIdx = 1;
      }

      length = JDRLine.getLength(p1, p2);

      if (!(index instanceof Integer))
      {
         if (nextIdx == n-1)
         {
            p3 = pathStart;
         }
         else
         {
            p3 = vec.get(nextIdx).getEnd();
         }

         length += JDRLine.getLength(p2, p3);
         diagonalLength = JDRLine.getLength(p1, p3);

         mid = p2;
         p2 = p3;

         nextIdx++;

         if (nextIdx == n)
         {
            nextIdx = 1;
         }
      }
      else
      {
         mid = JDRLine.getMidPoint(p1, p2);
         diagonalLength = length;
      }

      double sqThreshold = dialog.getLineDetectTinyStepThreshold();
      sqThreshold *= sqThreshold;

      if (prevIdx == n-1)
      {
         p3 = pathEnd;
      }
      else if (prevIdx == 1)
      {
         p3 = pathStart;
      }
      else
      {
         p3 = vec.get(prevIdx).getStart();
      }

      if (JDRLine.getSquareLength(p3, mid) < sqThreshold)
      {
         prevIdx--;

         if (prevIdx < 1)
         {
            prevIdx = n - 1 + prevIdx;
         }

         if (prevIdx == n-1)
         {
            p3 = pathEnd;
         }
         else if (prevIdx == 1)
         {
            p3 = pathStart;
         }
         else
         {
            p3 = vec.get(prevIdx).getStart();
         }
      }

      beforeAngle = JDRLine.getAngle(
        p3.getX(), p3.getY(), p1.getX(), p1.getY(), 
        p1.getX(), p1.getY(), mid.getX(), mid.getY());

      if (nextIdx >= n)
      {
         nextIdx = nextIdx - n + 1;
      }

      if (nextIdx == n-1)
      {
         p3 = pathStart;
      }
      else
      {
         p3 = vec.get(nextIdx).getEnd();
      }

      if (JDRLine.getSquareLength(mid, p3) < sqThreshold)
      {
         nextIdx++;

         if (nextIdx >= n)
         {
            nextIdx = nextIdx - n + 1;
         }

         if (nextIdx == n-1)
         {
            p3 = pathStart;
         }
         else
         {
            p3 = vec.get(nextIdx).getEnd();
         }
      }

      afterAngle = JDRLine.getAngle(
        mid.getX(), mid.getY(), p2.getX(), p2.getY(), 
        p2.getX(), p2.getY(), p3.getX(), p3.getY());
   }

   public String info(JDRResources resources)
   {
      if (p1 == null || p2 == null || mid == null)
      {
         return String.format("[%s]", index.toString());
      }

      if (index instanceof Integer)
      {
         return resources.getMessage("vectorize.spike_details_no_mid",
          index, p1.getX(), p1.getY(),
           p2.getX(), p2.getY(), beforeAngle, Math.toDegrees(beforeAngle),
           afterAngle, Math.toDegrees(afterAngle), length);
      }

      return resources.getMessage("vectorize.spike_details",
       index, p1.getX(), p1.getY(), mid.getX(), mid.getY(),
        p2.getX(), p2.getY(), beforeAngle, Math.toDegrees(beforeAngle),
        afterAngle, Math.toDegrees(afterAngle), diagonalLength);
   }

   public String toString()
   {
      if (p1 == null || p2 == null || mid == null)
      {
         return String.format("%s[index=%s]",
           getClass().getSimpleName(), index.toString());
      }

      return String.format("%s[index=%s; (%f,%f) -- (%f,%f) -- (%f,%f); beforeAngle=%f; afterAngle=%f; length=%f; diagonalLength=%f]",
       getClass().getSimpleName(), index.toString(), 
        p1.getX(), p1.getY(), mid.getX(), mid.getY(),
        p2.getX(), p2.getY(), beforeAngle, afterAngle, length, diagonalLength);
   }

   private Point2D p1, p2, mid;
   private double beforeAngle, afterAngle;
   private double length, diagonalLength;
   private Number index;
}

class Spike2Spike implements Comparable<Spike2Spike>
{
   public Spike2Spike(Spike spike1, Spike spike2, double midDiff,
    double inclinationDiff)
   {
      this.spike1 = spike1;
      this.spike2 = spike2;
      this.midDiff = midDiff;
      this.inclinationDiff = inclinationDiff;

      averageLength = 0.5*(spike1.getDiagonalLength() + spike2.getDiagonalLength());

      averageAngleDiff = 0.5*(
         Math.abs(spike1.getBeforeAngle()-spike1.getAfterAngle())
       + Math.abs(spike2.getBeforeAngle()-spike2.getAfterAngle())
      );

      sum = midDiff + inclinationDiff + averageLength + averageAngleDiff;
   }

   public int compareTo(Spike2Spike other)
   {
      if (sum < other.sum)
      {
         return -1;
      }

      if (sum > other.sum)
      {
         return 1;
      }

      return 0;
   }

   public Spike getSpike1()
   {
      return spike1;
   }

   public Spike getSpike2()
   {
      return spike2;
   }

   public double getFromMidway()
   {
      return midDiff;
   }

   public double getInclinationDifference()
   {
      return inclinationDiff;
   }

   public double getAverageLength()
   {
      return averageLength;
   }

   public double getAverageAngleDifference()
   {
      return averageAngleDiff;
   }

   public String toString()
   {
      return String.format("%s[spike1=%s,spike2=%s,midDiff=%f,inclinationDiff=%f,averageLength=%f,averageAngleDiff=%f,sum=%f]",
        getClass().getSimpleName(), spike1.toString(), spike2.toString(),
        midDiff, inclinationDiff, averageLength, averageAngleDiff, sum);
   }

   private Spike spike1, spike2;
   private double midDiff, inclinationDiff, averageLength, averageAngleDiff, sum;
}

class PathCoord
{
   public PathCoord(int type, Point2D p, double bend, Point2D grad1,
     Point2D grad2)
   {
      this.bend = bend;
      this.type = type;
      this.p = p;
      this.grad1 = grad1;
      this.grad2 = grad2;
   }

   public PathCoord(int type, Point2D p)
   {
      this.type = type;
      this.p = p;
   }

   public int getType()
   {
      return type;
   }

   public double getX()
   {
      return p.getX();
   }

   public double getY()
   {
      return p.getY();
   }

   public Point2D getPoint()
   {
      return p;
   }

   // average angle between this and previous pt
   public double getBend()
   {
      return bend;
   }

   public Point2D getGradient1()
   {
      return grad1;
   }

   public Point2D getGradient2()
   {
      return grad2;
   }

   public String toString()
   {
      return String.format(
       "PathCoord[p=%s,type=%d,bend=%f,gradient1=(%f,%f),gradient2=(%f,%f)]",
       p, type, bend, grad1.getX(), grad1.getY(), grad2.getX(), grad2.getY());
   }

   private double bend;
   private int type;
   private Point2D p, grad1, grad2;
}

class LineFit
{
   public LineFit(double delta, int idx1, int idx2, Point2D p1, Point2D p2)
   {
      this.delta = delta;
      this.idx1 = idx1;
      this.idx2 = idx2;
      this.p1 = p1;
      this.p2 = p2;
   }

   public String toString()
   {
      return String.format("LineFit[delta=%f,idx1=%d,idx2=%d,p1=(%f,%f),p2=(%f,%f)]",
        delta, idx1, idx2, p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }

   protected int idx1, idx2;
   protected double delta;
   protected Point2D p1, p2;
}

class LineifyResults
{
   public LineifyResults(ShapeComponentVector bestPath, double minAverageDelta,
      int bestOffset, LineFit[] bestLineFit, int n1, int n2, double variance,
      double minDelta)
   {
      this.bestPath = bestPath;
      this.minAverageDelta = minAverageDelta;
      this.bestOffset = bestOffset;
      this.bestLineFit = bestLineFit;
      this.n1 = n1;
      this.n2 = n2;
      this.variance = variance;
      this.minDelta = minDelta;
   }

   public String toString()
   {
      return String.format("LineifyResults[offset: %d, average delta: %f, path: %s]",
         bestOffset, minAverageDelta, bestPath);
   }

   protected int bestOffset, n1, n2;
   protected ShapeComponentVector bestPath;
   protected double minAverageDelta, variance, minDelta;
   protected LineFit[] bestLineFit;
}

class Smooth extends SwingWorker<Void,Rectangle>
{
   public Smooth(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapes,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapes = shapes;
      this.continueToNextStep = continueToNextStep;
      tinyStepThreshold = dialog.getSmoothingTinyStepThreshold();
      lengthThreshold = dialog.getLengthThreshold();
      thresholdDiff = dialog.getThresholdDiff();
      curveThresholdDiff = dialog.getCurveThresholdDiff();
      tryBezier = dialog.isTryBezierOn();
      bezierGradientThreshold = dialog.getCurveGradientThreshold();
      minBezierSamples = dialog.getCurveMinPoints();
      maxDeviation = dialog.getSmoothingMaxDeviation();
      deviationEpsilon = dialog.getSmoothingDeviationEpsilon();
      curveStatPtThreshold = dialog.getCurveStationaryPtThreshold();
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      progress = 0;
      maxProgress = 0;

      for (int i = 0; i < shapes.size(); i++)
      {
         maxProgress += shapes.get(i).size()-1;
      }

      if (maxProgress < 0)
      {// shouldn't happen
         return null;
      }

      for (int i = 0; i < shapes.size(); i++)
      {
         updateAndSleep();

         ShapeComponentVector orgShape = shapes.get(i);

         int p = progress;
         int n = orgShape.size() - 1;

         ShapeComponentVector shape = smoothShape(orgShape);

         if (shape != null)
         {
            shapes.set(i, shape);
            publish(shape.getBounds());
         }

         progress = p + n;
         setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
      }

      return null;
   }

   protected void process(java.util.List<Rectangle> list)
   {
      Iterator<Rectangle> iter = list.iterator();

      while (iter.hasNext())
      {
          Rectangle elem = iter.next();

          dialog.repaintImagePanel(elem);
      }
   }

   private void updateAndSleep() throws InterruptedException
   {
      dialog.updateTimeElapsed();
      Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

      // check for cancel
      if (dialog.isCancelled())
      {
         throw new UserCancelledException(dialog.getMessageDictionary());
      }
   }

   private void incProgress() throws InterruptedException
   {
      incProgress(1);
   }

   private void incProgress(int inc) throws InterruptedException
   {
      progress += inc;
      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
      updateAndSleep();
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedSmoothing(shapes, continueToNextStep);
   }

   private ShapeComponentVector smoothShape(ShapeComponentVector vec)
     throws InterruptedException
   {
      ShapeComponentVector path = null;
      JDRResources resources = dialog.getResources();

      Point2D startPt = vec.firstElement().getEnd();

      if (vec.isFilled())
      {
         dialog.addMessageIdLn("vectorize.smoothing_region",
          startPt.getX(), startPt.getY());
      }
      else if (vec.isClosed())
      {
         dialog.addMessageIdLn("vectorize.smoothing_loop",
          startPt.getX(), startPt.getY());
      }
      else
      {
         dialog.addMessageIdLn("vectorize.smoothing_path",
          startPt.getX(), startPt.getY());
      }

      double sqTinyStepThreshold = tinyStepThreshold*tinyStepThreshold;

      for (int i = 1, n = vec.size(), prevI = 1; i < n; i++)
      {
         incProgress(i-prevI);
         prevI = i;

         int j = i;

         Point2D startRunPt = null;
         Point2D endRunPt = null;
         double sum = 0.0;
         int N = 0;

         int changeIdx = -1;
         int changeIdx2 = -1;
         Point2D changePt = null;
         Point2D changePt2 = null;

         int xLastDir = 0;
         int yLastDir = 0;

         for ( ; j < n; j++)
         {
            ShapeComponent comp = vec.get(j);

            int type = comp.getType();

            if (type != PathIterator.SEG_LINETO)
            {
               break;
            }

            Point2D p0 = (endRunPt == null ? comp.getStart() : endRunPt);
            Point2D p1 = comp.getEnd();

            double dx = p0.getX()-p1.getX();
            double dy = p0.getY()-p1.getY();

            double sqLength = dx*dx+dy*dy;

            if (sqLength > sqTinyStepThreshold)
            {
               break;
            }

            int xdir = (p0.getX() < p1.getX() ? -1 : p0.getX() > p1.getX() ? 1 : 0);
            int ydir = (p0.getY() < p1.getY() ? -1 : p0.getY() > p1.getY() ? 1 : 0);

            if (startRunPt == null)
            {
               startRunPt = p0;
               xLastDir = xdir;
               yLastDir = ydir;
            }
            else
            {
               if (xdir != 0)
               {
                  if (xdir != xLastDir && xLastDir != 0)
                  {
                     if (changeIdx == -1)
                     {
                        changeIdx = j-1;

                        if (!tryBezier)
                        {
                           break;
                        }
                     }
                     else if (changeIdx2 == -1)
                     {
                        changeIdx2 = j-1;
                     }
                     else
                     {
                        break;
                     }
                  }

                  xLastDir = xdir;
               }

               if (ydir != 0)
               {
                  if (ydir != yLastDir && yLastDir != 0)
                  {
                     if (changeIdx == -1)
                     {
                        changeIdx = j-1;

                        if (!tryBezier)
                        {
                           break;
                        }
                     }
                     else if (changeIdx2 == -1)
                     {
                        changeIdx2 = j-1;
                     }
                     else
                     {
                        break;
                     }
                  }

                  yLastDir = ydir;
               }
            }

            endRunPt = p1;

            sum += Math.sqrt(sqLength);
            N++;
         }

         int endIdx = j-1;

         if (startRunPt != null && endRunPt != null && endIdx - i > 2)
         {
            dialog.addMessageIdLn("vectorize.smoothing_tiny_steps_run",
             startRunPt.getX(), startRunPt.getY(), 
             endRunPt.getX(), endRunPt.getY(),
             sum/N);

            DeviationResult line1Result = null;
            DeviationResult line2Result = null;

            Point2D stat1 = null;
            Point2D stat2 = null;
            int statIdx1 = changeIdx;
            int statIdx2 = changeIdx2;

            if (changeIdx != -1)
            {
               changePt = vec.get(changeIdx).getMid();
               stat1 = changePt;

               line1Result = DeviationResult.getLineDeviation(
                 startRunPt, changePt, vec, i, changeIdx);

               if (changeIdx2 != -1)
               {
                  changePt2 = vec.get(changeIdx2).getMid();
                  stat2 = changePt2;

                  dialog.addMessageIdLn("vectorize.smoothing_bends_found",
                     changePt.getX(), changePt.getY(),
                     changePt2.getX(), changePt2.getY());

                  line2Result = DeviationResult.getLineDeviation(
                    changePt, changePt2, vec, changeIdx, changeIdx2);
               }
               else
               {
                  dialog.addMessageIdLn("vectorize.smoothing_bend_found",
                     changePt.getX(), changePt.getY());

                  line2Result = DeviationResult.getLineDeviation(
                    changePt, endRunPt, vec, changeIdx, endIdx);

                  changeIdx2 = endIdx;
                  changePt2 = endRunPt;
               }

               dialog.addMessageLn(line1Result.info(resources));

               if (line2Result != null)
               {
                  dialog.addMessageLn(line2Result.info(resources));

                  if (line2Result.getMaxDistance() > maxDeviation)
                  {
                     line2Result = null;

                     if (line1Result.getMaxDistance() > maxDeviation)
                     {
                        line1Result = null;
                     }
                  }
                  else if (line1Result.getMaxDistance() > maxDeviation)
                  {
                     line1Result = null;
                     line2Result = null;
                  }
               }
               else if (line1Result.getMaxDistance() > maxDeviation)
               {
                  line1Result = null;
               }
            }
            else
            {
               line1Result = DeviationResult.getLineDeviation(
                 startRunPt, endRunPt, vec, i, endIdx);
               changeIdx = endIdx;

               dialog.addMessageLn(line1Result.info(resources));

               if (line1Result.getMaxDistance() > maxDeviation)
               {
                  line1Result = null;
               }
            }

            DeviationResult bestResult = getBestComponent(vec, i, endIdx,
             statIdx1, stat1, statIdx2, stat2);

            if (bestResult != null
             && (line1Result != null && line1Result.compareTo(bestResult) < 0)
             && (line2Result != null && line2Result.compareTo(bestResult) < 0)
               )
            {
               bestResult = null;
            }

            if (bestResult != null && line1Result != null
                && bestResult.isLine())
            {
               double delta = line1Result.getAverageDeviation();

               if (line2Result != null)
               {
                  delta = 0.5*(delta + line2Result.getAverageDeviation());
               }

               if (delta <= bestResult.getAverageDeviation()+deviationEpsilon)
               {
                  bestResult = null;
               }
            }

            if (bestResult != null)
            {
               if (path == null)
               {
                  path = vec.getSubPath(i-1);
               }

               ShapeComponent comp = bestResult.getComponent();
               path.addComponent(comp);

               dialog.addMessageIdLn("vectorize.smoothing_replacing",
                  vec.get(i).info(resources), 
                  vec.get(bestResult.getEndIndex()).info(resources),
                  comp.info(resources));

               i = bestResult.getEndIndex();
            }
            else if (line1Result != null)
            {
               if (path == null)
               {
                  path = vec.getSubPath(i-1);
               }

               ShapeComponent comp = line1Result.getComponent();
               path.addComponent(comp);

               if (line2Result == null)
               {
                  dialog.addMessageIdLn("vectorize.smoothing_replacing2",
                     vec.get(i).info(resources), 
                     vec.get(changeIdx).info(resources), 
                     comp);

                  i = changeIdx;
               }
               else
               {
                  ShapeComponent comp2 = line2Result.getComponent();
                  path.addComponent(comp2);

                  dialog.addMessageIdLn("vectorize.smoothing_replacing3",
                     vec.get(i).info(resources), 
                     vec.get(changeIdx2).info(resources), 
                     comp, comp2);

                  i = changeIdx2;
               }
            }
            else if (path != null)
            {
               ShapeComponent comp = vec.get(i);
               path.addComponent(comp);
            }
         }
         else if (path != null)
         {
            ShapeComponent comp = vec.get(i);
            path.addComponent(comp);
         }
      }

      return path;
   }

   private DeviationResult getBestComponent(ShapeComponentVector vec,
      int startIdx, int endIdx, int statIdx1, Point2D statPt1, 
      int statIdx2, Point2D statPt2)
   throws InterruptedException
   {
      ShapeComponent startComp = vec.get(startIdx);
      Point2D p0 = startComp.getStart();
      DeviationResult bestResult = null, bestSubThresholdResult = null;
      double minDelta = Double.MAX_VALUE;
      double minSubThresholdDelta = Double.MAX_VALUE;

      DeviationResult bestCurveResult = null, firstCurveResult=null;
      double minCurveDelta = Double.MAX_VALUE;

      for (int j = endIdx; j > startIdx+1; j--)
      {
         updateAndSleep();
         ShapeComponent endComp = vec.get(j);

         Point2D p1 = endComp.getEnd();

         DeviationResult result = DeviationResult.createLine(vec, startIdx, j, p0, p1);

         double delta = result.getAverageDeviation();

         if (delta < minDelta && result.getMaxDistance() <= maxDeviation)
         {
            if (result.getEstimatedLength() < lengthThreshold)
            {
               if (delta < minSubThresholdDelta)
               {
                  minSubThresholdDelta = delta;
                  bestSubThresholdResult = result;
               }
            }
            else
            {
               minDelta = delta;
               bestResult = result;
            }
         }

         if (minBezierSamples < (j-startIdx))
         {
            result = DeviationResult.createCurve(vec, startIdx, j, p0, p1, 
               maxDeviation);

            if (result != null)
            {
               if (firstCurveResult==null)
               {
                  firstCurveResult = result;
               }

               if (result.getAngle() > bezierGradientThreshold)
               {
                  delta = result.getAverageDeviation();

                  if (delta < minCurveDelta)
                  {
                     minCurveDelta = delta;
                     bestCurveResult = result;
                  }
               }
            }
         }
      }

      JDRResources resources = dialog.getResources();

      if (bestResult != null)
      {
         dialog.addMessageLn(bestResult.info(resources));
      }

      if (bestCurveResult != null)
      {
         double[] t = new double[2];
         Point2D[] Pt = new Point2D[2];
         double[] d1 = null;
         double[] d2 = null;

         if (bestCurveResult != firstCurveResult)
         {
            dialog.addMessageLn(firstCurveResult.info(resources));

            if (statPt1 != null)
            {
               d1 = firstCurveResult.getComponent()
                   .getStationaryDeviations(statPt1, statPt2, t, Pt);

               if (d1 != null)
               {
                  dialog.addMessageIdLn("vectorize.smoothing_stat_pt", 
                    t[0], Pt[0].getX(), Pt[0].getY(), 
                    statPt1.getX(), statPt1.getY(), d1[0]);

                  if (statPt2 != null && Pt[1] != null && d1.length > 0)
                  {
                     dialog.addMessageIdLn("vectorize.smoothing_stat_pt", 
                       t[1], Pt[1].getX(), Pt[1].getY(), 
                       statPt2.getX(), statPt2.getY(), d1[1]);
                  }
               }

               if (statIdx1 <= bestCurveResult.getEndIndex())
               {
                  d2 = bestCurveResult.getComponent()
                               .getStationaryDeviations(statPt1, statPt2, t, Pt);
               }
            }
         }

         dialog.addMessageLn(bestCurveResult.info(resources));

         if (statPt1 != null)
         {
            if (d2 == null)
            {
               dialog.addMessageIdLn("vectorize.smoothing_no_stat_pt");
            }
            else
            {
               dialog.addMessageIdLn("vectorize.smoothing_stat_pt", 
                 t[0], Pt[0].getX(), Pt[0].getY(), 
                 statPt1.getX(), statPt1.getY(), d2[0]);

               if (statPt2 != null && Pt[1] != null && d2.length > 0)
               {
                  dialog.addMessageIdLn("vectorize.smoothing_stat_pt", 
                    t[1], Pt[1].getX(), Pt[1].getY(), 
                    statPt2.getX(), statPt2.getY(), d2[1]);
               }
            }
         }

         if (bestCurveResult != firstCurveResult)
         {
            if (d1 != null)
            {
               if (d1[0] < curveStatPtThreshold &&
                   (d2 == null || d1[0] < d2[0])
                   &&
                    (
                       statPt2 == null
                    || (d1.length > 1 && 
                      d1[1] < curveStatPtThreshold &&
                       (d2 == null || d2.length < 2 || d1[1] < d2[1]))
                    )
                  )
               {
                  bestCurveResult = firstCurveResult;
                  minCurveDelta = firstCurveResult.getAverageDeviation();
               }
            }
         }

         if (minCurveDelta < minDelta + curveThresholdDiff)
         {
            return bestCurveResult;
         }
      }

      if (bestSubThresholdResult != null)
      {
         dialog.addMessageLn(bestSubThresholdResult.info(resources));

         if (minDelta < minSubThresholdDelta+thresholdDiff)
         {
            return bestResult;
         }
         else
         {
            return bestSubThresholdResult;
         }
      }

      return bestResult;
   }

   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapes;
   private double tinyStepThreshold=2.0, lengthThreshold=10.0, thresholdDiff=0.01,
    maxDeviation=2.0, deviationEpsilon=0.01;
   private boolean tryBezier=true;
   private int minBezierSamples = 5;
   private double bezierGradientThreshold=0.1, curveStatPtThreshold=2.0,
     curveThresholdDiff;
   private int progress, maxProgress;
   private boolean continueToNextStep;
}

class DeviationResult implements Comparable<DeviationResult>
{
   private DeviationResult()
   {
   }

   public static DeviationResult createLine(ShapeComponentVector vec,
      int startIdx, int endIdx,
      Point2D p1, Point2D p2)
   {
      DeviationResult result = new DeviationResult();
      result.computeLine(vec, startIdx, endIdx, p1, p2);
      return result;
   }

   private void computeLine(ShapeComponentVector vec, int startIdx, int endIdx,
      Point2D p1, Point2D p2)
   {
      this.startIdx = startIdx;
      this.endIdx = endIdx;
      int numPoints = endIdx-startIdx;
      double[] t = new double[numPoints];

      this.p1 = p1;
      this.p2 = p2;

      double sumDist = 0.0;
      maxDist = 0.0;
      minDist = Double.MAX_VALUE;

      double sumSignumX = 0.0;
      double sumSignumY = 0.0;

      double diff_y = p1.getY()-p2.getY();
      double diff_x = p1.getX()-p2.getX();

      double m = diff_y/diff_x;

      double m_sq = 0.0;
      double orthog_m=0.0;
      double orthog_m_sq = 0.0;
      boolean use_orthog = false;
      double factor;

      if (Double.isNaN(m))
      {
         use_orthog = true;
         orthog_m = -diff_x/diff_y;
         orthog_m_sq = orthog_m*orthog_m;
         factor = 1.0/(1.0 + orthog_m_sq);
      }
      else
      {
         m_sq = m*m;
         factor = 1.0/(1.0 + m_sq);
      }

      double t_factor = -1.0/(diff_x+diff_y);

      double a1 = 0.0, a2 = 0.0, a3 = 0.0;
      double b1 = 0.0, b2 = 0.0, b3 = 0.0, b4 = 0.0;

      for (int i = startIdx+1, j=0; i < endIdx; i++, j++)
      {
         ShapeComponent comp = vec.get(i);
         Point2D p = comp.getEnd();

         // closest point on the line
         double x, y;

         if (use_orthog)
         {
            x = (p1.getX()+orthog_m*(p1.getY()-p.getY())+orthog_m_sq*p.getX())*factor;
            y = orthog_m * (x - p.getX()) + p.getY();
         }
         else
         {
            x = (m_sq*p1.getX()+m*(p.getY()-p1.getY())+p.getX())*factor;
            y = m * (x - p1.getX()) + p1.getY();
         }
         
         double dx = p.getX() - x;
         double dy = p.getY() - y;

         double dist = Math.sqrt(dx*dx + dy*dy);

         sumDist += dist;

         if (dist > maxDist)
         {
            maxDist = dist;
         }

         if (dist < minDist)
         {
            minDist = dist;
         }
      }

      double[] coords = new double[6];

      averageDeviation = sumDist/numPoints;
      coords[0] = p2.getX();
      coords[1] = p2.getY();
      component = new ShapeComponent(PathIterator.SEG_LINETO, coords, p1);
      length = component.getDiagonalLength();
   }

   public static DeviationResult createCurve(ShapeComponentVector vec,
      int startIdx, int endIdx,
      Point2D p1, Point2D p2, double maxDeviation)
   {
      DeviationResult result = new DeviationResult();
      result.computeCurve(vec, startIdx, endIdx, p1, p2, maxDeviation);
      return result;
   }

   private void computeCurve(ShapeComponentVector vec, int startIdx, int endIdx,
      Point2D p1, Point2D p2, double maxDeviation)
   {
      this.startIdx = startIdx;
      this.endIdx = endIdx;

      this.p1 = p1;
      this.p2 = p2;

      int offset = Math.max(2, (endIdx-startIdx+1)/3);

      Point2D dp1 = JDRLine.getGradient(p1, vec.get(startIdx+offset).getEnd());
      Point2D dp2 = JDRLine.getGradient(vec.get(endIdx-offset).getEnd(), p2);

      Point2D c1 = new Point2D.Double(dp1.getX()/3 + p1.getX(),
                      dp1.getY()/3 + p1.getY());

      Point2D c2 = new Point2D.Double(p2.getX() - dp2.getX()/3,
                      p2.getY() - dp2.getY()/3);

      double[] coords = new double[6];

      coords[0] = c1.getX();
      coords[1] = c1.getY();
      coords[2] = c2.getX();
      coords[3] = c2.getY();
      coords[4] = p2.getX();
      coords[5] = p2.getY();

      component = new ShapeComponent(PathIterator.SEG_CUBICTO, coords, p1);

      computeCurveDeviation(c1, c2, vec);

      if (averageDeviation > maxDeviation)
      {
         tuneGradients(vec);
      }

      angle = JDRLine.getVectorAngle(dp1.getX(), dp1.getY(), dp2.getX(), dp2.getY());
   }

   private void tuneGradients(ShapeComponentVector vec)
   {
      int iter = 0;

      iter = tuneGradients(vec, iter, true, true);
   }

   private int tuneGradients(ShapeComponentVector vec, int iter, 
     boolean stepC1, boolean stepC2)
   {
      if (iter >= MAX_ITER) return iter;

      iter++;

      double prevMaxDist = maxDist;
      double prevMinDist = minDist;
      double prevDeviation = averageDeviation;
      double prevLength = length;
      double[] coords = component.getCoords();

      double bestMaxDist = maxDist;
      double bestMinDist = minDist;
      double bestDeviation = averageDeviation;
      double bestLength = length;
      Point2D bestC1 = null;
      Point2D bestC2 = null;

      Point2D c1 = new Point2D.Double(coords[0], coords[1]);
      Point2D c2 = new Point2D.Double(coords[2], coords[3]);

      if (stepC1)
      {
         c1.setLocation(coords[0]+STEP_SIZE, coords[1]);

         computeCurveDeviation(c1, c2, vec);

         if (averageDeviation < bestDeviation)
         {
            bestMaxDist = maxDist;
            bestMinDist = minDist;
            bestDeviation = averageDeviation;
            bestLength = length;
            bestC1 = new Point2D.Double(c1.getX(), c1.getY());

            c1.setLocation(c1.getX(), coords[1]+STEP_SIZE);
         }
         else
         {
            c1.setLocation(coords[0]-STEP_SIZE, coords[1]);

            computeCurveDeviation(c1, c2, vec);

            if (averageDeviation < bestDeviation)
            {
               bestMaxDist = maxDist;
               bestMinDist = minDist;
               bestDeviation = averageDeviation;
               bestLength = length;
               bestC1 = new Point2D.Double(c1.getX(), c1.getY());

               c1.setLocation(c1.getX(), coords[1]+STEP_SIZE);
            }
            else
            {
               c1.setLocation(coords[0], coords[1]+STEP_SIZE);
            }
         }

         computeCurveDeviation(c1, c2, vec);

         if (averageDeviation < bestDeviation)
         {
            bestMaxDist = maxDist;
            bestMinDist = minDist;
            bestDeviation = averageDeviation;
            bestLength = length;

            if (bestC1 == null)
            {
               bestC1 = new Point2D.Double(c1.getX(), c1.getY());
            }
            else
            {
               bestC1.setLocation(c1.getX(), c1.getY());
            }
         }
         else
         {
            c1.setLocation(c1.getX(), coords[1]-STEP_SIZE);
            
            computeCurveDeviation(c1, c2, vec);

            if (averageDeviation < bestDeviation)
            {
               bestMaxDist = maxDist;
               bestMinDist = minDist;
               bestDeviation = averageDeviation;
               bestLength = length;

               if (bestC1 == null)
               {
                  bestC1 = new Point2D.Double(c1.getX(), c1.getY());
               }
               else
               {
                  bestC1.setLocation(c1.getX(), c1.getY());
               }
            }
            else
            {
               c1.setLocation(c1.getX(), coords[1]);
            }
         }
      }

      if (stepC2)
      {
         c2.setLocation(coords[2]+STEP_SIZE, coords[3]);

         computeCurveDeviation(c1, c2, vec);

         if (averageDeviation < bestDeviation)
         {
            bestMaxDist = maxDist;
            bestMinDist = minDist;
            bestDeviation = averageDeviation;
            bestLength = length;
            bestC2 = new Point2D.Double(c2.getX(), c2.getY());

            c2.setLocation(c2.getX(), coords[3]+STEP_SIZE);
         }
         else
         {
            c2.setLocation(coords[2]-STEP_SIZE, coords[3]);

            computeCurveDeviation(c1, c2, vec);

            if (averageDeviation < bestDeviation)
            {
               bestMaxDist = maxDist;
               bestMinDist = minDist;
               bestDeviation = averageDeviation;
               bestLength = length;
               bestC2 = new Point2D.Double(c2.getX(), c2.getY());

               c2.setLocation(c2.getX(), coords[3]+STEP_SIZE);
            }
            else
            {
               c2.setLocation(coords[2], coords[3]+STEP_SIZE);
            }
         }

         computeCurveDeviation(c1, c2, vec);

         if (averageDeviation < bestDeviation)
         {
            bestMaxDist = maxDist;
            bestMinDist = minDist;
            bestDeviation = averageDeviation;
            bestLength = length;

            if (bestC2 == null)
            {
               bestC2 = new Point2D.Double(c2.getX(), c2.getY());
            }
            else
            {
               bestC2.setLocation(c2.getX(), c2.getY());
            }
         }
         else
         {
            c2.setLocation(c2.getX(), coords[3]-STEP_SIZE);

            computeCurveDeviation(c1, c2, vec);

            if (averageDeviation < bestDeviation)
            {
               bestMaxDist = maxDist;
               bestMinDist = minDist;
               bestDeviation = averageDeviation;
               bestLength = length;

               if (bestC2 == null)
               {
                  bestC2 = new Point2D.Double(c2.getX(), c2.getY());
               }
               else
               {
                  bestC2.setLocation(c2.getX(), c2.getY());
               }
            }
            else
            {
               c2.setLocation(c2.getX(), coords[3]);
            }
         }
      }

      if (bestC1 == null && bestC2 == null)
      {
         maxDist = prevMaxDist;
         minDist = prevMinDist;
         averageDeviation = prevDeviation;
         length = prevLength;
      }
      else
      {
         if (bestC1 != null)
         {
            coords[0] = bestC1.getX();
            coords[1] = bestC1.getY();
         }

         if (bestC2 != null)
         {
            coords[2] = bestC2.getX();
            coords[3] = bestC2.getY();
         }

         iter = tuneGradients(vec, iter, bestC1 != null, bestC2 != null);
      }

      return iter;
   }

   private void computeCurveDeviation(Point2D c1, Point2D c2,
     ShapeComponentVector vec)
   {
      maxDist = 0.0;
      minDist = Double.MAX_VALUE;

      averageDeviation = 0.0;
      length = 0.0;
      double prevX = p1.getX();
      double prevY = p1.getY();

      double[] result = new double[2];
      Point2D resultP = new Point2D.Double();

      int n = endIdx-startIdx;

      double incT = Math.min(0.001, 1.0/(n+1));
      double startT = incT;

      for (int i = startIdx; i < endIdx; i++)
      {
         ShapeComponent comp = vec.get(i);
         Point2D p = comp.getEnd();

         JDRBezier.getClosest(p, p1, c1, c2, p2, startT, incT, resultP, result);

         startT = Math.min(1.0, result[0]);

         double dist = Math.sqrt(result[1]);
         averageDeviation += dist;

         if (dist < minDist)
         {
            minDist = dist;
         }

         if (dist > maxDist)
         {
            maxDist = dist;
         }

         length += Point2D.distance(prevX, prevY, resultP.getX(), resultP.getY());

         prevX = resultP.getX();
         prevY = resultP.getY();
      }

      length += Point2D.distance(prevX, prevY, p2.getX(), p2.getY());

      averageDeviation /= (n-1);
   }

   public double getAngle()
   {
      return angle;
   }

   public double getEstimatedLength()
   {
      return length;
   }

   public double getMaxDistance()
   {
      return maxDist;
   }

   public double getMinDistance()
   {
      return minDist;
   }

   public double getAverageDeviation()
   {
      return averageDeviation;
   }

   public ShapeComponent getComponent()
   {
      return component;
   }

   public boolean isLine()
   {
      return component != null && component.getType() == PathIterator.SEG_LINETO;
   }

   public int getStartIndex()
   {
      return startIdx;
   }

   public int getEndIndex()
   {
      return endIdx;
   }

   public static DeviationResult getLineDeviation(Point2D startPt, Point2D endPt,
     ShapeComponentVector vec, int startIdx, int endIdx)
   {
      DeviationResult result = new DeviationResult();

      double sum = 0.0;

      for (int i = startIdx; i <= endIdx; i++)
      {
         ShapeComponent comp = vec.get(i);
         Point2D p = comp.getEnd();
         Point2D q = JDRLine.getClosestPointAlongLine(startPt, endPt, p);

         if (p != q)
         {
            double dist = Math.sqrt(ShapeComponent.getSquareDistance(p, q));
            sum += dist;

            if (dist < result.minDist)
            {
               result.minDist = dist;
            }

            if (dist > result.maxDist)
            {
               result.maxDist = dist;
            }
         }
         else
         {
            result.minDist = 0.0;
         }
      }

      int n = endIdx-startIdx+1;

      result.averageDeviation = sum/n;

      result.component = new ShapeComponent(PathIterator.SEG_LINETO, 
        new double[] { endPt.getX(), endPt.getY() }, startPt);
      result.length = result.component.getDiagonalLength();

      result.startIdx = startIdx;
      result.endIdx = endIdx;

      result.p1 = startPt;
      result.p2 = endPt;

      return result;
   }

   public String toString()
   {
      return String.format("average deviation: %f, dist range: [%f, %f], length: %f, index range: [%d, %d], p1: (%f,%f), p2: (%f,%f), component: %s, angle: %f radians", 
        averageDeviation, minDist, maxDist, length, startIdx, endIdx,
        p1.getX(), p1.getY(), p2.getX(), p2.getY(), component, angle);
   }

   public String info(JDRResources resources)
   {
      String text = resources.getMessage("vectorize.smoothing_possible_path",
       component.info(resources), averageDeviation, minDist, maxDist, length);

      if (component.getType() == PathIterator.SEG_LINETO)
      {
         return text;
      }

      return String.format("%s %s", text, resources.getMessage(
        "vectorize.smoothing_angle", angle, Math.toDegrees(angle)));
   }

   public int compareTo(DeviationResult other)
   {
      if (averageDeviation < other.averageDeviation
            && maxDist < other.maxDist)
      {
         return -1;
      }

      if (averageDeviation > other.averageDeviation
            && maxDist > other.maxDist)
      {
         return 1;
      }

      return 0;
   }

   private double averageDeviation=Double.MAX_VALUE;
   private Point2D p1, p2;
   private double maxDist = 0.0, minDist = Double.MAX_VALUE;
   private ShapeComponent component;
   private double length=0.0, angle=0.0;
   private int startIdx, endIdx;

   public static final int MAX_ITER = 100, STEP_SIZE=5;
}

class BestFitComponent
{
   public BestFitComponent(ShapeComponent comp)
   {
      component = comp;
   }

   public void set(ShapeComponent comp, double delta)
   {
      component = comp;
      this.delta = delta;
   }

   public void setComponent(ShapeComponent comp)
   {
      component = comp;
   }

   public void setDelta(double delta)
   {
      this.delta = delta;
   }

   public ShapeComponent getComponent()
   {
      return component;
   }

   public double getDelta()
   {
      return delta;
   }

   private ShapeComponent component=null;
   private double delta = Double.MAX_VALUE;
}

class RemoveTinyPaths extends SwingWorker<Void,Rectangle2D>
{
   public RemoveTinyPaths(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapes)
   {
      this.dialog = dialog;
      this.shapes = shapes;
      areaThreshold = dialog.getMaxTinyPaths();
   }

   protected Void doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (shapes == null || shapes.isEmpty())
      {
         return null;
      }

      int numShapes = shapes.size();
      progress = 0;
      maxProgress = numShapes;

      for (int i = numShapes-1; i >= 0; i--)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException(dialog.getMessageDictionary());
         }

         ShapeComponentVector vec = shapes.get(i);
         Rectangle2D bounds = vec.getBounds2D();

         if (bounds == null 
          || bounds.getWidth()*bounds.getHeight() < areaThreshold)
         {
            shapes.remove(i);
            publish(bounds);
         }

         incProgress();
      }

      return null;
   }

   protected void process(java.util.List<Rectangle2D> list)
   {
      Iterator<Rectangle2D> iter = list.iterator();

      while (iter.hasNext())
      {
         Rectangle2D bounds = iter.next();

         dialog.repaintImagePanel(bounds);
      }
   }

   private void incProgress()
   {
      progress++;
      setProgress((int)Math.min((100.0*progress)/maxProgress, 100));
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         get();
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
         return;
      }

      dialog.finishedRemoveTinyPaths(shapes);
   }

   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapes;
   private double areaThreshold;
   private int progress, maxProgress;
}

class ImagePanel extends JPanel implements MouseListener,MouseMotionListener
{
   public ImagePanel(VectorizeBitmapDialog dialog)
   {
      super();
      this.dialog = dialog;

      addMouseListener(this);
      addMouseMotionListener(this);
      setPreferredSize(DEFAULT_PREFERRED_SIZE);
   }

   public void setImage(BufferedImage image)
   {
      this.image = image;
      shapes = null;
      updateRegionBounds();
      updatePanel();
   }

   public void updatePanel()
   {
      double mag = getMagnification();

      if (image != null)
      {
         setPreferredSize(new Dimension(
          (int)Math.ceil(mag*image.getWidth(this)),
          (int)Math.ceil(mag*image.getHeight(this))));
      }
      else
      {
         setPreferredSize(DEFAULT_PREFERRED_SIZE);
      }

      revalidate();
      repaint();
   }

   public double getMagnification()
   {
      return dialog.getMainMagnification();
   }

   public double getResultMagnification()
   {
      return dialog.getResultMagnification();
   }

   public boolean isZoomLinked()
   {
      return dialog.isZoomLinked();
   }

   public Color getImageForeground()
   {
      return dialog.getImageForeground();
   }

   public Color getUnpinnedLineColor()
   {
      return dialog.getUnpinnedLineColor();
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      if (image == null) return;

      double mag = getMagnification();

      Graphics2D g2 = (Graphics2D)g;

      AffineTransform af = AffineTransform.getScaleInstance(mag, mag);

      g2.drawImage(image, af, this);

      if (shapes != null)
      {
         for (int i = 0; i < shapes.size(); i++)
         {
            Shape shape = shapes.get(i);
            drawShape(g2, af, shape);
         }
      }

      if (notRegion != null)
      {
         g2.setColor(dialog.getNotRegionColor());

         if (mag == 1.0)
         {
            g2.fill(notRegion);
         }
         else
         {
            g2.fill(af.createTransformedShape(notRegion));
         }
      }

      if (draggingRegion != null)
      {
         g2.setColor(dialog.getDragColor());
         g2.setStroke(DRAG_STROKE);
         g2.draw(draggingRegion);
      }

      if (workingShape != null)
      {
         drawShape(g2, af, workingShape);
      }
   }

   private void drawShape(Graphics2D g2, AffineTransform af, Shape shape)
   {
      Stroke stroke = g2.getStroke();

      Color connectorCol = dialog.getConnectorColor();

      g2.setColor(getUnpinnedLineColor());
      g2.draw(af.createTransformedShape(shape));

      PathIterator pi = shape.getPathIterator(null);
      double[] orgCoords = new double[6];
      double[] coords = new double[6];
      double[] prev = null;

      for (int i = 0; i < orgCoords.length; i++)
      {
         orgCoords[i] = 0.0;
      }

      double controlSize = dialog.getControlSize();
      double halfControlSize = 0.5*controlSize;
      Color controlColor = dialog.getControlColor();

      while (!pi.isDone())
      {
         int current = pi.currentSegment(orgCoords);

         af.transform(orgCoords, 0, coords, 0, orgCoords.length/2);

         switch (current)
         {
            case PathIterator.SEG_MOVETO:
              if (prev != null)
              {
                 g2.setColor(connectorCol);
                 g2.setStroke(CONNECT_STROKE);

                 g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

                 g2.setStroke(stroke);
              }
            case PathIterator.SEG_LINETO:
              g2.setColor(controlColor);

              g2.drawRect((int)Math.round(coords[0]-halfControlSize),
                          (int)Math.round(coords[1]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));
              if (prev == null)
              {
                 prev = new double[2];
              }

              prev[0] = coords[0];
              prev[1] = coords[1];
            break;
            case PathIterator.SEG_QUADTO:
              g2.setColor(connectorCol);
              g2.setStroke(CONNECT_STROKE);

              g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

              g2.drawLine((int)Math.round(coords[0]), (int)Math.round(coords[1]),
                  (int)Math.round(coords[2]), (int)Math.round(coords[3]));

              g2.setStroke(stroke);
              g2.setColor(controlColor);

              g2.drawRect((int)Math.round(coords[0]-halfControlSize),
                          (int)Math.round(coords[1]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));

              g2.drawRect((int)Math.round(coords[2]-halfControlSize),
                          (int)Math.round(coords[3]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));

              prev[0] = coords[2];
              prev[1] = coords[3];
            break;
            case PathIterator.SEG_CUBICTO:
              g2.setColor(connectorCol);
              g2.setStroke(CONNECT_STROKE);

              g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

              g2.drawLine((int)Math.round(coords[0]), (int)Math.round(coords[1]),
                  (int)Math.round(coords[2]), (int)Math.round(coords[3]));

              g2.drawLine((int)Math.round(coords[2]), (int)Math.round(coords[3]),
                  (int)Math.round(coords[4]), (int)Math.round(coords[5]));

              g2.setStroke(stroke);
              g2.setColor(controlColor);

              g2.drawRect((int)Math.round(coords[0]-halfControlSize),
                          (int)Math.round(coords[1]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));

              g2.drawRect((int)Math.round(coords[2]-halfControlSize),
                          (int)Math.round(coords[3]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));

              g2.drawRect((int)Math.round(coords[4]-halfControlSize),
                          (int)Math.round(coords[5]-halfControlSize),
                         (int)Math.ceil(controlSize), (int)Math.ceil(controlSize));

              prev[0] = coords[4];
              prev[1] = coords[5];
            break;
         }

         pi.next();
      }
   }

   public BufferedImage getImage()
   {
      return image;
   }

   public void setWorkingShape(Shape shape)
   {
      this.workingShape = shape;

      if (workingShape != null)
      {
         repaint(workingShape.getBounds());
      }
   }

   public void setShapes(Vector<Shape> shapes)
   {
      this.shapes = shapes;

      repaint();
   }

   public void setShapeList(Vector<ShapeComponentVector> shapeList)
   {
      if (shapeList == null)
      {
         shapes = null;
      }
      else
      {
         shapes = new Vector<Shape>(shapeList.size());

         for (ShapeComponentVector shapeVec : shapeList)
         {
            shapes.add(shapeVec.getPath());
         }
      }

      repaint();
   }

   public void updateCoords(MouseEvent evt)
   {
      double factor = 1.0/getMagnification();

      Point p = evt.getPoint();

      int x = (int)Math.round(factor*p.getX());
      int y = (int)Math.round(factor*p.getY());

      dialog.setCoords(x, y);
   }

   public void mouseClicked(MouseEvent evt)
   {
      if (dialog.isColourPickerOn() && evt.getClickCount() == 1
          && evt.getButton() == MouseEvent.BUTTON1)
      {
         Point p = evt.getPoint();

         double factor = 1.0/getMagnification();

         int x = (int)Math.round(factor*p.getX());
         int y = (int)Math.round(factor*p.getY());

         if (x < image.getWidth() && y < image.getHeight())
         {
            int rgb = image.getRGB(x, y);
            dialog.colourPickerChoice(new Color(rgb));
         }
      }
   }

   public void mouseEntered(MouseEvent evt)
   {
      updateCoords(evt);
   }

   public void mouseExited(MouseEvent evt)
   {
   }

   public void mousePressed(MouseEvent evt)
   {
      if (dialog.isRegionPickerOn() && evt.getButton() == MouseEvent.BUTTON1)
      {
         dragStart = evt.getPoint();
      }
      else
      {
         dragStart = null;
      }

      draggingRegion = null;
   }

   public void mouseReleased(MouseEvent evt)
   {
      if (dialog.isRegionPickerOn() && dragStart != null)
      {
         Point p = evt.getPoint();

         int x, y, width, height;

         if (dragStart.x < p.x)
         {
            x = dragStart.x;
            width = p.x - x;
         }
         else
         {
            x = p.x;
            width = dragStart.x - x;
         }

         if (dragStart.y < p.y)
         {
            y = dragStart.y;
            height = p.y - y;
         }
         else
         {
            y = p.y;
            height = dragStart.y - y;
         }

         double factor = 1.0/getMagnification();

         double scaledX = factor*x;
         double scaledY = factor*y;
         double scaledWidth = factor*width;
         double scaledHeight = factor*height;

         int imgWidth = image.getWidth();
         int imgHeight = image.getHeight();

         if (scaledX < imgWidth && scaledY < imgHeight)
         {
            double x1 = scaledX + scaledWidth;
            double y1 = scaledY + scaledHeight;

            if (x1 > imgWidth)
            {
               scaledWidth -= (x1-imgWidth);
            }

            if (y1 > imgHeight)
            {
               scaledHeight -= (y1-imgHeight);
            }

            Rectangle2D.Double rect = new Rectangle2D.Double(scaledX, scaledY, 
              scaledWidth, scaledHeight);

            int action = ScanImagePanel.REGION_PICKER_SET;
            int modifier = (evt.getModifiersEx() & 
                  (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK));

            if (modifier == MouseEvent.SHIFT_DOWN_MASK)
            {
               action = ScanImagePanel.REGION_PICKER_ADD;
            }
            else if (modifier == MouseEvent.CTRL_DOWN_MASK)
            {
               action = ScanImagePanel.REGION_PICKER_SUBTRACT;
            }

            dialog.regionPickerChoice(rect, action);

            Area region = dialog.getRegion();

            if (region == null)
            {
               notRegion = null;
            }
            else
            {
               notRegion = new Area(new Rectangle2D.Double(0, 0, imgWidth, imgHeight));
               notRegion.subtract(region);
            }

            updateRegionBounds();

            repaint();
         }
      }

      draggingRegion = null;
      dragStart = null;
   }

   public Rectangle2D getRegionBounds()
   {
      return regionBounds;
   }

   private void updateRegionBounds()
   {
      if (image == null)
      {
         regionBounds = null;
         return;
      }

      regionBounds = new Rectangle2D.Double(0, 0, 
         image.getWidth(this), image.getHeight(this));

      if (notRegion != null)
      {
         Area area = new Area(regionBounds);
         area.subtract(notRegion);

         if (!area.isEmpty())
         {
            regionBounds = area.getBounds2D();
         }
      }
   }

   public void clearRegion()
   {
      oldNotRegion = notRegion;
      oldRegionBounds = regionBounds;

      notRegion = null;
      updateRegionBounds();
      repaint();
   }

   public void restoreOldRegion()
   {
      notRegion = oldNotRegion;
      regionBounds = oldRegionBounds;
      repaint();
   }

   public void subtractRegion(Area area)
   {
      if (notRegion == null)
      {
         notRegion = area;
      }
      else
      {
         notRegion.add(area);
      }

      updateRegionBounds();
      repaint();
   }

   public Area getNotRegion()
   {
      return notRegion;
   }

   public void mouseDragged(MouseEvent evt)
   {
      updateCoords(evt);

      if (dragStart != null)
      {
         Point p = evt.getPoint();

         if (draggingRegion != null)
         {
            draggingRegion.width++;
            draggingRegion.height++;
            repaint(draggingRegion);
         }

         int x, y, width, height;

         if (dragStart.x < p.x)
         {
            x = dragStart.x;
            width = p.x - x;
         }
         else
         {
            x = p.x;
            width = dragStart.x - x;
         }

         if (dragStart.y < p.y)
         {
            y = dragStart.y;
            height = p.y - y;
         }
         else
         {
            y = p.y;
            height = dragStart.y - y;
         }

         draggingRegion = new Rectangle(x, y, width+1, height+1);

         repaint(draggingRegion);

         draggingRegion.width=width;
         draggingRegion.height=height;
      }
   }

   public void mouseMoved(MouseEvent evt)
   {
      updateCoords(evt);
   }

   public void clearAllShapes()
   {
      workingShape = null;
      shapes = null;
      repaint();
   }

   private VectorizeBitmapDialog dialog;
   private BufferedImage image;
   private Shape workingShape = null;
   private Vector<Shape> shapes;
   private Point dragStart = null;
   private Rectangle draggingRegion = null;
   private Area notRegion=null, oldNotRegion;
   private Rectangle2D regionBounds, oldRegionBounds;

   private static Stroke CONNECT_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
     BasicStroke.JOIN_BEVEL, 10.0f, new float[] {4.0f, 2.0f}, 0.0f);

   private static Stroke DRAG_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
     BasicStroke.JOIN_BEVEL, 10.0f, new float[] {2.0f, 2.0f}, 0.0f);

   public static Dimension DEFAULT_PREFERRED_SIZE = new Dimension(400,400);
}

class Result
{
   public Result(ShapeComponentVector shapeVec, Color colour)
   {
      foreground = colour;
      isFilled = shapeVec.isFilled();
      shape = shapeVec.getPath();
      stroke = new BasicStroke((float)shapeVec.getLineWidth(), BasicStroke.CAP_BUTT,
       BasicStroke.JOIN_MITER);
   }

   public void paint(Graphics2D g2)
   {
      g2.setColor(foreground);

      if (isFilled)
      {
         g2.fill(shape);
      }
      else
      {
         Stroke orgStroke = g2.getStroke();
         g2.setStroke(stroke);
         g2.draw(shape);
         g2.setStroke(orgStroke);
      }
   }

   public JDRPath getJDRPath(CanvasGraphics cg)
     throws MissingMoveException,EmptyPathException
   {
      PathIterator pi = shape.getPathIterator(null);

      JDRPath path = JDRPath.getPath(cg, pi);
    
      if (isFilled)
      {
         path.setFillPaint(new JDRColor(cg, foreground));
         path.setLinePaint(null);
      }
      else
      {
         path.setLinePaint(new JDRColor(cg, foreground));
         path.setFillPaint(null);

         path.setStroke(new JDRBasicStroke(cg, stroke.getLineWidth(),
           stroke.getEndCap(), stroke.getLineJoin()));
      }

      return path;
   }

   private Color foreground;
   private boolean isFilled;
   private Shape shape;
   private BasicStroke stroke;
}

class ResultPanel extends JPanel
{
   public ResultPanel(ImagePanel imagePanel)
   {
      super();
      this.imagePanel = imagePanel;
      resultList = new Vector<Result>();
      setBackground(Color.WHITE);
      setPreferredSize(ImagePanel.DEFAULT_PREFERRED_SIZE);
   }

   public void updatePanel()
   {
      if (imagePanel == null || resultList.isEmpty())
      {
         setPreferredSize(ImagePanel.DEFAULT_PREFERRED_SIZE);
      }
      else if (imagePanel.isZoomLinked())
      {
         setPreferredSize(imagePanel.getPreferredSize());
      }
      else
      {
         double mag = imagePanel.getResultMagnification();

         BufferedImage image = imagePanel.getImage();

         setPreferredSize(new Dimension(
          (int)Math.ceil(mag*image.getWidth(this)),
          (int)Math.ceil(mag*image.getHeight(this))));
      }

      revalidate();
      repaint();
   }

   public boolean hasResults()
   {
      return !resultList.isEmpty();
   }

   public Vector<Result> getResults()
   {
      return resultList;
   }

   public void newImage()
   {
      resultList.clear();
      currentShapeList = null;
      repaint();
   }

   public void removeResult(Result result)
   {
      resultList.remove(result);
   }

   public void addResult(Result result)
   {
      resultList.add(result);
   }

   public Result storeShape(ShapeComponentVector shape)
   {
      Color colour = imagePanel.getImageForeground();
      Result result = null;

      if (!shape.isEmpty())
      {
         result = new Result(shape, colour);
         resultList.add(result);
      }

      return result;
   }

   public void storeCurrentResults()
   {
      if (currentShapeList != null)
      {
         Color colour = imagePanel.getImageForeground();

         for (ShapeComponentVector shape : currentShapeList)
         {
            if (!shape.isEmpty())
            {
               resultList.add(new Result(shape, colour));
            }
         }

         currentShapeList = null;

         repaint();
      }
   }

   public void updateCurrentShapeList(Vector<ShapeComponentVector> newShapeList)
   {
      currentShapeList = newShapeList;
      repaint();
   }

   public Vector<ShapeComponentVector> getCurrentShapeList()
   {
      return currentShapeList;
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      Graphics2D g2 = (Graphics2D)g;

      RenderingHints oldHints = g2.getRenderingHints();
      AffineTransform oldAf = g2.getTransform();
      Stroke oldStroke = g2.getStroke();

      g2.setRenderingHints(RENDER_HINTS);

      Color lineCol = imagePanel.getUnpinnedLineColor();
      double mag = imagePanel.getResultMagnification();

      g2.scale(mag, mag);

      for (Result result : resultList)
      {
         result.paint(g2);
      }

      if (currentShapeList != null && !currentShapeList.isEmpty())
      {
         g2.setColor(lineCol);
         
         for (ShapeComponentVector shapeVec : currentShapeList)
         {
            if (!shapeVec.isEmpty())
            {
               Shape shape = shapeVec.getPath();

               if (shapeVec.isFilled())
               {
                  g2.setColor(CURRENT_FILL);
                  g2.fill(shape);
                  g2.setColor(lineCol);
               }

               g2.draw(shape);
            }
         }
      }

      g2.setTransform(oldAf);
      g2.setRenderingHints(oldHints);
      g2.setStroke(oldStroke);
   }

   private ImagePanel imagePanel;
   public static final RenderingHints RENDER_HINTS
      = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);

   private Vector<ShapeComponentVector> currentShapeList;
   private Vector<Result> resultList;

   public static final Color CURRENT_FILL=new Color(127,127,127,127);
}

class SummaryPanel extends JPanel
{
   public SummaryPanel(VectorizeBitmapDialog dialog)
   {
      super(new BorderLayout());
      setBackground(Color.WHITE);
      this.dialog = dialog;

      topField = new JTextField();
      topField.setEditable(false);
      topField.setOpaque(false);
      topField.setBorder(null);
      add(topField, "North");

      mainPanel = Box.createVerticalBox();
      mainPanel.setOpaque(false);

      add(mainPanel, "Center");
   }

   public void updateSummary(Vector<ShapeComponentVector> shapes)
   {
      mainPanel.removeAll();

      if (shapes == null || shapes.isEmpty())
      {
         topField.setText("");
      }
      else
      {
         for (int i = 0; i < shapes.size(); i++)
         {
            mainPanel.add(new SummaryPathPanel(dialog, i, shapes.get(i)));
         }

         JDRResources resources = dialog.getResources();

         topField.setText(resources.formatMessageChoice(shapes.size(),
           "vectorize.summary.paths"));
      }

      mainPanel.revalidate();
      repaint();
   }

   private JTextField topField;
   private JComponent mainPanel;
   private VectorizeBitmapDialog dialog;
}

class SummaryPathPanel extends JPanel implements ActionListener
{
   public SummaryPathPanel(VectorizeBitmapDialog dialog, int idx, ShapeComponentVector shape)
   {
      super(new BorderLayout());
      this.dialog = dialog;
      this.index = idx;
      setOpaque(false);
      setAlignmentY(Component.TOP_ALIGNMENT);

      JDRResources resources = dialog.getResources();

      String extraText = "";
      String extraText2 = "";

      if (shape.isEmpty())
      {
         extraText = resources.getString("vectorize.summary.empty");
      }
      else if (shape.lastElement().getType() == PathIterator.SEG_CLOSE)
      {
         extraText = resources.getMessage("vectorize.summary.closed",
          shape.getRule() == PathIterator.WIND_EVEN_ODD ? 
            resources.getString("linestyle.winding_rule.eo") : 
            resources.getString("linestyle.winding_rule.nz"));

         int numSubPaths = 0;

         for (int i = 1; i < shape.size(); i++)
         {
            if (shape.get(i).getType() == PathIterator.SEG_CLOSE
             && shape.get(i-1).getType() == PathIterator.SEG_MOVETO)
            {
               numSubPaths++;
            }
         }

         if (numSubPaths > 1)
         {
            extraText2 = resources.getMessage("vectorize.summary.subpaths", 
              numSubPaths);
         }

         if (!shape.isFilled())
         {
            if (extraText2.isEmpty())
            {
               extraText2 = resources.getMessage(
                 "vectorize.summary.line_width", shape.getLineWidth());
            }
            else
            {
               extraText2 += " " + resources.getMessage(
                 "vectorize.summary.line_width", shape.getLineWidth());
            }
         }
      }
      else
      {
         extraText = resources.getMessage(
           "vectorize.summary.line_width", shape.getLineWidth());
      }

      String text = resources.getMessage("vectorize.summary.path_n", 
       (idx+1), extraText, extraText2);

      JTextField topField = new JTextField(text);
      topField.setEditable(false);
      topField.setOpaque(false);
      topField.setBorder(null);
      topField.setAlignmentY(Component.TOP_ALIGNMENT);
      add(topField, "North");

      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < shape.size(); i++)
      {
         ShapeComponent comp = shape.get(i);

         if (i > 0)
         {
            builder.append(String.format("%n"));
         }

         builder.append(comp.toString());
      }

      JTextArea mainArea = resources.createAppInfoArea();
      mainArea.setText(builder.toString());
      mainArea.setAlignmentY(Component.TOP_ALIGNMENT);
      add(mainArea, "Center");

      Box buttonPanel = Box.createVerticalBox();
      buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
      add(buttonPanel, "West");

      buttonPanel.add(resources.createDialogButton(
        "vectorize.summary", "discard", this, null));
      buttonPanel.add(resources.createDialogButton(
        "vectorize.summary", "pin", this, null));

      Icon ic = createIcon(shape, text);

      if (ic != null)
      {
         JComponent iconComp = new IconPanel(ic);
         iconComp.setAlignmentY(Component.TOP_ALIGNMENT);
         add(iconComp, "East");
      }
   }

   private Icon createIcon(ShapeComponentVector shape, String description)
   {
      Rectangle2D bounds = dialog.getRegionBounds();

      if (bounds == null) return null;

      double width = bounds.getWidth();
      double height = bounds.getHeight();

      if (width <= 0 || height <= 0)
      {
         return null;
      }

      double factor;

      if (width > height)
      {
         factor = (double)ICON_SIZE/width;
      }
      else
      {
         factor = (double)ICON_SIZE/height;
      }

      BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, 
         BufferedImage.TYPE_4BYTE_ABGR);

      Graphics2D g2 = (Graphics2D)image.getGraphics();

      if (g2 == null) return null;

      g2.setPaint(dialog.getForeground());

      g2.translate(-factor*bounds.getX(), -factor*bounds.getY());
      g2.scale(factor, factor);
      g2.draw(shape.getPath());

      g2.dispose();

      return new ImageIcon(image, description);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null) return;

      if (command.equals("discard"))
      {
         dialog.deleteShape(index);
      }
      else if (command.equals("pin"))
      {
         dialog.storeShape(index);
      }
   }

   private VectorizeBitmapDialog dialog;
   private int index;
   public static final int ICON_SIZE=100;
}

class IconPanel extends JPanel
{
   public IconPanel(Icon ic)
   {
      super(null);
      this.ic = ic;

      Dimension dim = new Dimension(ic.getIconWidth(), ic.getIconHeight());
      setPreferredSize(dim);
      setOpaque(false);
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      ic.paintIcon(this, g, 0, 0);
   }

   private Icon ic;
}
