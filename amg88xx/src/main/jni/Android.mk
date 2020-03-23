# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libamg88xx
LOCAL_SRC_FILES:= amg88xx.c AMG88XX_API.c
LOCAL_LDLIBS += -llog
LOCAL_DEX_PREOPT := false

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
