package jcog.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import java.io.IOException;

/** serialization utilities */
public enum Serials { ;

	public static final ObjectMapper jsonMapper =
		new ObjectMapper()
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(SerializationFeature.WRAP_EXCEPTIONS)
			.enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
			.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
			.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
			.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)
			.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
			.enable(MapperFeature.AUTO_DETECT_FIELDS)
			.enable(MapperFeature.AUTO_DETECT_GETTERS)
			.enable(MapperFeature.AUTO_DETECT_IS_GETTERS)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	public static final ObjectMapper cborMapper =
		new ObjectMapper(new CBORFactory())
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

	/**
	 * json/msgpack serialization
	 */
	public static byte[] toBytes(Object x) throws JsonProcessingException {
		return cborMapper.writeValueAsBytes(x);
	}

	public static byte[] toBytes(Object x, Class cl) throws JsonProcessingException {
		return cborMapper.writerFor(cl).writeValueAsBytes(x);
	}

	/**
	 * msgpack deserialization
	 */
	public static <X> X fromBytes(byte[] msgPacked, Class<? extends X> type) throws IOException {
		return cborMapper/*.reader(type)*/.readValue(msgPacked, type);
	}

	public static <X> X fromBytes(byte[] msgPacked, int len, Class<? extends X> type) throws IOException {
		return cborMapper/*.reader(type)*/.readValue(msgPacked, 0, len, type);
	}

	public static JsonNode jsonNode(Object x) {
		if (x instanceof String) {
			try {
				return jsonMapper.readTree(x.toString());
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
		return cborMapper.valueToTree(x);
	}
}
