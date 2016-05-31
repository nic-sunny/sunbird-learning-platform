package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JsonContentParser {
	
	public Content parseContent(String json) {
		Content content = new Content();
		try {
			JSONObject root = new JSONObject(json);
			content = processContentDocument(root);
		} catch (JSONException ex) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR);
		}
		return content;
	}
	
	private Content processContentDocument(JSONObject root) {
		Content content = new Content();
		if (null != root) {
			content.setManifest(getContentManifest(root.getJSONObject(ContentWorkflowPipelineParams.manifest.name())));
			content.setControllers(getControllers(root.getJSONObject(ContentWorkflowPipelineParams.controller.name())));
		}
		return content;
	}
	
	private Manifest getContentManifest(JSONObject manifestObj) {
		Manifest manifest = new Manifest();
		if (null != manifestObj) {
			List<Media> medias = new ArrayList<Media>();
			JSONArray mediaObjs = manifestObj.getJSONArray(ContentWorkflowPipelineParams.media.name());
			for (int i = 0; i < mediaObjs.length(); i++)
				medias.add(getContentMedia(mediaObjs.getJSONObject(i)));
			manifest.setMedias(medias);
		}
		return manifest;
	}
	
	private Media getContentMedia(JSONObject mediaObj) {
		Media media = new Media();
		if (null != mediaObj) {
			media.setData(getData(mediaObj, ContentWorkflowPipelineParams.media.name()));
			media.setChildrenData(getChildrenData(mediaObj, ContentWorkflowPipelineParams.media.name()));
		}
		return media;
	}
	
	private List<Controller> getControllers(JSONObject controllerObj) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null !=  controllerObj) {
			JSONArray controllerObjs = controllerObj.getJSONArray(ContentWorkflowPipelineParams.controller.name());
			for (int i = 0; i < controllerObjs.length(); i++) {
				Controller controller = new Controller();
				controller.setData(getData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.controller.name()));
				controller.setcData(getCData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.__cdata.name()));
				controllers.add(controller);
			}
		}
		return controllers;
	}
	
	private List<Plugin> getPlugins(JSONObject object) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		
		return plugins;
	}
	
	private JSONObject getPluginViewOfDocument(JSONObject object) {
		if (null != object) {
			//Remove Manifest From Document
			object.remove(ContentWorkflowPipelineParams.manifest.name());
			
			// Remove Controllers From Document
			object.remove(ContentWorkflowPipelineParams.controller.name());
			
			// Remove all (String) Attributes
			Set<String> keys = getMapFromJsonObj(object).keySet();
			for (String key: keys)
				object.remove(key);
		}
		return object;
	}
	
	private Map<String, String> getData(JSONObject object, String elementName) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.element_name.name(), elementName);
		}
		return map;
	}
	
	private String getCData(JSONObject object, String elementName) {
		String cData = "";
		if (null != object && !StringUtils.isBlank(elementName)){
			Map<String, Object> map = ConversionUtil.toMap(object);
			JSONObject obj = (JSONObject) map.get(elementName);
			if (null != obj)
				cData = obj.toString(); 
		}
		return cData;
	}
	
	private List<Map<String, String>> getChildrenData(JSONObject object, String elementName) {
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		if (null != object && !StringUtils.isBlank(elementName))
			maps = toMap(object, elementName);
		return maps;
	}
	
	private Map<String, String> getMapFromJsonObj(JSONObject object) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object) {
			Iterator<String> keysItr = object.keys();
			while(keysItr.hasNext()) {
		        String key = keysItr.next();
		        Object value = object.get(key);
		        if(!(value instanceof JSONArray) && !(value instanceof JSONObject))
		        	map.put(key, (String) value);
			}
		}
		return map;
	}
	
	private List<Map<String, String>> toMap(JSONObject object, String parentKey) throws JSONException {
	    List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
	    Map<String, String> map = new HashMap<String, String>();
	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);
	        if(value instanceof JSONArray)
	        	maps.addAll(toList((JSONArray) value, key));
	        else if(value instanceof JSONObject)
	        	maps.addAll(toMap((JSONObject) value, key));
	        else {
		        map.put(key, (String)value);
		        map.put(ContentWorkflowPipelineParams.element_name.name(), key);
		        map.put(ContentWorkflowPipelineParams.group_element_name.name(), parentKey);
		        maps.add(map);
	        }
	    }
	    return maps;
	}

	private List<Map<String, String>> toList(JSONArray array, String parentKey) throws JSONException {
	    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	        	list.addAll(toList((JSONArray) value, parentKey));
	        } else if(value instanceof JSONObject) {
	        	list.addAll(toMap((JSONObject) value, parentKey));
	        }
	    }
	    return list;
	}
	

}
