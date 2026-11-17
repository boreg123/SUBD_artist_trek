import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Map;

public class NumberFixer implements JsonDeserializer<Map<String, Object>> {
    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                JsonPrimitive prim = value.getAsJsonPrimitive();
                if (prim.isNumber()) {
                    String numStr = prim.getAsString();
                    if (numStr.contains(".")) {
                        map.put(entry.getKey(), prim.getAsDouble());
                    } else {
                        map.put(entry.getKey(), prim.getAsInt());
                    }
                } else if (prim.isBoolean()) {
                    map.put(entry.getKey(), prim.getAsBoolean());
                } else {
                    map.put(entry.getKey(), prim.getAsString());
                }
            } else {
                map.put(entry.getKey(), context.deserialize(value, Object.class));
            }
        }
        return map;
    }
}