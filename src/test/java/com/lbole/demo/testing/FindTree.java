package com.lbole.demo.testing;

/**
 * @Author 马嘉祺
 * @Date 2020/9/15 0015 15 02
 * @Description <p></p>
 */
public class FindTree<K extends Comparable<K>, V> {
    
    private Node root;
    
    class Node implements Comparable<Node> {
        K key;
        V value;
        int height;
        Node parent, left, right;
        
        public Node(K key, V value, int height, Node parent) {
            this.key = key;
            this.value = value;
            this.height = height;
            this.parent = parent;
        }
        
        @Override
        public int compareTo(Node node) {
            return key.compareTo(node.key);
        }
        
    }
    
    public K getRKey() {
        return null == root ? null : root.key;
    }
    
    public V getRValue() {
        return null == root ? null : root.value;
    }
    
}
