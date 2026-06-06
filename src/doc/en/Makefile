resourcesdir=../../lib
dictdir=../../dictionaries
baseclass=../../java/jdrresources/JDRResources.java
jdrajrclass=../../java/jdr/io/JDRAJR.java
arara=arara --verbose
#texjavahelpmk=texjavahelpmk --debug-mode all --log texjavahelpmk.log
texjavahelpmk=texjavahelpmk
tjhxml2bib=tjhxml2bib --copy-overwrite-xml
tjhziphelpset=tjhziphelpset

all	: flowframtk.1 \
	  $(resourcesdir)/flowframtk-helpset.zip \
	  $(resourcesdir)/jdrview-helpset.zip \
	  jdrutils-en.pdf
	@grep -E '(Package glossaries-extra Warning:|unreferenced destination)' *.log || true

en-GB	: flowframtk-helpset/en-GB/index.xml \
	jdrview-helpset/en-GB/index.xml

en	: flowframtk-helpset/en/index.xml \
	jdrview-helpset/en/index.xml

flowframtk.1	: flowframtk.pod
		pod2man -c flowframtk flowframtk.pod flowframtk.1

flowframtk-props-en.bib	: $(dictdir)/flowframtk-en.xml \
			$(dictdir)/jdrcommon-en.xml 
	$(tjhxml2bib) --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	$(dictdir)/flowframtk-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	-o flowframtk-props-en.bib

flowframtk-props-en-GB.bib	: $(dictdir)/flowframtk-en.xml \
				$(dictdir)/flowframtk-en-GB.xml  \
				$(dictdir)/jdrcommon-en.xml \
				$(dictdir)/jdrcommon-en-GB.xml 
	$(tjhxml2bib) --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	$(dictdir)/flowframtk-en.xml \
	$(dictdir)/flowframtk-en-GB.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrcommon-en-GB.xml \
	-o flowframtk-props-en-GB.bib

jdrview-props-en.bib	: $(dictdir)/jdrview-en.xml $(dictdir)/jdrcommon-en.xml 
	$(tjhxml2bib) --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrview-en.xml \
	-o jdrview-props-en.bib

jdrview-props-en-GB.bib	: $(dictdir)/jdrview-en.xml \
	$(dictdir)/jdrcommon-en.xml $(dictdir)/jdrcommon-en-GB.xml
	$(tjhxml2bib) --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrcommon-en-GB.xml \
	$(dictdir)/jdrview-en.xml \
	-o jdrview-props-en-GB.bib


version.tex	: $(baseclass)
	@echo "\\versiondate{" > version.tex
	@grep 'String APP_VERSION = ' $(baseclass) | sed "s/public\sstatic\sfinal\sString\sAPP_VERSION\s=//" | tr -d "\"\; " >> version.tex
	@echo "}{" >> version.tex
	@grep 'String APP_DATE = ' $(baseclass) | sed "s/public\sstatic\sfinal\sString\sAPP_DATE\s=//" | tr -d "\"\; " >> version.tex
	@echo "}" >> version.tex

jdrversion.tex	: $(jdrajrclass)
	@echo "\\newcommand\\jdrversion{%" > jdrversion.tex
	@grep '// latest stable version' $(jdrajrclass) | sed "s/public\sstatic\sfinal\sfloat\sCURRENT_VERSION\s=//" | tr -d "f\;// latest stable version" >> jdrversion.tex
	@echo "\\unskip}" >> jdrversion.tex

flowframtk-en-GB.pdf	: flowframtk-en-GB.glstex 
	lualatex flowframtk-en-GB
	lualatex flowframtk-en-GB
	@grep -E '(Package glossaries-extra Warning:|unreferenced destination)' flowframtk-en-GB.log || true

