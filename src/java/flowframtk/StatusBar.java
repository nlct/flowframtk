// File          : StatusBar.java
// Description   : Status bar panel
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.jdr.JDRUnit;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.ZoomValue;
import com.dickimawbooks.flowframtk.dialog.StorageUnitDialog;
import com.dickimawbooks.flowframtk.dialog.InfoDialog;

/**
 * Status bar panel.
 * @author Nicola L C Talbot
 */
public class StatusBar extends JPanel
{
   public StatusBar(FlowframTk a)
   {
      super();

      this.application = a;

      storageUnitDialog = new StorageUnitDialog(application);
      infoDialog = new InfoDialog(application, "sec:selectobjects");

      Font statusFont = application.getStatusFont();

      int height = application.getStatusHeight();
      int posWidth = application.getStatusPositionWidth();
      int unitWidth = application.getStatusUnitWidth();
      int modWidth = application.getStatusModifiedWidth();

      JLabel tmp;

      if (posWidth <= 0 || height <= 0)
      {
         tmp = new JLabel("000.00,000.00 "
           +CanvasTextField.widestChar
           +CanvasTextField.widestChar
           +CanvasTextField.widestChar);
         tmp.setFont(statusFont);
         posDim = tmp.getPreferredSize();

         if (posWidth > 0)
         {
            posDim.width = posWidth;
         }
         else
         {
            application.setStatusPositionWidth(posWidth);
         }

         if (height <= 0)
         {
            height = posDim.height;
         }
         else
         {
            posDim.height = height;
         }
      }
      else
      {
         posDim = new Dimension(posWidth, height);
      }

      if (modWidth <= 0)
      {
         tmp = new JLabel(getResources().getMessage("info.modified")
           +CanvasTextField.widestChar);
         tmp.setFont(statusFont);
         modDim = tmp.getPreferredSize();
         modDim.height = height;
         application.setStatusModifiedWidth(modWidth);
      }
      else
      {
         modDim = new Dimension(modWidth, height);
      }

      if (unitWidth <= 0)
      {
         tmp = new JLabel(CanvasTextField.widestChar
          +CanvasTextField.widestChar
          +CanvasTextField.widestChar);
         tmp.setFont(statusFont);
         unitDim = tmp.getPreferredSize();
         unitDim.height = height;
         application.setStatusUnitWidth(unitWidth);
      }
      else
      {
         unitDim = new Dimension(unitWidth, height);
      }

      application.setStatusHeight(height);

      tmp = null;

      pos = new JLabel()
      {
         public Dimension getPreferredSize()
         {
            return posDim;
         }

         public Dimension getMinimumSize()
         {
            return posDim;
         }

         public Font getFont()
         {
            return application.getStatusFont();
         }
      };

      pos.setText(" ");

      pos.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (evt.getClickCount() > 1)
            {
               JDRFrame frame = application.getCurrentFrame();

               if (frame != null)
               {
                  application.displayGoToDialog();
               }
            }
         }
      });

      storageUnitLabel = new JLabel()
      {
         public Dimension getPreferredSize()
         {
            return unitDim;
         }

         public Dimension getMinimumSize()
         {
            return unitDim;
         }

         public Font getFont()
         {
            return application.getStatusFont();
         }
      };

      storageUnitLabel.setToolTipText(
         getResources().getMessage("info.storage_unit"));

      storageUnitLabel.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (evt.getClickCount() > 1)
            {
               storageUnitDialog.display();
            }
         }
      });

      modifiedLabel = new JLabel()
      {
         public Dimension getPreferredSize()
         {
            return modDim;
         }

         public Dimension getSize()
         {
            return modDim;
         }

         public Dimension getMinimumSize()
         {
            return modDim;
         }

         public Font getFont()
         {
            return application.getStatusFont();
         }
      };

      modifiedLabel.setText(" ");
      modifiedLabel.setToolTipText(
         getResources().getMessage("info.unmodified.tooltip"));

      lockIcon = getResources().appIcon("key.png");
      unlockIcon = getResources().appIcon("keycross.png");

      lockDim = new Dimension(lockIcon.getIconWidth(), height);

      lockLabel = new JLabel(unlockIcon)
      {
         public Dimension getMinimumSize()
         {
            return lockDim;
         }

         public Dimension getPreferredSize()
         {
            return lockDim;
         }
      };

      lockLabel.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (evt.getClickCount() > 1)
            {
               JDRFrame frame = application.getCurrentFrame();

               if (frame != null)
               {
                  application.setLockGrid(!frame.getGridLock());
               }
            }
         }
      });

      lockLabel.setToolTipText(getResources().getMessage("info.grid.lock_off"));

      zoomComp = new ZoomComponent(application, statusFont, height);
      Dimension zoomFieldDim = zoomComp.getZoomFieldSize();
      Dimension zoomControlDim = zoomComp.getZoomControlSize();

      infoField = new JTextField()
      {
         public Font getFont()
         {
            return application.getStatusFont();
         }
      };
      infoField.setEditable(false);
      infoField.setFocusable(false);
      infoField.setDragEnabled(false);
      infoField.setHighlighter(null);
      infoField.setToolTipText(
        getResources().getMessageIfExists("info.tooltip"));

      infoField.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (evt.getClickCount() > 1)
            {
               infoDialog.display(infoField.getText(), helpId);
            }
         }
      });

      helpButton = getResources().createSmallHelpButton(
       application.getRootPane(),
       new AbstractAction()
       {
          public void actionPerformed(ActionEvent evt)
          {
             if (helpId == null)
             {
                getResources().getHelpLib().openHelp();
             }
             else
             {
                try
                {
                   if (targetRef != null)
                   {
                      getResources().getHelpLib().openHelp(targetRef);
                   }
                   else if (navNode != null)
                   {
                      getResources().getHelpLib().openHelp(navNode);
                   }
                   else
                   {
                      getResources().getHelpLib().openHelpForId(helpId);
                   }
                }
                catch (Exception e)
                {
                   getResources().error(application, e);
                }
             }
          }
       });

      helpButton.setToolTipText(
        getResources().getMessageIfExists("info.help.tooltip"));


      int maxH = height;
      if (posDim.height > maxH) maxH = posDim.height;
      if (unitDim.height > maxH) maxH = unitDim.height;
      if (modDim.height > maxH) maxH = modDim.height;
      if (lockDim.height > maxH) maxH = lockDim.height;
      if (zoomControlDim.height > maxH) maxH = zoomControlDim.height;
      if (zoomFieldDim.height > maxH) maxH = zoomFieldDim.height;

      application.setStatusHeight(maxH);

      posDim.height = maxH;
      unitDim.height = maxH;
      modDim.height = maxH;
      lockDim.height = maxH;
      zoomControlDim.height = maxH;
      zoomFieldDim.height = maxH;

      setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridx=GridBagConstraints.RELATIVE;
      gbc.fill=GridBagConstraints.BOTH;
      gbc.weightx=0;

      add(pos, gbc);
      add(storageUnitLabel, gbc);
      add(modifiedLabel, gbc);
      add(lockLabel, gbc);
      add(zoomComp, gbc);
      add(helpButton, gbc);

      gbc.weightx=1;
      add(infoField, gbc);

      setBorder(BorderFactory.createLoweredBevelBorder());

      pos.setBorder(BorderFactory.createLoweredBevelBorder());
      storageUnitLabel.setBorder(BorderFactory.createLoweredBevelBorder());
      modifiedLabel.setBorder(
         BorderFactory.createLoweredBevelBorder());
      lockLabel.setBorder(BorderFactory.createLoweredBevelBorder());
      zoomComp.setBorder(BorderFactory.createLoweredBevelBorder());

      helpButton.setBorder(BorderFactory.createLoweredBevelBorder());

      infoField.setBorder(BorderFactory.createLoweredBevelBorder());

      FlowframTkSettings settings = application.getSettings();
      setZoomVisible(settings.showStatusZoom);
      setPositionVisible(settings.showStatusPosition);
      setModifiedVisible(settings.showStatusModified);
      setLockVisible(settings.showStatusLock);
      setInfoVisible(settings.showStatusInfo);
   }

   public void recalculate()
   {
      Font statusFont = application.getStatusFont();

      int height = application.getStatusHeight();
      int posWidth = application.getStatusPositionWidth();
      int unitWidth = application.getStatusUnitWidth();
      int modWidth = application.getStatusModifiedWidth();

      JLabel tmp;
      Dimension dim;

      tmp = new JLabel("000.00,000.00 "
           +CanvasTextField.widestChar);
      tmp.setFont(statusFont);
      dim = tmp.getPreferredSize();

      posDim.width = dim.width;
      posDim.height = dim.height;

      application.setStatusPositionWidth(posWidth);
      height = posDim.height;

      tmp = new JLabel(getResources().getMessage("info.modified")
           +CanvasTextField.widestChar);
      tmp.setFont(statusFont);
      dim = tmp.getPreferredSize();

      modDim.width = dim.width;
      modDim.height = height;
      application.setStatusModifiedWidth(modWidth);

      tmp = new JLabel(CanvasTextField.widestChar
           +CanvasTextField.widestChar
           +CanvasTextField.widestChar);
      tmp.setFont(statusFont);
      dim = tmp.getPreferredSize();

      unitDim.width = dim.width;
      unitDim.height = height;
      application.setStatusUnitWidth(unitWidth);

      lockDim.height = height;

      application.setStatusHeight(height);

      zoomComp.setZoomFieldFont(statusFont, height);

      revalidate();
   }

   public void setInfo(String infoText)
   {
      setInfo(infoText, null);
   }

   public void setInfo(String infoText, String helpId)
   {
      infoField.setText(infoText);
      infoField.setCaretPosition(0);
      this.helpId = helpId;

      if (helpId == null)
      {
         helpButton.setVisible(false);
         helpButton.setEnabled(false);
         navNode = null;
         targetRef = null;
      }
      else
      {
         helpButton.setVisible(true);
         helpButton.setEnabled(true);
         try
         {
            refreshHelpReference();
         }
         catch (Exception e)
         {
            getResources().error(application, e);
         }
      }
   }

   protected void refreshHelpReference() throws UnknownNodeException
   {
      TeXJavaHelpLib helpLib = getResources().getHelpLib();

      targetRef = null;
      navNode = helpLib.getNavigationNodeById(helpId);

      if (navNode == null)
      {
         targetRef = helpLib.getTargetRef(helpId);

         if (targetRef == null)
         {
            throw new UnknownNodeException(helpLib.getMessage(
              "error.node_id_not_found", helpId));
         }
      }
   }

   public String getInfo()
   {
      return infoField.getText();
   }

   public void setLock(boolean lock)
   {
      if (lock)
      {
         lockLabel.setIcon(lockIcon);
         lockLabel.setToolTipText(getResources().getMessage("info.grid.lock_on"));
      }
      else
      {
         lockLabel.setIcon(unlockIcon);
         lockLabel.setToolTipText(getResources().getMessage("info.grid.lock_off"));
      }
   }

   public void setPosition(String positionText)
   {
      pos.setText(positionText+" ");
   }

   public void setModified(boolean modified)
   {
      modifiedLabel.setText(modified ?
         getResources().getMessage("info.modified") : " ");

      modifiedLabel.setToolTipText(modified ?
         getResources().getMessage("info.modified.tooltip") :
         getResources().getMessage("info.unmodified.tooltip"));
   }

   public void setStorageUnit(byte id)
   {
      storageUnitLabel.setText(JDRUnit.UNIT_LABELS[id]);
   }

   public void setStorageUnit(JDRUnit unit)
   {
      storageUnitLabel.setText(unit.getLabel());
   }

   public synchronized void enableStatus()
   {
      pos.setEnabled(true);
      modifiedLabel.setEnabled(true);
      lockLabel.setEnabled(true);
      storageUnitLabel.setEnabled(true);
      zoomComp.setEnabled(true);
      updateZoom(application.getCurrentMagnification());
   }

   /**
    * Indicates that no frames are selected. There may be
    * frames that are minimized or there may be no frames at all.
    * @param numFrames the number of frames that exist but are
    * not selected
    */
   public synchronized void noFramesSelected(int numFrames)
   {
      if (numFrames == 0)
      {
         setInfo(getResources().getMessage("info.no_frames"));
      }
      else
      {
         setInfo(getResources().getMessage("info.no_frames_selected"));
      }
      pos.setEnabled(false);
      modifiedLabel.setEnabled(false);
      lockLabel.setEnabled(false);
      storageUnitLabel.setEnabled(false);
      zoomComp.setEnabled(false);
   }

   public void updateZoom(double factor)
   {
      zoomComp.setZoom(factor);
   }

   public void setZoomVisible(boolean isVisible)
   {
      zoomComp.setVisible(isVisible);
   }

   public void setPositionVisible(boolean isVisible)
   {
      pos.setVisible(isVisible);
   }

   public void setModifiedVisible(boolean isVisible)
   {
      modifiedLabel.setVisible(isVisible);
   }

   public void setLockVisible(boolean isVisible)
   {
      lockLabel.setVisible(isVisible);
   }

   public void setUnitVisible(boolean isVisible)
   {
      storageUnitLabel.setVisible(isVisible);
   }

   public void setInfoVisible(boolean isVisible)
   {
      infoField.setVisible(isVisible);
   }

   public void setHelpVisible(boolean isVisible)
   {
      helpButton.setVisible(isVisible);
   }

   public boolean isZoomVisible()
   {
      return zoomComp.isVisible();
   }

   public boolean isPositionVisible()
   {
      return pos.isVisible();
   }

   public boolean isModifiedVisible()
   {
      return modifiedLabel.isVisible();
   }

   public boolean isLockVisible()
   {
      return lockLabel.isVisible();
   }

   public boolean isUnitVisible()
   {
      return storageUnitLabel.isVisible();
   }

   public boolean isInfoVisible()
   {
      return infoField.isVisible();
   }

   public boolean isHelpVisible()
   {
      return helpButton.isVisible();
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private JLabel pos, modifiedLabel, lockLabel, storageUnitLabel;
   private JTextField infoField;
   private Icon lockIcon, unlockIcon;

   private ZoomComponent zoomComp;

   private Dimension posDim, modDim, unitDim, infoDim, lockDim;

   private FlowframTk application;

   private StorageUnitDialog storageUnitDialog;

   private JButton helpButton;

   private InfoDialog infoDialog;

   private String helpId = null;
   private NavigationNode navNode = null;
   private TargetRef targetRef = null;
}
