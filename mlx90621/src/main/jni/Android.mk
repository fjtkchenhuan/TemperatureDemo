# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libmlx90621
LOCAL_SRC_FILES:= mlx90621.c MLX90621_API.c
LOCAL_LDLIBS += -llog
LOCAL_DEX_PREOPT := false

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
