package com.dickimawbooks.jdr.io.svg;

import java.util.*;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGHandler extends DefaultHandler
{
   public SVGHandler(SVG svg, JDRGroup group)
   {
      super();
      this.group = group;
      this.svg = svg;
      stack = new ArrayDeque<SVGAbstractElement>();

      msgSystem = group.getCanvasGraphics().getMessageSystem();

      msgSystem.getPublisher().publishMessages(
         MessageInfo.createVerbose(1, "SVG handler initialised"));

      GraphicsEnvironment env = 
         GraphicsEnvironment.getLocalGraphicsEnvironment();
      availableFontFamilies = env.getAvailableFontFamilyNames();
   }

   public void startDocument()
   {
      msgSystem.getPublisher().publishMessages(
        MessageInfo.createVerbose(2, "<xml>"));
   }

   public void endDocument()
   {
      if (base != null)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createVerbose(2, "</xml>"),
           MessageInfo.createVerbose(1, "Generating image"));

         try
         {
            base.addToImage(group);
         }
         catch (InvalidFormatException e)
         {
            msgSystem.getPublisher().publishMessages(
              MessageInfo.createError(e));
         }
      }
   }

   public void startElement(String uri, String name, String qName, Attributes attrs)
   {
      msgSystem.getPublisher().publishMessages(
        MessageInfo.createVerbose(2, "<"+qName+">"));

      try
      {
         SVGAbstractElement parent = current;

         current = SVGAbstractElement.getElement(this, parent, name, uri, attrs);

         if (parent != null)
         {
            parent.addChild(current);
         }

         if (base == null && current instanceof SVGElement)
         {
            base = (SVGElement)current;
         }
   
         stack.push(current);
      }
      catch (UnknownSVGElementException e)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createWarning(e.getMessage()));
      }
      catch (InvalidFormatException e)
      {
         msgSystem.getPublisher().publishMessages(MessageInfo.createError(e));
      }
   }

   public void endElement(String uri, String name, String qName)
   {
      if (current != null)
      {
         current.endElement();
      }

      msgSystem.getPublisher().publishMessages(MessageInfo.createVerbose(2, "</"+qName+">"));

      if (!stack.isEmpty())
      {
         stack.pop();
         current = stack.peek();
      }
   }

   public void characters(char[] ch, int start, int length)
   {
      if (current != null)
      {
         current.addToContents(ch, start, length);
      }
   }

   public JDRMessage getMessageSystem()
   {
      return msgSystem;
   }

   public String getMessageWithFallback(String id, String fallbackFormat,
     Object... params)
   {
      return msgSystem.getMessageWithFallback(id, fallbackFormat, params);
   }

   public void debugMessage(String msg)
   {
      if (msgSystem.isDebuggingOn())
      {
         msgSystem.getPublisher().publishMessages(MessageInfo.createMessage(msg));
      }
   }

   public void debugMessage(Throwable cause)
   {
      if (msgSystem.isDebuggingOn())
      {
         error(cause);
      }
   }

   public void verbose(int level, String msg)
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createVerbose(level, msg));
   }

   public void warning(Throwable e)
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createWarning(e));
   }

   public void warning(String msg)
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createWarning(msg));
   }

   public void error(Throwable e)
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createError(e));
   }

   public void fatalError(Throwable e)
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createFatalError(e));
   }

   public boolean isFontFamilyAvailable(String family)
   {
      if (family.equals("Serif") || family.equals("SansSerif")
          || family.equals("Monospaced"))
      {
         return true;
      }

      for (String f : availableFontFamilies)
      {
         if (f.equals(family)) return true;
      }

      return false;
   }

   public JDRBasicStroke createDefaultStroke()
   {
      return new JDRBasicStroke(getCanvasGraphics());
   }

   public JDRFont createDefaultFont()
   {
      return new JDRFont(msgSystem);
   }

   public JDRPaint createDefaultTextPaint()
   {
      return new JDRColor(getCanvasGraphics(), 0, 0, 0);
   }

   public JDRPaint createDefaultLinePaint()
   {
      return new JDRTransparent(getCanvasGraphics());
   }

   public JDRPaint createDefaultFillPaint()
   {
      return new JDRColor(getCanvasGraphics(), 0, 0, 0);
   }

   public SVG getSVG()
   {
      return svg;
   }

   public JDRUnit getDefaultUnit()
   {
      return JDRUnit.bp;
   }

   public JDRUnit getStorageUnit()
   {
      return getCanvasGraphics().getStorageUnit();
   }

   public double toStorageUnit(double defaultUnitValue)
   {
      return getDefaultUnit().toUnit(defaultUnitValue, getStorageUnit());
   }

   public JDRLength toStorageLength(double defaultUnitValue)
   {
      return new JDRLength(getCanvasGraphics(),
        toStorageUnit(defaultUnitValue),
        getStorageUnit());
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return group.getCanvasGraphics();
   }

   public void setLastTextPosition(Point2D p)
   {
      lastTextPosition = p;
   }

   public Point2D getLastTextPosition()
   {
      return lastTextPosition;
   }

   private JDRGroup group;
   private SVG svg;

   private Point2D lastTextPosition;

   private ArrayDeque<SVGAbstractElement> stack;
   private SVGAbstractElement current = null;
   private SVGElement base = null;

   private JDRMessage msgSystem;
   private String[] availableFontFamilies;
}
