package com.dickimawbooks.jdr.io.svg;

import java.util.*;

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
   }

   public void startDocument()
   {
      msgSystem.getPublisher().publishMessages(
        MessageInfo.createVerbose(1, "<xml>"));
   }

   public void endDocument()
   {
      if (base != null)
      {
         msgSystem.getPublisher().publishMessages(
           MessageInfo.createVerbose(1, "</xml>"),
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
        MessageInfo.createVerbose(1, "<"+qName+">"));

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

      msgSystem.getPublisher().publishMessages(MessageInfo.createVerbose(1, "</"+qName+">"));

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

   public void processingInstruction(String target, String data)
      throws SAXException
   {
      msgSystem.getPublisher().publishMessages(
       MessageInfo.createVerbose(1, "Processing. Target: "+target+". Data: "+data));
   }

   public void warning(SAXParseException e)
      throws SAXException
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createWarning(e));
   }

   public void error(SAXParseException e)
      throws SAXException
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createError(e));
   }

   public void fatalError(SAXParseException e)
      throws SAXException
   {
      msgSystem.getPublisher().publishMessages(MessageInfo.createFatalError(e));
   }

   public SVG getSVG()
   {
      return svg;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return group.getCanvasGraphics();
   }

   private JDRGroup group;
   private SVG svg;

   private ArrayDeque<SVGAbstractElement> stack;
   private SVGAbstractElement current = null;
   private SVGElement base = null;

   private JDRMessage msgSystem;
}
