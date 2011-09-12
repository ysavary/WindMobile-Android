package ch.windmobile;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;

/*
 * From https://github.com/thest1/LazyList/
 */
public class MemoryCache {
    private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

    public Bitmap get(String id) {
        if (!cache.containsKey(id))
            return null;
        SoftReference<Bitmap> ref = cache.get(id);
        return ref.get();
    }

    public void put(String id, Bitmap bitmap) {
        cache.put(id, new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {
        cache.clear();
    }
}