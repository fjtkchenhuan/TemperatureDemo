# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libmlx90640
LOCAL_SRC_FILES:= mlx90640.c MLX90640_API.c
LOCAL_LDLIBS += -llog
LOCAL_DEX_PREOPT := false

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
