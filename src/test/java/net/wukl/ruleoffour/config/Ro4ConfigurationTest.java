package net.wukl.ruleoffour.config;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class Ro4ConfigurationTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(Ro4Configuration.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
