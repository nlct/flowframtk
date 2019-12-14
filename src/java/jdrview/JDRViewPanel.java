// File          : JDRViewPanel.java
// Description   : Panel used to display JDR/AJR image
// Creation Date : 4th June 2008
// Author        : Nicola L C Talbot
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

package com.dickimawbooks.jdrview;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;

/**
 * Panel used to display JDR/AJR image.
 * @author Nicola L C Talbot
 */
public class JDRViewPanel extends JPanel 
{
   /**
    * Creates instance of this panel.
    * @param application the application using this panel
    */
   public JDRViewPanel(JDRView application)
   {
      super();

      app = application;

      setBackground(Color.lightGray);

      ToolTipManager toolTipManager = ToolTipManager.sharedInstance();

      toolTipManager.registerComponent(this);
   }

   public String getToolTipText(MouseEvent e)
   {
      JDRGroup image = app.getImage();

      if (image == null)
      {
         return null;
      }

      CanvasGraphics cg = image.getCanvasGraphics();

      // Get mouse position
      Point p = e.getPoint();

      Point2D point = new Point2D.Double(
        cg.componentXToStorage(p.x), cg.componentYToStorage(p.y));

      String text = image.getDescription();

      // Is there anything located at this position?

      for (int i = image.size()-1; i >= 0; i--)
      {
         JDRCompleteObject object = image.get(i);

         if (object.containsStoragePoint(point.getX(), point.getY()))
         {
            text = object.getDescription();

            if (text != null && !text.isEmpty())
            {
               break;
            }
         }
      }

      if (text == "")
      {
         return null;
      }

      return text;
   }

   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      JDRGroup paths = app.getImage();

      if (paths == null)
      {
         return;
      }

      Graphics2D g2 = (Graphics2D)g;

      CanvasGraphics cg = paths.getCanvasGraphics();

      cg.setGraphicsDevice(g2);

      AffineTransform oldAf = g2.getTransform();
      Stroke oldStroke = g2.getStroke();

      double bpToCompXScale = cg.bpToComponentX(1.0);
      double bpToCompYScale = cg.bpToComponentY(1.0);
      double storageToCompXScale = cg.storageToComponentX(1.0);
      double storageToCompYScale = cg.storageToComponentY(1.0);
      double bpToStorage = cg.bpToStorage(1.0);

      double bpPaperWidth = cg.getPaperWidth();
      double bpPaperHeight = cg.getPaperHeight();

      g2.setPaint(Color.white);
      g2.fill(new Rectangle2D.Double(
         0, 0,
         bpToCompXScale*bpPaperWidth,
         bpToCompYScale*bpPaperHeight));

      g2.scale(storageToCompXScale, storageToCompYScale);

      JDRPaper paper = app.getPaper();

      RenderingHints oldHints = g2.getRenderingHints();
      g2.setRenderingHints(app.getRenderingHints());

      g2.setPaint(Color.lightGray);

      FlowFrame typeblock = paths.getFlowFrame();

      if (typeblock != null)
      {
         typeblock.draw(
            new BBox(cg, 0,0, bpPaperWidth*bpToStorage,
                              bpPaperHeight*bpToStorage));
      }

      for (int i = 0, n=paths.size(); i < n; i++)
      {
          JDRCompleteObject object = paths.get(i);

          object.draw(false);
      }

      g2.setRenderingHints(oldHints);

      g2.setTransform(oldAf);
   }

   private JDRView app;
}
