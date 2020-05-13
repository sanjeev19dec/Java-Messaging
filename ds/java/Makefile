#
# Make file to compile DSMLv2 implementation classes.
#
# $Id: $
#

#
# Commands
#
JAVAC=javac
JAR=jar
JAVADOC=javadoc

#
# Variables
#
UTI_SDK_JAR=$(HOME)/src/uti/sdk/dist/uti/sdk/lib/uti_sdk.jar
TIBRV_JAR=$(TIB_RV_HOME)/lib/tibrvj.jar
CP=$(CLASSPATH):$(UTI_SDK_JAR):$(TIBRV_JAR)
JAR_FILE=uti_ds.jar
SRC_DIR=uti/nextgen
LPROV_DIR=$(SRC_DIR)/lprovision
DS_DIR=$(SRC_DIR)/ds
WEB_DIR=$(DS_DIR)/web


# Javadoc variables
#
JDOC_SRC_PATH=.
JDOC_WINDOW_TITLE="UTI DS API Documentation"
JDOC_DOC_TITLE="<h2>UTI DS API Documentation</h2>"
JDOC_PCKGS=uti.nextgen.lprovision uti.nextgen.ds
JDOC_DIR=../doc/api


#
# Targets
#
all: compile javadoc

compile:
	$(JAVAC) -classpath $(CP) $(LPROV_DIR)/*.java
	$(JAVAC) -classpath $(CP) $(DS_DIR)/*.java
	$(JAVAC) -classpath $(CP) $(WEB_DIR)/*.java
	$(JAR) cfv $(JAR_FILE) $(LPROV_DIR)/*.class
	$(JAR) ufv $(JAR_FILE) $(DS_DIR)/*.class
	$(JAR) ufv $(JAR_FILE) $(WEB_DIR)/*.class

javadoc:
	-mkdir -p $(JDOC_DIR)
	$(JAVADOC) -sourcepath $(JDOC_SRC_PATH) -classpath $(CP) \
        -windowtitle $(JDOC_WINDOW_TITLE) -doctitle $(JDOC_DOC_TITLE) \
        -d $(JDOC_DIR) $(JDOC_PCKGS)

clean:
	rm $(JAR_FILE)
	rm $(LPROV_DIR)/*.class
	rm $(DS_DIR)/*.class
	rm $(WEB_DIR)/*.class
	rm -r $(JDOC_DIR)
