package com.example.Model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PrefixCloud {

    private static List<String> traverseQuery(Node node, List<String> result){
        if (node != null) {
            if(node.word!=null){
                result.add(node.word);
            }
            if (node.children!=null) {
                for (Node value : node.children.values()) {
                    traverseQuery(value, result);
                }
            }
        }
        return result;
    }

    public static List<String> query(String prefix) {
        Node node = getInstance().cloud.get(prefix.charAt(0));
        for (int i = 1; i < prefix.length() && node != null; i++) {
            node = node.get(prefix.charAt(i));
        }
        if (node != null) {
            //prefix is in cloud
            return traverseQuery(node, new ArrayList<>());
        }
        return null; //this should be an optional
    }

    private static class Node {
        public Node(String word, int index) {
            insert(word, index);
        }

        public Node get(Character c) {
            return children.get(c);
        }

        public String getWord() {
            return word;
        }

        private Hashtable<Character, Node> children;
        private String word;

        public void insert(String word, int index) {
            if (index == word.length() - 1) {
                this.word = word;
            } else {
                if (this.children == null) {
                    this.children = new Hashtable<>();
                }
                ++index;
                if (children.containsKey(word.charAt(0))) {
                    children.get(word.charAt(index)).insert(word, index);
                } else {
                    children.put(word.charAt(index), new Node(word, index));
                }
            }
        }
    }

    private final Hashtable<Character, Node> cloud;

    private static class PrefixCloudSingletonHelper {
        private static final PrefixCloud INSTANCE = new PrefixCloud();
    }

    public static PrefixCloud getInstance() {
        return PrefixCloudSingletonHelper.INSTANCE;
    }

    private PrefixCloud() {
        cloud = new Hashtable<>();
    }

    public static void add(String word) {
        if (getInstance().cloud.containsKey(word.charAt(0))) {
            getInstance().cloud.get(word.charAt(0)).insert(word, 0);
        } else {
            getInstance().cloud.put(word.charAt(0), new Node(word, 0));
        }
    }
}
