ifndef JDRSRC
JDRSRC:=$(shell dirname `pwd`)
endif
LIBDIR=$(JDRSRC)/lib
CLASSTREE=com/dickimawbooks

texjavaparserlib=$(shell kpsewhich --format=texmfscripts texjavaparserlib.jar)

ifeq ($(texjavaparserlib),)
  texjavaparserlib = $(HOME)/programming/java/texparser/src/lib/texjavaparserlib.jar
endif

texjavahelplib=$(shell kpsewhich --format=texmfscripts texjavahelplib.jar)

ifeq ($(texjavahelplib),)
  texjavahelplib = $(HOME)/programming/java/texjavahelp/src/lib/texjavahelplib.jar
endif

# Java 8 extended support until December 2030
target_version := --release 8 -Xlint:-options

all	: $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar \
	  $(LIBDIR)/flowframtk.jar $(LIBDIR)/jdrview.jar \
	  $(LIBDIR)/jdrinfo.jar $(LIBDIR)/jdrconverter.jar 

jdr	: $(LIBDIR)/jdr.jar

jdrresources	: $(LIBDIR)/jdrresources.jar

flowframtk	: $(LIBDIR)/flowframtk.jar

jdrview	: $(LIBDIR)/jdrview.jar

../lib/jdr.jar	: $(LIBDIR)/jdr.jar

../lib/jdrresources.jar	: $(LIBDIR)/jdrresources.jar

../lib/jdrview.jar	: $(LIBDIR)/jdrview.jar

../lib/flowframtk.jar	: $(LIBDIR)/flowframtk.jar

../lib/jdrconverter.jar	: $(LIBDIR)/jdrconverter.jar

../lib/jdrinfo.jar	: $(LIBDIR)/jdrinfo.jar

$(LIBDIR)/jdr.jar	: classes/$(CLASSTREE)/jdr/JDRObject.class
	cd classes ; \
	jar cf $(LIBDIR)/jdr.jar \
	$(CLASSTREE)/jdr/*.class \
	$(CLASSTREE)/jdr/io/*.class \
	$(CLASSTREE)/jdr/io/eps/*.class \
	$(CLASSTREE)/jdr/io/eps/operators/*.class \
	$(CLASSTREE)/jdr/io/svg/*.class \
	$(CLASSTREE)/jdr/exceptions/*.class \
	$(CLASSTREE)/jdr/marker/*.class

$(LIBDIR)/jdrresources.jar	: jdr classes/$(CLASSTREE)/jdrresources/JDRResources.class \
		jdrresources/icons/*.png jdrresources/icons/*/*.png \
		classes/$(CLASSTREE)/jdrresources/dictionaries/ \
		classes/$(CLASSTREE)/jdrresources/icons 
		cp ../dictionaries/jdrcommon-*.xml classes/$(CLASSTREE)/jdrresources/dictionaries/
		cd classes ; \
		jar cf $(LIBDIR)/jdrresources.jar \
		$(CLASSTREE)/jdrresources/*.class \
		$(CLASSTREE)/jdrresources/filter/*.class \
		$(CLASSTREE)/jdrresources/numfield/*.class \
		$(CLASSTREE)/jdrresources/dictionaries \
		$(CLASSTREE)/jdrresources/dictionaries/*.xml \
		$(CLASSTREE)/jdrresources/icons/*.png \
		$(CLASSTREE)/jdrresources/icons/*/*.png

classes/$(CLASSTREE)/jdrresources/dictionaries/	:
		mkdir -p classes/$(CLASSTREE)/jdrresources/dictionaries/

