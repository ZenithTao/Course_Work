// Do not edit this file for homework.

// A PBST is a "persistent binary search tree".
// PBST and Node objects are immutable: all fields are final.
// PBST uses naive insertion (like BST), so it is not balanced.
//
// When a BST operation would modify the BST, the corresponding PBST
// operation returns a new PBST.  The original PBST remains available
// and unchanged.  The total time and space per modification is O(H),
// where H is the height of the path traversed.

// This is based on share/book/BST.java, with these changes:
//   * all data fields in PBST and Node are final
//   * to help with PRedBlackBST, class Node has a 'color' field
//   * mostly removed "private" access (but left "public")
//   * rewrote many recursive methods as loops (just to show how)
//   * frequent use of ternary operator: "<Test> ? <ExpT> : <ExpF>"
//   * no deletion (keeping this homework simple).
//   * removed min, max, floor, ceiling (but kept rank, select)
//   * removed some linear-time methods (keys, range)
//   * IntIterator, implements Iterator<Key> using an int and select()
//   * added toString() method (which uses the iterator)
//   * added checkBST() method, checking BST properties
//   * added checkLLRB() method, checking LLRB properties
//   * eliminated dependence on book classes (StdIn/StdOut/Queue)

public class PBST<Key extends Comparable<Key>, Value>
    implements Iterable<Key>
{
    // Fields:
    final Node root;         // tree root, can only set this in the constructor

    // For counting Node creations (static, not an instance field):
    private static int nodes;   // total nodes created so far
    public static int nodes() { return nodes; }

    // Node is a persistent Node in a PBST (or a PRedBlackBST).
    // In a PBST, the color is always false (no red edges).
    class Node
    {
        final Key key;           // key, used for comparisons
        final Value val;         // data associated with the key
        final Node left, right;  // left and right subtrees
        final boolean color;     // true iff parent link is RED
        final int N;             // number of nodes in subtree

        Node(Key k, Value v, Node l, Node r, boolean c)
        {
            key = k;
            val = v;
            left = l;
            right = r;
            color = c;
            N = 1 + size(left) + size(right);
            ++nodes;
        }
        // Methods creating a new Node with one modified field:
        Node setLeft(Node l) {
            return l==left ? this : new Node(key, val, l, right, color);
        }
        Node setRight(Node r) {
            return r==right ? this : new Node(key, val, left, r, color);
        }
        Node setVal(Value v) {
            // For some purposes, .equals would suffice (and save memory).
            return v==val ? this : new Node(key, v, left, right, color);
        }
        Node setColor(boolean c) {
            return c==color ? this : new Node(key, val, left, right, c);
        }
        // Print a Node as just a "(key, value)" pair.
        public String toString() { return "(" + key + ", " + val + ")"; }
    }

    // Constructors.  Only the first is public.
    public PBST() { root=null; }
    PBST(Node r) { root=r; }

    // Create a new PBST, but only if the root has changed:
    PBST<Key,Value> setRoot(Node r) { return r==root ? this : new PBST<Key,Value>(r); }

    // Node accessor methods, allowing a null Node:
    int size(Node x) { return x==null ? 0 : x.N; }
    Key key(Node x) { return x==null ? null : x.key; }
    Value val(Node x) { return x==null ? null : x.val; }
    boolean isRed(Node x) { return x!=null && x.color; }

    // empty?
    public boolean isEmpty() { return size() == 0; }

    // Number of items.
    public int size() { return size(root); }

    // Does this PBST have the given key?
    public boolean contains(Key key) { return get(root, key) != null; }

    // Return value associated with key, or null if no such key.
    public Value get(Key key) { return val(get(root, key)); }

    Node get(Node x, Key key)
    {
        // A loop, a bit faster than recursion.
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp == 0) return x;
            x = cmp<0 ? x.left : x.right;
        }
        return null;
    }

    // Insert key-value pair in PBST, returning new modified PBST.
    // It may return this same tree unmodified, if there is no change.
    public PBST<Key,Value> put(Key k, Value v) { return setRoot(put(root, k, v)); }

    // Return tree that results from inserting (key,val) into tree x.
    // This could return x itself, if it is unchanged.
    Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(key, val, null, null, false);
        int cmp = key.compareTo(x.key);
        if (cmp<0) return x.setLeft(put(x.left, key, val));
        if (cmp>0) return x.setRight(put(x.right, key, val));
        return x.setVal(val);
    }

    // Return the key with rank r.  That is, with r smaller keys.
    public Key select(int r) { return key(select(root, r)); }
    Node select(Node x, int r) {
        // A loop here instead of recursion.
        while (x != null)
        {
            int t = size(x.left);
            if (r==t) return x;
            if (r<t) { x = x.left; }
            else { x = x.right; r = r-t-1; }
        }
        return x;
    }

    // Return the rank of a key (the number of strictly smaller keys).
    public int rank(Key key) { return rank(key, root); }
    int rank(Key key, Node x) { // in subtree
        int ret = 0;
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp < 0) { x = x.left; }
            else {
                ret += size(x.left);
                if (cmp==0) break;
                ret += 1;
                x = x.right;
            }
        }
        return ret;
    }

    // Here we implement Iterable<Key>:
    public java.util.Iterator<Key> iterator() { return new IntIterator(); }
    // An IntIterator visits the keys in order.  It uses just an int
    // to keep track of the next key to visit (using select).  In a
    // balanced tree, it needs O(N lg N) time to visit all N keys.
    class IntIterator implements java.util.Iterator<Key> {
        int pos;
        IntIterator() { pos = 0; }
        public Key next() { return select(pos++); }
        public boolean hasNext() { return pos < size(); }
        public void remove() { // we cannot remove
            throw new UnsupportedOperationException();
        }
    }

    // Return string of form "[(key0, val0), (key1, val1), ...]"
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[");
        String sep = "";
        // This "enhanced for loop" implicitly uses our iterator:
        for (Key key: this)
        {
            sb.append(sep + get(root, key));
            sep = ", ";
        }
        return sb.append("]").toString();
    }

    // Announce a bug, by throwing an exception.
    void bug(String msg) { throw new RuntimeException("bug: " + msg); }
        
    // Check integrity as BST, and maybe as LLRB.  Throw exception if bad.
    public void check()
    {
        // Check the BST (binary search tree) properties.
        checkInOrder(root, null, null);
        checkSizes(root);
        // Maybe also check LLRB (left-leaning red-black) properties.
        if (this instanceof PRedBlackBST)
        {
            if (isRed(root)) bug("root is red");
            checkIs23(root);
            int depth = 0;
            for (Node x=root; x != null; x = x.right) ++depth;
            checkBlackDepth(root, depth);
        }
    }
    
    // Check the keys are in increasing order.
    void checkInOrder(Node x, Key lower, Key upper)
    {
        if (x == null) return;
        if (lower != null && x.key.compareTo(lower) <= 0)
            bug("found " + x + " after " + lower);
        if (upper != null && x.key.compareTo(upper) >= 0)
            bug("found " + x + " before " + upper);
        checkInOrder(x.left, lower, x.key);
        checkInOrder(x.right, x.key, upper);
    }

    // Check that all the N (size) fields are correct.
    void checkSizes(Node x)
    {
        if (x == null) return;
        if (x.N != size(x.left) + size(x.right) + 1) 
            bug("bad size " + x.N + " at " + x);
        checkSizes(x.left);
        checkSizes(x.right);
    }

    // Check this is a 2-3 tree, with all 3-nodes represented by a left-leaning red edge.
    void checkIs23(Node x)
    {
        if (x == null) return;
        if (isRed(x.right))
            bug("right-leaning red edge below " + x);
        if (isRed(x) && isRed(x.left))
            bug("two red edges at " + x);
        checkIs23(x.left);
        checkIs23(x.right);
    }
    
    // Check that every x-to-null path has exactly bd black edges.
    private void checkBlackDepth(Node x, int bd)
    {
        if (x == null) return;
        if (!isRed(x)) bd--;
        if ((x.left == null || x.right == null) && bd != 0)
            bug("excess depth " + bd + " at " + x);
        checkBlackDepth(x.left, bd);
        checkBlackDepth(x.right, bd);
    }

}
