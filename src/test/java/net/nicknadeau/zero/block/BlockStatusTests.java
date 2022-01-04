package net.nicknadeau.zero.block;

import org.junit.Assert;
import org.junit.Test;

public class BlockStatusTests {

    @Test
    public void testFromNonExistentInt() {
        Assert.assertNull(BlockStatus.fromInt(-1));
    }

    @Test
    public void testToAndFromInt() {
        for (BlockStatus status : BlockStatus.values()) {
            Assert.assertEquals(status, BlockStatus.fromInt(status.toInt()));
        }
    }
}