$(LIBDIR)/flowframtk.jar	: jdr jdrresources classes/$(CLASSTREE)/flowframtk/FlowframTk.class \
		  classes/$(CLASSTREE)/flowframtk/dictionaries/ \
		  flowframtk/Manifest.txt \
		  ../lib/flowframtk-splashimage.png 
		cp -u ../lib/flowframtk-splashimage.png classes/
		cp ../dictionaries/flowframtk-*.xml classes/$(CLASSTREE)/flowframtk/dictionaries/
		cd classes ; \
		jar cmf ../flowframtk/Manifest.txt \
	          $(LIBDIR)/flowframtk.jar \
		  flowframtk-splashimage.png \
		  $(CLASSTREE)/flowframtk/*.class \
		  $(CLASSTREE)/flowframtk/dialog/*.class \
		  $(CLASSTREE)/flowframtk/dictionaries \
		  $(CLASSTREE)/flowframtk/dictionaries/*.xml

$(LIBDIR)/jdrview.jar	: jdr jdrresources classes/$(CLASSTREE)/jdrview/JDRView.class \
	classes/$(CLASSTREE)/jdrview/dictionaries/ \
	jdrview/Manifest.txt \
	../lib/jdrview-splashimage.png 
	cp -u ../lib/jdrview-splashimage.png classes/
	cp ../dictionaries/jdrview-*.xml classes/$(CLASSTREE)/jdrview/dictionaries/
	cd classes ; \
	jar cmf ../jdrview/Manifest.txt \
	   $(LIBDIR)/jdrview.jar \
	   jdrview-splashimage.png \
	   $(CLASSTREE)/jdrview/*.class \
	   $(CLASSTREE)/jdrview/dictionaries \
	   $(CLASSTREE)/jdrview/dictionaries/*.xml

$(LIBDIR)/jdrconverter.jar	: classes/$(CLASSTREE)/jdrconverter/JDRConverter.class \
		  classes/$(CLASSTREE)/jdrconverter/dictionaries/ \
		  jdrconverter/Manifest.txt
		cp ../dictionaries/jdrconverter-*.xml classes/$(CLASSTREE)/jdrconverter/dictionaries/
		cd classes ; \
		jar cmf ../jdrconverter/Manifest.txt \
	          $(LIBDIR)/jdrconverter.jar \
		  $(CLASSTREE)/jdrconverter/*.class \
		  $(CLASSTREE)/jdrconverter/dictionaries \
		  $(CLASSTREE)/jdrconverter/dictionaries/*.xml

$(LIBDIR)/jdrinfo.jar	: classes/$(CLASSTREE)/jdrinfo/JdrInfo.class \
		  classes/$(CLASSTREE)/jdrinfo/dictionaries/ \
		  jdrinfo/Manifest.txt
		cp ../dictionaries/jdrinfo-*.xml classes/$(CLASSTREE)/jdrinfo/dictionaries/
		cd classes ; \
		jar cmf ../jdrinfo/Manifest.txt \
	          $(LIBDIR)/jdrinfo.jar \
		  $(CLASSTREE)/jdrinfo/*.class \
		  $(CLASSTREE)/jdrinfo/dictionaries \
		  $(CLASSTREE)/jdrinfo/dictionaries/*.xml

classes/$(CLASSTREE)/jdr/JDRObject.class : \
	classes \
	jdr/*.java \
	jdr/marker/*.java \
	jdr/exceptions/*.java \
	jdr/io/*.java \
	jdr/io/eps/*.java \
	jdr/io/eps/operators/*.java  \
	jdr/io/svg/*.java  
	cd jdr ; \
	javac $(target_version) -d ../classes -Xlint:unchecked -Xlint:deprecation \
	  -cp $(texjavahelplib):$(texjavaparserlib) \
	  *.java */*.java */*/*.java */*/*/*.java


classes/$(CLASSTREE)/jdrresources/JDRResources.class : \
	classes \
	jdrresources/*.java \
	jdrresources/*/*.java 
	cd jdrresources ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	  -cp $(LIBDIR)/jdr.jar:$(texjavahelplib) \
	  *.java */*.java

classes/$(CLASSTREE)/flowframtk/FlowframTk.class : \
	jdr jdrresources classes \
	flowframtk/*.java \
	flowframtk/*/*.java
	cd flowframtk ; \
	javac $(target_version) -d ../classes -Xlint:unchecked -Xlint:deprecation \
	  -cp $(texjavahelplib):$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java */*.java


classes/$(CLASSTREE)/jdrview/JDRView.class : \
	jdr jdrresources classes \
	jdrview/*.java
	cd jdrview ; \
	javac $(target_version) -d ../classes -Xlint:unchecked  -Xlint:deprecation \
	  -cp $(texjavahelplib):$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java

classes/$(CLASSTREE)/jdrconverter/JDRConverter.class : \
	jdr jdrresources classes \
	jdrconverter/*.java
	cd jdrconverter ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar:$(texjavahelplib) \
	*.java

classes/$(CLASSTREE)/jdrinfo/JdrInfo.class : \
	jdr jdrresources classes \
	jdrinfo/*.java
	cd jdrinfo ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar:$(texjavahelplib) \
	*.java

classes	:
	mkdir -p classes

classes/$(CLASSTREE)/jdrresources/icons	:
	mkdir -p classes/$(CLASSTREE)/jdrresources
	cd classes/$(CLASSTREE)/jdrresources; ln -f -s $(JDRSRC)/java/jdrresources/icons


classes/$(CLASSTREE)/jdrview	:
	mkdir -p classes/$(CLASSTREE)/jdrview

classes/$(CLASSTREE)/jdrview/dictionaries/	:
	mkdir -p classes/$(CLASSTREE)/jdrview/dictionaries

classes/$(CLASSTREE)/flowframtk	:
	mkdir -p classes/$(CLASSTREE)/flowframtk

classes/$(CLASSTREE)/flowframtk/dictionaries/	:
	mkdir -p classes/$(CLASSTREE)/flowframtk/dictionaries

classes/$(CLASSTREE)/jdrconverter/dictionaries/	:
	mkdir -p classes/$(CLASSTREE)/jdrconverter/dictionaries

classes/$(CLASSTREE)/jdrinfo/dictionaries/	:
	mkdir -p classes/$(CLASSTREE)/jdrinfo/dictionaries

clean	:
	\rm -f $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar $(LIBDIR)/jdrview.jar
	\rm -f $(LIBDIR)/flowframtk.jar $(LIBDIR)/jdrconverter.jar  $(LIBDIR)/jdrinfo.jar
	\rm -f -r classes
