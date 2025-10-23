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
import java.nio.charset.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
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
 * Note that the bytes are in a different order so 
 * DataInputStream.readInt() etc can't be used.
 */

public class AcornDrawFile
{
   private AcornDrawFile(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   public AcornDrawFile(CanvasGraphics cg, DataInputStream din)
   {
      this(cg, din, null);
   }

   public AcornDrawFile(CanvasGraphics cg,
     DataInputStream din, ImportSettings importSettings)
   {
      this(cg);
      image = new JDRGroup(cg);
      currentGroup = image;
      affineTransform = new AffineTransform(
       DRAW_PT_TO_CM, 0, 0, -DRAW_PT_TO_CM, 0, 0);

      this.din = din;
      this.importSettings = importSettings;

      styNames = new Vector<String>();

      if (this.importSettings == null)
      {
         this.importSettings = new ImportSettings(cg.getMessageDictionary());
      }
   }

   public void setTextModeMappings(TeXMappings texMappings)
   {
      this.textModeMappings = texMappings;

      importSettings.useMappings = (texMappings != null);
   }

   public void setMathModeMappings(TeXMappings texMappings)
   {
      this.mathModeMappings = texMappings;
   }

   public JDRGroup readData() throws IOException,InvalidFormatException
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

      if (!styNames.isEmpty())
      {
         String preamble = getCanvasGraphics().getPreamble();

         resetStringBuffer(preamble.length()+styNames.firstElement().length()+12);

         stringBuffer.append(preamble);

         for (String sty : styNames)
         {
            if (sty.startsWith("["))
            {
               int idx = sty.indexOf("]");

               stringBuffer.append(String.format("\\usepackage%s{%s}%n",
                 sty.substring(0, idx+1), sty.substring(idx+1)));
            }
            else
            {
               stringBuffer.append(String.format("\\usepackage{%s}%n", sty));
            }
         }

         getCanvasGraphics().setPreamble(stringBuffer.toString());
      }

      return image;
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

   public void warning(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createWarning(message));
   }

