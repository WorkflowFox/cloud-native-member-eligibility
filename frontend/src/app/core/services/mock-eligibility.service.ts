import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay, switchMap } from 'rxjs/operators';
import { formatReadableDate } from '../date-utils';
import {
  EligibilityRequest,
  EligibilityResult,
  EligibilityStatus,
  IsoDate,
} from '../models/eligibility.model';
import { EligibilityService } from './eligibility.service';

interface SeedMember {
  name: string;
  planName: string;
  effectiveDate: IsoDate;
  terminationDate: IsoDate | null;
}

/**
 * Deterministic synthetic dataset per spec §8.3. `M-9999` is intentionally
 * absent to demonstrate MEMBER_NOT_FOUND.
 */
const SEED_MEMBERS: Readonly<Record<string, SeedMember>> = {
  'M-1001': {
    name: 'Jordan Testcase',
    planName: 'Acme Health Plan',
    effectiveDate: '2025-01-01',
    terminationDate: null,
  },
  'M-1002': {
    name: 'Riley Sampleton',
    planName: 'Northwind Choice PPO',
    effectiveDate: '2027-02-01',
    terminationDate: null,
  },
  'M-1003': {
    name: 'Avery Placeholder',
    planName: 'Beacon Standard HMO',
    effectiveDate: '2023-03-01',
    terminationDate: '2025-12-31',
  },
};

/**
 * Stage 2 stand-in for the Spring Boot eligibility endpoint. Implements the
 * same rules the backend will own (spec §9) purely so every business outcome,
 * plus loading and failure states, are reachable before the API exists. This
 * service is replaced by an HTTP implementation in Stage 5 without any UI
 * component changes, since callers only depend on `EligibilityService`.
 */
@Injectable()
export class MockEligibilityService extends EligibilityService {
  /** Simulated network latency in milliseconds; adjustable for demo purposes. */
  latencyMs = 900;
  /** When true, the next request simulates a technical/network failure instead of a business result. */
  forceError = false;

  override checkEligibility(request: EligibilityRequest): Observable<EligibilityResult> {
    return of(null).pipe(
      delay(this.latencyMs),
      switchMap(() =>
        this.forceError
          ? throwError(() => new Error('Simulated eligibility service failure'))
          : of(this.evaluate(request))
      )
    );
  }

  private evaluate(request: EligibilityRequest): EligibilityResult {
    const member = SEED_MEMBERS[request.memberId];

    if (!member) {
      return {
        memberId: request.memberId,
        memberName: null,
        planName: null,
        coverageEffectiveDate: null,
        coverageTerminationDate: null,
        checkCoverageOnDate: request.checkDate,
        eligibilityStatus: 'MEMBER_NOT_FOUND',
        eligibilityReason: `No member matches ID ${request.memberId}. Confirm the member ID and check again.`,
      };
    }

    let status: EligibilityStatus;
    let reason: string;

    if (request.checkDate < member.effectiveDate) {
      status = 'NOT_YET_ELIGIBLE';
      reason = `Coverage does not begin until ${formatReadableDate(member.effectiveDate)}, which is after ${formatReadableDate(request.checkDate)}.`;
    } else if (member.terminationDate && request.checkDate > member.terminationDate) {
      status = 'INELIGIBLE';
      reason = `Coverage ended on ${formatReadableDate(member.terminationDate)}, which is before ${formatReadableDate(request.checkDate)}.`;
    } else {
      status = 'ELIGIBLE';
      reason = `Coverage is active on ${formatReadableDate(request.checkDate)}.`;
    }

    return {
      memberId: request.memberId,
      memberName: member.name,
      planName: member.planName,
      coverageEffectiveDate: member.effectiveDate,
      coverageTerminationDate: member.terminationDate,
      checkCoverageOnDate: request.checkDate,
      eligibilityStatus: status,
      eligibilityReason: reason,
    };
  }
}
