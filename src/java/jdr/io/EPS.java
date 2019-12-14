// File          : EPS.java
// Purpose       : functions to save JDRGroup as EPS file
// Creation Date : 1st February 2006
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
package com.dickimawbooks.jdr.io;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.eps.*;

/**
 * Functions to save and load encapsulated PostScript (EPS) files.
 * Note that some PostScript commands can not be implemented, these
 * will either be approximated by the nearest equivalent, ignored
 * or will throw an exception.
 */
public class EPS
{
   private EPS(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   /**
    * Initialises device default, graphics stack and current 
    * graphics state, {@link #bitmapN} is reset to 0 and
    * empty {@link JDRGroup} is created.
    * @param bitmapBase the directory in which to put any extracted 
    * bitmap images
    * @param in currentfile
    */
   protected EPS(CanvasGraphics cg, String bitmapBase,
      BufferedReader in)
   {
      setCanvasGraphics(cg);
      group = new JDRGroup(cg);

      currentfile = new EPSFile(in);

      systemDict = new EPSSystemDict();
      globalDict = new EPSDict();
      fontDirectory = new EPSDict();

      graphicsStack=new Vector<GraphicsState>();

      savedStates = new Vector<EPSSaveState>();

      deviceDefault
           = new double[]{1, 0, 0, -1, 0, 0};

      stack = new EPSStack(this);

      graphicsState = new GraphicsState(this, deviceDefault);

      graphicsStack.add(graphicsState);

      seed = (new Date()).getTime();
      random = new Random(seed);

      bitmapbase = bitmapBase;

      bitmapN = 0;
   }

   /**
    * Saves image in EPS format.
    * @param allObjects all the objects that define the image
    * @param out the output stream
    * @param creator the name of the application calling this method
    * (for the %%Creator comment)
    * @throws IOException if I/O error occurs
    */
   public static void save(JDRGroup allObjects, PrintWriter out,
      String creator)
      throws IOException
   {
      CanvasGraphics cg = allObjects.getCanvasGraphics();
      BBox box = allObjects.getBpBBox();

      out.println("%!PS-Adobe-3.0 EPSF-3.0");
      out.println("%%Creator: "+creator);

      String desc = allObjects.getDescription();

      if (desc != null && !desc.equals(""))
      {
         out.println("%%Title: "+desc);
      }

      GregorianCalendar date = new GregorianCalendar();
      out.println("%%CreationDate: "+dateFormat.format(date.getTime()));
      out.println("%%LanguageLevel: "+allObjects.psLevel());

      // shift bottom left corner to origin

      double shiftX = -box.getMinX();
      double shiftY = -box.getMaxY();

      out.println("%%BoundingBox: 0 0 "
                  +((int)box.getWidth()) +" " 
                  +((int)box.getHeight()));
      out.println("%%HiResBoundingBox: 0.0 0.0 "
                  +box.getWidth() +" " +box.getHeight());
      out.println("%%EndComments");
      out.println("gsave");
      out.println(""+shiftX+" "+(-shiftY)+" translate");
      out.println("1 -1 scale");
      out.println("[] 0 setdash");
      out.println("10 setmiterlimit");
      out.println("1 setlinewidth");
      out.println("0 setlinecap");
      out.println("0 setlinejoin");
      allObjects.saveEPS(out);
      out.println("grestore");
      out.println("%%Trailer");
      out.println("%%EOF");
   }

   /**
    * Writes the PostScript code to fill the given path with the given
    * paint.
    * @param path the path to fill (in bp)
    * @param paint the fill colour
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public static void fillPath(Shape path, JDRPaint paint, PrintWriter out)
      throws IOException
   {
      if (!(paint instanceof JDRTransparent))
      {
         savePath(path, out);

         BBox bbox = new BBox(null, path.getBounds());

         paint.saveEPS(out, bbox);

         if ((paint instanceof JDRGradient)
          || (paint instanceof JDRRadial))
         {
            out.println("clip shfill");
         }
         else
         {
            out.println("fill");
         }
      }
   }

   /**
    * Writes the PostScript code to stroke the given path with the 
    * given paint.
    * @param path the path to stroke (in bp)
    * @param paint the line colour
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public static void drawPath(Shape path, JDRPaint paint, PrintWriter out)
      throws IOException
   {
      if (!(paint instanceof JDRTransparent))
      {
         savePath(path, out);

         paint.saveEPS(out, null);
         out.println("stroke");
      }
   }

   public static void saveStoragePoint(CanvasGraphics cg, PrintWriter out, double x, double y)
   {
      JDRUnit unit = cg.getStorageUnit();
      out.print(""+unit.toBp(x)+" "+unit.toBp(y)+" ");
   }

   public static void saveStoragePoint(CanvasGraphics cg, PrintWriter out, Point2D p)
   {
      saveStoragePoint(cg, out, p.getX(), p.getY());
   }

   /**
    * Writes the PostScript code defining the given path.
    * @param path the path to define
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public static void savePath(Shape path, PrintWriter out)
      throws IOException
   {
      double[] coords = new double[6];
      int type;
      double oldX=0, oldY=0;

      PathIterator pi = path.getPathIterator(null);

      out.println("newpath");

      while (!pi.isDone())
      {
         type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_CLOSE:
               out.println("closepath");
            break;
            case PathIterator.SEG_MOVETO:
               out.println(""+coords[0]+" "+(coords[1])
                          +" moveto");
               oldX = coords[0];
               oldY = coords[1];
            break;
            case PathIterator.SEG_LINETO:
               out.println(""+coords[0]+" "+(coords[1])
                          +" lineto");
               oldX = coords[0];
               oldY = coords[1];
            break;
            case PathIterator.SEG_QUADTO:
               JDRBezier curve = JDRBezier.quadToCubic(null, oldX, oldY,
                                         coords[0],coords[1],
                                         coords[2],coords[3]);
               out.println(""+curve.getControl1().x+" "
                           +(curve.getControl1().y)
                           +" "+curve.getControl2().x
                           +" "+(curve.getControl2().y)
                           +" "+curve.getEnd().x
                           +" "+(curve.getEnd().y)
                           +" curveto");
               oldX = coords[2];
               oldY = coords[3];
            break;
            case PathIterator.SEG_CUBICTO:
               out.println(""+coords[0]+" "+coords[1]
                           +" "+coords[2]+" "+coords[3]
                           +" "+coords[4]+" "+coords[5]
                           +" curveto");
               oldX = coords[4];
               oldY = coords[5];
            break;
         }

         pi.next();
      }
   }

   public JDRMessage getMessageSystem()
   {
      return canvasGraphics.getMessageSystem();
   }


   /**
    * Loads an Encapsulated PostScript file and returns the image
    * as a {@link JDRGroup}.
    * @param in input stream
    * @param bitmapBase the directory in which to save any raster
    * images found in the EPS file
    * @throws IOException if I/O error occurs
    * @throws InvalidFormatException if invalid PostScript code
    * found, or if PostScript code can't be implemented
    * @throws EOFException if end of file unexpectedly occurs
    * @throws NoninvertibleTransformException if code requires
    * inverting a non-invertible matrix
    */
   public static JDRGroup load(CanvasGraphics cg, BufferedReader in, 
      String bitmapBase)
   throws IOException,InvalidFormatException,
          NoninvertibleTransformException
   {
      String title = "";

      EPS eps = new EPS(cg, bitmapBase, in);

      String line = eps.currentfile.readline();

      warningString = new StringBuffer();

      if (!line.matches("%!PS-Adobe-\\d\\.\\d EPSF-\\d\\.\\d"))
      {
         throw new InvalidFormatException(
            cg.getStringWithValues("error.cant_parse_file_type",
             new String[] {line},
             String.format("Can't parse file type from '%s'", line)));
      }

      eps.currentfile.mark(256);
      line = eps.currentfile.readline();
      BBox box = null;

      while (line.startsWith("%"))
      {
         if (line.startsWith("%%BoundingBox:") && box == null)
         {
            String dimen = line.replaceFirst("%%BoundingBox: *", "");

            if (!dimen.startsWith("(atend)"))
            {
               String[] split = dimen.split(" +");

               if (split.length >= 4)
               {
                  double llx = Double.parseDouble(split[0]);
                  double lly = Double.parseDouble(split[1]);
                  double urx = Double.parseDouble(split[2]);
                  double ury = Double.parseDouble(split[3]);

                  box = new BBox(cg, llx, lly, urx, ury);
               }
            }
         }
         else if (line.startsWith("%%HiResBoundingBox:"))
         {
            String dimen = line.replaceFirst(
               "%%HiResBoundingBox: *", "");

            if (!dimen.startsWith("(atend)"))
            {
               String[] split = dimen.split(" +");

               if (split.length >= 4)
               {
                  double llx = Double.parseDouble(split[0]);
                  double lly = Double.parseDouble(split[1]);
                  double urx = Double.parseDouble(split[2]);
                  double ury = Double.parseDouble(split[3]);

                  box = new BBox(cg, llx, lly, urx, ury);
               }
            }
         }
         else if (line.startsWith("%%Title:"))
         {
            title = line.replaceFirst("%%Title: *","");
         }
         else if (line.startsWith("%%BeginPreview"))
         {
            while (!line.startsWith("%%EndPreview"))
            {
               line = eps.currentfile.readline();

               if (line == null)
               {
                  throw new EOFException(
                     "End of file reached while parsing preview");
               }
            }
         }

         eps.currentfile.mark(256);
         line = eps.currentfile.readline();
      }

      eps.currentfile.reset();

      eps.currentfile.execute(eps);

      eps.group.setDescription(title);

      if (eps.group != null)
      {
         //eps.group.scale(new JDRPoint(), 1,-1);
         BBox bb = eps.group.getStorageBBox();

         if (bb != null)
         {
            eps.group.translate(-bb.getMinX(),-bb.getMinY()); 
         }
      }

      return eps.group;
   }

   /**
    * Process object (adding to stack or executing as required).
    * @param object the object to process
    */
   public void processObject(EPSObject object)
   throws InvalidFormatException,InvalidPathException,
      NoninvertibleTransformException,
      IOException
   {
      stack.processObject(object);
   }

   /**
    * Gets the stack.
    * @return stack
    */
   public EPSStack getStack()
   {
      return stack;
   }

   /**
    * Prints given message if verbose level greater than 0.
    * (Adds terminating new line character.)
    * @param message the message to print 
    * @see #printMessage(String)
    */
   public void printlnMessage(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createVerbose(1, message));
   }

