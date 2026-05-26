package org.gridsuite.filter.wip.expert.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class SimpleExpertRuleTest {

    @Test
    void testSimpleFilterRuleReturnsExpectedOperatorType() {
        SimpleFilterRule simpleFilterRule = new SimpleFilterRule();

        assertThat(simpleFilterRule.getOperatorType()).isEqualTo(OperatorType.EXISTS);
    }

    @Test
    void testSimpleFilterRuleReturnsExpectedDataType() {
        SimpleFilterRule simpleFilterRule = new SimpleFilterRule();

        assertThat(simpleFilterRule.getDataType()).isEqualTo(DataType.FILTER);
    }

    @Test
    void testSimpleFilterRuleEvaluationReturnsTrue() {
        SimpleFilterRule simpleFilterRule = new SimpleFilterRule();

        assertThat(simpleFilterRule.evaluateRule(null)).isTrue();
    }

    @Test
    void testUnsupportedOperationExceptionIsExpected() {
        SimpleFilterRule simpleFilterRule = new SimpleFilterRule();

        PowsyblException actualException = simpleFilterRule.unsupportedOperatorException();

        assertThat(actualException).isInstanceOf(PowsyblException.class)
                .hasMessage("EXISTS operator not supported with FILTER rule data type");
    }

    private static final class SimpleFilterRule extends AbstractExpertRule {

        @Override
        protected OperatorType getOperatorType() {
            return OperatorType.EXISTS;
        }

        @Override
        public boolean evaluateRule(Identifiable<?> identifiable) {
            return true;
        }

        @Override
        public DataType getDataType() {
            return DataType.FILTER;
        }
    }

}
