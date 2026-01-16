package com.dickimawbooks.jdr.io.svg;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
      importSettings = svg.getImportSettings();
      stack = new ArrayDeque<SVGAbstractElement>();

      msgSystem = group.getCanvasGraphics().getMessageSystem();

      debugMessage("SVG handler initialised");

      GraphicsEnvironment env = 
         GraphicsEnvironment.getLocalGraphicsEnvironment();
      availableFontFamilies = env.getAvailableFontFamilyNames();
   }

   public void startDocument()
   {
      setIndeterminate(true);

      debugMessage("<xml>");
   }

   public void endDocument()
   {
      try
      {
         if (base == null)
         {
            throw new MissingElementException(this, "svg");
         }
         else
         {
            debugMessages("</xml>", "Generating image");

            base.addToImage(group);

            Rectangle2D bounds = base.getViewportBounds();

            if (bounds != null)
            {
               group.translate(-bounds.getX(), -bounds.getY());

               double widthBp = getStorageUnit().toBp(bounds.getWidth());
               double heightBp = getStorageUnit().toBp(bounds.getHeight());
               JDRPaper paper = getCanvasGraphics().getPaper();

               if (paper.getWidth() < widthBp || paper.getHeight() < heightBp)
               {
                  paper = JDRPaper.getClosestEnclosingPredefinedPaper(
                     widthBp, heightBp, JDRAJR.CURRENT_VERSION);

                  if (paper != null)
                  {
                     paper = JDRPaper.getClosestPredefinedPaper(
                        widthBp, heightBp, JDRAJR.CURRENT_VERSION);
                  }

                  getCanvasGraphics().setPaper(paper);
               }
            }
         }
      }
      catch (Throwable e)
      {
         error(e);
      }

      resetProgress();
   }

   public void startElement(String uri, String name, String qName, Attributes attrs)
   {
      debugMessage("<"+qName+">");

      SVGAbstractElement parent = current;

      current = SVGAbstractElement.getElement(this, parent, name);

      if (parent != null)
      {
         parent.addChild(current);
      }

      if (base == null && current instanceof SVGElement)
      {
         base = (SVGElement)current;
      }
   
      stack.push(current);

      current.addAttributes(uri, attrs);
      String id = current.getId();

      if (id != null)
      {
         addElement(id, current);
      }

      try
      {
         current.startElement();
      }
      catch (UnknownElementException e)
      {
         warning(e);
      }
      catch (InvalidFormatException e)
      {
         error(e);
      }
   }

   public void endElement(String uri, String name, String qName)
   {
      if (current != null)
      {
         try
         {
            current.endElement();
         }
         catch (InvalidFormatException e)
         {
            error(e);
         }
      }

      debugMessage("</"+qName+">");

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

   public int getVerbosity()
   {
      return msgSystem.getVerbosity();
   }

   public void setIndeterminate(boolean on)
   {
      if (getVerbosity() > 0)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createIndeterminate(on));
      }
   }

   public void setDeterminate(int max)
   {
      if (getVerbosity() > 0)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createSetProgress(0),
           MessageInfo.createProgress(max)
         );
      }
   }

   public void resetProgress()
   {
      if (getVerbosity() > 0)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createSetProgress(0)
         );
      }
   }

   public void incProgress()
   {
      if (getVerbosity() > 0)
      {
         msgSystem.getPublisher().publishMessages(MessageInfo.createIncProgress());
      }
   }

   public void debugMessage(String msg)
   {
      if (msgSystem.isDebuggingOn())
      {
         msgSystem.getPublisher().publishMessages(MessageInfo.createMessage(msg));
      }
   }

   public void debugMessages(String... msg)
   {
      if (msgSystem.isDebuggingOn())
      {
         for (String s : msg)
         {
            msgSystem.getPublisher().publishMessages(MessageInfo.createMessage(s));
         }
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

   public void setLaTeXText(JDRText jdrText)
   {
      svg.setLaTeXText(jdrText);
   }

   public void addElement(String id, SVGAbstractElement element)
   {
      if (idElementMap == null)
      {
         idElementMap = new HashMap<String,SVGAbstractElement>();
      }

      idElementMap.put(id, element);
   }

   public SVGAbstractElement getElement(String id)
   {
      return idElementMap == null ? null : idElementMap.get(id);
   }

   public SVGAbstractElement getElement(URI uri)
     throws SVGException
   {
      String path = uri.getPath();

      if (path != null && !path.isEmpty())
      {
         throw new ExternalRefUnsupportedException(this, uri.toString());
      }

      return getElement(uri.getFragment());
   }

   public URI parseUriValueRef(String str)
     throws URISyntaxException
   {
      Matcher m = URI_REF_VALUE_PATTERN.matcher(str);

      if (m.matches())
      {
         return new URI(m.group(1));
      }

      return null;
   }

   public SVGAbstractElement getAttributeValueRef(SVGAbstractAttribute attr)
     throws SVGException
   {
      String valueStr = attr.getSourceValue();

      try
      {
         URI uri = parseUriValueRef(valueStr);

         if (uri != null)
         {
            return getElement(uri);
         }
      }
      catch (URISyntaxException e)
      {
         throw new InvalidAttributeValueException(this, attr.getName(), valueStr);
      }

      return null;
   }

   private JDRGroup group;
   private SVG svg;

   ImportSettings importSettings;

   private Point2D lastTextPosition;

   private ArrayDeque<SVGAbstractElement> stack;
   private SVGAbstractElement current = null;
   private SVGElement base = null;

   private JDRMessage msgSystem;
   private String[] availableFontFamilies;

   private HashMap<String,SVGAbstractElement> idElementMap;

   public static final Pattern URI_REF_VALUE_PATTERN
    = Pattern.compile("url\\('(.+)'\\)");
}
