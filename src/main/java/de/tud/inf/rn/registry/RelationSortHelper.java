package de.tud.inf.rn.registry;

import de.tud.inf.rn.db.orm.Relation;

import java.util.Comparator;

/**
 * Created by nguonly on 7/30/15.
 */
public class RelationSortHelper {
    public static Comparator<Relation> SEQUENCE_DESC = (s1, s2) -> Long.compare(s2.sequence, s1.sequence);

    public static Comparator<Relation> TYPE_DESC = (s1, s2) -> Long.compare(s2.type, s1.type);
}
