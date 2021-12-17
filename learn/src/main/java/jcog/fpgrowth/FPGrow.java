package jcog.fpgrowth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static jcog.Str.n4;


/**
 * FP-Growth Procedure
 *
 * @author Yue Shang
 */
public class FPGrow {

    int supportThreshold = 3;

    private final Map<List<String>, Integer> freq = new HashMap<>();

    public FPGrow grow(List<List<String>> data) {

        HashMap<String, Integer> itemCount = count(data);


        for (List<String> transaction : data) {
            transaction.sort((o1, o2) ->
                    Integer.compare(itemCount.get(o2), itemCount.get(o1)));
        }

        grow(data, null);
        return this;
    }


    /**
     * @param data
     * @param postModel
     */
    void grow(List<List<String>> data, List<String> postModel) {
        Map<String, Integer> itemCount = count(data);
        Map<String, FPNode> headerTable = new HashMap<>();


        for (Entry<String, Integer> entry : itemCount.entrySet()) {
            String itemName = entry.getKey();
            Integer count = entry.getValue();
            if (count >= this.supportThreshold) {
                FPNode node = new FPNode(itemName);
                node.support = count;
                headerTable.put(itemName, node);
            }
        }

        FPNode root = buildTree(data, itemCount, headerTable);

        if (root.children.isEmpty()) return;

        
        if (isSingleBranch(root)) {
            FPNode curr = root;

            ArrayList<FPNode> path = new ArrayList<>(curr.children.values());

            List<List<FPNode>> combinations = new ArrayList<>();
            combinations(path, combinations);

            for (List<FPNode> combine : combinations) {
                int supp = 0;

                List<String> rule = new ArrayList<>();

                for (FPNode node : combine) {
                    rule.add(node.itemName);
                    supp = node.support;
                }

                if (postModel != null) {
                    rule.addAll(postModel);
                }

                freq.put(rule, supp);
            }

        }

        for (FPNode header : headerTable.values()) {

            List<String> rule = new ArrayList<>();
            rule.add(header.itemName);

            if (postModel != null) {
                rule.addAll(postModel);
            }

            freq.put(rule, header.support);

            List<String> newPostPattern = new ArrayList<>();
            newPostPattern.add(header.itemName);
            if (postModel != null) {
                newPostPattern.addAll(postModel);
            }

            
            List<List<String>> newCPB = new LinkedList<>();
            FPNode nextNode = header;
            while ((nextNode = nextNode.next) != null) {
                int leaf_supp = nextNode.support;

                
                LinkedList<String> path = new LinkedList<>();
                FPNode parent = nextNode;
                while (!"ROOT".equals((parent = parent.parent).itemName)) {
                    path.push(parent.itemName);
                }
                if (path.isEmpty()) continue;

                while (leaf_supp-- > 0) {
                    newCPB.add(path);
                }
            }
            grow(newCPB, newPostPattern);
        }
    }

    /**
     * Generate all the possible combinations for a given item setAt. Use bitmap
     *
     * @param path
     * @param combinations
     */
    private static void combinations(ArrayList<FPNode> path, List<List<FPNode>> combinations) {
        int length = path.size();
        if (length == 0) return;
        double c = Math.pow(2, length);
        for (int i = 1; i < c; i++) {

            String bitmap = Integer.toBinaryString(i);
            int bound = bitmap.length();
            List<FPNode> combine = IntStream.range(0, bound).filter(j -> bitmap.charAt(j) == '1').mapToObj(j -> path.get(length - bitmap.length() + j)).collect(Collectors.toList());
            combinations.add(combine);
        }
    }


    private static FPNode buildTree(List<List<String>> transactions, Map<String, Integer> itemCount, Map<String, FPNode> headerTable) {
        FPNode root = new FPNode("ROOT");
        root.parent = null;

        for (List<String> transaction : transactions) {
            FPNode prev = root;
            Map<String, FPNode> children = prev.children;

            for (String itemName : transaction) {
                
                if (!headerTable.containsKey(itemName)) continue;

                FPNode t;
                FPNode cc = children.get(itemName);
                if (cc != null) {
                    cc.support++;
                    t = cc;
                } else {
                    t = new FPNode(itemName);
                    t.parent = prev;
                    children.put(itemName, t);

                    
                    FPNode header = headerTable.get(itemName);
                    if (header != null) {
                        header.attach(t);
                    }
                }
                prev = t;
                children = t.children;
            }
        }

        return root;

    }

