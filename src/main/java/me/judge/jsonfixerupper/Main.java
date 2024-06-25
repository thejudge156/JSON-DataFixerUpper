package me.judge.jsonfixerupper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Gson GSON;

    public static void main(String[] args) {
        if(args.length != 1) {
            printUsage();
            return;
        }

        File jsonDataFixerUpperFile = new File(args[0]);
        if (!jsonDataFixerUpperFile.exists()) {
            System.out.println("File does not exist!");
            printUsage();
        }
        if (!jsonDataFixerUpperFile.getName().endsWith(".json")) {
            System.out.println("File is not a JSON!");
            printUsage();
        }

        AnimationJson parsedJson = null;
        try {
            parsedJson = GSON.fromJson(new FileReader(jsonDataFixerUpperFile), AnimationJson.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(parsedJson == null) {
            throw new IllegalStateException("JSON could not parse!");
        }

        for(AnimationJson.Animation anim : parsedJson.animations.values()) {
            anim.animation_length += 0.002;
            for(AnimationJson.Bone bone : anim.bones.values()) {
                Map<String, AnimationJson.GenericHolder> mergeRot = new HashMap<>();

                for (Map.Entry<String, AnimationJson.GenericHolder> holder : bone.position.entrySet()) {
                    setVec(holder.getValue());
                }
                for (Map.Entry<String, AnimationJson.GenericHolder> holder : bone.rotation.entrySet()) {
                    mergeRot.putAll(setVec(holder.getValue()));
                }

                bone.rotation.putAll(mergeRot);
            }
        }

        try {
            File out = new File("out.json");
            if(!out.exists()) {
                out.createNewFile();
            }
            FileWriter writer = new FileWriter(out);
            GSON.toJson(parsedJson, AnimationJson.class, new JsonWriter(writer));
            writer.flush();
            writer.close();
            System.out.println("Done processing!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, AnimationJson.GenericHolder> setVec(AnimationJson.GenericHolder vec) {
        Map<String, AnimationJson.GenericHolder> ret = new HashMap<>();
        try {
            double x = Double.parseDouble(vec.vector.get(0));
            double y = Double.parseDouble(vec.vector.get(1));
            double z = Double.parseDouble(vec.vector.get(2));

            /*
            boolean needsReset = false;
            double newX = x, newY = y, newZ = z;
            if(x <= -360 || x >= 360) {
                needsReset = true;
                newX = x % (x < 0 ? -360 : 360);
            }
            if(y <= -360 || y >= 360) {
                needsReset = true;
                newY = y % (y < 0 ? -360 : 360);
            }
            if(z <= -360 || z >= 360) {
                needsReset = true;
                newZ = z % (z < 0 ? -360 : 360);
            }

            if(needsReset) {
                AnimationJson.GenericHolder holder = new AnimationJson.GenericHolder();
                holder.vector = List.of(String.valueOf(newX), String.valueOf(newY), String.valueOf(newZ));
                holder.keyframe = "wack";
                ret.put(String.valueOf(Double.parseDouble(currKeyframe) + 0.002), holder);
            }*/

            vec.vector.set(0, String.valueOf(x));
            vec.vector.set(1, String.valueOf(y));
            vec.vector.set(2, String.valueOf(z));
        } catch (NumberFormatException exception) {
            System.out.println("String found! Replacing with inferred value.");

            StringBuilder x = new StringBuilder();
            StringBuilder y = new StringBuilder();
            StringBuilder z = new StringBuilder();

            boolean hasNeg = false;
            for(char n : vec.vector.get(0).toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    x.append(n);
                }
            }
            hasNeg = false;
            for(char n : vec.vector.get(1).toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    y.append(n);
                }
            }
            hasNeg = false;
            for(char n : vec.vector.get(2).toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    z.append(n);
                }
            }

            if(x.toString().equals("-")) {
                x.setCharAt(0, '0');
            }
            if(y.toString().equals("-")) {
                y.setCharAt(0, '0');
            }
            if(z.toString().equals("-")) {
                z.setCharAt(0, '0');
            }

            if(x.toString().endsWith("-")) {
                x.deleteCharAt(x.toString().length() - 1);
            }
            if(y.toString().endsWith("-")) {
                y.deleteCharAt(y.toString().length() - 1);
            }
            if(z.toString().endsWith("-")) {
                z.deleteCharAt(z.toString().length() - 1);
            }

            if(x.toString().isBlank()) {
                x.append("0");
            }
            if(y.toString().isBlank()) {
                y.append("0");
            }
            if(z.toString().isBlank()) {
                z.append("0");
            }

            vec.vector.set(0, String.valueOf(Double.valueOf(x.toString())));
            vec.vector.set(1, String.valueOf(Double.valueOf(y.toString())));
            vec.vector.set(2, String.valueOf(Double.valueOf(z.toString())));
        }
        return ret;
    }

    private static void printUsage() {
        System.out.println("Usage: ./jsondatafixerupper [json file]");
    }

    static {
        GsonBuilder builder = new GsonBuilder();
        GSON = builder.create();
    }
}
