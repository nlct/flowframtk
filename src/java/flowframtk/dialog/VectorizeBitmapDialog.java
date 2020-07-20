// File          : VectorizeBitmapDialog.java
// Description   : Dialog box for vectorizing a bitmap
// Date          : 25th May 2011
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
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for scanning a bitmap.
 * @author Nicola L C Talbot
 */
public class VectorizeBitmapDialog extends JDialog
   implements ActionListener
{
   public VectorizeBitmapDialog(FlowframTk application)
   {
      super(application, application.getResources().getString("vectorize.title"),
            true);
      this.application = application;

      createAndShowGUI();
   }

   private void createAndShowGUI()
   {
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            cancel();
         }
      });

      undoManager = new UndoManager();

      JDRResources resources = getResources();

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

      JPanel topPanel = new JPanel(new BorderLayout());
      getContentPane().add(topPanel, "North");

      coordField = new JTextField(12);
      coordField.setEditable(false);
      topPanel.add(coordField, "West");

      timeElapsedField = new JTextField("00:00:00");
      timeElapsedField.setEditable(false);
      topPanel.add(timeElapsedField, "Center");

      zoomWidget = new ZoomWidget(this);
      topPanel.add(zoomWidget, "East");

      mainPanel = new ImagePanel(this);

      JTabbedPane resultTabbedPane = new JTabbedPane();

      resultPanel = new ResultPanel(mainPanel);
      resultTabbedPane.addTab(resources.getString("vectorize.results"), 
        new JScrollPane(resultPanel));

      summaryPanel = new SummaryPanel(this);
      resultTabbedPane.addTab(resources.getString("vectorize.summary"), 
         new JScrollPane(summaryPanel));

      JSplitPane imagePane = new JSplitPane(
         JSplitPane.VERTICAL_SPLIT, 
         new JScrollPane(mainPanel), 
         resultTabbedPane);

      imagePane.setResizeWeight(0.5);
      imagePane.setOneTouchExpandable(true);

      controlPanel = new ControlPanel(this);

      historyPanel = Box.createVerticalBox();
      historyGroup = new ButtonGroup();

      JTabbedPane sideTabbedPane = new JTabbedPane();
      sideTabbedPane.addTab(resources.getString("vectorize.controls"),
        controlPanel);

      sideTabbedPane.addTab(resources.getString("vectorize.history"),
         new JScrollPane(historyPanel));

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                       imagePane, sideTabbedPane);

      splitPane.setResizeWeight(0.6);
      splitPane.setOneTouchExpandable(true);

      getContentPane().add(splitPane, "Center");

      scanStatusBar = new ScanStatusBar(getResources());
      getContentPane().add(scanStatusBar, "South");
      scanStatusBar.setVisible(false);

      pack();
      setLocationRelativeTo(application);
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
   }

   private void updateUndoRedo()
   {
      updateUndoRedo(undoManager.getUndoPresentationName(),
        undoManager.getRedoPresentationName());

      controlPanel.updateWidgets(false, mainPanel.getImage() != null,
          shapeList != null);
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
      addUndoableEdit(shapes, name, null);
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
            updateUndoRedo();
         }
      }
      else
      {
         for (int i = currentHistoryIndex+1; i <= idx; i++)
         {
            undoManager.redo();
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

   public boolean hasResults()
   {
      return !resultPanel.getResults().isEmpty();
   }

   public boolean clearImage()
   {
      if (getCurrentShapeList() != null)
      {
         int result = JOptionPane.showConfirmDialog(this, 
           getResources().getString("vectorize.confirm_store_current"), 
           getResources().getString("process.confirm"),
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

      this.shapeList = null;
      setImage(null);
      resultPanel.newImage();
      summaryPanel.updateSummary(null);
      undoManager.discardAllEdits();
      historyPanel.removeAll();
      currentHistoryIndex = 0;
      UndoableEditComp editComp= new UndoableEditComp(this);
      editComp.setSelected(true);
      historyGroup.add(editComp);
      historyPanel.add(editComp);

      return true;
   }

   public Vector<ShapeComponentVector> getCurrentShapeList()
   {
      return resultPanel.getCurrentShapeList();
   }

   public void clearAllResults()
   {
      int result = JOptionPane.showConfirmDialog(this, 
        getResources().getString("vectorize.confirm_clear_all"), 
        getResources().getString("process.confirm"),
         JOptionPane.YES_NO_CANCEL_OPTION);

      if (result == JOptionPane.YES_OPTION)
      {
         resultPanel.newImage();
      }
   }

   public void storeResults()
   {
      resultPanel.storeCurrentResults();
      undoManager.discardAllEdits();
   }

   public void error(Exception e)
   {
      error(e.getMessage());

      e.printStackTrace();
   }

   public void error(String msg)
   {
      getResources().error(this, msg);
   }

   public void display()
   {
      currentFrame = application.getCurrentFrame();
      JDRBitmap bitmap = currentFrame.getSelectedBitmap();

      resultPanel.newImage();
      clearImage();

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

      setVisible(true);
   }

   public void okay()
   {
      if (getCurrentShapeList() != null)
      {
         int result = JOptionPane.showConfirmDialog(this, 
           getResources().getString("vectorize.confirm_include current"), 
           getResources().getString("process.confirm"),
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

      if (n > 1)
      {
         currentFrame.addObject(group, 
            getResources().getString("undo.vectorize"));
      }
      else if (n == 1)
      {
         currentFrame.addObject(group.firstElement(), 
            getResources().getString("undo.vectorize"));
      }
   }

   public void cancel()
   {
      if (getCurrentShapeList() != null)
      {
         if (JOptionPane.showConfirmDialog(this, 
           getResources().getString("vectorize.confirm_discard_current"),
           getResources().getString("process.confirm"),
            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)

         {
            return;
         }
      }

      setVisible(false);
   }

   public void setImage(BufferedImage image)
   {
      mainPanel.setImage(image);
      revalidate();

      controlPanel.updateWidgets(false, image != null, shapeList != null);
   }

   public void updateImagePanel()
   {
      mainPanel.updatePanel();
      resultPanel.revalidate();
   }

   public void repaintImagePanel()
   {
      mainPanel.repaint();
      resultPanel.repaint();
   }

   public void repaintImagePanel(Rectangle bounds)
   {
      repaintImagePanel(bounds, true);
   }

   public void repaintImagePanel(Rectangle bounds, boolean repaintResults)
   {
      mainPanel.repaint(bounds);
      resultPanel.repaint(bounds);
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
   }

   public void storeShape(int i)
   {
      storeOldShapes();
      ShapeComponentVector shape = shapeList.remove(i);
      addUndoableEdit(shapeList, getResources().getString("vectorize.store_shape"), 
         resultPanel.storeShape(shape));
      resultPanel.updateCurrentShapeList(shapeList);
   }

   public void addResult(Result result)
   {
      resultPanel.addResult(result);
   }

   public void removeResult(Result result)
   {
      resultPanel.removeResult(result);
   }

   public void startTask(String info, SwingWorker task)
   {
      storeOldShapes();
      scanStatusBar.startTask(info, task);
      setHistoryPanelEnabled(false);
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
      controlPanel.taskFinished(mainPanel.getImage() != null, shapeList != null);
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
      controlPanel.updateWidgets(true, 
         mainPanel.getImage() != null, shapeList != null);

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

      if (getCurrentShapeList() != null)
      {
         int result = JOptionPane.showConfirmDialog(this, 
           getResources().getString("vectorize.confirm_store_current"),
           getResources().getString("process.confirm"),
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

   public void regionPickerChoice(Shape region, boolean add)
   {
      controlPanel.regionPickerChoice(region, add);
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

   public void setRegionPickerCursor(boolean on)
   {
      if (mainPanel != null)
      {
         if (on)
         {
            mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         }
         else
         {
            mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
      }
   }

   public double getMagnification()
   {
      return zoomWidget.getMagnification();
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

   public double getMinGap()
   {
      return controlPanel.getMinGap();
   }

   public double getMinSubPathGap()
   {
      return controlPanel.getMinSubPathGap();
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

   public double getLineDetectTinyStepThreshold()
   {
      return controlPanel.getLineDetectTinyStepThreshold();
   }

   public double getSmoothingTinyStepThreshold()
   {
      return controlPanel.getSmoothingTinyStepThreshold();
   }

   public double getLengthThreshold()
   {
      return controlPanel.getLengthThreshold();
   }

   public double getThresholdDiff()
   {
      return controlPanel.getThresholdDiff();
   }

   public double getMaxTinyPaths()
   {
      return controlPanel.getMaxTinyPaths();
   }

   public double getCurveGradientThreshold()
   {
      return controlPanel.getCurveGradientThreshold();
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
   private ResultPanel resultPanel;
   private SummaryPanel summaryPanel;

   private JComponent historyPanel;
   private ButtonGroup historyGroup;
   private int currentHistoryIndex=-1;

   private JFileChooser fileChooser;
   private JMenuItem undoItem, redoItem;

   private ScanStatusBar scanStatusBar;
   private ControlPanel controlPanel;
   private ZoomWidget zoomWidget;
   private JTextField coordField, timeElapsedField;

   private Cursor colourPickerCursor;

   private Vector<ShapeComponentVector> shapeList, oldShapeList;
   private UndoManager undoManager;
   private long startTime;

   public static final long SLEEP_DURATION=10;

   private FlowframTk application;
   private JDRFrame currentFrame;
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
   public ShapesUndoableEdit(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> oldShapes, 
     Vector<ShapeComponentVector> newShapes, String name, Result result)
   {
      this.dialog = dialog;
      this.name = name;
      this.newShapes = newShapes;
      this.oldShapes = oldShapes;
      this.result = result;

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

      if (result != null)
      {
         dialog.addResult(result);
      }

      dialog.updateShapes(newShapes);
   }

   public void undo() throws CannotUndoException
   {
      super.undo();

      if (result != null)
      {
         dialog.removeResult(result);
      }

      dialog.updateShapes(oldShapes);
   }

   private String name;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> oldShapes=null, newShapes;
   private Result result;

   private Icon icon, selectedIcon;

   public static final int ICON_SIZE=50, ICON_BORDER=2;
}

class ZoomWidget extends JPanel implements ChangeListener
{
   public ZoomWidget(VectorizeBitmapDialog dialog)
   {
      super();
      this.dialog = dialog;
      JDRResources resources = dialog.getResources();

      zoomSpinnerModel = new SpinnerNumberModel(100, 10, 1000, 5);

      JLabel zoomLabel = resources.createAppLabel("settings.zoom");
      add(zoomLabel);

      zoomSpinner = new JSpinner(zoomSpinnerModel);
      zoomLabel.setLabelFor(zoomSpinner);
      add(zoomSpinner);
      zoomSpinner.addChangeListener(this);
   }
  
   public void stateChanged(ChangeEvent e)
   {
      Object source = e.getSource();

      if (source == zoomSpinner)
      {
         dialog.updateImagePanel();
      }
   }

   public double getMagnification()
   {
      Number num = zoomSpinnerModel.getNumber();

      return 0.01*num.doubleValue();
   }

   private VectorizeBitmapDialog dialog;
   private SpinnerNumberModel zoomSpinnerModel; 
   private JSpinner zoomSpinner;
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

      ButtonGroup bg = new ButtonGroup();

      scanAllButton = resources.createAppRadioButton("vectorize.scan_all", 
         bg, true, this);
      subPanel.add(scanAllButton);

      scanRegionButton = resources.createAppRadioButton(
        "vectorize.scan_region", bg, false, this);
      subPanel.add(scanRegionButton);

      regionPicker = resources.createToggleButton("vectorize", "select_region", null);
      regionPicker.addChangeListener(this);
      subPanel.add(regionPicker);

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

         controlPanel.updateTaskButton();
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
            regionPicker.setEnabled(false);
            controlPanel.imagePanelClearRegion();
            region = null;
         }
         else
         {
            regionPicker.setEnabled(true);
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

   public void regionPickerChoice(Shape rect, boolean add)
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

      if (!add || region == null)
      {
         region = area;
      }
      else
      {
         region.add(area);
      }

      regionPicker.setSelected(false);
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
   }

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
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

      boolean enable = (!taskInProgress && imageLoaded) 
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

   public Color getImageForeground()
   {
      return imageForeground;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null)
      {
         return;
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

   private ControlPanel controlPanel;
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
      subPanel.add(gradientEpsilonSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      minGapSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 1.0);

      minGapLabel = resources.createAppLabel("vectorize.min_gap");
      subPanel.add(minGapLabel);

      minGapSpinner = controlPanel.createSpinner(
         minGapLabel, minGapSpinnerModel);
      subPanel.add(minGapSpinner);

      Dimension gradPrefSize = gradientEpsilonLabel.getPreferredSize();
      Dimension gapPrefSize = minGapLabel.getPreferredSize();

      if (gradPrefSize.width > gapPrefSize.width)
      {
         gapPrefSize.width = gradPrefSize.width;
         minGapLabel.setPreferredSize(gapPrefSize);
         minGapLabel.setMaximumSize(gapPrefSize);
      }
      else
      {
         gradPrefSize.width = gapPrefSize.width;
         gradientEpsilonLabel.setPreferredSize(gradPrefSize);
         gradientEpsilonLabel.setMaximumSize(gradPrefSize);
      }
   }

   public void stateChanged(ChangeEvent evt)
   {
      if (evt.getSource() == doOptimizeCheckBox)
      {
         boolean enable = doOptimizeCheckBox.isSelected();

         gradientEpsilonSpinner.setEnabled(enable);
         minGapSpinner.setEnabled(enable);
         gradientEpsilonLabel.setEnabled(enable);
         minGapLabel.setEnabled(enable);

         controlPanel.updateTaskButton();
      }
   }

   public void setSelected(boolean selected)
   {
      doOptimizeCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || (imageLoaded && controlPanel.isScanImageOn())));

      doOptimizeCheckBox.setEnabled(enable);

      enable = enable && doOptimizeCheckBox.isSelected();

      gradientEpsilonSpinner.setEnabled(enable);
      minGapSpinner.setEnabled(enable);
      gradientEpsilonLabel.setEnabled(enable);
      minGapLabel.setEnabled(enable);
   }

   public double getGradientEpsilon()
   {
      return gradientEpsilonSpinnerModel.getNumber().doubleValue();
   }

   public double getMinGap()
   {
      return minGapSpinnerModel.getNumber().doubleValue();
   }

   public boolean isOptimizeOn()
   {
      return doOptimizeCheckBox.isSelected();
   }

   private JLabel gradientEpsilonLabel, minGapLabel;

   private SpinnerNumberModel minGapSpinnerModel, gradientEpsilonSpinnerModel; 

   private JSpinner gradientEpsilonSpinner, minGapSpinner;

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

      minSubPathGapLabel = resources.createAppLabel(
        "vectorize.min_subpath_gap");
      subPanel.add(minSubPathGapLabel);

      minSubPathGapSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 1.0);

      minSubPathGapSpinner = controlPanel.createSpinner(
        minSubPathGapLabel, minSubPathGapSpinnerModel);
      subPanel.add(minSubPathGapSpinner);

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
      subPanel.add(minTinyAreaSpinner);

      minTinySizeLabel = resources.createAppLabel(
         "vectorize.tiny_subpath.size_max");
      subPanel.add(minTinySizeLabel);

      minTinySizeSpinnerModel = new SpinnerNumberModel(10, 0, 100, 1);

      minTinySizeSpinner = controlPanel.createSpinner(
         minTinySizeLabel, minTinySizeSpinnerModel);
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

         minSubPathGapSpinner.setEnabled(enable);
         minSubPathGapLabel.setEnabled(enable);

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

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || (imageLoaded && controlPanel.isScanImageOn())));

      doSplitSubPathsCheckBox.setEnabled(enable);

      enable = enable && doSplitSubPathsCheckBox.isSelected();

      minSubPathGapSpinner.setEnabled(enable);
      minSubPathGapLabel.setEnabled(enable);

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

   public double getMinSubPathGap()
   {
      return minSubPathGapSpinnerModel.getNumber().doubleValue();
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

   private JLabel minSubPathGapLabel, splitTypeLabel, minTinyAreaLabel,
     minTinySizeLabel;

   private SpinnerNumberModel minSubPathGapSpinnerModel,  
      minTinyAreaSpinnerModel, minTinySizeSpinnerModel;

   private JSpinner minSubPathGapSpinner, minTinyAreaSpinner, minTinySizeSpinner;

   private JCheckBox doSplitSubPathsCheckBox, removeTinyPathsCheckBox;

   private JRadioButton exteriorSplitOnlyButton, evenInteriorSplitButton, splitAllButton;

   private ControlPanel controlPanel;
}

class LineDetectionPanel extends JPanel implements ChangeListener
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

      deltaThresholdSpinnerModel = new SpinnerNumberModel(2.0, 0.0, 100.0, 0.5);

      deltaThresholdLabel = resources.createAppLabel("vectorize.delta_threshold");
      subPanel.add(deltaThresholdLabel);

      deltaThresholdSpinner = controlPanel.createSpinner(
         deltaThresholdLabel, deltaThresholdSpinnerModel);
      subPanel.add(deltaThresholdSpinner);

      detectIntersections = resources.createAppCheckBox(
         "vectorize.detect_intersections", true, this);
      add(detectIntersections);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      deltaVarianceThresholdSpinnerModel = new SpinnerNumberModel(16.0, 0.0, 1000.0, 1.0);

      deltaVarianceThresholdLabel = resources.createAppLabel(
         "vectorize.variance_threshold");
      subPanel.add(deltaVarianceThresholdLabel);

      deltaVarianceThresholdSpinner = controlPanel.createSpinner(
         deltaVarianceThresholdLabel, deltaVarianceThresholdSpinnerModel);
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
      subPanel.add(spikeReturnDistanceSpinner);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      tinyStepThresholdSpinnerModel = new SpinnerNumberModel(3.5, 1.0, 20.0, 0.5);

      tinyStepThresholdLabel = resources.createAppLabel(
         "vectorize.line_detect.tiny_step");
      subPanel.add(tinyStepThresholdLabel);

      tinyStepThresholdSpinner = controlPanel.createSpinner(
         tinyStepThresholdLabel, tinyStepThresholdSpinnerModel);
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

         detectIntersections.setEnabled(enable);

         enable = enable && detectIntersections.isSelected();

         deltaVarianceThresholdSpinner.setEnabled(enable);
         deltaVarianceThresholdLabel.setEnabled(enable);

         spikeReturnDistanceSpinner.setEnabled(enable);
         spikeReturnDistanceLabel.setEnabled(enable);

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

         tinyStepThresholdSpinner.setEnabled(enable);
         tinyStepThresholdLabel.setEnabled(enable);
      }
   }

   public void setSelected(boolean selected)
   {
      doLineDetectionCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || (imageLoaded && controlPanel.isScanImageOn())));

      doLineDetectionCheckBox.setEnabled(enable);

      enable = enable && doLineDetectionCheckBox.isSelected();

      deltaThresholdSpinner.setEnabled(enable);
      deltaThresholdLabel.setEnabled(enable);

      detectIntersections.setEnabled(enable);

      enable = enable && detectIntersections.isSelected();

      deltaVarianceThresholdSpinner.setEnabled(enable);
      deltaVarianceThresholdLabel.setEnabled(enable);

      spikeReturnDistanceSpinner.setEnabled(enable);
      spikeReturnDistanceLabel.setEnabled(enable);

      tinyStepThresholdSpinner.setEnabled(enable);
      tinyStepThresholdLabel.setEnabled(enable);
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
    tinyStepThresholdLabel, spikeReturnDistanceLabel;

   private SpinnerNumberModel deltaThresholdSpinnerModel,
      deltaVarianceThresholdSpinnerModel, tinyStepThresholdSpinnerModel,
      spikeReturnDistanceSpinnerModel;

   private JSpinner deltaThresholdSpinner, deltaVarianceThresholdSpinner,
    tinyStepThresholdSpinner, spikeReturnDistanceSpinner;

   private JCheckBox doLineDetectionCheckBox, detectIntersections;

   private ControlPanel controlPanel;
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

      tinyStepThresholdSpinnerModel = new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0);

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

      tryBezierCheckBox = resources.createAppCheckBox(
         "vectorize.smooth_shapes.try_bezier", true, this);
      add(tryBezierCheckBox);

      subPanel = Box.createHorizontalBox();
      subPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(subPanel);

      curveGradientThresholdSpinnerModel 
         = new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0);

      curveGradientThresholdLabel = resources.createAppLabel(
        "vectorize.smooth_shapes.curve_gradient_threshold");
      subPanel.add(curveGradientThresholdLabel);

      curveGradientThresholdSpinner = controlPanel.createSpinner(
         curveGradientThresholdLabel, curveGradientThresholdSpinnerModel);
      subPanel.add(curveGradientThresholdSpinner);

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

      prefSize = curveGradientThresholdLabel.getPreferredSize();

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

      prefSize = curveGradientThresholdLabel.getPreferredSize();
      prefSize.width = maxPrefSize.width;
      curveGradientThresholdLabel.setPreferredSize(prefSize);
      curveGradientThresholdLabel.setMaximumSize(prefSize);

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
         curveGradientThresholdSpinner.setEnabled(enable);
         curveMinPointsSpinner.setEnabled(enable);
         tinyStepThresholdLabel.setEnabled(enable);
         lengthThresholdLabel.setEnabled(enable);
         thresholdDiffLabel.setEnabled(enable);
         tryBezierCheckBox.setEnabled(enable);

         updateBezierWidgets();
      }
   }

   private void updateBezierWidgets()
   {
      boolean enable = doSmoothingCheckBox.isSelected() 
        && tryBezierCheckBox.isSelected();

      curveGradientThresholdLabel.setEnabled(enable);
      curveMinPointsLabel.setEnabled(enable);
   }

   public void setSelected(boolean selected)
   {
      doSmoothingCheckBox.setSelected(selected);
   }

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || (imageLoaded && controlPanel.isScanImageOn())));

      doSmoothingCheckBox.setEnabled(enable);

      enable = enable && doSmoothingCheckBox.isSelected();

      tinyStepThresholdSpinner.setEnabled(enable);
      lengthThresholdSpinner.setEnabled(enable);
      thresholdDiffSpinner.setEnabled(enable);
      curveGradientThresholdSpinner.setEnabled(enable);
      curveMinPointsSpinner.setEnabled(enable);
      tinyStepThresholdLabel.setEnabled(enable);
      lengthThresholdLabel.setEnabled(enable);
      thresholdDiffLabel.setEnabled(enable);

      tryBezierCheckBox.setEnabled(enable);

      enable = enable && tryBezierCheckBox.isSelected();

      curveGradientThresholdLabel.setEnabled(enable);
      curveMinPointsLabel.setEnabled(enable);
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

   public double getCurveGradientThreshold()
   {
      return curveGradientThresholdSpinnerModel.getNumber().doubleValue();
   }

   public int getCurveMinPoints()
   {
      return curveMinPointsSpinnerModel.getNumber().intValue();
   }

   private JLabel tinyStepThresholdLabel, lengthThresholdLabel, thresholdDiffLabel,
    curveGradientThresholdLabel, curveMinPointsLabel;

   private SpinnerNumberModel  
     tinyStepThresholdSpinnerModel, lengthThresholdSpinnerModel,
     thresholdDiffSpinnerModel,
     curveMinPointsSpinnerModel, curveGradientThresholdSpinnerModel;

   private JSpinner curveMinPointsSpinner, curveGradientThresholdSpinner,
     thresholdDiffSpinner, lengthThresholdSpinner, tinyStepThresholdSpinner;

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

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      boolean enable = (!taskInProgress && 
          (isVectorized || (imageLoaded && controlPanel.isScanImageOn())));

      doRemoveTinyPathsCheckBox.setEnabled(enable);

      enable = enable && doRemoveTinyPathsCheckBox.isSelected();

      maxTinyPathsSpinner.setEnabled(enable);
      maxTinyPathsLabel.setEnabled(enable);
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

      doTasksButton = resources.createAppJButton("vectorize", "dotasks", this);
      taskButtonPanel.add(doTasksButton);

      selectAllButton = resources.createAppJButton("vectorize", 
       "selectallitems", this);
      taskButtonPanel.add(selectAllButton);

      deselectAllButton = resources.createAppJButton("vectorize",
        "deselectallitems", this);
      taskButtonPanel.add(deselectAllButton);

      storeResultsButton = resources.createAppJButton("vectorize",
         "storeresults", this);
      taskButtonPanel.add(storeResultsButton);

      clearAllResultsButton = resources.createDialogButton("vectorize",
          "discard_all", this, null);
      taskButtonPanel.add(clearAllResultsButton);

      JComponent finishButtonPanel = new JPanel();
      buttonPanel.add(finishButtonPanel);

      okayButton = getResources().createOkayButton(this);
      finishButtonPanel.add(okayButton);

      cancelButton = getResources().createCancelButton(this);
      finishButtonPanel.add(cancelButton);

      updateWidgets(false, false, false);
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

   public void regionPickerChoice(Shape region, boolean add)
   {
      scanImagePanel.regionPickerChoice(region, add);
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

   public void taskFinished(boolean imageLoaded, boolean isVectorized)
   {
      updateWidgets(false, imageLoaded, isVectorized);
   }

   public void updateWidgets(boolean taskInProgress, boolean imageLoaded, 
      boolean isVectorized)
   {
      scanImagePanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);
      optimizeLinesPanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);
      splitSubPathsPanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);
      lineDetectionPanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);
      smoothingPanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);
      removeTinyPathsPanel.updateWidgets(taskInProgress, imageLoaded, isVectorized);

      cancelButton.setEnabled(!taskInProgress);
      okayButton.setEnabled(!taskInProgress && (isVectorized || dialog.hasResults()));

      boolean enable = !taskInProgress && imageLoaded;

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

   public void updateTaskButton()
   {
      if (doTasksButton != null)
      {
         doTasksButton.setEnabled(isScanImageOn() || isOptimizeOn() 
            || isSplitSubPathsOn() || isSmoothingOn() || isLineDetectionOn()
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

      if (command.equals("dotasks"))
      {
         dialog.doSelectedTasks();
      }
      else if (command.equals("storeresults"))
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
      else if (command.equals("okay"))
      {
         dialog.okay();
      }
      else if (command.equals("cancel"))
      {
         dialog.cancel();
      }
   }

   public void setAll(boolean selected)
   {
      scanImagePanel.setSelected(selected);
      optimizeLinesPanel.setSelected(selected);
      splitSubPathsPanel.setSelected(selected);
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

   public double getGradientEpsilon()
   {
      return optimizeLinesPanel.getGradientEpsilon();
   }

   public double getMinGap()
   {
      return optimizeLinesPanel.getMinGap();
   }

   public double getMinSubPathGap()
   {
      return splitSubPathsPanel.getMinSubPathGap();
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

   public double getLineDetectTinyStepThreshold()
   {
      return lineDetectionPanel.getTinyStepThreshold();
   }

   public double getSmoothingTinyStepThreshold()
   {
      return smoothingPanel.getTinyStepThreshold();
   }

   public double getLengthThreshold()
   {
      return smoothingPanel.getLengthThreshold();
   }

   public double getThresholdDiff()
   {
      return smoothingPanel.getThresholdDiff();
   }

   public double getCurveGradientThreshold()
   {
      return smoothingPanel.getCurveGradientThreshold();
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

   private JButton doTasksButton, storeResultsButton, selectAllButton, 
      deselectAllButton;
   private JDRButton clearAllResultsButton;

   private JDRButton okayButton, cancelButton;

   private ScanImagePanel scanImagePanel;
   private OptimizeLinesPanel optimizeLinesPanel;
   private SplitSubPathsPanel splitSubPathsPanel;
   private LineDetectionPanel lineDetectionPanel;
   private SmoothingPanel smoothingPanel;
   private RemoveTinyPathsPanel removeTinyPathsPanel;
}

class ScanStatusBar extends JPanel implements PropertyChangeListener,ActionListener
{
   public ScanStatusBar(JDRResources resources)
   {
      super();
      textField = new JTextField(12);
      textField.setEditable(false);
      add(textField);

      progressBar = new JProgressBar(0, 100);
      progressBar.setValue(0);
      progressBar.setStringPainted(true);
      add(progressBar);

      cancelButton = resources.createAppJButton("label", "abort", this);
      add(cancelButton);
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
         cancelButton.setEnabled(false);
         cancelled=true;
      }
   }

   public void taskFinished()
   {
      setVisible(false);
   }

   public void startTask(String info, SwingWorker task)
   {
      textField.setText(info);
      progressBar.setValue(0);
      cancelButton.setEnabled(true);
      cancelled = false;
      setVisible(true);
      task.addPropertyChangeListener(this);
      task.execute();
   }

   public boolean isCancelled()
   {
      return cancelled;
   }

   private JTextField textField;
   private JProgressBar progressBar;

   private JButton cancelButton;
   private boolean cancelled=false;
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

   public void addComponent(ShapeComponent comp)
   {
      if (!isEmpty())
      {
         comp.setStart(lastElement().getEnd());
      }

      add(comp);
   }

   public void appendPath(ShapeComponentVector path)
   {
      if (path.isEmpty()) return;

      if (!isEmpty())
      {
         ShapeComponent firstElement = path.firstElement();
         firstElement.setStart(lastElement().getEnd());
      }

      addAll(path);
   }

   public void appendSubPath(SubPath subPath)
   {
      appendSubPath(subPath, false);
   }

   public void appendSubPath(SubPath subPath, boolean lineConnect)
   {
      if (subPath.size() == 0) return;

      Point2D.Double pt = null;
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
                  Point2D.Double p1 = comp.getEnd();

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

   public Path2D.Double getPath()
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

   public Path2D.Double getSubPath2D(int endIdx)
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
      Point2D.Double start = null;

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

      Point2D.Double p = comp.getEnd();

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

         Point2D.Double pt = comp.getEnd();

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

   private int windingRule = Path2D.WIND_NON_ZERO;
   private boolean isFilled = true;
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

   public ShapeComponent(int type, double[] coords, Point2D.Double start)
   {
      this.type = type;
      this.coords = new double[coords.length];

      for (int i = 0; i < coords.length; i++)
      {
         this.coords[i] = coords[i];
      }

      this.start = start;
   }

   public void set(ShapeComponent otherComp)
   {
      this.type = otherComp.type;
      this.start = otherComp.start;

      for (int i = 0; i < coords.length; i++)
      {
         this.coords[i] = otherComp.coords[i];
      }
   }

   public int getType()
   {
      return type;
   }

   public double[] getCoords()
   {
      return coords;
   }

   public Point2D.Double getStart()
   {
      return start;
   }

   public void setStart(Point2D.Double newStart)
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

   public Point2D.Double getP(double t, Point2D startPt)
   {
      Point2D p = getEnd();

      return new Point2D.Double((1.0-t)*startPt.getX()+t*p.getX(),
                                (1.0-t)*startPt.getY()+t*p.getY());
   }

   public Point2D.Double getMid(Point2D startPt)
   {
      Point2D p = getEnd();

      return new Point2D.Double(startPt.getX()+0.5*(p.getX()-startPt.getX()),
                                startPt.getY()+0.5*(p.getY()-startPt.getY()));
   }

   public Point2D.Double getMid()
   {
      if (start == null)
      {
         Point2D p = getEnd();

         return new Point2D.Double(0.5*p.getX(), 0.5*p.getY());
      }

      return getMid(start);
   }

   public Point2D.Double getEnd()
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

   public Point2D.Double getStartGradient()
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

            return Math.sqrt(dx*dx + dy*dy);

         case PathIterator.SEG_QUADTO:

            dx = coords[2]-x0;
            dy = coords[3]-y0;

            return Math.sqrt(dx*dx + dy*dy);

         case PathIterator.SEG_CUBICTO:

            dx = coords[4]-x0;
            dy = coords[5]-y0;

            return Math.sqrt(dx*dx + dy*dy);
      }

      return 0.0;
   }

   public void addToPath(Path2D.Double path)
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
            return String.format("C %f %f %f %f %f", 
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
   private Point2D.Double start=null;
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
      super(resources.getStringWithValue(
         "vectorize.unsupported_colour_type", type));
      this.type = type;
   }

   public UnsupportedColourType(JDRResources resources, String name, int type)
   {
      super(resources.getStringWithValues("vectorize.unsupported_colour_type_name", 
        new String[]{name, ""+type}));
      this.type = type;
   }

   private int type;
}

class UserCancelledException extends InterruptedException
{
   public UserCancelledException()
   {
      super("Task Cancelled");
   }
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
               throw new UserCancelledException();
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

            if (scanRegion != null 
                   && !scanRegion.contains(x+0.5*rectWidth, y+0.5*rectHeight))
            {
               continue;
            }

            Raster raster = image.getData(new Rectangle(x, y, rectWidth, rectHeight));

            publish(raster);
         }
      }

      return null;
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
      minGap = dialog.getMinGap();
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

      newShapesVec = new Vector<ShapeComponentVector>();

      for (int i = 0; i < numShapes; i++)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException();
         }

         publish(shapeList.get(i));
      }

      return null;
   }

   protected void process(java.util.List<ShapeComponentVector> shapeVecList)
   {
      Iterator<ShapeComponentVector> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         ShapeComponentVector vec = iter.next();

         Vector<ShapeComponentVector> result = processShape(vec);

         newShapesVec.addAll(result);
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

            if (current.getType() == PathIterator.SEG_MOVETO && i > 0)
            {
               if (length < minGap)
               {
                  current.setType(PathIterator.SEG_LINETO);
               }
            }

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

      dialog.finishedOptimizeLines(newShapesVec, continueToNextStep);
   }

   private int progress, maxProgress;
   private double gradientEpsilon=0.01, minGap=2.0;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private volatile Vector<ShapeComponentVector> newShapesVec;
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
      Point2D.Double p = vec.get(startIdx).getEnd();

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
      Path2D.Double shape = new Path2D.Double(vec.getRule(), endIdx-startIdx+1);

      ShapeComponent comp = vec.get(startIdx);
      Point2D.Double pt = comp.getEnd();
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

   private ShapeComponentVector vec;
   private int startIdx, endIdx;
   private Vector<Integer> contains, container;
}

