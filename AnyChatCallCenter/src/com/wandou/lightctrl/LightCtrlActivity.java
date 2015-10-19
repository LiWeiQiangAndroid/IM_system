package com.wandou.lightctrl;


import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.bairuitech.callcenter.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.topeet.serialtest.senddata;

public class LightCtrlActivity extends Activity implements OnClickListener,
        OnSeekBarChangeListener {

    protected static final String TAG = "IatDemo";
    public Button mRecognize;
    public EditText mResultText;
    private SeekBar mSeekBar;
    private int seekBarProgress;
    private Toast mToast;
    public int times;
    private int ID;
    private LightCtrl mLight;
    public senddata data;

    
    //唤醒识别对象
//    private StringBuffer param;
//    private VoiceWakeuper mIvw;
    
//语音合成对象
    private SpeechSynthesizer mTts;
    // 语义理解对象
    private TextUnderstander mTextUnderstander;
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private final HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 引擎类型
    private final String mEngineType = SpeechConstant.TYPE_CLOUD;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences valueSharedPreferences;
    private Editor valueEditor ;

    int ret = 0; // 函数调用返回值

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        /**
         * 识别回调错误.
         */
        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

    };

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写监听器。
     */
    private final RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e(TAG, results.getResultString());
            printResult(results);
            if (isLast) {
                // TODO 最后的结果
            }
        }

        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onVolumeChanged(int arg0) {
            // TODO Auto-generated method stub
            
        }
    };


    
    private SynthesizerListener mSynListener = new SynthesizerListener(){ 
        //会话结束回调接口，没有错误时，error为null 
        public void onCompleted(SpeechError error) {
            
        } 
        //缓冲进度回调 
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。 
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            
        } 
        //开始播放 
        public void onSpeakBegin() {
            
        } 
        //暂停播放 
        public void onSpeakPaused() {
            
        } 
        //播放进度回调 
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置. 
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            
        } 
        //恢复播放回调接口 
        public void onSpeakResumed() {
            
        } 
        //会话事件回调接口 
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            
        }
        };
  
    
    private final TextUnderstanderListener searchListener = new TextUnderstanderListener() {
        // 语义错误回调
        @Override
        public void onError(SpeechError error) {
            String text = "理解语义失败";
            showTip(error.getPlainDescription(true));
            mResultText.setText(text);
            mResultText.setSelection(mResultText.length());
        }

        // 语义结果回调
        @Override
        public void onResult(UnderstanderResult result) {
            
            String resulttext = JsonParser.parseIatResult(result.getResultString());

            String text = null;
            // 读取json结果中的text字段
            try {
                JSONObject resultJson = new JSONObject(result.getResultString());
                text = resultJson.optString("answer");
                JSONObject resultJson2 = new JSONObject(text);
                text = resultJson2.optString("text");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (text=="")
                {
                text="抱歉我没听清楚";
                }
            setEditText(text);
        }
    };
//
//    private void changeSeekBarLevel() {
//        // TODO Auto-generated method stub
//        if (seekBarProgress >= 7) {
//            seekBarProgress = 0;
//        }
//        mSeekBar.setProgress(seekBarProgress + 1);
//    }
//
//    private void closeLight() {
//        // TODO Auto-generated method stub
//
//    }

    private void initLayout() {
        // TODO Auto-generated method stub
        mRecognize = (Button) findViewById(R.id.button);
        mResultText = (EditText) findViewById(R.id.editText);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
//        param =new StringBuffer();
//        String resPath = ResourceUtil.generateResourcePath(MainActivity.this, 
//                RESOURCE_TYPE.assets, "ivw/55aef8db.jet");
//        param.append(ResourceUtil.IVW_RES_PATH+"="+resPath); 
//        param.append(","+ResourceUtil.ENGINE_START+"="+SpeechConstant.ENG_IVW); 
//        SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START,param.toString());
        mRecognize.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    
    
    
    // 开始听写
    // 如何判断一次听写结束：OnResult isLast=true 或者 onError
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        mResultText.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();

        mTextUnderstander.setParameter(SpeechConstant.DOMAIN, "iat");
        times = 0;
        boolean isShowDialog = mSharedPreferences.getBoolean(
                getString(R.string.pref_key_iat_show), true);
        if (isShowDialog) {
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            showTip(getString(R.string.text_begin));
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip(getString(R.string.text_begin));
            }
        }

    }
