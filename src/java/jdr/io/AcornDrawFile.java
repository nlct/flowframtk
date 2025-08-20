/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.HashMap;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.marker.*;

/**
 * Functions to save and load Acorn DrawFile format.
 * https://www.riscosopen.org/wiki/documentation/show/File%20formats:%20DrawFile
 */

public class AcornDrawFile
{
   private AcornDrawFile(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   private AcornDrawFile(CanvasGraphics cg, DataInputStream din)
   {
      this(cg);
      image = new JDRGroup(cg);
      currentGroup = image;
      affineTransform = new AffineTransform(
       DRAW_PT_TO_CM, 0, 0, -DRAW_PT_TO_CM, 0, 0);
      this.din = din;
   }

   public static JDRGroup load(CanvasGraphics cg, DataInputStream din)
   throws IOException,InvalidFormatException
   {
      AcornDrawFile arf = new AcornDrawFile(cg, din);

      arf.readData();

      return arf.image;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public JDRMessage getMessageSystem()
   {
      return canvasGraphics.getMessageSystem();
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      return getMessageSystem().getMessageWithFallback(label, fallbackFormat, params);
   }

   public void printlnMessage(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createMessage(message));
   }

   public void printMessage(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createMessage(message, false));
   }

   public void printlnVerbose(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createVerbose(1, message));
   }

   public void printVerbose(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createVerbose(1, message, false));
   }

   public void printlnDebug(String message)
   {
      if (isDebuggingOn())
      {
         printlnMessage(message);
      }
   }

   public void printDebug(String message)
   {
      if (isDebuggingOn())
      {
         printMessage(message);
      }
   }

   public boolean isDebuggingOn()
   {
      return getMessageSystem().isDebuggingOn();
   }

   /**
    * Resets message system if verbose level greater than 0.
    */
   public void resetProgress()
   {
      if (getVerbosity() > 0)
      {
         getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createSetProgress(0));
      }
   }

   /**
    * Sets indeterminate message system if verbose level greater than 0.
    */
   public void setIndeterminate(boolean indeterminate)
   {
      if (getVerbosity() > 0)
      {
         getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createIndeterminate(indeterminate));
      }
   }

   /**
    * Increments message system progress if verbose level greater than 0.
    */
   public void incrementProgress()
   {
      if (getVerbosity() > 0)
      {
         getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createIncProgress());
      }
   }

   /**
    * Gets verbose value.
    * @return verbose value
    */
   public int getVerbosity()
   {
      return getMessageSystem().getVerbosity();
   }


   protected void readData() throws IOException,InvalidFormatException
   {
      String word = readString(4);

      if (!word.equals("Draw"))
      {
         throw new InvalidFormatException(
          getMessageWithFallback("error.not_acorn_drawfile", "Not an Acorn Draw file"));
      }

      majorVersion = readInt();

      printlnVerbose(getMessageWithFallback("message.acorn_drawfile.major",
        "Major version: {0}", majorVersion));

      minorVersion = readInt();

      printlnVerbose(getMessageWithFallback("message.acorn_drawfile.minor",
        "Minor version: {0}", minorVersion));

      producer = readString(12).trim();

      printlnVerbose(getMessageWithFallback("message.acorn_drawfile.producer", 
        "Producer: {0}", producer));

      lowBoundingX = readInt();
      lowBoundingY = readInt();
      highBoundingX = readInt();
      highBoundingY = readInt();

      printlnVerbose(getMessageWithFallback("message.acorn_drawfile.bounding_box", 
        "Bounding Box: ({0},{1}) ({2},{3})", 
        lowBoundingX, lowBoundingY, highBoundingX, highBoundingY));

      int objectId;

      while (true)
      {
         try
         {
            objectId = readInt();
            readObject(objectId);
         }
         catch (EOFException e)
         {
            break;
         }
      }
   }

   protected void readObject(int objectId)
   throws IOException,InvalidFormatException
   {
System.out.println("OBJECT ID: "+objectId);
      objectSize = readInt(); // Word aligned size, including header
System.out.println("SIZE: "+objectSize);

      switch (objectId)
      {
         case OBJECT_FONT_TABLE:
           System.out.println("FONT");
           readFontTable();
         break;
         case OBJECT_TEXT:
           System.out.println("TEXT");
           readText();
         break;
         case OBJECT_PATH:
           readPath();
         break;
         case OBJECT_SPRITE:
           System.out.println("SPRITE");
         break;
         case OBJECT_GROUP:
           readGroup();
         break;
         case OBJECT_TAGGED:
           System.out.println("TAGGED");
           readTagged();
         break;
         case OBJECT_TEXTAREA:
           System.out.println("TEXTAREA");
         break;
         case OBJECT_TEXTCOLUMN:
           System.out.println("TEXTCOLUMN");
         break;
         case OBJECT_OPTIONS:
           readOptions();
         break;
         case OBJECT_TRANSFORMED_TEXT:
           System.out.println("TRANSFORMED_TEXT");
         break;
         case OBJECT_TRANSFORMED_SPRITE:
           System.out.println("TRANSFORMED_SPRITE");
         break;
         case OBJECT_JPEG:
           System.out.println("JPEG");
         break;
         case OBJECT_GRID:
           System.out.println("GRID");
           readGrid();
         break;
         default:
System.out.println("UNKNOWN OBJECT ID "+objectId);
/*
         throw new UnsupportedFeatureException(
          getMessageWithFallback("error.acorn_drawfile.unsupported_object_id",
           "Unsupported object identifier: {0}", objectId));
*/
      }
   }

   protected void readOptions()
   throws IOException,InvalidFormatException
   {
      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      CanvasGraphics cg = image.getCanvasGraphics();

      int paperSize = readInt();

      int paperLimit = readInt();

      showLimits =
        ((paperLimit & PAPER_LIMIT_SHOW) == PAPER_LIMIT_SHOW);

      isLandscape =
        ((paperLimit & PAPER_LIMIT_LANDSCAPE) == PAPER_LIMIT_LANDSCAPE);

      JDRPaper paper = cg.getPaper();

      switch (paperSize)
      {
         case PAPER_A0:
           paper = (isLandscape ? JDRPaper.A0R : JDRPaper.A0);
         break;
         case PAPER_A1:
           paper = (isLandscape ? JDRPaper.A1R : JDRPaper.A1);
         break;
         case PAPER_A2:
           paper = (isLandscape ? JDRPaper.A2R : JDRPaper.A2);
         break;
         case PAPER_A3:
           paper = (isLandscape ? JDRPaper.A3R : JDRPaper.A3);
         break;
         case PAPER_A4:
           paper = (isLandscape ? JDRPaper.A4R : JDRPaper.A4);
         break;
         case PAPER_A5:
           paper = (isLandscape ? JDRPaper.A5R : JDRPaper.A5);
         break;
         case PAPER_A6:
           paper = (isLandscape ? JDRPaper.A6R : JDRPaper.A6);
         break;
         case PAPER_A7:
           paper = (isLandscape ? JDRPaper.A7R : JDRPaper.A7);
         break;
      }

      printlnVerbose(getMessageWithFallback("message.acorn_drawfile.papersize", 
        "Paper size: {0}", paper));

      cg.setPaper(paper);

      gridSpacing = readDouble();
      gridDivisions = readInt();

      isometricGrid = (readInt() != 0);
      autoAdjust = (readInt() != 0);
      showGrid = (readInt() != 0);
      lockGrid = (readInt() != 0);

      gridInches = (readInt() == 0);

      JDRUnit unit;

      if (gridInches)
      {
         unit = JDRUnit.in;
      }
      else
      {
         unit = JDRUnit.cm;
      }

      cg.setStorageUnit(unit);
      JDRGrid grid;

      if (isometricGrid)
      {
         grid = new JDRIsoGrid(cg, unit, gridSpacing, gridDivisions);
      }
      else
      {
         grid = new JDRRectangularGrid(cg, unit, gridSpacing, gridDivisions);
      }

      cg.setGrid(grid);
      cg.setDisplayGrid(showGrid);
      cg.setGridLock(lockGrid);

      affineTransform.setTransform(
       drawPointToUnit(1), 0, 0, -drawPointToUnit(1), 
       0.0, unit.fromBp(paper.getHeight()));

      readInt(); // zoom multiplier (1-8)
      readInt(); // zoom divider (1-8)
      readInt(); // zoom locking (0-> none, otherwise powers of 2)

      showTools = (readInt() != 0);// show/hide toolbox

      int drawTool = readInt(); // current tool (initial entry mode)

      try
      {
         image.getCanvasGraphics().setTool(getJdrTool(drawTool));
      }
      catch (JdrIllegalArgumentException e)
      {// shouldn't happen
      }

      readInt(); // undo buffer size (in bytes)
   }

   protected void readGrid()
   throws IOException,InvalidFormatException
   {
      // bounding box
      int lowX = readInt();
System.out.println("lowX: "+lowX);
      int lowY = readInt();
System.out.println("lowY: "+lowY);
      int highX = readInt();
System.out.println("highX: "+highX);
      int highY = readInt();
System.out.println("highY: "+highY);

      int value;
      value = readInt();
      value = readInt();
      value = readInt();// y-only subdivisions
      value = readInt();// y-only inch or cm
      value = readInt();// colour id (0-15)
   }

   protected void readPath()
   throws IOException,InvalidFormatException
   {
      // bounding box
      int lowX = readInt();
System.out.println("lowX: "+lowX);
      int lowY = readInt();
System.out.println("lowY: "+lowY);
      int highX = readInt();
System.out.println("highX: "+highX);
      int highY = readInt();
System.out.println("highY: "+highY);

      CanvasGraphics cg = image.getCanvasGraphics();

      int fillCol = readInt();

      JDRPaint fillPaint;

      if (fillCol == -1)
      {
         fillPaint = new JDRTransparent(cg);
      }
      else
      {
         fillPaint = new JDRColor(cg, new Color(fillCol));
      }

      int lineCol = readInt();
      JDRPaint linePaint;

      if (lineCol == -1)
      {
         linePaint = new JDRTransparent(cg);
      }
      else
      {
         linePaint = new JDRColor(cg, new Color(lineCol));
      }

      int outlineWidth = readInt();

      JDRBasicStroke stroke = new JDRBasicStroke(cg);
      JDRLength penWidth = new JDRLength(cg, outlineWidth/640.0, JDRUnit.bp);

      if (outlineWidth > 0)
      {
         stroke.setPenWidth(penWidth);
      }

      int pathStyle = readInt();

      int joinStyle = (pathStyle & PATH_STYLE_JOIN);

      switch (joinStyle)
      {
         case PATH_STYLE_JOIN_MITRED:
           stroke.setJoinStyle(BasicStroke.JOIN_MITER);
         break;
         case PATH_STYLE_JOIN_ROUND:
           stroke.setJoinStyle(BasicStroke.JOIN_ROUND);
         break;
         case PATH_STYLE_JOIN_BEVELLED:
           stroke.setJoinStyle(BasicStroke.JOIN_BEVEL);
         break;
      }

      int capStyle = (pathStyle & PATH_STYLE_START_CAP);
      int startCap = BasicStroke.CAP_BUTT;
      boolean hasTriangleStart = false;

      switch (capStyle)
      {
         case PATH_STYLE_START_CAP_BUTT:
           startCap = BasicStroke.CAP_BUTT;
         break;
         case PATH_STYLE_START_CAP_ROUND:
           startCap = BasicStroke.CAP_ROUND;
         break;
         case PATH_STYLE_START_CAP_SQUARE:
           startCap = BasicStroke.CAP_SQUARE;
         break;
         case PATH_STYLE_START_CAP_TRIANGLE:
            hasTriangleStart = true;
         break;
      }

      capStyle = (pathStyle & PATH_STYLE_END_CAP);
      int endCap = BasicStroke.CAP_BUTT;
      boolean hasTriangleEnd = false;

      switch (capStyle)
      {
         case PATH_STYLE_END_CAP_BUTT:
           endCap = BasicStroke.CAP_BUTT;
         break;
         case PATH_STYLE_END_CAP_ROUND:
           endCap = BasicStroke.CAP_ROUND;
         break;
         case PATH_STYLE_END_CAP_SQUARE:
           endCap = BasicStroke.CAP_SQUARE;
         break;
         case PATH_STYLE_END_CAP_TRIANGLE:
            hasTriangleEnd = true;
         break;
      }

      int triangleWidth = (pathStyle & PATH_STYLE_TRIANGLE_WIDTH)>>16;
      int triangleHeight = (pathStyle & PATH_STYLE_TRIANGLE_HEIGHT)>>24;

      if (startCap != endCap || hasTriangleStart || hasTriangleEnd)
      {
         stroke.setCapStyle(BasicStroke.CAP_BUTT);

         JDRMarker marker = null;

         if (hasTriangleStart)
         {
// TODO
            marker = JDRMarker.getPredefinedMarker(canvasGraphics,
              JDRMarker.ARROW_TRIANGLE_CAP);
         }
         else if (startCap == BasicStroke.CAP_SQUARE)
         {
            marker = new ArrowRectangleCap(penWidth, 1, false, 
               new JDRLength(cg, 0.5*penWidth.getValue(), penWidth.getUnit()));
         }
         else if (startCap == BasicStroke.CAP_ROUND)
         {
            marker = new ArrowRoundCap(penWidth, 1, false, 
               new JDRLength(cg, 0.5*penWidth.getValue(), penWidth.getUnit()));
         }

         if (marker != null)
         {
            stroke.setStartArrow(marker);
            marker = null;
         }

         if (hasTriangleEnd)
         {
// TODO
            marker = JDRMarker.getPredefinedMarker(canvasGraphics,
              JDRMarker.ARROW_TRIANGLE_CAP);
         }
         else if (endCap == BasicStroke.CAP_SQUARE)
         {
            marker = new ArrowRectangleCap(penWidth, 1, false, 
               new JDRLength(cg, 0.5*penWidth.getValue(), penWidth.getUnit()));
         }
         else if (endCap == BasicStroke.CAP_ROUND)
         {
            marker = new ArrowRoundCap(penWidth, 1, false, 
               new JDRLength(cg, 0.5*penWidth.getValue(), penWidth.getUnit()));
         }

         if (marker != null)
         {
            stroke.setEndArrow(marker);
         }

      }
      else
      {
         stroke.setCapStyle(startCap);
      }

      if ((pathStyle & PATH_STYLE_WINDING_RULE_EVEN_ODD)
            == PATH_STYLE_WINDING_RULE_EVEN_ODD)
      {
         stroke.setWindingRule(GeneralPath.WIND_EVEN_ODD);
      }
      else
      {
         stroke.setWindingRule(GeneralPath.WIND_NON_ZERO);
      }

      if ((pathStyle & PATH_STYLE_PATTERN_DASH)
            == PATH_STYLE_PATTERN_DASH)
      {
         int offset = readInt(); // user co-ordinates
         float dashOffset = (float)drawPointToUnit(offset);
         int n = readInt();
         float[] pattern = new float[n];

         for (int i = 0; i < n; i++)
         {
            pattern[i] = (float)drawPointToUnit(readInt());
         }

         stroke.setDashPattern(new DashPattern(cg, pattern, dashOffset));
      }

      JDRPath path = new JDRPath(linePaint, fillPaint, stroke);

      int tag;
      double[] coords = new double[6];
      double prevX=0.0, prevY=0.0;

      while ((tag = readInt()) != PATH_TAG_END)
      {
         switch (tag)
         {
            case PATH_TAG_MOVE:
              coords[0] = (double)readInt();
              coords[1] = (double)readInt();

              affineTransform.transform(coords, 0, coords, 0, 1);

              if (!path.isEmpty())
              {
                 path.add(new JDRSegment(cg, prevX, prevY, coords[0], coords[1]));
              }

              prevX = coords[0];
              prevY = coords[1];

            break;
            case PATH_TAG_LINE:
              coords[0] = (double)readInt();
              coords[1] = (double)readInt();

              affineTransform.transform(coords, 0, coords, 0, 1);

              path.add(new JDRLine(cg, prevX, prevY, coords[0], coords[1]));

              prevX = coords[0];
              prevY = coords[1];
            break;
            case PATH_TAG_BEZIER:
              coords[0] = (double)readInt();
              coords[1] = (double)readInt();
              coords[2] = (double)readInt();
              coords[3] = (double)readInt();
              coords[4] = (double)readInt();
              coords[5] = (double)readInt();

              affineTransform.transform(coords, 0, coords, 0, 3);

              path.add(new JDRBezier(cg, prevX, prevY, 
               coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));

              prevX = coords[4];
              prevY = coords[5];
            break;
            case PATH_TAG_CLOSE:
              path.close();
            break;
         }
      }

      if (!path.isEmpty())
      {
         currentGroup.add(path);
      }
   }

   protected void readGroup() throws IOException,InvalidFormatException
   {
      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      JDRGroup prevGroup = currentGroup;
      JDRGroup group = new JDRGroup(image.getCanvasGraphics());
      currentGroup.add(group);
      currentGroup = group;

      int groupSize = objectSize;
      int currentSize = 32;

      String name = readString(12).trim();

      if (!name.isEmpty())
      {
         group.setDescription(name);
      }

      while (currentSize < groupSize)
      {
         int objectId = readInt();
         readObject(objectId);

         currentSize += objectSize;
      }

      currentGroup = prevGroup;
   }

   protected void readTagged() throws IOException,InvalidFormatException
   {
      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

     // not sure about this

     int id = readInt();

     int objectId = readInt();
     readObject(objectId);
   }

   protected void readFontTable() throws IOException,InvalidFormatException
   {
      int oldBytesRead = bytesRead;
      byte id = readByte();

      String fontName = readString();

      if (fontTable == null)
      {
         fontTable = new HashMap<Byte,JDRFont>();
      }

      int idx = fontName.indexOf(".");
      String family = fontName;
      String variant = "";

      if (idx > 0)
      {
         family = fontName.substring(0, idx);
         variant = fontName.substring(idx+1);
      }

      if (family.equals("System") || family.equals("Portrhouse"))
      {
         family = "Monospaced";
      }
      else if (family.equals("Homerton")
            || family.equals("Sassoon")
              )
      {
         family = "SansSerif";
      }
// TODO Selwyn, Sidney
      else
      {
         family = "Serif";
      }

      int weight = JDRFont.SERIES_MEDIUM;
      int shape = JDRFont.SHAPE_UPRIGHT;

      if (variant.contains("Oblique"))
      {
         shape = JDRFont.SHAPE_SLANTED;
      }
      else if (variant.contains("Italic"))
      {
         shape = JDRFont.SHAPE_ITALIC;
      }

      if (variant.contains("Bold"))
      {
         weight = JDRFont.SERIES_BOLD;
      }

      JDRLength size = new JDRLength(image.getCanvasGraphics(), 
        10, JDRUnit.bp);

      fontTable.put(Byte.valueOf(id), new JDRFont(family, weight, shape, size));

      readString((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected JDRFont getFont(byte b)
   {
      return fontTable == null ? null : fontTable.get(Byte.valueOf(b));
   }

   protected void readText() throws IOException,InvalidFormatException
   {
      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      CanvasGraphics cg = image.getCanvasGraphics();

      int textCol = readInt();

      JDRPaint textPaint;

      if (textCol == -1)
      {
         textPaint = new JDRTransparent(cg);
      }
      else
      {
         textPaint = new JDRColor(cg, new Color(textCol));
      }

      int backCol = readInt();// background hint

      int textStyle = readInt();
      // lowest byte is the font number
      // the remainder should be 0

      JDRFont font = getFont(dataBuffer[0]);

      int xSize = readInt(); // 1/640 of a point
      int ySize = readInt(); // draw units

      JDRLength fontSize = new JDRLength(cg, ((double)ySize)/640.0, JDRUnit.bp);

      if (xSize != ySize)
      {// TODO
      }

      String family = "Monospaced";
      int weight = JDRFont.SERIES_MEDIUM;
      int shape = JDRFont.SHAPE_UPRIGHT;

      if (font != null)
      {
         family = font.getFamily();
         weight = font.getWeight();
         shape = font.getShape();
      }

      int x = readInt();
      int y = readInt();

      Point2D.Double p = new Point2D.Double(x, y);
      affineTransform.transform(p, p);

      String text = readString(); // zero terminated string padding

      JDRText jdrText = new JDRText(cg, p, family, weight, shape, fontSize, text);
      jdrText.setTextPaint(textPaint);

      currentGroup.add(jdrText);

      readString((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected byte[] getDataBuffer(int length)
   {
      if (dataBuffer == null || dataBuffer.length < length)
      {
         dataBuffer = new byte[length];
      }

      return dataBuffer;
   }

   protected StringBuilder getStringBuffer(int length)
   {
      if (stringBuffer == null)
      {
         stringBuffer = new StringBuilder(length);
      }
      else
      {
         stringBuffer.setLength(0);
      }

      return stringBuffer;
   }

   public byte readByte() throws IOException
   {
      byte b = din.readByte();
      bytesRead++;
      return b;
   }

   public String readString(int length) throws IOException
   {
      if (length <= 0) return "";

      getDataBuffer(length);

      int r = din.read(dataBuffer, 0, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readString: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(""+octet(dataBuffer[i]));
         }

         if (r == -1)
         {
            printlnDebug("");
         }
      }

      if (r == -1)
      {
         throw new EOFException();
      }

      bytesRead += length;

      getStringBuffer(length);

      for (int i = 0; i < length; i++)
      {
         stringBuffer.append((char)dataBuffer[i]);
      }

      if (isDebuggingOn())
      {
         printlnDebug("\t"+stringBuffer);
      }

      return stringBuffer.toString();
   }

   public String readString() throws IOException
   {
      // read to null

      getStringBuffer(256);

      byte b;

      while ((b = readByte()) != 0)
      {
         stringBuffer.append((char)b);
      }

      return stringBuffer.toString();
   }

   public int readInt() throws IOException
   {
      int length = 4;

      getDataBuffer(length);
      int r = din.read(dataBuffer, 0, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readInt: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(""+octet(dataBuffer[i]));
         }

         if (r == -1)
         {
            printlnDebug("");
         }
      }

      if (r == -1)
      {
         throw new EOFException();
      }

      bytesRead += length;

      getStringBuffer(length);

      stringBuffer.append(String.format("%s%s%s%s",
        octet(dataBuffer[3]), 
        octet(dataBuffer[2]),
        octet(dataBuffer[1]), 
        octet(dataBuffer[0])));

      int value = (int)Long.parseLong(stringBuffer.toString(), 16);

      if (isDebuggingOn())
      {
         printlnDebug("\t"+value);
      }

      return value;
   }

   protected String octet(byte b)
   {
      String str = String.format("%X", b);

      return str.length() == 1 ? "0"+str : str;
   }

   public double readDouble() throws IOException
   {
      int length = 8;

      getDataBuffer(length);
      int r = din.read(dataBuffer, 0, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readDouble: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(""+octet(dataBuffer[i]));
         }

         if (r == -1)
         {
            printlnDebug("");
         }
      }

      if (r == -1)
      {
         throw new EOFException();
      }

      bytesRead += length;

      getStringBuffer(length);

      stringBuffer.append(String.format("%s%s%s%s%s%s%s%s",
        octet(dataBuffer[3]), 
        octet(dataBuffer[2]),
        octet(dataBuffer[1]), 
        octet(dataBuffer[0]),
        octet(dataBuffer[7]), 
        octet(dataBuffer[6]),
        octet(dataBuffer[5]), 
        octet(dataBuffer[4])
      ));

      long longHex = Long.parseUnsignedLong(stringBuffer.toString(), 16); 
      double value = Double.longBitsToDouble(longHex);

      if (isDebuggingOn())
      {
         printlnDebug("\t"+value);
      }

      return value;
   }

   public int getJdrTool(int drawTool)
   {
      switch (drawTool)
      {
         case ACORN_TOOL_LINE:
            return JDRConstants.ACTION_OPEN_LINE;
         case ACORN_TOOL_CLOSED_LINE:
            return JDRConstants.ACTION_CLOSED_LINE;
         case ACORN_TOOL_CURVE:
            return JDRConstants.ACTION_OPEN_CURVE;
         case ACORN_TOOL_CLOSED_CURVE:
            return JDRConstants.ACTION_CLOSED_CURVE;
         case ACORN_TOOL_RECTANGLE:
            return JDRConstants.ACTION_RECTANGLE;
         case ACORN_TOOL_ELLIPSE:
            return JDRConstants.ACTION_ELLIPSE;
         case ACORN_TOOL_TEXTLINE:
            return JDRConstants.ACTION_TEXT;
         case ACORN_TOOL_SELECT:
            return JDRConstants.ACTION_SELECT;
      }

      return JDRConstants.ACTION_SELECT;
   }

   public double drawPointToUnit(int x)
   {
      if (gridInches)
      {
         return DRAW_PT_TO_IN * x;
      }
      else
      {
         return DRAW_PT_TO_CM * x;
      }
   }

   CanvasGraphics canvasGraphics;
   DataInputStream din;

   JDRGroup image;
   JDRGroup currentGroup=null;

   StringBuilder stringBuffer;
   byte[] dataBuffer;
   int bytesRead=0;

   int majorVersion;
   int minorVersion;
   String producer;
   int lowBoundingX, lowBoundingY, highBoundingX, highBoundingY;
   boolean showLimits=false, isLandscape=false;
   double gridSpacing;
   int gridDivisions;
   boolean isometricGrid=false;
   boolean autoAdjust=false;
   boolean showGrid=false;
   boolean lockGrid=false;
   boolean gridInches=false; // in or cm
   boolean showTools = true;
   int objectSize;

   AffineTransform affineTransform;

   HashMap<Byte,JDRFont> fontTable;

   static final int OBJECT_FONT_TABLE=0;
   static final int OBJECT_TEXT=1;
   static final int OBJECT_PATH=2;
   static final int OBJECT_SPRITE=5;
   static final int OBJECT_GROUP=6;
   static final int OBJECT_TAGGED=7;
   static final int OBJECT_TEXTAREA=9;
   static final int OBJECT_TEXTCOLUMN=10;
   static final int OBJECT_OPTIONS=11;
   static final int OBJECT_TRANSFORMED_TEXT=12;
   static final int OBJECT_TRANSFORMED_SPRITE=13;
   static final int OBJECT_JPEG=16;
   static final int OBJECT_GRID=17;

   static final int PAPER_A0 = 0x100;
   static final int PAPER_A1 = 0x200;
   static final int PAPER_A2 = 0x300;
   static final int PAPER_A3 = 0x400;
   static final int PAPER_A4 = 0x500;
   static final int PAPER_A5 = 0x600;
   static final int PAPER_A6 = 0x700;
   static final int PAPER_A7 = 0x800;

   static final int PAPER_LIMIT_SHOW = 1;
   static final int PAPER_LIMIT_LANDSCAPE = 1<<4;
   static final int PAPER_LIMIT_PRINTER = 1<<8;

   static final int ACORN_TOOL_LINE=1;
   static final int ACORN_TOOL_CLOSED_LINE=2;
   static final int ACORN_TOOL_CURVE=4;
   static final int ACORN_TOOL_CLOSED_CURVE=8;
   static final int ACORN_TOOL_RECTANGLE=16;
   static final int ACORN_TOOL_ELLIPSE=32;
   static final int ACORN_TOOL_TEXTLINE=64;
   static final int ACORN_TOOL_SELECT=128;

   static final int PATH_STYLE_JOIN=3;
   static final int PATH_STYLE_JOIN_MITRED=0;
   static final int PATH_STYLE_JOIN_ROUND=1;
   static final int PATH_STYLE_JOIN_BEVELLED=2;

   static final int PATH_STYLE_END_CAP=12;
   static final int PATH_STYLE_END_CAP_BUTT=0;
   static final int PATH_STYLE_END_CAP_ROUND=4;
   static final int PATH_STYLE_END_CAP_SQUARE=8;
   static final int PATH_STYLE_END_CAP_TRIANGLE=12;

   static final int PATH_STYLE_START_CAP=48;
   static final int PATH_STYLE_START_CAP_BUTT=0;
   static final int PATH_STYLE_START_CAP_ROUND=16;
   static final int PATH_STYLE_START_CAP_SQUARE=32;
   static final int PATH_STYLE_START_CAP_TRIANGLE=48;

   static final int PATH_STYLE_WINDING_RULE_EVEN_ODD=64;

   static final int PATH_STYLE_PATTERN_DASH=128;

   static final int PATH_STYLE_TRIANGLE_WIDTH=0xFF0000;
   static final int PATH_STYLE_TRIANGLE_HEIGHT=0xFF000000;

   static final int PATH_TAG_END=0;
   static final int PATH_TAG_MOVE=2;
   static final int PATH_TAG_CLOSE=5;
   static final int PATH_TAG_BEZIER=6;
   static final int PATH_TAG_LINE=8;

   static final double DRAW_PT_TO_CM=1.0/18144.0;
   static final double DRAW_PT_TO_IN = DRAW_PT_TO_CM * 0.3937008;
}
