package dk.dma.msinm.common;

import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Unit tests for the Sequence
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestDatabaseConfiguration.class, SqlProducer.class, Sequences.class,
        LogConfiguration.class, EntityManager.class
})
public class SequenceTest extends MsiNmUnitTest {

    @Inject
    Sequences sequences;

    public static final Sequence TEST_SEQUENCE = new DefaultSequence("test-seq-id", 1);


    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class
        );
    }

    @Test
    public void sequenceTest() {

        // Test that the first value is "1"
        Assert.assertEquals(1L, sequences.getNextValue(TEST_SEQUENCE));

        // Test that the next value is "2"
        Assert.assertEquals(2L, sequences.getNextValue(TEST_SEQUENCE));

    }

}
