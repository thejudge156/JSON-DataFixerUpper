package me.judge.jsonfixerupper;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

public class AnimJson {
    public String format_version;
    public Map<String, Animation> animations;
    public int azurelib_format_version;

    public static class Animation {
        double animation_length;
        Map<String, Bone> bones;
    }

    public static class Bone {
        public Map<String, Vector> rotation = new LinkedHashMap<>();
        public Map<String, Vector> position = new LinkedHashMap<>();
        public Vector scale;
    }

    public static class Vector {
        public String[] vector;
        public String easing;

        public Vector(String x, String y, String z) {
            vector = new String[] { x, y, z };
        }
    }

    public static class Vec3KeyframeAdapter extends TypeAdapter<Bone> {

        @Override
        public void write(JsonWriter out, Bone value) throws IOException {
            out.beginObject();
            out.name("rotation");
            out.beginObject();
            if(value.rotation.entrySet().isEmpty()) {
                out.name("vector");
                out.beginArray();
                out.value(0);
                out.value(0);
                out.value(0);
                out.endArray();
            }
            for(Map.Entry<String, Vector> vec : value.rotation.entrySet()) {
                if(!vec.getKey().equals("")) {
                    out.name(vec.getKey());
                    out.beginObject();
                }

                out.name("vector");
                out.beginArray();
                out.value(Double.valueOf(vec.getValue().vector[0]));
                out.value(Double.valueOf(vec.getValue().vector[1]));
                out.value(Double.valueOf(vec.getValue().vector[2]));
                out.endArray();

                if(vec.getValue().easing != null) {
                    out.name("easing");
                    out.value(vec.getValue().easing);
                }

                if(!vec.getKey().equals("")) {
                    out.endObject();
                }
            }
            out.endObject();

            out.name("position");
            out.beginObject();
            if(value.position.entrySet().isEmpty()) {
                out.name("vector");
                out.beginArray();
                out.value(0);
                out.value(0);
                out.value(0);
                out.endArray();
            }
            for(Map.Entry<String, Vector> vec : value.position.entrySet()) {
                if(!vec.getKey().equals("")) {
                    out.name(vec.getKey());
                    out.beginObject();
                }
                out.name("vector");
                out.beginArray();
                out.value(Double.valueOf(vec.getValue().vector[0]));
                out.value(Double.valueOf(vec.getValue().vector[1]));
                out.value(Double.valueOf(vec.getValue().vector[2]));
                out.endArray();

                if(vec.getValue().easing != null) {
                    out.name("easing");
                    out.value(vec.getValue().easing);
                }

                if(!vec.getKey().equals("")) {
                    out.endObject();
                }
            }

            if(value.scale != null) {
                out.name("scale");
                out.beginObject();
                out.name("vector");
                out.beginArray();
                out.value(Double.valueOf(value.scale.vector[0]));
                out.value(Double.valueOf(value.scale.vector[0]));
                out.value(Double.valueOf(value.scale.vector[0]));
                out.endArray();
                out.endObject();
            }

            out.endObject();
            out.endObject();
        }

        @Override
        public Bone read(JsonReader in) throws IOException {
            Bone bone = new Bone();
            in.beginObject();

            String currType = "a";
            String currField = "a";
            String currKeyframe = "a";
            while(in.hasNext()) {
                if(in.peek().equals(JsonToken.NAME)) {
                    currField = in.nextName();
                    if(currField.equals("position") || currField.equals("rotation")) {
                        currType = currField;
                        in.beginObject();
                    }
                    if(currField.contains(".")) {
                        currKeyframe = currField;
                    }
                }

                Map<String, Vector> list = "position".equals(currType) ? bone.position : bone.rotation;
                if(currKeyframe.contains(".")) {
                    in.beginObject();
                    in.nextName();
                    in.beginArray();
                    list.put(currKeyframe, new Vector(in.nextString(), in.nextString(), in.nextString()));
                    in.endArray();
                    currKeyframe = "";

                    if(in.peek().equals(JsonToken.NAME)) {
                        in.nextName();
                        in.nextString();
                    }

                    in.endObject();

                    if(in.peek().equals(JsonToken.END_OBJECT)) {
                        in.endObject();
                    }
                }

                if("vector".equals(currField)) {
                    in.beginArray();
                    currKeyframe = "";
                    list.put(currKeyframe, new Vector(in.nextString(), in.nextString(), in.nextString()));
                    in.endArray();

                    if(in.peek().equals(JsonToken.NAME)) {
                        in.nextName();
                        in.nextString();
                    }

                    if(in.peek().equals(JsonToken.END_OBJECT)) {
                        in.endObject();
                    }
                }

                if("scale".equals(currField)) {
                    in.beginObject();
                    in.nextName();
                    in.beginArray();
                    bone.scale = new Vector(String.valueOf(in.nextDouble()), String.valueOf(in.nextDouble()), String.valueOf(in.nextDouble()));
                    in.endArray();
                    in.endObject();
                }
            }
            in.endObject();

            return bone;
        }
    }
}
