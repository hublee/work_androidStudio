/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package master.flame.danmaku.danmaku.parser;

import android.text.TextUtils;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.FBDanmaku;
import master.flame.danmaku.danmaku.model.FTDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakuIterator;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.L2RDanmaku;
import master.flame.danmaku.danmaku.model.R2LDanmaku;
import master.flame.danmaku.danmaku.model.SpecialDanmaku;
import master.flame.danmaku.danmaku.model.SpecialDanmaku.LinePath;
import master.flame.danmaku.danmaku.model.android.DanmakuGlobalConfig;
import master.flame.danmaku.danmaku.model.android.Danmakus;

public class DanmakuFactory {

    public final static float OLD_BILI_PLAYER_WIDTH = 539;
    
    public final static float BILI_PLAYER_WIDTH = 682;
    
    public static int CURRENT_DISP_WIDTH = 0, CURRENT_DISP_HEIGHT = 0;
    
    private static float CURRENT_DISP_SIZE_FACTOR = 1.0f;

    public final static float OLD_BILI_PLAYER_HEIGHT = 385;
    
    public final static float BILI_PLAYER_HEIGHT = 438;

    public final static long COMMON_DANMAKU_DURATION = 3800; // B站原始分辨率下弹幕存活时间
    
    public static final int DANMAKU_MEDIUM_TEXTSIZE = 25;

    public final static long MIN_DANMAKU_DURATION = 4000;
    
    public static long REAL_DANMAKU_DURATION = COMMON_DANMAKU_DURATION;

    public static long MAX_DANMAKU_DURATION = MIN_DANMAKU_DURATION;

    public final static long MAX_DANMAKU_DURATION_HIGH_DENSITY = 9000;
    
    public static Duration MAX_Duration_Scroll_Danmaku;
    
    public static Duration MAX_Duration_Fix_Danmaku;
    
    public static Duration MAX_Duration_Special_Danmaku;
    
    public static IDanmakus sSpecialDanmakus = new Danmakus();

    public static IDisplayer sLastDisp;
    
    public static void resetDurationsData() {
        sLastDisp = null;
        CURRENT_DISP_WIDTH = CURRENT_DISP_HEIGHT = 0;
        sSpecialDanmakus.clear();
        MAX_Duration_Scroll_Danmaku = null;
        MAX_Duration_Fix_Danmaku = null;
        MAX_Duration_Special_Danmaku = null;
        MAX_DANMAKU_DURATION = MIN_DANMAKU_DURATION;
    }
    