    private static boolean isSingleBranch(FPNode root) {
        boolean rect = true;
        while (!root.children.isEmpty()) {
            if (root.children.size() > 1) {
                rect = false;
                break;
            }
            String childName = root.children.keySet().iterator().next();
            root = root.children.get(childName);
        }
        return rect;
    }


    private static HashMap<String, Integer> count(List<List<String>> transactions) {
        HashMap<String, Integer> itemCount = new HashMap<>();
        for (List<String> transaction : transactions) {
            for (String item : transaction) {
                itemCount.compute(item, (i, v) -> (v == null) ? 1 : v + 1);
            }
        }

        return itemCount;
    }

    /**
     * Load census data
     *
     * @param filename
     * @return
     * @throws IOException
     */
    private static List<List<String>> load(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<List<String>> transactions = new ArrayList<>();

        
        Pattern pattern = Pattern.compile("gain=\\w*|loss=\\w*");

        String newline;
        while ((newline = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(newline);
            newline = matcher.replaceAll("");
            newline = newline.replaceAll("( )+", " "); 
            String[] items = newline.split(" ");
            transactions.add(new ArrayList<>(Arrays.asList(items)));
        }
        br.close();

        return transactions;
    }


    /**
     * For test, print headers
     *
     * @param headers
     */
    private static void testHeadTable(HashMap<String, FPNode> headers) {
        if (headers == null) return;
        for (Entry<String, FPNode> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            int supp = headers.get(headerName).support;
            StringBuilder buff = new StringBuilder();
            FPNode currPointer = entry.getValue().next;
            while (currPointer != null) {
                buff.append(currPointer.itemName).append('(').append(currPointer.support).append(")---->");
                currPointer = currPointer.next;
            }

            System.out.println(headerName + '(' + supp + ") : " + buff);
        }
    }

    /**
     * test only
     *
     * @param minLength
     */
    private void print(int minLength) {
        float count = freq.size();
        stream().forEach(entry -> {
            List<String> rule = entry.getKey();
            if (rule.size() < minLength)
                return;
            int support = entry.getValue();
            float supportPct = entry.getValue() / count;
            System.out.println(n4(supportPct) + '\t' + Arrays.toString(rule.toArray()));
        });
    }

    private Stream<Entry<List<String>, Integer>> stream() {
        return freq.entrySet().stream().sorted((e1, e2) -> {
            
            int i = Integer.compare(e2.getValue(), e1.getValue());
            if (i == 0) {
                int c1 = e1.getKey().size();
                int c2 = e2.getKey().size();
                return Integer.compare(c1, c2);
            }
            return i;
        });
    }


    public static void main(String[] args) throws IOException {
        FPGrow model = new FPGrow();

        model.grow(FPGrow.load("/home/me/Downloads/FPGrowth-master/data/census-sample20.dat"));

        /*
        example file:

        age=middle-aged workclass=State-gov education=Bachelors edu_num=13 marital=Never-married occupation=Adm-clerical relationship=Not-in-family race=White sex=Male gain=medium loss=none hours=full-time country=United-States salary<=50K
        age=senior workclass=Self-emp-not-inc education=Bachelors edu_num=13 marital=Married-civ-spouse occupation=Exec-managerial relationship=Husband race=White sex=Male gain=none loss=none hours=half-time country=United-States salary<=50K
        age=middle-aged workclass=Private education=HS-grad edu_num=9 marital=Divorced occupation=Handlers-cleaners relationship=Not-in-family race=White sex=Male gain=none loss=none hours=full-time country=United-States salary<=50K
        age=senior workclass=Private education=11th edu_num=7 marital=Married-civ-spouse occupation=Handlers-cleaners relationship=Husband race=Black sex=Male gain=none loss=none hours=full-time country=United-States salary<=50K
        ...
        */

        
        model.print(2);
    }

    public static class FPNode {

        int support;
        String itemName;
        final Map<String, FPNode> children = new HashMap();
        FPNode next; 
        FPNode parent;

        public FPNode(String name) {
            this.itemName = name;
            this.support = 1;
            this.next = null;
            this.parent = null;
        }

        @Override
        public String toString() {
            return "FPNode [support=" + support + ", itemName=" + itemName + ']';
        }

        public void attach(FPNode t) {
            FPNode node = this;
            while (node.next != null) {
                node = node.next;
            }
            node.next = t;
        }


    }
}