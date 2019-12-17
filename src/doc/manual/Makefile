all	: flowframtk-en-GB.pdf flowframtk-en-US.pdf

../../lib/resources/helpsets	:
				 mkdir -p ../../lib/resources/helpsets

../../lib/resources/helpsets/sharedimages	: ../../lib/resources/helpsets
				 cp -r ../sharedimages ../../lib/resources/helpsets

../../lib/resources/helpsets/flowframtk/en-GB	:
				 mkdir -p ../../lib/resources/helpsets/flowframtk/en-GB

../../lib/resources/helpsets/flowframtk/en-GB/images-en-GB	: ../../lib/resources/helpsets/flowframtk/en-GB
				 cp -r images-en-GB ../../lib/resources/helpsets/flowframtk/en-GB/

flowframtk-en-GB.pdf	: flowframtk-en-GB.tex flowframtk.cls
			arara flowframtk-en-GB

flowframtk-en-GB.tex	: createflowframtkdocs flowframtk-en.xml \
			../../lib/resources/helpsets/sharedimages \
			../../lib/resources/helpsets/flowframtk/en-GB/images-en-GB \
			../../lib/resources/dictionaries/flowframtk-en-GB.prop \
			../../java/flowframtk/FlowframTkInvoker.java
			createflowframtkdocs FlowframTk en GB

../../lib/resources/helpsets/flowframtk/en-US	:
				 mkdir -p ../../lib/resources/helpsets/flowframtk/en-US

../../lib/resources/helpsets/flowframtk/en-US/images-en-US	: ../../lib/resources/helpsets/flowframtk/en-US
				 cp -r images-en-US ../../lib/resources/helpsets/flowframtk/en-US/

flowframtk-en-US.pdf	: flowframtk-en-US.tex flowframtk.cls
			arara flowframtk-en-US

flowframtk-en-US.tex	: createflowframtkdocs flowframtk-en.xml \
			../../lib/resources/helpsets/sharedimages \
			../../lib/resources/helpsets/flowframtk/en-US/images-en-US \
			../../lib/resources/dictionaries/flowframtk-en-US.prop \
			../../java/flowframtk/FlowframTkInvoker.java
			createflowframtkdocs FlowframTk en US

clean	:
	rm -f flowframtk-en-GB.aux
	rm -f flowframtk-en-GB.glg
	rm -f flowframtk-en-GB.glstex
	rm -f flowframtk-en-GB.log
	rm -f flowframtk-en-GB.out
	rm -f flowframtk-en-GB.toc
	rm -f flowframtk-en-US.aux
	rm -f flowframtk-en-US.glg
	rm -f flowframtk-en-US.glstex
	rm -f flowframtk-en-US.log
	rm -f flowframtk-en-US.out
	rm -f flowframtk-en-US.toc

squeaky	:
	rm -f flowframtk-en-GB.tex
	rm -f flowframtk-en-GB.bib
	rm -f flowframtk-en-GB.pdf
	rm -f flowframtk-en-US.tex
	rm -f flowframtk-en-US.bib
	rm -f flowframtk-en-US.pdf