flowframtk-en-GB.glstex	: flowframtk.bib shared.bib flowframtk-props-en-GB.bib \
			 flowframtk-en-GB.tex flowframtk-main.tex version.tex \
			 helpinterface.tex images-en-GB/*.png \
			../sharedimages/*.png ../sharedimages/*.tex \
			../en-sharedimages/*.png
	lualatex flowframtk-en-GB
	bibtex flowframtk-en-GB
	bib2gls flowframtk-en-GB
	lualatex flowframtk-en-GB
	bib2gls flowframtk-en-GB

flowframtk-en.pdf	: flowframtk-en.glstex 
	lualatex flowframtk-en
	lualatex flowframtk-en
	@grep -E '(Package glossaries-extra Warning:|unreferenced destination)' flowframtk-en.log || true

flowframtk-en.glstex	: flowframtk.bib shared.bib flowframtk-props-en.bib \
			 flowframtk-en.tex flowframtk-main.tex version.tex \
			 helpinterface.tex images-en/*.png \
			../sharedimages/*.png ../sharedimages/*.tex \
			../en-sharedimages/*.png
	lualatex flowframtk-en
	bibtex flowframtk-en
	bib2gls flowframtk-en
	lualatex flowframtk-en
	bibtex flowframtk-en


jdrview-en.pdf	: jdrview-en.glstex
	lualatex jdrview-en
	lualatex jdrview-en

jdrview-en.glstex	: jdrview.bib shared.bib jdrview-props-en.bib \
		jdrview-en.tex jdrview-main.tex version.tex \
		helpinterface.tex \
		../sharedimages/*.png ../sharedimages/*.tex
	lualatex jdrview-en
	bib2gls jdrview-en

jdrview-en-GB.pdf	: jdrview-en-GB.glstex
	lualatex jdrview-en-GB
	lualatex jdrview-en-GB

jdrview-en-GB.glstex	: jdrview.bib shared.bib jdrview-props-en-GB.bib \
			jdrview-en-GB.tex jdrview-main.tex version.tex \
			helpinterface.tex \
			../sharedimages/*.png ../sharedimages/*.tex
	lualatex jdrview-en-GB
	bib2gls jdrview-en-GB
	lualatex jdrview-en-GB
	bib2gls jdrview-en-GB

jdrutils-en.pdf	: jdrutils-en.glstex
	lualatex jdrutils-en
	lualatex jdrutils-en

jdrutils-en.glstex	: jdrutils.bib shared.bib flowframtk-props-en.bib \
		jdrutils-en.tex version.tex jdrversion.tex
	lualatex jdrutils-en
	bib2gls jdrutils-en

$(resourcesdir)/flowframtk-helpset.zip	: flowframtk-helpset/en/index.xml flowframtk-helpset/en-GB/index.xml 
	$(tjhziphelpset) --in-dir . --helpset flowframtk-helpset \
	--locales 'en,en-GB' \
	-L ../gpl-3.0-standalone.html en \
	--output $(resourcesdir)/flowframtk-helpset.zip

$(resourcesdir)/jdrview-helpset.zip	: jdrview-helpset/en/index.xml jdrview-helpset/en-GB/index.xml 
	$(tjhziphelpset) -i . --helpset jdrview-helpset \
	--locales 'en,en-GB' \
	-L ../gpl-3.0-standalone.html en \
	--output $(resourcesdir)/jdrview-helpset.zip

flowframtk-helpset/en-GB/index.xml	: flowframtk-en-GB.pdf  
	$(texjavahelpmk) flowframtk-en-GB.tex flowframtk-helpset/en-GB

flowframtk-helpset/en/index.xml	: flowframtk-en.pdf  
	$(texjavahelpmk) flowframtk-en.tex flowframtk-helpset/en

jdrview-helpset/en/index.xml	: jdrview-en.pdf  
	$(texjavahelpmk) jdrview-en.tex jdrview-helpset/en

jdrview-helpset/en-GB/index.xml	: jdrview-en-GB.pdf  
	$(texjavahelpmk) jdrview-en-GB.tex jdrview-helpset/en-GB

clean	:
	\rm -f version.tex
	\rm -f jdrversion.tex
	\rm -f flowframtk-en*.{aux,log,pdf,glg,glstex,out,toc,lof,lot,bbl,blg}
	\rm -f jdrview-en*.{aux,log,pdf,glg,glstex,out,toc,lof,lot}
	\rm -f jdrutils-en*.{aux,log,pdf,glg,glstex,out,toc,lof,lot}
	\rm -rf flowframtk-helpset
	\rm -rf jdrview-helpset
	\rm -f $(resourcesdir)/flowframtk-helpset.zip
	\rm -f $(resourcesdir)/jdrview-helpset.zip
	\rm -f flowframtk-props-en*.bib
	\rm -f jdrview-props-en*.bib
	\rm -f *.1
