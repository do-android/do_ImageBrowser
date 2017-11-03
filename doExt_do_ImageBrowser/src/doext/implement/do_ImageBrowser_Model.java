package doext.implement;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoActivityResultListener;
import core.interfaces.DoIPageView;
import core.interfaces.DoIScriptEngine;
import core.object.DoEventCenter;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.bean.Item;
import doext.define.do_ImageBrowser_IMethod;
import doext.imagebrowser.ShowPictureViewActivity;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_ImageBrowser_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_ImageBrowser_Model extends DoSingletonModule implements do_ImageBrowser_IMethod, DoActivityResultListener {
	private DoIPageView pageView;

	public do_ImageBrowser_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("show".equals(_methodName)) {
			this.show(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		// ...do something
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 预览；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void show(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		int _index = DoJsonHelper.getInt(_dictParas, "index", 0);
		JSONArray _data = DoJsonHelper.getJSONArray(_dictParas, "data");
		ArrayList<Item> _itmes = new ArrayList<Item>();
		for (int i = 0; i < _data.length(); i++) {
			JSONObject _node = _data.getJSONObject(i);
			String _source = DoJsonHelper.getString(_node, "source", "");
			String _init = DoJsonHelper.getString(_node, "init", "");
			boolean _isHttpUrl = true;
			if (!isHttpUrl(_source)) {
				_isHttpUrl = false;
				_source = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _source);
			}
			if (!isHttpUrl(_init)) {
				_init = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _init);
			}
			Item _item = new Item(_source, _init, _isHttpUrl);
			_itmes.add(_item);
		}
		pageView = _scriptEngine.getCurrentPage().getPageView();
		pageView.registActivityResultListener(this);
		Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		Intent i = new Intent(_activity, ShowPictureViewActivity.class);
		i.putExtra("itmes", _itmes);
		i.putExtra("selectPos", _index);
		_activity.startActivityForResult(i, 1000);
	}

	private boolean isHttpUrl(String _url) {
		if (_url == null || "".equals(_url) || _url.trim().length() == 0 || _url.startsWith("http://") || _url.startsWith("https://")) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == 1000 && intent != null) {
			DoEventCenter _eventCenter = getEventCenter();
			if (_eventCenter != null) {
				DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
				JSONObject _obj = new JSONObject();
				try {
					_obj.put("index", intent.getIntExtra("index", 0));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				_invokeResult.setResultNode(_obj);
				_eventCenter.fireEvent("result", _invokeResult);
			}
		}
		if (pageView != null) {
			pageView.unregistActivityResultListener(this);
		}
	}

}