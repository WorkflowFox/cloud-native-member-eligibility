export type EligibilityStatus =
  | 'ELIGIBLE'
  | 'NOT_YET_ELIGIBLE'
  | 'INELIGIBLE'
  | 'MEMBER_NOT_FOUND';

/** ISO 8601 calendar date, e.g. "2026-08-24". */
export type IsoDate = string;

export interface EligibilityRequest {
  memberId: string;
  checkDate: IsoDate;
}

export interface EligibilityResult {
  memberId: string;
  memberName: string | null;
  planName: string | null;
  coverageEffectiveDate: IsoDate | null;
  coverageTerminationDate: IsoDate | null;
  checkCoverageOnDate: IsoDate;
  eligibilityStatus: EligibilityStatus;
  eligibilityReason: string;
}
