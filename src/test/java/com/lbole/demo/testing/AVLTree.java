package com.lbole.demo.testing;

/**
 * @Author 马嘉祺
 * @Date 2020/9/15 0015 15 02
 * @Description <p></p>
 */
public class AVLTree<K extends Comparable<K>, V> {
    
    private AVLNode root;
    
    class AVLNode implements Comparable<AVLNode> {
        K key;
        V value;
        int height;
        AVLNode parent, left, right;
        
        public AVLNode(K key, V value, int height, AVLNode parent) {
            this.key = key;
            this.value = value;
            this.height = height;
            this.parent = parent;
        }
        
        @Override
        public int compareTo(AVLNode node) {
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
