#
# Makefile to compile UTI sServices sources 
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
RV_JAR=$(TIB_RV_HOME)/lib/tibrvj.jar
UTI_SDK_JAR=$(HOME)/src/uti/sdk/dist/uti/sdk/lib/uti_sdk.jar
CP=$(CLASSPATH):$(RV_JAR):$(UTI_SDK_JAR)

API_SRC=uti/nextgen/eservices
API_UTIL_SRC=$(API_SRC)/util

JAR_FILE=uti_eservices.jar

#
# Javadoc variables
#
JDOC_SRC_PATH=.
JDOC_WINDOW_TITLE="UTI eServices API Documentation"
JDOC_DOC_TITLE="<h2>UTI eServices API Documentation</h2>"
JDOC_PCKGS=uti.nextgen.eservices uti.nextgen.eservices.util
JDOC_DIR=../doc/api


#
# Targets
#
all: api jar javadoc

api:
	$(JAVAC) -classpath $(CP) $(API_UTIL_SRC)/*.java
	$(JAVAC) -classpath $(CP) $(API_SRC)/*.java

jar:
	$(JAR) cfv $(JAR_FILE) $(API_SRC)/*.class
	$(JAR) ufv $(JAR_FILE) $(API_UTIL_SRC)/*.class

javadoc:
	-mkdir $(JDOC_DIR)
	$(JAVADOC) -sourcepath $(JDOC_SRC_PATH) -classpath $(CP) \
        -windowtitle $(JDOC_WINDOW_TITLE) -doctitle $(JDOC_DOC_TITLE) \
        -d $(JDOC_DIR) $(JDOC_PCKGS)

clean:
	-rm $(API_SRC)/*.class
	-rm $(API_UTIL_SRC)/*.class
	-rm $(JAR_FILE)
	-rm -r $(JDOC_DIR)
