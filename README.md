# flowframtk
Vector graphics application that can be used with pgf.sty and flowfram.sty LaTeX packages

Home page: https://www.dickimaw-books.com/software/flowframtk

Screenshots: https://www.dickimaw-books.com/software/flowframtk/screenshots/

Blog posts: https://www.dickimaw-books.com/blog/tag/flowframtk

The latest public release can be obtained from [CTAN](https://ctan.org/pkg/flowframtk).
Newer releases available for testing can be found in the [Releases
page](https://github.com/nlct/flowframtk/releases).

FlowframTk is a vector graphics application written in Java that's
focused on exporting to LaTeX image files containing [`pgf`](https://ctan.org/pkg/pgf) 
code or to class files or packages that use the [`flowfram`](https://ctan.org/pkg/flowfram) 
package. You can use FlowframTk to:

 - Construct shapes using lines, moves and cubic Bézier segments.
 - Edit shapes by changing the defining control points.
 - Add symmetry to shapes.
 - Convert shapes into rotational, scaled or spiral patterns
 - Incorporate text and bitmap images (for annotation and background effects).
 - Display text along a shape.
 - Extract the parameters for TeX's `\parshape` command and for `\shapepar` and `\Shapepar`
   provided by [`\shapepar`](https://ctan.org/pkg/shapepar).
 - Construct frames for use with the [`flowfram`](https://ctan.org/pkg/flowfram) package and export as a class or package.
 - Pictures can be saved in FlowframTk's native open binary format (`.jdr`) or native open plain text format (`.ajr`) or can be exported as:
   + a `pgfpicture` environment for use in LaTeX documents with the [`pgf`](https://ctan.org/pkg/pgf) package;
   + a complete single-paged LaTeX document that contains the `pgf` image code;
   + a LaTeX package or class based on the `flowfram` package;
   + a PNG image file (full-paged or cropped);
   + a PDF image file (full-paged or cropped);
   + a PDF multi-paged document that uses the `flowfram` package for
     the layout;
   + a PostScript (PS) file (if enabled);
   + a scalable vector graphics (SVG) image file (if enabled).
   (Export functions for PDF require a TeX distribution.
    Export functions for PNG, PS, SVG may need a TeX distribution,
    depending on the export setting.
    The export to PS now needs to be explicitly enabled.) 
 - Alternative text can be specified to use when exporting to a LaTeX file.
 - Mappings can be used to specify what LaTeX font declarations should be used when exporting to a LaTeX file.
 - Accompanying Applications: 
   + `jdrview`: A lightweight jdr/ajr image viewer;
   + `jdrinfo`: Reads version information from jdr/ajr files;
   + `jdrconverter`: converts to or from jdr/ajr file format
     (in addition to the jdr and ajr formats, any supported import or export option 
      can be used for source or destination).

I'm currently working on bundling FlowframTk so that it can
be included in TeX Live. This means making some changes in order to
comply with TeX Live requirements.

The [TeX Java Parser library](https://github.com/nlct/texparser) and 
the [TeX Java Help system](https://github.com/nlct/texjavahelp) will need
to be provided as separate packages. FlowframTk will be split into
two separate packages:

 - `texjavadraw`: containing the libraries `jdr.jar` and
   `jdrresources.jar`, the command line applications `jdrinfo`, and
   `jdrconverter`, and the lightweight JDR/AJR viewer `jdrview`.
   Dependencies: `texjavahelp` (which in turn depends on `glossaries-extra`
   and `bib2gls`) and `texjavaparser`.

 - `flowframtk`: the main FlowframTk application.
   Dependencies: `texjavadraw`, `texjavahelp` and `texjavaparser`.

The common language files `jdrcommon-*.xml` will be bundled in `jdrresources.jar` and the
language files for specific applications will be bundled in the
application's jar file.

If you want to add a translation, you can do so via a pull request.
The source for the language files are in the `src/dictionaries`
sub-directory.

Currently, the TeX Java Parser library is only used for SVG imports
that contain MathJax in text areas that needs to be converted to
canvas text. I would like at some point to be able to extend the
parser to read in LaTeX drawing code, but that's a substantial
project which I don't have time for at the moment.

## Source Code

The source code depends on the [TeX Java Help library](https://github.com/nlct/texjavahelp) and the [TeX Java Parser Library](https://github.com/nlct/texparser).
The `texjavahelplib.jar` and `texjavaparserlib.jar` libraries needs to be added to the class path. The easiest way is to add them to the `lib` directory.

The documentation has LaTeX source code but depends on the
applicable language XML files in the `dictionaries`
directory. The TeX Java Help Library comes with some command line
tools that are used to build the documentation:

 - `tjhxml2bib` converts the XML language files to Bib2Gls bib files
   (this allows the documentation to pick up the widget text and
   menu hierarchy);
 - `texjavahelpmk` needs to be run after the PDF is created (so that
   it can pick up the table of contents, cross-references etc)
   and creates the HTML and XML files used for the in-application
   help.
 - `tjhziphelpset` bundles the helpset files into a zip archive.

Information below needs updating.

The `version.tex` file just contains `\date` and is automatically
created to pick up the current version and date information from the
Java source:
```bash
echo "\\date{Version " > version.tex
grep 'String APP_VERSION = ' $baseclass | sed "s/public\sstatic\sfinal\sString\sAPP_VERSION\s=//" | tr -d "\"\; " >> version.tex
grep 'String APP_DATE = ' $baseclass | sed "s/public\sstatic\sfinal\sString\sAPP_DATE\s=//" | tr -d "\"\; " >> version.tex
echo "}" >> version.tex
```
where `$baseclass` is the path to `JDRResources.java`.

For example, to create flowframtk-en.pdf:

Create `flowframtk-props-en.bib` from the XML dictionaries
    (where `$dictdir` is the path to `lib/resources/dictionaries`):
```bash
tjhxml2bib --copy-overwrite $dictdir/texjavahelplib-en.xml $dictdir/flowframtk-en.xml $dictdir/jdrcommon-en.xml -o flowframtk-props-en.bib
```

Build the document. This can be done with Arara:
```bash
arara flowframtk-en
```

If successful, the HTML and XML files can now be created (where
`$targetdir` is the path to `lib/resources/helpsets`):
```bash
texjavahelpmk flowframtk-en.tex $targetdir/flowframtk/en
```

## Licence

License GPLv3+: GNU GPL version 3 or later
http://gnu.org/licenses/gpl.html
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