    public static void notifyDispSizeChanged(IDisplayer disp) {
        if (disp != null)
            sLastDisp = disp;
        createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, disp);
    }
    
    public static BaseDanmaku createDanmaku(int type) {
        return createDanmaku(type, sLastDisp);
    }

    public static BaseDanmaku createDanmaku(int type, IDisplayer disp) {
        if (disp == null)
            return null;
        sLastDisp = disp;
        return createDanmaku(type, disp.getWidth(), disp.getHeight(), CURRENT_DISP_SIZE_FACTOR);
    }
    
    public static BaseDanmaku createDanmaku(int type, IDisplayer disp, float viewportScale) {
        if (disp == null)
            return null;
        sLastDisp = disp;
        return createDanmaku(type, disp.getWidth(), disp.getHeight(), viewportScale);
    } 
    
    /**
     * 创建弹幕数据请尽量使用此方法,参考BiliDanmakuParser或AcfunDanmakuParser
     * @param type 弹幕类型
     * @param viewportWidth danmakuview宽度,会影响滚动弹幕的存活时间(duration)
     * @param viewportHeight danmakuview高度
     * @param viewportScale 缩放比例,会影响滚动弹幕的存活时间(duration)
     * @return
     */
    public static BaseDanmaku createDanmaku(int type, int viewportWidth, int viewportHeight,
            float viewportScale) {
        return createDanmaku(type, (float) viewportWidth, (float) viewportHeight, viewportScale);
    }
    
    /**
     * 创建弹幕数据请尽量使用此方法,参考BiliDanmakuParser或AcfunDanmakuParser
     * @param type 弹幕类型
     * @param viewportWidth danmakuview宽度,会影响滚动弹幕的存活时间(duration)
     * @param viewportHeight danmakuview高度
     * @param viewportSizeFactor 会影响滚动弹幕的速度/存活时间(duration)
     * @return
     */
    public static BaseDanmaku createDanmaku(int type, float viewportWidth, float viewportHeight,
            float viewportSizeFactor) {
        boolean sizeChanged = updateViewportState(viewportWidth, viewportHeight, viewportSizeFactor);
        if (MAX_Duration_Scroll_Danmaku == null) {
            MAX_Duration_Scroll_Danmaku = new Duration(REAL_DANMAKU_DURATION);
            MAX_Duration_Scroll_Danmaku.setFactor(DanmakuGlobalConfig.DEFAULT.scrollSpeedFactor);
        } else if (sizeChanged) {
            MAX_Duration_Scroll_Danmaku.setValue(REAL_DANMAKU_DURATION);
        }
        
        if (MAX_Duration_Fix_Danmaku == null) {
            MAX_Duration_Fix_Danmaku = new Duration(COMMON_DANMAKU_DURATION);
        }

        if (sizeChanged && viewportWidth > 0) {
            updateMaxDanmakuDuration();
            float scaleX = 1f;
            float scaleY = 1f;
            if (CURRENT_DISP_WIDTH > 0 && CURRENT_DISP_HEIGHT > 0) {
                scaleX = viewportWidth / (float) CURRENT_DISP_WIDTH;
                scaleY = viewportHeight / (float) CURRENT_DISP_HEIGHT;
            }
            if (viewportHeight > 0) {
                updateSpecialDanmakusDate(scaleX, scaleY);
            }
        }

        BaseDanmaku instance = null;
        switch (type) {
            case 1: // 从右往左滚动
                instance = new R2LDanmaku(MAX_Duration_Scroll_Danmaku);
                break;
            case 4: // 底端固定
                instance = new FBDanmaku(MAX_Duration_Fix_Danmaku);
                break;
            case 5: // 顶端固定
                instance = new FTDanmaku(MAX_Duration_Fix_Danmaku);
                break;
            case 6: // 从左往右滚动
                instance = new L2RDanmaku(MAX_Duration_Scroll_Danmaku);
                break;
            case 7: // 特殊弹幕
                instance = new SpecialDanmaku();
                sSpecialDanmakus.addItem(instance);
                break;
        }
        return instance;
    }
    
    public static boolean updateViewportState(float viewportWidth, float viewportHeight,
            float viewportSizeFactor) {
        boolean sizeChanged = false;
        if (CURRENT_DISP_WIDTH != (int) viewportWidth
                || CURRENT_DISP_HEIGHT != (int) viewportHeight
                || CURRENT_DISP_SIZE_FACTOR != viewportSizeFactor) {
            sizeChanged = true;
            REAL_DANMAKU_DURATION = (long) (COMMON_DANMAKU_DURATION * (viewportSizeFactor
                    * viewportWidth / BILI_PLAYER_WIDTH));
            REAL_DANMAKU_DURATION = Math.min(MAX_DANMAKU_DURATION_HIGH_DENSITY,
                    REAL_DANMAKU_DURATION);
            REAL_DANMAKU_DURATION = Math.max(MIN_DANMAKU_DURATION, REAL_DANMAKU_DURATION);
            
            CURRENT_DISP_WIDTH = (int) viewportWidth;
            CURRENT_DISP_HEIGHT = (int) viewportHeight;
            CURRENT_DISP_SIZE_FACTOR = viewportSizeFactor;
        }
        return sizeChanged;
    }

    private static void updateSpecialDanmakusDate(float scaleX, float scaleY) {
        IDanmakus list = sSpecialDanmakus;
        IDanmakuIterator it = list.iterator();
        while (it.hasNext()) {
            SpecialDanmaku speicalDanmaku = (SpecialDanmaku) it.next();
            fillTranslationData(speicalDanmaku, speicalDanmaku.beginX, speicalDanmaku.beginY,
                    speicalDanmaku.endX, speicalDanmaku.endY, speicalDanmaku.translationDuration,
                    speicalDanmaku.translationStartDelay, scaleX, scaleY);
            LinePath[] linePaths = speicalDanmaku.linePaths;
            if (linePaths != null && linePaths.length > 0) {
                int length = linePaths.length;
                float[][] points = new float[length + 1][2];
                for (int j = 0; j < length; j++) {
                    points[j] = linePaths[j].getBeginPoint();
                    points[j + 1] = linePaths[j].getEndPoint();
                }
                fillLinePathData(speicalDanmaku, points, scaleX, scaleY);
            }
        }
    }

    public static void updateMaxDanmakuDuration() {
        long maxScrollDuration = (MAX_Duration_Scroll_Danmaku == null ? 0: MAX_Duration_Scroll_Danmaku.value), 
              maxFixDuration = (MAX_Duration_Fix_Danmaku == null ? 0 : MAX_Duration_Fix_Danmaku.value), 
              maxSpecialDuration = (MAX_Duration_Special_Danmaku == null ? 0: MAX_Duration_Special_Danmaku.value);

        MAX_DANMAKU_DURATION = Math.max(maxScrollDuration, maxFixDuration);
        MAX_DANMAKU_DURATION = Math.max(MAX_DANMAKU_DURATION, maxSpecialDuration);

        MAX_DANMAKU_DURATION = Math.max(COMMON_DANMAKU_DURATION, MAX_DANMAKU_DURATION);
        MAX_DANMAKU_DURATION = Math.max(REAL_DANMAKU_DURATION, MAX_DANMAKU_DURATION);
    }
    
    public static void updateDurationFactor(float f) {
        if (MAX_Duration_Scroll_Danmaku == null || MAX_Duration_Fix_Danmaku == null)
            return;
        MAX_Duration_Scroll_Danmaku.setFactor(f);
        updateMaxDanmakuDuration();
    }
    
    public static void fillText(BaseDanmaku danmaku, String text) {
        danmaku.text = text;
        if (TextUtils.isEmpty(text) || !text.contains(BaseDanmaku.DANMAKU_BR_CHAR)) {
            return;
        }
        
        String[] lines = String.valueOf(danmaku.text).split(BaseDanmaku.DANMAKU_BR_CHAR, -1);
        if (lines.length > 1) {
            danmaku.lines = lines;
        }
    }

    /**
     * Initial translation data of the special danmaku
     * 
     * @param item
     * @param beginX
     * @param beginX
     * @param beginY
     * @param endX
     * @param endY
     * @param translationDuration
     * @param translationStartDelay
     */
    public static void fillTranslationData(BaseDanmaku item, float beginX, float beginY,
            float endX, float endY, long translationDuration, long translationStartDelay,
            float scaleX, float scaleY) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL)
            return;
        ((SpecialDanmaku) item).setTranslationData(beginX * scaleX, beginY * scaleY, endX * scaleX,
                endY * scaleY, translationDuration, translationStartDelay);
        updateSpecicalDanmakuDuration(item);
    }
    
    public static void fillLinePathData(BaseDanmaku item, float[][] points, float scaleX,
            float scaleY) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL || points.length == 0
                || points[0].length != 2)
            return;
        for (int i = 0; i < points.length; i++) {
            points[i][0] *= scaleX;
            points[i][1] *= scaleY;
        }
        ((SpecialDanmaku) item).setLinePathData(points);
    }

    /**
     * Initial alpha data of the special danmaku
     * 
     * @param item
     * @param beginAlpha
     * @param endAlpha
     * @param alphaDuraion
     */
    public static void fillAlphaData(BaseDanmaku item, int beginAlpha, int endAlpha,
            long alphaDuraion) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL)
            return;
        ((SpecialDanmaku) item).setAlphaData(beginAlpha, endAlpha, alphaDuraion);
        updateSpecicalDanmakuDuration(item);
    }
    
    private static void updateSpecicalDanmakuDuration(BaseDanmaku item) {
        if (MAX_Duration_Special_Danmaku == null || (item.duration != null && item.duration.value > MAX_Duration_Special_Danmaku.value)) {
            MAX_Duration_Special_Danmaku = item.duration;
            updateMaxDanmakuDuration();
        }
    }
    
}
