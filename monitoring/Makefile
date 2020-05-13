#
# Makefile to build a monitoring distribution.
#

#
# Variables
#
DIST_DIR=dist
BASE_DIR=$(DIST_DIR)/uti/monitoring
ETC_DIR=$(BASE_DIR)/etc
RB_DIR=$(BASE_DIR)/rulebase
DEBUG_RB_DIR=$(RB_DIR)/debug

ETC_FILES=etc/*.*

TAR_FILE=uti_monitoring.tar.gz


#
# Rulebase deployment configurations
#

#
# PortalBuiler
#
PB_OWNLY_RB=rulebase/PB_BasicMonitor.hrb
PB_JNDI_OWNLY_RB=rulebase/PB_JNDIServerMonitor.hrb
PB_DB_OWNLY_RB=rulebase/PB_DatabaseServerMonitor.hrb
PB_WITH_JNDI_RB=$(PB_OWNLY_RB) $(PB_JNDI_OWNLY_RB)
PB_WITH_DB_RB=$(PB_OWNLY_RB) $(PB_DB_OWNLY_RB)
PB_WITH_JNDI_DB_RB=$(PB_WITH_JNDI_RB) $(PB_DB_OWNLY_RB)
PB_DEBUG=rulebase/PB_InternalMonitor.hrb

#
# Operating Systems
#
SOL_RB=rulebase/Solaris2xMonitor.hrb


#
# Custom Adapters
#
DS_RB=rulebase/DSAdapterMonitor.hrb
ESERVICES_RB=rulebase/EServicesAdapterMonitor.hrb
ALL_CUSTOM_AD=$(DS_RB) $(ESERVICES_RB)

#
# Targets
#
all: 

dirs:
	-mkdir $(DIST_DIR)
	-mkdir -p $(BASE_DIR)
	-mkdir $(ETC_DIR)
	-mkdir $(RB_DIR)
	-mkdir $(DEBUG_RB_DIR)

etccopy:
	cp $(ETC_FILES) $(ETC_DIR)

pb_only: dirs etccopy
	cp $(PB_OWNLY_RB) $(RB_DIR)

pb_jndi_only: dirs etccopy
	cp $(PB_JNDI_OWNLY_RB) $(RB_DIR)

pb_db_only: dirs etccopy
	cp $(PB_DB_ONWLY_RB) $(RB_DIR)

pb_jndi: dirs etccopy
	cp $(PB_WITH_JNDI_RB) $(RB_DIR)

pb_db: dirs etccopy
	cp $(PB_WITH_DB_RB) $(RB_DIR)

pb_jndi_db: dirs etccopy
	cp $(PB_WITH_JNDI_DB_RB) $(RB_DIR)

pb_debug: dirs etccopy
	cp $(PB_DEBUG) $(DEBUG_RB_DIR)

ds_ad: dirs etccopy
	cp $(DS_RB) $(RB_DIR)

eservices_ad: dirs etccopy
	cp $(ESERVICES_RB) $(RB_DIR)

all_custom_ad: dirs etccopy
	cp $(ALL_CUSTOM_AD) $(RB_DIR)

sol: dirs etccopy
	cp $(SOL_RB) $(RB_DIR)

tar:
	cd $(DIST_DIR); tar cfvz $(TAR_FILE) *
	mv $(DIST_DIR)/$(TAR_FILE) .

clean:
	rm -r $(DIST_DIR)
	rm $(TAR_FILE)
