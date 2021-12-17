package jcog.net.attn;

/**
 * Created by me on 5/2/17.
 */
public interface TagSet {

    /** identifier of what this represents; its mode */
    String id();

    /** returns if changed or not; if unsure, return true */
    boolean pri(String tag, float pri);

    float pri(String tag);

    void clear();


}
