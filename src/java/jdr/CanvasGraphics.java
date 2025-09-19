// File          : CanvasGraphics.java
// Purpose       : Canvas graphics information
// Creation Date : 2013-12-17
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdr;

import java.io.*;
import java.text.MessageFormat;
import java.awt.Point;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Color;
import java.awt.geom.*;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import java.awt.print.PageFormat;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing graphical information.
 * @author Nicola L C Talbot
 */
public class CanvasGraphics 
  implements Serializable,Cloneable,JDRConstants
{
   public CanvasGraphics()
   {
      this((JDRMessage)null);
   }

   public CanvasGraphics(JDRMessage messageSystem)
   {
      this((Graphics2D)null, 1.0, (JComponent)null, messageSystem, null, null);
   }

   public CanvasGraphics(Graphics2D g2, double magnification,
     JComponent component, BrowseUtil browseUtil, 
     JFileChooser bitmapChooser)
   {
      this(g2, magnification, 
           component, new JDRDefaultMessage(), browseUtil, bitmapChooser);
   }

   public CanvasGraphics(Graphics2D g2, double magnification,
     JComponent component, JDRMessage msgSystem, 
     BrowseUtil browseUtil, JFileChooser bitmapChooser)
   {
      this(g2, magnification,  
           component, msgSystem, browseUtil, bitmapChooser, 10);
   }

   public CanvasGraphics(Graphics2D g2, double magnification,
     JComponent component, JDRMessage msgSystem, 
     BrowseUtil browseUtil, JFileChooser bitmapChooser,
     double normalsize)
   {
      setMagnification(magnification);
      setGraphicsDevice(g2);
      setBrowseUtil(browseUtil);
      setBitmapChooser(bitmapChooser);
      setComponent(component);
      setLaTeXFontBase(new LaTeXFontBase(msgSystem, normalsize));
      setMessageSystem(msgSystem);

      display_grid = false;
      grid_lock    = false;
      tool         = ACTION_SELECT;
      showRulers   = true;
      paper        = JDRPaper.A4;
      storageUnit  = JDRUnit.bp;
      pointSize    = new JDRLength(msgSystem, DEFAULT_BP_POINT_SIZE, storageUnit);

      grid = new JDRRectangularGrid(this);
      scaleControlPoint = true;
   }

   public synchronized void setComponent(JComponent comp)
   {
      component = comp;
   }

   public JComponent getComponent()
   {
      return component;
   }

   public synchronized void setMagnification(double magnification)
   {
      if (magnification <= 0.0)
      {
         throw new IllegalArgumentException("Invalid magnification "
           + magnification);
      }

      this.magnification = magnification;
   }

   public synchronized void setGraphicsDevice(Graphics2D g2)
   {
      this.g2 = g2;
   }

   public double getMagnification()
   {
      return magnification;
   }

   public Graphics2D getGraphics()
   {
      return g2;
   }

   // Converts the length (in storage units) to pixels with the
   // given magnification applied.

   public double storageToComponentX(double storageLength)
   {
      return bpToComponentX(getStorageUnit().toBp(storageLength));
   }

   public double storageToComponentY(double storageLength)
   {
      return bpToComponentY(getStorageUnit().toBp(storageLength));
   }

   public double bpToComponentX(double bpLength)
   {
      return getMagNormX()*bpLength;
   }

   public double bpToComponentY(double bpLength)
   {
      return getMagNormY()*bpLength;
   }

   public double componentXToBp(double componentLength)
   {
      return componentLength/getMagNormX();
   }

   public double componentYToBp(double componentLength)
   {
      return componentLength/getMagNormY();
   }

   public double getMagNormX()
   {
      if (normTransformX == 1)
      {
         return getMagnification();
      }
      else
      {
         return getMagnification() * normTransformX;
      }
   }

   public double getMagNormY()
   {
      if (normTransformY == 1.0)
      {
         return getMagnification();
      }
      else
      {
         return getMagnification() * normTransformY;
      }
   }

   // Converts the length (in storage units) to bp (no
   // magnification applied).
   public double storageToBp(double storageLength)
   {
      if (getStorageUnitID() == JDRUnit.BP)
      {
         return storageLength;
      }

      return getStorageUnit().toBp(storageLength);
   }

   public float storageToBp(float storageLength)
   {
      return (float)storageToBp((float)storageLength);
   }

   // Converts from bp to storage units
   public double bpToStorage(double bpLength)
   {
      if (getStorageUnitID() == JDRUnit.BP)
      {
         return bpLength;
      }

      return getStorageUnit().fromBp(bpLength);
   }

   public double toStorage(JDRLength length)
   {
      return length.getValue(getStorageUnit());
   }

   public double componentXToStorage(double componentCoord)
   {
      return getStorageUnit().fromBp(componentXToBp(componentCoord));
   }

   public double componentYToStorage(double componentCoord)
   {
      return getStorageUnit().fromBp(componentYToBp(componentCoord));
   }

   public Point2D componentToStorage(Point componentPt)
   {
      return new Point2D.Double(componentXToStorage(componentPt.getX()),
                                componentYToStorage(componentPt.getY()));
   }


   public Point storageToComponent(JDRPoint storagePoint)
   {
      return new Point((int)storageToComponentX(storagePoint.x),
                       (int)storageToComponentY(storagePoint.y));
   }

   public synchronized void setColor(Color col)
   {
      if (g2 != null)
      {
         g2.setColor(col);
      }
   }

   public synchronized void setPaint(Paint paint)
   {
      if (g2 != null)
      {
         g2.setPaint(paint);
      }
   }

   public Paint getPaint()
   {
      return g2 == null ? null : g2.getPaint();
   }

   public Font getFont()
   {
      return g2 == null ? null : g2.getFont();
   }

   public synchronized void setFont(Font font)
   {
      if (g2 == null) return;
      g2.setFont(font);
   }

   public synchronized void setStroke(Stroke stroke)
   {
      if (stroke instanceof JDRStroke)
      {
         ((JDRStroke)stroke).setCanvasGraphics(this);
      }

      if (g2 == null) return;

      g2.setStroke(stroke);
   }

   public Stroke getStroke()
   {
      return g2 == null ? null : g2.getStroke();
   }

   public void draw(Shape shape)
   {
      if (g2 == null) return;

      g2.draw(shape);
   }

   public void fill(Shape shape)
   {
      if (g2 == null) return;

      g2.fill(shape);
   }

   public void drawImage(Image image, AffineTransform af)
   {
      if (g2 == null) return;

      g2.drawImage(image, af, component);
   }

   public void drawString(String string, double x, double y)
   {
      if (g2 == null) return;

      g2.drawString(string, (int)x, (int)y);
   }

   public void drawStorageLine(double storageX0, double storageY0,
      double storageX1, double storageY1)
   {
      if (g2 == null) return;

      g2.drawLine((int)storageX0, (int)storageY0,
                  (int)storageX1, (int)storageY1);
   }

   public void drawMagLine(double storageX0, double storageY0,
      double storageX1, double storageY1)
   {
      if (g2 == null) return;

      double factorX = storageToComponentX(1.0);
      double factorY = storageToComponentY(1.0);

      double x0 = storageX0*factorX;
      double y0 = storageY0*factorY;

      double x1 = storageX1*factorX;
      double y1 = storageY1*factorY;

      g2.drawLine((int)x0, (int)y0, (int)x1, (int)y1);
   }

   public void drawBpLine(double storageX0, double storageY0,
      double storageX1, double storageY1)
   {
      if (g2 == null) return;

      JDRUnit unit = getStorageUnit();
      double factor = unit.toBp(1.0);

      double x0 = storageX0*factor;
      double y0 = storageY0*factor;

      double x1 = storageX1*factor;
      double y1 = storageY1*factor;

      g2.drawLine((int)x0, (int)y0, (int)x1, (int)y1);
   }

   public DoubleDimension getComponentPointSize()
   {
      double bpSize = getBpPointSize();

      double x = isScaleControlPointsEnabled() ?
       bpToComponentX(bpSize) :
       bpSize;

      double y = isScaleControlPointsEnabled() ?
       bpToComponentY(bpSize) :
       bpSize;

      return new DoubleDimension(x, y);
   }

   public DoubleDimension getStoragePointSize()
   {
      if (isScaleControlPointsEnabled())
      {
         double storageSize = pointSize.getValue(storageUnit);

         return new DoubleDimension(storageSize, storageSize);
      }

      double dx = pointSize.getValue(JDRUnit.bp)/getMagNormX();
      double dy = pointSize.getValue(JDRUnit.bp)/getMagNormY();

      return new DoubleDimension(bpToStorage(dx), bpToStorage(dy));
   }

   public synchronized void setPointSize(JDRLength size, boolean scalePointBbox)
   {
      if (size.getValue() <= 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.SETTING_POINTSIZE, size.toString(), this);
      }

      pointSize = size;

      setScaleControlPoints(scalePointBbox);
   }

   public Rectangle2D getPointRect(double storageX, double storageY)
   {
      return getPointRect(storageX, storageY, 0);
   }

   public Rectangle2D getPointRect(double storageX, double storageY, int bpBorder)
   {
      return getPointRect(null, storageX, storageY, bpBorder);
   }

   public Rectangle2D getPointRect(Rectangle2D rect, 
     double storageX, double storageY, int bpBorder)
   {
      return getScaledPointRect(rect, 
        storageToComponentX(storageX),
        storageToComponentY(storageY), bpBorder);
   }

   public Rectangle2D getScaledPointRect(Rectangle2D rect, 
     double scaledX, double scaledY, int bpBorder)
   {
      double sizeX = isScaleControlPointsEnabled() ?
       bpToComponentX(bpBorder+getBpPointSize()) :
       bpBorder+getBpPointSize();

      double sizeY = isScaleControlPointsEnabled() ?
       bpToComponentY(bpBorder+getBpPointSize()) :
       bpBorder+getBpPointSize();

      double halfSizeX = sizeX*0.5;
      double halfSizeY = sizeY*0.5;

      if (rect == null)
      {
         rect = new Rectangle2D.Double(
              scaledX-halfSizeX, scaledY-halfSizeY,
              sizeX, sizeY
            );
      }
      else
      {
         rect.setRect(
              scaledX-halfSizeX, scaledY-halfSizeY,
              sizeX, sizeY
            );
      }

      return rect;
   }

   public int getStorageUnitID()
   {
      return getStorageUnit().getID();
   }

   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      // Storage unit, preamble, docClass and useAbsolute
      // are explicitly saved by JDR.save

      jdr.writeBoolean(isGridDisplayed());
      jdr.writeBoolean(isGridLocked());
      jdr.writeBoolean(showRulers());

      if (version >= 1.8f)
      {
         jdr.writeInt(getTool());
      }
      else
      {
         int tool = getTool();
         jdr.writeInt(tool == JDRConstants.ACTION_MATH ? 
                      JDRConstants.ACTION_TEXT : tool);
      }

      if (version < 1.8f)
      {
         jdr.writeInt((int)getLaTeXNormalSize());
      }

      getPaper().save(jdr);

      JDRGrid grid = getGrid();

      if (version < 1.6f)
      {
         grid.getListener().write(jdr, grid);
      }
      else
      {
         JDRGridLoader loader = jdr.getGridLoader();
         loader.save(jdr, grid);
      }

      if (version >= 1.8f)
      {
         jdr.writeLength(getPointSize());
         jdr.writeBoolean(isScaleControlPointsEnabled());
      }

   }

   public void read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      // Storage unit and preamble are explicitly loaded by JDR.load

      boolean displayGrid = jdr.readBoolean(
         InvalidFormatException.SETTING_SHOW_GRID);

      boolean gridLock = jdr.readBoolean(
         InvalidFormatException.SETTING_GRID_LOCK);

      boolean showRulers = jdr.readBoolean(
         InvalidFormatException.SETTING_SHOW_RULERS);

      int tool = jdr.readInt(
         InvalidFormatException.SETTING_TOOL,
         0, MAX_TOOLS, true, false);

      int normalsize = 10;

      if (version < 1.8f)
      {
         normalsize = jdr.readIntGt(
            InvalidFormatException.SETTING_NORMALSIZE, 0);
      }

      JDRPaper paper = JDRPaper.read(jdr);

      if (useSettingsOnLoad == JDRAJR.ALL_SETTINGS)
      {
         setDisplayGrid(displayGrid);
         setGridLock(gridLock);
         setShowRulers(showRulers);
         setTool(tool);
         setLaTeXNormalSize(normalsize);
         setPaper(paper);
      }
      else if (useSettingsOnLoad == JDRAJR.PAPER_ONLY)
      {
         setPaper(paper);
      }

      JDRGrid grid;

      if (version < 1.6f)
      {
         byte unitID = jdr.readByte(
            InvalidFormatException.UNIT_ID);

         JDRUnit unit = JDRUnit.getUnit((int)unitID);

         if (unit == null)
         {
            throw new InvalidValueException(
               InvalidFormatException.UNIT_ID, unitID, jdr);
         }

         int majorDivisions = jdr.readIntGt(
            InvalidFormatException.GRID_MAJOR, 0);
         int subDivisions   = jdr.readIntGe(
            InvalidFormatException.GRID_MINOR, 0);

         grid = new JDRRectangularGrid(this, unit, majorDivisions,
               subDivisions);
      }
      else
      {
         JDRGridLoader loader = jdr.getGridLoader();
         grid = loader.load(jdr);
      }

      if (useSettingsOnLoad == JDRAJR.ALL_SETTINGS)
      {
         setGrid(grid);
      }

      JDRLength pointSize;
      boolean scaleControl = true;

      if (version < 1.8f)
      {
         pointSize = new JDRLength(this, DEFAULT_BP_POINT_SIZE, JDRUnit.bp);
      }
      else
      {
         pointSize = jdr.readNonNegLength(
           InvalidFormatException.SETTING_POINTSIZE);

         scaleControl = jdr.readBoolean(
           InvalidFormatException.SETTING_SCALE_POINT);
      }

      if (useSettingsOnLoad == JDRAJR.ALL_SETTINGS)
      {
         setPointSize(pointSize);
         setScaleControlPoints(scaleControl);
      }
   }

   public double getStoragePaperWidth()
   {
      return bpToStorage(getPaper().getWidth());
   }

   public double getStoragePaperHeight()
   {
      return bpToStorage(getPaper().getHeight());
   }

   public double getPaperWidth()
   {
      return getPaper().getWidth();
   }

   public double getPaperHeight()
   {
      return getPaper().getHeight();
   }

   public JDRPaper getPaper(boolean isPortrait)
   {
      return getPaper().getPaper(isPortrait);
   }

   public int getPaperID()
   {
      return getPaper().getID();
   }

   public String getPaperName()
   {
      return getPaper().getName();
   }

   public synchronized void setPaper(JDRPaper paper)
   {
      this.paper = paper;
   }

   public JDRPaper getPaper()
   {
      return paper;
   }

   public void setMessageSystem(JDRMessage msgSystem)
   {
      messageSystem = msgSystem;
   }

   public JDRMessage getMessageSystem()
   {
      return messageSystem;
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return messageSystem;
   }

   public BrowseUtil getBrowseUtil()
   {
      return browseUtil;
   }

   public void setBrowseUtil(BrowseUtil bUtil)
   {
      this.browseUtil = bUtil;
   }

   public void setBitmapChooser(JFileChooser fileChooser)
   {
      this.bitmapChooser = fileChooser;
   }

   public String chooseBitmap(File file)
   {
      bitmapChooser.setSelectedFile(file);

      int result = bitmapChooser.showOpenDialog(component);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         return bitmapChooser.getSelectedFile().getAbsolutePath();
      }

      return null;
   }

   public int getBrowseDialog(String filename)
   {
      return browseUtil.getBrowseDialog(filename);
   }

   public int getInvalidFormatDialog(String filename)
   {
      return browseUtil.getInvalidFormatDialog(filename);
   }

   public int getCantRefreshDialog(String filename)
   {
      return browseUtil.getCantRefreshDialog(filename);
   }

   public boolean allowsBitmapBrowse()
   {
      return browseUtil != null && bitmapChooser != null;
   }

   public Object clone()
   {
      CanvasGraphics cg = new CanvasGraphics(g2, magnification,
       component, messageSystem, browseUtil, bitmapChooser,
       latexFonts.getNormalSize());

      cg.makeEqual(this);

      cg.setPreamble(preamble);
      cg.setMidPreamble(midPreamble);
      cg.setEndPreamble(endPreamble);
      cg.setDocBody(docBody);
      cg.setMagicComments(magicComments);
      cg.setDocClass(docClass);

      cg.isEvenPage = isEvenPage;
      cg.setUseAbsolutePages(useAbsolutePages());

      return cg;
   }

   public synchronized void setLaTeXFontBase(LaTeXFontBase fonts)
   {
      latexFonts = fonts;
   }

   public LaTeXFontBase getLaTeXFontBase()
   {
      return latexFonts;
   }

   public double getLaTeXNormalSize()
   {
      return latexFonts.getNormalSize();
   }

   public synchronized void setLaTeXNormalSize(double size)
   {
      latexFonts.setNormalSize(size);
   }

   public double getStorageBaselineskip(int index)
   {
      return getStorageUnit().fromPt(latexFonts.getBaselineskip(index));
   }

   public void setUseSettingsOnLoad(int flag)
      throws JdrIllegalArgumentException
   {
      if (flag < 0 || flag > 2)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.USE_SETTINGS_ON_LOAD_ID, flag, this);
      }

      useSettingsOnLoad = flag;
   }

   public int getUseSettingsOnLoad()
   {
      return useSettingsOnLoad;
   }

   public synchronized void setBitmapReplaced(boolean flag)
   {
      bitmapReplaced = flag;
   }

   public boolean isBitmapReplaced()
   {
      return bitmapReplaced;
   }

   private void writeObject(ObjectOutputStream out)
      throws IOException
   {
      out.writeByte((byte)getStorageUnitID());

      // preamble

      int n = (preamble == null ? 0 : preamble.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(preamble.charAt(i));
      }

      // mid preamble

      n = (midPreamble == null ? 0 : midPreamble.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(midPreamble.charAt(i));
      }

      // end preamble

      n = (endPreamble == null ? 0 : endPreamble.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(endPreamble.charAt(i));
      }

      // document body

      n = (docBody == null ? 0 : docBody.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(docBody.charAt(i));
      }

      // magic comments

      n = (magicComments == null ? 0 : magicComments.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(magicComments.charAt(i));
      }

      // document class

      n = (docClass == null ? 0 : docClass.length());

      out.writeInt(n);

      for (int i = 0; i < n; i++)
      {
         out.writeChar(docClass.charAt(i));
      }

      out.writeBoolean(absolutePages);

      out.writeInt(optimize);
   }

   private void readObject(ObjectInputStream in)
      throws IOException
   {
      readObjectNoData();

      byte id = in.readByte();

      setStorageUnit(id);

      // preamble

      int n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setPreamble(new String(str));
      }

      // mid preamble

      n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setMidPreamble(new String(str));
      }

      // end preamble

      n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setEndPreamble(new String(str));
      }

      // document body

      n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setDocBody(new String(str));
      }

      // magic comments

      n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setMagicComments(new String(str));
      }

      // document class

      n = in.readInt();

      if (n > 0)
      {
         char[] str = new char[n];

         for (int i = 0; i < n; i++)
         {
            str[i] = in.readChar();
         }

         setDocClass(new String(str));
      }

      absolutePages = in.readBoolean();

      optimize = in.readInt();
   }

   private void readObjectNoData()
      throws ObjectStreamException
   {
      g2 = null;
      magnification = 1.0;
      component = null;
      useSettingsOnLoad = JDRAJR.ALL_SETTINGS;
      latexFonts = new LaTeXFontBase(null);
      messageSystem = null;
      browseUtil = null;
      bitmapChooser = null;
      bitmapReplaced = false;
      preamble = null;
      midPreamble = null;
      endPreamble = null;
      docBody = null;
      magicComments = null;
      docClass = null;
      absolutePages = false;
      optimize = OPTIMIZE_SPEED;
   }

   /*
    * Converts JDR left-handed co-ordinate system to
    * a right-handed co-ordinate system. This just converts the
    * y-co-ordinate to paper height minus y.
    */
   public Point2D leftToRightTransform(Point2D original, Point2D target)
   {
      if (target == null)
      {
         return new Point2D.Double(original.getX(),
                    leftToRightTransformY(target.getY()));
      }

      target.setLocation(original.getX(),
                         leftToRightTransformY(target.getY()));

      return target;
   }

   public double leftToRightTransformY(double originalY)
   {
      return bpToStorage(getPaperHeight()) - originalY;
   }

   public String warning(String tag, String alt)
   {
      if (messageSystem == null)
      {
         System.out.println(alt);
         return alt;
      }

      String message = messageSystem.getMessageWithFallback(tag, alt);

      messageSystem.getPublisher().publishMessages(
         MessageInfo.createWarning(message));

      return message;
   }

   public String warningMessage(String altFormat, String tag, Object... params)
   {
      if (messageSystem == null)
      {
         String message = MessageFormat.format(altFormat, params);
         System.out.println(message);
         return message;
      }

      String message = messageSystem.getMessageWithFallback(tag, altFormat, params);

      messageSystem.getPublisher().publishMessages(
        MessageInfo.createWarning(message));

      return message;
   }

   @Deprecated
   public String getString(String tag, String alt)
   {
      return getMessageWithFallback(tag, alt);
   }

   @Deprecated
   public String getString(int lineNum, String tag, String alt)
   {
      return getMessageWithFallback(lineNum, tag, alt);
   }

   @Deprecated
   public String getMessageWithAlt(String altFormat, String tag, Object... params)
   {
      return getMessageWithFallback(tag, altFormat, params);
   }

   public String getMessageWithFallback(String tag, String altFormat, Object... params)
   {
      if (messageSystem == null)
      {
         return MessageFormat.format(altFormat, params);
      }

      return messageSystem.getMessageWithFallback(tag, altFormat, params);
   }

   @Deprecated
   public String getMessageWithAlt(int lineNum, String altFormat, 
       String tag, Object... params)
   {
      return getMessageWithFallback(lineNum, tag, altFormat, params);
   }

   public String getMessageWithFallback(int lineNum,
     String tag, String altFormat, Object... params)
   {
      if (lineNum < 0)
      {
         return getMessageWithFallback(tag, altFormat);
      }

      if (messageSystem == null)
      {
         return MessageFormat.format("Line {0}: {1}", lineNum, 
            MessageFormat.format(altFormat, params));
      }

      String msg = messageSystem.getMessageWithFallback(tag, altFormat, params);

      return messageSystem.getMessageWithFallback(
        "error.with_line", "Line {0}: {1}", lineNum, msg);
   }

   public synchronized void setPreamble(String preambleText)
   {
      preamble = preambleText;
   }

   public String getPreamble()
   {
      return preamble;
   }

   public boolean hasPreamble()
   {
      return preamble != null && !preamble.isEmpty();
   }

   public synchronized void setMidPreamble(String preambleText)
   {
      midPreamble = preambleText;
   }

   public String getMidPreamble()
   {
      return midPreamble;
   }

   public boolean hasMidPreamble()
   {
      return midPreamble != null && !midPreamble.isEmpty();
   }

   public synchronized void setEndPreamble(String preambleText)
   {
      endPreamble = preambleText;
   }

   public String getEndPreamble()
   {
      return endPreamble;
   }

   public boolean hasEndPreamble()
   {
      return endPreamble != null && !endPreamble.isEmpty();
   }

   public synchronized void setDocBody(String text)
   {
      docBody = text;
   }

   public String getDocBody()
   {
      return docBody;
   }

   public boolean hasDocBody()
   {
      return docBody != null && !docBody.isEmpty();
   }

   public void setMagicComments(String text)
   {
      magicComments = text;
   }

   public String getMagicComments()
   {
      return magicComments;
   }

   public boolean hasMagicComments()
   {
      return magicComments != null && !magicComments.isEmpty();
   }

   public synchronized void setDocClass(String cls)
   {
      docClass = cls;
   }

   public String getDocClass()
   {
      return docClass;
   }

   public boolean hasDocClass()
   {
      return docClass != null && !docClass.isEmpty();
   }

   public AffineTransform getResetTransform()
   {
      return resetTransform;
   }

   public synchronized void setResetTransform(AffineTransform af)
   {
      resetTransform = af;
   }

   public void makeEqual(CanvasGraphics cg)
   {
      g2 = cg.g2;
      component = cg.component;
      messageSystem = cg.messageSystem;
      browseUtil = cg.browseUtil;
      bitmapChooser = cg.bitmapChooser;
      latexFonts.makeEqual(cg.latexFonts);
      preamble = cg.preamble;
      midPreamble = cg.midPreamble;
      endPreamble = cg.endPreamble;
      docBody = cg.docBody;
      magicComments = cg.magicComments;
      docClass = cg.docClass;
      absolutePages = cg.absolutePages;
      display_grid = cg.display_grid;
      grid_lock = cg.grid_lock;
      showRulers = cg.showRulers();
      paper = cg.paper;
      tool = cg.tool;
      storageUnit = cg.storageUnit;
      pointSize.makeEqual(cg.pointSize);
      scaleControlPoint = cg.scaleControlPoint;
      optimize = cg.optimize;
      setGrid((JDRGrid)cg.grid.clone());
      originX = cg.originX;
      originY = cg.originY;
   }

   /**
    * Sets the current tool.
    * @param toolSetting the new tool
    */
   public synchronized void setTool(int toolSetting)
      throws JdrIllegalArgumentException
   {
      if (toolSetting < 0 || toolSetting >= MAX_TOOLS)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.SETTING_TOOL_ID, toolSetting, this);
      }

      tool = toolSetting;
   }

   /**
    * Sets the current tool using the given string. The given
    * string must be one of those listed in TOOL_NAMES.
    * @param string the string identifying the tool
    */
   public synchronized void setTool(String string)
     throws JdrIllegalArgumentException
   {
      for (int i = 0; i < TOOL_NAMES.length; i++)
      {
         if (TOOL_NAMES[i].equals(string))
         {
            tool = i;
            return;
         }
      }

      throw new JdrIllegalArgumentException(
         JdrIllegalArgumentException.SETTING_TOOL_NAME, string, this);
   }

   /**
    * Gets the current tool.
    * @return the current tool
    */
   public int getTool()
   {
      return tool;
   }

   /**
    * Gets the string identifying the current tool.
    * @return the string identifying the current tool
    * @see #setTool(String)
    */
   public String getToolString()
   {
      return TOOL_NAMES[tool];
   }

   public void setGrid(JDRGrid theGrid)
   {
      grid = theGrid;
      grid.setCanvasGraphics(this);
   }

   public JDRGrid getGrid()
   {
      return grid;
   }

   public boolean isGridDisplayed()
   {
      return display_grid;
   }

   public synchronized void setDisplayGrid(boolean showGrid)
   {
      display_grid = showGrid;
   }

   public synchronized void setGridLock(boolean lockGrid)
   {
      grid_lock = lockGrid;
   }

   public boolean isGridLocked()
   {
      return grid_lock;
   }

   public synchronized void setMargins(PageFormat pf)
   {
      paper.setImageableArea(pf);
   }

   public Rectangle2D getImageableArea()
   {
      return paper.getImageableArea();
   }

   public double getOriginX()
   {
      return originX;
   }

   public void setOriginX(double x)
   {
      originX = x;
   }

   public double getOriginY()
   {
      return originY;
   }

   public void setOriginY(double y)
   {
      originY = y;
   }

   public JDRUnit getStorageUnit()
   {
      return storageUnit;
   }

   public synchronized void setStorageUnit(byte id)
   {
      JDRUnit unit = JDRUnit.getUnit(id);

      if (unit == null)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNIT_ID, id, this);
      }

      storageUnit = unit;
   }

   public synchronized void setStorageUnit(JDRUnit unit)
   {
      storageUnit = unit;
   }

   public double getBpPointSize()
   {
      return pointSize.getValue(JDRUnit.bp);
   }

   public JDRLength getPointSize()
   {
      return pointSize;
   }

   public synchronized void setPointSize(JDRLength point)
   {
      if (point.getValue() <= 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.SETTING_POINTSIZE, point.toString(),
           this);
      }

      pointSize = point;
   }

   public boolean showRulers()
   {
      return showRulers;
   }

   public synchronized void setShowRulers(boolean flag)
   {
      showRulers = flag;
   }

   public boolean isScaleControlPointsEnabled()
   {
      return scaleControlPoint;
   }

   public synchronized void setScaleControlPoints(boolean flag)
   {
      scaleControlPoint = flag;
   }

   public synchronized void setIsEvenPage(boolean iseven)
   {
      isEvenPage = iseven;
   }

   public boolean isEvenPage()
   {
      return isEvenPage;
   }

   public boolean useAbsolutePages()
   {
      return absolutePages;
   }

   public synchronized void setUseAbsolutePages(boolean flag)
   {
      absolutePages = flag;
   }

   public String getHeaderLabel()
   {
      return headerlabel;
   }

   public synchronized void setHeaderLabel(String label)
   {
      headerlabel = label;
   }

   public String getFooterLabel()
   {
      return footerlabel;
   }

   public synchronized void setFooterLabel(String label)
   {
      footerlabel = label;
   }

   public String getEvenHeaderLabel()
   {
      return evenheaderlabel;
   }

   public synchronized void setEvenHeaderLabel(String label)
   {
      evenheaderlabel = label;
   }

   public String getEvenFooterLabel()
   {
      return evenfooterlabel;
   }

   public synchronized void setEvenFooterLabel(String label)
   {
      evenfooterlabel = label;
   }

   /**
    * Returns optimize setting. This may be {@link #OPTIMIZE_SPEED} (speed is more important
    * than memory restrictions) or {@link #OPTIMIZE_MEMORY}
    * (reducing memory requirements is more important that speed)
    * or {@link #OPTIMIZE_NONE} (don't attempt optimization). In
    * most cases {@link #OPTIMIZE_MEMORY} and {@link #OPTIMIZE_NONE}
    * are treated the same. {@link #OPTIMIZE_NONE} suppresses the
    * creation of a background image to reduce the canvas redrawing.
    * @return {@link #OPTIMIZE_SPEED} or 
    * {@link #OPTIMIZE_MEMORY} or {@link #OPTIMIZE_NONE}
    */
   public int getOptimize()
   {
      return optimize;
   }

   /**
    * Sets whether speed is more important than memory restrictions
    * or whether reducing memory requirements is more important
    * than speed.
    * @param setting may be either {@link #OPTIMIZE_SPEED} or
    * {@link #OPTIMIZE_MEMORY}
    * @throws IllegalArgumentException if argument is invalid
    */
   public void setOptimize(int setting)
   throws JdrIllegalArgumentException
   {
      if (!(setting == OPTIMIZE_SPEED || setting == OPTIMIZE_MEMORY
         || setting == OPTIMIZE_NONE))
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.OPTIMIZE, setting, this);
      }

      optimize = setting;
   }

   /**
    * Speed is more important than memory requirements.
    */
   public static final int OPTIMIZE_SPEED=0;
   /**
    * Conserving memory is more important that speed.
    */
   public static final int OPTIMIZE_MEMORY=1;

   /**
    * Don't try optimizing.
    */ 
   public static final int OPTIMIZE_NONE=2;

   /**
    * Determines whether speed or conserving memory are more important.
    */
   private int optimize=OPTIMIZE_SPEED;

   // TODO
   // Now that the copy & paste uses the JDR binary format instead
   // of serialization all volatile and transient can probably be
   // removed.

   /**
    * Indicates whether to show the grid.
    */
   protected volatile boolean display_grid;

   /**
    * Indicates whether to lock the grid.
    */
   protected volatile boolean grid_lock;
   /**
    * Indicates whether to show the rulers.
    */
   protected volatile boolean showRulers;

   /**
    * Indicates the current tool.
    */
   private volatile int tool;

   /**
    * Stores the current paper.
    */
   private volatile JDRPaper paper;

   /**
    * Stores the grid.
    */
   private volatile JDRGrid grid;

   /**
    * Storage unit. This is independent of the grid unit(s).
    */
   private volatile JDRUnit storageUnit;

   private double originX=0.0, originY=0.0; // TODO

   /**
    * Point size.
    */
   private volatile JDRLength pointSize;

   private volatile boolean scaleControlPoint;

   public static double DEFAULT_BP_POINT_SIZE = 10;

   public static final String[] TOOL_NAMES = new String[]
   {
      "select", "open_line", "closed_line", "open_curve",
      "closed_curve", "rectangle", "ellipse", "text", "math"
   };

   private volatile Graphics2D g2;
   private double magnification;
   private transient JComponent component;

   private transient AffineTransform resetTransform;

   private volatile int useSettingsOnLoad = JDRAJR.ALL_SETTINGS;

   private volatile LaTeXFontBase latexFonts;

   private transient JDRMessage messageSystem;

   private transient BrowseUtil browseUtil;

   private transient JFileChooser bitmapChooser;

   private volatile String preamble = null;

   private volatile String midPreamble = null;

   private volatile String endPreamble = null;

   private volatile String docBody = null;

   // header block designed for directives/magic comments
   private String magicComments = null;

   private volatile String docClass = null;

   private volatile boolean bitmapReplaced = false;

   public static double normTransformX = 1.0;
   public static double normTransformY = 1.0;

   private volatile boolean isEvenPage = false;

   private volatile boolean absolutePages = false;

   private volatile String headerlabel = "header";
   private volatile String evenheaderlabel = "evenheader";

   private volatile String footerlabel = "footer";
   private volatile String evenfooterlabel = "evenfooter";
}

