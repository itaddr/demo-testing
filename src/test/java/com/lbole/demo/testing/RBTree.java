package com.lbole.demo.testing;

import org.junit.Test;

/**
 * @Author 马嘉祺
 * @Date 2020/9/15 0015 14 30
 * @Description <p></p>
 */
public class RBTree<K extends Comparable<K>, V> {
    
    private final static int RED = 0, BLACK = 1;
    
    private RBNode root;
    
    class RBNode implements Comparable<RBNode> {
        K key;
        V value;
        int color;
        RBNode parent, left, right;
        
        public RBNode(K key, V value, int color, RBNode parent) {
            this.key = key;
            this.value = value;
            this.color = color;
            this.parent = parent;
        }
        
        @Override
        public int compareTo(RBNode node) {
            return key.compareTo(node.key);
        }
    }
    
    public K getRKey() {
        return null == root ? null : root.key;
    }
    
    public V getRValue() {
        return null == root ? null : root.value;
    }
    
    public boolean isRBTree() {
        return isRBTree(null);
    }
    
    public boolean isRBTree(RBNode node) {
        node = null == node ? root : node;
        RBNode parent = node.parent, nodeLeft = node.left, nodeRight = node.right;
        if (null != parent && (node != parent.left || node != parent.right)) {
            // 父节不为空，但是当前节点既不为父节点的左子节点，也不为父节点的右子节点
            return false;
        }
        if (null != nodeLeft) {
            // 当前节点的左子节点不为空
            if (nodeLeft.parent != node || nodeLeft.compareTo(node) >= 0) {
                // 当前节点的左子节点
                return false;
            }
            if (RED == nodeLeft.color) {
                if (RED == node.color) {
                    return false;
                }
            } else {
                if (null == nodeRight) {
                    return false;
                } else if (RED == nodeRight.color && (null == nodeRight.left || null == nodeRight.right)) {
                    return false;
                }
            }
            if (!isRBTree(nodeLeft)) {
                return false;
            }
        }
        if (null != nodeRight) {
            if (nodeRight.parent != node || nodeRight.compareTo(node) <= 0) {
                return false;
            }
            if (RED == nodeRight.color) {
                if (RED == node.color) {
                    return false;
                }
            } else {
                if (null == nodeLeft) {
                    return false;
                } else if (RED == nodeLeft.color && (null == nodeLeft.left || null == nodeLeft.right)) {
                    return false;
                }
            }
            if (!isRBTree(nodeRight)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 左旋转节点
     *
     * @param rotate
     */
    private void rotateLeft(RBNode rotate) {
        // 获取旋转节点的右子节点
        RBNode right, parent, broLeft;
        if (null == rotate || null == (right = rotate.right)) {
            return;
        }
        if (null != (broLeft = rotate.right = right.left)) {
            // 将旋转节点的右子节点设置为右子节点的左子节点，并将右子节点的左子节点父节点设置为旋转节点
            broLeft.parent = rotate;
        }
        if (null == (parent = right.parent = rotate.parent)) {
            // 右子节点的父节点设置为旋转节点的父节点，如果父节点为空则将右子节点设置为根节点，并将颜色设置为黑色
            (this.root = right).color = BLACK;
        } else if (parent.left == rotate) {
            parent.left = right;
        } else {
            parent.right = right;
        }
        right.left = rotate;
        rotate.parent = right;
    }
    
    /**
     * 右旋转节点
     *
     * @param rotate
     */
    private void rotateRight(RBNode rotate) {
        // 获取旋转节点的左子节点
        RBNode left, parent, broRight;
        if (null == rotate || null == (left = rotate.left)) {
            return;
        }
        if (null != (broRight = rotate.left = left.right)) {
            // 将旋转节点的左子节点设置为左子节点的右子节点，并将左子节点的右子节点父节点设置为旋转节点
            broRight.parent = rotate;
        }
        if (null == (parent = left.parent = rotate.parent)) {
            // 将左子节点的父节点设置为旋转节点的父节点，如果付几点为空则将左子节点设置为根节点，并将颜色置黑
            (this.root = left).color = BLACK;
        } else if (parent.left == rotate) {
            parent.left = left;
        } else {
            parent.right = left;
        }
        left.right = rotate;
        rotate.parent = left;
    }
    
    public V find(K key) {
        RBNode node = findNode(key, root);
        return null == node ? null : node.value;
    }
    
    private RBNode findNode(K key, RBNode current) {
        RBNode found = null;
        while (null != current && null == found) {
            int compare = key.compareTo(current.key);
            if (compare > 0) {
                current = current.right;
            } else if (compare < 0) {
                current = current.left;
            } else {
                found = current;
            }
        }
        return found;
    }
    
    public V put(K key, V value) {
        if (null == key) {
            return null;
        }
        RBNode current = root, parent = null;
        int compare = 0;
        while (null != current && 0 != (compare = key.compareTo(current.key))) {
            parent = current;
            current = compare > 0 ? current.right : current.left;
        }
        if (null != current) { // 要插入的key已存在
            return current.value = value;
        }
        if (null == parent) { // 要插入的树为空树
            return (root = new RBNode(key, value, BLACK, null)).value;
        }
        // 插入新节点
        RBNode insert = new RBNode(key, value, RED, parent);
        if (compare < 0) {
            parent.left = insert;
        } else {
            parent.right = insert;
        }
        // 重新平衡插入节点后的树
        fixAfterPut(insert);
        return value;
    }
    
    private void fixAfterPut(RBNode current) {
        for (RBNode parent, grandfather, graLeft, graRight; ; ) {
            if (null == (parent = current.parent)) {
                // 父节点为空，则当前节点为根节点
                current.color = BLACK;
                return;
            }
            if (BLACK == parent.color || null == (grandfather = parent.parent)) {
                // 父节点为黑色节点，或者祖父节点为空（父节点是根节点）
                return;
            }
            if ((graLeft = grandfather.left) == parent) { // 父节点为祖父节点的左子节点
                if (null != (graRight = grandfather.right) && RED == graRight.color) {
                    // 叔叔节点不为空并且是红色节点
                    graRight.color = BLACK; // 将叔叔节点颜色置黑
                    parent.color = BLACK; // 将父节点颜色置黑
                    grandfather.color = RED; // 将祖父节点颜色置红
                    current = grandfather; // 将祖父节点设为当前节点
                } else {
                    // 叔叔节点微空节点或者为黑色节点
                    if (current == parent.right) {
                        // 当前节点为父节点的右子节点
                        rotateLeft(current = parent); // 将将父节点设为当前节点并将当前节点左旋转
                        grandfather = (parent = current.parent).parent; // 重新为父节点和祖父节点赋值
                    }
                    parent.color = BLACK; // 将父节点颜色置黑
                    grandfather.color = RED; // 将祖父节点颜色置红
                    rotateRight(grandfather); // 将祖父节点进行右旋转
                }
            } else { // 父节点为祖父节点的右子节点
                if (graLeft != null && RED == graLeft.color) {
                    graLeft.color = BLACK;
                    parent.color = BLACK;
                    grandfather.color = RED;
                    current = grandfather;
                } else {
                    if (current == parent.left) {
                        rotateRight(current = parent);
                        grandfather = (parent = current.parent).parent;
                    }
                    parent.color = BLACK;
                    grandfather.color = RED;
                    rotateLeft(grandfather);
                }
            }
        }
    }
    
    public V remove(K key) {
        // key为空或者此数为空树
        RBNode remove, parent, replace;
        if (null == key || null == root || null == (remove = findNode(key, root))) {
            return null;
        }
        V value = remove.value;
        if (null != remove.left && null != (replace = remove.right)) {
            // 删除节点的左右子节点都不为空节点，将删除节点和后继节点替换
            while (null != replace.left) {
                replace = replace.left;
            }
            remove.key = replace.key;
            remove.value = replace.value;
            remove = replace;
        }
        if (null != (replace = null == (replace = remove.left) ? remove.right : replace)) {
            // 删除节点的左右子节点有一个不为空，将删除节点和子节点替换
            remove.key = replace.key;
            remove.value = replace.value;
            remove = replace;
        }
        if (null == (parent = remove.parent)) {
            // 删除节点为根节点
            root = null;
            return value;
        }
        fixBeforeRemove(remove);
        remove.parent = null;
        if (remove == parent.right) {
            parent.right = null;
        } else {
            parent.left = null;
        }
        return value;
    }
    
    /**
     * 删除 叶子节点 后的修复过程
     *
     * @param current 被删除的节点
     */
    private void fixBeforeRemove(RBNode current) {
        for (RBNode parent, left, right; null != current && null != (parent = current.parent); ) {
            // 当前节点不为空，并且当前节点的父节点也不为空
            if (RED == current.color) {
                /*
                 * 当前节点为红色节点，则：
                 * 1、当前节点的兄弟节点为空节点；
                 * 2、或者，当前节点分别有一个黑色子节点和红色子节点，且其中红色子节点的子节点都为空节点或都为黑色节点；
                 */
                current.color = BLACK;
                return;
            }
            /*
             * 当前节点情况：
             * 1、当前节点不为空，并且是黑色节点
             * 2、当前节点的兄弟节点不为空
             */
            if ((left = parent.left) == current) { // 如果当前节点为父节点的左子节点
                if (RED == (right = parent.right).color) {
                    /*
                     * 如果当前节点的兄弟节点为红色节点，则：
                     * 1、父节点就一定为黑色节点；
                     * 2、兄弟节点的左右子节点一定为黑色节点；
                     */
                    right.color = BLACK; // 将兄弟节点颜色置黑
                    parent.color = RED; // 将父节点颜色置红
                    rotateLeft(parent); // 将父节点左旋转（当前节点任然是父节点的左子节点）
                    right = parent.right; // 重新获取当前节点的兄弟节点
                }
                RBNode broLeft = right.left, broRight = right.right;
                if ((null == broRight || BLACK == broRight.color) && (null == broLeft || BLACK == broLeft.color)) {
                    // 兄弟节点的左右子节点不存在红色节点，则兄弟节点的左右子节点都为Nil节点或者都为黑色节点
                    right.color = RED; // 将兄弟节点颜色置红
                    current = parent; // 将父节点设为当前节点
                } else { // 兄弟节点下一定有一个红色子节点
                    if (null == broRight || BLACK == broRight.color) {
                        // 如果兄弟节点的右子节点为Nil节点或者黑色节点，则兄弟节点的左子节点一定为红色节点
                        broLeft.color = BLACK; // 将兄弟节点的左子节点颜色置黑
                        right.color = RED; // 将兄弟节点颜色置红
                        rotateRight(right); // 将兄弟节点右旋转
                        right = parent.right; // 重新获取右子节点
                        broRight = right.right;
                    }
                    right.color = parent.color; // 将兄弟节点的颜色置为父节点的颜色
                    broRight.color = BLACK; // 将兄弟节点的右子节点颜色置黑
                    parent.color = BLACK; // 将父节点颜色置黑
                    rotateLeft(parent); // 将父节点左旋转
                    return;
                }
            } else { // 当前节点为右子节点
                if (RED == left.color) {
                    left.color = BLACK;
                    parent.color = RED;
                    rotateRight(parent);
                    left = parent.left;
                }
                RBNode broLeft = left.left, broRight = left.right;
                if ((null == broLeft || BLACK == broLeft.color) && (null == broRight || BLACK == broRight.color)) {
                    left.color = RED;
                    current = parent;
                } else {
                    if (null == broLeft || BLACK == broLeft.color) {
                        broRight.color = BLACK;
                        left.color = RED;
                        rotateLeft(left);
                        left = parent.left;
                        broLeft = left.left;
                    }
                    left.color = parent.color;
                    broLeft.color = BLACK;
                    parent.color = BLACK;
                    rotateRight(parent);
                    return;
                }
            }
        }
    }
    
    @Test
    public void main(String[] args) {
        
        RBTree<Integer, String> bst = new RBTree<>();
        
        bst.put(100, "v100");
        bst.put(50, "v50");
        bst.put(150, "v150");
        bst.put(20, "v20");
        bst.put(85, "v85");
        bst.put(10, "v10");
        bst.put(15, "a15");
        bst.put(75, "v75");
        bst.put(95, "v95");
        bst.put(65, "v65");
        bst.put(76, "v76");
        bst.put(60, "v60");
        bst.put(66, "v66");
        bst.put(61, "v61");
        
        
        // 当前节点是左节点 的 5中情况
        //bst.delete(15); // 1. 兄弟节点是黑色的，且有一个右节点（可以断定 右节点是红色的）
        
        // 2. 兄弟节点是黑色的，且有一个左节点（可以断定 左节点是红色的
        //bst.put(140, "v140");
        //bst.delete(95);
        
        // 4. 兄弟节点是黑色的，且没有子节点
        //bst.delete(66);
        
        //5. 如果该兄弟节点是红色的，那么根据红黑树的特性可以得出它的一定有两个黑色的子节点
        //bst.delete(95);
        //bst.delete(15);
        
        
        System.out.println(bst.root);
    }
    
}
