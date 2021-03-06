package com.why.project.baiduspeech.recognization;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by fujiayi on 2017/6/16.
 */

public class MessageStatusRecogListener extends StatusRecogListener {
    private Handler handler;

    private long speechEndTime;

    private boolean needTime = true;

    private static final String TAG = "MesStatusRecogListener";

    public MessageStatusRecogListener(Handler handler) {
        this.handler = handler;
    }


    @Override
    public void onAsrReady() {
        super.onAsrReady();
        sendStatusMessage("引擎就绪，可以开始说话。");
    }

    @Override
    public void onAsrBegin() {
        super.onAsrBegin();
        sendStatusMessage("检测到用户说话");
    }

    @Override
    public void onAsrEnd() {
        super.onAsrEnd();
        speechEndTime = System.currentTimeMillis();
        sendMessage("检测到用户说话结束");
    }

    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {
        sendStatusMessage("临时识别结果，结果是“" + results[0] + "”；原始json：" + recogResult.getOrigalJson());
        super.onAsrPartialResult(results, recogResult);
    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        //String message = "识别结束，结果是”" + results[0] + "”";//why 实际中可以去掉，不需要
        String message = recogResult.getOrigalJson();//{"results_recognition":["什么什么"],"origin_result":{"corpus_no":6522034498058113957,"err_no":0,"result":{"word":["什么什么"]},"sn":"bfa8b286-ab0e-4f86-9209-1d36d38b1224","voice_energy":16191.7705078125},"error":0,"best_result":"什么什么","result_type":"final_result"}
        sendStatusMessage(message + "“；原始json：" + recogResult.getOrigalJson());
        if (speechEndTime > 0) {
            long diffTime = System.currentTimeMillis() - speechEndTime;
            //message += "；说话结束到识别结束耗时【" + diffTime + "ms】";// why 实际中可以去掉，不需要

        }
        speechEndTime = 0;
        sendMessage(message, status, true);
    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode, String errorMessage, String descMessage,
                                 RecogResult recogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult);
        //String message = "识别错误, 错误码：" + errorCode + " ," + subErrorCode + " ; " + descMessage;// why 实际中可以去掉，不需要
        String message = recogResult.getOrigalJson();//{"origin_result":{"sn":"","error":7,"desc":"No recognition result match","sub_error":7001},"error":7,"desc":"No recognition result match","sub_error":7001}
        sendStatusMessage(message + "；错误消息:" + errorMessage + "；描述信息：" + descMessage);
        if (speechEndTime > 0) {
            long diffTime = System.currentTimeMillis() - speechEndTime;
            //message += "。说话结束到识别结束耗时【" + diffTime + "ms】";// why实际中可以去掉，不需要
        }
        speechEndTime = 0;
        sendMessage(message, status, true);
        speechEndTime = 0;
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {
        super.onAsrOnlineNluResult(nluResult);
        if (!nluResult.isEmpty()) {
            sendStatusMessage("原始语义识别结果json：" + nluResult);
        }
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {
        super.onAsrFinish(recogResult);
        sendStatusMessage("识别一段话结束。如果是长语音的情况会继续识别下段话。");

    }

    /**
     * 长语音识别结束
     */
    @Override
    public void onAsrLongFinish() {
        super.onAsrLongFinish();
        sendStatusMessage("长语音识别结束。");
    }


    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineLoaded() {
        sendStatusMessage("【重要】asr.loaded：离线资源加载成功。没有此回调可能离线语法功能不能使用。");
    }

    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineUnLoaded() {
        sendStatusMessage(" 离线资源卸载成功。");
    }

    @Override
    public void onAsrExit() {
        super.onAsrExit();
        sendStatusMessage("识别引擎结束并空闲中");
    }

    private void sendStatusMessage(String message) {
        sendMessage(message, status);
    }

    private void sendMessage(String message) {
        sendMessage(message, WHAT_MESSAGE_STATUS);
    }

    private void sendMessage(String message, int what) {
        sendMessage(message, what, false);
    }


    private void sendMessage(String message, int what, boolean highlight) {


        if (needTime && what != STATUS_FINISHED) {
            message += "  ;time=" + System.currentTimeMillis();
        }
        if (handler == null){
            Log.i(TAG, message );
            return;
        }
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = status;
        if (highlight) {
            msg.arg2 = 1;
        }
        msg.obj = message + "\n";
        handler.sendMessage(msg);
    }
}
