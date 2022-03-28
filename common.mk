#
# Copyright (C) 2022 The PixelDust Project
#
# Licensed under the Apache License, Version 2.0 (the License);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

PRODUCT_PACKAGES += \
    PixeldustSystemUI \
    PixeldustSettings

# Preopt PixeldustSystemUI
PRODUCT_DEXPREOPT_SPEED_APPS += \
    PixeldustSystemUI

# Sysconfig
PRODUCT_COPY_FILES += \
    vendor/pixelstyle/prebuilt/common/etc/sysconfig/game_overlay.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/sysconfig/game_overlay.xml

# Copy quick tap enable sysconfig
ifneq ($(DISABLE_COLUMBUS), true)
PRODUCT_COPY_FILES += \
    vendor/pixelstyle/prebuilt/common/etc/sysconfig/quick_tap.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/sysconfig/quick_tap.xml
endif
