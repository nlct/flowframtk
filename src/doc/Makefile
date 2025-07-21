targetdir=../lib/resources/helpsets
dictdir=../lib/resources/dictionaries
baseclass=../java/jdrresources/JDRResources.java
arara=arara --verbose
#texjavahelpmk=texjavahelpmk --debug-mode all --log texjavahelpmk.log
texjavahelpmk=texjavahelpmk
xml2bib=xml2bib --copy-overwrite-xml

all	: en-GB en flowframtk.1

flowframtk.1	: flowframtk.pod
		pod2man -c flowframtk flowframtk.pod flowframtk.1

flowframtk-props-en.bib	: $(dictdir)/flowframtk-en.xml \
			$(dictdir)/jdrcommon-en.xml \
			$(dictdir)/texjavahelplib-en.xml
	$(xml2bib)  $(dictdir)/texjavahelplib-en.xml \
	$(dictdir)/flowframtk-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	-o flowframtk-props-en.bib

flowframtk-props-en-GB.bib	: $(dictdir)/flowframtk-en.xml \
				$(dictdir)/flowframtk-en-GB.xml  \
				$(dictdir)/jdrcommon-en.xml \
				$(dictdir)/jdrcommon-en-GB.xml \
				$(dictdir)/texjavahelplib-en.xml
	$(xml2bib)  $(dictdir)/texjavahelplib-en.xml \
	$(dictdir)/flowframtk-en.xml \
	$(dictdir)/flowframtk-en-GB.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrcommon-en-GB.xml \
	-o flowframtk-props-en-GB.bib

jdrview-props-en.bib	: $(dictdir)/jdrview-en.xml $(dictdir)/jdrcommon-en.xml \
			$(dictdir)/texjavahelplib-en.xml
	$(xml2bib) $(dictdir)/texjavahelplib-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrview-en.xml \
	-o jdrview-props-en.bib

jdrview-props-en-GB.bib	: $(dictdir)/jdrview-en.xml $(dictdir)/jdrcommon-en.xml \
			$(dictdir)/jdrcommon-en-GB.xml $(dictdir)/texjavahelplib-en.xml
	$(xml2bib) $(dictdir)/texjavahelplib-en.xml \
	$(dictdir)/jdrcommon-en.xml \
	$(dictdir)/jdrcommon-en-GB.xml \
	$(dictdir)/jdrview-en.xml \
	-o jdrview-props-en-GB.bib


version.tex	: $(baseclass)
	@echo "\\date{Version " > version.tex
	@grep 'String APP_VERSION = ' $(baseclass) | sed "s/public\sstatic\sfinal\sString\sAPP_VERSION\s=//" | tr -d "\"\; " >> version.tex
	@grep 'String APP_DATE = ' $(baseclass) | sed "s/public\sstatic\sfinal\sString\sAPP_DATE\s=//" | tr -d "\"\; " >> version.tex
	@echo "}" >> version.tex

flowframtk-en-GB.pdf	: flowframtk.bib flowframtk-props-en-GB.bib \
			 flowframtk-en-GB.tex flowframtk-main.tex version.tex \
			 helpinterface.tex images-en-GB/*.png \
			sharedimages/*.png sharedimages/*.tex
	$(arara) flowframtk-en-GB

flowframtk-en.pdf	: flowframtk.bib flowframtk-props-en.bib \
			  flowframtk-en.tex flowframtk-main.tex version.tex \
			 helpinterface.tex images-en/*.png \
			sharedimages/*.png sharedimages/*.tex
	$(arara) flowframtk-en

jdrview-en.pdf	: jdrview.bib jdrview-props-en.bib \
		jdrview-en.tex jdrview-main.tex version.tex \
		helpinterface.tex \
		sharedimages/*.png sharedimages/*.tex
	$(arara) jdrview-en

jdrview-en-GB.pdf	: jdrview.bib jdrview-props-en-GB.bib \
			jdrview-en-GB.tex jdrview-main.tex version.tex \
			helpinterface.tex \
			sharedimages/*.png sharedimages/*.tex
	$(arara) jdrview-en-GB

en-GB	: $(targetdir)/flowframtk/en-GB/index.xml \
	$(targetdir)/jdrview/en-GB/index.xml

en	: $(targetdir)/flowframtk/en/index.xml \
	$(targetdir)/jdrview/en/index.xml

$(targetdir)/flowframtk/en-GB/index.xml	: $(targetdir)/flowframtk/en-GB \
	  flowframtk-en-GB.pdf  
	$(texjavahelpmk) flowframtk-en-GB.tex $(targetdir)/flowframtk/en-GB

$(targetdir)/flowframtk/en/index.xml	: $(targetdir)/flowframtk/en \
	  flowframtk-en.pdf  
	$(texjavahelpmk) flowframtk-en.tex $(targetdir)/flowframtk/en

$(targetdir)/jdrview/en/index.xml	: $(targetdir)/jdrview/en \
	  jdrview-en.pdf  
	$(texjavahelpmk) jdrview-en.tex $(targetdir)/jdrview/en

$(targetdir)/jdrview/en-GB/index.xml	: $(targetdir)/jdrview/en-GB \
	  jdrview-en-GB.pdf  
	$(texjavahelpmk) jdrview-en.tex $(targetdir)/jdrview/en-GB


$(targetdir)/flowframtk/en	:
	mkdir -p $(targetdir)/flowframtk/en

$(targetdir)/flowframtk/en-GB	:
	mkdir -p $(targetdir)/flowframtk/en-GB

$(targetdir)/jdrview/en	:
	mkdir -p $(targetdir)/jdrview/en

$(targetdir)/jdrview/en-GB	:
	mkdir -p $(targetdir)/jdrview/en-GB

clean	:
	\rm -f flowframtk-en*.{aux,log,pdf,glg,glstex,out,toc,lof}
	\rm -f jdrview-en*.{aux,log,pdf,glg,glstex,out,toc,lof}
	\rm -rf $(targetdir)/flowframtk
	\rm -rf $(targetdir)/jdrview
	\rm -rf flowframtk-props-en*.bib
	\rm -rf jdrview-props-en*.bib
