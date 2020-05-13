#
# Makefile to build DS distribution.
#

#
# Variables
#
DIST_DIR=dist
AD_HOME=$(DIST_DIR)/uti/ds
GLOBAL_BIN_DIR=$(AD_HOME)/bin
DEP_DIR=$(AD_HOME)/deploy
DOC_DIR=$(AD_HOME)/doc
SRC_DIR=$(AD_HOME)/src
BASE_DIR=$(AD_HOME)/instance1
BIN_DIR=$(BASE_DIR)/bin
ETC_DIR=$(BASE_DIR)/etc
LIB_DIR=$(BASE_DIR)/lib
LOG_DIR=$(BASE_DIR)/log
XML_DIR=$(BASE_DIR)/xml

WARDIST_DIR=wardist
WEB_INF_DIR=$(WARDIST_DIR)/WEB-INF
WAR_LIB_DIR=$(WEB_INF_DIR)/lib
JS_DIR=$(WARDIST_DIR)/js
JSP_DIR=$(WARDIST_DIR)/jsp
WAR_ETC_DIR=$(WARDIST_DIR)/etc
WAR_XML_DIR=$(WARDIST_DIR)/xml

JWS_HOME=/opt/java/jwsdp-1.3
SAAJ_JAR=$(JWS_HOME)/saaj/lib/saaj-api.jar
SAAJIMPL_JAR=$(JWS_HOME)/saaj/lib/saaj-impl.jar
MAIL_JAR=$(JWS_HOME)/jwsdp-shared/lib/mail.jar
ACT_JAR=$(JWS_HOME)/jwsdp-shared/lib/activation.jar
XERCESIMPL_JAR=/opt/java/xerces/xercesImpl.jar
XERCESAPI_JAR=/opt/java/xerces/xml-apis.jar
UTI_SDK_JAR=$(HOME)/src/uti/sdk/dist/uti/sdk/lib/uti_sdk.jar
DS_JAR=java/uti_ds.jar
BIN_FILES=bin/*
GLOB_BIN_FILES=globalbin/*
DEP_FILES=deploy/*.sh
DOC_FILES=doc/*
ETC_FILES=etc/*.*
XML_FILES=xml/*.xml

WEB_XML=etc/web.xml
JS_FILES=$(HOME)/src/js/optiontrans/*.js
JSP_FILES=jsp/*.jsp
WAR_ETC_FILES=etc/dssoap_config.xml
WAR_XML_FILES=xml/soaptemplate.xml

WAR_FILE=uti_ds.war
TAR_FILE=uti_ds.tar.gz
SRC_TAR=src.tar.gz

#
# Targets
#
all: compile dir copy tar

compile:
	cd java; make

dir:
	mkdir $(DIST_DIR)
	mkdir -p $(AD_HOME)
	mkdir $(GLOBAL_BIN_DIR)
	mkdir $(BASE_DIR)
	mkdir $(BIN_DIR)
	mkdir $(DEP_DIR)
	mkdir $(DOC_DIR)
	mkdir $(ETC_DIR)
	mkdir $(LIB_DIR)
	mkdir $(LOG_DIR)
	mkdir $(XML_DIR)
	mkdir $(SRC_DIR)

copy:
	cp $(BIN_FILES) $(BIN_DIR)
	cp $(GLOB_BIN_FILES) $(GLOBAL_BIN_DIR)
	cp $(DEP_FILES) $(DEP_DIR)
	cp -r $(DOC_FILES) $(DOC_DIR)
	cp $(ETC_FILES) $(ETC_DIR)
	chmod u+w $(ETC_DIR)/*.*
	cp $(SAAJ_JAR) $(LIB_DIR)
	cp $(SAAJIMPL_JAR) $(LIB_DIR)
	cp $(XERCESIMPL_JAR) $(LIB_DIR)
	cp $(XERCESAPI_JAR) $(LIB_DIR)
	cp $(UTI_SDK_JAR) $(LIB_DIR)
	cp $(DS_JAR) $(LIB_DIR)
	cp $(XML_FILES) $(XML_DIR)
	echo 1 > $(LOG_DIR)/active.txt

tar:
	cd java; make clean
	tar cfvz $(SRC_TAR) Makefile bin doc etc globalbin java xml xsd
	mv $(SRC_TAR) $(SRC_DIR)
	cd $(DIST_DIR); tar cfvz $(TAR_FILE) *
	mv $(DIST_DIR)/$(TAR_FILE) .

war:
	cd java; make
	mkdir $(WARDIST_DIR)
	mkdir $(WEB_INF_DIR)
	mkdir $(WAR_LIB_DIR)
	mkdir $(JS_DIR)
	mkdir $(JSP_DIR)
	mkdir $(WAR_ETC_DIR)
	mkdir $(WAR_XML_DIR)
	cp $(WEB_XML) $(WEB_INF_DIR)
	cp $(JS_FILES) $(JS_DIR)
	cp $(JSP_FILES) $(JSP_DIR)
	cp $(WAR_ETC_FILES) $(WAR_ETC_DIR)
	cp $(WAR_XML_FILES) $(WAR_XML_DIR)
	cp $(DS_JAR) $(WAR_LIB_DIR)
	cp $(UTI_SDK_JAR) $(WAR_LIB_DIR)
	cp $(XERCESIMPL_JAR) $(WAR_LIB_DIR)
	cp $(XERCESAPI_JAR) $(WAR_LIB_DIR)
	cp $(SAAJ_JAR) $(WAR_LIB_DIR)
	cp $(SAAJIMPL_JAR) $(WAR_LIB_DIR)
	cp $(MAIL_JAR) $(WAR_LIB_DIR)
	cp $(ACT_JAR) $(WAR_LIB_DIR)
	cd $(WARDIST_DIR); jar cfv $(WAR_FILE) *
	mv $(WARDIST_DIR)/$(WAR_FILE) .

warclean:
	rm -rf $(WARDIST_DIR)
	rm $(WAR_FILE)

clean:
	rm -rf $(DIST_DIR)
	rm $(TAR_FILE)

