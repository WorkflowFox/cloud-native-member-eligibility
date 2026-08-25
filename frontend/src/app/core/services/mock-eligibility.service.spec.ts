import { MockEligibilityService } from './mock-eligibility.service';
import { EligibilityResult } from '../models/eligibility.model';

function flush(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('MockEligibilityService', () => {
  let service: MockEligibilityService;

  beforeEach(() => {
    service = new MockEligibilityService();
    service.latencyMs = 0;
  });

  it('returns ELIGIBLE on the coverage effective date (inclusive boundary)', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-1001', checkDate: '2025-01-01' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('ELIGIBLE');
  });

  it('returns NOT_YET_ELIGIBLE the day before the effective date', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-1002', checkDate: '2027-01-31' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('NOT_YET_ELIGIBLE');
  });

  it('returns ELIGIBLE on the coverage termination date (inclusive boundary)', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-1003', checkDate: '2025-12-31' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('ELIGIBLE');
  });

  it('returns INELIGIBLE the day after the termination date', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-1003', checkDate: '2026-01-01' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('INELIGIBLE');
  });

  it('treats a null termination date as coverage with no recorded end date', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-1001', checkDate: '2099-01-01' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('ELIGIBLE');
  });

  it('returns MEMBER_NOT_FOUND with null member/plan/coverage fields for an unknown member', async () => {
    let result: EligibilityResult | undefined;
    service.checkEligibility({ memberId: 'M-9999', checkDate: '2026-01-01' }).subscribe((r) => (result = r));
    await flush();
    expect(result?.eligibilityStatus).toBe('MEMBER_NOT_FOUND');
    expect(result?.memberId).toBe('M-9999');
    expect(result?.memberName).toBeNull();
    expect(result?.planName).toBeNull();
    expect(result?.coverageEffectiveDate).toBeNull();
    expect(result?.coverageTerminationDate).toBeNull();
  });

  it('errors when forceError is set, simulating a technical failure', async () => {
    service.forceError = true;
    let error: unknown;
    service.checkEligibility({ memberId: 'M-1001', checkDate: '2025-01-01' }).subscribe({
      error: (e) => (error = e),
    });
    await flush();
    expect(error).toBeInstanceOf(Error);
  });
});
