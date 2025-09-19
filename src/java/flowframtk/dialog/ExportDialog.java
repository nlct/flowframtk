/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk.dialog;

import java.io.File;
import java.io.FilenameFilter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.ExportSettings;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box used for exports.
 */

public class ExportDialog extends JDialog
   implements ActionListener,ItemListener
{
   public ExportDialog(FlowframTk application, 
      JFileChooser exportFC,
      TeXFileFilter pgfFileFilter,
      TeXFileFilter latexDocFileFilter,
      ClsFileFilter clsFileFilter,
      StyFileFilter styFileFilter,
      PngFileFilter pngFileFilter,
      EpsFileFilter epsFileFilter,
      PdfFileFilter pdfFileFilter,
      SvgFileFilter svgFileFilter,
      JDRAppSelector appSelector)
   {
      super(application, 
         application.getResources().getMessage("export.title"), true);
      this.application = application;
      this.exportFC = exportFC;

      JDRResources resources = getResources();
      exportSettings = new ExportSettings(resources.getMessageDictionary());

      exportSettings.copyFrom(application.getExportSettings());

      JLabelGroup labelGrp = new JLabelGroup();

      JLabel fileLabel = resources.createAppLabel("export.file");
      labelGrp.add(fileLabel);

      fileField = new FileField(resources, this, exportFC, 
        fileLabel);

      exportFC.addActionListener(this);

      getContentPane().add(fileField, "North");

      int height = 0;
      Dimension dim;

      ExportSettings exportSettings = application.getExportSettings();

      dviLaTeXPanel = new ProcessSettingsPanel(application,
         appSelector, "latex",
         resources.createAppLabel("processes.latex"),
         labelGrp);

      dviLaTeXPanel.initialise(
        exportSettings.dviLaTeXApp,
        exportSettings.dviLaTeXOptions);

      dim = dviLaTeXPanel.getPreferredSize();
      height = dim.height;

      dviLaTeXPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      pdfLaTeXPanel = new ProcessSettingsPanel(application,
         appSelector, "pdflatex",
         resources.createAppLabel("processes.pdflatex"),
         labelGrp);
      pdfLaTeXPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      pdfLaTeXPanel.initialise(
        exportSettings.pdfLaTeXApp,
        exportSettings.pdfLaTeXOptions);

      dim = pdfLaTeXPanel.getPreferredSize();
      height = Math.max(height, dim.height);

      pdftopngPanel = new ProcessSettingsPanel(application,
        appSelector, application.getPdfToPngApp(),
        resources.createAppLabel("processes.pdftopng"), labelGrp);
      pdftopngPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      pdftopngPanel.initialise(
        exportSettings.pdftopngApp,
        exportSettings.pdftopngOptions);

      dim = pdftopngPanel.getPreferredSize();
      height = Math.max(height, dim.height);

      dvipsPanel = new ProcessSettingsPanel(application,
         appSelector, "dvips",
         resources.createAppLabel("processes.dvips"),
         labelGrp);
      dvipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      dvipsPanel.initialise(
        exportSettings.dvipsApp,
        exportSettings.dvipsOptions);

      dim = dvipsPanel.getPreferredSize();
      height = Math.max(height, dim.height);

      dvisvgmPanel = new ProcessSettingsPanel(application,
         appSelector, "dvisvgm",
         resources.createAppLabel("processes.dvisvgm"),
         labelGrp);
      dvisvgmPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      dvisvgmPanel.initialise(
        exportSettings.dvisvgmApp,
        exportSettings.dvisvgmOptions);

      dim = dvisvgmPanel.getPreferredSize();
      height = Math.max(height, dim.height);

      dim = dviLaTeXPanel.getMaximumSize();
      dim.height = height;
      dviLaTeXPanel.setMaximumSize(dim);

      dim = pdfLaTeXPanel.getMaximumSize();
      dim.height = height;
      pdfLaTeXPanel.setMaximumSize(dim);

      dim = dvipsPanel.getMaximumSize();
      dim.height = height;
      dvipsPanel.setMaximumSize(dim);

      dim = dvisvgmPanel.getMaximumSize();
      dim.height = height;
      dvisvgmPanel.setMaximumSize(dim);

      dim = pdftopngPanel.getMaximumSize();
      dim.height = height;
      pdftopngPanel.setMaximumSize(dim);

      libGsComp = Box.createVerticalBox();
      libGsComp.setAlignmentX(Component.LEFT_ALIGNMENT);

      JTextArea libGsArea = resources.createAppInfoArea("appselect.libgs");
      libGsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
      libGsComp.add(libGsArea);

      JLabel libgsLabel = resources.createAppLabel("processes.libgs");
      labelGrp.add(libgsLabel);

      libGsFileField = new FileField(resources, this, "",
        appSelector.getFileChooser(), libgsLabel);

      libGsComp.add(libGsFileField);

      JComponent mainPanel = new JPanel(new BorderLayout());
      getContentPane().add(mainPanel, "Center");

      Box typeComp = Box.createVerticalBox();
      typeComp.setBorder(BorderFactory.createTitledBorder(
         resources.getMessage("export.format")));

      mainPanel.add(typeComp, "North");

      JComponent imageFileTypeComp = createRow();
      typeComp.add(imageFileTypeComp);

      JLabel typeLabel = resources.createAppLabel("export.format.image_type");
      labelGrp.add(typeLabel);
      imageFileTypeComp.add(typeLabel);

      flfFileTypeComp = createRow();
      typeComp.add(flfFileTypeComp);

      typeLabel = resources.createAppLabel("export.format.flf_type");
      labelGrp.add(typeLabel);
      flfFileTypeComp.add(typeLabel);

      fileTypeButtons = new FileTypeButton[MAX_FILE_TYPE_BUTTONS];

      ButtonGroup bg = new ButtonGroup();

      fileTypeButtons[TYPE_PGF] = new FileTypeButton(this,
         ExportSettings.Type.PGF, "pgfpicture", bg,
         pgfFileFilter, ".tex");

      imageFileTypeComp.add(fileTypeButtons[TYPE_PGF]);

      fileTypeButtons[TYPE_IMAGE_DOC] = new FileTypeButton(this, 
         ExportSettings.Type.IMAGE_DOC, "imagedoc", bg,
         latexDocFileFilter, ".tex");

      imageFileTypeComp.add(fileTypeButtons[TYPE_IMAGE_DOC]);

      fileTypeButtons[TYPE_CLS] = new FileTypeButton(this, 
         ExportSettings.Type.CLS, "cls", bg,
         clsFileFilter, ".cls");

      flfFileTypeComp.add(fileTypeButtons[TYPE_CLS]);

      fileTypeButtons[TYPE_STY] = new FileTypeButton(this,
         ExportSettings.Type.STY, "sty", bg,
         styFileFilter, ".sty");

      flfFileTypeComp.add(fileTypeButtons[TYPE_STY]);

      fileTypeButtons[TYPE_IMAGE_PDF] = new FileTypeButton(this,
         ExportSettings.Type.IMAGE_PDF, "imagepdf", bg,
         pdfFileFilter, ".pdf", true, pdfLaTeXPanel);

      imageFileTypeComp.add(fileTypeButtons[TYPE_IMAGE_PDF]);

      fileTypeButtons[TYPE_FLF_DOC] = new FileTypeButton(this, 
         ExportSettings.Type.FLF_DOC, "flfdoc", bg,
         latexDocFileFilter, ".tex");

      flfFileTypeComp.add(fileTypeButtons[TYPE_FLF_DOC]);

      fileTypeButtons[TYPE_FLF_PDF] = new FileTypeButton(this,
         ExportSettings.Type.FLF_PDF, "flfpdf", bg,
         pdfFileFilter, ".pdf", true, pdfLaTeXPanel);

      flfFileTypeComp.add(fileTypeButtons[TYPE_FLF_PDF]);

      fileTypeButtons[TYPE_EPS] = new FileTypeButton(this,
         ExportSettings.Type.EPS, "eps", bg,
         epsFileFilter, ".eps", false, dviLaTeXPanel, dvipsPanel);

      imageFileTypeComp.add(fileTypeButtons[TYPE_EPS]);

      fileTypeButtons[TYPE_SVG] = new FileTypeButton(this, 
         ExportSettings.Type.SVG, "svg", bg,
         svgFileFilter, ".svg", false, dviLaTeXPanel, dvisvgmPanel, libGsComp);

      imageFileTypeComp.add(fileTypeButtons[TYPE_SVG]);

      fileTypeButtons[TYPE_PNG] = new FileTypeButton(this, 
         ExportSettings.Type.PNG, "png", bg,
         pngFileFilter, ".png", false, pdfLaTeXPanel, pdftopngPanel);

      imageFileTypeComp.add(fileTypeButtons[TYPE_PNG]);

      Box settingsPanel = Box.createVerticalBox();
      settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


      boundsComp = createRow();
      settingsPanel.add(boundsComp);

      JLabel boundsLabel = resources.createAppLabel("export.bounds");
      labelGrp.add(boundsLabel);
      boundsComp.add(boundsLabel);

      bg = new ButtonGroup();

      usePaperSizeBoundsBox = resources.createAppRadioButton("export",
        "bounds.papersize", bg, false, null);
      boundsComp.add(usePaperSizeBoundsBox);

      useImageBoundsBox = resources.createAppRadioButton("export",
        "bounds.image", bg, true, null);
      boundsComp.add(useImageBoundsBox);

      useTypeblockBoundsBox = resources.createAppRadioButton("export",
        "bounds.typeblock", bg, false, null);
      boundsComp.add(useTypeblockBoundsBox);

      pngUseAlphaBox = resources.createAppCheckBox(
        "export", "use_alpha", true, null);
      pngUseAlphaBox.addItemListener(this);
      pngUseAlphaBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(pngUseAlphaBox);

      markupComp = createRow();
      settingsPanel.add(markupComp);

      textualShadingComp = createRow();
      settingsPanel.add(textualShadingComp);

      JLabel textualShadingLabel = resources.createAppLabel("export.textualshading");
      labelGrp.add(textualShadingLabel);
      textualShadingComp.add(textualShadingLabel);
      bg = new ButtonGroup();

      textualShadingAverageBox = resources.createAppRadioButton("export",
        "textualshading.average", bg, true, null);
      textualShadingComp.add(textualShadingAverageBox);

      textualShadingStartBox = resources.createAppRadioButton("export",
        "textualshading.start", bg, false, null);
      textualShadingComp.add(textualShadingStartBox);

      textualShadingEndBox = resources.createAppRadioButton("export",
        "textualshading.end", bg, false, null);
      textualShadingComp.add(textualShadingEndBox);

      textualShadingToPathBox = resources.createAppRadioButton("export",
        "textualshading.topath", bg, false, null);
      textualShadingComp.add(textualShadingToPathBox);

      textPathOutlineComp = createRow();
      settingsPanel.add(textPathOutlineComp);

      JLabel textPathOutlineLabel = resources.createAppLabel("export.textpathoutline");
      labelGrp.add(textPathOutlineLabel);
      textPathOutlineComp.add(textPathOutlineLabel);
      bg = new ButtonGroup();

      textPathOutlineIgnoreBox = resources.createAppRadioButton("export",
        "textpathoutline.ignore", bg, true, null);
      textPathOutlineComp.add(textPathOutlineIgnoreBox);

      textPathOutlineToPathBox = resources.createAppRadioButton("export",
        "textpathoutline.topath", bg, false, null);
      textPathOutlineComp.add(textPathOutlineToPathBox);

      shapeparUseHPaddingComp = createRow();
      settingsPanel.add(shapeparUseHPaddingComp);

      JLabel shapeparUseHPaddingLabel = resources.createAppLabel("export.shapeparcs");
      labelGrp.add(shapeparUseHPaddingLabel);
      shapeparUseHPaddingComp.add(shapeparUseHPaddingLabel);
      bg = new ButtonGroup();

      shapeparUseHPaddingOnBox = new JRadioButton("\\Shapepar", true);
      bg.add(shapeparUseHPaddingOnBox);
      shapeparUseHPaddingComp.add(shapeparUseHPaddingOnBox);

      shapeparUseHPaddingOffBox = new JRadioButton("\\shapepar");
      bg.add(shapeparUseHPaddingOffBox);
      shapeparUseHPaddingComp.add(shapeparUseHPaddingOffBox);

      useExternalProcessBox = resources.createAppCheckBox(
        "export", "use_process", true, null);
      useExternalProcessBox.addItemListener(this);
      useExternalProcessBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(useExternalProcessBox);

      docClassComp = createRow();
      settingsPanel.add(docClassComp);
      bg = new ButtonGroup();

      JLabel docClassLabel = resources.createAppLabel("export.load_doc_class");
      labelGrp.add(docClassLabel);
      docClassComp.add(docClassLabel);

      useDefaultDocClassBox = resources.createAppRadioButton("export",
       "load_doc_class.default", bg, true, null);
      useDefaultDocClassBox.addItemListener(this);
      docClassComp.add(useDefaultDocClassBox);

      useSpecifiedDocClassBox = resources.createAppRadioButton("export",
       "load_doc_class.specified", bg, false, null);
      useSpecifiedDocClassBox.addItemListener(this);
      docClassComp.add(useSpecifiedDocClassBox);

      docClassField = new JTextField(16);
      docClassField.setEnabled(false);
      docClassComp.add(docClassField);

      docClassField.setMaximumSize(docClassField.getPreferredSize());

      JLabel markupLabel = resources.createAppLabel("export.markup");
      labelGrp.add(markupLabel);
      markupComp.add(markupLabel);
      bg = new ButtonGroup();

      markupNoneBox = resources.createAppRadioButton("export",
        "markup.none", bg, true, null);
      markupComp.add(markupNoneBox);

      markupPairedBox = resources.createAppRadioButton("export",
        "markup.paired", bg, false, null);
      markupComp.add(markupPairedBox);

      markupEncapBox = resources.createAppRadioButton("export",
        "markup.encap", bg, false, null);
      markupComp.add(markupEncapBox);

      usePdfInfoBox = resources.createAppCheckBox(
        "export", "pdf_info", true, null);
      usePdfInfoBox.addItemListener(this);
      usePdfInfoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(usePdfInfoBox);

      bitmapsToEpsBox = resources.createAppCheckBox(
        "export", "bitmaps_to_eps", false, null);
      bitmapsToEpsBox.addItemListener(this);
      bitmapsToEpsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(bitmapsToEpsBox);

      settingsPanel.add(pdfLaTeXPanel);
      settingsPanel.add(pdftopngPanel);
      settingsPanel.add(dviLaTeXPanel);
      settingsPanel.add(dvipsPanel);
      settingsPanel.add(dvisvgmPanel);
      settingsPanel.add(libGsComp);

      timeoutComp = createRow();
      settingsPanel.add(timeoutComp);

      JLabel timeoutLabel = resources.createAppLabel("processes.timeout");
      labelGrp.add(timeoutLabel);
      timeoutComp.add(timeoutLabel);

      timeoutModel = new SpinnerNumberModel(
        300000L, Long.valueOf(0L), null, Long.valueOf(1));

      timeoutSpinner = new JSpinner(timeoutModel);
      JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor)timeoutSpinner.getEditor();

      ed.getTextField().setColumns(9);
      timeoutComp.add(timeoutSpinner);

      dim = timeoutSpinner.getPreferredSize();
      timeoutSpinner.setMaximumSize(dim);

      timeoutLabel.setLabelFor(timeoutSpinner);

      timeoutComp.add(new JLabel(resources.getMessage("processes.millisecs")));
      timeoutComp.add(Box.createHorizontalGlue());

      settingsPanel.add(Box.createVerticalGlue());

      mainPanel.add(new JScrollPane(settingsPanel), "Center");

      JPanel bottomPanel = new JPanel(new BorderLayout());
      getContentPane().add(bottomPanel, "South");
      
      rememberSettingsBox = resources.createAppCheckBox("export", "remember", true, null);
      rememberSettingsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      bottomPanel.add(rememberSettingsBox, "West");

      // balance

      bottomPanel.add(
       new Box.Filler(
           rememberSettingsBox.getMinimumSize(),
           rememberSettingsBox.getPreferredSize(),
           rememberSettingsBox.getMaximumSize()
          ),
       "East");

      JPanel buttonPanel = new JPanel();

      resources.createOkayCancelHelpButtons(this, buttonPanel, this, "sec:exportimage");

      bottomPanel.add(buttonPanel, "Center");

      // SVG has the longest panel so temporarily enable to
      // calculate maximum preferred height
      fileTypeButtons[TYPE_SVG].setSelected(true);

      pack();
      libGsArea.setMinimumSize(libGsArea.getPreferredSize());
      libGsComp.setMaximumSize(libGsComp.getPreferredSize());

      pack();

      supportEpsSvg = true;
      setEpsSvgSupport(application.getSettings().isSupportExportEpsSvgEnabled());

      fileTypeButtons[TYPE_PGF].setSelected(true);

      setLocationRelativeTo(application);
   }

   protected JComponent createRow()
   {
      JComponent comp = Box.createHorizontalBox();
      comp.setAlignmentX(Component.LEFT_ALIGNMENT);
      return comp;
   }

   public void setEpsSvgSupport(boolean enable)
   {
      if (supportEpsSvg != enable)
      {
         supportEpsSvg = enable;

         fileTypeButtons[TYPE_EPS].setVisible(supportEpsSvg);
         fileTypeButtons[TYPE_SVG].setVisible(supportEpsSvg);
         fileTypeButtons[TYPE_EPS].setEnabled(supportEpsSvg);
         fileTypeButtons[TYPE_SVG].setEnabled(supportEpsSvg);

         if (supportEpsSvg)
         {
            exportFC.addChoosableFileFilter(
             fileTypeButtons[TYPE_EPS].getFileFilter());
            exportFC.addChoosableFileFilter(
             fileTypeButtons[TYPE_SVG].getFileFilter());
         }
         else
         {
            exportFC.removeChoosableFileFilter(
             fileTypeButtons[TYPE_EPS].getFileFilter());
            exportFC.removeChoosableFileFilter(
             fileTypeButtons[TYPE_SVG].getFileFilter());

            if (currentFileTypeButton == fileTypeButtons[TYPE_EPS]
             || currentFileTypeButton == fileTypeButtons[TYPE_SVG])
            {
               fileTypeButtons[TYPE_IMAGE_PDF].setSelected(true);
            }
         }
      }
   }

   public void display(JDRFrame frame)
   {
      this.frame = frame;
      image = frame.getAllPaths();

      exportSettings.copyFrom(application.getSettings().getExportSettings());

      pdfLaTeXPanel.initialise(
        exportSettings.pdfLaTeXApp,
        exportSettings.pdfLaTeXOptions);

      pdftopngPanel.initialise(
        exportSettings.pdftopngApp,
        exportSettings.pdftopngOptions);

      if (supportEpsSvg)
      {
         dviLaTeXPanel.initialise(
           exportSettings.dviLaTeXApp,
           exportSettings.dviLaTeXOptions);

         dvipsPanel.initialise(
           exportSettings.dvipsApp,
           exportSettings.dvipsOptions);

         dvisvgmPanel.initialise(
           exportSettings.dvisvgmApp,
           exportSettings.dvisvgmOptions);

         libGsFileField.setFileName(exportSettings.libgs);
      }

      FlowFrame ff = image.getFlowFrame();

      if (ff == null)
      {
         if (useTypeblockBoundsBox.isSelected())
         {
            useImageBoundsBox.setSelected(true);
         }

         useTypeblockBoundsBox.setEnabled(false);

         for (int i = 0; i < flfFileTypeComp.getComponentCount(); i++)
         {
            flfFileTypeComp.getComponent(i).setEnabled(false);
         }

         if (exportSettings.type == ExportSettings.Type.STY
          || exportSettings.type == ExportSettings.Type.CLS)
         {
            exportSettings.type = ExportSettings.Type.PGF;
         }
         else if (exportSettings.type == ExportSettings.Type.FLF_DOC)
         {
            exportSettings.type = ExportSettings.Type.IMAGE_DOC;
         }
      }
      else
      {
         useTypeblockBoundsBox.setEnabled(true);

         for (int i = 0; i < flfFileTypeComp.getComponentCount(); i++)
         {
            flfFileTypeComp.getComponent(i).setEnabled(true);
         }
      }

      File file = frame.getCurrentExportFile();

      if (file == null && !frame.getFilename().isEmpty())
      {
         file = new File(frame.getFilename());

         String name = file.getName();
         String basename = name;

         int idx = name.lastIndexOf(".");
         String ext = null;

         if (idx > -1)
         {
            ext = name.substring(idx+1).toLowerCase();

            if (ext.equals("ajr") || ext.equals("jdr"))
            {
               basename = name.substring(0, idx);
            }
         }

         switch (exportSettings.type)
         {
            case CLS:
              ext = ".cls";
            break;
            case STY:
              ext = ".sty";
            break;
            case IMAGE_PDF:
            case FLF_PDF:
              ext = ".pdf";
            break;
            case EPS:
              ext = ".eps";
            break;
            case SVG:
              ext = ".svg";
            break;
            case PNG:
              ext = ".png";
            break;
            case PGF:
            case IMAGE_DOC:
            case FLF_DOC:
            default: ext = ".tex";
         }

         name = basename + ext;

         File currentDir = exportFC.getCurrentDirectory();

         file = new File(currentDir, name);
      }

      FileTypeButton fileTypeBtn = currentFileTypeButton;

      if (file != null)
      {
         exportFC.setSelectedFile(file);

         if (fileTypeBtn != null && !fileTypeBtn.accept(file))
         {
            for (FileTypeButton btn : fileTypeButtons)
            {
               if (btn.isEnabled() && btn.accept(file))
               {
                  fileTypeBtn = btn;
                  break;
               }
            }
         }
      }

      if (fileTypeBtn == null || !fileTypeBtn.isEnabled())
      {
         fileTypeBtn = fileTypeButtons[TYPE_PGF];
      }

      switch (exportSettings.bounds)
      {
         case PAPER:
           usePaperSizeBoundsBox.setSelected(true);
         break;
         case IMAGE:
           useImageBoundsBox.setSelected(true);
         break;
         case TYPEBLOCK:
           useTypeblockBoundsBox.setSelected(true);
         break;
      }

      switch (exportSettings.objectMarkup)
      {
         case NONE:
            markupNoneBox.setSelected(true);
         break;
         case PAIRED:
            markupPairedBox.setSelected(true);
         break;
         case ENCAP:
            markupEncapBox.setSelected(true);
         break;
      }

      switch (exportSettings.textualShading)
      {
         case AVERAGE:
            textualShadingAverageBox.setSelected(true);
         break;
         case START:
            textualShadingStartBox.setSelected(true);
         break;
         case END:
            textualShadingEndBox.setSelected(true);
         break;
         case TO_PATH:
            textualShadingToPathBox.setSelected(true);
         break;
      }

      switch (exportSettings.textPathOutline)
      {
         case TO_PATH:
            textPathOutlineToPathBox.setSelected(true);
         break;
         case IGNORE:
            textPathOutlineIgnoreBox.setSelected(true);
         break;
      }

      useExternalProcessBox.setSelected(exportSettings.useExternalProcess);

      pngUseAlphaBox.setSelected(exportSettings.pngUseAlpha);

      usePdfInfoBox.setSelected(exportSettings.usePdfInfo);

      bitmapsToEpsBox.setSelected(exportSettings.bitmapsToEps);

      if (exportSettings.shapeparUseHpadding)
      {
         shapeparUseHPaddingOnBox.setSelected(true);
      }
      else
      {
         shapeparUseHPaddingOffBox.setSelected(true);
      }

      CanvasGraphics cg = image.getCanvasGraphics();

      if (cg.hasDocClass())
      {
         useSpecifiedDocClassBox.setSelected(false);
         docClassField.setText(cg.getDocClass());
      }
      else if (exportSettings.docClass != null)
      {
         useDefaultDocClassBox.setSelected(true);
         docClassField.setText(exportSettings.docClass);
      }
      else
      {
         docClassField.setText("");
         useDefaultDocClassBox.setSelected(true);
      }

      timeoutSpinner.setValue(Long.valueOf(exportSettings.timeout));

      if (fileTypeBtn != currentFileTypeButton)
      {
         fileTypeBtn.setSelected(true);
      }

      fileField.requestFocusInWindow();

      setVisible(true);
   }

   protected void okay()
   {
      File file = fileField.getFile();

      if (file == null)
      {
         getResources().error(this, getResources().getMessage("error.no_filename"));

         return;
      }

      if (dviLaTeXPanel.isVisible())
      {
         exportSettings.dviLaTeXApp = dviLaTeXPanel.getFileName();

         if (exportSettings.dviLaTeXApp == null || exportSettings.dviLaTeXApp.isEmpty())
         {
            String name = dviLaTeXPanel.getName();

            getResources().error(this,
             getResources().getMessage("error.no_required_setting", 
              name == null ? "latex" : name));

            return;
         }

         exportSettings.dviLaTeXOptions = dviLaTeXPanel.getOptionArray();
      }

      if (pdfLaTeXPanel.isVisible())
      {
         exportSettings.pdfLaTeXApp = pdfLaTeXPanel.getFileName();

         if (exportSettings.pdfLaTeXApp == null || exportSettings.pdfLaTeXApp.isEmpty())
         {
            String name = pdfLaTeXPanel.getName();

            getResources().error(this,
             getResources().getMessage("error.no_required_setting", 
              name == null ? "pdflatex" : name));

            return;
         }

         exportSettings.pdfLaTeXOptions = pdfLaTeXPanel.getOptionArray();
      }

      if (dvipsPanel.isVisible())
      {
         exportSettings.dvipsApp = dvipsPanel.getFileName();

         if (exportSettings.dvipsApp == null || exportSettings.dvipsApp.isEmpty())
         {
            String name = dvipsPanel.getName();

            getResources().error(this,
             getResources().getMessage("error.no_required_setting", 
              name == null ? "dvips" : name));
         }

         exportSettings.dvipsOptions = dvipsPanel.getOptionArray();
      }

      if (dvisvgmPanel.isVisible())
      {
         exportSettings.dvisvgmApp = dvisvgmPanel.getFileName();

         if (exportSettings.dvisvgmApp == null
          || exportSettings.dvisvgmApp.isEmpty())
         {
            String name = dvisvgmPanel.getName();

            getResources().error(this,
             getResources().getMessage("error.no_required_setting", 
              name == null ? "dvisvgm" : name));

            return;
         }

         exportSettings.dvisvgmOptions = dvisvgmPanel.getOptionArray();

         exportSettings.libgs = libGsFileField.getFileName();
      }

      if (pdftopngPanel.isVisible())
      {
         exportSettings.pdftopngApp = pdftopngPanel.getFileName();

         if (exportSettings.pdftopngApp == null
          || exportSettings.pdftopngApp.isEmpty())
         {
            String name = pdftopngPanel.getName();

            getResources().error(this,
             getResources().getMessage("error.no_required_setting", 
              name == null ? "pdftopng" : name));

            return;
         }

         exportSettings.pdftopngOptions = pdftopngPanel.getOptionArray();
      }

      if (file.exists())
      {
         int selection = getResources().confirm(frame,
            new String[]
            {file.getAbsolutePath(),
            getResources().getMessage("warning.file_exists")},
            getResources().getMessage("warning.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

         if (selection != JOptionPane.YES_OPTION) return;
      }

      frame.setCurrentExportFile(file);

      exportSettings.type = currentFileTypeButton.getType();

      if (boundsComp.isVisible())
      {
         if (usePaperSizeBoundsBox.isSelected())
         {
            exportSettings.bounds = ExportSettings.Bounds.PAPER;
         }
         else if (useImageBoundsBox.isSelected())
         {
            exportSettings.bounds = ExportSettings.Bounds.IMAGE;
         }
         else if ( useTypeblockBoundsBox.isEnabled()
                && useTypeblockBoundsBox.isSelected()
                 )
         {
            exportSettings.bounds = ExportSettings.Bounds.TYPEBLOCK;
         }
      }

      if (markupComp.isVisible())
      {
         if (markupNoneBox.isSelected())
         {
            exportSettings.objectMarkup = ExportSettings.ObjectMarkup.NONE;
         }
         else if (markupPairedBox.isSelected())
         {
            exportSettings.objectMarkup = ExportSettings.ObjectMarkup.PAIRED;
         }
         else if (markupEncapBox.isSelected())
         {
            exportSettings.objectMarkup = ExportSettings.ObjectMarkup.ENCAP;
         }
      }

      if (textualShadingComp.isVisible())
      {
         if (textualShadingAverageBox.isSelected())
         {
            exportSettings.textualShading = ExportSettings.TextualShading.AVERAGE;
         }
         else if (textualShadingStartBox.isSelected())
         {
            exportSettings.textualShading = ExportSettings.TextualShading.START;
         }
         else if (textualShadingEndBox.isSelected())
         {
            exportSettings.textualShading = ExportSettings.TextualShading.END;
         }
         else if (textualShadingToPathBox.isSelected())
         {
            exportSettings.textualShading = ExportSettings.TextualShading.TO_PATH;
         }
      }

      if (textPathOutlineComp.isVisible())
      {
         if (textPathOutlineToPathBox.isSelected())
         {
            exportSettings.textPathOutline = ExportSettings.TextPathOutline.TO_PATH;
         }
         else if (textPathOutlineIgnoreBox.isSelected())
         {
            exportSettings.textPathOutline = ExportSettings.TextPathOutline.IGNORE;
         }
      }

      if (useExternalProcessBox.isVisible())
      {
         exportSettings.useExternalProcess = useExternalProcessBox.isSelected();
      }

      if (timeoutComp.isVisible())
      {
         exportSettings.timeout = timeoutModel.getNumber().longValue();
      }

      if (pngUseAlphaBox.isVisible())
      {
         exportSettings.pngUseAlpha = pngUseAlphaBox.isSelected();
      }

      if (usePdfInfoBox.isVisible())
      {
         exportSettings.usePdfInfo = usePdfInfoBox.isSelected();
      }

      if (bitmapsToEpsBox.isVisible())
      {
         exportSettings.bitmapsToEps = bitmapsToEpsBox.isSelected();
      }

      if (shapeparUseHPaddingComp.isVisible())
      {
         exportSettings.shapeparUseHpadding = shapeparUseHPaddingOnBox.isSelected();
      }

      if (docClassComp.isVisible())
      {
         if (useDefaultDocClassBox.isSelected())
         {
            exportSettings.docClass = null;
         }
         else
         {
            exportSettings.docClass = docClassField.getText().trim();
         }
      }

      if (rememberSettingsBox.isSelected())
      {
         application.getSettings().copyFrom(exportSettings);

         if (docClassComp.isVisible()
             && useSpecifiedDocClassBox.isSelected())
         {
            String cls = docClassField.getText().trim();

            if (!cls.isEmpty())
            {
               image.getCanvasGraphics().setDocClass(cls);
            }
         }
      }

      setVisible(false);

      switch (exportSettings.type)
      {
         case IMAGE_PDF:
         case FLF_PDF:
           frame.savePDF(file, exportSettings);
         break;
         case EPS:
           frame.saveEPS(file, exportSettings);
         break;
         case SVG:
           frame.saveSVG(file, exportSettings);
         break;
         case PNG:
           frame.savePNG(file, exportSettings);
         break;
         case STY:
         case CLS:
         case FLF_DOC:
            frame.saveFlowFrame(file, exportSettings);
         break;
         case PGF:
         case IMAGE_DOC:
         default:
           frame.savePGF(file, exportSettings);
      }
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      Object src = evt.getSource();

      if (src == useDefaultDocClassBox
        || src == useSpecifiedDocClassBox)
      {
         if (useDefaultDocClassBox.isSelected())
         {
            docClassField.setEnabled(false);
         }
         else
         {
            docClassField.setEnabled(true);
            docClassField.requestFocusInWindow();
         }
      }
      else
      {
         updateWidgets(evt);
      }
   }

   protected void updateWidgets(ItemEvent evt)
   {
      FileTypeButton prevButton = currentFileTypeButton;

      for (FileTypeButton btn : fileTypeButtons)
      {
         if (btn.isSelected())
         {
            currentFileTypeButton = btn;
            break;
         }
      }

      if (currentFileTypeButton == null)
      {
         currentFileTypeButton = fileTypeButtons[TYPE_PGF];
      }

      if (prevButton != currentFileTypeButton
       || evt.getSource() == useExternalProcessBox)
      {
         dviLaTeXPanel.setVisible(false);
         pdfLaTeXPanel.setVisible(false);
         pdftopngPanel.setVisible(false);
         dvipsPanel.setVisible(false);
         dvisvgmPanel.setVisible(false);
         libGsComp.setVisible(false);

         currentFileTypeButton.showProcessPanels();

         exportFC.setFileFilter(currentFileTypeButton.getFileFilter());

         File file = fileField.getFile();

         if (file != null && !currentFileTypeButton.accept(file))
         {
            String name = file.getName();
            int idx = name.lastIndexOf(".");

            if (idx > 0)
            {
               name = name.substring(0, idx)
                 + currentFileTypeButton.getDefaultExtension() ;

               file = new File(file.getParentFile(), name);
               fileField.setFile(file);
            }
         }
      }

      boolean requiresExternalProcess = currentFileTypeButton.requiresProcesses();
      boolean mayUseExternalProcess = currentFileTypeButton.mayRequireProcesses();

      useExternalProcessBox.setVisible(!requiresExternalProcess && mayUseExternalProcess);

      timeoutComp.setVisible(requiresExternalProcess
       || (mayUseExternalProcess && useExternalProcessBox.isSelected()));

      boolean showBounds = true;
      boolean showAlpha = false;
      boolean enablePaperSize = true;
      boolean showShapePar = false;
      boolean showBitmapsToEps = false;
      boolean showMarkup = false;
      boolean showDocClassComp = false;
      boolean showTextualShading = true;
      boolean showPathOutline = true;

      if (useExternalProcessBox.isVisible() && useExternalProcessBox.isSelected())
      {
         showDocClassComp = true;
         showMarkup = true;
      }

      ExportSettings.Type type = currentFileTypeButton.getType();

      switch (type)
      {
         case EPS:
         case SVG:
            showBitmapsToEps = useExternalProcessBox.isSelected();
         break;
         case IMAGE_PDF:
         case IMAGE_DOC:
           showDocClassComp = true;
           showMarkup = true;
         break;
         case CLS:
         case FLF_DOC:
         case FLF_PDF:
           showDocClassComp = true;
         case STY:
           showBounds = false;
           showShapePar = true;
         break;
         case PNG:
            if (useExternalProcessBox.isSelected())
            {
               showAlpha = false;
               showTextualShading = true;
               showPathOutline = true;
            }
            else
            {
               showAlpha = true;
               showTextualShading = false;
               showPathOutline = false;
            }
         break;
         case PGF:
            if (usePaperSizeBoundsBox.isSelected())
            {
               useImageBoundsBox.setSelected(true);
            }

            enablePaperSize = false;
            showMarkup = true;
         break;
      }

      boundsComp.setVisible(showBounds);
      pngUseAlphaBox.setVisible(showAlpha);
      usePaperSizeBoundsBox.setEnabled(enablePaperSize);
      shapeparUseHPaddingComp.setVisible(showShapePar);
      docClassComp.setVisible(showDocClassComp);
      usePdfInfoBox.setVisible(showDocClassComp 
        && !(type == ExportSettings.Type.STY || type == ExportSettings.Type.PNG));
      bitmapsToEpsBox.setVisible(showBitmapsToEps);
      markupComp.setVisible(showMarkup);
      textualShadingComp.setVisible(showTextualShading);
      textPathOutlineComp.setVisible(showPathOutline);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
      else if (action.equals("ApproveSelection"))
      {
         // file chooser selection approved

         FileFilter filter = exportFC.getFileFilter();

         if (currentFileTypeButton.getFileFilter() == filter)
         {
            return;
         }

         for (FileTypeButton btn : fileTypeButtons)
         {
            if (btn.getFileFilter() == filter)
            {
               btn.setSelected(true);
               return;
            }
         }

         // Not found. May have all files filter set (if available)

         File file = exportFC.getSelectedFile();

         for (FileTypeButton btn : fileTypeButtons)
         {
            if (btn.accept(file))
            {
               btn.setSelected(true);
               return;
            }
         }
      }
   }

   public boolean isUseExternalProcessOn()
   {
      return useExternalProcessBox.isSelected() && useExternalProcessBox.isEnabled();
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FileTypeButton[] fileTypeButtons;
   private FileTypeButton currentFileTypeButton;
   private JComponent flfFileTypeComp;

   private FileField fileField, libGsFileField;

   private ProcessSettingsPanel dviLaTeXPanel, pdfLaTeXPanel, dvipsPanel,
     dvisvgmPanel, pdftopngPanel;
   private JComponent libGsComp;

   private JComponent timeoutComp;
   private JSpinner timeoutSpinner;
   private SpinnerNumberModel timeoutModel;

   private JCheckBox useExternalProcessBox, rememberSettingsBox, pngUseAlphaBox,
     usePdfInfoBox, bitmapsToEpsBox;

   private JComponent boundsComp;
   private JRadioButton usePaperSizeBoundsBox, useImageBoundsBox, useTypeblockBoundsBox;

   private JComponent markupComp;
   private JRadioButton markupNoneBox, markupPairedBox, markupEncapBox;

   private JComponent textualShadingComp;
   private JRadioButton textualShadingAverageBox, textualShadingStartBox,
     textualShadingEndBox, textualShadingToPathBox;

   private JComponent textPathOutlineComp;
   private JRadioButton textPathOutlineToPathBox, textPathOutlineIgnoreBox;

   private JComponent shapeparUseHPaddingComp;
   private JRadioButton shapeparUseHPaddingOnBox, shapeparUseHPaddingOffBox;

   private JComponent docClassComp;
   private JRadioButton useDefaultDocClassBox, useSpecifiedDocClassBox;
   private JTextField docClassField;

   private FlowframTk application;
   private JDRFrame frame;
   private JDRGroup image;
   private JFileChooser exportFC;
   private ExportSettings exportSettings;
   private boolean supportEpsSvg;

   public static final int TYPE_PGF=0, TYPE_IMAGE_DOC=1, TYPE_FLF_DOC=2,
    TYPE_CLS=3, TYPE_STY=4, TYPE_IMAGE_PDF=5, TYPE_FLF_PDF=6,
    TYPE_EPS=7, TYPE_SVG=8, TYPE_PNG=9;

   public static final int MAX_FILE_TYPE_BUTTONS = 10;
}

class FileTypeButton extends JRadioButton
{
   public FileTypeButton(ExportDialog dialog, ExportSettings.Type type,
     String tag, ButtonGroup bg,
     FileFilter filter, String defExt)
   {
      this(dialog, type, tag, bg, filter, defExt, false);
   }

   public FileTypeButton(ExportDialog dialog, ExportSettings.Type type,
     String tag, ButtonGroup bg,
     FileFilter filter, String defExt, boolean requiresProcesses, JComponent... processComps)
   {
      super(dialog.getResources().getMessage("export."+tag));
      this.dialog = dialog;
      this.type = type;
      this.filter = filter;
      this.defExt = defExt;
      this.requiresProcesses = requiresProcesses;
      this.processComps = processComps;

      bg.add(this);

      tag = "export."+tag;
      int mnemonic = dialog.getResources().getMnemonic(tag);

      if (mnemonic > 0)
      {
         setMnemonic(mnemonic);
      }

      String tooltip = dialog.getResources().getToolTipText(tag);

      if (tooltip != null)
      {
         setToolTipText(tooltip);
      }

      addItemListener(dialog);
   }

   public boolean requiresProcesses()
   {
      return requiresProcesses;
   }

   public boolean mayRequireProcesses()
   {
      return processComps.length > 0;
   }

   public void showProcessPanels()
   {
      boolean visible = requiresProcesses || dialog.isUseExternalProcessOn();

      for (JComponent comp : processComps)
      {
         comp.setVisible(visible);
      }
   }

   public FileFilter getFileFilter()
   {
      return filter;
   }

   public boolean accept(File file)
   {
      return filter.accept(file);
   }

   public String getDefaultExtension()
   {
      return defExt;
   }

   public ExportSettings.Type getType()
   {
      return type;
   }

   ExportDialog dialog;
   FileFilter filter;
   boolean requiresProcesses;
   JComponent[] processComps;
   String defExt;
   ExportSettings.Type type;
}
