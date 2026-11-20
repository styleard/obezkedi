package com.example.obezkedi;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** İstemci tarafında hangi kedilerin obez olduğunu tutar (entity id). */
public class ObezDurum {
    public static final Set<Integer> OBEZ = ConcurrentHashMap.newKeySet();
}
