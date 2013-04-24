package net.ubuntudaily.quickquest.commons.json;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonHelper {

	private JsonHelper() {
		
	}
	private static final Logger LOG = LoggerFactory.getLogger(JsonHelper.class); 
	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}
	public static final <T> T fromJson(String jsonStr, Class<T> t){
		T obj = null;
		try {
			obj = mapper.readValue(jsonStr, t);
		} catch (JsonParseException e) {
			LOG.error(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return obj;
	}
	public static final <T> T fromJson(File jsonFile, Class<T> t){
		T obj = null;
		try {
			obj = mapper.readValue(jsonFile, t);
		} catch (JsonParseException e) {
			LOG.error(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return obj;
	}
	public static final String toJson(Object o){
		String jsonStr = "";
		try {
			jsonStr = mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage());
		}
		return jsonStr;
	}
	public static final void toJson(File target, Object o){
		try {
			mapper.writeValue(target, o);
		} catch (JsonGenerationException e) {
			LOG.error(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}
}
