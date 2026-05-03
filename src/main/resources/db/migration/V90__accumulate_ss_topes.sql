-- V90__accumulate_ss_topes.sql
--
-- P_TOPE_MAX and P_TOPE_MIN must accumulate across segments so that multi-segment
-- employees (e.g. two half-month periods) end up with the correct prorated total.
-- Each segment contributes base * daysInSegment / daysInPeriod (MENSUAL) or
-- base * daysInSegment (DIARIO), and the sum is used by B_CC_MAX/B_CC in the aggregate plan.

update payroll_engine.payroll_concept pc
set    result_composition_mode = 'ACCUMULATE',
       updated_at               = current_timestamp
from   payroll_engine.payroll_object po
where  pc.object_id = po.id
  and  po.rule_system_code = 'ESP'
  and  po.object_code in ('P_TOPE_MAX', 'P_TOPE_MIN');
