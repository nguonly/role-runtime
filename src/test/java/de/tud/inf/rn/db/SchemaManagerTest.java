package de.tud.inf.rn.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nguonly role 7/9/15.
 */
public class SchemaManagerTest {

    @Before
    public void beforeEachTest(){
        SchemaManager.drop();
    }

    @Test
    public void create(){
        Assert.assertTrue(SchemaManager.create());
    }
}