//
//    @Override
//    public void onStop(){
//        mIvw.startListening(mWakeuperListener); //听写监听器 
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lightctrl_activity);
        initLayout();
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=55aef8db");

        mLight = new LightCtrl();
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(LightCtrlActivity.this,
                mInitListener);
        mTextUnderstander = TextUnderstander.createTextUnderstander(
                LightCtrlActivity.this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(LightCtrlActivity.this, mInitListener);
        mTts= SpeechSynthesizer.createSynthesizer(LightCtrlActivity.this, null);
        
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");//设置发音人 
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速 
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        
//        mIvw = VoiceWakeuper.createWakeuper(MainActivity.this, null);
//        String curThresh="科大讯飞";
//        mIvw.setParameter(SpeechConstant.IVW_THRESHOLD,"0:"+curThresh); //设置当前业务类型为唤醒 
//        mIvw.setParameter(SpeechConstant.IVW_SST,"wakeup"); //设置唤醒一直保持，直到调用stopListening，传入0则完成一次唤醒后，会话立即结束（默认0） 
//        mIvw.setParameter(SpeechConstant.KEEP_ALIVE,"1");
        
        valueSharedPreferences = this.getSharedPreferences("valueSaveXML", MODE_PRIVATE);
        valueEditor = valueSharedPreferences.edit();
        mLight.SetLight(valueSharedPreferences.getInt("lightValue",0));
        mSeekBar.setProgress(mLight.GetLight());
        mSharedPreferences = getSharedPreferences(this.getPackageName(),
                MODE_PRIVATE);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

    }

    // if ((resultText.trim()).equals("增大")) {
    // changeSeekBarLevel();
    // }
    // if ((resultText.trim()).equals("关闭") || (resultText.trim()).equals("关灯"))
    // {
    // closeLight();
    // }

    // }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // TODO Auto-generated method stub
        mLight.SetLight(progress);
        seekBarProgress = progress;
        valueEditor.putInt("lightValue", seekBarProgress);
        valueEditor.commit();//提交修改
        new senddata().sent(mLight.GetLight());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String resultText = resultBuffer.toString();
        // mResultText.setText(resultText);
        // mResultText.setSelection(mResultText.length());

        text = resultText;
        if (times == 0) {
            ID = InstructionsAnalyze.InstructionsAnalyze(text);
            Toast.makeText(LightCtrlActivity.this, String.format("ID = %d", ID),
                    Toast.LENGTH_SHORT).show();
            Log.e("Reutrn ID", String.valueOf(ID));
            if (ID != 0) {
                // ID=Msb.getProgress();
                // sb.setProgress(0);
                // SBEditor.putInt("ID", ID);
                // new senddata().sent(ID);
                mLight.SetLight(mSeekBar.getProgress());
                switch (ID) {
                case 1:// turn on
                    if (mLight.GetLight() == 0) {
                        mLight.SetLight(4);
                        text="开灯到中等亮度";
                    } else {
                        text="灯已经打开";
                    }
                    mSeekBar.setProgress(mLight.GetLight());
                    break;
                case 2:// turn off
                    if (mLight.GetLight() != 0) {
                        mLight.SetLight(0);
                        text="关灯";
                    } else {
                        text="灯已经关闭";
                    }
                    mSeekBar.setProgress(mLight.GetLight());
                    break;
                case 3:// increase
                    if (mLight.GetLight() != 8) {
                        mLight.Up(1);
                        text="增大亮度";
                    } else {
                        text="灯已经最亮";
                    }
                    mSeekBar.setProgress(mLight.GetLight());
                    break;
                case 4:// decrease
                    if (mLight.GetLight() != 0) {
                        mLight.Down(1);
                        text="减小亮度";
                    } else {
                        text="灯已经最暗";
                    }
                    mSeekBar.setProgress(mLight.GetLight());
                    break;
                case 5:
                    new senddata().sent(9);
                    text="切换到自动亮度模式";
                default:
                }
                setEditText(text);
            } else {

                // flytek AI resoponse here
                // Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT)
                // .show();
//                String testtext = "牛顿第一定律";
//                resultText = testtext;
                // 初始化监听器
                mTextUnderstander.understandText(resultText, searchListener);
            }
            times++;
        }
    }
    
//
//    private WakeuperListener mWakeuperListener = new WakeuperListener() { 
//        public void onResult(WakeuperResult result) 
//    { 
//            String text = result.getResultString();
//    } 
//    
//        public void onError(SpeechError error) {
//        } 
//        public void onBeginOfSpeech() {
//            
//        } 
//        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) 
//        { 
//            if (SpeechEvent.EVENT_IVW_RESULT == eventType) 
//        { //当使用唤醒+识别功能时获取识别结果 
//            //arg1:是否最后一个结果，1:是，0:否。 
//            RecognizerResult reslut = ((RecognizerResult)obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT)); 
//            String text="I am awaken.";
//            setEditText(text);
//        } 
//        }
//        };

    /**
     * 参数设置
     * 
     * @param param
     * @return
     */
    public void setEditText (String text)
    {
        mResultText.setText(text);
        mResultText.setSelection(mResultText.length());
        mTts.startSpeaking(text, mSynListener);
    }
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS,
                mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,
                mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,
                mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");

        // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
        // 注：该参数暂时只对在线听写有效
//        mIat.setParameter(SpeechConstant.ASR_DWA,
//                mSharedPreferences.getString("iat_dwa_preference", "0"));
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}