class SplitSubPaths extends SwingWorker<Void,Void>
{
   public SplitSubPaths(VectorizeBitmapDialog dialog, Vector<ShapeComponentVector> shapeList,
     boolean continueToNextStep)
   {
      this.dialog = dialog;
      this.shapeList = shapeList;

      minGap = dialog.getMinSubPathGap();
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

                  if (nextComp.getType() == PathIterator.SEG_MOVETO)
                  {
                     Point2D gapDp = nextComp.getStartGradient();
                     double gapLength = Math.sqrt(gapDp.getX()*gapDp.getX()
                                             + gapDp.getY()*gapDp.getY());

                     if (gapLength < minGap)
                     {
                        continue;
                     }
                  }
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
            Point2D.Double pt = sp.getCompleteVector().get(sp.getStartIndex()).getEnd();

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
         throw new UserCancelledException();
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
            Point2D.Double prevPt = null;

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
               Point2D.Double prevPt = null;

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
      Point2D.Double prevPt = null;

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
   private double minGap, minTinyArea;
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private volatile Vector<ShapeComponentVector> newShapesVec;
   private int splitType, minTinySize;
   private boolean continueToNextStep, removeTiny;
   private Comparator<Integer> comparator;

   public static final int SPLIT_ALL=0, EVEN_INTERIOR_SPLIT=1, SPLIT_EXTERIOR_ONLY=2;
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
      maxProgress = 0;
      int numShapes = shapeList.size();

      for (int i = 0; i < numShapes; i++)
      {
         maxProgress += shapeList.get(i).size();
      }

      newShapesVec = new Vector<ShapeComponentVector>();
      dialog.updateShapes(newShapesVec, false);

      for (int i = 0; i < numShapes; i++)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException();
         }

