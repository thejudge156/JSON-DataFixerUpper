package me.judge.jsonfixerupper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

        AnimJson parsedJson = null;
        try {
            parsedJson = GSON.fromJson(new FileReader(jsonDataFixerUpperFile), AnimJson.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(parsedJson == null) {
            throw new IllegalStateException("JSON could not parse!");
        }

        for(Object anim : parsedJson.animations.values()) {
            for(AnimJson.Bone bone : ((AnimJson.Animation)anim).bones.values()) {
                for(AnimJson.Vector vec : bone.position.values()) {
                    setVec(vec);
                }
                for(AnimJson.Vector vec : bone.rotation.values()) {
                    setVec(vec);
                }
            }
        }

        try {
            File out = new File("out.json");
            if(!out.exists()) {
                out.createNewFile();
            }
            FileWriter writer = new FileWriter(out);
            GSON.toJson(parsedJson, AnimJson.class, new JsonWriter(writer));
            writer.flush();
            writer.close();
            System.out.println("Done processing!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setVec(AnimJson.Vector vec) {
        try {
            vec.vector[0] = String.valueOf(Double.valueOf(vec.vector[0]));
            vec.vector[1] = String.valueOf(Double.valueOf(vec.vector[1]));
            vec.vector[2] = String.valueOf(Double.valueOf(vec.vector[2]));
        } catch (NumberFormatException exception) {
            System.out.println("String found! Replacing with inferred value.");

            StringBuilder x = new StringBuilder();
            StringBuilder y = new StringBuilder();
            StringBuilder z = new StringBuilder();

            boolean hasNeg = false;
            for(char n : vec.vector[0].toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    x.append(n);
                }
            }
            for(char n : vec.vector[1].toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    y.append(n);
                }
            }
            for(char n : vec.vector[2].toCharArray()) {
                if(Character.isDigit(n) || (!hasNeg && n == '-')) {
                    if(n == '-') {
                        hasNeg = true;
                    }
                    z.append(n);
                }
            }

            vec.vector[0] = String.valueOf(Double.valueOf(x.toString()));
            vec.vector[1] = String.valueOf(Double.valueOf(y.toString()));
            vec.vector[2] = String.valueOf(Double.valueOf(z.toString()));
        }
    }

    private static void printUsage() {
        System.out.println("Usage: ./jsondatafixerupper [json file]");
    }


    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AnimJson.Bone.class, new AnimJson.Vec3KeyframeAdapter());
        GSON = builder.create();
    }
}
