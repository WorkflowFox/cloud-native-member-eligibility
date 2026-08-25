import { Observable } from 'rxjs';
import { EligibilityRequest, EligibilityResult } from '../models/eligibility.model';

/**
 * Contract for retrieving an eligibility decision. Components depend on this
 * abstraction only; the Stage 2 mock and the future HTTP implementation are
 * swapped in purely through DI configuration (see app.config.ts).
 */
export abstract class EligibilityService {
  abstract checkEligibility(request: EligibilityRequest): Observable<EligibilityResult>;
}