         publish(shapeList.get(i));
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
         throw new UserCancelledException();
      }
   }

   protected void process(java.util.List<ShapeComponentVector> shapeVecList)
   {
      Iterator<ShapeComponentVector> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         incProgress();
         ShapeComponentVector vec = iter.next();

         try
         {
            tryLineify(vec);
         }
         catch (InterruptedException e)
         {
            return;
         }
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

      dialog.finishedLineDetection(newShapesVec, continueToNextStep);
   }

   private void addShape(ShapeComponentVector newShape)
   {
      newShapesVec.add(newShape);
      dialog.repaintImagePanel(newShape.getBounds(), false);
   }

   private void tryLineify(ShapeComponentVector vec)
     throws InterruptedException
   {
      int startIdx = 0;
      int n = vec.size();

      if (n < 4)
      {
         addShape(vec);
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
         addShape(vec);
         return;
      }

      if (subPaths.size() == 1)
      {
         tryLineifyRegion(subPaths.firstElement());
         return;
      }

      n = subPaths.size();
      Shape[] shapes = new Shape[n];

      SubPath outer = null;
      Vector<SubPath> inner = new Vector<SubPath>();

      for (int i = 0; i < n; i++)
      {
         SubPath sp1 = subPaths.get(i);
         Shape shape1 = sp1.getShape();
         shapes[i] = shape1;

         for (int j = 0; j < i; j++)
         {
            // is this sub-path inside or outside any of the other
            // sub-paths?

            SubPath sp2 = subPaths.get(j);
            Shape shape2 = shapes[j];
            
            Point2D.Double pt1 = vec.get(sp1.getStartIndex()).getEnd();
            Point2D.Double pt2 = vec.get(sp2.getStartIndex()).getEnd();
   
            if (shape1.contains(pt2))
            {
               if (inner.contains(sp1))
               {
                  addShape(vec);
                  return;
               }

               if (outer == null)
               {
                  outer = sp1;
                  inner.add(sp2);
               }
            }
            else if (shape2.contains(pt1))
            {
               if (inner.contains(sp2))
               {
                  addShape(vec);
                  return;
               }

               if (outer == null)
               {
                  outer = sp2;
                  inner.add(sp1);
               }
            }
         }
      }

      if (outer == null || inner.isEmpty())
      {
         addShape(vec);
         return;
      }

      for (int i = 0; i < n; i++)
      {
         SubPath sp = subPaths.get(i);

         if (!(outer == sp || inner.contains(sp)))
         {
            addShape(vec);
            return;
         }
      }

      if (inner.size() == 1)
      {
         tryLineifyLoop(outer, inner.firstElement());
      }
      else
      {
// TODO
      }

/*
      ShapeComponentVector newVec = new ShapeComponentVector(n/2);
      newVec.setRule(vec.getRule());

      for (int i = 0; i < n; i++)
      {
         SubPath sp = subPaths.get(i);

         if (vec.get(sp.getEndIndex()).getType() != PathIterator.SEG_CLOSE)
         {
            newVec.appendSubPath(sp);
         }
         else if (i == n-1)
         {
            newVec.appendPath(tryLineifyRegion(sp));
         }
         else
         {
            SubPath sp2 = subPaths.get(i+1);

            if (vec.get(sp2.getEndIndex()).getType() != PathIterator.SEG_CLOSE)
            {
               newVec.appendPath(tryLineifyRegion(sp));
            }
            else
            {
               // How big is the gap between the end of the first
               // sub-path and the start of the next one?

               double delta = 0.5*sp2.getComponent(0).getDiagonalLength();

               if (delta < deltaThreshold)
               {
                  Point2D.Double pt1 = vec.get(sp.getStartIndex()).getEnd();
                  Point2D.Double pt2 = vec.get(sp2.getStartIndex()).getEnd();
   
                  if (shapes[i].contains(pt2) || shapes[i+1].contains(pt1))
                  {
                     ShapeComponentVector v = tryLineifyLoop(sp, sp2, pt1, pt2);
   
                     if (v == null)
                     {
                        newVec.appendPath(tryLineifyRegion(sp));
                     }
                     else
                     {
                        newVec.appendPath(v);
                        i++;
                     }
                  }
                  else
                  {
                     newVec.appendPath(tryLineifyRegion(sp));
                  }
               }
               else
               {
                  newVec.appendPath(tryLineifyRegion(sp));
               }
            }
         }
      }

      if (newVec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         newVec.setFilled(false);
      }
      */

      //return newVec;
   }

   private void tryLineifyRegion(SubPath subPath)
    throws InterruptedException
   {
      int startIdx = subPath.getStartIndex();
      int endIdx = subPath.getEndIndex();

      ShapeComponentVector fullVec = subPath.getCompleteVector();
      ShapeComponentVector vec = new ShapeComponentVector(endIdx-startIdx+1);

      Point2D.Double prevPt = null;

      for (int i = startIdx; i <= endIdx; i++)
      {
         ShapeComponent comp = fullVec.get(i);
         comp.setStart(prevPt);
         vec.add(comp);
         prevPt = comp.getEnd();
      }

      tryLineifyRegion(vec);
   }

   private void tryLineifyRegion(ShapeComponentVector vec)
    throws InterruptedException
   {
      int n = vec.size();

      if (n < 3 || vec.lastElement().getType() != PathIterator.SEG_CLOSE)
      {
         addShape(vec);
         return;
      }

      n--;

      PathCoord[] pts = new PathCoord[n];

      for (int i = 0; i < n; i++)
      {
         ShapeComponent comp = vec.get(i);

         Point2D.Double grad1 = new Point2D.Double();
         Point2D.Double grad2 = new Point2D.Double();
         double bend = getBendAngle(vec, i, grad1, grad2);
         pts[i] = new PathCoord(comp.getType(), comp.getEnd(), bend, grad1, grad2);
      }

      int n2 = n/2;
      int n1 = n-n2;

      LineifyResults results = lineifyBestFit(pts, n1, n2);

      if (results.minAverageDelta < deltaThreshold)
      {
         if (!doLineIntersectionCheck)
         {
            addShape(results.bestPath);
            return;
         }
         else
         {
            if (results.variance < varianceThreshold)
            {
               addShape(results.bestPath);
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
         addShape(vec);
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
         addShape(vec);
         return;
      }

      ShapeComponentVector reducedShape = new ShapeComponentVector(remainingN);

      Point2D.Double prevPt = null;

      for (int i = 0; i < pts.length; i++)
      {
         if (indexes[i] != null)
         {
            Point2D.Double p = pts[i].getPoint();
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

         Point2D.Double grad1 = new Point2D.Double();
         Point2D.Double grad2 = new Point2D.Double();
         double bend = getBendAngle(vec, i, grad1, grad2);
         q[i] = new PathCoord(comp.getType(), comp.getEnd(), bend, grad1, grad2);
      }

      int m2 = q.length/2;
      int m1 = q.length-m2;

      LineifyResults reducedPathResults = lineifyBestFit(q, m1, m2);

      if (reducedPathResults.minAverageDelta < deltaThreshold)
      {
         addShape(reducedPathResults.bestPath);
      }
      else
      {
         addShape(vec);
         return;
      }

      for (Vector<Integer> spike : spikes)
      {
         ShapeComponentVector newShape = new ShapeComponentVector(spike.size()+1);
         prevPt = null;
         double[] coords = null;

         for (Integer num : spike)
         {
            Point2D.Double p = pts[num].getPoint();
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

         tryLineifyRegion(newShape);
      }

   }

   private void tryLineifyLoop(SubPath sp1, SubPath sp2)
    throws InterruptedException
   {
      // last component will always be a close path segment
      int n1 = sp1.size()-1;
      int n2 = sp2.size()-1;

      Point2D.Double[] pts1 = new Point2D.Double[n1];
      Point2D.Double[] pts2 = new Point2D.Double[n2];

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

      if (averageDelta < deltaThreshold)
      {
         if (!doLineIntersectionCheck)
         {
            addShape(path);
            return;
         }
         else
         {
            double variance = calculateVariance(linefit, averageDelta);

            if (variance < varianceThreshold)
            {
               addShape(path);
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
         if (bestAverageDelta < deltaThreshold)
         {
            addShape(path);
         }
         else
         {
            addShape(sp1.getCompleteVector());
         }

         return;
      }

      double variance = calculateVariance(bestLineFit, bestAverageDelta);

      if (bestAverageDelta < deltaThreshold && variance < varianceThreshold)
      {
         addShape(path);
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
               addShape(sp1.getCompleteVector());
               return;
            }

            bestLineFit = maxNonSpikesLineFit;
            bestAverageDelta = maxNonSpikesDelta;
            bestOffset = maxNonSpikesOffset;
            bestPath = maxNonSpikesPath;
         }
         else
         {
            addShape(sp1.getCompleteVector());
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
               addShape(sp1.getCompleteVector());
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
               addShape(sp1.getCompleteVector());
               return;
            }
         }
         else
         {
            addShape(sp1.getCompleteVector());
            return;
         }
      }

      Point2D.Double[] q1 = new Point2D.Double[remainingN1];
      Point2D.Double[] q2 = new Point2D.Double[remainingN2];

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

      if (bestAverageDelta < deltaThreshold)
      {
         addShape(bestPath);
      }
      else
      {
         addShape(sp1.getCompleteVector());
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

         Point2D.Double prevPt = null;
         double[] coords = null;

         for (int i = 0; i < m1; i++)
         {
            int j = (sIdx1+i)%pts1.length;
            Point2D.Double p = pts1[j];
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

         Point2D.Double p1 = pts2[sIdx2];
         Point2D.Double p2 = pts2[(sIdx2+m2-1)%pts2.length];

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
               Point2D.Double p = pts2[j];

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
               Point2D.Double p = pts2[j];

               coords = new double[6];
               coords[0] = p.getX();
               coords[1] = p.getY();

               path.add(new ShapeComponent(PathIterator.SEG_LINETO, coords, prevPt));
               prevPt = p;
            }
         }

         path.add(new ShapeComponent(PathIterator.SEG_CLOSE, coords, prevPt));

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

   public double getDistance(Point2D p0, Point2D p1)
   {
      double dx = p1.getX()-p0.getX();
      double dy = p1.getY()-p0.getY();

      return Math.sqrt(dx*dx + dy*dy);
   }

   public double getDistance(PathCoord p0, PathCoord p1)
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

         Point2D.Double grad1 = pts[i].getGradient1();
         Point2D.Double grad2 = pts[i].getGradient2();
         double angle = Math.PI-getAngle(grad2, grad1);

         if ((angle >= SPIKE_ANGLE_LOWER1 && angle <= SPIKE_ANGLE_UPPER1)
          || (angle >= SPIKE_ANGLE_LOWER2 && angle <= SPIKE_ANGLE_UPPER2))
         {
            double nextDist = getDistance(pts[i].getPoint(), pts[i+1].getPoint());

            if (nextDist < maxTinyStep)
            {
               Point2D.Double nextGrad1 = pts[i+1].getGradient1();
               Point2D.Double nextGrad2 = pts[i+1].getGradient2();
               double nextAngle = Math.PI-getAngle(nextGrad2, nextGrad1);

               if ((nextAngle < SPIKE_ANGLE_UPPER1 && nextAngle < angle)
                || (nextAngle > SPIKE_ANGLE_LOWER2 && nextAngle > angle))
               {
                  // Next angle is sharper
                  continue;
               }
            }

            Point2D.Double q1 = new Point2D.Double(
              pts[i].getX() + extensionDist * grad2.getX(),
              pts[i].getY() + extensionDist * grad2.getY()
              );

            Point2D.Double q2 = new Point2D.Double(
              pts[i].getX() - extensionDist * grad1.getX(),
              pts[i].getY() - extensionDist * grad1.getY()
              );

            double q1Angle = Math.PI + Math.atan2(grad2.getY(), grad2.getX());
            double q2Angle = Math.PI + Math.atan2(grad1.getY(), grad1.getX());

            double qAngle = q2Angle-q1Angle;

            double theta = q1Angle + 0.1*qAngle;
            Point2D.Double q3 = new Point2D.Double(
               pts[i].getX() + extensionDist*Math.cos(theta),
               pts[i].getY() - extensionDist*Math.sin(theta)
            );

            theta = q1Angle + 0.9*qAngle;
            Point2D.Double q4 = new Point2D.Double(
               pts[i].getX() + extensionDist*Math.cos(theta),
               pts[i].getY() - extensionDist*Math.sin(theta)
            );

            theta = q1Angle + 0.5*qAngle;

            Point2D.Double q5 = new Point2D.Double(
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
         if (linefit[i].delta > deltaThreshold)
         {
            sum++;
         }
      }

      return sum;
   }

   private double calculateMean(LineFit[] linefit)
   {
      double sumDelta = 0.0;

      for (int i = 0; i < linefit.length; i++)
      {
         sumDelta += linefit[i].delta;
      }

      return sumDelta / linefit.length;
   }

   private double calculateVariance(LineFit[] linefit, double averageDelta)
   {
      double sum = 0.0;

      for (int i = 0; i < linefit.length; i++)
      {
         double diff = linefit[i].delta - averageDelta;
         sum += diff*diff;
      }

      return sum / linefit.length;
   }

   private LineFit[] fitLoop(Point2D.Double[] pts1, Point2D.Double[] pts2,
      int offset, ShapeComponentVector path)
   {
      int n1 = pts1.length;
      int n2 = pts2.length;

      double[] coords = null;
      int maxN = (int)Math.max(n1, n2);
      int minN = (int)Math.min(n1, n2);

      Point2D.Double prevPt = null;
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

            Point2D.Double p1 = pts1[idx1];
            Point2D.Double p2 = pts2[idx2];

            double dx = 0.5*(p2.getX()-p1.getX());
            double dy = 0.5*(p2.getY()-p1.getY());

            Point2D.Double p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

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

      Point2D.Double prevPt = null;

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

         Point2D.Double grad1 = new Point2D.Double();
         Point2D.Double grad2 = new Point2D.Double();
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
         return minAverageDelta < deltaThreshold ? bestPath : null;
      }
      else if (minAverageDelta < deltaThreshold && variance < varianceThreshold)
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

         Point2D.Double p1=null, p2=null;

         if ((pts.length+bestLineFit[startIdx].idx2
                 - bestLineFit[endIdx].idx2)%pts.length
             == 1 && spike.size() > 2)
         {
            Point2D.Double r1 = pts[bestLineFit[startIdx].idx2].getPoint();
            Point2D.Double r2 = pts[bestLineFit[endIdx].idx2].getPoint();

            Point2D.Double p = pts[bestLineFit[startIdx].idx1].getPoint();

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

         Point2D.Double prevPt = null;
         double[] coords = null;
         int subIdx = 0;

         for (int j = startIdx; j <= endIdx; j++)
         {
            int k = bestLineFit[j].idx1;

            subIndexes[subIdx++] = Integer.valueOf(k);
            Point2D.Double p = pts[k].getPoint();
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
               Point2D.Double p = pts[num.intValue()].getPoint();
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
               newShapesVec.add(lastShape);
               dialog.repaintImagePanel(lastShape.getBounds(), false);
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
         newShapesVec.add(lastShape);
         dialog.repaintImagePanel(lastShape.getBounds(), false);
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

      if (results.minAverageDelta < deltaThreshold
              && results.variance < varianceThreshold)
      {
         return results.bestPath;
      }

      ShapeComponentVector newShape = new ShapeComponentVector(remaining+1);
      Point2D.Double prevPt = null;
      double[] coords = null;

      for (int i = 0; i < remaining; i++)
      {
         Point2D.Double p = remainingPts[i].getPoint();
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
         }
      }

      if (bestPath != path)
      {
         path.clear();
         path.addAll(bestPath);
      }

      return linefit;
   }

   private LineFit[] tryLineify(int maxN, int minN, int div, int r, 
     PathCoord[] pts, int offset, int n1, int n2, ShapeComponentVector path)
   {
      Point2D.Double prevPt = null;
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

/*
            int idx1 = (offset+i)%pts.length;
            int idx2 = (offset+pts.length-j-1)%pts.length;
*/
            idx1 = (offset+idx1)%pts.length;
            idx2 = (offset+pts.length-idx2-1)%pts.length;

            Point2D.Double p1 = pts[idx1].getPoint();
            Point2D.Double p2 = pts[idx2].getPoint();

            double dx = 0.5*(p2.getX()-p1.getX());
            double dy = 0.5*(p2.getY()-p1.getY());

            Point2D.Double p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

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

         Point2D.Double p1 = pts[idx1].getPoint();
         Point2D.Double p2 = pts[idx2].getPoint();

         double dx = 0.5*(p2.getX()-p1.getX());
         double dy = 0.5*(p2.getY()-p1.getY());

         Point2D.Double p = new Point2D.Double(p1.getX()+dx, p1.getY()+dy);

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
            Point2D.Double grad1 = new Point2D.Double();
            Point2D.Double grad2 = new Point2D.Double();
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
         Point2D.Double startPt = null;

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

            Point2D.Double p = new Point2D.Double(
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
         return minDelta < deltaThreshold ? bestPath : vec;
      }

      ShapeComponent bestStartComp = vec.get(bestStartIdx);
      ShapeComponent bestEndComp = vec.get(bestEndIdx);

      int n1 = bestEndIdx - bestStartIdx;
      int n2 = n - 1 - n1;

      ShapeComponent lastComp = vec.get(n-2); 
      Point2D.Double lastPoint = lastComp.getEnd();

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
            Point2D.Double p0 = comp.getStart();

            if (k == 0)
            {
               p0 = lastPoint;
            }

            Point2D.Double p1 = comp.getEnd();

            int type = (k == n-2 ? PathIterator.SEG_LINETO : vec.get(k+1).getType());

            if (extra >= d)
            {
               for (int l = 1; l < d && j < pts1.length-1; l++)
               {
                  Point2D.Double pt = comp.getP(((double)l)/d, p0);
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
                  Point2D.Double pt = comp.getP(((double)l)/(extra+1), p0);
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
            Point2D.Double p0 = comp.getStart();

            if (k == 0)
            {
               p0 = lastPoint;
            }

            Point2D.Double p1 = comp.getEnd();

            int type = (k == 0 ? PathIterator.SEG_LINETO : comp.getType());

            if (extra >= d)
            {
               for (int l = d-1; l >= 1 && j < pts2.length-1; l--)
               {
                  Point2D.Double pt = comp.getP(((double)l)/d, p0);
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
                  Point2D.Double pt = comp.getP(((double)l)/(extra+1), p0);
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
      Point2D.Double prevPt = null;

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

      if (delta < minDelta && delta < deltaThreshold)
      {
         newPath.setFilled(false);
         return newPath;
      }
      else if (minDelta < deltaThreshold)
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
            Point2D.Double r = comp.getEnd();

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
            Point2D.Double r = comp.getEnd();

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
   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapeList;
   private volatile Vector<ShapeComponentVector> newShapesVec;
   private boolean continueToNextStep, doLineIntersectionCheck;
   private static final double HALF_PI = 0.5*Math.PI,
    SPIKE_ANGLE_LOWER1=0.2*Math.PI, SPIKE_ANGLE_UPPER1=0.8*Math.PI,
    SPIKE_ANGLE_LOWER2=1.2*Math.PI, SPIKE_ANGLE_UPPER2=1.8*Math.PI;
}

class PathCoord
{
   public PathCoord(int type, Point2D.Double p, double bend, Point2D.Double grad1,
     Point2D.Double grad2)
   {
      this.bend = bend;
      this.type = type;
      this.p = p;
      this.grad1 = grad1;
      this.grad2 = grad2;
   }

   public PathCoord(int type, Point2D.Double p)
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

   public Point2D.Double getPoint()
   {
      return p;
   }

   // average angle between this and previous pt
   public double getBend()
   {
      return bend;
   }

   public Point2D.Double getGradient1()
   {
      return grad1;
   }

   public Point2D.Double getGradient2()
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
   private Point2D.Double p, grad1, grad2;
}

class LineFit
{
   public LineFit(double delta, int idx1, int idx2, Point2D.Double p1, Point2D.Double p2)
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
   protected Point2D.Double p1, p2;
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

class ShapeComponentVectorListElement
{
   public ShapeComponentVectorListElement(int index, ShapeComponentVector vec)
   {
      this.index = index;
      this.vec = vec;
   }

   public int getIndex()
   {
      return index;
   }

   public ShapeComponentVector getVector()
   {
      return vec;
   }

   private int index;
   private ShapeComponentVector vec;
}

class Smooth extends SwingWorker<Void,ShapeComponentVectorListElement>
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
      tryBezier = dialog.isTryBezierOn();
      bezierGradientThreshold = dialog.getCurveGradientThreshold();
      minBezierSamples = dialog.getCurveMinPoints();
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
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException();
         }

         publish(new ShapeComponentVectorListElement(i, shapes.get(i)));
      }

      return null;
   }

   protected void process(java.util.List<ShapeComponentVectorListElement> shapeVecList)
   {
      Iterator<ShapeComponentVectorListElement> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         ShapeComponentVectorListElement elem = iter.next();
         ShapeComponentVector vec = elem.getVector();

         ShapeComponentVector shape = smoothShape(vec);

         if (shape != null)
         {
            shapes.set(elem.getIndex(), shape);
         }
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

      dialog.finishedSmoothing(shapes, continueToNextStep);
   }

   private ShapeComponentVector smoothShape(ShapeComponentVector vec)
   {
      ShapeComponentVector path = null;

      for (int i = 1, n = vec.size(); i < n; i++)
      {
         incProgress();

         int j = i;

         for ( ; j < n; j++)
         {
            ShapeComponent comp = vec.get(j);
            int type = comp.getType();

            if (type != PathIterator.SEG_LINETO)
            {
               break;
            }

            Point2D p0 = comp.getStart();
            Point2D p1 = comp.getEnd();

            double dx = p0.getX()-p1.getX();
            double dy = p0.getY()-p1.getY();

            double length = Math.sqrt(dx*dx+dy*dy);

            if (length > tinyStepThreshold)
            {
               break;
            }
         }

         int endIdx = j-1;

         if (endIdx - i > 2)
         {
            DeviationResult bestResult = getBestComponent(vec, i, endIdx);

            if (bestResult != null)
            {
               if (path == null)
               {
                  path = vec.getSubPath(i-1);
               }

               ShapeComponent comp = bestResult.getComponent();
               path.addComponent(comp);
               i = bestResult.getEndIndex();
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
      int startIdx, int endIdx)
   {
      ShapeComponent startComp = vec.get(startIdx);
      Point2D.Double p0 = startComp.getStart();
      DeviationResult bestResult = null, bestSubThresholdResult = null;
      double minDelta = Double.MAX_VALUE;
      double minSubThresholdDelta = Double.MAX_VALUE;

      for (int j = endIdx; j > startIdx+1; j--)
      {
         ShapeComponent endComp = vec.get(j);

         Point2D.Double p1 = endComp.getEnd();

         DeviationResult result = DeviationResult.create(vec, startIdx, j, p0, p1,
            tryBezier && minBezierSamples < (j-startIdx),
            bezierGradientThreshold);

         double delta = result.getAverageDeviation();

         if (delta < minDelta)
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
      }

      if (bestSubThresholdResult != null)
      {
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
   private double tinyStepThreshold=2.0, lengthThreshold=10.0, thresholdDiff=0.01;
   private boolean tryBezier=true;
   private int minBezierSamples = 5;
   private double bezierGradientThreshold=8.0;
   private int progress, maxProgress;
   private boolean continueToNextStep;
}

class DeviationResult
{
   private DeviationResult()
   {
   }

   public static DeviationResult create(ShapeComponentVector vec,
      int startIdx, int endIdx,
      Point2D.Double p1, Point2D.Double p2, boolean tryBezier,
      double gradientThreshold)
   {
      DeviationResult result = new DeviationResult();
      result.compute(vec, startIdx, endIdx, p1, p2, tryBezier, gradientThreshold);
      return result;
   }

   public void compute(ShapeComponentVector vec, int startIdx, int endIdx,
      Point2D.Double p1, Point2D.Double p2, boolean tryBezier, double gradientThreshold)
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

         if (tryBezier)
         {
            t[j] = (x+y-p1.getX()-p1.getY())*t_factor;

            double t_sq = t[j]*t[j];
            double t_cube = t_sq*t[j];
            double one_minus_t = 1.0-t[j];
            double one_minus_t_sq = one_minus_t * one_minus_t;
            double one_minus_t_sq_x_t = one_minus_t_sq * t[j];
            double one_minus_t_x_t_sq = one_minus_t * t_sq;
            double one_minus_t_cube = one_minus_t_sq * one_minus_t;

            double kx = one_minus_t_cube * p1.getX() + t_cube * p2.getX();
            double ky = one_minus_t_cube * p1.getY() + t_cube * p2.getY();

            a1 += one_minus_t_sq_x_t * one_minus_t_sq_x_t;
            a2 += one_minus_t_sq_x_t * one_minus_t_x_t_sq;
            a3 += one_minus_t_x_t_sq * one_minus_t_x_t_sq;

            double k_minus_x = kx - p.getX();
            double k_minus_y = ky - p.getY();

            b1 += one_minus_t_sq_x_t * k_minus_x;
            b2 += one_minus_t_sq_x_t * k_minus_y;
            b3 += one_minus_t_x_t_sq * k_minus_x;
            b4 += one_minus_t_x_t_sq * k_minus_y;
         }
      }

      boolean chooseLine = true;

      double[] coords = new double[6];

      double lineAverageDeviation = sumDist/numPoints;

      if (tryBezier)
      {
         a1 *= 3.0;
         a2 *= 3.0;
         a3 *= 3.0;

         double c_factor = 1.0/(a2 * a2 - a1 * a3);

         if (!Double.isNaN(c_factor))
         {
            double c1_x = c_factor * (b1 * a3 - a2 * b3);
            double c1_y = c_factor * (b2 * a3 - a2 * b4);
            double c2_x = c_factor * (a1 * b3 - a2 * b1);
            double c2_y = c_factor * (a1 * b4 - a2 * b2);

            sumDist = 0.0;
            double curveLength = 0.0;
            double prevX = p1.getX();
            double prevY = p1.getY();
            double curveMinDist = Double.MAX_VALUE;
            double curveMaxDist = 0.0;

            for (int i = startIdx, j=0; i < endIdx; i++, j++)
            {
               ShapeComponent comp = vec.get(i);
               Point2D p = comp.getEnd();

               double t_sq = t[j] * t[j];
               double one_minus_t = 1.0 - t[j];
               double one_minus_t_sq = one_minus_t * one_minus_t;

               double d1 = one_minus_t * one_minus_t_sq;
               double d2 = 3 * one_minus_t_sq * t[j];
               double d3 = 3 * one_minus_t * t_sq;
               double d4 = t_sq * t[j];

               double bx = d1 * p1.getX() + d2 * c1_x + d3 * c2_x + d4 * p2.getX();
               double by = d1 * p1.getY() + d2 * c1_y + d3 * c2_y + d4 * p2.getY();

               double dx = bx - p.getX();
               double dy = by - p.getY();

               double dist = Math.sqrt(dx*dx + dy*dy);
               sumDist += dist;

               if (dist < curveMinDist)
               {
                  curveMinDist = dist;
               }

               if (dist > curveMaxDist)
               {
                  curveMaxDist = dist;
               }

               dx = prevX - bx;
               dy = prevY - by;

               curveLength += Math.sqrt(dx*dx + dy*dy);

               prevX = bx;
               prevY = by;
            }

            double dx = prevX - p2.getX();
            double dy = prevY - p2.getY();

            curveLength += Math.sqrt(dx*dx + dy*dy);

            double curveAverageDeviation = sumDist / numPoints;

            double lineGradX = p2.getX() - p1.getX();
            double lineGradY = p2.getY() - p1.getY();

            double dP0x = 3*(c1_x - p1.getX());
            double dP0y = 3*(c1_y - p1.getY());

            double dP1x = 3*(p2.getX() - c2_x);
            double dP1y = 3*(p2.getY() - c2_y);

            dx = lineGradX-dP0x;
            dy = lineGradY-dP0y;
            double diff0 = Math.sqrt(dx*dx + dy*dy);

            dx = lineGradX-dP1x;
            dy = lineGradY-dP1y;
            double diff1 = Math.sqrt(dx*dx + dy*dy);

            if (curveAverageDeviation < lineAverageDeviation
              && diff0 > gradientThreshold && diff1 > gradientThreshold)
            {
               chooseLine = false;
               coords[0] = c1_x;
               coords[1] = c1_y;
               coords[2] = c2_x;
               coords[3] = c2_y;
               coords[4] = p2.getX();
               coords[5] = p2.getY();

               averageDeviation = curveAverageDeviation;

               component = new ShapeComponent(PathIterator.SEG_CUBICTO, coords, p1);
               length = curveLength;
               minDist = curveMinDist;
               maxDist = curveMaxDist;
            }
         }
      }

      if (chooseLine)
      {
         averageDeviation = lineAverageDeviation;
         coords[0] = p2.getX();
         coords[1] = p2.getY();
         component = new ShapeComponent(PathIterator.SEG_LINETO, coords, p1);
         length = component.getDiagonalLength();
      }
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

   public int getStartIndex()
   {
      return startIdx;
   }

   public int getEndIndex()
   {
      return endIdx;
   }

   public String toString()
   {
      return String.format("average deviation: %f, dist range: [%f, %f], length: %f, index range: [%d, %d], component: %s", 
        averageDeviation, minDist, maxDist, length, startIdx, endIdx, component);
   }

   private double averageDeviation=Double.MAX_VALUE;
   private Point2D.Double p1, p2;
   private double maxDist = 0.0, minDist = Double.MAX_VALUE;
   private ShapeComponent component;
   private double length=0.0;
   private int startIdx, endIdx;
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

class RemoveTinyPaths extends SwingWorker<Void,ShapeComponentVector>
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

      newShapesVec = new Vector<ShapeComponentVector>(numShapes);

      for (int i = 0; i < numShapes; i++)
      {
         dialog.updateTimeElapsed();
         Thread.sleep(VectorizeBitmapDialog.SLEEP_DURATION);

         // check for cancel
         if (dialog.isCancelled())
         {
            throw new UserCancelledException();
         }

         publish(shapes.get(i));
      }

      return null;
   }

   protected void process(java.util.List<ShapeComponentVector> shapeVecList)
   {
      Iterator<ShapeComponentVector> iter = shapeVecList.iterator();

      while (iter.hasNext())
      {
         incProgress();

         ShapeComponentVector vec = iter.next();

         Rectangle2D bounds = vec.getBounds2D();

         if (bounds != null 
          && bounds.getWidth()*bounds.getHeight() >= areaThreshold)
         {
            newShapesVec.add(vec);
         }

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

      dialog.finishedRemoveTinyPaths(newShapesVec);
   }

   private VectorizeBitmapDialog dialog;
   private Vector<ShapeComponentVector> shapes;
   private double areaThreshold;
   private int progress, maxProgress;
   private Vector<ShapeComponentVector> newShapesVec;
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

         revalidate();
      }
      else
      {
         setPreferredSize(DEFAULT_PREFERRED_SIZE);
      }

      repaint();
   }

   public double getMagnification()
   {
      return dialog.getMagnification();
   }

   public Color getImageForeground()
   {
      return dialog.getImageForeground();
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
         g2.setColor(NOT_REGION_COLOUR);

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
         g2.setColor(DRAG_COLOUR);
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

      g2.setColor(LINE_COLOUR);
      g2.draw(af.createTransformedShape(shape));

      PathIterator pi = shape.getPathIterator(null);
      double[] orgCoords = new double[6];
      double[] coords = new double[6];
      double[] prev = null;

      for (int i = 0; i < orgCoords.length; i++)
      {
         orgCoords[i] = 0.0;
      }

      double controlSize = CONTROL_SIZE;
      double halfControlSize = 0.5*controlSize;

      while (!pi.isDone())
      {
         int current = pi.currentSegment(orgCoords);

         af.transform(orgCoords, 0, coords, 0, orgCoords.length/2);

         switch (current)
         {
            case PathIterator.SEG_MOVETO:
              if (prev != null)
              {
                 g2.setColor(CONNECT_COLOUR);
                 g2.setStroke(CONNECT_STROKE);

                 g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

                 g2.setStroke(stroke);
              }
            case PathIterator.SEG_LINETO:
              g2.setColor(CONTROL_COLOUR);

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
              g2.setColor(CONNECT_COLOUR);
              g2.setStroke(CONNECT_STROKE);

              g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

              g2.drawLine((int)Math.round(coords[0]), (int)Math.round(coords[1]),
                  (int)Math.round(coords[2]), (int)Math.round(coords[3]));

              g2.setStroke(stroke);
              g2.setColor(CONTROL_COLOUR);

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
              g2.setColor(CONNECT_COLOUR);
              g2.setStroke(CONNECT_STROKE);

              g2.drawLine((int)Math.round(prev[0]), (int)Math.round(prev[1]),
                  (int)Math.round(coords[0]), (int)Math.round(coords[1]));

              g2.drawLine((int)Math.round(coords[0]), (int)Math.round(coords[1]),
                  (int)Math.round(coords[2]), (int)Math.round(coords[3]));

              g2.drawLine((int)Math.round(coords[2]), (int)Math.round(coords[3]),
                  (int)Math.round(coords[4]), (int)Math.round(coords[5]));

              g2.setStroke(stroke);
              g2.setColor(CONTROL_COLOUR);

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
      double factor = 1.0/dialog.getMagnification();

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

         double factor = 1.0/dialog.getMagnification();

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

            dialog.regionPickerChoice(rect, evt.isShiftDown());

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

            repaint();
         }
      }

      draggingRegion = null;
      dragStart = null;
   }

   public void clearRegion()
   {
      notRegion = null;
      repaint();
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

   private VectorizeBitmapDialog dialog;
   private BufferedImage image;
   private Shape workingShape = null;
   private Vector<Shape> shapes;
   private Point dragStart = null;
   private Rectangle draggingRegion = null;
   private Area notRegion=null;

   private static int CONTROL_SIZE=4;
   public static Color LINE_COLOUR=Color.RED, CONTROL_COLOUR=Color.ORANGE,
     CONNECT_COLOUR=Color.CYAN, DRAG_COLOUR=Color.MAGENTA;

   public static Color NOT_REGION_COLOUR=new Color(127, 127, 127, 127);

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
         g2.draw(shape);
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
      }
      else
      {
         path.setLinePaint(new JDRColor(cg, foreground));
      }

      path.setStroke(new JDRBasicStroke(cg));

      return path;
   }

   private Color foreground;
   private boolean isFilled;
   private Shape shape;
}

class ResultPanel extends JPanel
{
   public ResultPanel(ImagePanel imagePanel)
   {
      super();
      this.imagePanel = imagePanel;
      resultList = new Vector<Result>();
      setBackground(Color.WHITE);

   }

   public Dimension getPreferredSize()
   {
      return imagePanel == null ? ImagePanel.DEFAULT_PREFERRED_SIZE
        : imagePanel.getPreferredSize();
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

      g2.setRenderingHints(RENDER_HINTS);

      double mag = imagePanel.getMagnification();

      g2.scale(mag, mag);

      for (Result result : resultList)
      {
         result.paint(g2);
      }

      if (currentShapeList != null && !currentShapeList.isEmpty())
      {
         g2.setColor(imagePanel.LINE_COLOUR);
         
         for (ShapeComponentVector shapeVec : currentShapeList)
         {
            if (!shapeVec.isEmpty())
            {
               Shape shape = shapeVec.getPath();

               if (shapeVec.isFilled())
               {
                  g2.setColor(CURRENT_FILL);
                  g2.fill(shape);
                  g2.setColor(imagePanel.LINE_COLOUR);
               }

               g2.draw(shape);
            }
         }
      }
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

         topField.setText(shapes.size() == 1 ?
           resources.getString("vectorize.summary.path") : 
           resources.getStringWithValue("vectorize.summary.paths", shapes.size()));
      }

      revalidate();
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

      JDRResources resources = dialog.getResources();

      String extraText = "";
      String extraText2 = "";

      if (shape.isEmpty())
      {
         extraText = resources.getString("vectorize.summary.empty");
      }
      else if (shape.lastElement().getType() == PathIterator.SEG_CLOSE)
      {
         extraText = resources.getStringWithValue("vectorize.summary.closed",
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
            extraText2 = resources.getStringWithValue("vectorize.summary.subpaths", 
              numSubPaths);
         }
      }

      String text = resources.getStringWithValues("vectorize.summary.path_n", 
       new String[]{String.format("%d", idx+1), extraText, extraText2});

      JTextField topField = new JTextField(text);
      topField.setEditable(false);
      topField.setOpaque(false);
      add(topField, "North");

      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < shape.size(); i++)
      {
         ShapeComponent comp = shape.get(i);
         builder.append(String.format("%n%s", comp.toString()));
      }

      JTextArea mainArea = new JTextArea(builder.toString());
      mainArea.setEditable(false);
      mainArea.setOpaque(false);
      add(mainArea, "Center");

      Box buttonPanel = Box.createVerticalBox();
      add(buttonPanel, "West");

      buttonPanel.add(resources.createDialogButton(
        "vectorize.summary", "discard", this, null));
      buttonPanel.add(resources.createAppJButton(
        "vectorize.summary", "store", this));
   }

   public void actionPerformed(ActionEvent evt)
   {
      String command = evt.getActionCommand();

      if (command == null) return;

      if (command.equals("delete"))
      {
         dialog.deleteShape(index);
      }
      else if (command.equals("store"))
      {
         dialog.storeShape(index);
      }
   }

   private VectorizeBitmapDialog dialog;
   private int index;
}
