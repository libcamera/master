LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := nativefuntion
LOCAL_SRC_FILES := nativefuntion.c 
LOCAL_LDLIBS    := -lm -llog -ldl -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
