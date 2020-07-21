// File          : JDRView.java
// Description   : JDR/AJR viewer
// Date          : 4th June 2008
// Last Modified : 1 October 2009
// Version       : 1.3
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
import java.net.*;  
import java.awt.print.*;

import javax.swing.*;
import javax.help.*;  
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * JDRView application. Used for just viewing a single
 * JDR/AJR image. Primarily provided as a means of checking the
 * output of applications that create JDR/AJR files.
 * @author Nicola L C Talbot
 */
public class JDRView extends JFrame
   implements ActionListener,JDRApp,Printable
{
   /**
    * Creates a new frame.
    * @param filename the name of the file containing the image
    * to load, or null if no image file
    */
   public JDRView(JDRViewInvoker invoker, String filename)
   {
      this(invoker, filename, true);
   }

   /**
    * Creates a new frame.
    * @param filename the name of the file containing the image
    * to load, or null if no image file
    * @param antiAlias indicates whether to use anti-aliasing
    */
   public JDRView(JDRViewInvoker invoker, String filename, boolean antiAlias)
   {
      this(invoker, filename, antiAlias, null);
   }

   /**
    * Creates a new frame.
    * @param filename the name of the file containing the image
    * to load, or null if no image file
    * @param antiAlias indicates whether to use anti-aliasing
    * @param cwdDir current working directory
    */
   public JDRView(JDRViewInvoker invoker, String filename, 
      boolean antiAlias, File cwdDir)
   {
      super(invoker.getName());
      this.invoker = invoker;

      init(filename, antiAlias, cwdDir);
   }

   private void init(String filename, boolean antiAlias, File cwdDir)
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // set dimensions and location
      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension d = tk.getScreenSize();
      int screenHeight = d.height;
      int screenWidth  = d.width;
      int width  = 3*screenHeight/4;
      int height = screenWidth/2;
      setSize(width,height);
      setLocation((screenWidth-width)/2, (screenHeight-height)/2);

      setIconImage(getResources().getSmallAppIcon().getImage());

      // set up annotation font

      try
      {
         JDRCompleteObject.annoteFont = new Font(
            getResources().getString("font.annote.name"), 0,
            getResources().getInt("font.annote.size"));
      }
      catch (NumberFormatException e)
      {
         getResources().internalError(this,
            getResources().getStringWithValue(
               "internal_error.integer_key",
               "font.annote.size"));
      }

      initializeHelp(this);

      // create menu bar, menu and menu item
      JMenuBar mbar = new JMenuBar();
      setJMenuBar(mbar);

      // File Menu

      fileM = new JMenu(getResources().getString("file.label"));
      fileM.setMnemonic(getResources().getCodePoint("file.mnemonic"));
      mbar.add(fileM);

      // Open

      openItem =
         new JMenuItem(getResources().getString("file.open"),
                       getResources().getCodePoint("file.open.mnemonic"));
      openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
         InputEvent.CTRL_MASK));
      openItem.addActionListener(this);
      fileM.add(openItem);

      // open dialog box
      openjdrFC = new JDRFileChooser(getResources());

      fileFilter = new JdrAjrFileFilter(
         getResources().getString("filter.jdrajr"));

      openjdrFC.addChoosableFileFilter(fileFilter);

      openjdrFC.setFileFilter(fileFilter);

      if (cwdDir != null)
      {
         openjdrFC.setCurrentDirectory(cwdDir);
      }

      // reload item

      reloadItem =
         new JMenuItem(getResources().getString("file.reload"),
                       getResources().getCodePoint("file.reload.mnemonic"));
      reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
         InputEvent.CTRL_MASK));
      reloadItem.addActionListener(this);
      fileM.add(reloadItem);

      // properties item

      propertiesItem =
         new JMenuItem(getResources().getString("file.properties"),
                       getResources().getCodePoint("file.properties.mnemonic"));
      propertiesItem.addActionListener(this);
      fileM.add(propertiesItem);

      propertiesDialog = new JDRPropertiesDialog(this);

      // print item

      printItem =
         new JMenuItem(getResources().getString("file.print"),
            getResources().getCodePoint("file.print.mnemonic"));
      printItem.addActionListener(this);
      fileM.add(printItem);

      // quit item

      quitItem =
         new JMenuItem(getResources().getString("file.quit"),
                       getResources().getCodePoint("file.quit.mnemonic"));
      quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
         InputEvent.CTRL_MASK));
      quitItem.addActionListener(this);
      fileM.add(quitItem);

      // settings menu

      settingsM = new JMenu(getResources().getString("settings.label"));
      settingsM.setMnemonic(getResources().getCodePoint("settings.mnemonic"));
      mbar.add(settingsM);

      // anti-aliasing

      antiAliasItem = new JCheckBoxMenuItem(
         getResources().getString("settings.antialias"), antiAlias);
      antiAliasItem.setMnemonic(
         getResources().getCodePoint("settings.antialias.mnemonic"));
      antiAliasItem.addActionListener(this);
      settingsM.add(antiAliasItem);

      setAntiAlias(antiAlias);

      // Zoom sub menu

      zoomM = new JMenu(getResources().getString("settings.zoom"));
      zoomM.setMnemonic(getResources().getCodePoint("settings.zoom.mnemonic"));
      settingsM.add(zoomM);

      ButtonGroup zoomGroup = new ButtonGroup();
      
      // Fit Width

      zoomWidthItem = new JMenuItem(
         getResources().getString("settings.zoom.width"),
         getResources().getCodePoint("settings.zoom.width.mnemonic"));
      zoomWidthItem.addActionListener(this);
      zoomM.add(zoomWidthItem);

      // Fit Height

      zoomHeightItem = new JMenuItem(
         getResources().getString("settings.zoom.height"),
         getResources().getCodePoint("settings.zoom.height.mnemonic"));
      zoomHeightItem.addActionListener(this);
      zoomM.add(zoomHeightItem);

      // Fit Page

      zoomPageItem = new JMenuItem(
         getResources().getString("settings.zoom.page"),
         getResources().getCodePoint("settings.zoom.page.mnemonic"));
      zoomPageItem.addActionListener(this);
      zoomM.add(zoomPageItem);

      zoomM.addSeparator();

      // User defined zoom

      zoomSettingsItem = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.user"), false);
      zoomSettingsItem.setMnemonic(
         getResources().getCodePoint("settings.zoom.user.mnemonic"));

      zoomSettingsChooserBox = new ZoomSettings(this, this);
      zoomSettingsItem.addActionListener(this);
      zoomM.add(zoomSettingsItem);
      zoomGroup.add(zoomSettingsItem);


      // 25% Magnification

      zoom25Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.25"), false);
      zoom25Item.setMnemonic(getResources().getCodePoint("settings.zoom.25.mnemonic"));
      zoom25Item.addActionListener(this);
      zoomM.add(zoom25Item);
      zoomGroup.add(zoom25Item);

      // 50% Magnification

      zoom50Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.50"), false);
      zoom50Item.setMnemonic(getResources().getCodePoint("settings.zoom.50.mnemonic"));
      zoom50Item.addActionListener(this);
      zoomM.add(zoom50Item);
      zoomGroup.add(zoom50Item);

      // 75% Magnification

      zoom75Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.75"), false);
      zoom75Item.setMnemonic(getResources().getCodePoint("settings.zoom.75.mnemonic"));
      zoom75Item.addActionListener(this);
      zoomM.add(zoom75Item);
      zoomGroup.add(zoom75Item);

      // 100% Magnification

      zoom100Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.100"), true);
      zoom100Item.setMnemonic(getResources().getCodePoint("settings.zoom.100.mnemonic"));
      zoom100Item.addActionListener(this);
      zoomM.add(zoom100Item);
      zoomGroup.add(zoom100Item);

      // 200% Magnification

      zoom200Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.200"), false);
      zoom200Item.setMnemonic(getResources().getCodePoint("settings.zoom.200.mnemonic"));
      zoom200Item.addActionListener(this);
      zoomM.add(zoom200Item);
      zoomGroup.add(zoom200Item);

      // 400% Magnification

      zoom400Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.400"), false);
      zoom400Item.setMnemonic(getResources().getCodePoint("settings.zoom.400.mnemonic"));
      zoom400Item.addActionListener(this);
      zoomM.add(zoom400Item);
      zoomGroup.add(zoom400Item);

      // 800% Magnification

      zoom800Item = new JRadioButtonMenuItem(
         getResources().getString("settings.zoom.800"), false);
      zoom800Item.setMnemonic(getResources().getCodePoint("settings.zoom.800.mnemonic"));
      zoom800Item.addActionListener(this);
      zoomM.add(zoom800Item);
      zoomGroup.add(zoom800Item);


      // help menu

      JMenu helpM = new JMenu(getResources().getString("help.label"));
      helpM.setMnemonic(getResources().getCodePoint("help.mnemonic"));
      mbar.add(helpM);

      // manual

      addHelpItem(helpM);

      // Licence dialog

      licenceDialog = new LicenceDialog(getResources(), this);
      licenceItem = new JMenuItem(
         getResources().getString("help.licence"),
         getResources().getCodePoint("help.licence.mnemonic"));
      licenceItem.addActionListener(this);
      helpM.add(licenceItem);


      // About dialog

      aboutDialog = new AboutDialog(getResources(), this, invoker.getName(),
         invoker.getVersion());
      aboutItem = new JMenuItem(
         getResources().getString("help.about"),
         getResources().getCodePoint("help.about.mnemonic"));
      aboutItem.addActionListener(this);
      helpM.add(aboutItem);

      // set up print request attribute set
      printRequestAttributeSet = new HashPrintRequestAttributeSet();

      // initialise panel
      panel = new JDRViewPanel(this);

      scrollPane = new JScrollPane(panel);

      getContentPane().add(scrollPane);

      scrollPane.getInputMap().put(
         KeyStroke.getKeyStroke("PAGE_UP"),
         "blockScrollUp");
      scrollPane.getActionMap().put(
         "blockScrollUp",
         new BlockScrollUpAction());

      scrollPane.getInputMap().put(
          KeyStroke.getKeyStroke("PAGE_DOWN"),
          "blockScrollDown");
      scrollPane.getActionMap().put(
          "blockScrollDown",
          new BlockScrollDownAction());

      scrollPane.getInputMap().put(
          KeyStroke.getKeyStroke("shift PAGE_UP"),
          "blockScrollLeft");
      scrollPane.getActionMap().put( 
          "blockScrollLeft",
          new BlockScrollLeftAction());

      scrollPane.getInputMap().put(
          KeyStroke.getKeyStroke("shift PAGE_DOWN"),
          "blockScrollRight");
      scrollPane.getActionMap().put(
          "blockScrollRight",
          new BlockScrollRightAction());

      // load filename if specified

      canvasGraphics = new CanvasGraphics(getMessageSystem());

      canvasGraphics.setBitmapChooser(bitmapFC);

      // set the browse utility for bitmaps

      canvasGraphics.setBrowseUtil(
         new BrowseUtil(getResources().getString("browse.label"),
                        getResources().getString("browse.not_found"),
                        getResources().getString("browse.invalid_format"),
                        getResources().getString("browse.cant_refresh"),
                        getResources().getString("browse.title"),
                        getResources().getString("browse.invalid_title"),
                        getResources().getString("browse.discard")));
      if (filename != null)
      {
         loadImage(new File(filename));
      }
      else
      {
         image = null;
         propertiesItem.setEnabled(false);
         reloadItem.setEnabled(false);
      }

      printItem.setEnabled(image != null);

      setVisible(true);
   }

   class BlockScrollUpAction extends AbstractAction
   {
      public BlockScrollUpAction()
      {
         super("blockScrollUp");
      }

      public void actionPerformed(ActionEvent evt)
      {
         blockScrollUp();
      }
   }

   class BlockScrollDownAction extends AbstractAction
   {
      public BlockScrollDownAction()
      {
         super("blockScrollDown");
      }

      public void actionPerformed(ActionEvent evt)
      {
         blockScrollDown();
      }
   }

   class BlockScrollLeftAction extends AbstractAction
   {
      public BlockScrollLeftAction()
      {
         super("blockScrollLeft");
      }

      public void actionPerformed(ActionEvent evt)
      {
         blockScrollLeft();
      }
   }

   class BlockScrollRightAction extends AbstractAction
   {
      public BlockScrollRightAction()
      {
         super("blockScrollRight");
      }

      public void actionPerformed(ActionEvent evt)
      {
         blockScrollRight();
      }
   }

   public void blockScrollUp()
   {
      JScrollBar bar = scrollPane.getVerticalScrollBar();

      int increment = bar.getBlockIncrement(-1);

      int value = bar.getValue() - increment;

      if (value > bar.getMinimum())
      {
         value = bar.getMinimum();
      }

      bar.setValue(value);
   }

   public void blockScrollDown()
   {
      JScrollBar bar = scrollPane.getVerticalScrollBar();

      int increment = bar.getBlockIncrement(1);

      int value = bar.getValue() + increment;

      if (value > bar.getMaximum())
      {
         value = bar.getMaximum();
      }

      bar.setValue(value);
   }

   public void blockScrollLeft()
   {
      JScrollBar bar = scrollPane.getHorizontalScrollBar();

      int increment = bar.getBlockIncrement(-1);

      int value = bar.getValue() - increment;

      if (value < bar.getMinimum())
      {
         value = bar.getMinimum();
      }

      bar.setValue(value);
   }

   public void blockScrollRight()
   {
      JScrollBar bar = scrollPane.getHorizontalScrollBar();

      int increment = bar.getBlockIncrement(1);

      int value = bar.getValue() + increment;

      if (value > bar.getMaximum())
      {
         value = bar.getMaximum();
      }

      bar.setValue(value);
   }

   /**
    * Performs the actions associated with the menu items.
    * @param evt the event that is triggering this action
    */
   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == openItem)
      {
         int result = openjdrFC.showOpenDialog(this);

         if (result == JFileChooser.APPROVE_OPTION)
         {
            loadImage(openjdrFC.getSelectedFile());

            panel.repaint();
         }
      }
      else if (source == reloadItem)
      {
         if (currentFile != null)
         {
            loadImage(currentFile);
            repaint();
         }
      }
      else if (source == propertiesItem)
      {
         if (currentFile != null)
         {
            propertiesDialog.display(currentFile, currentFormat,
               image, (settingsFlag == JDR.NO_SETTINGS ? null :
               getPaper()));
         }
      }
      else if (source == printItem)
      {
         print();
      }
      else if (source == quitItem)
      {
         System.exit(0);
      }
      else if (source == antiAliasItem)
      {
         setAntiAlias(antiAliasItem.isSelected());
         panel.repaint();
      }
      else if (source == zoomSettingsItem)
      {
         zoomSettingsChooserBox.setMag(magnification);
         zoomSettingsChooserBox.display();
      }
      else if (source == zoomWidthItem)
      {
         zoomWidth();
         zoomSettingsItem.setSelected(true);
      }
      else if (source == zoomHeightItem)
      {
         zoomHeight();
         zoomSettingsItem.setSelected(true);
      }
      else if (source == zoomPageItem)
      {
         zoomPage();
         zoomSettingsItem.setSelected(true);
      }
      else if (source == zoom25Item)
      {
         setCurrentMagnification(0.25);
      }
      else if (source == zoom50Item)
      {
         setCurrentMagnification(0.5);
      }
      else if (source == zoom75Item)
      {
         setCurrentMagnification(0.75);
      }
      else if (source == zoom100Item)
      {
         setCurrentMagnification(1);
      }
      else if (source == zoom200Item)
      {
         setCurrentMagnification(2);
      }
      else if (source == zoom400Item)
      {
         setCurrentMagnification(4);
      }
      else if (source == zoom800Item)
      {
         setCurrentMagnification(8);
      }
      else if (source == licenceItem)
      {
         licenceDialog.setVisible(true);
      }
      else if (source == aboutItem)
      {
         aboutDialog.setVisible(true);
      }
   }

   public PrintService getPrintService()
   {
      if (image != null)
      {
         JDRPaper p = canvasGraphics.getPaper();

         printRequestAttributeSet.add(p.isPortrait() ?
            OrientationRequested.PORTRAIT :
            OrientationRequested.LANDSCAPE);
         printRequestAttributeSet.add(p.getMediaSizeName());

         // find print services

         PrintService[] services = PrinterJob.lookupPrintServices();

         if (services.length > 0)
         {
            MediaPrintableArea[] mpa = (MediaPrintableArea[])
               services[0].getSupportedAttributeValues(
                  MediaPrintableArea.class, null,
                  printRequestAttributeSet);

            if (mpa != null && mpa.length > 0)
            {
               printRequestAttributeSet.add(mpa[0]);
            }

            return services[0];
         }
      }

      return null;
   }

   public void print()
   {
      PrintService service = getPrintService();

      if (service != null)
      {
         // obtain printer job
         PrinterJob printJob = PrinterJob.getPrinterJob();

         printJob.setPrintable(this);

         try
         {
            printJob.setPrintService(service);

            if (printJob.printDialog(printRequestAttributeSet))
            {
               printJob.print(printRequestAttributeSet);
            }
         }
         catch (PrinterException pe)
         {
            getResources().error(this, new String[]
               {getResources().getString("error.printing"),
               pe.getMessage()});
         }
         catch (Exception e)
         {
            getResources().internalError(this, e);
         }
      }
      else
      {
         getResources().error(this,
            getResources().getString("error.printing.no_service"));
      }
   }

   public int print(Graphics g, PageFormat pageFormat, int pageIndex)   {
      if (pageIndex > 0)
      {
         return Printable.NO_SUCH_PAGE;
      }
      else
      {
         Graphics2D g2 = (Graphics2D)g;

         RepaintManager currentManager
            = RepaintManager.currentManager(this);
         currentManager.setDoubleBufferingEnabled(false);

         AffineTransform oldAf = g2.getTransform();

         for (int i = 0, n = image.size(); i < n; i++)
         {
             image.get(i).print(g2);
         }

         g2.setTransform(oldAf);

         currentManager.setDoubleBufferingEnabled(true);

         return Printable.PAGE_EXISTS;
      }
   }

   public void zoomWidth()
   {
      double mag = canvasGraphics.getMagnification();

      double paperWidth = canvasGraphics.bpToComponentX(getPaperWidth())
                        / mag;
      double paperHeight = canvasGraphics.bpToComponentY(getPaperHeight())
                         / mag;


      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;

      JScrollBar vBar = scrollPane.getVerticalScrollBar();

      if (!vBar.isVisible())
      {
         // Vertical scroll bar is currently not visible.
         // Will scaling cause it to reappear?

         int viewHeight = dim.height;
         dim = vBar.getSize();

         if (paperHeight*(viewWidth-dim.width)/paperWidth > viewHeight)
         {
            viewWidth -= dim.width;
         }
      }
      else if (scrollPane.getVerticalScrollBarPolicy()
            == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
      {
         // Vertical scroll bar is currently visible.
         // Will scaling cause it to disappear?

         int viewHeight = dim.height;
         dim = vBar.getSize();

         if (paperHeight*(viewWidth+dim.width)/paperWidth < viewHeight)
         {
            viewWidth += dim.width;
         }
      }

      setCurrentMagnification(viewWidth/paperWidth);
   }

   public void zoomHeight()
   {
      double mag = canvasGraphics.getMagnification();

      double paperWidth = canvasGraphics.bpToComponentX(getPaperWidth())
                        / mag;
      double paperHeight = canvasGraphics.bpToComponentY(getPaperHeight())
                         / mag;

      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;
      int viewHeight = dim.height;

      JScrollBar hBar = scrollPane.getHorizontalScrollBar();

      if (!hBar.isVisible())
      {
         // Horizontal scroll bar is currently not visible.
         // Will scaling cause it to reappear?

         dim = hBar.getSize();

         if (paperWidth*(viewHeight-dim.height)/paperHeight > viewWidth)
         {
            viewHeight -= dim.height;
         }
      }
      else if (scrollPane.getHorizontalScrollBarPolicy()
            == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
      {
         // Horizontal scroll bar is currently visible.
         // Will scaling cause it to disappear?

         dim = hBar.getSize();

         if (paperWidth*(viewHeight+dim.height)/paperHeight < viewWidth)
         {
            viewHeight += dim.height;
         }
      }

      setCurrentMagnification(viewHeight/paperHeight);
   }

   public void zoomPage()
   {
      double mag = canvasGraphics.getMagnification();

      double paperWidth = canvasGraphics.bpToComponentX(getPaperWidth())
                        / mag;
      double paperHeight = canvasGraphics.bpToComponentY(getPaperHeight())
                         / mag;


      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;
      int viewHeight = dim.height;

      double magX = viewWidth/paperWidth;
      double magY = viewHeight/paperHeight;

      if (magX < magY)
      {
         JScrollBar vBar = scrollPane.getVerticalScrollBar();

         if (vBar.isVisible() && 
            scrollPane.getVerticalScrollBarPolicy()
               == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
         {
            // Vertical scroll bar is currently visible.
            // Will scaling cause it to disappear?

            dim = vBar.getSize();

            if (paperHeight*(viewWidth+dim.width)/paperWidth 
                  < viewHeight)
            {
               viewWidth += dim.width;

               magX = viewWidth/paperWidth;
            }
         }
      }
      else
      {
         JScrollBar hBar = scrollPane.getHorizontalScrollBar();

         if (hBar.isVisible() && 
            scrollPane.getHorizontalScrollBarPolicy()
               == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
         {
            // Horizontal scroll bar is currently visible.
            // Will scaling cause it to disappear?

            dim = hBar.getSize();

            if (paperWidth*(viewHeight+dim.height)/paperHeight 
                  < viewWidth)
            {
               viewHeight += dim.height;

               magY = viewHeight/paperHeight;
            }
         }
      }

      setCurrentMagnification(magX < magY ? magX : magY);
   }

   public double zoomAction(ZoomValue zoomValue)
   {
      if (zoomValue instanceof PercentageZoomValue)
      {
         double mag = ((PercentageZoomValue)zoomValue).getValue();
         setCurrentMagnification(mag);
         return mag;
      }

      String action = zoomValue.getActionCommand();

      if (action.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         zoomWidth();
      }
      else if (action.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         zoomHeight();
      }
      else if (action.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         zoomPage();
      }
   
      return getCurrentMagnification();
   }

   /**
    * Gets the paper for the current image.
    * @return the paper of the current image
    */
   public JDRPaper getPaper()
   {
      return canvasGraphics.getPaper();
   }

   /**
    * Gets the paper width of the current image.
    * @return the paper width of the current image (in PostScript
    * points)
    */
   public double getPaperWidth()
   {
      return canvasGraphics.getPaperWidth();
   }

   /**
    * Gets the paper height of the current image.
    * @return the paper height of the current image (in PostScript
    * points)
    */
   public double getPaperHeight()
   {
      return canvasGraphics.getPaperHeight();
   }

   /**
    * Gets the rendering hints to use when displaying the image.
    * @return the application's rendering hints
    */
   public RenderingHints getRenderingHints()
   {
      return renderHints;
   }

   /**
    * Sets the rendering hints to use/not use anti-aliasing.
    * @param antialias true if rendering hints should use
    * anti-aliasing
    */
   public void setAntiAlias(boolean antialias)
   {
      if (renderHints == null)
      {
         renderHints = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            (antialias ?
            RenderingHints.VALUE_ANTIALIAS_ON :
            RenderingHints.VALUE_ANTIALIAS_OFF));

         return;
      }

      if (antialias)
      {
         renderHints.put(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
      }
      else
      {
         renderHints.put(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_OFF);
      }
   }

   /**
    * Gets the magnification used to display the image.
    * @return the current magnification
    */
   public double getCurrentMagnification()
   {
      return magnification;
   }

   /**
    * Sets the magnification used to display the image.
    * Does nothing if the given magnification is less than or
    * equal to 0.
    * @param mag the required magnification
    */
   public void setCurrentMagnification(double mag)
   {
      if (mag <= 0)
      {
         return;
      }

      canvasGraphics.setMagnification(mag);

      Dimension dim = new Dimension(
         (int)canvasGraphics.bpToComponentX(canvasGraphics.getPaperWidth()),
         (int)canvasGraphics.bpToComponentY(canvasGraphics.getPaperHeight()));

      setPreferredSize(dim);

      if (panel != null)
      {
         panel.setPreferredSize(dim);
         panel.revalidate();
         panel.repaint();
      }

      if (scrollPane != null)
      {
         scrollPane.setPreferredSize(dim);
         scrollPane.revalidate();
      }
   }

   /**
    * Gets the image currently displayed or null if no image.
    * @return the image currently displayed or null of no image
    */
   public JDRGroup getImage()
   {
      return image;
   }

   /**
    * Adds handbook to the given menu.
    */
   public JMenuItem addHelpItem(JMenu helpM)
   {
      return getResources().addHelpItem(helpM, invoker.getName());
   }

   /**
    * Initializes the helpset.
    */
   public void initializeHelp(JFrame parent)
   {
      getResources().initialiseHelp(parent, invoker.getName().toLowerCase());
   }

   /**
    * Loads image from given filename.
    * @param file the file containing the image
    */
   public void loadImage(File file)
   {
      String lc = file.getName().toLowerCase();

      try
      {
         if (lc.endsWith(".jdr"))
         {
            loadJDRImage(file);
         }
         else if (lc.endsWith(".ajr"))
         {
            loadAJRImage(file);
         }
         else if (AJR.isAJR(file))
         {
            loadAJRImage(file);
         }
         else
         {
            loadJDRImage(file);
         }
      }
      catch (Exception e)
      {
         getResources().error(this, e);
      }
   }

   /**
    * Loads image from given filename (JDR format).
    * @param file the file containing the image
    */
   public void loadJDRImage(File file)
     throws InvalidFormatException,IOException
   {
      currentFile = file;
      currentFormat = "?";

      setTitle(invoker.getName()+" - "+file.toString());
      propertiesItem.setEnabled(true);
      reloadItem.setEnabled(true);
      printItem.setEnabled(true);
      image=null;

      DataInputStream in=null;

      try
      {
         in = new DataInputStream(new BufferedInputStream(new FileInputStream(currentFile)));

         JDR jdr = new JDR();

         image = jdr.load(in, canvasGraphics);

         currentFormat = "JDR "+jdr.getLastLoadedVersion();

         settingsFlag = jdr.getLastLoadedSettingsID();

         if (settingsFlag == JDR.NO_SETTINGS)
         {
            BBox bounds = image.getBpBBox();

            double width = (bounds == null ? 0 : Math.max(100, bounds.getMaxX()));
            double height = (bounds == null ? 0 : Math.max(100, bounds.getMaxY()));

            JDRPaper paper = JDRPaper.getClosestEnclosingPredefinedPaper(
               width, height, jdr.getVersion());

            if (paper == null)
            {
               paper = new JDRPaper(getMessageSystem(), width, height);
            }

            canvasGraphics.setPaper(paper);
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      setCurrentMagnification(magnification);
   }

   /**
    * Loads image from given filename (AJR format).
    * @param file the file containing the image
    */
   public void loadAJRImage(File file)
     throws InvalidFormatException,IOException
   {
      currentFile = file;
      currentFormat = "?";

      setTitle(invoker.getName()+" - "+file.toString());
      propertiesItem.setEnabled(true);
      reloadItem.setEnabled(true);
      printItem.setEnabled(true);
      image=null;

      BufferedReader in=null;

      try
      {
         in = new BufferedReader(new FileReader(currentFile));

         AJR ajr = new AJR();

         image = ajr.load(in, canvasGraphics);

         currentFormat = "AJR "+ajr.getLastLoadedVersion();

         settingsFlag = ajr.getLastLoadedSettingsID();

         if (settingsFlag == JDR.NO_SETTINGS)
         {
            BBox bounds = image.getBpBBox();

            double width = Math.max(100, bounds.getMaxX());
            double height = Math.max(100, bounds.getMinX());

            JDRPaper paper = JDRPaper.getClosestPredefinedPaper(
               width, height, ajr.getVersion());

            if (paper == null)
            {
               paper = new JDRPaper(getMessageSystem(), width, height);
            }

            canvasGraphics.setPaper(paper);
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      setCurrentMagnification(magnification);
   }

   public void enableHelpOnButton(AbstractButton comp, String id)
   {
      getResources().enableHelpOnButton(comp, id);
   }

   public JDRResources getResources()
   {
      return invoker.getResources();
   }

   public JDRGuiMessage getMessageSystem()
   {
      return invoker.getMessageSystem();
   }

   private File currentFile=null;
   private String currentFormat="";

   private JDRGroup image=null;
   private CanvasGraphics canvasGraphics;

   private int settingsFlag=JDRAJR.NO_SETTINGS;

   private double magnification=1.0;

   private JScrollPane scrollPane;
   private JDRViewPanel panel;

   private JMenu fileM, settingsM, zoomM, helpM;

   private JMenuItem openItem, reloadItem, quitItem, 
      zoomWidthItem, zoomHeightItem, zoomPageItem,
      aboutItem, licenceItem, propertiesItem, printItem;

   private JCheckBoxMenuItem antiAliasItem;

   private JRadioButtonMenuItem zoomSettingsItem, zoom25Item,
      zoom50Item, zoom75Item, zoom100Item, zoom200Item, zoom400Item,
      zoom800Item;

   private JDRPropertiesDialog propertiesDialog;

   private ZoomSettings zoomSettingsChooserBox;

   private AboutDialog aboutDialog;

   private LicenceDialog licenceDialog;

   private RenderingHints renderHints=null;

   private JDRFileChooser openjdrFC, bitmapFC;

   private JdrAjrFileFilter fileFilter;

   private HashPrintRequestAttributeSet printRequestAttributeSet;

   private JDRViewInvoker invoker;
}