   public void warning(String message, Throwable e)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createWarning(message));

      if (isDebuggingOn())
      {
         e.printStackTrace();
      }
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


   protected void provideTypeblock()
   {
      FlowFrame flowframe = image.getFlowFrame();

      if (flowframe == null)
      {
         flowframe = new FlowFrame(getCanvasGraphics(), FlowFrame.TYPEBLOCK);
         image.setFlowFrame(flowframe);

         double[] coords = new double[4];
         coords[0] = lowBoundingX;
         coords[1] = lowBoundingY;
         coords[2] = highBoundingX;
         coords[3] = highBoundingY;

         affineTransform.transform(coords, 0, coords, 0, 2);

         CanvasGraphics cg = getCanvasGraphics();

         JDRPaper paper = cg.getPaper();

         // Paper width and height is in PostScript points

         double w = cg.bpToStorage(paper.getWidth());
         double h = cg.bpToStorage(paper.getHeight());
         double right = w - coords[2];
         double bottom = h - coords[3];

         // make sure the margins don't overlap or exceed paper
         // bounds

         if (coords[0] >= 0.0 && right >= 0.0
             && (coords[0] + right < w))
         {
            flowframe.setLeft(coords[0]);
            flowframe.setRight(right);
         }

         if (coords[1] >= 0.0 && bottom >= 0.0
             && (coords[1] + bottom < h))
         {
            flowframe.setTop(coords[1]);
            flowframe.setBottom(bottom);
         }
      }
   }

   protected void readObject(int objectId)
   throws IOException,InvalidFormatException
   {
      objectSize = readInt(); // Word aligned size, including header

      switch (objectId)
      {
         case OBJECT_FONT_TABLE:
           readFontTable();
         break;
         case OBJECT_TEXT:
           readText();
         break;
         case OBJECT_PATH:
           readPath();
         break;
         case OBJECT_SPRITE:
           readSprite();
         break;
         case OBJECT_GROUP:
           readGroup();
         break;
         case OBJECT_TAGGED:
           readTagged();
         break;
         case OBJECT_TEXTAREA:
           readTextArea();
         break;
         case OBJECT_TEXTCOLUMN:
           throw new InvalidFormatException(
            getMessageWithFallback("error.acorn_drawfile.misplaced_text_column",
             "Text column found outside text area"));
         case OBJECT_OPTIONS:
           readOptions();
         break;
         case OBJECT_TRANSFORMED_TEXT:
           readTransformedText();
         break;
         case OBJECT_TRANSFORMED_SPRITE:
           readTransformedSprite();
         break;
         case OBJECT_JPEG:
           readJpg();
         break;
         case OBJECT_GRID:
           readGrid();
         break;
         case 3: // unimplemented
         case 4: // unimplemented
         case 8: // not used
         case 14: // not used
         case 15: // not used
           warning(getMessageWithFallback("error.acorn_drawfile.unsupported_object_id",
           "Unsupported object identifier: {0}", objectId));
         break;
         default:
         throw new UnsupportedFeatureException(
          getMessageWithFallback("error.acorn_drawfile.unknown_object_id",
           "Unknown object identifier: {0}", objectId));
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
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

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
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

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
      warning(getMessageWithFallback("error.acorn_drawfile.not_implemented_object_id",
       "Object identifier {0} not yet implemented", OBJECT_TAGGED));

      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

     // not sure about this

     int id = readInt();

     int objectId = readInt();
     readObject(objectId);

     readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected void readFontTable() throws IOException,InvalidFormatException
   {
      int oldBytesRead = bytesRead;
      byte id = readByte();

      String fontName = readString().trim();

      if (fontTables == null)
      {
         fontTables = new HashMap<Byte,FontTable>();
      }

      fontTables.put(Byte.valueOf(id), new FontTable(this, fontName));

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected FontTable getFontTable(byte b)
   {
      return fontTables == null ? null : fontTables.get(Byte.valueOf(b));
   }

   // The Acorn Draw "text line" is analogous to JDRText
   protected void readText() throws IOException,InvalidFormatException
   {
      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      JDRText jdrText = readTextSpecs();
      currentGroup.add(jdrText);

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected JDRText readTextSpecs() throws IOException,InvalidFormatException
   {
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
      byte fontId = dataBuffer.get(0);

      int xSize = readInt(); // 1/640 of a point
      int ySize = readInt(); // draw units

      FontTable fontTable = getFontTable(fontId);

      JDRLength fontSize = new JDRLength(cg, ((double)ySize)/640.0, JDRUnit.bp);

      if (xSize != ySize)
      {// TODO
      }

      JDRFont jdrFont;

      if (fontTable == null)
      {
         jdrFont = new JDRFont("Monospaced", JDRFont.SERIES_MEDIUM,
            JDRFont.SHAPE_UPRIGHT, fontSize);
      }
      else
      {
         jdrFont = fontTable.getFont(fontSize);
      }

      int x = readInt();
      int y = readInt();

      Point2D.Double p = new Point2D.Double(x, y);
      affineTransform.transform(p, p);

      CharacterMap map = CharacterMap.SYSTEM_FONT;

      if (fontTable != null)
      {
         map = fontTable.getMap();
      }

      String text = readString(map); // zero terminated string padding

      JDRText jdrText = new JDRText(cg, p, jdrFont, text);

      String latexText = null;

      if (importSettings.useMappings)
      {
         if (map == CharacterMap.SIDNEY && mathModeMappings != null)
         {
            latexText = "$" + mathModeMappings.applyMappings(
              text, styNames) + "$";
         }
         else if (mathModeMappings != null
              && text.length() > 1 && text.startsWith("$") && text.endsWith("$"))
         {
            text = text.substring(1, text.length()-1);

            latexText = "$" + mathModeMappings.applyMappings(
              text, styNames) + "$";

            jdrText.setText(text);
         }
         else if (textModeMappings != null)
         {
            latexText = textModeMappings.applyMappings(text, styNames);
         }
         else
         {
            jdrText.escapeTeXChars();
         }
      }

      jdrText.setTextPaint(textPaint);

      if (fontTable != null)
      {
         jdrText.setLaTeXFont(fontTable.getLaTeXFont(fontSize));
      }

      if (latexText != null && !latexText.equals(text))
      {
         jdrText.setLaTeXText(latexText);
      }

      return jdrText;
   }

   protected void readJpg() throws IOException,InvalidFormatException
   {
      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      int width = readInt();
      int height = readInt();

      int xdpi = readInt();
      int ydpi = readInt();

      // transformation matrix
      double[] matrix = new double[6];
      matrix[0] = readInt()/65536.0;
      matrix[1] = readInt()/65536.0;
      matrix[2] = readInt()/65536.0;
      matrix[3] = readInt()/65536.0;
      matrix[4] = readInt();
      matrix[5] = readInt();

      affineTransform.transform(matrix, 4, matrix, 4, 1);

      CanvasGraphics cg = image.getCanvasGraphics();

      double yShift = cg.getStorageUnit().fromBp(height / 480.0);

      matrix[5] -= yShift;

      int dataLength = readInt();

      byte[] data = readBytes(dataLength);

      if (!importSettings.extractBitmaps)
      {
         // warn and discard

         warning(getMessageWithFallback("message.acorn_drawfile.ignoring_jpeg",
           "Ignoring embedded JPEG"));
      }
      else
      {
         bitmapCount++;
         File file = new File(importSettings.bitmapDir, 
           String.format((Locale)null, "%s%06d.jpg",
            importSettings.bitmapNamePrefix, bitmapCount));

         DataOutputStream dout = null;

         try
         {
            dout = new DataOutputStream(new FileOutputStream(file));
            dout.write(data, 0, dataLength);
         }
         finally
         {
            if (dout != null)
            {
               dout.close();
            }
         }

         JDRBitmap bitmap = new JDRBitmap(cg, file);
         bitmap.setTransformation(matrix);
         currentGroup.add(bitmap);
      }

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   // NB the Acorn Draw File "text area" is a multi-line area.
   protected void readTextArea() throws IOException,InvalidFormatException
   {
      int orgSize = objectSize;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      provideTypeblock();

      int objectId;

      Vector<JDRPath> columns = new Vector<JDRPath>();

      while ((objectId = readInt()) != 0)
      {
         if (objectId == OBJECT_TEXTCOLUMN)
         {
            JDRPath path = readTextColumn();
            columns.add(path);
            currentGroup.add(path);
         }
         else if (objectId != 0)
         {
            throw new InvalidFormatException(
               getMessageWithFallback("error.acorn_drawfile.misplaced_object_in_textarea",
                "Misplaced object identifier {0} found in text area ({1} or 0 expected)",
                 objectId,
                OBJECT_TEXTCOLUMN));
         }
      }

      readInt();// reserved
      readInt();// reserved

      int fgCol = readInt();
      int bgCol = readInt();

      int offset = 0;

      CharacterMap map = CharacterMap.SYSTEM_FONT;
      byte fontId;
      String fontName, str;
      int fontSize;
      DataBuffer db = new DataBuffer(this, 32);
      FontTable ft;

      HashMap<Byte,FontTable> localFontTable = new HashMap<Byte,FontTable>();

      int textAreaSize = readBytes();
      resetStringBuffer(textAreaSize);

      for (int i = 0; i < dataBuffer.length(); i++)
      {
         byte b = dataBuffer.get(i);

         if (b == '\\')
         {
            if (i > offset)
            {
               String text = dataBuffer.toString(offset, i-offset, map);

               if (!(stringBuffer.length() == 0 && text.trim().isEmpty()))
               {
                  if (importSettings.useMappings && textModeMappings != null)
                  {
                     text = textModeMappings.applyMappings(text, styNames);
                  }

                  stringBuffer.append(text);
               }
            }

            i++;
            b = dataBuffer.get(i);

            switch (b)
            {
               case '!':
               // version
                 do
                 {
                    i++;
                    b = dataBuffer.get(i);
                 }
                 while (Character.isWhitespace((char)b));
                 // b should be 1
               break;
               case 'A':

                 // alignment
                 i++;
                 b = dataBuffer.get(i);

                 if (b == 'L')
                 {
                    stringBuffer.append("\\flushleft ");
                 }
                 else if (b == 'R')
                 {
                    stringBuffer.append("\\flushright ");
                 }
                 else if (b == 'C')
                 {
                    stringBuffer.append("\\centering ");
                 }

                 if (dataBuffer.get(i+1) == '/')
                 {
                    i++;
                 }
               break;
               case 'B':
                 // background hint
               case 'C':
                 // foreground colour
               case 'D':
                 // number of columns
               case 'P':
                 // Paragraph leading
               case 'L':
                 // Leading
                 do
                 {
                    //skip space
                    i++;
                    b = dataBuffer.get(i);
                 }
                 while (Character.isWhitespace((char)b));
                 // read value (currently ignored)
                 db.setLength(0);
                 while (!Character.isWhitespace((char)b))
                 {
                    db.append(b);
                    i++;
                    b = dataBuffer.get(i);
                 }
               break;

               case 'F':
                 // Font table
                 do
                 {
                    //skip space
                    i++;
                    b = dataBuffer.get(i);
                 }
                 while (Character.isWhitespace((char)b));
                 // read font id
                 db.setLength(0);
                 do
                 {
                    db.append(b);
                    i++;
                    b = dataBuffer.get(i);
                 }
                 while (!Character.isWhitespace((char)b));
                 fontId = 0;
                 str = db.toString(CharacterMap.SYSTEM_FONT);
                 try
                 {
                    fontId = Byte.parseByte(str);
                    do
                    {
                       //skip space
                       i++;
                       b = dataBuffer.get(i);
                    }
                    while (Character.isWhitespace((char)b));
                    // read font name
                    db.setLength(0);
                    do
                    {
                       db.append(b);
                       i++;
                       b = dataBuffer.get(i);
                    }
                    while (!Character.isWhitespace((char)b));
                    fontName = db.toString(CharacterMap.SYSTEM_FONT);
                    do
                    {
                       //skip space
                       i++;
                       b = dataBuffer.get(i);
                    }
                    while (Character.isWhitespace((char)b));
                    db.setLength(0);
                    // read font size
                    do
                    {
                       db.append(b);
                       i++;
                       b = dataBuffer.get(i);
                    }
                    while (Character.isDigit((char)b));
                    i--;
                    str = db.toString(CharacterMap.SYSTEM_FONT);
                    fontSize = 0;

                    fontSize = Integer.parseInt(str);

                    localFontTable.put(Byte.valueOf(fontId),
                       new FontTable(this, fontName, fontSize));
                 }
                 catch (NumberFormatException e)
                 {
                    warning(getMessageWithFallback(
                      "error.acorn_drawfile.invalid_font_format",
                      "Invalid font format"), e);
                 }
               break;
               case '-':
                  stringBuffer.append("\\-");
               break;
               default:
                 if (Character.isDigit((char)b))
                 {
                    db.setLength(0);
                    // select font
                    while (Character.isDigit((char)b))
                    {
                       db.append(b);
                       i++;
                       b = dataBuffer.get(i);
                    }

                    i--;
                    str = db.toString(CharacterMap.SYSTEM_FONT);
                    try
                    {
                       fontId = Byte.parseByte(str);

                       ft = localFontTable.get(Byte.valueOf(fontId));

                       if (ft == null)
                       {
                          ft = fontTables.get(Byte.valueOf(fontId));
                       }

                       if (ft != null)
                       {
                          map = ft.getMap();
                          stringBuffer.append(ft.getDeclarations());
                       }
                    }
                    catch (NumberFormatException e)
                    {
                       warning(getMessageWithFallback(
                         "error.acorn_drawfile.invalid_font_id",
                         "Invalid font id {0}", str), e);
                    }
                 }
                 else
                 {
                    stringBuffer.append((char)b);
                 }
            }

            offset = i+1;
         }
      }

      if (dataBuffer.length() > offset)
      {
         String text = dataBuffer.toString(offset, dataBuffer.length()-offset, map);

         if (importSettings.useMappings && textModeMappings != null)
         {
            text = textModeMappings.applyMappings(text, styNames);
         }

         stringBuffer.append(text);
      }

      String content = stringBuffer.toString();

      if (columns.size() == 1)
      {
         JDRPath path = columns.firstElement();

         String idl = "textareacolumn";

         if (totalColumnCount > 0)
         {
            idl = idl + totalColumnCount;
         }

         FlowFrame flowFrame = new FlowFrame(getCanvasGraphics(),
          FlowFrame.DYNAMIC, false, idl, "all");

         flowFrame.setContents(content);

         path.setFlowFrame(flowFrame);

         totalColumnCount++;
      }
      else
      {
         String body = getCanvasGraphics().getDocBody();

         if (body == null || body.isEmpty())
         {
            getCanvasGraphics().setDocBody(content);
         }
         else
         {
            getCanvasGraphics().setDocBody(
             String.format("%s%n\\newpage%n%s", body, content));
         }

         for (JDRPath path : columns)
         {
            String idl = "textareacolumn";

            if (totalColumnCount > 0)
            {
               idl = idl + totalColumnCount;
            }

            FlowFrame flowFrame = new FlowFrame(getCanvasGraphics(),
             FlowFrame.FLOW, false, idl, "all");

            path.setFlowFrame(flowFrame);

            totalColumnCount++;
         }
      }

      int padding = (orgSize-(32+28*columns.size()+textAreaSize))/4;

      if (padding > 0)
      {
         readBytes(padding);
      }
   }

   protected JDRPath readTextColumn() throws IOException,InvalidFormatException
   {
      readInt();// size

      // bounding box

      double[] coords = new double[4];

      coords[0] = readInt();
      coords[1] = readInt();
      coords[2] = readInt();
      coords[3] = readInt();

      affineTransform.transform(coords, 0, coords, 0, 2);

      JDRPath path = JDRPath.constructRectangle(getCanvasGraphics(),
       coords[0], coords[1], coords[2], coords[3]);

      path.setLinePaint(new JDRColor(getCanvasGraphics(), Color.BLACK));

      path.setStroke(new JDRBasicStroke(getCanvasGraphics(), 1.0,
       BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

      return path;
   }

   protected void readSprite() throws IOException,InvalidFormatException
   {
      warning(getMessageWithFallback("error.acorn_drawfile.not_implemented_object_id",
       "Object identifier {0} not yet implemented", OBJECT_SPRITE));

      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));

      // dataBuffer should now hold the sprite content which should
      // be scaled to the bounding box.
      // TODO convert to PNG or JPG.
   }

   protected void readTransformedSprite() throws IOException,InvalidFormatException
   {
      warning(getMessageWithFallback("error.acorn_drawfile.not_implemented_object_id",
       "Object identifier {0} not yet implemented", OBJECT_TRANSFORMED_SPRITE));

// TODO
      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      // transformation matrix
      double[] matrix = new double[6];
      matrix[0] = readInt()/65536.0;
      matrix[1] = readInt()/65536.0;
      matrix[2] = readInt()/65536.0;
      matrix[3] = readInt()/65536.0;
      matrix[4] = readInt();
      matrix[5] = readInt();

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected void readTransformedText() throws IOException,InvalidFormatException
   {
      warning(getMessageWithFallback("error.acorn_drawfile.not_fully_implemented_object_id",
       "Object identifier {0} not fully implemented", OBJECT_TRANSFORMED_TEXT));

      int oldBytesRead = bytesRead;

      // bounding box
      int lowX = readInt();
      int lowY = readInt();
      int highX = readInt();
      int highY = readInt();

      // transformation matrix
      double[] matrix = new double[6];
      matrix[0] = readInt()/65536.0;
      matrix[1] = readInt()/65536.0;
      matrix[2] = readInt()/65536.0;
      matrix[3] = readInt()/65536.0;
      matrix[4] = readInt();
      matrix[5] = readInt();

      affineTransform.transform(matrix, 4, matrix, 4, 1);

      int fontFlags = readInt(); // bit 0 -> kern, bit 1 -> right to left

      JDRText jdrText = readTextSpecs();

// TODO apply transform

      currentGroup.add(jdrText);

      readBytes((objectSize-8)-(bytesRead-oldBytesRead));
   }

   protected DataBuffer resetDataBuffer()
   {
      return resetDataBuffer(16);
   }

   protected DataBuffer resetDataBuffer(int minCapacity)
   {
      if (dataBuffer == null)
      {
         dataBuffer = new DataBuffer(this, minCapacity);
      }
      else
      {
         dataBuffer.setLength(0);

         if (dataBuffer.getCapacity() < minCapacity)
         {
            dataBuffer.setMinimumCapacity(minCapacity);
         }
      }

      return dataBuffer;
   }

   protected StringBuilder resetStringBuffer()
   {
      return resetStringBuffer(16);
   }

   protected StringBuilder resetStringBuffer(int minCapacity)
   {
      if (stringBuffer == null)
      {
         stringBuffer = new StringBuilder(minCapacity);
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

   public byte[] readBytes(int length) throws IOException
   {
      if (length <= 0) return null;

      byte[] array = new byte[length];

      int r = din.read(array, 0, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readBytes: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(octet(array[i]));
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

      return array;
   }

   // read up to \0
   public int readBytes() throws IOException
   {
      resetDataBuffer();

      byte b;

      while ((b = readByte()) != 0)
      {
         if (b == -1) return -1;

         dataBuffer.append(b);
         bytesRead++;
      }

      return dataBuffer.length();
   }

   public String readString(int length) throws IOException
   {
      return readString(length, CharacterMap.SYSTEM_FONT);
   }

   public String readString(int length, CharacterMap map) throws IOException
   {
      if (length <= 0) return "";

      resetDataBuffer(length);

      int r = dataBuffer.read(din, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readString: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(octet(dataBuffer.get(i)));
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

      String text = dataBuffer.toString(map);

      if (isDebuggingOn())
      {
         printlnDebug("\t"+text);
      }

      return text;
   }

   public String readString() throws IOException
   {
      return readString(CharacterMap.SYSTEM_FONT);
   }

   public String readString(CharacterMap map) throws IOException
   {
      // read to null

      byte b;

      resetDataBuffer();

      while ((b = readByte()) != 0)
      {
         dataBuffer.append(b);
      }

      return dataBuffer.toString(map);
   }

   public int readInt() throws IOException
   {
      int length = 4;

      resetDataBuffer(length);

      int r = dataBuffer.read(din, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readInt: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(octet(dataBuffer.get(i)));
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

      resetStringBuffer(length);

      stringBuffer.append(String.format("%s%s%s%s",
        octet(dataBuffer.get(3)), 
        octet(dataBuffer.get(2)),
        octet(dataBuffer.get(1)), 
        octet(dataBuffer.get(0))));

      int value = (int)Long.parseLong(stringBuffer.toString(), 16);

      if (isDebuggingOn())
      {
         printlnDebug("\t"+value);
      }

      return value;
   }

   protected String octet(byte b)
   {
      return String.format("%02X", b);
   }

   public double readDouble() throws IOException
   {
      int length = 8;

      resetDataBuffer(length);
      int r = dataBuffer.read(din, length);

      if (isDebuggingOn())
      {
         printlnDebug(String.format("readDouble: %d byte(s) read", r));

         for (int i = 0; i < r; i++)
         {
            if (i > 0)
            {
               printDebug(" ");
            }

            printDebug(octet(dataBuffer.get(i)));
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

      resetStringBuffer(length);

      stringBuffer.append(String.format("%s%s%s%s%s%s%s%s",
        octet(dataBuffer.get(3)), 
        octet(dataBuffer.get(2)),
        octet(dataBuffer.get(1)), 
        octet(dataBuffer.get(0)),
        octet(dataBuffer.get(7)), 
        octet(dataBuffer.get(6)),
        octet(dataBuffer.get(5)), 
        octet(dataBuffer.get(4))
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

   public static enum CharacterMap
   {
      SYSTEM_FONT,
      UTF8,
      CYRILLIC,
      GREEK,
      HEBREW,
      LATIN1,
      LATIN2,
      LATIN3,
      LATIN4,
      LATIN5,
      LATIN6,
      LATIN7,
      LATIN8,
      LATIN9,
      LATIN10,
      WELSH,
      SELWYN,
      SIDNEY;
   }

   CanvasGraphics canvasGraphics;
   DataInputStream din;

   ImportSettings importSettings;
   int bitmapCount=0;
   TeXMappings textModeMappings=null;
   TeXMappings mathModeMappings=null;

   JDRGroup image;
   JDRGroup currentGroup=null;

   StringBuilder stringBuffer;
   DataBuffer dataBuffer;
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
   int totalColumnCount = 0;

   AffineTransform affineTransform;

   HashMap<Byte,FontTable> fontTables;
   Vector<String> styNames;

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

class DataBuffer
{
   public DataBuffer(AcornDrawFile adf)
   {
      this(adf, 64);
   }

   public DataBuffer(AcornDrawFile adf, int capacity)
   {
      if (capacity <= 1)
      {
         throw new IllegalArgumentException("Invalid capacity "+capacity);
      }

      this.adf = adf;

      data = new byte[capacity];
      builder = new StringBuilder(capacity);

      try
      {
         latin1 = Charset.forName("ISO-8859-1");
      }
      catch (Exception e)
      {
         if (adf.isDebuggingOn())
         {
            e.printStackTrace();
         }
      }

      try
      {
         latin2 = Charset.forName("ISO-8859-2");
      }
      catch (Exception e)
      {
         if (adf.isDebuggingOn())
         {
            e.printStackTrace();
         }
      }
   }

   public int getCapacity()
   {
      return data.length;
   }

   public int length()
   {
      return length;
   }

   public void setLength(int n)
   {
      length = n;
   }

   public byte get(int i)
   throws ArrayIndexOutOfBoundsException
   {
      if (i < 0 || i > length)
      {
         throw new ArrayIndexOutOfBoundsException("Invalid array index "+i);
      }

      return data[i];
   }

   public void append(byte b)
   {
      if (length < data.length)
      {
         data[length] = b;
         length++;
      }
      else
      {
         enlarge();
         data[length] = b;
         length++;
      }
   }

   protected void enlarge()
   {
      enlarge((int) Math.ceil(1.5 * data.length));
   }

   protected void enlarge(int newCapacity)
   {
      byte[] orgData = data;
      data = new byte[newCapacity];

      for (int i = 0; i < orgData.length; i++)
      {
         data[i] = orgData[i];
      }

      length = orgData.length;
   }

   public void setMinimumCapacity(int newCapacity)
   {
      if (newCapacity > data.length)
      {
         enlarge(newCapacity);
      }
   }

   public int read(DataInputStream din, int size)
   throws IOException
   {
      length = 0;

      if (size > getCapacity())
      {
         enlarge(size);
      }

      int result = din.read(data, 0, size);

      if (result >= 0)
      {
         length = size;
      }

      return result;
   }

   public int append(DataInputStream din, int size)
   throws IOException
   {
      if (size > data.length + size)
      {
         enlarge(data.length + size);
         data = new byte[data.length + size];
      }

      int result = din.read(data, length, size);

      if (result >= 0)
      {
         length += size;
      }

      return result;
   }

   public String toString(AcornDrawFile.CharacterMap map)
   {
      return toString(0, length, map);
   }

   public String toString(int offset, int n, AcornDrawFile.CharacterMap map)
   {
      Charset charset = latin1;
      builder.setLength(0);

      switch (map)
      {
         case UTF8 :
            return new String(data, offset, n);
         case SYSTEM_FONT :
         case LATIN1 :
            appendLatinFontChar(offset, n, latin1);
         break;
         case LATIN2 :
            appendLatinFontChar(offset, n, latin2);
         break;
         case SELWYN :
            appendSelwynFontChar(offset, n);
         break;
// TODO
         default:
            appendLatinFontChar(offset, n, latin1);
      }

      return builder.toString();
   }

   private void appendSidneyFontChar(int offset, int n)
   {
      for (int i = 0; i < n; i++)
      {
         appendSidneyFontChar(data[i+offset]);
      }
   }

   private void appendSidneyFontChar(byte c)
   {
      switch ((char)c)
      {
         case 0x22: builder.appendCodePoint(0x2200); break;
         case 0x24: builder.appendCodePoint(0x2203); break;
         case 0x27: builder.appendCodePoint(0x220B); break;
         case 0x40: builder.appendCodePoint(0x2245); break;
         case 0x41: builder.appendCodePoint(0x1D6E2); break;
         case 0x42: builder.appendCodePoint(0x1D6E3); break;
         case 0x43: builder.appendCodePoint(0x1D6F8); break;
         case 0x44: builder.appendCodePoint(0x1D6E5); break;
         case 0x45: builder.appendCodePoint(0x1D6E6); break;
         case 0x46: builder.appendCodePoint(0x1D6F7); break;
         case 0x47: builder.appendCodePoint(0x1D6E4); break;
         case 0x48: builder.appendCodePoint(0x1D6E8); break;
         case 0x49: builder.appendCodePoint(0x1D6EA); break;
         case 0x4A: builder.appendCodePoint(0x1D717); break;
         case 0x4B: builder.appendCodePoint(0x1D6EB); break;
         case 0x4C: builder.appendCodePoint(0x1D6EC); break;
         case 0x4D: builder.appendCodePoint(0x1D6ED); break;
         case 0x4E: builder.appendCodePoint(0x1D6EE); break;
         case 0x4F: builder.appendCodePoint(0x1D6F0); break;
         case 0x50: builder.appendCodePoint(0x1D6F1); break;
         case 0x51: builder.appendCodePoint(0x1D6E9); break;
         case 0x52: builder.appendCodePoint(0x1D6E2); break;
         case 0x53: builder.appendCodePoint(0x1D6F4); break;
         case 0x54: builder.appendCodePoint(0x1D6F5); break;
         case 0x55: builder.append('Y'); break;
         case 0x56: builder.appendCodePoint(0x1D70D); break;
         case 0x57: builder.appendCodePoint(0x1D6FA); break;
         case 0x58: builder.appendCodePoint(0x1D6EF); break;
         case 0x59: builder.appendCodePoint(0x1D6F9); break;
         case 0x5A: builder.appendCodePoint(0x1D6E7); break;
         case 0x5C: builder.appendCodePoint(0x2234); break;
         case 0x5E: builder.appendCodePoint(0x22A5); break;
         case 0x61: builder.appendCodePoint(0x1D6FC); break;
         case 0x62: builder.appendCodePoint(0x1D6FD); break;
         case 0x63: builder.appendCodePoint(0x1D712); break;
         case 0x64: builder.appendCodePoint(0x1D6FF); break;
         case 0x65: builder.appendCodePoint(0x1D700); break;
         case 0x66: builder.appendCodePoint(0x1D719); break;
         case 0x67: builder.appendCodePoint(0x1D6FE); break;
         case 0x68: builder.appendCodePoint(0x1D702); break;
         case 0x69: builder.appendCodePoint(0x1D704); break;
         case 0x6A: builder.appendCodePoint(0x1D711); break;
         case 0x6B: builder.appendCodePoint(0x1D705); break;
         case 0x6C: builder.appendCodePoint(0x1D706); break;
         case 0x6D: builder.appendCodePoint(0x1D707); break;
         case 0x6E: builder.appendCodePoint(0x1D708); break;
         case 0x6F: builder.appendCodePoint(0x1D70A); break;
         case 0x70: builder.appendCodePoint(0x1D70B); break;
         case 0x71: builder.appendCodePoint(0x1D703); break;
         case 0x72: builder.appendCodePoint(0x1D70C); break;
         case 0x73: builder.appendCodePoint(0x1D70E); break;
         case 0x74: builder.appendCodePoint(0x1D70F); break;
         case 0x75: builder.appendCodePoint(0x1D710); break;
         case 0x76: builder.appendCodePoint(0x1D71B); break;
         case 0x77: builder.appendCodePoint(0x1D714); break;
         case 0x78: builder.appendCodePoint(0x1D709); break;
         case 0x79: builder.appendCodePoint(0x1D713); break;
         case 0x7A: builder.appendCodePoint(0x1D701); break;

         case 0xA1: builder.appendCodePoint(0x1D6F6); break;
         case 0xA2: builder.appendCodePoint(0x2032); break;
         case 0xA3: builder.appendCodePoint(0x2264); break;
         case 0xA4: builder.appendCodePoint(0x2215); break;
         case 0xA5: builder.appendCodePoint(0x221E); break;
         case 0xA6: builder.append('f'); break;
         case 0xA7: builder.appendCodePoint(0x2663); break;
         case 0xA8: builder.appendCodePoint(0x2666); break;
         case 0xA9: builder.appendCodePoint(0x2665); break;
         case 0xAA: builder.appendCodePoint(0x2660); break;

         case 0xAB: builder.appendCodePoint(0x2194); break;
         case 0xAC: builder.appendCodePoint(0x2190); break;
         case 0xAD: builder.appendCodePoint(0x2191); break;
         case 0xAE: builder.appendCodePoint(0x2192); break;
         case 0xAF: builder.appendCodePoint(0x2193); break;

         case 0xB2: builder.appendCodePoint(0x2033); break;
         case 0xB3: builder.appendCodePoint(0x2265); break;
         case 0xB4: builder.appendCodePoint(0x00D7); break;
         case 0xB5: builder.appendCodePoint(0x221D); break;
         case 0xB6: builder.appendCodePoint(0x2202); break;
         case 0xB7: builder.appendCodePoint(0x2022); break;
         case 0xB8: builder.appendCodePoint(0x00F7); break;
         case 0xB9: builder.appendCodePoint(0x2260); break;
         case 0xBA: builder.appendCodePoint(0x2261); break;
         case 0xBB: builder.appendCodePoint(0x2248); break;

         case 0xBC: builder.appendCodePoint(0x2026); break;
         case 0xBD: builder.appendCodePoint(0x2223); break;
         case 0xBE: builder.appendCodePoint(0x2015); break;
         case 0xBF: builder.appendCodePoint(0x2BA0); break;

         case 0xC0: builder.appendCodePoint(0x2135); break;
         case 0xC1: builder.appendCodePoint(0x2111); break;
         case 0xC2: builder.appendCodePoint(0x211C); break;
         case 0xC3: builder.appendCodePoint(0x2118); break;
         case 0xC4: builder.appendCodePoint(0x2297); break;
         case 0xC5: builder.appendCodePoint(0x2295); break;
         case 0xC6: builder.appendCodePoint(0x2205); break;
         case 0xC7: builder.appendCodePoint(0x2229); break;
         case 0xC8: builder.appendCodePoint(0x222A); break;
         case 0xC9: builder.appendCodePoint(0x2283); break;
         case 0xCA: builder.appendCodePoint(0x2287); break;
         case 0xCB: builder.appendCodePoint(0x2284); break;
         case 0xCC: builder.appendCodePoint(0x2282); break;
         case 0xCD: builder.appendCodePoint(0x2286); break;
         case 0xCE: builder.appendCodePoint(0x2208); break;
         case 0xCF: builder.appendCodePoint(0x2209); break;
         case 0xD0: builder.appendCodePoint(0x2220); break;
         case 0xD1: builder.appendCodePoint(0x2207); break;

         case 0xD2: builder.appendCodePoint(0x00AE); break;
         case 0xD3: builder.appendCodePoint(0x00A9); break;
         case 0xD4: builder.appendCodePoint(0x2122); break;

         case 0xD5: builder.appendCodePoint(0x220F); break;
         case 0xD6: builder.appendCodePoint(0x221A); break;
         case 0xD7: builder.appendCodePoint(0x2219); break;
         case 0xD8: builder.appendCodePoint(0x00AC); break;
         case 0xD9: builder.appendCodePoint(0x2227); break;
         case 0xDA: builder.appendCodePoint(0x2228); break;

         case 0xDB: builder.appendCodePoint(0x21D4); break;
         case 0xDC: builder.appendCodePoint(0x21D0); break;
         case 0xDD: builder.appendCodePoint(0x21D1); break;
         case 0xDE: builder.appendCodePoint(0x21D2); break;
         case 0xDF: builder.appendCodePoint(0x21D3); break;

         case 0xE0: builder.appendCodePoint(0x22C4); break;
         case 0xE1: builder.appendCodePoint(0x27E8); break;
         case 0xE2: builder.appendCodePoint(0x00AE); break;
         case 0xE3: builder.appendCodePoint(0x00A9); break;
         case 0xE4: builder.appendCodePoint(0x2122); break;

         case 0xE5: builder.appendCodePoint(0x2211); break;

         case 0xE6: builder.appendCodePoint(0x239B); break;
         case 0xE7: builder.appendCodePoint(0x239C); break;
         case 0xE8: builder.appendCodePoint(0x239D); break;
         case 0xE9: builder.appendCodePoint(0x23A1); break;
         case 0xEA: builder.appendCodePoint(0x23A2); break;
         case 0xEB: builder.appendCodePoint(0x23A3); break;
         case 0xEC: builder.appendCodePoint(0x23A7); break;
         case 0xED: builder.appendCodePoint(0x23A8); break;
         case 0xEE: builder.appendCodePoint(0x23A9); break;
         case 0xEF: builder.appendCodePoint(0x23AA); break;

         case 0xF0: builder.appendCodePoint(0x20AC); break;
         case 0xF1: builder.appendCodePoint(0x27E9); break;
         case 0xF2: builder.appendCodePoint(0x222B); break;

         case 0xF3: builder.appendCodePoint(0x2320); break;
         case 0xF4: builder.appendCodePoint(0x23AE); break;
         case 0xF5: builder.appendCodePoint(0x2321); break;

         case 0xF6: builder.appendCodePoint(0x239E); break;
         case 0xF7: builder.appendCodePoint(0x239F); break;
         case 0xF8: builder.appendCodePoint(0x23A0); break;

         case 0xF9: builder.appendCodePoint(0x23A4); break;
         case 0xFA: builder.appendCodePoint(0x23A5); break;
         case 0xFB: builder.appendCodePoint(0x23A6); break;

         case 0xFC: builder.appendCodePoint(0x23AB); break;
         case 0xFD: builder.appendCodePoint(0x23AC); break;
         case 0xFE: builder.appendCodePoint(0x23AD); break;
         default: 

          if (
                (0x20 <= c && c <= 0x3F)
             || c == '[' || c == ']' || c == '_'
             || (0x7B <= c && c <= 0x7F)
             || c == 0xB1 || c == 0xB2
             )
          {
             builder.appendCodePoint(c);
          }
          else if (((char)c) > 0x7F)
          {
             if (latin1 != null)
             {
                builder.append(new String(new byte[] { c }, latin1));
             }
             else
             {
                adf.warning(adf.getMessageWithFallback(
                 "error.io.unsupported_char", 
                 "Unsupported character 0x{0}", String.format("%02X", c)));

                builder.appendCodePoint(0xFFFD);
             }
          }
          else
          {
             builder.append((char)c);
          }
      }
   }

   private void appendSelwynFontChar(int offset, int n)
   {
      for (int i = 0; i < n; i++)
      {
         appendSelwynFontChar(data[i+offset]);
      }
   }

   private void appendSelwynFontChar(byte c)
   {
      switch ((char)c)
      {
         case 0x2A: builder.appendCodePoint(0x1F59B); break;
         case 0x2B: builder.appendCodePoint(0x1F599); break;
         case 0x34: builder.append('\u2742'); break;
         case 0x38: builder.append('\u2743'); break;
         case 0x48: builder.append('\u2745'); break;
         case 0x5D: builder.append('\u2746'); break;
         case 0x61: builder.append('\u260E'); break;
         case 0x62: builder.append('\u2714'); break;
         case 0x63: builder.append('\u2718'); break;
         case 0x64: builder.append('\u2744'); break;
         case 0x65: builder.append('\u2605'); break;
         case 0x66: builder.append('\u273B'); break;
         case 0x67: builder.append('\u2750'); break;
         case 0x68: builder.append('\u2751'); break;
         case 0x69: builder.append('\u2752'); break;
         case 0x6A: builder.append('\u2B25'); break;
         case 0x6B: builder.append('\u27A7'); break;
         case 0x6C: builder.append('\u25CF'); break;
         case 0x6D: builder.append('\u274D'); break;
         case 0x6E: builder.append('\u25A0'); break;
         case 0x6F: builder.append('\u274F'); break;
         case 0x70: builder.append('\u2748'); break;
         case 0x71: builder.append('\u2749'); break;
         case 0x72: builder.append('\u274B'); break;//??
         case 0x73: builder.append('\u25B2'); break;
         case 0x74: builder.append('\u25BC'); break;
         case 0x75: builder.append('\u274A'); break;
         case 0x76: builder.append('\u2756'); break;
// 0x77 ??
         case 0x78: builder.append('\u2758'); break;
         case 0x79: builder.append('\u2759'); break;
         case 0x7A: builder.append('\u275A'); break;
         case 0x7B: builder.append('\u275B'); break;
         case 0x7C: builder.append('\u275C'); break;
         case 0x7D: builder.append('\u275D'); break;
         case 0x7E: builder.append('\u275E'); break;

         case 0x92: builder.append('\u276C'); break;
         case 0x93: builder.append('\u2771'); break;
         case 0x94: builder.append('\u2770'); break;
         case 0x95: builder.append('\u276A'); break;
         case 0x96: builder.append('\u2768'); break;
         case 0x97: builder.append('\u2773'); break;
         case 0x98: builder.append('\u276E'); break;
         case 0x99: builder.append('\u276F'); break;
         case 0x9A: builder.append('\u2772'); break;
         case 0x9B: builder.append('\u276D'); break;
         case 0x9C: builder.append('\u2769'); break;
         case 0x9D: builder.append('\u276B'); break;
         case 0x9E: builder.append('\u2774'); break;
         case 0x9F: builder.append('\u2775'); break;

         case 0xA1: builder.append('\u2761'); break;
         case 0xA2: builder.append('\u2762'); break;
         case 0xA3: builder.append('\u2763'); break;
         case 0xA4: builder.append('\u2764'); break;
         case 0xA5: builder.append('\u2765'); break;
         case 0xA6: builder.append('\u2766'); break;
         case 0xA7: builder.append('\u2767'); break;
         case 0xA8: builder.append('\u2663'); break;
         case 0xA9: builder.append('\u2666'); break;
         case 0xAA: builder.append('\u2665'); break;
         case 0xAB: builder.append('\u2660'); break;

         case 0xD5: builder.append('\u2192'); break;
         case 0xD6: builder.append('\u2194'); break;
         case 0xD7: builder.append('\u2195'); break;

         case 0xE7: builder.append('\u2733'); break;

         default: 

          if (
                  (0x21 <= c && c <= 0x29)
               || (0x2C <= c && c <= 0x33)
               || (0x2C <= c && c <= 0x33)
               || (0x35 <= c && c <= 0x37)
               || (0x39 <= c && c <= 0x47)
               || (0x49 <= c && c <= 0x5C)
               || (0x5E <= c && c <= 0x60)
             )
          {
             builder.appendCodePoint(0x26E0 + c);
          }
          else if (0xAC <= c && c <= 0xB5)
          {
             // circled numbers 1-10
             builder.append(0x23B4 + c);
          }
          else if (
                    (0xB6 <= c && c <= 0xD4)
                 || (0xB8 <= c && c <= 0xE6)
                 || (0xE8 <= c && c <= 0xFE)
                  )
          {
             // negative circled numbers 1-10
             // sans-serif circled numbers 1-10
             // negative sans-serif circled numbers 1-10
             // arrows
             builder.append(0x26C0 + c);
          }
          else if (((char)c) > 0x7F)
          {
             if (latin1 != null)
             {
                builder.append(new String(new byte[] { c }, latin1));
             }
             else
             {
                adf.warning(adf.getMessageWithFallback(
                 "error.io.unsupported_char", 
                 "Unsupported character 0x{0}", String.format("%02X", c)));

                builder.appendCodePoint(0xFFFD);
             }
          }
          else
          {
             builder.append((char)c);
          }
      }
   }

   private void appendLatinFontChar(int offset, int n, Charset charset)
   {
      for (int i = 0; i < n; i++)
      {
         appendLatinFontChar(data[i+offset], charset);
      }
   }

   private void appendLatinFontChar(byte c, Charset charset)
   {
      switch ((char)c)
      {
         case 0x80: builder.append("\u20AC"); break;
         case 0x81: builder.append("\u0174"); break;
         case 0x82: builder.append("\u0175"); break;
         case 0x83: builder.append("\u25F0"); break;
         case 0x84: builder.append("\uD83D\uDDD9"); break;
         case 0x85: builder.append("\u0176"); break;
         case 0x86: builder.append("\u0177"); break;
// 0x87 8^7 ??
         case 0x88: builder.append("\u21E6"); break;
         case 0x89: builder.append("\u21E8"); break;
         case 0x8A: builder.append("\u21E9"); break;
         case 0x8B: builder.append("\u21E7"); break;
         case 0x8C: builder.append("\u2026"); break;
         case 0x8D: builder.append("\u2122"); break;
         case 0x8E: builder.append("\u2030"); break;
         case 0x8F: builder.append("\u2022"); break;
         case 0x90: builder.append("\u2018"); break;
         case 0x91: builder.append("\u2019"); break;
         case 0x92: builder.append("\u2039"); break;
         case 0x93: builder.append("\u203A"); break;
         case 0x94: builder.append("\u201C"); break;
         case 0x95: builder.append("\u201D"); break;
         case 0x96: builder.append("\u201E"); break;
         case 0x97: builder.append("\u2010"); break;
         case 0x98: builder.append("\u2014"); break;
         case 0x99: builder.append("\u2013"); break;
         case 0x9A: builder.append("\u0152"); break;
         case 0x9B: builder.append("\u0153"); break;
         case 0x9C: builder.append("\u2020"); break;
         case 0x9D: builder.append("\u2021"); break;
         case 0x9E: builder.append("fi"); break;
         case 0x9F: builder.append("fl"); break;
         default: 

          if (((char)c) > 0x7F)
          {
             if (charset != null)
             {
                builder.append(new String(new byte[] { c }, charset));
             }
             else
             {
                adf.warning(adf.getMessageWithFallback(
                 "error.io.unsupported_char", 
                 "Unsupported character 0x{0}", String.format("%02X", c)));

                builder.appendCodePoint(0xFFFD);
             }
          }
          else
          {
             builder.append((char)c);
          }
      }
   }

   byte[] data;
   int length=0;
   StringBuilder builder;
   Charset latin1, latin2;
   AcornDrawFile adf;
}

class FontTable
{
   public FontTable(AcornDrawFile adf, String fontName)
   {
      this(adf, fontName, 0);
   }

   public FontTable(AcornDrawFile adf, String fontName, int defSize)
   {
      this.adf = adf;
      this.name = fontName;
      this.defSize = defSize;

      if (defSize > 0)
      {
         fontHeight = new JDRLength(adf.getCanvasGraphics(), defSize, 
           JDRUnit.bp);
      }

      latexFontBase = adf.getCanvasGraphics().getLaTeXFontBase();

      charMap = AcornDrawFile.CharacterMap.SYSTEM_FONT;

      int idx = fontName.indexOf(".");
      String family = fontName;
      String variant = "";

      if (idx > 0)
      {
         family = fontName.substring(0, idx);
         variant = fontName.substring(idx+1);
      }

      if (family.startsWith("System")
       || family.equals("Portrhouse"))
      {
         family = "Monospaced";
         latexFamily = "\\ttfamily";
      }
      else if (family.equals("Homerton")
            || family.equals("Sassoon")
              )
      {
         family = "SansSerif";
         latexFamily = "\\sffamily";
      }
      else if (family.equals("Sidney"))
      {
         family = "Serif";
         charMap = AcornDrawFile.CharacterMap.SIDNEY;
         latexFamily = "\\rmfamily";
      }
      else if (family.equals("Selwyn"))
      {
         family = "Serif";
         charMap = AcornDrawFile.CharacterMap.SELWYN;
         latexFamily = "\\rmfamily";
      }
      else
      {
         family = "Serif";
         latexFamily = "\\rmfamily";
      }

      int weight = JDRFont.SERIES_MEDIUM;
      int shape = JDRFont.SHAPE_UPRIGHT;

      if (variant.contains("Oblique"))
      {
         shape = JDRFont.SHAPE_SLANTED;
         latexShape = "\\slshape";
      }
      else if (variant.contains("Italic"))
      {
         shape = JDRFont.SHAPE_ITALIC;
         latexShape = "\\itshape";
      }

      if (variant.contains("Bold"))
      {
         weight = JDRFont.SERIES_BOLD;
         latexShape = "\\bfseries";
      }

   }

   public JDRFont getFont(JDRLength size)
   {
      return new JDRFont(family, weight, shape, size);
   }

   public LaTeXFont getLaTeXFont(JDRLength size)
   {
      return new LaTeXFont(latexFamily, latexWeight, latexShape,
       latexFontBase.getLaTeXCmd(size));
   }

   public String getDeclarations()
   {
      if (fontHeight != null)
      {
         return latexFamily + latexWeight + latexShape
          + latexFontBase.getLaTeXCmd(fontHeight)
          + " ";
      }
      else
      {
         return latexFamily + latexWeight + latexShape + " ";
      }
   }

   public AcornDrawFile.CharacterMap getMap()
   {
      return charMap;
   }

   public String toString()
   {
      return name;
   }

   String name;
   AcornDrawFile adf;
   AcornDrawFile.CharacterMap charMap;  
   String family;
   int weight=0, shape=0;
   int defSize;
   JDRLength fontHeight;
   LaTeXFontBase latexFontBase;
   String latexFamily="\\rmfamily", latexWeight="\\mdseries", latexShape="\\upshape";
}
