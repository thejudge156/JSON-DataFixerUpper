package me.judge.jsonfixerupper;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

public class AnimationJson {
    String azurelib_format_version;
    Map<String, Animation> animations;

    public static class Animation {
        double animation_length;
        Map<String, Bone> bones;
    }

    public static class Bone {
        @JsonAdapter(GenericHolderAdapter.class)
        Map<String, GenericHolder> rotation = new HashMap<>();
        @JsonAdapter(GenericHolderAdapter.class)
        Map<String, GenericHolder> position = new HashMap<>();
        List<String> scale = new ArrayList<>();
    }

    public static class GenericHolderAdapter extends TypeAdapter<Map<String, GenericHolder>> {

        @Override
        public void write(JsonWriter out, Map<String, GenericHolder> value) throws IOException {
            out.beginObject();
            for (String key : value.keySet()) {
                out.name(key);
                out.beginObject();
                out.name("vector");
                out.beginArray();
                for(String vec : value.get(key).vector) {
                    out.value(Double.valueOf(vec));
                }
                out.endArray();
                if(value.get(key).easing != null) {
                    out.name("easing");
                    out.value(value.get(key).easing);
                }
                out.endObject();
            }
            out.endObject();
        }

        @Override
        public Map<String, GenericHolder> read(JsonReader in) throws IOException {
            Map<String, GenericHolder> holderMap = new HashMap<>();

            in.beginObject();
            while(in.hasNext()) {
                GenericHolder holder = new GenericHolder();
                String name = holder.keyframe = in.nextName();
                if(in.peek() == JsonToken.BEGIN_OBJECT) {
                    in.beginObject();
                    in.nextName();
                    in.beginArray();
                    while (in.hasNext()) {
                        holder.vector.add(in.nextString());
                    }
                    in.endArray();
                    if(in.peek() == JsonToken.NAME) {
                        in.nextName();
                        holder.easing = in.nextString();
                    }
                    in.endObject();
                } else {
                    holder.keyframe = name = "0.0";
                    in.beginArray();
                    while (in.hasNext()) {
                        holder.vector.add(in.nextString());
                    }
                    in.endArray();
                    if(in.peek() == JsonToken.NAME) {
                        in.nextName();
                        holder.easing = in.nextString();
                    }
                }

                holderMap.put(name, holder);
            }
            in.endObject();

            return holderMap;
        }
    }

    public static class GenericHolder {
        String keyframe;
        List<String> vector = new ArrayList<>();
        String easing;
    }
}