   /**
    * Prints given message if verbose level greater than 0.
    * @param message the message to display
    * @see #printlnMessage(String)
    */
   public void printMessage(String message)
   {
      getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createVerbose(1, message, false));
   }

   /**
    * Resets message system if verbose level greater than 0.
    */
   public void resetProgress()
   {
      if (getVerbosity() > 0)
      {
         getMessageSystem().getPublisher().publishMessages(MessageInfo.createSetProgress(0));
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
         getMessageSystem().getPublisher().publishMessages(MessageInfo.createIncProgress());
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

   /**
    * Prints warning message and appends to warning list.
    * @param file the file being parsed
    * @param message the warning message
    */
   public void warning(EPSFile file, String message)
   {
      String msg;

      if (file.getType() == EPSFile.CURRENTFILE)
      {
         msg = getMessageSystem().getStringWithValues("error.with_line",
             new String[] {String.format("%d", file.getLineNum()), message},
             String.format("%d: %s", file.getLineNum(), message));
      }
      else
      {
         msg = getMessageSystem().getStringWithValues("error.with_file_and_line",
          new String[]
          {
            file.getName(), String.format("%d", file.getLineNum()), message
          },
          String.format("%s:%d: %s", file.getName(), file.getLineNum(), message));
      }

      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createWarning(msg));

      warningString.append(
        String.format("%s%n", 
          getMessageSystem().getStringWithValues(
            "warning.tag",
            new String[] {msg}, 
            String.format("Warning: %s", msg))
        )
      );
   }

   /**
    * Prints warning message and 
    * appends to warning list.
    * @param message the warning message
    */
   public void warning(String message)
   {
      warning(currentfile, message);
   }

   /**
    * Gets line number of current file.
    * @return current line number
    */
   public int getLineNum()
   {
      return currentfile.getLineNum();
   }

   /**
    * Gets all warning messages as a single string.
    * (Each message is separated by the end of line character,
    * the warning string is reset to empty by 
    * {@link #load(CanvasGraphics,BufferedReader,String)})
    * @return warning messages (or empty string if no messages)
    */
   public static String getWarnings()
   {
      return warningString.toString();
   }

   /**
    * Gets the current graphics state.
    * @return the current graphics state
    */
   public GraphicsState getCurrentGraphicsState()
   {
      return graphicsStack.lastElement();
   }

   /**
    * Adds given graphics state to the graphics stack.
    * @param graphicsState the graphics state to add to stack
    */
   public void addGraphicsState(GraphicsState graphicsState)
   {
      graphicsStack.add(graphicsState);
   }

   /**
    * Pops the last element off the graphics stack.
    * Does nothing if only one element left on the stack.
    * @return the element that was removed or null if none removed
    */
   public GraphicsState popGraphicsState()
   {
      int n = graphicsStack.size();

      if (n <= 1)
      {
         return null;
      }

      GraphicsState element = null;

      try
      {
         element = graphicsStack.remove(n-1);
      }
      catch (ArrayIndexOutOfBoundsException ignore)
      {
         // won't happen, already tested n
      }

      return element;
   }

   /**
    * Removes graphic states from stack until saved state is
    * encountered. Removes all to bottom element if state is
    * not found.
    */
   public void grestoreall()
   {
      EPSSaveState savedState = savedStates.lastElement();

      if (savedState == null)
      {
         return;
      }

      savedStates.remove(savedStates.size()-1);

      while (graphicsStack.lastElement() 
               != savedState.getGraphicsState()
          && graphicsStack.size() > 1)
      {
         popGraphicsState();
      }
   }


   /**
    * Saves the current state and returns. (Not fully implemented.)
    */
   public EPSSaveState saveState()
   {
      EPSSaveState savedState = new EPSSaveState(this);

      savedStates.add(savedState);

      return savedState;
   }

   /**
    * Gets the current file.
    * @return current file
    */
   public EPSFile getCurrentFile()
   {
      return currentfile;
   }

   /**
    * Gets the name of the next bitmap to be saved. This method
    * increments {@link #bitmapN} and constructs the filename
    * from {@link #bitmapbase} and {@link #bitmapN}.
    * @param extension file extension (without the dot)
    * @return the file name
    */
   public String getNextBitmapName(String extension)
   {
      bitmapN++;
      return bitmapbase+bitmapN+"."+extension;
   }

   /**
    * Adds given object to {@link #group}.
    */
   public void addJDRObject(JDRCompleteObject object)
   {
      group.add(object);
   }

   /**
    * Sets the given matrix to the device default.
    * @param matrix on exit this contains the device default matrix
    * @throws InvalidFormatException if matrix is not a transformation
    * matrix
    */
   public void setToDefault(EPSArray matrix)
      throws InvalidFormatException
   {
      matrix.setMatrix(deviceDefault);
   }

   /**
    * Gets the LaTeX font base used to determine the LaTeX
    * font size changing declaration.
    */
   public LaTeXFontBase getLaTeXFontBase()
   {
      return canvasGraphics.getLaTeXFontBase();
   }

   /**
    * Gets the systen dictionary.
    */
   public EPSSystemDict getSystemDict()
   {
      return systemDict;
   }

   /**
    * Gets the global dictionary.
    * @return global dictionary
    */
   public EPSDict getGlobalDict()
   {
      return globalDict;
   }

   /**
    * Gets a random integer.
    * @return random integer 
    */
   public int getRandomInt()
   {
      return random.nextInt();
   }

   /**
    * Sets the random generator seed.
    * @param randomSeed new seed
    */
   public void setRandomSeed(long randomSeed)
   {
      seed = randomSeed;
      random.setSeed(seed);
   }

   /**
    * Gets the random generator seed.
    * @return random generator seed
    */
   public long getRandomSeed()
   {
      return seed;
   }

   /**
    * Gets the normal size font.
    * @return normal size font
    */
   public double getNormalSize()
   {
      return latexFontBase.getNormalSize();
   }

   /**
    * Gets named font dictionary from the font directory.
    * @param name the font name
    * @return the font dictionary associated with the font name
    */
   public EPSFont getFontDict(String name)
   {
      EPSFont fontDict;

      try
      {
         fontDict = (EPSFont)fontDirectory.get(name);
      }
      catch (NoReadAccessException e)
      {
         // this shouldn't happen
         return new EPSFont(name, latexFontBase);
      }

      if (fontDict != null)
      {
         return fontDict;
      }

      fontDict = new EPSFont(name, latexFontBase);

      fontDirectory.put(name, fontDict);

      return fontDict;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
      canvasGraphics.setStorageUnit(JDRUnit.BP);
   }

   private static DateFormat dateFormat
      = DateFormat.getDateTimeInstance();

   /**
    * Objects constituting image formed from EPS commands.
    */
   protected JDRGroup group;
   /**
    * Current EPS graphics stack.
    */
   protected Vector<GraphicsState> graphicsStack;
   /**
    * Default transformation matrix.
    */
   protected double[] deviceDefault;

   /**
    * Current graphics state.
    */
   protected GraphicsState graphicsState;

   private EPSStack stack;

   /**
    * Directory in which to place raster images found in 
    * EPS file.
    */
   protected String bitmapbase;

   /**
    * Keeps a count of the number of raster images found.
    */
   protected int bitmapN=0;

   private static StringBuffer warningString = new StringBuffer();

   private EPSFile currentfile=null;

   private LaTeXFontBase latexFontBase;

   private EPSSystemDict systemDict;

   private EPSDict fontDirectory;

   private EPSDict globalDict;

   private Random random;
   private long seed;

   private Vector<EPSSaveState> savedStates;

   private CanvasGraphics canvasGraphics;
}


